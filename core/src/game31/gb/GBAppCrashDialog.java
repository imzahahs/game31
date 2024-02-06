package game31.gb;

import game31.AppCrashDialog;
import game31.glitch.MpegGlitch;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

public class GBAppCrashDialog {

    public GBAppCrashDialog(AppCrashDialog dialog) {
        AppCrashDialog.Internal s = new AppCrashDialog.Internal();



        Font titleFont = new Font("opensans-light.ttf", 48, 0x39395bff);

        Font noticeFont = new Font("opensans-regular.ttf", 32, 0x39395bff);

        new Font("opensans-bold.ttf", 32, 0x363636ff).name("DIALOG_BOLD");
        Font cancelFont = new Font("opensans-semibold.ttf", 32, 0x363636ff);

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

            TextBox titleView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.9f).anchorTop().offset(0, -0.28f))
                    .text(new Text()
                            .font(titleFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(17f / 276f, 0)
                            .text("App Malfunction")
                    )
                    .attach();

            s.textView = new TextBox()
                    .viewport(titleView)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).offset(0, -0.100f))
                    .text(new Text()
                            .font(noticeFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .wrapChars(22f)
                            .center()
                            .autoLength()
                    )
                    .attach();

            s.textFormat = "Unfortunately, [DIALOG_BOLD]%s[] was found working out of bounds and has to be stopped. Please contact IRIS Support for recovery options.";

            // Line
            s.okayButton = new Clickable()
                    .viewport(s.textView)
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
                            .text("Close")
                    )
                    .attach();
            sprite = new Sprite(0.003f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x39395bff).alpha(0.5f);
            new StaticSprite()
                    .viewport(s.okayButton)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .attach();

            s.glitch = new MpegGlitch(null, "sounds/glitch_medium.ogg");
            s.glitch.setGlitchGraph(
                    new CompoundGraph(new Graph[]{
                            new ConstantGraph(2.8f, 0.3f),
                            new ConstantGraph(0.0f, 0.1f),
                            new ConstantGraph(2.8f, 0.2f),
                            new ConstantGraph(0.0f, 0.15f),
                            new ConstantGraph(2.8f, 0.1f),
                            new ConstantGraph(0.0f, 0.12f),
                            new ConstantGraph(2.8f, 0.4f),
                            new ConstantGraph(0.0f, 0.06f),
                            new ConstantGraph(3.8f, 0.2f),
                            new ConstantGraph(0.0f, 0.19f),
                            new ConstantGraph(2.8f, 0.2f),
                            new ConstantGraph(0.0f, 0.11f),
                            new ConstantGraph(3.8f, 0.4f),
                            new ConstantGraph(0.0f, 0.15f),
                            new ConstantGraph(2.8f, 0.15f),
                            new ConstantGraph(0.0f, 0.07f),
                            new ConstantGraph(2.8f, 0.2f),
                            new ConstantGraph(0.0f, 0.12f),
                            new ConstantGraph(3.8f, 0.4f),
                            new ConstantGraph(0.0f, 0.19f)
                    }),
                    true,
                    null
            );
            s.glitch.setGlitchLoopThreshold(0.5f);
            s.glitch.setLsdEffect(0.5f, 0);
            s.glitch.setDetachScheduled(1.5f);
            s.glitch.setInputBlockTime(Float.MAX_VALUE);

        }

        dialog.setInternal(s);

    }

}
