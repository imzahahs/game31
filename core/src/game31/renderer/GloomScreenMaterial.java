package game31.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GloomScreenMaterial extends ScreenMaterial {
    public static final String u_screenTexture = "u_screenTexture";

    public static final String u_threshold = "u_threshold";
    public static final String u_blackThreshold = "u_blackThreshold";
    public static final String u_whiteThreshold = "u_whiteThreshold";
    public static final String u_alpha = "u_alpha";

    public float threshold = 0.4f;       // Must be > 0
    public float blackThreshold = 0f;
    public float whiteThreshold = 1f;
    public float alpha = 0f;

    public GloomScreenMaterial() {
        this(SaraRenderer.RENDER_EFFECT1);
    }

    public GloomScreenMaterial(int renderBufferIndex) {
        super("shaders/GloomMaterial.glsl", renderBufferIndex, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    protected void program(ShaderProgram program) {
        // Bind backbuffer
        FrameBuffer backBuffer = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_FINAL];
        backBuffer.getColorBufferTexture().bind(1);
        program.setUniformi(u_screenTexture, 1);

        // Reset
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        program.setUniformf(u_threshold, threshold);
        program.setUniformf(u_blackThreshold, blackThreshold);
        program.setUniformf(u_whiteThreshold, whiteThreshold);
        program.setUniformf(u_alpha, alpha);
    }
}
