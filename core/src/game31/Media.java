package game31;

import com.badlogic.gdx.graphics.Pixmap;

import game31.model.MediaModel;
import sengine.File;
import sengine.graphics2d.Material;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.TextureUtils;
import sengine.materials.SimpleMaterial;
import sengine.materials.VideoMaterial;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;

/**
 * Created by Azmi on 22/7/2016.
 */
public class Media {

    public static float MAX_CROP_LENGTH = 1f;

    public static final String THUMBNAIL_EXTENSION = ".thumb";
    public static final String THUMBNAIL_SQUARE_EXTENSION = ".square";

    public static final String EXTENSION_MP4 = "mp4";
    public static final String EXTENSION_OGG = "ogg";

    public static final String VIDEO_COVER = "/cover.jpg";


//    public static class AudioRecordingInfo implements MassSerializable {
//        public static final String CFG_COVER = "cover";
//        public static final String CFG_DURATION = "duration";
//        public static final String CFG_EXTENSION = ".AudioInfo";
//
//        public final Sprite cover;
//        public final float duration;
//
//        public static AudioRecordingInfo load(String filename) {
//            AudioRecordingInfo info = File.getHints(filename, true, false);
//            if (info != null)
//                return info;
//            // Load now
//            Config config = Config.load(filename + CFG_EXTENSION);
//            Sprite cover = config.get(CFG_COVER);
//            float duration = config.get(CFG_DURATION);
//            info = new AudioRecordingInfo(null, duration);
//            File.saveHints(filename, info);
//            return info;
//        }
//
//        @MassConstructor
//        public AudioRecordingInfo(Sprite cover, float duration) {
//            this.cover = cover;
//            this.duration = duration;
//        }
//
//        @Override
//        public Object[] mass() {
//            return new Object[] { cover, duration };
//        }
//    }

    /*
    public static Photo[] load(String filename, ScriptState state) {
        // TODO: load from filesystem
        try {
            String content = File.read(filename);
            PhotoAppModel model = Globals.gson.fromJson(content, PhotoAppModel.class);
            Photo[] photos = new Photo[model.photos.length];
            for(int c = 0; c < photos.length; c++)
                photos[c] = new Photo(model.photos[c], state, model.thumb_config);
            return photos;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to load photo app " + filename, e);
        }
    }
    */

    public final String filename;

    public final Sprite thumbnail;
    public final Sprite thumbnailSquare;
    public final Sprite croppedThumb;
    public final Sprite croppedFull;
    public final Sprite fullSquare;

    // Meta
    public final String name;
    public final String dateText;

    public final String caption;

    // Albums
    public final String album;

    // Photo
    public final Sprite full;

    // Video
    public final VideoMaterial video;

    // Audio
    public final VoiceProfile audioInfo;

    // Gameplay
    public final String trigger;
    public final boolean isHidden;
    public final boolean isHiddenUntilOpened;
    public final String corruption;

    public boolean wasOpened() {
        return !isHidden || Globals.grid.isStateUnlocked(Globals.STATE_GALLERY_OPENED_MEDIA_PREFIX + album + "/" + name);
    }

    public boolean markOpened() {
        return Globals.grid.unlockState(Globals.STATE_GALLERY_OPENED_MEDIA_PREFIX + album + "/" + name);
    }

    public boolean isVideo() {
        return video != null;
    }

    public boolean isAudio() {
        return audioInfo != null;
    }

    public void loadBest(StaticSprite view, float length) {
        Sprite best;
        if(view.isEffectivelyRendering()) {
            // Load best quality if rendering
            best = full.isLoaded() ? full : thumbnail;
            full.load();
        }
        else
            best = thumbnail;       // Else just revert to thumbnail

        Mesh current = view.visual();

        // If length doesnt matter, just use best
        if(length <= 0 || length == best.length) {
            if(best != current)
                view.visual(best);          // update
            return;
        }

        // Else crop needed, check if can use existing
        if(current != null && current.getMaterial() == best.getMaterial() && current.getLength() == best.getLength())
            return;     // can use existing

        // Else need to crop best
        best = new Sprite(best.length, best.getMaterial());
        best.crop(length);
        view.visual(best);
    }

    public void loadBest(Clickable view, float length) {
        Sprite best;
        if(view.isEffectivelyRendering()) {
            // Load best quality if rendering
            best = full.isLoaded() ? full : thumbnail;
            full.load();
        }
        else
            best = thumbnail;       // Else just revert to thumbnail

        Mesh current = view.buttonUp();

        // If length doesnt matter, just use best
        if(length <= 0 || length == best.length) {
            if(best != current)
                view.visuals(best);          // update
            return;
        }

        // Else crop needed, check if can use existing
        if(current != null && current.getMaterial() == best.getMaterial() && current.getLength() == best.getLength())
            return;     // can use existing

        // Else need to crop best
        best = new Sprite(best.length, best.getMaterial());
        best.crop(length);
        view.visuals(best);
    }

    public Sprite loadBestCrop() {
        Sprite best = croppedFull.isLoaded() ? croppedFull : croppedThumb;         // return the next best thing
        croppedFull.load();         // load best quality
        return best;
    }

    public Sprite loadBestSquare() {
        Sprite best = fullSquare.isLoaded() ? fullSquare : thumbnailSquare;         // return the next best thing
        fullSquare.load();          // load best quality
        return best;
    }

    public Media(MediaModel model, ScriptState state, String thumbnailConfig, String squareConfig) {

        this.filename = model.filename;

        // Build thumbnail filename
        String[] paths = File.splitExtension(model.filename);
        if (paths[1] == null)
            throw new RuntimeException("Filenames must have extension");
        String thumbnailFilename = paths[0] + THUMBNAIL_EXTENSION;
        String thumbnailSquareFilename = paths[0] + THUMBNAIL_SQUARE_EXTENSION;
        thumbnailFilename += "." + paths[1];
        thumbnailSquareFilename += "." + paths[1];
        if (paths[1].equalsIgnoreCase(EXTENSION_MP4)) {
            // Is a video
            video = Material.load(model.filename + ".VideoMaterial");
            // Check if thumbnail exists
            Sprite sprite = Sprite.load(thumbnailFilename, false);
            if (sprite == null) {
                // Thumbnail does not exist, compile thumbnail and full
                Pixmap firstFrame = video.get(0);
                video.performGC(true);      // release

                // Compile full
                SimpleMaterial.CompileConfig config = SimpleMaterial.CompileConfig.load(model.filename);
                full = new Sprite(new SimpleMaterial(model.filename, TextureUtils.duplicate(firstFrame), config));
                full.save(model.filename);

                // Compile thumbnail
                config = SimpleMaterial.CompileConfig.load(thumbnailConfig);
                thumbnail = new Sprite(new SimpleMaterial(thumbnailFilename, TextureUtils.duplicate(firstFrame), config));
                thumbnail.save(thumbnailFilename);

                // Square
                config = SimpleMaterial.CompileConfig.load(squareConfig);
                thumbnailSquare = new Sprite(new SimpleMaterial(thumbnailSquareFilename, firstFrame, config));
                thumbnailSquare.save(thumbnailSquareFilename);

            } else {
                thumbnail = sprite;
                thumbnailSquare = Sprite.load(thumbnailSquareFilename);
                full = Sprite.load(model.filename);
            }
            // Unused
            audioInfo = null;
        } else if (paths[1].equalsIgnoreCase(EXTENSION_OGG)) {
            // Is a voice recording
            audioInfo = VoiceProfile.load(model.filename);
            thumbnail = null;
            thumbnailSquare = null;
            // Unused
            full = null;
            video = null;
        } else {
            // Else is a photo
            // Check if thumbnail exists
            Sprite sprite = Sprite.load(thumbnailFilename, false);
            if (sprite == null) {
                // Thumbnail does not exist, compile thumbnail and full
                SimpleMaterial.CompileConfig config = SimpleMaterial.CompileConfig.load(model.filename);
                // Compile full
                full = new Sprite(new SimpleMaterial(model.filename, config));
                full.save(model.filename);
                // Compile thumbnail
                config = SimpleMaterial.CompileConfig.load(thumbnailConfig);
                thumbnail = new Sprite(new SimpleMaterial(thumbnailFilename, model.filename, config));
                thumbnail.save(thumbnailFilename);
                // Square
                config = SimpleMaterial.CompileConfig.load(squareConfig);
                thumbnailSquare = new Sprite(new SimpleMaterial(thumbnailSquareFilename, model.filename, config));
                thumbnailSquare.save(thumbnailSquareFilename);
            } else {
                thumbnail = sprite;
                thumbnailSquare = Sprite.load(thumbnailSquareFilename);
                full = Sprite.load(model.filename);
            }
            // Unused
            video = null;
            audioInfo = null;
        }

        // Crop full
        if (full != null) {
            if (full.length <= MAX_CROP_LENGTH) {
                // Can be use as it is
                croppedFull = full;
                croppedThumb = thumbnail;
            } else {
                // Else need to crop
                croppedFull = new Sprite(full.length, full.getMaterial());
                croppedFull.crop(MAX_CROP_LENGTH);
                croppedThumb = new Sprite(thumbnail.length, thumbnail.getMaterial());
                croppedThumb.crop(MAX_CROP_LENGTH);
            }
            fullSquare = new Sprite(full.length, full.getMaterial());
            fullSquare.crop(1f);        // square
        }
        else {
            croppedFull = null;
            croppedThumb = null;
            fullSquare = null;
        }


        // Rest
        name = model.name;
        caption = model.caption;
        dateText = model.date_text;
        album = model.album;

        trigger = model.trigger;

        isHidden = model.is_hidden;
        isHiddenUntilOpened = model.is_hidden_until_opened;

        corruption = model.corruption;
    }
}
