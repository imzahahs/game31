package game31.renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.Sys;

public class LsdScreenMaterial extends ScreenMaterial {

    public static final String u_progress = "u_progress";     // Hue progress, 0.0 ~ 1.0
    public static final String u_amount = "u_amount";

    public float progress = 0.5f;       // 0.0 ~ 1.0
    public float amount = 0.2f;

    public LsdScreenMaterial() {
        this(SaraRenderer.RENDER_EFFECT1);
    }

    public LsdScreenMaterial(int renderBufferIndex) {
        super("shaders/LsdMaterial.glsl", renderBufferIndex, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    protected void program(ShaderProgram program) {

        program.setUniformf(u_progress, progress);
        program.setUniformf(u_amount, 1.0f - amount);
    }
}
