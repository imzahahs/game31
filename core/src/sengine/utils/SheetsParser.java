package sengine.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.IntArray;
import com.opencsv.CSVReader;

import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.util.ArrayList;

import sengine.mass.MassException;

/**
 * Created by Azmi on 3/13/2017.
 */

public class SheetsParser {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Row {
        String[] fields();
    }

    public static final String SKIP = ">>>";


    public static class ParseException extends RuntimeException {

        public ParseException(String s) {
            super(s);
        }

        public ParseException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }

    public enum Primitive {
        BOOLEAN(boolean.class, Boolean.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                String fieldName = field.getName();
                if(value.endsWith(fieldName)) {
                    if(value.length() == fieldName.length()) {
                        field.setBoolean(object, true);
                        return;
                    }
                    else if(value.length() == fieldName.length() + 1) {
                        char indicator = value.charAt(0);
                        if(indicator == '+') {
                            field.setBoolean(object, true);
                            return;
                        }
                        else if(indicator == '-'){
                            field.setBoolean(object, false);
                            return;
                        }
                    }
                    // Else unrecognized
                    throw new RuntimeException("Invalid boolean indicator: " + value);
                }

                // Maybe a simple yes or no
                value = value.toLowerCase();

                if(value.equals("true") || value.equals("yes")) {
                    field.setBoolean(object, true);
                    return;
                }
                else if(value.equals("false") || value.equals("no")) {
                    field.setBoolean(object, false);
                    return;
                }

                // Else unrecognized
                throw new ParseException("Unrecognized boolean value: " + value);
            }
        },
        BYTE(byte.class, Byte.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                field.setByte(object, Byte.parseByte(value));
            }
        },
        CHAR(char.class, Character.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                if(value.length() != 1)
                    throw new RuntimeException("Invalid character: " + value);
                field.setChar(object, value.charAt(0));
            }
        },
        SHORT(short.class, Short.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                field.setShort(object, Short.parseShort(value));
            }
        },
        INT(int.class, Integer.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                field.setInt(object, Integer.parseInt(value));
            }
        },
        LONG(long.class, Long.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                field.setLong(object, Long.parseLong(value));
            }
        },
        FLOAT(float.class, Float.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                field.setFloat(object, Float.parseFloat(value));
            }
        },
        DOUBLE(double.class, Double.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                field.setDouble(object, Double.parseDouble(value));
            }
        },
        STRING(String.class, String.class) {
            @Override
            public void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException {
                field.set(object, value);
            }
        }
        ;




        public final Class<?> primitiveType;
        public final Class<?> wrapperType;

        Primitive(Class<?> primitiveType, Class<?> wrapperType) {
            this.primitiveType = primitiveType;
            this.wrapperType = wrapperType;
        }

        public abstract void read(Field field, Object object, String value) throws IllegalArgumentException, IllegalAccessException;

        public static Primitive findPrimitive(Class<?> type) {
            Primitive[] primitives = values();
            for(int c = 0; c < primitives.length; c++) {
                Primitive p = primitives[c];
                if(type == p.primitiveType || type == p.wrapperType)
                    return p;
            }
            return null;
        }
    }

    private static class Serializer<T> {

        public final Class<T> type;
        public final Field[] fields;
        public final Primitive[] primitives;

        public final Method[] methods;
        public final Class<?>[][] methodParameterTypes;

        public Serializer(Class<T> type) {
            // Check if compatible constructor exist
            try {
                type.getConstructor();
            } catch (Throwable e) {
                throw new MassException("Cannot serialize without an accessible no-arg constructor: " + type);
            }

            this.type = type;

            // Collect all fields
            Array<Field> allFields = new Array<Field>(Field.class);
            Array<Method> methods = new Array<Method>(Method.class);
            Class<?> nextClass = type;
            while (nextClass != Object.class) {
                // Get methods
                Method[] typeMethods = nextClass.getDeclaredMethods();
                for(int c = 0; c < typeMethods.length; c++) {
                    Method method = typeMethods[c];
                    int modifiers = method.getModifiers();
                    if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || method.isSynthetic())
                        continue;		// ignore these fields
                    methods.add(method);
                }
                // Get fields
                Row rowInfo = nextClass.getAnnotation(Row.class);
                if(rowInfo == null) {
                    nextClass = nextClass.getSuperclass();
                    continue;       // not supported
                }
                String[] fieldNames = rowInfo.fields();
                for(int c = 0; c < fieldNames.length; c++) {
                    String fieldName = fieldNames[c];
                    try {
                        Field field = nextClass.getField(fieldName);
                        // Filter modifiers
                        int modifiers = field.getModifiers();
                        if (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)|| field.isSynthetic())
                            continue;		// ignore these fields
                        // Check access
                        if (!field.isAccessible()) {
                            try {
                                field.setAccessible(true);
                            } catch (AccessControlException ex) {
                                // ignored, hopefully we dont use this
                            }
                        }
                        allFields.add(field);
                    } catch (Throwable e) {
                        throw new RuntimeException("Failed to retrieve field " + fieldName, e);
                    }
                }
                nextClass = nextClass.getSuperclass();
            }

            // Save to array
            this.fields = allFields.toArray();

            // Recognize primitives
            this.primitives = new Primitive[fields.length];
            for(int c = 0; c < fields.length; c++)
                primitives[c] = Primitive.findPrimitive(fields[c].getType());

            // Methods
            this.methods = methods.toArray();
            this.methodParameterTypes = new Class<?>[methods.size][];

            for(int c = 0; c < methods.size; c++) {
                Method method = methods.items[c];
                // Check access
                if (!method.isAccessible()) {
                    try {
                        method.setAccessible(true);
                    } catch (AccessControlException ex) {
                        continue;			// failed
                    }
                }
                methodParameterTypes[c] = method.getParameterTypes();
            }


        }

        public void readPrimitiveField(Object object, String value, int index) throws IllegalAccessException {
            primitives[index].read(fields[index], object, value);
        }

        public int findField(String name) {
            for(int c = 0; c < fields.length; c++) {
                if(fields[c].getName().equals(name))
                    return c;
            }
            return -1;
        }

        public int findMethod(String name) {
            for(int c = 0; c < methods.length; c++) {
                if(methods[c].getName().equals(name))
                    return c;
            }
            return -1;
        }

        public T newInstance () {
            try {
                return type.newInstance();
            } catch (Throwable e) {
                throw new ParseException("Unable to instantiate type: " + type, e);
            }
        }
    }

    private final IdentityMap<Class<?>, Serializer<?>> serializers = new IdentityMap<Class<?>, Serializer<?>>();


    private <T> Serializer<T> findSerializer(Class<T> type) {
        Serializer<T> serializer = (Serializer<T>) serializers.get(type);
        if(serializer == null) {
            // Create new
            serializer = new Serializer<T>(type);
            serializers.put(type, serializer);
        }
        return serializer;
    }

    private final Array<String[]> lines = new Array<String[]>(String[].class);
    private final IntArray lineShifts = new IntArray();
    private int currentLine = 0;


    private void clear() {
        lines.clear();
        lineShifts.clear();
        currentLine = 0;
    }

    private void loadCSV(String csv) {
        // Clear
        clear();

        // Read entire csv
        CSVReader reader = new CSVReader(new StringReader(csv));
        String[] values;
        try {
            while ((values = reader.readNext()) != null) {
                // Cleanup values
                // First remove all comments or empty strings and identify shift
                int shift = -1;
                boolean hasCommented = false;
                for (int c = 0; c < values.length; c++) {
                    if(hasCommented)
                        values[c] = null;     // once commented, remove all subsequent
                    else {
                        String value = values[c].trim();
                        if (value.isEmpty())
                            values[c] = null;     // remove empty
                        else if (value.startsWith("//")) {
                            values[c] = null;     // remove comment, and remember to remove all subsequent
                            hasCommented = true;
                        }
                        else if (shift == -1)
                            shift = c;      // recognize first column
                    }
                }

                // Skip empty lines
                if(shift == -1)
                    continue;

                // Now compact columns
                int offset = 0;
                for (int c = shift; c < values.length; c++) {
                    String value = values[c];
                    if(value == null)
                        offset++;                  // empty spaces in between first column, indicate shift
                    else {
                        // Another value, compact them
                        values[c] = null;
                        values[c - offset] = value;
                    }
                }
                // Keep
                lines.add(values);
                lineShifts.add(shift);
            }
        } catch (Throwable e) {
            throw new ParseException("Failed to parse csv", e);
        }
    }


    private String[] parseStringArray(int baseShift) {
        Array<String> array = new Array<String>(String.class);
        boolean isNextLine = false;
        for(; currentLine < lines.size; currentLine++) {
            // Check shift
            int shift = isNextLine ? lineShifts.items[currentLine] : baseShift;
            if(shift < baseShift)
                break;              // shifting up
            String[] values = lines.items[currentLine];

            for(; shift < values.length; shift++) {
                String value = values[shift];
                if(value == null)
                    break;      // end of line
                array.add(value);
            }
            // Try next line now
            isNextLine = true;
        }
        // Done
        return array.toArray();
    }

    private <T> T[] parseObjectArray(Class<T> componentType, int baseShift) {
        // Its an array of objects
        Array<T> array = new Array<T>(componentType);
        boolean isNextLine = false;
        while(currentLine < lines.size) {
            // Check shift
            int shift = isNextLine ? lineShifts.items[currentLine] : baseShift;
            if(shift < baseShift)
                break;              // shifting up
            int line = currentLine;
            T component = parse(componentType, baseShift);
            if(component == null)
                break;          //  Cannot parse, either shift change or end of sheet
            // Else parsed, add to array
            array.add(component);
            // Increase line if not yet
            if(currentLine == line)
                currentLine++;
            isNextLine = true;
        }
        // Done return array
        return array.toArray();
    }

    private int adjustBaseShift(int baseShift) {
        if(currentLine >= lines.size)
            return -1;      // sheet ended
        String[] values = lines.items[currentLine];
        if(values[baseShift] != null)
            return baseShift;           // baseShift is valid at the current line
        // Else need find on the next line
        currentLine++;
        if(currentLine >= lines.size)
            return -1;      // sheet ended
        int shift = lineShifts.items[currentLine];
        if(shift < baseShift)
            return -1;              // shifting up
        return shift;               // Found a valid baseShift on the next line
    }

    /**
     * primitive array types not supported, multidimensional arrays not supported
     * @param type
     * @param baseShift
     * @param <T>
     * @return
     */
    private <T> T parse(Class<T> type, int baseShift) {
        // Adjust base shift
        baseShift = adjustBaseShift(baseShift);
        if(baseShift == -1)
            return null;            // Either sheet ended, or shifting up

        // Check if array
        if(type.isArray()) {
            Class<?> componentType = type.getComponentType();
            if(componentType == String.class)
                return (T) parseStringArray(baseShift);
            else
                return (T) parseObjectArray(componentType, baseShift);
        }

        // Get serializer
        Serializer<T> serializer = findSerializer(type);

        // Instantiate
        T object = serializer.newInstance();

        boolean isNextLine = false;
        while(currentLine < lines.size) {
            // Remember line
            int line = currentLine;

            // Check shift
            int shift = isNextLine ? lineShifts.items[currentLine] : baseShift;
            if(shift < baseShift)
                return object;              // shifting up
            baseShift = shift;              // accept forward shifts, but not back

            String[] values = lines.items[currentLine];

            // First line, possibility of horizontal unpacking, first value must not match any known field names
            String fieldName = values[shift];
            boolean isMethod = false;
            int index = serializer.findField(fieldName);
            if(index == -1) {
                index = serializer.findMethod(fieldName);
                if(index != -1)
                    isMethod = true;
            }

            if(index == -1) {
                // Value is not a field name, use horizontal unpacking
                if(isNextLine)
                    return object;          // cannot use multiple horizontal unpacks, must be another object in an array
                for(int c = 0; c < serializer.fields.length && (shift + c) < values.length; c++) {
                    String value = values[shift + c];
                    if(value == null)
                        break;          // end of lne
                    if(value.equals(SKIP))
                        continue;
                    try {
                        if (serializer.primitives[c] == null) {
                            // This field is an object, recurse here and stop horizontal unpacking
                            Object parsed = parse(serializer.fields[c].getType(), shift + c);       // will always return something
                            serializer.fields[c].set(object, parsed);
                            break;          // stop horizontal unpacking
                        }
                        else {
                            // Else its a primitive, parse and save
                            serializer.readPrimitiveField(object, values[shift + c], c);

                        }
                    } catch (Throwable e) {
                        throw new ParseException("Horizontal unpack failed for field \"" + serializer.fields[c].getName() + "\" index " + c + " line " + line, e);
                    }
                }
            }
            else if(isMethod) {
                // First column is a method name
                Method method = serializer.methods[index];
                Class<?>[] parameterTypes = serializer.methodParameterTypes[index];
                Object[] parameters = new Object[parameterTypes.length];

                for(int c = 0; c < parameterTypes.length && (shift + c + 1) < values.length; c++) {
                    String value = values[shift + c + 1];
                    Class<?> parameterType = parameterTypes[c];
                    if(value == null)
                        break;          // end of lne
                    if(value.equals(SKIP))
                        continue;
                    try {
                        if(parameterType == boolean.class || parameterType == Boolean.class) {
                            value = value.toLowerCase();
                            if(value.equals("true") || value.equals("yes"))
                                parameters[c] = true;
                            else if(value.equals("false") || value.equals("no"))
                                parameters[c] = false;
                            else
                                throw new RuntimeException("Invalid boolean indicator: " + value);
                        }
                        else if(parameterType == byte.class || parameterType == Byte.class) {
                            parameters[c] = Byte.parseByte(value);
                        }
                        else if(parameterType == char.class || parameterType == Character.class) {
                            if(value.length() != 1)
                                throw new RuntimeException("Invalid character: " + value);
                            parameters[c] = value.charAt(0);
                        }
                        else if(parameterType == short.class || parameterType == Short.class) {
                            parameters[c] = Short.parseShort(value);
                        }
                        else if(parameterType == int.class || parameterType == Integer.class) {
                            parameters[c] = Integer.parseInt(value);
                        }
                        else if(parameterType == long.class || parameterType == Long.class) {
                            parameters[c] = Long.parseLong(value);
                        }
                        else if(parameterType == float.class || parameterType == Float.class) {
                            parameters[c] = Float.parseFloat(value);
                        }
                        else if(parameterType == double.class || parameterType == Double.class) {
                            parameters[c] = Double.parseDouble(value);
                        }
                        else if(parameterType == String.class) {
                            parameters[c] = value;
                        }
                        else {
                            // Else its an object, recurse here and stop horizontal unpacking
                            Object parsed = parse(parameterType, shift + c + 1);       // will always return something
                            parameters[c] = parsed;
                            break;
                        }
                    } catch (Throwable e) {
                        throw new ParseException("Horizontal unpack failed for method \"" + method.getName() + "\" index " + c + " line " + line, e);
                    }

                }

                // Call this method
                try {
                    method.invoke(object, parameters);
                } catch (Throwable e) {
                    throw new ParseException("Horizontal unpack failed for method \"" + method.getName() + "\" line " + line, e);
                }
            }
            else {
                // Else first column is a field name, vertical unpacking, check if its another object
                try {
                    if (serializer.primitives[index] == null) {
                        // Its another object, recurse
                        Object parsed = parse(serializer.fields[index].getType(), shift + 1);
                        if(parsed != null)
                            serializer.fields[index].set(object, parsed);
                    }
                    else {
                        // Else its a primitive field
                        String value = values[shift + 1];
                        if(value != null)
                            serializer.readPrimitiveField(object, value, index);
                    }
                } catch (Throwable e) {
                    throw new ParseException("Vertical unpack failed for field \"" + serializer.fields[index].getName() + "\" line " + line, e);
                }
            }
            // Done, if still the same line, increment
            if(line == currentLine)
                currentLine++;
            // Else must have moved down from parsing an object
            isNextLine = true;
        }

        // Parsed till end of sheet
        return object;
    }

    public <T> T parse(String csv, Class<T> type) {
        // Load csv first
        loadCSV(csv);

        // Parse to structure
        T result = parse(type, 0);

        // Clear
        clear();

        // Done
        return result;
    }
}
