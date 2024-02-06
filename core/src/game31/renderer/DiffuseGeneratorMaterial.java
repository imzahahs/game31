package game31.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DiffuseGeneratorMaterial extends ScreenMaterial {

    public static final String u_backBuffer = "u_backBuffer";
//    public static final String u_resolution = "u_resolution";
    public static final String u_amount = "u_amount";

    public float diffuseAmount = 0.05f;

    public DiffuseGeneratorMaterial() {
        super("shaders/DiffuseGenerator.glsl", SaraRenderer.RENDER_FINAL, GL20.GL_ONE, GL20.GL_ZERO);
    }

    @Override
    protected void program(ShaderProgram program) {

        // Bind backbuffer
        FrameBuffer backBuffer = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_EFFECT2];
        backBuffer.getColorBufferTexture().bind(1);
        program.setUniformi(u_backBuffer, 1);

        // Reset
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        // Uniforms
        program.setUniformf(u_resolution, backBuffer.getWidth(), backBuffer.getHeight());
        program.setUniformf(u_amount, diffuseAmount);
    }
}
