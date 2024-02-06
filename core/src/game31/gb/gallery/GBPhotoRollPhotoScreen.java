package game31.gb.gallery;

import game31.Globals;
import game31.ScreenBar;
import game31.app.gallery.PhotoRollPhotoScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.NullAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 24/7/2016.
 */
public class GBPhotoRollPhotoScreen implements PhotoRollPhotoScreen.InterfaceSource {

    public GBPhotoRollPhotoScreen(PhotoRollPhotoScreen screen) {
        PhotoRollPhotoScreen.Internal s = new PhotoRollPhotoScreen.Internal();

        Animation buttonPressedAnim = new ColorAnim(1f, new ConstantGraph(0.75f), ConstantGraph.one);

        Font regularFont = new Font("opensans-regular.ttf", 32);
        Font shareFont = new Font("opensans-semibold.ttf", 32);


        PatchedSprite patch;


        {
            // Window

            s.window = new UIElement.Group();

            Sprite sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x111111ff);

            s.bgView = new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();

            s.maxZoom = 20f;
            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .scrollable(true, true)
                    .maxZoom(s.maxZoom)
                    .minTouchMoveDistance(Globals.minTouchMoveDistance)
                    .selectiveRendering(true, false)
                    .attach();
            s.gravityThreshold = 0.45f;     // previously 0.51f
            s.flipDistance = 0.99f;

            s.photoView = new StaticSprite()
                    .viewport(s.surface)
                    .metrics(new UIElement.Metrics())
                    .visual(null, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();

            s.leftPhotoView = new StaticSprite()
                    .viewport(s.surface)
                    .metrics(new UIElement.Metrics().move(-1f, 0))
                    .visual(null, SaraRenderer.TARGET_BG_SHADOWS)
                    ;

            s.rightPhotoView = new StaticSprite()
                    .viewport(s.surface)
                    .metrics(new UIElement.Metrics().move(+1f, 0))
                    .visual(null, SaraRenderer.TARGET_BG_SHADOWS)
                    ;

            // Top tab

            s.bars = new ScreenBar();
            s.bars.attach(screen);
            s.bars.showNavbar(true, true, true);
            // Remove navbar bg
            ColorAttribute.of(s.bars.navbar().visual()).alpha(0);
            s.bars.navbar().iterate(null, StaticSprite.class, false, null).detach();



            {
                // Send button
                patch = PatchedSprite.create("system/rounded.png", 229f / 769f, 0.1f);
                ColorAttribute.of(patch).set(0x16a85dff);

                s.sendButton = new Clickable()
                        .viewport(s.bars.navbar())
                        .metrics(new UIElement.Metrics().scale(769f / 2250f).anchorTop().pan(0, +1).move(0, +0.025f))
                        .visuals(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .animation(
                                new CompoundAnim(0.2f, new Animation[] {
                                        new MoveAnim(1, null, new QuadraticGraph(-patch.getLength(), 0, true)),
                                        new ScissorAnim(1, new Animation[] {
                                                new MoveAnim(1, null, new QuadraticGraph(+patch.getLength(), 0, true))
                                        })
                                }),
                                new ColorAnim(1f, new CompoundGraph(new Graph[] {
                                        new ConstantGraph(0.8f, 0.5f),
                                        new ConstantGraph(1.0f, 0.5f)
                                }), null),
                                new ScaleAnim(0.12f, new QuadraticGraph(1f, 1.1f, -0.5f, true)),
                                new ScaleAnim(0.12f, new QuadraticGraph(1.1f, 1.0f, false)),
                                null
                        )
                        .text(new Text()
                                .font(shareFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                                .position(patch.length, -7f)
                                .text("Send")
                        )
                        ;
            }

            {
                // Corruption
                s.corruptionView = new StaticSprite()
                        .metrics(new UIElement.Metrics())
                        .target(SaraRenderer.TARGET_INTERACTIVE)
                ;

                // Icon
                new StaticSprite()
                        .viewport(s.corruptionView)
                        .metrics(new UIElement.Metrics().scale(0.2f).scaleIndex(0))
                        .visual(Sprite.load("system/fix.png"), SaraRenderer.TARGET_INTERACTIVE)
                        .animation(
                                null,
                                new SequenceAnim(new Animation[] {
                                        new NullAnim(0.5f),
                                        new FadeAnim(0.5f, new ConstantGraph(0.5f))
                                }),
                                null
                        )
                        .attach();

                patch = PatchedSprite.create("system/rounded.png", 229f / 769f, 0.1f);
                ColorAttribute.of(patch).set(0x16a85dff);
                s.restoreButton = new Clickable()
                        .viewport(s.bars.navbar())
                        .metrics(new UIElement.Metrics().scale(769f / 2250f).anchorTop().pan(0, +1).move(0, +0.025f))
                        .visuals(patch, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .animation(
                                new CompoundAnim(0.2f, new Animation[] {
                                        new MoveAnim(1, null, new QuadraticGraph(-patch.getLength(), 0, true)),
                                        new ScissorAnim(1, new Animation[] {
                                                new MoveAnim(1, null, new QuadraticGraph(+patch.getLength(), 0, true))
                                        })
                                }),
                                new ColorAnim(1f, new CompoundGraph(new Graph[] {
                                        new ConstantGraph(0.8f, 0.5f),
                                        new ConstantGraph(1.0f, 0.5f)
                                }), null),
                                new ScaleAnim(0.12f, new QuadraticGraph(1f, 1.1f, -0.5f, true)),
                                new ScaleAnim(0.12f, new QuadraticGraph(1.1f, 1.0f, false)),
                                new CompoundAnim(0.2f, new Animation[] {
                                        new MoveAnim(1, null, new QuadraticGraph(0, -patch.getLength(), true)),
                                        new ScissorAnim(1, new Animation[] {
                                                new MoveAnim(1, null, new QuadraticGraph(0, +patch.getLength(), true))
                                        })
                                })
                        )
                        .text(new Text()
                                .font(shareFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                                .position(patch.length, -7f)
                                .text("Restore")
                        )
                ;
            }


            // Navigation buttons
            {
                float x = 0.33f;
                float arrowScale = 0.06f;
                float y = +0.045f;
                sprite = Sprite.load("apps/gallery/right.png").instantiate();
                ColorAttribute.of(sprite).set(0xffffffff);
                s.leftButton = new Clickable()
                        .viewport(s.bars.navbar())
                        .metrics(new UIElement.Metrics().anchorTop().pan(0, +1).move(-x, y).scale(-arrowScale, arrowScale))
                        .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .animation(null, null, buttonPressedAnim, null, null)
                        .inputPadding(2f, 1f, 2f, 1f)
                        .attach();

                s.rightButton = new Clickable()
                        .viewport(s.bars.navbar())
                        .metrics(new UIElement.Metrics().anchorTop().pan(0, +1).move(+x, y).scale(arrowScale))
                        .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                        .animation(null, null, buttonPressedAnim, null, null)
                        .inputPadding(2f, 1f, 2f, 1f)
                        .attach();

                s.controlDisabledAnim = new ColorAnim(0x555555ff);

                s.nextSound = Sound.load("sounds/general_forward.ogg");
                s.previousSound = Sound.load("sounds/general_back.ogg");

            }


            // Bottom name view
            s.captionView = new TextBox()
                    .viewport(s.bars.appbar())
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1).move(0, -0.01f))
                    .text(new Text()
                            .font(regularFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.9f, 100f / 333f, 22f)
                            .ellipsize(3)
                            .topCenter()
                            .text("IMAGE3142")
                    )
                    .attach();
            s.captionFullscreenAnim = new FadeAnim(0.3f, LinearGraph.oneToZero);
            s.captionWindowedAnim = new FadeAnim(0.3f, LinearGraph.zeroToOne);

            // Fullscreen animations
            s.topGroupFullscreenAnim = new MoveAnim(
                    0.3f,
                    null,
                    new QuadraticGraph(0f, +0.3f, false)
            );
            s.topGroupWindowedAnim = new MoveAnim(
                    0.3f,
                    null,
                    new QuadraticGraph(+0.3f, 0f, true)
            );
            s.bottomGroupFullscreenAnim = new MoveAnim(
                    0.3f,
                    null,
                    new QuadraticGraph(0f, -0.5f, false)
            );
            s.bottomGroupWindowedAnim = new MoveAnim(
                    0.3f,
                    null,
                    new QuadraticGraph(-0.5f, 0f, true)
            );
            s.bgFullscreenAnim = new ColorAnim(0.3f, QuadraticGraph.oneToZero, null);
            s.bgWindowedAnim = new ColorAnim(0.3f, QuadraticGraph.zeroToOneInverted, null);

            s.fullscreenZoomThreshold = 1.1f;

        }

        // Commit
        screen.setInternal(s);
    }

    public Mesh createCorruptionMesh(float length) {
        PatchedSprite patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", length, 0f);
        ColorAttribute.of(patch).set(0x000000ff);
        return patch;
    }
}
