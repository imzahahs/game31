package game31.gb.chats;

import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.ScreenBar;
import game31.app.chats.WhatsupThreadScreen;
import game31.renderer.SaraRenderer;
import game31.renderer.TextEffectCompositor;
import game31.renderer.TextEffectGenerator;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.NullAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.InputField;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 15/7/2016.
 */
public class GBWhatsupMessageThread {


    public GBWhatsupMessageThread(WhatsupThreadScreen thread) {
        WhatsupThreadScreen.Internal s = new WhatsupThreadScreen.Internal();

        Sprite sprite;
        PatchedSprite patch;

        Animation buttonPressedAnim = new ColorAnim(1f, new ConstantGraph(0.75f), ConstantGraph.one);
        Animation buttonReleasedAnim = new ColorAnim(0.3f, new LinearGraph(0.75f, 1f), ConstantGraph.one);

        Font textFont = new Font("opensans-regular.ttf", "emojis/emojione", 40, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0xffffffff));
        Font userTextFont = new Font("opensans-regular.ttf", "emojis/emojione", 40, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0x1f1f22ff));

        Font senderBoldFont = textFont; // new Font("opensans-bold.ttf", "emojis/emojione", 40, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0xffffffff));
        Font userBoldFont = textFont; // new Font("opensans-bold.ttf", "emojis/emojione", 40, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0x1f1f22ff));

        Font inputFont = new Font("opensans-regular.ttf", "emojis/emojione", 40, Color.CLEAR, 0, Color.CLEAR, 0, 0, Color.BLACK); // new Font("robotoregular.ttf", "emojis/emojione", 32, Color.CLEAR, 0, Color.CLEAR, 0, 0, Color.WHITE);

        Font actionFont = new Font("opensans-regular.ttf", 32, 0x4d4d4dff);

        Font linkTextFont = new Font("opensans-semibold.ttf", 32, 0xffffffff);
        Font linkUrlFont = new Font("opensans-regular.ttf", 32, 0x75ee83ff);

        Font boldFont = new Font("opensans-semibold.ttf", 32);


        Font nameFont = new Font("opensans-bold.ttf", 32);

        Font timeFont = new Font("opensans-regular.ttf", 25, 0xb3b3b377);
        Font userTimeFont = new Font("opensans-regular.ttf", 25, 0x2f2e3377);

        Font newMessageFont = new Font("opensans-light.ttf", 32);

        Font dayFont = new Font("opensans-semibold.ttf", 40, 0x0e162dff);

        Font inviteButtonFont = new Font("opensans-bold.ttf", 40, 0x000000ff);


        float messageCornerSize = 0.13f; // 0.07f;

        float messageWrapChars = 16f;

        float leftPadding = 0.75f;
        float topPadding = 0.75f;
        float rightPadding = 0.75f;
        float bottomPadding = 1.2f;

        float messageYInterval = +0.025f;
        float dateYInterval = -0.035f;


        {
            // Text effects
            TextEffectGenerator textEffectGenerator = new TextEffectGenerator();
            textEffectGenerator.amount = 0.05f;
            s.textEffectGenerator = new Sprite(Globals.LENGTH, textEffectGenerator);

            TextEffectCompositor textEffectCompositor = new TextEffectCompositor();
            textEffectCompositor.blackThreshold = 0.2f;
            textEffectCompositor.whiteThreshold = 0.4f;
            s.textEffectCompositor = new Sprite(Globals.LENGTH, textEffectCompositor);
            ColorAttribute.of(s.textEffectCompositor).set(0,0,0, 0.7f);

            s.tTextEffectTimeout = 5f;


            // Window
            s.window = new UIElement.Group();

            // Bg
//            sprite = new Sprite(Grid.LENGTH, SaraRenderer.renderer.coloredMaterial);
//            ColorAttribute.of(sprite).set(0x252531ff);
            sprite = Sprite.load("apps/chats/bg-tile.png").instantiate();
            sprite.tile(1f * 7.5f, Globals.LENGTH * 7.8f);
            ColorAttribute.of(sprite).set(0xddddddff);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();


            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .length(Globals.LENGTH)
                    .padding(0, 0.185f, 0, 0.32f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();

            s.bars = new ScreenBar();
            s.bars.showAppbar("Sara Whitman", "last seen 6.41AM", +0.12f, 0, +0.17f, 0);
            s.bars.showNavbar(true, true, true);
            s.bars.color(0x0e162dff, 0f, 0x0e162dff, 0.5f);
            s.bars.showShadows(0x0e162dff, 1f);
            s.bars.attach(thread);

            // Profile
            s.profileView = new StaticSprite()
                    .viewport(s.bars.appbar())
                    .metrics(new UIElement.Metrics().scale(0.11f).anchorLeft().move(+0.03f, 0))
                    .visual(Sprite.load("system/profile.png"), SaraRenderer.TARGET_APPBAR)
                    .attach();

            s.onlineIndicatorMesh = Sprite.load("apps/chats/circle.png").instantiate();
            ColorAttribute.of(s.onlineIndicatorMesh).set(0x1befd0ff);
            s.offlineIndicatorMesh = Sprite.load("apps/chats/circle-empty.png").instantiate();
            ColorAttribute.of(s.offlineIndicatorMesh).set(0xf74d56ff);

            s.onlineIndicatorView = new StaticSprite()
                    .viewport(s.bars.appbar())
                    .metrics(new UIElement.Metrics().scale(0.03f).anchorLeft().move(+0.17f, -0.038f))
                    .visual(s.onlineIndicatorMesh, SaraRenderer.TARGET_APPBAR)
                    .attach();

            UIElement.Group chatbar = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().move(0, 390f / 2250f))
                    .length(35f / 270f)
                    .attach();


            {
                float chatbarWidth = 1043f;
                float chatbarHeight = 116f;
                float chatbarLength = chatbarHeight / chatbarWidth;

                s.chatButton = new Clickable()
                        .viewport(chatbar)
                        .metrics(new UIElement.Metrics().scale(chatbarWidth / 1080f))
                        .length(chatbarLength)
//                    .visuals(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                        .inputPadding(0.05f, 0.07f, 0.05f, 0.03f)
                        .attach();

                s.chatField = new InputField()
                        .viewport(s.chatButton)
                        .metrics(new UIElement.Metrics().pan(0f, -0.0f).scale(0.9f))
                        .font(inputFont, s.chatButton.getLength() * 0.4f, -3.5f, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                        .cursor("", "", 0.5f)
                        .attach();

                float borderSize = 7f;
                float inborderWidth = chatbarWidth - (borderSize * 2);
                float inborderHeight = chatbarHeight - (borderSize * 2);
                float inborderLength = inborderHeight / inborderWidth;
                // Chatbar active view
                s.sendActiveView = new UIElement.Group()
                        .viewport(s.chatButton)
                        .animation(
                                null,
                                null,
                                new ScissorAnim(0.3f, new Animation[] {
                                        new ScaleAnim(1f, ScaleAnim.Location.LEFT, QuadraticGraph.oneToZeroInverted, null)
                                })
                        )
                        ;
                patch = PatchedSprite.create("system/circle-medium.png", chatbarLength, chatbarLength / 2f);
                ColorAttribute.of(patch).set(0x0dcc62ff);
                new StaticSprite()
                        .viewport(s.sendActiveView)
                        .visual(patch, SaraRenderer.TARGET_APPBAR)
                        .animation(
                                new FadeAnim(0.3f, LinearGraph.zeroToOne),
                                null,
                                new FadeAnim(0.3f, LinearGraph.oneToZero)
                        )
                        .attach();
                patch = PatchedSprite.create("system/circle-medium.png", inborderLength, inborderLength / 2f);
                ColorAttribute.of(patch).set(0xf9f9f9ff);
                new StaticSprite()
                        .viewport(s.sendActiveView)
                        .metrics(new UIElement.Metrics().scale(inborderWidth / chatbarWidth))
                        .visual(patch, SaraRenderer.TARGET_APPBAR)
                        .animation(
                                new FadeAnim(0.3f, LinearGraph.zeroToOne),
                                null,
                                new FadeAnim(0.3f, LinearGraph.oneToZero)
                        )
                        .attach();

                UIElement.Group activeGroup = new UIElement.Group()
                        .viewport(s.sendActiveView)
                        .metrics(new UIElement.Metrics().scale(288f / 1043f).anchorRight())
                        .animation(
                                new ScissorAnim(0.3f, new Animation[] {
                                        new ScaleAnim(1f, ScaleAnim.Location.RIGHT, QuadraticGraph.zeroToOneInverted, null)
                                }),
                                null,
                                null
                        )
                        .scissor(true)
                        .length(116f / 288f)
                        .attach();

                float cornerSize = (116f / 288f) / 2f;
                patch = PatchedSprite.create("system/circle-medium.png", 116f / 288f, 0, cornerSize, cornerSize, cornerSize);
                ColorAttribute.of(patch).set(0x0dcc62ff);
                s.sendActiveButton = new Clickable()
                        .viewport(activeGroup)
                        .visuals(patch, SaraRenderer.TARGET_APPBAR)
                        .animation(
                                null,
                                new ScissorAnim(1f, new Animation[] {
                                        new ScaleAnim(1f, ScaleAnim.Location.RIGHT, new SineGraph(1f, 1f, 0f, 0.03f, 0.97f), null)
                                }),
                                buttonPressedAnim,
                                buttonReleasedAnim,
                                null
                        )
                        .attach();
                new StaticSprite()
                        .viewport(s.sendActiveButton)
                        .metrics(new UIElement.Metrics().scale(52f / 288f).anchorRight().offset(-0.73f, 0))
                        .visual(Sprite.load("apps/chats/send-icon.png"), SaraRenderer.TARGET_APPBAR)
                        .attach();

                new StaticSprite()
                        .viewport(s.sendActiveButton)
                        .metrics(new UIElement.Metrics().scale(101f / 288f).anchorRight().offset(-1.3f, 0))
                        .visual(Sprite.load("apps/chats/send-active-icon.png"), SaraRenderer.TARGET_APPBAR)
                        .attach();


                // Chatbar inactive view
                patch = PatchedSprite.create("system/circle-medium.png", chatbarLength, chatbarLength / 2f);
                ColorAttribute.of(patch).set(0x494949ff);
                StaticSprite sendInactiveView = new StaticSprite()
                        .viewport(s.chatButton)
                        .visual(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                        .attach();
                patch = PatchedSprite.create("system/circle-medium.png", inborderLength, inborderLength / 2f);
                ColorAttribute.of(patch).set(0x666666ff);
                new StaticSprite()
                        .viewport(sendInactiveView)
                        .metrics(new UIElement.Metrics().scale(inborderWidth / chatbarWidth))
                        .visual(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                        .attach();


                cornerSize = (116f / 134f) / 2f;
                patch = PatchedSprite.create("system/circle-medium.png", 116f / 134f, 0, cornerSize, cornerSize, cornerSize);
                ColorAttribute.of(patch).set(0x494949ff);
                s.sendInactiveButton = new Clickable()
                        .viewport(sendInactiveView)
                        .metrics(new UIElement.Metrics().scale(134f / 1043f).anchorRight())
                        .visuals(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                        .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null)
                        .inputPadding(1f, 0.7f, 1f, 1f)
                        .attach();
                new StaticSprite()
                        .viewport(s.sendInactiveButton)
                        .metrics(new UIElement.Metrics().scale(52f / 134f).anchorRight().offset(-0.73f, 0))
                        .visual(Sprite.load("apps/chats/send-icon.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                        .attach();

            }


            s.statusTypingTitleFormat = "%s is typing...";
            s.statusSelfTypingTitle = "You are typing...";
            s.statusOnline = "Online";
            s.statusOffline = "Offline";

            Animation invisibleColor = new ColorAnim(0.15f, 0xffffff00, false);
            Animation normalColor = new NullAnim(0.15f);
            s.offlineIndicatorAnim = new SequenceAnim(new Animation[] {
                    invisibleColor,
                    normalColor,
                    invisibleColor,
                    normalColor,
                    invisibleColor,
                    normalColor,
                    invisibleColor,
                    normalColor
            });

            s.messageReceivedSound = Sound.load("sounds/chat_reply.ogg");

            s.smoothScrollSpeed = 1.2f;
            s.keyboardPaddingY = 360f / 2250f; // 0.28f;
            s.tOnlineNextThreshold = 10f;       // If next message is within 10 seconds, assume online
        }

        {
            // Sender message view
            s.senderMessageBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorLeft().anchor(+0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-npc.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(0.2f, 0)
                    .centerLeft()
                    .font(textFont, messageWrapChars, 200f, SaraRenderer.TARGET_INTERACTIVE_TEXT);
            s.senderFirstVisual = "apps/chats/bubble-npc-first.png";

            s.senderMessageTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(-0.5f, -0.5f).pan(+0.84f, +3.0f))
                    .text(new Text()
                            .font(timeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -7.0f)
                            .centerLeft()
                    );


            s.senderMessageYInterval = messageYInterval;

            s.senderCustomFonts = new Font[] {
                    senderBoldFont
            };
            s.senderCustomFontNames = new String[] {
                    "demon"
            };
        }


        {
            // Group message view
            s.groupMessageBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorLeft().anchor(+0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-npc.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding + 0.65f, rightPadding, bottomPadding)
                    .centerLeft()
                    .font(textFont, messageWrapChars, 200f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .minSize(0.2f, 0f)
                    ;

            s.groupMessageNameView = new TextBox()
                    .viewport(s.groupMessageBox)
                    .metrics(new UIElement.Metrics().scale(0.3f).scaleIndex(2).anchorTop().anchorLeft().offset(+0.207f, -0.16f))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(18f / 110f, -8f)
                            .centerLeft()
                    )
                    .attach();

            // Group name colors
            s.groupMemberFontColors = new int[]{
                    0xff7777ff,
                    0xa4ff77ff,
                    0x77e7ffff,
                    0x7784ffff,
                    0xed77ffff,
                    0xffea77ff
            };

            s.groupMessageTimeBox = s.senderMessageTimeBox;
            s.groupMessageYInterval = messageYInterval;
        }


        {
            // Sender photo view
            s.senderPhotoBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorLeft().anchor(+0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-npc.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(0.9f, 0.9f);


            s.senderPhotoView = new Clickable()
                    .viewport(s.senderPhotoBox)
                    .metrics(new UIElement.Metrics().scale(0.88f).anchor(0, +0.035f))
                    .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .attach();

            sprite = Sprite.load("system/rounded-edge.png").instantiate();
            ColorAttribute.of(sprite).set(0x392a70ff);
            float cornerSize = 0.06f;
            StaticSprite corner = new StaticSprite()
                    .viewport(s.senderPhotoView)
                    .metrics(new UIElement.Metrics().scale(+cornerSize, +cornerSize).anchor(-0.5f, +0.5f).pan(+0.5f, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(-cornerSize, +cornerSize).anchor(+0.5f, +0.5f).pan(+0.5f, -0.5f))
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(+cornerSize, -cornerSize).anchor(-0.5f, -0.5f).pan(+0.5f, -0.5f))
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(-cornerSize, -cornerSize).anchor(+0.5f, -0.5f).pan(+0.5f, -0.5f))
                    .attach();

            s.senderPhotoActionView = new PatchedTextBox()
                    .viewport(s.senderPhotoBox)
                    .visual("apps/chats/action-bg.png", 0.06f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(45f / 111f).anchor(+0.72f, 0f))
                    .minSize(1f, 26f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("View")
                    .refresh()
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .enable()
                    .attach();

            s.senderPhotoTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(-0.5f, -0.5f).pan(+0.84f, +3.0f))
                    .text(new Text()
                            .font(timeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -7.0f)
                            .centerLeft()
                    );

            s.senderPhotoYInterval = messageYInterval;
        }


        {
            // Sender video view
            s.senderVideoBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorLeft().anchor(+0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-npc.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(0.83f, 0.83f);

            s.senderVideoView = new Clickable()
                    .viewport(s.senderVideoBox)
                    .metrics(new UIElement.Metrics().scale(0.87f).anchor(0, +0.033f))
                    .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .attach();

            s.senderVideoActionView = new PatchedTextBox()
                    .viewport(s.senderVideoBox)
                    .visual("apps/chats/action-bg.png", 0.06f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(45f / 111f).anchor(+0.72f, 0f))
                    .minSize(1f, 26f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("Play")
                    .refresh()
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .enable()
                    .attach();

            // Movie indicator
            sprite = new Sprite(10f / 85f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0, 0, 0, 0.6f);
            StaticSprite movieBg = new StaticSprite()
                    .viewport(s.senderVideoView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            new StaticSprite()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(14f / 150f).anchor(-58f / 150f, 0))
                    .visual(Sprite.load("apps/gallery/video-icon.png"), SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            s.senderVideoDurationView = new TextBox()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(85f / 150f).anchor(+26f / 150f, 0))
                    .text(new Text()
                            .font(boldFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_SUB_TEXT)
                            .position(9f / 85f, 0)
                            .text("3:42")
                            .centerRight()
                    )
                    .attach();

            // Edges
            sprite = Sprite.load("system/rounded-edge.png").instantiate();
            ColorAttribute.of(sprite).set(0x392a70ff);
            float cornerSize = 0.06f;
            StaticSprite corner = new StaticSprite()
                    .viewport(s.senderVideoView)
                    .metrics(new UIElement.Metrics().scale(+cornerSize, +cornerSize).anchor(-0.5f, +0.5f).pan(+0.5f, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(-cornerSize, +cornerSize).anchor(+0.5f, +0.5f).pan(+0.5f, -0.5f))
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(+cornerSize, -cornerSize).anchor(-0.5f, -0.5f).pan(+0.5f, -0.5f))
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(-cornerSize, -cornerSize).anchor(+0.5f, -0.5f).pan(+0.5f, -0.5f))
                    .attach();

            s.senderVideoTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(-0.5f, -0.5f).pan(+0.84f, +3.0f))
                    .text(new Text()
                            .font(timeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -7.0f)
                            .centerLeft()
                    );

            s.senderVideoYInterval = messageYInterval;
        }

        {
            // Sender audio view
            s.senderAudioBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorLeft().anchor(+0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-npc.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(1.0f, 0.16f);


            // Mic icon
            sprite = Sprite.load("apps/gallery/mic-icon.png").instantiate();
            ColorAttribute.of(sprite).set(0xffffffff);
            StaticSprite micView = new StaticSprite()
                    .viewport(s.senderAudioBox)
                    .metrics(new UIElement.Metrics().scale(0.12f).anchor(+0.61f, 0))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            sprite = sprite.instantiate();
            ColorAttribute.of(sprite).set(0x0dcc62ff);
            new StaticSprite()
                    .viewport(micView)
                    .metrics(new UIElement.Metrics().scale(1f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .name("levels")
                    .attach();

            s.audioLevelAnim = new ScissorAnim(1f, new Animation[] {
                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, ConstantGraph.one, LinearGraph.zeroToOne)
            });


            sprite = Sprite.load("system/circle.png").instantiate();
            ColorAttribute.of(sprite).set(0x2d2158ff);
            s.senderAudioPlayButton = new Clickable()
                    .viewport(s.senderAudioBox)
                    .metrics(new UIElement.Metrics().scale(0.19f).move(-0.34f, +0.0f))
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .inputPadding(10f, 0.35f, 10f, 0.35f)
                    .attach();

            s.audioPlaySprite = Sprite.load("apps/chats/play.png");
            s.audioStopSprite = Sprite.load("apps/chats/pause.png");

            new StaticSprite()
                    .viewport(s.senderAudioPlayButton)
                    .metrics(new UIElement.Metrics().scale(0.5f))
                    .visual(s.audioPlaySprite, SaraRenderer.TARGET_INTERACTIVE)
                    .name("icon")
                    .attach();


            PatchedSprite progressBgMat = PatchedSprite.create("system/circle.png", 13f / 547f, (13f / 547f) / 2f);
            ColorAttribute.of(progressBgMat).set(0x2d2158ff);

            PatchedSprite progressBarMat = progressBgMat.instantiate();
            ColorAttribute.of(progressBarMat).set(0xffffffff);

            float knobSize = 0.09f;
            new HorizontalProgressBar()
                    .viewport(s.senderAudioPlayButton)
                    .metrics(new UIElement.Metrics().scale(3.4f).anchorRight().move(+0.12f, 0).pan(+1f, 0f))
                    .visual(progressBgMat, progressBarMat, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .knob(Sprite.load("system/circle.png"), knobSize, knobSize / 2f, knobSize / 2f)
                    .progress(0.8f)
                    .length(progressBgMat.length)
                    .name("progressbar")
                    .attach();



            s.senderAudioTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(+0.5f, -0.5f).pan(-0.84f, +3.5f))
                    .text(new Text()
                            .font(timeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -7.0f)
                            .centerRight()
                    );


            s.senderAudioYInterval = messageYInterval;
        }

        {
            // Sender link view
            s.senderLinkBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f * 0.71f).anchorLeft().anchor(+0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-npc.png", messageCornerSize * (1f / 0.71f), SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding * 6.0f, rightPadding, bottomPadding * 1.8f)
                    .animation(
                            null,
                            null,
                            new ColorAnim(0xeeeeeeff),
                            null,
                            null
                    )
                    .centerLeft()
                    .font(linkTextFont, (messageWrapChars / (1f / 0.7f)) * 1.1f, 200f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .enable()
                    .minSize(1f, 0f)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    ;

            sprite = new Sprite(550f / 931f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xffffffff);

            s.senderLinkThumbnailView = new StaticSprite()
                    .viewport(s.senderLinkBox)
                    .metrics(new UIElement.Metrics().scale(0.85f).anchorTop().move(0, -0.08f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            // Edges
            sprite = Sprite.load("system/rounded-edge.png").instantiate();
            ColorAttribute.of(sprite).set(0x392a70ff);
            float cornerSize = 0.06f;
            StaticSprite corner = new StaticSprite()
                    .viewport(s.senderLinkThumbnailView)
                    .metrics(new UIElement.Metrics().scale(+cornerSize, +cornerSize).anchor(-0.5f, +0.5f).pan(+0.5f, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(-cornerSize, +cornerSize).anchor(+0.5f, +0.5f).pan(+0.5f, -0.5f))
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(+cornerSize, -cornerSize).anchor(-0.5f, -0.5f).pan(+0.5f, -0.5f))
                    .attach();
            corner.instantiate()
                    .metrics(new UIElement.Metrics().scale(-cornerSize, -cornerSize).anchor(+0.5f, -0.5f).pan(+0.5f, -0.5f))
                    .attach();

            s.senderLinkUrlView = new TextBox()
                    .viewport(s.senderLinkBox)
                    .metrics(new UIElement.Metrics().scale(0.55f).anchorBottom().anchorLeft().move(+0.10f, +0.17f))
                    .text(new Text()
                            .font(linkUrlFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.15f, 9f)
                            .centerLeft()
                            .ellipsize(1)
                    )
                    .attach();
            
            s.senderLinkActionView = new PatchedTextBox()
                    .viewport(s.senderLinkBox)
                    .visual("apps/chats/action-bg.png", 0.06f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(58f / 111f).anchor(+0.82f, 0f))
                    .minSize(1f, 26f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("Open")
                    .refresh()
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .enable()
                    .attach();


            s.senderLinkTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(-0.5f, -0.5f).pan(+0.84f, +3.0f))
                    .text(new Text()
                            .font(timeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -7.0f)
                            .centerLeft()
                    );


            s.senderLinkYInterval = messageYInterval;
        }

        {
            // Sender invite group
            float cornerSize = messageCornerSize * 0.6f;
            float bottomLength = 105f / 597f;
            float length = (608f / 1080f) + bottomLength;
            patch = PatchedSprite.create("apps/chats/bubble-npc.png", length,  cornerSize);
            s.senderInviteGroup = new StaticSprite()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f).scale(0.940f).anchor(0, -0.01f))
                    .visual(patch, SaraRenderer.TARGET_INTERACTIVE)
                    ;
            s.senderInviteImageView = new StaticSprite()
                    .viewport(s.senderInviteGroup)
                    .metrics(new UIElement.Metrics().anchorTop().scale(0.957f))
                    .length(length - bottomLength)
                    .target(SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            {
                sprite = Sprite.load("system/rounded-edge.png").instantiate();
                ColorAttribute.of(sprite).set(0x0c1327ff);
                float edgeSize = 0.05f;
                StaticSprite corner = new StaticSprite()
                        .viewport(s.senderInviteImageView)
                        .metrics(new UIElement.Metrics().scale(+edgeSize, +edgeSize).anchor(-0.5f, +0.5f).pan(+0.5f, -0.5f))
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .attach();
                corner.instantiate()
                        .metrics(new UIElement.Metrics().scale(-edgeSize, +edgeSize).anchor(+0.5f, +0.5f).pan(+0.5f, -0.5f))
                        .attach();
            }
            s.senderInviteTextView = new TextBox()
                    .viewport(s.senderInviteGroup)
                    .metrics(new UIElement.Metrics().anchorBottom().anchorLeft().scale(380f / 597f).move(+0.059f, +0.042f))
                    .text(new Text()
                            .font(textFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(90f / 410f, messageWrapChars)
                            .centerLeft()
                    )
                    .attach();
            patch = PatchedSprite.create("system/circle.png", 80f / 150f, 35f / 210f);
            ColorAttribute.of(patch).set(0x75ee83ff);
            s.senderInviteAcceptButton = new Clickable()
                    .viewport(s.senderInviteGroup)
                    .metrics(new UIElement.Metrics().anchorBottom().anchorRight().scale(150f / 597f).move(-0.036f, +0.043f))
                    .visuals(patch, SaraRenderer.TARGET_INTERACTIVE)
                    .text(new Text()
                            .font(inviteButtonFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(patch.length, -5.5f)
                            .text("ACCEPT")
                    )
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .inputPadding(10f, 1.75f, 1f, 0.1f)
                    .attach();

            s.senderInviteYInterval = messageYInterval -0.06f;

        }


        {
            // User message
            s.userMessageBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorRight().anchor(-0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-player-first.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(0.2f, 0)
                    .centerLeft()
                    .font(userTextFont, messageWrapChars, 200f, SaraRenderer.TARGET_INTERACTIVE_TEXT);
            s.userNormalVisual = "apps/chats/bubble-player.png";

            s.userMessageTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(+0.5f, -0.5f).pan(-0.84f, +3.0f))
                    .text(new Text()
                            .font(userTimeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -7.0f)
                            .centerRight()
                    );

            s.userMessageYInterval = messageYInterval;

            s.userCustomFonts = new Font[] {
                    userBoldFont
            };
            s.userCustomFontNames = new String[] {
                    "demon"
            };
        }

        {
            // User photo view
            s.userPhotoBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorRight().anchor(-0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-player.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(0.9f, 0.9f);

            s.userPhotoView = new Clickable()
                    .viewport(s.userPhotoBox)
                    .metrics(new UIElement.Metrics().scale(0.88f).anchor(0, +0.035f))
                    .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .attach();

            s.userPhotoActionView = new PatchedTextBox()
                    .viewport(s.userPhotoBox)
                    .visual("apps/chats/action-bg.png", 0.06f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(45f / 111f).anchor(-0.72f, 0f))
                    .minSize(1f, 26f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("View")
                    .refresh()
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .enable()
                    .attach();

            s.userPhotoTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(+0.5f, -0.5f).pan(-0.52f, +2.3f))
                    .text(new Text()
                            .font(userTimeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -6f)
                    );

            s.userPhotoYInterval = messageYInterval;
        }


        {
            // User video view
            s.userVideoBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorRight().anchor(-0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-player.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(0.9f, 0.9f);


            s.userVideoView = new Clickable()
                    .viewport(s.userVideoBox)
                    .metrics(new UIElement.Metrics().scale(0.88f).anchor(0, +0.035f))
                    .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .attach();

            s.userVideoActionView = new PatchedTextBox()
                    .viewport(s.userVideoBox)
                    .visual("apps/chats/action-bg.png", 0.06f, SaraRenderer.TARGET_INTERACTIVE)
                    .metrics(new UIElement.Metrics().scale(45f / 111f).anchor(-0.72f, 0f))
                    .minSize(1f, 26f / 78f)
                    .font(actionFont, -8.1f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .text("Play")
                    .refresh()
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .enable()
                    .attach();

            // Movie indicator
            sprite = new Sprite(10f / 85f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0, 0, 0, 0.6f);
            StaticSprite movieBg = new StaticSprite()
                    .viewport(s.userVideoView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            new StaticSprite()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(14f / 150f).anchor(-58f / 150f, 0))
                    .visual(Sprite.load("apps/gallery/video-icon.png"), SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            s.userVideoDurationView = new TextBox()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(85f / 150f).anchor(+26f / 150f, 0))
                    .text(new Text()
                            .font(boldFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_SUB_TEXT)
                            .position(9f / 85f, 0)
                            .text("3:42")
                            .centerRight()
                    )
                    .attach();

            s.userVideoTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(+0.5f, -0.5f).pan(-0.52f, +2.3f))
                    .text(new Text()
                            .font(userTimeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -6f)
                    );

            s.userVideoYInterval = messageYInterval;
        }

        {
            // User audio view
            s.userAudioBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorRight().anchor(-0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-player.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(1.0f, 0.2f);


            // Mic icon
            sprite = Sprite.load("apps/gallery/mic-icon.png").instantiate();
            ColorAttribute.of(sprite).set(0x444353ff);
            StaticSprite micView = new StaticSprite()
                    .viewport(s.userAudioBox)
                    .metrics(new UIElement.Metrics().scale(0.16f).anchor(-0.66f, 0))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            sprite = sprite.instantiate();
            ColorAttribute.of(sprite).set(0xfafafaff);
            new StaticSprite()
                    .viewport(micView)
                    .metrics(new UIElement.Metrics().scale(1f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .name("levels")
                    .attach();



            sprite = Sprite.load("system/circle.png").instantiate();
            ColorAttribute.of(sprite).set(0x444353ff);
            s.userAudioPlayButton = new Clickable()
                    .viewport(s.userAudioBox)
                    .metrics(new UIElement.Metrics().scale(0.19f).move(-0.36f, 0))
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .inputPadding(10f, 0.35f, 10f, 0.35f)
                    .attach();

            new StaticSprite()
                    .viewport(s.userAudioPlayButton)
                    .metrics(new UIElement.Metrics().scale(0.5f))
                    .visual(s.audioPlaySprite, SaraRenderer.TARGET_INTERACTIVE)
                    .name("icon")
                    .attach();

            PatchedSprite progressBgMat = PatchedSprite.create("system/circle.png", 27f / 547f, (27f / 547f) / 2f);
            ColorAttribute.of(progressBgMat).set(0x8c8c8cff);

            PatchedSprite progressBarMat = progressBgMat.instantiate();
            ColorAttribute.of(progressBarMat).set(0x444353ff);

            float knobSize = 0.12f;
            new HorizontalProgressBar()
                    .viewport(s.userAudioPlayButton)
                    .metrics(new UIElement.Metrics().scale(3.5f).anchorRight().move(+0.22f, 0).pan(+1f, 0f))
                    .visual(progressBgMat, progressBarMat, SaraRenderer.TARGET_INTERACTIVE)
                    .knob(Sprite.load("system/circle.png"), knobSize, knobSize / 2f, knobSize / 2f)
                    .progress(0.8f)
                    .length(progressBgMat.length)
                    .name("progressbar")
                    .attach();


            s.userAudioTimeBox = new TextBox()
                    .metrics(new UIElement.Metrics().scale(0.186f).anchor(+0.5f, -0.5f).pan(-0.52f, +2.3f))
                    .text(new Text()
                            .font(userTimeFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(1f, -6f)
                    );

            s.userAudioYInterval = messageYInterval;
        }


        {
            // Corrupted visual
            s.corruptedSenderVisual = "apps/chats/bubble-npc-corrupted.png.NoiseMaterial";
            s.corruptedUserVisual = "apps/chats/bubble-player-corrupted.png.NoiseMaterial";
            PatchedSprite.load(s.corruptedSenderVisual);
            PatchedSprite.load(s.corruptedUserVisual);
            s.corruptedAnim = new ColorAnim(1f, 0x999999ff, false, 1);

            s.corruptedFixButton = new Clickable()
                    .visuals(Sprite.load("system/fix.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            new ScaleAnim(0.3f, new QuadraticGraph(0f, 1f, 1.2f, true)),
                            new SequenceAnim(new Animation[] {
                                    new NullAnim(0.5f),
                                    new FadeAnim(0.5f, new ConstantGraph(0.5f))
                            }),
                            new ScaleAnim(0.1f, new QuadraticGraph(1f, 1.2f, true)),
                            new ScaleAnim(0.2f, new LinearGraph(1.2f, 1f)),
                            null
                    )
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    ;
            s.corruptedFixButtonSenderMetrics = new UIElement.Metrics().scaleIndex(2).scale(84f / 540f).anchor(+0.5f, 0).pan(+0.7f, 0);
            s.corruptedFixButtonUserMetrics = new UIElement.Metrics().scaleIndex(2).scale(84f / 540f).anchor(-0.5f, 0).pan(-0.7f, 0);

            s.corruptedFixInputPaddingX = 10f;
            s.corruptedFixInputPaddingY = 0.1f;

            s.tCorruptedTextInterval = Globals.tCorruptedTextInterval;
        }

        {
            // Corruption
            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", 1f, 0.2f);
            ColorAttribute.of(patch).set(0x000000ff);

            s.corruptedImageView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(1.11f))
                    .visual(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
            ;

            // Icon
            new StaticSprite()
                    .viewport(s.corruptedImageView)
                    .metrics(new UIElement.Metrics().scale(0.35f))
                    .visual(Sprite.load("system/fix.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(
                            null,
                            new SequenceAnim(new Animation[] {
                                    new NullAnim(0.5f),
                                    new FadeAnim(0.5f, new ConstantGraph(0.5f))
                            }),
                            null
                    )
                    .attach();
        }

        {
            // Dates
            s.dateTextBox = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.67f).pan(0, -0.5f).anchor(0, -0.017f))
                    .visual("system/circle.png", 0.06f, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .padding(1.5f, 0.7f, 1.5f, 0.7f)
                    .minSize(0.2f, 0)
                    .animation(null, new ColorAnim(0x75ee83ff), null)
                    .font(dayFont, 19f, 100f, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT);

            s.dateYInterval = dateYInterval;
        }


        {
            // Sender typing box
            Animation moveAppearAnim = new MoveAnim(0.2f, new QuadraticGraph(-1f, 0, true), null);
            s.senderTypingView = new PatchedTextBox()
                    .viewport(s.surface)
                    .metrics(new UIElement.Metrics().scale(0.65f).anchorLeft().anchor(+0.02f, 0).pan(0, -0.5f))
                    .visual("apps/chats/bubble-npc.png", messageCornerSize, SaraRenderer.TARGET_INTERACTIVE)
                    .padding(leftPadding, topPadding, rightPadding, bottomPadding)
                    .minSize(0.2f, 0f)
                    .centerLeft()
                    .font(textFont, messageWrapChars, 200f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(moveAppearAnim, null, null)
                    .refresh();

            sprite = Sprite.load("apps/chats/circle.png").instantiate();
            ColorAttribute.of(sprite).set(0x75ee83ff);

            new StaticSprite()
                    .viewport(s.senderTypingView)
                    .metrics(new UIElement.Metrics().scale(0.12f).anchor(-0.17f, 0f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(
                            moveAppearAnim,
                            new ScaleAnim(1f, new SineGraph(1f, 1f, 0f, 0.1f, 1f)),
                            null
                    )
                    .attach();
            new StaticSprite()
                    .viewport(s.senderTypingView)
                    .metrics(new UIElement.Metrics().scale(0.12f).anchor(0, 0f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(
                            moveAppearAnim,
                            new ScaleAnim(1f, new SineGraph(1f, 1f, 0.2f, 0.1f, 1f)),
                            null
                    )
                    .attach();
            new StaticSprite()
                    .viewport(s.senderTypingView)
                    .metrics(new UIElement.Metrics().scale(0.12f).anchor(+0.17f, 0f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .animation(
                            moveAppearAnim,
                            new ScaleAnim(1f, new SineGraph(1f, 1f, 0.4f, 0.1f, 1f)),
                            null
                    )
                    .attach();
        }

        {
            // New messages row
            s.newMessagesRow = new UIElement.Group()
                    .metrics(new UIElement.Metrics().pan(0, -1.1f))
                    .length(118f / 2250f)
                    ;

            // line
            sprite = new Sprite(0.008f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xa8a8a8ff);
            new StaticSprite()
                    .viewport(s.newMessagesRow)
                    .metrics(new UIElement.Metrics().scale(800f / 2550f).anchorLeft().move(+0.03f, 0))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();
            new StaticSprite()
                    .viewport(s.newMessagesRow)
                    .metrics(new UIElement.Metrics().scale(800f / 2550f).anchorRight().move(-0.03f, 0))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();

            new TextBox()
                    .viewport(s.newMessagesRow)
                    .metrics(new UIElement.Metrics().scale(685f / 2550f))
                    .text(new Text()
                            .font(newMessageFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(138f / 685f, 0)
                            .text("New messages")
                    )
                    .attach();

            s.newMessageYInterval = -0.09f;
            s.newMessageCenterYOffset = +0.30f;
        }

        if(Globals.checkAllAssets) {
            PatchedSprite.load(s.senderFirstVisual);
            PatchedSprite.load(s.corruptedSenderVisual);
            PatchedSprite.load(s.corruptedUserVisual);
            PatchedSprite.load(s.userNormalVisual);
        }

        thread.setInternal(s);
    }
}
