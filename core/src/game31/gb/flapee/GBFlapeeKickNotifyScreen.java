package game31.gb.flapee;

import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.app.flapee.FlapeeKickNotifyScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.NullAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.animation.VibrateAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

public class GBFlapeeKickNotifyScreen {

    public GBFlapeeKickNotifyScreen(FlapeeKickNotifyScreen screen) {
        FlapeeKickNotifyScreen.Internal s = new FlapeeKickNotifyScreen.Internal();

        Font newHighScoreFont = new Font("gaegu-bold.ttf", 64, Color.CLEAR, 0, Color.BLACK, 4, 4, Color.YELLOW, -4, -15);

        Sprite sprite;

        {
            s.time = 12f;

            s.lostSound = Sound.load("sounds/flapee/lost.ogg");

            s.themeName = "sounds/flapee/theme-demon100.ogg";
            s.themeVolume = 1f;

            s.window = new UIElement.Group()
                    .animation(new SequenceAnim(new Animation[] {
                            new ScissorAnim(1.2f, new Animation[] {
                                    new ScaleAnim(1f, ConstantGraph.one, new CompoundGraph(new Graph[] {
                                            new QuadraticGraph(0f, 0.2f,  0.5f, 0, false),
                                            new QuadraticGraph(0.2f, 0.4f, 0.5f, 0, true),
                                    }))
                            }),
                            new ScissorAnim(2f, new Animation[] {
                                    new ScaleAnim(1f, ConstantGraph.one, new CompoundGraph(new Graph[] {
                                            new ConstantGraph(0.4f, 0.3f),
                                            new QuadraticGraph(0.4f, 1f, 0.7f, 0, false),
                                    }))
                            }),
                    }), null, new ScissorAnim(1.5f, new Animation[] {
                            new ScaleAnim(1f, ConstantGraph.one, QuadraticGraph.oneToZeroInverted)
                    }))
                    .scissor(true)
                    ;


            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff).alpha(0.91f);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_OVERLAY)
                    .attach();



            new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.5f).move(0, -0.12f))
                    .visual(Sprite.load("apps/flapee/kick-podium.png"), SaraRenderer.TARGET_OVERLAY)
                    .animation(
                            new SequenceAnim(new Animation[] {
                                    new NullAnim(3.3f),
                                    new CompoundAnim(1.4f, new Animation[] {
                                            new VibrateAnim(1f, new QuadraticGraph(0.24f, 0f, 1f, 0, true)),
                                            new MoveAnim(1f,
                                                    null,
                                                    new SineGraph(1f, 0.5f, 0, 0.9f, 0)
                                            ),
                                            new RotateAnim(1f, new LinearGraph(0, -156.56f + 360f * 6f))
                                    }),
                                    new CompoundAnim(0.3f, new Animation[] {
                                            new VibrateAnim(1f, new QuadraticGraph(0.24f, 0f, 1f, 0, true)),
                                            new RotateAnim(1f, new ConstantGraph(-156.56f)),
                                    }),
                                    new RotateAnim(100f, new ConstantGraph(-156.56f))
                            }),
                            null, null
                    )
                    .attach();

            new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.21f).move(0, +0.12f))
                    .visual(Sprite.load("apps/flapee/kick-bird.png"), SaraRenderer.TARGET_OVERLAY_INTERACTIVE)
                    .animation(
                            new SequenceAnim(new Animation[] {
                                    new MoveAnim(2.95f, null, new SineGraph(1f, 6f, 0, 0.04f, 0)),
                                    new CompoundAnim(4f, new Animation[] {
                                            new RotateAnim(1f, new QuadraticGraph(0f, 360f * 10f, true)),
                                            new MoveAnim(1f, new QuadraticGraph(0f, +30f, true), new QuadraticGraph(0f, +15f, true))
                                    }),
                                    new ScaleAnim(100f)

                            }),
                            null, null
                    )
                    .attach();

            new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.61f).move(0, +0.12f))
                    .visual(Sprite.load("apps/flapee/kick-impact.png"), SaraRenderer.TARGET_OVERLAY)
                    .animation(
                            new SequenceAnim(new Animation[] {
                                    new ScaleAnim(3.3f),
                                    new CompoundAnim(0.2f, new Animation[] {
                                            new ScaleAnim(1f, QuadraticGraph.zeroToOneInverted),
                                            new FadeAnim(1f, new CompoundGraph(new Graph[] {
                                                    new ConstantGraph(1f, 0.7f),
                                                    new QuadraticGraph(1f, 0f, 0.3f, 0, false),
                                            }))
                                    }),
                                    new ScaleAnim(100f)
                            }),
                            null, null
                    )
                    .attach();

            UIElement.Group legAnchor = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.36f).move(-0.31f, +0.61f))
                    .length(0)
                    .animation(new SequenceAnim(new Animation[] {
                                    new RotateAnim(0.5f, new QuadraticGraph(-10f, 0f, false)),
                                    new RotateAnim(0.5f, new QuadraticGraph(0f, +10f, true)),
                                    new RotateAnim(0.5f, new QuadraticGraph(+10f, 0f, false)),
                                    new RotateAnim(0.5f, new QuadraticGraph(0f, -10f, true)),

                                    new RotateAnim(0.25f, new QuadraticGraph(-10f, 0f, false)),
                                    new RotateAnim(1f, new QuadraticGraph(0f, +30f, true)),

                                    new RotateAnim(0.2f, new QuadraticGraph(+30f, -120f, true)),
                                    new RotateAnim(100f, new ConstantGraph(-120f)),


                            }),
                            null,
                            null
                    )
                    .attach();

            new StaticSprite()
                    .viewport(legAnchor)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(Sprite.load("apps/flapee/kick-foot.png"), SaraRenderer.TARGET_OVERLAY)
                    .animation(new SequenceAnim(new Animation[] {
                            new ScaleAnim(0.3f),
                            new ScaleAnim(1f, ScaleAnim.Location.TOP, QuadraticGraph.zeroToOneInverted),
                            new NullAnim(1.8f),
                            new CompoundAnim(2f, new Animation[] {
                                    new FadeAnim(1f, QuadraticGraph.oneToZero),
                                    new MoveAnim(1f, new QuadraticGraph(0f, 2f, true), new QuadraticGraph(0f, 5f, true))
                            }),
                            new ScaleAnim(100f)

                    }), null, null)
                    .attach();

            new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.9f).move(0, -0.40f))
                    .text(new Text()
                            .font(newHighScoreFont, SaraRenderer.TARGET_OVERLAY_TEXT)
                            .position(0.2f, 9f)
                            .text("You lost your #1 spot!")
                    )
                    .animation(
                            new ScaleAnim(2.95f),
                            new SequenceAnim(new Animation[] {
                                    new ScaleAnim(0.15f, new ConstantGraph(1.02f)),
                                    new NullAnim(0.15f)
                            }),
                            null
                    )
                    .attach();

            screen.viewport.detachChilds();
            screen.elements.clear();
            screen.elements.addAll(new UIElement[] {
                    s.window,
            });
        }


        screen.setInternal(s);

    }
}
