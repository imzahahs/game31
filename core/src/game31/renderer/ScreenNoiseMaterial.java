package game31.renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;
import sengine.materials.ColorAttribute;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenNoiseMaterial extends Material {

    public static final String u_time = "u_time";
    public static final String u_resolution = "u_resolution";


    final Shader shader;

    public ScreenNoiseMaterial(int srcBlendFunc, int destBlendFunc) {
        super(GL20.GL_ALWAYS, false, GL20.GL_NEVER, srcBlendFunc, destBlendFunc);

        this.shader = Shader.load("shaders/ScreenNoiseMaterial.glsl");
    }

    @Override
    public void initialize(MaterialInstance m) {
        // Default shader requires color attribute
        m.getAttribute(ColorAttribute.class, 0);
    }

    @Override
    public Shader bind() {
        // Bind shader
        ShaderProgram program = shader.bind();

        program.setUniformf(u_resolution, Sys.system.getWidth(), Sys.system.getHeight());

        float time = Sys.getTime() % 60f;           // Shaders cant handle such a high number
        program.setUniformf(u_time, time);

        return shader;
    }

    @Override
    public void unbind() {
        // nothing special
    }

    @Override
    public float getLength() {
        return Sys.system.getLength();
    }

    @Override
    public void load() {
        // nothing to load
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void ensureLoaded() {
        // nothing to load
    }
}
