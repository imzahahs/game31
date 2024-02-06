package sengine.calc;

import sengine.mass.MassSerializable;

public class Range implements MassSerializable {

	public final float min;
	public final float rf;
	public final float minValue;
	public final float maxValue;
	public final boolean randomSign;
	
	public Range(float min, float rf) {
		this(min, rf, false);		// default is false
	}
	
	@MassConstructor
	public Range(float min, float rf, boolean randomSign) {
		this.min = min;
		this.rf = rf;
		this.randomSign = randomSign;
		// Identify range
		float maxValue = Math.max(min, min + rf);
		float minValue = Math.min(min, min + rf);
		this.maxValue = randomSign ? (maxValue >= 0.0f ? maxValue : -maxValue) : maxValue;
		this.minValue = randomSign ? (minValue <= 0.0f ? minValue : -minValue) : minValue;
	}
	@Override
	public Object[] mass() {
		return new Object[] { min, rf, randomSign };
	}
	
	public float generate() {return generateFor(min, rf, randomSign);}
	public int generateInt() {return (int)generate();}
	
	public float position(float generated) {
		return (generated - minValue) / (maxValue - minValue);
	}
	
	public static boolean decideBoolean(float chances) {
		return Math.random() < chances;
	}
	
	public static float generateFor(float min, float rf, boolean randomSign) {
		return randomSign ? ((min + ((float)Math.random() * rf)) * (Math.random() < 0.5f ? +1 : -1)) : (min + ((float)Math.random() * rf)); 
	}
	public static int generateIntFor(float min, float rf, boolean randomSign) {return (int)Math.round(generateFor(min, rf, randomSign));}
	
	public static int generateInt(int max) {
		int value = (int)(Math.random() * (max + 1));
		if(value > max)
			value = max;
		return value;
	}
}
