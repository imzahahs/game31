package game31.gb;

import com.badlogic.gdx.graphics.Color;

import game31.Globals;
import game31.Keyboard;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.NullAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.PatchedSprite;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.Toast;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 4/7/2016.
 */

public class GBKeyboard {

    public GBKeyboard(Keyboard keyboard) {
        Keyboard.Internal s = new Keyboard.Internal();


        Font textFont = new Font("opensans-regular.ttf", "emojis/emojione", 48, Color.CLEAR, 0, Color.CLEAR, 0, 0, new Color(0xf2f2f2ff));
        Font suggestFont = new Font("opensans-regular.ttf", "emojis/emojione", 32, Color.CLEAR, 0, Color.CLEAR, 0, 0, Color.BLACK);
        Font selectorTitleFont = new Font("opensans-semibold.ttf", 48);

        Font hintFont = new Font("opensans-regular.ttf", 32);

        Font selectorSubFont = suggestFont.instantiate();
        ColorAttribute.of(selectorSubFont).set(0xbbbbbbff);

        Font attachmentFont = new Font("opensans-bold.ttf", 32, 0x16a85dff);

        Font poweredByIrisFont = new Font("opensans-regular.ttf", 32, 0x838181ff);

        Animation buttonPressedAnim = new ColorAnim(0xccccccff);



        PatchedSprite patchedSprite;

        Sprite sprite;

        Sprite bgMat = new Sprite(371f / 550f, Material.load("system/keyboard-bg.png"));

        s.viewGroup = new UIElement.Group()
                .length(bgMat.length)
                .animation(
                        new MoveAnim(0.5f * Globals.tKeyboardAnimationSpeedMultiplier, null, new QuadraticGraph(-0.8f, 0f, true)),
                        null,
                        new MoveAnim(0.5f * Globals.tKeyboardAnimationSpeedMultiplier, null, new QuadraticGraph(0f, -0.8f, false))
                )
                .metrics(new UIElement.Metrics().anchorBottom());



        // Autocorrect
        {
            UIElement.Group surfaceGroupContainer = new UIElement.Group()
                    .viewport(s.viewGroup)
                    .attach();

            patchedSprite = PatchedSprite.create("system/square.png", 63f / 550f, 0);

            s.wordGroup = new StaticSprite()
                    .viewport(surfaceGroupContainer)
                    .visual(patchedSprite, SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE)
                    .animation(
                            new MoveAnim(0.5f * Globals.tKeyboardAnimationSpeedMultiplier, null, new QuadraticGraph(-0.13f, 0f, true)),
                            null,
                            new MoveAnim(0.5f * Globals.tKeyboardAnimationSpeedMultiplier, null, new QuadraticGraph(0f, -0.13f, false)))
                    .metrics(new UIElement.Metrics().anchor(0, +0.5f).pan(0, +0.5f));

            // Autocomplete toast
            sprite = new Sprite(patchedSprite.getLength(), SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x303030ff);

            s.hintView = new Toast()
                    .viewport(surfaceGroupContainer)
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD_UNDERLAY)
                    .animation(
                            new MoveAnim(0.5f * Globals.tKeyboardAnimationSpeedMultiplier, null, new QuadraticGraph(-0.13f, 0f, true)),
                            null,
                            new MoveAnim(0.5f * Globals.tKeyboardAnimationSpeedMultiplier, null, new QuadraticGraph(0f, -0.13f, false)))
                    .metrics(new UIElement.Metrics().anchor(0, +0.5f).pan(0, +0.5f))
                    .time(1f);

            s.hintTextView = new TextBox()
                    .viewport(s.hintView)
                    .text(new Text()
                            .font(hintFont, SaraRenderer.TARGET_KEYBOARD_UNDERLAY_TEXT)
                            .position(0.9f, patchedSprite.length * 0.4f, 0)
                            .text("Continue typing")
                    )
                    .attach();

            Animation pressedColor = new ColorAnim(0.15f, 0.6f, 0.6f, 0.6f, 1f, false);
            Animation normalColor = new NullAnim(0.15f);
            s.wordHintAnim = new SequenceAnim(new Animation[]{
                    new NullAnim(2f),
                    pressedColor,
                    normalColor,
                    pressedColor,
                    normalColor,
                    pressedColor,
                    normalColor,
                    pressedColor,
                    normalColor
            });


            s.wordSurface = new ScrollableSurface()
                    .viewport(s.wordGroup)
                    .length(63f / 550f)
                    .attach();

            s.wordTemplate = new PatchedTextBox()
                    .viewport(s.wordSurface)
                    .metrics(new UIElement.Metrics().scale(1))
                    .visual("system/square.png", 0.007f, SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE)
                    .font(textFont, -18f, 1f, SaraRenderer.TARGET_KEYBOARD_AUTOCOMPLETE_TEXT)
                    .passThroughInput(true)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .animation(null, null, buttonPressedAnim, null, null)
                    .padding(4, 0, 4, 0)
                    .enable();

        }


        // Keyboard
        {
            s.keyboardView = new StaticSprite()
                    .viewport(s.viewGroup)
                    .visual(bgMat, SaraRenderer.TARGET_KEYBOARD)
                    .passThroughInput(false)
                    .attach();

            float row1y = +0.36f;
            float row1x = -0.445f;
            float row1interval = +0.099f;

            float row2y = row1y - 0.24f;
            float row2x = -0.39f;
            float row2interval = row1interval;

            float row3y = row2y - 0.24f;
            float row3x = row2x - 0.027f;
            float row3interval = row1interval;

            float row4y = row3y - 0.25f;

            float ay = row1y;
            float ax = row1x;
            float xinterval = row1interval;
            float keySize = 48f / 550f;     // 55f

            PatchedSprite keyGreyMat = PatchedSprite.create("system/rounded.png", 198f / 161f, 0.1f);            // 140f / 87f
            ColorAttribute.of(keyGreyMat).alpha(0f);

            Animation keyPressedAnim = new CompoundAnim(1f, new Animation[] {
                    new ColorAnim(1f, null, new ConstantGraph(32f / 255f), false),       // Bg
                    new ColorAnim(1f, new ConstantGraph(0.4f), null, false, 1),
            });
            Animation keyReleasedAnim = new CompoundAnim(0.2f, new Animation[] {
                    new ColorAnim(1f, null, new LinearGraph(32f / 255f, 0), false),       // Bg
                    new ColorAnim(1f, new QuadraticGraph(0.4f, 1f, 0f, true), null, true, 1)
            });

            Clickable key = new Clickable()
                    .visuals(keyGreyMat, SaraRenderer.TARGET_KEYBOARD)
                    .text(new Text()
                            .font(textFont)
                            .position(0, +0.05f, 0.7f, keyGreyMat.getLength(), -1f)
                            .target(SaraRenderer.TARGET_KEYBOARD_TEXT)
                    )
                    .animation(null, null, keyPressedAnim, keyReleasedAnim, null)
                    .inputPadding(0.07f, 0.30f, 0.07f, 0.30f);


            Clickable keyQ = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax, ay))
                    .attach();

            Clickable keyW = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyE = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyR = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyT = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyY = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyU = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyI = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyO = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyP = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            // Second row
            ax = row2x;
            ay = row2y;
            xinterval = row2interval;

            Clickable keyA = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax, ay))
                    .inputPadding(1f, 0.30f, 0.07f, 0.30f)
                    .attach();

            Clickable keyS = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyD = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyF = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyG = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyH = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyJ = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyK = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyL = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .inputPadding(0.07f, 0.30f, 1f, 0.30f)
                    .attach();

            // Second row
            ax = row3x;
            ay = row3y;
            xinterval = row2interval;


            patchedSprite = PatchedSprite.create("system/rounded.png", 198f / 230f, 0.07f);
            ColorAttribute.of(patchedSprite).alpha(0f);

            s.keyboardShift = new Clickable()
                    .viewport(s.keyboardView)
                    .visuals(patchedSprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics((new UIElement.Metrics().scale(keySize * (keyGreyMat.length / patchedSprite.length)).anchor(ax, ay)))
                    .animation(null, null, keyPressedAnim, keyReleasedAnim, null)
                    .inputPadding(0.07f, 0.17f, 0.07f, 0.17f)
                    .attach();

            sprite = Sprite.load("system/keyboard-shift.png");
            ColorAttribute.of(sprite).set(0xf1f1f1ff);
            new StaticSprite()
                    .viewport(s.keyboardShift)
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics(new UIElement.Metrics().scale(0.5f).move(0, +0.03f))
                    .attach();

            // alphabets
            Clickable keyZ = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += (xinterval + 0.026f), ay))
                    .attach();

            Clickable keyX = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyC = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyV = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyB = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyN = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();

            Clickable keyM = key.instantiate()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(keySize).anchor(ax += xinterval, ay))
                    .attach();


            // backspace
            patchedSprite = PatchedSprite.create("system/rounded.png", 198f / 230f, 0.07f);
            ColorAttribute.of(patchedSprite).alpha(0f);

            s.keyboardEraseButton = new Clickable()
                    .viewport(s.keyboardView)
                    .visuals(patchedSprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics((new UIElement.Metrics().scale(keySize * (keyGreyMat.length / patchedSprite.length)).anchor(ax += (xinterval + 0.022f), ay)))
                    .animation(null, null, keyPressedAnim, keyReleasedAnim, null)
                    .inputPadding(0.07f, 0.17f, 0.07f, 0.17f)
                    .attach();

            sprite = Sprite.load("system/keyboard-back.png");
            ColorAttribute.of(sprite).set(0xf1f1f1ff);
            new StaticSprite()
                    .viewport(s.keyboardEraseButton)
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics(new UIElement.Metrics().scale(0.57f).move(0, +0.03f))
                    .attach();

            ay = row4y;

            // numeric
            patchedSprite = PatchedSprite.create("system/rounded.png", 198f / 370f, 0.05f);
            ColorAttribute.of(patchedSprite).alpha(0f);

            s.keyboardNumeric = new Clickable()
                    .viewport(s.keyboardView)
                    .visuals(patchedSprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics((new UIElement.Metrics().scale(keySize * (keyGreyMat.length / patchedSprite.length)).anchorLeft().anchor(+0.02f, ay)))
                    .animation(null, null, keyPressedAnim, keyReleasedAnim, null)
                    .text(new Text()
                            .font(textFont, SaraRenderer.TARGET_KEYBOARD_TEXT)
                            .position(patchedSprite.length, -4.5f)
                            .text("123")
                    )
                    .inputPadding(1f, 0.16f, 0.04f, 0.16f)
                    .attach();

            // space
            patchedSprite = PatchedSprite.create("system/rounded.png", 198f / 969f, 0.02f);
            ColorAttribute.of(patchedSprite).alpha(0.13f);

            s.keyboardSpace = new Clickable()
                    .viewport(s.keyboardView)
                    .visuals(patchedSprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics((new UIElement.Metrics().scale(keySize * (keyGreyMat.length / patchedSprite.length)).anchor(0, ay)))
                    .animation(
                            null, null,
                            new FadeAnim(0.7f),
                            new FadeAnim(0.2f, new LinearGraph(0.7f, 1f)),
                            null
                    )
                    .inputPadding(0.01f, 0.06f, 0.01f, 1f)
                    .attach();

            // confirm
            patchedSprite = PatchedSprite.create("system/rounded.png", 198f / 370f, 0.05f);
            ColorAttribute.of(patchedSprite).alpha(0f);

            s.keyboardConfirm = new Clickable()
                    .viewport(s.keyboardView)
                    .visuals(patchedSprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics(new UIElement.Metrics().scale(keySize * (keyGreyMat.length / patchedSprite.length)).anchorRight().anchor(-0.02f, ay))
                    .text(new Text()
                            .font(textFont, SaraRenderer.TARGET_KEYBOARD_TEXT)
                            .position(patchedSprite.length, -4.5f)
                            .text("confirm")
                    )
                    .animation(null, null, keyPressedAnim, keyReleasedAnim, null)
                    .inputPadding(0.04f, 0.16f, 1f, 0.16f)
                    .attach();

            // Lines
            sprite = new Sprite(9f / 2018f, Material.load("apps/lock/line.png"));
            ColorAttribute.of(sprite).set(0xe2e2e2ff);
            new StaticSprite()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(2100f / 2250f).move(0, +0.165f))
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .attach();
            new StaticSprite()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(2100f / 2250f).move(0, +0.0f))
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .attach();
            new StaticSprite()
                    .viewport(s.keyboardView)
                    .metrics(new UIElement.Metrics().scale(2100f / 2250f).move(0, -0.155f))
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .attach();

            s.keyboardKeys = new Clickable[]{
                    keyQ, keyW, keyE, keyR, keyT, keyY, keyU, keyI, keyO, keyP,
                    keyA, keyS, keyD, keyF, keyG, keyH, keyJ, keyK, keyL,
                    keyZ, keyX, keyC, keyV, keyB, keyN, keyM
            };

            s.uppercaseKeys = new String[]{
                    "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
                    "A", "S", "D", "F", "G", "H", "J", "K", "L",
                    "Z", "X", "C", "V", "B", "N", "M"
            };

            s.lowercaseKeys = new String[]{
                    "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                    "a", "s", "d", "f", "g", "h", "j", "k", "l",
                    "z", "x", "c", "v", "b", "n", "m"
            };

            s.numericKeys = new String[]{
                    "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                    "-", "|", ":", ";", "(", ")", "$", "&", "@",
                    ".", ",", "?", "!", "'", "\"", "/"
            };

            Animation pressedColor = new ColorAnim(0.15f, 0.6f, 0.6f, 0.6f, 1f, false);
            Animation normalColor = new NullAnim(0.15f);
            s.hintAnim = new SequenceAnim(new Animation[]{
                    pressedColor,
                    normalColor,
                    pressedColor,
                    normalColor,
                    pressedColor,
                    normalColor,
                    pressedColor,
                    normalColor
            });

            s.keySound = Sound.load("sounds/keyboard_keys.ogg");
            s.backSound = Sound.load("sounds/keyboard_back.ogg");
            s.enterSound = Sound.load("sounds/keyboard_enter.ogg");
            s.spaceSound = Sound.load("sounds/keyboard_space.ogg");
            s.keySoundVolume = 0.25f;

            s.openSound = Sound.load("sounds/keyboard_open.ogg");
            s.acceptSound = Sound.load("sounds/chat_select.ogg");
            s.cancelSound = Sound.load("sounds/general_back.ogg");
        }


        // Dialog selector
        {
            sprite = new Sprite(426f / 540f, Material.load("system/keyboard-bg.png"));

            s.selectorView = new StaticSprite()
                    .viewport(s.viewGroup)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .passThroughInput(false)
                    .attach();

            s.selectorSurface = new ScrollableSurface()
                    .viewport(s.selectorView)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .length(s.selectorView.getLength() - 0.12f)
                    .padding(0, 0.02f, 0, 0.02f)
                    .scrollable(false, true)
                    .selectiveRendering(true, true)
                    .attach();

            s.selectorRow = new PatchedTextBox()
                    .metrics(new UIElement.Metrics().scale(0.85f).pan(0, -0.5f))
                    .visual("system/keyboard-bubble.png", 0.04f, SaraRenderer.TARGET_KEYBOARD)
                    .padding(1.1f, 0.9f * 1.5f, 1.1f, 1.5f * 1.5f)
                    .centerLeft()
                    .minSize(1f, 0f)
                    .animation(
                            null,
                            null,
                            new ColorAnim(0xadffe6ff),
                            null,
                            null
                    )
                    .font(suggestFont, 18f, 200f, SaraRenderer.TARGET_KEYBOARD_TEXT)
                    .passThroughInput(true)
                    .inputPadding(1f, 0.0105f, 1f, 0.0105f)
                    .maxTouchMoveDistance(Globals.maxTouchMoveDistance)
                    .enable();

            // Back to keyboard
            s.selectorKeyboardRow = s.selectorRow.instantiate()
                    .font(selectorSubFont)
                    .text("Show keyboard")
                    .refresh();

            sprite = Sprite.load("system/keyboard.png");
            ColorAttribute.of(sprite).set(0xbbbbbbff);
            new StaticSprite()
                    .viewport(s.selectorKeyboardRow)
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics(new UIElement.Metrics().scale(0.1f).anchor(+0.35f, 0))
                    .attach();

            // Back to keyboard
            s.selectorKeyboardRow = s.selectorRow.instantiate()
                    .font(selectorSubFont)
                    .text("Show keyboard")
                    .refresh();

            // Attachment
            s.selectorAttachmentRow = s.selectorRow.instantiate()
                    .font(attachmentFont)
                    .text("Send Video")
                    ;

            sprite = Sprite.load("system/attachment.png");
            ColorAttribute.of(sprite).set(0x16a85dff);
            new StaticSprite()
                    .viewport(s.selectorAttachmentRow)
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .metrics(new UIElement.Metrics().scale(0.07f).move(+0.41f, 0))
                    .attach();




            // Selector title
            s.selectorTitle = new UIElement.Group()
                    .viewport(s.selectorView)
                    .metrics(new UIElement.Metrics().anchorTop())
                    .length(230f / 2250f)
                    .attach();
            new TextBox()
                    .viewport(s.selectorTitle)
                    .metrics(new UIElement.Metrics().scale(1331f / 2250f).anchorLeft().move(+297f / 2250f, 0))
                    .text(new Text()
                            .font(textFont, SaraRenderer.TARGET_KEYBOARD_TEXT)
                            .position(109f / 1331f, -10f)
                            .centerLeft()
                            .text("Choose a reply")
                    )
                    .attach();

            // dialog icon
            sprite = Sprite.load("system/keyboard-chat.png");
            new StaticSprite()
                    .viewport(s.selectorTitle)
                    .metrics(new UIElement.Metrics().scale(152f / 2250f).anchorLeft().move(+83f / 2250f, 0))
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .attach();

            new TextBox()
                    .viewport(s.selectorTitle)
                    .metrics(new UIElement.Metrics().scale(0.18f).anchorRight().move(-0.02f, +0f))
                    .text(new Text()
                            .font(poweredByIrisFont, SaraRenderer.TARGET_KEYBOARD_TEXT)
                            .position(0.4f, 0f)
                            .centerRight()
                            .text("with IRIS™")
                    )
                    .attach();

            sprite = new Sprite(9f / 2018f, Material.load("apps/lock/line.png"));
            ColorAttribute.of(sprite).set(0xe2e2e2ff);
            new StaticSprite()
                    .viewport(s.selectorTitle)
                    .metrics(new UIElement.Metrics().scale(2100f / 2250f).anchorBottom().move(0, -0.01f))
                    .visual(sprite, SaraRenderer.TARGET_KEYBOARD)
                    .attach();

            s.selectorRowIntervalY = 0.02f;
        }


        // Custom colors
        Font boldFont = new Font("opensans-semibold.ttf", 32);
        boldFont.color("C_ACT", 0xff0000ff);



        // Commit
        keyboard.setInternal(s);
    }

}
