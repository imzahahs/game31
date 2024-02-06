package sengine.calc;

import sengine.mass.MassSerializable;

public class ConstantGraph extends Graph implements MassSerializable {
	
	public static final ConstantGraph zero = new ConstantGraph(0f);
	public static final ConstantGraph one = new ConstantGraph(1f);
	
	final float constant;
	final float length;
	
	public ConstantGraph() {
		this(0f, 1f);
	}
	
	public ConstantGraph(float constant) {
		this(constant, 1f);
	}
	
	@MassConstructor
	public ConstantGraph(float constant, float length) {
		this.constant = constant;
		this.length = length;
	}
	@Override
	public Object[] mass() {
		return new Object[] { constant, length };
	}

	@Override
	public float getStart() {
		return constant;
	}

	@Override
	public float getEnd() {
		return constant;
	}

	@Override
	public float getLength() {
		return length;
	}

	@Override
	float calculate(float progress) {
		return constant;
	}
}
