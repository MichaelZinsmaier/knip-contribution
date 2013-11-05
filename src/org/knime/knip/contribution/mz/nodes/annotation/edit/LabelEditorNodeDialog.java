/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * --------------------------------------------------------------------- *
 *
 */
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
import org.knime.knip.base.node.NodeTools;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.base.node.dialog.DataAwareDefaultNodeSettingsPane;
import org.knime.knip.core.types.NativeTypes;
import org.knime.knip.core.util.EnumListProvider;
import org.knime.knip.io.nodes.annotation.DialogComponentAnnotatorView;

/**
 * TODO Auto-generated
 * 
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
 *         Zinsmaier</a>
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

		createNewGroup("");
		addDialogComponent(new DialogComponentColumnFilter(
				ValueToCellNodeModel.createColumnSelectionModel(), 0, true,
				ImgPlusValue.class));
		closeCurrentGroup();

		// label settings

		createNewTab("Label Settings");
		setHorizontalPlacement(true);
		createNewGroup("Options");

		addDialogComponent(new DialogComponentStringSelection(
				LabelEditorNodeModel.createLabelingTypeSM(),
				"Storage Img Type", EnumListProvider.getStringList(NativeTypes
						.intTypeValues())));

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
		int firstImage = NodeTools.firstCompatibleColumn(inSpec,
				ImgPlusValue.class);
		int firstLabel = NodeTools.firstCompatibleColumn(inSpec,
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

		if (NodeTools.firstCompatibleColumn(inSpec, ImgPlusValue.class,
				firstImage) != -1
				|| NodeTools.firstCompatibleColumn(inSpec, LabelingValue.class,
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
