package game31.app.gallery;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;

import java.util.Locale;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.MediaAlbum;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.VoiceProfile;
import game31.gb.gallery.GBPhotoRollVideoScreen;
import sengine.Entity;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.graphics2d.Material;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.Sprite;
import sengine.materials.VideoMaterial;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.OnReleased;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 9/8/2016.
 */
public class PhotoRollVideoScreen extends Menu<Grid> implements OnClick<Grid>, OnReleased<Grid> {
    private static final String TAG = "VideoPlayer";

    public static final String OGG_EXTENSION = ".ogg";

    public interface BuilderSource {
        Animation createFullscreenAnim(Sprite videoMesh);
        Animation createWindowedAnim(Sprite videoMesh);
    }

    public static class Internal {

        // Window
        public UIElement<?> window;
        public ScreenBar bars;

        public StaticSprite bgView;

        public Clickable sendButton;            // TODO

        public StaticSprite videoView;
        public Clickable fullscreenCancelButton;

        public Clickable playButton;
        public Mesh playButtonMesh;
        public Mesh pauseButtonMesh;
        public TextBox elapsedView;
        public HorizontalProgressBar progressBar;
        public TextBox durationView;
        public float tStartDelay;

        public Clickable fullscreenButton;


        public StaticSprite audioIconView;
        public Animation.Instance audioLevelAnim;

        public TextBox captionView;

        // Animation
        public Animation topGroupFullscreenAnim;
        public Animation topGroupWindowedAnim;
        public Animation controlGroupFullscreenAnim;
        public Animation controlGroupWindowedAnim;
        public Animation captionFullscreenAnim;
        public Animation captionWindowedAnim;
        public Animation bgFullscreenAnim;
        public Animation bgWindowedAnim;

        // Sounds
        public Audio.Sound maximizeSound;
        public Audio.Sound minimizeSound;

    }


    // App
    private final PhotoRollApp app;
    private Internal s;

    // Interface source
    private final Builder<BuilderSource> builder;

    // Current
    private MediaAlbum album = null;
    private int index;
    private PhotoRollAlbumsScreen.SelectCallback selectCallback = null;
    private boolean isAutoplay = false;
    private Entity<?> fromScreen = null;
    private float transitionX;
    private float transitionY;
    private float transitionSize;

    private VideoMaterial video = null;
    private VoiceProfile audioInfo = null;
    private String audioFilename = null;
    private Music audioTrack = null;
    private Sprite videoMesh = null;
    private int playingTime = -1;
    private boolean isPlaying = false;
    private boolean isTimeMultiplierObserved = false;
    private float tPlayingTime = -1;
    private float tAudioPlayingTime = -1;
    private boolean isFullscreen = false;
    private boolean isStarted = false;
    private boolean hasPlayed = false;
    private boolean isForcedPlayback = false;

    public float currentPosition() {
        if(audioTrack == null)
            return -1;
        return audioTrack.getPosition();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setForcedPlayback(boolean forcedPlayback) {
        isForcedPlayback = forcedPlayback;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        // Refresh
        if(album != null)
            show(album, index, selectCallback, false);
        else
            clear();
    }


    private int findNextTrack() {
        // Find next track
        for(int c = index + 1; c < album.medias.size; c++) {
            // Check if this media is a video or audio
            Media media = album.medias.items[c];
            if(media.isVideo() || media.isAudio())
                return c;
        }
        return -1;
    }

    private int findPrevTrack() {
        // Find prev track
        for(int c = index - 1; c >= 0; c--) {
            // Check if this media is a video or audio
            Media media = album.medias.items[c];
            if(media.isVideo() || media.isAudio())
                return c;
        }
        return -1;
    }

//    public void showNextTrack() {
//        if(album == null)
//            return;     // no album to use
//        // Find next track
//        index = findNextTrack();
//        show(album, index);
//    }
//
//    public void showPrevTrack() {
//        if(album == null)
//            return;     // no album to use
//        // Find prev track
//        index = findPrevTrack();
//        show(album, index);
//    }

    public void show(MediaAlbum album, int index, PhotoRollAlbumsScreen.SelectCallback selectCallback, boolean autoPlay) {
        clear();

        if(selectCallback != null)
            autoPlay = false;

        this.album = album;
        this.index = index;
        this.selectCallback = selectCallback;
        this.isAutoplay = autoPlay;

        hasPlayed = false;

        Media media = album.medias.items[index];

        // Mark as opened
        media.markOpened();

        if(media.isVideo()) {
            // Open video file
            video = Material.load(media.video.filename + VideoMaterial.CFG_EXTENSION);
            if(Globals.r_highQuality)
                video.filenameOverride = null;
            else
                video.filenameOverride = video.filename + ".low";
            video.show(0, false);
            video.load();
            audioFilename = media.video.filename + OGG_EXTENSION;

            // Update video view
            videoMesh = new Sprite(video.length, media.thumbnail.getMaterial());
            s.videoView.visual(videoMesh);
            s.videoView.attach();

            s.audioIconView.detach();
            audioInfo = null;

            // Show fullscreen button
            s.fullscreenButton.attach();
            s.fullscreenCancelButton.attach();
        }
        else {
            // Audio
            audioInfo = media.audioInfo;
            audioFilename = media.filename;

            video = null;
            videoMesh = null;
            s.videoView.detach();

            s.audioIconView.attach();
            s.audioLevelAnim.setProgress(0);     // reset

            // Cannot fullscreen on audio
            s.fullscreenButton.detach();
            s.fullscreenCancelButton.detach();
        }

        // Audio and subtitles
        audioTrack = Gdx.audio.newMusic(File.open(audioFilename));
        Globals.grid.notification.startSubtitle(audioFilename);

        // Title
        s.bars.showAppbar(media.name, null, 0, 0, 0, 0);

        // Is selecting ?
        if(selectCallback != null)
            s.sendButton.attach();
        else
            s.sendButton.detach();

        // Caption
        if(media.caption != null && !media.caption.isEmpty())
            s.captionView.text(media.caption).windowAnimation(null, false, false).attach();
        else
            s.captionView.detach();

//        if(findNextTrack() != -1)
//            s.nextTrackButton.windowAnimation(null, false, false).enable();
//        else
//            s.nextTrackButton.windowAnimation(s.controlDisabledAnim.startAndReset(), true, true).disable();
//        if(findPrevTrack() != -1)
//            s.prevTrackButton.windowAnimation(null, false, false).enable();
//        else
//            s.prevTrackButton.windowAnimation(s.controlDisabledAnim.startAndReset(), true, true).disable();

        s.playButton.visuals(s.playButtonMesh);
    }

    public void toggleFullscreen() {
        if(isFullscreen)
            showWindowed(true);
        else
            showFullscreen(true, false);
    }

    public void showFullscreen(boolean playSound, boolean isInstant) {
        if(isFullscreen)
            return;
        if(videoMesh.length < 1f) {
            Animation startAnim = builder.build().createFullscreenAnim(videoMesh);
            s.videoView.windowAnimation(startAnim.startAndReset(), true, true);
            if(isInstant)
                s.videoView.windowAnim.setProgress(1f);
            Globals.grid.screen.enterFullscreen(isInstant);
        }
        s.bars.appbar().windowAnimation(s.topGroupFullscreenAnim.startAndReset(), true, true);
        s.bars.navbar().windowAnimation(s.controlGroupFullscreenAnim.startAndReset(), true, true);
        if(s.captionView.isAttached()) {
            s.captionView.windowAnimation(s.captionFullscreenAnim.startAndReset(), true, true);
            if(isInstant)
                s.captionView.windowAnim.setProgress(1f);
        }
        s.bgView.windowAnimation(s.bgFullscreenAnim.startAndReset(), true, true);
        if(isInstant) {
            s.bars.appbar().windowAnim.setProgress(1f);
            s.bars.navbar().windowAnim.setProgress(1f);
            s.bgView.windowAnim.setProgress(1f);
        }
        isFullscreen = true;
        if(playSound)
            s.maximizeSound.play();

    }

    public boolean isInFullscreen() {
        return s.videoView.windowAnim != null || s.bars.appbar().windowAnim != null;
    }

    public void showWindowed(boolean playSound) {
        if(!isFullscreen)
            return;
        if(videoMesh.length < 1f) {
            Animation startAnim = builder.build().createWindowedAnim(videoMesh);
            s.videoView.windowAnimation(startAnim.startAndReset(), true, false);
            Globals.grid.screen.exitFullscreen();
        }
        s.bars.appbar().windowAnimation(s.topGroupWindowedAnim.startAndReset(), true, false);
        s.bars.navbar().windowAnimation(s.controlGroupWindowedAnim.startAndReset(), true, false);
        if(s.captionView.isAttached())
            s.captionView.windowAnimation(s.captionWindowedAnim.startAndReset(), true, false);
        s.bgView.windowAnimation(s.bgWindowedAnim.startAndReset(), true, false);
        isFullscreen = false;
        if(playSound)
            s.minimizeSound.play();
    }

    public void finishWindowAnim() {
        if(s.videoView.windowAnim != null)
            s.videoView.windowAnim.setProgress(1f);
        if(s.bars.appbar().windowAnim != null)
            s.bars.appbar().windowAnim.setProgress(1f);
        if(s.bars.navbar().windowAnim != null)
            s.bars.navbar().windowAnim.setProgress(1f);
        if(s.captionView.windowAnim != null)
            s.captionView.windowAnim.setProgress(1f);
        if(s.bgView.windowAnim != null)
            s.bgView.windowAnim.setProgress(1f);
    }

    public void clear() {
        if(video != null)
            video.clear();
        if(audioTrack != null)
            audioTrack.dispose();
        if(audioFilename != null)
            Globals.grid.notification.stopSubtitle(audioFilename);
        album = null;
        index = -1;
        video = null;
        audioFilename = null;
        audioTrack = null;
        audioInfo = null;
        playingTime = -1;
        isPlaying = false;
        isStarted = false;
        tPlayingTime = 0;
        isTimeMultiplierObserved = false;
        tAudioPlayingTime = 0;
        videoMesh = null;
        s.videoView.visual(null);
        // Reset fullscreen
        isFullscreen = false;
        s.videoView.windowAnimation(null, false, false);
        s.bars.appbar().windowAnimation(null, false, false);
        s.bars.navbar().windowAnimation(null, false, false);
        s.captionView.windowAnimation(null, false, false);
        s.bgView.windowAnimation(null, false, false);
    }

    public void setOpenFrom(Entity<?> transitionFrom, float x, float y, float size) {
        fromScreen = transitionFrom;
        transitionX = x;
        transitionY = y;
        transitionSize = size;
    }

    public void open(Entity<?> transitionFrom, Entity<?> target) {
        setOpenFrom(transitionFrom, -1, -1, -1);
        ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(transitionFrom, this, target);
        transition.attach(target);
    }

    public void open(Entity<?> transitionFrom, Entity<?> target, float x, float y, float size) {
        setOpenFrom(transitionFrom, x, y, size);
        ScreenTransition transition = ScreenTransitionFactory.createHomescreenOutTransition(
                transitionFrom, this, target,
                x, y, size
        );
        transition.attach(target);
    }



    public PhotoRollVideoScreen(PhotoRollApp app) {
        this.app = app;

        // Initialize
        builder = new Builder<BuilderSource>(GBPhotoRollVideoScreen.class, this);
        builder.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Analytics
        Game.analyticsView(album.medias.items[index].name, Globals.ANALYTICS_CONTENT_TYPE_GALLERY);
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        if(fromScreen == null) {
            // Only clear if intended, not when receiving a phonecall
            grid.resumeAmbiance();

            clear();
        }

        showWindowed(false);

        // Trigger
        grid.trigger(Globals.TRIGGER_LEAVING_VIDEO_SCREEN);
    }

    private void updatePlayingTime(int elapsed, float duration) {
        if(elapsed == playingTime)
            return;
        playingTime = elapsed;
        // Playing time
        int minutes = elapsed / 60;
        int seconds = elapsed % 60;
        s.elapsedView.text().text(String.format(Locale.US, "%d:%02d", minutes, seconds));
        // Remaining time
        elapsed = Math.round(duration) - elapsed;
        minutes = elapsed / 60;
        seconds = elapsed % 60;
        s.durationView.text().text(String.format(Locale.US, "-%d:%02d", minutes, seconds));
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Start if not yet started
        if(!isStarted && renderTime > s.tStartDelay && viewport.camera() != null) {
            isStarted = true;
            // Simulate pressing start (if not selecting)
            if(selectCallback == null && isAutoplay) {
                if (!isPlaying)
                    s.playButton.simulateClick(); // onClick(grid, s.playButton);
                if (video != null && !isFullscreen)
                    onClick(grid, s.fullscreenButton, Input.Buttons.LEFT);
            }
        }

        // Play
        if(video != null) {
            if(videoMesh.getMaterial() != video && video.isLoaded())
                videoMesh.setMaterial(video);
            // Determine current frame
            if(isPlaying) {
                // Delay idle scare
                grid.idleScare.reschedule();


                float effectiveTimeMultiplier = getEffectiveTimeMultiplier();
                if(effectiveTimeMultiplier == 0f) {
                    if(!isTimeMultiplierObserved) {
                        audioTrack.pause();
                        isTimeMultiplierObserved = true;
                    }
                }
                else if(effectiveTimeMultiplier == 1f && isTimeMultiplierObserved) {
                    audioTrack.play();
                    isTimeMultiplierObserved = false;
                }
                else if(!audioTrack.isPlaying()) {
                    // Finished playing
                    isPlaying = false;
                    isForcedPlayback = false;
                    s.playButton.visuals(s.playButtonMesh);
                    // Try next track if can
                    tPlayingTime = 0f;
                    tAudioPlayingTime = 0;
                    audioTrack.setPosition(0);
                    showWindowed(true);
                    // Trigger
                    grid.trigger(Globals.TRIGGER_VIDEO_PLAYBACK_FINISHED);
                    // Subtitle
                    grid.notification.resetSubtitle(audioFilename);
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
                    /*  TODO: doesnt work well with HTC m7
                    tPlayingTime += getRenderDeltaTime();
                    // Sync with audio track playing time
                    float audioPlayingTime = audioTrack.getPosition();
                    float syncDelta = audioPlayingTime - tPlayingTime;
                    if(Math.abs(syncDelta) > SYNC_TOLERANCE) {
                        Sys.info(TAG, "Syncing " + (int)(syncDelta * 1000) + "ms with audio track");
                        tPlayingTime = audioPlayingTime;
                    }
                    */
                }
            }
            // Load frame
            video.show(tPlayingTime, false);
            Globals.grid.notification.updateSubtitles(tPlayingTime, s.videoView.windowAnim != null);

            float progress = tPlayingTime / video.duration;
            if(progress < 0f)
                progress = 0;
            if(progress > 1f)
                progress = 1f;
            // Update timers
            updatePlayingTime(Math.round(tPlayingTime), video.duration);
            // Progress
            if(!s.progressBar.isPressing())
                s.progressBar.progress(progress);
        }
        else if(audioTrack != null) {
            // Determine current frame
            if(isPlaying) {
                float effectiveTimeMultiplier = getEffectiveTimeMultiplier();
                if(effectiveTimeMultiplier == 0f && audioTrack.isPlaying()) {
                    audioTrack.pause();
                    isTimeMultiplierObserved = true;
                }
                else if(effectiveTimeMultiplier == 1f && isTimeMultiplierObserved) {
                    audioTrack.play();
                    isTimeMultiplierObserved = false;
                }
                else if(!audioTrack.isPlaying()) {
                    // Finished playing
                    isPlaying = false;
                    isForcedPlayback = false;
                    s.playButton.visuals(s.playButtonMesh);
                    tPlayingTime = 0f;
                    tAudioPlayingTime = 0;
                    audioTrack.setPosition(0);
                    s.audioLevelAnim.setProgress(0);     // reset
                    // Trigger
                    grid.trigger(Globals.TRIGGER_VIDEO_PLAYBACK_FINISHED);
                    // Subtitle
                    grid.notification.resetSubtitle(audioFilename);
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
                    // Indicate loudness
                    s.audioLevelAnim.setProgress(audioInfo.sample(tPlayingTime));
                }
            }

            // Subtitles
            Globals.grid.notification.updateSubtitles(tPlayingTime, false);     // portrait for recording

            // Determine frame
            float progress = tPlayingTime / audioInfo.duration;
            if(progress > 1f)
                progress = 1f;
            // Update timers
            int elapsed = Math.round(tPlayingTime);
            updatePlayingTime(elapsed, audioInfo.duration);
            // Progress
            if(!s.progressBar.isPressing())
                s.progressBar.progress(progress);
        }
    }

    public void closeToHomescreen(Grid v) {
        fromScreen = null;
        v.homescreen.transitionBack(this, v);
    }

    public void close(Entity<?> target) {
        if(fromScreen == null)
            return;     // not open
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
    }

    public void replaceAudioTrack(String audioFilename) {
        if(audioTrack != null)
            audioTrack.dispose();
        audioTrack = Gdx.audio.newMusic(File.open(audioFilename));
    }

    public void play(Grid v) {
        audioTrack.play();
        s.playButton.visuals(s.pauseButtonMesh);
        isPlaying = true;

        // Pause all music
//                v.musicApp.playerScreen.pauseTrack();
        v.stopAmbiance();

        if(!hasPlayed) {
            hasPlayed = true;
            // Eval trigger
            Media video = album.medias.items[index];
            if(video.trigger != null && !video.trigger.isEmpty())
                v.eval(video.name, video.trigger);
        }
    }

    public void pause() {
        if(audioTrack != null) {
            audioTrack.pause();
            s.playButton.visuals(s.playButtonMesh);
            isPlaying = false;
        }
        showWindowed(true);
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.bars.backButton()) {
            if(!v.trigger(Globals.TRIGGER_BACK_FROM_VIDEO_PLAYER))
                return;     // not allowed
            close(v.screensGroup);
            return;
        }

        if(view == s.bars.homeButton()) {
            if(!v.trigger(Globals.TRIGGER_BACK_FROM_VIDEO_PLAYER))
                return;     // not allowed
            closeToHomescreen(v);
            return;
        }

        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        if(view == s.sendButton) {
            fromScreen = null;
            // Build name for this media and submit to keyboard
            Media video = album.medias.items[index];
            String name = Globals.PHOTOROLL_PREFIX + video.album + "/" + video.name;
            selectCallback.onSelectedMedia(name);
            // Close
            selectCallback.onReturnFromAttachment(this);
            return;
        }


//        if(view == nextTrackButton) {
//            if(!v.trigger(Globals.TRIGGER_VIDEOAPP_PLAYER_NAVIGATE))
//                return;     // not allowed
//            showNextTrack();
//            return;
//        }
//
//        if(view == prevTrackButton && album != null) {
//            if(!v.trigger(Globals.TRIGGER_VIDEOAPP_PLAYER_NAVIGATE))
//                return;     // not allowed
//            showPrevTrack();
//            return;
//        }

        if(view == s.fullscreenButton) {
            toggleFullscreen();
            return;
        }

        if(view == s.fullscreenCancelButton) {
            if(!isForcedPlayback)
                toggleFullscreen();
            return;
        }

        if(view == s.playButton) {
            if(isPlaying) {
                pause();
            }
            else {
                play(v);
            }
        }
    }

    @Override
    public void onReleased(Grid v, UIElement<?> view, float x, float y, int button) {
//        if(view == s.progressBar) {       // Disabled since seeking dont work well anymore
//
//            if(x < 0f)
//                x = 0f;
//            if(x > 1f)
//                x = 1f;
//            float targetTime;
//            float duration = video != null ? video.duration : audioInfo.duration;
//            targetTime = x * duration;
//            if(targetTime > (duration - 1f))            // TODO: hack for Unable to allocate audio buffers. AL Error: 40963
//                targetTime = duration - 1f;
//            targetTime = (int)(targetTime / Globals.s_mediaSeekResolution) * Globals.s_mediaSeekResolution;
//            // Only entertain seek requests if it exceeds minimum seek difference
//            if(Math.abs(targetTime - tPlayingTime) > Globals.s_mediaMinSeekDiff) {
//                tAudioPlayingTime = tPlayingTime = targetTime;
//                if (x == 1f)
//                    audioTrack.stop();
//                else {
//                    if (!audioTrack.isPlaying()) {
//                        audioTrack.play();
//                        audioTrack.setPosition(tPlayingTime);
//                        audioTrack.pause();
//                    } else {
//                        audioTrack.setPosition(tPlayingTime);
//                        if(video != null)
//                            pause();
//                    }
//                    if(video != null)
//                        video.show(tPlayingTime, true);
//                }
//                // Subtitle
//                if(audioFilename != null)
//                    Globals.grid.notification.resetSubtitle(audioFilename);
//            }
//        }
    }
}
