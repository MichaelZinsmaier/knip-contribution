package org.knime.knip.contribution.mz.nodes.annotation.edit.ops;

import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.BinaryOperation;

public class LabelingTypeDelete<L extends Comparable<L>> implements
		BinaryOperation<LabelingType<L>, LabelingType<L>, LabelingType<L>> {

	@Override
	public LabelingType<L> compute(LabelingType<L> input1,
			LabelingType<L> deleteMask, LabelingType<L> output) {

		if (deleteMask.getLabeling().isEmpty()) {
			output.setLabeling(input1.getLabeling());
		}

		return output;
	}

	@Override
	public BinaryOperation<LabelingType<L>, LabelingType<L>, LabelingType<L>> copy() {
		return new LabelingTypeDelete<L>();
	}

}
