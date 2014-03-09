/*Copyright (C) 2014 Michael Zinsmaier
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
*/
package org.knime.knip.contribution.mz.nodes.annotation.edit;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.labeling.Labeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.base.data.filter.column.FilterColumnTable;
import org.knime.base.data.filter.row.FilterRowGenerator;
import org.knime.base.data.filter.row.FilterRowTable;
import org.knime.base.node.preproc.filter.row.rowfilter.MissingCellRowFilter;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
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
public class LabelingEditorNodeDialog<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> extends
		DataAwareDefaultNodeSettingsPane {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(LabelingEditorNodeDialog.class);
	
	private static final String APPEND_DEFAULT = "_Label";

	private DialogComponentAnnotatorView<Labeling<String>> m_dialogComponentAnnotator;
	private SettingsModelString m_smColCreationMode = ValueToCellNodeModel
			.createColCreationModeModel();
	private SettingsModelString m_smColumnSuffix = ValueToCellNodeModel
			.createColSuffixNodeModel();

	@SuppressWarnings("unchecked")
	public LabelingEditorNodeDialog() {
		super();

		removeTab("Options");
		createNewTab("Selection");
		createNewGroup("Image Annotation");

		SettingsModelLabelAnnotator annotatorSM = LabelingEditorNodeModel
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

		m_dialogComponentAnnotator.updateDataTable(new FilterRowTable(filteredTable, new FilterMissingRows()));
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

	/**
	 * Filter all rows with missing cells from a data table
	 * @author Michael Zinsmaier
	 */
	private class FilterMissingRows implements FilterRowGenerator {

		@Override
		public boolean isIn(DataRow row) {
			boolean somethingMissing = false;
			
			for (DataCell cell : row) {
				if (cell.isMissing()) {
					somethingMissing = true;
				}
			}
			
			return !somethingMissing;
		}
	}
}
