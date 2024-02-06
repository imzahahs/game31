package sengine.animation;

import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;
import sengine.materials.ColorAttribute;

public class FadeAnim extends Animation implements MassSerializable {

	private final Graph alphaGraph;
	private final int layer;

    public FadeAnim(float alpha) {
        this(alpha, 0);
    }

    public FadeAnim(float alpha, int layer) {
        this(1f, new ConstantGraph(alpha), layer);
    }

    public FadeAnim(Graph alphaGraph) {
        this(1f, alphaGraph, 0);
    }

    public FadeAnim(float length, Graph alphaGraph) {
        this(length, alphaGraph, 0);
    }

	@MassConstructor
	public FadeAnim(float length, Graph alphaGraph, int layer) {
		super(length);
		
		this.alphaGraph = alphaGraph;
        this.layer = layer;
	}

	@Override
	public Object[] mass() {
		return new Object[] { length, alphaGraph, layer };
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		ColorAttribute color = a.getAttribute(ColorAttribute.class, layer);
		if(color == null)
			return;		// Color attribute not supported
		color.current.a *= alphaGraph.generate(progress); 
	}
}
