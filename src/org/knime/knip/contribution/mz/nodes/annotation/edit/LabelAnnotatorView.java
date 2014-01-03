package org.knime.knip.contribution.mz.nodes.annotation.edit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.img.BinaryOperationAssignment;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import org.knime.core.data.DataCell;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.contribution.mz.nodes.annotation.edit.ops.CopyLabeling;
import org.knime.knip.contribution.mz.nodes.annotation.edit.ops.LabelingAddManipulationOp;
import org.knime.knip.contribution.mz.nodes.annotation.edit.ops.LabelingRemoveManipulationOp;
import org.knime.knip.core.types.ImgFactoryTypes;
import org.knime.knip.core.types.NativeTypes;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ImgViewer;
import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorMinimapPanel;
import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorToolbar;
import org.knime.knip.core.ui.imgviewer.annotator.RowColKey;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorRowColKeyChgEvent;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorFreeFormTool;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorRectangleTool;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelingWithMetadataChgEvent;
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
import org.knime.knip.io.nodes.annotation.AbstractDefaultAnnotatorView;
import org.knime.knip.io.nodes.annotation.AnnotatorView;
import org.knime.knip.io.nodes.annotation.deprecated.AnnotatorImgCanvas;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class LabelAnnotatorView<T extends RealType<T> & NativeType<T>> extends AbstractDefaultAnnotatorView<Labeling<String>>
		implements AnnotatorView<Labeling<String>> {

	private OverlayLiveAnnotationManager<T> m_liveManager = new OverlayLiveAnnotationManager<T>();

	private EventService m_eventService;
	
	private HashMap<RowColKey, Labeling<String>> m_alteredLabelings = new HashMap<RowColKey, Labeling<String>>();

	private RowColKey m_currentKey;

	private Labeling<String> m_currentLabeling;
	
	private LabelingCell<String> m_currentCell;

	private boolean m_isAddMode = true;
	
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
		
		m_alteredLabelings.put(key, annotation);
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
		annotator.addViewerComponent(new EditAnnotatorModeSelectionPanel());
		annotator.addViewerComponent(new AnnotatorToolbar(new AnnotatorRectangleTool(), new AnnotatorFreeFormTool()));
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
		
		ImgPlusCell<T> imgPlusCell = null;
		
		for (DataCell c : currentRow) {
			if (c.isMissing()) {
				return;
			}
		}
		
		
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
			//create an array img based copy of the labeling
			Labeling<String> inputLabeling = m_currentCell.getLabeling();
		
			long[] dims = new long[inputLabeling.numDimensions()];
			inputLabeling.dimensions(dims);
				
			NativeImgFactory<?> imgFac = (NativeImgFactory<?>) ImgFactoryTypes.getImgFactory(ImgFactoryTypes.ARRAY_IMG_FACTORY);
			NativeImgLabeling<String, IntType> copiedLabeling;
			try {
				copiedLabeling = new NativeImgLabeling<String, IntType>(imgFac.imgFactory(new IntType())
				        .create(dims, new IntType()));
						
				CopyLabeling<String> copy = new CopyLabeling<String>();
				copy.compute(inputLabeling, copiedLabeling);

				//and set the current labeling
				m_currentLabeling = copiedLabeling;
			} catch (IncompatibleTypeException e) {
				e.printStackTrace();
			}
		}
		
		m_currentKey = key;

		m_eventService.publish(new LabelingWithMetadataChgEvent<String>(m_currentLabeling, m_currentCell.getLabelingMetadata()));
		
		m_eventService.publish(new AnnotatorRowColKeyChgEvent(key));
		m_eventService.publish(new ImgRedrawEvent());		
	}
	
	@Override
	protected EventService getEventService() {
		return m_eventService;
	}

	
	@EventListener
	public void editModeChanged(EditAnnotatorModeEvent e) {
		m_isAddMode  = e.isAddMode();
	}
	
	@EventListener
	public void elementFinished(OverlayElementFinishedEvent e) {
		NativeImgFactory<?> factory = (NativeImgFactory<?>) ImgFactoryTypes.getImgFactory(ImgFactoryTypes.ARRAY_IMG_FACTORY);

		IntType emptyLabel = new IntType(0);
		
		final Labeling<String> labelingNew = e.getOverlay().renderSegmentationImage(
				factory, false, NativeTypes.INTTYPE);

		Labeling<String> result;
		if (m_isAddMode) {
			BinaryOperationAssignment<LabelingType<String>, LabelingType<String>, LabelingType<String>> add = 
					new BinaryOperationAssignment<LabelingType<String>, LabelingType<String>, LabelingType<String>>(new LabelingAddManipulationOp<String>(emptyLabel));
			 result = (Labeling<String>) add.compute(m_currentLabeling, labelingNew, m_currentLabeling);
		} else {
			BinaryOperationAssignment<LabelingType<String>, LabelingType<String>, LabelingType<String>> remove = 
					new BinaryOperationAssignment<LabelingType<String>, LabelingType<String>, LabelingType<String>>(new LabelingRemoveManipulationOp<String>(emptyLabel));
			 result = (Labeling<String>) remove.compute(m_currentLabeling, labelingNew, m_currentLabeling);
		}
		
		m_alteredLabelings.put(m_currentKey, result);
		m_currentLabeling = result;
		
		m_eventService.publish(new LabelingWithMetadataChgEvent<String>(m_currentLabeling, m_currentCell.getLabelingMetadata()));
	}

	
}
