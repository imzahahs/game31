package game31.app.flapee;

import game31.Globals;
import sengine.calc.SetSelector;

public class DemonVoice {

    public static DemonVoice select(SetSelector<DemonVoice> chatter) {
        if(chatter == null)
            return null;
        // Check if any of the chatter has not been played yet
        boolean hasUnplayedVoice = false;
        boolean hasRepeatable = false;
        for(int c = 0; c < chatter.set.length; c++) {
            DemonVoice voice = chatter.set[c];
            if(!voice.hasPlayed()) {
                hasUnplayedVoice = true;
                break;
            }
            // Else check if can atleast repeat
            if(!voice.doNotRepeat)
                hasRepeatable = true;
        }
        if(!hasUnplayedVoice && !hasRepeatable)
            return null;     // Nothing can be played from this set
        // Else there could be at least one that can be played
        if(hasUnplayedVoice) {
            // Find for an unplayed voice
            DemonVoice voice;
            while((voice = chatter.select()).hasPlayed());       // Skip played voices until found an unplayed one
            return voice;
        }
        else {
            // Else there are no unplayed voices, find a voice that can be repeated
            DemonVoice voice;
            while((voice = chatter.select()).doNotRepeat);     // Skip unrepeatable voices until found a repeatable one
            return voice;
        }
    }

    public final String voiceFilename;
    public final boolean interrupt;
    public final boolean doNotRepeat;

    private int hasPlayed = 0;      // 0 if not checked, +1 if played, -1 if not played yet

    public DemonVoice(String voiceFilename, boolean interrupt, boolean doNotRepeat) {
        this.voiceFilename = voiceFilename;
        this.interrupt = interrupt;
        this.doNotRepeat = doNotRepeat;
    }

    public boolean hasPlayed() {
        if(hasPlayed == 0) {
            // Haven't checked before, check now
            if(Globals.grid.isStateUnlocked(Globals.STATE_FLAPEEBIRD_DEMON_CHATTER + voiceFilename))
                hasPlayed = +1;
            else
                hasPlayed = -1;
        }
        return hasPlayed == +1;
    }

    public void notifyPlayed() {
        Globals.grid.unlockState(Globals.STATE_FLAPEEBIRD_DEMON_CHATTER + voiceFilename);
        hasPlayed = +1;
    }
}
