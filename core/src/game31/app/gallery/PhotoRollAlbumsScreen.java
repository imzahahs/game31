package game31.app.gallery;

import com.badlogic.gdx.utils.Array;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.MediaAlbum;
import game31.RefreshAction;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.gallery.GBPhotoRollAlbumsScreen;
import sengine.Entity;
import sengine.graphics2d.Sprite;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 25/7/2016.
 */
public class PhotoRollAlbumsScreen extends Menu<Grid> implements OnClick<Grid> {

    public interface SelectCallback {
        void onSelectedMedia(String media);
        void onReturnFromAttachment(Entity<?> attachmentScreen);
    }

    private final PhotoRollApp app;


    // Window


    // Interface source
    private final Builder<Object> interfaceSource;

    // Row group
    private Clickable rowGroup;
    private StaticSprite rowPreviewView;
    private TextBox rowTitleView;
    private String rowTitleFormat;

    private Sprite audioThumbIcon;

    // Window
    private UIElement<?> window;
    private ScrollableSurface surface;
    private ScreenBar bars;
    private Clickable refreshButton;

    // Current
    private final Array<MediaAlbum> albums = new Array<MediaAlbum>(MediaAlbum.class);
    private final Array<Clickable> rows = new Array<Clickable>(Clickable.class);
    private float surfaceY;

    private SelectCallback selectCallback = null;

    // Transition
    private Entity<?> transitionFrom;

    public void show(SelectCallback selectCallback) {
        this.selectCallback = selectCallback;
        transitionFrom = null;
    }


    public void open(Entity<?> transitionFrom, Entity<?> target) {
        this.transitionFrom = transitionFrom;
        ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(transitionFrom, this, target);
        transition.attach(target);
    }

    public void clear() {
        surfaceY = (+surface.getLength() / 2f) - surface.paddingTop();
        surface.detachChilds();
        albums.clear();
        rows.clear();
    }


    public void setRowGroup(Clickable group, StaticSprite previewView, TextBox titleView, String titleFormat, Sprite audioThumbIcon) {
        rowGroup = group;
        rowPreviewView = previewView;
        rowTitleView = titleView;
        rowTitleFormat = titleFormat;
        this.audioThumbIcon = audioThumbIcon;
    }


    public void setWindow(UIElement<?> window, ScrollableSurface surface, ScreenBar bars, Clickable refreshButton) {
        // Activate
        if(this.window != null)
            this.window.detach();
        this.window = window.viewport(viewport).attach();
        this.surface = surface;
        this.bars = bars;
        this.refreshButton = refreshButton;

        // Refresh
        if(albums.size > 0)
            app.refresh();
        else
            clear();
    }

    public void addAlbum(MediaAlbum album) {
        albums.add(album);

        // Instantiate and add row
        Clickable row = rowGroup.instantiate().viewport(surface).attach();
        row.metrics.anchorWindowY = surfaceY / surface.getLength();
        row.find(rowTitleView).text().text(String.format(rowTitleFormat, album.name, album.medias.size));
        if(album.medias.size > 0) {
            Media last = album.medias.items[album.medias.size - 1];
            Sprite thumb;
            if(last.isAudio())
                thumb = audioThumbIcon;
            else
                thumb = last.thumbnailSquare;
            row.find(rowPreviewView).visual(thumb);
        }

        rows.add(row);

        surfaceY += (-row.getLength() * row.metrics.scaleY);     // Starting a new row

    }


    public PhotoRollAlbumsScreen(PhotoRollApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<Object>(GBPhotoRollAlbumsScreen.class, this);
        interfaceSource.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();
    }

    @Override
    public void onClick(final Grid v, UIElement<?> button, int b) {
        if(button == bars.backButton()) {
            if(transitionFrom != null) {
                ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, transitionFrom, getEntityParent());
                transition.attach(getEntityParent());
                transitionFrom = null;
            }
            else
                v.homescreen.transitionBack(this, v);
            return;
        }

        if(button == bars.homeButton()) {
            transitionFrom = null;
            v.homescreen.transitionBack(this, v);
            return;
        }

        if(button == bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        if(button == refreshButton) {
            new RefreshAction() {
                @Override
                protected void load() {
                    app.load(Globals.galleryConfigFilename, v.state);
                }
            }.start();
            return;
        }

        for(int c = 0; c < rows.size; c++) {
            if(button == rows.items[c]) {
                // This is the button, show
                app.gridScreen.show(albums.items[c], true, selectCallback);
                // Open
                ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(this, app.gridScreen, v.screensGroup);
                transition.attach(v);
                return;
            }
        }
    }
}
