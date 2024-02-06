package game31.renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenFisheyeMaterial extends ScreenMaterial {

    public static final String u_strength = "u_strength";
    public static final String u_radius = "u_radius";
    public static final String u_center = "u_center";

    public ScreenFisheyeMaterial() {
        super("shaders/ScreenFisheyeMaterial.glsl", SaraRenderer.RENDER_FIRST, GL20.GL_ONE, GL20.GL_ZERO);
    }


    public float strength = 0.5f;
    public float radius = 1.5f;
    public final Vector2 center = new Vector2();

    @Override
    protected void program(ShaderProgram program) {


        program.setUniformf(u_center, center);
        program.setUniformf(u_strength, strength);
        program.setUniformf(u_radius, radius);


    }
}
