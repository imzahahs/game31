package game31;

import com.badlogic.gdx.Input;

import game31.gb.GBCreditsMenu;
import sengine.Sys;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 9/25/2017.
 */

public class CreditsMenu extends Menu<Grid> implements OnClick<Grid> {

    public static class Internal {
        public UIElement<?> window;

        public UIElement<?> creditsGroup;
        public float tMoveStart;
        public float moveSpeedY;
        public float tCreditsMaxTime;

        public Clickable closeButton;

    }


    private final Builder<Object> builder;
    private Internal s;

    private float tMoveStartScheduled = 0;
    private Runnable onFinish;

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
        }

        s = internal;
        s.window.viewport(viewport).attach();

        tMoveStartScheduled = getRenderTime() + s.tMoveStart;
    }

    public void show(Runnable onFinish) {
        this.onFinish = onFinish;
        attach(Globals.grid.compositor);
    }

    public CreditsMenu() {
        builder = new Builder<Object>(GBCreditsMenu.class, this);
        builder.build();
    }

    @Override
    protected void recreate(Grid v) {
        super.recreate(v);

        builder.start();

        // Reset
        s.creditsGroup.metrics.y = 0;

        tMoveStartScheduled = getRenderTime() + s.tMoveStart;

        // Analytics
        Game.analyticsView(Globals.ANALYTICS_CONTENT_MAIN_MENU_CREDITS, Globals.ANALYTICS_CONTENT_TYPE_MAIN_MENU);
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        super.render(v, r, renderTime);

        if(renderTime > tMoveStartScheduled) {
            float elapsed = renderTime - tMoveStartScheduled;
            if(elapsed > s.tCreditsMaxTime) {
                onClick(v, s.closeButton, Input.Buttons.LEFT);
                return;
            }
            // Move
            s.creditsGroup.metrics.y = s.moveSpeedY * elapsed;
        }

        // Max framerate
        Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.closeButton) {
            // Run on finish
            if(onFinish != null)
                onFinish.run();
            return;
        }
    }
}
