package sengine.materials;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.GarbageCollector;
import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;
import sengine.mass.MassSerializable;
import sengine.utils.Config;

public class VideoMaterial extends Material implements GarbageCollector.Collectible, MassSerializable {
    private static final String ID = "VideoMaterial";
    public static final String CFG_EXTENSION = ".VideoMaterial";

    public static final String DEFAULT_NORMAL_SHADER = "shaders/VideoMaterial.glsl";
    public static final String DEFAULT_ANDROID_SHADER = "shaders/VideoMaterial-android.glsl";

    public static final String u_texture = "u_texture";

    public static float GC_TIME = 8f;

    public static class Type extends Material.Type {
        public Type() {
            super(ID);
        }

        @Override
        protected Material create(String name) {
            // Load config
            CompileConfig config = CompileConfig.load(name);

            return new VideoMaterial(name, config);
        }
    }

    public static class CompileConfig {
        public static CompileConfig load(String filename) {
            CompileConfig config = new CompileConfig();
            Config.load(filename + CFG_EXTENSION, false).apply(config);
            return config;
        }

        public int depthFunc = GL20.GL_ALWAYS;
        public boolean depthMask = false;
        public int faceCullingMode = GL20.GL_NEVER;
        public int srcBlendFunc = GL20.GL_ONE;                  // No need blending
        public int destBlendFunc = GL20.GL_ZERO;
        public Shader shader = Shader.load(DEFAULT_NORMAL_SHADER);
        public Shader androidShader = Shader.load(DEFAULT_ANDROID_SHADER);
        public Texture.TextureFilter minFilter = Texture.TextureFilter.Linear; // TextureFilter.MipMapLinearNearest;
        public Texture.TextureFilter magFilter = Texture.TextureFilter.Linear; // TextureFilter.MipMapLinearNearest;
        public Texture.TextureWrap uWrap = Texture.TextureWrap.ClampToEdge;
        public Texture.TextureWrap vWrap = Texture.TextureWrap.ClampToEdge;
    }

    public static class Metadata {
        public final int width;
        public final int height;
        public final float duration;

        public Metadata(int width, int height, float duration) {
            this.width = width;
            this.height = height;
            this.duration = duration;
        }
    }

    public interface PlatformProvider {
        PlatformHandle open(String filename);
        Metadata inspect(String filename);
    }

    public interface PlatformHandle {
        Texture upload(VideoMaterial material, Texture existing, float timestamp, boolean ensureLoaded);
        Pixmap decode(float timestamp);
        void dispose();
    }

    public static PlatformProvider platform;

    // Identity
    public final String filename;
    public final int width;
    public final int height;
    public final float length;
    public final float duration;

    public final Shader shader;
    public final Shader androidShader;
    public final Texture.TextureFilter minFilter;
    public final Texture.TextureFilter magFilter;
    public final Texture.TextureWrap uWrap;
    public final Texture.TextureWrap vWrap;

    // Internal
    private Texture texture = null;
    private PlatformHandle handle = null;
    private float tGarbageScheduled = -1;
    private float requestedPosition = 0;

    public String filenameOverride = null;

    public void clear() {
        if(handle != null) {
            handle.dispose();
            handle = null;
        }
        if(texture != null) {
            texture.dispose();
            texture = null;
        }
        if(tGarbageScheduled != -1) {
            GarbageCollector.remove(this);
            tGarbageScheduled = -1;
        }
        requestedPosition = 0;
    }

    public void show(float position, boolean ensureLoaded) {
        if(handle == null)
            handle = platform.open(filenameOverride != null ? filenameOverride : filename);
        requestedPosition = position;
        texture = handle.upload(this, texture, position, ensureLoaded);
        requestWatchGC();
    }

    public Pixmap get(float position) {
        if(handle == null)
            handle = platform.open(filenameOverride != null ? filenameOverride : filename);
        requestedPosition = position;
        requestWatchGC();
        return handle.decode(position);
    }

    public VideoMaterial(String filename) {
        this(filename, CompileConfig.load(filename));
    }

    public VideoMaterial(String filename, CompileConfig config) {
        super(config.depthFunc, config.depthMask, config.faceCullingMode, config.srcBlendFunc, config.destBlendFunc);


        this.filename = filename;

        this.shader = config.shader;
        this.androidShader = config.androidShader;
        this.minFilter = config.minFilter;
        this.magFilter = config.magFilter;
        this.uWrap = config.uWrap;
        this.vWrap = config.vWrap;

        // Find metadata (should only happen when compiling)
        Metadata metadata = platform.inspect(filename);
        this.width = metadata.width;
        this.height = metadata.height;
        this.length = (float)height / (float)width;
        this.duration = metadata.duration;
    }

    @MassConstructor
    public VideoMaterial(int depthFunc, boolean depthMask, int faceCullingMode, int srcBlendFunc, int destBlendFunc,
                         String filename, Shader shader, Shader androidShader, Texture.TextureFilter minFilter, Texture.TextureFilter magFilter,
                         Texture.TextureWrap uWrap, Texture.TextureWrap vWrap,
                         int width, int height, float duration
    ) {
        super(depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc);


        this.filename = filename;

        this.shader = shader;
        this.androidShader = androidShader;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.uWrap = uWrap;
        this.vWrap = vWrap;

        this.width = width;
        this.height = height;
        this.length = (float)height / (float)width;
        this.duration = duration;
    }

    @Override
    public Object[] mass() {
        return new Object[] { depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc,
                filename, shader, androidShader, minFilter, magFilter, uWrap, vWrap,
                width, height, duration
        };
    }


    @Override
    public boolean performGC(boolean forced) {
        float tSysTime = Sys.getTime();
        if(tSysTime < tGarbageScheduled && !forced)
            return false;       // no GC
        // Else clear
        clear();
        return true;
    }

    private void requestWatchGC() {
        if(tGarbageScheduled == -1)
            GarbageCollector.add(this);
        tGarbageScheduled = Sys.getTime() + GC_TIME;
    }

    @Override
    public void load() {
        if(texture != null)
            return;
        show(requestedPosition, false);
    }

    @Override
    public boolean isLoaded() {
        return texture != null;
    }

    @Override
    public void ensureLoaded() {
        if(texture != null)
            return;
        show(requestedPosition, true);
    }

    @Override
    public void initialize(MaterialInstance m) {
        // nothing
    }

    @Override
    public Shader bind() {
        load();     // request upload

        if(texture == null)
            return null;        // not loaded yet

        texture.bind();

        Shader selected = Gdx.app.getType() == Application.ApplicationType.Android ? androidShader : shader;		// Normal shader
        ShaderProgram program = selected.bind();
        program.setUniformi(u_texture, 0);

        return selected;
    }

    @Override
    public void unbind() {
        // nothing
    }

    @Override
    public float getLength() {
        return length;
    }
}
