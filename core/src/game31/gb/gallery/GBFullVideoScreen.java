package game31.gb.gallery;

import game31.Globals;
import game31.app.gallery.FullVideoScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.CompoundAnim;
import sengine.animation.RotateAnim;
import sengine.animation.ScaleAnim;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.ui.StaticSprite;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 8/24/2017.
 */

public class GBFullVideoScreen implements FullVideoScreen.BuilderSource {

    public GBFullVideoScreen(FullVideoScreen screen) {
        FullVideoScreen.Internal s = new FullVideoScreen.Internal();

        Sprite sprite;

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
