package game31.gb.gallery;

import game31.Globals;
import game31.ScreenBar;
import game31.app.gallery.PhotoRollAlbumsScreen;
import game31.renderer.SaraRenderer;
import sengine.audio.Sound;
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
 * Created by Azmi on 25/7/2016.
 */
public class GBPhotoRollAlbumsScreen {


    public GBPhotoRollAlbumsScreen(PhotoRollAlbumsScreen screen) {

        Font titleFont = new Font("opensans-regular.ttf", 48);


        {
            // Row

            Font nameFont = titleFont;

            Sprite touchedBg = new Sprite(246f / 1080f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(touchedBg).set(0x091027ff);

            Clickable bg = new Clickable()
                    .metrics(new UIElement.Metrics().pan(0, -0.5f))
                    .visuals(null, touchedBg, SaraRenderer.TARGET_BG)
                    .sound(Sound.load("sounds/general_forward.ogg"))
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance);

            StaticSprite previewView = new StaticSprite()
                    .viewport(bg)
                    .metrics(new UIElement.Metrics().anchorLeft().scale(181f / 1080f).move(+0.03f, 0))
                    // .visual(Sprite.load(profileFilename), SaraRenderer.TARGET_INTERACTIVE)
                    .visual(null, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();


            TextBox titleView = new TextBox()
                    .viewport(bg)
                    .metrics(new UIElement.Metrics().anchor(+15f / 270f, 0).scale(154f / 270f))
                    .text(new Text()
                            .font(nameFont)
                            .position(23f / 154f, -10f)
                            .centerLeft()
                            .target(SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .text("Another Sat @ CFATL")
                    ).attach();

            // Bottom line
            Sprite line = new Sprite(1f / 270f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(line).set(0x70708855);
            StaticSprite lineView = new StaticSprite()
                    .viewport(bg)
                    .metrics(new UIElement.Metrics().anchorBottom().move(+0.21f, 0))
                    .visual(line, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .attach();


            // Row title format
            titleFont.color("AlbumScreen.count", 0xbfe1ddff);
            String titleFormat = "%s [AlbumScreen.count](%,d)[]";

            Sprite audioThumbIcon = Sprite.load("apps/gallery/mic-thumb.png");

            screen.setRowGroup(bg, previewView, titleView, titleFormat, audioThumbIcon);
        }


        {
            // Window

            UIElement.Group window = new UIElement.Group();

            Sprite sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff);

            StaticSprite windowBg = new StaticSprite()
                    .viewport(window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();

            ScrollableSurface surface = new ScrollableSurface()
                    .viewport(window)
                    .length(Globals.LENGTH)
                    .padding(0, 0.167f, 0, 0.18f)
                    .scrollable(false, true)
                    .selectiveRendering(true, false)
                    .attach();

            ScreenBar bars = new ScreenBar();
            bars.attach(screen);
            bars.showAppbar("Albums", null, 0, 0, 0, 0);
            bars.showNavbar(true, true, true);

            Clickable refreshButton = null;     // not used
//            PatchedSprite patch = PatchedSprite.create("system/appbar-button.png", 0.8f, 0.36f);
//
//            Clickable refreshButton = new Clickable()
//                    .viewport(status.appbar())
//                    .metrics(new UIElement.Metrics().scale(0.14f).anchorRight().move(-0.03f, 0))
//                    .visuals(patch, SaraRenderer.TARGET_APPBAR)
//                    .animation(null, null, buttonPressedAnim, null, null)
//                    .attach();
//
//            new StaticSprite()
//                    .viewport(refreshButton)
//                    .metrics(new UIElement.Metrics().scale(0.5f))
//                    .visual(Sprite.load("apps/chats/refresh.png"), SaraRenderer.TARGET_APPBAR)
//                    .attach();

            screen.setWindow(window, surface, bars, refreshButton);
        }


    }


}
