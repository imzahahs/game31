package game31.triggers;

import game31.Globals;
import game31.Grid;
import game31.app.homescreen.Homescreen;
import game31.glitch.MpegGlitch;
import game31.renderer.SaraRenderer;
import sengine.Entity;
import sengine.animation.Animation;
import sengine.graphics2d.Sprite;
import sengine.ui.Menu;
import sengine.ui.StaticSprite;

public class JumpscareScreen extends Menu<Grid> implements Homescreen.App {

    public static class ScareInfo {
        public final Sprite mat;
        public final float time;

        public ScareInfo(Sprite mat, boolean crop, float time) {
            if(mat.length != Globals.LENGTH) {
                if(crop) {
                    mat = new Sprite(mat.length, mat.getMaterial());
                    mat = mat.crop(Globals.LENGTH);
                }
                else
                    mat = new Sprite(Globals.LENGTH, mat.getMaterial());
            }

            this.mat = mat;
            this.time = time;
        }
    }

    private final ScareInfo[] scares;
    private final StaticSprite screen;

    private MpegGlitch glitch;

    private Runnable onFinished;

    private int index;
    private float tScareEndScheduled;

    private Animation switchAnim;

    public JumpscareScreen onFinished(Runnable onFinished) {
        this.onFinished = onFinished;
        return this;
    }

    public JumpscareScreen animation(Animation startAnim, Animation switchAnim, Animation idleAnim, Animation endAnim) {
        screen.animation(startAnim, idleAnim, endAnim);
        this.switchAnim = switchAnim;
        return this;
    }

    public JumpscareScreen glitch(MpegGlitch glitch) {
        this.glitch = glitch;
        return this;
    }

    public JumpscareScreen load() {
        for(ScareInfo scare : scares)
            scare.mat.load();
        return this;
    }

    public JumpscareScreen(ScareInfo ... scares) {
        this.scares = scares;

        // Screen
        screen = new StaticSprite()
                .viewport(viewport)
                .target(SaraRenderer.TARGET_TRANSITION)
                .passThroughInput(false)
                .attach();

        elements.add(screen);
    }


    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        index = 0;
        screen.visual(scares[0].mat);
        tScareEndScheduled = scares[0].time;

        if(glitch != null)
            glitch.attach(grid);

        load();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        if(onFinished != null)
            onFinished.run();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(renderTime > tScareEndScheduled) {
            // Switch
            index++;
            if(index < scares.length) {
                ScareInfo info = scares[index];
                screen.visual(info.mat);
                if(switchAnim != null)
                    screen.windowAnimation(switchAnim.startAndReset(), true, false);
                tScareEndScheduled = renderTime + info.time;
            }
            else {
                tScareEndScheduled = Float.MAX_VALUE;
                detachWithAnim();
                if(glitch != null)
                    glitch.detachWithAnim();
            }
        }
    }


    @Override
    public Entity<?> open() {
        attach(Globals.grid.screensGroup);
        return null;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {

    }
}
