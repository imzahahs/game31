package game31.gb;

import game31.Globals;
import game31.SimTrailerMenu;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.NullAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.SequenceAnim;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.materials.VideoMaterial;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

public class GBSimTrailerMenu implements SimTrailerMenu.BuilderSource {

    public GBSimTrailerMenu(SimTrailerMenu screen) {
        SimTrailerMenu.Internal s = new SimTrailerMenu.Internal();

        Font skipFont = new Font("opensans-bold.ttf", 48, 0xffffffaa);

        Font buyButtonFont = new Font("arcamajora-heavy.otf", 48);
        Font closeButtonFont = new Font("opensans-regular.ttf", 48);
        Font headerFont = new Font("arcamajora-heavy.otf", 40);
        Font textFont = new Font("opensans-regular.ttf", 40);

        Sprite sprite;
        PatchedSprite patch;

        int pressedColor = 0xaaaaaaff;
        Animation pressedAnim = new ColorAnim(pressedColor);

        Animation releasedAnim = new SequenceAnim(new Animation[]{
                new NullAnim(0.04f),
                new ColorAnim(0.08f, pressedColor, true),
                new NullAnim(0.04f),
                new ColorAnim(0.03f, pressedColor, true),
        });

        {
            // Window
            s.window = new UIElement.Group();

            // Bg
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x000000ff);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();


            // Video view
            s.videoView = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics())
                    .target(SaraRenderer.TARGET_INTERACTIVE)
                    ;

            s.trailerPath = "content/videos/simulacra-trailer.mp4";
            if(Globals.checkAllAssets)
                Material.load(s.trailerPath + VideoMaterial.CFG_EXTENSION);

            // Skip button
            sprite = new Sprite(47f / 132f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0xffffff11);
            s.skipView = new StaticSprite()
                    .viewport(s.videoView)
                    .metrics(new UIElement.Metrics().scale(0.19f).anchorBottom().anchorRight().move(0, +0.03f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_FLOATING)
                    .animation(
                            new SequenceAnim(new Animation[] {
                                    new ScaleAnim(1.5f),
                                    new FadeAnim(0.5f, LinearGraph.zeroToOne),
                            }),
                            null,
                            null
                    )
                    .attach();
            new TextBox()
                    .viewport(s.skipView)
                    .text(new Text()
                            .font(skipFont, SaraRenderer.TARGET_INTERACTIVE_FLOATING_TEXT)
                            .position(sprite.length, -6f)
                            .text("Skip Ad")
                    )
                    .animation(
                            new SequenceAnim(new Animation[] {
                                    new FadeAnim(0.5f, LinearGraph.zeroToOne),
                            }),
                            null,
                            null
                    )
                    .attach();

            // Input view
            s.tapView = new Clickable()
                    .viewport(s.videoView)
                    .length(Globals.LENGTH)
                    .attach();

            s.infoGroup = new UIElement.Group()
                    .viewport(s.window)
                    .length(Globals.LENGTH)
                    .attach();


            // Info group bg
            sprite = Sprite.load("menu/promo-bg.png");
            ColorAttribute.of(sprite).set(0xffffffff);
            new StaticSprite()
                    .viewport(s.infoGroup)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();
            sprite = sprite.instantiate();
            ColorAttribute.of(sprite).alpha(0);
            new StaticSprite()
                    .viewport(s.infoGroup)
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(
                            new SequenceAnim(new Animation[] {
                                    new CompoundAnim(0.75f, new Animation[] {
                                            new ColorAnim(1f, new LinearGraph(0f, 1f), null, true),
                                            new ColorAnim(1f, null, new ConstantGraph(1f), false),
                                    }),
                                    new ColorAnim(1f, null, new LinearGraph(1f, 0f), false),
                            }),
                            null,
                            null
                    )
                    .attach();

            patch = PatchedSprite.create("system/circle.png", 70f / 333f, (70f / 333f) / 2f);
            ColorAttribute.of(patch).set(0xd20f89ff);
            s.buyButton = new Clickable()
                    .viewport(s.infoGroup)
                    .metrics(new UIElement.Metrics().scale(715f / 1080f).move(0, -0.66f))
                    .visuals(patch, SaraRenderer.TARGET_INTERACTIVE)
                    .text(new Text()
                            .font(buyButtonFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(patch.length, 9.5f)
                            .text("BUY NOW")
                    )
                    .animation(
                            null,
                            new ColorAnim(1f, new SineGraph(1f, 1f, 0f, 0.2f, 0.8f), null),
                            pressedAnim,
                            releasedAnim,
                    null
                    )
                    .inputPadding(10f, 0.06f, 10f, 0.06f)
                    .attach();

            s.closeButton = new Clickable()
                    .viewport(s.infoGroup)
                    .metrics(new UIElement.Metrics().scale(321f / 545f).move(0, -0.8f))
                    .length(36f / 321f)
                    .text(new Text()
                            .font(closeButtonFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(26f / 321f, 0)
                            .text("Close")
                    )
                    .animation(null, null,
                            new ColorAnim(1f, pressedColor, false, 1),
                            null,
                            null
                    )
                    .inputPadding(10f, 0.06f, 10f, 0.06f)
                    .attach();


            // Content
            new TextBox()
                    .viewport(s.infoGroup)
                    .metrics(new UIElement.Metrics().scale(0.94f).move(0, +0.80f))
                    .text(new Text()
                            .font(headerFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .wrapChars(19f)
                    )
                    .autoLengthText("AT FIRST THERE WAS")
                    .attach();

            new StaticSprite()
                    .viewport(s.infoGroup)
                    .metrics(new UIElement.Metrics().scale(0.86f).move(0, +0.67f))
                    .visual(Sprite.load("menu/sim-title.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            new TextBox()
                    .viewport(s.infoGroup)
                    .metrics(new UIElement.Metrics().scale(0.94f).move(0, +0.46f))
                    .text(new Text()
                            .font(textFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .wrapChars(-23f)
                    )
                    .autoLengthText("Witness the beginning of it all. SIMULACRA\n" +
                            "is the definitive experience of a found \n" +
                            "phone horror game.")
                    .attach();


            ConstantGraph lowGraph = new ConstantGraph(0.8f);
            ConstantGraph veryLowGraph = new ConstantGraph(1.2f);
            new StaticSprite()
                    .viewport(s.infoGroup)
                    .metrics(new UIElement.Metrics().move(0, +0.15f))
                    .visual(Sprite.load("menu/find-her-text.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            null,
                            new SequenceAnim(new Animation[]{
                                    new NullAnim(0.1f),
                                    new ColorAnim(0.2f, lowGraph, null),
                                    new NullAnim(0.2f),
                                    new ColorAnim(0.1f, lowGraph, null),
                                    new NullAnim(0.3f),
                                    new ColorAnim(0.3f, veryLowGraph, null),
                                    new NullAnim(0.1f),
                                    new ColorAnim(0.2f, lowGraph, null),
                                    new NullAnim(0.2f),
                                    new ColorAnim(0.3f, veryLowGraph, null),
                            }),
                            null
                    )
                    .attach();

            new TextBox()
                    .viewport(s.infoGroup)
                    .metrics(new UIElement.Metrics().scale(0.94f).move(0, -0.39f))
                    .text(new Text()
                            .font(textFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .wrapChars(-23f)
                    )
                    .autoLengthText("You find a phone belonging to a missing person\n" +
                            " named Anna. In it, you see a mysterious video \n" +
                            "of her crying for help. Her chats, emails and \n" +
                            "social media paints only a part of the story, \n" +
                            "and it is up to you to figure out the rest of it.")
                    .attach();

        }

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
