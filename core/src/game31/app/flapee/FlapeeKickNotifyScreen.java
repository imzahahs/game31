package game31.app.flapee;

import game31.Globals;
import game31.Grid;
import game31.app.homescreen.Homescreen;
import game31.gb.flapee.GBFlapeeKickNotifyScreen;
import sengine.Entity;
import sengine.audio.Audio;
import sengine.ui.Menu;
import sengine.ui.UIElement;
import sengine.utils.Builder;

public class FlapeeKickNotifyScreen extends Menu<Grid> implements Homescreen.App {


    public static class Internal {
        public UIElement.Group window;

        public float time;

        public Audio.Sound lostSound;

        public String themeName;
        public float themeVolume;
    }

    private final Builder<Object> builder;
    private Internal s;

    private Runnable onFinished = null;

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
    }

    public FlapeeKickNotifyScreen() {
        builder = new Builder<Object>(GBFlapeeKickNotifyScreen.class, this);
        builder.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        grid.notification.hideTracker();
        grid.inputEnabled = false;      // disable all input

        // Audio
        Audio.playMusic(s.themeName, true, s.themeVolume);
        s.lostSound.play();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        grid.inputEnabled = true;       // enable all input

        if(onFinished != null) {
            onFinished.run();
            onFinished = null;
        }

        Audio.stopMusic();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(renderTime > s.time && !isDetaching())
            detachWithAnim();
    }

    // Testing
    @Override
    public Entity<?> open() {
        attach(Globals.grid.compositor);
        return null;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {

    }
}
