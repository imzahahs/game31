package game31.renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

import game31.Globals;
import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;
import sengine.materials.ColorAttribute;

/**
 * Created by Azmi on 8/25/2017.
 */

public class RorschachMaterial extends Material {

    public static String SHADER_FILENAME = "shaders/RorschachMaterial.glsl";

    public static float LENGTH = 0.7f;

    public static final String u_time = "u_time";
    public static final String u_complexity = "u_complexity";

    public static final String u_texture = "u_texture";
    public static final String u_textureScale = "u_textureScale";
    public static final String u_textureOffset = "u_textureOffset";

    public static final String u_glitchTime = "u_glitchTime";

    public static final String u_visibility = "u_visibility";

    private final Shader shader;

    private final Vector2 textureScale = new Vector2();
    private final Vector2 textureOffset = new Vector2();

    public float time = 0;
    public float complexity = 55f;
    public float visibility = 1f;

    public void size(float size) {
        float height = LENGTH * size;

        // Calculate scale and offset
        textureScale.set(size, -height / Globals.LENGTH);
        float excessLength = Globals.LENGTH - height;
        textureOffset.set((1f - size) / 2f, 1f - ((excessLength / Globals.LENGTH) / 2f));
    }

    public RorschachMaterial() {
        super(GL20.GL_ALWAYS, false, GL20.GL_NEVER, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shader = Shader.load(SHADER_FILENAME);

        size(1f);
    }

    @Override
    public void load() {
        // nothing
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void ensureLoaded() {
        // nothing
    }

    @Override
    public void initialize(MaterialInstance m) {
        m.getAttribute(ColorAttribute.class, 0);
    }

    @Override
    public Shader bind() {

        // Bind shader
        ShaderProgram program = shader.bind();


        // Bind screen
        Texture texture = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_FINAL].getColorBufferTexture();
        texture.bind();
        program.setUniformi(u_texture, 0);
        program.setUniformf(u_textureOffset, textureOffset);
        program.setUniformf(u_textureScale, textureScale);


//        float time = Sys.getTime() % 60f;           // Mobile shaders cant handle such a high number
//        float rorschachTime = time * 0.0075f;         // TODO
        program.setUniformf(u_time, time % 60f);

        program.setUniformf(u_complexity, complexity);

        // Glitch
        program.setUniformf(u_glitchTime, Sys.getTime());

        // Visibility
        program.setUniformf(u_visibility, visibility);


        return shader;
    }

    @Override
    public void unbind() {
        // nothing
    }

    @Override
    public float getLength() {
        return LENGTH;
    }
}
