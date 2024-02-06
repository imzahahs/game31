package sengine.utils;


import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import bsh.Interpreter;
import sengine.File;
import sengine.Sys;

/**
 * Created by Azmi on 23/7/2016.
 */
public class Builder<T> extends LiveEditor.Source<T> {
    static final String TAG = "Builder";

    public static final String BASEPATH = "builder";
    public static final String PARAM_PREFIX = "__param";


    public static final HashMap<Class<?>, Class<?>> overrides = new HashMap();
    private static final HashMap<Class<?>, Interpreter> cachedInterpreters = new HashMap<>();

    private final Class<? extends T> type;
    private final Object[] params;
    private T build = null;

    public T buildFromCompiled() {
        build = null;           // reset
        Constructor<?>[] constructors = type.getConstructors();
        for(int c = 0; c < constructors.length; c++) {
            Constructor<?> constructor = constructors[c];
            if(constructor.getParameterTypes().length == params.length) {
                try {
                    build = (T) constructor.newInstance(params);
                    return build;
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to build from compiled code " + type.getName(), e);
                }
            }
        }
        throw new RuntimeException("No constructor with " + params.length + " parameters for " + type.getName());
    }

    public Builder(Class<? extends T> builderType, Object ... params) {
        Class<?> override = overrides.get(builderType);
        if(override != null) {
            builderType = (Class<? extends T>) override;
        }

        // Get enclosing class
        Class<?> enclosingType = builderType;
        Class<?> nextEnclosingType;
        while((nextEnclosingType = enclosingType.getEnclosingClass()) != null) {
            if(!Modifier.isStatic(enclosingType.getModifiers()))
                throw new RuntimeException("Cannot use non-static inner class as builder: " + builderType.getName());
            enclosingType = nextEnclosingType;
        }
        this.type = builderType;
        this.params = params;
        // Get path to enclosing class
        String name = enclosingType.getCanonicalName();
        String filename = name.replace('.', '/') + ".java";
        // Set path
        path(BASEPATH, filename);
    }

    /**
     * Builds the source if not already and returns.
     * @return T
     */
    public T build() {
        if(build != null)
            return build;
        // Load from cache first
        synchronized (cachedInterpreters) {
            Interpreter interpreter = cachedInterpreters.get(type);
            if(interpreter != null) {
                try {
                    loadFromInterpreter(interpreter);
                    return build;           // done
                } catch (Throwable e) {
                    Sys.error(TAG, "Failed to load from cached interpreter", e);
                    cachedInterpreters.remove(type);            // Try rebuild
                }
            }
        }
        return rebuild();
    }

    public T rebuild() {
        load(false);
        if(build == null)
            buildFromCompiled();
        return build;
    }

    @Override
    protected void onChangeDetected() {
        rebuild();
    }

    @Override
    protected T convert(String content) throws Throwable {
        build = null;       // reset
        Interpreter interpreter = File.interpreter();
        interpreter.eval(new StringReader(content));
        loadFromInterpreter(interpreter);
        synchronized (cachedInterpreters) {
            cachedInterpreters.put(type, interpreter);      // Save to cache
        }
        return build;
    }

    private void loadFromInterpreter(Interpreter interpreter) throws Throwable {
        String paramsList = "";
        for(int c = 0; c < params.length; c++) {
            String name = PARAM_PREFIX + c;
            if(!paramsList.isEmpty())
                paramsList += ", ";
            paramsList += name;
            interpreter.set(name, params[c]);
        }
        build = (T) interpreter.eval("new " + type.getCanonicalName() + "(" + paramsList + ");");
    }
}
