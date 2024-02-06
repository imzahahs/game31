package game31;

import game31.app.homescreen.Homescreen;
import game31.gb.GBNotEnoughSpaceDialog;
import sengine.Entity;
import sengine.audio.Audio;
import sengine.ui.Clickable;
import sengine.ui.OnClick;
import sengine.ui.UIElement;
import sengine.utils.Builder;

public class NotEnoughSpaceDialog extends DialogBox implements Homescreen.App, OnClick<Grid> {

    public static class Internal {
        public UIElement<?> window;

        public Clickable okayButton;

        public Audio.Sound openSound;
    }


    // Sources
    private final Builder<Object> builder;
    private Internal s;

    public void setInternal(Internal internal) {
        s = internal;

    }

    public NotEnoughSpaceDialog() {
        builder = new Builder<Object>(GBNotEnoughSpaceDialog.class, this);
        builder.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        s.openSound.play();

        clear();

        prepare(s.window);
        show();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.okayButton) {
            detachWithAnim();
            return;
        }
    }


    @Override
    public Entity<?> open() {
        attach(Globals.grid.screensGroup);
        return null;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }
}
