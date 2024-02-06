package game31.triggers;

import game31.Grid;
import sengine.Entity;
import sengine.audio.Audio;

public class MusicFadeInTime extends Entity<Grid> {
    private final float tFadeInTime;
    private final float maxVolume;

    public MusicFadeInTime(float tFadeInTime, float maxVolume) {
        this.tFadeInTime = tFadeInTime;
        this.maxVolume = maxVolume;
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        float volume = renderTime / tFadeInTime;
        if(volume >= 1f) {
            Audio.setMusicVolume(maxVolume);
            detach();
        }
        else
            Audio.setMusicVolume(volume * maxVolume);
    }
}
