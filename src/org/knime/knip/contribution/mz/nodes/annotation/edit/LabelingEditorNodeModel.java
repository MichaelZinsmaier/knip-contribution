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

import java.util.List;
import java.util.Map;

import net.imglib2.labeling.Labeling;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.NodeUtils;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.core.types.NativeTypes;
import org.knime.knip.core.ui.imgviewer.annotator.RowColKey;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class LabelingEditorNodeModel<L extends Comparable<L>>
		extends ValueToCellNodeModel<LabelingValue<L>, LabelingCell<String>>
		implements BufferedDataTableHolder {

	static String LABEL_SETTINGS_KEY = "editedLabels";

	static SettingsModelLabelAnnotator createAnnotatorSM() {
		return new SettingsModelLabelAnnotator(LABEL_SETTINGS_KEY);
	}
	private SettingsModelLabelAnnotator m_annotationsSM = createAnnotatorSM();

	private LabelingCellFactory m_labelingCellFactory;
	
	private DataRow m_currentRow;
	
	
	@Override
	protected void addSettingsModels(List<SettingsModel> settingsModels) {
		settingsModels.add(m_annotationsSM);
	}
	
	@Override
	protected void prepareExecute(ExecutionContext exec) {
		m_labelingCellFactory = new LabelingCellFactory(exec);
	}

	protected void computeDataRow(final DataRow row) {
		m_currentRow = row;
	}
	
	@Override
	protected LabelingCell<String> compute(LabelingValue<L> cellValue)
			throws Exception {

		Map<RowColKey, Labeling<String>> labelMap = m_annotationsSM
				.getAnnotationMap();
		
		// calculate key
		String rowName = m_currentRow.getKey().getString();
	
		final Labeling<String> outLabel = labelMap.get(new RowColKey(rowName, LabelAnnotatorView.FIXED_COL));

		if ((outLabel != null)) {
			return m_labelingCellFactory.createCell(outLabel,cellValue.getLabelingMetadata());
		} else {
			// => missing cell
			return null;
		}
	}
}
