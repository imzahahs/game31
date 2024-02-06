package game31;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

import game31.app.homescreen.Homescreen;
import game31.gb.GBNotification;
import game31.model.SubtitlesModel;
import game31.renderer.SaraRenderer;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.calc.Graph;
import sengine.graphics2d.CircularSprite;
import sengine.graphics2d.Mesh;
import sengine.mass.MassSerializable;
import sengine.materials.AnimatedMaterial;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.OnPressed;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.Toast;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 29/8/2016.
 */
public class Notification extends Menu<Grid> implements OnClick<Grid>, OnPressed<Grid> {
    static final String TAG = "Notification";

    public static class Internal {
        // Window
        public UIElement<?> window;
        public ScrollableSurface surface;
        public Clickable quickView;

        public UIElement.Group notificationGroup;
        public StaticSprite imageView;
        public StaticSprite iconView;
        public TextBox titleView;
        public TextBox detailView;
        public float tDismissTime;

        public UIElement.Group accessView;
        public Clickable[] appButtons;
        public StaticSprite appNoteView;
        public TextBox appNoteTextView;

        public Toast hintView;

        public float hintMoveX;
        public float tHintMoveTime;

        public Clickable accessCloseButton;

        public UIElement<?> trackerGroup;
        public ScrollableSurface trackerSurface;
        public Animation trackerEnterAnim;

        public Clickable exitButton;

        public float trackerStartY;
        public float trackerPendingY;
        public float trackerPendingPaddingY;
        public float trackerTimelineY;

        public StaticSprite pendingIconView;
        public StaticSprite failedIconView;
        public StaticSprite noteIconView;
        public StaticSprite successIconView;

        public TextBox pendingStatusTextView;
        public TextBox pendingDescriptionView;
        public TextBox failedStatusTextView;
        public TextBox failedDescriptionView;
        public TextBox noteStatusTextView;
        public TextBox noteDescriptionView;
        public TextBox successStatusTextView;
        public TextBox successDescriptionView;

        public String defaultPendingText;
        public String defaultFailedText;
        public String defaultNoteText;
        public String defaultCompletedText;


        public StaticSprite lineView;
        public float lineMinHeight;

        public StaticSprite coverView;
        public ColorAttribute coverColorAttribute;
        public Graph coverAlphaGraph;
        public float coverStartX;
        public float coverEndX;
        public float coverClearRenderX;

        public UIElement exitDialogGroup;
        public Clickable exitDialogYesButton;
        public Clickable exitDialogNoButton;

        public Audio.Sound slideShowSound;
        public Audio.Sound slideHideSound;
        public Audio.Sound trackerOpenSound;

        public StaticSprite saveView;
        public AnimatedMaterial.Instance saveIconAnim;
        public float tSaveIconStartTime;
        public float tSaveIconAnimTime;
        public float tSaveTotalTime;
        public float tSaveMinInterval;

        public Clickable downloadView;
        public CircularSprite downloadProgressMat;
        public StaticSprite downloadProgressView;
        public StaticSprite downloadFinishedView;
        public Animation downloadStartAnim;
        public Animation downloadFinishedAnim;
        public String downloadInProgressText;
        public String downloadOpenText;

        public UIElement.Metrics subtitlePortraitMetrics;
        public UIElement.Metrics subtitleLandscapeMetrics;
        public UIElement.Metrics subtitleLandscapeNormalMetrics;
        public UIElement.Group subtitleContainerView;
        public PatchedTextBox subtitleView;
        public float tSubtitleDurationPerWord;
    }

    public enum QuestType {
        PENDING,
        NOTE,
        FAILED,
        SUCCESS
    }

    public static class Quest implements MassSerializable {
        public String tag;
        public String status;
        public QuestType type;
        public String description;

        public Quest() {
        }

        @MassConstructor
        public Quest(String tag, String status, QuestType type, String description) {
            this.tag = tag;
            this.status = status;
            this.type = type;
            this.description = description;
        }

        @Override
        public Object[] mass() {
            return new Object[] { tag, status, type, description };
        }
    }

    private static class Subtitle {

        final float position;
        final String text;
        final float duration;

        Subtitle(final SubtitlesModel.TextModel textModel) {
            this.position = textModel.position;
            this.text = textModel.text;
            this.duration = textModel.duration;
        }
    }

    private static class Download {
        final String id;
        final float tDownloadTime;
        final Runnable onFinished;
        final Runnable onOpen;

        public Download(String id, float tDownloadTime, Runnable onFinished, Runnable onOpen) {
            this.id = id;
            this.tDownloadTime = tDownloadTime;
            this.onFinished = onFinished;
            this.onOpen = onOpen;
        }
    }

    // Interface source
    private final Builder<Object> interfaceSource;
    private Internal s;


    // Current
    private final Array<String> quickAccessApps = new Array<String>(String.class);
    private float tAutoDismissScheduled = Float.MAX_VALUE;
    private boolean wasRenderingQuickView = false;
    private int quickViewDirection = 0;
    private float lastQuickViewX = 0;
    private String notificationApp;
    private boolean isNotificationTouched = false;
    private boolean isShowingTracker = false;
    private boolean isTrackerRequireRefresh = false;

    private final Array<Quest> quests = new Array<Quest>(Quest.class);

    private float tSaveIconStarted = Float.MAX_VALUE;
    private float tLastSaved = -1;

    private String savename = null;

    private float tDownloadStarted = -1;
    private Download activeDownload = null;
    private final Array<Download> queuedDownloads = new Array<>(Download.class);

    // Subtitles
    private final HashMap<String, Subtitle[]> subtitlesLookup = new HashMap<>();
    private Subtitle[] currentSubtitles;
    private String currentSubtitleFilename = null;
    private int currentSubtitleIndex = -1;
    private PatchedTextBox subtitleView;
    private float tSubtitleEndScheduled = Float.MAX_VALUE;
    private Music currentAutomatedSubtitle = null;


    public void pack(ScriptState state) {
        state.set(savename, quests.toArray());
    }

    public void load(String savename) {
        this.savename = savename;

        Quest[] saved = Globals.grid.state.get(savename, null);
        if(saved != null)
            quests.addAll(saved);
        refreshQuests();
    }

    public void loadSubtitles(final String filename) {
        // Clear
        subtitlesLookup.clear();
        // Load
        SubtitlesModel.FileModel[] lookup = new JsonSource<>(filename, SubtitlesModel.class).load().files;
        // Convert
        for (int c = 0; c < lookup.length; ++c) {
            SubtitlesModel.FileModel model = lookup[c];
            Subtitle[] subtitles = new Subtitle[model.texts.length];
            for(int i = 0; i < model.texts.length; i++) {
                subtitles[i] = new Subtitle(model.texts[i]);
            }
            subtitlesLookup.put(model.filename, subtitles);
        }
    }

    public void startSubtitle(String filename) {
        startSubtitle(filename, null);
    }


    public void startSubtitle(String filename, Music automatedSubtitle) {
        currentSubtitleFilename = filename;
        currentSubtitleIndex = -1;
        currentSubtitles = subtitlesLookup.get(filename);
        if(currentSubtitles != null)
            currentAutomatedSubtitle = automatedSubtitle;
        else
            currentAutomatedSubtitle = null;
    }

    public void stopSubtitle(String filename) {
        if(currentSubtitleFilename != null && !currentSubtitleFilename.equals(filename))
            return;     // Don't stop as filename mismatches ( could be due to screen transitions )
        currentSubtitles = null;
        currentSubtitleIndex = -1;
        currentSubtitleFilename = null;
        currentAutomatedSubtitle = null;
        hideSubtitleView();
    }

    public void resetSubtitle(String filename) {
        if(currentSubtitleFilename != null && !currentSubtitleFilename.equals(filename))
            return;     // Don't reset as filename mismatches
        currentSubtitleIndex = -1;
        hideSubtitleView();
    }

    private void hideSubtitleView() {
        if (subtitleView != null) {
            subtitleView.detachWithAnim();
            tSubtitleEndScheduled = Float.MAX_VALUE;
            subtitleView = null;
        }
    }


    public void updateSubtitles(float elapsed, boolean isLandscape) {
        if (currentSubtitles == null || elapsed <= 0f)
            return;

        // Find subtitle index according to elapsed
        int bestIndex = -1;
        for (int c = 0; c < currentSubtitles.length; c++) {
            if (currentSubtitles[c].position > elapsed)
                break;
            // Else this is the last visible subtitle
            bestIndex = c;
        }

        // Check if need to update subtitle
        if(bestIndex == -1 || bestIndex <= currentSubtitleIndex)
            return;     // Subtitle is the same, or moved backwards (some timing problem)


        // Else new subtitle shown
        currentSubtitleIndex = bestIndex;
        Subtitle subtitle = currentSubtitles[bestIndex];
        if (subtitleView == null) {
            // There is no known subtitle view now, but check in case if the previous one is still detaching
            subtitleView = viewport.find(s.subtitleView);
            if (subtitleView != null)
                subtitleView.detach();      // previous view is detaching, detach immediately
            // Create new view
            subtitleView = s.subtitleView.instantiate().attach();
        }
        // Alignment
        if(isLandscape) {
            if(Sys.system.getLength() > 1f)
                s.subtitleContainerView.metrics(s.subtitleLandscapeNormalMetrics.instantiate());
            else
                s.subtitleContainerView.metrics(s.subtitleLandscapeMetrics.instantiate());
        }
        else
            s.subtitleContainerView.metrics(s.subtitlePortraitMetrics.instantiate());
        // Update view
        subtitleView.text(subtitle.text);
        // Calculate time shown
        float duration = subtitle.duration;
        if (duration <= 0f) {
            // Auto calculate duration, explode words and estimate per word
            int words = subtitle.text.trim().split("\\s+").length;
            if (words == 0)
                words = 1;
            duration = words * s.tSubtitleDurationPerWord;
        }
        tSubtitleEndScheduled = getRenderTime() + duration;
    }

    public void startDownload(String id, float tDownloadTime, Runnable onFinished, Runnable onOpenDownload) {
        Download download = new Download(id, tDownloadTime, onFinished, onOpenDownload);
        if(activeDownload != null) {
            // Already download in progress, queue
            queuedDownloads.add(download);
            return;
        }
        // Else start now
        startDownload(download);
    }

    private void startDownload(Download download) {
        tDownloadStarted = getRenderTime();
        activeDownload = download;

        s.downloadView.attach();
        s.downloadView.windowAnimation(s.downloadStartAnim.startAndReset(), true, true);
        s.downloadView.text(s.downloadInProgressText);
        s.downloadView.disable();
        s.downloadFinishedView.detach();
        s.downloadProgressView.attach();
        s.downloadProgressMat.show(0f, 0f);         // empty circle
    }

    public void clearFinishedDownload() {
        if(activeDownload != null && tDownloadStarted == -1)
            clearDownload(null);
    }

    public void clearDownload(String id) {
        if(activeDownload == null)
            return;     // No downloads left, nothing to clear
        if(id == null || activeDownload.id.equals(id)) {
            activeDownload = null;
            if(queuedDownloads.size > 0)
                startDownload(queuedDownloads.removeIndex(0));
            else {
                tDownloadStarted = -1;
                s.downloadView.detachWithAnim();
            }
            return;
        }
        // Else find in queued downloads
        for(int c = 0; c < queuedDownloads.size; c++) {
            Download download = queuedDownloads.items[c];
            if(download.id.equals(id))
                queuedDownloads.removeIndex(c);
        }
    }


    public void showSavedIndicator() {
        float elapsed = Sys.getTime() - tLastSaved;
        if(elapsed > s.tSaveMinInterval) {
            // Allowed to show save indicator
            s.saveView.attach();
            s.saveIconAnim.progress(0);
            tSaveIconStarted = getRenderTime() + s.tSaveIconStartTime;
            // Remember to keep track of current time to not show save indicator too frequently
            tLastSaved = Sys.getTime();
        }
    }

    public void hideNow() {
        s.surface.move(+1000, 0);
    }

    public void hideAccessView() {
        if(s.quickView.renderingEnabled && s.accessView.isAttached())
            s.surface.seekGravityTarget(+1f, 0);
    }

    public void showTracker() {
        isShowingTracker = true;        // show tracker by default (for homescreen)
        // If notification view is attached with quick view and is visible, show tracker with transition
        if(s.accessView.isAttached() && s.quickView.renderingEnabled) {
            s.trackerGroup.windowAnimation(s.trackerEnterAnim.startAndReset(), true, false);
            s.trackerGroup.attach();
            if(isTrackerRequireRefresh)
                refreshQuests();
        }
    }

    public void hideTracker() {
        isShowingTracker = false;           // dont show tracker by default
        if(s.trackerGroup.isAttached())
            s.trackerGroup.detachWithAnim();
    }

    public void openTracker() {
        if(s.trackerGroup.isAttached())
            return;         // already showing
        s.trackerGroup.attach();
        if(isTrackerRequireRefresh)
            refreshQuests();
        s.trackerSurface.move(0, -1000);
        if(s.accessView.isAttached() && s.quickView.renderingEnabled) {
            s.trackerGroup.windowAnimation(s.trackerEnterAnim.startAndReset(), true, false);
            s.trackerOpenSound.play();
        }
        s.surface.seekGravityTarget(-1f, 0);
    }

    public Quest getQuest(String tag) {
        for (int c = 0; c < quests.size; c++) {
            if (quests.items[c].tag.equals(tag)) {
                return quests.items[c];
            }
        }
        return null;
    }

    public void addQuest(String tag, QuestType type) {
        addQuest(tag, null, type, null);
    }


    public void addQuest(String tag, QuestType type, String description) {
        addQuest(tag, null, type, description);
    }

    public void addQuest(String tag, String status, QuestType type, String description) {
        // Check there is an existing quest
        Quest quest = null;
        for(int c = 0; c < quests.size; c++) {
            if(quests.items[c].tag.equals(tag)) {
                quest = quests.items[c];
                if(quest.type == QuestType.SUCCESS || quest.type == QuestType.FAILED)
                    return;     // cannot change quests after updating
                quests.removeIndex(c);
                break;
            }
        }
        if(quest == null) {
            if(description == null) {
                Sys.error(TAG, "New quest with no description for \"" + tag + "\"");
                return;
            }
            quest = new Quest();
            quest.tag = tag;
        }

        if(status == null) {
            if(type == QuestType.PENDING)
                status = s.defaultPendingText;
            else if(type == QuestType.FAILED)
                status = s.defaultFailedText;
            else if(type == QuestType.NOTE)
                status = s.defaultNoteText;
            else // if(type == QuestType.SUCCESS)
                status = s.defaultCompletedText;
        }

        quest.status = status;
        quest.type = type;
        if(description != null)
            quest.description = description;

        quests.insert(0, quest);
        isTrackerRequireRefresh = true;

        // Show indicator for all ScreenBars
        ScreenBar bar = null;
        while((bar = Globals.grid.iterate(bar, ScreenBar.class, false, null)) != null) {
            bar.showIrisIndicator();
        }
    }

    private float addQuestRow(int index, float y, boolean showLine) {
        Quest q = quests.items[index];

        StaticSprite iconView;
        TextBox statusView;
        TextBox descriptionView;

        if(q.type == QuestType.PENDING) {
            iconView = s.pendingIconView;
            statusView = s.pendingStatusTextView;
            descriptionView = s.pendingDescriptionView;
        }
        else if(q.type == QuestType.NOTE) {
            iconView = s.noteIconView;
            statusView = s.noteStatusTextView;
            descriptionView = s.noteDescriptionView;
        }
        else if(q.type == QuestType.FAILED) {
            iconView = s.failedIconView;
            statusView = s.failedStatusTextView;
            descriptionView = s.failedDescriptionView;
        }
        else { // if(q.type == QuestType.SUCCESS)
            iconView = s.successIconView;
            statusView = s.successStatusTextView;
            descriptionView = s.successDescriptionView;
        }

        iconView = iconView.instantiate().viewport(s.trackerSurface).attach();
        iconView.metrics.anchorY += y;
        statusView = statusView.instantiate().viewport(s.trackerSurface).attach();
        statusView.autoLengthText(q.status);
        statusView.metrics.anchorY += y;
        descriptionView = descriptionView.instantiate().viewport(s.trackerSurface).attach();
        descriptionView.autoLengthText(q.description);
        descriptionView.metrics.anchorY += y;

        // Calculate height
        float height = descriptionView.getLength() * descriptionView.metrics.scaleY;

        if(showLine && q.type != QuestType.PENDING && height > s.lineMinHeight) {
            float lineHeight = height - s.lineMinHeight;
            StaticSprite lineView = s.lineView.instantiate().viewport(s.trackerSurface).attach();
            lineView.metrics.anchorY += y;
            lineView.metrics.scaleY *= (lineHeight - s.lineMinHeight) / lineView.metrics.scaleY;
        }

        y -= height;

        return y;
    }

    public void refreshQuests() {
        isTrackerRequireRefresh = false;

        s.trackerSurface.detachChilds();

        float y = s.trackerStartY;

        // Start with pending quests
        int lastQuestLine = -1;
        for(int c = 0; c < quests.size; c++) {
            Quest q = quests.items[c];

            if(q.type == QuestType.PENDING) {
                y = addQuestRow(c, y, false);
                y -= s.trackerPendingY;
            }
            else
                lastQuestLine = c;
        }

        y -= s.trackerPendingPaddingY;

        // Then other quests
        for(int c = 0; c < quests.size; c++) {
            Quest q = quests.items[c];

            if(q.type != QuestType.PENDING) {
                y = addQuestRow(c, y, c < lastQuestLine);
                y -= s.trackerTimelineY;
            }
        }
    }

    public void clearQuests() {
        s.trackerSurface.detachChilds();
        quests.clear();
    }


    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        // Start with access view
        s.accessView.attach();
        refreshQuests();

    }

    public void clearNotification() {
        s.imageView.visual(null);
        s.iconView.visual(null);

        tAutoDismissScheduled = Float.MAX_VALUE;
    }

    public void refreshQuickAccess() {
        if(!s.quickView.renderingEnabled || !s.accessView.isAttached())
            return;     // not visible

        Homescreen homescreen = Globals.grid.homescreen;

        quickAccessApps.clear();

        // First add all apps that has notification
        for(int c = 0; c < homescreen.notificationApps.size; c++) {
            if(!addQuickAccessButton(homescreen.notificationApps.items[c]))
                break;
        }
        if(quickAccessApps.size < s.appButtons.length) {
            // Then add apps that were last opened
            for (int c = 0; c < homescreen.lastOpenedList.size; c++) {
                if(!addQuickAccessButton(homescreen.lastOpenedList.items[c]))
                    break;
            }
        }

        // Clear remaining quick access buttons
        for(int c = quickAccessApps.size; c < s.appButtons.length; c++)
            s.appButtons[c].detach();
    }

    private boolean addQuickAccessButton(String app) {
        Homescreen homescreen = Globals.grid.homescreen;
        // Check if reached max limit
        if(quickAccessApps.size >= s.appButtons.length)
            return false;       // no more space
        if(quickAccessApps.contains(app, true) || (homescreen.lastOpenedApp != null && homescreen.lastOpenedApp.equals(app)))
            return true;                // Already contains or its the same as the current app
        // Configure button
        Clickable actual = homescreen.resolveButton(app);
        if(actual == null)
            return true;       // cannot resolve, but still can proceed
        Clickable button = s.appButtons[quickAccessApps.size]
                .visuals(actual.buttonDown())
                .attach();
        quickAccessApps.add(app);       // recognize
        // Notification
        String text = homescreen.getNotificationText(app);
        StaticSprite noteView = button.find(s.appNoteView);
        if(text == null) {
            if(noteView != null)
                noteView.detach();
        }
        else {
            if(noteView == null)
                noteView = s.appNoteView.instantiate().viewport(button).attach();
            noteView.find(s.appNoteTextView).text(text);
        }
        return true;
    }

    public void show(Mesh image, Mesh icon, float tDismissTime, String title, String detail, String contextName) {
        clearNotification();

        isNotificationTouched = false;
        s.hintView.detach();

        notificationApp = contextName;

        s.accessView.detach();
        s.notificationGroup.attach();

        if(image != null)
            s.imageView.attach().visual(image);
        else
            s.imageView.detach();
        if(icon != null)
            s.iconView.attach().visual(icon);
        else
            s.iconView.detach();
        s.titleView.text(title);
        s.detailView.text(detail);

        if(tDismissTime <= 0)
            tDismissTime = s.tDismissTime;       // default

        tAutoDismissScheduled = getRenderTime() + tDismissTime;

        s.surface.seekGravityTarget(-1f, 0);
    }

    public Notification() {
        interfaceSource = new Builder<Object>(GBNotification.class, this);
        interfaceSource.build();

        // Load subtitles if necessary
        if(Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_SUBTITLE_ENABLED, false)) {
            loadSubtitles(Globals.subtitlesConfigFilename);
        }

        // Load saved state
        load(Globals.irisQuestSave);
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();
    }

    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        super.renderFinish(v, r, renderTime);

        // Downloading
        if(tDownloadStarted != -1) {
            float elapsed = renderTime - tDownloadStarted;
            if(elapsed > activeDownload.tDownloadTime) {
                s.downloadView.windowAnimation(s.downloadFinishedAnim.startAndReset(), true, true);
                s.downloadView.text(s.downloadOpenText);
                s.downloadView.enable();
                s.downloadProgressView.detachWithAnim();
                s.downloadFinishedView.attach();
                s.downloadProgressMat.show(0f, 360f);       // full circle

                tDownloadStarted = -1;

                if(activeDownload.onFinished != null)
                    activeDownload.onFinished.run();
            }
            else
                s.downloadProgressMat.show(0f, (elapsed / activeDownload.tDownloadTime) * 360f);
        }

        // Subtitle
        if(currentAutomatedSubtitle != null) {
            if(currentAutomatedSubtitle.isPlaying())
                updateSubtitles(currentAutomatedSubtitle.getPosition(), false);     // Defaults to portrait, landscape modes should be handled by caller
            else
                currentAutomatedSubtitle = null;        // Forget, caller should handle disposing
        }
        if(renderTime > tSubtitleEndScheduled)
            hideSubtitleView();

        // Save indicator progress
        if(renderTime > tSaveIconStarted) {
            float elapsed = renderTime - tSaveIconStarted;
            float progress = elapsed / s.tSaveIconAnimTime;
            s.saveIconAnim.progress(progress);
            if(elapsed > s.tSaveTotalTime) {
                // Enough time to show, stop
                s.saveView.detachWithAnim();
                tSaveIconStarted = Float.MAX_VALUE;
            }
        }

        // Check quick access needs refreshing
        if(s.quickView.renderingEnabled) {
            if(!wasRenderingQuickView) {
                refreshQuickAccess();
                if(s.accessView.isAttached()) {
                    if (isShowingTracker) {
                        s.trackerGroup.attach();        // if required to show tracker by default
                        if(isTrackerRequireRefresh)
                            refreshQuests();
                        s.trackerSurface.move(0, -1000);
                    }
                }
                wasRenderingQuickView = true;
                quickViewDirection = 0;     // reset
                lastQuickViewX = s.surface.movedX();
            }
            else {
                // Track direction to play sound
                float quickViewX = s.surface.movedX();
                if(quickViewX > lastQuickViewX) {
                    if(quickViewDirection != +1) {
                        if(quickViewDirection == 0)
                            s.slideHideSound.play();
                        quickViewDirection = +1;
                    }
                }
                else if (quickViewX < lastQuickViewX) {
                    if(quickViewDirection != -1) {
                        if(quickViewDirection == 0)
                            s.slideShowSound.play();
                        quickViewDirection = -1;
                    }
                }
                else if(!s.surface.isTouching())
                    quickViewDirection = 0;
                lastQuickViewX = quickViewX;
            }
        }
        else if(wasRenderingQuickView) {
            wasRenderingQuickView = false;
            // Hide tracker if showing
//            if(!s.surface.isSmoothMoving() && s.trackerGroup.isAttached()) {
            if(s.trackerGroup.isAttached()) {
                s.trackerGroup.detach();
            }
        }

        // Auto dismiss
        if(tAutoDismissScheduled != Float.MAX_VALUE) {
            if(!s.surface.isSmoothMoving() && s.surface.movedX() > -0.5f) {
                // Moved to the right, finished notification
                tAutoDismissScheduled = Float.MAX_VALUE;
                // Restore access view
                s.accessView.attach();
                s.notificationGroup.detach();
                // Hint to open next time
                if(!isNotificationTouched)
                    s.hintView.attach();
            }
            else if (renderTime > tAutoDismissScheduled) {
                if(!s.surface.isTouching() && !s.trackerGroup.isAttached()) {
                    tAutoDismissScheduled = getRenderTime() + s.tDismissTime;
                    s.surface.seekGravityTarget(+1f, 0);
                }
                else {
                    // Else is touching, restore to access view now
                    tAutoDismissScheduled = Float.MAX_VALUE;
                    // Restore access view
                    s.accessView.attach();
                    refreshQuickAccess();
                    if(isShowingTracker)
                        showTracker();
                    s.notificationGroup.detach();
                }
            }
        }

        // End tracker ignore
        if(!s.surface.isSmoothMoving())
            s.trackerGroup.inputEnabled = true;

        // Tracker cover
        if(s.trackerGroup.isAttached() && s.trackerGroup.renderingEnabled) {
            float x = s.trackerGroup.getX();

            if(x <= s.coverClearRenderX) {
                if(s.coverView.isAttached())
                    s.coverView.detach();         // cover is totally hidden
                if(!s.surface.isSmoothMoving())
                    SaraRenderer.renderer.clearBufferedRenderCalls();       // Clear render calls, because its hidden
            }
            else if(x > s.coverStartX) {
                if(s.coverView.isAttached())
                    s.coverView.detach();     // tracker view is not visible
            }
            else {
                if(!s.coverView.isAttached())
                    s.coverView.attach();
                // Else show alpha
                float progress = (s.coverStartX - x) / (s.coverStartX - s.coverEndX);
                if(progress > 1f)
                    progress = 1f;
                else if(progress < 0f)
                    progress = 0f;
                float alpha = s.coverAlphaGraph.generate(progress);
                s.coverColorAttribute.alpha(alpha);
            }
        }
        else if(s.coverView.isAttached())
            s.coverView.detach();         // tracker view is not visible

    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();

        clearNotification();
    }



    @Override
    public void onClick(Grid v, UIElement<?> view, int b) {
        if(view == s.quickView) {
            if(s.notificationGroup.isAttached() && notificationApp != null) {
                // Disable tracker UI temporarily (to allow pressing home button)
                s.trackerGroup.inputEnabled = false;
                // Open notification app
                v.homescreen.switchApp(notificationApp);
                // Dismiss
                s.surface.cancelTouch(v);
                s.surface.seekGravityTarget(+1f, 0);
                notificationApp = null;
                isNotificationTouched = true;
            }
            return;
        }

        // App buttons
        if(s.accessView.isAttached()) {
            for(int c = 0; c < s.appButtons.length; c++) {
                Clickable button = s.appButtons[c];
                if(view == button) {
                    // Disable tracker UI temporarily (to allow pressing home button)
                    s.trackerGroup.inputEnabled = false;
                    // Switch app
                    String app = quickAccessApps.items[c];
                    v.homescreen.switchApp(app);
                    // Dismiss
                    s.surface.cancelTouch(v);
                    s.surface.seekGravityTarget(+1f, 0);
                    return;
                }
            }
        }

        if(view == s.accessCloseButton) {
            // Pressed close on quick bar
            // Dismiss
            s.surface.cancelTouch(v);
            s.surface.seekGravityTarget(+1f, 0);
            return;
        }

        if(view == s.surface) {
            if(!s.surface.isSmoothMoving() && s.surface.movedX() > -0.5f) {
                v.postMessage(new Runnable() {
                    @Override
                    public void run() {
                        s.surface.stop();
                        s.surface.smoothMove(-s.surface.movedX() + s.hintMoveX, 0, s.tHintMoveTime);
                    }
                });
                if (!s.hintView.isAttached())
                    s.hintView.attach();
            }
            return;
        }

        if(view == s.exitButton) {
            if(!s.surface.isSmoothMoving())         // Only respond if surface is not moving
                s.exitDialogGroup.attach();
            return;
        }

        if(view == s.exitDialogYesButton) {
            // Exit to main menu
            v.photoRollApp.videoScreen.clear();
            Sys.system.activate(null);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Grid grid = new Grid(true);
                    Sys.system.activate(grid);
                }
            });
            return;
        }

        if(view == s.exitDialogNoButton) {
            s.exitDialogGroup.detachWithAnim();
            return;
        }

        if(view == s.downloadView) {
            if(s.downloadView.isWindowAnimating(s.downloadFinishedAnim)) {
                // Open if available and hide indicator
                s.downloadView.detachWithAnim();
                if(activeDownload.onOpen != null)
                    activeDownload.onOpen.run();
                // Start next download if queued
                activeDownload = null;
                if(queuedDownloads.size > 0)
                    startDownload(queuedDownloads.removeIndex(0));
                else {
                    tDownloadStarted = -1;
                    s.downloadView.detachWithAnim();
                }
            }
            return;
        }
    }

    @Override
    public void onPressed(Grid v, UIElement<?> view, float x, float y, int button) {
        if(view == s.surface) {
            isNotificationTouched = true;
            if(v.keyboard.isShowing())
                v.keyboard.hide();
        }
    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(inputType == INPUT_KEY_UP && (key == Input.Keys.BACK || key == Input.Keys.ESCAPE) && s.trackerGroup.isAttached() && !s.surface.isSmoothMoving() && !s.surface.isTouching() && s.surface.movedX() < -0.5f) {
            if(isShowingTracker)
                s.exitDialogGroup.attach();             // show exit dialog only if tracker and notification bar is showing and homescreen is showing
            else if(s.exitDialogGroup.isAttached())
                s.exitDialogGroup.detachWithAnim();     // on other screens other than homescreen, close dialog
            else
                s.surface.seekGravityTarget(+1f, 0);    // on other screens other than homescreen, hide notification
            return true;
        }
        return false;
    }
}
