package game31.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenTearMaterial extends ScreenMaterial {

    public static final String u_strength = "u_strength";

    public float strength = 0.5f;

    public ScreenTearMaterial() {
        super("shaders/ScreenTearMaterial.glsl");
    }

    @Override
    protected void program(ShaderProgram program) {
        program.setUniformf(u_strength, strength);
    }
}
