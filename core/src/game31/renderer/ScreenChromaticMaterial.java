package game31.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.Sys;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.VibrationGraph;

/**
 * Created by Azmi on 28/6/2016.
 */
public class ScreenChromaticMaterial extends ScreenMaterial {

    public static final String u_cadistortion = "u_cadistortion";

    public final Graph waveGraph = new VibrationGraph(3f, new ConstantGraph(5f), new ConstantGraph(2.5f) );


    public ScreenChromaticMaterial() {
        super("shaders/ScreenChromaticMaterial.glsl");
    }


    @Override
    protected void program(ShaderProgram program) {
        float chromaticDistortion = waveGraph.generate(Sys.getTime());

        program.setUniformf(u_cadistortion, chromaticDistortion);

    }
}
