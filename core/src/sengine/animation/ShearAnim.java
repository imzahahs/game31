package sengine.animation;

import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public class ShearAnim extends Animation implements MassSerializable {

	public enum Location {
		TOP {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(0, +length);
				a.shear(shearX, shearY);
				a.translate(0, -length);
			}
		},
		BOTTOM {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(0, -length);
				a.shear(shearX, shearY);
				a.translate(0, +length);
			}
		},
		LEFT {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				a.translate(-0.5f, 0);
				a.shear(shearX, shearY);
				a.translate(+0.5f, 0);
			}
		},
		RIGHT {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				a.translate(+0.5f, 0);
				a.shear(shearX, shearY);
				a.translate(-0.5f, 0);
			}
		},
		TOPLEFT {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(-0.5f, +length);
				a.shear(shearX, shearY);
				a.translate(+0.5f, -length);
			}
		},
		TOPRIGHT {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(+0.5f, +length);
				a.shear(shearX, shearY);
				a.translate(-0.5f, -length);
			}
		},
		BOTTOMLEFT {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(-0.5f, -length);
				a.shear(shearX, shearY);
				a.translate(+0.5f, +length);
			}
		},
		BOTTOMRIGHT {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(+0.5f, -length);
				a.shear(shearX, shearY);
				a.translate(-0.5f, +length);
			}
		},
		CENTER {
			@Override
			public void apply(float shearX, float shearY, Animatable2D a) {
				a.shear(shearX, shearY);
			}
		},
		;

		public abstract void apply(float shearX, float shearY, Animatable2D a);
	}

	private final Location location;
	private final Graph shearXGraph;
	private final Graph shearYGraph;

	public ShearAnim(float length, float sx, float sy) {
		this(
				length,
				Location.CENTER,
				new ConstantGraph(sx),
				new ConstantGraph(sy)
		);
	}

	public ShearAnim(float length, Location location, float sx, float sy) {
		this(
				length,
				location,
				new ConstantGraph(sx),
				new ConstantGraph(sy)
		);
	}

	public ShearAnim(float length, Graph shearXGraph, Graph shearYGraph) {
		this(
			length,
			Location.CENTER,
				shearXGraph,
				shearYGraph
		);
	}

	@MassConstructor
	public ShearAnim(float length, Location location, Graph shearXGraph, Graph shearYGraph) {
		super(length);

		this.location = location;

		this.shearXGraph = shearXGraph;
		this.shearYGraph = shearYGraph;
	}

	@Override
	public Object[] mass() {
		return new Object[] { length, location, shearXGraph, shearYGraph};
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		float scaleX = shearXGraph != null ? shearXGraph.generate(progress) : 0f;
		float scaleY = shearYGraph != null ? shearYGraph.generate(progress) : 0f;

		location.apply(scaleX, scaleY, a);
	}
}
