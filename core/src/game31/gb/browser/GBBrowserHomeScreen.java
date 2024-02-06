package game31.gb.browser;

import com.badlogic.gdx.utils.Align;

import game31.Globals;
import game31.ScreenBar;
import game31.app.browser.BrowserScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.NullAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.Range;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.InputField;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 7/8/2017.
 */

public class GBBrowserHomeScreen {

    public GBBrowserHomeScreen(BrowserScreen screen) {
        BrowserScreen.Internal s = new BrowserScreen.Internal();

        Sprite sprite;
        PatchedSprite patch;

        Font homeNavInactiveFont = new Font("raleway-semibold.ttf", 32, 0xccccccff);
        Font homeNavHomepageFont = new Font("raleway-semibold.ttf", 32, 0xe1e3e2ff);

        Font surfFont = new Font("raleway-semibold.ttf", 48, 0x42d6efff);

        Font homeSubtitleFont = new Font("raleway-semibold.ttf", 32, 0xe1e3e2ff);

        Font homeBookmarkNameFont = new Font("raleway-semibold.ttf", 40, 0xf2f2f2ff);
        Font homeBookmarkTitleFont = new Font("opensans-lightitalic.ttf", 32, 0xf2f2f2ff);

        Font mostVisitedLinkTitle = new Font("raleway-semibold.ttf", 32, 0x313131ff);

        Font webNavTitleFont = new Font("raleway-regular.ttf", 32, 0x4d4d4dff);

        // Link fonts
        Font navLinkName = new Font("raleway-semibold.ttf", 32, 0x333333ff);
        navLinkName.name("browser_linkname");
        Font navLinkTitle = new Font("opensans-lightitalic.ttf", 32, 0x333333ff);
        Font navSectionFont = new Font("opensans-bold.ttf", 32, 0x888888ff);


        // TODO
        Sprite faviconDefault = Sprite.load("apps/browser/favicon-default.png");


        {
            s.window = new UIElement.Group();

            // Bg
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x252c41ff);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();


            s.bars = new ScreenBar();
            s.bars.showAppbar("Surf", null, 0, 0, 0, 0);
            s.bars.showShadows(0x0e162dff, 1f);
            s.bars.color(0x0e162dff, 1f, 0x0e162dff, 0.5f);
            s.bars.showNavbar(true, true, true);

            s.backSound = Sound.load("sounds/browser_click.ogg");
        }


        {
            // Web view
            s.webGroup = new UIElement.Group()
                    .viewport(s.window)
                    .attach();

            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xffffffff);
            s.webBgView = new StaticSprite()
                    .viewport(s.webGroup)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();

            s.webSurface = new ScrollableSurface()
                    .viewport(s.webGroup)
                    .length(Globals.LENGTH)
                    .padding(0, 0.297f, 0, 0.16f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();
            s.webSurfacePaddingNormal = 0.16f;
            s.webSurfacePaddingWithInput = (371f / 550f) + 0.07f;

            s.webImageTarget = SaraRenderer.TARGET_INTERACTIVE;
            s.webTextTarget = SaraRenderer.TARGET_INTERACTIVE_TEXT;



            // Top navigation bar
            sprite = new Sprite(298f / 2255f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff);
            s.webNavView = new StaticSprite()
                    .viewport(s.webGroup)
                    .metrics(new UIElement.Metrics().anchorTop().move(0, -373f / 2250f))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                    .attach();

            // Surf button
            float tabSize = 1700f;
            float cornerSize = ((298f / tabSize) / 2f) / (132f / 256f);
            patch = PatchedSprite.create("apps/browser/tab.png", 298f / tabSize, 0, cornerSize, cornerSize, cornerSize);
            ColorAttribute.of(patch).set(0x252c41ff);
            StaticSprite navSurfBg = new StaticSprite()
                    .viewport(s.webNavView)
                    .metrics(new UIElement.Metrics().scale(tabSize / 2255f).anchorLeft())
                    .visual(patch, SaraRenderer.TARGET_APPBAR)
                    .attach();

            patch = PatchedSprite.create("system/circle-tl.png", 197f / 1450f, (197f / 1450f) / 2f);
            ColorAttribute.of(patch).set(0xe1e3e2ff);
            s.webNavUrlButton = new Clickable()
                    .viewport(navSurfBg)
                    .metrics(new UIElement.Metrics().scale(1450f / tabSize).anchorLeft().move(+60f / 2255f, 0))
                    .visuals(patch, SaraRenderer.TARGET_APPBAR)
                    .text(new Text()
                            .font(webNavTitleFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(+0.055f, +0.002f, 1196f / 1450f, 116f / 1196f, 12f)
                            .centerLeft()
                            .ellipsize(1)
                            .text("robotbank.web/C59MQ4WF6/convo/C59MQ4WF6-1499739879.057625/")
                    )
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.8f),
                            new FadeAnim(0.2f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .attach();
            s.webNavFaviconView = new StaticSprite()
                    .viewport(s.webNavUrlButton)
                    .metrics(new UIElement.Metrics().scale(123f / 1450f).anchorLeft().move(+0.03f, 0f))
                    .visual(faviconDefault, SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Home button
            tabSize = 767f;
            cornerSize = ((298f / tabSize) / 2f) / (132f / 256f);
            patch = PatchedSprite.create("apps/browser/tab.png", 298f / tabSize, 0, cornerSize, cornerSize, cornerSize);
            ColorAttribute.of(patch).set(0x192137ff);
            s.webNavHomeButton = new Clickable()
                    .viewport(s.webNavView)
                    .metrics(new UIElement.Metrics().scale(tabSize / 2255f).anchorRight().move(-0.10f, 0))
                    .visuals(patch, SaraRenderer.TARGET_APPBAR_BG)
                    .animation(
                            null,
                            null,
                            new ColorAnim(0x232a39ff, false),
                            new ColorAnim(
                                    0.2f,
                                    new LinearGraph(35f / 255f, 28f / 255f),
                                    new LinearGraph(42f / 255f, 36f / 255f),
                                    new LinearGraph(57f / 255f, 49f / 255f),
                                    null,
                                    false
                            ),
                            null
                    )
                    .inputPadding(-0.25f, 0, 0, 0)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .attach();

            sprite = Sprite.load("apps/browser/home.png").instantiate();
            ColorAttribute.of(sprite).set(0x7e8fa3ff);
            new StaticSprite()
                    .viewport(s.webNavHomeButton)
                    .metrics(new UIElement.Metrics().scale(131f / tabSize).anchorRight().move(-0.30f, +0.002f))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();


            // Close button
            sprite = Sprite.load("system/close.png").instantiate();
            ColorAttribute.of(sprite).set(0x7e8fa355);
            s.webNavCloseButton = new Clickable()
                    .viewport(s.webNavView)
                    .metrics(new UIElement.Metrics().scale(115f / 2255f).anchorRight().move(-100f / 2255f, 0))
                    .visuals(sprite, SaraRenderer.TARGET_APPBAR)
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.8f),
                            new FadeAnim(0.2f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .inputPadding(1.3f, 0.5f, 1f, 0.6f)
                    .sound(Sound.load("sounds/general_back.ogg"))
                    .attach();

            // Loading bar
            {
                float loadingBarHeight = 30f;
                float loadingBarLength = loadingBarHeight / 2255f;
                float glowWidth = 245f;
                float glowSize = glowWidth / 2255f;
                Sprite bg = new Sprite(loadingBarLength, SaraRenderer.renderer.coloredMaterial);
                ColorAttribute.of(bg).set(0xcdfbf1ff);
                Sprite bar = new Sprite(loadingBarLength, SaraRenderer.renderer.coloredMaterial);
                Sprite knob = new Sprite(loadingBarHeight / glowWidth, Material.load("apps/browser/loading-bar.png"));
                ColorAttribute.of(bar).set(0x42d6efff);
                s.webLoadingBar = new HorizontalProgressBar()
                        .viewport(s.webNavView)
                        .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1))
                        .visual(bg, bar, SaraRenderer.TARGET_APPBAR)
                        .knob(knob, glowSize, 0, 0, -(glowSize / 2f), 0)
                        .animation(
                                new ScissorAnim(0.5f, new Animation[] {
                                        new MoveAnim(1f, null, new QuadraticGraph(+loadingBarLength, 0, true))
                                }),
                                null,
                                new SequenceAnim(new Animation[] {
                                        new NullAnim(0.3f),
                                        new ScissorAnim(0.5f, new Animation[] {
                                                new MoveAnim(1f, null, new QuadraticGraph(0, +loadingBarLength, false))
                                        })
                                })
                        )
                        ; // .attach();
            }

            // Selection
            s.selectButton = new Clickable()
                    .target(SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .animation(
                            null,
                            null,
                            new ScaleAnim(0.15f, new QuadraticGraph(1.05f, 1f, true)),
                            new FadeAnim(0.5f, QuadraticGraph.oneToZero),
                            null
                    )
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .sound(Sound.load("sounds/browser_click.ogg"))
                    ;


            s.selectCornerSize = 0.03f;
            s.selectMinWidth = s.selectCornerSize * 2f;
            s.selectMinHeight = s.selectCornerSize * 2f;
            s.selectSprite = PatchedSprite.create("apps/browser/select.png", 1f, s.selectCornerSize);

            // Input field
            s.inputField = new InputField()
                    .target(s.webTextTarget)
                    .passThroughInput(true)
                    .alignment(Align.left)      // center left
                    .enable();
            s.inputMaxBottomY = (-Globals.LENGTH / 2f) + (371f / 550f) + 0.07f;


            // Checkbox
            s.checkbox = new Clickable()
                    .visuals(Sprite.load("system/close.png").instantiate(), s.webTextTarget)
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    ;

            s.checkboxTickedAnim = new ScaleAnim(0.2f, new QuadraticGraph(0f, 1f, 1f, 1.3f, true));
            s.checkboxUntickedAnim = new SequenceAnim(new Animation[] {
                    new ScaleAnim(0.2f, LinearGraph.oneToZero),
                    new FadeAnim(0f)
            });

            // Loading simulation
            s.tLoadingSeekSpeed = 0.5f;

            s.textBandwidth = new Range(30, 10);
            s.maxTextBandwidth = 120;

            s.imageBandwidth = new Range(128 * 128, 64 * 64);
            s.maxImageBandwidth = 400 * 400;

            s.minImageProgress = 0.1f;

            s.tLoadingInterval = new Range(0.2f, 0.1f);

            s.pixelationLevels = new float[] {
                    3f,
                    6f,
                    10f,
                    20f,
            };

            s.tConnectingTime = new Range(0.7f, 0.5f);
            s.connectingProgress = 0.15f;
//            s.tConnectingBarTime = 0.6f;

            s.maxCachedObjects = 100;
            s.maxCachedProgress = 0.95f;
        }



        {
            // Dropdown menu
            s.dropGroup = new UIElement.Group()
                    .viewport(s.window)
                    ; // .attach();

            UIElement.Group navSurfaceGroup = new UIElement.Group()
                    .viewport(s.dropGroup)
                    .animation(
                            new MoveAnim(0.3f, null, new QuadraticGraph(+Globals.LENGTH, 0f, true)),
                            null, null
                    )
                    .attach();

            s.dropSurface = new ScrollableSurface()
                    .viewport(navSurfaceGroup)
                    .metrics(new UIElement.Metrics().anchorTop().move(0, 0))
                    .length(Globals.LENGTH)
                    .padding(0, 0.297f, 0, 0.16f)
                    .scrollable(false, true)
                    .selectiveRendering(true, true)
                    .attach();

            // Top navigation bar
            sprite = new Sprite(298f / 2255f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff);
            StaticSprite navGroup = new StaticSprite()
                    .viewport(s.dropGroup)
                    .metrics(new UIElement.Metrics().anchorTop().move(0, -373f / 2250f))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                    .attach();

            // Surf button
            float tabSize = 1700f;
            float cornerSize = ((298f / tabSize) / 2f) / (132f / 256f);
            patch = PatchedSprite.create("apps/browser/tab.png", 298f / tabSize, 0, cornerSize, cornerSize, cornerSize);
            ColorAttribute.of(patch).set(0xeeeeeeff);
            s.dropNavUrlButton = new Clickable()
                    .viewport(navGroup)
                    .metrics(new UIElement.Metrics().scale(tabSize / 2255f).anchorLeft())
                    .visuals(patch, SaraRenderer.TARGET_APPBAR)
                    .animation(
                            null,
                            null,
                            new ColorAnim(0xffffffff, false),
                            null,
                            null
                    )
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .attach();

            s.dropNavUrlView = new InputField()
                    .viewport(s.dropNavUrlButton)
                    .metrics(new UIElement.Metrics().scale(1196f / tabSize).anchorLeft().move(+0.148f, +0.0015f))
                    .font(webNavTitleFont, 116f / 1196f, -12f, SaraRenderer.TARGET_APPBAR_TEXT)
                    .alignment(Align.left)
//                    .length()
//                    .text(new Text()
//                            .font(webNavTitleFont, SaraRenderer.TARGET_APPBAR_TEXT)
//                            .position(+0.055f, +0.002f, 1196f / 1450f, 116f / 1196f, 12f)
//                            .centerLeft()
//                            .ellipsize(1)
//                            .text("Where to ?")
//                    )
//                    .disable()
                    .attach();
            s.dropNavFaviconView = new StaticSprite()
                    .viewport(s.dropNavUrlButton)
                    .metrics(new UIElement.Metrics().scale(123f / tabSize).anchorLeft().move(+0.0525f, 0f))
                    .visual(faviconDefault, SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Home button
            tabSize = 767f;
            cornerSize = ((298f / tabSize) / 2f) / (132f / 256f);
            patch = PatchedSprite.create("apps/browser/tab.png", 298f / tabSize, 0, cornerSize, cornerSize, cornerSize);
            ColorAttribute.of(patch).set(0x192137ff);
            s.dropNavHomeButton = new Clickable()
                    .viewport(navGroup)
                    .metrics(new UIElement.Metrics().scale(tabSize / 2255f).anchorRight().move(-0.10f, 0))
                    .visuals(patch, SaraRenderer.TARGET_APPBAR_BG)
                    .animation(
                            null,
                            null,
                            new ColorAnim(0x232a39ff, false),
                            new ColorAnim(
                                    0.2f,
                                    new LinearGraph(35f / 255f, 28f / 255f),
                                    new LinearGraph(42f / 255f, 36f / 255f),
                                    new LinearGraph(57f / 255f, 49f / 255f),
                                    null,
                                    false
                            ),
                            null
                    )
                    .inputPadding(-0.25f, 0, 0, 0)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .attach();

            sprite = Sprite.load("apps/browser/home.png").instantiate();
            ColorAttribute.of(sprite).set(0x7e8fa3ff);
            new StaticSprite()
                    .viewport(s.dropNavHomeButton)
                    .metrics(new UIElement.Metrics().scale(131f / tabSize).anchorRight().move(-0.30f, +0.002f))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();


            // Close button
            sprite = Sprite.load("system/close.png").instantiate();
            ColorAttribute.of(sprite).set(0x7e8fa355);
            s.dropNavCloseButton = new Clickable()
                    .viewport(navGroup)
                    .metrics(new UIElement.Metrics().scale(115f / 2255f).anchorRight().move(-100f / 2255f, 0))
                    .visuals(sprite, SaraRenderer.TARGET_APPBAR)
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.8f),
                            new FadeAnim(0.2f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .inputPadding(1.3f, 0.5f, 1f, 0.6f)
                    .sound(Sound.load("sounds/general_back.ogg"))
                    .attach();

            // Dropdown navigation
            s.dropLinkFormat = "[browser_linkname]%s - []%s";
            // Link button
            sprite = new Sprite(220f / 2255f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xe1e3e2ff);
            s.dropLinkButton = new Clickable()
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .text(new Text()
                            .font(navLinkTitle, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                            .position(+65f / 2253f, 0, 1894f / 2253f, 104f / 1894f, 17f)
                            .ellipsize(1)
                            .centerLeft()
                    )
                    .animation(
                            null,
                            null,
                            new ColorAnim(0xffffffff, false),
                            null,
                            null
                    )
                    .sound(Sound.load("sounds/browser_click.ogg"))
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    ;
            s.dropLinkFaviconView = new StaticSprite()
                    .viewport(s.dropLinkButton)
                    .metrics(new UIElement.Metrics().scale(104f / 2253f).anchorLeft().move(+88f / 2253f, 0))
                    .visual(faviconDefault, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .attach();

            // Section
            sprite = new Sprite(200f / 2255f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xeeeeeeff);
            Clickable sectionView = new Clickable()
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .text(new Text()
                            .font(navSectionFont, SaraRenderer.TARGET_INTERACTIVE_OVERLAY_TEXT)
                            .position(0, 0, 2100f / 2253f, 140f / 2100f, 23f)
                            .centerLeft()
                    )
                    .disable();
            s.dropBookmarksSectionView = sectionView.instantiate().text("BOOKMARKS");
            s.dropHistorySectionView = sectionView.instantiate().text("HISTORY");


            // Bottom end
            cornerSize = 119f / 2253f;
            patch = PatchedSprite.create("system/circle.png", 119f / 2253f, cornerSize, cornerSize, 0f, cornerSize);
            ColorAttribute.of(patch).set(0xe1e3e2ff);
            s.dropEndSection = new StaticSprite()
                    .visual(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    ;

            s.dropDefaultFavicon = faviconDefault;
        }

        {
            // Homepage
            s.homeGroup = new UIElement.Group()
                    .viewport(s.window)
                    ;//.attach();

            // Top navigation bar
            sprite = new Sprite(298f / 2255f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff);
            s.homeNavView = new StaticSprite()
                    .viewport(s.homeGroup)
                    .metrics(new UIElement.Metrics().anchorTop().move(0, -373f / 2250f))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                    .attach();

            // Home nav bar
            // Home button
            patch = PatchedSprite.create("apps/browser/tab.png", 298f / 1319f, ((298f / 1319f) / 2f) / (132f / 256f));
            ColorAttribute.of(patch).set(0x252c41ff);
            Clickable navHomeButton = new Clickable()
                    .viewport(s.homeNavView)
                    .metrics(new UIElement.Metrics().scale(1319f / 2255f).anchorRight())
                    .visuals(patch, SaraRenderer.TARGET_APPBAR)
                    .text(new Text()
                            .font(homeNavHomepageFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(+0.065f, 0, 1f, 298f / 1319f, -12f)
                            .text("Home page")
                    )
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.8f),
                            new FadeAnim(0.2f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .attach();
            sprite = Sprite.load("apps/browser/home.png").instantiate();
            ColorAttribute.of(sprite).set(0xe1e3e2ff);
            new StaticSprite()
                    .viewport(navHomeButton)
                    .metrics(new UIElement.Metrics().scale(131f / 1319f).anchorLeft().move(+229f / 1319f, +0.002f))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();


            // Surf button bg
            sprite = new Sprite(298f / 1379f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x192137ff);
            new StaticSprite()
                    .viewport(s.homeNavView)
                    .metrics(new UIElement.Metrics().scale(1379f / 2255f).anchorLeft())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                    .attach();

            // Surf button
            patch = PatchedSprite.create("system/circle-tl.png", 197f / 950f, (197f / 950f) / 2f);
            ColorAttribute.of(patch).set(0x666b7aff);
            s.homeNavUrlEmptyButton = new Clickable()
                    .viewport(s.homeNavView)
                    .metrics(new UIElement.Metrics().anchorLeft().scale(950f / 2255f).move(+45f / 2255f, 0))
                    .visuals(patch, SaraRenderer.TARGET_APPBAR)
                    .text(new Text()
                            .font(homeNavInactiveFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(720f / 950f, 197f / 720f, -7f)
                            .ellipsize(1)
                            .text("Surf's Up")
                    )
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.8f),
                            new FadeAnim(0.2f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    ; // .attach();

            // Url button with favicon
            patch = PatchedSprite.create("system/circle-tl.png", 197f / 950f, (197f / 950f) / 2f);
            ColorAttribute.of(patch).set(0xe1e3e2ff);
            s.homeNavUrlButton = new Clickable()
                    .viewport(s.homeNavView)
                    .metrics(new UIElement.Metrics().anchorLeft().scale(950f / 2255f).move(+45f / 2255f, 0))
                    .visuals(patch, SaraRenderer.TARGET_APPBAR)
                    .text(new Text()
                            .font(webNavTitleFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(+0.075f, +0.004f, 170f / 237f, 29f / 170f, 6.82f)
                            .centerLeft()
                            .ellipsize(1)
                    )
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.8f),
                            new FadeAnim(0.2f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    ;

            s.homeNavFaviconView = new StaticSprite()
                    .viewport(s.homeNavUrlButton)
                    .metrics(new UIElement.Metrics().scale(123f / 950f).anchorLeft().move(+0.0455f, 0f))
                    .visual(faviconDefault, SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Surface
            s.homeSurface = new ScrollableSurface()
                    .viewport(s.homeGroup)
                    .length(Globals.LENGTH)
                    .padding(0, 0.30f, 0, 0.16f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();


            // Logo
            StaticSprite logo = new StaticSprite()
                    .viewport(s.homeSurface)
                    .metrics(new UIElement.Metrics().scale(498f / 2255f).anchorTop().move(0, -0.33f))
                    .visual(Sprite.load("apps/browser/logo.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            new TextBox()
                    .viewport(logo)
                    .metrics(new UIElement.Metrics().scale(1f).anchorBottom().pan(0, -1.75f))
                    .text(new Text()
                            .font(surfFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(95f / 458f, 0)
                            .text("S U R F")
                    )
                    .attach();

            // Most visited
            sprite = new Sprite(1.1f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x192137ff);
            StaticSprite mostVisitedGroup = new StaticSprite()
                    .viewport(s.homeSurface)
                    .metrics(new UIElement.Metrics().anchorTop().move(0, -0.67f))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();

            // Title
            new TextBox()
                    .viewport(mostVisitedGroup)
                    .metrics(new UIElement.Metrics().scale(2050f / 2255f).anchorTop().anchorLeft().move(+0.045f, -0.025f))
                    .text(new Text()
                            .font(homeSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(95f / 2050f, -21f)
                            .centerLeft()
                            .text("Recently visited")
                    )
                    .attach();

            // Boxes
            patch = PatchedSprite.create("system/rounded.png", 730f / 957f, 0.04f);
            ColorAttribute.of(patch).set(0xe0e2e1ff);
            Clickable linkButton = new Clickable()
                    .visuals(patch, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            null,
                            null,
                            new FadeAnim(0.8f),
                            new FadeAnim(0.2f, new LinearGraph(0.8f, 1f)),
                            null
                    )
                    .sound(Sound.load("sounds/browser_click.ogg"))
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    ;
            sprite = new Sprite(550f / 931f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xffffffff);
            s.homeMostVisitedPreviewView = new StaticSprite()
                    .viewport(linkButton)
                    .metrics(new UIElement.Metrics().scale(920f / 957f).anchorBottom().move(0, +0.02f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                    .attach();
            s.homeMostVisitedTitleView = new TextBox()
                    .viewport(linkButton)
                    .metrics(new UIElement.Metrics().scale(780f / 957f).anchorTop().anchorRight().move(-0.01f, -0.01f))
                    .text(new Text()
                            .font(mostVisitedLinkTitle, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(150f / 780f, 10f)
                            .text("Faris & Kane - Spend your money here!")
                            .centerLeft()
                            .ellipsize(1, "...")
                    )
                    .attach();
            s.homeMostVisitedFaviconView = new StaticSprite()
                    .viewport(linkButton)
                    .metrics(new UIElement.Metrics().scale(0.10f).anchorTop().anchorLeft().move(+0.037f, -0.037f))
                    .visual(faviconDefault, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            // Box 1
            float boxSize = 990f / 2255f;
            float y1 = -220f / 2255f;
            float y2 = -1050f / 2255f;
            float x = 96f / 2255f;
            Clickable link1 = linkButton.instantiate()
                    .viewport(mostVisitedGroup)
                    .metrics(new UIElement.Metrics().scale(boxSize).anchorTop().anchorLeft().move(+x, y1));
            Clickable link2 = linkButton.instantiate()
                    .viewport(mostVisitedGroup)
                    .metrics(new UIElement.Metrics().scale(boxSize).anchorTop().anchorRight().move(-x, y1));
            Clickable link3 = linkButton.instantiate()
                    .viewport(mostVisitedGroup)
                    .metrics(new UIElement.Metrics().scale(boxSize).anchorTop().anchorLeft().move(+x, y2));
            Clickable link4 = linkButton.instantiate()
                    .viewport(mostVisitedGroup)
                    .metrics(new UIElement.Metrics().scale(boxSize).anchorTop().anchorRight().move(-x, y2));

            s.homeMostVisitedButtons = new Clickable[] {
                    link1,
                    link2,
                    link3,
                    link4,
            };

            // Title
            new TextBox()
                    .viewport(mostVisitedGroup)
                    .metrics(new UIElement.Metrics().scale(2050f / 2255f).anchorTop().anchorLeft().move(+0.045f, -0.83f))
                    .text(new Text()
                            .font(homeSubtitleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(95f / 2050f, -21f)
                            .centerLeft()
                            .text("Bookmarks")
                    )
                    .attach();

            // Bookmark rows
            sprite = new Sprite(426f / 2256f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff);
            s.homeBookmarkRow = new Clickable()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .visuals(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    .animation(
                            null,
                            null,
                            new ColorAnim(0.8f, 0.8f, 0.8f, 1f),
                            new ColorAnim(0.2f, new LinearGraph(0.8f, 1f), null),
                            null
                    )
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .sound(Sound.load("sounds/browser_click.ogg"))
                    ;
            // Line
            sprite = new Sprite(7f / 2256f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x252c41ff);
            new StaticSprite()
                    .viewport(s.homeBookmarkRow)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();
            s.homeBookmarkFaviconView = new StaticSprite()
                    .viewport(s.homeBookmarkRow)
                    .metrics(new UIElement.Metrics().scale(240f / 2256f).move(-900f / 2256f, -0.001f))
                    .visual(faviconDefault, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            s.homeBookmarkNameView = new TextBox()
                    .viewport(s.homeBookmarkRow)
                    .metrics(new UIElement.Metrics().scale(1664f / 2256f).move(+165f / 2256f, +82f / 2256f))
                    .text(new Text()
                            .font(homeBookmarkNameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(95f / 1664f, 10f)
                            .ellipsize(1)
                            .centerLeft()
                    )
                    .attach();
            s.homeBookmarkTitleView = new TextBox()
                    .viewport(s.homeBookmarkRow)
                    .metrics(new UIElement.Metrics().scale(1664f / 2256f).move(+165f / 2256f, -80f / 2256f))
                    .text(new Text()
                            .font(homeBookmarkTitleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(90f / 1664f, 16f)
                            .ellipsize(1)
                            .centerLeft()
                    )
                    .attach();
            s.homeBookmarkStartY = (s.homeSurface.getLength() / 2f) - s.homeSurface.paddingTop() - 1.27f;



        }

        // Commit
        screen.setInternal(s);
    }


}
