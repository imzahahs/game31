package game31;

import com.badlogic.gdx.math.Matrix4;

import game31.renderer.SaraRenderer;
import sengine.Entity;
import sengine.animation.Animation;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 2/7/2016.
 */
public class ScreenTransition extends Entity<Grid> {

    public final Entity<?> from;
    public final Entity<?> to;
    public final Entity<?> target;

    public final Sprite firstScreen;
    public final Sprite secondScreen;

    public final Animation firstScreenAnimType;
    public final Animation secondSecreenAnimType;

    private final Animation.Instance firstScreenAnim;
    private final Animation.Instance secondScreenAnim;

    private boolean rendereredScreens = false;
    private boolean isActive = false;

    private boolean isActive(Grid v) {
        if(isActive)
            return true;
        isActive = true;       // by default
        // Else check
        ScreenTransition transition = null;
        while((transition = v.iterate(transition, ScreenTransition.class, false, null)) != null) {
            if(transition == this)
                continue;           // same
            // Else a different transition was also attached, compare instance
            if(transition.isActive) {
                isActive = false;
                return false;           // another transition is currently active
            }
        }
        // Else no other transition, activate
        return true;
    }


    public ScreenTransition(Entity<?> from, Entity<?> to, Entity<?> target, Sprite firstScreen, Sprite secondScreen, Animation firstScreenAnimType, Animation secondScreenAnimType) {
        this.from = from;
        this.to = to;
        this.target = target;
        this.firstScreen = firstScreen;
        this.secondScreen = secondScreen;
        this.firstScreenAnimType = firstScreenAnimType;
        this.secondSecreenAnimType = secondScreenAnimType;

        to.attach(this);

        from.inputEnabled = false;
        to.inputEnabled = false;
        to.timeMultiplier = 0;


        firstScreenAnim = firstScreenAnimType.startAndReset();
        secondScreenAnim = secondScreenAnimType.startAndReset();
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        if(!isActive(v))
            return;
        Matrices.push();
        Matrices.camera = v.compositor.camera;

        if(!rendereredScreens) {
            SaraRenderer.renderer.startSecondBuffer();
//            Sys.system.streamingDisabledThisFrame = true;     // Control this via isStreamed materials ??
        }
        else
            SaraRenderer.renderer.clearBufferedRenderCalls();
    }


    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        if(!isActive)
            return;
        if(!rendereredScreens) {
            rendereredScreens = true;
            // Hide quick access
            v.notification.hideAccessView();
        }
        SaraRenderer.renderer.stopSecondBuffer();


        Matrix4 m = Matrices.model;
        Matrices.push();

        m.translate(+0.5f, +Globals.LENGTH / 2f, 0);
        m.scale(1f, -1f, 1f);

        Matrices.target = SaraRenderer.TARGET_TRANSITION;

        Matrices.push();

        boolean firstAnimFinished = !firstScreenAnim.updateAndApply(firstScreen, getRenderDeltaTime());
        firstScreen.render();

        Matrices.pop();

        boolean secondAnimFinished = !secondScreenAnim.updateAndApply(secondScreen, getRenderDeltaTime());
        secondScreen.render();

        Matrices.pop();

        if(firstAnimFinished && secondAnimFinished) {
            from.detach();
            to.attach(target);
            to.inputEnabled = true;
            from.inputEnabled = true;
            to.timeMultiplier = 1f;
            detach();
        }

        Matrices.pop();     // from render();
    }

}
