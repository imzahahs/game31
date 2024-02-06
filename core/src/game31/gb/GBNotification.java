package game31.gb;

import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.Notification;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.graphics2d.CircularSprite;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.AnimatedMaterial;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.Toast;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 29/8/2016.
 */
public class GBNotification {

    public GBNotification(Notification notification) {
        Notification.Internal s = new Notification.Internal();

        Font titleFont = new Font("opensans-bold.ttf", 32, 0xffffffff);

        Font detailFont = new Font("opensans-regular.ttf", 32, 0xffffffff);

        Font notificationFont = new Font("opensans-bold.ttf", 32, 0xffffffff);

        Font statusFont = new Font("opensans-light.ttf", 32);
        Font descriptionFont = new Font("opensans-regular.ttf", 32);
        Font irisTitleFont = new Font("opensans-semibold.ttf", 48, 0x1ce8deff);

        Font exitButtonFont = new Font("opensans-bold.ttf", 48, 0xc7b8f8ff);
        Font exitDialogFont = new Font("opensans-regular.ttf", 40, 0x000000ff);

        Font saveTextFont = new Font("opensans-bold.ttf", 40, 0xffffffff);

        Font subtitleFont = new Font("opensans-bold.ttf", 32, new Color(0.0f, 0.0f, 0.0f, 1.0f), 6.0f, Color.CLEAR, 0, 0, Color.WHITE, -3, 0);
        Font subtitleMinorFont = new Font("opensans-italic.ttf", 32);
        subtitleMinorFont.color("SUBTITLE_MINOR", 0xaaaaaaff);


        Sprite sprite;
        PatchedSprite patch;

        // Window
        {
            patch = PatchedSprite.create("system/square-shadowed.png", 427f / 2250f, 0f, 0f, 0f, 0.04f);        // 0.02f
            ColorAttribute.of(patch).set(0x020a22ff);

            s.window = new UIElement.Group();

            // Surface
            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .scrollable(true, false)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .length(patch.length)
                    .scrollGravity(0.3f, 0f, 1f, 0)
                    .selectiveRendering(true, false)
                    .minTouchMoveDistance(Globals.minTouchMoveDistance)
                    .attach();

            s.quickView = new Clickable()
                    .viewport(s.surface)
                    .metrics(new UIElement.Metrics().move(+1f, 0))
                    //.metrics(new UIElement.Metrics().scale(580f / 632f).anchorTop().anchor(0f, -0.05f))
                    .visuals(patch, SaraRenderer.TARGET_OVERLAY)
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .attach();

            // Yellow bar
            sprite = new Sprite(373f / 63f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x7341f8ff);
            new StaticSprite()
                    .viewport(s.quickView)
                    .metrics(new UIElement.Metrics().anchorLeft().anchorTop().scale(63f / 2250f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    .attach();

            // Notification group
            s.notificationGroup = new UIElement.Group()
                    .viewport(s.quickView)
                    .length(s.quickView.getLength())
                    .animation(
                            new ScissorAnim(1f, new Animation[] {
                                    new ScaleAnim(1f, ScaleAnim.Location.LEFT, QuadraticGraph.zeroToOneInverted, ConstantGraph.one)
                            }),
                            null,
                            null
                    )
                    ;

            s.imageView = new StaticSprite()
                    .viewport(s.notificationGroup)
                    .metrics(new UIElement.Metrics().scale(0.11f).anchor(-0.35f, +0.06f))
                    .target(SaraRenderer.TARGET_OVERLAY)
                    // .visual(Sprite.load("content/profiles/girl3.png"))
                    .attach();

            s.iconView = new StaticSprite()
                    .viewport(s.imageView)
                    .metrics(new UIElement.Metrics().scale(0.35f).anchor(+0.4f, -0.4f))
                    .target(SaraRenderer.TARGET_OVERLAY)
                    //.visual(Sprite.load("apps/messages.png"))
                    .attach();

            s.titleView = new TextBox()
                    .viewport(s.notificationGroup)
                    .metrics(new UIElement.Metrics().scale(0.7f).anchor(+0.10f, +0.22f))
                    .text(new Text()
                            .font(titleFont, SaraRenderer.TARGET_OVERLAY_TEXT)
                            .position(0.05f, 0)
                            .centerLeft()
                            //.text("Debby Whitmana")
                    )
                    .attach();

            s.detailView = new TextBox()
                    .viewport(s.notificationGroup)
                    .metrics(new UIElement.Metrics().scale(0.7f).anchor(+0.10f, -0.10f))
                    .text(new Text()
                            .font(detailFont, SaraRenderer.TARGET_OVERLAY_TEXT)
                            .position(0.06f, 17f)
                            .centerLeft()
                            .ellipsize(1)
                            //.text("You have 34 messages from 4 contact")
                    )
                    .attach();

            // Notification group
            s.accessView = new UIElement.Group()
                    .viewport(s.quickView)
                    .length(s.quickView.getLength())
                    .animation(
                            new ScissorAnim(0.5f, new Animation[] {
                                    new ScaleAnim(1f, ScaleAnim.Location.LEFT, QuadraticGraph.zeroToOneInverted, ConstantGraph.one)
                            }),
                            null,
                            null
                    )
                    ;

            Animation appButtonPressedAnim = new ScaleAnim(0.15f, new QuadraticGraph(1f, 1.1f, -0.5f, false));
            Animation appButtonReleasedAnim = new ScaleAnim(0.1f, new QuadraticGraph(1.1f, 1.0f, false));

            Clickable appButton = new Clickable()
                .visuals(Sprite.load("apps/chats/icon.png"))
                .target(SaraRenderer.TARGET_OVERLAY_INTERACTIVE).length(1f)
                .animation(null, null, appButtonPressedAnim, appButtonReleasedAnim, null)
                .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                .inputPadding(0.24f, 0.2f, 0.24f, 0.2f)
                .passThroughInput(true);

            // Notification view
            sprite = Sprite.load("system/notification.png");
            s.appNoteView = new StaticSprite()
                    .metrics(new UIElement.Metrics().anchorTop().anchorRight().scale(0.5f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_TEXT)
                    .animation(new ScaleAnim(0.15f,
                                    ScaleAnim.Location.TOPRIGHT,
                                    new CompoundGraph(new Graph[]{
                                            new QuadraticGraph(0f, 1.3f, 0.7f, 0f, true),
                                            new QuadraticGraph(1.3f, 1f, 0.3f, 0f, false)
                                    })
                            ),
                            new ScaleAnim(1f,
                                    ScaleAnim.Location.TOPRIGHT,
                                    new CompoundGraph(new Graph[]{
                                            new LinearGraph(1f, 1.2f, 0.1f),
                                            new LinearGraph(1.2f, 1f, 0.1f),
                                            new LinearGraph(1f, 1.2f, 0.1f),
                                            new LinearGraph(1.2f, 1f, 0.1f),
                                            new ConstantGraph(1f, 0.6f)
                                    })),
                            new ScaleAnim(0.15f,
                                    ScaleAnim.Location.TOPRIGHT,
                                    new LinearGraph(1f, 0f)
                            )
                    );

            s.appNoteTextView = new TextBox()
                    .viewport(s.appNoteView)
                    .metrics(new UIElement.Metrics().move(+0.03f, +0.05f).scale(0.6f))
                    .text(new Text()
                            .font(notificationFont, SaraRenderer.TARGET_OVERLAY_TEXT)
                            .position(0.7f, 0)
                            .text("99")
                    )
                    .attach();

            s.appNoteView.instantiate().viewport(appButton).attach();

            float x = -0.34f;
            float xInterval = +0.18f;
            float size = 0.12f;
            float y = +0.006f;

            Clickable appButton1 = appButton.instantiate().viewport(s.accessView)
                    .metrics(new UIElement.Metrics().scale(size).move(x, y))
                    .attach();
            x += xInterval;
            Clickable appButton2 = appButton.instantiate().viewport(s.accessView)
                    .metrics(new UIElement.Metrics().scale(size).move(x, y))
                    .attach();
            x += xInterval;
            Clickable appButton3 = appButton.instantiate().viewport(s.accessView)
                    .metrics(new UIElement.Metrics().scale(size).move(x, y))
                    .attach();
            x += xInterval;
            Clickable appButton4 = appButton.instantiate().viewport(s.accessView)
                    .metrics(new UIElement.Metrics().scale(size).move(x, y))
                    .attach();
            x += xInterval;

            s.appButtons = new Clickable[] {
                    appButton1,
                    appButton2,
                    appButton3,
                    appButton4,
            };

            // Close button
            sprite = Sprite.load("system/close.png").instantiate();
            ColorAttribute.of(sprite).alpha(0.1f);
            s.accessCloseButton = new Clickable()
                    .viewport(s.accessView)
                    .metrics(new UIElement.Metrics().scale(size * 0.6f).move(x + 0.01f, y))
                    .visuals(sprite, SaraRenderer.TARGET_OVERLAY)
                    .animation(
                            null,
                            null,
                            new ColorAnim(1f, 1f, 1f, 0.3f, false),
                            new ColorAnim(0.2f, null, null, null, new QuadraticGraph(0.3f, 0.1f, false), false),
                            null
                    )
                    .inputPadding(1f, 1.3f, 1f, 1.3f)
                    .attach();

            sprite = Sprite.load("system/notification-hint.png").instantiate();
            ColorAttribute.of(sprite).set(0xffffffff).alpha(0.5f); //.set(0x625f9cff);
            Animation arrowAnim = new CompoundAnim(0.8f, new Animation[] {
                    new FadeAnim(1f, QuadraticGraph.oneToZero),
                    new MoveAnim(1f, new LinearGraph(+1.4f, -0.0f), null),
            });
            s.hintView = new Toast()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorTop().scale(0.55f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_BG)
                    .animation(
                            null,
                            null,
                            new SequenceAnim(new Animation[] {
                                    arrowAnim,
                                    arrowAnim,
                                    arrowAnim,
                            })
                    )
                    .time(0f)
                    ;



            s.hintMoveX = -0.2f;
            s.tHintMoveTime = 0.1f;

            s.tDismissTime = 2f;
        }


        {

            // Tracker cover
            Sprite bgSprite = Sprite.load("apps/iris/bg.png").instantiate();
            if (bgSprite.length != Globals.LENGTH) {              // Crop according to length
                bgSprite = new Sprite(bgSprite.length, bgSprite.getMaterial());
                bgSprite.crop(Globals.LENGTH);
            }
            sprite = bgSprite.instantiate();
            s.coverColorAttribute = ColorAttribute.of(sprite).set(0xaaaaaaff).alpha(1f);
            s.coverView = new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD_TEXT)      // underneath overlay
                    ;
            s.coverAlphaGraph = new QuadraticGraph(0f, 1f, false);
            s.coverStartX = +1.5f;
            s.coverEndX = 0.5f;
            s.coverClearRenderX = 0.5f;

            s.trackerEnterAnim = new MoveAnim(0.5f, new QuadraticGraph(+1f, 0f, true), null);


            // Bg
            s.trackerGroup = new StaticSprite()
                    .viewport(s.surface)
                    .metrics(new UIElement.Metrics().anchorTop().move(+1f, 0))
                    .visual(bgSprite, SaraRenderer.TARGET_OVERLAY_BG)
                    ; // .attach();

            UIElement.Group window = new UIElement.Group()
                    .viewport(s.trackerGroup)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .length(Globals.LENGTH - (373f / 2250f))
                    .enable()
                    .passThroughInput(false)
                    .attach();

            // Title
            sprite = Sprite.load("system/memo-iris.png").instantiate();
            ColorAttribute.of(sprite).set(0x1ce8deff);
            new StaticSprite()
                    .viewport(window)
                    .metrics(new UIElement.Metrics().scale(230f / 2251f).anchorTop().move(-0.16f, -0.04f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    .attach();
            new TextBox()
                    .viewport(window)
                    .metrics(new UIElement.Metrics().scale(697f / 2251f).anchorTop().move(+0.07f, -0.07f))
                    .text(new Text()
                            .font(irisTitleFont, SaraRenderer.TARGET_OVERLAY_TEXT)
                            .position(107f / 697f, 0)
                            .text("Iris Memos")
                    )
                    .attach();

            s.trackerSurface = new ScrollableSurface()
                    .viewport(window)
                    .metrics(new UIElement.Metrics().anchorBottom().move(0, +0.16f))
                    .scrollable(false, true)
                    .selectiveRendering(true, true)
                    .length(Globals.LENGTH - (1200f / 2250f))       // 800f / 2250f
                    .padding(0, 0, 0, 0.05f)
                    .passThroughInput(true)
                    .attach();

            sprite = new Sprite(0.16f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x6a61b7ff).alpha(1f);

            s.exitButton = new Clickable()
                    .viewport(window)
                    .metrics(new UIElement.Metrics().scale(1f).anchorBottom())
                    .visuals(sprite, SaraRenderer.TARGET_OVERLAY)
                    .text(new Text()
                            .font(exitButtonFont, SaraRenderer.TARGET_OVERLAY_TEXT)
                            .position(0.16f, -22f)
                            .text("EXIT TO MAIN MENU")
                    )
                    .sound(Sound.load("sounds/general_invalid.ogg"))
                    .attach();

            sprite = new Sprite(517f / 2250f, Material.load("system/gradient.png"));
            ColorAttribute.of(sprite).set(0x000000ff).alpha(0.4f);
            new StaticSprite()
                    .viewport(s.exitButton)
                    .metrics(new UIElement.Metrics().anchorTop().scale(1, -1))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    .attach();

            // Exit dialog
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff).alpha(0.5f);
            s.exitDialogGroup = new StaticSprite()
                    .viewport(s.trackerGroup)
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_TEXT)
                    .animation(new FadeAnim(0.5f, LinearGraph.zeroToOne), null, null)
                    .passThroughInput(false)
                    ;

            patch = PatchedSprite.create("system/rounded.png", 1700f / 2017f, 0.03f);
            ColorAttribute.of(patch).set(0xf2f2f2ff);
            StaticSprite dialogView = new StaticSprite()
                    .viewport(s.exitDialogGroup)
                    .metrics(new UIElement.Metrics().scale(2017f / 2250f).move(0, -0.1f))
                    .visual(patch, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .animation(new ScaleAnim(0.15f, new SineGraph(1f, 0.5f, 0f, 0.1f, 1f)), null, null)
                    .attach();
            patch = PatchedSprite.create("system/quit-header.png", 400f / 2017f, 0.02f);
            StaticSprite dialogHeader = new StaticSprite()
                    .viewport(dialogView)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(patch, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();
            new StaticSprite()
                    .viewport(dialogHeader)
                    .metrics(new UIElement.Metrics().scale(210f / 2017f))
                    .visual(Sprite.load("system/quit-icon.png"), SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();

            // Text
            new TextBox()
                    .viewport(dialogView)
                    .metrics(new UIElement.Metrics().move(0, -0.02f))
                    .text(new Text()
                            .font(exitDialogFont, SaraRenderer.TARGET_OVERLAY_DIALOG)
                            .position(0.9f, 1156f / 2017f, 18f)
                            .text("Are you sure you want to return to main menu?\n" +
                                    "\n" +
                                    "You will lose any progress you have made since the last checkpoint.")
                    )
                    .attach();

            float bottomButtonHeight = 0.3f;

            Animation dialogButtonPressAnim = new FadeAnim(0.5f, 1);
            s.exitDialogNoButton = new Clickable()
                    .viewport(dialogView)
                    .metrics(new UIElement.Metrics().anchorLeft().anchorBottom().scale(0.5f))
                    .length(bottomButtonHeight)
                    .animation(null, null, dialogButtonPressAnim, null, null)
                    .text(new Text()
                            .font(exitDialogFont, SaraRenderer.TARGET_OVERLAY_DIALOG)
                            .position(79f / 522f, -10f)
                            .text("No")
                    )
                    .attach();

            // Line
            sprite = new Sprite(bottomButtonHeight / 0.01f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xbec2ccff);
            new StaticSprite()
                    .viewport(s.exitDialogNoButton)
                    .metrics(new UIElement.Metrics().anchorRight().scale(0.01f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();
            sprite = new Sprite(0.004f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xbec2ccff);
            new StaticSprite()
                    .viewport(s.exitDialogNoButton)
                    .metrics(new UIElement.Metrics().anchorTop().anchorLeft().scale(1f / 0.5f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();

            s.exitDialogYesButton = new Clickable()
                    .viewport(dialogView)
                    .metrics(new UIElement.Metrics().anchorRight().anchorBottom().scale(0.5f))
                    .length(bottomButtonHeight)
                    .animation(null, null, dialogButtonPressAnim, null, null)
                    .text(new Text()
                            .font(exitDialogFont, SaraRenderer.TARGET_OVERLAY_DIALOG)
                            .position(79f / 522f, -10f)
                            .text("Yes")
                    )
                    .attach();


            // Quest tracker Icons
            float x = -520f / 2251f;
            float scale = 105f / 2251f;

            sprite = Sprite.load("system/memo-pending.png").instantiate();
            ColorAttribute.of(sprite).set(0x1ce8deff);
            s.pendingIconView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(scale * 0.4f).move(x, 0).pan(0, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    ;

            sprite = Sprite.load("system/memo-note.png").instantiate();
            ColorAttribute.of(sprite).set(0xf1f145ff);
            s.noteIconView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(scale).move(x, +0.005f).pan(0, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    ;

            sprite = Sprite.load("system/memo-closed.png").instantiate();
            ColorAttribute.of(sprite).set(0xff4848ff);
            s.failedIconView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(scale).move(x, +0.005f).pan(0, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    ;

            sprite = Sprite.load("system/memo-success.png").instantiate();
            ColorAttribute.of(sprite).set(0x0fd32aff);
            s.successIconView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(scale).move(x, 0).pan(0, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    ;

            TextBox statusView = new TextBox()
                    .metrics(new UIElement.Metrics().scale(434f / 2251f).anchorLeft().move(+70f / 2251f, 0).pan(0, -0.5f))
                    .text(new Text()
                            .font(statusFont, SaraRenderer.TARGET_OVERLAY_TEXT)
                            .position(86f / 434f, 5f)
                            .centerRight()
                    )
                    ;

            TextBox descriptionView = new TextBox()
                    .metrics(new UIElement.Metrics().scale(1426f / 2251f).anchorRight().move(-125f / 2251f, 0).pan(0, -0.5f))
                    .text(new Text()
                            .font(descriptionFont, SaraRenderer.TARGET_OVERLAY_TEXT)
                            .position(86f / 1426f, 16f)
                            .centerLeft()
                    )
                    ;

            s.pendingStatusTextView = statusView.instantiate()
                    .animation(null, new ColorAnim(0x1ce8deff), null);
            s.pendingDescriptionView = descriptionView.instantiate()
                    .animation(null, new ColorAnim(0xf4f9fcff), null);
            s.failedStatusTextView = statusView.instantiate()
                    .animation(null, new ColorAnim(0xff4848ff), null);
            s.failedDescriptionView = descriptionView.instantiate()
                    .animation(null, new ColorAnim(244f / 255f, 249f / 255f, 252f / 255f, 0.6f), null);
            s.noteStatusTextView = statusView.instantiate()
                    .animation(null, new ColorAnim(0xf1f145ff), null);
            s.noteDescriptionView = descriptionView.instantiate()
                    .animation(null, new ColorAnim(244f / 255f, 249f / 255f, 252f / 255f, 0.6f), null);
            s.successStatusTextView = statusView.instantiate()
                    .animation(null, new ColorAnim(0x0fd32aff), null);
            s.successDescriptionView = descriptionView.instantiate()
                    .animation(null, new ColorAnim(244f / 255f, 249f / 255f, 252f / 255f, 0.6f), null);

            s.defaultPendingText = "Pending";
            s.defaultFailedText = "Failed";
            s.defaultNoteText = "Memo";
            s.defaultCompletedText = "Completed";

            sprite = new Sprite(1f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x7d99bbff);
            s.lineView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(9f / 2251f).anchorLeft().move(+594f / 2251f, -0.06f).pan(0, -0.5f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    ;


            s.trackerStartY = (s.trackerSurface.getLength() / 2f) - s.trackerSurface.paddingTop();
            s.trackerPendingY = 0.05f;
            s.trackerPendingPaddingY = 0.07f;
            s.trackerTimelineY = 0.12f;

            s.lineMinHeight = -0.02f;

            s.slideShowSound = Sound.load("sounds/iris_shown.ogg");
            s.slideHideSound = Sound.load("sounds/iris_hide.ogg");
            s.trackerOpenSound = Sound.load("sounds/general_forward.ogg");
        }


        {
            // Save indicator
            float height = 371f;
            float width = 544f;
            sprite = new Sprite(height / width, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x7341f8ff);
            s.saveView = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(width / 2250f).anchorTop().anchorRight())
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .animation(
                            new SequenceAnim(new Animation[] {
                                    new MoveAnim(0.25f, new QuadraticGraph(+1.4f, +0.75f, true), null),
                                    new MoveAnim(0.9f, new ConstantGraph(+0.75f), null),
                                    new MoveAnim(0.3f, new QuadraticGraph(+0.75f, 0f, true), null)
                            }),
                            null,
                            new MoveAnim(0.25f, new QuadraticGraph(0f, +1.4f, false), null)
                    )
                    ;

            sprite = Sprite.load("system/circle-medium.png").instantiate();
            ColorAttribute.of(sprite).set(0x7341f8ff);
            new StaticSprite()
                    .viewport(s.saveView)
                    .metrics(new UIElement.Metrics().anchor(-0.5f, 0).scale(height / width))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();

            // Animation
            AnimatedMaterial material = Material.load("system/save-anim.png.AnimatedMaterial");
            s.saveIconAnim = new AnimatedMaterial.Instance(material);
            sprite = new Sprite(s.saveIconAnim);
            ColorAttribute.of(sprite).set(0xffffffff);
            new StaticSprite()
                    .viewport(s.saveView)
                    .metrics(new UIElement.Metrics().scale(220f / width).move(-0.51f, 0f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();
            s.tSaveIconStartTime = +0.2f;
            s.tSaveIconAnimTime = 0.7f;

            new TextBox()
                    .viewport(s.saveView)
                    .metrics(new UIElement.Metrics().scale(360f / width).move(+0.10f, 0))
                    .text(new Text()
                            .font(saveTextFont, SaraRenderer.TARGET_OVERLAY_DIALOG)
                            .position(79f / 336f, 0)
                            .text("SAVED")
                    )
                    .attach();

            s.tSaveMinInterval = 60f;        // do not show save indicator within 1 minute
            s.tSaveTotalTime = 3f;

            // Downloading indicator
            width = 1100f;
            sprite = new Sprite(height / width, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x7341f8ff);
            s.downloadView = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(width / 2250f).anchorTop().anchorRight())
                    .visuals(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .text(new Text()
                            .font(saveTextFont, SaraRenderer.TARGET_OVERLAY_DIALOG)
                            .position(+0.07f, 0, 0.8f, sprite.length, 8f)
                            .centerLeft()
                            .text("RECEIVING")
                    )
                    .inputPadding(0.2f, 0, 0, 0)
                    .animation(
                            null,
                            null,
                            new MoveAnim(0.05f, new QuadraticGraph(0f, -0.10f, true), null),
                            new MoveAnim(0.05f, new QuadraticGraph(-0.10f, 0f, false), null),
                            new MoveAnim(0.25f, new QuadraticGraph(0f, +1.5f, false), null)
                    )
                    ;

            s.downloadInProgressText = "RECEIVING";
            s.downloadOpenText = "OPEN?";

            sprite = Sprite.load("system/circle-medium.png").instantiate();
            ColorAttribute.of(sprite).set(0x7341f8ff);
            StaticSprite circle = new StaticSprite()
                    .viewport(s.downloadView)
                    .metrics(new UIElement.Metrics().anchor(-0.5f, 0).scale(height / width))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();
            sprite = Sprite.load("system/down-arrow.png").instantiate();
            ColorAttribute.of(sprite).set(0xffffffff);
            s.downloadProgressView = new StaticSprite()
                    .viewport(circle)
                    .metrics(new UIElement.Metrics().scale(0.25f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .animation(
                            null,
                            new SequenceAnim(new Animation[]{
                                    new CompoundAnim(1f, new Animation[]{
                                            new MoveAnim(1f, null, new QuadraticGraph(sprite.length, 0f, true)),
                                            new ScissorAnim(1f, new Animation[]{
                                                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, ConstantGraph.one, new QuadraticGraph(0f, 1f, true))
                                            })
                                    }),
                                    new FadeAnim(0.25f, LinearGraph.oneToZero),
                                    new ScaleAnim(0.25f)
                            }),
                            null
                    )
                    ; // .attach();
            sprite = Sprite.load("system/memo-success.png").instantiate();
            ColorAttribute.of(sprite).set(0xffffffff);
            s.downloadFinishedView = new StaticSprite()
                    .viewport(circle)
                    .metrics(new UIElement.Metrics().scale(0.35f).move(-0.01f, -0.01f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    ; // .attach();

            sprite = Sprite.load("system/circle-hollow.png").instantiate();
            ColorAttribute.of(sprite).set(0xffffff33);
            new StaticSprite()
                    .viewport(circle)
                    .metrics(new UIElement.Metrics().scale(0.60f))
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();
            s.downloadProgressMat = new CircularSprite("system/circle-hollow.png");
            ColorAttribute.of(s.downloadProgressMat).set(0xffffffff);
            new StaticSprite()
                    .viewport(circle)
                    .metrics(new UIElement.Metrics().scale(0.60f))
                    .visual(s.downloadProgressMat, SaraRenderer.TARGET_OVERLAY_DIALOG)
                    .attach();


            s.downloadStartAnim = new SequenceAnim(new Animation[] {
                    new MoveAnim(0.25f, new QuadraticGraph(+1.5f, +0.25f, true), null),
                    new MoveAnim(0.9f, new ConstantGraph(0.25f), null),
                    new MoveAnim(0.3f, new QuadraticGraph(0.25f, +0.86f, true), null)
            });
            s.downloadFinishedAnim = new MoveAnim(0.3f, new QuadraticGraph(+0.86f, +0.45f, true), null);




            // Subtitles
            s.subtitlePortraitMetrics = new UIElement.Metrics();
            s.subtitleLandscapeMetrics = new UIElement.Metrics().rotate(-90).scale((1f / Globals.LENGTH));
            s.subtitleLandscapeNormalMetrics = new UIElement.Metrics().rotate(-90).move(-((Globals.LENGTH - 1f) / 2f) + 0.1f, 0);
            s.subtitleContainerView = new UIElement.Group()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .attach();
            s.subtitleView = new PatchedTextBox()
                    .viewport(s.subtitleContainerView)
                    .visual("system/rounded.png", 0.03f, SaraRenderer.TARGET_OVERLAY_BG)
                    .metrics(new UIElement.Metrics().anchorBottom().scale(0.9f).move(0.0f, 0.17f))        //
                    .font(subtitleFont, 24.0f, 1.0f, SaraRenderer.TARGET_OVERLAY_BG)
                    .padding(1.0f, 1.0f, 1.0f, 1.0f)
                    .animation(
                            null,
                            new ColorAnim(0.0f, 0.0f, 0.0f, 0.75f),
                            new CompoundAnim(1.0f, new Animation[] {
                                    new FadeAnim(1.0f, LinearGraph.oneToZero, 0),
                                    new FadeAnim(1.0f, LinearGraph.oneToZero, 1)
                            })
                    );


            s.tSubtitleDurationPerWord = 0.7f;
        }



        // Commit
        notification.setInternal(s);
    }

}
