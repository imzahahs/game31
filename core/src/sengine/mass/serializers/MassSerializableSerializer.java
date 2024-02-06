package sengine.mass.serializers;

import java.lang.reflect.Constructor;
import sengine.mass.Mass;
import sengine.mass.MassException;
import sengine.mass.MassSerializable;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

public class MassSerializableSerializer implements Serializer<MassSerializable> {
	
	public static enum Primitive {
		BOOLEAN(boolean.class, Boolean.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeBoolean((Boolean)o);
			}
			@Override
			public Object read(Mass m, Input s) {
				return s.readBoolean();
			}
		},
		BYTE(byte.class, Byte.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeByte((Byte)o);
			}
			@Override
			public Object read(Mass m, Input s) {
				return s.readByte();
			}
		},
		CHAR(char.class, Character.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeChar((Character)o);
			}
			@Override
			public Object read(Mass m, Input s) {
				return s.readChar();
			}
		},
		SHORT(short.class, Short.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeShort((Short)o);
			}

			@Override
			public Object read(Mass m, Input s) {
				return s.readShort();
			}
		},
		INT(int.class, Integer.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeInt((Integer)o);
			}

			@Override
			public Object read(Mass m, Input s) {
				return s.readInt();
			}
		},
		LONG(long.class, Long.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeLong((Long)o);
			}
			@Override
			public Object read(Mass m, Input s) {
				return s.readLong();
			}
		},
		FLOAT(float.class, Float.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeFloat((Float)o);
			}

			@Override
			public Object read(Mass m, Input s) {
				return s.readFloat();
			}
		},
		DOUBLE(double.class, Double.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeDouble((Double)o);
			}
			@Override
			public Object read(Mass m, Input s) {
				return s.readDouble();
			}
		},
		STRING(String.class, String.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				s.writeString((String)o);
			}
			@Override
			public Object read(Mass m, Input s) {
				return s.readString();
			}

		},
		OBJECT(Object.class, Object.class) {
			@Override
			public void write(Mass m, Output s, Object o) {
				m.write(o);
			}
			@Override
			public Object read(Mass m, Input s) {
				return m.read();
			}
		}
		;
		
		public final Class<?> primitiveType;
		public final Class<?> wrapperType;
		
		Primitive(Class<?> primitiveType, Class<?> wrapperType) {
			this.primitiveType = primitiveType;
			this.wrapperType = wrapperType;
		}
		
		public abstract void write(Mass m, Output s, Object o);
		public abstract Object read(Mass m, Input s);
		
		public static Primitive findPrimitive(Class<?> type) {
			Primitive[] primitives = Primitive.values();
			for(int c = 0; c < primitives.length; c++) {
				Primitive p = primitives[c];
				if(type == p.primitiveType || type == p.wrapperType)
					return p;
			}
			return OBJECT;
		}
	}

	

	@Override
	public MassSerializable read(Mass m, Input s, Class<MassSerializable> type) {
		// Determine constructor
		Constructor<?> constructor;
		Primitive[] constructorPrimitives;
		if(constructors.length > 1) {
			int index = s.readByteUnsigned();
			constructor = constructors[index];
			constructorPrimitives = primitives[index];
		}
		else {
			constructor = constructors[0];
			constructorPrimitives = primitives[0];
		}
		// Read arguments
		Object[] arguments = new Object[constructorPrimitives.length];
		for(int c = 0; c < arguments.length; c++)
			arguments[c] = constructorPrimitives[c].read(m, s);
		// Instantiate
		try {
			return (MassSerializable) constructor.newInstance(arguments);
		} catch(Throwable e) {
			String argumentsString = "";
			for(int c = 0; c < arguments.length; c++)
				argumentsString += "[" + c + "]: " + arguments[c] + "\n";
			throw new MassException("Failed to instantiate object: " + type + " with arguments: " + argumentsString, e);
		}
	}

	@Override
	public void write(Mass m, Output s, MassSerializable o) {
		Object[] arguments = o.mass();
		// Save arguments
		if(arguments == null)
			throw new MassException("Serializable failed to give constructor arguments");
		// Find the correct constructor
		int index = -1;
		for(int c = 0; c < constructors.length; c++) {
			if(primitives[c].length == arguments.length) {
				index = c;
				break;
			}
		}
		if(index == -1)
			throw new MassException("MassConstructor not found for number of arguments given: " + arguments.length);
		if(constructors.length > 1)
			s.writeByte((byte)index);
		Primitive[] constructorPrimitives = primitives[index];
		for(int c = 0; c < arguments.length; c++) {
			try {
				constructorPrimitives[c].write(m, s, arguments[c]);
			} catch(ClassCastException e) {
				throw new MassException("Unexpected argument type: " + arguments[c].getClass() + " expected: " + constructorPrimitives[c].primitiveType, e);
			}
		}
	}
	
	public final Constructor<?>[] constructors;
	public final Primitive[][] primitives;
	
	public MassSerializableSerializer() {
		constructors = null;
		primitives = null;
	}
	
	public MassSerializableSerializer(Class<?> type) {
		// Find MassConstructors
		Constructor<?>[] ctors = type.getConstructors();
		int numConstructors = 0;
		for(int c = 0; c < ctors.length; c++) {
			if(ctors[c].getAnnotation(MassSerializable.MassConstructor.class) != null)
				numConstructors++;
		}
		if(numConstructors == 0)
			throw new IllegalArgumentException("MassConstructor annotation not found in class: " + type);
		constructors = new Constructor<?>[numConstructors];
		for(int c = 0, i = 0; c < ctors.length; c++) {
			if(ctors[c].getAnnotation(MassSerializable.MassConstructor.class) != null)
				constructors[i++] = ctors[c];
		}
		// Sort array
		while(true) {
			boolean sorted = true;
			// Sort ascending
			for(int c = 1; c < constructors.length; c++) {
				int currentCount = constructors[c].getParameterTypes().length;
				int prevCount = constructors[c - 1].getParameterTypes().length;
				if(currentCount == prevCount)
					throw new MassException("Cannot have duplicate equal amount of arguments for MassConstructor of type: " + type + " count: " + currentCount);
				else if(currentCount < prevCount) {
					Constructor<?> ctor = constructors[c - 1];
					constructors[c - 1] = constructors[c];
					constructors[c] = ctor;
					sorted = false;
				}
			}
			if(sorted)
				break;
		}
		// Find primitives for each constructor
		primitives = new Primitive[numConstructors][];
		for(int c = 0; c < numConstructors; c++) {
			Class<?>[] parameters = constructors[c].getParameterTypes();
			Primitive[] constructorPrimitives = new Primitive[parameters.length];
			for(int p = 0; p < parameters.length; p++)
				constructorPrimitives[p] = Primitive.findPrimitive(parameters[p]);
			primitives[c] = constructorPrimitives;
		}
	}
}
