package game31.triggers;

import game31.Globals;
import game31.Grid;
import game31.renderer.LiquidCrystalGeneratorMaterial;
import game31.renderer.SaraRenderer;
import game31.renderer.ScreenMaterial;
import sengine.Entity;
import sengine.Sys;
import sengine.calc.Graph;
import sengine.graphics2d.Sprite;

public class LcdBurnEffect extends Entity<Grid> {


    private final LiquidCrystalGeneratorMaterial generatorMaterial;

    private Graph amountGraph = null;
    private Graph acidGraph = null;
    private float tGraphStarted = Float.MAX_VALUE;

    private final Sprite effectBufferGenerator;
    private final Sprite screen;


    public void startGraph(Graph amountGraph, Graph acidGraph) {
        this.amountGraph = amountGraph;
        this.acidGraph = acidGraph;
        tGraphStarted = getRenderTime();
    }

    public LcdBurnEffect() {
        generatorMaterial = new LiquidCrystalGeneratorMaterial();

        effectBufferGenerator = new Sprite(Globals.LENGTH, generatorMaterial);
        screen = new Sprite(Globals.LENGTH, new ScreenMaterial(SaraRenderer.RENDER_EFFECT1));
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        v.screen.effectBufferGenerator = effectBufferGenerator;
        v.screen.screen = screen;

        // Request max framerate
        Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);
        v.idleScare.reschedule();

        if(tGraphStarted > renderTime || amountGraph == null || acidGraph == null)
            return;     // nothing
        float elapsed = renderTime - tGraphStarted;
        if(elapsed > amountGraph.getLength() && elapsed > acidGraph.getLength()) {
            generatorMaterial.amount = amountGraph.getEnd();
            generatorMaterial.acid = acidGraph.getEnd();
            tGraphStarted = Float.MAX_VALUE;
        }
        else if(elapsed > amountGraph.getLength()) {
            generatorMaterial.amount = amountGraph.getEnd();
            generatorMaterial.acid = acidGraph.generate(elapsed);
        }
        else if(elapsed > acidGraph.getLength()) {
            generatorMaterial.amount = amountGraph.generate(elapsed);
            generatorMaterial.acid = acidGraph.getEnd();
        }
        else {
            generatorMaterial.amount = amountGraph.generate(elapsed);
            generatorMaterial.acid = acidGraph.generate(elapsed);
        }
    }

    @Override
    protected void recreate(Grid v) {
        super.recreate(v);

        // Request burn effect generation
        SaraRenderer.renderer.requestEffectBuffer(Globals.r_liquidCrystalEffectResolution);

        // Remove any overlay
        v.screen.overlay = null;
        v.screen.animateOverlay(null, null);
    }

    @Override
    protected void release(Grid v) {
        super.release(v);

        // Clear effect
        v.screen.effectBufferGenerator = null;
        v.screen.screen = v.screen.defaultScreen;
    }
}
