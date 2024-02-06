package game31.renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import game31.Globals;
import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;
import sengine.materials.ColorAttribute;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenMaterial extends Material {

    public static final String u_resolution = "u_resolution";
    public static final String u_aspectRatio = "u_aspectRatio";
    public static final String u_texture = "u_texture";
    public static final String u_time = "u_time";
    public static final String u_random = "u_random";

    public final int renderBufferIndex;

    public final Shader shader;

    public ScreenMaterial() {
        this(SaraRenderer.RENDER_FINAL, GL20.GL_ONE, GL20.GL_ZERO);
    }

    public ScreenMaterial(int renderBufferIndex) {
        this(renderBufferIndex, GL20.GL_ONE, GL20.GL_ZERO);
    }

    public ScreenMaterial(int renderBufferIndex, int srcBlendFunc, int destBlendFunc) {
        this("shaders/ScreenMaterial.glsl", renderBufferIndex, srcBlendFunc, destBlendFunc);
    }

    // For screen effects
    public ScreenMaterial(String shaderFilename) {
        this(shaderFilename, SaraRenderer.RENDER_FINAL, GL20.GL_ONE, GL20.GL_ZERO);
    }

    public ScreenMaterial(String shaderFilename, int renderBufferIndex, int srcBlendFunc, int destBlendFunc) {
        super(GL20.GL_ALWAYS, false, GL20.GL_NEVER, srcBlendFunc, destBlendFunc);

        this.shader = Shader.load(shaderFilename);

        this.renderBufferIndex = renderBufferIndex;;
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

        // Bind material
        Texture texture = SaraRenderer.renderer.renderBuffers[renderBufferIndex].getColorBufferTexture();
        texture.bind();
        program.setUniformi(u_texture, 0);

        // Default values

        FrameBuffer buffer = SaraRenderer.renderer.renderBuffers[0];

        int bufferWidth = buffer.getWidth();
        int bufferHeight = buffer.getHeight();
        program.setUniformf(u_resolution, bufferWidth, bufferHeight);
        program.setUniformf(u_aspectRatio, (float)bufferWidth / (float)bufferHeight);

        float time = Sys.getTime() % 60f;           // Shaders cant handle such a high number
        program.setUniformf(u_time, time);

        program.setUniformf(u_random, (float) Math.random());

        // Allow sub classes to program
        program(program);

        return shader;
    }

    protected void program(ShaderProgram program) {
        // For subclasses
    }

    @Override
    public void unbind() {
        // nothing special
    }

    @Override
    public float getLength() {
        return Globals.LENGTH;
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
