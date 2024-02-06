package game31.gb.gallery;

import java.util.Locale;

import game31.Globals;
import game31.ScreenBar;
import game31.app.gallery.PhotoRollGridScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.NullAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 21/7/2016.
 */
public class GBPhotoRollGridScreen implements PhotoRollGridScreen.BuilderSource {

    public GBPhotoRollGridScreen(PhotoRollGridScreen screen) {
        PhotoRollGridScreen.Internal s = new PhotoRollGridScreen.Internal();

        Sprite sprite;
        PatchedSprite patch;

        Font boldFont = new Font("opensans-semibold.ttf", 32);

        Font dateFont = new Font("opensans-light.ttf", 40);

        Font nameFont = new Font("opensans-regular.ttf", 32, 0xffffff55);

        Font notificationFont = new Font("opensans-bold.ttf", 32, 0xffffffff);

        Font summaryFont = new Font("opensans-light.ttf", 48, 0xffffffff);


        {
            // Name view
            s.nameView = new TextBox()
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0,-1).move(0, -0.06f))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(25f / 174f, 8f)
                            .ellipsize(1)
                            .text("fp_trailer_draft5A.vidx")
                    )
            ;


            sprite = Sprite.load("system/notification-square.png");
            s.unopenedView = new StaticSprite()
                    .metrics(new UIElement.Metrics().anchorTop().anchorRight().scale(0.25f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
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
            new TextBox()
                    .viewport(s.unopenedView)
                    .metrics(new UIElement.Metrics().move(+0.085f, +0.05f).scale(0.6f))
                    .text(new Text()
                            .font(notificationFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(0.8f, 0)
                            .text("!")
                    )
                    .attach();

        }


        {
            // Photo group
            s.photoView = new Clickable()
                    .metrics(new UIElement.Metrics().scale(165f / 535f).anchor(-174f / 535f, 0).pan(0, -0.5f))
                    .length(1f)
                    .target(SaraRenderer.TARGET_INTERACTIVE)
                    .animation(null, null, new ColorAnim(0xaaaaaaff), null, null)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    ;

        }

        {
            // Video group
            s.videoView = new Clickable()
                    .metrics(new UIElement.Metrics().scale(165f / 535f).anchor(-174f / 535f, 0).pan(0, -0.5f))
                    .length(1f)
                    .target(SaraRenderer.TARGET_INTERACTIVE)
                    .animation(null, null, new ColorAnim(0xaaaaaaff), null, null)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    ;

            // Movie indicator
            Sprite movieBgMat = new Sprite(34f / 150f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(movieBgMat).set(0, 0, 0, 0.6f);
            StaticSprite movieBg = new StaticSprite()
                    .viewport(s.videoView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(movieBgMat, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();

            // Icon
            new StaticSprite()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(22f / 150f).anchor(-49f / 150f, 0))
                    .visual(Sprite.load("apps/gallery/video-icon.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();

            s.videoDurationView = new TextBox()
                    .viewport(movieBg)
                    .metrics(new UIElement.Metrics().scale(85f / 150f).anchor(+22f / 150f, 0))
                    .text(new Text()
                            .font(boldFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(15f / 85f, 0)
                            .text("3:42")
                            .centerRight()
                    )
                    .attach();
        }

        {
            // Audio group
            sprite = new Sprite(1f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x746284ff);
            s.audioView = new Clickable()
                    .metrics(new UIElement.Metrics().scale(165f / 535f).anchor(-174f / 535f, 0).pan(0, -0.5f))
                    .length(1f)
                    .visuals(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(null, null, new ColorAnim(0xaaaaaaff), null, null)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    ;

            // Icon
            new StaticSprite()
                    .viewport(s.audioView)
                    .metrics(new UIElement.Metrics().scale(120f / 252f).anchor(0, +0.11f))
                    .visual(Sprite.load("apps/gallery/mic-icon.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();

            // Audio indicator
            Sprite audioBgMat = new Sprite(34f / 150f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(audioBgMat).set(0, 0, 0, 0.6f);
            StaticSprite audioBg = new StaticSprite()
                    .viewport(s.audioView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(audioBgMat, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();

            // Icon
            new StaticSprite()
                    .viewport(audioBg)
                    .metrics(new UIElement.Metrics().scale(22f / 150f).anchor(-49f / 150f, 0))
                    .visual(Sprite.load("apps/gallery/speaker-icon.png"), SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .attach();

            s.audioDurationView = new TextBox()
                    .viewport(audioBg)
                    .metrics(new UIElement.Metrics().scale(85f / 150f).anchor(+22f / 150f, 0))
                    .text(new Text()
                            .font(boldFont)
                            .target(SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(15f / 85f, 0)
                            .text("3:42")
                            .centerRight()
                    )
                    .attach();
        }

        {
            // Corruption
            patch = PatchedSprite.create("system/rounded-glow.png.NoiseMaterial", 1f, 0.2f);
            ColorAttribute.of(patch).set(0x000000ff);

            s.corruptionView = new StaticSprite()
                    .metrics(new UIElement.Metrics().scale(1.18f))
                    .visual(patch, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    ;

            // Icon
            new StaticSprite()
                    .viewport(s.corruptionView)
                    .metrics(new UIElement.Metrics().scale(0.5f))
                    .visual(Sprite.load("system/fix.png"), SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(
                            null,
                            new SequenceAnim(new Animation[] {
                                    new NullAnim(0.5f),
                                    new FadeAnim(0.5f, new ConstantGraph(0.5f))
                            }),
                            null
                    )
                    .attach();
        }


        {
            // Window
            s.window = new UIElement.Group();

            // Bg
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();

            s.surface = new ScrollableSurface()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .padding(0, 0.167f, 0, 0.18f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();

            s.bars = new ScreenBar();
            s.bars.attach(screen);
//            s.bars.showShadows(0x252531ff, 0.9f);
            s.bars.showNavbar(true, true, true);

            s.photoIntervalX = 174f / 535f;
            s.photoIntervalY = -13f / 640f; // -13f / 640f;
            s.photoIntervalWithNameY = -70f / 640f; // -13f / 640f;
            s.photosPerRow = 3;


            s.dateView = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(1).anchorLeft().pan(0, -0.5f))
                    .visual("system/square.png", 0.02f, SaraRenderer.TARGET_INTERACTIVE)
                    .font(dateFont, -19f, 1f, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                    .padding(1.5f, 3f, 2f, 4.5f)
                    .animation(null, new ColorAnim(0x0e162dff), null)
                    .centerLeft()
                    ;
            sprite = new Sprite(1f / 550f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xffffffff);
            new StaticSprite()
                    .viewport(s.dateView)
                    .metrics(new UIElement.Metrics().anchorTop().anchorLeft().anchor(-0.15f, -0.43f).scaleIndex(2))
                    .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                    .attach();

            s.dateIntervalY = 0; // -42f / 640f;


            s.summaryView = new TextBox()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .text(new Text()
                            .font(summaryFont)
                            .position(128f / 640f, -15f)
                            .center()
                    );
        }

        // Commit
        screen.setInternal(s);
    }

    public String summarize(int photos, int videos, int recordings) {
        String photosText;
        if(photos == 1)
            photosText = "1 Photo";
        else
            photosText = String.format(Locale.US, "%,d Photos", photos);
        String videosText;
        if(videos == 1)
            videosText = "1 Video";
        else
            videosText = String.format(Locale.US, "%,d Videos", videos);
        String audioText;
        if(recordings == 1)
            audioText = "1 Recording";
        else
            audioText = String.format(Locale.US, "%,d Recordings", recordings);

        if(photos == 0 && videos == 0 && recordings == 0)
            return "Nothing here";

        String summary = "";
        if(photos > 0)
            summary += photosText;
        if(videos > 0) {
            if(!summary.isEmpty())
                summary += ", ";
            summary += videosText;
        }
        if(recordings > 0) {
            if(!summary.isEmpty())
                summary += ", ";
            summary += audioText;
        }
        return summary;
    }

    public Animation animateThumb(int index) {
        float tStartDelay = (index % 4) * 0.07f;         // 0.05f
        return new SequenceAnim(new Animation[] {
                // new FadeAnim(index * 0.05f, new ConstantGraph(0.3f)),
                // new FadeAnim(0.2f, new QuadraticGraph(0.3f, 1f, true))
                new ScaleAnim(tStartDelay, ConstantGraph.zero),
                new ScaleAnim(0.2f, new QuadraticGraph(0f, 1f, true))
        });
    }
}
