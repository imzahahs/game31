package game31.triggers;

import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.MediaAlbum;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.ScriptState;
import game31.app.chats.WhatsupContact;
import game31.glitch.MpegGlitch;
import game31.renderer.SaraRenderer;
import sengine.Entity;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.NullAnim;
import sengine.animation.SequenceAnim;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.ui.StaticSprite;
import sengine.ui.Toast;
import sengine.ui.UIElement;

public class IntroSequence extends Entity<Grid> implements ScriptState.OnChangeListener<Object> {

    private final StaticSprite bgView;

    private final Graph diffuseFlashGraph;
    private final Graph lsdFlashGraph;
    private final Graph diffuseStartVideoGraph;
    private final Graph lsdStartVideoGraph;
    private final Graph diffuseVideoEndGraph;
    private final Graph lsdVideoEndGraph;

    private final BlissEffect dreamEffect;

    private final MpegGlitch endGlitch;
    private final Toast transitionView;

    private final Toast fadeInView;

    private final Animation backgroundAppearAnim;

    private final Animation chatChangeStartAnim;
    private final Animation chatChangeLoopAnim;

    private int state = 0;
    private float tStateScheduled = 0;

    private boolean hasPlaybackFinished = false;

    public IntroSequence() {
        Sprite sprite = new Sprite(1f / Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        bgView = new StaticSprite()
                .metrics(new UIElement.Metrics().scale(Globals.LENGTH).rotate(-90f))
                .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                ; // .attach();

        dreamEffect = new BlissEffect(
                "sounds/flapee/gloom_1.ogg", true,
                0.33f,
                0,
                0,
                0.5f,
                1.0f,
                new FadeAnim(2f, new QuadraticGraph(0.8f, 0.2f, false)),
                new FadeAnim(0.2f),
                null
        );

        chatChangeStartAnim = new FadeAnim(2f, new CompoundGraph(
                new ConstantGraph(1f, 0.3f),
                new QuadraticGraph(1f, 0.2f, 0.7f, 0, false)
        ));
        chatChangeLoopAnim = new FadeAnim(0.2f);

        diffuseFlashGraph = new CompoundGraph(
                new QuadraticGraph(1f, 0.05f, 0.2f, 0, true),
                new QuadraticGraph(0.05f, 0f, 0.8f, 0, true),
                new QuadraticGraph(0f, 0.05f, 0.8f, 0, false)
        );
        lsdFlashGraph = new QuadraticGraph(0.7f, 0.0f, 0.8f, 0, true);

        diffuseStartVideoGraph = new QuadraticGraph(1f, 0.05f, 0.2f, 0, true);
        lsdStartVideoGraph = new QuadraticGraph(0.4f, 0.2f, 1.3f, 0, true);

        diffuseVideoEndGraph = new QuadraticGraph(1f, 0.05f, 1.2f, 0, true);
        lsdVideoEndGraph = new QuadraticGraph(0.4f, 0.2f, 1.3f, 0, true);

        backgroundAppearAnim = new ColorAnim(14f, LinearGraph.zeroToOne, null);

        sprite = new Sprite(Globals.LENGTH, Material.load("system/square.png.NoiseMaterial"));
        ColorAttribute.of(sprite).set(1, 0, 0, 1);
        transitionView = new Toast()
                .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                .animation(
                        null,
                        null,
                        new SequenceAnim(
                                new NullAnim(0.5f),
                                new FadeAnim(0.12f, ConstantGraph.zero),
                                new NullAnim(0.09f),
                                new FadeAnim(0.06f, ConstantGraph.zero),
                                new NullAnim(0.06f),
                                new FadeAnim(0.10f, ConstantGraph.zero),
                                new NullAnim(0.12f)
                        )
                );

        endGlitch = new MpegGlitch(null, "sounds/glitch_medium.ogg");
        endGlitch.setGlitchGraph(null, false, new CompoundGraph(
                new QuadraticGraph(2.0f, 0.5f, 1.0f, 0, true),
                new ConstantGraph(0f, 0.5f)
        ));
        endGlitch.setLsdEffect(0.5f, 0.25f);

        sprite = new Sprite(Sys.system.getLength(), SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        fadeInView = new Toast()
                .animation(null, null, new FadeAnim(0.2f, LinearGraph.oneToZero))
                .visual(sprite, SaraRenderer.TARGET_SCREEN)
                ;
    }

    @Override
    protected void recreate(Grid v) {
        // Prepare video player
        Media media = v.photoRollApp.unlock("Videos/trailer_draft5A.vidx", true);
        MediaAlbum album = v.photoRollApp.findAlbum(media.album);
        int mediaIndex = album.indexOf(media);

        v.photoRollApp.videoScreen.show(album, mediaIndex, null, false);   // TODO: TESTING
        if (!ACT1.a1_allow_skip_introvid)
            v.photoRollApp.videoScreen.setForcedPlayback(true);
//        v.photoRollApp.videoScreen.replaceAudioTrack("content/videos/flapeebird-trailer-reverb.ogg");

        v.screensGroup.detachChilds();

        v.photoRollApp.videoScreen.attach(v.screensGroup);

        // Prepare intro chats
        v.whatsupApp.pack(v.state);
        v.whatsupApp.load(Globals.introChatsConfigFilename, v.state);

        // Hide everything
        bgView.viewport(v.photoRollApp.videoScreen.viewport).attach();
        v.inputEnabled = false;

        v.state.addOnChangeListener("intro.", Object.class, this);
    }

    @Override
    protected void release(Grid v) {
        v.state.removeOnChangeListener(this);
    }

    @Override
    protected void render(final Grid v, float r, final float renderTime) {

        switch (state) {
            case 0:
                if(renderTime > 6f) {
                    // Fullscreen
                    v.photoRollApp.videoScreen.showFullscreen(false, true);
                    v.photoRollApp.videoScreen.finishWindowAnim();

                    // Show intro
                    v.photoRollApp.videoScreen.play(v);
                    v.inputEnabled = true;

                    // Show everything
                    bgView.detachWithAnim();

                    v.addTrigger(Globals.TRIGGER_VIDEO_PLAYBACK_FINISHED, new Grid.Trigger() {
                        @Override
                        public boolean trigger(String name) {
                            onPlaybackFinished();
                            return true;
                        }
                    });

                    // On back from video player, proceed straight to Teddy's chat
                    v.addTrigger(Globals.TRIGGER_BACK_FROM_VIDEO_PLAYER, new Grid.Trigger() {
                        @Override
                        public boolean trigger(String name) {
                            v.removeTrigger(name);

                            onPlaybackFinished();

                            // Configure returning to Teddy's chat
                            WhatsupContact contact = v.whatsupApp.getContact(1);
                            contact.readMessages = 1000;
                            v.whatsupApp.threadScreen.open(contact);

                            v.state.set("chats.introteddy1.intro_started", true);

                            // Force go back to threadscreen
                            ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(v.photoRollApp.videoScreen, v.whatsupApp.threadScreen, v.screensGroup);
                            transition.attach(v.screensGroup);
                            return false;
                        }
                    });

                    v.addTrigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN, new Grid.Trigger() {
                        @Override
                        public boolean trigger(String name) {
                            return false;       // not allowed till sequence ends
                        }
                    });

                    state++;
                    tStateScheduled = renderTime + 57.5f;
                }
                break;

            case 1:
                if(renderTime > tStateScheduled) {
                    dreamEffect.resumeAmbient();

                    state++;
                }
                break;
        }

        if(v.timeMultiplier != 1f && !v.photoRollApp.videoScreen.isInFullscreen())
            v.timeMultiplier = 1f;
    }

    private void onPlaybackFinished() {
        if(hasPlaybackFinished)
            return;
        hasPlaybackFinished = true;

        Globals.grid.removeTrigger(Globals.TRIGGER_VIDEO_PLAYBACK_FINISHED);

        Globals.grid.screen.animateBackground(backgroundAppearAnim, null);

        // Resume lsd effect
        dreamEffect.animateEffect(diffuseVideoEndGraph, lsdVideoEndGraph);
        dreamEffect.resetScreenOverlayAnim(true);
        dreamEffect.resumeAmbient();
        dreamEffect.attach(Globals.grid);

        Globals.grid.timeMultiplier = 0.3f;

        // Fade in
        fadeInView.viewport(Globals.grid.screen.viewport).attach();
    }

    @Override
    public void onChanged(String name, Object var, Object prev) {
        final Grid v = Globals.grid;

        if(name.equals("intro.teddy1")) {
            // Change to another teddy
            v.scheduleRunnable(new Runnable() {
                @Override
                public void run() {
                    // Reset time
                    v.setSystemTime(Globals.gameTimeOffset);

                    WhatsupContact contact = v.whatsupApp.getContact(2);
                    contact.readMessages = 1000;
                    v.whatsupApp.threadScreen.open(contact);

                    v.state.set("chats.introteddy2.intro_started", true);

                    transitionView.viewport(v.whatsupApp.threadScreen.viewport).attach();
                    endGlitch.attach(v);
                    endGlitch.detachWithAnim();
                    dreamEffect.animateEffect(diffuseVideoEndGraph, lsdVideoEndGraph);
                    dreamEffect.setAmbientVolume(0.66f);
                    v.screen.animateOverlay(chatChangeStartAnim, chatChangeLoopAnim);
                }
            }, 3f);
        }
        else if(name.equals("intro.teddy2")) {
            // Change to another teddy
            v.scheduleRunnable(new Runnable() {
                @Override
                public void run() {
                    // Reset time
                    v.setSystemTime(Globals.gameTimeOffset);

                    // Change to another teddy
                    WhatsupContact contact = v.whatsupApp.getContact(0);
                    contact.readMessages = 1000;
                    v.whatsupApp.threadScreen.open(contact);

                    v.state.set("chats.introteddy3.intro_started", true);

                    transitionView.viewport(v.whatsupApp.threadScreen.viewport).attach();
                    endGlitch.attach(v);
                    endGlitch.detachWithAnim();
                    dreamEffect.animateEffect(diffuseVideoEndGraph, lsdVideoEndGraph);
                    dreamEffect.setAmbientVolume(1f);
                    v.screen.animateOverlay(chatChangeStartAnim, chatChangeLoopAnim);
                }
            }, 3f);
        }
        else if(name.equals("intro.teddy3")) {
            // Change to another teddy
            v.scheduleRunnable(new Runnable() {
                @Override
                public void run() {
                    // Reset time
                    v.setSystemTime(Globals.gameTimeOffset);

                    // Load original
                    v.whatsupApp.pack(v.state);
                    v.whatsupApp.load(Globals.chatsConfigFilename, v.state);

                    // Change to another teddy
                    WhatsupContact contact = v.whatsupApp.findContact("Teddy");
                    contact.readMessages = 1000;
                    v.whatsupApp.threadScreen.open(contact);

                    v.state.set("chats.teddy.a1_started", true);

                    transitionView.viewport(v.whatsupApp.threadScreen.viewport).attach();
                    endGlitch.attach(v);
                    endGlitch.detachWithAnim();

                    // Remove hooks
                    v.removeTrigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN);

                    // Remove effects
                    dreamEffect.detachWithAnim();

                    // Stopped
                    detach();
                }
            }, 3f);
        }
        // Else UB

    }
}
