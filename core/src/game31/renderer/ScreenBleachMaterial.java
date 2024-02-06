package game31.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenBleachMaterial extends ScreenMaterial {

    public static final String u_strength = "u_strength";


    public float strength = 3f;         // 3f

    public ScreenBleachMaterial() {
        super("shaders/ScreenBleachMaterial.glsl");
    }

    @Override
    protected void program(ShaderProgram program) {
        program.setUniformf(u_strength, strength);
    }
}
