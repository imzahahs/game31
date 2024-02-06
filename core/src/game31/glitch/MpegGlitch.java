package game31.glitch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import game31.Globals;
import game31.Grid;
import game31.renderer.ScreenMpegMaterial;
import sengine.Entity;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.audio.Stream;
import sengine.calc.Graph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 5/15/2017.
 */

public class MpegGlitch extends Entity<Grid> {
    private static final String TAG = "MpegGlitch";

    private final Sprite screen;
    private final ScreenMpegMaterial material;

    private final Audio.Sound startSound;
    private final String loopSoundName;
    private Music music = null;
    private float tLastMusicPosition;

    private long timeStart = Long.MIN_VALUE;
    private long timeEnd = Long.MIN_VALUE;
    private float timeSpeed = 0;

    private long savedTime = Long.MIN_VALUE;

    private float tInputBlockTime = -1;

    private float tDetachScheduled = Float.MAX_VALUE;

    private Stream startSoundStream = null;
    private Graph glitchGraph = null;
    private boolean glitchGraphLoop = false;
    private Graph glitchEndGraph = null;
    private float tGlitchEndStarted = -1;

    private float glitchLoopThreshold = -1;
    private boolean glitchLoopStopsPlayback = false;

    private float lsdThreshold = Float.MAX_VALUE;
    private float lsdFadeTime = 0f;

    private float tLsdFadeStarted = -1;

    private float renderTime = 0;

    private Runnable onFinished = null;

    private Animation screenBgAnim;

    private int shaderStatus = 0;       // 1 for success, -1 for failed

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void setLsdEffect(float threshold, float fadeTime) {
        this.lsdThreshold = threshold;
        this.lsdFadeTime = fadeTime;
    }

    public void setGlitchLoopThreshold(float glitchLoopThreshold) {
        this.glitchLoopThreshold = glitchLoopThreshold;
    }

    public void setGlitchLoopStopsPlayback(boolean glitchLoopStopsPlayback) {
        this.glitchLoopStopsPlayback = glitchLoopStopsPlayback;
    }

    public void setScreenBgAnim(Animation anim) {
        screenBgAnim = anim;
    }

    public void setInputBlockTime(float tInputBlockTime) {
        this.tInputBlockTime = tInputBlockTime;
    }

    public void setDetachScheduled(float tDelay) {
        this.tDetachScheduled = tDelay;
    }

    public void setTimeGlitch(long start, long end, float speed) {
        timeStart = start;
        timeEnd = end;
        timeSpeed = speed;
    }

    public void setGlitchGraph(Graph glitchGraph, boolean loop, Graph glitchEndGraph) {
        this.glitchGraph = glitchGraph;
        this.glitchGraphLoop = loop;
        this.glitchEndGraph = glitchEndGraph;
    }

    public void pause() {
        if(music != null)
            music.pause();
        renderingEnabled = false;
        Globals.grid.screen.screen = Globals.grid.screen.defaultScreen;
        Globals.grid.screensGroup.timeMultiplier = 1f;
    }

    public void resume() {
        renderingEnabled = true;
    }

    public void stopMusic() {
        if(music != null) {
            music.stop();
            music.dispose();
            music = null;
        }
        if(startSoundStream != null) {
            startSoundStream.stop();
            startSoundStream = null;
        }
    }

    public MpegGlitch(String startSoundName, String loopSoundName) {
        material = new ScreenMpegMaterial();
        screen = new Sprite(material);

        startSound = startSoundName != null ? Sound.load(startSoundName) : null;
        this.loopSoundName = loopSoundName;

    }

    @Override
    protected void recreate(Grid v) {
        if(startSound != null)
            startSoundStream = startSound.play();
        if(screenBgAnim != null) {
            v.screen.animateBackground(screenBgAnim, null);
        }

        savedTime = v.getSystemTime();

        // Hide quick access
        v.notification.hideNow();

        if(music == null) {
            if(loopSoundName != null) {
                music = Gdx.audio.newMusic(File.open(loopSoundName));
                music.setLooping(true);
                music.play();
                music.setPosition(tLastMusicPosition);
            }
            else
                music = null;
        }

        inputEnabled = true;

        tGlitchEndStarted = -1;
        renderTime = 0;
    }

    @Override
    protected void release(Grid v) {
        super.release(v);

        v.screen.screen = v.screen.defaultScreen;
        v.screensGroup.timeMultiplier = 1f;

        if(music != null)
            tLastMusicPosition = music.getPosition();
        stopMusic();

        if(timeStart != Long.MIN_VALUE)
            v.setSystemTime(savedTime);

        if(screenBgAnim != null)
            v.screen.animateBackground(null, null);

        if(onFinished != null)
            onFinished.run();
    }

    @Override
    protected void render(Grid v, float r, float entityRenderTime) {
        renderTime += v.getRenderDeltaTime();

        // TODO: remove this once a workaround for GE8100 gpus have been found
        if(shaderStatus == 0) {
            try {
                // Test loading material
                material.shader.load();
                v.screen.screen = screen;
                shaderStatus = +1;
            } catch (Throwable e) {
                Sys.error(TAG, "Failed to load screen material shader", e);
                shaderStatus = -1;
            }
        }
        else if(shaderStatus == +1)
            v.screen.screen = screen;

        if(timeStart != Long.MIN_VALUE) {
            long elapsed = Math.round(renderTime * timeSpeed) * 1000;
            long duration = timeEnd - timeStart;
            elapsed %= duration;
            v.setSystemTime(timeStart + elapsed);
        }

        if(renderTime > tDetachScheduled && tGlitchEndStarted == -1) {
            detachWithAnim();
        }

        if(inputEnabled && renderTime > tInputBlockTime) {
            inputEnabled = false;
        }

        if(tGlitchEndStarted != -1) {
            float elapsed = renderTime - tGlitchEndStarted;
            if(music != null) {
                float volume = 1f - (elapsed / glitchEndGraph.getLength());
                if(volume < 0f)
                    volume = 0f;
                music.setVolume(volume);
            }
            if(elapsed > glitchEndGraph.getLength()) {
                material.power = glitchEndGraph.getEnd();
                detach();
            }
            else
                material.power = glitchEndGraph.generate(elapsed);

        }
        else if(glitchGraph != null) {
            if(!glitchGraphLoop && renderTime >= glitchGraph.getLength())
                material.power = glitchGraph.getEnd();
            else
                material.power = glitchGraph.generate(glitchGraphLoop ? Sys.getTime() : renderTime);
        }

        // Loop threshold
        if(music != null) {
            if (material.power > glitchLoopThreshold) {
                if (!music.isPlaying())
                    music.play();
                if (glitchLoopStopsPlayback)
                    v.screensGroup.timeMultiplier = 0f;
            } else {
                if (music.isPlaying())
                    music.pause();
                if (glitchLoopStopsPlayback)
                    v.screensGroup.timeMultiplier = 1f;
            }
        }

        // Lsd
        if(material.power > lsdThreshold) {
            material.lsdMix = 1f;
            tLsdFadeStarted = renderTime;
        }
        else if(tLsdFadeStarted != -1) {
            float elapsed = renderTime - tLsdFadeStarted;
            if(elapsed >= lsdFadeTime) {
                material.lsdMix = 0f;
                tLsdFadeStarted = -1;
            }
            else
                material.lsdMix = QuadraticGraph.oneToZeroInverted.generate(elapsed / lsdFadeTime);
        }
    }

    public void detachWithAnim() {
        if(glitchEndGraph != null) {
            if(tGlitchEndStarted == -1)
                tGlitchEndStarted = renderTime;
        }
        else
            detach();

    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {

        return true;            // absorb all kinds
    }
}
