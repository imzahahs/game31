package sengine.calc;

import sengine.mass.MassSerializable;

public class LinearGraph extends Graph implements MassSerializable {

	public static final LinearGraph zeroToOne = new LinearGraph(0, 1);
	public static final LinearGraph oneToZero = new LinearGraph(1, 0);
	
	final float length;
	final float start;
	final float end;
	
	public LinearGraph(float start, float end) {
		this(start, end, 1.0f);
	}
	
	@MassConstructor
	public LinearGraph(float start, float end, float length) {
		this.start = start;
		this.end = end;
		this.length = length;
	}
	@Override
	public Object[] mass() {
		return new Object[] { start, end, length };
	}

	@Override public float getEnd() { return end; }
	@Override public float getStart() { return start; }
	@Override public float getLength() { return length; }
	
	@Override
	float calculate(float progress) {
		return start + ((float)progress / (float)length * (end - start));
	}
}