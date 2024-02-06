package game31.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenVhsMaterial extends ScreenMaterial {

    public static final String u_random = "u_random";
    public static final String u_strength = "u_strength";

    public float strength = 0f;

    public ScreenVhsMaterial() {
        super("shaders/ScreenVhsMaterial.glsl");
    }


    @Override
    protected void program(ShaderProgram program) {
        // Uniforms
        program.setUniformf(u_random, (float) Math.random());

        program.setUniformf(u_strength, strength);
    }

}
