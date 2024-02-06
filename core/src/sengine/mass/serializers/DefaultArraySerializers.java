
package sengine.mass.serializers;

import java.lang.reflect.Array;

import sengine.mass.Mass;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

public class DefaultArraySerializers {
	public static class ByteArraySerializer implements Serializer<byte[]> {
		@Override
		public void write (Mass kryo, Output output, byte[] object) {
			output.writeInt(object.length);
			output.writeBytes(object);
		}

		@Override
		public byte[] read (Mass kryo, Input input, Class<byte[]> type) {
			int length = input.readInt();
			return input.readBytes(length);
		}
	}

	public static class IntArraySerializer implements Serializer<int[]> {
		@Override
		public void write (Mass kryo, Output output, int[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				output.writeInt(object[i]);
		}

		@Override
		public int[] read (Mass kryo, Input input, Class<int[]> type) {
			int length = input.readInt();
			int[] array = new int[length];
			for (int i = 0; i < length; i++)
				array[i] = input.readInt();
			return array;
		}
	}

	public static class FloatArraySerializer implements Serializer<float[]> {
		@Override
		public void write (Mass kryo, Output output, float[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				output.writeFloat(object[i]);
		}

		@Override
		public float[] read (Mass kryo, Input input, Class<float[]> type) {
			int length = input.readInt();
			float[] array = new float[length];
			for (int i = 0; i < length; i++)
				array[i] = input.readFloat();
			return array;
		}
	}

	public static class LongArraySerializer implements Serializer<long[]> {
		@Override
		public void write (Mass kryo, Output output, long[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				output.writeLong(object[i]);
		}

		@Override
		public long[] read (Mass kryo, Input input, Class<long[]> type) {
			int length = input.readInt();
			long[] array = new long[length];
			for (int i = 0; i < length; i++)
				array[i] = input.readLong();
			return array;
		}
	}

	public static class ShortArraySerializer implements Serializer<short[]> {
		@Override
		public void write (Mass kryo, Output output, short[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				output.writeShort(object[i]);
		}

		@Override
		public short[] read (Mass kryo, Input input, Class<short[]> type) {
			int length = input.readInt();
			short[] array = new short[length];
			for (int i = 0; i < length; i++)
				array[i] = input.readShort();
			return array;
		}
	}

	public static class CharArraySerializer implements Serializer<char[]> {
		@Override
		public void write (Mass kryo, Output output, char[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				output.writeChar(object[i]);
		}

		@Override
		public char[] read (Mass kryo, Input input, Class<char[]> type) {
			int length = input.readInt();
			char[] array = new char[length];
			for (int i = 0; i < length; i++)
				array[i] = input.readChar();
			return array;
		}
	}

	public static class DoubleArraySerializer implements Serializer<double[]> {
		@Override
		public void write (Mass kryo, Output output, double[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				output.writeDouble(object[i]);
		}

		@Override
		public double[] read (Mass kryo, Input input, Class<double[]> type) {
			int length = input.readInt();
			double[] array = new double[length];
			for (int i = 0; i < length; i++)
				array[i] = input.readDouble();
			return array;
		}
	}

	public static class BooleanArraySerializer implements Serializer<boolean[]> {
		@Override
		public void write (Mass kryo, Output output, boolean[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				output.writeBoolean(object[i]);
		}

		@Override
		public boolean[] read (Mass kryo, Input input, Class<boolean[]> type) {
			int length = input.readInt();
			boolean[] array = new boolean[length];
			for (int i = 0; i < length; i++)
				array[i] = input.readBoolean();
			return array;
		}
	}

	public static class StringArraySerializer implements Serializer<String[]> {
		@Override
		public void write (Mass kryo, Output output, String[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				output.writeString(object[i]);
		}

		@Override
		public String[] read (Mass kryo, Input input, Class<String[]> type) {
			int length = input.readInt();
			String[] array = new String[length];
			for (int i = 0; i < length; i++)
				array[i] = input.readString();
			return array;
		}
	}

	public static class ObjectArraySerializer implements Serializer<Object[]> {
		@Override
		public void write (Mass kryo, Output output, Object[] object) {
			output.writeInt(object.length);
			for (int i = 0, n = object.length; i < n; i++)
				kryo.write(object[i]);
		}

		@Override
		public Object[] read (Mass kryo, Input input, Class<Object[]> type) {
			int length = input.readInt();
			Object[] object = (Object[])Array.newInstance(type.getComponentType(), length);
			kryo.reference(object);
			for (int i = 0, n = object.length; i < n; i++)
				object[i] = kryo.read();
			return object;
		}
	}
}
