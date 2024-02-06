package game31.gb.restore;

import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.ScreenBar;
import game31.app.restore.RestoreImageScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.NullAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.calc.VibrationGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 6/19/2017.
 */

public class GBRestoreImageScreen {

    public GBRestoreImageScreen(RestoreImageScreen screen) {
        RestoreImageScreen.Internal s = new RestoreImageScreen.Internal();

        Font progressFont = new Font("opensans-bold.ttf", 32);

        Font tutorialTitleFont = new Font("opensans-semibold.ttf", 64, 0xffffffff);
        Font tutorialInstructionFont = new Font("opensans-regular.ttf", 48, 0x898989ff);

        Font skipButtonFont = new Font("opensans-bold.ttf", 32, 0xffffff44);

        Font selectedFont = new Font("opensans-semibold.ttf", 64, new Color(0,0,0, 0.25f), 2f, Color.CLEAR, 0, 0, Color.WHITE);

        Sprite sprite;
        PatchedSprite patch;


        {
            s.imageView = new Clickable()
                    .target(SaraRenderer.TARGET_INTERACTIVE)
                    ;

            // Rounded edges
            {
                sprite = Sprite.load("system/rounded-edge.png").instantiate();
                ColorAttribute.of(sprite).set(0x000000ff);
                float size = 0.1f;
                new StaticSprite()
                        .viewport(s.imageView)
                        .metrics(new UIElement.Metrics().anchor(-0.51f, +0.51f).pan(+0.5f, -0.5f).scale(+size, +size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.imageView)
                        .metrics(new UIElement.Metrics().anchor(+0.51f, +0.51f).pan(+0.5f, -0.5f).scale(-size, +size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.imageView)
                        .metrics(new UIElement.Metrics().anchor(+0.51f, -0.51f).pan(+0.5f, -0.5f).scale(-size, -size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.imageView)
                        .metrics(new UIElement.Metrics().anchor(-0.51f, -0.51f).pan(+0.5f, -0.5f).scale(+size, -size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
            }

            // Empty view
            sprite = new Sprite(1f, SaraRenderer.renderer.screenNoiseMaterial);
            ColorAttribute.of(sprite).set(0x222222ff).alpha(1f);
            s.imageEmptyView = new StaticSprite()
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    ;

            // Corrupted view
            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", 1f, 0.1f);
            ColorAttribute.of(patch).set(0x444444ff);
            s.imageCorruptedView = new StaticSprite()
                    .visual(patch, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .animation(
                            null,
                            new ColorAnim(
                                    1f,
                                    new VibrationGraph(1f, null, null),
                                    new ConstantGraph(0.2f),
                                    new ConstantGraph(0.2f),
                                    new ConstantGraph(1f)
                            ),
                            null
                    )
                    ;

            s.imageEmptySize = 0.34f;
            s.imageCorruptedSize = 0.37f;

            // Selected
            sprite = new Sprite(1f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x29abe2ff).alpha(0.5f);
            s.selectedView = new UIElement.Group()
                    .animation(
                            new ScissorAnim(0.3f, new Animation[] {
                                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, null, new QuadraticGraph(0, 1f, true))
                            }),
                            null,
                            new ScissorAnim(0.3f, new Animation[] {
                                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, null, new QuadraticGraph(1f, 0f, false))
                            })
                    )
                    .length(sprite.length)
                    ;
            new StaticSprite()
                    .viewport(s.selectedView)
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB_TEXT)
                    .attach()
                    ;
            s.selectedOrderView = new TextBox()
                    .viewport(s.selectedView)
                    .metrics(new UIElement.Metrics().scale(0.3f))
                    .text(new Text()
                            .font(selectedFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, 0)
                    )
                    .attach();

            // Cell metrics
            float x1 = -0.33f;
            float x2 = 0f;
            float x3 = +0.33f;
            float y1 = +0.33f;
            float y2 = 0;
            float y3 = -0.33f;
            float size = 0.33f;

            s.imageCellMetrics = new UIElement.Metrics[] {
                    new UIElement.Metrics().scale(size).anchor(x1, y1),
                    new UIElement.Metrics().scale(size).anchor(x2, y1),
                    new UIElement.Metrics().scale(size).anchor(x3, y1),
                    new UIElement.Metrics().scale(size).anchor(x1, y2),
                    new UIElement.Metrics().scale(size).anchor(x2, y2),
                    new UIElement.Metrics().scale(size).anchor(x3, y2),
                    new UIElement.Metrics().scale(size).anchor(x1, y3),
                    new UIElement.Metrics().scale(size).anchor(x2, y3),
                    new UIElement.Metrics().scale(size).anchor(x3, y3),
            };
        }

        {
            // Main image
            s.mainView = new Clickable()
                    .target(SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            null,
                            null,
                            null,
                            null,
                            null
                    )
            ;

            // Empty view
            sprite = new Sprite(1f, SaraRenderer.renderer.screenNoiseMaterial);
            ColorAttribute.of(sprite).set(0x222222ff).alpha(1f);
            s.mainEmptyView = new StaticSprite()
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                    .animation(
                            null,
                            null,
                            new ScissorAnim(0.3f, new Animation[] {
                                    new MoveAnim(1f, null, new QuadraticGraph(0f, +1, true))
                            })
                    )
            ;

            // Corrupted view
            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", 1f, 0.1f);
            ColorAttribute.of(patch).set(0x444444ff);
            s.mainCorruptedView = new StaticSprite()
                    .visual(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(
                            new SequenceAnim(new Animation[] {
                                    new NullAnim(0.05f),
                                    new ScaleAnim(0.02f),
                                    new NullAnim(0.05f),
                                    new ScaleAnim(0.03f),
                            }),
                            new ColorAnim(
                                    1f,
                                    new VibrationGraph(1f, null, null),
                                    new ConstantGraph(0.2f),
                                    new ConstantGraph(0.2f),
                                    new ConstantGraph(1f)
                            ),
                            new SequenceAnim(new Animation[] {
                                    new ScaleAnim(0.03f),
                                    new NullAnim(0.05f),
                                    new ScaleAnim(0.02f),
                                    new NullAnim(0.05f),

                            })
                    )
            ;

            s.mainEmptySize = 0.34f;
            s.mainCorruptedSize = 0.37f;

            s.mainImageMetrics = new UIElement.Metrics().anchorTop().scale(0.835f);
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
//            s.bars.showShadows(0x252531ff, 1f);


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
                            .font(tutorialTitleFont, SaraRenderer.TARGET_BG_SHADOWS)
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
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    .animation(null, new RotateAnim(1f, new LinearGraph(0f, 360f)), null)
                    .attach();

            // Bottom panel
            sprite = new Sprite(1725f / 2251f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff).alpha(1f);
            s.bottomPanel = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .animation(
                            null,
                            null,
                            new MoveAnim(0.3f, null, new QuadraticGraph(0f, -1f, false))
                    )
                    .attach();

            // Progress bar
            {
                Sprite bg = new Sprite(123f / 2251f, SaraRenderer.renderer.coloredMaterial);
                ColorAttribute.of(bg).set(0x111111ff);
                Sprite bar = new Sprite(123f / 2251f, SaraRenderer.renderer.coloredMaterial);
                ColorAttribute.of(bar).set(0x165b78ff);
                s.progressBar = new HorizontalProgressBar()
                        .viewport(s.bottomPanel)
                        .metrics(new UIElement.Metrics().anchorTop().move(0, +0.00f))
                        .visual(bg, bar, SaraRenderer.TARGET_INTERACTIVE)
                        .progress(0.33f)
                        .attach();

                s.tProgressBarSeekTime = 0.2f;

                s.progressPercentageView = new TextBox()
                        .viewport(s.progressBar)
                        .metrics(new UIElement.Metrics().scale(1060f / 2251f).anchorLeft().move(+48f / 2251f, 0))
                        .text(new Text()
                                .font(progressFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                                .position(55f / 1060f, 0)
                                .centerLeft()
                                .text("36% RESTORED")
                        )
                        .attach();
                s.progressUsedView = new TextBox()
                        .viewport(s.progressBar)
                        .metrics(new UIElement.Metrics().scale(1060f / 2251f).anchorRight().move(-48f / 2251f, 0))
                        .text(new Text()
                                .font(progressFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                                .position(55f / 1060f, 0)
                                .centerRight()
                                .text("1 / 6")
                        )
                        .attach();
            }

            // Anchors
            sprite = new Sprite(1973f / 2251f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff);
            s.mainImageAnchor = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1f).move(0, +0.285f))
                    .length(sprite.length)
                    .animation(
                            new ScissorAnim(0.3f, new Animation[] {
                                    new MoveAnim(1f, null, new QuadraticGraph(-1f, 0f, true))
                            }),
                            null,
                            null
                    )
                    ;
            s.fragmentImageAnchor = new UIElement.Group()
                    .viewport(s.bottomPanel)
                    .attach();

            // Positions
            {
                float size = 594f / 2251f;
                float x1 = -622f / 2251f;
                float x2 = 0;
                float x3 = +622f / 2251f;
                float y = +0.045f;
                float y1 = y + (+309f / 2251f);
                float y2 = y + (-309f / 2251f);

                s.fragmentMetrics = new UIElement.Metrics[]{
                        new UIElement.Metrics().scale(size).move(x1, y1),
                        new UIElement.Metrics().scale(size).move(x2, y1),
                        new UIElement.Metrics().scale(size).move(x3, y1),
                        new UIElement.Metrics().scale(size).move(x1, y2),
                        new UIElement.Metrics().scale(size).move(x2, y2),
                        new UIElement.Metrics().scale(size).move(x3, y2),
                };
            }


            // Tutorial
            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", 1973f / 2251f, 0f);
            ColorAttribute.of(patch).set(0x000000ff).alpha(0f);
            s.tutorialGroup = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1f).move(0, +0.3f))
                    .visual(patch, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();

            new TextBox()
                    .viewport(s.tutorialGroup)
                    .metrics(new UIElement.Metrics().scale(1729f / 2251f).move(0, +0.08f))
                    .text(new Text()
                            .font(tutorialTitleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(105f / 1329f, 0)
                            .text("IMAGE CORRUPTED")
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
                    .metrics(new UIElement.Metrics().scale(1729f / 2251f).move(0, -0.06f))
                    .text(new Text()
                            .font(tutorialInstructionFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(256f / 1329f, 17f)
                            .text("Select the fragments in the correct order to rebuild the image.")
                    )
                    .attach();

            s.skipButton = new Clickable()
                    .viewport(s.tutorialGroup)
                    .metrics(new UIElement.Metrics().scale(0.1f).anchorTop().anchorRight().move(-0.05f, -0.05f))
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

            s.progressTextFormat = "%d%% RESTORED";
            s.countTextFormat = "%d / %d";

            s.tWinDelay = Globals.restoreWinDelay;

            s.openSound = Sound.load("sounds/repair_enter.ogg");
            s.closeSound = Sound.load("sounds/repair_close.ogg");

            s.acceptedSound = Sound.load("sounds/repair_accepted.ogg");
            s.savingSound = Sound.load("sounds/repair_saving_loop.ogg");
            s.winSound = Sound.load("sounds/repair_saved.ogg");

            s.addSound = Sound.load("sounds/repair_select.ogg");
            s.removeSound = Sound.load("sounds/repair_deselect.ogg");
        }

        // Commit
        screen.setInternal(s);
    }
}
