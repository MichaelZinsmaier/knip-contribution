package org.knime.knip.contribution.mz.nodes.annotation.edit;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.labeling.Labeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.base.data.filter.column.FilterColumnTable;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.NodeUtils;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.base.node.dialog.DataAwareDefaultNodeSettingsPane;
import org.knime.knip.core.types.NativeTypes;
import org.knime.knip.core.util.EnumUtils;
import org.knime.knip.io.nodes.annotation.DialogComponentAnnotatorView;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class LabelEditorNodeDialog<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> extends
		DataAwareDefaultNodeSettingsPane {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(LabelEditorNodeDialog.class);
	
	private static final String APPEND_DEFAULT = "_Label";

	private DialogComponentAnnotatorView<Labeling<String>> m_dialogComponentAnnotator;
	private SettingsModelString m_smColCreationMode = ValueToCellNodeModel
			.createColCreationModeModel();
	private SettingsModelString m_smColumnSuffix = ValueToCellNodeModel
			.createColSuffixNodeModel();

	@SuppressWarnings("unchecked")
	public LabelEditorNodeDialog() {
		super();

		removeTab("Options");
		createNewTab("Selection");
		createNewGroup("Image Annotation");

		SettingsModelLabelAnnotator annotatorSM = LabelEditorNodeModel
				.createAnnotatorSM();
		m_dialogComponentAnnotator = new DialogComponentAnnotatorView<Labeling<String>>(
				new LabelAnnotatorView(), annotatorSM);
		addDialogComponent(m_dialogComponentAnnotator);
		closeCurrentGroup();

		// column selection dialog component
		createNewTab("Column Selection");
		createNewGroup("Creation Mode");
		addDialogComponent(new DialogComponentStringSelection(
				m_smColCreationMode, "Column Creation Mode",
				ValueToCellNodeModel.COL_CREATION_MODES));
		closeCurrentGroup();

		createNewGroup("Column suffix");
		addDialogComponent(new DialogComponentString(m_smColumnSuffix,
				"Column suffix"));
		closeCurrentGroup();

		// add append suffix logic
		m_smColCreationMode.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (m_smColCreationMode.getStringValue().equals(
						ValueToCellNodeModel.COL_CREATION_MODES[1])) {
					// append
					if (m_smColumnSuffix.getStringValue().isEmpty()) {
						m_smColumnSuffix.setStringValue(APPEND_DEFAULT);
					}
				} else {
					if (m_smColumnSuffix.getStringValue()
							.equals(APPEND_DEFAULT)) {
						m_smColumnSuffix.setStringValue("");
					}
				}
			}
		});

	}

	@Override
	public void onClose() {
		m_dialogComponentAnnotator.reset();
	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObject[] input) throws NotConfigurableException {

		// update input data dependent
		BufferedDataTable inputTable = (BufferedDataTable) input[0];
		DataTableSpec inSpec = inputTable.getDataTableSpec();

		// get index of first image column (if available) and first label column
		// issue a warning
		// if there are more
		int firstImage = NodeUtils.firstCompatibleColumn(inSpec,
				ImgPlusValue.class);
		int firstLabel = NodeUtils.firstCompatibleColumn(inSpec,
				LabelingValue.class);

		DataTable filteredTable;
		if (firstImage != -1 && firstLabel != -1) {
			filteredTable = new FilterColumnTable(inputTable, firstImage,
					firstLabel);
		} else if (firstLabel != -1) {
			filteredTable = new FilterColumnTable(inputTable, firstLabel);
		} else {
			filteredTable = new FilterColumnTable(inputTable, new int[] {});
		}

		if (NodeUtils.firstCompatibleColumn(inSpec, ImgPlusValue.class,
				firstImage) != -1
				|| NodeUtils.firstCompatibleColumn(inSpec, LabelingValue.class,
						firstLabel) != -1) {	
			LOGGER.warn("There are multiple candidate columns. The first image/labeling column is choosen.");
		}

		m_dialogComponentAnnotator.updateDataTable(filteredTable);
	}

	/**
	 * If column creation mode is 'append', a suffix needs to be chosen!
	 */
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (m_smColCreationMode.getStringValue().equals(
				ValueToCellNodeModel.COL_CREATION_MODES[1])
				&& m_smColumnSuffix.getStringValue().trim().isEmpty()) {
			throw new InvalidSettingsException(
					"If the selected column creation mode is 'append', a column suffix for the resulting column name must to be chosen!");
		}

		super.saveAdditionalSettingsTo(settings);
	}

}
