package org.knime.knip.contribution.mz.nodes.annotation.edit;

import org.knime.knip.core.ui.event.KNIPEvent;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class EditAnnotatorModeEvent implements KNIPEvent {

	private final boolean m_addMode;

	public EditAnnotatorModeEvent(boolean add) {
		m_addMode = add;
	}
	
	public boolean isAddMode() {
		return m_addMode;
	}
	
	public boolean isRemoveMode() {
		return !m_addMode;
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
