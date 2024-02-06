package game31.gb;

import com.badlogic.gdx.utils.Align;

import game31.CreditsMenu;
import game31.Globals;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.FadeAnim;
import sengine.animation.SequenceAnim;
import sengine.calc.LinearGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 9/25/2017.
 */

public class GBCreditsMenu {

    private static Animation createStart(float tDelay) {
        return new SequenceAnim(new FadeAnim(0.5f, LinearGraph.zeroToOne), tDelay, true);
    }

    private static void add(UIElement parent, Font font, float x, float y, float tDelay, int align, String text) {
        new TextBox()
                .viewport(parent).metrics(new UIElement.Metrics().move(x, y))
                .text(new Text()
                        .font(font, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                        .position(0.9f, 0.09f, -22f)
                        .align(align)
                        .text(text)
                )
                .animation(createStart(tDelay), null, null)
                .attach();

    }

    public GBCreditsMenu(CreditsMenu menu) {
        CreditsMenu.Internal s = new CreditsMenu.Internal();

        Sprite sprite;

        Font boldFont = new Font("opensans-semibold.ttf", 40, 0xffffff99);
        Font normalFont = new Font("opensans-regular.ttf", 40, 0xffffff99);
        Font lightFont = new Font("opensans-light.ttf", 40, 0xffffff99);

        Font skipFont = new Font("opensans-light.ttf", 32, 0x444444ff);

        {

            s.window = new UIElement.Group();

            s.closeButton = new Clickable()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.5f).anchorBottom().anchorRight().move(-0.03f, +0.03f))
                    .length(0.14f)
                    .text(new Text()
                            .font(skipFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(0.06f, 0f)
                            .centerRight()
                            .text("SKIP")
                    )
                    .animation(
                            new SequenceAnim(new FadeAnim(0.5f, LinearGraph.zeroToOne, 1), 0.5f, true),
                            null,
                            new FadeAnim(0.5f, 1),
                            null,
                            null
                    )
                    .inputPadding(0.1f, 0.1f, 0.1f, 0.1f)
                    .attach();


            // Bg
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.screenNoiseMaterial);
            ColorAttribute.of(sprite).set(0x222222ff);
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .animation(new ColorAnim(
                            1f,
                            LinearGraph.zeroToOne,
                            null
                    ), null, null)
                    .attach();

            s.creditsGroup = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics())
                    .attach();


            float y = +0.3f;
            float x = 0;
            float yInterval = -0.06f;
            float tDelay = 0.5f;

            // SIMULACRA
            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.0f, Align.center,   "SIMULACRA: Pipe Dreams");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 1.0f, Align.center,   "A KAIGAN GAMES PRODUCTION");

            y += yInterval * 3;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "PRODUCER");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Shahrizar");

            y += yInterval * 2;
            x = -0.25f;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "GAME DESIGN");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Jeremy Ooi");

            x = +0.25f;
            y -= yInterval * 2;
            tDelay -= 2.0f;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "DEVELOPMENT");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Azmi Shah");

            x = 0;
            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "STORY BY");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Jeremy Ooi");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "WRITTEN BY");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Priya Kulasagaran");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Jeremy Ooi");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "ART AND UI DESIGN");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "LeeYing Foo");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "OPERATIONS");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Suhana Sulaiman");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "ADDITIONAL DEVELOPMENT");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Alex Lee");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Arief Prasetyo");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "VIDEO PRODUCTION");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Write Handed");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "AUDIO PRODUCTION");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "IMBA Interactive");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 1.5f, Align.center,   "PIPE DREAMS & FLAPEEBIRD THEMES");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Theatre of Sound");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 2.5f, Align.center,   "CAST");

            y += yInterval;

            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Teddy Jones");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Alfred Loh");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "FlapeeBird Simulacrum");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Lee Min Hui");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 2.5f, Align.center,   "SPECIAL APPEARANCES");

            y += yInterval;

            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Cat-Ass-Trophy Girl");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Nur Zakuan");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Paradox Bob Guy");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Ui Hua Cheah");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Trapped Girl");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Mia Sabrina Mahadir");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Jump N Shoot Girl");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "LeeYing Foo");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Jump N Shoot Guy");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Amir Yunos");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Allarena Guy #1");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Tan Meng Kheng");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Allarena Guy #2");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Gregory Sze");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Ninja Fox Guy");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Adi Bin Khalid");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Ninja Fox Girl");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Dina Megat");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.left,     "Sad Guy in Trailer");
            add(s.creditsGroup, normalFont, x, y,               tDelay,         Align.right,    "Dushanth Daniel Ray");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 2.5f, Align.center,   "ATTRIBUTION");

            y += yInterval;

            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 0.5f, Align.center,   "Music from Ozzed.net");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "“The Misadventure Begins”");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "“Ferrous Rage”");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "“World Nap”");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "“Shell shock shake”");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "“Boink”");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "“Nackskott”");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "“Lingonsalt”");

            y += yInterval;


            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 0.5f, Align.center,   "Gameplay footage for IRIS ADs");
            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 0.5f, Align.center,   "provided by KDU University College");
            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 0.5f, Align.center,   "student projects");

            y += yInterval;

            add(s.creditsGroup, boldFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Trapped");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "kdu.itch.io/trapped");
            add(s.creditsGroup, boldFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Trapped2D");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "kdu.itch.io/trapped2d");
            add(s.creditsGroup, boldFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Catastrophe Armageddon");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "kdu.itch.io/catastrophe-armageddon");
            add(s.creditsGroup, boldFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Paradox Bob");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "kdu.itch.io/paradox-bob");
            add(s.creditsGroup, boldFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "Allarena");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "kdu.itch.io/allrena");
            add(s.creditsGroup, boldFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "World of Ninja");
            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "kdu.itch.io/worldofninja");

            y += yInterval;


            add(s.creditsGroup, boldFont,   x, y += yInterval,  tDelay += 3.0f, Align.center,   "SPECIAL THANKS");

            y += yInterval;

            add(s.creditsGroup, normalFont, x, y += yInterval,  tDelay += 0.5f, Align.center,   "MDeC, KDU University College");

            y = -yInterval;
            add(s.window, boldFont, 0, y,               tDelay += 13.0f, Align.center,   "And to all our loyal fans!");
            add(s.window, boldFont, 0, y += yInterval,  tDelay += 2.0f, Align.center,   "Thank you");


            // Timing
            s.tCreditsMaxTime = 67f;
            s.tMoveStart = 8f;
            s.moveSpeedY = +0.09f;
        }

        // Commit
        menu.setInternal(s);
    }
}
