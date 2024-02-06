package game31.gb;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

import game31.Globals;
import game31.MainMenu;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.NullAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.SequenceAnim;
import sengine.calc.ConstantGraph;
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
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.LoadingMenu;

/**
 * Created by Azmi on 9/4/2017.
 */

public class GBMainMenu {

    public static Animation startAnim(float tDelay) {
        return new SequenceAnim(new Animation[] {
                new ScaleAnim(tDelay),
                new CompoundAnim(0.15f, new Animation[] {
                        new ScaleAnim(1f, new QuadraticGraph(0.8f, 1.0f, true)),
                        new FadeAnim(1f, QuadraticGraph.zeroToOneInverted),
                        new FadeAnim(1f, QuadraticGraph.zeroToOneInverted, 1),
                })
        });
    }

    public GBMainMenu(MainMenu menu) {
        MainMenu.Internal s = new MainMenu.Internal();

        Sprite sprite;
        PatchedSprite patch;

        Font volumeFont = new Font("opensans-light.ttf", 48, 0xf2f2f2ff);
        Font headphonesFont = new Font("opensans-regular.ttf", 48, 0xf2f2f2ff);

        Font noticeFont = new Font("opensans-semibold.ttf", 48, 0xffffffff);

        Font loadingFont = new Font("opensans-light.ttf", 32, 0xffffffff);

        Font copyrightFont = new Font("opensans-light.ttf", 32, 0xffffff33);

        Font helpButtonFont = new Font("opensans-bold.ttf", 32, 0xffffff44);


        Font dialogFont = new Font("opensans-regular.ttf", 48, 0x000000ff);
        Font dialogButtonFont = new Font("opensans-semibold.ttf", 48, 0x464649ff);

        Font simAdFont = new Font("opensans-bold.ttf", 48);


        int pressedColor = 0xaaaaaaff;
        Animation pressedAnim = new ColorAnim(pressedColor);

        Animation releasedAnim = new SequenceAnim(new Animation[]{
                new NullAnim(0.04f),
                new ColorAnim(0.08f, pressedColor, true),
                new NullAnim(0.04f),
                new ColorAnim(0.03f, pressedColor, true),
        });
        Animation titleHideAnim = new CompoundAnim(0.5f, new Animation[] {
                new FadeAnim(0.5f, QuadraticGraph.oneToZero),
                new FadeAnim(0.5f, QuadraticGraph.oneToZero, 1),
        });



        {
            s.window = new UIElement.Group();

            float windowY = - (+277f / 2250f) - 0.01f;

            // Screen
            s.screenGroup = new UIElement.Group()
                    .viewport(s.window)
                    ; // .attach();
            // Bg
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.screenNoiseMaterial);
            ColorAttribute.of(sprite).set(0x222222ff);
            new StaticSprite()
                    .viewport(s.screenGroup)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();
            // Bottom
            sprite = new Sprite(0.06f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x020202ff);
            new StaticSprite()
                    .viewport(s.screenGroup)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();


            s.screen = new StaticSprite()
                    .viewport(s.screenGroup)
                    .metrics(new UIElement.Metrics().scale(1.07f).anchorBottom().move(0, (+329f / 2250f) + windowY))
                    .visual(Sprite.load("menu/screen.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            null,
                            new FadeAnim(1f, new VibrationGraph(1f, 0.4f, 0.4f)),
                            null
                    )
                    .attach();

            // Glows
            s.baseGlow = new StaticSprite()
                    .viewport(s.screenGroup)
                    .metrics(new UIElement.Metrics().scale(1f).anchorBottom().move(0, (+457f / 2250f) + windowY))
                    .visual(Sprite.load("menu/glow-base.png"), SaraRenderer.TARGET_BG_SHADOWS)
                    .animation(
                            null,
                            new FadeAnim(1f, new VibrationGraph(1f, 0.1f, 0.9f)),
                            null
                    )
                    .attach();

            s.fullGlow = new StaticSprite()
                    .viewport(s.screenGroup)
                    .metrics(new UIElement.Metrics().scale(1f).scale(1f, 1.2f).anchorBottom().move(0, (+277f / 2250f) + windowY))
                    .visual(Sprite.load("menu/glow-full.png"), SaraRenderer.TARGET_BG)
                    .animation(
                            null,
                            new CompoundAnim(20f, new Animation[]{
//                                    new ColorAnim(1.2f, 1.2f, 1.2f, 1f),
                                    new FadeAnim(1f, new SineGraph(1f, 5f, 0f, 0.1f, 0.9f)),
//                                    new FadeAnim(1f, new VibrationGraph(1f, 0.05f, 0.95f)),
                            }),
                            null
                    )
                    .attach();
            ConstantGraph lowGraph = new ConstantGraph(0.9f);
            ConstantGraph veryLowGraph = new ConstantGraph(0.6f);
            s.fullGlow.windowAnimation(new SequenceAnim(new Animation[]{
                    new FadeAnim(1.4f, ConstantGraph.one),
                    new FadeAnim(0.8f, lowGraph),
                    new FadeAnim(3.4f, ConstantGraph.one),
                    new FadeAnim(1.2f, lowGraph),
                    new FadeAnim(0.9f, ConstantGraph.one),
                    new FadeAnim(0.6f, veryLowGraph),
                    new FadeAnim(2.6f, ConstantGraph.one),
                    new FadeAnim(1.6f, lowGraph),
                    new FadeAnim(4.2f, ConstantGraph.one),
                    new FadeAnim(0.4f, veryLowGraph),
            }).loopAndReset(), true, true);


            // Shadow
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff).alpha(0.6f);
            s.offShadow = new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_BG)
                    .animation(
                            new SequenceAnim(new Animation[]{
                                    new ColorAnim(0.05f, 0x000000aa, false),
                                    new ColorAnim(0.07f, 0x00000044, false),
                                    new ColorAnim(0.025f, 0x000000ff, false),
                                    new ColorAnim(0.025f, 0x00000022, false),
                                    new ColorAnim(0.3f, null, new QuadraticGraph(1f, 0f, false), false)
                            }),
                            new FadeAnim(4.1f, ConstantGraph.zero),
                            new SequenceAnim(new Animation[]{
                                    new ColorAnim(0.1f, 0x00000044, false),
                                    new ColorAnim(0.05f, 0x00000000, false),
                                    new ColorAnim(0.1f, 0x000000aa, false),
                                    new ColorAnim(0.07f, 0x00000000, false),
                                    new ColorAnim(0.75f, 0x000000ff, false),
                            })
                    )
                    ;


            // Smoke particles
            s.particle = new MainMenu.ParticleType();
            s.particle.mats = new SetRandomizedSelector<Sprite>(new Sprite[]{
                    Sprite.load("menu/particle1.png"),
            });
            s.particle.target = SaraRenderer.TARGET_BG_SHADOWS;
            s.particle.x = new Range(0f, 1f);
            s.particle.y = new Range(0f, Globals.LENGTH);
            s.particle.scale = new Range(0.9f, 0.3f);
            s.particle.xSpeed = new Range(0.2f * 0.2f, 0.1f * 0.2f, true);
            s.particle.ySpeed = new Range(0.1f * 0.2f, 0.1f * 0.2f, true);
            s.particle.rotateSpeed = new Range(40f * 0.2f, 20f * 0.2f, true);

            s.particle.startAnim = new FadeAnim(2f, LinearGraph.zeroToOne);
            s.particle.endAnim = new FadeAnim(2f, LinearGraph.oneToZero);
            s.particle.idleAnim = new CompoundAnim(10f, new Animation[]{
                    new ScaleAnim(1f, new SineGraph(1f, 2f, 0f, 0.1f, 1f)),
                    new FadeAnim(1f, new SineGraph(1f, 4f, 0f, 0.05f, 0.15f)),
            });
            s.particle.tLifeTime = new Range(6f, 4f);
            s.numParticles = 10;


            // Menu
            float buttonY = +0.09f;

            s.title = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1.1f).move(-0.01f, (+1650f / 2250f) + windowY))     // +1030f
                    .visual(Sprite.load("menu/title-pipedreams.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.0f), null, titleHideAnim)
                    ;

            s.quitButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(757f / 2250f).move(0, (-518f / 2250f) + windowY + buttonY))
                    .visuals(Sprite.load("menu/quit.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.8f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;

            s.simAdButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(2000f / 2250f).move(0, (-1170f / 2250f) + windowY + buttonY))
                    .visuals(Sprite.load("menu/sim-button.png"), SaraRenderer.TARGET_BG)
                    .animation(startAnim(2.0f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;
            new StaticSprite()
                    .viewport(s.simAdButton)
                    .metrics(new UIElement.Metrics().scale(0.65f))
                    .visual(Sprite.load("menu/sim-button-text.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            null,
                            new ColorAnim(1.5f, new SineGraph(1f, 1f, 0f, 0.6f, 1.6f), null),
                            null
                    )
                    .attach();


            s.creditsButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1183f / 2250f).move(0, (-096f / 2250f) + windowY + buttonY))
                    .visuals(Sprite.load("menu/credits.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.6f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;

            s.newGameButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(993f / 2250f).move(0, (+324f / 2250f) + windowY + buttonY))
                    .visuals(Sprite.load("menu/begin.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.4f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;

            s.newGamePlusButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1137f / 2250f).move(0, (+324f / 2250f) + windowY + buttonY))
                    .visuals(Sprite.load("menu/beginplus.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.4f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;

            s.restartButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1274f / 2250f).move(0, (+324f / 2250f) + windowY + buttonY))
                    .visuals(Sprite.load("menu/restart.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.4f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;

            s.continueButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1400f / 2250f).move(0, (+702f / 2250f) + windowY + buttonY))
                    .visuals(Sprite.load("menu/continue.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.2f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;
            s.tTitleHideTime = 2f;
            s.tNewGameMusicDelay = 6.1f;          // Wait for SIMULACRA portrait title to finish

            // Warning group
            s.newGameWarningView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.95f).move(0, 0))
                    .text(new Text()
                            .font(noticeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(1f, -14f)
                            .text("Are you sure you\n" +
                                    "want to restart ?\n" +
                                    "You will lose your\n" +
                                    "current progress.")
                    )
                    .animation(startAnim(1f), null, titleHideAnim)
                    ;
            s.newGameYesButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(628f / 2250).move(+652f / 2250f, -906f / 2250f))
                    .visuals(Sprite.load("menu/yes.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.2f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;
            s.newGameNoButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(598f / 2250).move(-636f / 2250f, -906f / 2250f))
                    .visuals(Sprite.load("menu/no.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.4f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;



            // Remove adds button
            sprite = Sprite.load("system/memo-closed.png").instantiate();
            ColorAttribute.of(sprite).set(0x5c5c5cff);
            s.removeAdsButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(60f / 1080f).anchorTop().move(-0.225f, -0.025f))
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.8f), null, pressedAnim, releasedAnim, titleHideAnim)
                    .text(new Text()
                            .font(helpButtonFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(+3.1f, 0f, 4.4f, 0.5f, 0)
                            .text("REMOVE ADS")
                    )
                    .inputPadding(1f, 2f, 6f, 2f)
                    ;

            // Discord
            sprite = Sprite.load("menu/discord.png").instantiate();
            ColorAttribute.of(sprite).set(0x5c5c5cff);
            s.discordButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(60f / 1080f).anchorTop().anchorRight().move(-0.265f, -0.025f))
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.8f), null, pressedAnim, releasedAnim, titleHideAnim)
                    .inputPadding(1.5f, 2f, 0.5f, 2f)
                    ;
            s.discordNewAnim = new SequenceAnim(new Animation[] {
                    new ColorAnim(0.2f, 10f, 10f, 10f, 1f, true),
                    new NullAnim(0.2f),
            });

            // Help menu
            s.helpButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(60f / 1080f).anchorTop().anchorRight().move(-0.165f, -0.025f))
                    .visuals(Sprite.load("menu/help.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.8f), null, pressedAnim, releasedAnim, titleHideAnim)
                    .text(new Text()
                            .font(helpButtonFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(+1.9f, 0f, 1.8f, 0.5f, 0)
                            .text("HELP")
                    )
                    .inputPadding(0.5f, 2f, 10f, 2f)
                    ;

            s.helpTitleView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.95f).move(0, +0.3f))
                    .text(new Text()
                            .font(noticeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(1f, -14f)
                            .text("Talk to us if you need\n" +
                                    "help or find any issues\n" +
                                    "with SIMULACRA\n")
                    )
                    .animation(startAnim(1f), null, titleHideAnim)
                    ;

            s.twitterButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(320f / 1080f).move(-0.32f, 0.0f))
                    .visuals(Sprite.load("menu/twitter-button.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.2f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;

            s.mailButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(320f / 1080f).move(0f, 0.0f))
                    .visuals(Sprite.load("menu/mail-button.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.4f), null, pressedAnim, releasedAnim, titleHideAnim)
                    ;
            s.fbButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(320f / 1080f).move(+0.32f, 0.0f))
                    .visuals(Sprite.load("menu/fb-button.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.6f), null, pressedAnim, releasedAnim, titleHideAnim)
            ;

            // Subtitles
            s.subtitleLabelView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.6f).move(-0.17f, -0.30f))
                    .text(new Text()
                            .font(noticeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.15f, -11f)
                            .centerRight()
                            .text("Subtitles")
                    )
                    .animation(startAnim(1.8f), null, titleHideAnim);
            s.subtitleButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.07f).move(0.2f, -0.30f))
                    .visuals(Sprite.load("apps/chats/circle-empty.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.8f), null, pressedAnim, releasedAnim, titleHideAnim)
                    .inputPadding(10.0f, 0.2f, 10.0f, 0.2f)
                    ;
            sprite = Sprite.load("system/circle.png").instantiate();
            ColorAttribute.of(sprite).set(-16711681);
            s.subtitleEnabledView = new StaticSprite()
                    .viewport(s.subtitleButton)
                    .metrics(new UIElement.Metrics().scale(0.4f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    ;

            // High quality videos
            s.highQualityVideosLabelView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.6f).move(-0.17f, -0.42f))
                    .text(new Text()
                            .font(noticeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.15f, -11f)
                            .centerRight()
                            .text("High Quality Graphics")
                    )
                    .animation(startAnim(1.8f), null, titleHideAnim);
            s.highQualityVideosButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.07f).move(0.2f, -0.42f))
                    .visuals(Sprite.load("apps/chats/circle-empty.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.8f), null, pressedAnim, releasedAnim, titleHideAnim)
                    .inputPadding(10.0f, 0.2f, 10.0f, 0.2f)
            ;
            sprite = Sprite.load("system/circle.png").instantiate();
            ColorAttribute.of(sprite).set(-16711681);
            s.highQualityVideosEnabledView = new StaticSprite()
                    .viewport(s.highQualityVideosButton)
                    .metrics(new UIElement.Metrics().scale(0.4f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
            ;



            s.privacyPolicyButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.35f).anchorTop().anchorRight().move(-0.038f, -0.038f))
                    .text(new Text()
                            .font(helpButtonFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .text("PRIVACY POLICY")
                            .autoLength()
                    )
                    .animation(startAnim(1.0f), new FadeAnim(0.6f, 1), new ColorAnim(1f, pressedColor, true, 1), releasedAnim, titleHideAnim)
                    .inputPadding(0.2f, 0.1f, 0.1f, 0.2f)
                    ; // .attach();
            s.privacyPolicyButton.length(s.privacyPolicyButton.text().length);


            s.helpBackButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(444f / 1080f).move(0, -0.55f))
                    .visuals(Sprite.load("menu/back-button.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(2.0f), null, pressedAnim, releasedAnim, titleHideAnim)
                    .inputPadding(0.5f, 0f, 0.5f, 0.2f)
                    ;


            s.versionView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().anchorRight().move(-0.02f, +0.02f))
                    .text(new Text()
                            .font(copyrightFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(1f, 0.03f, -23f)
                            .centerRight()
                            .text(Globals.version)
                    )
                    .animation(startAnim(1.0f), null, titleHideAnim)
                    ;

            // Beta feedback
            s.betaFeedbackGroup = new UIElement.Group()
                    .viewport(s.window)
                    ;
            patch = PatchedSprite.create("system/circle.png", 0.21f, 0.1f);
            ColorAttribute.of(patch).set(0xd7119aff);
            s.betaFeedbackButton = new Clickable()
                    .viewport(s.betaFeedbackGroup)
                    .metrics(new UIElement.Metrics().scale(0.4f).anchorTop().move(0, -0.05f))
                    .visuals(patch, SaraRenderer.TARGET_INTERACTIVE)
                    .text(new Text()
                            .font(simAdFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(patch.length, -12f)
                            .text("BETA FEEDBACK")
                    )
                    .inputPadding(0.1f, 1f, 0.1f, 0.1f)
                    .animation(startAnim(1.0f),
                            new SequenceAnim(new Animation[] {
                                    new ColorAnim(0.2f, new ConstantGraph(2f), null),
                                    new NullAnim(0.2f)
                            }),
                            pressedAnim, releasedAnim, titleHideAnim
                    )
                    .attach();



            s.loadingView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().move(0, +0.35f))
                    .text(new Text()
                            .font(loadingFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.1f, -18f)
                            .text("Please Wait")
                    )
                    .animation(
                            new FadeAnim(3f, LinearGraph.zeroToOne),
                            new FadeAnim(1f, new SineGraph(1f, 1f, 0f, 0.1f, 0.5f)),
                            null
                    );
            s.tLoadingViewDelay = 2f;






            // Headphones
            s.headphonesGroup = new UIElement.Group()
                    .viewport(s.window);
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff);
            new StaticSprite()
                    .viewport(s.headphonesGroup)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();
            new TextBox()
                    .viewport(s.headphonesGroup)
                    .metrics(new UIElement.Metrics().scale(1449f / 2250f).move(0, +1175f / 2250f))
                    .text(new Text()
                            .font(volumeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(285f / 1449f, 0)
                            .text("For the best experience\n" +
                                    "please turn on the volume"
                            )
                    )
                    .attach();
            new StaticSprite()
                    .viewport(s.headphonesGroup)
                    .metrics(new UIElement.Metrics().scale(1294f / 2250f).move(0, 0))
                    .visual(Sprite.load("menu/headphones.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            new TextBox()
                    .viewport(s.headphonesGroup)
                    .metrics(new UIElement.Metrics().scale(1667f / 2250f).move(0, -1193f / 2250f))
                    .text(new Text()
                            .font(headphonesFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(143f / 1667f, 0)
                            .text("Headphones recommended")
                    )
                    .attach();

            if(Gdx.app.getType() == Application.ApplicationType.Android)
                s.tHeadphonesTime = 2.5f;     // Android boot delayed by 0.5s
            else if(Gdx.app.getType() == Application.ApplicationType.iOS)
                s.tHeadphonesTime = 0.5f;     // iOS boot eats alot of time
            else
                s.tHeadphonesTime = 3f;


            // Logo
            s.kaiganGroup = new UIElement.Group()
                    .viewport(s.window);
            new StaticSprite()
                    .viewport(s.kaiganGroup)
                    .metrics(new UIElement.Metrics().scale(1531f / 2250f).move(0, +118f / 2250f))
                    .visual(Sprite.load("menu/kaigan.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            s.tKaiganTime = 5f;


            // Theme
            s.themeMusic = "sounds/theme.ogg";
            s.tThemeTime = 1f;
            s.tThemeFadeOutTime = 3f;


            // Timing
            s.tLeaveTitleDelay = 1.2f;

            // Loading menu
            s.loadingMenu = new LoadingMenu(null,
                    null,
                    0,
                    0,
                    0,
                    1f,
                    true,
                    false
            );
        }


        {
            Sprite headerAndroidSprite = Sprite.load("platform/android/dialog-header.png");
            Sprite buttonAndroidSprite = Sprite.load("platform/android/signin.png");
            Sprite headerAppleSprite = Sprite.load("platform/apple/dialog-header.png");
            Sprite buttonAppleSprite = Sprite.load("platform/apple/signin.png");

            Sprite headerSprite;
            Sprite buttonSprite;
            if(Gdx.app.getType() == Application.ApplicationType.Android) {
                headerSprite = headerAndroidSprite;
                buttonSprite = buttonAndroidSprite;
            }
            else {      // Apple
                headerSprite = headerAppleSprite;
                buttonSprite = buttonAppleSprite;
            }

            // Google play icon
            sprite = Sprite.load("platform/android/playservices.png").instantiate();
            ColorAttribute.of(sprite).set(0xcc0b6fff);
            s.googlePlayButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorTop().anchorLeft().scale(0.11f).move(+0.02f, -0.02f))
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(startAnim(1.8f), null, pressedAnim, releasedAnim, titleHideAnim)
                    .inputPadding(0.1f, 0.1f, 0.1f, 0.1f)
                    ;


            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff).alpha(0.5f);
            s.googlePlayBg = new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            new FadeAnim(1f, LinearGraph.zeroToOne),
                            null,
                            new FadeAnim(1f, LinearGraph.oneToZero)
                    )
                    .passThroughInput(false)
                    ;

            sprite = Sprite.load("system/loading-circle.png").instantiate();
            ColorAttribute.of(sprite).set(0xcc0b6fff);
            s.googlePlayLoadingView = new StaticSprite()
                    .viewport(s.googlePlayBg)
                    .metrics(new UIElement.Metrics().scale(0.18f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            new FadeAnim(1f, LinearGraph.zeroToOne),
                            new RotateAnim(1f, new LinearGraph(0f, 360f)),
                            new FadeAnim(1f, LinearGraph.oneToZero)
                    )
                    ;

            sprite = new Sprite(1461f / 1911f, SaraRenderer.renderer.coloredMaterial);
            s.googlePlayPrompt = new StaticSprite()
                    .viewport(s.googlePlayBg)
                    .metrics(new UIElement.Metrics().scale(1911f / 2250f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(
                            new ScaleAnim(0.15f, QuadraticGraph.zeroToOneInverted),
                            null,
                            new ScaleAnim(0.15f, QuadraticGraph.oneToZero)
                    )
                    ;

            StaticSprite headerView = new StaticSprite()
                    .viewport(s.googlePlayPrompt)
                    .metrics(new UIElement.Metrics().anchor(0, +0.5f))
                    .visual(headerSprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .attach();
            new StaticSprite()
                    .viewport(headerView)
                    .metrics(new UIElement.Metrics().scale(0.18f))
                    .visual(Sprite.load("platform/android/playservices.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .attach();

            new TextBox()
                    .viewport(s.googlePlayPrompt)
                    .metrics(new UIElement.Metrics().scale(1363f / 1911f).move(0, +0.1f))
                    .text(new Text()
                            .font(dialogFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                            .position(251f / 1363f, 0)
                            .text("Login to enable cloud\nsave and achievements")
                    )
                    .attach();

            s.googlePlayPromptLoginButton = new Clickable()
                    .viewport(s.googlePlayPrompt)
                    .metrics(new UIElement.Metrics().scale(1358f / 1911f).move(0, -0.135f))
                    .visuals(buttonSprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(null, null, pressedAnim, releasedAnim, null)
                    .inputPadding(1f, 0.2f, 1f, 0.1f)
                    .attach();

            patch = PatchedSprite.create("system/rounded.png", 342f / 1911f, 0.02f, 0.02f, 0f, 0.02f);
            s.googlePlayPromptContinueButton = new Clickable()
                    .viewport(s.googlePlayPrompt)
                    .metrics(new UIElement.Metrics().anchor(0, -0.5f))
                    .visuals(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .text(new Text()
                            .font(dialogButtonFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                            .position(patch.length, -17f)
                            .text("SKIP")
                    )
                    .animation(null, null, pressedAnim, releasedAnim, null)
                    .inputPadding(1f, 0.05f, 1f, 0.1f)
                    .attach();
            // Line
            sprite = new Sprite(4f / 960f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xbec2ccff);
            new StaticSprite()
                    .viewport(s.googlePlayPromptContinueButton)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();

            s.doneBgAnim = new FadeAnim(2f, LinearGraph.oneToZero);
            s.doneBgLoopAnim = new FadeAnim(1f, ConstantGraph.zero);

        }

        // Commit
        menu.internal(s);
    }

}
