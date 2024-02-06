package game31.app.gallery;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import game31.Globals;
import game31.Grid;
import game31.app.homescreen.Homescreen;
import game31.JsonSource;
import game31.Media;
import game31.MediaAlbum;
import game31.ScriptState;
import game31.model.MediaModel;
import game31.model.PhotoAppModel;
import sengine.Entity;
import sengine.Sys;

/**
 * Created by Azmi on 21/7/2016.
 */
public class PhotoRollApp extends Entity<Grid> implements Homescreen.App {
    private static final String TAG = "PhotoRollApp";

    public static final String VAR_UNLOCKED = "photoroll.unlocked";

    public final PhotoRollAlbumsScreen albumsScreen;
    public final PhotoRollGridScreen gridScreen;
    public final PhotoRollPhotoScreen photoScreen;
    public final PhotoRollVideoScreen videoScreen;
    public final FullVideoScreen fullVideoScreen;

    // Current
    private final Array<MediaAlbum> albums = new Array<MediaAlbum>(MediaAlbum.class);
    private final ObjectMap<String, Media> lookup = new ObjectMap<String, Media>();

    // Sources
    private JsonSource<PhotoAppModel> configSource;

    public MediaAlbum findAlbum(String name) {
        if(name == null || name.isEmpty())
            return null;
        for(int c = 0; c < albums.size; c++) {
            if(albums.items[c].name.contentEquals(name))
                return albums.items[c];
        }
        return null;
    }

    public void addMedia(Media media) {
        // Album
        MediaAlbum album = findAlbum(media.album);
        if(album == null) {
            album = new MediaAlbum(media.album);
            albums.add(album);
        }
        album.medias.add(media);
    }

    public void removeMedia(Media media) {
        // Album
        MediaAlbum album = findAlbum(media.album);
        if(album == null)
            return;     // cannot find album
        album.medias.removeValue(media, true);
        refresh();
    }

    // Controls
    public void clear() {
        gridScreen.clear();
        albumsScreen.clear();
        albums.clear();
        lookup.clear();
    }

    public void prepareAlbums(String ... names) {
        for(String name : names) {
            MediaAlbum album = findAlbum(name);
            if(album == null)
                album = new MediaAlbum(name);
            albums.add(album);
        }
    }

    public Media find(String path) {
        return lookup.get(path);
    }

    public void lock(String path) {
        Media media = find(path);
        if(media == null)
            throw new RuntimeException("Cannot find media " + path);
        // Lock it
        String unlockedVar = VAR_UNLOCKED + "." + path;
        Globals.grid.state.set(unlockedVar, false);
        // Remove media
        removeMedia(media);
    }

    public boolean isUnlocked(String path) {
        Media media = find(path);
        if(media == null)
            throw new RuntimeException("Cannot find media " + path);
        if(!media.isHidden && !media.isHiddenUntilOpened)
            return true;           // not hidden
        String unlockedVar = VAR_UNLOCKED + "." + path;
        boolean isUnlocked = Globals.grid.state.get(unlockedVar, false);
        return isUnlocked;
    }

    public Media unlock(String path, boolean opened) {
        // Find
        Media media = find(path);
        if(media == null)
            throw new RuntimeException("Cannot find media " + path);
        if(!media.isHidden && !media.isHiddenUntilOpened)
            return media;           // not hidden
        // Else is hidden in either one way
        if(media.isHiddenUntilOpened && !opened)
            return media;           // cannot unlock as needs to be opened first
        ScriptState state = Globals.grid.state;
        String unlockedVar = VAR_UNLOCKED + "." + path;
        boolean isUnlocked = state.get(unlockedVar, false);
        if(!isUnlocked) {
            state.set(unlockedVar, true);
            // Add to album
            addMedia(media);
            refresh();
        }
        return media;
    }


    public void load(final String filename, final ScriptState state) {
        // Reset
        if(configSource != null)
            configSource.stop();
        clear();

        configSource = new JsonSource<PhotoAppModel>(filename, PhotoAppModel.class);
        configSource.listener(new JsonSource.OnChangeListener<PhotoAppModel>() {
            @Override
            public void onChangeDetected(JsonSource<PhotoAppModel> source) {
                load(filename, state);
            }
        });

        PhotoAppModel config = configSource.load();

        // Load all photos
        for(int c = 0; c < config.photos.length; c++) {
            MediaModel model = config.photos[c];
            Media media = null;
            try {
                media = new Media(model, state, config.thumb_config, config.square_config);
            } catch (Throwable e) {
                Sys.error(TAG, "Unable to load media " + model.filename, e);
                if(model.filename.endsWith(Media.EXTENSION_MP4))
                    model.filename = "content/videos/novideo.mp4";
                else if(model.filename.endsWith(Media.EXTENSION_OGG))
                    model.filename = "content/recordings/noaudio.ogg";
                else
                    model.filename = "content/gallery/notexture.png";
                media = new Media(model, state, config.thumb_config, config.square_config);
            }
            // Add lookup
            String path = model.album + "/" + model.name;
            lookup.put(path, media);
            // Check hidden status
            Boolean isUnlocked = state.get(VAR_UNLOCKED + "." + path, null);
            if(isUnlocked != null && !isUnlocked)
                continue;       // this video is locked by state, regardless of whether its hidden or not
            else if(media.isHidden || media.isHiddenUntilOpened) {
                if(isUnlocked == null)
                    continue;           // haven't unlocked yet
                // Else can add
            }
            addMedia(media);
        }

        // Refresh
        refresh();

        if(isAttached())
            configSource.start();
    }

    public void refresh() {
        // Refresh all screens
        MediaAlbum album = gridScreen.getCurrentAlbum();
        if(album != null) {
            gridScreen.clear();
            album = findAlbum(album.name);
            if(album != null)
                gridScreen.show(album, false, null);        // backToAlbumScreen was previously true, disabled to make it work with downloads
        }
        // Albums screen
        albumsScreen.clear();
        for(int c = 0; c < albums.size; c++) {
            album = albums.items[c];
            if(album.name.startsWith("_"))
                continue;       // is hidden
            albumsScreen.addAlbum(album);
        }
    }


    public PhotoRollApp() {
        albumsScreen = new PhotoRollAlbumsScreen(this);
        gridScreen = new PhotoRollGridScreen(this);
        photoScreen = new PhotoRollPhotoScreen(this);
        videoScreen = new PhotoRollVideoScreen(this);
        fullVideoScreen = new FullVideoScreen(this);

        load(Globals.galleryConfigFilename, Globals.grid.state);
    }

    @Override
    protected void recreate(Grid v) {
        super.recreate(v);

        if(configSource != null)
            configSource.start();
    }

    @Override
    protected void release(Grid v) {
        super.release(v);

        if(configSource != null)
            configSource.stop();
    }

    @Override
    public Entity<?> open() {
        if(!Globals.grid.trigger(Globals.TRIGGER_OPEN_GALLERY_FROM_HOMESCREEN))
            return null;        // not allowed
        albumsScreen.show(null);
        return albumsScreen;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }
}
