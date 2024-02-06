package sengine.animation;

import sengine.calc.Graph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public class RotateAnim extends Animation implements MassSerializable {
	
	public static enum Location {
		TOP {
			@Override
			public void apply(float rotation, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(0.0f, +length);
				a.rotate(rotation);
				a.translate(0.0f, -length);
			}
		},
		BOTTOM {
			@Override
			public void apply(float rotation, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(0.0f, -length);
				a.rotate(rotation);
				a.translate(0.0f, +length);
			}
		},
		LEFT {
			@Override
			public void apply(float rotation, Animatable2D a) {
				a.translate(-0.5f, 0.0f);
				a.rotate(rotation);
				a.translate(+0.5f, 0.0f);
			}
		},
		RIGHT {
			@Override
			public void apply(float rotation, Animatable2D a) {
				a.translate(+0.5f, 0.0f);
				a.rotate(rotation);
				a.translate(-0.5f, 0.0f);
			}
		},
		TOPLEFT {
			@Override
			public void apply(float rotation, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(-0.5f, +length);
				a.rotate(rotation);
				a.translate(+0.5f, -length);
			}
		},
		TOPRIGHT {
			@Override
			public void apply(float rotation, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(+0.5f, +length);
				a.rotate(rotation);
				a.translate(-0.5f, -length);
			}
		},
		BOTTOMLEFT {
			@Override
			public void apply(float rotation, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(-0.5f, -length);
				a.rotate(rotation);
				a.translate(+0.5f, +length);
			}
		},
		BOTTOMRIGHT {
			@Override
			public void apply(float rotation, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate(+0.5f, -length);
				a.rotate(rotation);
				a.translate(-0.5f, +length);
			}
		},
		CENTER {
			@Override
			public void apply(float rotation, Animatable2D a) {
				a.rotate(rotation);
			}
		},
		;
		
		public abstract void apply(float rotation, Animatable2D a);
	}

	public final Location location;
	public final Graph rotateGraph;

	public RotateAnim(float length, Graph rotateGraph) {
		this(length, Location.CENTER, rotateGraph);
	}

	@MassConstructor
	public RotateAnim(float length, Location location, Graph rotateGraph) {
		super(length);
		
		this.location = location;
		
		this.rotateGraph = rotateGraph;
	}
	@Override
	public Object[] mass() {
		return new Object[] { length, location, rotateGraph };
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		location.apply(rotateGraph.generate(progress), a);
	}
}
