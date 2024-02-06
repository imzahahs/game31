package sengine.calc;

import sengine.mass.MassSerializable;

public class VibrationGraph extends Graph implements MassSerializable {

	final float length;
	
	final Graph amplitude;
	final Graph base;

	public VibrationGraph(float length, float amplitude, float base) {
		this(
				length,
				amplitude != 0 ? new ConstantGraph(amplitude) : null,
				base != 0 ? new ConstantGraph(base) : null
		);
	}

	@MassConstructor
	public VibrationGraph(float length, Graph amplitude, Graph base) {
		this.length = length;
		
		this.amplitude = amplitude;
		this.base = base;
	}
	@Override
	public Object[] mass() {
		return new Object[] { length, amplitude, base };
	}
	
	@Override
	public float getStart() {
		return calculate(0.0f);
	}

	@Override
	public float getEnd() {
		return calculate(length);
	}

	@Override
	public float getLength() {
		return length;
	}

	@Override
	float calculate(float progress) {
		
		float vibration = (float) Math.random();
		
		progress = progress / length;
		
		// Amplitude
		if(amplitude != null)
			vibration *= amplitude.generate(progress);
		// Base
		if(base != null)
			vibration += base.generate(progress);

		return vibration;
	}
}