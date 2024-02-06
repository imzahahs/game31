package game31.triggers;

import game31.Grid;
import sengine.Entity;
import sengine.audio.Audio;

public class MusicFadeOutEntity extends Entity<Grid> {
    private final float tFadeOutTime;
    private final float tStartDelay;

    private float startVolume = 0f;

    public MusicFadeOutEntity(float tStartDelay, float tFadeOutTime) {
        this.tStartDelay = tStartDelay;
        this.tFadeOutTime = tFadeOutTime;
    }

    @Override
    protected void recreate(Grid v) {
        if(Audio.musicVolume > 0f)
            startVolume = Audio.getMusicVolume() / Audio.musicVolume;
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        renderTime -= tStartDelay;
        if(renderTime < 0)
            return;     // not yet time
        float volume = (1f - (renderTime / tFadeOutTime)) * startVolume;
        if(volume <= 0f) {
            Audio.stopMusic();
            detach();
        }
        else
            Audio.setMusicVolume(volume);
    }
}
