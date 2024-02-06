package game31.app.gallery;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.gb.gallery.GBFullVideoScreen;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.materials.VideoMaterial;
import sengine.ui.Menu;
import sengine.ui.StaticSprite;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 8/24/2017.
 */

public class FullVideoScreen extends Menu<Grid> {

    private static final String OGG_EXTENSION = ".ogg";

    public static class Internal {
        public UIElement<?> window;

        public StaticSprite videoView;
    }


    public interface BuilderSource {
        Animation createFullscreenAnim(Sprite videoMesh);
    }


    // App
    private final PhotoRollApp app;
    private Internal s;

    // Interface source
    private final Builder<BuilderSource> builder;

    private VideoMaterial video = null;
    private Music audioTrack = null;
    private Sprite videoMesh = null;
    private float tPlayingTime = -1;
    private float tAudioPlayingTime = -1;
    private Runnable onEnded;



    public void show(String mediaPath) {
        clear();

        Media media = Globals.grid.photoRollApp.find(mediaPath);
        if(media == null || !media.isVideo())
            throw new RuntimeException("Not valid video path \"" + mediaPath + "\"");

        // Open video file
        video = Material.load(media.video.filename + VideoMaterial.CFG_EXTENSION);
        if(Globals.r_highQuality)
            video.filenameOverride = null;
        else
            video.filenameOverride = video.filename + ".low";
        video.show(0, false);
        video.load();

        // Audio and subtitles
        String audioFilename = video.filename + OGG_EXTENSION;
        audioTrack = Gdx.audio.newMusic(File.open(audioFilename));
        Globals.grid.notification.startSubtitle(audioFilename);

        // Update video view
        videoMesh = new Sprite(video.length, media.thumbnail.getMaterial());
        s.videoView.visual(videoMesh);
        s.videoView.detach();

    }

    public void play(Runnable onEnded) {
        audioTrack.play();
        s.videoView.attach();

        this.onEnded = onEnded;

        if(videoMesh.length < 1f) {
            // All videos are assumed to be landscape for now
            Animation startAnim = builder.build().createFullscreenAnim(videoMesh);
            s.videoView.windowAnimation(startAnim.startAndReset(), true, true);
            s.videoView.windowAnim.setProgress(1f);     // start
            Globals.grid.screen.enterFullscreen(true);
        }
        else
            s.videoView.windowAnimation(null, false, false);
    }


    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
    }



    public void clear() {
        if(video != null) {
            Globals.grid.notification.stopSubtitle(video.filename + OGG_EXTENSION);
            video.clear();
        }
        if(audioTrack != null)
            audioTrack.dispose();
        video = null;
        audioTrack = null;
        tPlayingTime = 0;
        tAudioPlayingTime = 0;
        videoMesh = null;
        s.videoView.visual(null);
    }


    public FullVideoScreen(PhotoRollApp app) {
        this.app = app;

        // Initialize
        builder = new Builder<BuilderSource>(GBFullVideoScreen.class, this);
        builder.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        grid.notification.hideNow();

        builder.start();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        clear();

        // Exit fullscreen if applicable
        if(s.videoView.windowAnim != null)
            Globals.grid.screen.exitFullscreen();
    }


    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(!s.videoView.isAttached())
            return;

        // Delay idle scare
        grid.idleScare.reschedule();

        // Play
        // Determine current frame
        if(!audioTrack.isPlaying()) {
            // Finished
            s.videoView.detach();
            if(onEnded != null)
                onEnded.run();
        }
        else {
            Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);
            float currentAudioPosition = audioTrack.getPosition();
            if(currentAudioPosition != tAudioPlayingTime) {
                if(tPlayingTime < currentAudioPosition) {
                    // Only accept new position if its skipping frames, looking back frames are very expensive
                    tPlayingTime = currentAudioPosition;
                    tAudioPlayingTime = currentAudioPosition;
                }
            }
            else if(tAudioPlayingTime != 0) {
                // Manually calculate audio position, since there is no movement (on some devices, audio position has a very low resolution)
                tPlayingTime += Gdx.graphics.getRawDeltaTime();
            }
        }

        // Determine frame
        video.show(tPlayingTime, false);
        Globals.grid.notification.updateSubtitles(tPlayingTime, s.videoView.windowAnim != null);

        if(videoMesh.getMaterial() != video && video.isLoaded())
            videoMesh.setMaterial(video);
    }

}
