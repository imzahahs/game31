package game31.renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import game31.Globals;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;
import sengine.materials.ColorAttribute;

public class TextEffectCompositor extends Material {


    public static final String u_texture = "u_texture";

    public static final String u_blackThreshold = "u_blackThreshold";
    public static final String u_whiteThreshold = "u_whiteThreshold";

    private final Shader shader;

    public float blackThreshold = 0.7f;
    public float whiteThreshold = 0.8f;

    public TextEffectCompositor() {
        super(GL20.GL_ALWAYS, false, GL20.GL_NEVER, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        this.shader = Shader.load("shaders/TextEffectCompositor.glsl");
    }


    @Override
    public void initialize(MaterialInstance m) {
        // Ensure attributes
        m.getAttribute(ColorAttribute.class, 0);
    }

    @Override
    public Shader bind() {

        // Bind shader
        ShaderProgram program = shader.bind();

        // Bind material
        FrameBuffer backBuffer = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_TEXT_EFFECT1];
        Texture texture = backBuffer.getColorBufferTexture();
        texture.bind();
        program.setUniformi(u_texture, 0);

        program.setUniformf(u_blackThreshold, blackThreshold);
        program.setUniformf(u_whiteThreshold, whiteThreshold);

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
