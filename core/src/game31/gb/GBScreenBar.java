package game31.gb;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

import java.text.SimpleDateFormat;
import java.util.Locale;

import game31.ScreenBar;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SetDistributedSelector;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 5/13/2017.
 */

public class GBScreenBar {

    public GBScreenBar(ScreenBar bar) {

        ScreenBar.Internal s = new ScreenBar.Internal();

        Sprite sprite;

        Font titleBigFont = new Font("opensans-semibold.ttf", 48);
        Font subtitleFont = new Font("opensans-light.ttf", 32);

        {
            sprite = new Sprite(373f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff).alpha(0.5f);

            s.appbarView = new StaticSprite()
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR);

            sprite = new Sprite(373f / 63f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x7341f8ff);

            s.notifyIndicatorView = new StaticSprite()
                    .viewport(s.appbarView)
                    .metrics(new UIElement.Metrics().anchorRight().anchorTop().scale(63f / 2250f))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Line at the bottom
            sprite = new Sprite(4f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xf2f2f2ff);

            new StaticSprite()
                    .viewport(s.appbarView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Title
            s.titleView = new TextBox()
                    .viewport(s.appbarView)
                    .metrics(new UIElement.Metrics().scale(1442f / 2250f).move(-291f / 2250f, 0))
                    .text(new Text()
                            .font(titleBigFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(124f / 1442f, -11f)
                            .centerLeft()
                            .text("Gallery")
                    )
                    .animation(null, new ColorAnim(0xf4f9fcff), null)
                    .attach();

            // Extended titles
            s.extTitleView = new TextBox()
                    .viewport(s.appbarView)
                    .metrics(new UIElement.Metrics().scale(1442f / 2250f).move(-291f / 2250f, +55f / 2250f))
                    .text(new Text()
                            .font(titleBigFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(124f / 1442f, -11f)
                            .centerLeft()
                            .text("10:30 AM")
                    )
                    .animation(null, new ColorAnim(0xf4f9fcff), null)
                    ;
            s.extSubtitleView = new TextBox()
                    .viewport(s.appbarView)
                    .metrics(new UIElement.Metrics().scale(1442f / 2250f).move(-291f / 2250f, -85f / 2250f))
                    .text(new Text()
                            .font(subtitleFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(70f / 1442f, -16f)
                            .centerLeft()
                            .text("Tuesday, 2 Feb")
                    )
                    .animation(null, new ColorAnim(0xf4f9fcff), null)
                    ;

            // Status
            s.statusWithTextMetrics = new UIElement.Metrics().move(0, +0.028f);      // up a bit
            s.statusWithoutTextMetrics = new UIElement.Metrics();           // nothing

            s.statusGroup = new UIElement.Group()
                    .viewport(s.appbarView)
                    .metrics(s.statusWithTextMetrics)
                    .length(0)
                    .attach();

            s.batterySprites = new Sprite[] {
                    Sprite.load("system/stat-batt5.png"),
                    Sprite.load("system/stat-batt4.png"),
                    Sprite.load("system/stat-batt3.png"),
                    Sprite.load("system/stat-batt2.png"),
                    Sprite.load("system/stat-batt1.png"),
                    Sprite.load("system/stat-batt0.png")
            };
            s.batteryLevels = new float[] {
                    0.80f,
                    0.60f,
                    0.40f,
                    0.20f,
                    0.10f,
                    0.05f
            };

            s.batteryView = new StaticSprite()
                    .viewport(s.statusGroup)
                    .metrics(new UIElement.Metrics().scale(140f / 2250f).move(+898f / 2250f, 0))
                    .visual(s.batterySprites[0], SaraRenderer.TARGET_APPBAR)
                    .animation(null, new ColorAnim(0xe4e4e4ff), null)
                    .attach();

            s.wifiSprites = new SetDistributedSelector<Sprite>(new Sprite[] {
                    Sprite.load("system/stat-wifi3.png"),
                    Sprite.load("system/stat-wifi4.png"),
                    Sprite.load("system/stat-wifi3.png"),
                    Sprite.load("system/stat-wifi2.png"),
                    Sprite.load("system/stat-wifi1.png"),
                    Sprite.load("system/stat-wifi2.png"),
                    Sprite.load("system/stat-wifi0.png"),
                    Sprite.load("system/stat-wifi2.png"),
                    Sprite.load("system/stat-wifi3.png"),
                    Sprite.load("system/stat-wifi4.png"),
            }, new float[] {
                    16.666f,            // offset a little so that both wifi and cell doesnt update at the same time
                    10f,
                    14f,
                    10f,
                    6f,
                    4f,
                    2f,
                    7f,
                    10f,
                    14f,
            });

            s.wifiView = new StaticSprite()
                    .viewport(s.statusGroup)
                    .metrics(new UIElement.Metrics().scale(100f / 2250f). move(+743f / 2250f, 0))
                    .visual(s.wifiSprites.set[0], SaraRenderer.TARGET_APPBAR)
                    .animation(null, new ColorAnim(0xe4e4e4ff), null)
                    .attach();

            s.cellSprites = new SetDistributedSelector<Sprite>(new Sprite[] {
                    Sprite.load("system/stat-cell4.png"),
                    Sprite.load("system/stat-cell5.png"),
                    Sprite.load("system/stat-cell4.png"),
                    Sprite.load("system/stat-cell3.png"),
                    Sprite.load("system/stat-cell1.png"),
                    Sprite.load("system/stat-cell3.png"),
                    Sprite.load("system/stat-cell2.png"),
                    Sprite.load("system/stat-cell0.png"),
                    Sprite.load("system/stat-cell2.png"),
            }, new float[] {
                    15f,
                    12f,
                    6f,
                    4f,
                    4f,
                    20f,
                    12f,
                    4f,
                    25f,
            });

            s.cellView = new StaticSprite()
                    .viewport(s.statusGroup)
                    .metrics(new UIElement.Metrics().scale(195f / 2250f).move(+564f / 2250f, 0))
                    .visual(s.cellSprites.set[0], SaraRenderer.TARGET_APPBAR)
                    .animation(null, new ColorAnim(0xe4e4e4ff), null)
                    .attach();

            s.timeView = new TextBox()
                    .viewport(s.appbarView)
                    .metrics(new UIElement.Metrics().scale(490f / 2250f).move(+720f / 2250f, -75f / 2250f))
                    .text(new Text()
                            .font(subtitleFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(80f / 490f, -5f)
                            .centerRight()
                            .text("10:30 PM")
                    )
                    .attach();

            s.timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
            s.dateFormat = new SimpleDateFormat("EEE, d MMMM", Locale.US);

        }


        {
            // Bottom
            sprite = new Sprite(360f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff).alpha(0.5f);

            s.bottomBarView = new StaticSprite()
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
//                    .viewport(bar.viewport).attach()
                    ;

            // Line at the top
            sprite = new Sprite(4f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x121312ff);

            new StaticSprite()
                    .viewport(s.bottomBarView)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Gradient
            sprite = new Sprite(360f / 2250f, Material.load("system/gradient-big.png"));
            ColorAttribute.of(sprite).set(0x0e162dff).alpha(0.39f);
            new StaticSprite()
                    .viewport(s.bottomBarView)
                    .metrics(new UIElement.Metrics().scale(1, -1))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR)
                    .attach();


            float navSize = 0.10f;
            float navX = 0.33f;
            float navY = +0.01f;

            Animation pressedAnim = new ScaleAnim(0.15f, new LinearGraph(1f, 0.9f));
            Animation releasedAnim = new ScaleAnim(0.1f, new LinearGraph(0.9f, 1f));

            if(Gdx.app.getType() == Application.ApplicationType.iOS) {
                // TODO: remove when not needed

                Font buttonFont = new Font("opensans-light.ttf", 40);

                s.bottomBackButton = new Clickable()
                        .viewport(s.bottomBarView)
                        .metrics(new UIElement.Metrics().scale(navSize).move(-navX, navY))
                        .visuals(Sprite.load("system/nav-back.png"), SaraRenderer.TARGET_APPBAR)
                        .animation(null, null, pressedAnim, releasedAnim, null)
                        .inputPadding(1.3f, 0.18f, 1.3f, 0.25f)
                        .sound(Sound.load("sounds/general_back.ogg"))
                        .text(new Text()
                                .font(buttonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                                .position(+1.15f, -0.07f, 1.4f, 0.2f, 0)
                                .centerLeft()
                                .text("Back")
                        )
                        .attach();

                s.bottomHomeButton = new Clickable()
                        .viewport(s.bottomBarView)
                        .metrics(new UIElement.Metrics().scale(navSize).move(0, navY))
                        .visuals(Sprite.load("system/nav-home.png"), SaraRenderer.TARGET_APPBAR)
                        .animation(null, new FadeAnim(0f), pressedAnim, releasedAnim, null)
                        .inputPadding(1.3f, 0.18f, 1.3f, 0.25f)
                        .attach();

                s.bottomIrisButton = new Clickable()
                        .viewport(s.bottomBarView)
                        .metrics(new UIElement.Metrics().scale(navSize).move(+navX + 0.01f, navY))
                        .visuals(Sprite.load("system/nav-iris.png"), SaraRenderer.TARGET_APPBAR)
                        .animation(null, null, pressedAnim, releasedAnim, null)
                        .inputPadding(1.3f, 0.18f, 1.3f, 0.25f)
                        .text(new Text()
                                .font(buttonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                                .position(-1.3f, -0.07f, 1.4f, 0.2f, 0)
                                .centerRight()
                                .text("Memos")
                        )
                        .attach();



                sprite = Sprite.load("system/nav-glow.png");
                ColorAttribute.of(sprite).alpha(0.15f);

                s.bottomPressed = new StaticSprite()
                        .metrics(new UIElement.Metrics().scale(1.2f, 3f).anchor(0, -0.11f))
                        .visual(sprite, SaraRenderer.TARGET_APPBAR)
                        .animation(new CompoundAnim(0.15f, new Animation[] {
                                        new FadeAnim(LinearGraph.zeroToOne),
                                        new ScaleAnim(1f, new LinearGraph(0.3f, 1f), ConstantGraph.one)
                                }),
                                new FadeAnim(2f, new SineGraph(1f, 1f, 0, 0.2f, 0.8f)),
                                new CompoundAnim(0.15f, new Animation[] {
                                        new FadeAnim(LinearGraph.oneToZero),
                                        new ScaleAnim(1f, new LinearGraph(1f, 1.3f), ConstantGraph.one)
                                }))
                ;

                Animation indicatorAnim = new CompoundAnim(0.5f, new Animation[] {
                        new FadeAnim(new CompoundGraph(new Graph[] {
                                new LinearGraph(0f, 1f, 0.5f),
                                new LinearGraph(1f, 0f, 0.5f),
                        })),
                        new ScaleAnim(1f, new QuadraticGraph(0.3f, 1.3f, true), ConstantGraph.one)
                });
                Animation indicatorEmptyAnim = new ScaleAnim(0.3f);

                s.bottomIrisIndicatorView = new StaticSprite()
                        .viewport(s.bottomIrisButton)
                        .metrics(new UIElement.Metrics().scale(1.2f, 3f))
                        .visual(sprite, SaraRenderer.TARGET_APPBAR)
                        .animation(
                                null,
                                null,
                                new SequenceAnim(new Animation[] {
                                        indicatorAnim,
                                        indicatorEmptyAnim,
                                        indicatorAnim,
                                        indicatorEmptyAnim,
                                        indicatorAnim,
                                        indicatorEmptyAnim,
                                })
                        );
            }
            else {
                s.bottomBackButton = new Clickable()
                        .viewport(s.bottomBarView)
                        .metrics(new UIElement.Metrics().scale(navSize).move(-navX, navY))
                        .visuals(Sprite.load("system/nav-back.png"), SaraRenderer.TARGET_APPBAR)
                        .animation(null, null, pressedAnim, releasedAnim, null)
                        .inputPadding(1.3f, 0.18f, 1.3f, 0.25f)
                        .sound(Sound.load("sounds/general_back.ogg"))
                        .attach();

                s.bottomHomeButton = new Clickable()
                        .viewport(s.bottomBarView)
                        .metrics(new UIElement.Metrics().scale(navSize).move(0, navY))
                        .visuals(Sprite.load("system/nav-home.png"), SaraRenderer.TARGET_APPBAR)
                        .animation(null, null, pressedAnim, releasedAnim, null)
                        .inputPadding(1.3f, 0.18f, 1.3f, 0.25f)
                        .attach();

                s.bottomIrisButton = new Clickable()
                        .viewport(s.bottomBarView)
                        .metrics(new UIElement.Metrics().scale(navSize).move(+navX, navY))
                        .visuals(Sprite.load("system/nav-iris.png"), SaraRenderer.TARGET_APPBAR)
                        .animation(null, null, pressedAnim, releasedAnim, null)
                        .inputPadding(1.3f, 0.18f, 1.3f, 0.25f)
                        .attach();


                sprite = Sprite.load("system/nav-glow.png");
                ColorAttribute.of(sprite).alpha(0.15f);

                s.bottomPressed = new StaticSprite()
                        .metrics(new UIElement.Metrics().scale(4f).anchor(0, -0.11f))
                        .visual(sprite, SaraRenderer.TARGET_APPBAR)
                        .animation(new CompoundAnim(0.15f, new Animation[]{
                                        new FadeAnim(LinearGraph.zeroToOne),
                                        new ScaleAnim(1f, new LinearGraph(0.3f, 1f), ConstantGraph.one)
                                }),
                                new FadeAnim(2f, new SineGraph(1f, 1f, 0, 0.2f, 0.8f)),
                                new CompoundAnim(0.15f, new Animation[]{
                                        new FadeAnim(LinearGraph.oneToZero),
                                        new ScaleAnim(1f, new LinearGraph(1f, 1.3f), ConstantGraph.one)
                                }))
                ;

                Animation indicatorAnim = new CompoundAnim(0.5f, new Animation[]{
                        new FadeAnim(new CompoundGraph(new Graph[]{
                                new LinearGraph(0f, 1f, 0.5f),
                                new LinearGraph(1f, 0f, 0.5f),
                        })),
                        new ScaleAnim(1f, new QuadraticGraph(0.3f, 1.3f, true), ConstantGraph.one)
                });
                Animation indicatorEmptyAnim = new ScaleAnim(0.3f);

                s.bottomIrisIndicatorView = new StaticSprite()
                        .viewport(s.bottomIrisButton)
                        .metrics(new UIElement.Metrics().scale(4f))
                        .visual(sprite, SaraRenderer.TARGET_APPBAR)
                        .animation(
                                null,
                                null,
                                new SequenceAnim(new Animation[]{
                                        indicatorAnim,
                                        indicatorEmptyAnim,
                                        indicatorAnim,
                                        indicatorEmptyAnim,
                                        indicatorAnim,
                                        indicatorEmptyAnim,
                                })
                        );
            }
        }

        {
            // Shadows
            // Top
            sprite = new Sprite((373f * 0.9f) / 2550f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff).alpha(1);

            StaticSprite topShadow = new StaticSprite()
                    .viewport(s.appbarView)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG);

            Sprite gradient = new Sprite((373f * 0.2f) / 2550f, Material.load("system/gradient-thick.png"));

            StaticSprite topGradient = new StaticSprite()
                    .viewport(topShadow)
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1))
                    .visual(gradient, SaraRenderer.TARGET_APPBAR_BG);

            // Bottom
            sprite = new Sprite((360f * 0.6f) / 2550f, SaraRenderer.renderer.coloredMaterial);

            StaticSprite bottomShadow = new StaticSprite()
                    .viewport(s.bottomBarView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG);

            gradient = new Sprite((360f * 0.5f) / 2550f, Material.load("system/gradient-thick.png"));

            StaticSprite bottomGradient = new StaticSprite()
                    .viewport(bottomShadow)
                    .metrics(new UIElement.Metrics().anchorTop().scale(1, -1))
                    .visual(gradient, SaraRenderer.TARGET_APPBAR_BG);

            s.topShadowViews = new StaticSprite[] {
                    topShadow,
                    topGradient,
            };
            s.bottomShadowViews = new StaticSprite[] {
                    bottomShadow,
                    bottomGradient
            };
        }


        bar.setInternal(s);
    }

}
