package game31.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import game31.Grid;
import sengine.Entity;
import sengine.File;
import sengine.calc.QuadraticGraph;

public class MusicHandler extends Entity<Grid> {

    private final Music music;
    private final float tFadeInTime;
    private final float tFadeOutTime;

    private final float volume;

    private boolean hasFinishedFadeIn = false;
    private float tDetachStarted = -1;

    public MusicHandler(String music, float tFadeInTime, float tFadeOutTime, float volume) {
        this(music, tFadeInTime, tFadeOutTime, volume, true);
    }

    public MusicHandler(String music, float tFadeInTime, float tFadeOutTime, float volume, boolean loop) {
        this.music = Gdx.audio.newMusic(File.open(music));
        this.music.setLooping(loop);

        this.tFadeInTime = tFadeInTime;
        this.tFadeOutTime = tFadeOutTime;

        this.volume = volume;
    }

    @Override
    protected void recreate(Grid v) {
        if(tFadeInTime > 0f)
            music.setVolume(0);
        else {
            music.setVolume(volume);
            hasFinishedFadeIn = true;
        }
        music.play();

    }

    @Override
    protected void release(Grid v) {
        music.stop();
        music.dispose();

        detach();       // Make sure detached
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        if(!hasFinishedFadeIn) {
            float progress = renderTime / tFadeInTime;
            if(progress >= 1f) {
                music.setVolume(volume);
                hasFinishedFadeIn = true;
            }
            else
                music.setVolume(QuadraticGraph.zeroToOne.generate(progress) * volume);
        }

        if(tDetachStarted != -1) {
            float progress = (renderTime - tDetachStarted) / tFadeOutTime;
            if(progress >= 1f) {
                music.setVolume(0f);
                detach();
            }
            else
                music.setVolume(QuadraticGraph.oneToZeroInverted.generate(progress) * volume);
        }

        if(!music.isPlaying())
            detach();
    }

    public void detachWithAnim() {
        if(tFadeOutTime <= 0)
            detach();
        else if(tDetachStarted == -1)
            tDetachStarted = getRenderTime();
    }
}
