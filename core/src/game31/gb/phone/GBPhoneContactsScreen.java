package game31.gb.phone;

import game31.Globals;
import game31.ScreenBar;
import game31.app.phone.PhoneContactsScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.audio.Sound;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 24/8/2016.
 */
public class GBPhoneContactsScreen {

    public GBPhoneContactsScreen(PhoneContactsScreen screen) {
        PhoneContactsScreen.Internal s = new PhoneContactsScreen.Internal();


        Font nameFont = new Font("opensans-regular.ttf", 48, 0xf4f9fcff);

        Font headerFont = new Font("opensans-regular.ttf", 48, 0xf4f9fcff);

        Sprite sprite;

        // Header
        {
            sprite = new Sprite(40f / 250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x00415fff).alpha(0);
            s.header = new StaticSprite()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE);

            s.headerTextView = new TextBox()
                    .viewport(s.header)
                    .text(new Text()
                            .font(headerFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.9f, sprite.length * 0.6f, -14f)
                            .bottomLeft()
                    ).attach();

        }

        // Row
        {
            Sprite touchedBg = new Sprite(58f / 370f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(touchedBg).set(0x091027ff);
            s.row = new Clickable()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .visuals(null, touchedBg, SaraRenderer.TARGET_BG)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance);

            s.profileView = new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchorLeft().anchor(+0.05f, 0).scale(40f / 370f))
                    .visual(null, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            s.nameView = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(155f / 370f).anchorLeft().anchor(+75f / 370f, 0))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(14f / 155f, 0)
                            .centerLeft()
                            .text("Cody Allison")
                    ).attach();

            // Line
            sprite = new Sprite(1f / 270f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x8c8ca4ff).alpha(0.25f);
            new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchor(+0.05f, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
        }


        // Window
        {
            // Window
            Sprite whiteBg = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(whiteBg).set(0x0e162dff);

            s.window = new UIElement.Group();

            // Background
            StaticSprite windowBg = new StaticSprite()
                    .viewport(s.window)
                    .visual(whiteBg, SaraRenderer.TARGET_BG)
                    .attach();

            // Surface
            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .padding(0, 0.30f, 0, 0.25f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();

            // Screen bar
            s.bars = new ScreenBar();
            s.bars.showAppbar("Contacts", null);
            s.bars.showNavbar(true, true, true);
            s.bars.color(0x0e162dff, 0f, 0x0e162dff, 0.5f);
            s.bars.showShadows(0x0e162dff, 1f);
            s.bars.attach(screen);

            // Done

            // Tab buttons
            sprite = new Sprite(300f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x808dffff).alpha(0.05f);

            StaticSprite tabGroup = new StaticSprite()
                    .viewport(s.bars.appbar())
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                    .attach();

            sprite = new Sprite(1f / 270f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x808dffff).alpha(0.05f);
            new StaticSprite()
                    .viewport(tabGroup)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            float x2 = 0.35f;
            float x1 = 0.12f;

            sprite = new Sprite(10f / 128f, Material.load("apps/calls/tab-select.png"));

            Animation tabPressedAnim = new ColorAnim(0.1f, null, new LinearGraph(0.3f, 0.7f), false);
            Animation tabReleasedAnim = new ColorAnim(0.1f, null, new LinearGraph(0.7f, 0f), false);
            Animation tabInactiveAnim = new ColorAnim(1, null, ConstantGraph.zero);

            Clickable tabButton = new Clickable()
                    .metrics(new UIElement.Metrics().scale(420f / 2250f).anchorTop())
                    .visuals(sprite, SaraRenderer.TARGET_APPBAR)
                    .inputPadding(0.2f, 0.3f, 0.2f, 0.8f)
                    .sound(Sound.load("sounds/general_changetab.ogg"))
                    ;
            StaticSprite tabIcon = new StaticSprite()
                    .viewport(tabButton)
                    .metrics(new UIElement.Metrics().scale(153f / 420f).move(0, -148f / 420f))
                    .target(SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Favourites button
            s.tabFavourites = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabFavourites.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabFavourites.metrics.move(-x2, 0);
            s.tabFavourites.find(tabIcon).visual(Sprite.load("apps/calls/tab-favs-inactive.png"));

            // Recents button
            s.tabRecents = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabRecents.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabRecents.metrics.move(-x1, 0);
            s.tabRecents.find(tabIcon).visual(Sprite.load("apps/calls/tab-recents-inactive.png"));

            // Contacts button
            s.tabContacts = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabContacts.disable();
//            s.tabContacts.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabContacts.metrics.move(+x1, 0);
            s.tabContacts.find(tabIcon).visual(Sprite.load("apps/calls/tab-contacts-active.png"));

            // Dialer button
            s.tabDialer = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabDialer.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabDialer.metrics.move(+x2, 0);
            s.tabDialer.find(tabIcon).visual(Sprite.load("apps/calls/tab-dial-inactive.png"));
        }
        
        // Commit
        screen.setInternal(s);
    }
}
