package game31.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.Sys;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenColorSplitMaterial extends ScreenMaterial {

    public static final String u_delta = "u_delta";

    public float deltaX = 0;
    public float deltaY = 0;

    public void setDelta(float delta) {
        deltaX = Math.round(Sys.system.getWidth() * delta);
        deltaY = Math.round(Sys.system.getHeight() * delta);
    }

    public ScreenColorSplitMaterial() {
        super("shaders/ScreenColorSplitMaterial.glsl");
    }

    @Override
    protected void program(ShaderProgram program) {
        program.setUniformf(u_delta, deltaX, deltaY);
    }
}
