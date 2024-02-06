package game31.gb.gallery;

import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.ScreenBar;
import game31.app.gallery.PhotoRollVideoScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 9/8/2016.
 */
public class GBPhotoRollVideoScreen implements PhotoRollVideoScreen.BuilderSource {

    public GBPhotoRollVideoScreen(PhotoRollVideoScreen screen) {
        PhotoRollVideoScreen.Internal s = new PhotoRollVideoScreen.Internal();

        Animation buttonPressedAnim = new ColorAnim(1f, new ConstantGraph(0.75f), ConstantGraph.one);
        Font durationFont = new Font("opensans-light.ttf", 32);

        Font shareFont = new Font("opensans-semibold.ttf", 32);

        Font captionFont = new Font("opensans-regular.ttf", 32, new Color(0, 0, 0, 0.5f), 3, Color.CLEAR, 0, 0, Color.WHITE, -3, 0);


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

            UIElement.Group cancelButtonGroup = new UIElement.Group()
                    .viewport(s.window)
                    .attach();

            s.fullscreenCancelButton = new Clickable()
                    .viewport(cancelButtonGroup)
                    .length(Globals.LENGTH)
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .attach();

            s.bars = new ScreenBar();
            s.bars.attach(screen);
            s.bars.showNavbar(true, true, true);
            // Remove navbar bg
            ColorAttribute.of(s.bars.navbar().visual()).alpha(0);
            s.bars.navbar().iterate(null, StaticSprite.class, false, null).detach();


            // Video view
            s.videoView = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics())
                    .target(SaraRenderer.TARGET_BG)
                    .attach();

            // Caption
            s.captionView = new TextBox()
                    .viewport(s.bars.appbar())
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1))
                    .text(new Text()
                            .font(captionFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.9f, 1f, 22f)
                            .topLeft()
                    )
                    ;
            s.captionFullscreenAnim = new FadeAnim(0.3f, LinearGraph.oneToZero);
            s.captionWindowedAnim = new FadeAnim(0.3f, LinearGraph.zeroToOne);

            // Control group
            sprite = new Sprite(650f / 1080f, Material.load("system/gradient-high.png"));
            ColorAttribute.of(sprite).set(0x000000ff).alpha(0.4f);

            UIElement.Group controlGroup = new UIElement.Group()
                    .viewport(s.bars.navbar())
                    .metrics(new UIElement.Metrics().anchorBottom().move(0, +0.1f))
                    .length(sprite.length)
                    .attach();

            StaticSprite controlBg = new StaticSprite()
                    .viewport(s.bars.navbar())
                    .metrics(new UIElement.Metrics().anchorBottom().scale(1, -1).pan(0, -1))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();



            {
                // Send button
                patch = PatchedSprite.create("system/rounded.png", 229f / 769f, 0.1f);
                ColorAttribute.of(patch).set(0x16a85dff);

                s.sendButton = new Clickable()
                        .viewport(controlGroup)
                        .metrics(new UIElement.Metrics().scale(769f / 2250f).anchorBottom().move(0, +0.21f))
                        .visuals(patch, SaraRenderer.TARGET_INTERACTIVE)
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
                                .font(shareFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                                .position(patch.length, -7f)
                                .text("Send")
                        )
                        ;
            }


            s.playButtonMesh = Sprite.load("apps/gallery/play.png");
            s.pauseButtonMesh = Sprite.load("apps/gallery/pause.png");

            s.playButton = new Clickable()
                    .viewport(controlGroup)
                    .metrics(new UIElement.Metrics().scale(36f / 436f).anchorBottom().move(-0.395f, +0.23f))
                    .visuals(s.playButtonMesh, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .sound(Sound.load("sounds/gallery_playvideo.ogg"))
                    .inputPadding(3f, 2f, 3f, 2f)
                    .attach();

//            Clickable skipLeftButton = new Clickable()
//                    .viewport(controlGroup)
//                    .metrics(new UIElement.Metrics().scale(50f / 436f).anchorBottom().move(-0.25f, +0.18f))
//                    .visuals(Sprite.load("apps/gallery/prev.png"), SaraRenderer.TARGET_INTERACTIVE)
//                    .animation(null, null, buttonPressedAnim, null, null)
//                    .sound(Sound.load("sounds/tick.wav"))
//                    .attach();
//
//            Clickable skipRightButton = new Clickable()
//                    .viewport(controlGroup)
//                    .metrics(new UIElement.Metrics().scale(-50f / 436f, 50f / 436f).anchorBottom().move(+0.25f, +0.18f))
//                    .visuals(Sprite.load("apps/gallery/prev.png"), SaraRenderer.TARGET_INTERACTIVE)
//                    .animation(null, null, buttonPressedAnim, null, null)
//                    .sound(Sound.load("sounds/tick.wav"))
//                    .attach();
//            s.controlDisabledAnim = new ColorAnim(0.75f, 0.75f, 0.75f, 1f);

            Sprite progressBgMat = new Sprite(8f / 547f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(progressBgMat).set(0xccccccff).alpha(0.29f);

            Sprite progressBarMat = progressBgMat.instantiate();
            ColorAttribute.of(progressBarMat).set(0xf74d55ff);

            s.progressBar = new HorizontalProgressBar()
                    .viewport(controlGroup)
                    .metrics(new UIElement.Metrics().scale(1f).anchorBottom().move(0, +0.1f))
                    .visual(progressBgMat, progressBarMat, SaraRenderer.TARGET_INTERACTIVE)
                    .passThroughInput(true)     // Was set to false since seeking dont really work well anymore
                    .progress(1f)
                    .length(progressBgMat.length)
                    .inputPadding(0.05f, 0.05f, 0.05f, 0.05f)
                    .attach();

            float x = 0.395f;

            s.elapsedView = new TextBox()
                    .viewport(controlGroup)
                    .metrics(new UIElement.Metrics().scale(0.14f).anchorBottom().move(-x, +0.14f))
                    .text(new Text()
                            .font(durationFont)
                            .position(16f / 81f, 0)
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .center()
                            .text("5:10")
                    )
                    .attach();

            s.durationView = new TextBox()
                    .viewport(controlGroup)
                    .metrics(new UIElement.Metrics().scale(0.14f).anchorBottom().move(+x, +0.14f))
                    .text(new Text()
                            .font(durationFont)
                            .position(16f / 81f, 0)
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .center()
                            .text("-0:12")
                    )
                    .attach();


            s.fullscreenButton = new Clickable()
                    .viewport(controlGroup)
                    .metrics(new UIElement.Metrics().scale(31f / 436f).anchorBottom().move(+0.395f, +0.23f))
                    .visuals(Sprite.load("apps/gallery/fullscreen.png"), SaraRenderer.TARGET_APPBAR)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .inputPadding(1f, 1f, 1f, 1f)
                    .attach();


            // Fullscreen group animations
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
            s.controlGroupFullscreenAnim = new MoveAnim(
                    0.3f,
                    null,
                    new QuadraticGraph(0f, -0.5f, false)
            );
            s.controlGroupWindowedAnim = new MoveAnim(
                    0.3f,
                    null,
                    new QuadraticGraph(-0.5f, 0f, true)
            );
            s.bgFullscreenAnim = new ColorAnim(0.3f, QuadraticGraph.oneToZero, null);
            s.bgWindowedAnim = new ColorAnim(0.3f, QuadraticGraph.zeroToOneInverted, null);



            s.maximizeSound = Sound.load("sounds/gallery_maximize.ogg");
            s.minimizeSound = Sound.load("sounds/gallery_minimize.ogg");

            s.tStartDelay = 0.0f;

            sprite = Sprite.load("apps/gallery/mic-large.png").instantiate();
            ColorAttribute.of(sprite).set(0x404040ff);

            s.audioIconView = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(479f / 1080f).anchor(0, +0.05f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    ;

            sprite = sprite.instantiate();
            ColorAttribute.of(sprite).set(0xfafafaff);
            StaticSprite audioLoudnessView = new StaticSprite()
                    .viewport(s.audioIconView)
                    .metrics(new UIElement.Metrics().scale(1f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();
            s.audioLevelAnim = new ScissorAnim(1f, new Animation[] {
                    new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, ConstantGraph.one, LinearGraph.zeroToOne)
            }).startAndReset();
            audioLoudnessView.windowAnimation(s.audioLevelAnim, false, true);

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
        Animation startAnim = new CompoundAnim(0.3f, new Animation[] {
                new RotateAnim(1f, new QuadraticGraph(-90, 0, true)),
                new ScaleAnim(1f, new LinearGraph(scale, 1f))
        });
        return startAnim;
    }

}
