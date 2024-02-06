package game31.gb.homescreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.ScreenBar;
import game31.app.homescreen.Homescreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.ScaleAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 5/12/2017.
 */

public class GBHomescreen {

    public GBHomescreen(Homescreen screen) {

        Homescreen.Internal s = new Homescreen.Internal();


        // App title font, white with translucent black borders
        Font titleFont = new Font("opensans-semibold.ttf", 32, new Color(0, 0, 0, 0.15f), 4, new Color(0, 0, 0, 0.25f), 0, +6, Color.WHITE, -4, 0);

        Font notificationFont = new Font("opensans-bold.ttf", 32, 0xffffffff);

        Animation appButtonPressedAnim = new ScaleAnim(0.15f, new QuadraticGraph(1f, 1.1f, -0.5f, false));
        Animation appButtonReleasedAnim = new ScaleAnim(0.1f, new QuadraticGraph(1.1f, 1.0f, false));


        float appIconSize;
        float appBottomPadding;
        float appTextSize;
        float sideOffsetY;

        if(Gdx.app.getType() == Application.ApplicationType.iOS) {
            // TODO: remove when not needed
            appIconSize = 105f / 475f;
            appBottomPadding = 182f / 427f;
            appTextSize = 7.2f;
            sideOffsetY = +0.04f;
        }
        else {
            appIconSize = 85f / 475f;
            appBottomPadding = 162f / 427f;
            appTextSize = 6.5f;
            sideOffsetY = 0;
        }


        {
            int maxRows = 5;
            int maxColumns = 4;

            float iconSize = appIconSize;

            float leftPadding = 68f / 475f;
            float bottomPadding = appBottomPadding;

            // App button metrics
            s.appMetrics = new UIElement.Metrics[maxRows][maxColumns];
            for(int r = 0; r < maxRows; r++) {
                for(int c = 0; c < maxColumns; c++) {
                    float x = -0.5f + leftPadding + (c * 114f / 475f);
                    float y = +bottomPadding +((maxRows - r - 1) * 226f / 843f);

                    if(c == 0 || c == 3)
                        y += sideOffsetY;

                    s.appMetrics[r][c] = new UIElement.Metrics().scale(iconSize).anchorBottom().move(x, y);
                }
            }

            s.appButton = new Clickable()
                    .target(SaraRenderer.TARGET_INTERACTIVE).length(1f)
                    .animation(null, null, appButtonPressedAnim, appButtonReleasedAnim, null)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .passThroughInput(true)
                    .inputPadding(0.03f, 0.03f, 0.03f, 0.06f);

            // Shadow
            Sprite iconShadow = Sprite.load("system/homescreen-shadow.png");
            ColorAttribute.of(iconShadow).alpha(0.9f);
            new StaticSprite()
                    .viewport(s.appButton)
                    .metrics(new UIElement.Metrics().scale(290f / 185f).move(0, 0))
                    .visual(iconShadow, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();

            s.appTitleView = new TextBox()
                    .viewport(s.appButton)
                    .metrics(new UIElement.Metrics().scale(118f / 85f).anchor(0, -57f / 85f))
                    .text(new Text()
                            .font(titleFont)
                            .position(16f / 106f, appTextSize)
                            .target(SaraRenderer.TARGET_INTERACTIVE)
                    ).attach();
        }



        {
            // Dock icon metrics
            int maxColumns = 4;

            float iconSize = appIconSize;
            float leftPadding = 68f / 475f;
            float bottomPadding = +44f / 427f;       // 74f

            s.dockAppMetrics = new UIElement.Metrics[maxColumns];

            for(int c = 0; c < maxColumns; c++) {
                float x = -0.5f + leftPadding + (c * 114f / 475f);
                float y = +bottomPadding;

                if(c == 0 || c == 3)
                    y += sideOffsetY;

                s.dockAppMetrics[c] = new UIElement.Metrics().scale(iconSize).anchorBottom().move(x, y);
            }

            // Button
            s.dockAppButton = new Clickable()
                    .target(SaraRenderer.TARGET_INTERACTIVE).length(1)
                    .animation(null, null, appButtonPressedAnim, appButtonReleasedAnim, null)
                    .inputPadding(0.03f, 0.03f, 0.03f, 0.06f);

            // Shadow
            Sprite iconShadow = Sprite.load("system/homescreen-shadow.png");
            ColorAttribute.of(iconShadow).alpha(0.9f);
            new StaticSprite()
                    .viewport(s.dockAppButton)
                    .metrics(new UIElement.Metrics().scale(290f / 185f).move(0, 0))
                    .visual(iconShadow, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();

            s.dockAppButtonReflection = new StaticSprite()
                    .viewport(s.dockAppButton)
                    .metrics(new UIElement.Metrics().scale(1f, -1f).anchor(0, -1f))
                    .target(SaraRenderer.TARGET_BG_SHADOWS).length(1)
                    .animation(null, new ColorAnim(1f, new ConstantGraph(0.55f), new ConstantGraph(0.25f)), null)
                    .attach();

            s.dockAppTitleView = new TextBox()
                    .viewport(s.dockAppButton)
                    .metrics(new UIElement.Metrics().scale(118f / 85f).anchor(0, -57f / 85f))
                    .text(new Text()
                            .font(titleFont)
                            .position(16f / 106f, -appTextSize)
                            .target(SaraRenderer.TARGET_INTERACTIVE)
                    ).attach();
        }

        // Notifications
        {
            Sprite notificationBg = Sprite.load("system/notification.png");

            // Notification view
            s.notificationView = new StaticSprite()
                    .metrics(new UIElement.Metrics().anchorTop().anchorRight().scale(0.4f))
                    .visual(notificationBg, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
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

            s.notificationTextView = new TextBox()
                    .viewport(s.notificationView)
                    .metrics(new UIElement.Metrics().move(+0.03f, +0.05f).scale(0.6f))
                    .text(new Text()
                            .font(notificationFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(0.7f, 0)
                            .text("99+")
                    )
                    .attach();

            // Locked view
            s.notificationLockedView = new StaticSprite()
                    .metrics(new UIElement.Metrics().anchor(+0.5f, -0.5f).pan(-0.25f, +0.25f).scale(0.3f))
                    .visual(Sprite.load("system/locked-icon.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING);

            // Error view
            s.notificationErrorView = new StaticSprite()
                    .metrics(new UIElement.Metrics().anchor(+0.5f, -0.5f).pan(-0.25f, +0.25f).scale(0.3f))
                    .visual(Sprite.load("system/error-icon.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING);
        }


        {
            // Window
            s.window = new UIElement.Group();
            s.bg = new StaticSprite()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .target(SaraRenderer.TARGET_BG)
                    .attach();

            // Status
            ScreenBar screenBar = new ScreenBar();
            screenBar.showAppbarClock(0,0);
            screenBar.attach(screen);


            // Home swipeable surface
            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .scrollable(true, false)
                    .selectiveRendering(true, false)
                    .minimumPadding(0.1f, 0, 0.1f, 0)
                    .scrollGravity(0.3f, 0f, 1f, Globals.LENGTH / 2f)
                    .attach();

            // Dock background
//            Sprite sprite = new Sprite(150f / 1242f, Material.load("system/gradient-thick.png"));
            Sprite sprite = Sprite.load("system/homescreen-bottom.png").instantiate();
            ColorAttribute.of(sprite).set(0x000000ff).alpha(1f);
            s.dockView = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .length(sprite.length)
                    .attach();
            new StaticSprite()
                    .viewport(s.dockView)
                    .metrics(new UIElement.Metrics().scale(1f, 1f))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();


            // Sounds
            s.openSound = Sound.load("sounds/homescreen_openapp.ogg");
            s.closeSound = Sound.load("sounds/homescreen_homepage.ogg");

            s.tQueuedAppTimeout = 2f;


        }

        // Done
        screen.setInternal(s);

//        // Date and time widget
//        HomescreenDateWidget dateWidget = new HomescreenDateWidget(screen);
//        dateWidget.attach(screen);
    }
}
