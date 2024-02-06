package game31;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Array;

import game31.app.homescreen.Homescreen;
import game31.gb.GBRebootingDialog;
import sengine.Entity;
import sengine.File;
import sengine.audio.Audio;
import sengine.ui.OnClick;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 5/15/2017.
 */

public class RebootingDialog extends DialogBox implements OnClick<Grid>, Homescreen.App {


    public static class Internal {
        public UIElement<?> window;

        public TextBox statusView;

        public Audio.Sound startSound;
        public String loopSound;
        public Audio.Sound entrySound;
        public Audio.Sound endSound;
    }

    private static class BootEntry {
        public final String title;
        public final float tInterval;
        public final Runnable run;

        public BootEntry(String title, float tInterval, Runnable run) {
            this.title = title;
            this.tInterval = tInterval;
            this.run = run;
        }
    }

    // Sources
    private final Builder<Object> interfaceSource;
    private Internal s;


    // Working
    private final Array<BootEntry> entries = new Array<BootEntry>(BootEntry.class);
    private int index = -1;
    private float tNextScheduled = -1;
    private Runnable onFinish;
    private Music loadingSound;

    public void addEntry(String title, float tInterval, Runnable run) {
        entries.add(new BootEntry(title, tInterval, run));
    }

    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    public void setInternal(Internal internal) {
        s = internal;

        clear();

        clearEntries();
        reset();


        prepare(s.window);
        show();

    }

    public void reset() {
        tNextScheduled = -1;
        index = -1;
    }

    public void clearEntries() {
        entries.clear();
    }

    public RebootingDialog() {

        interfaceSource = new Builder<Object>(GBRebootingDialog.class, this);
        interfaceSource.build();

    }


    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        reset();

        s.startSound.play();
        loadingSound = Gdx.audio.newMusic(File.open(s.loopSound));
        loadingSound.setLooping(true);
        loadingSound.play();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();
    }


    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(renderTime > tNextScheduled) {
            index++;

            if(index >= entries.size) {
                // Finished
                if(onFinish != null)
                    onFinish.run();
                tNextScheduled = Float.MAX_VALUE;
                s.endSound.play();
                loadingSound.stop();
                loadingSound.dispose();
                detachWithAnim();
            }
            else {
                // Populate
                BootEntry entry = entries.items[index];

                s.entrySound.play();

                s.statusView.autoLengthText(entry.title);

                prepare(s.window);

                if(entry.run != null)
                    entry.run.run();
                tNextScheduled = renderTime + entry.tInterval;
            }
        }

    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {

    }


    @Override
    public Entity<?> open() {
        attach(Globals.grid.compositor);
        return null;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }
}
