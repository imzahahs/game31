package sengine.materials;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.File;
import sengine.GarbageCollector;
import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;
import sengine.graphics2d.TextureUtils;
import sengine.graphics2d.TextureZERO;
import sengine.graphics2d.texturefile.TextureFile;
import sengine.graphics2d.texturefile.TextureLoader;
import sengine.mass.MassSerializable;
import sengine.utils.Config;

public class SimpleMaterial extends Material implements GarbageCollector.Collectible, MassSerializable {
	public static final String ID = "SimpleMaterial";

    public static final String CFG_EXTENSION = ".SimpleMaterial";

	public static class Type extends Material.Type {
		public Type() {
			super(ID);
		}

		@Override
		protected Material create(String name) {
            // Load config
			CompileConfig config = CompileConfig.load(name);

            return new SimpleMaterial(name, config);
		}
	}
	
	public static final String DEFAULT_NORMAL_SHADER = "shaders/SimpleMaterial-normal.glsl";
	
	public static final float DEFAULT_GC_TIME = 15.0f;
	public static final float DEFAULT_LODZERO_MINIFICATION = 0.05f;
	public static final float DEFAULT_RESIZING_GAMMA = 2.2f;

	public static int minTextureSize = 64 * 64;
	
	public static boolean squareTextures = false;
	public static boolean potTextures = false;

	// Uniforms
	public static final String u_texture = "u_texture";
	public static final String u_textureLodBias = "u_textureLodBias";

	public static class ImageCompileConfig {
		public float onScreenSize = 1.0f;
        public int minSize = -1;
        public int maxSize = -1;
        public float cropLength = -1f;          // no cropping
		public boolean mipmapping = false; 
		public boolean pma = false;
		public float compressQuality = 1.0f;            // max quality
		public TextureFilter minFilter = TextureFilter.Linear; // TextureFilter.MipMapLinearNearest;
		public TextureFilter magFilter = TextureFilter.Linear; // TextureFilter.MipMapLinearNearest;
		public TextureWrap uWrap = TextureWrap.ClampToEdge;
		public TextureWrap vWrap = TextureWrap.ClampToEdge;
		public float lodZeroMinification = DEFAULT_LODZERO_MINIFICATION;
		public float resizingGamma = DEFAULT_RESIZING_GAMMA;
		
		public Pixmap compileLodZero(Pixmap processedImage) {
			int width = Math.round(processedImage.getWidth() * lodZeroMinification);
			int height = Math.round(processedImage.getHeight() * lodZeroMinification);
			if(width < 1)
				width = 1;
			if(height < 1)
				height = 1;
            Pixmap lodZero = TextureUtils.duplicate(processedImage);
            lodZero = TextureUtils.resize(lodZero, width, height);
			return lodZero;
		}
		
		public Pixmap process(Pixmap image) {
			// Make sure image is of a standard format
			image = TextureUtils.standardizeFormat(image);
			// Validate pma first
			if(pma)
				image = TextureUtils.premultiplyAlpha(image);
            if(cropLength > 0)
                image = TextureUtils.crop(image, cropLength);
			// Resize base image to best dimensions
			boolean pot = potTextures;
			if(uWrap != TextureWrap.ClampToEdge || vWrap != TextureWrap.ClampToEdge)
				pot = true;			// required pot if using tiling, for iOS
			if(mipmapping)
				image = TextureUtils.validateDimensions(image, onScreenSize, minSize, maxSize, true, squareTextures, false);
			else
				image = TextureUtils.validateDimensions(image, onScreenSize, minSize, maxSize, pot, squareTextures, false);
			return image;
		}
		
		public void compile(String textureFilename, Pixmap processedImage, float compressQuality) {
			// Check if texture has already been compiled
			if(File.open(textureFilename + ".texture", false) != null)
				return;		// already compiled
			Sys.info(ID, "Converting image: " + textureFilename);
			Pixmap[] levels;
			if(!mipmapping)
				levels = new Pixmap[] { processedImage };
			else {
				// Else using mipmapping, create levels
				Pixmap image = TextureUtils.duplicate(processedImage);
				int numLevels = TextureUtils.getLevels(image.getWidth(), image.getHeight());
				levels = new Pixmap[numLevels];
				levels[0] = TextureUtils.duplicate(image);	// Duplicate as were using image to resize to other lod levels
				for(int c = 1; c < levels.length; c++) {
					image = TextureUtils.resizeHalf(image, resizingGamma);
					levels[c] = TextureUtils.duplicate(image);
				}
				image.dispose();
			}
			// Save 
			TextureFile textureFile = new TextureFile();
			textureFile.save(textureFilename + ".texture", new Pixmap[][] { levels }, compressQuality);
			textureFile.clear();
			// Release all levels
			if(mipmapping) {
				for(int c = 0; c < levels.length; c++)
					levels[c].dispose();
			}
		}
	}
	
	public static class CompileConfig extends ImageCompileConfig {
        public static CompileConfig load(String filename) {
            CompileConfig config = new CompileConfig();
            Config.load(filename + CFG_EXTENSION, false).apply(config);
            return config;
        }

		public int depthFunc = GL20.GL_ALWAYS;
		public boolean depthMask = false;
		public int faceCullingMode = GL20.GL_NEVER;
		public int srcBlendFunc = GL20.GL_SRC_ALPHA;
		public int destBlendFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
		public Shader normalShader = Shader.load(DEFAULT_NORMAL_SHADER);
		public float textureLodBias = 0f;
		public boolean isStreamed = true;
		public float tGarbageTime = DEFAULT_GC_TIME;
	}
	
	// Identity
	public final String textureFilename;
	public final float length;
	public final TextureZERO zero;
	// Rendering parameters
	public final Shader normalShader;
	public final float textureLodBias;
	public final boolean isStreamed;
	public final TextureFilter minFilter;
	public final TextureFilter magFilter;
	public final TextureWrap uWrap;
	public final TextureWrap vWrap;
	public final float tGarbageTime;
	// Current
	protected TextureLoader loader = null;
	protected Texture texture = null;
	protected float tLastUsed = -1;

    /**
     * An empty material, meant to replace with own texture.
     * @param length
     */
	public SimpleMaterial(float length) {
		this(GL20.GL_ALWAYS, false,
				GL20.GL_NEVER,
				GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
				null, length,
				new TextureZERO(),
				Shader.load(SimpleMaterial.DEFAULT_NORMAL_SHADER),
				0f,
				false,
				TextureFilter.Linear, TextureFilter.Linear,
				TextureWrap.ClampToEdge, TextureWrap.ClampToEdge,
				SimpleMaterial.DEFAULT_GC_TIME
		);
	}

	public SimpleMaterial(String textureFilename, CompileConfig config) {
		this(textureFilename, textureFilename, config);
	}

    public SimpleMaterial(String textureFilename, String imageFilename, CompileConfig config) {
        this(textureFilename, new Pixmap(File.open(imageFilename)), config);
    }

    /**
	 * Compiles the texture and creates the material, using the parameters set by CompileConfig
	 * @param textureFilename
	 * @param config
	 */
    public SimpleMaterial(String textureFilename, Pixmap image, CompileConfig config) {
		super(config.depthFunc, config.depthMask, config.faceCullingMode, config.srcBlendFunc, config.destBlendFunc);
		// Compiler ctor
		// Get length
		this.length = config.cropLength > 0 ? config.cropLength : ((float)image.getHeight() / (float)image.getWidth());
		
		// Process image
		image = config.process(image);

		// Check if image can fit in FS
		if(!config.mipmapping && (image.getWidth() * image.getHeight()) <= minTextureSize) {
			this.zero = new TextureZERO(image);
			this.textureFilename = null; 
		}
		else {
			if(!config.isStreamed)
				this.zero = new TextureZERO();
			else
				this.zero = new TextureZERO(config.compileLodZero(image));
			config.compile(textureFilename, image, config.compressQuality);
			this.textureFilename = textureFilename;
			// Release
			image.dispose();

		}

		// Set rendering parameters
		this.normalShader = config.normalShader;
        this.textureLodBias = config.textureLodBias;
        this.isStreamed = config.isStreamed;
		this.minFilter = config.minFilter;
		this.magFilter = config.magFilter;
		this.uWrap = config.uWrap;
		this.vWrap = config.vWrap;
		this.tGarbageTime = config.tGarbageTime;
	}
	
	/**
	 * Creates the material using the parameters set by CompileConfig. Assumes texture has been compiled.
	 * @param textureFilename
	 * @param config
	 * @param length
	 * @param zero
	 */
	public SimpleMaterial(String textureFilename, CompileConfig config, float length, TextureZERO zero) {
		super(config.depthFunc, config.depthMask, config.faceCullingMode, config.srcBlendFunc, config.destBlendFunc);
		this.textureFilename = textureFilename;
		this.length = length;
		this.zero = zero;
		this.normalShader = config.normalShader;
        this.textureLodBias = config.textureLodBias;
        this.isStreamed = config.isStreamed;
		this.minFilter = config.minFilter;
		this.magFilter = config.magFilter;
		this.uWrap = config.uWrap;
		this.vWrap = config.vWrap;
		this.tGarbageTime = config.tGarbageTime;
	}

	@MassConstructor
	public SimpleMaterial(int depthFunc, boolean depthMask, int faceCullingMode,
			int srcBlendFunc, int destBlendFunc, String textureFilename,
			float length, TextureZERO zero,  
			Shader normalShader, float textureLodBias, boolean isStreamed,
			TextureFilter minFilter, TextureFilter magFilter, 
			TextureWrap uWrap, TextureWrap vWrap, float tGarbageTime
	) {
		super(depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc);
		this.textureFilename = textureFilename;
		this.length = length;
		this.zero = zero;
		this.normalShader = normalShader;
        this.textureLodBias = textureLodBias;
        this.isStreamed = isStreamed;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
		this.tGarbageTime = tGarbageTime;
	}
	@Override
	public Object[] mass() {
		return new Object[] { depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc, textureFilename, length, zero, normalShader, textureLodBias, isStreamed, minFilter, magFilter, uWrap, vWrap, tGarbageTime };
	}

	@Override
	public void load() {
		if(textureFilename == null)
			return;		// n/a
        // Use GC
        if(tLastUsed == -1)
            GarbageCollector.add(this);
        tLastUsed = Sys.getTime();
        // Use texture
        if(texture == null) {
            // Load texture
            if(loader == null) {
                loader = new TextureLoader(textureFilename + ".texture");
                // Check if streaming is allowed
//                if(!isStreamed)
//                    loader.finish();
//                else
                    loader.start();
            }
//            else if(!isStreamed)
//                loader.finish();
            if(loader.isComplete()) {
                Texture[] textures = loader.get();
                texture = textures[0];
                texture.setFilter(minFilter, magFilter);
                texture.setWrap(uWrap, vWrap);
            }
        }
	}

	public void replaceTexture(Texture texture) {
		if(this.texture == texture)
			return;
        if(loader != null) {
            loader.release();
            loader = null;
        }
        this.texture = texture;
        if(texture == null)
            return;
		if(texture.getMinFilter() != minFilter || texture.getMagFilter() != magFilter)
			texture.setFilter(minFilter, magFilter);
		if(texture.getUWrap() != uWrap || texture.getVWrap() != vWrap)
			texture.setWrap(uWrap, vWrap);
	}

	@Override
	public boolean isLoaded() {
		return texture != null;
	}

	@Override
	public void ensureLoaded() {
		tLastUsed = Sys.getTime();
		if(texture != null || textureFilename == null)
			return;		// already loaded or no need to load
		// Else finish loading now
		if(loader == null) {
			// Else load now
			loader = new TextureLoader(textureFilename + ".texture");
			// Require GC
			GarbageCollector.add(this);
		}
		loader.finish();
		Texture[] textures = loader.get();
		texture = textures[0];
		texture.setFilter(minFilter, magFilter);
		texture.setWrap(uWrap, vWrap);
	}

	@Override
	public void initialize(MaterialInstance m) {
		// Ensure attributes
		m.getAttribute(ColorAttribute.class, 0);
	}

	@Override
	public void bindTexture(int target) {
		if(!isStreamed)
			ensureLoaded();
		zero.bind(texture, target, minFilter, magFilter, uWrap, vWrap);
	}

	@Override
	public Shader bind() {
		// Bind relevant textures and shader
        bindTexture(0);
        ShaderProgram program = normalShader.bind();		// Normal shader
        program.setUniformi(u_texture, 0);
        program.setUniformf(u_textureLodBias, textureLodBias);
		// Return primary shader
		return normalShader;
	}

	@Override
	public void unbind() {
		// nothing
	}
	
	@Override
	public float getLength() {
		return length;
	}

	@Override
	public boolean performGC(boolean forced) {
		float elapsed = Sys.getTime() - tLastUsed; 
		if(elapsed < tGarbageTime && !forced)
			return false;		// no GC
		// Else GC now
		if(loader != null) {
			loader.release();
			loader = null;
		}
		zero.unload();
		texture = null;
		tLastUsed = -1;
		return true;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + textureFilename;
	}
}
