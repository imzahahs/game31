package game31.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.Sys;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenArtMaterial extends ScreenMaterial {

    public static final String u_time = "u_time";

    public ScreenArtMaterial() {
        super("shaders/ScreenArtMaterial.glsl");
    }

    @Override
    protected void program(ShaderProgram program) {
        program.setUniformf(u_time, Sys.getTime());
    }
}
