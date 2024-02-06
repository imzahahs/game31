package game31.gb;

import game31.NotEnoughSpaceDialog;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.audio.Sound;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

public class GBNotEnoughSpaceDialog {

    public GBNotEnoughSpaceDialog(NotEnoughSpaceDialog dialog) {
        NotEnoughSpaceDialog.Internal s = new NotEnoughSpaceDialog.Internal();



        Font titleFont = new Font("opensans-light.ttf", 48, 0x39395bff);

        Font noticeFont = new Font("opensans-regular.ttf", 32, 0x39395bff);

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
                            .text("irisOS 13 Only")
                    )
                    .attach();

            TextBox textView = new TextBox()
                    .viewport(titleView)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).offset(0, -0.100f))
                    .text(new Text()
                            .font(noticeFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .wrapChars(22f)
                    )
                    .autoLengthText("Your phone ([DIALOG_BOLD]irisOS 11[]) is not supported by this app. To learn about our upgrade options, please visit your nearest Iris Store.")
                    .attach();


            // Line
            s.okayButton = new Clickable()
                    .viewport(textView)
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

            s.openSound = Sound.load("sounds/general_invalid.ogg");
        }

        dialog.setInternal(s);

    }

}
