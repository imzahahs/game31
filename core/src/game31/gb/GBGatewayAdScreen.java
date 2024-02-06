package game31.gb;

import java.text.SimpleDateFormat;
import java.util.Locale;

import game31.Globals;
import game31.app.GatewayAdScreen;
import game31.glitch.MpegGlitch;
import game31.renderer.SaraRenderer;
import game31.renderer.ScreenMaterial;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.CircularSprite;
import sengine.graphics2d.Font;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

public class GBGatewayAdScreen implements GatewayAdScreen.BuilderSource {

    public GBGatewayAdScreen(GatewayAdScreen screen)  {
        GatewayAdScreen.Internal s = new GatewayAdScreen.Internal();

        Font logoTitleFont = new Font("opensans-bold.ttf", 48, 0xffffffff);
        Font timerFont = new Font("opensans-semibold.ttf", 32, 0xffffffff);
        Font skipFont = new Font("opensans-bold.ttf", 48, 0xffffffaa);
        Font titleFont = new Font("opensans-regular.ttf", 48, 0xffffffff);

        Font liveFont = new Font("opensans-bold.ttf", 32, 0xffffffff);

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

            // Live group
            s.liveGroup = new UIElement.Group()
                    .viewport(s.videoView)
                    .length(-1)
                    .attach();

            float lineWidth = 0.015f;
            float lineSize = 0.15f;
            float padding = 0.035f;

            Sprite horizontalLine = new Sprite(lineWidth, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(horizontalLine).set(0x999999ff);
            Sprite verticalLine = new Sprite(1f / lineWidth, SaraRenderer.renderer.coloredMaterial);
            verticalLine.copyAttributes(horizontalLine);

            new StaticSprite()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(lineSize).anchorTop().anchorLeft().move(+padding, -padding))
                    .visual(horizontalLine, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            new StaticSprite()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(lineWidth * lineSize).anchorTop().anchorLeft().move(+padding, -padding))
                    .visual(verticalLine, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            new StaticSprite()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(lineSize).anchorTop().anchorRight().move(-padding, -padding))
                    .visual(horizontalLine, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            new StaticSprite()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(lineWidth * lineSize).anchorTop().anchorRight().move(-padding, -padding))
                    .visual(verticalLine, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            new StaticSprite()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(lineSize).anchorBottom().anchorLeft().move(+padding, +padding))
                    .visual(horizontalLine, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            new StaticSprite()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(lineWidth * lineSize).anchorBottom().anchorLeft().move(+padding, +padding))
                    .visual(verticalLine, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();

            // Live indicator
            sprite = Sprite.load("system/circle.png").instantiate();
            ColorAttribute.of(sprite).set(0xff0000ff);
            new StaticSprite()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(0.013f).anchorTop().move(-0.015f, -padding - 0.02f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            null,
                            new FadeAnim(1f, new QuadraticGraph(1f, 0.3f, false)),
                            null
                    )
                    .attach();
            new TextBox()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(0.10f).anchorTop().move(+0.055f, -padding - 0.02f))
                    .text(new Text()
                            .font(liveFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(0.13f, 0)
                            .centerLeft()
                            .text("LIVE")
                    )
                    .attach();
            new TextBox()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(0.19f).anchorTop().anchorTop().anchorLeft().move(+padding + 0.02f, -padding - 0.02f))
                    .text(new Text()
                            .font(liveFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(0.065f, 0)
                            .centerLeft()
                            .text("TEDDYHOME-PC")
                    )
                    .attach();

            // Signal
            sprite = Sprite.load("system/stat-wifi4.png").instantiate();
            new StaticSprite()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(0.02f).anchorBottom().anchorLeft().move(+padding + 0.02f, +padding + 0.02f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();
            new TextBox()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(0.10f).anchorBottom().anchorLeft().move(+padding + 0.02f + 0.029f, +padding + 0.02f))
                    .text(new Text()
                            .font(liveFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(0.12f, 0)
                            .centerLeft()
                            .text("GOOD")
                    )
                    .attach();

            // Timer
            s.liveTimerView = new TextBox()
                    .viewport(s.liveGroup)
                    .metrics(new UIElement.Metrics().scale(0.2f).anchorTop().anchorTop().anchorRight().move(-padding - 0.02f, -padding - 0.02f))
                    .text(new Text()
                            .font(liveFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(0.3f, -13f)
                            .topRight()
                            .text("00:47:32:104\n24 JUN")
                    )
                    .attach();
            s.liveTimerFormat = new SimpleDateFormat("dd MMM yyyy\nHH:mm:ss", Locale.US);
            s.liveTimerFormat.setTimeZone(Globals.grid.timeZone);


            // Logo
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.screenNoiseMaterial);
            ColorAttribute.of(sprite).set(0.1f, 0.1f, 0.1f, 0.3f);
            s.logoGroup = new StaticSprite()
                    .viewport(s.videoView)
                    .metrics(new UIElement.Metrics())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .length(-1)
                    .animation(new ColorAnim(4.5f, new ConstantGraph(0.1f), new QuadraticGraph(1f, 0.3f, true), false), null, null)
                    ; // .attach();
            sprite = Sprite.load("apps/iris/chaos-star.png.NoiseMaterial").instantiate();
            ColorAttribute.of(sprite).set(0xff7777ff);
            s.symbolView = new StaticSprite()
                    .viewport(s.logoGroup)
                    .metrics(new UIElement.Metrics().scale(0.20f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            new FadeAnim(0.3f, new CompoundGraph(new Graph[]{
                                    new ConstantGraph(1.0f, 0.12f),
                                    new ConstantGraph(0f, 0.25f),
                                    new ConstantGraph(1.0f, 0.18f),
                                    new ConstantGraph(0f, 0.1f),
                                    new ConstantGraph(1.0f, 0.15f),
                                    new ConstantGraph(0f, 0.2f)
                            })),
                            null,
                            null
                    )
                    .attach();
            s.codeView = new StaticSprite()
                    .viewport(s.logoGroup)
                    .metrics(new UIElement.Metrics().scale(0.20f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            new FadeAnim(0.3f, new CompoundGraph(new Graph[]{
                                    new ConstantGraph(1.0f, 0.12f),
                                    new ConstantGraph(0f, 0.25f),
                                    new ConstantGraph(1.0f, 0.18f),
                                    new ConstantGraph(0f, 0.1f),
                                    new ConstantGraph(1.0f, 0.15f),
                                    new ConstantGraph(0f, 0.2f)
                            })),
                            new ColorAnim(0xff7777ff),
                            null
                    )
                    .attach();

            s.tSymbolTime = 3f;
            s.tCodeTime = 6.5f;

            s.logoGlitch = new MpegGlitch("sounds/glitch_end_medium.ogg", null);
            s.logoGlitch.setGlitchGraph(null, false, new QuadraticGraph(10f, 0.5f, 1.5f, 0, true));

            s.codeGlitch = new MpegGlitch("sounds/glitch_end_strong.ogg", null);
            s.codeGlitch.setGlitchGraph(null, false, new QuadraticGraph(2.0f, 0.5f, 0.8f, 0, true));

            s.skipFailedGlitch = new MpegGlitch("sounds/glitch_end_strong.ogg", null);
            s.skipFailedGlitch.setGlitchGraph(null, false, new QuadraticGraph(2.0f, 0.5f, 0.8f, 0, true));

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

            s.introSound = Sound.load("sounds/flapee/ad-jingle-demon.ogg");
            s.endSound = Sound.load("sounds/glitch_start_low.ogg");
        }

        // Commit
        screen.setInternal(s);
    }


    public Animation createFullscreenAnim(Sprite videoMesh) {
        float scale = 1f / videoMesh.length;
        if(scale > Globals.LENGTH)
            scale = Globals.LENGTH;
        Animation startAnim = new CompoundAnim(0.3f, new Animation[] {
                new RotateAnim(1f, new QuadraticGraph(0, -90, true)),
                new ScaleAnim(1f, new LinearGraph(1f, scale))
        });
        return startAnim;
    }

}
