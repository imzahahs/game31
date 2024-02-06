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

public class TextEffectGenerator extends Material {


    public static final String u_texture = "u_texture";
    public static final String u_backBuffer = "u_backBuffer";

    public static final String u_resolution = "u_resolution";

    public static final String u_time = "u_time";

    public static final String u_amount = "u_amount";

    private final Shader shader;

    public float amount = 0.05f;

    public TextEffectGenerator() {
        super(GL20.GL_ALWAYS, false, GL20.GL_NEVER, GL20.GL_ONE, GL20.GL_ZERO);

        this.shader = Shader.load("shaders/TextEffectGenerator.glsl");
    }


    @Override
    public void initialize(MaterialInstance m) {
        // nothing
    }

    @Override
    public Shader bind() {

        // Bind shader
        ShaderProgram program = shader.bind();

        // Bind material
        // Back buffer
        FrameBuffer backBuffer = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_TEXT_EFFECT2];
        Texture texture = backBuffer.getColorBufferTexture();
        texture.bind(1);
        program.setUniformi(u_backBuffer, 1);
        // Color buffer
        texture = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_TEXT_EFFECT_BUFFER].getColorBufferTexture();
        texture.bind(0);
        program.setUniformi(u_texture, 0);


        // Program
        program.setUniformf(u_resolution, backBuffer.getWidth(), backBuffer.getHeight());

        program.setUniformf(u_amount, amount);

        float time = Sys.getTime() % 60f;           // Shaders cant handle such a high number
        program.setUniformf(u_time, time);

        return shader;
    }

    @Override
    public void unbind() {
        // nothing
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
