package game31.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import game31.Globals;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;

public class LightingCompositorMaterial extends Material {


    public static final String u_texture = "u_texture";
    public static final String u_lighting = "u_lighting";

    public static final String u_lightingColor = "u_lightingColor";

    public static final String u_raysColor = "u_raysColor";
    public static final String u_raysFog = "u_raysFog";

    private final Shader shader;

    public final Color lightingColor = new Color();
    public final Color raysColor = new Color();

    public float raysFog = 0;


    public LightingCompositorMaterial() {
        super(GL20.GL_ALWAYS, false, GL20.GL_NEVER, GL20.GL_ONE, GL20.GL_ZERO);

        this.shader = Shader.load("shaders/LightingCompositor.glsl");
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
        Texture texture = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_FLAPEE_LIGHTING].getColorBufferTexture();
        texture.bind(1);
        program.setUniformi(u_lighting, 1);

        texture = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_FLAPEE_GAMEPLAY].getColorBufferTexture();
        texture.bind(0);
        program.setUniformi(u_texture, 0);

        // Program
        program.setUniformf(u_lightingColor, lightingColor);

        program.setUniformf(u_raysColor, raysColor);
        program.setUniformf(u_raysFog, raysFog);

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
