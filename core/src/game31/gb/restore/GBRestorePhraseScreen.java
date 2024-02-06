package game31.gb.restore;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

import game31.Globals;
import game31.ScreenBar;
import game31.app.restore.RestorePhraseScreen;
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
import sengine.audio.Sound;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.PatchedTextBox;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 6/21/2017.
 */

public class GBRestorePhraseScreen {

    public GBRestorePhraseScreen(RestorePhraseScreen screen) {
        RestorePhraseScreen.Internal s = new RestorePhraseScreen.Internal();

        Sprite sprite;
        PatchedSprite patch;

        Font wordFont = new Font("opensans-regular.ttf", 48);

        Font tutorialTitleFont = new Font("opensans-semibold.ttf", 64, 0xffffffff);
        Font tutorialInstructionFont = new Font("opensans-regular.ttf", 48, 0x898989ff);

        Font skipButtonFont = new Font("opensans-bold.ttf", 32, 0xffffff44);


        {
            // Top words
            s.topWord = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(1f).anchorTop().anchorLeft())
                    .visual("system/rounded.png", 0.02f, SaraRenderer.TARGET_INTERACTIVE)
                    .font(wordFont, -17f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .padding(3f, 1.5f, 3f, 1.5f)
                    .animation(
                            new ScissorAnim(0.3f, new Animation[] {
                                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, null, new QuadraticGraph(0f, 1f, true))
                            }),
                            new ColorAnim(0x1a6a94ff),
                            new FadeAnim(0.8f),
                            new FadeAnim(0.15f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .enable()
            ;
            s.topEmptyWord = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(1f).anchorTop().anchorLeft())
                    .visual("system/rounded-glow-edge.png", 0.02f, SaraRenderer.TARGET_INTERACTIVE)
                    .font(wordFont, -17f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .padding(3f, 1.5f, 3f, 1.5f)
                    .animation(
                            null,
                            new CompoundAnim(1f, new Animation[] {
                                    new ColorAnim(1f, 0x888888ff, true, 0),         // box
                                    new ColorAnim(1f, 0x00000000, true, 1)          // invisible font (keep text for width calculation)
                            }),
                            null
                    )
            ;


            s.topWinWord = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(1f).anchorTop().anchorLeft())
                    .visual("system/rounded.png", 0.02f, SaraRenderer.TARGET_INTERACTIVE)
                    .font(wordFont, -17f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .padding(3f, 1.5f, 3f, 1.5f)
                    .animation(
                            null,
                            new SequenceAnim(new Animation[]{
                                    new ColorAnim(0.4f,
                                            new LinearGraph(26f / 255f, 26f / 255f),
                                            new LinearGraph(106f / 255f, 148f / 255f),
                                            new LinearGraph(148f / 255f, 110f / 255f),
                                            null,
                                            false
                                    ),
                                    new ColorAnim(0.4f,
                                            new LinearGraph(26f / 255f, 26f / 255f),
                                            new LinearGraph(148f / 255f, 106f / 255f),
                                            new LinearGraph(110f / 255f, 148f / 255f),
                                            null,
                                            false
                                    )
                            }),
                            null,
                            null,
                            null
                    )
            ;

            s.topStartX = 0;
            s.topStartY = 0;
            s.topXpadding = 0.03f;
            s.topYinterval = -0.13f;
        }

        {
            // Bottom words
            s.bottomWord = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(1f).anchorTop().anchorLeft())
                    .visual("system/rounded.png", 0.02f, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .font(wordFont, -17f, 1f, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                    .padding(3f, 1.5f, 3f, 1.5f)
                    .animation(
                            null,
                            new ColorAnim(0x1a6a94ff),
                            new FadeAnim(0.8f),
                            new FadeAnim(0.15f, new LinearGraph(0.8f, 1f)),
                            new ScissorAnim(0.3f, new Animation[] {
                                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, null, new QuadraticGraph(1f, 0f, true))
                            })
                    )
                    .enable()
            ;
            s.bottomReappearAnim = new ScissorAnim(0.3f, new Animation[] {
                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, null, new QuadraticGraph(0f, 1f, true))
            });
            s.bottomSelectedWord = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(1f).anchorTop().anchorLeft())
                    .visual("system/rounded-glow-edge.png", 0.02f, SaraRenderer.TARGET_INTERACTIVE)
                    .font(wordFont, -17f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .padding(3f, 1.5f, 3f, 1.5f)
                    .animation(
                            null,
                            new CompoundAnim(1f, new Animation[] {
                                    new ColorAnim(1f, 0x888888ff, true, 0),         // box
                                    new ColorAnim(1f, 0x888888ff, true, 1)          // font
                            }),
                            new FadeAnim(0.8f),
                            new FadeAnim(0.15f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .enable()
            ;
            s.draggedWord = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(1f))
                    .visual("system/rounded.png", 0.02f, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .font(wordFont, -17f, 1f, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                    .padding(3f, 1.5f, 3f, 1.5f)
                    .animation(
                            null,
                            new ColorAnim(0x1a6a94ff),
                            new FadeAnim(0.8f),
                            new FadeAnim(0.15f, new LinearGraph(0.8f, 1f)),
                            new ScissorAnim(0.3f, new Animation[] {
                                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, null, new QuadraticGraph(1f, 0f, true))
                            })
                    )
                    .enable()
            ;
            s.draggedBottomYThreshold = 0f;
            s.draggedInputYOffset = Gdx.app.getType() == Application.ApplicationType.Desktop ? 0 : +0.14f;

            s.bottomStartX = 0;
            s.bottomStartY = 0;
            s.bottomXpadding = 0.03f;
            s.bottomYinterval = -0.13f;
        }


        {
            // Window
            s.window = new UIElement.Group();

            // Entry and exit splashes
            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", Globals.LENGTH, 0f);
            ColorAttribute.of(patch).set(0x660000ff).alpha(1f);
            s.splashView = new StaticSprite()
                    .viewport(s.window)
                    .visual(patch, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .animation(
                            null,
                            null,
                            new SequenceAnim(new Animation[] {
                                    new NullAnim(0.2f),
                                    new ScaleAnim(0.05f),
                                    new NullAnim(0.07f),
                                    new ScaleAnim(0.03f),
                                    new FadeAnim(0.2f, LinearGraph.oneToZero)
                            })
                    )
            ;
//            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", Grid.LENGTH, 0f);
//            ColorAttribute.of(patch).set(0x550000ff).alpha(1f);
            s.splashEndView = new StaticSprite()
                    .viewport(s.window)
                    .visual(patch, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .animation(
                            null,
                            null,
                            null
                    )
            ;
            s.tSplashEndDelay = 0.12f;


            // Bg
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.screenNoiseMaterial);
            ColorAttribute.of(sprite).set(0x222222ff).alpha(1f);
//            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", Grid.LENGTH, 0f);
//            ColorAttribute.of(patch).set(0x030303ff).alpha(0.2f);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();

            // Bars
            s.bars = new ScreenBar();
            s.bars.showAppbar("Restore Tool", "Image from by Greg", 0, 0, 0, 0);
            s.bars.showNavbar(true, true, true);
            s.bars.color(0x090909ff, 1f, 0x090909ff, 1f);

            // Bottom panel
            sprite = new Sprite(Globals.LENGTH / 2f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff).alpha(1f);
            s.bottomPanel = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    .animation(
                            null,
                            null,
                            new MoveAnim(0.3f, null, new QuadraticGraph(0f, -1f, false))
                    )
                    .attach();

            s.bottomGroup = new UIElement.Group()
                    .viewport(s.bottomPanel)
                    .metrics(new UIElement.Metrics().anchorTop().scale(0.9f).move(0, -0.08f))
                    .length(s.bottomPanel.getLength() - 0.2f)
                    .attach();


            s.topGroup = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorTop().scale(0.9f).move(0f, -0.2f))
                    .length(s.bottomPanel.getLength() - 0.2f)
                    .attach();



            // Tutorial
            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", 1973f / 2251f, 0f);
            ColorAttribute.of(patch).set(0x000000ff).alpha(0f);
            s.tutorialGroup = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1f).move(0, +0.4f))
                    .visual(patch, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();

            new TextBox()
                    .viewport(s.tutorialGroup)
                    .metrics(new UIElement.Metrics().scale(1729f / 2251f).move(0, +0.06f))
                    .text(new Text()
                            .font(tutorialTitleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(105f / 1329f, 0)
                            .text("MESSAGE CORRUPTED")
                    )
                    .animation(
                            null,
                            new SequenceAnim(new Animation[] {
                                    new ColorAnim(0.5f, 0x999999ff, false),
                                    new ColorAnim(0.5f, 0xffffffff, false)
                            }),
                            null
                    )
                    .attach();

            new TextBox()
                    .viewport(s.tutorialGroup)
                    .metrics(new UIElement.Metrics().scale(1729f / 2251f).move(0, -0.07f))
                    .text(new Text()
                            .font(tutorialInstructionFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(256f / 1329f, 17f)
                            .text("Select the words in the correct order to rebuild the message.")
                    )
                    .attach();


            s.skipButton = new Clickable()
                    .viewport(s.tutorialGroup)
                    .metrics(new UIElement.Metrics().scale(0.1f).anchorTop().anchorRight().move(-0.05f, -0.15f))
                    .length(0.5f)
                    .text(new Text()
                            .font(skipButtonFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.5f, 0)
                            .text("SKIP")
                    )
                    .animation(
                            null,
                            new FadeAnim(1f, new SineGraph(1f, 1f, 0f, 0.25f, 0.75f), 1),
                            new ColorAnim(1f, 0xaaaaaaff, false, 1),
                            null,
                            null
                    )
                    .inputPadding(2f, 1f, 1f, 2f)
            ;

            sprite = Sprite.load("system/double-down-arrow.png").instantiate();
            ColorAttribute.of(sprite).set(0x898989ff).alpha(0.3f);
            new StaticSprite()
                    .viewport(s.tutorialGroup)
                    .metrics(new UIElement.Metrics().scale(201f / 2251f).move(0, -0.27f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            null,
                            new MoveAnim(1f, null, new SineGraph(1f, 1f, 0f, 0.2f, 0)),
                            null
                    )
                    .attach();

            // Win Panel
            s.winPanel = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .length(1725f / 2251f)
            ;
            new TextBox()
                    .viewport(s.winPanel)
                    .metrics(new UIElement.Metrics().scale(1729f / 2251f).move(0, +0.21f))
                    .text(new Text()
                            .font(tutorialTitleFont, SaraRenderer.TARGET_BG)
                            .position(110f / 1329f, 0)
                            .text("Saving")
                    )
                    .animation(
                            null,
                            new FadeAnim(1f, new SineGraph(1f, 1f, 0f, 0.2f, 0.8f)),
                            null
                    )
                    .attach();
            sprite = Sprite.load("system/loading-circle.png").instantiate();
            ColorAttribute.of(sprite).set(0x999999ff);
            new StaticSprite()
                    .viewport(s.winPanel)
                    .metrics(new UIElement.Metrics().scale(0.13f).move(0f, +0.02f))
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .animation(null, new RotateAnim(1f, new LinearGraph(0f, 360f)), null)
                    .attach();



            s.tWinDelay = Globals.restoreWinDelay;

            s.openSound = Sound.load("sounds/repair_enter.ogg");
            s.closeSound = Sound.load("sounds/repair_close.ogg");

            s.acceptedSound = Sound.load("sounds/repair_accepted.ogg");
            s.savingSound = Sound.load("sounds/repair_saving_loop.ogg");
            s.winSound = Sound.load("sounds/repair_saved.ogg");

            s.dragSound = Sound.load("sounds/repair_select.ogg");
            s.dropSound = Sound.load("sounds/repair_deselect.ogg");

        }

        // Commit
        screen.setInternal(s);
    }

}
