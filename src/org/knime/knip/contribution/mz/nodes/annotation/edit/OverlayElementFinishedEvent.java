package org.knime.knip.contribution.mz.nodes.annotation.edit;

import org.knime.knip.core.ui.event.KNIPEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class OverlayElementFinishedEvent implements KNIPEvent {

	private final Overlay m_overlay;

	public OverlayElementFinishedEvent(Overlay overlay) {
		m_overlay = overlay;		
	}
	
	public Overlay getOverlay() {
		return m_overlay;
	}
	
	
	@Override
	public ExecutionPriority getExecutionOrder() {
		return ExecutionPriority.NORMAL;
	}

	@Override
	public <E extends KNIPEvent> boolean isRedundant(E thatEvent) {
		return false;
	}

}
