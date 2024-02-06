package game31.gb.flapee;

import com.badlogic.gdx.graphics.Color;

import game31.AppCrashDialog;
import game31.Globals;
import game31.ScreenBar;
import game31.app.flapee.DemonVoice;
import game31.app.flapee.FlapeeBirdScreen;
import game31.glitch.MpegGlitch;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.NullAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.Range;
import sengine.calc.SetRandomizedSelector;
import sengine.calc.SineGraph;
import sengine.calc.VibrationGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.PatchedTextBox;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.Toast;
import sengine.ui.UIElement;

public class GBFlapeeBirdScreen {

    private FlapeeBirdScreen.Internal s = new FlapeeBirdScreen.Internal();
    private FlapeeBirdScreen.ShowdownInternal stt = new FlapeeBirdScreen.ShowdownInternal();        // teddy trusts
    private FlapeeBirdScreen.ShowdownInternal stn = new FlapeeBirdScreen.ShowdownInternal();        // teddy not trusts


    private Font menuTitleFont = new Font("gaegu-bold.ttf", 48, 0x000000ff);
    private Font menuSubtitleFont = new Font("gaegu-regular.ttf", 48, 0x000000ff);
    private Font fineprintFont = new Font("gaegu-regular.ttf", 20, 0x00000099);

    private Font dialogFont = new Font("gaegu-bold.ttf", 48, 0xffaa06ff);

    private Font hoursPlayedFont = new Font("gaegu-regular.ttf", 48, 0x000000ff);

    private Font playerLeaderboardFont = new Font("gaegu-bold.ttf", 48, 0x000000ff);

    private Font menuButtonFont = new Font("gaegu-bold.ttf", 48, Color.CLEAR, 0, new Color(0x000000ff), +3, +4, Color.WHITE, -1, 0);
    private Font playButtonFont = new Font("gaegu-bold.ttf", 64, Color.CLEAR, 0, new Color(0x000000ff), +3, +5, Color.WHITE, -1, 0);
    private Font eggBuyFont = new Font("gaegu-bold.ttf", 32, Color.BLACK, 2, Color.CLEAR, 0, 0, Color.WHITE, -4, 0);
    private Font eggRewardFont = new Font("gaegu-bold.ttf", 48, Color.BLACK, 3, Color.CLEAR, 0, 0, Color.WHITE, -4, 0);

    //        Font scoreFont = new Font("opensans-semibold.ttf", 48, new Color(0x000000ff), 5, Color.CLEAR, 0, 0, Color.WHITE, -3, 0);
    private Font scoreFont = new Font("gaegu-bold.ttf", 256, 0xffffff33);

    private Font eggsFont = new Font("gaegu-bold.ttf", 48, new Color(0x000000ff), 3, Color.CLEAR, 0, 0, new Color(0xffff00ff), -3, 0);

    private Font newHighScoreFont = new Font("gaegu-bold.ttf", 64, Color.CLEAR, 0, Color.BLACK, 4, 4, Color.WHITE, -4, -15);

    private Font shopBoldFont = new Font("gaegu-bold.ttf", 48, 0x000000ff);

    private Font newFont = new Font("gaegu-bold.ttf", 20, 0xffffffff);

    private Animation buttonIdleAnim = new ScaleAnim(1.5f, new SineGraph(1f, 1f, 0.75f, 0.02f, 1.02f));
    private Animation buttonPressedAnim = new CompoundAnim(0.15f, new Animation[] {
            new ScaleAnim(1f, new QuadraticGraph(1f, 0.95f, true)),
            new MoveAnim(1f, null, new ConstantGraph(-0.03f))
    });
    private Animation buttonReleasedAnim = new SequenceAnim(new Animation[] {
            new CompoundAnim(0.3f, new Animation[] {
                    new MoveAnim(1f, null, new QuadraticGraph(-0.03f, 0f, true)),
                    new ScaleAnim(1f, new SineGraph(1f, 3f, 0f, new QuadraticGraph(0.05f, 0f, true), new ConstantGraph(1f), null))
            }),
    });

    private Audio.Sound buttonPressedSound = Sound.load("sounds/flapee/button-pressed.ogg");
    private Audio.Sound buttonReleasedSound = Sound.load("sounds/flapee/button-released.ogg");

    private Animation menuAppearAnim = new SequenceAnim(new Animation[] {
            new ScaleAnim(0.16f),
            new CompoundAnim(0.18f, new Animation[] {
                    new RotateAnim(1f, new QuadraticGraph(-90f, 0f, false)),
                    new ScaleAnim(1f, QuadraticGraph.zeroToOne),
            }),
            new ScaleAnim(0.25f, new SineGraph(1f, 3f, 0f, new QuadraticGraph(0.08f, 0f, true), ConstantGraph.one, null))
    });
    private Animation menuDisappearAnim = new SequenceAnim(new Animation[] {
            new ScaleAnim(0.1f, new QuadraticGraph(1f, 1.1f, true)),
            new CompoundAnim(0.18f, new Animation[] {
                    new RotateAnim(1f, new QuadraticGraph(0f, +90f, false)),
                    new ScaleAnim(1f, new QuadraticGraph(1.1f, 0f, false)),
            }),
    });


    private StaticSprite newSticker;

//            new ScaleAnim(0.18f, QuadraticGraph.oneToZeroInverted);

    {
        shopBoldFont.name("SHOP_BOLD");
    }


    private Sprite sprite;
    private PatchedSprite patch;


    private void buildBase() {
        s.window = new UIElement.Group();

        // Bg
//        sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
//        ColorAttribute.of(sprite).set(0x000000ff);
//        new StaticSprite()
//                .viewport(s.window)
//                .visual(sprite, SaraRenderer.TARGET_BG)
//                .attach();


        s.bars = new ScreenBar();
        s.bars.showNavbar(true, true, true);
//            s.bars.showShadows(0x000000ff, 0.9f);
        s.bars.color(0x000000ff, 0.25f, 0x000000ff, 0.25f);

        s.barsHideAnim = new MoveAnim(
                0.3f,
                null,
                new QuadraticGraph(0f, -0.5f, false)
        );
        s.barsShowAnim = new MoveAnim(
                0.3f,
                null,
                new QuadraticGraph(-0.5f, 0f, true)
        );


        // Region to tap
        s.tapView = new Clickable()
                .viewport(s.window)
                .length(Globals.LENGTH)
                .attach();

        // Input blocker
        sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff).alpha(0.5f);
        s.inputBlockerView = new StaticSprite()
                .viewport(s.bars.viewport)
                .visual(sprite, SaraRenderer.TARGET_APPBAR_TEXT)
                .passThroughInput(false)
                .animation(
                        new FadeAnim(0.5f, LinearGraph.zeroToOne),
                        null,
                        null
                )
        ;

        // Title
        s.titleView = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(471f / 512f).move(0, +0.62f))
                .visual(Sprite.load("apps/flapee/flapee-title.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(
                        new ColorAnim(1f, new QuadraticGraph(3f, 1f, true), null),
                        new MoveAnim(1f, null, new SineGraph(1f, 1f, 0f, 0.003f, 0f)),
                        menuDisappearAnim
                )
                ; // .attach();

        s.royaleTitleView = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(471f / 512f).move(0, +0.56f))
                .visual(Sprite.load("apps/flapee/flapee-royale-title.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(
                        new ColorAnim(1f, new QuadraticGraph(3f, 1f, true), null),
                        new MoveAnim(1f, null, new SineGraph(1f, 1f, 0f, 0.003f, 0f)),
                        menuDisappearAnim
                )
                ; //.attach();


        // Loading circle
        sprite = Sprite.load("apps/flapee/loading-circle.png").instantiate();
        s.loadingView = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(0.22f).move(0, -0.1f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        new RotateAnim(0.8f, new LinearGraph(0, 360)),
                        menuDisappearAnim)
        ;
        s.tLoadingTime = 1.5f;

        s.readyView = new Toast()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(245f / 288f).move(0, +0.55f))
                .visual(Sprite.load("apps/flapee/get-ready-title.png"), SaraRenderer.TARGET_INTERACTIVE)
                .animation(
                        new SequenceAnim(new Animation[]{
                                new ScaleAnim(1.0f),
                                new NullAnim(0.12f),
                                new ScaleAnim(0.12f),
                                new NullAnim(0.12f),
                                new ScaleAnim(0.12f),
                                new NullAnim(0.12f),
                                new ScaleAnim(0.12f),
                        }),
                        null,
                        new FadeAnim(0.25f, LinearGraph.oneToZero)
                )
                .time(1.5f)
        ;

        s.tutorialView = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(132f / 288f).move(0, -0.42f))
                .visual(Sprite.load("apps/flapee/flap-tutorial.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne), 0.3f, true),
                        new SequenceAnim(new FadeAnim(0.25f, new ConstantGraph(0.5f)), 0.25f, false),
                        new FadeAnim(0.25f, LinearGraph.oneToZero)
                )
        ;

        s.gameOverView = new StaticSprite()
                .viewport(s.window)
//                    .metrics(new UIElement.Metrics().scale(245f / 288f).move(0, +0.55f))
                .metrics(new UIElement.Metrics().scale(411f / 512f).move(0, +0.62f))
                .visual(Sprite.load("apps/flapee/game-over-title.png"), SaraRenderer.TARGET_INTERACTIVE)
                .animation(
                        new ScaleAnim(0.2f, LinearGraph.zeroToOne),
                        null,
                        new FadeAnim(0.5f, LinearGraph.oneToZero)
                )
        ;
        s.tNormalGameOverDelay = 2f;

        s.scoreView = new TextBox()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(0.8f).move(0, +0.15f))
                .text(new Text()
                        .font(scoreFont, SaraRenderer.TARGET_FLAPEE_SKY)
                        .position(0.7f, -1.5f)
                        .text("0")
                )
        ;
        s.scoredAnim = new FadeAnim(0.4f, new LinearGraph(4f, 1f));

        // New high score
        s.newHighScoreView = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(0.95f).move(0, -0.33f))
                .visual(Sprite.load("apps/flapee/highscore-splat.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        new SequenceAnim(new Animation[] {
                                new CompoundAnim(0.12f, new Animation[] {
                                        new ColorAnim(1f, new QuadraticGraph(30f, 1f, false), null),
                                        new ScaleAnim(1f, new QuadraticGraph(0.5f, 1f, false))
                                }),
                                new ScaleAnim(0.3f, new SineGraph(1f, 3f, 0f, new QuadraticGraph(0.1f, 0f, true), ConstantGraph.one, null))
                        }),
                        new SequenceAnim(new ScaleAnim(0.1f, new ConstantGraph(1.025f)), 0.1f, false),
                        menuDisappearAnim
                )
                ; // .attach();
        new TextBox()
                .viewport(s.newHighScoreView)
                .metrics(new UIElement.Metrics().scale(0.70f).move(0f, +0.01f))
                .text(new Text()
                        .font(newHighScoreFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(275f / 753f, 0)
                        .text("NEW\nHIGHSCORE!")
                )
                .animation(
                        null,
                        new ColorAnim(0xffff00ff),
                        null
                )
                .attach();
        s.tNewHighScoreTime = 4f;
        s.tNewHighScoreRoyaleTime = 8f;

        s.rankingView = new TextBox()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(0.77f).move(0f, +0.62f))
                .text(new Text()
                        .font(newHighScoreFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(0.5f, 0)
                        .text("YOU PLACED #5")
                )
                .animation(
                        new SequenceAnim(new ScaleAnim(0.12f, new QuadraticGraph(1.1f, 1f, false)), 0.2f, true),
                        null,
                        new FadeAnim(0.15f, LinearGraph.oneToZero)
                )
                ; //.attach();
        s.rankingFormat = "YOU PLACED #%d";

        s.rankingRoyaleView = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(0.85f).move(0f, +0.52f).rotate(-10f))
                .visual(Sprite.load("apps/flapee/winner-royale.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        new SequenceAnim(new ScaleAnim(0.4f,
                                new SineGraph(1f, 3f, 0f,
                                        new QuadraticGraph(0.1f, 0f, true),
                                        ConstantGraph.one,
                                        null
                                )
                        ), 0.5f, false),
                        menuDisappearAnim
                )
//                .attach()
                ;
        sprite = Sprite.load("apps/flapee/celebration-rays.png").instantiate();
        ColorAttribute.of(sprite).set(0xffffc1ff).alpha(0.6f);
        new StaticSprite()
                .viewport(s.rankingRoyaleView)
                .metrics(new UIElement.Metrics().scale(1.4f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                .animation(
                        new FadeAnim(1f, QuadraticGraph.zeroToOneInverted),
                        new CompoundAnim(16.5f, new Animation[] {
                                new ScaleAnim(6f, new SineGraph(1f, 3f, 0f, 0.2f, 1.2f)),
                                new RotateAnim(10f, new LinearGraph(0f, 360f))
                        }),
                        null
                )
                .attach();
        new TextBox()
                .viewport(s.rankingRoyaleView)
                .metrics(new UIElement.Metrics().scale(654f / 1008f).move(+0.15f, -0.005f).rotate(+2f))
                .text(new Text()
                        .font(newHighScoreFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(80f / 604f, 0)
                        .text("ROYALE FLUSH!")
                )
                .attach();

        // New sticker
        patch = PatchedSprite.create("system/rounded.png", 0.41f, 0.07f);
        newSticker = new StaticSprite()
                .visual(patch, SaraRenderer.TARGET_APPBAR)
                .animation(null, new SequenceAnim(new Animation[]{
                        new ColorAnim(0.05f, 0xff2222ff, false),
                        new ColorAnim(0.05f, 0xff7519ff, false),

                }), null)
                ;
        new TextBox()
                .viewport(newSticker)
                .text(new Text()
                        .font(newFont, SaraRenderer.TARGET_APPBAR_TEXT)
                        .position(newSticker.getLength(), -3f)
                        .text("NEW")
                )
                .attach();

        // Die splash
        sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
        s.dieSplash = new Toast()
                .viewport(s.window)
                .visual(sprite, SaraRenderer.TARGET_APPBAR)
                .animation(
                        null,
                        null,
                        new FadeAnim(0.4f, new QuadraticGraph(1f, 0f, 0, true))
                )
                ;


        s.eggsNotEnoughSound = Sound.load("sounds/general_invalid.ogg");
        s.newHighScoreSound = Sound.load("sounds/flapee/new-high-score.ogg");

        s.gameplayThemeVolume = 0.5f;

        // Game stats
        s.tFlapInterval = 0.15f;
        s.pipeHitVerticalAnim = new MoveAnim(1f, null, new SineGraph(1f, 20f, 0f, new QuadraticGraph(0.015f, 0f, false), null, null));
        s.pipeHitHorizontalAnim = new RotateAnim(1f, new SineGraph(1f, 20f, 0f, new QuadraticGraph(0.4f, 0f, false), null, null));

        s.lightingChangeStartGraph = new QuadraticGraph(0f, 1f, 1.0f, 0, true);
        s.lightingChangeEndGraph = new CompoundGraph(new Graph[] {
                new QuadraticGraph(1f, 0.5f, 1.5f, 0f, false),
                new QuadraticGraph(0.5f, 0f, 1.5f, 0f, true)
        });
        s.lightingVoiceGraph = new VibrationGraph(1f, new LinearGraph(0f, +0.5f), null);
        s.tLightingEndCooldown = 2f;

        // Demon voice

        s.demonVoiceSfxVolume = 0.08f;
        s.demonVoiceBgMusicVolume = 0.08f;

        s.demonVoiceScreenBgStartAnim = new FadeAnim(0.5f, LinearGraph.oneToZero);
        s.demonVoiceScreenBgLoopAnim = new FadeAnim(1f, ConstantGraph.zero);
        s.demonVoiceScreenBgEndAnim = new FadeAnim(3f, LinearGraph.zeroToOne);

        s.demonOpenEggshopVoices = new SetRandomizedSelector(new DemonVoice[]{
                new DemonVoice("content/vo/demon/eggstore_bestdeal.ogg", false, false),
                new DemonVoice("content/vo/demon/eggstore_dobetter.ogg", false, false),
                new DemonVoice("content/vo/demon/eggstore_rightplace.ogg", false, false),
                new DemonVoice("content/vo/demon/eggstore_tiredofdying.ogg", false, false),
                new DemonVoice("content/vo/demon/eggstore_anyyounger.ogg", false, false),
                new DemonVoice("content/vo/demon/eggstore_armleg.ogg", false, false),
                new DemonVoice("content/vo/demon/eggstore_spendsome.ogg", false, false),
                new DemonVoice("content/vo/demon/eggstore_youthfulspunk.ogg", false, false)
        });

        s.demonBuyEggsVoices = new SetRandomizedSelector(new DemonVoice[] {
                new DemonVoice("content/vo/demon/buy_gooddeal.ogg", false, false),
                new DemonVoice("content/vo/demon/buy_spendonsomething.ogg", false, false),
                new DemonVoice("content/vo/demon/buy_brilliant.ogg", false, false),
                new DemonVoice("content/vo/demon/buy_eggcelent.ogg", false, false),
                new DemonVoice("content/vo/demon/buy_wontregret.ogg", false, false)
        });

        s.demonRejectEggshopVoices = new SetRandomizedSelector(new DemonVoice[]{
                new DemonVoice("content/vo/demon/notoeggs_aww.ogg", false, false),
                new DemonVoice("content/vo/demon/notoeggs_changemind.ogg", false, false),
                new DemonVoice("content/vo/demon/notoeggs_comeon.ogg", false, false),
                new DemonVoice("content/vo/demon/notoeggs_nofun.ogg", false, false),
                new DemonVoice("content/vo/demon/notoeggs_regret.ogg", false, false)
        });


        s.tDemonHitTauntMinInterval = 30f;          // used to be 15f

        // Cheat config
        s.cheatCrashDialog = new AppCrashDialog("FlapeeBird");
        s.cheatMaxPipes = 6;
        s.cheatUnlockTags = new String[] {
                "mail.flapee_cheating1.cheated",
                "mail.flapee_cheating2.cheated",
                "mail.flapee_cheating3.cheated",
        };

        s.ingamePowerupBirdAnim = new ScaleAnim(0.3f, new SineGraph(1f, 1f, 0f, 0.2f, 1.2f));
        s.reviveBirdAnim = new ColorAnim(0.2f, new CompoundGraph(new Graph[] {
                new ConstantGraph(1f, 0.5f),
                new ConstantGraph(2f, 0.5f),
        }), null);

//        new FadeAnim(0.3f, new CompoundGraph(new Graph[] {
//                new ConstantGraph(0.5f, 0.5f),
//                new ConstantGraph(1f, 0.5f)
//        }));

        // Timing
        s.gameTimeMultiplier = 10f; // 10x the actual time
    }

    private void buildFindingFriendsPopup() {
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 239f / 373f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.findingFriendsGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(373f / 512f).move(0, -0.2f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        null,
                        menuDisappearAnim
                )
        ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 133f / 792f, 21f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.findingFriendsGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new TextBox()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(23f / 324f, 0)
                        .text("Royale Mode")
                )
                .attach();

        Toast toast = new Toast()
                .viewport(s.findingFriendsGroup)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorTop().move(0, -0.245f))
                .length(25f / 324f)
                .time(1.5f)
                .attach();
        new TextBox()
                .viewport(toast)
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(25f / 324f, -10f)
                        .text("Finding your Friends")
                )
                .attach();
        toast = new Toast()
                .viewport(s.findingFriendsGroup)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorTop().move(0, -0.245f))
                .length(25f / 324f)
                .time(10f)
                .animation(new ScaleAnim(1.5f), null, null)
                .attach();
        new TextBox()
                .viewport(toast)
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(25f / 324f, -10f)
                        .text("Adding Teddy")
                )
                .attach();

        Sprite barBg = Sprite.load("apps/flapee/progress-bar.png").instantiate();
        ColorAttribute.of(barBg).set(0xffd2a9ff);
        Sprite bar = barBg.instantiate();
        ColorAttribute.of(bar).set(0xf67e44ff);
        new HorizontalProgressBar()
                .viewport(s.findingFriendsGroup)
                .metrics(new UIElement.Metrics().scale(317f / 373f).anchorBottom().move(0, +0.12f))
                .visual(barBg, bar, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        s.tFindingFriendsTime = 4f;
    }

    private void buildResourceIcons() {
        // Egg icon
        s.eggsView = new TextBox()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(0.20f).anchorTop().anchorRight().move(-0.13f, -0.05f))
                .text(new Text()
                        .font(eggsFont, SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE_TEXT)
                        .position(0.3f, -2.7f)
                        .centerRight()
                        .text("0")
                )
        ;
        new StaticSprite()
                .viewport(s.eggsView)
                .metrics(new UIElement.Metrics().scale(0.36f).anchorRight().pan(+1, 0).move(+0.1f, 0))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE)
                .attach();
        s.eggsNotEnoughAnim = new CompoundAnim(0.5f, new Animation[] {
                new MoveAnim(1f, new SineGraph(1f, 3f, 0f, new LinearGraph(0.07f, 0f), null, null), null),
                new ColorAnim(1f, new CompoundGraph(new Graph[] {
                        new ConstantGraph(0f, 0.125f),
                        new ConstantGraph(1f, 0.125f),
                        new ConstantGraph(0f, 0.125f),
                        new ConstantGraph(1f, 0.125f),
                        new ConstantGraph(0f, 0.125f),
                        new ConstantGraph(1f, 0.125f),
                        new ConstantGraph(0f, 0.125f),
                        new ConstantGraph(1f, 0.125f),
                }), ConstantGraph.zero, ConstantGraph.zero, null, false)
        });

        // Egg icon
        s.lifehoursView = new TextBox()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(0.20f).anchorTop().anchorLeft().move(+0.13f, -0.05f))
                .text(new Text()
                        .font(eggsFont, SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE_TEXT)
                        .position(0.3f, -2.7f)
                        .centerLeft()
                        .text("0")
                )
                .animation(menuAppearAnim, null, new MoveAnim(0.3f, new QuadraticGraph(0f, -1.5f, false), null))
        ;
        new StaticSprite()
                .viewport(s.lifehoursView)
                .metrics(new UIElement.Metrics().scale(0.36f).anchorLeft().pan(-1, 0).move(-0.1f, 0))
                .visual(Sprite.load("apps/flapee/heartlife.png"), SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE)
                .attach();

        s.resourceTransitionGraph = new LinearGraph(0f, 1f, 2f);
    }

    private void buildIngamePowerupButtons() {
        // Powerup
        sprite = Sprite.load("apps/flapee/jetstream-button.png");
        s.ingamePowerupButton = new Clickable()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(700f / 1080f).anchorBottom().move(0, +0.035f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        null,
                        buttonPressedAnim,
                        buttonReleasedAnim,
                        null
                )
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(+0.15f, +0.01f, 500f / 700f, 170f / 500f, 6f)
                        .centerLeft()
                        .text("JETSTREAM!")
                )
                .inputPadding(10f, 0.1f, 10f, 10f)
        ;
        StaticSprite eggIcon = new StaticSprite()
                .viewport(s.ingamePowerupButton)
                .metrics(new UIElement.Metrics().scale(0.14f).anchorLeft().move(+0.09f, 0).rotate(-10))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .attach();
        s.ingamePowerupCostView = new TextBox()
                .viewport(eggIcon)
                .metrics(new UIElement.Metrics().scale(1.5f))
                .text(new Text()
                        .font(eggBuyFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0.3f, -2.1f)
                        .text("20")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/jetstream-disabled-button.png");
        s.ingamePowerupChargingView = new StaticSprite()
                .viewport(s.ingamePowerupButton)
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
        ;

        s.ingamePowerupChargingAnim = new ScissorAnim(1f, new Animation[] {
                new ScaleAnim(1f, ScaleAnim.Location.RIGHT, LinearGraph.oneToZero, null)
        });

        sprite = Sprite.load("apps/flapee/jetstream-border.png").instantiate();
        ColorAttribute.of(sprite).set(0xffff00ff);
        s.ingamePowerupAvailableView = new Toast()
                .viewport(s.ingamePowerupButton)
                .metrics(new UIElement.Metrics().scale(758f / 700f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                .animation(
                        null,
                        null,
                        new SequenceAnim(new Animation[] {
                                new ScaleAnim(0.15f),
                                new NullAnim(0.15f),
                                new ScaleAnim(0.15f),
                                new NullAnim(0.15f),
                                new ScaleAnim(0.15f),
                                new NullAnim(0.15f),
                                new ScaleAnim(0.15f),
                        })
                )
        ;
    }

    private void buildSendingEggsPopup() {
        // Sending Eggs
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 322f / 373f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.sendingEggsGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(373f / 512f).move(0, -0.2f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        null,
                        menuDisappearAnim
                )
        ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 133f / 792f, 21f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.sendingEggsGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new TextBox()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(23f / 324f, 0)
                        .text("Sending Eggs")
                )
                .attach();

        StaticSprite teddyProfileView = new StaticSprite()
                .viewport(s.sendingEggsGroup)
                .metrics(new UIElement.Metrics().scale(0.23f).anchorTop().move(0, -0.23f))
                .visual(Sprite.load("content/profiles/chats/teddy.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .attach();
        sprite = Sprite.load("system/circle-medium.png").instantiate();
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(teddyProfileView)
                .metrics(new UIElement.Metrics().scale(1.05f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new TextBox()
                .viewport(s.sendingEggsGroup)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorTop().move(0, -0.51f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(25f / 324f, -10f)
                        .text("Teddy")
                )
                .attach();

        Sprite barBg = Sprite.load("apps/flapee/progress-bar.png").instantiate();
        ColorAttribute.of(barBg).set(0xffd2a9ff);
        Sprite bar = barBg.instantiate();
        ColorAttribute.of(bar).set(0xf67e44ff);
        new HorizontalProgressBar()
                .viewport(s.sendingEggsGroup)
                .metrics(new UIElement.Metrics().scale(317f / 373f).anchorBottom().move(0, +0.12f))
                .visual(barBg, bar, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        s.tSendingEggsTime = 3f;
    }

    private void buildAdvancedMenu() {
        // Main menu
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 200f / 373f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.menuGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(373f / 512f).move(0, +0.27f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(menuAppearAnim, null, menuDisappearAnim)
                ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 133f / 792f, 21f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.menuGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new TextBox()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(23f / 324f, 0)
                        .text("YOUR FRIENDS")
                )
                .attach();

        s.friendsRowPlayer = new Sprite(0.125f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(s.friendsRowPlayer).set(0xff9827ff);
        StaticSprite row = new StaticSprite()
                .viewport(s.menuGroup)
                .visual(s.friendsRowPlayer, SaraRenderer.TARGET_INTERACTIVE_FLOATING);

        s.friendRowNameView = new TextBox()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorLeft().move( +0.07f, 0))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(25f / 324f, -10f)
                        .centerLeft()
                        .text("Player")
                )
                .attach();
        s.friendRowScoreView = new TextBox()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorRight().move( -0.07f, 0))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(25f / 324f, -10f)
                        .centerRight()
                        .text("0")
                )
                .attach();

        float rowScale = 0.985f;
        float y = -titleBg.getLength();
        float yInterval = -row.getLength() * rowScale;
        s.friendRows = new StaticSprite[2];
        for(int c = 0; c < s.friendRows.length; c++) {
            s.friendRows[c] = row.instantiate()
                    .metrics(new UIElement.Metrics().anchorTop().scale(rowScale).move(0, y))
                    .attach();
            y += yInterval;
        }

        // Buttons
//        patch = PatchedSprite.create("apps/flapee/button.png", 77f / 218f, 12f / 218f);
        sprite = Sprite.load("apps/flapee/highscore-button.png");
        s.highScoreButton = new Clickable()
                .viewport(s.menuGroup)
                .metrics(new UIElement.Metrics().scale(535f / 792f).anchorBottom().pan(0, -0.5f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -7.5f)
                        .text("HIGH SCORES")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/play-button.png");
        s.playButton = new Clickable()
                .viewport(s.moreEggsGroup)
                .metrics(new UIElement.Metrics().scale(218f / 373f).anchorBottom().pan(0, -0.5f).move(0, -0.15f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(playButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -4f)
                        .text("PLAY!")
                )
                .attach();
    }

    private void buildBasicMenu() {
        // Basic menu
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 190f / 373f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.basicGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(373f / 512f).move(0, -0.05f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(menuAppearAnim, null, menuDisappearAnim)
        ;

        patch = PatchedSprite.create("apps/flapee/window-bg.png", 133f / 792f, 21f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.basicGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        new TextBox()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(23f / 324f, 0)
                        .text("YOUR SCORE")
                )
                .attach();


        s.basicScoreView = new TextBox()
                .viewport(s.basicGroup)
                .metrics(new UIElement.Metrics().scale(0.23f).anchorTop().move(0, -0.25f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_IRIS_OVERLAY_TEXT)
                        .position(0.33f, 0)
                        .text("20")
                )
                .attach();

        // Buttons
//        s.basicHighScoreButton = new Clickable()
//                .viewport(s.basicGroup)
//                .metrics(new UIElement.Metrics().scale(535f / 792f).anchorBottom().pan(0, -0.5f))
//                .visuals(Sprite.load("apps/flapee/highscore-button.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
//                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
//                .sound(null, buttonPressedSound, buttonReleasedSound)
//                .text(new Text()
//                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
//                        .position(0, +0.015f, 1f, patch.length, -7.5f)
//                        .text("HIGH SCORES")
//                )
//                .attach();

        sprite = Sprite.load("apps/flapee/play-button.png");
        s.basicPlayButton = new Clickable()
                .viewport(s.basicGroup)
                .metrics(new UIElement.Metrics().scale(218f / 373f).anchorBottom().move(0, -0.50f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(playButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -4f)
                        .text("PLAY!")
                )
                .attach();
    }

    private void buildHighScoreMenu() {
        // High score group
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 550f / 373f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.scoreGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(373f / 512f).move(0, -0.07f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(menuAppearAnim, null, menuDisappearAnim)
        ;

        patch = PatchedSprite.create("apps/flapee/window-bg.png", 133f / 792f, 21f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.scoreGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        new TextBox()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(23f / 324f, 0)
                        .text("HIGH SCORES")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/back-button.png");
        s.scoreCloseButton = new Clickable()
                .viewport(s.scoreGroup)
                .metrics(new UIElement.Metrics().scale(351f / 792f).anchorBottom().pan(0, -0.5f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -4f)
                        .text("BACK")
                )
                .attach();


        s.scoreRowPlayer = new Sprite(151f / 784f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(s.scoreRowPlayer).set(0xff9827ff);
        StaticSprite row = new StaticSprite()
                .viewport(s.scoreGroup)
                .visual(s.scoreRowPlayer, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                ;

        // Line
        sprite = new Sprite(4f / 729f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(row)
                .metrics(new UIElement.Metrics().anchorBottom().scale(729f / 784f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        s.scoreRowNameView = new TextBox()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorTop().anchorLeft().move(+0.05f, -0.0f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(32f / 324f, -8f)
                        .bottomLeft()
                )
                .text("Teddy")
                .attach();
        s.scoreRowHoursPlayedView = new TextBox()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorBottom().anchorLeft().move(+0.05f, +0.0f))
                .text(new Text()
                        .font(hoursPlayedFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(25f / 324f, -16f)
                        .topLeft()
                )
                .text("played 9999 hours")
                .attach();
        s.scoreRowHoursPlayedFormat = "played %.1f hours";
        s.scoreRowScoreView = new TextBox()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(25f / 324f, -10f)
                        .centerRight()
                        .text("0")
                )
                .attach();
        s.scoreNormalFont = menuSubtitleFont;
        s.scorePlayerFont = playerLeaderboardFont;

        float y = -titleBg.getLength();
        float rowScale = 0.985f;
        float yInterval = -s.scoreRowPlayer.length * rowScale;
        s.scoreRows = new StaticSprite[6];
        for(int c = 0; c < s.scoreRows.length; c++) {
            s.scoreRows[c] = row.instantiate()
                    .viewport(s.scoreGroup)
                    .metrics(new UIElement.Metrics().anchorTop().move(0, y).scale(rowScale))
                    .attach();
            y += yInterval;
        }

        s.scorePlayerName = "You";
    }

    private void buildInviteFriendsMenu() {
        // Invite group
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 450f / 373f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.inviteGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(373f / 512f).move(0, -0.12f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(menuAppearAnim, null, menuDisappearAnim)
        ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 133f / 792f, 21f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.inviteGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new TextBox()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(23f / 324f, 0)
                        .text("INVITE FRIENDS")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/back-button.png");
        s.inviteCloseButton = new Clickable()
                .viewport(s.inviteGroup)
                .metrics(new UIElement.Metrics().scale(351f / 792f).anchorBottom().pan(0, -0.5f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -4f)
                        .text("BACK")
                )
                .attach();

        UIElement.Group row = new UIElement.Group()
                .viewport(s.inviteGroup)
                .length(0.14f);
        s.inviteRowProfileView = new StaticSprite()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(0.15f).anchorLeft().move(+0.05f, 0))
                .visual(Sprite.load("content/profiles/chats/teddy.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        s.inviteRowNameView = new TextBox()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorLeft().move(+0.09f, 0.0f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(+0.15f, 0, 1f, 25f / 324f, -10f)
                        .centerLeft()
                )
                .text("Teddy")
                .attach();

        sprite = Sprite.load("apps/flapee/invite-button.png");
        s.inviteRowSendButton = new Clickable()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(110f / 373f).anchorRight().move(-0.05f, 0))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -4.5f)
                        .text("INVITE")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/invite-disabled-button.png");
        s.inviteRowSentButton = new Clickable()
                .viewport(row)
                .metrics(new UIElement.Metrics().scale(110f / 373f).anchorRight().move(-0.05f, 0))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -4.5f)
                        .text("SENT")
                )
                .disable()
                .attach();


        float y = -0.19f;
        float yInterval = -0.18f;
        s.inviteRows = new UIElement.Group[5];
        for(int c = 0; c < s.inviteRows.length; c++) {
            s.inviteRows[c] = row.instantiate()
                    .viewport(s.inviteGroup)
                    .metrics(new UIElement.Metrics().anchorTop().move(0, y))
                    .attach();
            y += yInterval;
        }
    }

    private void buildMoreEggsSubMenu() {
        // More eggs group (IAP buttons)
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 150f / 373f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.moreEggsGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(373f / 512f).move(0, -0.16f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        new SequenceAnim(menuAppearAnim, 0.15f, true),
                        null,
                        menuDisappearAnim
                )
                ;
        // Deco
        new StaticSprite()
                .viewport(s.moreEggsGroup)
                .metrics(new UIElement.Metrics().scale(834f / 790f).anchorTop())
                .visual(Sprite.load("apps/flapee/roof.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        s.moreEggsAdOnlyGroup = new UIElement.Group()
                .viewport(s.moreEggsGroup)
                .length(s.moreEggsGroup.getLength())
                .attach();


        s.moreEggsAdAndInviteGroup = new UIElement.Group()
                .viewport(s.moreEggsGroup)
                .length(s.moreEggsGroup.getLength())
                ; // .attach();

        sprite = Sprite.load("apps/flapee/nest.png");
        new StaticSprite()
                .viewport(s.moreEggsAdAndInviteGroup)
                .metrics(new UIElement.Metrics().anchorBottom().scale(0.48f).move(-0.25f, -0.08f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new StaticSprite()
                .viewport(s.moreEggsAdAndInviteGroup)
                .metrics(new UIElement.Metrics().anchorBottom().scale(0.48f).move(+0.24f, -0.08f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new StaticSprite()
                .viewport(s.moreEggsAdOnlyGroup)
                .metrics(new UIElement.Metrics().anchorBottom().scale(0.51f).move(+0.23f, -0.02f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();


        new TextBox()
                .viewport(s.moreEggsAdAndInviteGroup)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorTop().move(0, -0.08f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(16f / 324f, 0)
                        .text("GET MORE EGGS")
                )
                .attach();

        new TextBox()
                .viewport(s.moreEggsAdOnlyGroup)
                .metrics(new UIElement.Metrics().scale(130f / 373f).anchorLeft().move(+0.07f, -0.01f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .centerLeft()
                )
                .autoLengthText("NEED FREE\nEGGS?")
                .attach();



        sprite = Sprite.load("apps/flapee/eggs-button.png");
        s.moreEggsWatchAdButton = new Clickable()
                .viewport(s.moreEggsAdAndInviteGroup)
                .metrics(new UIElement.Metrics().scale(140f / 373f).anchorBottom().pan(0, -0.5f).move(-0.23f, +0.13f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(+0.07f, +0.03f, 105f / 170f, 90f / 105f, 3.5f)
                        .centerLeft()
                        .text("WATCH AD")
                )
                .attach();
        StaticSprite eggIcon = new StaticSprite()
                .viewport(s.moreEggsWatchAdButton)
                .metrics(new UIElement.Metrics().scale(0.34f).anchor(-0.5f, 0).move(+0.03f, 0))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new RotateAnim(0.6f, RotateAnim.Location.BOTTOM, new SineGraph(1f, 3f, 0, new LinearGraph(20f, 0f), new ConstantGraph(-10f), null)),
                                new RotateAnim(0.4f, RotateAnim.Location.BOTTOM, new ConstantGraph(-10f))
                        }),
                        null
                )
                .attach();
        TextBox rewardText = new TextBox()
                .viewport(eggIcon)
                .metrics(new UIElement.Metrics().scale(1.5f))
                .text(new Text()
                        .font(eggBuyFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0.5f, -2.5f)
                        .text("+30")
                )
                .attach();

        s.moreEggsWatchAdSingleButton = s.moreEggsWatchAdButton.instantiate()
                .viewport(s.moreEggsAdOnlyGroup)
                .metrics(new UIElement.Metrics().scale(140f / 373f).move(+0.25f, -0.01f))
                .attach();

        s.moreEggsWatchAdNewSticker = newSticker.instantiate()
                .metrics(new UIElement.Metrics().scale(0.5f).anchor(+0.5f, +0.5f).move(-0.055f, -0.055f).rotate(45f))
                ;


        s.moreEggsInviteButton = s.moreEggsWatchAdButton.instantiate()
                .metrics(new UIElement.Metrics().scale(140f / 373f).anchorBottom().pan(0, -0.5f).move(+0.26f, +0.13f))
                .text("INVITE FRIENDS")
                .attach();
        s.moreEggsInviteButton.find(eggIcon)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new RotateAnim(0.3f, RotateAnim.Location.BOTTOM, new SineGraph(1f, 3f, 0, new LinearGraph(20f, 0f), new ConstantGraph(-10f), null)),
                                new RotateAnim(0.2f, RotateAnim.Location.BOTTOM, new ConstantGraph(-10f))
                        }),
                        null
                )
                ;
        s.moreEggsInviteButton.find(rewardText).text("+90");

        s.moreEggsInviteNewSticker = newSticker.instantiate()
                .viewport(s.moreEggsInviteButton)
                .metrics(new UIElement.Metrics().scale(0.5f).anchor(+0.5f, +0.5f).move(-0.055f, -0.055f).rotate(45f))
        ;

        sprite = Sprite.load("apps/flapee/egg-shop-button.png");
        s.moreEggsShopButton = new Clickable()
                .viewport(s.moreEggsGroup)
                .metrics(new UIElement.Metrics().scale(725f / 790f).anchorBottom().pan(0, -0.5f).move(0, -0.42f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.011f, 140 / 218f, 77f / 140, 5.9f)
                        .text("BUY EGGS")
                )
                .attach();      // TODO: attach on demand

        new StaticSprite()
                .viewport(s.moreEggsShopButton)
                .metrics(new UIElement.Metrics().scale(0.17f).anchorBottom().anchorLeft().move(+0.08f, +0.015f).rotate(-10f))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new RotateAnim(0.7f, RotateAnim.Location.BOTTOM, new SineGraph(1f, 3f, 0, new LinearGraph(9f, 0f), new ConstantGraph(-10f), null)),
                                new RotateAnim(0.2f, RotateAnim.Location.BOTTOM, new ConstantGraph(-10f))
                        }),
                        null
                )
                .attach();
        new StaticSprite()
                .viewport(s.moreEggsShopButton)
                .metrics(new UIElement.Metrics().scale(0.13f).anchorBottom().anchorLeft().move(+0.05f, +0.015f).rotate(+20f))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new RotateAnim(0.3f, RotateAnim.Location.BOTTOM, new SineGraph(1f, 3f, 0, new LinearGraph(-9f, 0f), new ConstantGraph(-10f), null)),
                                new RotateAnim(0.2f, RotateAnim.Location.BOTTOM, new ConstantGraph(-10f))
                        }),
                        null
                )
                .attach();
        new StaticSprite()
                .viewport(s.moreEggsShopButton)
                .metrics(new UIElement.Metrics().scale(0.18f).anchorBottom().anchorRight().move(-0.08f, +0.015f).rotate(+10f))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new RotateAnim(1.2f, RotateAnim.Location.BOTTOM, new SineGraph(1f, 3f, 0, new LinearGraph(-9f, 0f), new ConstantGraph(-10f), null)),
                                new RotateAnim(0.5f, RotateAnim.Location.BOTTOM, new ConstantGraph(-10f))
                        }),
                        null
                )
                .attach();
        new StaticSprite()
                .viewport(s.moreEggsShopButton)
                .metrics(new UIElement.Metrics().scale(0.11f).anchorBottom().anchorRight().move(-0.05f, +0.015f).rotate(+20f))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new RotateAnim(0.7f, RotateAnim.Location.BOTTOM, new SineGraph(1f, 3f, 0, new LinearGraph(+12f, 0f), new ConstantGraph(-10f), null)),
                                new RotateAnim(0.3f, RotateAnim.Location.BOTTOM, new ConstantGraph(-10f))
                        }),
                        null
                )
                .attach();

        s.moreEggsShopNewSticker = newSticker.instantiate()
                .viewport(s.moreEggsShopButton)
                .metrics(new UIElement.Metrics().scale(0.22f).anchor(+0.5f, +0.5f).move(-0.055f, -0.055f).rotate(45f))
                ;
    }

    private void buildDeathChanceMenu() {
        s.chanceMenu = new UIElement.Group()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().move(0, -0.09f))
                .length(0)
        ;

        // Powerup
        sprite = Sprite.load("apps/flapee/multiplier-button.png");
        s.chanceReviveButton = new Clickable()
                .viewport(s.chanceMenu)
                .metrics(new UIElement.Metrics().scale(535f / 1080f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        new SequenceAnim(new Animation [] {
                                new ScaleAnim(0.12f, new LinearGraph(1f, 1.15f)),
                                new ScaleAnim(0.18f, new LinearGraph(1.15f, 1f))
                        }),
                        new SequenceAnim(new Animation[] {
                                new NullAnim(0.5f),
                                new MoveAnim(0.5f, new SineGraph(1f, 3f, 0f, new LinearGraph(0.05f, 0f), null, null), null)
                        }),
                        buttonPressedAnim,
                        buttonReleasedAnim,
                        null
                )
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(+0.14f, +0.01f, 300f / 535f, 169f / 300f, 4f)
                        .centerLeft()
                        .text("REVIVE")
                )
                .attach();
        StaticSprite eggIcon = new StaticSprite()
                .viewport(s.chanceReviveButton)
                .metrics(new UIElement.Metrics().scale(0.18f).anchorLeft().move(+0.12f, 0).rotate(-10))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .attach();
        s.chanceReviveCostView = new TextBox()
                .viewport(eggIcon)
                .metrics(new UIElement.Metrics().scale(1.5f))
                .text(new Text()
                        .font(eggBuyFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0.3f, -2f)
                        .text("10")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/revive-disabled-button.png");
        s.chanceReviveTimerView = new StaticSprite()
                .viewport(s.chanceReviveButton)
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        s.chanceReviveTimerAnim = new ScissorAnim(1f, new Animation[] {
                new ScaleAnim(1f, ScaleAnim.Location.RIGHT, LinearGraph.zeroToOne, null)
        });

        sprite = Sprite.load("apps/flapee/back-button.png");
        s.chanceDieButton = new Clickable()
                .viewport(s.chanceMenu)
                .metrics(new UIElement.Metrics().scale(351f / 1080f).move(0, -0.19f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        new SequenceAnim(new ScaleAnim(0.3f, LinearGraph.zeroToOne), 0.3f, true),
                        null,
                        buttonPressedAnim,
                        buttonReleasedAnim,
                        null
                )
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.02f, 1f, sprite.length, 4.5f)
                        .text("DIE")
                )
                .attach();
    }

    private void buildRewardAcceptPopup() {
        // Reward accept menu
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 380f / 373f, 18f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.rewardGroup = new StaticSprite()
                .viewport(s.bars.viewport)
                .metrics(new UIElement.Metrics().scale(934f / 1080f).move(0, -0.12f))
                .visual(patch, SaraRenderer.TARGET_IRIS_OVERLAY)
                .animation(menuAppearAnim, null, menuDisappearAnim)
                ; // .attach();
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 133f / 792f, 18f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.rewardGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_IRIS_OVERLAY)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                .attach();
        new TextBox()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_IRIS_OVERLAY_TEXT)
                        .position(23f / 324f, 0)
                        .text("YOUR REWARD")
                )
                .attach();


        s.rewardVisualClosed = Sprite.load("apps/flapee/chest-closed.png");
        s.rewardVisualOpen = Sprite.load("apps/flapee/chest-open.png");
        s.rewardVisualView = new StaticSprite()
                .viewport(s.rewardGroup)
                .metrics(new UIElement.Metrics().scale(0.7f).move(+0.06f, -0.02f))
                .visual(s.rewardVisualClosed, SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE)
                .animation(
                        new SequenceAnim(new Animation[] {
                                new ScaleAnim(0.5f),
                                new CompoundAnim(0.25f, new Animation[] {
                                        new MoveAnim(1f, null, new QuadraticGraph(+1.7f, 0f, false)),
                                        new ScaleAnim(1f, new CompoundGraph(new Graph[] {
                                                new QuadraticGraph(0f, 1f, 0.4f, 0, false),
                                                new ConstantGraph(1f, 0.6f)
                                        })),
                                }),
                                new CompoundAnim(0.3f, new Animation[] {
                                        new ScaleAnim(1f, ScaleAnim.Location.BOTTOM,
                                                new SineGraph(1f, 3f, 0f, new LinearGraph(0.1f, 0f), new LinearGraph(1.1f, 1f), null),
                                                new SineGraph(1f, 3f, +0.5f, new LinearGraph(0.1f, 0f), new LinearGraph(1.1f, 1f), null)
                                        ),
                                        new MoveAnim(1f, null, new SineGraph(1f, 3f, +0.75f,
                                                new QuadraticGraph(+0.1f, 0f, true),
                                                new QuadraticGraph(+0.1f, 0f, true),
                                                null
                                        ))
                                })
                        }),
                        null,
                        null
                )
                .attach();

        // Stain bottom
        new StaticSprite()
                .viewport(s.rewardGroup)
                .metrics(new UIElement.Metrics().scale(0.7f).move(-0.01f, -0.22f))
                .visual(Sprite.load("apps/flapee/chest-pee.png"), SaraRenderer.TARGET_IRIS_OVERLAY)
                .animation(
                        new SequenceAnim(
                                new ScaleAnim(5.3f, new QuadraticGraph(0.5f, 1f, true)),
                                1.1f, true
                        ),
                        null,
                        null
                )
                .attach();

        s.rewardIdleAnim = new SequenceAnim(new Animation[] {
                new NullAnim(1f),
                new ColorAnim(0.1f, 1.5f, 1.5f, 1.5f, 1f, false),
                new NullAnim(0.1f),
                new ColorAnim(0.1f, 1.5f, 1.5f, 1.5f, 1f, false),
        });
        s.rewardOpenAnim = new ScaleAnim(0.5f, ScaleAnim.Location.BOTTOM,
                new SineGraph(1f, 3f, 0f, new QuadraticGraph(0.1f, 0f, true), new QuadraticGraph(1.1f, 1f, true), null),
                new SineGraph(1f, 3f, +0.5f, new QuadraticGraph(0.1f, 0f, true), new QuadraticGraph(1.1f, 1f, true), null)
        );
        s.tRewardStartDelay = 1.0f;
        s.tRewardEndDelay = 3f;

        s.rewardTextView = new TextBox()
                .viewport(s.rewardGroup)
                .metrics(new UIElement.Metrics().scale(0.4f).move(-0.15f, +0.65f))
                .text(new Text()
                        .font(eggRewardFont, SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE_TEXT)
                        .position(0.20f, 0)
                        .text("+10")
                        .centerRight()
                )
                .animation(
                        new CompoundAnim(0.5f, new Animation[] {
                                new FadeAnim(1f, LinearGraph.zeroToOne),
                                new MoveAnim(1f, null, new QuadraticGraph(-1.5f, 0f, true))
                        }),
                        null,
                        null
                )
                ; // .attach();
        new StaticSprite()
                .viewport(s.rewardTextView)
                .metrics(new UIElement.Metrics().scale(0.26f).anchorRight().pan(+1, 0).move(+0.05f, 0))
                .visual(Sprite.load("apps/flapee/egg.png"), SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE)
                .animation(
                        new FadeAnim(0.5f, LinearGraph.zeroToOne),
                        null,
                        null
                )
                .attach();

        s.rewardAcceptText = new TextBox()
                .viewport(s.rewardGroup)
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_IRIS_OVERLAY_TEXT)
                        .position(0, -0.39f, 0.7f, patch.length, -7.5f)
                        .text("Tap to Open")
                )
                .animation(
                        new ScaleAnim(0.7f),
                        new SequenceAnim(new Animation[] {
                                new ColorAnim(0.15f, 0x000000ff, false, 1),
                                new ColorAnim(0.15f, 0xffff00ff, false, 1),
                        }),
                        null
                );
        s.rewardAcceptButton = new Clickable()
                .viewport(s.rewardGroup)
                .length(s.rewardGroup.getLength())
                .attach();

        s.rewardAppearSound = Sound.load("sounds/flapee/lootbox.ogg");
        s.rewardOpenSound = Sound.load("sounds/flapee/lootbox-open.ogg");
    }

    private void buildShopMenu() {
//        sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.screenNoiseMaterial);
//        ColorAttribute.of(sprite).set(0x660000ff).alpha(0.7f);
//        s.shopBgView = new StaticSprite()
//                .viewport(s.window)
//                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB_TEXT)
//                .animation(
//                        new FadeAnim(0.4f, LinearGraph.zeroToOne),
//                        null,
//                        new FadeAnim(0.5f, LinearGraph.oneToZero)
//                )
//                ;

        Toast sigil = new Toast()
                .metrics(new UIElement.Metrics())
                .target(SaraRenderer.TARGET_INTERACTIVE_SUB_TEXT)
                ;

        int sigilColor = 0x660000ff;
        float sigilAlpha = 0.5f;

        Sprite sigil1 = Sprite.load("apps/flapee/sigils/1.png.NoiseMaterial");
        ColorAttribute.of(sigil1).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil2 = Sprite.load("apps/flapee/sigils/2.png.NoiseMaterial");
        ColorAttribute.of(sigil2).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil3 = Sprite.load("apps/flapee/sigils/3.png.NoiseMaterial");
        ColorAttribute.of(sigil3).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil4 = Sprite.load("apps/flapee/sigils/4.png.NoiseMaterial");
        ColorAttribute.of(sigil4).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil5 = Sprite.load("apps/flapee/sigils/5.png.NoiseMaterial");
        ColorAttribute.of(sigil5).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil6 = Sprite.load("apps/flapee/sigils/6.png.NoiseMaterial");
        ColorAttribute.of(sigil6).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil7 = Sprite.load("apps/flapee/sigils/7.png.NoiseMaterial");
        ColorAttribute.of(sigil7).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil8 = Sprite.load("apps/flapee/sigils/8.png.NoiseMaterial");
        ColorAttribute.of(sigil8).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil9 = Sprite.load("apps/flapee/sigils/9.png.NoiseMaterial");
        ColorAttribute.of(sigil9).set(sigilColor).alpha(sigilAlpha);
        Sprite sigil10 = Sprite.load("apps/flapee/sigils/10.png.NoiseMaterial");
        ColorAttribute.of(sigil10).set(sigilColor).alpha(sigilAlpha);

        s.shopSigils = new SetRandomizedSelector(new Toast[] {
                sigil.instantiate().visual(sigil1),
                sigil.instantiate().visual(sigil2),
                sigil.instantiate().visual(sigil3),
                sigil.instantiate().visual(sigil4),
                sigil.instantiate().visual(sigil5),
                sigil.instantiate().visual(sigil6),
                sigil.instantiate().visual(sigil7),
                sigil.instantiate().visual(sigil8),
                sigil.instantiate().visual(sigil9),
                sigil.instantiate().visual(sigil10)
        });

        s.shopSigilAnim = new SetRandomizedSelector(new Animation[] {
                new FadeAnim(2.5f, new SineGraph(1f, 3f, 0f, new LinearGraph(0.5f, 0f), new LinearGraph(0.5f, 0f), null)),
                new SequenceAnim(new Animation[] {
                        new FadeAnim(0.2f, new LinearGraph(0f, 1f)),
                        new FadeAnim(0.9f, new LinearGraph(1f, 0.5f)),
                        new FadeAnim(0.2f, new LinearGraph(0.5f, 1f)),
                        new FadeAnim(1.3f, new LinearGraph(1f, 0f)),
                }),
                new SequenceAnim(new Animation[] {
                        new FadeAnim(0.9f, new LinearGraph(1f, 0.2f)),
                        new FadeAnim(1.9f, new LinearGraph(0.2f, 0.2f)),
                        new FadeAnim(0.3f, new LinearGraph(0.2f, 0f)),
                }),
                new FadeAnim(2.8f, new VibrationGraph(1f, new CompoundGraph(new Graph[] {
                        new LinearGraph(0f, 0.3f, 0.2f),
                        new LinearGraph(0.3f, 0.3f, 0.7f),
                        new LinearGraph(0.3f, 0f, 0.1f),
                }), null))
        });

        s.shopSigilsGroup = new UIElement.Group()
                .viewport(s.window)
                .attach();

        s.shopSigilSize = new Range(0.3f, 0.4f);

        s.tShopSigilInterval = new Range(0.1f, 0.3f);

//        s.shopBuyGlitch = new MpegGlitch("sounds/glitch_start_low.ogg", null);

        s.shopBuyGlitch = new MpegGlitch(null, "sounds/flapee/theme-demon100-muted.ogg");
        s.shopBuyGlitch.setGlitchGraph(
                new CompoundGraph(new Graph[]{
                        new ConstantGraph(2.8f, 0.3f),
                        new ConstantGraph(0.0f, 4.2f),
                        new ConstantGraph(2.8f, 0.2f),
                        new ConstantGraph(0.0f, 0.5f),
                        new ConstantGraph(2.8f, 0.1f),
                        new ConstantGraph(0.0f, 3.8f),
                        new ConstantGraph(2.8f, 0.4f),
                        new ConstantGraph(0.0f, 1.4f),
                        new ConstantGraph(3.8f, 0.2f),
                        new ConstantGraph(0.0f, 5.6f),
                        new ConstantGraph(2.8f, 0.2f),
                        new ConstantGraph(0.0f, 0.6f),
                        new ConstantGraph(3.8f, 0.4f),
                        new ConstantGraph(0.0f, 6.1f),
                        new ConstantGraph(2.8f, 0.15f),
                        new ConstantGraph(0.0f, 0.3f),
                        new ConstantGraph(2.8f, 0.2f),
                        new ConstantGraph(0.0f, 0.2f),
                        new ConstantGraph(3.8f, 1.4f),
                        new ConstantGraph(0.0f, 4.8f)
                }),
                false,
                new LinearGraph(1.0f, 0f, 0.25f)
        );
        s.shopBuyGlitch.setGlitchLoopThreshold(0.5f);
        s.shopBuyGlitch.setLsdEffect(0.5f, 0);
        s.shopBuyGlitch.setGlitchLoopStopsPlayback(true);

        // Shop main window
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 1160f / 1007f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffc48aff);
        s.shopGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(450f / 512f).move(0, -0.11f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(menuAppearAnim, null, menuDisappearAnim)
                ; // .attach();
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 133f / 792f, 21f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.shopGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(3f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new TextBox()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(324f / 373f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(23f / 324f, -8f)
                        .text("Limited Time Offer")
                )
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new ColorAnim(0.07f, 0xffff00ff, false),
                                new ColorAnim(0.07f, 0xffffffff, false),
                        }),
                        null
                )
                .attach();

        sprite = Sprite.load("apps/flapee/back-button.png");
        s.shopCloseButton = new Clickable()
                .viewport(s.shopGroup)
                .metrics(new UIElement.Metrics().scale(365f / 1019f).anchorBottom().pan(0, -0.5f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -4f)
                        .text("BACK")
                )
                .attach();

        new StaticSprite()
                .viewport(s.shopGroup)
                .metrics(new UIElement.Metrics().scale(212f / 527f).anchorTop().move(0, -0.23f))
                .visual(Sprite.load("apps/flapee/egg-shop-visual.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();


        s.shopOfferView = new TextBox()
                .viewport(s.shopGroup)
                .metrics(new UIElement.Metrics().scale(471f / 527f).anchorTop().move(0, -0.55f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(131f / 471f, 11f)
                        .text("Buy [SHOP_BOLD]100 eggs[] for just [SHOP_BOLD]10000 hours[] of your life?")
                )
                .attach();
        s.shopOfferFormat = "Buy [SHOP_BOLD]%d eggs[] for just [SHOP_BOLD]%d hours[] of your life?";

        s.shopPurchaseButton = new Clickable()
                .viewport(s.shopGroup)
                .metrics(new UIElement.Metrics().scale(357f / 527f).anchorBottom().move(0, +0.14f))
                .visuals(Sprite.load("apps/flapee/jetstream-button.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(-0.11f, +0.005f, 214f / 337f, 31f / 214f, -5f)
                        .centerRight()
                        .text("GIVE 10000")
                )
                .animation(null, new CompoundAnim(2f, new Animation[] {
                        new SequenceAnim(new Animation[] {
                                new MoveAnim(0.3f, new SineGraph(1f, 3f, 0f, new QuadraticGraph(0.04f, 0f, true), null, null), null),
                                new NullAnim(0.3f),
                        }),
                        buttonIdleAnim
                }), buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .attach();
        s.shopPurchaseButtonFormat = "GIVE %d";

        sprite = Sprite.load("apps/flapee/heartlife.png").instantiate();
        new StaticSprite()
                .viewport(s.shopPurchaseButton)
                .metrics(new UIElement.Metrics().scale(0.14f).anchorRight().move(-0.11f, 0))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        s.shopEggsAmount = new int[] {
                100,
                200,
                300,
                400,
                500,
                750,
                1000,
        };
        s.shopLifeAmount = new int[] {
                100,
                250,
                500,
                1000,
                2500,
                5000,
                10000,
        };

        s.shopPurchasedThreshold = Globals.g_flapeeShopPurchasedThreshold;
    }

    private void buildPermissionsPopup() {
        // More eggs group (IAP buttons)
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 762f / 792f, 21f / 373f);
        ColorAttribute.of(patch).set(0xffca4aff);
        s.permissionGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(373f / 512f).move(0, -0.1f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(menuAppearAnim, null, menuDisappearAnim)
                ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 173f / 936f, 21f / 373f, 21f / 373f, 21f / 373f, 0);
        ColorAttribute.of(patch).set(0xff8a00ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.permissionGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.975f))
                .visual(Sprite.load("apps/flapee/ads-header.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new ColorAnim(0.2f, new ConstantGraph(1f), new ConstantGraph(10f)),
                                new NullAnim(0.2f),
                        }),
                        null
                )
                .attach();
        new StaticSprite()
                .viewport(s.permissionGroup)
                .metrics(new UIElement.Metrics().scale(0.23f).anchorTop().move(0, -0.26f))
                .visual(Sprite.load("apps/flapee/ads-icon.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        new ScaleAnim(0.2f, QuadraticGraph.zeroToOneInverted),
                        new SequenceAnim(new Animation[] {
                                new ScaleAnim(0.6f, new SineGraph(1f, 3f, 0f,
                                        new QuadraticGraph(0.3f, 0f, 0,false),
                                        new QuadraticGraph(1.3f, 1f, 0,false), null)),
                                new NullAnim(0.5f),
                        }),
                        null
                )
                .attach();



        new TextBox()
                .viewport(s.permissionGroup)
                .metrics(new UIElement.Metrics().anchorTop().move(0, -0.44f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(0.92f, 130f / 324f, 10f)
                        .text("Allow FlapeeBird to show [SHOP_BOLD]NON-STOP ADs[] all the time?")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/accept-button.png");
        s.permissionAcceptButton = new Clickable()
                .viewport(s.permissionGroup)
                .metrics(new UIElement.Metrics().scale(170f / 373f).anchorBottom().pan(+0.5f, -0.5f).move(+0.01f, 0f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(sprite.length, 4.5f)
                        .text("ACCEPT")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/back-button.png");
        s.permissionDenyButton = s.permissionAcceptButton.instantiate()
                .metrics(new UIElement.Metrics().scale(351f / 792f).anchorBottom().pan(-0.5f, -0.5f).move(-0.01f, 0f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .text("DENY")
                .attach();
    }

    private void buildUpdatingPopup() {
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 425f / 373f, 21f / 393f);
        ColorAttribute.of(patch).set(0x95bdffff);
        s.updatingGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(393f / 512f).move(0, -0.1f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        null,
                        menuDisappearAnim
                )
        ;

        // Loading circle
        sprite = Sprite.load("apps/flapee/loading-circle.png").instantiate();
        new StaticSprite()
                .viewport(s.updatingGroup)
                .metrics(new UIElement.Metrics().scale(0.27f).anchorTop().move(0, -0.24f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(null, new RotateAnim(0.8f, new LinearGraph(0, 360)), null)
                .attach()
        ;

        new TextBox()
                .viewport(s.updatingGroup)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorTop().move(0, -0.09f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(30f / 324f, -10f)
                        .text("Downloading new content")
                )
                .attach();

        s.updatingTextView = new TextBox()
                .viewport(s.updatingGroup)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorTop().move(0, -0.58f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(25f / 324f, -12f)
                        .text("5.31GB at 523KB/s")
                )
                .attach();

        new TextBox()
                .viewport(s.updatingGroup)
                .metrics(new UIElement.Metrics().scale(324f / 373f).anchorTop().move(0, -0.74f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .wrapChars(12f)
                )
                .autoLengthText("We are updating your game with exciting new features. Come back later!")
                .attach();

        sprite = Sprite.load("apps/flapee/back-button.png");
        s.updatingCloseButton = new Clickable()
                .viewport(s.updatingGroup)
                .metrics(new UIElement.Metrics().scale(170f / 373f).anchorBottom().pan(0, -0.5f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(sprite.length, 4.5f)
                        .text("Close")
                )
                .attach();

        s.updatingFormat = "%.2fGB at %.1fKB/s";
        s.updatingNumber = new Range(513f, 74f);
        s.tUpdatingNumberInterval = 0.5f;
    }

    private void buildDialogPopup() {
        // Time between dialogs
        s.tDialogRefreshDelay = 0.30f;

        // Bottom padding
        s.dialogLengthPadding = 0.40f;

        s.dialogContainer = new PatchedTextBox()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(393f / 512f).move(0, -0.1f))
                .visual("apps/flapee/window-bg.png", 21f / 393f, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(menuAppearAnim, new ColorAnim(0x95bdffff), menuDisappearAnim)
                ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 179f / 936f, 21f / 393f, 21f / 393f, 21f / 393f, 0);
        ColorAttribute.of(patch).set(0x3530ffff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(s.dialogContainer)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(5f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = Sprite.load("apps/flapee/survey-header.png").instantiate();
        ColorAttribute.of(sprite).set(0x3d40ffff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.985f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.19f))
                .visual(Sprite.load("apps/flapee/survey-icon.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();


        s.dialogGroup = new UIElement.Group()
                .viewport(s.dialogContainer)
                .metrics(new UIElement.Metrics().anchorTop())
                .attach();

        s.dialogTextView = new TextBox()
                .viewport(s.dialogGroup)
                .metrics(new UIElement.Metrics().anchorTop().move(0, -0.24f))
                .text(new Text()
                        .font(menuTitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(0.92f, 130f / 324f, 12f)
                        .text("Looks like you've been enjoying FlapeeBird!\n\nTell us how you feel by dropping us a rating!")
                )
                .attach();

        s.dialogStarGroup = new UIElement.Group()
                .viewport(s.dialogTextView)
                .metrics(new UIElement.Metrics().scale(0.69f).anchorBottom().pan(0, -1).move(0, -0.12f))
                .length(0.15f)
                .attach();

        patch = PatchedSprite.create("system/rounded.png", 336f / 851f, 13f / (373f * 0.72f));
        ColorAttribute.of(patch).set(0x313d7fff);
        new StaticSprite()
                .viewport(s.dialogStarGroup)
                .metrics(new UIElement.Metrics().scale(0.95f / 0.73f).anchorTop().move(0, +0.09f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();

        s.dialogStarSelectedMat = Sprite.load("apps/flapee/survey-star-filled.png");
        s.dialogStarDeselectedMat = Sprite.load("apps/flapee/survey-star-empty.png");
        s.dialogStarTopMat = Sprite.load("apps/flapee/survey-star-top.png");

        Clickable starButton = new Clickable()
                .viewport(s.dialogStarGroup)
                .visuals(s.dialogStarDeselectedMat, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                ;

        float x = -0.5f;

        // Star views
        s.dialogStarViews = new Clickable[5];
        for(int c = 0; c < s.dialogStarViews.length; c++) {
            Clickable b = starButton.instantiate()
                    .metrics(new UIElement.Metrics().scale(0.29f).move(x, 0))
                    .animation(new SequenceAnim(menuAppearAnim, 0.05f * c, true), buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                    .attach();
            s.dialogStarViews[c] = b;

            x += 1f / ((float)s.dialogStarViews.length - 1);
        }

        s.dialogStarHintAnim = new SequenceAnim(new Animation[]{
                new ScaleAnim(0.12f, new ConstantGraph(1.2f)),
                new NullAnim(0.12f),
                new ScaleAnim(0.12f, new ConstantGraph(1.2f)),
                new NullAnim(0.12f),
                new ScaleAnim(0.12f, new ConstantGraph(1.2f)),
                new NullAnim(0.12f),
                new ScaleAnim(0.12f, new ConstantGraph(1.2f))
        });

        // Star labels
        s.dialogStartLeftTextView = new TextBox()
                .viewport(s.dialogStarGroup)
                .metrics(new UIElement.Metrics().scale(0.55f).anchorBottom().anchorLeft().pan(0, -1).move(-0.16f / 2f, -0.09f))
                .text(new Text()
                        .font(dialogFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(0.16f, -5f)
                        .centerLeft()
                        .text("Likely")
                )
                .animation(new SequenceAnim(menuAppearAnim, 0.05f * 5, true), null, null)
                .attach();
        s.dialogStartRightTextView = s.dialogStartLeftTextView.instantiate()
                .metrics(new UIElement.Metrics().scale(0.55f).anchorBottom().anchorRight().pan(0, -1).move(+0.16f / 2f, -0.09f))
                .text("Unlikely")
                .animation(new SequenceAnim(menuAppearAnim, 0.05f * 6, true), null, null)
                .attach();
        s.dialogStartRightTextView.text().centerRight();

        // Buttons

        sprite = Sprite.load("apps/flapee/accept-button.png");
        s.dialogPositiveButton = new Clickable()
                .viewport(s.dialogContainer)
                .metrics(new UIElement.Metrics().scale(351f / 792f).anchorBottom().pan(+0.5f, -0.5f).move(+0.01f, 0f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(new SequenceAnim(menuAppearAnim, 0.1f * 1, true), buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(sprite.length, 5.5f)
                        .text("ACCEPT")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/back-button.png");
        s.dialogNegativeButton = s.dialogPositiveButton.instantiate()
                .metrics(new UIElement.Metrics().scale(351f / 792f).anchorBottom().pan(-0.5f, -0.5f).move(-0.01f, 0f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(new SequenceAnim(menuAppearAnim, 0.1f * 2, true), buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .text("DENY")
                .attach();

        s.dialogSingleButton = s.dialogPositiveButton.instantiate()
                .metrics(new UIElement.Metrics().scale(351f / 792f).anchorBottom().pan(0, -0.5f).move(-0.01f, 0f))
                .animation(new SequenceAnim(menuAppearAnim, 0.1f * 1, true), buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                ; // .attach();
    }

    private void buildShowdown() {
        // Giveup teddy trusts
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 1114f / 934f, 18f / 430f);
        ColorAttribute.of(patch).set(0xffc48aff);
        stt.giveupGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(900f / 1080f).move(0, -0.14f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        null,
                        menuDisappearAnim
                )
        ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 171f / 934f, 18f / 430f, 18f / 430f, 18f / 430f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        StaticSprite titleBg = new StaticSprite()
                .viewport(stt.giveupGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(4f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.90f).move(0, -0.02f))
                .visual(Sprite.load("apps/flapee/giveup-title.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();


        sprite = Sprite.load("apps/flapee/jetstream-button.png");
        stt.giveupYesButton = new Clickable()
                .viewport(stt.giveupGroup)
                .metrics(new UIElement.Metrics().scale(703f / 934f).anchorBottom().move(0, +0.115f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.01f, 1f, sprite.length, -9f)
                        .text("He is a lost cause")
                )
                .animation(null, new CompoundAnim(2f, new Animation[] {
                        new SequenceAnim(new Animation[] {
                                new MoveAnim(0.3f, new SineGraph(1f, 3f, 0f, new QuadraticGraph(0.04f, 0f, true), null, null), null),
                                new NullAnim(0.3f),
                        }),
                        buttonIdleAnim
                }), buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .attach();

        sprite = Sprite.load("apps/flapee/jetstream-disabled-button.png");
        stt.giveupNoButton = new Clickable()
                .viewport(stt.giveupGroup)
                .metrics(new UIElement.Metrics().scale(650f / 934f).anchor(0, -0.5f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.01f, 1f, sprite.length, -9f)
                        .text("I won't allow it")
                )
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .attach();

        new TextBox()
                .viewport(stt.giveupGroup)
                .metrics(new UIElement.Metrics().scale(0.83f).anchorTop().move(0, -0.27f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .position(100f / 324f, 0f)
                        .text("Allow Teddy to leave this \n" +
                                "mortal world into his own \n" +
                                "eternal paradise?")
                )
                .attach();

        new StaticSprite()
                .viewport(stt.giveupGroup)
                .metrics(new UIElement.Metrics().scale(407f / 934f).anchorTop().move(0, -0.52f))
                .visual(Sprite.load("apps/flapee/giveup-ghost.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
//                        new FadeAnim(3f, new SineGraph(1f, 8f, 0, new LinearGraph(0.1f, 0f), new LinearGraph(0f, 1f), null)),
                        new ScaleAnim(0.4f, new CompoundGraph(new Graph[] {
                                new QuadraticGraph(0f, 1.3f, 0.8f, 0, false),
                                new QuadraticGraph(1.3f, 1f, 0.2f, 0, true),
                        })),
                        new CompoundAnim(3f, new Animation[] {
                                new MoveAnim(1f,
                                        new SineGraph(1f, 2f, 0, 0.1f, 0f),
                                        new SineGraph(1f, 1f, 0f, 0.05f, 0f)
                                ),
                                new RotateAnim(1f, new SineGraph(1f, 3f, 0f, 5f, 0))
                        }),
                        null
                )
                .attach();

        // Giveup teddy doesnt trust
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 1100f / 934f, 18f / 430f);
        ColorAttribute.of(patch).set(0xffc48aff);
        stn.giveupGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(900f / 1080f).move(0, -0.14f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        null,
                        menuDisappearAnim
                )
        ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 171f / 934f, 18f / 430f, 18f / 430f, 18f / 430f, 0);
        ColorAttribute.of(patch).set(0xff7519ff);
        titleBg = new StaticSprite()
                .viewport(stn.giveupGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(4f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.90f).move(0, -0.02f))
                .visual(Sprite.load("apps/flapee/giveup-title.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();


        sprite = Sprite.load("apps/flapee/jetstream-button.png");
        stn.giveupYesButton = new Clickable()
                .viewport(stn.giveupGroup)
                .metrics(new UIElement.Metrics().scale(703f / 934f).anchorBottom().move(0, +0.115f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.01f, 1f, sprite.length, -9f)
                        .text("Yes I am a loser")
                )
                .animation(null, new CompoundAnim(2f, new Animation[] {
                        new SequenceAnim(new Animation[] {
                                new MoveAnim(0.3f, new SineGraph(1f, 3f, 0f, new QuadraticGraph(0.04f, 0f, true), null, null), null),
                                new NullAnim(0.3f),
                        }),
                        buttonIdleAnim
                }), buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .inputPadding(1f, 0.02f, 1f, 0.02f)
                .attach();

        sprite = Sprite.load("apps/flapee/jetstream-disabled-button.png");
        stn.giveupNoButton = new Clickable()
                .viewport(stn.giveupGroup)
                .metrics(new UIElement.Metrics().scale(650f / 934f).anchor(0, -0.5f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(0, +0.01f, 1f, sprite.length, -9f)
                        .text("I'm not done yet")
                )
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .inputPadding(1f, 0.02f, 1f, 0.02f)
                .attach();

        new TextBox()
                .viewport(stn.giveupGroup)
                .metrics(new UIElement.Metrics().scale(0.83f).anchorTop().move(0, -0.27f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .text("You are an even bigger\n" +
                                "\n" +
                                "\n" +
                                "\n" +
                                "than he is! You agree that\n" +
                                "Teddy is forever and always\n" +
                                "will be better than you?")
                        .autoLength()
                )
                .attach();

        new StaticSprite()
                .viewport(stn.giveupGroup)
                .metrics(new UIElement.Metrics().scale(817f / 934f).anchorTop().move(0, -0.35f))
                .visual(Sprite.load("apps/flapee/giveup-loser.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new ScaleAnim(0.18f, new ConstantGraph(1.05f)),
                                new NullAnim(0.18f),
                        }),
                        null
                )
                .attach();

        stn.giveupVoice = stt.giveupVoice = new SetRandomizedSelector(new DemonVoice[] {
                new DemonVoice("content/vo/demon/session3_giveup_1.ogg", false, false),
                new DemonVoice("content/vo/demon/session3_giveup_2.ogg", false, false),
                new DemonVoice("content/vo/demon/session3_giveup_3.ogg", false, false),
        });


        // Subscription window
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 1414f / 874f, 15f / 430f);
        ColorAttribute.of(patch).set(0xffc48aff);
        stn.subscribeGroup = stt.subscribeGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(900f / 1080f).move(0, +0.05f))
                .visual(patch, SaraRenderer.TARGET_APPBAR_BG)
                .animation(
                        menuAppearAnim,
                        null,
                        menuDisappearAnim
                )
        ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 141f / 934f, 15f / 430f, 15f / 430f, 15f / 430f, 0);
        ColorAttribute.of(patch).set(0xc92f00ff);
        titleBg = new StaticSprite()
                .viewport(stt.subscribeGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_APPBAR_BG)
                .attach();
        sprite = new Sprite(4f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_APPBAR)
                .attach();
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.80f).move(0, -0.005f))
                .visual(Sprite.load("apps/flapee/sub-title.png"), SaraRenderer.TARGET_APPBAR)
                .animation(
                        null,
                        new SequenceAnim(new Animation[] {
                                new ColorAnim(0.18f, new ConstantGraph(2f), null),
                                new NullAnim(0.18f),
                        }),
                        null
                )
                .attach();

        new StaticSprite()
                .viewport(stt.subscribeGroup)
                .metrics(new UIElement.Metrics().scale(621f / 874f).anchorTop().move(0, -0.18f))
                .visual(Sprite.load("apps/flapee/sub-vipee.png"), SaraRenderer.TARGET_APPBAR)
                .attach();


        // Bottom content
        new TextBox()
                .viewport(stt.subscribeGroup)
                .metrics(new UIElement.Metrics().scale(0.83f).anchorBottom().move(0, +0.60f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_APPBAR_TEXT)
                        .text("Be one with the game and \n" +
                                "transform into an unstoppable \n" +
                                "gaming force with:")
                        .autoLength()
                )
                .attach();

        new StaticSprite()
                .viewport(stt.subscribeGroup)
                .metrics(new UIElement.Metrics().scale(759f / 874f).anchorBottom().move(0, +0.43f))
                .visual(Sprite.load("apps/flapee/sub-unlimited.png"), SaraRenderer.TARGET_APPBAR)
                .animation(
                        null,
                        new ScaleAnim(1f,
                                new SineGraph(1f, 1f, 0.5f, 0.01f, 1.01f),
                                new SineGraph(1f, 1f, 0f, 0.05f, 1.05f)
                        ),
                        null
                )
                .attach();

        new TextBox()
                .viewport(stt.subscribeGroup)
                .metrics(new UIElement.Metrics().scale(0.7f).anchorBottom().move(0, +0.36f))
                .text(new Text()
                        .font(fineprintFont, SaraRenderer.TARGET_APPBAR_TEXT)
                        .text("Once accepted this deal is irreversible.")
                        .autoLength()
                )
                .attach();


        // Chest animation
        StaticSprite chestVew = new StaticSprite()
                .viewport(stt.subscribeGroup)
                .metrics(new UIElement.Metrics().scale(0.6f).anchorTop().move(+0.05f, -0.30f))
                .visual(Sprite.load("apps/flapee/chest-closed.png"), SaraRenderer.TARGET_APPBAR)
                .animation(
                        new ScaleAnim(0.4f, new CompoundGraph(new Graph[] {
                                new ConstantGraph(0f, 0.4f),
                                new QuadraticGraph(0f, 1.2f, 0.4f, 0, true),
                                new QuadraticGraph(1.2f, 1f, 0.2f, 0, false)
                        })),
                        null,
                        null
                )
                .attach();

        sprite = Sprite.load("apps/flapee/celebration-rays.png").instantiate();
        ColorAttribute.of(sprite).set(0xfffa8aff).alpha(0.6f);
        new StaticSprite()
                .viewport(chestVew)
                .metrics(new UIElement.Metrics().scale(2.0f).move(-0.05f, -0.1f))
                .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                .animation(
                        new FadeAnim(1f, QuadraticGraph.zeroToOneInverted),
                        new CompoundAnim(2.5f, new Animation[] {
                                new ScaleAnim(6f, new SineGraph(1f, 3f, 0f, 0.02f, 1.02f)),
                                new RotateAnim(10f, new LinearGraph(0f, 360f))
                        }),
                        null
                )
                .attach();



        sprite = Sprite.load("apps/flapee/back-button.png");
        stn.subscribeNoButton = stt.subscribeNoButton = new Clickable()
                .viewport(stt.subscribeGroup)
                .metrics(new UIElement.Metrics().scale(365f / 1019f).anchorBottom().pan(0, -0.5f))
                .visuals(sprite, SaraRenderer.TARGET_APPBAR)
                .animation(null, buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                        .position(0, +0.015f, 1f, sprite.length, -4f)
                        .text("NO")
                )
                .attach();

        stn.subscribeYesButton = stt.subscribeYesButton = new Clickable()
                .viewport(stt.subscribeGroup)
                .metrics(new UIElement.Metrics().scale(702f / 874f).anchorBottom().move(0, +0.12f))
                .visuals(Sprite.load("apps/flapee/jetstream-button.png"), SaraRenderer.TARGET_APPBAR)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                        .position(-0.09f, +0.010f, 214f / 337f, 31f / 214f, -5f)
                        .centerRight()
                        .text("SUBSCRIBE WITH")
                )
                .animation(null, new CompoundAnim(2f, new Animation[] {
                        new SequenceAnim(new Animation[] {
                                new MoveAnim(0.3f, new SineGraph(1f, 3f, 0f, new QuadraticGraph(0.04f, 0f, true), null, null), null),
                                new NullAnim(0.3f),
                        }),
                        buttonIdleAnim
                }), buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .attach();

        sprite = Sprite.load("apps/flapee/heartlife.png").instantiate();
        new StaticSprite()
                .viewport(stt.subscribeYesButton)
                .metrics(new UIElement.Metrics().scale(0.14f).anchorRight().move(-0.09f, 0))
                .visual(sprite, SaraRenderer.TARGET_APPBAR)
                .attach();

        stt.subscribeVoice = new SetRandomizedSelector(new DemonVoice[] {
                new DemonVoice("content/vo/demon/session3A_subscription_1.ogg", true, false),
                new DemonVoice("content/vo/demon/session3A_subscription_2.ogg", true, false),
                new DemonVoice("content/vo/demon/session3A_subscription_3.ogg", true, false),
        });

        stn.subscribeVoice = new SetRandomizedSelector(new DemonVoice[] {
                new DemonVoice("content/vo/demon/session3B_subscription_1.ogg", true, false),
                new DemonVoice("content/vo/demon/session3B_subscription_2.ogg", true, false),
                new DemonVoice("content/vo/demon/session3B_subscription_3.ogg", true, false),
        });

        stt.subscribedVoice = stn.subscribedVoice = new DemonVoice("content/vo/demon/session3_acceptsubscription.ogg", true, false);

        stt.tLifehoursDrainInterval = stn.tLifehoursDrainInterval = 3f;
        stt.lifehoursDrainAmount = stn.lifehoursDrainAmount = 5000;
        stt.lifehoursMinDrainCap = stn.lifehoursMinDrainCap = 100;
        stt.lifehoursDrainEggs = stn.lifehoursDrainEggs = 9999;


        // Showdown dialog
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 760f / 934f, 18f / 430f);
        ColorAttribute.of(patch).set(0xffc48aff);
        stt.showdownGroup = new StaticSprite()
                .viewport(s.window)
                .metrics(new UIElement.Metrics().scale(900f / 1080f).move(0, -0.05f))
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        menuAppearAnim,
                        null,
                        menuDisappearAnim
                )
        ;
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 266f / 934f, 18f / 430f, 18f / 430f, 18f / 430f, 0);
        ColorAttribute.of(patch).set(0xf26522ff);
        titleBg = new StaticSprite()
                .viewport(stt.showdownGroup)
                .metrics(new UIElement.Metrics().anchorTop())
                .visual(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        sprite = new Sprite(4f / 792f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().anchorBottom())
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        // Vs with profiles
        new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.19f).move(0, 0))
                .visual(Sprite.load("apps/flapee/showdown-vs.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        StaticSprite teddyProfileView = new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.20f).move(+0.27f, 0))
                .visual(Sprite.load("content/profiles/chats/teddy.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .attach();
        StaticSprite playerProfileView = new StaticSprite()
                .viewport(titleBg)
                .metrics(new UIElement.Metrics().scale(0.20f).move(-0.27f, 0))
                .visual(Sprite.load("content/profiles/chats/player.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .attach();
        sprite = Sprite.load("system/circle-medium.png").instantiate();
        ColorAttribute.of(sprite).set(0x000000ff);
        new StaticSprite()
                .viewport(playerProfileView)
                .metrics(new UIElement.Metrics().scale(1.05f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();
        new StaticSprite()
                .viewport(teddyProfileView)
                .metrics(new UIElement.Metrics().scale(1.05f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .attach();


        // Top anim
        new StaticSprite()
                .viewport(stt.showdownGroup)
                .metrics(new UIElement.Metrics().scale(780f / 900f).anchor(0, +0.5f).move(+0.51f, +0.16f))
                .visual(Sprite.load("apps/flapee/showdown-title-bg.png"), SaraRenderer.TARGET_INTERACTIVE_SUB)
                .animation(
                        new MoveAnim(2.8f, new CompoundGraph(new Graph[] {
                                new LinearGraph(+0.8f, +0.1f, 0.2f),
                                new QuadraticGraph(+0.1f, 0f, 0.8f, 0, true)
                        }), null),
                        null,
                        null
                )
                .attach();
        new StaticSprite()
                .viewport(stt.showdownGroup)
                .metrics(new UIElement.Metrics().scale(888f / 900f).anchor(0, +0.5f).move(0, +0.26f))
                .visual(Sprite.load("apps/flapee/showdown-title-bg.png"), SaraRenderer.TARGET_INTERACTIVE_SUB)
                .animation(
                        new MoveAnim(2.5f, new CompoundGraph(new Graph[] {
                                new LinearGraph(-0.8f, -0.1f, 0.2f),
                                new QuadraticGraph(-0.1f, 0f, 0.8f, 0, true)
                        }), null),
                        null,
                        null
                )
                .attach();
        new StaticSprite()
                .viewport(stt.showdownGroup)
                .metrics(new UIElement.Metrics().scale(700f / 900f).anchor(0, +0.5f).move(-0.59f, +0.10f))
                .visual(Sprite.load("apps/flapee/showdown-title-bg.png"), SaraRenderer.TARGET_INTERACTIVE_SUB)
                .animation(
                        new MoveAnim(2.3f, new CompoundGraph(new Graph[] {
                                new LinearGraph(-0.8f, -0.1f, 0.2f),
                                new QuadraticGraph(-0.1f, 0f, 0.8f, 0, true)
                        }), null),
                        null,
                        null
                )
                .attach();

        StaticSprite titleView = new StaticSprite()
                .viewport(stt.showdownGroup)
                .metrics(new UIElement.Metrics().scale(594f / 900f).anchor(0, +0.5f).move(0, +0.18f))
                .visual(Sprite.load("apps/flapee/showdown-title.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                .animation(
                        new SequenceAnim(new Animation[] {
                                new CompoundAnim(0.4f, new Animation[] {
                                        new ColorAnim(1f, 10f, 10f, 10f, 1f, true),
                                        new ScaleAnim(1f, new CompoundGraph(new Graph[] {
                                                new QuadraticGraph(0f, 1.2f, 0.8f, 0, false),
                                                new QuadraticGraph(1.2f, 1f, 0.2f, 0, true),
                                        }))
                                }),
                                new ColorAnim(2f, new QuadraticGraph(10f, 1f, true), null)
                        }),
                        null,
                        null
                )
                .attach();

        sprite = Sprite.load("apps/flapee/celebration-rays.png").instantiate();
        ColorAttribute.of(sprite).set(0xfffa8aff).alpha(0.6f);
        new StaticSprite()
                .viewport(titleView)
                .metrics(new UIElement.Metrics().scale(2.0f).move(-0.05f, -0.1f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                .animation(
                        new FadeAnim(1f, QuadraticGraph.zeroToOneInverted),
                        new CompoundAnim(9.5f, new Animation[] {
                                new ScaleAnim(6f, new SineGraph(1f, 3f, 0f, 0.02f, 1.02f)),
                                new RotateAnim(10f, new LinearGraph(0f, 360f))
                        }),
                        null
                )
                .attach();


        // Text
        TextBox textView = new TextBox()
                .viewport(stt.showdownGroup)
                .metrics(new UIElement.Metrics().scale(0.83f).anchorTop().move(0, -0.35f))
                .text(new Text()
                        .font(menuSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                        .wrapChars(12f)
                        .text("Teddy has challenged you to a showdown. If you think that he deserves to be redeemed, then prove it!")
                        .autoLength()
                )
                .attach();

        sprite = Sprite.load("apps/flapee/accept-button.png");
        stt.showdownAcceptButton = new Clickable()
                .viewport(stt.showdownGroup)
                .metrics(new UIElement.Metrics().scale(351f / 792f).anchorBottom().pan(+0.5f, -0.5f).move(+0.01f, 0f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(new SequenceAnim(menuAppearAnim, 0.1f * 1, true), buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(sprite.length, 5.5f)
                        .text("ACCEPT")
                )
                .attach();

        sprite = Sprite.load("apps/flapee/back-button.png");
        stt.showdownLaterButton = new Clickable()
                .viewport(stt.showdownGroup)
                .metrics(new UIElement.Metrics().scale(351f / 792f).anchorBottom().pan(-0.5f, -0.5f).move(-0.01f, 0f))
                .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .animation(new SequenceAnim(menuAppearAnim, 0.1f * 2, true), buttonIdleAnim, buttonPressedAnim, buttonReleasedAnim, null)
                .sound(null, buttonPressedSound, buttonReleasedSound)
                .text(new Text()
                        .font(menuButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .position(sprite.length, 5.5f)
                        .text("LATER")
                )
                .attach();

        stt.showdownVoice = new DemonVoice("content/vo/demon/session3A_showdown.ogg", true, false);

        // No trust showdown dialog
        stn.showdownGroup = stt.showdownGroup.instantiate();
        patch = PatchedSprite.create("apps/flapee/window-bg.png", 840f / 934f, 18f / 430f);
        ColorAttribute.of(patch).set(0xffc48aff);
        stn.showdownGroup.visual(patch);

        stn.showdownAcceptButton = stn.showdownGroup.find(stt.showdownAcceptButton);
        stn.showdownLaterButton = stn.showdownGroup.find(stt.showdownLaterButton);

        // Customize
        stn.showdownGroup.find(textView).autoLengthText("Teddy has challenged you to a showdown. This prick has been on your business for long enough! Show him how much of a loser he truly is.");
        stn.showdownGroup.find(playerProfileView).visual(Sprite.load("content/profiles/chats/player.png"));

        stn.showdownVoice = new DemonVoice("content/vo/demon/session3B_showdown.ogg", true, false);
    }

    public GBFlapeeBirdScreen(FlapeeBirdScreen screen) {

        buildBase();
        buildFindingFriendsPopup();
        buildResourceIcons();
        buildIngamePowerupButtons();
        buildSendingEggsPopup();
        buildMoreEggsSubMenu();
        buildAdvancedMenu();
        buildBasicMenu();
        buildHighScoreMenu();
        buildInviteFriendsMenu();
        buildDeathChanceMenu();
        buildRewardAcceptPopup();
        buildShopMenu();
        buildPermissionsPopup();

        buildDialogPopup();

        buildUpdatingPopup();

        buildShowdown();


        // Add showdowns
        s.showdowns.put("teddy_trust", stt);
        s.showdowns.put("teddy_no_trust", stn);

        // Commit
        screen.setInternal(s);

    }
}
