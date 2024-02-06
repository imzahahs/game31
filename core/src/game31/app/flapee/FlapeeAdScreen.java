package game31.app.flapee;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.MediaAlbum;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.flapee.GBFlapeeAdScreen;
import game31.triggers.ACT1;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.calc.SetRandomizedSelector;
import sengine.graphics2d.CircularSprite;
import sengine.graphics2d.Sprite;
import sengine.materials.VideoMaterial;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

public class FlapeeAdScreen extends Menu<Grid> implements OnClick<Grid> {


    public interface BuilderSource {
        Animation createFullscreenAnim(Sprite videoMesh);
        Animation createWindowedAnim(Sprite videoMesh);
    }

    private static final String OGG_EXTENSION = ".ogg";

    public static class Internal {
        public UIElement<?> window;

        public StaticSprite videoView;

        public TextBox titleView;

        public StaticSprite logoView;
        public float tLogoTime;

        public StaticSprite closeView;

        public StaticSprite timerGroup;
        public CircularSprite timerMesh;
        public TextBox timerTextView;
        public float tSkipTime;

        public Clickable tapView;

        public Audio.Sound introSound;
    }




    // App
    private FlapeeBirdScreen gameScreen;
    private Internal s;

    // Interface source
    private final Builder<BuilderSource> builder;

    private VideoMaterial video = null;
    private Music audioTrack = null;
    private Sprite videoMesh = null;
    private float tPlayingTime = -1;
    private float tAudioPlayingTime = -1;
    private float tSkipTime;
    private boolean hasStarted = false;
    private boolean isEnding = false;
    private boolean hasIntroSoundPlayed = false;

    // Ads album
    private MediaAlbum adsAlbum;
    private SetRandomizedSelector<Media> adSelector;

    private Runnable onFinished = null;

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void show(boolean instantRotation) {
        show(instantRotation, true, null);
    }

    public void show(boolean instantRotation, boolean canSkip) {
        show(instantRotation, canSkip, null);
    }

    public void show(boolean instantRotation, boolean canSkip, String preferredAd) {
        clear();

        Media media = preferredAd != null ? Globals.grid.photoRollApp.find(preferredAd) : null;

        if(media == null) {
            // Get ads album
            MediaAlbum album = Globals.grid.photoRollApp.findAlbum(Globals.FLAPEE_BIRD_ADS_ALBUM);
            if (adsAlbum != album) {
                adsAlbum = album;
                adSelector = new SetRandomizedSelector<>(adsAlbum.medias.toArray());
            }

            // Select a video
            while (!(media = adSelector.select()).isVideo()) ;
        }


        // Achievement
        ACT1.unlockAchievement(Globals.Achievement.IRIS_ADS, media.album + "/" + media.name);

        // Title
        s.titleView.text(media.caption);

        // Open video file
        video = media.video;
        if(Globals.r_highQuality)
            video.filenameOverride = null;
        else
            video.filenameOverride = video.filename + ".low";
        video.load();

        // Audio and subtitles
        String audioFilename = video.filename + OGG_EXTENSION;
        audioTrack = Gdx.audio.newMusic(File.open(audioFilename));
        Globals.grid.notification.startSubtitle(audioFilename);

        // Update video view
        videoMesh = new Sprite(video.length, media.thumbnail.getMaterial());
        s.videoView.visual(videoMesh);

        if(canSkip)
            tSkipTime = Math.min(s.tSkipTime, media.video.duration);        // Choose the shorter skip time
        else
            tSkipTime = media.video.duration;

        // Stop video playback
        Globals.grid.photoRollApp.videoScreen.pause();

        // Rotate
//        if(videoMesh.length < 1f) {
            // All videos are assumed to be landscape for now
            Animation startAnim = builder.build().createFullscreenAnim(videoMesh);
            s.videoView.windowAnimation(startAnim.startAndReset(), true, true);
            s.videoView.windowAnim.setProgress(1f);     // start
            Globals.grid.screen.enterFullscreen(instantRotation);
//        }
//        else
//            s.videoView.windowAnimation(null, false, false);

    }

    public void open(boolean isAbrupt) {
        // Transition
//        ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(gameScreen, this, Globals.grid.screensGroup);
        ScreenTransition transition;
        if(isAbrupt) {
            transition = ScreenTransitionFactory.createBrightTransition(Globals.grid.screensGroup, this, Globals.grid.screensGroupContainer);
            s.introSound.play();
            hasIntroSoundPlayed = true;
        }
        else
            transition = ScreenTransitionFactory.createFadeTransition(Globals.grid.screensGroup, this, Globals.grid.screensGroupContainer);
        transition.attach(Globals.grid);
    }

    public void close() {
        // Transition back
//        ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(this, gameScreen, Globals.grid.screensGroup);
        ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(this, Globals.grid.screensGroup, Globals.grid.screensGroupContainer);
        transition.attach(Globals.grid);

        isEnding = true;
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

        hasIntroSoundPlayed = false;
    }


    public FlapeeAdScreen(FlapeeBirdScreen gameScreen) {
        this.gameScreen = gameScreen;

        // Initialize
        builder = new Builder<BuilderSource>(GBFlapeeAdScreen.class, this);
        builder.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Reset
        s.logoView.attach();
        s.closeView.detach();
        s.timerGroup.detach();

        // Stop all chats
        grid.whatsupApp.timeMultiplier = 0f;
        grid.mailApp.timeMultiplier = 0f;
        grid.notification.hideNow();

        hasStarted = false;
        isEnding = false;

        // Analytics
        Game.analyticsView(Globals.ANALYTICS_CONTENT_TYPE_IRIS_ADS);
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        clear();


        // Allow all chats
        grid.whatsupApp.timeMultiplier = 1f;
        grid.mailApp.timeMultiplier = 1f;

        // Exit fullscreen
        Globals.grid.screen.exitFullscreen();

        if(onFinished != null) {
            Runnable r = onFinished;
            onFinished = null;
            r.run();
        }
    }


    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(isEnding)
            return;

        // Delay idle scare
        grid.idleScare.reschedule();

        if(!hasStarted) {
            if(!hasIntroSoundPlayed && renderTime > 0) {
                s.introSound.play();
                hasIntroSoundPlayed = true;
            }
            if(renderTime > s.tLogoTime) {
                // Allow play video
                audioTrack.play();

                s.logoView.detachWithAnim();
                s.timerGroup.attach();
                s.titleView.attach();

                hasStarted = true;
            }
            else
                return;     // still showing logo
        }

        // Update skip time
        float elapsed = renderTime - s.tLogoTime;
        if(elapsed > tSkipTime) {
            s.timerGroup.detachWithAnim();
            s.closeView.attach();
            tSkipTime = Float.MAX_VALUE;
        }
        else if(!s.closeView.isAttached()) {
            // Update timer
            s.timerTextView.text(Integer.toString(Math.round(tSkipTime - elapsed)));
            s.timerMesh.show(0f, 360f - ((elapsed / tSkipTime) * 360f));
        }

        // Play
        // Determine current frame
        if(!audioTrack.isPlaying()) {
            close();
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


    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.tapView && s.closeView.isAttached()) {
            // Close now
            close();
            return;
        }
    }
}
