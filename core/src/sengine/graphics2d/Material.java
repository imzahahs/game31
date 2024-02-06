package sengine.graphics2d;

import java.util.HashMap;

import sengine.File;
import sengine.Streamable;
import sengine.Sys;
import sengine.materials.AnimatedMaterial;
import sengine.materials.ColoredMaterial;
import sengine.materials.SimpleMaterial;
import sengine.materials.SpriteBatchMaterial;
import sengine.materials.VideoMaterial;

public abstract class Material implements Streamable {

	public static final String DEFAULTS_FILENAME = "defaults"; 
	public static final String DEFAULT_MATERIAL = "defaultMaterial." + ColoredMaterial.ID;

	// Default material
	public static Material defaultMaterial = null;
	
	// Material types
	public static SimpleMaterial.Type simpleMaterialType;
	public static ColoredMaterial.Type coloredMaterialType;
	public static SpriteBatchMaterial.Type spriteBatchMaterialType;
	public static AnimatedMaterial.Type animatedMaterialType;
	public static VideoMaterial.Type videoMaterialType;

	// Default material type
	public static Type defaultMaterialType = null;
	
	public static Material getDefaultMaterial() {
		defaultMaterial = Material.load(DEFAULT_MATERIAL);
		return defaultMaterial;
	}
	
	public static void reset() {
		// Register initial types
		simpleMaterialType = new SimpleMaterial.Type();
		coloredMaterialType = new ColoredMaterial.Type();
		spriteBatchMaterialType = new SpriteBatchMaterial.Type();
		animatedMaterialType = new AnimatedMaterial.Type();
		videoMaterialType = new VideoMaterial.Type();
		// Defaults
		defaultMaterial = null; 
		defaultMaterialType = null;
	}
	
	// Type handlers
	private static final HashMap<String, Type> types = new HashMap<String, Type>();
	
	public static abstract class Type {
		static final String TAG = "Material.Type";
		
		public final String id;
		
		public Type(String id) {
			this.id = id;
			// Register to types
			Type replaced = types.put(id, this);
			if(replaced != null && replaced.getClass() != getClass())
				Sys.debug(TAG, "Replaced existing material: " + replaced  + " " + this);
		}

		protected abstract Material create(String name);
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "#" + id;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Material> T load(String name) {
		Material m = File.getHints(name, false, false);
		if(m != null)
			return (T)m;	// Material already loaded
		// Else material wasnt created before
		// Infer material type from extension
		String split[] = File.splitExtension(name);
		Type type = types.get(split[1]);
		if(type == null) {
			if(defaultMaterialType != null) {
				// Use default material
				type = defaultMaterialType;
				split[0] = name;
			}
			else		// Else default material type is also undefined
				throw new RuntimeException("Failed to find material type for: " + name);
		}

		m = type.create(split[0]);
		if(m == null)
			throw new RuntimeException("Material type failed to create material for: " + name + " " + type);
		// Material created, save to FS
		File.saveHints(name, m);
		return (T)m;
	}

	// Basic properties
	public final int depthFunc;
	public final boolean depthMask;
	public final int faceCullingMode;
	public final int srcBlendFunc;
	public final int destBlendFunc;
	
	public Material(int depthFunc, boolean depthMask, int faceCullingMode, int srcBlendFunc, int destBlendFunc) {
		this.depthFunc = depthFunc;
		this.depthMask = depthMask;
		this.faceCullingMode = faceCullingMode;
		this.srcBlendFunc = srcBlendFunc;
		this.destBlendFunc = destBlendFunc;
	}
	
	// Used by MaterialInstance
	public abstract void initialize(MaterialInstance m);
	// Rendering
	public abstract Shader bind();
	public abstract void unbind();
	// General material information
	public abstract float getLength();

    // Binds the default texture on the specified texture unit
	public void bindTexture(int target) {
        // no texture
    }
}
