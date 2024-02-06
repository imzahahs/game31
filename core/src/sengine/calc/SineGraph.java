package sengine.calc;

import sengine.mass.MassSerializable;

public class SineGraph extends Graph implements MassSerializable {

	final float length;
	
	final float f;
	final float phase;
	
	final Graph amplitude;
	final Graph progression;
	final Graph base;
	
	final float start;
	final float end;

	public SineGraph(float length, float waves, float phase, float amplitude, float base) {
		this(length, waves, phase, new ConstantGraph(amplitude), new ConstantGraph(base), null);
	}

	@MassConstructor
	public SineGraph(float length, float waves, float phase, Graph amplitude, Graph base, Graph progression) {
		this.length = length;
		this.f = waves / length;
		this.phase = phase;
		
		this.amplitude = amplitude;
		this.base = base;
		this.progression = progression;
		
		this.start = calculate(0.0f);
		this.end = calculate(length);
	}
	@Override
	public Object[] mass() {
		return new Object[] { length, f * length, phase, amplitude, base, progression };
	}
	
	@Override
	public float getStart() {
		return start;
	}

	@Override
	public float getEnd() {
		return end;
	}

	@Override
	public float getLength() {
		return length;
	}

	@Override
	float calculate(float progress) {
		float t = progress / length;
		if(progression != null)
			progress = progression.generate(t) * length;
		float w = (float)Math.sin(((progress * f) + phase) * 2 * Math.PI);
		// Amplitude
		if(amplitude != null)
			w *= amplitude.generate(t);
		// Base
		if(base != null)
			w += base.generate(t);
		return w;
	}
}
