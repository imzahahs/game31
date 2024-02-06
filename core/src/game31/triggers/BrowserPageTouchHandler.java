package game31.triggers;

import game31.Globals;
import sengine.Universe;
import sengine.ui.Clickable;

public class BrowserPageTouchHandler extends Clickable {

    private final float time;

    private float tPressedStarted = -1;

    private Runnable onActivated;

    public BrowserPageTouchHandler onActivated(Runnable onActivated) {
        this.onActivated = onActivated;
        return this;
    }

    public BrowserPageTouchHandler(float time) {
        viewport(Globals.grid.browserApp.webGroup());
        passThroughInput(true);
        maxTouchMoveDistance(Globals.maxTouchMoveDistance);

        this.time = time;
    }

    @Override
    public void touchPressed(Universe v, float x, float y, int button) {
        tPressedStarted = getRenderTime();
    }

    @Override
    public void touchReleased(Universe v, float x, float y, int button) {
        tPressedStarted = -1;
    }

    @Override
    public void touchPressing(Universe v, int button) {
        if(tPressedStarted == -1)
            return;
        float elapsed = getRenderTime() - tPressedStarted;
        if(elapsed > time && onActivated != null) {
            tPressedStarted = -1;
            onActivated.run();
        }
    }
}
