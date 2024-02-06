package game31.gb.mail;

import com.badlogic.gdx.graphics.Color;

import java.util.Locale;

import game31.Globals;
import game31.ScreenBar;
import game31.app.mail.MailThreadScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.NullAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 17/8/2016.
 */
public class GBMailThreadScreen implements MailThreadScreen.InterfaceSource  {


    public GBMailThreadScreen(MailThreadScreen screen) {
        MailThreadScreen.Internal s = new MailThreadScreen.Internal();

        Animation buttonPressedAnim = new ColorAnim(0xeeeeeeff);

        Font boldFont = new Font("opensans-bold.ttf", 32);


        Font headerFont = new Font("opensans-regular.ttf", 32, 0x858688ff);
        headerFont.color("MAIL_INFO", 0xffd902ff);

//        Font messageFont = new Font("opensans-regular.ttf", 40, 0xffffffff);
        Font messageFont = new Font("opensans-regular.ttf", "emojis/emojione", 40, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0xffffffff));
        Font messageBoldFont = new Font("opensans-bold.ttf", "emojis/emojione", 40, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0xffffffff));
        messageBoldFont.color("MAIL_BOLD", 0xffffffff);

        Font messageItalicFont = new Font("opensans-italic.ttf", "emojis/emojione", 40, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0xffffffff));
        messageItalicFont.color("MAIL_ITALIC", 0xffffffff);

        messageFont.color("LINK", 0x0645adff);         // Link colors TODO: implement on click ?

        Font regularFontMaroon = new Font("opensans-regular.ttf", 32, 0x5e5e5eff);


        Sprite line = new Sprite(2f / 960f, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(line).set(0xccccccff);

        Font actionFont = new Font("opensans-bold.ttf", 32, 0x000000FF);


        // Reply group
        {
            s.latestMessageView = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.92f).pan(0, -0.5f))
                    .text(new Text()
                            .font(messageFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .wrapChars(21f)
                            .centerLeft()
                    );

            s.replyInfoView = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.95f).pan(0, -0.5f))
                    .text(new Text()
                            .font(regularFontMaroon)
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .wrapChars(21f)
                            .centerLeft()
                    );

            s.replyMessageView = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.96f).anchorRight().pan(0, -0.5f))
                    .text(new Text()
                            .font(messageFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.95f, 1, 21f)
                            .centerLeft()
                    );

            // Corruption views
            float corruptedPadding = 0.7f;
            s.latestCorruptedView = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.85f).pan(0, -0.5f))
                    .visual("system/rounded-glow.png.NoiseMaterial", 0.09f, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(corruptedPadding, corruptedPadding, corruptedPadding, corruptedPadding)
                    .font(messageFont, 21f, 200f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .centerLeft()
                    .minSize(1f, 0)
                    .animation(null,
                            new CompoundAnim(1f, new Animation[] {
                                    new ColorAnim(0x000000ff),
                                    new ColorAnim(1f, 0x888888ff, true, 1),
                            }),
                            null
                    )
                    ;

            s.replyCorruptedView = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.82f).anchorRight().pan(-0.03f, -0.5f))
                    .visual("system/rounded-glow.png.NoiseMaterial", 0.09f, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(corruptedPadding, corruptedPadding, corruptedPadding, corruptedPadding)
                    .font(messageFont, 21f, 200f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .centerLeft()
                    .minSize(1f, 0.2f)
                    .animation(null,
                            new CompoundAnim(1f, new Animation[] {
                                    new ColorAnim(0x000000ff),
                                    new ColorAnim(1f, 0x888888ff, true, 1),
                            }),
                            null
                    )
                    ;

            s.corruptedFixButton = new Clickable()
                    .metrics(new UIElement.Metrics().scale(0.22f))
                    .visuals(Sprite.load("system/fix.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            new ScaleAnim(0.3f, new QuadraticGraph(0f, 1f, 1.2f, true)),
                            new SequenceAnim(new Animation[] {
                                    new NullAnim(0.5f),
                                    new ColorAnim(0.5f, 0.5f, 0.5f, 0.5f, 1f, true)
                            }),
                            new ScaleAnim(0.1f, new QuadraticGraph(1f, 1.2f, true)),
                            new ScaleAnim(0.2f, new LinearGraph(1.2f, 1f)),
                            null
                    )
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    ;

            s.corruptedFixInputPaddingX = 10f;
            s.corruptedFixInputPaddingY = 0.1f;

            s.tCorruptedTextInterval = Globals.tCorruptedTextInterval;


            Sprite indicatorSprite = new Sprite(1f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(indicatorSprite).set(0xa0a0a0ff);

            s.replyIndicatorView = new StaticSprite()
                .metrics(new UIElement.Metrics().scale(2f / 640f, 1).anchorLeft().anchor(+20f / 640f, 0).pan(0, -0.5f))
                .visual(indicatorSprite, SaraRenderer.TARGET_INTERACTIVE);


            // Action
            s.latestActionButton = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.92f).pan(0, -0.5f))
                    .visual("system/rounded-shadowed.png", 0.03f, SaraRenderer.TARGET_INTERACTIVE)
                    .center()
                    .font(actionFont, 18f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .padding(2f, 1.4f, 2f, 1.4f)
                    .animation(
                            null,
                            new ColorAnim(0xffd902ff),
                            new ColorAnim(0xddddddff),
                            null,
                            null)
                    .inputPadding(1f, 0.2f, 1f, 0.2f)
                    .minSize(0.3f, 0)
                    .enable()
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    ;
            s.replyActionButton = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.92f).anchor(+0.02f, 0).pan(0, -0.5f))
                    .visual("system/rounded-shadowed.png", 0.03f, SaraRenderer.TARGET_INTERACTIVE)
                    .center()
                    .font(actionFont, 20f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .padding(2f, 1.4f, 2f, 1.4f)
                    .animation(
                            null,
                            new ColorAnim(0xffd902ff),
                            new ColorAnim(0xddddddff),
                            null,
                            null)
                    .inputPadding(1f, 0.2f, 1f, 0.2f)
                    .minSize(0.3f, 0)
                    .enable()
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
            ;

            // Graphic
            s.latestGraphicView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(0.92f).pan(0, -0.5f))
                    .target(SaraRenderer.TARGET_INTERACTIVE)
                    ;

            s.replyGraphicView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(0.90f).anchorLeft().anchor(+0.067f, 0).pan(0, -0.5f))
                    .target(SaraRenderer.TARGET_INTERACTIVE)
                    ;

            // Reply image view
            s.latestImageView = new Clickable()
                    .metrics(new UIElement.Metrics().scale(0.9f).pan(0, -0.5f))
                    .target(SaraRenderer.TARGET_BG)
                    .animation(null, null,
                            new ScaleAnim(0.2f, new QuadraticGraph(1f, 1.03f, true)),
                            new ScaleAnim(0.2f, new QuadraticGraph(1.03f, 1f, true)),
                            null
                    )
                    .maxTouchMoveDistance(0.05f)
                    .passThroughInput(true);

            s.latestImageActionView = new PatchedTextBox()
                    .viewport(s.latestImageView)
                    .visual("system/rounded.png", 0.08f, 0.08f, 0f, 0.08f, 0f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(0.3f).anchorBottom().anchorRight())
                    .minSize(1f, 20f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("View")
                    .refresh()
                    .animation(null, new ColorAnim(1,1,1, 0.9f), buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(0.05f)
                    .passThroughInput(true)
                    .enable()
                    .attach();

            s.replyImageView = new Clickable()
                    .metrics(new UIElement.Metrics().scale(0.9f).anchorLeft().anchor(+0.067f, 0).pan(0, -0.5f))
                    .target(SaraRenderer.TARGET_BG)
                    .animation(null, null,
                            new ScaleAnim(0.2f, new QuadraticGraph(1f, 1.03f, true)),
                            new ScaleAnim(0.2f, new QuadraticGraph(1.03f, 1f, true)),
                            null
                    )
                    .maxTouchMoveDistance(0.05f)
                    .passThroughInput(true);

            s.replyImageActionView = new PatchedTextBox()
                    .viewport(s.replyImageView)
                    .visual("system/rounded.png", 0.08f, 0.08f, 0f, 0.08f, 0f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(0.3f).anchorBottom().anchorRight())
                    .minSize(1f, 20f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("View")
                    .refresh()
                    .animation(null, new ColorAnim(1,1,1, 0.9f), buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(0.05f)
                    .passThroughInput(true)
                    .enable()
                    .attach();

            // Reply video view
            s.latestVideoView = new Clickable()
                    .metrics(new UIElement.Metrics().scale(0.3f).anchorLeft().anchor(+20f / 640f, 0).pan(0, -0.4f))
                    .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(0.05f)
                    .passThroughInput(true);

            s.latestVideoActionView = new PatchedTextBox()
                    .viewport(s.latestVideoView)
                    .visual("apps/chats/action-bg.png", 0.06f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(1.1f).anchor(+1.25f, 0f))
                    .minSize(1f, 26f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("Play")
                    .refresh()
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(0.05f)
                    .passThroughInput(true)
                    .enable()
                    .attach();

            // Movie indicator
            Sprite movieBgMat = new Sprite(15f / 85f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(movieBgMat).set(0, 0, 0, 0.6f);
            StaticSprite movieBg = new StaticSprite()
                    .viewport(s.latestVideoView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(movieBgMat, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            new StaticSprite()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(22f / 150f).anchor(-49f / 150f, 0))
                    .visual(Sprite.load("apps/gallery/video-icon.png"), SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            s.latestVideoDurationView = new TextBox()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(85f / 150f).anchor(+22f / 150f, 0))
                    .text(new Text()
                            .font(boldFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_SUB_TEXT)
                            .position(13f / 85f, 0)
                            .text("3:42")
                            .centerRight()
                    )
                    .attach();

            s.replyVideoView = new Clickable()
                    .metrics(new UIElement.Metrics().scale(0.3f).anchorLeft().anchor(+0.07f, 0).pan(0, -0.5f))
                    .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(0.05f)
                    .passThroughInput(true);

            s.replyVideoActionView = new PatchedTextBox()
                    .viewport(s.replyVideoView)
                    .visual("apps/chats/action-bg.png", 0.06f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(1.1f).anchor(+1.25f, 0f))
                    .minSize(1f, 26f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("Play")
                    .refresh()
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(0.05f)
                    .passThroughInput(true)
                    .enable()
                    .attach();

            // Movie indicator
            movieBg = new StaticSprite()
                    .viewport(s.replyVideoView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(movieBgMat, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            new StaticSprite()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(22f / 150f).anchor(-49f / 150f, 0))
                    .visual(Sprite.load("apps/gallery/video-icon.png"), SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            s.replyVideoDurationView = new TextBox()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(85f / 150f).anchor(+22f / 150f, 0))
                    .text(new Text()
                            .font(boldFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_SUB_TEXT)
                            .position(13f / 85f, 0)
                            .text("3:42")
                            .centerRight()
                    )
                    .attach();



            s.messageIntervalY = 0.1f;
            s.replyInfoViewIntervalY = 0.04f;
            s.blockIntervalY = +0.01f;
        }


        // Header group
        {
            // Header group
            s.headerGroup = new UIElement.Group()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f));

            s.headerFromView = new TextBox()
                    .viewport(s.headerGroup)
                    .metrics(new UIElement.Metrics().anchorTop().anchorLeft().scale(0.57f).move(+0.04f, -0.04f))
                    .text(new Text()
                            .font(headerFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(160f / 1940f, -14f)
                            .centerLeft()
                            .text("From:")
                    )
                    .attach();
            s.headerFromLabel = "From:  [MAIL_INFO]";

            s.headerToView = new TextBox()
                    .viewport(s.headerGroup)
                    .metrics(new UIElement.Metrics().anchorTop().anchorLeft().scale(0.57f).move(+0.04f, -0.095f))
                    .text(new Text()
                            .font(headerFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(160f / 1940f, -14f)
                            .centerLeft()
                            .text("To:")
                    )
                    .attach();
            s.headerToLabel = "To:  [MAIL_INFO]";

            s.headerSubjectView = new TextBox()
                    .viewport(s.headerGroup)
                    .metrics(new UIElement.Metrics().anchorTop().anchorLeft().scale(0.92f).move(+0.04f, -0.155f))
                    .text(new Text()
                            .font(headerFont)
                            .position(180f / 1940f, 22f)
                            .text("Project Update #14: Drifer: A Space Trading Game by Celcius Game Studios")
                            .centerLeft()
                            .autoLength()
                    )
                    .attach();
            s.headerSubjectLabel = "Subject:  [MAIL_INFO]";

            s.headerTimeView = new TextBox()
                    .viewport(s.headerGroup)
                    .metrics(new UIElement.Metrics().anchorTop().anchorRight().scale(0.35f).offset(-0.08f, -0.135f))
                    .text(new Text()
                            .font(headerFont)
                            .position(1, 9.2f)
                            .text("21 September 2012, 6.42PM")
                            .topRight()
                    )
                    .attach();

        }



        // Window
        {
            s.window = new UIElement.Group();

            Sprite whiteBg = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(whiteBg).set(0x0e162dff);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(whiteBg, SaraRenderer.TARGET_BG)
                    .attach();

            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .padding(0, 0.167f, 0, 0.25f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();

            s.bars = new ScreenBar();
            s.bars.showAppbar("7 of 8", null, 0, 0, 0, 0);
            s.bars.showNavbar(true, true, true);
            s.bars.color(0x0e162dff, 0f, 0x0e162dff, 0.5f);
            s.bars.showShadows(0x0e162dff, 1f);
            s.bars.attach(screen);


            // Bottom tab
            Sprite sprite = new Sprite(114f / 1080f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x666666ff);
            s.replyButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .text(new Text()
                            .font(actionFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                            .position(37f / 1080f, 0)
                            .text("Offline") // TODO .text("Write a Reply")
                    )
                    ; // .attach();
        }


        // Commit
        screen.setInternal(s);
    }

    public String buildThreadTimeString(String date, String time) {
        return String.format(Locale.US, "%s\n%s", date, time);
    }

    public String buildReplyInfoString(String name, String email, String date, String time) {
        // On 23 July 2016, 6:43PM, Jonathan S. Geller <jon@cmail.mac> wrote:
        return String.format(Locale.US, "On %s, %s, [MAIL_INFO]%s <%s>[] sent:", date, time, name, email);
    }
}
