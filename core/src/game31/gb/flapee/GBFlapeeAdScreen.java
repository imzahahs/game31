package game31.gb.flapee;

import game31.Globals;
import game31.app.flapee.FlapeeAdScreen;
import game31.app.gallery.FullVideoScreen;
import game31.renderer.SaraRenderer;
import game31.renderer.ScreenMaterial;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.audio.Sound;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.CircularSprite;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

public class GBFlapeeAdScreen implements FlapeeAdScreen.BuilderSource {

    public GBFlapeeAdScreen(FlapeeAdScreen screen)  {
        FlapeeAdScreen.Internal s = new FlapeeAdScreen.Internal();

        Font timerFont = new Font("opensans-semibold.ttf", 32, 0xffffffff);
        Font skipFont = new Font("opensans-bold.ttf", 48, 0xffffffaa);
        Font titleFont = new Font("opensans-regular.ttf", 48, 0xffffffff);

        Sprite sprite;

        {

            // Window
            s.window = new UIElement.Group();

            // Bg
            // Use a screencap of the previous screen as the bg
            sprite = new Sprite(Globals.LENGTH, new ScreenMaterial(SaraRenderer.RENDER_FIRST));
            new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(1, -1))
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .animation(
                            null,
                            new ColorAnim(0.25f, 0.25f, 0.25f, 1f),
                            null
                    )
                    .attach();



            // Video view
            s.videoView = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.95f))
                    .target(SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            // Title
            s.titleView = new TextBox()
                    .viewport(s.videoView)
                    .metrics(new UIElement.Metrics().scale(1f).anchorTop())
                    .text(new Text()
                            .font(titleFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                            .position(0, -0.02f, 0.96f, 0.09f, 31f)
                            .topLeft()
                    )
                    ;
            sprite = new Sprite(s.titleView.getLength() * 0.7f, Material.load("system/gradient.png"));
            ColorAttribute.of(sprite).set(0x000000ff).alpha(0.5f);
            new StaticSprite()
                    .viewport(s.titleView)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            // Logo
            s.logoView = new StaticSprite()
                    .viewport(s.videoView)
                    .metrics(new UIElement.Metrics())
                    .visual(Sprite.load("apps/flapee/iris-ads.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .length(-1)
                    .attach();
            s.tLogoTime = 3f;

            // Timer view
            sprite = Sprite.load("system/circle.png").instantiate();
            ColorAttribute.of(sprite).set(0x00000088);
            s.timerGroup = new StaticSprite()
                    .viewport(s.videoView)
                    .metrics(new UIElement.Metrics().scale(0.07f).anchorBottom().anchorRight().move(-0.03f, +0.03f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            new ScaleAnim(0.25f, QuadraticGraph.zeroToOneInverted),
                            null,
                            new ScaleAnim(0.25f, LinearGraph.oneToZero)
                    )
                    ;
            s.timerMesh = new CircularSprite("system/circle-hollow.png");
            new StaticSprite()
                    .viewport(s.timerGroup)
                    .metrics(new UIElement.Metrics().scale(0.8f))
                    .visual(s.timerMesh, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            s.timerTextView = new TextBox()
                    .viewport(s.timerGroup)
                    .metrics(new UIElement.Metrics().scale(0.7f))
                    .text(new Text()
                            .font(timerFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(0.4f, 0)
                            .text("30")
                    )
                    .attach();
            s.tSkipTime = 8f;

            // Skip button
            sprite = new Sprite(47f / 132f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x00000066);
            s.closeView = new StaticSprite()
                    .viewport(s.videoView)
                    .metrics(new UIElement.Metrics().scale(0.19f).anchorBottom().anchorRight().move(0, +0.03f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            new FadeAnim(0.25f, LinearGraph.zeroToOne),
                            null,
                            null
                    )
                    ;
            new TextBox()
                    .viewport(s.closeView)
                    .text(new Text()
                            .font(skipFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(sprite.length, -6f)
                            .text("Skip Ad")
                    )
                    .animation(
                            new FadeAnim(0.25f, LinearGraph.zeroToOne),
                            null,
                            null
                    )
                    .attach();

            // Input view
            s.tapView = new Clickable()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .attach();

            s.introSound = Sound.load("sounds/flapee/ad-jingle.ogg");
        }

        // Commit
        screen.setInternal(s);
    }


    public Animation createFullscreenAnim(Sprite videoMesh) {
        float scale = 1f / videoMesh.length;
        Animation startAnim = new CompoundAnim(0.3f, new Animation[] {
                new RotateAnim(1f, new QuadraticGraph(0, -90, true)),
                new ScaleAnim(1f, new LinearGraph(1f, scale))
        });
        return startAnim;
    }

    public Animation createWindowedAnim(Sprite videoMesh) {
        float scale = 1f / videoMesh.length;
        if(scale > Globals.LENGTH)
            scale = Globals.LENGTH;
        Animation startAnim = new CompoundAnim(0.3f, new Animation[] {
                new RotateAnim(1f, new QuadraticGraph(-90, 0, true)),
                new ScaleAnim(1f, new LinearGraph(scale, 1f))
        });
        return startAnim;
    }
}
