package sengine.graphics2d;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import sengine.File;
import sengine.GarbageCollector;
import sengine.Sys;
import sengine.mass.DefaultSerializer;
import sengine.mass.Mass;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

@DefaultSerializer(Shader.ShaderSerializer.class)
public class Shader implements GarbageCollector.Collectible {
	static final String TAG = "Shader";

    public static class ShaderSerializer implements Serializer<Shader> {

        @Override
        public Shader read(Mass m, Input s, Class<Shader> type) {
            String filename = s.readString();
            String vertexShader = s.readString();
            String fragmentShader = s.readString();

            if(filename == null)
                return new Shader(filename, vertexShader, fragmentShader);          // custom unnamed shader

            // Else named shader, try to reduce duplicates by only loading once
            Shader shader = File.peekHints(filename);
            if(shader != null)
                return shader;          // Use existing

            // Else first time loading this shader
            shader = new Shader(filename, vertexShader, fragmentShader);
            File.saveHints(filename, shader);
            return shader;
        }

        @Override
        public void write(Mass m, Output s, Shader o) {
            s.writeString(o.filename);
            s.writeString(o.vertexShader);
            s.writeString(o.fragmentShader);
        }
    }

	
	public static final String VERTEX_EXTENSION = ".vertex";
	public static final String FRAGMENT_EXTENSION = ".fragment";

	public static boolean recompileShaders = false;

	// Last used shader
	public static Shader lastShader = null;
	
	public static Shader load(String filename) {
		Shader shader = File.getHints(filename, false);
		if(shader != null)
			return shader;		// Shader already loaded
		// Else load now
		shader = new Shader(filename);
		// Save to FS
		File.saveHints(filename, shader);
		return shader;
	}

	private class CachedShaderProgram extends ShaderProgram {

		public CachedShaderProgram(String vertexShader, String fragmentShader) {
			super(vertexShader, fragmentShader);
		}

        @Override
		public int fetchUniformLocation(String name, boolean pedantic) {
            // Lookup from cache
            int index = cachedUniforms.indexOf(name, true);
            int location;
            if(index == -1) {
                // First time encountering
                location = super.fetchUniformLocation(name, pedantic);
                // Save
                cachedUniforms.add(name);
                cachedUniformLocations.add(location);
            }
            else
                location = cachedUniformLocations.items[index];
            return location;
		}

		@Override
		public int getAttributeLocation(String name) {
			int index = cachedAttributeNames.indexOf(name, true);
			if(index != -1)
                return cachedAttributeLocation.items[index];

            int location = super.getAttributeLocation(name);
            cachedAttributeNames.add(name);
            cachedAttributeLocation.add(location);
			return location;
		}

		@Override
		public void disableVertexAttribute(String name) {
			disableVertexAttribute(getAttributeLocation(name));
		}
	}
	
	public final String filename;
	public final String vertexShader;
	public final String fragmentShader;

	// Current
	private CachedShaderProgram program = null;

    // Cached
    private final Array<VertexAttributes> cachedAttributes = new Array<VertexAttributes>(VertexAttributes.class);
    private final Array<int[]> cachedAttributesLocation = new Array<int[]>(int[].class);
	private final Array<String> cachedAttributeNames = new Array<String>(String.class);
	private final IntArray cachedAttributeLocation = new IntArray();

    private final Array<String> cachedUniforms = new Array<String>(String.class);
    private final IntArray cachedUniformLocations = new IntArray();


    public int[] getAttributeLocations(VertexAttributes attributes) {
        // Lookup from cache
        int index = cachedAttributes.indexOf(attributes, true);
        int[] locations;
        if(index == -1) {
            // First time encountering this attributes set, load shader and find locations
            if (program == null)
                load();
            int numAttributes = attributes.size();
            locations = new int[numAttributes];
            for (int c = 0; c < numAttributes; c++) {
                locations[c] = program.getAttributeLocation(attributes.get(c).alias);
            }
            // Save
            cachedAttributes.add(attributes);
            cachedAttributesLocation.add(locations);
        }
        else
            locations = cachedAttributesLocation.items[index];
        return locations;
    }


	public Shader(String filename) {
		this(filename, null, null);
	}

	public Shader(String filename, String vertexShader, String fragmentShader) {
		this.filename = filename;

		String vertexShaderText = vertexShader;
		String fragmentShaderText = fragmentShader;

		if(recompileShaders || vertexShader == null || fragmentShader == null) {
			Sys.info(TAG, "Recompiling shader: " + filename);
			try {
				vertexShaderText = File.read(filename + VERTEX_EXTENSION, false);
				fragmentShaderText = File.read(filename + FRAGMENT_EXTENSION, false);
				if(vertexShaderText == null || fragmentShaderText == null) {
					Sys.info(TAG, "Cannot read shader file: " + filename);
					vertexShaderText = vertexShader;
					fragmentShaderText = fragmentShader;
				}
			}
			catch(Throwable e) {
				Sys.error(TAG, "Failed to recompile shader: " + filename, e);
			}
		}

		if(vertexShaderText == null || fragmentShaderText == null)
			throw new RuntimeException("Failed to load compile shader: " + filename);
		
		this.vertexShader = vertexShaderText;
		this.fragmentShader = fragmentShaderText;

		if(recompileShaders && Thread.currentThread() == Sys.system.getRenderingThread())
			load(true);
	}

	public ShaderProgram bind() {
		if(lastShader == this)
			return program;		// This shader has already been bound
		if(program == null)
			load();
		// Bind shader
		program.begin();
		lastShader = this;
		return program;
	}
	
	public ShaderProgram getProgram() {
        if(program == null)
            load();
		return program;
	}

	public void load() {
    	load(false);
	}

	public void load(boolean ignoreErrors) {
		if (program != null)
			return;        // already loaded
		// Compile now
		program = new CachedShaderProgram(vertexShader, fragmentShader);
		String log = program.getLog();
		if(log == null)
			log = "";
		else
			log = log.trim();
		if(!log.isEmpty())
			Sys.debug(TAG, "Shader compilation log for " + filename + "\n" + log);
		if (!program.isCompiled()) {   // Compile failure
			Sys.error(TAG, "Compile error for " + filename);
			if(!ignoreErrors) {
			    program = null;
                throw new RuntimeException("Shader " + filename + " failed to compile\n" + log);
            }
		}
		// Require GC cycle
		GarbageCollector.add(this);
	}
	

	@Override
	public boolean performGC(boolean forced) {
		if(!forced)
			return false;		// For normal GC, releasing shaders are not worth it
		// Else release shader
		program.dispose();
		program = null;
		// Reset last shader
		lastShader = null;
		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + filename;
	}
}
