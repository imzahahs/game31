package sengine.animation;

import sengine.calc.Graph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public class VibrateAnim extends Animation implements MassSerializable {

	final Graph vibrateXGraph;
	final Graph vibrateYGraph;
	final float resolution;

	public VibrateAnim(float length, Graph vibrateGraph) {
		this(length, vibrateGraph, vibrateGraph, -1f);
	}

	public VibrateAnim(float length, Graph vibrateGraph, float resolution) {
		this(length, vibrateGraph, vibrateGraph, resolution);
	}

	public VibrateAnim(float length, Graph vibrateXGraph, Graph vibrateYGraph) {
		this(length, vibrateXGraph, vibrateYGraph, -1f);
	}
	
	@MassConstructor
	public VibrateAnim(float length, Graph vibrateXGraph, Graph vibrateYGraph, float resolution) {
		super(length);
		
		this.vibrateXGraph = vibrateXGraph;
		this.vibrateYGraph = vibrateYGraph;
		this.resolution = resolution;
	}
	@Override
	public Object[] mass() {
		return new Object[] { length, vibrateXGraph, vibrateYGraph, resolution };
	}
	
	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		float x;
		float y;
		if(resolution > 0f) {
			long hash1 = hashCode();
			long hash2 = a.hashCode();
			long time = Math.round((double)renderTime / (double)resolution);
			long seed = (hash1 << 32) | hash2;
			seed ^= time;
			seed ^= (seed << 21);
			seed ^= (seed >>> 35);
			seed ^= (seed << 4);
			x = (float)(seed & ((1L << 24) - 1)) / (float)(1 << 24);
			seed = (hash2 << 32) | hash1;
			seed ^= time;
			seed ^= (seed << 21);
			seed ^= (seed >>> 35);
			seed ^= (seed << 4);
			y = (float)(seed & ((1L << 24) - 1)) / (float)(1 << 24);
		}
		else {
			x = (float)Math.random();
			y = (float)Math.random();
		}
		x -= 0.5f;
		y -= 0.5f;
		
		if(vibrateXGraph != null)
			x *= vibrateXGraph.generate(progress);
		else
			x = 0f;
		if(vibrateYGraph != null)
			y *= vibrateYGraph.generate(progress);
		else
			y = 0f;
		a.translate(x, y);
	}
}
