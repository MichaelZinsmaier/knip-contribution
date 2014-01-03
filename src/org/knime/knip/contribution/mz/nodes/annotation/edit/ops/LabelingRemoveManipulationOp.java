package org.knime.knip.contribution.mz.nodes.annotation.edit.ops;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.IntegerType;

public class LabelingRemoveManipulationOp<L extends Comparable<L>> implements
		BinaryOperation<LabelingType<L>, LabelingType<L>, LabelingType<L>> {

	private final IntegerType<?> m_emptyIndex;

	public LabelingRemoveManipulationOp(IntegerType<?> emptyIndex) {
		m_emptyIndex = emptyIndex;
	}
	
	@Override
	public LabelingType<L> compute(LabelingType<L> input,
			LabelingType<L> removeInput, LabelingType<L> output) {

		if (!removeInput.getIndex().equals(m_emptyIndex)) {
			List<L> labels = new ArrayList<L>();
			List<L> rem = removeInput.getLabeling();
			
			for (L lab : input.getLabeling()) {
				if (!rem.contains(lab)) {
					labels.add(lab);
				}
			}
			
			output.setLabeling(labels);
		}

		return output;
	}

	@Override
	public BinaryOperation<LabelingType<L>, LabelingType<L>, LabelingType<L>> copy() {
		return new LabelingRemoveManipulationOp<L>(m_emptyIndex);
	}

}
