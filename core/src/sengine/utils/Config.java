package sengine.utils;

import java.io.StringReader;
import java.util.HashMap;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.Primitive;
import bsh.UtilEvalError;
import sengine.File;
import sengine.Sys;
import sengine.mass.MassSerializable;
import sengine.mass.serializers.FieldSerializer;

/**
 * Created by Azmi on 24/6/2016.
 */
public class Config implements MassSerializable {
    public static final String TAG = "Config";
    public static boolean DEBUG = true;

    public static final String HINTS_EXTENSION = ".ConfigHints";

    public static final String VAR_CONFIGNAME = "CONFIGNAME";

    public static final String DEFAULTS_FILENAME = "defaults";

    public static String[] IGNORED_VARS = new String[] {
            "bsh", File.VAR_FILENAME, VAR_CONFIGNAME
    };

    public static Config load(String filename) {
        return load(filename, true);
    }

    public static Config load(String filename, boolean enforceExistence) {
        Config config = File.getHints(filename + HINTS_EXTENSION, true, false);
        if(config == null) {
            config = new Config(filename, enforceExistence);
            File.saveHints(filename + HINTS_EXTENSION, config);
        }
        return config;
    }

    private static final WeakCache<Class<?>, FieldSerializer> serializers = new WeakCache<Class<?>, FieldSerializer>();

    private final String filename;

    private final HashMap<String, Object> fields;

    public boolean contains(String name) {
        return fields.containsKey(name);
    }

    public <T> T get(String name) {
        if(!fields.containsKey(name))
            throw new RuntimeException(name + " not found in config " + filename);
        return (T) fields.get(name);
    }

    public String getFilename() {
        return filename;
    }

    public void apply(Object to) {
        Class<?> type = to.getClass();
        FieldSerializer serializer = serializers.get(type);
        if(serializer == null) {
            serializer = new FieldSerializer(type);
            serializers.put(type, serializer);
        }
        serializer.copy(fields, to);
    }

    public Config(String filename, boolean enforceExistence, Object ... params) {
        this.filename = filename;
        this.fields = new HashMap<String, Object>();

        // Get extension
        String extension = File.getExtension(filename);
        String defaultsFilename = null;
        if(extension != null)
            defaultsFilename = DEFAULTS_FILENAME + "." + extension;

        // Prepare interpreter
        Interpreter interpreter = File.interpreter();
        try {
            interpreter.set(VAR_CONFIGNAME, filename);
            // Add given parameters
            for(int c = 0; c < params.length; c += 2) {
                String name = (String) params[c];
                Object value = params[c + 1];
                interpreter.set(name, value);
            }
        } catch (EvalError evalError) {
            Sys.error(TAG, "Unexpected interpreter error", evalError);
        }

        // Run cascading config
        try {
            if(defaultsFilename != null) {
                // Get base defaults
                String path = defaultsFilename;
                File.run(path, false, false, false, interpreter);
                // Cascade subsequent directories
                String[] directories = filename.split("/");
                String basePath = "";
                for(int c = 0; c < (directories.length - 1); c++) {
                    if(c > 0)
                        basePath += "/";
                    basePath += directories[c];
                    path = basePath + "/" + defaultsFilename;
                    File.run(path, false, false, false, interpreter);
                }
            }
            // Run final config
            File.run(filename, enforceExistence, false, false, interpreter);
        } catch (Exception e) {
            throw new RuntimeException("Unable to eval config " + filename, e);
        }


        // Save settings
        String[] varNames = interpreter.getNameSpace().getVariableNames();
        if(DEBUG) Sys.info(TAG, "Loaded config for " + filename);
        for(int c = 0; c < varNames.length; c++) {
            String name = varNames[c];
            boolean ignored = false;
            // Check if this name is ignored
            for(int i = 0; i < IGNORED_VARS.length; i++) {
                if(name.contentEquals(IGNORED_VARS[i])) {
                    ignored = true;
                    break;
                }
            }
            if(ignored)
                continue;       // ignoring this variable
            // Else keep this variable
            try {
                Object var = interpreter.getNameSpace().getVariable(name);
                if(var instanceof Primitive) {
                    Primitive primitive = (Primitive) var;
                    var = primitive.getValue();
                }
                if(DEBUG) Sys.info(TAG, name + " => " + var);
                fields.put(name, var);
            } catch (UtilEvalError utilEvalError) {
                Sys.error(TAG, "Unable to extract config variable " + name + " for " + filename, utilEvalError);
            }
        }
    }

    @MassConstructor
    public Config(String filename, HashMap<String, Object> fields) {
        this.filename = filename;
        this.fields = fields;
    }


    @Override
    public Object[] mass() {
        return new Object[] { filename, fields };
    }
}
