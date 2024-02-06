package game31.glitch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import game31.Grid;
import game31.renderer.ScreenShampainMaterial;
import sengine.Entity;
import sengine.File;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.audio.Stream;
import sengine.calc.Graph;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 5/15/2017.
 */

public class VhsGlitch extends Entity<Grid> {

    private final Sprite screen;
    private final ScreenShampainMaterial material;

    private final Audio.Sound startSound;
    private final Music music;

    private long timeStart = Long.MIN_VALUE;
    private long timeEnd = Long.MIN_VALUE;
    private float timeSpeed = 0;

    private long savedTime = Long.MIN_VALUE;

    private float tInputBlockTime = -1;

    private Stream startSoundStream = null;
    private Graph glitchGraph = null;
    private boolean glitchGraphLoop = false;
    private Graph glitchEndGraph = null;
    private float tGlitchEndStarted = -1;

    private Animation screenBgAnim;

    public void setScreenBgAnim(Animation anim) {
        screenBgAnim = anim;
    }

    public void setInputBlockTime(float tInputBlockTime) {
        this.tInputBlockTime = tInputBlockTime;
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

    public void pauseMusic() {
        if(music != null) {
            music.pause();
        }
    }

    public void resumeMusic() {
        if(music != null) {
            music.play();
        }
    }

    public void stopMusic() {
        if(music != null) {
            music.stop();
            music.dispose();
        }
        if(startSoundStream != null) {
            startSoundStream.stop();
            startSoundStream = null;
        }
    }

    public VhsGlitch(String startSoundName, String loopSoundName) {
        material = new ScreenShampainMaterial();
        screen = new Sprite(material);

        startSound = startSoundName != null ? Sound.load(startSoundName) : null;
        if(loopSoundName != null)
            music = Gdx.audio.newMusic(File.open(loopSoundName));
        else
            music = null;
    }

    @Override
    protected void recreate(Grid v) {
        if(startSound != null)
            startSoundStream = startSound.play();
        if(music != null) {
            music.setLooping(true);
            music.play();
        }
        if(screenBgAnim != null) {
            v.screen.animateBackground(screenBgAnim, null);
        }

        savedTime = v.getSystemTime();

        // Hide quick access
        v.notification.hideNow();

        inputEnabled = true;
    }

    @Override
    protected void release(Grid v) {
        super.release(v);

        v.screen.screen = v.screen.defaultScreen;

        stopMusic();

        if(timeStart != Long.MIN_VALUE)
            v.setSystemTime(savedTime);

        if(screenBgAnim != null)
            v.screen.animateBackground(null, null);
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        v.screen.screen = screen;

        if(timeStart != Long.MIN_VALUE) {
            long elapsed = Math.round(renderTime * timeSpeed) * 1000;
            long duration = timeEnd - timeStart;
            elapsed %= duration;
            v.setSystemTime(timeStart + elapsed);
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
                material.power = glitchGraph.generate(renderTime);
        }
    }

    public void detachWithAnim() {
        if(glitchEndGraph != null)
            tGlitchEndStarted = getRenderTime();
        else
            detach();

    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {

        return true;            // absorb all kinds
    }
}
