
package sengine.mass.serializers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import sengine.mass.Mass;
import sengine.mass.MassException;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DefaultSerializers {
	public static class BooleanSerializer implements Serializer<Boolean> {
		public void write (Mass kryo, Output output, Boolean object) {
			output.writeBoolean(object);
		}

		public Boolean read (Mass kryo, Input input, Class<Boolean> type) {
			return input.readBoolean();
		}
	}

	public static class ByteSerializer implements Serializer<Byte> {
		public void write (Mass kryo, Output output, Byte object) {
			output.writeByte(object);
		}

		public Byte read (Mass kryo, Input input, Class<Byte> type) {
			return input.readByte();
		}
	}

	public static class CharSerializer implements Serializer<Character> {
		public void write (Mass kryo, Output output, Character object) {
			output.writeChar(object);
		}

		public Character read (Mass kryo, Input input, Class<Character> type) {
			return input.readChar();
		}
	}

	public static class ShortSerializer implements Serializer<Short> {
		public void write (Mass kryo, Output output, Short object) {
			output.writeShort(object);
		}

		public Short read (Mass kryo, Input input, Class<Short> type) {
			return input.readShort();
		}
	}

	public static class IntSerializer implements Serializer<Integer> {
		public void write (Mass kryo, Output output, Integer object) {
			output.writeInt(object);
		}

		public Integer read (Mass kryo, Input input, Class<Integer> type) {
			return input.readInt();
		}
	}

	public static class LongSerializer implements Serializer<Long> {
		public void write (Mass kryo, Output output, Long object) {
			output.writeLong(object);
		}

		public Long read (Mass kryo, Input input, Class<Long> type) {
			return input.readLong();
		}
	}

	public static class FloatSerializer implements Serializer<Float> {
		public void write (Mass kryo, Output output, Float object) {
			output.writeFloat(object);
		}

		public Float read (Mass kryo, Input input, Class<Float> type) {
			return input.readFloat();
		}
	}

	public static class DoubleSerializer implements Serializer<Double> {
		public void write (Mass kryo, Output output, Double object) {
			output.writeDouble(object);
		}

		public Double read (Mass kryo, Input input, Class<Double> type) {
			return input.readDouble();
		}
	}

	/** @see Output#writeString(String) */
	public static class StringSerializer implements Serializer<String> {
		public void write (Mass kryo, Output output, String object) {
			output.writeString(object);
		}

		public String read (Mass kryo, Input input, Class<String> type) {
			return input.readString();
		}
	}

	public static class BigIntegerSerializer implements Serializer<BigInteger> {
		public void write (Mass kryo, Output output, BigInteger object) {
			BigInteger value = (BigInteger)object;
			byte[] bytes = value.toByteArray();
			output.writeInt(bytes.length);
			output.writeBytes(bytes);
		}

		public BigInteger read (Mass kryo, Input input, Class<BigInteger> type) {
			int length = input.readInt();
			byte[] bytes = input.readBytes(length);
			return new BigInteger(bytes);
		}
	}

	public static class BigDecimalSerializer implements Serializer<BigDecimal> {
		private static BigIntegerSerializer bigIntegerSerializer = new BigIntegerSerializer();

		public void write (Mass kryo, Output output, BigDecimal object) {
			BigDecimal value = (BigDecimal)object;
			bigIntegerSerializer.write(kryo, output, value.unscaledValue());
			output.writeInt(value.scale());
		}

		public BigDecimal read (Mass kryo, Input input, Class<BigDecimal> type) {
			BigInteger unscaledValue = bigIntegerSerializer.read(kryo, input, null);
			int scale = input.readInt();
			return new BigDecimal(unscaledValue, scale);
		}
	}

	public static class ClassSerializer implements Serializer<Class> {
		public void write (Mass kryo, Output output, Class object) {
			output.writeString(object.getName());
		}

		public Class read (Mass kryo, Input input, Class<Class> type) {
			String className = input.readString();
			Class<?> parsed = Mass.parseClassName(className);
			if(parsed == null)
				throw new MassException("Unable to find class type: " + className);
			return parsed;
		}
	}

	public static class DateSerializer implements Serializer<Date> {
		public void write (Mass kryo, Output output, Date object) {
			output.writeLong(object.getTime());
		}

		public Date read (Mass kryo, Input input, Class<Date> type) {
			return new Date(input.readLong());
		}
	}

	public static class EnumSerializer implements Serializer<Enum> {
		public void write (Mass kryo, Output output, Enum object) {
			output.writeInt(object.ordinal());
		}

		public Enum read (Mass kryo, Input input, Class<Enum> type) {
		    if(!type.isEnum())
                // Sometimes, the class we get here is not an enum (if the constant
		        // overrides / extends some members of the enum). The real enum is the superclass
		        type = (Class<Enum>) type.getSuperclass();
			int ordinal = input.readInt();
			return type.getEnumConstants()[ordinal];
		}
	}

	public static class EnumSetSerializer implements Serializer<EnumSet> {
		public void write (Mass kryo, Output output, EnumSet object) {
			if (object.isEmpty()) throw new MassException("An empty EnumSet cannot be serialized.");
			Class<?> enumType = object.iterator().next().getClass();
			kryo.write(enumType);
			output.writeByte((byte)object.size());
			for (Object element : object) {
				Enum e = (Enum)element;
				output.writeInt((byte)e.ordinal());
			}
		}

		public EnumSet read (Mass kryo, Input input, Class<EnumSet> type) {
			Class<Enum> enumType = (Class<Enum>)kryo.read();
			EnumSet object = EnumSet.noneOf(enumType);
			int length = input.readByte();
			for (int i = 0; i < length; i++)
				object.add(enumType.getEnumConstants()[input.readByte()]);
			return object;
		}
	}

	/** @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a> */
	public static class CurrencySerializer implements Serializer<Currency> {
		public void write (Mass kryo, Output output, Currency object) {
			output.writeString(object.getCurrencyCode());
		}

		public Currency read (Mass kryo, Input input, Class<Currency> type) {
			String currencyCode = input.readString();
			return Currency.getInstance(currencyCode);
		}
	}

	public static class StringBufferSerializer implements Serializer<StringBuffer> {
		public void write (Mass kryo, Output output, StringBuffer object) {
			output.writeString(object.toString());
		}

		public StringBuffer read (Mass kryo, Input input, Class<StringBuffer> type) {
			String value = input.readString();
			return new StringBuffer(value);
		}
	}

	public static class StringBuilderSerializer implements Serializer<StringBuilder> {
		public void write (Mass kryo, Output output, StringBuilder object) {
			output.writeString(object.toString());
		}

		public StringBuilder read (Mass kryo, Input input, Class<StringBuilder> type) {
			return new StringBuilder(input.readString());
		}
	}

	public static class CollectionsEmptyListSerializer implements Serializer {
		public void write (Mass kryo, Output output, Object object) {
		}

		public Object read (Mass kryo, Input input, Class type) {
			return Collections.EMPTY_LIST;
		}
	}

	public static class CollectionsEmptyMapSerializer implements Serializer {
		public void write (Mass kryo, Output output, Object object) {
		}

		public Object read (Mass kryo, Input input, Class type) {
			return Collections.EMPTY_MAP;
		}
	}

	public static class CollectionsEmptySetSerializer implements Serializer {
		public void write (Mass kryo, Output output, Object object) {
		}

		public Object read (Mass kryo, Input input, Class type) {
			return Collections.EMPTY_SET;
		}
	}

	public static class CollectionsSingletonListSerializer implements Serializer<List> {
		public void write (Mass kryo, Output output, List object) {
			kryo.write(object.get(0));
		}

		public List read (Mass kryo, Input input, Class type) {
			return Collections.singletonList(kryo.read());
		}
	}

	public static class CollectionsSingletonMapSerializer implements Serializer<Map> {
		public void write (Mass kryo, Output output, Map object) {
			Entry entry = (Entry)object.entrySet().iterator().next();
			kryo.write(entry.getKey());
			kryo.write(entry.getValue());
		}

		public Map read (Mass kryo, Input input, Class type) {
			Object key = kryo.read();
			Object value = kryo.read();
			return Collections.singletonMap(key, value);
		}
	}

	public static class CollectionsSingletonSetSerializer implements Serializer<Set> {
		public void write (Mass kryo, Output output, Set object) {
			kryo.write(object.iterator().next());
		}

		public Set read (Mass kryo, Input input, Class type) {
			return Collections.singleton(kryo.read());
		}
	}

	public static class TimeZoneSerializer implements Serializer<TimeZone> {
		public void write (Mass kryo, Output output, TimeZone object) {
			output.writeString(object.getID());
		}

		public TimeZone read (Mass kryo, Input input, Class<TimeZone> type) {
			return TimeZone.getTimeZone(input.readString());
		}
	}

	public static class CalendarSerializer implements Serializer<Calendar> {
		// The default value of gregorianCutover.
		private static final long DEFAULT_GREGORIAN_CUTOVER = -12219292800000L;
		private static final TimeZoneSerializer timeZoneSerializer = new TimeZoneSerializer();

		public void write (Mass kryo, Output output, Calendar object) {
			timeZoneSerializer.write(kryo, output, object.getTimeZone()); // can't be null
			output.writeLong(object.getTimeInMillis());
			output.writeBoolean(object.isLenient());
			output.writeByte((byte)object.getFirstDayOfWeek());
			output.writeByte((byte)object.getMinimalDaysInFirstWeek());
			if (object instanceof GregorianCalendar)
				output.writeLong(((GregorianCalendar)object).getGregorianChange().getTime());
			else
				output.writeLong(DEFAULT_GREGORIAN_CUTOVER);
		}

		public Calendar read (Mass kryo, Input input, Class<Calendar> type) {
			Calendar result = Calendar.getInstance(timeZoneSerializer.read(kryo, input, TimeZone.class));
			result.setTimeInMillis(input.readLong());
			result.setLenient(input.readBoolean());
			result.setFirstDayOfWeek(input.readByteUnsigned());
			result.setMinimalDaysInFirstWeek(input.readByteUnsigned());
			long gregorianChange = input.readLong();
			if (gregorianChange != DEFAULT_GREGORIAN_CUTOVER)
				if (result instanceof GregorianCalendar) ((GregorianCalendar)result).setGregorianChange(new Date(gregorianChange));
			return result;
		}
	}

	public static class TreeMapSerializer extends MapSerializer {
		public void write (Mass kryo, Output output, Map map) {
			TreeMap treeMap = (TreeMap)map;
			kryo.write(treeMap.comparator());                // TODO: 7/7/2016 why no read()
			super.write(kryo, output, map);
		}
	}
}
