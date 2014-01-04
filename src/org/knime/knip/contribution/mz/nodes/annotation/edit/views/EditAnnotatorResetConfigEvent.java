package org.knime.knip.contribution.mz.nodes.annotation.edit.views;
import org.knime.knip.core.ui.event.KNIPEvent;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class EditAnnotatorResetConfigEvent implements KNIPEvent {

	public EditAnnotatorResetConfigEvent() {

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
