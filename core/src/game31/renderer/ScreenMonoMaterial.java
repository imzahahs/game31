package game31.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by Azmi on 8/25/2017.
 */

public class ScreenMonoMaterial extends ScreenMaterial {


    public static final String u_delta = "u_delta";
    public static final String u_visibility = "u_visibility";

    public float deltaBase = 0;
    public float deltaVariance = 0;
    public float visibility = 1f;

    public ScreenMonoMaterial() {
        super("shaders/ScreenMonoMaterial.glsl");
    }

    @Override
    protected void program(ShaderProgram program) {

        float delta = deltaBase + (float)(Math.random() * deltaVariance);
        program.setUniformf(u_delta, delta);

        // Visibility
        program.setUniformf(u_visibility, visibility);
    }
}
