package game31.gb.friends;

import game31.Globals;
import game31.ScreenBar;
import game31.app.friends.FriendsProfileScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.audio.Sound;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 6/10/2017.
 */

public class GBFriendsProfileScreen {

    public GBFriendsProfileScreen(FriendsProfileScreen screen) {
        FriendsProfileScreen.Internal s = new FriendsProfileScreen.Internal();

        Sprite sprite;
        PatchedSprite patch;

        Font nameFont = new Font("opensans-regular.ttf", 48, 0xf4f9fcff);
        Font handleFont = new Font("opensans-regular.ttf", 32, 0xb3b3b3ff);

        Font descriptionFont = new Font("opensans-light.ttf", 32, 0xf4f9fcff);


        {
            // Header
            s.headerGroup = new UIElement.Group()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    ;

            s.headerBannerView = new StaticSprite()
                    .viewport(s.headerGroup)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .length(687f / 2251f).target(SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            s.headerProfileView = new StaticSprite()
                    .viewport(s.headerBannerView)
                    .metrics(new UIElement.Metrics().scale(457f / 2251f).move(-818f / 2251f, -329f / 2251f))
                    .visual(Sprite.load("apps/jabbr/profile-default.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();

            s.headerNameView = new TextBox()
                    .viewport(s.headerBannerView)
                    .metrics(new UIElement.Metrics().scale(2087f / 2251f).move(0, -650f / 2251f))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(100f / 2251f, 0)
                            .centerLeft()
                    )
                    .attach();
            s.headerHandleView = new TextBox()
                    .viewport(s.headerBannerView)
                    .metrics(new UIElement.Metrics().scale(2087f / 2251f).move(0, -772f / 2251f))
                    .text(new Text()
                            .font(handleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(75f / 2251f, 0)
                            .centerLeft()
                    )
                    .attach();

            s.headerDescriptionView = new TextBox()
                    .viewport(s.headerBannerView)
                    .metrics(new UIElement.Metrics().scale(2087f / 2251f).move(0, -900f / 2251f).pan(0, -0.5f))
                    .text(new Text()
                            .font(descriptionFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(1f, 22f)
                            .centerLeft()
                    )
                    .attach();

            // Line
            sprite = new Sprite(2f / 1029f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0b0c0fff);
            new StaticSprite()
                    .viewport(s.headerDescriptionView)
                    .metrics(new UIElement.Metrics().anchorBottom().scale(1f / s.headerDescriptionView.metrics.scaleX).move(0, -0.04f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

        }

        {
            // Window
            s.window = new UIElement.Group();

            // Bg
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x171c2dff);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();

            // Surface
            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .padding(0, 0.30f, 0, 0.16f)
                    .scrollable(false, true)
                    .selectiveRendering(true, true)
                    .attach();

            s.bars = new ScreenBar();
            s.bars.showAppbar(null, null, 0, 0, 0, 0);
            s.bars.showNavbar(true, true, true);
            s.bars.color(0x171c2dff, 0.9f, 0x171c2dff, 0.25f);
            s.bars.showShadows(0x171c2dff, 0.25f, 0x171c2dff, 0.7f);
            s.bars.attach(screen);

            // Title
            new StaticSprite()
                    .viewport(s.bars.appbar())
                    .metrics(new UIElement.Metrics().scale(500f / 2250f).anchorLeft().move(+105f / 2250f, -0.000f))
                    .visual(Sprite.load("apps/jabbr/title.png"), SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Tab buttons
            sprite = new Sprite(300f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x293450ff).alpha(0.6f);

            StaticSprite tabGroup = new StaticSprite()
                    .viewport(s.bars.appbar())
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                    .attach();

            sprite = new Sprite(4f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x293450ff).alpha(0.5f);
            new StaticSprite()
                    .viewport(tabGroup)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();

            float x1 = 0.25f;

            sprite = new Sprite(4f / 128f, Material.load("apps/jabbr/tab-select.png"));
            ColorAttribute.of(sprite).set(0x46c4f2ff);

            Animation tabPressedAnim = new ColorAnim(0.1f, null, new LinearGraph(0.3f, 0.7f), false);
            Animation tabReleasedAnim = new ColorAnim(0.1f, null, new LinearGraph(0.7f, 0f), false);
            Animation tabInactiveAnim = new ColorAnim(1, null, ConstantGraph.zero);

            Clickable tabButton = new Clickable()
                    .metrics(new UIElement.Metrics().scale(820f / 2250f).anchorTop())
                    .visuals(sprite, SaraRenderer.TARGET_APPBAR)
                    .inputPadding(0.8f, 0.3f, 0.8f, 0.5f)
                    .sound(Sound.load("sounds/general_changetab.ogg"))
                    ;
            StaticSprite tabIcon = new StaticSprite()
                    .viewport(tabButton)
                    .metrics(new UIElement.Metrics().scale(153f / 820f).move(0, -73f / 420f))
                    .target(SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Profile tab
            s.tabProfile = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabProfile.disable();
//            s.tabProfile.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabProfile.metrics.move(-x1, 0);
            s.tabProfile.find(tabIcon).visual(Sprite.load("apps/jabbr/tab-profile-active.png"));

            // Feed tab
            s.tabFeed = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabFeed.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabFeed.metrics.move(+x1, 0);
            s.tabFeed.find(tabIcon).visual(Sprite.load("apps/jabbr/tab-feed-inactive.png")).metrics.scale(190f / 820f);
        }

        // Commit
        screen.setInternal(s);
    }

}
