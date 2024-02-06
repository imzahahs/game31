package game31.app.gallery;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

import java.util.Locale;

import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.MediaAlbum;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.gallery.GBPhotoRollGridScreen;
import sengine.animation.Animation;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

import static game31.Globals.grid;

/**
 * Created by Azmi on 21/7/2016.
 */
public class PhotoRollGridScreen extends Menu<Grid> implements OnClick<Grid> {

    public interface BuilderSource {
        String summarize(int photos, int videos, int audio);
        Animation animateThumb(int index);
    }

    public static class Internal {
        // Window
        public UIElement<?> window;
        public ScrollableSurface surface;
        public ScreenBar bars;
        public TextBox summaryView;

        // Downloads view
        public TextBox nameView;
        public StaticSprite unopenedView;

        // Photo group
        public Clickable photoView;
        public float photoIntervalX;
        public float photoIntervalY;
        public float photoIntervalWithNameY;
        public int photosPerRow;

        // Date row
        public PatchedTextBox dateView;
        public float dateIntervalY;

        // Video group
        public Clickable videoView;
        public TextBox videoDurationView;

        // Audio group
        public Clickable audioView;
        public TextBox audioDurationView;

        // Corruption
        public StaticSprite corruptionView;
    }

    // App
    private final PhotoRollApp app;

    // Interface source
    private final Builder<BuilderSource> interfaceSource;
    private Internal s;

    // Current
    private final Array<Media> medias = new Array<Media>(Media.class);
    private final Array<Clickable> mediaViews = new Array<Clickable>(Clickable.class);
    private float surfaceY;
    private float nextSurfaceY;
    private MediaAlbum album = null;
    private boolean showAsDownloads = false;
    private boolean backToAlbumScreen = false;
    private PhotoRollAlbumsScreen.SelectCallback selectCallback = null;
    private String currentDateText = null;
    private int currentDateIndex = 0;
    private Clickable trackedCorruptionButton = null;


    // Interface sources
    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        clear();
    }


    public void clear() {
        nextSurfaceY = surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        medias.clear();
        mediaViews.clear();
        s.surface.detachChilds();
        album = null;
        showAsDownloads = false;
        currentDateText = "";
        currentDateIndex = 0;
        trackedCorruptionButton = null;
    }

    // Controls
    private void addMedia(Media media) {
        // Update date
        if(media.dateText != null && !media.dateText.isEmpty() && !currentDateText.equals(media.dateText)) {
            currentDateText = media.dateText;

            surfaceY = nextSurfaceY;
            currentDateIndex = 0;

            PatchedTextBox view = s.dateView.instantiate().viewport(s.surface).attach();
            view.text(currentDateText);
            view.metrics.anchorWindowY = (surfaceY / s.surface.getLength());

            nextSurfaceY = surfaceY += (-view.getLength() * view.metrics.scaleY) + s.dateIntervalY;     // Next row
        }
        // Else invalid date text or is the same

        Clickable view;
        if(media.isVideo()) {
            view = s.videoView.instantiate().viewport(s.surface);
            int seconds = (int)media.video.duration;
            int minutes = seconds / 60;
            seconds %= 60;
            view.find(s.videoDurationView).text().text(String.format(Locale.US, "%d:%02d", minutes, seconds));
        }
        else if(media.isAudio()) {
            view = s.audioView.instantiate().viewport(s.surface);
            int seconds = (int)media.audioInfo.duration;
            int minutes = seconds / 60;
            seconds %= 60;
            view.find(s.audioDurationView).text().text(String.format(Locale.US, "%d:%02d", minutes, seconds));
        }
        else
            view = s.photoView.instantiate().viewport(s.surface);

        int index = currentDateIndex;
        int column = index % s.photosPerRow;

        view.metrics.anchorWindowX += s.photoIntervalX * column;
        view.metrics.anchorWindowY = (surfaceY / s.surface.getLength());

        Animation appearAnim = interfaceSource.build().animateThumb(medias.size);
        view.windowAnimation(appearAnim.startAndReset(), true, false);

        // Set image
        if(media.thumbnailSquare != null)
            view.visuals(media.thumbnailSquare, view.target());

        // Show as downloads
        if(showAsDownloads) {
            s.nameView.instantiate()
                    .viewport(view)
                    .text(media.name)
                    .attach();
            // Show unopened if needed
            if(!media.wasOpened())
                s.unopenedView.instantiate().viewport(view).attach();
        }

        view.attach();

        // Corruption
        if(media.corruption != null) {
            boolean isSolved = Globals.grid.state.get(media.corruption, false);

            if(!isSolved)
                s.corruptionView.instantiate().viewport(view).attach();
        }


        currentDateIndex++;

        medias.add(media);
        mediaViews.add(view);
        nextSurfaceY = surfaceY + (-view.getLength() * view.metrics.scaleY) + (showAsDownloads ? s.photoIntervalWithNameY : s.photoIntervalY);     // Next row
        if((currentDateIndex % s.photosPerRow) == 0)
            surfaceY = nextSurfaceY;

    }

    public MediaAlbum getCurrentAlbum() {
        return album;
    }

    public void close() {
        Grid v = grid;

        ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(this, app.albumsScreen, v.screensGroup);
        transition.attach(v);
    }

    public void show(MediaAlbum album, boolean backToAlbumScreen, PhotoRollAlbumsScreen.SelectCallback selectCallback) {
        clear();

        // Set album and add all medias
        this.album = album;
        this.showAsDownloads = album.name.equals(Globals.GALLERY_DOWNLOADS_ALBUM);
        this.backToAlbumScreen = backToAlbumScreen;
        this.selectCallback = selectCallback;


        if(isAttached() && showAsDownloads)
            Globals.grid.notification.clearFinishedDownload();

        // Title
        s.bars.showAppbar(album.name, null);

        int numPhotos = 0;
        int numVideos = 0;
        int numRecordings = 0;

        for(int c = 0; c < album.medias.size; c++) {
            Media media = album.medias.items[c];
            if(media.isVideo())
                numVideos++;
            else if(media.isAudio())
                numRecordings++;
            else
                numPhotos++;
            addMedia(media);
        }
        // Add bottom summary
        surfaceY = nextSurfaceY;            // Go to new row
        s.summaryView.metrics.anchorWindowY = (surfaceY / s.surface.getLength());
        s.summaryView.text().text(interfaceSource.build().summarize(numPhotos, numVideos, numRecordings));
        s.summaryView.viewport(s.surface).attach();
        // appbar.title(album.name, null);
        scrollToTop();
    }



    public void scrollToTop() {
        s.surface.move(0, -1000);
    }

    public PhotoRollGridScreen(PhotoRollApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<BuilderSource>(GBPhotoRollGridScreen.class, this);
        interfaceSource.build();
    }


    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        // Refresh all corruption views
        boolean isTrackedCorruptionButtonSolved = false;
        for(int c = 0; c < mediaViews.size; c++) {
            Clickable button = mediaViews.items[c];
            StaticSprite corruptionView = button.find(s.corruptionView);
            if(corruptionView != null) {
                boolean isSolved = Globals.grid.state.get(medias.items[c].corruption, false);
                if(isSolved) {
                    if(trackedCorruptionButton == button)
                        isTrackedCorruptionButtonSolved = true;
                    corruptionView.detachWithAnim();            // was solved, detach
                }
            }
        }
        if(!isTrackedCorruptionButtonSolved)
            trackedCorruptionButton = null;     // don't open this as not yet solved

        if(showAsDownloads)
            Globals.grid.notification.clearFinishedDownload();

        // Allow idle scares
        grid.idleScare.reschedule();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        super.render(v, r, renderTime);

        if(renderTime > 0 && trackedCorruptionButton != null) {
            // tracked button was solved, simulate pressed
            onClick(v, trackedCorruptionButton, Input.Buttons.LEFT);
            trackedCorruptionButton = null;
        }
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.bars.backButton()) {
            // Stop idle scares
            v.idleScare.stop();

            if(backToAlbumScreen)
                close();
            else
                v.homescreen.transitionBack(this, v);

            album = null;
            return;
        }

        if(view == s.bars.homeButton()) {
            // Stop idle scares
            v.idleScare.stop();

            v.homescreen.transitionBack(this, v);

            album = null;
            return;
        }

        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        // Media views
        for(int c = 0; c < mediaViews.size; c++) {
            if(view == mediaViews.items[c]) {
                if(!v.trigger(Globals.TRIGGER_PHOTOROLL_OPEN_MEDIA))
                    return;     // not allowed

                // Stop idle scares
                v.idleScare.stop();

                // If its a photo, open in photoroll, else open in video player
                Media m = medias.items[c];

                // First check if its corrupted
                if(m.corruption != null) {
                    boolean isSolved = Globals.grid.state.get(m.corruption, false);
                    if(!isSolved) {
                        // Not yet solved, corruption view
                        v.restoreImageApp.show(m.corruption);
                        v.restoreImageApp.open(this);
                        trackedCorruptionButton = (Clickable) view;
                        return;
                    }
                }

                if(m.isVideo() || m.isAudio()) {
                    // Video or audio
                    app.videoScreen.show(album, c, selectCallback, true);
                    app.videoScreen.open(this, v.screensGroup, view.getX(), view.getY(), view.getWidth());
                }
                else {
                    // Clicked on a photo, open
                    app.photoScreen.show(album, c, selectCallback);
                    app.photoScreen.open(this, v.screensGroup, view.getX(), view.getY(), view.getWidth());
                }

                if(showAsDownloads && m.wasOpened()) {
                    StaticSprite unopenedView = view.find(s.unopenedView);
                    if(unopenedView != null)
                        unopenedView.detachWithAnim();
                }
                return;
            }
        }
    }
}
