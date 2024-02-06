package game31.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.graphics2d.Material;
import sengine.materials.SimpleMaterial;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenBlurMaterial extends ScreenMaterial {

    public static final String u_alphaTexture = "u_alphaTexture";

    private final SimpleMaterial alphaTexture;

    public ScreenBlurMaterial(String alphaFilename) {
        super("shaders/ScreenBlurMaterial.glsl", SaraRenderer.RENDER_FINAL, GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        alphaTexture = Material.load(alphaFilename);
    }


    @Override
    protected void program(ShaderProgram program) {

        alphaTexture.bindTexture(1);
        program.setUniformi(u_alphaTexture, 1);

        // Reset
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

    }

    @Override
    public void load() {
        alphaTexture.load();
    }
}
