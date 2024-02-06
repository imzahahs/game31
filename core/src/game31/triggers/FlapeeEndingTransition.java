package game31.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BooleanArray;

import game31.Globals;
import game31.Grid;
import game31.VoiceProfile;
import game31.glitch.MpegGlitch;
import game31.renderer.SaraRenderer;
import game31.renderer.ScreenMaterial;
import sengine.Entity;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.MoveAnim;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 2/7/2016.
 */
public class FlapeeEndingTransition extends Entity<Grid> {

    public final Entity<?> from;
    public final Entity<?> to;
    public final Entity<?> target;

    private final MpegGlitch glitch;

    private final Animation bgAnim;

    private final Sprite screen;
    private final Array<Animation.Instance> screenAnims = new Array<>(Animation.Instance.class);
    private final Array<Animation.Instance> screenBgAnims = new Array<>(Animation.Instance.class);
    private final BooleanArray screenType = new BooleanArray();

    private final Animation screenAnim;

    private final Sprite toScreen;
    private final Animation.Instance toScreenAnim;

    private final Graph tSwapIntervalGraph;
    private final Graph swapToChance;
    private final float tSwapRandomStartTime;

    private final Audio.Sound swapSound;

    private final Music voiceTrack;
    private final VoiceProfile voiceProfile;

    private final Graph swapSoundGraph;

    private float tStartVoiceScheduled;
    private float time;

    private float tSwapInterval;
    private float tSwapScheduled = -1;
    private float tLastSwap;


    private Runnable onFinished;

    private boolean rendereredScreens = false;


    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public FlapeeEndingTransition(Entity<?> from, Entity<?> to, Entity<?> target, float tStartDelay, float tEndDelay, String voiceFilename) {
        this.from = from;
        this.to = to;
        this.target = target;

        // Glitch
        glitch = new MpegGlitch(null, null);
        glitch.setGlitchGraph(
                new CompoundGraph(new Graph[]{
                        new ConstantGraph(0.0f, 1.5f),
                        new ConstantGraph(2.8f, 0.3f),
                        new ConstantGraph(0.0f, 1.2f),
                        new ConstantGraph(2.8f, 0.2f),
                        new ConstantGraph(0.0f, 1.5f),
                        new ConstantGraph(2.8f, 0.1f),
                        new ConstantGraph(0.0f, 1.8f),
                        new ConstantGraph(2.8f, 0.4f),
                        new ConstantGraph(0.0f, 1.4f),
                        new ConstantGraph(2.8f, 0.2f),
                        new ConstantGraph(0.0f, 1.6f),
                        new ConstantGraph(2.8f, 0.2f),
                        new ConstantGraph(0.0f, 1.6f),
                        new ConstantGraph(2.8f, 0.4f),
                        new ConstantGraph(0.0f, 1.1f),
                        new ConstantGraph(2.8f, 0.15f),
                        new ConstantGraph(0.0f, 1.3f),
                        new ConstantGraph(2.8f, 0.2f),
                        new ConstantGraph(0.0f, 1.2f),
                        new ConstantGraph(2.4f, 1.4f),
                        new ConstantGraph(0.0f, 1.8f)
                }),
                false,
                new LinearGraph(1.0f, 0f, 0.25f)
        );

        this.screen = new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST));

        this.bgAnim = new ColorAnim(0.45f, new QuadraticGraph(1f, 0.35f, 1f, 0f, true), ConstantGraph.one);
        screenAnim = new MoveAnim(0.45f, new QuadraticGraph(1f, 0f, true), null);

        this.toScreen = new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND));
        toScreenAnim = screenAnim.start();


        tSwapIntervalGraph = new CompoundGraph(
                new LinearGraph(2f, 1f, 0.1f),
                new LinearGraph(1.0f, 0.05f, 0.5f),
                new ConstantGraph(0.05f, 0.4f)
//                new LinearGraph(0.05f, 1.0f, 0.1f)
        );

        swapToChance = new QuadraticGraph(0.33f, 0.6f, false);

        tSwapRandomStartTime = 8f;


        swapSound = Sound.load("sounds/homescreen_openapp.ogg");

        voiceTrack = Gdx.audio.newMusic(File.open(voiceFilename));
        voiceProfile = VoiceProfile.load(voiceFilename);

        swapSoundGraph = new CompoundGraph(
                new LinearGraph(1f, 0.7f, 0.2f),
                new LinearGraph(0.7f, 0.5f, 0.4f),
                new ConstantGraph(0.5f, 0.3f),
                new LinearGraph(0.5f, 0.7f, 0.1f)
        );

        tStartVoiceScheduled = tStartDelay;

        time = tStartDelay + voiceProfile.duration + tEndDelay;

        to.attach(this);

        from.inputEnabled = false;
        to.inputEnabled = false;
        to.timeMultiplier = 0;

    }

    @Override
    protected void recreate(Grid v) {
        // Start glitch
        glitch.attach(v);

        // First frame, add from screen animated into bg
        screenAnims.add(screenAnim.start());
        screenBgAnims.add(bgAnim.startAndReset());
        screenType.add(false);
        // Now for to screen
        screenAnims.add(screenAnim.startAndReset());
        screenBgAnims.add(bgAnim.start());
        screenType.add(true);

        // Sound
        swapSound.play(swapSoundGraph.getStart());

        // Schedule next swap
        tSwapInterval = tSwapIntervalGraph.getStart();
        tSwapScheduled = tSwapInterval;

        // Stop all demon voice
        v.flapeeBirdApp.stopDemonVoice();
    }

    @Override
    protected void release(Grid v) {
        glitch.detachWithAnim();

        if(onFinished != null)
            onFinished.run();

        // Cleanup
        voiceTrack.dispose();

        // Stop subtitles
        v.notification.stopSubtitle(voiceProfile.filename);
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        Matrices.push();
        Matrices.camera = v.compositor.camera;

        // Request max framerate
        Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);
        v.idleScare.reschedule();

        if(!rendereredScreens) {
            SaraRenderer.renderer.startSecondBuffer();
            Sys.system.streamingDisabledThisFrame = true;       // disable streaming, we are capturing fully loaded screens
            // Stop from screen, not needed anymore
            from.detach();
        }
        else
            SaraRenderer.renderer.clearBufferedRenderCalls();       // Clear 1st buffer
    }


    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        if(!rendereredScreens) {
            rendereredScreens = true;
            // Hide quick access
            v.notification.hideAccessView();
            // Stop
            SaraRenderer.renderer.stopSecondBuffer();
        }
        else
            SaraRenderer.renderer.clearBufferedRenderCalls();       // Clear 2nd buffer



        Matrix4 m = Matrices.model;
        Matrices.push();

        m.translate(+0.5f, +Globals.LENGTH / 2f, 0);
        m.scale(1f, -1f, 1f);

        Matrices.target = SaraRenderer.TARGET_TRANSITION;


        // Timing
        if(renderTime > tStartVoiceScheduled) {
            tStartVoiceScheduled = Float.MAX_VALUE;

            voiceTrack.play();

            // Start subtitle
            v.notification.startSubtitle(voiceProfile.filename, voiceTrack);
        }

        float progress = renderTime / time;
        if(renderTime < time) {
            tSwapInterval = tSwapIntervalGraph.generate(progress);
        }
        else if(toScreenAnim.getProgress() == -1 && (renderTime - tLastSwap) > tSwapIntervalGraph.getEnd()) {
            tSwapInterval = Float.MAX_VALUE;
            // Check if finishing
            if(screenAnims.size > 0)
                screenBgAnims.peek().reset();       // start end anim of last
            // Start last anim
            toScreenAnim.reset();
            // Sound
            swapSound.play(swapSoundGraph.getEnd());
        }

        if(renderTime > tSwapScheduled) {
            screenAnims.add(screenAnim.startAndReset());
            for(int c = 0; c < screenBgAnims.size; c++) {
                Animation.Instance anim = screenBgAnims.items[c];
                if(anim.getProgress() == -1)
                    anim.reset();
            }
            screenBgAnims.add(bgAnim.start());

            if(renderTime > tSwapRandomStartTime)
                screenType.add(Math.random() <= swapToChance.generate(progress));
            else
                screenType.add(!screenType.peek());

            // Sound
            swapSound.play(swapSoundGraph.generate(progress));

            // Schedule next
            tSwapScheduled = renderTime + tSwapInterval;
            tLastSwap = renderTime;
        }

        // Screens
        for(int c = 0; c < screenAnims.size; c++) {
            Animation.Instance appearAnim = screenAnims.items[c];
            Animation.Instance bgAnim = screenBgAnims.items[c];

            Matrices.push();

            Sprite screen = screenType.items[c] ? toScreen : this.screen;

            boolean isBgAnimActive = true;
            if(bgAnim.getProgress() != -1)
                isBgAnimActive = bgAnim.updateAndApply(screen, getRenderDeltaTime());
            boolean isAppearAnimActive = appearAnim.updateAndApply(screen, getRenderDeltaTime());

            screen.render();

            Matrices.pop();

            // Cleanup stack
            if(!isBgAnimActive && !isAppearAnimActive) {
                screenAnims.removeIndex(c);
                screenBgAnims.removeIndex(c);
                screenType.removeIndex(c);
                c--;
            }
        }

        if(toScreenAnim.getProgress() != -1) {
            Matrices.push();

            if(!toScreenAnim.updateAndApply(toScreen, getRenderDeltaTime())) {
                // Finished
                to.attach(target);
                to.inputEnabled = true;
                from.inputEnabled = true;
                to.timeMultiplier = 1f;
                detach();
            }
            toScreen.render();

            Matrices.pop();
        }

        Matrices.pop();

        Matrices.pop();     // from render();
    }

}
