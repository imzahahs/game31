package game31.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.Shader;
import sengine.graphics2d.TextureZERO;
import sengine.materials.SimpleMaterial;
import sengine.utils.Config;

/**
 * Created by Azmi on 28/6/2016.
 */
public class NoiseMaterial extends SimpleMaterial {

    public static final String ID = "NoiseMaterial";


    public static final String DEFAULT_NOISE_SHADER = "shaders/NoiseMaterial.glsl";

    public static class Type extends Material.Type {
        public Type() {
            super(ID);
        }

        @Override
        protected Material create(String name) {
            // Load config
            NoiseCompileConfig config = NoiseCompileConfig.load(name);

            return new NoiseMaterial(name, config);
        }
    }

    public static class NoiseCompileConfig extends CompileConfig {
        public static NoiseCompileConfig load(String filename) {
            NoiseCompileConfig config = new NoiseCompileConfig();
            Config.load(filename + CFG_EXTENSION, false).apply(config);
            return config;
        }

        public NoiseCompileConfig() {
            // Change defaults
            normalShader = Shader.load(DEFAULT_NOISE_SHADER);
        }
    }

    public static final String u_time = "u_time";
    public static final String u_resolution = "u_resolution";


    public NoiseMaterial(String textureFilename, NoiseCompileConfig config) {
        this(textureFilename, textureFilename, config);
    }

    public NoiseMaterial(String textureFilename, String imageFilename, NoiseCompileConfig config) {
        super(textureFilename, imageFilename, config);
    }

    @MassConstructor
    public NoiseMaterial(int depthFunc, boolean depthMask, int faceCullingMode,
                          int srcBlendFunc, int destBlendFunc, String textureFilename,
                          float length, TextureZERO zero,
                          Shader normalShader, float textureLodBias, boolean isStreamed,
                          Texture.TextureFilter minFilter, Texture.TextureFilter magFilter,
                          Texture.TextureWrap uWrap, Texture.TextureWrap vWrap, float tGarbageTime
    ) {
        super(depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc, textureFilename, length, zero, normalShader, textureLodBias, isStreamed, minFilter, magFilter, uWrap, vWrap, tGarbageTime);
    }


    @Override
    public Shader bind() {
        // Bind relevant textures and shader
        bindTexture(0);

        ShaderProgram program = normalShader.bind();		// Normal shader
        program.setUniformi(u_texture, 0);
        program.setUniformf(u_textureLodBias, textureLodBias);
        program.setUniformf(u_resolution, Sys.system.getWidth(), Sys.system.getHeight());

        // Bind time
        float time = Sys.getTime() % 60f;           // Shaders cant handle such a high number
        program.setUniformf(u_time, time);

        // Return primary shader
        return normalShader;
    }
}
