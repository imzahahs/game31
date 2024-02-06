package game31;

import game31.gb.GBIdleScareScheduler;
import sengine.Entity;
import sengine.audio.Audio;
import sengine.calc.Range;
import sengine.utils.Builder;

/**
 * Created by Azmi on 8/29/2017.
 */

public class IdleScareScheduler extends Entity<Grid> {

    public static class Internal {
        public String[] soundPaths;
        public float[] tIntervals;
        public Range tMinIdleTime;
    }

    private final Builder<Object> builder;
    private Internal s;


    // State
    private int count = 0;
    private long lastScareTime = 0;
    private float tScheduled = Float.MAX_VALUE;


    public void pack(ScriptState state) {
        Globals.grid.state.set(Globals.STATE_IDLE_SCARE_COUNT, count);
        Globals.grid.state.set(Globals.STATE_IDLE_SCARE_LAST_TIME, lastScareTime);
    }


    public void setInternal(Internal internal) {
        s = internal;
    }

    public void reschedule() {
        if(count >= s.soundPaths.length)
            return;     // finished all scares
        long currentTime = Globals.grid.getSystemTime();
        long elapsedMillis = currentTime - lastScareTime;
        if(elapsedMillis < 0)
            return;     // ignore because there is some glitch that messes up with system time
        float elapsed = elapsedMillis / 1000f;
        float tInterval = s.tIntervals[count];
        float tRemainingWait = tInterval - elapsed;
        float tIdleWaitTime = s.tMinIdleTime.generate();
        float tWaitTime = Math.max(tRemainingWait, tIdleWaitTime);
        tScheduled = getRenderTime() + tWaitTime;
    }

    public void stop() {
        tScheduled = Float.MAX_VALUE;
    }


    public IdleScareScheduler() {
        builder = new Builder<Object>(GBIdleScareScheduler.class, this);
        builder.build();

        // Pull count and last scare from state
        count = Globals.grid.state.get(Globals.STATE_IDLE_SCARE_COUNT, 0);
        lastScareTime = Globals.grid.state.get(Globals.STATE_IDLE_SCARE_LAST_TIME, 0L);
        if(lastScareTime == 0)
            lastScareTime = Globals.grid.getSystemTime();
    }

    @Override
    protected void recreate(Grid v) {
        builder.start();
    }

    @Override
    protected void release(Grid v) {
        builder.stop();
    }


    @Override
    protected void render(Grid v, float r, float renderTime) {
        if(renderTime > tScheduled) {
            if(Audio.isMusicPlaying())
                reschedule();           // Try next time
            else {
                tScheduled = Float.MAX_VALUE;
                // Play audio track
                Audio.playMusic(s.soundPaths[count], false);
                // Increase count
                count++;
                lastScareTime = v.getSystemTime();
            }
        }
    }
}
