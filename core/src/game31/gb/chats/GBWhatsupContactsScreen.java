package game31.gb.chats;

import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.ScreenBar;
import game31.app.chats.WhatsupContactsScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.ConstantGraph;
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
 * Created by Azmi on 20/7/2016.
 */
public class GBWhatsupContactsScreen {

    public GBWhatsupContactsScreen(WhatsupContactsScreen screen) {

        Font boldFont = new Font("opensans-semibold.ttf", 32);

        Font nameFont = new Font("opensans-semibold.ttf", 48);

        Font unreadFont = boldFont.instantiate();
        ColorAttribute.of(unreadFont).set(0x252531ff);

        Font messageFont = new Font("opensans-regular.ttf", "emojis/emojione", 32, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0x858688ff));
        Font timeFont = messageFont;


        Animation buttonPressedAnim = new ColorAnim(1f, new ConstantGraph(0.75f), ConstantGraph.one);


        // robotobold.color("WhatsupContactScreen.typing", 0x49bc33ff);
        boldFont.color("WhatsupContactScreen.typing", 0x7fee1dff);

        Sprite touchedBg = new Sprite(59f / 270f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(touchedBg).set(0x091027ff);
        Clickable bg = new Clickable()
                .metrics(new UIElement.Metrics().pan(0, -0.5f))
                .visuals(null, touchedBg, SaraRenderer.TARGET_BG)
                .sound(Sound.load("sounds/general_forward.ogg"))
                .passThroughInput(true)
                .maxTouchMoveDistance(Globals.maxTouchMoveDistance);

        Sprite sprite = Sprite.load("apps/calls/right.png").instantiate();
        ColorAttribute.of(sprite).set(0x858688ff);
        StaticSprite rightArrowView = new StaticSprite()
                .viewport(bg)
                .metrics(new UIElement.Metrics().anchor(+121f / 270f, -0.16f).scale(6f / 270f))
                .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                .attach();

        StaticSprite profile = new StaticSprite()
                .viewport(bg)
                .metrics(new UIElement.Metrics().anchor(-106f / 270f, 0).scale(35f / 270f))
                .visual(null, SaraRenderer.TARGET_INTERACTIVE)
                .attach();


        Sprite onlineIndicatorMesh = Sprite.load("apps/chats/circle.png").instantiate();
        ColorAttribute.of(onlineIndicatorMesh).set(0x1befd0ff);
        Sprite offlineIndicatorMesh = Sprite.load("apps/chats/circle-empty.png").instantiate();
        ColorAttribute.of(offlineIndicatorMesh).set(0xffffffff).alpha(0.4f);


        StaticSprite onlineIndicator = new StaticSprite()
                .viewport(bg)
                .metrics(new UIElement.Metrics().anchor(-72f / 270f, +9.5f / 59f).scale(10f / 270f))
                .visual(onlineIndicatorMesh, SaraRenderer.TARGET_INTERACTIVE)
                .attach();

        Animation onlineAnim = new SequenceAnim(new Animation[] {
                new FadeAnim(0.2f, new ConstantGraph(1f)),
                new FadeAnim(0.2f, new ConstantGraph(0.4f))
        });
        Animation offlineAnim = new ColorAnim(0xffffffff);

        TextBox nameView = new TextBox()
                .viewport(bg)
                .metrics(new UIElement.Metrics().anchor(+2f / 270f, +10f / 59f).scale(124f / 270f))
                .text(new Text()
                        .font(nameFont)
                        .position(14f / 124f, -9f)
                        .centerLeft()
                        .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                ).attach();


        TextBox messageView = new TextBox()
                .viewport(bg)
                .metrics(new UIElement.Metrics().anchorLeft().anchor(+58f / 270f, -10f / 59f).scale(184f / 270f))
                .text(new Text()
                        .font(messageFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                        .position(13f / 184f, 16f)
                        .ellipsize(1)
                        .centerLeft()
                ).attach();

        TextBox timeView = new TextBox()
                .viewport(bg)
                .metrics(new UIElement.Metrics().anchor(+100f / 270f, +0.18f).scale(50f / 270f))
                .text(new Text()
                        .font(timeFont)
                        .position(13f / 50f, -5f)
                        .centerRight()
                        .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                ).attach();

        // Bottom line
        Sprite line = new Sprite(1f / 290f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(line).set(0x70708855);
        StaticSprite lineView = new StaticSprite()
                .viewport(bg)
                .metrics(new UIElement.Metrics().anchorBottom().move(+0.04f, 0))
                .visual(line, SaraRenderer.TARGET_INTERACTIVE)
                .attach();

        StaticSprite unreadBg = new StaticSprite()
                .viewport(bg)
                .metrics(new UIElement.Metrics().anchor(-72f / 270f, +9.5f / 59f).scale(15f / 270f))
                .visual(onlineIndicatorMesh, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                .attach();


        TextBox unreadTextView = new TextBox()
                .viewport(unreadBg)
                .metrics(new UIElement.Metrics().scale(0.7f))
                .text(new Text()
                        .font(unreadFont)
                        .position(unreadBg.getLength(), -1.2f)
                        .target(SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                ).attach();


        screen.setRowGroup(bg, profile, nameView, messageView, timeView, unreadBg, unreadTextView,
                onlineIndicator,
                onlineIndicatorMesh, offlineIndicatorMesh,
                onlineAnim, offlineAnim
        );


        // Window

        UIElement.Group window = new UIElement.Group();

        sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x0e162dff);

        StaticSprite windowBg = new StaticSprite()
                .viewport(window)
                .visual(sprite, SaraRenderer.TARGET_BG)
                .attach();

        ScrollableSurface surface = new ScrollableSurface()
                .viewport(window)
                .length(Globals.LENGTH)
                .padding(0, 0.167f, 0, 0.18f)
                .scrollable(false, true)
                .selectiveRendering(true, false)
                .attach();

        ScreenBar bars = new ScreenBar();
        bars.showAppbar("Chats", null, 0, 0, 0, 0);
        bars.showShadows(0x0e162dff, 1f);
        bars.showNavbar(true, true, true);
        bars.attach(screen);


//        PatchedSprite patch = PatchedSprite.create("system/appbar-button.png", 0.8f, 0.36f);
//
//        Clickable refreshButton = new Clickable()
//                .viewport(appbar.window())
//                .metrics(new UIElement.Metrics().scale(0.14f).anchorRight().move(-0.03f, 0))
//                .visuals(patch, SaraRenderer.TARGET_APPBAR)
//                .animation(null, null, buttonPressedAnim, null, null)
//                .attach();
//
//        new StaticSprite()
//                .viewport(refreshButton)
//                .metrics(new UIElement.Metrics().scale(0.5f))
//                .visual(Sprite.load("apps/chats/refresh.png"), SaraRenderer.TARGET_APPBAR)
//                .attach();
        Clickable refreshButton = null;


        // Status
        String typingTitleFormat = "[WhatsupContactScreen.typing]%s is typing...[]";
        String selfTypingTitle = "[WhatsupContactScreen.typing]You are typing...[]";

        screen.setTitleFormats(typingTitleFormat, selfTypingTitle);

        screen.setWindow(window, surface, bars, refreshButton);

    }
}
