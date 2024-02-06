package game31.app.flapee;

import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.calc.Graph;
import sengine.calc.Range;
import sengine.calc.SetDistributedSelector;
import sengine.calc.SetSelector;
import sengine.graphics2d.Sprite;

public class Level {

    public final Lighting baseLighting = new Lighting();
    public final Lighting demonLighting = new Lighting();

    public float speedX;

    public SetSelector<Sprite> grounds;
    public int belowGroundColor;
    public float groundY;

    public SetSelector<FlapeeBirdScreen.PropType> bushes;
    public SetSelector<Animation> bushAnims;
    public float bushZ;
    public float bushY;
    public Range bushIntervalX;
    public Range bushSize;

    public SetSelector<FlapeeBirdScreen.PropType> trees;
    public SetSelector<Animation> treeAnims;
    public float treeAnimSyncX = 0;
    public float treeZ;
    public float treeY;
    public Range treeIntervalX;
    public Range treeSize;

    public SetSelector<FlapeeBirdScreen.PropType> mountains;
    public float mountainZ;
    public float mountainY;
    public Range mountainIntervalX;
    public Range mountainSize;

    public SetSelector<FlapeeBirdScreen.PropType> clouds;
    public SetSelector<Animation> cloudAnims;
    public float cloudsZ;
    public Range cloudsY;
    public Range tCloudInterval;
    public Range cloudsSize;
    public Range cloudsSpeed;

//    public SetSelector<Sprite> pipes;
//    public float pipeSize;
//    public float pipeBoundingXAdjust;
//    public Range pipeIntervalDistance;
//    public Range pipeGapRange;
//    public Range pipeDirectionAmount;
//    public Range pipeYOffsetRange;
//    public Range pipeMinYOffsetRange;
//    public float pipeMinY;
//    public float pipeMaxY;


    public float pipeStartX;
    public float pipeSize;

    public Stage[] stages;


    public Sprite bird;
    public float birdX;
    public float birdSize;
    public float birdRotateMax;
    public float birdRotateMaxVelocityY;
    public float birdRotateAngle;
    public float birdGravityY;
    public float birdFlapY;
    public float birdPissX;
    public float birdPissY;
    public float birdPissAngle;
    public SetDistributedSelector<Float> birdPissVelocity;
    public SetDistributedSelector<Float> birdPowerupPissVelocity;
    public Graph birdPissVelocityModifier;
    public SetDistributedSelector<Float> birdPissVolume;
    public Graph birdPissVolumeModifier;
    public Range birdPissActiveTime;
    public Range birdPissInactiveTime;
    public Range birdPissIntermittentActiveTime;
    public Range birdPissIntermittentTime;

    public PissEffect birdPissEffect;

    public Animation.Loop birdHoverAnim;


    public Graph birdDeathRotateGraph;
    public float birdDeathPissVelocity;
    public float birdDeathVelocityY;

    public float powerupEffectTime;
    public float powerupSpeedMultiplier;
    public float powerupChargingTime;

    public Audio.Sound flapSound;
    public Audio.Sound hitGroundSound;
    public Audio.Sound hitPipeSound;
    public Audio.Sound scoreSound;
    public Audio.Sound revivingSound;
    public Audio.Sound revivedSound;
    public Audio.Sound pissEffectSound;
    public float tPissEffectTimeout;
    public float tPissEffectFade;

    public String themeMusic;
    public String gameplayMusic;
    public String powerupMusic;
    public String storeMusic;

    public float collisionMaxTime;

    public float chanceTime;

    public LeaderboardScore[] leaderboard;
}
