package game31.app.restore;

import com.badlogic.gdx.utils.Array;

import java.util.Locale;

import game31.Globals;
import game31.Grid;
import game31.JsonSource;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.app.homescreen.Homescreen;
import game31.gb.restore.GBRestoreImageScreen;
import sengine.Entity;
import sengine.File;
import sengine.Sys;
import sengine.audio.Audio;
import sengine.audio.Stream;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 6/19/2017.
 */

public class RestoreImageScreen extends Menu<Grid> implements Homescreen.App, OnClick<Grid> {
    private static final String TAG = "RestoreImageScreen";

    public static class Internal {
        public UIElement<?> window;
        public ScreenBar bars;

        public String progressTextFormat;
        public String countTextFormat;

        public UIElement<?> bottomPanel;
        public UIElement<?> winPanel;

        public UIElement<?> mainImageAnchor;
        public UIElement<?> fragmentImageAnchor;

        public Clickable imageView;
        public StaticSprite imageCorruptedView;
        public StaticSprite imageEmptyView;
        public UIElement<?> selectedView;
        public TextBox selectedOrderView;
        public float imageCorruptedSize;
        public float imageEmptySize;

        public Clickable mainView;
        public StaticSprite mainCorruptedView;
        public StaticSprite mainEmptyView;
        public float mainCorruptedSize;
        public float mainEmptySize;

        public UIElement.Metrics[] imageCellMetrics;

        public UIElement.Metrics mainImageMetrics;
        public UIElement.Metrics[] fragmentMetrics;

        public UIElement<?> tutorialGroup;
        public Clickable skipButton;

        public HorizontalProgressBar progressBar;
        public float tProgressBarSeekTime;
        public TextBox progressPercentageView;
        public TextBox progressUsedView;

        public StaticSprite splashView;
        public StaticSprite splashEndView;
        public float tSplashEndDelay;

        public float tWinDelay;


        public Audio.Sound openSound;
        public Audio.Sound closeSound;



        public Audio.Sound acceptedSound;
        public Audio.Sound savingSound;
        public Audio.Sound winSound;

        public Audio.Sound addSound;
        public Audio.Sound removeSound;
    }

    private final Builder<Object> builder;
    private Internal s;

    // Working
    private Clickable mainView = null;
    private final Array<Clickable> fragments = new Array<Clickable>(Clickable.class);
    private final Array<Clickable> selected = new Array<Clickable>(Clickable.class);
    private RestoreImageModel model = null;
    private Entity<?> transitionFrom = null;
    private float tSplashEndScheduled = Float.MAX_VALUE;
    private final int[] mainProfile = new int[Globals.restoreImageFragments];
    private float tWinScheduled = Float.MAX_VALUE;
    private float tSkipScheduled = Float.MAX_VALUE;

    private Stream savingSound = null;

    private void playSavingSound() {
        stopSavingSound();
        savingSound = s.savingSound.loop();
    }

    private void stopSavingSound() {
        if(savingSound == null)
            return;
        savingSound.stop();
        savingSound = null;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
        s.bars.attach(this);

        // Refresh
        if(model != null) {
            Globals.grid.postMessage(new Runnable() {
                @Override
                public void run() {
                    show(model);
                }
            });
        }
    }

    public void open(Entity<?> transitionFrom) {
        // Attach immediately
        this.transitionFrom = transitionFrom;
        attach(Globals.grid.screensGroup);
        transitionFrom.detach();

        s.openSound.play();
    }

    public void show(String filename) {
        // Load
        RestoreImageModel model = File.getHints(filename);      // This shouldn't fail
        show(model);
    }

    private void show(RestoreImageModel model) {
        // Clear
        clear();

        this.model = model;

        // Stats
        refreshStats();

        // Title bar
        s.bars.showAppbar(s.bars.title(), model.subtitle);

        // Create profiles
        int numProfiles = model.profiles.size / Globals.restoreImageFragments;
        for(int c = 0; c < numProfiles; c++) {
            Clickable fragment = createImageView(model.profiles.items, c * Globals.restoreImageFragments);
            fragment.viewport(s.fragmentImageAnchor).metrics(s.fragmentMetrics[c]).attach();

            // Recognize input
            fragments.add(fragment);
        }

        // Skip button
        tSkipScheduled = getRenderTime() + Globals.tRestoreSkipDelay;
        s.skipButton.detach();
    }

    private Clickable createImageView(int[] profile, int offset) {
        Clickable view = s.imageView.instantiate().visuals(model.image);
        for(int c = 0; c < Globals.restoreImageFragments; c++) {
            int p = profile[offset + c];
            if(p == RestoreImageModel.P_IMAGE)
                continue;
            StaticSprite fragment;
            float scale;
            if(p == RestoreImageModel.P_EMPTY) {
                fragment = s.imageEmptyView.instantiate();
                scale = s.imageEmptySize;
            }
            else { // if(p == RestoreImageModel.P_CORRUPTED)
                fragment = s.imageCorruptedView.instantiate();
                scale = s.imageCorruptedSize;
            }
            fragment.viewport(view)
                    .metrics(s.imageCellMetrics[c].instantiate().scale(scale))
                    .attach();
        }
        return view;
    }

    private void refreshMainView() {
        // Create main view if not yet
        if(mainView == null) {
            s.mainImageAnchor.attach();
            s.tutorialGroup.detachWithAnim();
            mainView = s.mainView.instantiate().visuals(model.image).viewport(s.mainImageAnchor).metrics(s.mainImageMetrics).attach();
        }

        // Clear main profile
        for(int c = 0; c < mainProfile.length; c++)
            mainProfile[c] = 0;

        // Recalculate
        for(int c = 0; c < selected.size; c++) {
            int offset = fragments.indexOf(selected.items[c], true) * Globals.restoreImageFragments;
            for(int i = 0; i < Globals.restoreImageFragments; i++) {
                int layer = model.profiles.items[offset + i];
                int current = mainProfile[i];
                if(layer != RestoreImageModel.P_EMPTY)
                    current = layer;            // If its empty, just pass through previous, all else will override
                mainProfile[i] = current;
            }
        }

        // Now for each cell, check if it has changed
        for(int c = 0; c < Globals.restoreImageFragments; c++) {
            int p = mainProfile[c];
            StaticSprite current = mainView.find("cell_" + c);
            StaticSprite correct = null;            // correct fragment
            float scale = -1;
            if(p == RestoreImageModel.P_EMPTY) {
                correct = s.mainEmptyView.instantiate();
                scale = s.mainEmptySize;
            }
            else if(p == RestoreImageModel.P_CORRUPTED) {
                correct = s.mainCorruptedView.instantiate();
                scale = s.mainCorruptedSize;
            }
            // Check if match
            if(correct == null) {
                // Changing to showing image, detach current if exists
                if(current != null)
                    current.name(null).detachWithAnim();
                continue;
            }
            // Else there is an effect now, check if current matches
            if(current != null) {
                if(current.visual() == correct.visual())
                    continue;           // is the same effect, maintain
                // Else different, detach current
                current.name(null).detachWithAnim();
            }
            // Time to add new effect
            correct.viewport(mainView)
                    .name("cell_" + c)
                    .metrics(s.imageCellMetrics[c].instantiate().scale(scale))
                    .attach();
        }

        // Check profile states
        boolean isEmpty = true;
        boolean isCorrect = true;
        for(int c = 0; c < Globals.restoreImageFragments; c++) {
            if(mainProfile[c] != RestoreImageModel.P_EMPTY)
                isEmpty = false;
            if(mainProfile[c] != RestoreImageModel.P_IMAGE)
                isCorrect = false;
        }

        // If empty, detach and show tutorial
        if(isEmpty) {
            s.mainImageAnchor.detachWithAnim();
            mainView.detachWithAnim();
            mainView = null;
            s.tutorialGroup.attach();
        }

        // Update stats
        refreshStats();

        if(isCorrect)
            startWin(true);
    }

    private void startWin(boolean allowAchievements) {
        // Schedule end
        tWinScheduled = getRenderTime() + s.tWinDelay;
        inputEnabled = false;

        // Show complete image
        s.mainImageAnchor.attach();
        s.tutorialGroup.detachWithAnim();
        if(mainView != null)
            mainView.detach();
        mainView = s.mainView.instantiate().visuals(model.image).viewport(s.mainImageAnchor).metrics(s.mainImageMetrics).attach();

        // Switch to finish
        s.bottomPanel.detachWithAnim();
        s.winPanel.attach();

        s.acceptedSound.play();
        playSavingSound();
    }

    private void refreshStats() {
        // Count total correct
        int totalCorrect = 0;
        for(int c = 0; c < Globals.restoreImageFragments; c++) {
            if(mainProfile[c] == RestoreImageModel.P_IMAGE)
                totalCorrect++;
        }
        // Update stats
        int percentage = Math.round(((float)totalCorrect / (float)Globals.restoreImageFragments) * 100f);
        s.progressPercentageView.text(String.format(Locale.US, s.progressTextFormat, percentage));
        s.progressUsedView.text(String.format(Locale.US, s.countTextFormat, selected.size, fragments.size));
        s.progressBar.seek(percentage / 100f, s.tProgressBarSeekTime);
    }




    private void clear() {
        model = null;

        s.fragmentImageAnchor.detachChilds();

        if(mainView != null) {
            mainView.detach();
            mainView = null;
        }

        fragments.clear();
        selected.clear();

        s.bottomPanel.attach();
        s.winPanel.detach();
        s.tutorialGroup.attach();

        tWinScheduled = Float.MAX_VALUE;


        // Clear main profile
        for(int c = 0; c < mainProfile.length; c++)
            mainProfile[c] = 0;
    }

    public void load(String filename) {
        // Load config
        new JsonSource<RestoreImageConfig>(filename, RestoreImageConfig.class).load();
    }

    public RestoreImageScreen() {
        builder = new Builder<Object>(GBRestoreImageScreen.class, this);
        builder.build();

        // Load config
        load(Globals.restoreImageConfigFilename);
    }


    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        s.splashView.attach();
        grid.postMessage(new Runnable() {
            @Override
            public void run() {
                s.splashView.detachWithAnim();
            }
        });

        // Reset
        inputEnabled = true;
        tSplashEndScheduled = Float.MAX_VALUE;
        s.splashEndView.detach();

        stopSavingSound();

        if(Globals.autoresolveRestoreScreens)
            startWin(false);
    }


    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        // Clear
//        clear();              // 20171114 - No need to clear, bug when there is a call ??
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        super.render(v, r, renderTime);

        // Always max framerate
        Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);


        if(renderTime > tSkipScheduled) {
            tSkipScheduled = Float.MAX_VALUE;
            s.skipButton.attach();
        }

        if(renderTime > tSplashEndScheduled) {
            detach();
            transitionFrom.attach(Globals.grid.screensGroup);
        }

        if(renderTime > tWinScheduled) {
            tWinScheduled = Float.MAX_VALUE;

            // Update state
            v.state.set(model.name, true);

            // Trigger
            if(model.trigger != null && !model.trigger.isEmpty())
                v.eval(TAG, model.trigger);

            stopSavingSound();
            s.winSound.play();

            // Smooth transition back
            ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(this, transitionFrom, v.screensGroup);
            transition.attach(v.screensGroup);
        }
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int b) {
        if(view == s.bars.backButton() || view == s.bars.homeButton()) {
            if(view == s.bars.homeButton())
                transitionFrom = v.homescreen;
            tSplashEndScheduled = getRenderTime() + s.tSplashEndDelay;
            s.splashEndView.attach();
            inputEnabled = false;
            s.closeSound.play();
            return;
        }

        if(view == s.skipButton) {
            startWin(false);
            return;
        }

        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        for(int c = 0; c < fragments.size; c++) {
            Clickable button = fragments.items[c];
            if(button == view) {
                // Toggle this fragment
                toggleFragment(c);
                return;
            }
        }
    }

    private void toggleFragment(int index) {
        Clickable fragment = fragments.items[index];
        if(!selected.contains(fragment, true)) {
            // Adding new fragment
            // Mark fragment as used
            UIElement<?> view = s.selectedView.instantiate().viewport(fragment).attach();
            view.find(s.selectedOrderView).text(Integer.toString(selected.size + 1));

            // Recognize selected
            selected.add(fragment);

            s.addSound.play();
        }
        else {
            fragment.find(s.selectedView).detachWithAnim();
            selected.removeValue(fragment, true);

            // Refresh all order indicators
            for(int c = 0; c < selected.size; c++) {
                fragment = selected.items[c];
                fragment.find(s.selectedOrderView).text(Integer.toString(c + 1));
            }

            s.removeSound.play();
        }

        refreshMainView();
    }

    @Override
    public Entity<?> open() {
        open(Globals.grid.homescreen);
        return null;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }
}
