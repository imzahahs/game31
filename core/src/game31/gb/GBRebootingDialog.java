package game31.gb;

import game31.RebootingDialog;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.RotateAnim;
import sengine.audio.Sound;
import sengine.calc.LinearGraph;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 5/15/2017.
 */

public class GBRebootingDialog {

    public GBRebootingDialog(RebootingDialog dialog) {
        RebootingDialog.Internal s = new RebootingDialog.Internal();


        Font titleFont = new Font("opensans-light.ttf", 48, 0x39395bff);

        Font noticeFont = new Font("opensans-regular.ttf", 32, 0x39395bff);


        Font descriptionFont = new Font("opensans-regular.ttf", 32, 0x39395bff);

        Animation buttonPressedAnim = new ColorAnim(0xeeeeeeff);

        Font answerFont = new Font("opensans-regular.ttf", 48, 0x363636ff);



        {
            s.window = new UIElement.Group()
                    .metrics(new UIElement.Metrics().anchorTop())
                    .length(0)
                    ;

            // Lock image
            Sprite sprite;

            PatchedSprite patch = PatchedSprite.create("system/rounded.png", 221f / 857f, 0.04f, 0.04f, 0.04f, 0);             // 0.16f
            ColorAttribute.of(patch).set(0x39395bff);

            StaticSprite lockBg = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorTop().offset(0, +0.01f))
                    .visual(patch, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .passThroughInput(false)
                    .attach();

            sprite = Sprite.load("system/dialog-options.png");
            ColorAttribute.of(sprite).set(0xa0a0ffff);

            StaticSprite lockIcon = new StaticSprite()
                    .viewport(lockBg)
                    .metrics(new UIElement.Metrics().scale(0.15f))
                    .visual(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .attach();

            TextBox titleView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.9f).anchorTop().offset(0, -0.35f))
                    .text(new Text()
                            .font(titleFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(17f / 276f, 0)
                            .text("System Restore")
                    )
                    .attach();

            TextBox messageView = new TextBox()
                    .viewport(titleView)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).offset(0, -0.07f))
                    .text(new Text()
                            .font(noticeFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .wrapChars(22f)
                            .center()
                            .text("Please wait while we restore your device, do not switch off while restoration is in progress.")
                            .autoLength()
                    )
                    .attach();


            // Loading circle
            sprite = Sprite.load("system/loading-circle.png").instantiate();
            ColorAttribute.of(sprite).set(0x39395bff);

            new StaticSprite()
                    .viewport(messageView)
                    .metrics(new UIElement.Metrics().scale(0.22f).anchorBottom().pan(0, -1).offset(0, -0.3f))
                    .visual(sprite, SaraRenderer.TARGET_IRIS_OVERLAY)
                    .animation(null, new RotateAnim(1f, new LinearGraph(0, 360)), null)
                    .attach();

            s.statusView = new TextBox()
                    .viewport(messageView)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).offset(0, -0.36f))
                    .text(new Text()
                            .font(descriptionFont)
                            .target(SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(20f / 276f, 18f)
                            .topCenter()
                            .text("Restoring Calls...")
                    )
                    .animation(null, new FadeAnim(1f, new SineGraph(1f, 1f, 0f, 0.25f, 0.75f)), null)
                    .attach();

            new UIElement.Group()
                    .viewport(s.statusView)
                    .metrics(new UIElement.Metrics().anchorBottom().offset(0, -0.06f))
                    .length(0)
                    .attach();

            s.startSound = Sound.load("sounds/restore_start.ogg");
            s.entrySound = Sound.load("sounds/restore_next.ogg");
            s.loopSound = "sounds/restore_loop.ogg";
            s.endSound = Sound.load("sounds/restore_ended.ogg");

        }

        dialog.setInternal(s);
    }

}
