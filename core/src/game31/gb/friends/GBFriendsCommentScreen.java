package game31.gb.friends;

import com.badlogic.gdx.graphics.Color;

import java.util.Arrays;
import java.util.HashSet;

import game31.Globals;
import game31.ScreenBar;
import game31.app.friends.FriendsCommentScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.ColorAnim;
import sengine.animation.MoveAnim;
import sengine.audio.Sound;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.InputField;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 6/8/2017.
 */

public class GBFriendsCommentScreen implements FriendsCommentScreen.InterfaceSource {

    public GBFriendsCommentScreen(FriendsCommentScreen screen) {
        FriendsCommentScreen.Internal s = new FriendsCommentScreen.Internal();

        Sprite sprite;
        PatchedSprite patch;

        Font nameFont = new Font("opensans-regular.ttf", 32, 0xf4f9fcff);
        Font handleFont = new Font("opensans-light.ttf", 32, 0xb3b3b3ff);

        Font inputFont = new Font("opensans-light.ttf", "emojis/emojione", 32, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0x2c507cff)); // new Font("robotoregular.ttf", "emojis/emojione", 32, Color.CLEAR, 0, Color.CLEAR, 0, 0, Color.WHITE);

        Font commentMessageFont = new Font("opensans-light.ttf", "emojis/emojione", 32, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0xf4f9fcff));


        {
            // Comments
            s.commentBgView = new StaticSprite()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .target(SaraRenderer.TARGET_BG_SHADOWS)
                    .passThroughInput(true)
                    ;

            s.commentBgPaddingY = 0.05f;

            // Line
            sprite = new Sprite(3f / 1029f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e1017ff);
            new StaticSprite()
                    .viewport(s.commentBgView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            s.commentGroup = new UIElement.Group()
                    .metrics(new UIElement.Metrics().anchorTop())
                    ;


            s.commentUserProfileView = new StaticSprite()
                    .viewport(s.commentGroup)
                    .metrics(new UIElement.Metrics().scale((130f / 1029f) * 0.96f).anchorTop().move(-0.415f, -0.025f))
                    .visual(Sprite.load("apps/jabbr/profile-default.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            s.commentUserNameView = new TextBox()
                    .viewport(s.commentGroup)
                    .metrics(new UIElement.Metrics().scale(780f / 1029f).anchorTop().anchorLeft().move(+180f / 1029f, -0.05f))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(39f / 780f, -19f)
                            .centerLeft()
                    )
                    .attach();

            s.commentUserHandleView = new TextBox()
                    .viewport(s.commentUserNameView)
                    .metrics(new UIElement.Metrics().anchorRight().pan(+1f, 0).scaleIndex(2).scale(819f / 1029f).move(+0.12f, 0))
                    .text(new Text()
                            .font(handleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(39f / 819f, -22f)
                            .bottomLeft()
                    )
                    .attach();

            s.commentMessageView = new TextBox()
                    .viewport(s.commentGroup)
                    .metrics(new UIElement.Metrics().scale(830f / 1029f).anchorTop().anchorLeft().move(+180f / 1029f, -0.10f))
                    .text(new Text()
                            .font(commentMessageFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(27f / 830f, 22f)
                            .centerLeft()
                    )
                    .attach();

            s.commentImageOffsetY = -0.05f;
            s.commentImageView = new Clickable()
                    .viewport(s.commentMessageView)
                    .metrics(new UIElement.Metrics().scale(1f).anchorBottom().pan(0, -1f).offset(0, 0))
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
                ColorAttribute.of(sprite).set(0x1e2f38ff);
                float size = 0.03f;
                new StaticSprite()
                        .viewport(s.commentImageView)
                        .metrics(new UIElement.Metrics().anchor(-0.5f, +0.5f).pan(+0.5f, -0.5f).scale(+size, +size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.commentImageView)
                        .metrics(new UIElement.Metrics().anchor(+0.5f, +0.5f).pan(+0.5f, -0.5f).scale(-size, +size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.commentImageView)
                        .metrics(new UIElement.Metrics().anchor(+0.5f, -0.5f).pan(+0.5f, -0.5f).scale(-size, -size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                new StaticSprite()
                        .viewport(s.commentImageView)
                        .metrics(new UIElement.Metrics().anchor(-0.5f, -0.5f).pan(+0.5f, -0.5f).scale(+size, -size))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
            }

            s.commentIntervalY = 0;
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
                    .metrics(new UIElement.Metrics().anchorTop())
                    .length(Globals.LENGTH - 0.27f)
                    .padding(0, 0.165f, 0, 0)
                    .scrollable(false, true)
                    .selectiveRendering(true, true)
                    .attach();

            s.bars = new ScreenBar();
            s.bars.showAppbar("Comments", null, 0, 0, 0, 0);
            s.bars.showNavbar(true, true, true);
            s.bars.color(0x171c2dff, 0.9f, 0x171c2dff, 0.25f);
            s.bars.showShadows(0x171c2dff, 0.25f, 0x171c2dff, 0.7f);
            s.bars.attach(screen);

            // Comment bar
            sprite = new Sprite(245f / 2251f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xcae5dcff);
            StaticSprite commentBar = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().move(0, +0.16f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .attach();

            sprite = Sprite.load("apps/jabbr/send.png").instantiate();
            ColorAttribute.of(sprite).set(0x2c507cff);
            s.commentButton = new Clickable()
                    .viewport(commentBar)
                    .metrics(new UIElement.Metrics().scale(99f / 2251f).anchorRight().move(-0.05f, 0))
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(
                            null, null,
                            new MoveAnim(0.12f, new QuadraticGraph(0f, +0.4f, +1.1f, true), null),
                            new MoveAnim(0.12f, new QuadraticGraph(+0.4f, 0f, 0f, true), null),
                            null
                    )
                    .inputPadding(50f, 1f, 10f, 0.5f)
                    .attach();



            s.inputField = new InputField()
                    .viewport(commentBar)
                    .metrics(new UIElement.Metrics().anchorLeft().move(+0.03f, +0.005f).scale(1817f / 2251f))
                    .font(inputFont, 80f / 1817f, -4.5f, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                    .attach();

            s.commentTypeMessageView = new TextBox()
                    .viewport(s.inputField)
                    .text(new Text()
                            .font(inputFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                            .position(s.inputField.getLength(), s.inputField.wrapChars())
                            .topLeft()
                            .text("Type a Message")
                    )
                    .attach();

            s.tSmoothMoveTime = 0.2f;

            s.keyboardPaddingY = 0.16f;


            s.tTimeRefreshInterval = 60;        // every 1 minute
        }


        {
            // Profanity
            s.profanityWords = new HashSet(Arrays.asList(
                    "fuck",
                    "fucker",
                    "fucks",
                    "fucking",
                    "fuckin",
                    "motherfucker",
                    "shit",
                    "bitch",
                    "damn",
                    "crap",
                    "piss",
                    "dick",
                    "cock",
                    "pussy",
                    "asshole",
                    "arsehole",
                    "fag",
                    "bastard",
                    "slut",
                    "douche",
                    "bloody",
                    "cunt",
                    "bollocks"
            ));
        }


        // Commit
        screen.setInternal(s);
    }


    public Mesh createCommentBg(float length, boolean isNew) {
        Sprite sprite = new Sprite(length, SaraRenderer.renderer.coloredMaterial);
        if(!isNew)
            ColorAttribute.of(sprite).set(0x1c253aff);      // existing

        return sprite;
    }



    public String textLongElapsed(long time) {
        // Convert to seconds
        time /= 1000;
        long current = Globals.grid.getSystemTime() / 1000;
        long elapsed = current - time;
        // Convert to minutes
        elapsed /= 60;
        if(elapsed <= 1)
            return "a minute ago";
        if(elapsed < 60)
            return elapsed + " minutes ago";
        // Convert to hours
        elapsed /= 60;
        if(elapsed <= 1)
            return "an hour ago";
        if(elapsed < 24)
            return elapsed + " hours ago";
        // Convert to days
        elapsed /= 24;
        if(elapsed <= 1)
            return "yesterday";
        if(elapsed < 30)
            return elapsed + " days ago";
        // Convert to months
        elapsed /= 30;
        if(elapsed <= 1)
            return "last month";
        if(elapsed < 12)
            return elapsed + " months ago";
        // Convert to years
        elapsed /= 12;
        if(elapsed <= 1)
            return "last year";
        return elapsed + " years ago";
    }

}
