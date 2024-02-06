package game31.app.flapee;

import sengine.calc.Graph;
import sengine.calc.Range;
import sengine.calc.SetSelector;
import sengine.graphics2d.Sprite;

public class Stage {
    public SetSelector<Sprite> pipes;
    public float pipeBoundingXAdjust;
    public Range pipeIntervalDistance;
    public Range pipeGapRange;
    public Range pipeDirectionAmount;
    public Range pipeYOffsetRange;
    public Range pipeMinYOffsetRange;
    public float pipeMinY;
    public float pipeMaxY;

    public SetSelector<Graph> pipeGapGraphs;
    public SetSelector<Graph> pipeYGraphs;

    public float stageStartX;
    public Range stageLength;

    public SetSelector<DemonVoice> demonIdleChatter;
    public Range tDemonIdleChatterDelay;
    public SetSelector<DemonVoice> demonPlayerHighscoreChatter;
    public SetSelector<DemonVoice> demonPlayerHitChatter;

    public int[] reviveCosts;
    public int[] powerupCosts;
}
