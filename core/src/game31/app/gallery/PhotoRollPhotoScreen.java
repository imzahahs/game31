package game31.app.gallery;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.MediaAlbum;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.gallery.GBPhotoRollPhotoScreen;
import sengine.Entity;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.graphics2d.Mesh;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 24/7/2016.
 */
public class PhotoRollPhotoScreen extends Menu<Grid> implements OnClick<Grid> {

    public interface InterfaceSource {
        Mesh createCorruptionMesh(float length);
    }

    public static class Internal {
        // Window
        public UIElement<?> window;
        public ScrollableSurface surface;
        public float maxZoom;
        public float flipDistance;
        public float gravityThreshold;

        public ScreenBar bars;

        public StaticSprite bgView;

        public Clickable leftButton;
        public Clickable rightButton;
        public Animation controlDisabledAnim;

        public Audio.Sound nextSound;
        public Audio.Sound previousSound;

        public Clickable sendButton;

        public Animation topGroupFullscreenAnim;
        public Animation topGroupWindowedAnim;
        public Animation bottomGroupFullscreenAnim;
        public Animation bottomGroupWindowedAnim;
        public Animation captionFullscreenAnim;
        public Animation captionWindowedAnim;
        public Animation bgFullscreenAnim;
        public Animation bgWindowedAnim;
        public float fullscreenZoomThreshold;

        // Slideshow controls
        public TextBox captionView;

        // Photo view
        public StaticSprite photoView;
        public StaticSprite leftPhotoView;
        public StaticSprite rightPhotoView;

        // Corruption view
        public StaticSprite corruptionView;
        public Clickable restoreButton;
    }


    // App
    private final PhotoRollApp app;

    // Interface source
    private final Builder<InterfaceSource> interfaceSource;
    private Internal s;


    // Current
    private Media photo = null;
    private MediaAlbum album = null;
    private int albumIndex = -1;
    private PhotoRollAlbumsScreen.SelectCallback selectCallback = null;

    private int leftAlbumIndex = -1;
    private Media leftPhoto = null;
    private int rightAlbumIndex = -1;
    private Media rightPhoto = null;
    private float tLastSurfaceClick = -1;

    private String contextName;
    private boolean isFirstFrame = true;
    private boolean isFullscreen = false;
    private boolean isCorrupted = false;
    private boolean isRestoring = false;

    // Transition
    private Entity<?> transitionFrom;
    private float transitionX;
    private float transitionY;
    private float transitionSize;



    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void show(MediaAlbum album, int index, PhotoRollAlbumsScreen.SelectCallback selectCallback) {
        show(album, index, selectCallback, true, null, null);
    }

    public void show(MediaAlbum album, int index, PhotoRollAlbumsScreen.SelectCallback selectCallback, boolean allowNavigation, String title, String caption) {
        // Clear
        clear();

        // Remember album
        this.album = album;
        albumIndex = index;
        photo = album.medias.items[index];

        this.selectCallback = selectCallback;

        // Update view
        s.photoView.visual(photo.full.isLoaded() ? photo.full : photo.thumbnail);

        // Zoom out back
        s.surface.zoom(1f);
        s.surface.stop();
        s.surface.move(-s.surface.movedX(), -s.surface.movedY());
        isFirstFrame = true;

        // Caption
        if(caption == null)
            caption = photo.caption;
        if(caption != null && !caption.isEmpty())
            s.captionView.text(caption).windowAnimation(null, false, false).attach();
        else
            s.captionView.detach();

        // Appbar
        if(title == null)
            title = photo.dateText;
//            title = String.format(Locale.US, "%,d of %,d", album.countPhotoIndex(albumIndex) + 1, album.countPhotos());
        s.bars.showAppbar(title, null, 0, 0, 0, 0);

        if(allowNavigation) {
            // Check if can have left and right photos
            leftAlbumIndex = album.findPrevPhoto(albumIndex);
            if(leftAlbumIndex != -1) {
                leftPhoto = album.medias.items[leftAlbumIndex];
                s.leftPhotoView.visual(leftPhoto.full.isLoaded() ? leftPhoto.full : leftPhoto.thumbnail);
                s.leftPhotoView.attach();
                s.leftButton.attach().windowAnimation(null, false, false);
                // Check corruption
                if(leftPhoto.corruption != null && !Globals.grid.state.get(leftPhoto.corruption, false)) {
                    s.corruptionView.instantiate()
                            .viewport(s.leftPhotoView)
                            .visual(interfaceSource.build().createCorruptionMesh(leftPhoto.full.length))
                            .attach();
                }
            }
            else {
                s.leftPhotoView.detach();
                s.leftButton.attach().windowAnimation(s.controlDisabledAnim.startAndReset(), true, true);
            }

            rightAlbumIndex = album.findNextPhoto(albumIndex);
            if(rightAlbumIndex != -1) {
                rightPhoto = album.medias.items[rightAlbumIndex];
                s.rightPhotoView.visual(rightPhoto.full.isLoaded() ? rightPhoto.full : rightPhoto.thumbnail);
                s.rightPhotoView.attach();
                s.rightButton.attach().windowAnimation(null, false, false);
                // Check corruption
                if(rightPhoto.corruption != null && !Globals.grid.state.get(rightPhoto.corruption, false)) {
                    s.corruptionView.instantiate()
                            .viewport(s.rightPhotoView)
                            .visual(interfaceSource.build().createCorruptionMesh(rightPhoto.full.length))
                            .attach();
                }
            }
            else {
                s.rightPhotoView.detach();
                s.rightButton.attach().windowAnimation(s.controlDisabledAnim.startAndReset(), true, true);
            }

            if(leftAlbumIndex != -1 || rightAlbumIndex != -1)
                s.surface.scrollGravity(s.gravityThreshold, 0, 1f, 0);
            else
                s.surface.scrollGravity(0, 0, 0, 0);
        }
        else {
            s.leftPhotoView.detach();
            s.rightPhotoView.detach();
            s.leftButton.detach();
            s.rightButton.detach();
            s.surface.scrollGravity(0, 0, 0, 0);
        }

        // Corruption
        isCorrupted = false;
        if(photo.corruption != null) {
            // Check corruption
            boolean isSolved = Globals.grid.state.get(photo.corruption, false);

            if(!isSolved) {
                // Still corrupted
                isCorrupted = true;

                // Apply corruption view
                s.corruptionView.instantiate()
                        .viewport(s.photoView)
                        .visual(interfaceSource.build().createCorruptionMesh(photo.full.length))
                        .attach();
            }
        }

        if(!isCorrupted) {
            // Mark as opened
            photo.markOpened();
            // Trigger
            if(photo.trigger != null && !photo.trigger.isEmpty())
                Globals.grid.eval(photo.name, photo.trigger);
        }

        refreshActionButton();
    }

    private void refreshActionButton() {
        if(!isCorrupted) {
            s.surface.maxZoom(s.maxZoom);
            s.restoreButton.detachWithAnim();

            // Is selecting ?
            if (selectCallback != null)
                s.sendButton.attach();
            else
                s.sendButton.detachWithAnim();
        }
        else {
            isFullscreen = false;
            s.surface.maxZoom(1f);
            s.restoreButton.attach();
            s.sendButton.detachWithAnim();          // cannot send corrupted files
        }
    }

    public void clear() {
        contextName = null;
        albumIndex = -1;
        photo = null;
        leftAlbumIndex = -1;
        leftPhoto = null;
        rightAlbumIndex = -1;
        rightPhoto = null;
        // Clear all corruption views
        s.photoView.detachChilds();
        s.leftPhotoView.detachChilds();
        s.rightPhotoView.detachChilds();
        isRestoring = false;
    }

    public void open(Entity<?> transitionFrom, Entity<?> target, float x, float y, float size) {
        this.transitionFrom = transitionFrom;
        transitionX = x;
        transitionY = y;
        transitionSize = size;
        ScreenTransition transition = ScreenTransitionFactory.createHomescreenOutTransition(
                transitionFrom, this, target,
                x, y, size
        );
        transition.attach(target);
    }

    public void close() {
        // Stop idle scares
        Globals.grid.idleScare.stop();

        ScreenTransition transition = ScreenTransitionFactory.createHomescreenInTransition(
                this, transitionFrom, getEntityParent(),
                transitionX,
                transitionY,
                transitionSize
        );
        transition.attach(getEntityParent());
        contextName = null;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

    }


    private void showFullscreen() {
        if (s.bars.appbar().windowAnim == null || s.bars.appbar().windowAnim.anim != s.topGroupFullscreenAnim)
            s.bars.appbar().windowAnimation(s.topGroupFullscreenAnim.startAndReset(), true, true);
        if (s.bars.navbar().windowAnim == null || s.bars.navbar().windowAnim.anim != s.bottomGroupFullscreenAnim)
            s.bars.navbar().windowAnimation(s.bottomGroupFullscreenAnim.startAndReset(), true, true);
        if(s.captionView.isAttached() && (s.captionView.windowAnim == null || s.captionView.windowAnim.anim != s.captionFullscreenAnim))
            s.captionView.windowAnimation(s.captionFullscreenAnim.startAndReset(), true, true);
        if(s.bgView.windowAnim== null || s.bgView.windowAnim.anim != s.bgFullscreenAnim)
            s.bgView.windowAnimation(s.bgFullscreenAnim.startAndReset(), true, true);
    }

    private void showWindowed() {
        if(s.bars.appbar().windowAnim != null && s.bars.appbar().windowAnim.anim != s.topGroupWindowedAnim)
            s.bars.appbar().windowAnimation(s.topGroupWindowedAnim.startAndReset(), true, false);
        if(s.bars.navbar().windowAnim != null && s.bars.navbar().windowAnim.anim != s.bottomGroupWindowedAnim)
            s.bars.navbar().windowAnimation(s.bottomGroupWindowedAnim.startAndReset(), true, false);
        if(s.captionView.isAttached() && s.captionView.windowAnim != null && s.captionView.windowAnim.anim != s.captionWindowedAnim)
            s.captionView.windowAnimation(s.captionWindowedAnim.startAndReset(), true, false);
        if(s.bgView.windowAnim != null && s.bgView.windowAnim.anim != s.bgWindowedAnim)
            s.bgView.windowAnimation(s.bgWindowedAnim.startAndReset(), true, false);
    }

    public PhotoRollPhotoScreen(PhotoRollApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<InterfaceSource>(GBPhotoRollPhotoScreen.class, this);
        interfaceSource.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        // Start in windowed
        isFullscreen = false;
        s.bars.appbar().windowAnimation(null, false, false);
        s.bars.navbar().windowAnimation(null, false, false);
        s.captionView.windowAnimation(null, false, false);
        s.bgView.windowAnimation(null, false, false);

        // Check corruption
        if(isRestoring) {
            isRestoring = false;
            // Check for corruption again
            boolean isSolved = Globals.grid.state.get(photo.corruption, false);
            if(isSolved) {
                isCorrupted = false;
                // Clear corruption
                s.photoView.detachChilds();
                // Update restore & select buttons
                refreshActionButton();
            }
        }

        // Allow idle scares
        grid.idleScare.reschedule();

        // Analytics
        Game.analyticsView(photo.name, Globals.ANALYTICS_CONTENT_TYPE_GALLERY);
    }


    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(!isFirstFrame) {
            // Is not the first frame (after transition frame)
            photo.full.load();
            if(s.photoView.visual() == photo.thumbnail) {
                // Load main photo
                if (photo.full.isLoaded())
                    s.photoView.visual(photo.full);
            }
            else {
                // Main photo is done loading, load left and right photos
                if(leftPhoto != null) {
                    leftPhoto.full.load();
                    if (s.leftPhotoView.visual() == leftPhoto.thumbnail && leftPhoto.full.isLoaded())
                        s.leftPhotoView.visual(leftPhoto.full);
                }
                if(rightPhoto != null) {
                    rightPhoto.full.load();
                    if (s.rightPhotoView.visual() == rightPhoto.thumbnail && rightPhoto.full.isLoaded())
                        s.rightPhotoView.visual(rightPhoto.full);
                }
            }
        }
        isFirstFrame = false;

        if(isFullscreen)
            showFullscreen();
        if(s.surface.zoom() > s.fullscreenZoomThreshold) {
            // Hide when zoomed in
            if(!isFullscreen)
                showFullscreen();
            if(s.leftPhotoView.isAttached() || s.rightPhotoView.isAttached()) {
                s.leftPhotoView.detach();
                s.rightPhotoView.detach();
                s.surface.scrollGravity(0, 0, 0, 0);
                s.surface.move(-s.surface.movedX(), -s.surface.movedY());
            }
        }
        else {
            // Else show
            if(!isFullscreen)
                showWindowed();
            if((leftPhoto != null || rightPhoto != null) && !s.leftPhotoView.isAttached() && !s.rightPhotoView.isAttached()) {
                if(leftPhoto != null)
                    s.leftPhotoView.attach();
                if(rightPhoto != null)
                    s.rightPhotoView.attach();
                s.surface.scrollGravity(s.gravityThreshold, 0, 1f, 0);
                s.surface.move(-s.surface.movedX(), -s.surface.movedY());
            }

            // Check if moved to a new position
            if(!s.surface.isTouching() && !s.surface.isSmoothMoving()) {
                if(rightAlbumIndex != -1 && s.surface.movedX() <= -s.flipDistance) {
                    show(album, rightAlbumIndex, selectCallback);
                }
                if(leftAlbumIndex != -1 && s.surface.movedX() >= +s.flipDistance) {
                    show(album, leftAlbumIndex, selectCallback);
                }
            }
        }
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();
    }

    @Override
    public void onClick(Grid v, UIElement<?> button, int b) {
        if(button == s.bars.backButton()) {
            close();
            return;
        }

        if(button == s.bars.homeButton()) {
            // Stop idle scares
            v.idleScare.stop();

            v.homescreen.transitionBack(this, v);
            return;
        }

        if(button == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        if(button == s.surface && !isCorrupted) {
            float elapsed = getRenderTime() - tLastSurfaceClick;
            if(elapsed < Globals.doubleClickTime) {
                if (s.surface.zoom() > 1f) {
                    s.surface.smoothZoomAtPointer(0, 0.1f, 0.5f);           // max zoom, zoom out all the way
                    isFullscreen = false;
                }
                else
                    s.surface.smoothZoomAtPointer(0, 2f, 0.25f);         // zooming in
                tLastSurfaceClick = -1;         // Reset
            }
            else {
                tLastSurfaceClick = getRenderTime();
                // Toggle fullscreen if zoomed out
                if(s.surface.zoom() <= s.fullscreenZoomThreshold)
                    isFullscreen = !isFullscreen;
            }
            return;
        }

        if(button == s.sendButton) {
            // Stop idle scares
            v.idleScare.stop();

            // Build name for this media and submit to keyboard
            String name = Globals.PHOTOROLL_PREFIX + photo.album + "/" + photo.name;
            selectCallback.onSelectedMedia(name);
            // Close
            selectCallback.onReturnFromAttachment(this);
            return;
        }

        if(button == s.restoreButton) {
            // Stop idle scares
            v.idleScare.stop();

            // Restore
            isRestoring = true;
            v.restoreImageApp.show(photo.corruption);
            v.restoreImageApp.open(this);
            return;
        }

        if(button == s.leftButton) {
            if(!v.trigger(Globals.TRIGGER_PHOTOROLL_VIEWER_NAVIGATE))
                return;     // not allowed
            if(leftPhoto != null && !s.surface.isSmoothMoving()) {
                s.surface.stop();
                s.surface.move(-s.surface.movedX(), -s.surface.movedY());
                s.surface.seekGravityTarget(+1, 0);
                s.previousSound.play();
            }
            return;
        }
        if(button == s.rightButton) {
            if(!v.trigger(Globals.TRIGGER_PHOTOROLL_VIEWER_NAVIGATE))
                return;     // not allowed
            if(rightPhoto != null && !s.surface.isSmoothMoving()) {
                s.surface.stop();
                s.surface.move(-s.surface.movedX(), -s.surface.movedY());
                s.surface.seekGravityTarget(-1, 0);
                s.nextSound.play();
            }
            return;
        }

    }
}
