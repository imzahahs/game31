package game31.gb;

import game31.Globals;
import game31.Screen;
import game31.renderer.SaraRenderer;
import game31.renderer.ScreenBlurMaterial;
import game31.renderer.ScreenMaterial;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.NullAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.SequenceAnim;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.VibrationGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.ui.UIElement;

public class GBScreen implements Screen.BuilderSource {

    public GBScreen(Screen screen) {
        Screen.Internal s = new Screen.Internal();

        // TODO: kaigan
        final int buildFontSize = 20;
        Font buildFont = new Font("opensans-bold.ttf", buildFontSize);

        {
            s.window = new UIElement.Group();

            // TODO
//            new TextBox() {
//
//                @Override
//                protected void render(Universe v, float r, float renderTime) {
//                    text().length = (float)buildFontSize / (float)Sys.system.getWidth();
//                    text().wrapChars = Sys.system.getWidth() / buildFontSize;       // resolution dependent
//
//                    super.render(v, r, renderTime);
//                }
//            }.viewport(s.window)
//                    .metrics(new UIElement.Metrics().anchorBottom().anchorRight().move(-0.02f, +0.02f))
//                    .text(new Text()
//                            .font(buildFont, SaraRenderer.TARGET_CONSOLE)
//                            .position(0.005f, 0)
//                            .bottomRight()
//                            .text("Build " + Globals.buildNumber)
//                    )
//                    .attach();

            float glowSampleWidth = 0.5f;
            float borderWidth = 0.04f;          // 0.075f
            float glowAlpha = 0.7f;             // 0.5f
            float verticalGlowAlpha = 0.4f;             // 0.5f
            float verticalGlowSampleSize = 0.02f;

            s.verticalGlowScaleY = 8f;
            s.verticalGlowMinLength = 0.07f;

            Sprite sprite = new Sprite(new ScreenBlurMaterial("system/glow-left-alpha.png"));
            sprite.crop(16f / glowSampleWidth, -1f, 0f);
            ColorAttribute.of(sprite).alpha(glowAlpha);

            s.leftGlow = sprite;

            sprite = new Sprite(new ScreenBlurMaterial("system/glow-right-alpha.png"));
            sprite.crop(16f / glowSampleWidth, +1f, 0f);
            ColorAttribute.of(sprite).alpha(glowAlpha);

            s.rightGlow = sprite;

            sprite = new Sprite(new ScreenBlurMaterial("system/square.png"));
//        sprite.crop(0.05f , 0, ((sprite.getLength() - 0.2f) / sprite.getLength()));
            sprite.crop(verticalGlowSampleSize, 0, +1);
            ColorAttribute.of(sprite).alpha(verticalGlowAlpha);

            s.topGlow = sprite;

            sprite = new Sprite(new ScreenBlurMaterial("system/square.png"));
//        sprite.crop(0.05f , 0, (0.2f - sprite.getLength()) / sprite.getLength());
            sprite.crop(verticalGlowSampleSize , 0, -1);
            ColorAttribute.of(sprite).alpha(verticalGlowAlpha);

            s.bottomGlow = sprite;

            s.verticalGlowShadow = new Sprite(s.bottomGlow.getLength(), Material.load("system/gradient.png"));
            ColorAttribute.of(s.verticalGlowShadow).set(0x000000ff);

            s.border = new Sprite(16f / borderWidth, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(s.border).set(0x000000ff).alpha(0.2f);

            // Bg
            s.bgLeft = Sprite.load("system/screenbg-left.png");
            s.bgRight = Sprite.load("system/screenbg-right.png");

            s.bgRenderMinSize = 0.95f;


            s.bgAnimIdle = new SequenceAnim(
                    new NullAnim(1000f),
                    new SequenceAnim(
                            new ColorAnim(0.4f, 0.7f, 0.7f, 0.7f, 1f, true),
                            new NullAnim(0.1f),
                            new ColorAnim(0.2f, 0.4f, 0.4f, 0.4f, 1f, true)
                    ),
                    new NullAnim(1600f),
                    new FadeAnim(3f, new VibrationGraph(1f, 0.5f, 0.5f)),
                    new NullAnim(1200f),
                    new SequenceAnim(
                            new FadeAnim(5f, new LinearGraph(1f, 0.2f)),
                            new ColorAnim(0.3f, 2f, 2f, 2f, 1f, true)
                    ),
                    new NullAnim(900f),
                    new SequenceAnim(
                            new ColorAnim(0.6f, 0.4f, 0.4f, 0.4f, 1f, true),
                            new NullAnim(0.4f),
                            new ColorAnim(0.3f, 0, 0, 0, 1f, true),
                            new ColorAnim(0.3f, 1.5f, 1.5f, 1.5f, 1f, true)
                    ),
                    new NullAnim(800f),
                    new ColorAnim(0.3f, 0.2f, 0.2f, 0.2f, 1f, true),
                    new NullAnim(1000f),
                    new ColorAnim(0.3f, 0.2f, 0.2f, 0.2f, 1f, true),
                    new NullAnim(700f),
                    new FadeAnim(1f, new VibrationGraph(1f, 0.5f, 0.5f)),
                    new NullAnim(1000f),
                    new SequenceAnim(
                            new ColorAnim(0.4f, 0.3f, 0.3f, 0.3f, 1f, true),
                            new NullAnim(0.3f),
                            new ColorAnim(0.4f, 0.7f, 0.7f, 0.7f, 1f, true),
                            new ColorAnim(0.2f, 1.7f, 1.7f, 1.7f, 1f, true),
                            new FadeAnim(0.5f, LinearGraph.zeroToOne)
                    ),
                    new NullAnim(1000f),
                    new SequenceAnim(
                            new ColorAnim(0.4f, 0.7f, 0.7f, 0.7f, 1f, true),
                            new NullAnim(0.3f),
                            new ColorAnim(0.25f, 0.4f, 0.4f, 0.4f, 1f, true),
                            new ColorAnim(0.4f, 0, 0, 0, 1f, true),
                            new FadeAnim(3f, LinearGraph.zeroToOne)
                    ),
                    new NullAnim(1600f),
                    new FadeAnim(2f, new VibrationGraph(1f, 0.5f, 0.5f))
            );

            s.defaultScreen = new Sprite(Globals.LENGTH, new ScreenMaterial());
        }

        // TODO Liquid Crystal
//        SaraRenderer.renderer.requestEffectBuffer(Globals.r_liquidCrystalEffectResolution);
//        LiquidCrystalGeneratorMaterial generator = new LiquidCrystalGeneratorMaterial();
//        screen.effectBufferGenerator = new Sprite(Globals.LENGTH, generator);
//        s.defaultScreen = new Sprite(Globals.LENGTH, new ScreenMaterial(SaraRenderer.RENDER_EFFECT1));

        // Commit
        screen.setInternal(s);

    }

    public Animation createEnterFullscreenAnim(float scale) {
        Animation startAnim = new CompoundAnim(0.3f,
                new RotateAnim(1f, new QuadraticGraph(0, 90, true)),
                new ScaleAnim(1f, new LinearGraph(1f, scale))
        );
        return startAnim;
    }

    public Animation createExitFullscreenAnim(float scale) {
        Animation startAnim = new CompoundAnim(0.3f,
                new RotateAnim(1f, new QuadraticGraph(90, 0, true)),
                new ScaleAnim(1f, new LinearGraph(scale, 1f))
        );
        return startAnim;
    }
}
