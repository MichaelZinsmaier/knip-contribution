package org.knime.knip.contribution.mz.nodes.annotation.edit;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.knip.base.nodes.view.TableCellViewNodeView;

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class LabelEditorNodeFactory<T extends RealType<T> & NativeType<T>, L extends Comparable<L>>
		extends NodeFactory<LabelEditorNodeModel<T>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LabelEditorNodeModel<T> createNodeModel() {
		return new LabelEditorNodeModel<T>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public NodeView<LabelEditorNodeModel<T>> createNodeView(final int i,
			final LabelEditorNodeModel<T> nodeModel) {
		return new TableCellViewNodeView(nodeModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new LabelEditorNodeDialog<T, L>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}
}
