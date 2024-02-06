package game31.gb;

import game31.DialogBox;
import game31.Globals;
import game31.renderer.SaraRenderer;
import sengine.animation.FadeAnim;
import sengine.animation.ScaleAnim;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.ui.StaticSprite;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 20/8/2016.
 */
public class GBDialogBox implements DialogBox.InterfaceSource {

    public GBDialogBox(DialogBox dialog) {

        // Window
        {
            Mesh inputBlockerSprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(inputBlockerSprite).set(0,0,0, 0.7f);

            UIElement.Group window = new UIElement.Group();

            StaticSprite inputBlocker = new StaticSprite()
                    .viewport(window)
                    .visual(inputBlockerSprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(new FadeAnim(0.3f, new LinearGraph(0f, 1f)), null, new FadeAnim(0.3f, new LinearGraph(1f, 0f)))
                    .passThroughInput(false)
                    .attach();

            StaticSprite bg = new StaticSprite()
                    .viewport(window)
                    .metrics(new UIElement.Metrics().scale(0.9f))
                    .target(SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .animation(new ScaleAnim(0.15f, new QuadraticGraph(0f, 1f, true)), null, new ScaleAnim(0.15f, new QuadraticGraph(1f, 0f, false)))
                    ;//.attach();

            UIElement.Group group = new UIElement.Group()
                    .viewport(bg)
                    //.metrics(new UIElement.Metrics().anchorTop().scale(0.853f).offset(0, -0.08f))       // 0.851f
                    .metrics(new UIElement.Metrics().anchorTop())       // 0.851f
                    .attach();

            dialog.setWindow(window, bg, group, inputBlocker);
        }

    }

    public Mesh buildDialogBg(float length) {
        Mesh bgSprite = PatchedSprite.create("system/rounded.png", length + 0.00f, 0.04f);             // 0.16f
        ColorAttribute.of(bgSprite).set(0xd8d8ffff);
        // ColorAttribute.of(bgSprite).set(1, 1, 1, 0.8f);     // 0.8f

        return bgSprite;
    }
}
