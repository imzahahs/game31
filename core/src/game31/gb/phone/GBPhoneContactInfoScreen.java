package game31.gb.phone;

import game31.Globals;
import game31.ScreenBar;
import game31.app.phone.PhoneContactInfoScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.audio.Sound;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 24/8/2016.
 */
public class GBPhoneContactInfoScreen {

    public GBPhoneContactInfoScreen(PhoneContactInfoScreen screen) {
        PhoneContactInfoScreen.Internal s = new PhoneContactInfoScreen.Internal();

        Font nameFont = new Font("opensans-light.ttf", 48, 0xf4f9fcff);

        Font infoFont = new Font("opensans-regular.ttf", 48, 0xf4f9fcff);

        Font titleFont = new Font("opensans-light.ttf", 32, 0xf4f9fcff);


        Animation buttonPressedAnim = new ColorAnim(1f, 0x091027ff, false);

        Sprite sprite;

        // Header row
        {
            s.header = new UIElement.Group()
                    .length(470f / 640f)
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    ;


            s.profileView = new StaticSprite()
                    .viewport(s.header)
                    .metrics(new UIElement.Metrics().scale(0.5f).anchorTop().move(0, -0.05f))
                    .visual(Sprite.load("system/profile.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            s.maskMaterial = Material.load("system/circle-big.png");


            s.nameView = new TextBox()
                    .viewport(s.header)
                    .metrics(new UIElement.Metrics().scale(0.8f).anchorTop().move(0, -0.61f))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(30f / 470f, 0)
                            .text("Sara Young")
                    )
                    .attach();
        }

        // Row
        {
            s.row = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.84f).pan(0, -0.5f))
                    .visual("system/rounded.png", 0.08f, 0, 0, 0, 0, SaraRenderer.TARGET_INTERACTIVE)
                    .font(infoFont, 17f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .minSize(1f, 0)
                    .padding(0.5f, 1.6f, 2.0f, 0.7f)
                    .animation(null, new ColorAnim(0x00000000), buttonPressedAnim, null, null)
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .text("1, Infinity Rd\nCupertino California")
                    .centerLeft()
                    ;

            s.rowTitle = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(0.19f).anchorTop().anchorLeft().move(+0.032f, -0.035f))
                    .text(new Text()
                            .font(titleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(21f / 141f, 0)
                            .centerLeft()
                            .text("work")
                    ).attach();

            s.rowActionIcon = new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(22f / 370f).anchorRight().move(-0.04f, 0))
                    .target(SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(null, new ColorAnim(0x858688ff), null)      // colorize
                    .attach();

            s.rowYInterval = 0.00f;

            s.phoneAttribIcon = Sprite.load("apps/calls/call.png");

            // Line
            sprite = new Sprite(1f / 270f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x8c8ca4ff).alpha(0.25f);
            new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchor(+0.035f, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
        }


        // Window
        {
            // Window
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff);

            s.window = new UIElement.Group();

            // Background
            StaticSprite windowBg = new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();

            // Surface
            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .padding(0, 0.167f, 0, 0.18f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();

            // Screen bar
            s.bars = new ScreenBar();
            s.bars.showAppbar("Info", null);
            s.bars.showNavbar(true, true, true);
            s.bars.color(0x0e162dff, 0f, 0x0e162dff, 0.5f);
            s.bars.showShadows(0x0e162dff, 1f);
            s.bars.attach(screen);

            // Done
        }


        screen.setInternal(s);
    }
}
