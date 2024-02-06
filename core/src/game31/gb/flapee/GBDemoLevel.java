package game31.gb.flapee;

import game31.app.flapee.FlapeeBirdScreen;
import game31.app.flapee.LeaderboardScore;
import game31.app.flapee.Level;
import game31.app.flapee.PissEffect;
import game31.app.flapee.Stage;
import sengine.animation.Animation;
import sengine.animation.MoveAnim;
import sengine.animation.ShearAnim;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.audio.SoundsSet;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.Range;
import sengine.calc.SetDistributedSelector;
import sengine.calc.SetRandomizedSelector;
import sengine.calc.SineGraph;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;

public class GBDemoLevel implements FlapeeBirdScreen.LevelSource {
    @Override
    public Level buildLevel() {

        Level level = new Level();

        // Lighting
        {
            level.baseLighting.bg = Sprite.load("apps/flapee/game/background.png").instantiate();
//        ColorAttribute.of(level.bg).set(1f, 0.7f, 0.7f, 1f);

            level.baseLighting.sunMat = Sprite.load("apps/flapee/game/sun.png").instantiate();
            level.baseLighting.sunColor.set(0xfcff01ff).a = 0.20f;
            level.baseLighting.sunSize = 0.5f;
            level.baseLighting.sunX = new CompoundGraph(false, new SineGraph(250f, 1f, 0f, 0.4f, 0.5f));
            level.baseLighting.sunY = new CompoundGraph(false, new SineGraph(230f, 0.5f, 0f, 0.4f, 0.9f));


            // Lighting and God rays
            level.baseLighting.lightColor.set(0x7cd7e7ff).a = 0.3f;
            level.baseLighting.godraysColor.set(0x7ad3e2ff).mul(0.9f).a = 0.45f;
            level.baseLighting.lightEdge = 0.4f;
            level.baseLighting.lightScale = 0.2f;
            level.baseLighting.lightGamma = 0.4545f;
            level.baseLighting.godraysScale = 0.7f;
            level.baseLighting.godraysGamma = 0.9545f;
            level.baseLighting.godraysFog = 0;

        }

        {
            level.demonLighting.bg = Sprite.load("apps/flapee/game/background.png").instantiate();
            ColorAttribute.of(level.demonLighting.bg).set(1f, 0.7f, 0.7f, 1f);

            level.demonLighting.sunMat = Sprite.load("apps/flapee/game/sun.png").instantiate();
            level.demonLighting.sunColor.set(0xff0000ff).a = 0.60f;
            level.demonLighting.sunSize = 2.5f;
            level.demonLighting.sunX = new CompoundGraph(false, new SineGraph(200f, 1f, 0f, 0.4f, 0.5f));
            level.demonLighting.sunY = new CompoundGraph(false, new SineGraph(230f, 0.5f, 0f, 0.4f, 0.3f));

            level.demonLighting.lightColor.set(0xff0000ff).a = 1f;
            level.demonLighting.godraysColor.set(0x748a96ff).mul(0.9f).a = 0.4f;
            level.demonLighting.lightEdge = 0.1f;
            level.demonLighting.lightScale = 0.2f;
            level.demonLighting.lightGamma = 0.9545f;
            level.demonLighting.godraysScale = 0.3f;
            level.demonLighting.godraysGamma = 0.7545f;
            level.demonLighting.godraysFog = 0.0f;

        }



        level.grounds = new SetRandomizedSelector<>(
                Sprite.load("apps/flapee/game/ground_tile_1.png"),
                Sprite.load("apps/flapee/game/ground_tile_2.png")
        );
        level.belowGroundColor = 0xeedf8eff;

        level.mountains = new SetRandomizedSelector<>(
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/mountain_1.png"), 401f / 401f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/mountain_2.png"), 283f / 401f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/mountain_3.png"), 289f / 401f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/mountain_4.png"), 183f / 401f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/mountain_5.png"), 199f / 401f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/mountain_6.png"), 358f / 401f)
        );

        level.mountainIntervalX = new Range(-0.15f, 0.1f);
        level.mountainSize = new Range(0.4f, 0.0f);
        level.mountainZ = 0.8f;
        level.mountainY = -0.015f;

        level.trees = new SetRandomizedSelector<>(
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/tree_1.png"), 513f / 513f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/tree_2.png"), 341f / 513f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/tree_3.png"), 1029f / 513f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/tree_4.png"), 543f / 513f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/tree_5.png"), 929f / 513f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/tree_6.png"), 425f / 513f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/tree_7.png"), 469f / 513f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/tree_8.png"), 300f / 513f)
        );
        level.treeAnims = new SetRandomizedSelector<Animation>(
                new ShearAnim(9f, ShearAnim.Location.BOTTOM,
                        new SineGraph(1f, 6f, 0f,
                                new CompoundGraph(
                                        new LinearGraph(0f, 0.03f, 0.3f),
                                        new LinearGraph(0.03f, 0.01f, 0.5f),
                                        new LinearGraph(0.01f, 0.0f, 0.2f)
                                ),
                                new CompoundGraph(
                                        new LinearGraph(0f, -0.03f, 0.2f),
                                        new LinearGraph(-0.03f, 0f, 0.8f)
                                ),
                                null
                        ),
                        null
                ),
                new ShearAnim(7f, ShearAnim.Location.BOTTOM,
                        new SineGraph(1f, 4f, 0f,
                                new CompoundGraph(
                                        new LinearGraph(0f, 0.015f, 0.2f),
                                        new LinearGraph(0.015f, 0.0f, 0.3f),
                                        new LinearGraph(0.0f, 0.03f, 0.4f),
                                        new LinearGraph(0.03f, 0.0f, 0.1f)
                                ),
                                new CompoundGraph(
                                        new LinearGraph(0f, -0.03f, 0.2f),
                                        new ConstantGraph(-0.03f, 0.6f),
                                        new LinearGraph(-0.03f, 0f, 0.2f)
                                ),
                                null
                        ),
                        null
                )
        );
        level.treeIntervalX = new Range(-0.25f, 0.4f);    // -0.25f, 0.4f
        level.treeSize = new Range(0.40f, 0.0f);
        level.treeZ = 0.5f;
        level.treeY = -0.015f;

        level.bushes = new SetRandomizedSelector<>(
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_1.png"), 201f / 201f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_2.png"), 195f / 201f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_3.png"), 181f / 201f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_4.png"), 149f / 201f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_5.png"), 149f / 201f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_6.png"), 165f / 201f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_7.png"), 173f / 201f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_8.png"), 53f / 201f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/bush_9.png"), 114f / 201f)
        );
        level.bushAnims = new SetRandomizedSelector<Animation>(
                new ShearAnim(2.5f, ShearAnim.Location.BOTTOM,
                        new SineGraph(1f, 4f, 0f,
                                new CompoundGraph(
                                        new LinearGraph(0.09f, 0.01f, 0.3f),
                                        new LinearGraph(0.01f, 0.12f, 0.7f)
                                ),
                                new ConstantGraph(-0.12f),
                                null
                        ),
                        null
                ),
                new ShearAnim(4.5f, ShearAnim.Location.BOTTOM,
                        new SineGraph(1f, 5f, 0f,
                                new CompoundGraph(
                                        new LinearGraph(0.06f, 0.01f, 0.3f),
                                        new LinearGraph(0.01f, 0.12f, 0.5f),
                                        new LinearGraph(0.12f, 0.06f, 0.2f)
                                ),
                                new ConstantGraph(-0.12f),
                                null
                        ),
                        null
                )
        );
        level.bushIntervalX = new Range(-0.12f, 0.12f);     // -0.08f, 0.18f
        level.bushSize = new Range(0.15f, 0.0f);
        level.bushZ = 0.1f;
        level.bushY = -0.015f;

        level.clouds = new SetRandomizedSelector<>(
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/cloud_1.png"), 307f / 307f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/cloud_2.png"), 292f / 307f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/cloud_3.png"), 292f / 307f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/cloud_4.png"), 292f / 307f)
        );
        level.cloudAnims = new SetRandomizedSelector<Animation>(
                new MoveAnim(2.5f, new CompoundGraph(
                        new SineGraph(1f, 1f, 0.25f, new ConstantGraph(0.2f), new ConstantGraph(0.2f), new CompoundGraph(
                                new LinearGraph(0f, 0.5f, 0.4f),
                                new LinearGraph(0.5f, 0f, 0.6f)
                        ))
//                        new LinearGraph(0f, -0.1f, 0.2f),
//                        new LinearGraph(-0.1f, 0f, 0.8f)
                ), null)
//                new CompoundAnim()
//                new ScaleAnim(
//                        1f,
//                        new SineGraph(1f, 1f, 0f, 0.1f, 1f),
//                        new SineGraph(1f, 1f, 0.5f, 0.1f, 1f)
//                )
        );
        level.tCloudInterval = new Range(1.5f, 0.3f);
        level.cloudsSize = new Range(0.25f, 0.05f);
        level.cloudsZ = 0.9f;
        level.cloudsY = new Range(0.6f, 1.2f);
        level.cloudsSpeed = new Range(-0.1f, -0.2f);

        level.pipeSize = 0.3f;
        level.groundY = -0.6f;
        level.pipeStartX = 1.9f;


        SetRandomizedSelector<Sprite> pipes = new SetRandomizedSelector<>(
                Sprite.load("apps/flapee/game/pipe_1.png"),
                Sprite.load("apps/flapee/game/pipe_2.png"),
                Sprite.load("apps/flapee/game/pipe_3.png"),
                Sprite.load("apps/flapee/game/pipe_4.png")
        );


//        Stage stage1 = new Stage();           // Normal stage
//        stage1.pipes = pipes;
//        stage1.pipeBoundingXAdjust = 0.06f;
//        stage1.pipeIntervalDistance = new Range(0.6f, 0f);
//        stage1.pipeGapRange = new Range(0.30f, 0.2f);
//        stage1.pipeDirectionAmount = new Range(3, 3);
//        stage1.pipeYOffsetRange = new Range(0f, 0.2f);
//        stage1.pipeMinYOffsetRange = new Range(0.0f, 0.1f);
//        stage1.pipeMinY = 0.08f;
//        stage1.pipeMaxY = -level.groundY + 0.72f;

        Stage stage1 = new Stage();     // This is an easy stage
        stage1.stageLength = new Range(10, 0);
        stage1.stageStartX = 0.5f;
        stage1.pipes = pipes;
        stage1.pipeBoundingXAdjust = 0.06f;
        stage1.pipeIntervalDistance = new Range(0.9f, 0f);
        stage1.pipeGapRange = new Range(0.45f, 0.0f);
        stage1.pipeDirectionAmount = new Range(3, 3);
        stage1.pipeYOffsetRange = new Range(0f, 0.2f);
        stage1.pipeMinYOffsetRange = new Range(0.0f, 0.1f, true);
        stage1.pipeMinY = 0.08f;
        stage1.pipeMaxY = -level.groundY + 0.72f;

        stage1.tDemonIdleChatterDelay = new Range(Float.MAX_VALUE, 0);
        stage1.demonIdleChatter = null;

        stage1.powerupCosts = new int[] {
                20
        };
        stage1.reviveCosts = new int[] {
                10
        };


        Stage stage2 = new Stage();     // This is an easy stage
        stage2.stageLength = new Range(10, 0);
        stage2.stageStartX = 0.5f;
        stage2.pipes = pipes;
        stage2.pipeBoundingXAdjust = 0.06f;
        stage2.pipeIntervalDistance = new Range(0.6f, 0f);
        stage2.pipeGapRange = new Range(0.3f, 0.1f);
        stage2.pipeDirectionAmount = new Range(3, 3);
        stage2.pipeYOffsetRange = new Range(0f, 0.2f);
        stage2.pipeMinYOffsetRange = new Range(0.0f, 0.1f, true);
        stage2.pipeMinY = 0.08f;
        stage2.pipeMaxY = -level.groundY + 0.72f;

        stage2.tDemonIdleChatterDelay = new Range(Float.MAX_VALUE, 0);
        stage2.demonIdleChatter = null;

        stage2.powerupCosts = new int[] {
                20
        };
        stage2.reviveCosts = new int[] {
                10
        };

        level.stages = new Stage[] {
                stage1,
                stage2,
        };


        level.bird = Sprite.load("apps/flapee/game/bird.png");
        level.birdX = 0.26f;
        level.birdSize = 0.15f;
        level.birdRotateAngle = -50f;
        level.birdRotateMax = -15f;
        level.birdRotateMaxVelocityY = -0.4f;
        level.birdGravityY = 3.0f;
        level.birdFlapY = +1.0f;
        level.birdHoverAnim = new MoveAnim(
                1f,
                null,
                new SineGraph(1f, 1f, 0f, 0.06f, 0f)
        ).loopAndReset();


        level.birdPissX = -0.022f;
        level.birdPissY = -0.035f;
        level.birdPissAngle = -10f;
        level.birdPowerupPissVelocity = new SetDistributedSelector<>(new Float[] {
                2.5f,
                2.2f,
                2.9f,
                1.8f,
                3.1f,
                3.1f,
                3.1f,
                3.1f,
                3.1f,
        }, new float[] {
                1.7f,
                2.1f,
                0.4f,
                1.2f,
                0.3f,
                0.3f,
                0.3f,
                0.3f,
                0.3f,
        });
        level.birdPissVelocity = new SetDistributedSelector<>(new Float[] {
                +0.5f,
        }, new float[] {
                10f,
        });
        level.birdPissVelocityModifier = new SineGraph(1f, 1f, 0f, 0.3f, 0.15f);       // 0.15f amp
        level.birdPissVolume = new SetDistributedSelector<>(new Float[] {
                0.015f,
                0.01f,
                0.025f,
        }, new float[] {
                10f,
                5,
                1f,
        });
        level.birdPissVolumeModifier = new SineGraph(0.09f, 1f, 0f, 0.005f, 0f);
        level.birdPissActiveTime = new Range(0.2f, 0.8f); // new Range(0.3f, 1.3f);
        level.birdPissInactiveTime = new Range(0.3f, 0.5f);     // 0.2f, 0.8f
        level.birdPissIntermittentTime = new Range(0.05f, 0.05f);
        level.birdPissIntermittentActiveTime = new Range(0.1f, 0.3f);

        level.birdPissEffect = new PissEffect(
                Material.load("apps/flapee/game/piss.png"),
                new SetRandomizedSelector<>(
                        Sprite.load("apps/flapee/game/splash1.png")
                ),
                new Range(0.07f, 0.03f)
        );

        level.birdDeathRotateGraph = new QuadraticGraph(0f, -180f, 2f, true);
        level.birdDeathPissVelocity = 2.1f;
        level.birdDeathVelocityY = 1.4f;

        level.speedX = 0.4f;

        level.powerupSpeedMultiplier = 2f;
        level.powerupChargingTime = 5f * level.powerupSpeedMultiplier;
        level.powerupEffectTime = 5f * level.powerupSpeedMultiplier;


        level.collisionMaxTime = 0.05f;
        level.chanceTime = 5f;

        level.scoreSound = Sound.load("sounds/flapee/point.ogg");

        level.flapSound = SoundsSet.create("sounds/flapee/flap.SoundSet", new SetRandomizedSelector<Audio.Sound>(
                Sound.load("sounds/flapee/flap1.ogg"),
                Sound.load("sounds/flapee/flap2.ogg"),
                Sound.load("sounds/flapee/flap3.ogg")
        ));

        level.hitGroundSound = SoundsSet.create("sounds/flapee/hit-ground.SoundSet", new SetRandomizedSelector<Audio.Sound>(
                Sound.load("sounds/flapee/hit-ground1.ogg"),
                Sound.load("sounds/flapee/hit-ground2.ogg")
        ));

        level.hitPipeSound = SoundsSet.create("sounds/flapee/hit-pipe.SoundSet", new SetRandomizedSelector<Audio.Sound>(
                Sound.load("sounds/flapee/hit-pipe1.ogg"),
                Sound.load("sounds/flapee/hit-pipe2.ogg")
        ));

        level.themeMusic = "sounds/flapee/theme.ogg";
        level.gameplayMusic = "sounds/flapee/theme-muted.ogg";
        level.powerupMusic = "sounds/flapee/theme-fast.ogg";
        level.storeMusic = "sounds/flapee/theme-demon20.ogg";

        level.revivingSound = Sound.load("sounds/flapee/heartbeat.ogg");
        level.revivedSound = Sound.load("sounds/flapee/revive.ogg");

        level.pissEffectSound = Sound.load("sounds/flapee/piss.ogg");
        level.tPissEffectTimeout = 0f;
        level.tPissEffectFade = 0.1f;

        level.leaderboard = new LeaderboardScore[] {        // These are ignored, demo level does not show leaderboard
                new LeaderboardScore("Teddy", 28, 214.7f, true),
                new LeaderboardScore("Calvin", 22, 130.6f, false),
                new LeaderboardScore("Natasha H", 11, 16.6f, false),
                new LeaderboardScore("Scott", 13, 12.3f, false),
                new LeaderboardScore("Freddy T", 10, 8.3f, false),
                new LeaderboardScore("Samuel T", 8, 5.7f, false),
                new LeaderboardScore("Cheah", 3, 4.1f, false),
        };
        
        return level;
    }
}
