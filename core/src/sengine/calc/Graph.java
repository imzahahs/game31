package sengine.calc;

public abstract class Graph {
	// Constrains
	public abstract float getStart();
	public abstract float getEnd();
	public abstract float getLength();
	// Calculation
	abstract float calculate(float progress);

	// Indirect implementations
	public final int getEndInt() { return (int)getEnd(); }
	public final int getStartInt() { return (int)getStart(); }
	
	// Standard graph
	public final float generate(float progress) {
		return calculate(remainderf(progress, getLength()));
	}
	
	public final float clampProgress(float progress) {
		float length = getLength();
		if(progress > length)
			return length;
		else if(progress < 0.0f)
			return 0.0f;
		return progress;
	}
	
	public static final float remainderf(float dividend, float divisor) {
		float modulus = dividend % divisor;
		return (modulus == 0.0f && dividend >= divisor ? divisor : modulus);   
	}
	
	public final int generateInt(float progress) {return (int)generate(progress);}
	// Reverse graph
	public final float generateReversed(float progress) {
		float length = getLength();
		return calculate(length - remainderf(progress, length));
	}
	public final int generateReversedInt(float progress) {return (int)generateReversed(progress);}
}