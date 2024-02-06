package game31.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by Azmi on 6/2/2017.
 */

public class ScreenShampainMaterial extends ScreenMaterial {

    public static final String u_power = "u_power";

    public float power = 0f;

    public ScreenShampainMaterial() {
        super("shaders/ScreenShampainGlitch.glsl");
    }


    @Override
    protected void program(ShaderProgram program) {
        program.setUniformf(u_power, power);
    }

}