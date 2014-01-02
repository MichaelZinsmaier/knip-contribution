package org.knime.knip.contribution.mz.nodes.annotation.edit.ops;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.BinaryOperation;

public class LabelingTypeMerge<L extends Comparable<L>> implements
		BinaryOperation<LabelingType<L>, LabelingType<L>, LabelingType<L>> {

	@Override
	public LabelingType<L> compute(LabelingType<L> input1,
			LabelingType<L> input2, LabelingType<L> output) {

		if (input2.getIndex().getInteger() != 0) {
				List<L> labelings = new ArrayList<L>(input1.getLabeling());
				for (L label : input2.getLabeling()) {
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
		return new LabelingTypeMerge<L>();
	}

}
