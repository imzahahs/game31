package sengine.calc;

import sengine.mass.MassSerializable;

public class QuadraticGraph extends Graph implements MassSerializable {


	public static final QuadraticGraph zeroToOne = new QuadraticGraph(0, 1, false);
	public static final QuadraticGraph oneToZero = new QuadraticGraph(1, 0, false);
	public static final QuadraticGraph zeroToOneInverted = new QuadraticGraph(0, 1, true);
	public static final QuadraticGraph oneToZeroInverted = new QuadraticGraph(1, 0, true);

	final float start;
	final float end;
	final float length;
	final float a;
	final float b;
	final boolean inverted;
	
	public QuadraticGraph(float start, float end, boolean inverted) {
		this(start, end, 1f, 0f, inverted);
	}
	
	public QuadraticGraph(float start, float end, float b, boolean inverted) {
		this(start, end, 1.0f, b, inverted);
	}
	
	@MassConstructor
	public QuadraticGraph(float start, float end, float length, float b, boolean inverted) {
		this.inverted = inverted;
		if(inverted) {
			float temp = end;
			end = start;
			start = temp;
		}
		this.start = start;
		this.end = end;
		this.length = length;
		// Calculate graph params
		this.b = b;
		this.a = ((end - start) - (length * b)) / (length * length);
		
		/*
		 * y = ax^2 + bx + c
		 * y (end) = 100 when
		 * x (length) = 10, 
		 * c (start) = 10,
		 * b (b) = 10,
		 * a (a) = ?
		 * 
		 * 100 = a(10^2) + 10(10) + 10
		 * a = ((100 - 10) - (10 * 10)) / (10^2)
		 */
	}
	@Override
	public Object[] mass() {
		return new Object[] { start, end, length, b, inverted };
	}
	

	@Override public float getStart() { return inverted?end:start; }
	@Override public float getEnd() { return inverted?start:end; }
	@Override public float getLength() { return length; }

	@Override
	float calculate(float progress) {
		if(inverted)
			progress = length-progress;
		return start + (a * progress * progress) + (b * progress);
	}
}
