package game31.gb.phone;

import game31.Globals;
import game31.ScreenBar;
import game31.app.phone.PhoneRecentScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.ScaleAnim;
import sengine.audio.Sound;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
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
public class GBPhoneRecentScreen {

    public GBPhoneRecentScreen(PhoneRecentScreen screen) {
        PhoneRecentScreen.Internal s = new PhoneRecentScreen.Internal();

        Font nameFont = new Font("opensans-regular.ttf", 48, 0xf4f9fcff);

        Font deviceFont = new Font("opensans-light.ttf", 32, 0xf4f9fcff);

        Font timeFont = new Font("opensans-light.ttf", 32, 0x858688ff);


        Sprite sprite;


        // Row
        {
            Sprite touchedBg = new Sprite(380f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(touchedBg).set(0x091027ff);
            s.row = new Clickable()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .visuals(null, touchedBg, SaraRenderer.TARGET_BG)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance);

            s.profileView = new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchorLeft().anchor(+0.025f, 0).scale(40f / 370f))
                    .visual(Sprite.load("system/profile.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .attach();


            s.nameView = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(200f / 370f).anchorLeft().anchor(+63f / 370f, +10f / 51f))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(14f / 200f, 0)
                            .centerLeft()
                            .text("Cody Allison")
                    ).attach();



            s.timeView = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(170f / 370f).anchor(-8f / 370f, -11f / 51f))
                    .text(new Text()
                            .font(deviceFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(9f / 170f, 0)
                            .centerLeft()
                            .text("mobile")
                    ).attach();

            s.deviceView = new TextBox()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(55f / 370f).anchor(+107f / 370f, 0))
                    .text(new Text()
                            .font(timeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(10f / 55f, 0)
                            .centerRight()
                            .text("phone")
                    ).attach();


            s.outgoingIconMesh = Sprite.load("apps/calls/outgoing.png").instantiate();
            ColorAttribute.of(s.outgoingIconMesh).set(0x0fd32aff);
            s.incomingIconMesh = Sprite.load("apps/calls/incoming.png").instantiate();
            ColorAttribute.of(s.incomingIconMesh).set(0x2994f9ff);
            s.missedIconMesh = Sprite.load("apps/calls/missed.png").instantiate();
            ColorAttribute.of(s.missedIconMesh).set(0xf74d56ff);


            s.outgoingIconMetrics = new UIElement.Metrics().scale(14f / 370f).anchorLeft().anchor(+65f / 370f, -0.22f);
            s.incomingIconMetrics = s.outgoingIconMetrics.instantiate().scale(14f / 370f);
            s.missedIconMetrics = s.outgoingIconMetrics.instantiate().scale(19f / 370f);

            s.callTypeIcon = new StaticSprite()
                    .viewport(s.row)
                    .metrics(s.outgoingIconMetrics)
                    .visual(s.incomingIconMesh, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            // Call button
            sprite = Sprite.load("apps/calls/call.png").instantiate();
            ColorAttribute.of(sprite).set(0x858688ff);
            s.callButton = new Clickable()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().scale(22f / 370f).anchorRight().move(-0.035f, 0f))
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .animation(null, null,
                            new ScaleAnim(0.15f, new QuadraticGraph(1f, 1.2f, 1.6f, true)),
                            new ScaleAnim(0.2f, new QuadraticGraph(1.2f, 1f, -0.5f, true)),
                            null)
                    .inputPadding(3f, 1f, 1f, 1f)
                    .attach();

//            // Right arrow
//            sprite = Sprite.load("apps/calls/right.png").instantiate();
//            ColorAttribute.of(sprite).set(0x858688ff);
//            new StaticSprite()
//                    .viewport(s.row)
//                    .metrics(new UIElement.Metrics().scale(9f / 370f).anchorRight().move(-0.02f, 0f))
//                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
//                    .attach();

            // Line
            sprite = new Sprite(1f / 270f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x8c8ca4ff).alpha(0.25f);
            new StaticSprite()
                    .viewport(s.row)
                    .metrics(new UIElement.Metrics().anchor(+0.03f, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

        }


        // Window
        {
            // Window
            Sprite bgMat = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(bgMat).set(0x0e162dff);

            s.window = new UIElement.Group();

            // Background
            StaticSprite windowBg = new StaticSprite()
                    .viewport(s.window)
                    .visual(bgMat, SaraRenderer.TARGET_BG)
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
            s.bars.showAppbar("Recents", null);
            s.bars.showNavbar(true, true, true);
            s.bars.color(0x0e162dff, 0f, 0x0e162dff, 0.5f);
            s.bars.showShadows(0x0e162dff, 1f);
            s.bars.attach(screen);


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
            s.tabRecents.disable();
//            s.tabRecents.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabRecents.metrics.move(-x1, 0);
            s.tabRecents.find(tabIcon).visual(Sprite.load("apps/calls/tab-recents-active.png"));

            // Contacts button
            s.tabContacts = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabContacts.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabContacts.metrics.move(+x1, 0);
            s.tabContacts.find(tabIcon).visual(Sprite.load("apps/calls/tab-contacts-inactive.png"));

            // Dialer button
            s.tabDialer = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabDialer.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabDialer.metrics.move(+x2, 0);
            s.tabDialer.find(tabIcon).visual(Sprite.load("apps/calls/tab-dial-inactive.png"));
        }

        
        screen.setInternal(s);
    }
}
