package org.knime.knip.contribution.mz.nodes.annotation.edit.ops;

import net.imglib2.Cursor;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.UnaryOperation;

//TODO replace this whole operation with new imgcopyoperation op

/**
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class CopyLabeling<L extends Comparable<L>> implements UnaryOperation<Labeling<L>, Labeling<L>> {

	@Override
	public Labeling<L> compute(Labeling<L> input, Labeling<L> output) {
   
		Cursor< LabelingType<L> > c1 = input.cursor();
		Cursor< LabelingType<L> > c2 = output.cursor();
    
		while ( ( c1.hasNext() && c2.hasNext() ) )
		{
            c1.fwd();
            c2.fwd();
            c2.get().set( c1.get() );
		}

		return output;
	}

	@Override
	public UnaryOperation<Labeling<L>, Labeling<L>> copy() {
		return new CopyLabeling<L>();
	}

	
}
