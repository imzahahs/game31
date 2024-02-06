package game31.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import java.text.SimpleDateFormat;
import java.util.Date;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.GBGatewayAdScreen;
import game31.glitch.MpegGlitch;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.audio.Audio;
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

public class GatewayAdScreen extends Menu<Grid> implements OnClick<Grid> {


    public interface BuilderSource {
        Animation createFullscreenAnim(Sprite videoMesh);
    }

    private static final String OGG_EXTENSION = ".ogg";

    private static final Date date = new Date();

    public static class Internal {
        public UIElement<?> window;

        public StaticSprite videoView;

        public UIElement.Group liveGroup;
        public TextBox liveTimerView;
        public SimpleDateFormat liveTimerFormat;

        public StaticSprite logoGroup;
        public MpegGlitch logoGlitch;
        public StaticSprite symbolView;
        public MpegGlitch codeGlitch;
        public StaticSprite codeView;
        public float tSymbolTime;
        public float tCodeTime;

        public MpegGlitch skipFailedGlitch;

        public StaticSprite closeView;

        public StaticSprite timerGroup;
        public CircularSprite timerMesh;
        public TextBox timerTextView;
        public float tSkipTime;

        public Clickable tapView;

        public Audio.Sound introSound;
        public Audio.Sound endSound;
    }




    // App
    private Internal s;

    // Interface source
    private final Builder<BuilderSource> builder;

    private Media media = null;
    private VideoMaterial video = null;
    private Music audioTrack = null;
    private Sprite videoMesh = null;
    private float tPlayingTime = -1;
    private float tAudioPlayingTime = -1;
    private float tSkipTime;
    private Music codeSound;

    private long prevTime;


    private float tCodeScheduled = Float.MAX_VALUE;
    private float tEndScheduled = Float.MAX_VALUE;
    private boolean isEnding = false;
    private boolean allowSkip;

    private Runnable onFinished;

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void show(String mediaPath, String codePath, String audioCodePath, boolean instantRotation, boolean allowSkip) {
        clear();

        this.allowSkip = allowSkip;

        media = Globals.grid.photoRollApp.find(mediaPath);
        if(media == null || !media.isVideo())
            throw new RuntimeException("Not valid video path \"" + mediaPath + "\"");

        // Set code
        if(codePath != null)
            s.codeView.visual(Sprite.load(codePath + ".NoiseMaterial"));
        else
            s.codeView.visual(null);
        if(audioCodePath != null) {
            codeSound = Gdx.audio.newMusic(File.open(audioCodePath));
            codeSound.setLooping(true);
        }

        // Timing
        this.prevTime = -1;

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

        // Choose the shorter skip time
        tSkipTime = Math.min(s.tSkipTime, media.video.duration);

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

    public void open() {
        // Transition
//        ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(gameScreen, this, Globals.grid.screensGroup);
        ScreenTransition transition = ScreenTransitionFactory.createBrightTransition(Globals.grid.screensGroup, this, Globals.grid.screensGroupContainer);
        transition.attach(Globals.grid);
    }

    public void close(boolean isSkipped) {
        if(isEnding)
            return;         // already ending

        // Transition back
//        ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(this, gameScreen, Globals.grid.screensGroup);
        ScreenTransition transition;
        if(isSkipped)
            transition = ScreenTransitionFactory.createFadeTransition(this, Globals.grid.screensGroup, Globals.grid.screensGroupContainer);
        else
            transition = ScreenTransitionFactory.createBrightTransition(this, Globals.grid.screensGroup, Globals.grid.screensGroupContainer);
        transition.attach(Globals.grid);

        isEnding = true;

        // Exit fullscreen
        Globals.grid.screen.exitFullscreen();

        if(onFinished != null)
            onFinished.run();
    }


    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        if(isAttached())
            close(false);        // For development purposes
    }



    public void clear() {
        if(video != null) {
            Globals.grid.notification.stopSubtitle(video.filename + OGG_EXTENSION);
            video.clear();
        }
        if(audioTrack != null)
            audioTrack.dispose();
        media = null;
        video = null;
        audioTrack = null;
        tPlayingTime = 0;
        tAudioPlayingTime = 0;
        videoMesh = null;
        s.videoView.visual(null);
        if(codeSound != null)
            codeSound.dispose();
        codeSound = null;

        onFinished = null;
    }


    public GatewayAdScreen() {
        // Initialize
        builder = new Builder<BuilderSource>(GBGatewayAdScreen.class, this);
        builder.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Reset
        s.logoGroup.detach();
        s.closeView.detach();
        s.timerGroup.attach();
        s.liveGroup.attach();

        tCodeScheduled = Float.MAX_VALUE;
        tEndScheduled = Float.MAX_VALUE;

        // Allow play video
        audioTrack.play();
//        if(playIntroSound)
        s.introSound.play();

        // Stop all chats
        grid.whatsupApp.timeMultiplier = 0f;
        grid.mailApp.timeMultiplier = 0f;
        grid.notification.hideNow();

        isEnding = false;

        // Analytics
        Game.analyticsView(media.name, Globals.ANALYTICS_CONTENT_TYPE_TEDDY_ADS);
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
    }


    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(isEnding)
            return;

        // Delay idle scare
        grid.idleScare.reschedule();

        // Update skip time
        if(tSkipTime != Float.MAX_VALUE) {
            if(renderTime > tSkipTime) {
                s.timerGroup.detachWithAnim();
                s.closeView.attach();
                tSkipTime = Float.MAX_VALUE;
            }
            else {
                // Update timer
                s.timerTextView.text(Integer.toString(Math.round(tSkipTime - renderTime)));
                s.timerMesh.show(0f, 360f - ((renderTime / tSkipTime) * 360f));
            }
        }

        if(s.logoGroup.isAttached()) {
            // Check if time to end
            if(renderTime > tCodeScheduled) {
                // Transition to showing code
                s.symbolView.detachWithAnim();
                s.codeView.attach();
                s.codeGlitch.attach(grid);
                s.codeGlitch.detachWithAnim();
                tCodeScheduled = Float.MAX_VALUE;
                tEndScheduled = renderTime + s.tCodeTime;
                // Play code sound
                if(codeSound != null)
                    codeSound.play();
            }
            else if(renderTime > tEndScheduled) {
                // Time to end
                tEndScheduled = Float.MAX_VALUE;
                close(false);
                s.endSound.play();
            }

            return;
        }

        // Play
        // Determine current frame
        if(!audioTrack.isPlaying()) {
            // Show symbol
            s.logoGroup.attach();
            s.logoGlitch.attach(grid);
            s.logoGlitch.detachWithAnim();
            s.codeView.detach();
            s.symbolView.attach();
            s.timerGroup.detachWithAnim();
            s.closeView.detachWithAnim();
            s.liveGroup.detachWithAnim();
            if(s.codeView.visual() != null)
                tCodeScheduled = renderTime + s.tSymbolTime;
            else
                tEndScheduled = renderTime + s.tSymbolTime;
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

            // Update timer
            long currentTime = grid.getSystemTime();
            if(Math.abs(currentTime - prevTime) > 1000) {
                // Update time
                date.setTime(currentTime);
                s.liveTimerView.text(s.liveTimerFormat.format(date).toUpperCase());
                prevTime = currentTime;
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
            if(allowSkip)
                close(true);
            else {
                s.skipFailedGlitch.attach(v);
                s.skipFailedGlitch.detachWithAnim();
            }
            return;
        }
    }
}
