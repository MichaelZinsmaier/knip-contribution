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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingFactory;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.img.BinaryOperationAssignment;
import net.imglib2.ops.img.UnaryOperationAssignment;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.img.unary.ImgCopyOperation;
import net.imglib2.ops.operation.iterableinterval.unary.IterableIntervalCopy;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.core.data.DataCell;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.exceptions.KNIPRuntimeException;
import org.knime.knip.contribution.mz.nodes.annotation.edit.ops.LabelingTypeDelete;
import org.knime.knip.contribution.mz.nodes.annotation.edit.ops.LabelingTypeMerge;
import org.knime.knip.core.awt.labelingcolortable.DefaultLabelingColorTable;
import org.knime.knip.core.data.img.DefaultLabelingMetadata;
import org.knime.knip.core.types.ImgFactoryTypes;
import org.knime.knip.core.types.NativeTypes;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ImgViewer;
import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorMinimapPanel;
import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorToolbar;
import org.knime.knip.core.ui.imgviewer.annotator.OverlayAnnotatorManager;
import org.knime.knip.core.ui.imgviewer.annotator.RowColKey;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorRowColKeyChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelingWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.OverlayChgEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.panels.ImgNormalizationPanel;
import org.knime.knip.core.ui.imgviewer.panels.PlaneSelectionPanel;
import org.knime.knip.core.ui.imgviewer.panels.RendererSelectionPanel;
import org.knime.knip.core.ui.imgviewer.panels.TransparencyPanel;
import org.knime.knip.core.ui.imgviewer.panels.infobars.LabelingViewInfoPanel;
import org.knime.knip.core.ui.imgviewer.panels.providers.AWTImageProvider;
import org.knime.knip.core.ui.imgviewer.panels.providers.CombinedRU;
import org.knime.knip.core.ui.imgviewer.panels.providers.ImageRU;
import org.knime.knip.core.ui.imgviewer.panels.providers.LabelingRU;
import org.knime.knip.core.ui.imgviewer.panels.providers.OverlayRU;
import org.knime.knip.core.util.ImgUtils;
import org.knime.knip.io.nodes.annotation.AbstractDefaultAnnotatorView;
import org.knime.knip.io.nodes.annotation.AnnotatorView;
import org.knime.knip.io.nodes.annotation.deprecated.AnnotatorImgCanvas;

/**
 * TODO Auto-generated
 * 
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
 *         Zinsmaier</a>
 */
public class LabelAnnotatorView<T extends RealType<T> & NativeType<T>> extends AbstractDefaultAnnotatorView<Labeling<String>>
		implements AnnotatorView<Labeling<String>> {

	private OverlayLiveAnnotationManager<T> m_liveManager = new OverlayLiveAnnotationManager<T>();

	private EventService m_eventService;
	
	private HashMap<RowColKey, Labeling<String>> m_alteredLabelings = new HashMap<RowColKey, Labeling<String>>();

	private RowColKey m_currentKey;

	private Labeling<String> m_currentLabeling;
	
	private LabelingCell<String> m_currentCell;
	
	public LabelAnnotatorView() {
			createAnnotator();
	}
	
	//AnnotatorView

	@Override
	public Labeling<String> getAnnotation(RowColKey key) {
		return m_alteredLabelings.get(key);
	}

	@Override
	public void setAnnotation(RowColKey key, Labeling<String> annotation) {
		// assumption labels that should be added like this come from
		// serialization => they belong to the input table
		
		//TODO
	}

	
	@Override
	public List<RowColKey> getIdentifiersOfManagedSources() {
		LinkedList<RowColKey> ret = new LinkedList<RowColKey>();
		ret.addAll(m_alteredLabelings.keySet());	
		return ret;
	}
	

	//AbstractDefaultAnnotatorView

	@Override
	protected JComponent createAnnotatorComponent() {
		ImgViewer annotator = new ImgViewer();
		annotator
				.addViewerComponent(new AWTImageProvider(0, new OverlayRU<String>(new CombinedRU(new ImageRU<T>(true), new LabelingRU<String>()))));
		annotator.addViewerComponent(m_liveManager);
		annotator.addViewerComponent(new EditAnnotatorLabelPanel());
		annotator.addViewerComponent(AnnotatorToolbar.createEditToolbar());
		annotator.addViewerComponent(new AnnotatorMinimapPanel());
		annotator.addViewerComponent(new ImgNormalizationPanel<T, Img<T>>());
		annotator.addViewerComponent(new PlaneSelectionPanel<T, Img<T>>());
		annotator.addViewerComponent(new RendererSelectionPanel<T>());
		annotator.addViewerComponent(new TransparencyPanel());
		annotator.addViewerComponent(new LabelingViewInfoPanel<String>());
		annotator.addViewerComponent(new AnnotatorImgCanvas<T>());
		m_eventService = annotator.getEventService();
		m_eventService.subscribe(this);
		
		return annotator;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void currentSelectionChanged(DataCell[] currentRow, int currentColNr, RowColKey key) {
		m_currentKey = key;
		
		ImgPlusCell<T> imgPlusCell = null;
		
		if (currentRow.length == 2) {
            // Labeling and image
			if (currentRow[0] instanceof ImgPlusValue) {
				imgPlusCell = (ImgPlusCell<T>)currentRow[0];
				m_currentCell = (LabelingCell<String>)currentRow[1];
			} else {				
				imgPlusCell = (ImgPlusCell<T>)currentRow[1];
				m_currentCell = (LabelingCell<String>)currentRow[0];
			}

			m_eventService.publish(new ImgWithMetadataChgEvent<T>(imgPlusCell.getImgPlus().getImg(), imgPlusCell.getMetadata()));		
        } else {
        	m_currentCell = (LabelingCell<String>)currentRow[0];
        }

		if (m_alteredLabelings.containsKey(key)) {
			m_currentLabeling = m_alteredLabelings.get(key);
		} else {
			//TODO COPY INTO ARRAY IMG
			m_currentLabeling = m_currentCell.getLabeling().copy();
		}

		m_eventService.publish(new LabelingWithMetadataChgEvent<String>(m_currentLabeling, m_currentCell.getLabelingMetadata()));
		
		m_eventService.publish(new AnnotatorRowColKeyChgEvent(key));
		m_eventService.publish(new ImgRedrawEvent());		
	}
	
	@Override
	protected EventService getEventService() {
		return m_eventService;
	}

	@EventListener
	public void elementFinished(OverlayElementFinishedEvent e) {
		NativeImgFactory<?> factory = (NativeImgFactory<?>) ImgFactoryTypes.getImgFactory(ImgFactoryTypes.ARRAY_IMG_FACTORY);

		final Labeling<String> labelingNew = e.getOverlay().renderSegmentationImage(
				factory, false, NativeTypes.INTTYPE);
		
		//further speed up possible:
		//one good way would be to merge only at the points where an actual change happened (i.e. all from new label)
		//alternative work directly on the native labeling?
		
		//merge
		BinaryOperationAssignment<LabelingType<String>, LabelingType<String>, LabelingType<String>> merge = 
				new BinaryOperationAssignment<LabelingType<String>, LabelingType<String>, LabelingType<String>>(new LabelingTypeMerge<String>());

		//delete
		BinaryOperationAssignment<LabelingType<String>, LabelingType<String>, LabelingType<String>> delete = 
				new BinaryOperationAssignment<LabelingType<String>, LabelingType<String>, LabelingType<String>>(new LabelingTypeDelete<String>());

		
		Labeling<String> result = (Labeling<String>) merge.compute(m_currentLabeling, labelingNew, m_currentLabeling);
		
		m_alteredLabelings.put(m_currentKey, result);
		m_currentLabeling = result;
		
		m_eventService.publish(new LabelingWithMetadataChgEvent<String>(m_currentLabeling, m_currentCell.getLabelingMetadata()));
	}

	
}
