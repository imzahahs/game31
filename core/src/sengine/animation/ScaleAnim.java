package sengine.animation;

import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public class ScaleAnim extends Animation implements MassSerializable {

	public static final ScaleAnim gone = new ScaleAnim(1f, ConstantGraph.zero);

	public static enum Location {
		TOP {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate((offsetX - (offsetX * scaleX)), (offsetY - (offsetY * scaleY)) + length - (scaleY * length));
				a.scale(scaleX, scaleY);
			}
		},
		BOTTOM {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate((offsetX - (offsetX * scaleX)), (offsetY - (offsetY * scaleY)) - length + (scaleY * length));
				a.scale(scaleX, scaleY);
			}
		},
		LEFT {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				a.translate((offsetX - (offsetX * scaleX)) - 0.5f + (scaleX / 2.0f), (offsetY - (offsetY * scaleY)));
				a.scale(scaleX, scaleY);
			}
		},
		RIGHT {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				a.translate((offsetX - (offsetX * scaleX)) + 0.5f - (scaleX / 2.0f), (offsetY - (offsetY * scaleY)));
				a.scale(scaleX, scaleY);
			}
		},
		TOPLEFT {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate((offsetX - (offsetX * scaleX)) - 0.5f + (scaleX / 2.0f), (offsetY - (offsetY * scaleY)) + length - (scaleY * length));
				a.scale(scaleX, scaleY);
			}
		},
		TOPRIGHT {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate((offsetX - (offsetX * scaleX)) + 0.5f - (scaleX / 2.0f), (offsetY - (offsetY * scaleY)) + length - (scaleY * length));
				a.scale(scaleX, scaleY);
			}
		},
		BOTTOMLEFT {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate((offsetX - (offsetX * scaleX)) - 0.5f + (scaleX / 2.0f), (offsetY - (offsetY * scaleY)) - length + (scaleY * length));
				a.scale(scaleX, scaleY);
			}
		},
		BOTTOMRIGHT {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				float length = a.getLength() / 2.0f;
				a.translate((offsetX - (offsetX * scaleX)) + 0.5f - (scaleX / 2.0f), (offsetY - (offsetY * scaleY)) - length + (scaleY * length));
				a.scale(scaleX, scaleY);
			}
		},
		CENTER {
			@Override
			public void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a) {
				if(offsetX != 0 || offsetY != 0)
					a.translate((offsetX - (offsetX * scaleX)), (offsetY - (offsetY * scaleY)));
				a.scale(scaleX, scaleY);
			}
		},
		;
		
		public abstract void apply(float scaleX, float scaleY, float offsetX, float offsetY, Animatable2D a);
	}
	
	final Location location;
	final Graph scaleXGraph;
	final Graph scaleYGraph;
	final Graph offsetXGraph;
	final Graph offsetYGraph;
	
	public ScaleAnim(float length) {
		this(
			length,
			Location.CENTER,
			ConstantGraph.zero,
			ConstantGraph.zero,
			ConstantGraph.zero,
			ConstantGraph.zero
		);
	}
	
	public ScaleAnim(float length, Graph scaleGraph) {
		this(
			length,
			Location.CENTER,
			scaleGraph,
			scaleGraph,
			ConstantGraph.zero,
			ConstantGraph.zero
		);
	}
	
	public ScaleAnim(float length, Graph scaleXGraph, Graph scaleYGraph) {
		this(
			length,
			Location.CENTER,
			scaleXGraph,
			scaleYGraph,
			ConstantGraph.zero,
			ConstantGraph.zero
		);
	}

	public ScaleAnim(float length, Location location, Graph scaleGraph) {
		this(
			length,
			location,
			scaleGraph,
			scaleGraph,
			ConstantGraph.zero,
			ConstantGraph.zero
		);
	}

	public ScaleAnim(float length, Location location, Graph scaleXGraph, Graph scaleYGraph) {
		this(
				length,
				location,
				scaleXGraph,
				scaleYGraph,
				ConstantGraph.zero,
				ConstantGraph.zero
		);
	}

	@MassConstructor
	public ScaleAnim(float length, Location location, Graph scaleXGraph, Graph scaleYGraph, Graph offsetXGraph, Graph offsetYGraph) {
		super(length);
		
		this.location = location;
		
		this.scaleXGraph = scaleXGraph;
		this.scaleYGraph = scaleYGraph;
		this.offsetXGraph = offsetXGraph;
		this.offsetYGraph = offsetYGraph;
	}

	@Override
	public Object[] mass() {
		return new Object[] { length, location, scaleXGraph, scaleYGraph, offsetXGraph, offsetYGraph };
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		float scaleX = scaleXGraph != null ? scaleXGraph.generate(progress) : 1f;
		float scaleY = scaleYGraph != null ? scaleYGraph.generate(progress) : 1f;
		float offsetX = offsetXGraph != null ? offsetXGraph.generate(progress) : 0f;
		float offsetY = offsetYGraph != null ? offsetYGraph.generate(progress) : 0f;

		location.apply(scaleX, scaleY, offsetX, offsetY, a);
	}
}
