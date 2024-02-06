package game31.gb;

import game31.IdleScareScheduler;
import sengine.calc.Range;

/**
 * Created by Azmi on 8/29/2017.
 */

public class GBIdleScareScheduler {

    public GBIdleScareScheduler(IdleScareScheduler o) {
        IdleScareScheduler.Internal s = new IdleScareScheduler.Internal();


        {
            s.soundPaths = new String[] {
                    // not used in pipe dreams
            };

            // Estimated playtime is 3 hours - 180 minutes. we have 18 clips so each clip should be about 10 minutes apart
            s.tIntervals = new float[] {
                    // not used in pipe dreams
            };

            s.tMinIdleTime = new Range(10f, 10f);       // 10 ~ 20 seconds minimum idle time

        }

        // Commit
        o.setInternal(s);
    }

}
