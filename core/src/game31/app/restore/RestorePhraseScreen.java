package game31.app.restore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import game31.Globals;
import game31.Grid;
import game31.JsonSource;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.app.homescreen.Homescreen;
import game31.gb.restore.GBRestorePhraseScreen;
import sengine.Entity;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.audio.Stream;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.OnDragged;
import sengine.ui.OnPressed;
import sengine.ui.OnReleased;
import sengine.ui.PatchedTextBox;
import sengine.ui.StaticSprite;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 6/21/2017.
 */

public class RestorePhraseScreen extends Menu<Grid> implements OnClick<Grid>, OnPressed<Grid>, OnDragged<Grid>, OnReleased<Grid>, Homescreen.App {
    private static final String TAG = "RestorePhraseScreen";

    public static class Internal {
        public UIElement<?> window;
        public ScreenBar bars;

        public UIElement<?> bottomPanel;
        public UIElement<?> winPanel;

        public UIElement<?> topGroup;
        public UIElement<?> bottomGroup;

        public PatchedTextBox topWord;
        public PatchedTextBox topEmptyWord;
        public PatchedTextBox topWinWord;
        public float topXpadding;
        public float topYinterval;
        public float topStartX;
        public float topStartY;
        public PatchedTextBox bottomWord;
        public Animation bottomReappearAnim;
        public PatchedTextBox draggedWord;
        public float draggedBottomYThreshold;
        public float draggedInputYOffset;
        public PatchedTextBox bottomSelectedWord;
        public float bottomXpadding;
        public float bottomYinterval;
        public float bottomStartX;
        public float bottomStartY;

        public UIElement<?> tutorialGroup;
        public Clickable skipButton;

        public StaticSprite splashView;
        public StaticSprite splashEndView;
        public float tSplashEndDelay;

        public float tWinDelay;

        public Audio.Sound openSound;
        public Audio.Sound closeSound;

        public Audio.Sound acceptedSound;
        public Audio.Sound savingSound;
        public Audio.Sound winSound;

        public Audio.Sound dragSound;
        public Audio.Sound dropSound;
    }


    private final Builder<Object> builder;
    private Internal s;

    // Working
    private final Array<PatchedTextBox> bottomWords = new Array<PatchedTextBox>(PatchedTextBox.class);
    private final Array<PatchedTextBox> selectedWords = new Array<PatchedTextBox>(PatchedTextBox.class);
    private final Array<PatchedTextBox> topWords = new Array<PatchedTextBox>(PatchedTextBox.class);
    private final IntArray topIndices = new IntArray();
    private Entity<?> transitionFrom = null;
    private RestorePhraseModel model;
    private float tSplashEndScheduled = Float.MAX_VALUE;

//    private PatchedTextBox topEmptyWord;
    private PatchedTextBox draggedWord;
    private boolean isDraggedBottom;
    private int draggedIndex;
    private PatchedTextBox touchedBox;
    private float draggedStartX;
    private float draggedStartY;
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


    private int displaceEmptyWord(int index) {
        int current = topWords.indexOf(s.topEmptyWord, true);
        if(current == index)
            return index;
        // Else take the form of the replaced word
        PatchedTextBox template = index < topWords.size ? topWords.items[index] : draggedWord;
        s.topEmptyWord.text(template.text()).refresh();
        if (current != -1) {
            topWords.removeIndex(current);
            topIndices.removeIndex(current);
        }
        topWords.insert(index, s.topEmptyWord);
        topIndices.insert(index, -1);
        // Refresh
        refreshTopPositions();
        return index;
    }

    private void showEmptyWord(int defaultPosition) {
        if(draggedWord == null)
            return;         // UB
        // Configure empty word
        if(!s.topEmptyWord.isAttached()) {
            s.topEmptyWord
                    .viewport(s.topGroup)
                    .attach();
        }
        // Move to default position first
        int current = displaceEmptyWord(defaultPosition);
        // Determine dragged word location
        draggedWord.calculateWindow(Globals.grid.compositor.camera);
        float x = draggedWord.getX();
        float y = draggedWord.getY();
        float lastEmptyDistance;
        {
            float boxX = s.topEmptyWord.getX();
            float boxY = s.topEmptyWord.getY();
            float deltaX = x - boxX;
            float deltaY = y - boxY;
            lastEmptyDistance = (deltaX * deltaX) + (deltaY * deltaY);
        }

        // Now find the nearest
        int best = current;
        float bestDistance = Float.MAX_VALUE;
        for(int c = 0; c < topWords.size; c++) {
            PatchedTextBox box = topWords.items[c];
            // Calculate distance
            float boxX = box.getX();
            float boxY = box.getY();
            float deltaX = x - boxX;
            float deltaY = y - boxY;
            float distance = (deltaX * deltaX) + (deltaY * deltaY);
            if(distance < Globals.maxWordSnapDistance && distance < bestDistance) {
                best = c;
                bestDistance = distance;
            }
        }
        // Insert empty at best position if not already
        if(current == best)
            return;
        displaceEmptyWord(best);
        // Calculate new empty distance
        float emptyDistance;
        {
            float boxX = s.topEmptyWord.getX();
            float boxY = s.topEmptyWord.getY();
            float deltaX = x - boxX;
            float deltaY = y - boxY;
            emptyDistance = (deltaX * deltaX) + (deltaY * deltaY);
        }
        if(lastEmptyDistance < emptyDistance)
            displaceEmptyWord(current);             // The last position is actually better, so revert
    }

    private void cancelEmptyWord() {
        int index = topWords.indexOf(s.topEmptyWord, true);
        if(index != -1) {
            topWords.removeIndex(index);
            topIndices.removeIndex(index);
            s.topEmptyWord.detach();
            // Refresh top positions
            refreshTopPositions();
        }
    }

    private void refreshTopPositions() {
        float x = s.topStartX;
        float y = s.topStartY;
        for (int i = 0; i < topWords.size; i++) {
            PatchedTextBox box = topWords.items[i];

            boolean isRowStarting = x == s.topStartX;
            float width = box.metrics.scaleX;
            float right = x + (isRowStarting ? 0 : s.topXpadding) + width;
            if (right > 1f) {
                // Next row
                x = s.topStartX;
                y += s.topYinterval;
            } else if (!isRowStarting)
                x += s.topXpadding;

            box.metrics.anchorWindowX = s.topWord.metrics.anchorWindowX + x;
            box.metrics.anchorY = s.topWord.metrics.anchorY + y;

            box.calculateWindow(Globals.grid.compositor.camera);

            x += width;
        }
        // Check if need to show tutorials
        if(topWords.size > 0) {
            if(s.tutorialGroup.isAttached())
                s.tutorialGroup.detachWithAnim();
        }
        else if(!s.tutorialGroup.isAttached())
            s.tutorialGroup.attach();
    }

    private void refreshStats() {
        // Count total correct
        int totalCorrect = 0;
        for(int c = 0; c < topWords.size; c++) {
            PatchedTextBox box = topWords.items[c];
            if(box == s.topEmptyWord)
                continue;           // ignore
            String word = box.text();
            if(word.equalsIgnoreCase(model.words[c]))
                totalCorrect++;
        }
        // Update stats
        if(totalCorrect == model.words.length)
            startWin(true);
    }

    private void startWin(boolean allowAchievements) {
        // Win
        tWinScheduled = getRenderTime() + s.tWinDelay;
        inputEnabled = false;

        // Switch to finish
        s.bottomPanel.detachWithAnim();
        s.winPanel.attach();

        s.acceptedSound.play();
        playSavingSound();

        cancelDragged();

        // Clear all top words
        for(int c = 0; c < topWords.size; c++) {
            topWords.items[c].detach();
        }
        topWords.clear();
        topIndices.clear();
        // Add again
        for(int c = 0; c < model.words.length; c++) {
            String word = model.words[c];
            PatchedTextBox box = s.topWinWord.instantiate()
                    .viewport(s.topGroup)
                    .text(word)
                    .refresh()
                    .attach();
            topWords.add(box);

        }
        refreshTopPositions();
    }

    private void cancelDragged() {
        if(draggedWord == null)
            return;
        boolean topWordAccepted = true;
        if(draggedWord.metrics.anchorY < s.draggedBottomYThreshold) {
            // Didnt drag to top, restore bottom
            topWordAccepted = false;
            cancelBottomSelected(draggedIndex);
        }
        if(topWordAccepted) {
            // Recreate top word
            PatchedTextBox box = s.topWord.instantiate()
                    .viewport(s.topGroup)
                    .text(draggedWord.text())
                    .refresh()
                    .attach();
            int current = topWords.indexOf(s.topEmptyWord, true);
            topWords.insert(current, box);
            topIndices.insert(current, draggedIndex);
        }
        // Detach dragged box
        draggedWord.detachWithAnim();
        draggedWord.cancelTouch();
        draggedWord = null;
        touchedBox = null;
        s.dropSound.play();
        // Cancel top empty word if any
        cancelEmptyWord();
        // Stats
        refreshStats();
    }

    public void show(String filename) {
        // Load
        RestorePhraseModel model = File.getHints(filename);      // This shouldn't fail
        show(model);
    }

    public void show(RestorePhraseModel model) {
        clear();

        this.model = model;

        s.bars.showAppbar(s.bars.title(), model.subtitle);

        // Stats
        refreshStats();

        // Add words randomly to bottom panel
        Array<String> words = new Array<String>(String.class);
        words.addAll(model.words);
        words.shuffle();                // This will never result in the correct order
        float x = s.bottomStartX;
        float y = s.bottomStartY;
        for(String word : words) {
            PatchedTextBox box = s.bottomWord.instantiate()
                    .viewport(s.bottomGroup)
                    .text(word)
                    .refresh()
                    .attach();

            boolean isRowStarting = x == s.bottomStartX;
            float width = box.metrics.scaleX;
            float right = x + (isRowStarting ? 0 : s.bottomXpadding) + width;
            if(right > 1f) {
                // Next row
                x = s.bottomStartX;
                y += s.bottomYinterval;
            }
            else if(!isRowStarting)
                x += s.bottomXpadding;

            box.metrics.move(x, y);
            bottomWords.add(box);
            selectedWords.add(null);        // reserve space

            x += width;
        }

        // Skip button
        tSkipScheduled = getRenderTime() + Globals.tRestoreSkipDelay;
        s.skipButton.detach();
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


    private void clear() {
        s.topGroup.detachChilds();
        s.bottomGroup.detachChilds();

        if(draggedWord != null) {
            draggedWord.detach();
            draggedWord = null;
        }
        touchedBox = null;

        tWinScheduled = Float.MAX_VALUE;

        s.topEmptyWord.detach();

        bottomWords.clear();
        selectedWords.clear();
        topWords.clear();
        topIndices.clear();

        s.bottomPanel.attach();
        s.winPanel.detach();
        s.tutorialGroup.attach();
    }

    public void load(String filename) {
        // Load config
        new JsonSource<RestorePhraseConfig>(filename, RestorePhraseConfig.class).load();
    }

    public RestorePhraseScreen() {
        builder = new Builder<Object>(GBRestorePhraseScreen.class, this);
        builder.build();

        // Load config
        load(Globals.restorePhraseConfigFilename);
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

        if(Globals.autoresolveRestoreScreens)
            startWin(false);
    }


    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        // Clear
//        clear();                  // 20171113 - Prevent clearing screen when getting a phone call

        stopSavingSound();
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

        // Verify dragged word is still being dragged
        if(draggedWord != null && !Gdx.input.isTouched(draggedWord.touchedPointer()))
            cancelDragged();

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

    private void showBottomSelected(int index) {
        if(selectedWords.items[index] != null)
            return;     // already selected
        PatchedTextBox bottomBox = bottomWords.items[index];
        PatchedTextBox selectedBox = s.bottomSelectedWord.instantiate()
                .viewport(s.bottomGroup)
                .text(bottomBox.text())
                .refresh()
                .attach();
        selectedBox.metrics.anchorWindowX = bottomBox.metrics.anchorWindowX;
        selectedBox.metrics.anchorY = bottomBox.metrics.anchorY;
        selectedWords.items[index] = selectedBox;
    }

    private void cancelBottomSelected(int index) {
        if(selectedWords.items[index] == null)
            return;
        selectedWords.items[index].detachWithAnim();
        selectedWords.items[index] = null;
        bottomWords.items[index].attach().windowAnimation(s.bottomReappearAnim.startAndReset(), true, false);
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.bars.backButton() || view == s.bars.homeButton()) {
            if(view == s.bars.homeButton())
                transitionFrom = v.homescreen;
            tSplashEndScheduled = getRenderTime() + s.tSplashEndDelay;
            s.splashEndView.attach();
            s.closeSound.play();
            inputEnabled = false;
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

        // Bottom selected word
        for(int c = 0; c < selectedWords.size; c++) {
            // Replace with selected box
            PatchedTextBox selectedBox = selectedWords.items[c];
            if (view == selectedBox) {
                int index = topIndices.indexOf(c);
                if(index == -1)
                    return;         // was just dragged, not selected
                // Remove from top
                PatchedTextBox topBox = topWords.items[index];
                topBox.detach();
                topWords.removeIndex(index);
                topIndices.removeIndex(index);

                // Refresh top word positions
                refreshTopPositions();

                // Cancel selected
                cancelBottomSelected(c);

                // Stats
                refreshStats();

                return;
            }
        }


        // Top word
        for(int c = 0; c < topWords.size; c++) {
            // Replace with selected box
            PatchedTextBox topBox = topWords.items[c];
            if (view == topBox && topBox != s.topEmptyWord) {
                // Immediately remove and cancel bottom
                topBox.detach();
                topWords.removeIndex(c);
                int index = topIndices.removeIndex(c);

                // Refresh top word positions
                refreshTopPositions();

                // Cancel selected
                cancelBottomSelected(index);

                // Stats
                refreshStats();

                s.dropSound.play();

                return;
            }
        }

        // Bottom word
        for(int c = 0; c < bottomWords.size; c++) {
            // Replace with selected box
            PatchedTextBox bottomBox = bottomWords.items[c];
            if(view == bottomBox) {
                // Add selected box if not yet added
                showBottomSelected(c);
                bottomBox.detachWithAnim();

                // Add at top
                PatchedTextBox box = s.topWord.instantiate()
                        .viewport(s.topGroup)
                        .text(bottomBox.text())
                        .refresh()
                        .attach();
                topWords.add(box);
                topIndices.add(c);

                // Refresh top word positions
                refreshTopPositions();

                // Stats
                refreshStats();

                s.dragSound.play();

                // Done
                return;
            }
        }

    }

    @Override
    public void onPressed(Grid v, UIElement<?> view, float x, float y, int button) {

    }

    @Override
    public void onDragged(Grid v, UIElement<?> view, float x, float y, int button) {

        // Transform input coordinates
        x += view.getInputX();
        y += view.getInputY();
        x *= view.getInputScaleX();
        y *= view.getInputScaleY();
        x -= v.compositor.camera.position.x;            // No idea why this adjustment is needed, but it works
        y -= v.compositor.camera.position.y;

        if(view == draggedWord) {
            draggedWord.metrics.anchorWindowX = x;
            draggedWord.metrics.anchorY = y + s.draggedInputYOffset;

            // Refresh top empty word, default at current position
            int index = topWords.indexOf(s.topEmptyWord, true);
            showEmptyWord(index);

            return;
        }
        else if(draggedWord != null)
            return;         // only respond to drags on at a time

        // Check if moving a top word
        for(int c = 0; c < topWords.size; c++) {
            // Replace with selected box
            PatchedTextBox topBox = topWords.items[c];
            if (view == topBox) {
                int pointer = topBox.touchedPointer();
                if(topBox != touchedBox) {
                    // First time assessing this drag, track position
                    touchedBox = topBox;
                    draggedStartX = x;
                    draggedStartY = y;
                    return;
                }
                else {
                    // Check distance
                    float deltaX = x - draggedStartX;
                    float deltaY = y - draggedStartY;
                    float minDistance = Globals.minWordDragDistance;
                    minDistance *= minDistance;
                    if(((deltaX * deltaX) + (deltaY * deltaY)) < minDistance)
                        return;         // not yet dragged enough
                }

                topBox.cancelTouch();    // cancel touch
                topBox.detach();         // remove immediately

                // Show dragged
                isDraggedBottom = false;
                draggedIndex = topIndices.items[c];         // resolve to bottom index
                draggedWord = s.draggedWord.instantiate()
                        .viewport(s.window)
                        .text(topBox.text())
                        .refresh()
                        .attach();
                draggedWord.metrics.anchorWindowX = x;
                draggedWord.metrics.anchorY = y + s.draggedInputYOffset;
                s.dragSound.play();

                draggedWord.simulateTouch(v, pointer, Input.Buttons.LEFT);      // transfer touch here

                // Remove top box
                topWords.removeIndex(c);
                topIndices.removeIndex(c);

                // Show top empty word, default to current position
                showEmptyWord(c);

                return;
            }
        }

        // Check if moving a bottom word
        for(int c = 0; c < bottomWords.size; c++) {
            // Replace with selected box
            PatchedTextBox bottomBox = bottomWords.items[c];
            if (view == bottomBox) {
                int pointer = bottomBox.touchedPointer();
                if(bottomBox != touchedBox) {
                    // First time assessing this drag, track position
                    touchedBox = bottomBox;
                    draggedStartX = x;
                    draggedStartY = y;
                    return;
                }
                else {
                    // Check distance
                    float deltaX = x - draggedStartX;
                    float deltaY = y - draggedStartY;
                    float minDistance = Globals.minWordDragDistance;
                    minDistance *= minDistance;
                    if(((deltaX * deltaX) + (deltaY * deltaY)) < minDistance)
                        return;         // not yet dragged enough
                }
                // Show bottom selected
                showBottomSelected(c);
                bottomBox.cancelTouch();    // cancel touch
                bottomBox.detach();         // remove immediately
                // Show dragged
                isDraggedBottom = true;
                draggedIndex = c;
                draggedWord = s.draggedWord.instantiate()
                        .viewport(s.window)
                        .text(bottomBox.text())
                        .refresh()
                        .attach();
                draggedWord.metrics.anchorWindowX = x;
                draggedWord.metrics.anchorY = y + s.draggedInputYOffset;
                s.dragSound.play();

                draggedWord.simulateTouch(v, pointer, Input.Buttons.LEFT);      // transfer touch here

                // Show top empty word, default at last position
                showEmptyWord(topWords.size);

                return;
            }
        }
    }


    @Override
    public void onReleased(Grid v, UIElement<?> view, float x, float y, int button) {
        if(view == draggedWord)
            cancelDragged();
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
