package sengine.materials;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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

/**
 * Created by Azmi on 8/18/2017.
 */

public class AnimatedMaterial extends Material implements GarbageCollector.Collectible, MassSerializable {
    public static final String ID = "AnimatedMaterial";

    public static final String CFG_EXTENSION = ".AnimatedMaterial";

    public static class Instance extends Material implements MassSerializable {

        public final AnimatedMaterial material;
        private MaterialInstance mesh = null;

        private int frame = 0;

        public void frame(int frame) {
            if(frame >= material.numFrames)
                frame = material.numFrames - 1;
            this.frame = frame;
        }

        public int frame() {
            return frame;
        }

        public void progress(float progress) {
            if(progress > 1f)
                progress = 1f;
            else if(progress < 0f)
                progress = 0f;
            frame = (int)(progress * (material.numFrames - 1));
        }


        @MassConstructor
        public Instance(AnimatedMaterial material) {
            super(material.depthFunc, material.depthMask, material.faceCullingMode, material.srcBlendFunc, material.destBlendFunc);

            this.material = material;
        }

        @Override
        public Object[] mass() {
            return new Object[] { material };
        }

        @Override
        public void load() {
            material.load();
        }

        @Override
        public boolean isLoaded() {
            return material.isLoaded();
        }

        @Override
        public void ensureLoaded() {
            material.ensureLoaded();
        }


        @Override
        public void initialize(MaterialInstance m) {
            if(mesh != null) {
                // This instance is in use, create new
                Instance instance = new Instance(material);
                m.setMaterial(instance, m.getAttributes());
                return;
            }
            // Remember
            mesh = m;
            // Ensure attributes
            m.getAttribute(ColorAttribute.class, 0);            // Supported
        }

        @Override
        public void bindTexture(int target) {
            if(!material.isStreamed)
                ensureLoaded();
            Texture texture = material.textures == null ? null : material.textures[frame];
            material.zero.bind(texture, target, material.minFilter, material.magFilter, material.uWrap, material.vWrap);
        }

        @Override
        public Shader bind() {
            // Bind relevant textures and shader
            bindTexture(0);
            ShaderProgram program = material.normalShader.bind();		// Normal shader
            program.setUniformi(u_texture, 0);
            // Return primary shader
            return material.normalShader;
        }

        @Override
        public void unbind() {
            // nothing
        }

        @Override
        public float getLength() {
            return material.getLength();
        }
    }




    public static class Type extends Material.Type {
        public Type() {
            super(ID);
        }

        @Override
        protected Material create(String name) {
            // Load config
            CompileConfig config = CompileConfig.load(name);

            return new AnimatedMaterial(name, config);
        }
    }

    public static final String DEFAULT_NORMAL_SHADER = "shaders/SimpleMaterial-normal.glsl";

    public static final float DEFAULT_GC_TIME = 15.0f;
    public static final float DEFAULT_LODZERO_MINIFICATION = 0.05f;
    public static final float DEFAULT_RESIZING_GAMMA = 2.2f;

    public static boolean squareTextures = false;
    public static boolean potTextures = false;

    // Uniforms
    public static final String u_texture = "u_texture";


    public static class CompileConfig {
        public static CompileConfig load(String filename) {
            CompileConfig config = new CompileConfig();
            Config.load(filename + CFG_EXTENSION, false).apply(config);
            return config;
        }

        public float onScreenSize = 1.0f;
        public int minSize = -1;
        public int maxSize = -1;
        public float cropLength = -1f;          // no cropping
        public boolean mipmapping = false;
        public boolean pma = false;
        public float compressQuality = 1.0f;            // max quality
        public Texture.TextureFilter minFilter = Texture.TextureFilter.Linear; // TextureFilter.MipMapLinearNearest;
        public Texture.TextureFilter magFilter = Texture.TextureFilter.Linear; // TextureFilter.MipMapLinearNearest;
        public Texture.TextureWrap uWrap = Texture.TextureWrap.ClampToEdge;
        public Texture.TextureWrap vWrap = Texture.TextureWrap.ClampToEdge;
        public float lodZeroMinification = DEFAULT_LODZERO_MINIFICATION;
        public float resizingGamma = DEFAULT_RESIZING_GAMMA;

        public int depthFunc = GL20.GL_ALWAYS;
        public boolean depthMask = false;
        public int faceCullingMode = GL20.GL_NEVER;
        public int srcBlendFunc = GL20.GL_SRC_ALPHA;
        public int destBlendFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
        public Shader normalShader = Shader.load(DEFAULT_NORMAL_SHADER);
        public boolean isStreamed = true;
        public float tGarbageTime = DEFAULT_GC_TIME;

        public int numFrames = 0;      // automatic if not specified
        public float duration = 0f;     // not important

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
            if(uWrap != Texture.TextureWrap.ClampToEdge || vWrap != Texture.TextureWrap.ClampToEdge)
                pot = true;			// required pot if using tiling, for iOS
            if(mipmapping)
                image = TextureUtils.validateDimensions(image, onScreenSize, minSize, maxSize, true, squareTextures, false);
            else
                image = TextureUtils.validateDimensions(image, onScreenSize, minSize, maxSize, pot, squareTextures, false);
            return image;
        }

        public Pixmap compile(String filename) {
            String[] paths = File.splitExtension(filename);

            // Count number of frames if not specified
            if(numFrames <= 0) {
                numFrames = 0;
                // Count number of frames
                while(File.exists(filename + "/" + numFrames + "." + paths[1]))
                    numFrames++;
                if(numFrames == 0)
                    throw new RuntimeException("No frames found for \"" + filename + "\"");
            }

            // Load first frame
            Pixmap firstFrame = new Pixmap(File.open(filename + "/0." + paths[1]));
            firstFrame = process(firstFrame);

            // Check if texture has already been compiled
            if(File.open(filename + ".texture", false) != null)
                return firstFrame;              // already loaded
            Sys.info(ID, "Converting animation: " + filename);

            Pixmap[][] frames = new Pixmap[numFrames][];

            for(int c = 0; c < numFrames; c++) {
                Pixmap frame;
                if(c == 0)
                    frame = TextureUtils.duplicate(firstFrame);
                else {
                    frame = new Pixmap(File.open(filename + "/" + c + "." + paths[1]));
                    frame = process(frame);
                }
                Pixmap[] levels;
                if(!mipmapping)
                    levels = new Pixmap[] { frame };
                else {
                    // Else using mipmapping, create levels
                    int numLevels = TextureUtils.getLevels(frame.getWidth(), frame.getHeight());
                    levels = new Pixmap[numLevels];
                    levels[0] = TextureUtils.duplicate(frame);	// Duplicate as were using image to resize to other lod levels
                    for(int l = 1; l < levels.length; l++) {
                        frame = TextureUtils.resizeHalf(frame, resizingGamma);
                        levels[l] = TextureUtils.duplicate(frame);
                    }
                    frame.dispose();
                }
                frames[c] = levels;
            }

            // Save
            TextureFile textureFile = new TextureFile();
            textureFile.save(filename + ".texture", frames, compressQuality);
            textureFile.clear();
            // Release all
            for(int c = 0; c < numFrames; c++) {
                Pixmap[] levels = frames[c];
                for(int l = 0; l < levels.length; l++) {
                    levels[l].dispose();
                }
                frames[c] = null;
            }
            return firstFrame;
        }

    }

    // Identity
    public final String textureFilename;
    public final float length;
    public final TextureZERO zero;
    // Rendering parameters
    public final Shader normalShader;
    public final boolean isStreamed;
    public final Texture.TextureFilter minFilter;
    public final Texture.TextureFilter magFilter;
    public final Texture.TextureWrap uWrap;
    public final Texture.TextureWrap vWrap;
    public final float tGarbageTime;

    public final int numFrames;
    public final float duration;

    // Current
    protected TextureLoader loader = null;
    protected Texture[] textures = null;
    protected float tLastUsed = -1;


    // Compiler ctor
    public AnimatedMaterial(String textureFilename, CompileConfig config) {
        super(config.depthFunc, config.depthMask, config.faceCullingMode, config.srcBlendFunc, config.destBlendFunc);

        // Compile and get first frame
        Pixmap image = config.compile(textureFilename);

        // Get length
        this.length = config.cropLength > 0 ? config.cropLength : ((float)image.getHeight() / (float)image.getWidth());

        // Check if need zero
        if(!config.isStreamed)
            this.zero = new TextureZERO();
        else
            this.zero = new TextureZERO(config.compileLodZero(image));

        this.textureFilename = textureFilename;

        // Clear
        image.dispose();

        // Set rendering parameters
        this.normalShader = config.normalShader;
        this.isStreamed = config.isStreamed;
        this.minFilter = config.minFilter;
        this.magFilter = config.magFilter;
        this.uWrap = config.uWrap;
        this.vWrap = config.vWrap;
        this.tGarbageTime = config.tGarbageTime;

        this.numFrames = config.numFrames;
        this.duration = config.duration;
    }

    @MassConstructor
    public AnimatedMaterial(int depthFunc, boolean depthMask, int faceCullingMode,
                            int srcBlendFunc, int destBlendFunc, String textureFilename,
                            float length, TextureZERO zero,
                            Shader normalShader, boolean isStreamed,
                            Texture.TextureFilter minFilter, Texture.TextureFilter magFilter,
                            Texture.TextureWrap uWrap, Texture.TextureWrap vWrap, float tGarbageTime,
                            int numFrames, float duration

    ) {
        super(depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc);
        this.textureFilename = textureFilename;
        this.length = length;
        this.zero = zero;
        this.normalShader = normalShader;
        this.isStreamed = isStreamed;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.uWrap = uWrap;
        this.vWrap = vWrap;
        this.tGarbageTime = tGarbageTime;

        this.numFrames = numFrames;
        this.duration = duration;
    }
    @Override
    public Object[] mass() {
        return new Object[] { depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc, textureFilename, length, zero, normalShader, isStreamed, minFilter, magFilter, uWrap, vWrap, tGarbageTime, numFrames, duration };
    }

    @Override
    public void load() {
        // Use GC
        if(tLastUsed == -1)
            GarbageCollector.add(this);
        tLastUsed = Sys.getTime();
        // Use texture
        if(textures == null) {
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
                textures = loader.get();
                if(textures.length != numFrames)
                    throw new RuntimeException("Number of frames mismatched, found \"" + textures.length + "\" required \"" + numFrames + "\"");
                for(int c = 0; c < numFrames; c++) {
                    textures[c].setFilter(minFilter, magFilter);
                    textures[c].setWrap(uWrap, vWrap);
                }
            }
        }
    }

    @Override
    public boolean isLoaded() {
        return textures != null;
    }

    @Override
    public void ensureLoaded() {
        if(textures != null)
            return;		// already loaded or no need to load
        // Else finish loading now
        if(loader == null) {
            // Else load now
            loader = new TextureLoader(textureFilename + ".texture");
            // Require GC
            GarbageCollector.add(this);
        }
        loader.finish();
        textures = loader.get();
        if(textures.length != numFrames)
            throw new RuntimeException("Number of frames mismatched, found \"" + textures.length + "\" required \"" + numFrames + "\"");
        for(int c = 0; c < numFrames; c++) {
            textures[c].setFilter(minFilter, magFilter);
            textures[c].setWrap(uWrap, vWrap);
        }
    }

    @Override
    public void initialize(MaterialInstance m) {
        // Instantiate (cuz we bind different textures per frame)
        Instance instance = new Instance(this);
        m.setMaterial(instance, m.getAttributes());
    }

    @Override
    public Shader bind() {
        return null;                // will never happen
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
        textures = null;
        tLastUsed = -1;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + textureFilename;
    }



}
