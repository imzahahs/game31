package game31.gb.flapee;

import game31.app.flapee.DemonVoice;
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
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.Range;
import sengine.calc.SetDistributedSelector;
import sengine.calc.SetRandomizedSelector;
import sengine.calc.SineGraph;
import sengine.calc.VibrationGraph;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;

public class GBDemonLevel implements FlapeeBirdScreen.LevelSource {

    @Override
    public Level buildLevel() {
        Level level = new Level();

        // Lighting
        {

            level.baseLighting.bg = Sprite.load("apps/flapee/game/demon/bg.png").instantiate();
//            ColorAttribute.of(level.baseLighting.bg).set(1f, 0.3f, 0.3f, 1f);

            level.baseLighting.sunMat = Sprite.load("apps/flapee/game/sun.png").instantiate();
            level.baseLighting.sunColor.set(0xff7711ff).a = 0.45f;
            level.baseLighting.sunSize = 2.5f;
            level.baseLighting.sunX = new CompoundGraph(true, new SineGraph(9f, 1f, 0f, 0.4f, 0.4f));
            level.baseLighting.sunY = new CompoundGraph(true, new SineGraph(13f, 1f, 0.5f, 0.6f, 0.9f));


            // Lighting and God rays
            level.baseLighting.lightColor.set(0xff7711ff).a = 0.5f;
            level.baseLighting.godraysColor.set(0x7e1a0eff).mul(1f).a = 0.4f;
            level.baseLighting.lightEdge = 0f;
            level.baseLighting.lightScale = 0.07f;
            level.baseLighting.lightGamma = 1.5f;
            level.baseLighting.godraysScale = 0.3f;
            level.baseLighting.godraysGamma = 0.9545f;
            level.baseLighting.godraysFog = 0.5f;
        }


        {
            level.demonLighting.bg = Sprite.load("apps/flapee/game/demon/bg.png").instantiate();
//            ColorAttribute.of(level.demonLighting.bg).set(0xaaaaaaff);

            level.demonLighting.sunMat = Sprite.load("apps/flapee/game/sun.png").instantiate();
            level.demonLighting.sunColor.set(0xff1111ff).a = 0.55f;        // 0.45f
            level.demonLighting.sunSize = 2.5f;
            level.demonLighting.sunX = new CompoundGraph(true, new SineGraph(9f, 1f, 0f, 0.4f, 0.4f));
            level.demonLighting.sunY = new CompoundGraph(true, new SineGraph(13f, 1f, 0.5f, 0.6f, 0.9f));

            level.demonLighting.lightColor.set(0xff1111ff).a = 0.95f;
            level.demonLighting.godraysColor.set(0x7e1a0eff).mul(1f).a = 0.6f;
            level.demonLighting.lightEdge = 0.5f;       // 0.7f
            level.demonLighting.lightScale = 0.2f;      // 0.05f
            level.demonLighting.lightGamma = 0.3f;   // 0.4545f
            level.demonLighting.godraysScale = 0.6f;    // 0.3f
            level.demonLighting.godraysGamma = 1f; // 0.9545f
            level.demonLighting.godraysFog = 0.8f;        // 0.8f

        }



        level.grounds = new SetRandomizedSelector<>(
                Sprite.load("apps/flapee/game/demon/ground_tile_1.png"),
                Sprite.load("apps/flapee/game/demon/ground_tile_2.png")
        );
        level.belowGroundColor = 0xc08229ff;

        level.mountains = new SetRandomizedSelector<>(
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/finger_1.png"), 356f / 356f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/finger_2.png"), 308f / 356f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/finger_3.png"), 331f / 356f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/finger_4.png"), 326f / 356f)
        );

        level.mountainIntervalX = new Range(-0.1f, 0.5f);
        level.mountainSize = new Range(0.31f, 0.04f);
        level.mountainZ = 0.8f;
        level.mountainY = -0.015f;

        level.trees = new SetRandomizedSelector<>(
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tree_1.png"), 436f / 436f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tree_2.png"), 307f / 436f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tree_3.png"), 450f / 436f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tree_4.png"), 304f / 436f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tree_5.png"), 307f / 436f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tree_6.png"), 306f / 436f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tree_7.png"), 359f / 436f)
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
        level.treeAnimSyncX = 1.2f;
        level.treeIntervalX = new Range(-0.12f, +0.05f);
        level.treeSize = new Range(0.41f, 0.03f);
        level.treeZ = 0.3f;
        level.treeY = -0.015f;

        level.bushes = new SetRandomizedSelector<>(
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tongue_1.png"), 207f / 207f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tongue_2.png"), 175f / 207f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tongue_3.png"), 152f / 207f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tongue_4.png"), 189f / 207f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tongue_5.png"), 168f / 207f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tongue_6.png"), 120f / 207f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/tongue_7.png"), 181f / 207f)
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
        level.bushIntervalX = new Range(-0.08f, 0.11f);
        level.bushSize = new Range(0.11f, 0.05f);
        level.bushZ = 0.1f;
        level.bushY = -0.025f;

        level.clouds = new SetRandomizedSelector<>(
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/cloud_1.png"), 279f / 279f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/cloud_2.png"), 295f / 279f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/cloud_3.png"), 290f / 279f),
                new FlapeeBirdScreen.PropType(Sprite.load("apps/flapee/game/demon/cloud_4.png"), 304f / 279f)
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
        level.cloudsSize = new Range(0.20f, 0.05f);
        level.cloudsZ = 0.9f;
        level.cloudsY = new Range(0.6f, 1.2f);
        level.cloudsSpeed = new Range(-0.1f, -0.2f);


        level.pipeSize = 0.3f;
        level.groundY = -0.6f;
        level.pipeStartX = 1.9f;

        SetRandomizedSelector<Sprite> pipes = new SetRandomizedSelector<>(
                Sprite.load("apps/flapee/game/demon/foot_1.png"),
                Sprite.load("apps/flapee/game/demon/foot_2.png"),
                Sprite.load("apps/flapee/game/demon/foot_3.png"),
                Sprite.load("apps/flapee/game/demon/foot_4.png")
        );

        // Chatter
        SetRandomizedSelector<DemonVoice> winChatter = new SetRandomizedSelector<>(
                new DemonVoice("content/vo/demon/highscore_winner.ogg", true, false)
        );

        SetRandomizedSelector<DemonVoice> hitChatter = new SetRandomizedSelector<>(
                new DemonVoice("content/vo/demon/giggle_1.ogg", false, false),
                new DemonVoice("content/vo/demon/giggle_2.ogg", false, false),
                new DemonVoice("content/vo/demon/giggle_3.ogg", false, false),
                new DemonVoice("content/vo/demon/giggle_4.ogg", false, false),
                new DemonVoice("content/vo/demon/giggle_5.ogg", false, false),
                new DemonVoice("content/vo/demon/giggle_6.ogg", false, false)
        );

        SetRandomizedSelector<DemonVoice> idleChatter = new SetRandomizedSelector<>(
                new DemonVoice("content/vo/demon/forced_watchvideos_allroadsleadnowhere.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_areyoucosmos.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_cleanseincalm.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_endingsbreedjoy.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_endlessgrind.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_everydayisdrudgery.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_iseeyoursoul.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_noselfinpeace.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_nothingfeelreal.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_numbsme.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_surrenderbliss.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_timeispassing.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_wearealllost.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_nothingfeelreal.ogg", false, false),
                new DemonVoice("content/vo/demon/forced_watchvideos_wordsaremeaningless.ogg", false, false)
        );


        Stage stage1 = new Stage();     // This is the normal stage
        stage1.pipes = pipes;
        stage1.pipeBoundingXAdjust = 0.06f;
        stage1.pipeIntervalDistance = new Range(0.5f, 0f);
        stage1.pipeGapRange = new Range(0.4f, 0.0f);
        stage1.pipeDirectionAmount = new Range(3, 3);
        stage1.pipeYOffsetRange = new Range(0f, 0.2f);
        stage1.pipeMinYOffsetRange = new Range(0.0f, 0.1f, true);
        stage1.pipeMinY = 0.08f;
        stage1.pipeMaxY = -level.groundY + 0.72f;
        stage1.stageLength = new Range(5, 0);
        stage1.stageStartX = 0.5f;

        stage1.tDemonIdleChatterDelay = new Range(1.5f, 0);
        stage1.demonIdleChatter = idleChatter;
        stage1.demonPlayerHitChatter = hitChatter;
        stage1.demonPlayerHighscoreChatter = winChatter;

        stage1.powerupCosts = new int[] {
                40,
                60,
                100,
        };
        stage1.reviveCosts = new int[] {
                10,
                20,
                40,
                60,
                80,
                100,
        };


        Stage stage2a = new Stage();     // Fakeout slam
        stage2a.pipes = pipes;
        stage2a.pipeBoundingXAdjust = 0.06f;
        stage2a.pipeIntervalDistance = new Range(0.55f, 0f);
        stage2a.pipeGapRange = new Range(0.5f, 0.0f);
        stage2a.pipeDirectionAmount = new Range(3, 3);
        stage2a.pipeYOffsetRange = new Range(0.0f, 0.2f);
        stage2a.pipeMinYOffsetRange = new Range(0, 0.1f, true);
        stage2a.pipeMinY = 0.08f;
        stage2a.pipeMaxY = -level.groundY + 0.72f;
        stage2a.stageLength = new Range(10, 0);
        stage2a.stageStartX = 0.5f;

        stage2a.pipeGapGraphs = new SetRandomizedSelector<Graph>(
                new CompoundGraph(
                        new ConstantGraph(1f, 2.75f),
                        new QuadraticGraph(1f, 0f, 0.1f, 0, false),
                        new LinearGraph(0f, 1f, 0.8f)
                )
        );

        stage2a.tDemonIdleChatterDelay = new Range(1.5f, 0);
        stage2a.demonIdleChatter = idleChatter;
        stage2a.demonPlayerHitChatter = hitChatter;
        stage2a.demonPlayerHighscoreChatter = winChatter;

        stage2a.powerupCosts = new int[] {
                50,
                60,
                100,
        };
        stage2a.reviveCosts = new int[] {
                20,
                40,
                60,
                80,
                100,
        };

        Stage stage2 = new Stage();     // Moving up
        stage2.pipes = pipes;
        stage2.pipeBoundingXAdjust = 0.06f;
        stage2.pipeIntervalDistance = new Range(0.6f, 0f);
        stage2.pipeGapRange = new Range(0.3f, 0.2f);
        stage2.pipeDirectionAmount = new Range(3, 3);
        stage2.pipeYOffsetRange = new Range(0.0f, 0.2f);
        stage2.pipeMinYOffsetRange = new Range(-0.1f, 0, false);
        stage2.pipeMinY = 0.08f;
        stage2.pipeMaxY = -level.groundY + 0.72f;

        stage2.pipeYGraphs = new SetRandomizedSelector<Graph>(
                new CompoundGraph(
                        new ConstantGraph(0f, 0.5f),
                        new QuadraticGraph(0f, +0.5f, 2f, 0f, true),
                        new QuadraticGraph(+0.5f, 0f, 2f, 0f, false)
                )
        );

        stage2.stageLength = new Range(10, 0);
        stage2.stageStartX = 0.5f;

        stage2.tDemonIdleChatterDelay = new Range(1.5f, 0);
        stage2.demonIdleChatter = idleChatter;
        stage2.demonPlayerHitChatter = hitChatter;
        stage2.demonPlayerHighscoreChatter = winChatter;

        stage2.powerupCosts = new int[] {
                60,
                100,
        };
        stage2.reviveCosts = new int[] {
                30,
                40,
                60,
                80,
                100,
        };

        Stage stage3 = new Stage();     // Wave gap
        stage3.pipes = pipes;
        stage3.pipeBoundingXAdjust = 0.06f;
        stage3.pipeIntervalDistance = new Range(level.pipeSize, 0f);
        stage3.pipeGapRange = new Range(0.5f, 0.0f);
        stage3.pipeDirectionAmount = new Range(3, 3);
        stage3.pipeYOffsetRange = new Range(0f, 0.0f);
        stage3.pipeMinYOffsetRange = new Range(0.0f, 0.0f, true);
        stage3.pipeMinY = 0.08f;
        stage3.pipeMaxY = -level.groundY + 0.72f;
        stage3.stageLength = new Range(10, 0);
        stage3.stageStartX = 0.5f;

        stage3.pipeGapGraphs = new SetRandomizedSelector<Graph>(
                new SineGraph(2.1f, 1f, -0.2f, +0.5f, +0.5f)
        );


        stage3.tDemonIdleChatterDelay = new Range(1.5f, 0);
        stage3.demonIdleChatter = idleChatter;
        stage3.demonPlayerHitChatter = hitChatter;
        stage3.demonPlayerHighscoreChatter = winChatter;

        stage3.powerupCosts = new int[] {
                70,
                100,
        };
        stage3.reviveCosts = new int[] {
                40,
                60,
                80,
                100,
        };

        Stage stage4 = new Stage();     // Moves the opposite way
        stage4.pipes = pipes;
        stage4.pipeBoundingXAdjust = 0.06f;
        stage4.pipeIntervalDistance = new Range(0.7f, 0f);
        stage4.pipeGapRange = new Range(0.45f, 0.0f);
        stage4.pipeDirectionAmount = new Range(3, 3);
        stage4.pipeYOffsetRange = new Range(0.0f, 0.0f);
        stage4.pipeMinYOffsetRange = new Range(-10f, 0.0f, false);
        stage4.pipeMinY = 0.08f;
        stage4.pipeMaxY = -level.groundY + 0.72f;
        stage4.stageLength = new Range(10, 0);
        stage4.stageStartX = 0.5f;

        stage4.pipeYGraphs = new SetRandomizedSelector<Graph>(
                new CompoundGraph(
                        new ConstantGraph(0f, 2.7f),
                        new QuadraticGraph(0f, +1f, 0.25f, 0f, true),
                        new QuadraticGraph(+1f, 0f, 3f, 0f, false)
                ),
                new CompoundGraph(
                        new ConstantGraph(+1f, 2.7f),
                        new QuadraticGraph(+1f, 0f, 0.25f, 0f, true),
                        new QuadraticGraph(0f, +1f, 3f, 0f, false)
                )
        );

        stage4.tDemonIdleChatterDelay = new Range(1.5f, 0);
        stage4.demonIdleChatter = idleChatter;
        stage4.demonPlayerHitChatter = hitChatter;
        stage4.demonPlayerHighscoreChatter = winChatter;

        stage4.powerupCosts = new int[] {
                80,
                100,
        };
        stage4.reviveCosts = new int[] {
                50,
                60,
                80,
                100,
        };

        Stage stage6 = new Stage();     // Vibrate
        stage6.pipes = pipes;
        stage6.pipeBoundingXAdjust = 0.06f;
        stage6.pipeIntervalDistance = new Range(0.55f, 0f);
        stage6.pipeGapRange = new Range(0.35f, 0.2f);
        stage6.pipeDirectionAmount = new Range(3, 3);
        stage6.pipeYOffsetRange = new Range(0.1f, 0.2f);
        stage6.pipeMinYOffsetRange = new Range(0, 0.1f, true);
        stage6.pipeMinY = 0.08f;
        stage6.pipeMaxY = -level.groundY + 0.72f;
        stage6.stageLength = new Range(10, 0);
        stage6.stageStartX = 0.5f;

        stage6.pipeGapGraphs = new SetRandomizedSelector<Graph>(
                new CompoundGraph(
                        new VibrationGraph(3f, new LinearGraph(1f, 0.025f), new LinearGraph(0f, 0.975f)),
                        new VibrationGraph(7f, new LinearGraph(0.025f, 1f), new LinearGraph(0.975f, 0f))
                )
        );
        stage6.pipeYGraphs = new SetRandomizedSelector<Graph>(
                new CompoundGraph(
                        new VibrationGraph(3f, new LinearGraph(2f, 0.025f), new LinearGraph(-1f, -0.0125f)),
                        new VibrationGraph(7f, new LinearGraph(0.025f, 2f), new LinearGraph(-0.0125f, -1f))
                )
        );

        stage6.tDemonIdleChatterDelay = new Range(1.5f, 0);
        stage6.demonIdleChatter = idleChatter;
        stage6.demonPlayerHitChatter = hitChatter;
        stage6.demonPlayerHighscoreChatter = winChatter;

        stage6.powerupCosts = new int[] {
                90,
                100,
        };
        stage6.reviveCosts = new int[] {
                60,
                80,
                100,
        };

        level.stages = new Stage[] {
                stage1,
                stage2a,
                stage2,
                stage3,
                stage4,
                stage6,
                stage4,
                stage2,
        };


        level.bird = Sprite.load("apps/flapee/game/demon/bird.png");
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
                Material.load("apps/flapee/game/blood.png"),
                new SetRandomizedSelector<>(
                        Sprite.load("apps/flapee/game/blood_splash1.png")
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

        level.flapSound = SoundsSet.create("sounds/flapee/flap.SoundSet", new SetRandomizedSelector<>(
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

        level.themeMusic = "sounds/flapee/theme-demon100.ogg";
        level.gameplayMusic = "sounds/flapee/theme-demon100-muted.ogg";
        level.powerupMusic = "sounds/flapee/theme-demon50.ogg";
        level.storeMusic = "sounds/flapee/theme-demon50.ogg";

        level.revivingSound = Sound.load("sounds/flapee/heartbeat.ogg");
        level.revivedSound = Sound.load("sounds/flapee/revive.ogg");

        level.pissEffectSound = Sound.load("sounds/flapee/piss.ogg");
        level.tPissEffectTimeout = 0f;
        level.tPissEffectFade = 0.1f;

        level.leaderboard = new LeaderboardScore[] {
                new LeaderboardScore("Teddy", 70, 131013.7f, true),
                new LeaderboardScore("Calvin", 41, 665.6f, false),
                new LeaderboardScore("Natasha H", 37, 438.3f, false),
                new LeaderboardScore("Scott", 34, 313.6f, false),
                new LeaderboardScore("Freddy T", 21, 24.3f, false),
                new LeaderboardScore("Samuel T", 17, 18.1f, false),
                new LeaderboardScore("Cheah", 13, 12.7f, false),
        };

        return level;
    }
}
