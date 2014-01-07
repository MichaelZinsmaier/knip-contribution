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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.labelingcolortable.RandomMissingColorHandler;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorTool;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorLabelsColResetEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorLabelsSelChgEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorResetEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorRowColKeyChgEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorToolChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseDraggedEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMousePressedEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseReleasedEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.OverlayChgEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElement2D;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.panels.HiddenViewerComponent;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class OverlayLiveAnnotationManager<T extends RealType<T>> extends
		HiddenViewerComponent {

	/** default serial id */
	private static final long serialVersionUID = 1L;

	private String[] m_selectedLabels;

	private PlaneSelectionEvent m_sel;

	/* Are not serialized or calculated from serzalization values */
	private EventService m_eventService;

	private Overlay m_currentOverlay;

	private AnnotatorTool<?> m_currentTool;

	private RandomAccessibleInterval<?> m_src;

	public OverlayLiveAnnotationManager() {
		m_selectedLabels = new String[] { "Unknown" };
	}

	@Override
	public void setEventService(final EventService eventService) {
		m_eventService = eventService;
		eventService.subscribe(this);
	}

	@EventListener
	public void onLabelsColorReset(final AnnotatorLabelsColResetEvent e) {
		for (final String label : e.getLabels()) {
			RandomMissingColorHandler.resetColor(label);
		}
		m_eventService.publish(new ImgRedrawEvent());
	}

	@EventListener
	public void onSelectedLabelsChg(final AnnotatorLabelsSelChgEvent e) {
		m_selectedLabels = e.getLabels();
	}

	@EventListener
	public void onToolChange(final AnnotatorToolChgEvent e) {
		if (m_currentTool != null) {
			m_currentTool.fireFocusLost(m_currentOverlay);
		}

		m_currentTool = e.getTool();
	}

	/**
	 * @param axes
	 */
	@EventListener
	public void onUpdate(final IntervalWithMetadataChgEvent<?, ?> e) {
		final long[] dims = new long[e.getRandomAccessibleInterval()
				.numDimensions()];
		e.getRandomAccessibleInterval().dimensions(dims);
		m_src = e.getRandomAccessibleInterval();

		if ((m_sel == null) || !isInsideDims(m_sel.getPlanePos(), dims)) {
			m_sel = new PlaneSelectionEvent(0, 1, new long[e
					.getRandomAccessibleInterval().numDimensions()]);
		}
	}

	@EventListener
	public void onCellChange(final AnnotatorRowColKeyChgEvent e) {
		emptyOverlay();
	}

	@EventListener
	public void onUpdate(final PlaneSelectionEvent sel) {
		m_sel = sel;
	}


	private boolean isInsideDims(final long[] planePos, final long[] dims) {
		if (planePos.length != dims.length) {
			return false;
		}

		for (int d = 0; d < planePos.length; d++) {
			if (planePos[d] >= dims[d]) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Handling mouse events
	 */

	@EventListener
	public void onMousePressed(final ImgViewerMousePressedEvent e) {
		if ((m_currentOverlay != null) && (m_currentTool != null)) {
			m_currentTool.onMousePressed(e, m_sel, m_currentOverlay,
					m_selectedLabels);
		}
	}

	@EventListener
	public void onMouseDragged(final ImgViewerMouseDraggedEvent e) {
		if ((m_currentOverlay != null) && (m_currentTool != null)) {
			m_currentTool.onMouseDragged(e, m_sel, m_currentOverlay,
					m_selectedLabels);
		}
	}

	@EventListener
	public void onMouseReleased(final ImgViewerMouseReleasedEvent e) {
		if ((m_currentOverlay != null) && (m_currentTool != null)) {
			if (e.getClickCount() > 1) {
				m_currentTool.onMouseDoubleClick(e, m_sel, m_currentOverlay,
						m_selectedLabels);
			} else {
				m_currentTool.onMouseReleased(e, m_sel, m_currentOverlay,
					m_selectedLabels);
				testIfDone();
			}
		}
	}

	public void testIfDone() {
		if (m_currentOverlay.getElements().length > 0) {
			boolean done = true;

			for (OverlayElement2D el : m_currentOverlay.getElements()) {
				OverlayElementStatus status = el.getStatus();
				done &= (status.equals(OverlayElementStatus.ACTIVE) || status.equals(OverlayElementStatus.IDLE));
			}
			if (done) {
				m_eventService.publish(new OverlayElementFinishedEvent(m_currentOverlay));
				emptyOverlay();
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@EventListener
	public void reset(final AnnotatorResetEvent e) {
		m_currentOverlay = null;
		m_src = null;
		m_selectedLabels = new String[] { "Unknown" };
	}

	private void emptyOverlay() {
		m_currentOverlay = new Overlay(m_src);
		m_currentOverlay.setEventService(m_eventService);
		m_eventService.publish(new OverlayChgEvent(m_currentOverlay));
	}
	
	@Override
	public void saveComponentConfiguration(ObjectOutput out) throws IOException {
		// nothing to do
	}

	@Override
	public void loadComponentConfiguration(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// nothing to do		
	}

}
