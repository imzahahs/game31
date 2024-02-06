package game31.renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

import game31.Globals;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;

public class LightingGeneratorMaterial extends Material {


    public static final String u_texture = "u_texture";

    public static final String u_center = "u_center";

    public static final String u_lightingEdge = "u_lightingEdge";
    public static final String u_lightingScale = "u_lightingScale";
    public static final String u_lightingGamma = "u_lightingGamma";


    public static final String u_raysScale = "u_raysScale";
    public static final String u_raysGamma = "u_raysGamma";

    private final Shader shader;

    public final Vector2 center = new Vector2(1.4f, 1.2f);

    public float lightingEdge = 0.7f;
    public float lightingScale = 0.05f;
    public float lightingGamma = 0.4545f;

    public float raysScale = 0.05f;
    public float raysGamma = 0.4545f;


    public LightingGeneratorMaterial() {
        super(GL20.GL_ALWAYS, false, GL20.GL_NEVER, GL20.GL_ONE, GL20.GL_ZERO);

        this.shader = Shader.load("shaders/LightingGenerator.glsl");
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
        Texture texture = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_FLAPEE_GAMEPLAY].getColorBufferTexture();
        texture.bind();
        program.setUniformi(u_texture, 0);

        // Program
        program.setUniformf(u_center, center);

        program.setUniformf(u_lightingEdge, lightingEdge);
        program.setUniformf(u_lightingScale, lightingScale);
        program.setUniformf(u_lightingGamma, lightingGamma);

        program.setUniformf(u_raysScale, raysScale);
        program.setUniformf(u_raysGamma, raysGamma);

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
