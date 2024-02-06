
package sengine.mass.serializers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import sengine.mass.Mass;
import sengine.mass.MassException;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

public class FieldSerializer<T> implements Serializer<T> {
	
	private static final Comparator<Field> nameComparator = new Comparator<Field>() {
		@Override
		public int compare(Field o1, Field o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	public static enum Primitive {
		BOOLEAN(boolean.class, Boolean.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				output.writeBoolean(field.getBoolean(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.setBoolean(object, input.readBoolean());
			}
		},
		BYTE(byte.class, Byte.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				output.writeByte(field.getByte(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.setByte(object, input.readByte());
			}
		},
		CHAR(char.class, Character.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				output.writeChar(field.getChar(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.setChar(object, input.readChar());
			}
		},
		SHORT(short.class, Short.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				output.writeShort(field.getShort(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.setShort(object, input.readShort());
			}
		},
		INT(int.class, Integer.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				output.writeInt(field.getInt(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.setInt(object, input.readInt());
			}
		},
		LONG(long.class, Long.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				output.writeLong(field.getLong(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.setLong(object, input.readLong());
			}
		},
		FLOAT(float.class, Float.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				output.writeFloat(field.getFloat(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.setFloat(object, input.readFloat());
			}
		},
		DOUBLE(double.class, Double.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				output.writeDouble(field.getDouble(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.setDouble(object, input.readDouble());
			}
		},
		OBJECT(Object.class, Object.class) {
			@Override
			public void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				m.write(field.get(object));
			}

			@Override
			public void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
				field.set(object, m.read());
			}
		}
		;
		
		public final Class<?> primitiveType;
		public final Class<?> wrapperType;
		
		Primitive(Class<?> primitiveType, Class<?> wrapperType) {
			this.primitiveType = primitiveType;
			this.wrapperType = wrapperType;
		}
		
		public abstract void write(Mass m, Output output, Field field, Object object) throws IllegalArgumentException, IllegalAccessException;
		public abstract void read(Mass m, Input input, Field field, Object object) throws IllegalArgumentException, IllegalAccessException;
		
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
	
	public final Field[] fields;
	public final Primitive[] primitives;
	
	public FieldSerializer(Class<T> type) {
		if (type.isInterface()) {
			fields = new Field[0]; // No fields to serialize.		// TODO: what is this?
			primitives = new Primitive[0];
			return;
		}
		
		// Check if compatible constructor exist
		try {
			type.getConstructor();
		} catch (Throwable e) {
			throw new MassException("Cannot use FieldSerializer for class without an accessible no-arg constructor: " + type);
		}

		// Collect all fields
		ArrayList<Field> allFields = new ArrayList<Field>();
		ArrayList<Field> classFields = new ArrayList<Field>();
		Class<?> nextClass = type;
		while (nextClass != Object.class) {
			// Get fields
			Field[] fields = nextClass.getDeclaredFields();
			// Filter fields
			for(int c = 0; c < fields.length; c++) {
				Field field = fields[c];
				// Filter modifiers
				int modifiers = field.getModifiers();
				if (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers) || field.isSynthetic()) 
					continue;		// ignore these fields
				// Check access
				if (!field.isAccessible()) {
					try {
						field.setAccessible(true);
					} catch (AccessControlException ex) {
						continue;			// failed
					}
				}
				// Can use this field
				classFields.add(field);
			}
			// Sort lexicographically 
			Collections.sort(classFields, nameComparator);
			// Add to all fields
			allFields.addAll(classFields);
			classFields.clear();
			nextClass = nextClass.getSuperclass();
		}

		// Save to array
		this.fields = allFields.toArray(new Field[allFields.size()]);
		
		// Recognize primitives
		this.primitives = new Primitive[fields.length];
		for(int c = 0; c < fields.length; c++)
			primitives[c] = Primitive.findPrimitive(fields[c].getType());
	}

	public void copy(HashMap<String, Object> from, T to) {
		for(int c = 0; c < fields.length; c++) {
			Field field = fields[c];
			String name = field.getName();
            if(from.containsKey(name)) {
                Object value = from.get(name);
                try {
                    field.set(to, value);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to assign value " + name + " " + value + " to " + to, e);
                }
            }
		}
	}
	
	@Override
	public void write(Mass m, Output output, T object) {
		for(int c = 0; c < fields.length; c++) {
			Field field = fields[c];
			Primitive primitive = primitives[c];
			try {
				primitive.write(m, output, field, object);
			} catch (Throwable e) {
				throw new MassException("Failed to write field-" + c + " [" + primitive + "] for object: " + object);
			}
		}
	}
	
	@Override
	public T read(Mass m, Input input, Class<T> type) {
		T object = Mass.newInstance(type);
		for(int c = 0; c < fields.length; c++) {
			Field field = fields[c];
			Primitive primitive = primitives[c];
			try {
				primitive.read(m, input, field, object);
			} catch (Throwable e) {
				throw new MassException("Failed to read field-" + c + " [" + primitive + "] for object: " + object, e);
			}
		}
		return object;
	}
}
