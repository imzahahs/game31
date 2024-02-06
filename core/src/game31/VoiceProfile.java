package game31;

import sengine.File;
import sengine.calc.QuadraticGraph;
import sengine.mass.MassSerializable;

/**
 * Created by Azmi on 8/25/2017.
 */

public class VoiceProfile implements MassSerializable {
    static final String TAG = "VoiceProfile";

    public static final String HINTS_EXTENSION = ".VoiceProfile";

    public static VoiceProfile load(String path) {
        String hintsName = path + HINTS_EXTENSION;
        VoiceProfile voice = File.getHints(hintsName, false);
        if(voice == null || Globals.recompileVoiceProfiles) {
            voice = Game.game.platform.createTextVoice(path);
            File.saveHints(hintsName, voice);
        }
        return voice;
    }

    // Identity
    public final String filename;
    public final byte[] samples;
    public final float duration;

    @MassConstructor
    public VoiceProfile(String filename, byte[] samples, float duration) {
        this.filename = filename;
        this.samples = samples;
        this.duration = duration;
    }

    @Override
    public Object[] mass() {
        return new Object[] { filename, samples, duration };
    }


    public float sample(float position) {
        float progress = position / duration;
        if(progress > 1f)
            progress = 1f;
        else if(progress < 0f)
            progress = 0f;
        // Sample
        progress *= samples.length - 1;
        int start = (int)progress;
        float sample1 = (float)(samples[start] & 0xFF) / 255f;
        if(start == (samples.length - 1))
            return sample1;     // precisely at the end
        // Else interpolate
        float sample2 = (float)(samples[start + 1] & 0xFF) / 255f;
        progress %= 1;
        if(sample2 > sample1)
            progress = QuadraticGraph.zeroToOneInverted.generate(progress);
        else
            progress = QuadraticGraph.zeroToOne.generate(progress);
        return sample1 + ((sample2 - sample1) * progress);
    }

}
