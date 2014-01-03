package org.knime.knip.contribution.mz.nodes.annotation.edit.ops;

import java.util.ArrayList;
import java.util.List;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.IntegerType;

public class LabelingAddManipulationOp<L extends Comparable<L>> implements
		BinaryOperation<LabelingType<L>, LabelingType<L>, LabelingType<L>> {

	private final IntegerType<?> m_emptyIndex;

	public LabelingAddManipulationOp(IntegerType<?> emptyIndex) {
		m_emptyIndex = emptyIndex;
	}
	
	@Override
	public LabelingType<L> compute(LabelingType<L> input,
			LabelingType<L> addInput, LabelingType<L> output) {

		if (!addInput.getIndex().equals(m_emptyIndex)) {
				List<L> labelings = new ArrayList<L>(input.getLabeling());
				for (L label : addInput.getLabeling()) {
					if (!labelings.contains(label)) {
						labelings.add(label);
					}
				}
				output.setLabeling(labelings);
		}
		
		return output;
	}

	@Override
	public BinaryOperation<LabelingType<L>, LabelingType<L>, LabelingType<L>> copy() {
		return new LabelingAddManipulationOp<L>(m_emptyIndex);
	}

}
