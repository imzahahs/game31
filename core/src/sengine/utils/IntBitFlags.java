package sengine.utils;

public class IntBitFlags {
	public final int shift;
	public final int length;
	public final int mask;
	
	public static int calcLengthRequired(int maxValue) {
		int bits = 1;
		int c = 2;
		while((c - 1) < maxValue) {
			bits++;
			c *= 2;
		}
		return bits;
	}
	
	public IntBitFlags(IntBitFlags previous, int maxValue) {
		this(previous.shift + previous.length, calcLengthRequired(maxValue), maxValue);
	}
	
	public IntBitFlags(IntBitFlags previous, int length, int maxValueCheck) {
		this(previous.shift + previous.length, length, maxValueCheck);
	}
	
	public IntBitFlags(int shift, int maxValue) {
		this(shift, calcLengthRequired(maxValue), maxValue);
	}
	
	public IntBitFlags(int shift, int length, int maxValueCheck) {
		// Evaluate params
		if(shift < 0)
			throw new IllegalArgumentException("Shift cannot be < 0: " + shift);
		else if(shift >= 32)
			throw new IllegalArgumentException("Shift cannot be >= 32: " + shift);
		else if(length <= 0)
			throw new IllegalArgumentException("Length cannot be <= 0: " + length);
		else if(length > 32)
			throw new IllegalArgumentException("Length cannot be > 32: " + length);
		else if((length + shift) > 32)
			throw new IllegalArgumentException("Shift and length exceeds 32 bits: " + shift + " " + length);
		this.shift = shift;
		this.length = length;
		long maskBits = 2;
		for(int c = 1; c < length; c++)
			maskBits *= 2;
		maskBits--;
		if(maskBits < maxValueCheck)
			throw new IllegalArgumentException("Max value exceeds allocated flag capacity: " + maxValueCheck + " " + maskBits);
		this.mask = (int)maskBits;		// need the unsafe cast to get low 32 bits
	}
	
	public final int getInt(int source) {
		return (source >> shift) & mask;
	}
	
	public final int setInt(int source, int flags) {
		return (source & ~(mask << shift)) | ((flags & mask) << shift); 
	}
	
	public static class EnumFlags<T extends Enum<T>> extends IntBitFlags {
		public final T[] values;
		
		public EnumFlags(IntBitFlags previous, T[] values) {
			this(previous.shift + previous.length, values);
		}

		public EnumFlags(T[] values) {
			this(0, values);
		}

		public EnumFlags(int shift, T[] values) {
			super(shift, calcLengthRequired(values.length - 1), values.length - 1);
			
			this.values = values;
		}
		
		public final T getEnum(int source) {
			return values[getInt(source)];
		}
		
		public final int setEnum(int source, Enum<T> flag) {
			return setInt(source, flag.ordinal());
		}
	}
}
