package game31.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.materials.SimpleMaterial;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenMpegMaterial extends ScreenMaterial {

    public static final String u_lsdMix = "u_lsdMix";
    public static final String u_power = "u_power";
    public static final String u_noiseTexture = "u_noiseTexture";



    private final SimpleMaterial noise;

    public float lsdMix = 0f;
    public float power = 0f;

    public ScreenMpegMaterial() {
        super("shaders/ScreenMpegMaterial.glsl");

        noise = Material.load("system/noise.png");
    }


    @Override
    protected void program(ShaderProgram program) {
        // Noise texture
        noise.bindTexture(1);
        program.setUniformi(u_noiseTexture, 1);

        // Reset
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        // Uniforms
        program.setUniformf(u_lsdMix, 1.0f - lsdMix);
        program.setUniformf(u_power, power);
    }

    @Override
    public void unbind() {
//        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);           // TODO: not necessary ?
    }

    @Override
    public void load() {
        noise.load();
    }
}
