package game31.app.browser;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Locale;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.JsonSource;
import game31.Keyboard;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.ScriptState;
import game31.app.homescreen.Homescreen;
import game31.gb.browser.GBBrowserHomeScreen;
import game31.model.BrowserAppModel;
import game31.renderer.LayoutDescriptor;
import game31.renderer.LayoutMesh;
import game31.renderer.PixelationAttribute;
import sengine.Entity;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.audio.Audio;
import sengine.calc.Range;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.InputField;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;
import sengine.utils.LiveEditor;

/**
 * Created by Azmi on 7/8/2017.
 */

public class BrowserScreen extends Menu<Grid> implements OnClick<Grid>, Homescreen.App, Keyboard.KeyboardInput {
    private static final String TAG = "BrowserScreen";

    public static final String WEB_PREFIX = "web://";
    public static final String TRIGGER_PREFIX = "trigger://";

    public static final String INPUT_TYPE_NUMERIC = "numeric";

    public static final String STATE_MOST_VISITED_SUFFIX = ".mostVisited";
    public static final String STATE_BOOKMARKS_SUFFIX = ".bookmarks";
    public static final String STATE_HISTORY_SUFFIX = ".history";

    public static class Internal {
        // Window
        public UIElement<?> window;
        public ScreenBar bars;

        // Home group
        public UIElement.Group homeGroup;
        public ScrollableSurface homeSurface;
        public Clickable homeBookmarkRow;
        public TextBox homeBookmarkNameView;
        public TextBox homeBookmarkTitleView;
        public StaticSprite homeBookmarkFaviconView;
        public float homeBookmarkStartY;
        public StaticSprite homeNavView;
        public Clickable homeNavUrlEmptyButton;
        public Clickable homeNavUrlButton;
        public StaticSprite homeNavFaviconView;
        public Clickable[] homeMostVisitedButtons;
        public TextBox homeMostVisitedTitleView;
        public StaticSprite homeMostVisitedFaviconView;
        public StaticSprite homeMostVisitedPreviewView;

        // Dropdown group
        public UIElement.Group dropGroup;
        public ScrollableSurface dropSurface;
        public StaticSprite dropNavFaviconView;
        public InputField dropNavUrlView;
        public Clickable dropNavUrlButton;
        public Clickable dropNavHomeButton;
        public Clickable dropNavCloseButton;
        public Clickable dropBookmarksSectionView;
        public Clickable dropHistorySectionView;
        public Clickable dropLinkButton;
        public String dropLinkFormat;
        public StaticSprite dropLinkFaviconView;
        public StaticSprite dropEndSection;
        public Sprite dropDefaultFavicon;


        // Web group
        public UIElement.Group webGroup;
        public StaticSprite webNavView;
        public StaticSprite webNavFaviconView;
        public Clickable webNavUrlButton;
        public Clickable webNavHomeButton;
        public Clickable webNavCloseButton;
        public StaticSprite webBgView;
        public ScrollableSurface webSurface;
        public float webSurfacePaddingWithInput;
        public float webSurfacePaddingNormal;
        public int webImageTarget;
        public int webTextTarget;
        public HorizontalProgressBar webLoadingBar;
        public float tLoadingSeekSpeed;

        // Loading simulation parameters
        public Range textBandwidth;            // Characters
        public int maxTextBandwidth;
        public Range imageBandwidth;           // Number of pixels
        public int maxImageBandwidth;
        public Range tLoadingInterval;                 // Interval between loads
        public float[] pixelationLevels;
        public float minImageProgress;
        public Range tConnectingTime;
        public float connectingProgress;
        public int maxCachedObjects;
        public float maxCachedProgress;

        // Selection
        public Clickable selectButton;
        public PatchedSprite selectSprite;
        public float selectMinWidth;
        public float selectMinHeight;
        public float selectCornerSize;

        // Input fields
        public InputField inputField;
        public float inputMaxBottomY;

        // Checkbox
        public Clickable checkbox;
        public Animation checkboxTickedAnim;
        public Animation checkboxUntickedAnim;

        // Sounds
        public Audio.Sound backSound;
    }

    public static class PageDescriptor {
        public final String name;
        public final String title;
        public final Sprite favicon;
        public final String url;
        public final boolean isPublic;
        public final float connectingSpeed;
        public final float speed;
        public final Sprite preview;          // only used for most visited section

        private PageDescriptor(BrowserAppModel.PageModel model) {
            this.name = model.name;
            this.title = model.title;
            this.favicon = Sprite.load(model.favicon);
            this.url = model.url;
            this.isPublic = model.isPublic;
            this.connectingSpeed = model.connectingSpeed;
            this.speed = model.speed;
            this.preview = model.previewFilename != null ? Sprite.load(model.previewFilename) : null;
        }
    }

    private class ImageLoadingTracker {
        final StaticSprite view;
        final PixelationAttribute pixelator;
        final int totalBytes;

        int downloadedBytes = 0;

        ImageLoadingTracker(StaticSprite view) {
            this.view = view;
            this.pixelator = view.visual().getAttribute(PixelationAttribute.class, 0);
            this.totalBytes = (int)(pixelator.textureResolution.x * pixelator.textureResolution.y);

            // Check if exists in cache
            Object cached = view.visual().getMaterial();
            int index = cachedObjects.indexOf(cached, true);
            if(index != -1) {
                int downloaded = cachedDownloaded.items[index];
                int maxDownloadAllowed = Math.round((float)totalBytes * s.maxCachedProgress);
                if(downloaded == -1 || downloaded > maxDownloadAllowed)
                    downloadedBytes = maxDownloadAllowed;
                else
                    downloadedBytes = downloaded;
                // Move this object to top of cache
                cachedObjects.removeIndex(index);
                cachedDownloaded.removeIndex(index);
                cachedObjects.add(cached);
                cachedDownloaded.add(downloaded);
            }

            // Update view pixelation
            updateViewPixelation();
        }

        void updateViewPixelation() {
            float progress = ((float)downloadedBytes / (float)totalBytes);
            if(!view.isAttached() && progress >= s.minImageProgress) {
                // Downloaded enough to show this image
                view.attach();
                s.webSurface.refresh();     // This is to refresh render visibility status
            }
            int pixelationLevel = Math.round((1.0f - progress) * (float)(s.pixelationLevels.length - 1));
            pixelator.pixelation(s.pixelationLevels[pixelationLevel]);

        }
    }

    private class TextLoadingTracker {
        final TextBox textBox;
        final int totalBytes;

        int downloadedBytes = 0;

        TextLoadingTracker(TextBox textBox) {
            this.textBox = textBox;
            this.totalBytes = textBox.text().text.length();

            // Check if exists in cache
            Object cached = textBox.text().text;
            int index = cachedObjects.indexOf(cached, true);
            if(index != -1) {
                int downloaded = cachedDownloaded.items[index];
                int maxDownloadAllowed = Math.round((float)totalBytes * s.maxCachedProgress);
                if(downloaded == -1 || downloaded > maxDownloadAllowed)
                    downloadedBytes = maxDownloadAllowed;
                else
                    downloadedBytes = downloaded;
                // Move this object to top of cache
                cachedObjects.removeIndex(index);
                cachedDownloaded.removeIndex(index);
                cachedObjects.add(cached);
                cachedDownloaded.add(downloaded);
            }
        }
    }

    // Sources
    private final Builder<Object> builder;
    private Internal s;

    // Loaded
    private final ObjectMap<String, PageDescriptor> pages = new ObjectMap<>();
    private String configFilename;



    // Current
    private boolean isShowingHome = false;

    private final Array<PageDescriptor> mostVisited = new Array<>(PageDescriptor.class);
    private final Array<PageDescriptor> bookmarks = new Array<>(PageDescriptor.class);
    private final Array<PageDescriptor> history = new Array<>(PageDescriptor.class);

    private final ObjectMap<Clickable, PageDescriptor> homeBookmarkRows = new ObjectMap<>();


    // Current loading
    private final Array<PageDescriptor> pageStack = new Array<>(PageDescriptor.class);
    // Cache
    private final Array<Object> cachedObjects = new Array<>(Object.class);
    private final IntArray cachedDownloaded = new IntArray();
    private final Array<Object> tempCachedObjects = new Array<>(Object.class);
    private final IntArray tempCachedDownloaded = new IntArray();
    // Loading
    private final Array<ImageLoadingTracker> imageTracker = new Array<>(ImageLoadingTracker.class);
    private final Array<TextLoadingTracker> textTracker = new Array<>(TextLoadingTracker.class);
    private int totalTracked = 0;
    private float tNextLoadScheduled = Float.MAX_VALUE;
    private final ObjectMap<Clickable, String> links = new ObjectMap<>();
    private final Array<InputField> inputs = new Array<>(InputField.class);
    private final Array<Clickable> checkboxes = new Array<>(Clickable.class);
    private PageDescriptor page;
    private LayoutDescriptor layout;

    // Scheduled to load
    private float tOpenScheduled = Float.MAX_VALUE;
    private PageDescriptor openingPage = null;

    // Dropdown links
    private final ObjectMap<Clickable, PageDescriptor> dropLinks = new ObjectMap<>();

    private boolean isDropRequiresRefresh = false;
    private boolean isMostVisitedRequiresRefresh = false;
    private boolean isHomeBookmarksRequiresRefresh = false;

    // Current selected input field
    private InputField activeField = null;
    private int activeFieldIndex = -1;

    // Transition
    private Entity<?> fromScreen = null;
    private float transitionX;
    private float transitionY;
    private float transitionSize;
    private boolean closeTabOnRelease = false;

    public UIElement.Group webGroup() {
        return s.webGroup;
    }

    public void open(Entity<?> transitionFrom, Entity<?> target, float x, float y, float size) {
        fromScreen = transitionFrom;
        transitionX = x;
        transitionY = y;
        transitionSize = size;
        if(transitionSize == -1) {
            ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(transitionFrom, this, target);
            transition.attach(target);
        }
        else {
            ScreenTransition transition = ScreenTransitionFactory.createHomescreenOutTransition(
                    transitionFrom, this, target,
                    x, y, size
            );
            transition.attach(target);
        }
    }

    public void close(Entity<?> target) {
        if(fromScreen == null)
            return;     // not open
        // Trigger
        if(!Globals.grid.trigger(Globals.TRIGGER_LEAVING_BROWSER_SCREEN))
            return;     // not allowed to leave
        if(transitionSize == -1) {
            // Fade transition
            ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, fromScreen, target);
            transition.attach(target);
        }
        else {
            ScreenTransition transition = ScreenTransitionFactory.createHomescreenInTransition(
                    this, fromScreen, target,
                    transitionX,
                    transitionY,
                    transitionSize
            );
            transition.attach(target);
        }
        fromScreen = null;
        closeTabOnRelease = true;
    }

    public LayoutDescriptor getLayout(String url) {
        return File.getHints(configFilename + "/" + url);
    }

    public PageDescriptor getPage(String url) {
        return pages.get(url);
    }


    public PageDescriptor currentPage() {
        return page;
    }

    public LayoutDescriptor currentLayout() {
        return layout;
    }

    public void reloadCurrentPage() {
        final String TRANSIENT_PREFIX = "transient/";
        final String PNG_SUFFIX = ".png";
        final String PNG_TEXTURE_SUFFIX = ".png.texture";

        if(page == null || configFilename == null)
            return;     // no loaded or not even loaded anything yet
        // Load back model
        BrowserAppModel model = new JsonSource<>(configFilename, BrowserAppModel.class).load();
        // Find for page
        BrowserAppModel.PageModel pageModel = null;
        for(int c = 0; c < model.pages.length; c++) {
            BrowserAppModel.PageModel p = model.pages[c];
            if (p.url.equals(page.url)) {
                pageModel = p;
                break;
            }
        }
        if(pageModel == null) {
            Sys.error(TAG, "Unable to reload, page \"" + page.url + "\" not found in \"" + configFilename);
            return;
        }

        // Keep layout files for backwards compatibility with other pages

        // Remove content files
        for(int c = 0; c < layout.contentSprites.length; c++) {
            File.open(TRANSIENT_PREFIX + layout.contentName + c + PNG_SUFFIX).delete();
            File.open(layout.contentName + c + PNG_TEXTURE_SUFFIX).delete();
            File.removeHints(layout.contentName + c + PNG_SUFFIX);
            File.removeHints(layout.contentName + c + PNG_SUFFIX + Sprite.EXTENSION);
        }

        // Recreate page
        PageDescriptor descriptor = new PageDescriptor(pageModel);
        pages.put(descriptor.url, descriptor);
        // Compile layout
        LayoutDescriptor layout = new LayoutDescriptor(pageModel.layout);
        File.saveHints(configFilename + "/" + descriptor.url, layout);

        // Refresh
        pageStack.pop();
        showPage(page);
        finishLoad();
    }

    public void clearMostVisited() {
        mostVisited.clear();
        isMostVisitedRequiresRefresh = true;
    }

    public void insertMostVisited(int index, String url) {
        PageDescriptor page = getPage(url);
        if(page == null)
            throw new RuntimeException("Most visited page not found \"" + url + "\"");
        if(index > mostVisited.size) {
            Sys.error(TAG, "Cannot insert most visited page at index \"" + index + "\", current size is \"" + mostVisited.size + "\"");
            index = mostVisited.size;
        }
        mostVisited.insert(index, page);
        isMostVisitedRequiresRefresh = true;
    }

    public void addMostVisited(String url) {
        insertMostVisited(mostVisited.size, url);
    }

    public void clearBookmarks() {
        bookmarks.clear();
        isDropRequiresRefresh = true;
        isHomeBookmarksRequiresRefresh = true;
    }

    public void insertBookmarks(int index, String url) {
        PageDescriptor page = getPage(url);
        if(page == null)
            throw new RuntimeException("Bookmark page not found \"" + url + "\"");
        if(index > bookmarks.size) {
            Sys.error(TAG, "Cannot insert bookmark page at index \"" + index + "\", current size is \"" + bookmarks.size + "\"");
            index = bookmarks.size;
        }
        bookmarks.insert(index, page);
        isDropRequiresRefresh = true;
        isHomeBookmarksRequiresRefresh = true;
    }

    public void addBookmark(String url) {
        insertBookmarks(bookmarks.size, url);
    }

    public void clearHistory() {
        history.clear();
        isDropRequiresRefresh = true;
    }

    public void insertHistory(int index, String url) {
        PageDescriptor page = getPage(url);
        if(page == null)
            throw new RuntimeException("History page not found \"" + url + "\"");
        insertHistory(index, page);
    }

    private void insertHistory(int index, PageDescriptor page) {
        if(index > history.size) {
            Sys.error(TAG, "Cannot insert history page at index \"" + index + "\", current size is \"" + history.size + "\"");
            index = history.size;
        }
        history.insert(index, page);
        isDropRequiresRefresh = true;
    }

    public void addHistory(String url) {
        insertHistory(history.size, url);
    }


    private void prepareMostVisitedMenu() {
        if(!isMostVisitedRequiresRefresh)
            return;
        isMostVisitedRequiresRefresh = false;

        // Discard extras
        if(mostVisited.size > s.homeMostVisitedButtons.length)
            mostVisited.removeRange(s.homeMostVisitedButtons.length, mostVisited.size - 1);
        for(int c = 0; c < mostVisited.size; c++) {
            PageDescriptor page = mostVisited.items[c];
            // Configure ui
            Clickable button = s.homeMostVisitedButtons[c].attach();
            button.find(s.homeMostVisitedFaviconView).visual(page.favicon);
            button.find(s.homeMostVisitedTitleView).text(page.title);
            button.find(s.homeMostVisitedPreviewView).visual(page.preview);
        }
        for(int c = mostVisited.size; c < s.homeMostVisitedButtons.length; c++)
            s.homeMostVisitedButtons[c].detach();       // remove extras
    }

    private void prepareHomeBookmarks() {
        if(!isHomeBookmarksRequiresRefresh)
            return;
        isHomeBookmarksRequiresRefresh = false;

        // Clear current rows
        for(ObjectMap.Entry<Clickable, PageDescriptor> entry : homeBookmarkRows)
            entry.key.detach();
        homeBookmarkRows.clear();

        // Attach each row
        float surfaceY = s.homeBookmarkStartY;
        for(int c = 0; c < bookmarks.size; c++) {
            // Create new row
            PageDescriptor page = bookmarks.items[c];
            Clickable row = s.homeBookmarkRow.instantiate();
            row.find(s.homeBookmarkNameView).text(page.name);
            row.find(s.homeBookmarkTitleView).text(page.title);
            row.find(s.homeBookmarkFaviconView).visual(page.favicon);
            // Position and attach
            row.metrics.anchorY = surfaceY;
            row.viewport(s.homeSurface).attach();
            surfaceY -= row.getHeight();
            // Remember
            homeBookmarkRows.put(row, page);
        }
    }

    private void prepareDropMenu() {
        if(!isDropRequiresRefresh)
            return;
        isDropRequiresRefresh = false;

        // Clear
        dropLinks.clear();

        // Prepare drop menu
        float surfaceY = +(s.dropSurface.getLength() / 2f) - s.dropSurface.paddingTop();
        s.dropSurface.detachChilds();

        // Bookmarks
        if(bookmarks.size> 0) {
            s.dropBookmarksSectionView.viewport(s.dropSurface).attach();
            s.dropBookmarksSectionView.metrics.anchorY = surfaceY;
            surfaceY -= s.dropBookmarksSectionView.getLength();
            for (int c = 0; c < bookmarks.size; c++) {
                PageDescriptor page = bookmarks.items[c];
                // Add bookmark link
                Clickable link = s.dropLinkButton.instantiate().viewport(s.dropSurface).attach();
                link.metrics.anchorY = surfaceY;
                surfaceY -= link.getLength();
                // Configure bookmark link
                link.text(String.format(Locale.US, s.dropLinkFormat, page.name, page.title));
                link.find(s.dropLinkFaviconView).visual(page.favicon);
                // Recognize input
                dropLinks.put(link, page);
            }
        }
        // History
        if(history.size > 0) {
            s.dropHistorySectionView.viewport(s.dropSurface).attach();
            s.dropHistorySectionView.metrics.anchorY = surfaceY;
            surfaceY -= s.dropHistorySectionView.getLength();
            for(int c = 0; c < history.size; c++) {
                PageDescriptor page = history.items[c];
                // Add bookmark link
                Clickable link = s.dropLinkButton.instantiate().viewport(s.dropSurface).attach();
                link.metrics.anchorY = surfaceY;
                surfaceY -= link.getLength();
                // Configure bookmark link
                link.text(String.format(Locale.US, s.dropLinkFormat, page.name, page.title));
                link.find(s.dropLinkFaviconView).visual(page.favicon);
                // Recognize input
                dropLinks.put(link, page);
            }
        }
        // End section
        s.dropEndSection.viewport(s.dropSurface).attach();
        s.dropEndSection.metrics.anchorY = surfaceY;
    }

    public void load(final String filename, final ScriptState state) {
        // Reset
        pages.clear();

        clearMostVisited();
        clearBookmarks();
        clearHistory();
        clearCache();
        clearPageTab();

        // Load config
        configFilename = filename;
        BrowserAppModel model = new JsonSource<>(filename, BrowserAppModel.class).load();

        // Load all pages
        for(int c = 0; c < model.pages.length; c++) {
            BrowserAppModel.PageModel pageModel = model.pages[c];
            PageDescriptor descriptor = new PageDescriptor(pageModel);
            pages.put(descriptor.url, descriptor);
            // Compile layout if required to
            if(Globals.rebuildAllWebsites) {
                LayoutDescriptor layout = new LayoutDescriptor(pageModel.layout);
                File.saveHints(configFilename + "/" + descriptor.url, layout);
            }
        }


        // Public pages

        // Load most visited
        String[] saved = state.get(configFilename + STATE_MOST_VISITED_SUFFIX, null);
        if(!Globals.d_ignoreSurferSaves && saved != null) {
            for(int c = 0; c < saved.length; c++)
                addMostVisited(saved[c]);
        }
        else {
            for(int c = 0; c < model.mostVisited.length; c++)
                addMostVisited(model.mostVisited[c]);
        }
        // Bookmarks
        saved = state.get(configFilename + STATE_BOOKMARKS_SUFFIX, null);
        if(!Globals.d_ignoreSurferSaves && saved != null) {
            for(int c = 0; c < saved.length; c++)
                addBookmark(saved[c]);
        }
        else {
            for(int c = 0; c < model.bookmarks.length; c++)
                addBookmark(model.bookmarks[c]);
        }
        // History
        saved = state.get(configFilename + STATE_HISTORY_SUFFIX, null);
        if(!Globals.d_ignoreSurferSaves && saved != null) {
            for(int c = 0; c < saved.length; c++)
                addHistory(saved[c]);
        }
        else {
            for(int c = 0; c < model.history.length; c++)
                addHistory(model.history[c]);
        }
    }

    public void pack(ScriptState state) {
        // Most visited
        String[] saved = new String[mostVisited.size];
        for(int c = 0; c < mostVisited.size; c++)
            saved[c] = mostVisited.items[c].url;
        state.set(configFilename + STATE_MOST_VISITED_SUFFIX, saved);
        // Bookmarks
        saved = new String[bookmarks.size];
        for(int c = 0; c < bookmarks.size; c++)
            saved[c] = bookmarks.items[c].url;
        state.set(configFilename + STATE_BOOKMARKS_SUFFIX, saved);
        // History
        saved = new String[history.size];
        for(int c = 0; c < history.size; c++)
            saved[c] = history.items[c].url;
        state.set(configFilename + STATE_HISTORY_SUFFIX, saved);
    }

    public void openPage(String url) {
        PageDescriptor page = getPage(url);
        if(page == null)
            throw new IllegalArgumentException("Page not found \"" + url + "\"");
        openPage(page);

    }

    private void openPage(PageDescriptor page) {
        openingPage = page;
        // Schedule opening
        float tConnectingTime = s.tConnectingTime.generate() * page.connectingSpeed;
        tOpenScheduled = getRenderTime() + tConnectingTime;
        // Show progress
        s.webLoadingBar.progress(0);
        s.webLoadingBar.attach();
        // float tBarSeekTime = Math.min(tConnectingTime, s.tConnectingBarTime);
        s.webLoadingBar.seek(s.connectingProgress, tConnectingTime);
        // Freeze loading
        if(tNextLoadScheduled != Float.MAX_VALUE)
            tNextLoadScheduled = tOpenScheduled;
    }

    public String getInput(String name) {
        if(layout == null) {
            Sys.error(TAG, "Layout not set for input \"" + name + "\"");
            return null;
        }
        for(int c = 0; c < layout.inputs.length; c++) {
            LayoutDescriptor.LayoutInput layoutInput = layout.inputs[c];
            if(layoutInput.name.equals(name))
                return inputs.items[c].text();
        }
        // Not found
        Sys.error(TAG, "Input \"" + name + "\" not found for page \"" + page.url + "\"");
        return null;
    }

    public String getCheckbox(String groupName) {
        if(layout == null) {
            Sys.error(TAG, "Layout not set for checkbox \"" + groupName + "\"");
            return null;
        }
        for(int c = 0; c < layout.checkboxes.length; c++) {
            LayoutDescriptor.LayoutCheckbox layoutCheckbox = layout.checkboxes[c];
            if(layoutCheckbox.groupName.equals(groupName) && checkboxes.items[c].windowAnim.anim == s.checkboxTickedAnim)
                return layoutCheckbox.name;
        }
        // Not ticked
        return null;
    }

    public void centerCheckbox(String checkboxName) {
        // Find this checkbox
        for(int c = 0; c < layout.checkboxes.length; c++) {
            LayoutDescriptor.LayoutCheckbox layoutCheckbox = layout.checkboxes[c];
            if(layoutCheckbox.name.equals(checkboxName)) {
                // Center scrollablesurface here
                float y = checkboxes.items[c].getY();
                s.webSurface.move(0, (s.webSurface.getLength() / 2f) -y);
            }
        }
    }

    public void activateField(String field) {
        if(layout == null) {
            Sys.error(TAG, "Layout not set to activate field \"" + field + "\"");
            return;
        }
        for(int c = 0; c < layout.inputs.length; c++) {
            LayoutDescriptor.LayoutInput layoutInput = layout.inputs[c];
            if(layoutInput.name.equals(field)) {
                activateField(c);
                return;
            }
        }
        // Not found
        Sys.error(TAG, "Input \"" + field + "\" not found to activate in page \"" + page.url + "\"");

    }

    private void activateField(int index) {
        cancelActiveField();
        activeField = inputs.items[index];
        activeFieldIndex = index;

        if(activeField.text() == null)
            activeField.text("");           // To show cursor

        activeField.animateCursor(true);

        // Show keyboard
        Keyboard keyboard = Globals.grid.keyboard;
        keyboard.resetAutoComplete();
        keyboard.showKeyboard(this, viewport, 0, false, false, false, true, this, activeField.text(), layout.inputs[index].confirmText);

        // Make sure its visible and keyboard doesnt hide it
        float fieldBottom = activeField.getBottom();
        if(fieldBottom < s.inputMaxBottomY || !activeField.isEffectivelyRendering()) {
            s.webSurface.padding(
                    s.webSurface.paddingLeft(),
                    s.webSurface.paddingTop(),
                    s.webSurface.paddingRight(),
                    s.webSurfacePaddingWithInput
            );
            s.webSurface.move(0, +(s.inputMaxBottomY - fieldBottom));
        }

        // If numeric
        if(layout.inputs[activeFieldIndex].inputType.equals(INPUT_TYPE_NUMERIC))
            keyboard.showNumeric();
    }

    private void cancelActiveField() {
        if(activeField == null)
            return;
        activeField.animateCursor(false);
        activeField = null;
        activeFieldIndex = -1;
        s.webSurface.padding(
                s.webSurface.paddingLeft(),
                s.webSurface.paddingTop(),
                s.webSurface.paddingRight(),
                s.webSurfacePaddingNormal
        );
    }

    private void cancelPageOpening() {
        openingPage = null;
        tOpenScheduled = Float.MAX_VALUE;
        // Hide if not loading anymore
        if(tNextLoadScheduled == Float.MAX_VALUE)
            s.webLoadingBar.detachWithAnim();
    }

    private void clearCache() {
        cachedObjects.clear();
        cachedDownloaded.clear();
        tempCachedObjects.clear();
        tempCachedDownloaded.clear();
    }

    public void clearPageTab() {
        pageStack.clear();
        clearPage();
    }

    private void clearPage() {
        // Save current page progress if available
        saveCurrentPageCache();

        cancelActiveField();

        openingPage = null;
        tOpenScheduled = Float.MAX_VALUE;

        if(page != null)
            Globals.grid.trigger(Globals.TRIGGER_LEAVING_BROWSER_PAGE);
        page = null;
        layout = null;

        imageTracker.clear();
        textTracker.clear();
        links.clear();
        inputs.clear();
        checkboxes.clear();
        totalTracked = 0;
        tNextLoadScheduled = Float.MAX_VALUE;
    }

    public void finishLoad() {
        for(int c = 0; c < imageTracker.size; c++) {
            ImageLoadingTracker tracker = imageTracker.items[c];
            // Finished loading this image
            if(!tracker.view.isAttached())
                tracker.view.attach();
            tracker.pixelator.pixelation(-1);           // loaded full
        }
        imageTracker.clear();

        for(int c = 0; c < textTracker.size; c++) {
            TextLoadingTracker tracker = textTracker.items[c];
            tracker.textBox.attach();
        }
        textTracker.clear();

        // Finished loading
        s.webLoadingBar.seek(1f, s.tLoadingSeekSpeed).detachWithAnim();
        tNextLoadScheduled = Float.MAX_VALUE;
    }

    private void saveCurrentPageCache() {
        if(layout == null)
            return;

        // Save download progresses
        for(int c = 0; c < imageTracker.size; c++) {
            ImageLoadingTracker tracker = imageTracker.items[c];
            tempCachedObjects.add(tracker.view.visual().getMaterial());     // save material
            tempCachedDownloaded.add(tracker.downloadedBytes);
        }
        for(int c = 0; c < textTracker.size; c++) {
            TextLoadingTracker tracker = textTracker.items[c];
            tempCachedObjects.add(tracker.textBox.text().text);     // save material
            tempCachedDownloaded.add(tracker.downloadedBytes);
        }

        // Merge into normal cache
        for(int c = 0; c < tempCachedObjects.size; c++) {
            Object cached = tempCachedObjects.items[c];
            int downloaded = tempCachedDownloaded.items[c];
            int index = cachedObjects.indexOf(cached, true);
            if(index != -1)
                cachedDownloaded.items[index] = downloaded;         // already cached, just update
            else {
                // New object
                cachedObjects.add(cached);
                cachedDownloaded.add(downloaded);
            }
        }

        // Reset
        tempCachedObjects.clear();
        tempCachedDownloaded.clear();

        // Now trim
        if(cachedObjects.size > s.maxCachedObjects) {
            int trimmed = (cachedObjects.size - s.maxCachedObjects) - 1;
            cachedObjects.removeRange(0, trimmed);
            cachedDownloaded.removeRange(0, trimmed);
        }
    }

    public void showPage(PageDescriptor page) {
        // Clear
        clearPage();

        // Get layout
        layout = getLayout(page.url);
        this.page = page;

        // Add to stack
        if(pageStack.size == 0 || pageStack.peek() != page)
            pageStack.add(page);

        // Add to history if the last one is not the same page
        if(!page.url.equals(Globals.BROWSER_NOT_FOUND_PAGE) && (history.size == 0 || history.items[0] != page))
            insertHistory(0, page);

        // Show correct view
        showWebView();

        // Update nav bars
        s.webNavFaviconView.visual(page.favicon);
        s.webNavUrlButton.text(page.url);

        // Clear web surface
        s.webSurface.detachChilds();
        s.webSurface.padding(
                s.webSurface.paddingLeft(),
                s.webSurface.paddingTop(),
                s.webSurface.paddingRight(),
                s.webSurfacePaddingNormal
        );

        // Reset loading bar
        if(!s.webLoadingBar.isAttached())
            s.webLoadingBar.attach();
        s.webLoadingBar.progress(s.connectingProgress);

        // Compose page
        // Background color
        ColorAttribute.of(s.webBgView.visual()).set(layout.bgColor);
        float layoutLength = (float)layout.height / (float)layout.width;
        float surfaceY = +(s.webSurface.getLength() / 2f) - s.webSurface.paddingTop() - (layoutLength / 2f);

        // Dummy element to enforce min surface size
        new UIElement.Group()
                .viewport(s.webSurface)
                .metrics(new UIElement.Metrics().move(0, surfaceY))
                .length(layoutLength)
                .attach();

        // Layout
        for(int c = 0; c < layout.layoutMeshes.length; c++) {
            LayoutMesh mesh = layout.layoutMeshes[c].instantiate();
            // Configure pixelation
            mesh.getAttribute(PixelationAttribute.class, 0)
                    .resolution(layout.sectionSize, layout.sectionSize);
            // Create layout
            StaticSprite view = new StaticSprite()
                    .viewport(s.webSurface)
                    .metrics(new UIElement.Metrics().move(0, surfaceY))
                    .visual(mesh, s.webImageTarget)
                    .attach();
            imageTracker.add(new ImageLoadingTracker(view));
        }

        // Content
        for(int c = 0; c < layout.contentSprites.length; c++) {
            LayoutDescriptor.ContentSprite content = layout.contentSprites[c];
            // Configure pixelation
            Sprite sprite = content.sprite.instantiate();
            sprite.getAttribute(PixelationAttribute.class, 0)
                    .resolution(content.width, content.height);
            // Create content sprite
            StaticSprite view = new StaticSprite()
                    .viewport(s.webSurface)
                    .metrics(new UIElement.Metrics().scale(content.size).move(content.x, surfaceY + content.y))
                    .visual(sprite, s.webImageTarget)
                    ; // .attach();
            imageTracker.add(new ImageLoadingTracker(view));
        }

        // Texts
        for(int c = 0; c < layout.texts.length; c++) {
            LayoutDescriptor.LayoutText layoutText = layout.texts[c];
            TextBox textBox = new TextBox()
                    .viewport(s.webSurface)
                    .metrics(new UIElement.Metrics().scale(layoutText.size).move(layoutText.x, surfaceY + layoutText.y))
                    .text(layoutText.text.instantiate().target(s.webTextTarget).wrapChars(layoutText.wrapChars))
                    .animation(layoutText.startAnim, layoutText.idleAnim, layoutText.endAnim)
                    .windowAnimation(new ColorAnim(layoutText.color).startAndReset(), true, true)
                    ;

            textTracker.add(new TextLoadingTracker(textBox));
        }

        // Count total items to load
        totalTracked = imageTracker.size + textTracker.size;

        // Links
        for(int c = 0; c < layout.links.length; c++) {
            LayoutDescriptor.LayoutLink layoutLink = layout.links[c];
            // Create mesh
            float width = Math.max(layoutLink.width, s.selectMinWidth);
            float height = Math.max(layoutLink.height, s.selectMinHeight);
            float length = height / width;
            float cornerSize = (1f / width) * s.selectCornerSize;
            PatchedSprite patch = s.selectSprite.instantiate(length, cornerSize);
            // Create button
            Clickable button = s.selectButton.instantiate()
                    .viewport(s.webSurface)
                    .metrics(new UIElement.Metrics().scale(width).move(layoutLink.x, surfaceY + layoutLink.y))
                    .visuals(null, patch, s.selectButton.target())
                    .attach();
            links.put(button, layoutLink.action);
        }

        // Inputs
        for(int c = 0; c < layout.inputs.length; c++) {
            LayoutDescriptor.LayoutInput layoutInput = layout.inputs[c];
            // Create input field
            InputField input = s.inputField.instantiate()
                    .viewport(s.webSurface)
                    .metrics(new UIElement.Metrics().scale(layoutInput.size).move(layoutInput.x, surfaceY + layoutInput.y))
                    .font(layoutInput.font, layoutInput.length, layoutInput.wrapChars, s.inputField.target())
                    .inputPadding(layoutInput.inputPaddingLeft, layoutInput.inputPaddingTop, layoutInput.inputPaddingRight, layoutInput.inputPaddingBottom)
                    .windowAnimation(new ColorAnim(layoutInput.color).loopAndReset(), true, true)
                    .attach();
            // Recognize intent
            inputs.add(input);
        }

        // Checkbox
        for(int c = 0; c < layout.checkboxes.length; c++) {
            LayoutDescriptor.LayoutCheckbox layoutCheckbox = layout.checkboxes[c];
            // Create checkbox
            Mesh mat = s.checkbox.buttonUp().instantiate();
            ColorAttribute.of(mat).set(layoutCheckbox.color);
            Clickable checkbox = s.checkbox.instantiate()
                    .viewport(s.webSurface)
                    .metrics(new UIElement.Metrics().scale(layoutCheckbox.size).move(layoutCheckbox.x, surfaceY + layoutCheckbox.y))
                    .visuals(mat)
                    .inputPadding(layoutCheckbox.inputPaddingLeft, layoutCheckbox.inputPaddingTop, layoutCheckbox.inputPaddingRight, layoutCheckbox.inputPaddingBottom)
                    .windowAnimation(s.checkboxUntickedAnim.startAndReset(), true, true)
                    .attach();
            checkbox.windowAnim.setProgress(1f);        // dont need animation to begin with
            // Recognize intent
            checkboxes.add(checkbox);
        }

        // Trigger
        if(layout.trigger != null && !layout.trigger.isEmpty())
            Globals.grid.eval(TAG, layout.trigger);

        // Queue loading
        tNextLoadScheduled = getRenderTime() + (s.tLoadingInterval.generate() * page.speed);       // Queue loading

        // Move up
        s.webSurface.move(0, -1000);

        // Allow idle scare
        Globals.grid.idleScare.reschedule();
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
        s.bars.attach(this);

        if(configFilename != null) {
            load(configFilename, Globals.grid.state);
            showHomeView();
        }
    }


    public BrowserScreen() {

        // Initialize
        builder = new Builder<Object>(GBBrowserHomeScreen.class, this);
        builder.build();

        // Load
        load(Globals.browserConfigFilename, Globals.grid.state);
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Show correct layout on show
        if(page == null)
            showHomeView();     // Else default to home

        // Reconfigure timers
        if(tNextLoadScheduled != Float.MAX_VALUE)
            tNextLoadScheduled = 0;
        if(tOpenScheduled != Float.MAX_VALUE)
            tOpenScheduled = 0;
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        if(closeTabOnRelease) {
            clearPageTab();
            showHomeView();
            closeTabOnRelease = false;
        }

        fromScreen = null;      // If a web page opens another screen, just forget about view order.
    }


    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(layout != null) {
            // Load all images
            for(int c = 0; c < layout.layoutMeshes.length; c++)
                layout.layoutMeshes[c].load();
//            for(int c = 0; c < layout.contentSprites.length; c++)     // 20180922: Some really big websites use alot of memory, better for selective loading?
//                layout.contentSprites[c].sprite.load();
        }

        // Queue opening
        if(renderTime > tOpenScheduled) {
            showPage(openingPage);
        }

        if(renderTime > tNextLoadScheduled) {
            // Get allotted bandwidth
            int imageBandwidthRemaining = s.maxImageBandwidth;
            int textBandwidthRemaining = s.maxTextBandwidth;

            // Process images
            while(imageBandwidthRemaining > 0 && imageTracker.size > 0) {
                ImageLoadingTracker tracker = imageTracker.random();
                int remaining = tracker.totalBytes - tracker.downloadedBytes;
                int bandwidth = s.imageBandwidth.generateInt();
                if(bandwidth > imageBandwidthRemaining)
                    bandwidth = imageBandwidthRemaining;
                if(bandwidth > remaining)
                    bandwidth = remaining;
                imageBandwidthRemaining -= bandwidth;
                tracker.downloadedBytes += bandwidth;
                if(tracker.downloadedBytes == tracker.totalBytes) {
                    // Finished loading this image
                    if(!tracker.view.isAttached())
                        tracker.view.attach();
                    tracker.pixelator.pixelation(-1);           // loaded full
                    imageTracker.removeValue(tracker, true);
                    // Remember as downloaded
                    tempCachedObjects.add(tracker.view.visual().getMaterial());
                    tempCachedDownloaded.add(-1);       // done
                    continue;
                }
                // Else there are still more remaining bytes
                // Update pixelator
                tracker.updateViewPixelation();
            }

            // Process texts
            while(textBandwidthRemaining > 0 && textTracker.size > 0) {
                TextLoadingTracker tracker = textTracker.random();
                int remaining = tracker.totalBytes - tracker.downloadedBytes;
                int bandwidth = s.textBandwidth.generateInt();
                if(bandwidth > textBandwidthRemaining)
                    bandwidth = textBandwidthRemaining;
                if(bandwidth > remaining)
                    bandwidth = remaining;
                textBandwidthRemaining -= bandwidth;
                tracker.downloadedBytes += bandwidth;
                if(tracker.downloadedBytes == tracker.totalBytes) {
                    // Finished loading this text
                    tracker.textBox.attach();
                    textTracker.removeValue(tracker, true);
                    // Remember as downloaded
                    tempCachedObjects.add(tracker.textBox.text().text);
                    tempCachedDownloaded.add(-1);       // done
                    continue;
                }
                // Else still loading
            }

            // Calculate remaining items
            int trackedRemaining = imageTracker.size + textTracker.size;
            if(trackedRemaining == 0) {
                // Done loading
                s.webLoadingBar.seek(1f, s.tLoadingSeekSpeed).detachWithAnim();
                tNextLoadScheduled = Float.MAX_VALUE;
            }
            else {
                float progress = 1.0f - ((float)trackedRemaining / (float)totalTracked);
                progress = s.connectingProgress + ((1.0f - s.connectingProgress) * progress);
                s.webLoadingBar.seek(progress, s.tLoadingSeekSpeed);
                tNextLoadScheduled = renderTime + (s.tLoadingInterval.generate() * page.speed);
            }
        }

    }

    @Override
    public Entity<?> open() {
        showHomeView();         // Always show homeview on open (to highlight most visited for gameplay purposes)
        return this;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int b) {
        if (view == s.bars.homeButton()) {
            // Stop idle scare
            v.idleScare.stop();

            if(!Globals.grid.trigger(Globals.TRIGGER_LEAVING_BROWSER_SCREEN))
                return;     // not allowed to leave

            fromScreen = null;
            v.homescreen.transitionBack(this, v);
            return;
        }

        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        if (view == s.bars.backButton()) {
            // Stop idle scare
            v.idleScare.stop();
            // If on home, back to homescreen
            if(isShowingHome) {
                if(fromScreen != null)
                    showWebView();      // Came from another screen, show back the website
                else
                    v.homescreen.transitionBack(this, v);
            }
            else {
                // Move back one stack
                if(pageStack.size > 1) {
                    pageStack.pop();
                    PageDescriptor page = pageStack.pop();
                    showPage(page);
                }
                else {
                    if(fromScreen != null)
                        close(v.screensGroup);
                    else {
                        clearPageTab();
                        showHomeView();
                    }
                }
                // Sound
                s.backSound.play();
            }
            return;
        }



        // Home button from web view
        if(view == s.webNavHomeButton) {
            showHomeView();
            return;
        }

        // Url on home view
        if(view == s.homeNavUrlEmptyButton) {
            // Clicked on empty url, show dropdown
            showDropView();
            return;
        }
        if(view == s.homeNavUrlButton) {
            // Page is already showing, just show
            showWebView();
            return;
        }

        // Url on web view
        if(view == s.webNavUrlButton) {
            showDropView();
            return;
        }

        // Url on drop view
        if(view == s.dropNavUrlButton) {
            // Hide
            if(!s.dropNavUrlView.isCursorAnimated())
                showDropView();
            else {
                s.dropNavUrlView.animateCursor(true);
                v.keyboard.hide();
            }
            return;
        }

        // Home button from drop view
        if(view == s.dropNavHomeButton) {
            showHomeView();
            return;
        }

        // Close button from drop view
        if(view == s.dropNavCloseButton) {
            hideDropView();
            return;
        }

        // Close button from web view
        if(view == s.webNavCloseButton) {
            if(fromScreen != null)
                close(v.screensGroup);
            else {
                clearPageTab();
                showHomeView();
            }
        }

        // Most visited
        for(int c = 0; c < s.homeMostVisitedButtons.length; c++) {
            if(view == s.homeMostVisitedButtons[c]) {
                // Visit this page
                showPage(mostVisited.items[c]);
                return;
            }
        }

        if(view instanceof Clickable) {
            Clickable button = (Clickable) view;

            // Checkboxes
            int index = checkboxes.indexOf(button, true);
            if(index != -1) {
                // Cancel input field
                cancelActiveField();
                LayoutDescriptor.LayoutCheckbox currentCheckbox = layout.checkboxes[index];
                // Activate this checkbox
                if(button.windowAnim.anim == s.checkboxTickedAnim) {
                    // Was ticked, just untick it
                    button.windowAnimation(s.checkboxUntickedAnim.startAndReset(), true, true);
                }
                else {
                    // Was unticked, tick it and untick all other checkboxes of the same group
                    button.windowAnimation(s.checkboxTickedAnim.startAndReset(), true, true);
                    // Untick the rest
                    for(int c = 0; c < layout.checkboxes.length; c++) {
                        if(c == index)
                            continue;
                        LayoutDescriptor.LayoutCheckbox layoutCheckbox = layout.checkboxes[c];
                        if(layoutCheckbox.groupName.equals(currentCheckbox.groupName) && checkboxes.items[c].windowAnim.anim == s.checkboxTickedAnim) {
                            checkboxes.items[c].windowAnimation(s.checkboxUntickedAnim.startAndReset(), true, true);
                        }
                    }
                }
                // Action
                executeAction(currentCheckbox.action);
                return;
            }

            // Home bookmarks
            PageDescriptor link = homeBookmarkRows.get(button);
            if(link != null) {
                showPage(link);
                return;
            }


            // Check links
            String action = links.get(button);
            if(action != null) {
                // Execute this action
                executeAction(action);
                return;
            }

            // Dropdown links
            link = dropLinks.get(button);
            if(link != null) {
                if(isShowingHome)
                    showPage(link);
                else
                    openPage(link);
                hideDropView();
                return;
            }
        }

        if(view instanceof InputField) {
            InputField inputField = (InputField) view;
            int index = inputs.indexOf(inputField, true);
            if(index != -1) {
                // Selecting this input field
                activateField(index);
                return;
            }
        }
    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        // Special commands
        if(Globals.allowWebsiteRefresh && inputType == INPUT_KEY_UP && key == Input.Keys.F5) {
            // Special command to refresh current page
            LiveEditor.editor.clearOnlineCache();       // TODO: hack to clear cache (but clears ALL cache)
            reloadCurrentPage();
            return true;
        }

        return super.input(v, inputType, key, character, scrolledAmount, pointer, x, y, button);
    }

    private void executeAction(String action) {
        if(action == null || action.isEmpty())
            return;     // UB

        // Stop idle scare
        Globals.grid.idleScare.stop();

        if(action.startsWith(WEB_PREFIX)) {
            String url = action.substring(WEB_PREFIX.length());
            if(!pages.containsKey(url))
                Sys.error(TAG, "Page not found \"" + url + "\"");
            else
                openPage(url);
        }
        else if(action.startsWith(TRIGGER_PREFIX)) {
            String trigger = action.substring(TRIGGER_PREFIX.length());
            try {
                Globals.grid.eval(TAG, trigger);
            } catch (Throwable e) {
                Sys.error(TAG, "Exception on trigger \"" + trigger + "\"", e);
            }
        }
        else
            Sys.error(TAG, "Unrecognized action \"" + action + "\"");
    }


    private void showHomeView() {
        // Cancel page that was going to be shown
        cancelPageOpening();

        prepareMostVisitedMenu();
        prepareHomeBookmarks();

        s.webGroup.detach();
        s.dropGroup.detach();
        s.homeGroup.attach();
        s.homeNavView.attach();
        isShowingHome = true;

        // Scroll to top
        s.homeSurface.move(0, -1000);

        // Update nav bar
        if(page != null) {
            s.homeNavUrlEmptyButton.detach();
            s.homeNavUrlButton.attach().text(page.url);
            s.homeNavFaviconView.visual(page.favicon);
        }
        else {
            s.homeNavUrlButton.detach();
            s.homeNavUrlEmptyButton.attach();
        }
    }

    private void showWebView() {
        s.homeGroup.detach();
        s.dropGroup.detach();
        s.webGroup.attach();
        s.webNavView.attach();
        isShowingHome = false;

        // Analytics
        Game.analyticsView(page.url, Globals.ANALYTICS_CONTENT_TYPE_BROWSER);
    }

    private void showDropView() {
        s.webNavView.detach();
        s.homeNavView.detach();
        s.dropGroup.attach();
        s.dropSurface.move(0, -1000);           // default up

        prepareDropMenu();

        // Update nav bar
        if(page != null) {
            s.dropNavUrlView.text(page.url);
            s.dropNavFaviconView.visual(page.favicon);
        }
        else {
            s.dropNavUrlView.text("");
            s.dropNavFaviconView.visual(s.dropDefaultFavicon);
        }

        s.dropNavUrlView.animateCursor(true);

        // Show keyboard
        cancelActiveField();
        Keyboard keyboard = Globals.grid.keyboard;
        keyboard.resetAutoComplete();
        keyboard.showKeyboard(this, viewport, 0, false, false, false, false, this, s.dropNavUrlView.text(), "go");
    }

    private void hideDropView() {
        s.dropGroup.detach();
        if(isShowingHome)
            s.homeNavView.attach();
        else
            s.webNavView.attach();
    }



    @Override
    public void keyboardTyped(String text, int matchOffset) {
        if(s.dropGroup.isAttached()) {
            s.dropNavUrlView.text(text);
            return;
        }
        if(activeField == null)
            return;         // ignore

        activeField.text(text);
    }

    @Override
    public void keyboardClosed() {
        if(s.dropGroup.isAttached()) {
            s.dropNavUrlView.animateCursor(false);
            return;
        }
        // Cancel active field
        cancelActiveField();
    }

    @Override
    public void keyboardPressedConfirm() {
        if(s.dropGroup.isAttached()) {
            // Go to page
            PageDescriptor page = pages.get(s.dropNavUrlView.text());
            if(page == null)
                page = getPage(Globals.BROWSER_NOT_FOUND_PAGE);       // never existed
            else if(!page.isPublic && !(mostVisited.contains(page, true) || bookmarks.contains(page, true) || history.contains(page, true)))
                page = getPage(Globals.BROWSER_NOT_FOUND_PAGE);
            if(isShowingHome)
                showPage(page);
            else
                openPage(page);
            hideDropView();
            Globals.grid.keyboard.hide();
            return;
        }
        if(activeField == null)
            return;         // UB

        // Execute action
        LayoutDescriptor.LayoutInput input = layout.inputs[activeFieldIndex];
        String action = input.confirmAction;
        if(action == null || action.isEmpty()) {
            // No action, try selecting next field
            if(activeFieldIndex < (layout.inputs.length - 1)) {
                activateField(activeFieldIndex + 1);
                return;
            }
            else
                Sys.error(TAG, "No action specified for field \"" + layout + "\" in \"" + page.url + "\"");
        }
        else
            executeAction(action);

        // Close keyboard
        Globals.grid.keyboard.hide();

        // Close selection
        cancelActiveField();
    }
}
