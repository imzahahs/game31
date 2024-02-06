package sengine.animation;

import sengine.calc.Graph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public class MoveAnim extends Animation implements MassSerializable {

	final Graph xGraph;
	final Graph yGraph;

	@MassConstructor
	public MoveAnim(float length, Graph xGraph, Graph yGraph) {
		super(length);
		
		this.xGraph = xGraph;
		this.yGraph = yGraph;
	}
	@Override
	public Object[] mass() {
		return new Object[] { length, xGraph, yGraph };
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		float x = xGraph != null ? xGraph.generate(progress) : 0f;
		float y = yGraph != null ? yGraph.generate(progress) : 0f;
		a.translate(x, y);
	}
}
