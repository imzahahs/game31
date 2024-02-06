package game31.gb.mail;

import java.util.Locale;

import game31.Globals;
import game31.ScreenBar;
import game31.app.mail.MailInboxScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.ColorAnim;
import sengine.audio.Sound;
import sengine.graphics2d.Font;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 15/8/2016.
 */
public class GBMailInboxScreen implements MailInboxScreen.InterfaceSource  {

    public GBMailInboxScreen(MailInboxScreen screen) {
        MailInboxScreen.Internal s = new MailInboxScreen.Internal();


        Font nameFont = new Font("opensans-semibold.ttf", 48, 0xf4f9fcff);

        Font subjectFont = new Font("opensans-regular.ttf", 40, 0xf4f9fcff);

        Font messageFont = new Font("opensans-regular.ttf", 40, 0x858688ff);
        Font timeFont = messageFont;


        Sprite sprite;

        {
            Sprite touchedBg = new Sprite(140f / 640f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(touchedBg).set(0x091027ff);

            s.row = new Clickable()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .visuals(null, touchedBg, SaraRenderer.TARGET_BG)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance);


            s.rowNameView = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchorLeft().anchor(+60f / 640f, +45f / 156f).scale(372f / 640f))
                    .text(new Text()
                            .font(nameFont)
                            .position(24f / 372f, 10f)
                            .centerLeft()
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .ellipsize(1)
                    )
                    .attach();
            s.rowNameUnreadAnim = new ColorAnim(0xffd902ff, false);

            s.rowSubjectView = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchorLeft().anchor(+60f / 640f, 0f / 156f).scale(504f / 640f))
                    .text(new Text()
                            .font(subjectFont)
                            .position(21f / 504f, 19f)
                            .centerLeft()
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .ellipsize(1)
                    ).attach();


            s.rowMessageView = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchorLeft().anchor(+60f / 640f, -46f / 156f).scale(504f / 640f))
                    .text(new Text()
                            .font(messageFont)
                            .position(35f / 504f, 20f)
                            .centerLeft()
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .ellipsize(1)
                    ).attach();

            s.rowTimeView = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchor(+231f / 640f, +50f / 156f).scale(138f / 640f))
                    .text(new Text()
                            .font(timeFont)
                            .position(22f / 138f, -6.5f)
                            .centerRight()
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    ).attach();

            // Right arrow
            sprite = Sprite.load("apps/calls/right.png").instantiate();
            ColorAttribute.of(sprite).set(0x858688ff);

            new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(50f / 2250f).anchorRight().move(-0.035f, -0.02f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            // Bottom line
            sprite = new Sprite(2f / 960f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x707088ff);
            new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(0.93f).anchor(0, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            sprite = Sprite.load("system/notification-bg.png").instantiate();
            ColorAttribute.of(sprite).set(0xffd902ff);

            s.rowUnreadIcon = new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchorTop().anchorLeft().move(+0.04f, -0.035f).scale(17f / 640f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
        }

        // Window
        {
            s.window = new UIElement.Group();

            // Bg
            Sprite whiteBg = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(whiteBg).set(0x0e162dff);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(whiteBg, SaraRenderer.TARGET_BG)
                    .attach();

            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .padding(0, 0.167f, 0, 0.18f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();

            s.bars = new ScreenBar();
            s.bars.showAppbar("Inbox", null, 0, 0, 0, 0);
            s.bars.showShadows(0x0e162dff, 1f);
            s.bars.showNavbar(true, true, true);
            s.bars.attach(screen);

            s.messageSound = Sound.load("sounds/general_notify.ogg");
            s.notificationIcon = Sprite.load("apps/mail/icon.png");
        }

        // Commit
        screen.setInternal(s);
    }

    public String buildUnreadTitleString(int totalUnread) {
        if(totalUnread == 0)
            return "Inbox";
        else
            return String.format(Locale.US, "Inbox (%,d)", totalUnread);
    }
}
