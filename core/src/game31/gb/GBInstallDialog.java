package game31.gb;

import game31.InstallDialog;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

public class GBInstallDialog {

    public GBInstallDialog(InstallDialog dialog) {
        InstallDialog.Internal s = new InstallDialog.Internal();



        Font titleFont = new Font("opensans-light.ttf", 48, 0x39395bff);

        Font noticeFont = new Font("opensans-regular.ttf", 32, 0x39395bff);

        Font instructionFont = new Font("opensans-bold.ttf", 32, 0x363636ff);
        Font authorizedFont = new Font("opensans-bold.ttf", 32, 0xed3ba3ff);
        Font cancelFont = new Font("opensans-semibold.ttf", 32, 0x363636ff);


        Font descriptionFont = new Font("opensans-regular.ttf", 32, 0x39395bff);

        Animation buttonPressedAnim = new ColorAnim(0xeeeeeeff);



        {
            s.window = new UIElement.Group()
                    .metrics(new UIElement.Metrics().anchorTop())
                    .length(0)
            ;

            // Lock image
            Sprite sprite;

            PatchedSprite patch = PatchedSprite.create("system/rounded.png", 161f / 857f, 0.04f, 0.04f, 0.04f, 0);             // 0.16f
            ColorAttribute.of(patch).set(0x39395bff);

            StaticSprite lockBg = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorTop().offset(0, +0.01f))
                    .visual(patch, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .passThroughInput(false)
                    .attach();

            sprite = Sprite.load("system/dialog-alert.png");
            ColorAttribute.of(sprite).set(0xa0a0ffff);
            new StaticSprite()
                    .viewport(lockBg)
                    .metrics(new UIElement.Metrics().scale(0.12f))
                    .visual(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .attach();

            s.titleView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.9f).anchorTop().offset(0, -0.28f))
                    .text(new Text()
                            .font(titleFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(17f / 276f, 0)
                            .text("Fingerprint Required")
                    )
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.2f, LinearGraph.oneToZero)
                    )
                    .attach();


            sprite = Sprite.load("system/touch.png").instantiate();
            ColorAttribute.of(sprite).set(0x39395bff);
            s.touchButton = new Clickable()
                    .viewport(s.titleView)
                    .metrics(new UIElement.Metrics().scale(0.35f).anchorBottom().pan(0, -1).offset(0, -0.24f))
                    .visuals(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .animation(
                            null,
                            new FadeAnim(new CompoundGraph(new Graph[] {
                                    new ConstantGraph(1f, 0.5f),
                                    new ConstantGraph(0.7f, 0.5f)
                            })),
                            new ScaleAnim(0.2f, new QuadraticGraph(1f, 1.1f, true)),
                            null,
                            new ScaleAnim(0.2f, new SineGraph(1f, 0.25f, 0.25f, 1f, 0), null)
                    )
                    .attach();

            sprite = Sprite.load("system/touch.png").instantiate();
            ColorAttribute.of(sprite).set(0xed3ba3ff);
            s.touchingView = new StaticSprite()
                    .viewport(s.touchButton)
                    .visual(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                    ;
            s.pressedAnim = new ScissorAnim(3f, new Animation[] {
                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, null, LinearGraph.zeroToOne)
            });
            s.releasedAnim = new ScissorAnim(0.7f, new Animation[] {
                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, null, LinearGraph.oneToZero)
            });


            s.instructionView = new TextBox()
                    .viewport(s.titleView)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).offset(0, -0.65f))
                    .text(new Text()
                            .font(instructionFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .wrapChars(18f)
                            .center()
                            .text("Touch and Hold")
                            .autoLength()
                    )
                    .animation(
                            null,
                            new FadeAnim(new CompoundGraph(new Graph[] {
                                    new ConstantGraph(1f, 0.5f),
                                    new ConstantGraph(0.7f, 0.5f)
                            })),
                            new FadeAnim(0.2f, LinearGraph.oneToZero)
                    )
                    .attach();

            s.noticeView = new TextBox()
                    .viewport(s.titleView)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).offset(0, -0.769f))
                    .text(new Text()
                            .font(noticeFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .wrapChars(22f)
                            .center()
                            .text("Touch here to install FlapeeBird. You should not install apps that you do not trust.")
                            .autoLength()
                    )
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.2f, LinearGraph.oneToZero)
                    )
                    .attach();

            // Line
            s.cancelButton = new Clickable()
                    .viewport(s.noticeView)
                    .metrics(new UIElement.Metrics().scale(1f / 0.9f).anchorBottom().pan(0, -1).offset(0, -0.08f))
                    .length(0.13f)
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.5f, 1),
                            null,
                            null)
                    .text(new Text()
                            .font(cancelFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(30f / 522f, -20f)
                            .text("Cancel")
                    )
                    .attach();
            sprite = new Sprite(0.003f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x39395bff).alpha(0.5f);
            new StaticSprite()
                    .viewport(s.cancelButton)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .attach();

            // Done
            s.doneTextView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.9f).anchorTop().offset(0, -0.28f))
                    .text(new Text()
                            .font(titleFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(17f / 276f, 0)
                            .text("Downloading Now")
                    )
                    .animation(
                            new SequenceAnim(
                                    new FadeAnim(0.5f, LinearGraph.zeroToOne),
                                    0.2f, true
                            ),
                            null,
                            null
                    )
                    ;

            sprite = Sprite.load("system/correct.png").instantiate();
            ColorAttribute.of(sprite).set(0xed3ba3ff);
            s.doneView = new StaticSprite()
                    .viewport(s.doneTextView)
                    .metrics(new UIElement.Metrics().scale(0.50f).anchorBottom().pan(0, -1).offset(0, -0.24f))
                    .visual(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .animation(
                            new SequenceAnim(
                                    new ScaleAnim(0.2f, new SineGraph(1f, 0.25f, 0f, 1f, 0), null),
                                    0.2f, true
                                ),
                            null,
                            new FadeAnim(0.3f, LinearGraph.oneToZero)
                    )
                    .attach();

            new TextBox()
                    .viewport(s.doneTextView)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).offset(0, -0.69f))
                    .text(new Text()
                            .font(authorizedFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .wrapChars(18f)
                            .center()
                            .text("User Verified")
                            .autoLength()
                    )
                    .animation(
                            new SequenceAnim(
                                    new FadeAnim(0.5f, LinearGraph.zeroToOne),
                                    0.2f, true
                            ),
                            null,
                            null
                    )
                    .attach();

            new TextBox()
                    .viewport(s.doneTextView)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).offset(0, -0.82f))
                    .text(new Text()
                            .font(noticeFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .wrapChars(22f)
                            .center()
                            .text("Please wait while FlapeeBird is being downloaded. You may continue to use your device while download is in progress.")
                            .autoLength()
                    )
                    .animation(
                            new SequenceAnim(
                                    new FadeAnim(0.5f, LinearGraph.zeroToOne),
                                    0.2f, true
                            ),
                            null,
                            null
                    )
                    .attach();

            s.tDoneDelay = 3f;
        }

        dialog.setInternal(s);

    }

}
