package sengine.calc;

import sengine.mass.MassSerializable;

public class CompoundGraph extends Graph implements MassSerializable {
	
	final Graph graphs[];
	final float  milestones[];
	final float length;
	final float lengthHalf;
	final float start;
	final float end;
	final boolean symmetric;
	
	public CompoundGraph(Graph ... graphs) {
		this(graphs, false);
	}
	
	public CompoundGraph(boolean symmetric, Graph ... graphs) {
		this(graphs, symmetric);
	}
	
	@MassConstructor
	public CompoundGraph(Graph graphs[], boolean symmetric) {
		this.graphs = graphs;
		this.symmetric = symmetric;
		this.milestones = new float[graphs.length];
		// Calculate progress
		float length = 0;
		for(int c = 0; c < graphs.length; c++) {
			milestones[c] = graphs[c].getLength();
			length += milestones[c];
		}
		this.start = graphs[0].getStart();
		if(symmetric) {
			this.lengthHalf = length;
			this.length = length * 2;
			this.end = start;
		}
		else {
			this.lengthHalf = length;
			this.length = length;
			this.end = graphs[graphs.length - 1].getEnd();
		}
	}
	@Override
	public Object[] mass() {
		return new Object[] { graphs, symmetric };
	}

	@Override public float getStart() { return start; }
	@Override public float getEnd() { return end; }
	@Override public float getLength() { return length; }
	
	public float calculateHalf(float progress) {
		for(int c = 0; c < graphs.length; c++) {
			if(progress > milestones[c])
				progress -= milestones[c];
			else	// This is the target graph
				return graphs[c].generate(progress);
		}
		// Unexpected behavior, mathematically impossible to reach here
		return graphs[graphs.length - 1].getEnd();
	}

	@Override
	public float calculate(float progress) {
		// Seek current graph
		if(symmetric && progress > lengthHalf)
			return calculateHalf(length - progress);
		// Else normal graph
		return calculateHalf(progress);			
	}
}