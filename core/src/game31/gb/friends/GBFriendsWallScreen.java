package game31.gb.friends;

import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.ScreenBar;
import game31.app.friends.FriendsWallScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.ScaleAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
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
 * Created by Azmi on 3/9/2017.
 */

public class GBFriendsWallScreen implements FriendsWallScreen.InterfaceSource {

    public GBFriendsWallScreen(FriendsWallScreen screen) {
        FriendsWallScreen.Internal s = new FriendsWallScreen.Internal();

        Sprite sprite;
        PatchedSprite patch;

        Font nameFont = new Font("opensans-bold.ttf", 32, 0xf4f9fcff);
        Font handleFont = new Font("opensans-light.ttf", 32, 0xb3b3b3ff);

        Font timeFont = new Font("opensans-light.ttf", 32, 0xb3b3b3ff);
        Font timeNewFont = new Font("opensans-bold.ttf", 32, 0x46c4f2ff);

        Font messageFont = new Font("opensans-regular.ttf", "emojis/emojione", 32, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0xf4f9fcff));

        Font commentButtonFont = new Font("opensans-regular.ttf", 32, 0x616868ff);

        Font regularFont = new Font("opensans-semibold.ttf", 32);
        regularFont.color("TAG", 0x46c4f2ff);
        regularFont.color("HANDLE", 0x46c4f2ff);

        Font boldFont = new Font("opensans-bold.ttf", 32);
        boldFont.color("JABBR_BOLD", 0xffffffff);

        Font locationLabelFont = new Font("opensans-light.ttf", 32, 0x46c4f2ff);

        {
            // Post
            s.postView = new Clickable()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))         // alight to top
                    .visuals(null, SaraRenderer.TARGET_BG)
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    ;

            s.postClearNewTime = 4f;

            // Line
            sprite = new Sprite(2f / 1029f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0b0c0fff);
            new StaticSprite()
                    .viewport(s.postView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            s.postGroup = new UIElement.Group()
                    .metrics(new UIElement.Metrics().anchorTop().scale(0.96f).offset(0, -0.02f))
                    ;

            s.postBgPaddingY = 0.04f;

            UIElement.Group headerGroup = new UIElement.Group()
                    .viewport(s.postGroup)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .length(154f / 1029f)
                    .attach();

            s.postUserProfileView = new StaticSprite()
                    .viewport(headerGroup)
                    .metrics(new UIElement.Metrics().scale(130f / 1029f).anchor(-448f / 1029f, 0))
                    .visual(Sprite.load("apps/jabbr/profile-default.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            s.postUserNameView = new TextBox()
                    .viewport(headerGroup)
                    .metrics(new UIElement.Metrics().scale(819f / 1029f).anchorLeft().move(+0.16f, +0.04f))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(39f / 819f, -16f)
                            .centerLeft()
                    )
                    .attach();
            s.postUserHandleView = new TextBox()
                    .viewport(s.postUserNameView)
                    .metrics(new UIElement.Metrics().anchorRight().pan(+1f, 0).scaleIndex(2).scale(819f / 1029f).move(+0.12f, 0))
                    .text(new Text()
                            .font(handleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(39f / 819f, -19f)
                            .bottomLeft()
                    )
                    .attach();

            s.postTimeView = new TextBox()
                    .viewport(headerGroup)
                    .metrics(new UIElement.Metrics().scale(150f / 1029f).anchorRight().move(0f, +0.04f))
                    .text(new Text()
                            .font(timeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(27f / 150f, 0)
                            .centerRight()
                    )
                    .attach();


            s.postTimeNewFont = timeNewFont;
            s.postTimeNewAnim = new FadeAnim(0.5f, new CompoundGraph(new Graph[] {
                    new ConstantGraph(0.5f, 0.5f),
                    new ConstantGraph(1.0f, 0.5f),
            }));


            s.postMessageView = new TextBox()
                    .viewport(headerGroup)
                    .metrics(new UIElement.Metrics().anchorRight().anchorTop().scale(0.76f).move(0, -0.09f))
                    .text(new Text()
                            .font(messageFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(1f, 18f)
                            .topLeft()
                    )
                    .attach();


            // Message icon
            sprite = Sprite.load("apps/jabbr/message.png").instantiate();
            ColorAttribute.of(sprite).set(0xccccccff);
            new StaticSprite()
                    .viewport(headerGroup)
                    .metrics(new UIElement.Metrics().anchorLeft().anchorTop().scale(0.05f).move(+0.165f, -0.09f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            // Location
            s.postLocationOffsetY = -0.05f;
            s.postLocationGroup = new UIElement.Group()
                    .viewport(s.postMessageView)
                    .metrics(new UIElement.Metrics().scale(1.1f).anchorBottom().anchorRight().pan(0, -1f).offset(0, 0))
                    .length(45f / 436f)
                    .attach();
            sprite = Sprite.load("apps/jabbr/location.png");
            ColorAttribute.of(sprite).set(0x46c4f2ff);
            new StaticSprite()
                    .viewport(s.postLocationGroup)
                    .metrics(new UIElement.Metrics().scale(24f / 436f).anchorLeft().move(+0.006f, 0))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            s.postLocationView = new TextBox()
                    .viewport(s.postLocationGroup)
                    .metrics(new UIElement.Metrics().scale(390f / 436f).anchorLeft().move(+40f / 436f, 0))
                    .text(new Text()
                            .font(locationLabelFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(27f / 390f, 18f)
                            .centerLeft()
                    )
                    .attach();
            s.postLocationFormat = "from [HANDLE]%s";


            s.postImageOffsetY = -0.05f;
            s.postImageView = new Clickable()
                    .viewport(s.postLocationGroup)
                    .metrics(new UIElement.Metrics().scale(1.1f).scaleIndex(2).anchorBottom().anchorRight().pan(0, -1f).offset(0, 0))
                    .visuals(null, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(null, null,
                            new ColorAnim(0.2f, new QuadraticGraph(1f, 0.8f, true), null),
                            new ColorAnim(0.2f, new QuadraticGraph(0.8f, 1f, true), null),
                            null
                    )
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .attach();

            // Rounded edges
            {
                sprite = Sprite.load("system/rounded-edge.png").instantiate();
                ColorAttribute.of(sprite).set(0x171c2dff);
                float size = 0.03f;
                new StaticSprite()
                        .viewport(s.postImageView)
                        .metrics(new UIElement.Metrics().anchor(-0.5f, +0.5f).pan(+0.5f, -0.5f).scale(+size, +size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.postImageView)
                        .metrics(new UIElement.Metrics().anchor(+0.5f, +0.5f).pan(+0.5f, -0.5f).scale(-size, +size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.postImageView)
                        .metrics(new UIElement.Metrics().anchor(+0.5f, -0.5f).pan(+0.5f, -0.5f).scale(-size, -size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.postImageView)
                        .metrics(new UIElement.Metrics().anchor(-0.5f, -0.5f).pan(+0.5f, -0.5f).scale(+size, -size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
            }

            UIElement.Group commentsGroup = new UIElement.Group()
                    .viewport(s.postImageView)
                    .metrics(new UIElement.Metrics().scaleIndex(3).anchorBottom().anchorRight().scale(1.1f).pan(0, -1f).offset(-0.02f, -0.02f))
                    .length(122f / 1029f)
                    .attach();

            float commentButtonLength = 122f / 350f;

            s.postLikeButton = new Clickable()
                    .viewport(commentsGroup)
                    .metrics(new UIElement.Metrics().scale(350f / 1029f).anchor(-0.34f, 0))
                    .length(commentButtonLength)
                    .text(new Text()
                            .font(commentButtonFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(+0.19f, 0f, 0.67f, commentButtonLength, -5f)
                            .centerLeft()
                    )
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .animation(null, null,
                            new ScaleAnim(0.15f, new QuadraticGraph(1f, 1.1f, -1.5f, true)),
                            new ScaleAnim(0.15f, new QuadraticGraph(1.1f, 1f, -0.5f, true)),
                            null
                    )
                    .sound(Sound.load("sounds/jabbr_like.ogg"))
                    .inputPadding(1f, 0.1f, 0, 0.1f)
                    .attach();


            s.postLikeEmptySprite = Sprite.load("apps/jabbr/heart.png").instantiate();
            ColorAttribute.of(s.postLikeEmptySprite).set(0x616868ff);
            s.postLikeFilledSprite = s.postLikeEmptySprite.instantiate();
            ColorAttribute.of(s.postLikeFilledSprite).set(0xfc2472ff);

            s.postLikeIcon = new StaticSprite()
                    .viewport(s.postLikeButton)
                    .metrics(new UIElement.Metrics().scale(0.16f).anchorLeft().anchor(+0.1f, 0))
                    .visual(s.postLikeEmptySprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            s.postCommentButton = new Clickable()
                    .viewport(commentsGroup)
                    .metrics(new UIElement.Metrics().scale(350f / 1029f).anchor(+0.002f, 0))
                    .length(commentButtonLength)
                    .text(new Text()
                            .font(commentButtonFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(+0.19f, 0f, 0.67f, commentButtonLength, -5f)
                            .centerLeft()
                    )
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .animation(null, null,
                            new ScaleAnim(0.15f, new QuadraticGraph(1f, 1.1f, -1.5f, true)),
                            new ScaleAnim(0.15f, new QuadraticGraph(1.1f, 1f, -0.5f, true)),
                            null
                    )
                    .sound(Sound.load("sounds/jabbr_comment.ogg"))
                    .inputPadding(0, 0.1f, 1f, 0.1f)
                    .attach();

            sprite = Sprite.load("apps/jabbr/comment.png").instantiate();
            ColorAttribute.of(sprite).set(0x616868ff);
            s.postCommentFilledSprite = sprite.instantiate();
            ColorAttribute.of(s.postCommentFilledSprite).set(0xfc2472ff);

            s.postCommentIcon = new StaticSprite()
                    .viewport(s.postCommentButton)
                    .metrics(new UIElement.Metrics().scale(0.16f).anchorLeft().anchor(+0.1f, 0))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            s.postFontFilledAnim = new ColorAnim(1f, 0xfc2472ff, false, 1);

            s.postIntervalY = 0;
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
//                    .padding(0, 0.30f, 0, 0.16f)          // Used for tabs
                    .padding(0, 0.165f, 0, 0.16f)
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
                    ; // .attach();         // Profile tab not needed for Pipe Dreams

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
            s.tabProfile.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabProfile.metrics.move(-x1, 0);
            s.tabProfile.find(tabIcon).visual(Sprite.load("apps/jabbr/tab-profile-inactive.png"));

            // Feed tab
            s.tabFeed = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabFeed.disable();
//            s.tabFeed.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabFeed.metrics.move(+x1, 0);
            s.tabFeed.find(tabIcon).visual(Sprite.load("apps/jabbr/tab-feed-active.png")).metrics.scale(190f / 820f);

            s.notificationIcon = Sprite.load("apps/jabbr/icon.png");
            s.notificationSound = Sound.load("sounds/jabbr_notify.ogg");

            s.tTimeRefreshInterval = 60;        // every 1 minute
        }


        // Commit
        screen.setInternal(s);
    }

    public String textLikes(int likes) {
        if(likes <= 0)
            return "";
        return Integer.toString(likes);
    }

    public String textComments(int comments) {
        if(comments <= 0)
            return "";
        return Integer.toString(comments);
    }

    public String textShortElapsed(long time) {
        // Convert to seconds
        time /= 1000;
        long current = Globals.grid.getSystemTime() / 1000;
        long elapsed = current - time;
        // Convert to minutes
        elapsed /= 60;
        if(elapsed < 1)
            elapsed = 1;
        if(elapsed < 60)
            return elapsed + " min";
        // Convert to hours
        elapsed /= 60;
        if(elapsed < 24)
            return elapsed + " hr";
        // Convert to days
        elapsed /= 24;
        if(elapsed < 30)
            return elapsed + " d";
        // Convert to months
        elapsed /= 30;
        if(elapsed < 12)
            return elapsed + " mo";
        // Convert to years
        elapsed /= 12;
        return elapsed + "yr";
    }
}
