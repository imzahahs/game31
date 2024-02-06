package game31;

import java.util.Locale;

import game31.app.homescreen.Homescreen;
import game31.gb.GBAppCrashDialog;
import game31.glitch.MpegGlitch;
import sengine.Entity;
import sengine.ui.Clickable;
import sengine.ui.OnClick;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

public class AppCrashDialog extends DialogBox implements Homescreen.App, OnClick<Grid> {

    public static class Internal {
        public UIElement<?> window;

        public TextBox textView;
        public String textFormat;

        public Clickable okayButton;

        public MpegGlitch glitch;
    }


    // Sources
    private final Builder<Object> builder;
    private Internal s;

    private final String appName;

    public void setInternal(Internal internal) {
        s = internal;

    }

    public AppCrashDialog(String appName) {
        builder = new Builder<Object>(GBAppCrashDialog.class, this);
        builder.build();

        this.appName = appName;
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        clear();

        s.textView.autoLengthText(String.format(Locale.US, s.textFormat, appName));

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
        s.glitch.setOnFinished(new Runnable() {
            @Override
            public void run() {
                attach(Globals.grid.homescreen);        // attach to homescreen??
            }
        });
        s.glitch.attach(Globals.grid);
        return null;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }
}
