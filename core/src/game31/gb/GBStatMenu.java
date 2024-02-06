package game31.gb;

import game31.Globals;
import game31.StatMenu;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 9/25/2017.
 */

public class GBStatMenu {


    public GBStatMenu(StatMenu menu) {
        StatMenu.Internal s = new StatMenu.Internal();

        Font endTitleFont = new Font("arcamajora-heavy.otf", 40, 0x000000ff);

        Font noticeFont = new Font("arcamajora-heavy.otf", 40, 0xffffffff);

        Font statGreenFont = new Font("arcamajora-heavy.otf", 40);
        statGreenFont.color("STAT_GREEN", 0x5ff400ff);
        Font statFont = new Font("arcamajora-heavy.otf", 40, 0xf2f2f2ff);

        Sprite sprite;
        PatchedSprite patch;

        {
            s.window = new UIElement.Group();

            UIElement.Group endingGroup = new UIElement.Group()
                    .viewport(s.window)
                    .animation(
//                            new SequenceAnim(new Animation[] {
//                                    new ScaleAnim(1f),
//                                    new NullAnim(0.15f),
//                                    new ScaleAnim(0.15f),
//                                    new NullAnim(0.15f),
//                                    new ScaleAnim(0.15f),
//                            }),
                            new SequenceAnim(
                                new ScissorAnim(1f, new Animation[] {
                                        new MoveAnim(1f, null, new QuadraticGraph(-Globals.LENGTH, 0f, true))
                                }),
                                1.5f,
                                true
                            ),
                            null,
                            null
                    )
                    .attach();

            s.bgView = new StaticSprite()
                    .viewport(s.window)
                    .target(SaraRenderer.TARGET_BG)
                    .animation(
                            new ColorAnim(1f, LinearGraph.zeroToOne, null),
                            null,
                            null
                    )
                    .attach();

            s.bgs = new Sprite[] {
                    Sprite.load("apps/stat/bg-teddy.png"),
                    Sprite.load("apps/stat/bg-teddy.png"),          // Can't find any other option
                    Sprite.load("apps/stat/bg-teddy.png"),
                    Sprite.load("apps/stat/bg-teddy.png"),
            };

            // Ending title
            patch = PatchedSprite.create("system/rounded.png", 318f / 2120f, 0.01f);
            ColorAttribute.of(patch).set(0xf4f4f4ff);
            StaticSprite endTitleBg = new StaticSprite()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(0.9f).anchorTop().move(0, -0.1f))
                    .visual(patch, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            s.endTitleView = new TextBox()
                    .viewport(endTitleBg)
                    .metrics(new UIElement.Metrics().scale(0.97f))
                    .text(new Text()
                            .font(endTitleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(endTitleBg.getLength() - 0.04f, 17f)
                            .text("Save Anna by sacrificing Taylor")
                    )
                    .attach();
            s.endTitles = new String[] {
                    "You and Teddy are both erased from reality",
                    "You are erased from reality while Teddy is spared",
                    "Teddy is erased from reality while you are spared",
                    "Both you and Teddy beat FlapeeBird at its own game"
            };

            // Notice view
            s.endNoticeView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.8f).move(0, -0.1f))
                    .text(new Text()
                            .font(noticeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(Globals.LENGTH, 15f)
                            .text("Save Anna by sacrificing Taylor")
                    )
                    .animation(
                            new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 2f, true),
                            null,
                            new FadeAnim(1f, LinearGraph.oneToZero)
                    )
                    .attach();
            s.endNotice = new String[] {
                    "You managed to save Teddy from despair. However, taking FlapeeBird's subscription deal erases you from this reality, while Teddy is saved from the same fate.\n\n" +
                            "Is that what you intended?\n\n" +
                            "SIMULACRA has many easter eggs and multiple endings. Play again and try for a different outcome.",
                    "You managed to save Teddy from despair and beat FlapeeBird without the subscription deal. You and Teddy are completely released from its hold.\n\n" +
                            "SIMULACRA has many easter eggs and multiple endings. Play again and try for a different outcome. ",
                    "You managed to save Teddy from despair but you gave up on your fated showdown. You helplessly watch as Teddy gets erased from this reality.\n\n" +
                            "SIMULACRA has many easter eggs and multiple endings. Play again and try to find them all.",
                    "You allowed Teddy to be consumed by despair. You also took FlapeeBird's subscription deal, which erases you from this reality along with Teddy.\n\n" +
                            "Is that what you intended?\n\n" +
                            "SIMULACRA has many easter eggs and multiple endings. Play again and try for a different outcome.",
                    "You allowed Teddy to be consumed by despair and beat FlapeeBird without the subscription deal. You helplessly watch as Teddy gets erased from this reality.\n\n" +
                            "Is that what you intended?\n\n" +
                            "SIMULACRA has many easter eggs and multiple endings. Play again and try for a different outcome.",
                    "You allowed Teddy to be consumed by despair and gave up on your fated showdown. You helplessly watch as Teddy gets erased from this reality.\n\n" +
                            "Is that what you intended?\n\n" +
                            "SIMULACRA has many easter eggs and multiple endings. Play again and try for a different outcome.",
                    "You gave all your life hours to FlapeeBird, which completely erases you from reality. Without your help, Teddy eventually gets consumed by despair and shares your fate.\n\n" +
                            "Is that what you intended?\n\n" +
                            "SIMULACRA has many easter eggs and multiple endings. Play again and try for a different outcome.",
            };

            s.opinionView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.8f).move(0, -0.1f))
                    .text(new Text()
                            .font(noticeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(Globals.LENGTH, 15f)
                            .text("Thank you for playing.\n\nDid you enjoy the experience?")
                    )
                    .animation(
                            new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 1f, true),
                            null,
                            new FadeAnim(1f, LinearGraph.oneToZero)
                    )
                    .attach();
            s.opinionYesButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().anchorLeft().scale(0.5f))
                    .length(421f / (2250f / 2f))
                    .text(new Text()
                            .font(statFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(421f / 2250f, -9f)
                            .text("YES")
                    )
                    .animation(new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 1f, true), null, new FadeAnim(0.7f, 1), null, new FadeAnim(1f, LinearGraph.oneToZero, 1))
                    .attach();
            s.opinionNoButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().anchorRight().scale(0.5f))
                    .length(421f / (2250f / 2f))
                    .text(new Text()
                            .font(statFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(421f / 2250f, -9f)
                            .text("NO")
                    )
                    .animation(new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 1f, true), null, new FadeAnim(0.7f, 1), null, new FadeAnim(1f, LinearGraph.oneToZero, 1))
                    .attach();

            s.reviewView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.8f).move(0, -0.1f))
                    .text(new Text()
                            .font(noticeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(Globals.LENGTH, 15f)
                            .text("Great!\n\nPlease gave us a rating and leave a review to show your support.")
                    )
                    .animation(
                            new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 1f, true),
                            null,
                            new FadeAnim(1f, LinearGraph.oneToZero)
                    )
                    .attach();
            s.reviewYesButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().anchorLeft().scale(0.5f))
                    .length(421f / (2250f / 2f))
                    .text(new Text()
                            .font(statFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(421f / 2250f, -9f)
                            .text("OKAY")
                    )
                    .animation(new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 1f, true), null, new FadeAnim(0.7f, 1), null, new FadeAnim(1f, LinearGraph.oneToZero, 1))
                    .attach();
            s.reviewNoButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().anchorRight().scale(0.5f))
                    .length(421f / (2250f / 2f))
                    .text(new Text()
                            .font(statFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(421f / 2250f, -9f)
                            .text("SKIP")
                    )
                    .animation(new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 1f, true), null, new FadeAnim(0.7f, 1), null, new FadeAnim(1f, LinearGraph.oneToZero, 1))
                    .attach();


            s.socialView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.95f).move(0, +0.10f))
                    .text(new Text()
                            .font(noticeFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(Globals.LENGTH, 16f)
                            .text("For news on SIMULACRA and our upcoming titles,\n\nFollow us on Twitter and Facebook")
                    )
                    .animation(
                            new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 1f, true),
                            null,
                            new FadeAnim(1f, LinearGraph.oneToZero)
                    )
                    ;
            s.socialTwitterButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(320f / 1080f).move(-0.22f, -0.35f))
                    .visuals(Sprite.load("menu/twitter-button.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne), 1f, true), null, new FadeAnim(0.7f), null, new FadeAnim(1f, LinearGraph.oneToZero))
                    ;
            s.socialFbButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(320f / 1080f).move(+0.22f, -0.35f))
                    .visuals(Sprite.load("menu/fb-button.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne), 1f, true), null, new FadeAnim(0.7f), null, new FadeAnim(1f, LinearGraph.oneToZero))
                    ;
            s.socialNextButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .length(421f / 2250f)
                    .text(new Text()
                            .font(statFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(421f / 2250f, -18f)
                            .text("NEXT")
                    )
                    .animation(
                            new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne), 1f, true),
                            null,
                            new FadeAnim(0.7f, 1),
                            null,
                            new FadeAnim(1f, LinearGraph.oneToZero, 1)
                    )
                    ;

//            "You and Teddy are both erased from reality",
//                    "You are erased from reality while Teddy is spared",
//                    "Teddy is erased from reality while you are spared",
//                    "Both you and Teddy beat FlapeeBird at its own game"

            // Icons
            Sprite bothDeadIcon = Sprite.load("apps/stat/both-dead.png");
            Sprite bothDeadDisabledIcon = Sprite.load("apps/stat/both-dead-disabled.png");
            Sprite playerDiesIcon = Sprite.load("apps/stat/player-dies.png");
            Sprite playerDiesDisabledIcon = Sprite.load("apps/stat/player-dies-disabled.png");
            Sprite teddyDiesIcon = Sprite.load("apps/stat/teddy-dies.png");
            Sprite teddyDiesDisabledIcon = Sprite.load("apps/stat/teddy-dies-disabled.png");
            Sprite bothSurviveIcon = Sprite.load("apps/stat/both-survive.png");
            Sprite bothSurviveDisabledIcon = Sprite.load("apps/stat/both-survive-disabled.png");

            s.endIcons = new Sprite[] {
                    bothDeadIcon,
                    playerDiesIcon,
                    teddyDiesIcon,
                    bothSurviveIcon,
            };

            s.endIconsDisabled = new Sprite[] {
                    bothDeadDisabledIcon,
                    playerDiesDisabledIcon,
                    teddyDiesDisabledIcon,
                    bothSurviveDisabledIcon,
            };


            float y = (-751f / 2250f) - 0.04f;
            float size = 356f / 2250f;

            float x1 = +324f / 2250f;
            float x2 = +849f / 2250f;
            float x3 = +1371f / 2250f;
            float x4 = +1893f / 2250f;

            StaticSprite bothDeadIconView = new StaticSprite()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(size).anchor(-0.5f, +0.5f).move(x1 , y))
                    .visual(bothDeadIcon, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            StaticSprite playerDiesIconView = bothDeadIconView.instantiate()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(size).anchor(-0.5f, +0.5f).move(x2 , y))
                    .visual(playerDiesIcon)
                    .attach();

            StaticSprite teddyDiesIconView = bothDeadIconView.instantiate()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(size).anchor(-0.5f, +0.5f).move(x3 , y))
                    .visual(teddyDiesIcon)
                    .attach();

            StaticSprite bothSurviveIconView = bothDeadIconView.instantiate()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(size).anchor(-0.5f, +0.5f).move(x4 , y))
                    .visual(bothSurviveIcon)
                    .attach();

            s.endIconViews = new StaticSprite[] {
                    bothDeadIconView,
                    playerDiesIconView,
                    teddyDiesIconView,
                    bothSurviveIconView,
            };

            // Arrows
            y = (-450f / 2250f) - 0.05f;
            size = 132f / 2250f;
            float xOffset = +0.02f;

            sprite = Sprite.load("apps/stat/arrow-down.png").instantiate();
            ColorAttribute.of(sprite).set(0xf4f4f4ff);
            StaticSprite bothDeadArrowView = new StaticSprite()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(size).anchor(-0.5f, +0.5f).move(x1 + xOffset, y))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    ;
            StaticSprite playerDiesArrowView = new StaticSprite()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(size).anchor(-0.5f, +0.5f).move(x2 + xOffset, y))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    ;
            StaticSprite teddyDiesArrowView = new StaticSprite()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(size).anchor(-0.5f, +0.5f).move(x3 + xOffset, y))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    ;
            StaticSprite bothSurviveArrowView = new StaticSprite()
                    .viewport(endingGroup)
                    .metrics(new UIElement.Metrics().scale(size).anchor(-0.5f, +0.5f).move(x4 + xOffset, y))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    ;

            s.endIconArrows = new StaticSprite[] {
                    bothDeadArrowView,
                    playerDiesArrowView,
                    teddyDiesArrowView,
                    bothSurviveArrowView,
            };


            // Stat template
            size = 2167f / 2250f;

            sprite = new Sprite(416f / 2167f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff).alpha(0.58f);
            StaticSprite statView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(size))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    ;
            TextBox statTextView = new TextBox()
                    .viewport(statView)
                    .metrics(new UIElement.Metrics().scale(2104f / 2167f).move(0, +60f / 2167f))
                    .text(new Text()
                            .font(statFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(225f / 2104f, 22f)
                            .topLeft()
                            .text("[STAT_GREEN]You and 40%[] of other players choose to lie to Ashley")
                    )
                    .attach();
            sprite = new Sprite(35f / 2095f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x830318ff);
            StaticSprite statProgressBgView = new StaticSprite()
                    .viewport(statView)
                    .metrics(new UIElement.Metrics().scale(2095f / 2167f).move(0, -140f / 2167f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            sprite = new Sprite(35f / 2095f, Material.load("apps/stat/bar.png"));
            StaticSprite statProgressView = new StaticSprite()
                    .viewport(statProgressBgView)
                    .metrics(new UIElement.Metrics().anchorLeft())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            new ScaleAnim(1.5f, ScaleAnim.Location.LEFT, QuadraticGraph.zeroToOneInverted, null),
                            null,
                            null
                    )
                    .attach();

            float statY = -0.08f;

            // Stat boxes
            StaticSprite stat1 = statView.instantiate()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(size).move(0, (+766f / 2250f) + statY))
                    .animation(new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(-1f, 0f, true), null), 0.5f, true), null, new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(0f, +1f, false), null), 0.25f, false))
                    .attach();
            StaticSprite stat2 = statView.instantiate()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(size).move(0, (+299f / 2250f) + statY))
                    .animation(new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(-1f, 0f, true), null), 1.0f, true), null, new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(0f, +1f, false), null), 0.5f, false))
                    .attach();
            StaticSprite stat3 = statView.instantiate()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(size).move(0, (-160f / 2250f) + statY))
                    .animation(new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(-1f, 0f, true), null), 1.5f, true), null, new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(0f, +1f, false), null), 0.75f, false))
                    .attach();
            StaticSprite stat4 = statView.instantiate()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(size).move(0, (-641f / 2250f) + statY))
                    .animation(new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(-1f, 0f, true), null), 2.0f, true), null, new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(0f, +1f, false), null), 1.0f, false))
                    .attach();
            StaticSprite stat5 = statView.instantiate()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(size).move(0, (-1122f / 2250f) + statY))
                    .animation(new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(-1f, 0f, true), null), 2.5f, true), null, new SequenceAnim(new MoveAnim(0.25f, new QuadraticGraph(0f, +1f, true), null), 1.25f, false))
                    .attach();

            s.statViews = new UIElement[] {
                    stat1,
                    stat2,
                    stat3,
                    stat4,
                    stat5,
            };

            s.statTextViews = new TextBox[] {
                    stat1.find(statTextView),
                    stat2.find(statTextView),
                    stat3.find(statTextView),
                    stat4.find(statTextView),
                    stat5.find(statTextView),
            };

            s.statProgressViews = new StaticSprite[] {
                    stat1.find(statProgressView),
                    stat2.find(statProgressView),
                    stat3.find(statProgressView),
                    stat4.find(statProgressView),
                    stat5.find(statProgressView),
            };

            s.statYesTexts = new String[] {
                    "[STAT_GREEN]You and %d%%[] of players managed to bring Teddy back from despair",
                    "[STAT_GREEN]You and %d%%[] of players spent more than half of your life on eggs",
                    "[STAT_GREEN]You and %d%%[] of players players voted for Pay to Win in FlapeeBird's survey",
                    "[STAT_GREEN]You and %d%%[] of players shared FlapeeBird with all your contacts",
                    "[STAT_GREEN]You and %d%%[] of players took FlapeeBird's subscription deal",
            };

            s.statNoTexts = new String[] {
                    "[STAT_GREEN]You and %d%%[] of players allowed Teddy to be consumed by despair",
                    "[STAT_GREEN]You and %d%%[] of players spent less than half of your life on eggs",
                    "[STAT_GREEN]You and %d%%[] of players voted against Pay to Win in FlapeeBird's survey",
                    "[STAT_GREEN]You and %d%%[] of players did not share FlapeeBird with all your contacts",
                    "[STAT_GREEN]You and %d%%[] of players rejected FlapeeBird's subscription deal",
            };


            // Back to main menu
            s.nextButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .length(421f / 2250f)
                    .text(new Text()
                            .font(statFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(421f / 2250f, -18f)
                            .text("NEXT")
                    )
                    .animation(
                            new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 3f, true),
                            null,
                            new FadeAnim(0.7f, 1),
                            null,
                            new FadeAnim(1f, LinearGraph.oneToZero, 1)
                    )
                    .attach();

            // Back to main menu
            s.endButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .length(421f / 2250f)
                    .text(new Text()
                            .font(statFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(421f / 2250f, -18f)
                            .text("NEXT")
                    )
                    .animation(
                            new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne, 1), 4f, true),
                            null,
                            new FadeAnim(0.7f, 1),
                            null,
                            new FadeAnim(1f, LinearGraph.oneToZero, 1)
                    )
                    .attach();

            // Loading view

            // Loading group
            sprite = Sprite.load("system/loading-circle.png").instantiate();
            ColorAttribute.of(sprite).set(0xffffffff).alpha(0.4f);
            s.loadingView = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.16f))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .animation(new SequenceAnim(new FadeAnim(1f, LinearGraph.zeroToOne), 1.5f, true), new RotateAnim(1f, new LinearGraph(0f, 360f)), null)
                    .attach();
            s.tMinLoadingTime = 4f;
        }

        // Commit
        menu.setInternal(s);

    }

}
