package game31;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import game31.gb.GBSimTrailerMenu;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.materials.VideoMaterial;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.StaticSprite;
import sengine.ui.UIElement;
import sengine.utils.Builder;

public class SimTrailerMenu extends Menu<Grid> implements OnClick<Grid> {

    private static final String OGG_EXTENSION = ".ogg";


    public static class Internal {
        public UIElement.Group window;

        public StaticSprite videoView;

        public StaticSprite skipView;
        public Clickable tapView;

        public String trailerPath;

        public UIElement.Group infoGroup;
        public Clickable closeButton;
        public Clickable buyButton;

    }


    public interface BuilderSource {
        Animation createFullscreenAnim(Sprite videoMesh);
    }



    private final Builder<BuilderSource> builder;
    private Internal s;

    private VideoMaterial video = null;
    private Music audioTrack = null;
    private Sprite videoMesh = null;
    private float tPlayingTime = -1;
    private float tAudioPlayingTime = -1;

    private boolean hasFinishedVideo = false;

    private Runnable onClose;

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
    }



    private void clear() {
        if(video != null)
            video.clear();
        if(audioTrack != null)
            audioTrack.dispose();
        video = null;
        audioTrack = null;
        tPlayingTime = 0;
        tAudioPlayingTime = 0;
        videoMesh = null;
        s.videoView.visual(null);
    }


    public SimTrailerMenu() {
        // Initialize
        builder = new Builder<BuilderSource>(GBSimTrailerMenu.class, this);
        builder.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        if(grid.idleScare != null)
            grid.idleScare.stop();

        clear();

        s.videoView.attach();
        s.infoGroup.detach();

        // Open video file
        video = Material.load(s.trailerPath + ".VideoMaterial");
        if(Globals.r_highQuality)
            video.filenameOverride = null;
        else
            video.filenameOverride = video.filename + ".low";
        video.ensureLoaded();

        // Audio
        String audioFilename = video.filename + OGG_EXTENSION;
        audioTrack = Gdx.audio.newMusic(File.open(audioFilename));

        // Update video view
        videoMesh = new Sprite(video);
        s.videoView.visual(videoMesh);

        // Play
        audioTrack.play();
        s.videoView.attach();

        if(videoMesh.length < 1f) {
            // All videos are assumed to be landscape for now
            Animation startAnim = builder.build().createFullscreenAnim(videoMesh);
            s.videoView.windowAnimation(startAnim.startAndReset(), true, true);
            s.videoView.windowAnim.setProgress(1f);     // start
            Globals.grid.screen.enterFullscreen(true);
        }
        else
            s.videoView.windowAnimation(null, false, false);

        // Reset
        hasFinishedVideo = false;

        // Analytics
        Game.analyticsView(Globals.ANALYTICS_CONTENT_MAIN_MENU_SIMPROMO, Globals.ANALYTICS_CONTENT_TYPE_MAIN_MENU);
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        clear();

    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(hasFinishedVideo)
            return;

        // Play
        // Determine current frame
        if(!audioTrack.isPlaying()) {
            // Finished
            showInfoMenu();
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
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.tapView && !hasFinishedVideo) {
            // Close video now
            showInfoMenu();
            return;
        }

        if(view == s.buyButton) {
            // Analytics
            Game.analyticsEvent(Globals.ANALYTICS_EVENT_SIMPROMO);

            Game.game.platform.openSimulacraAppPage();
            return;
        }

        if(view == s.closeButton) {
            // Go back to main menu
            if(onClose != null)
                onClose.run();
            return;
        }
    }


    private void showInfoMenu() {
        hasFinishedVideo = true;
        s.videoView.detachWithAnim();
        s.infoGroup.attach();

        audioTrack.stop();

        // Exit fullscreen if applicable
        if(s.videoView.windowAnim != null)
            Globals.grid.screen.exitFullscreen();
    }
}
