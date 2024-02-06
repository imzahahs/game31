package game31.app.flapee;

import sengine.mass.MassSerializable;

public class LeaderboardScore implements MassSerializable {
    final String name;
    final int score;
    final float hoursPlayed;
    final boolean isFriend;

    @MassConstructor
    public LeaderboardScore(String name, int score, float hoursPlayed, boolean isFriend) {
        this.name = name;
        this.score = score;
        this.hoursPlayed = hoursPlayed;
        this.isFriend = isFriend;
    }

    @Override
    public Object[] mass() {
        return new Object[] { name, score, hoursPlayed, isFriend };
    }
}
