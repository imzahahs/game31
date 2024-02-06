package game31.gb.phone;

import game31.Globals;
import game31.ScreenBar;
import game31.app.phone.PhoneDialerScreen;
import game31.renderer.SaraRenderer;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.ScaleAnim;
import sengine.audio.Audio;
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
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 24/8/2016.
 */
public class GBPhoneDialerScreen {

    private Clickable createDialKey(UIElement.Metrics metrics, String key, String alphabets) {
        Clickable key1 = new Clickable()
                .metrics(metrics.scale(208f / 640f))            // 215f
//                .visuals(keyPatch, SaraRenderer.TARGET_INTERACTIVE)
                .target(SaraRenderer.TARGET_INTERACTIVE)
                .length(373f / 553f)
                .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null);

        if(key != null) {
            TextBox key1number = new TextBox()
                    .viewport(key1)
                    .metrics(new UIElement.Metrics().scale(87f / 215f).anchor(0, +15f / 137f))
                    .text(new Text()
                            .font(keyNumberFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(55f / 87f, 0)
                            .text(key)
                    )
                    .attach();
        }

        if(alphabets != null) {
            TextBox alphabetsView = new TextBox()
                    .viewport(key1)
                    .metrics(new UIElement.Metrics().scale(87f / 215f).anchor(0, -40f / 137f))
                    .text(new Text()
                            .font(keyAlphaFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(17f / 87f, 0)
                            .text(alphabets)
                    )
                    .attach();
        }

        return key1;
    }

    private PatchedSprite keyPatch;
    private Animation buttonPressedAnim;
    private Animation buttonReleasedAnim;
    private Font keyNumberFont;
    private Font keyAlphaFont;

    public GBPhoneDialerScreen(PhoneDialerScreen screen) {
        PhoneDialerScreen.Internal s = new PhoneDialerScreen.Internal();

        Font regularFontLarge = new Font("opensans-regular.ttf", 64);

        Font boldFont = new Font("opensans-bold.ttf", 32);
        Font regularFont = new Font("opensans-regular.ttf", 32);

        Font tabFont = new Font("opensans-bold.ttf", 32, 0x1f284dff);


        keyNumberFont = new Font("opensans-light.ttf", 64, 0xf8f8f8ff);

        keyAlphaFont = new Font("opensans-light.ttf", 32, 0x8f8f8fff);


        buttonPressedAnim = new ScaleAnim(0.15f, new QuadraticGraph(0.5f, 1.1f, 1.2f, true));
        buttonReleasedAnim = new ScaleAnim(0.2f, new QuadraticGraph(1.1f, 1f, -0.9f, false));


        keyPatch = PatchedSprite.create("system/rounded-shadowed.png", 137f / 215f, 0.05f);
        ColorAttribute.of(keyPatch).set(0xacb4bdff);

        // Window
        {
            // Window
            Sprite greyBg = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(greyBg).set(0x0e162dff);

            s.window = new UIElement.Group();

            // Background
            StaticSprite windowBg = new StaticSprite()
                    .viewport(s.window)
                    .visual(greyBg, SaraRenderer.TARGET_BG)
                    .attach();

            // Status bar
            s.bars = new ScreenBar();
            s.bars.showAppbar("Dial", null);
            s.bars.showNavbar(true, true, true);
            s.bars.attach(screen);

            // Dialer bg
            Sprite sprite;

            s.dialedView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.9f).anchor(0, +0.265f))
                    .text(new Text()
                            .font(regularFontLarge, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(64f / 640f, 0)
                    )
                    .attach();




            // Line
            sprite = new Sprite(5f / 1500f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x8c8ca4ff);

            new StaticSprite()
                    .viewport(s.dialedView)
                    .metrics(new UIElement.Metrics().anchorBottom().scale(0.9f).move(0, -0.05f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();


            // Keypads
            UIElement.Group keypadGroup = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorTop().anchor(0, -250f / 960f))
                    .length(674f / 640f)
                    .attach();

            float rowOffset = -0.07f;
            float row1y = +0.50f - (71f / 674f) + rowOffset;
            float row2y = +0.50f - (206f / 674f) + rowOffset;
            float row3y = +0.50f - (341f / 674f) + rowOffset;
            float row4y = +0.50f - (476f / 674f) + rowOffset;
            float row5y = +0.50f - (613f / 674f) + rowOffset;
            float rowX = 0.03f;


            Clickable key1 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row1y), "1", null)
                    .viewport(keypadGroup)
                    .attach();

            Clickable key2 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchor(0, row1y), "2", "ABC")
                    .viewport(keypadGroup)
                    .attach();

            Clickable key3 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row1y), "3", "DEF")
                    .viewport(keypadGroup)
                    .attach();

            Clickable key4 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row2y), "4", "GHI")
                    .viewport(keypadGroup)
                    .attach();

            Clickable key5 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchor(0, row2y), "5", "JKL")
                    .viewport(keypadGroup)
                    .attach();

            Clickable key6 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row2y), "6", "MNO")
                    .viewport(keypadGroup)
                    .attach();

            Clickable key7 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row3y), "7", "PQRS")
                    .viewport(keypadGroup)
                    .attach();

            Clickable key8 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchor(0, row3y), "8", "TUV")
                    .viewport(keypadGroup)
                    .attach();

            Clickable key9 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row3y), "9", "WXYZ")
                    .viewport(keypadGroup)
                    .attach();

            Clickable keyStar = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row4y), "*", null)
                    .viewport(keypadGroup)
                    .attach();

            Clickable key0 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchor(0, row4y), "0", "+")
                    .viewport(keypadGroup)
                    .attach();

            Clickable keyHash = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row4y), "#", null)
                    .viewport(keypadGroup)
                    .attach();

            s.voiceMailButton = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row5y), null, null)
                    .viewport(keypadGroup)
                    .attach();

            sprite = Sprite.load("apps/calls/voicemail.png").instantiate();
            ColorAttribute.of(sprite).set(0x8f8f8fff);

            new StaticSprite()
                    .viewport(s.voiceMailButton)
                    .metrics(new UIElement.Metrics().scale(0.33f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            s.callButton = new Clickable()
                    .viewport(keypadGroup)
                    .metrics(new UIElement.Metrics().scale(0.15f).anchor(0f, row5y))
                    .visuals(Sprite.load("apps/calls/phone-accept.png"), SaraRenderer.TARGET_INTERACTIVE)
                    .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null)
                    .inputPadding(0.6f, 0.3f, 0.6f, 0.3f)
                    .attach();

            s.eraseButton = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row5y), null, null)
                    .viewport(keypadGroup)
                    .attach();

            sprite = Sprite.load("apps/calls/back.png").instantiate();
            ColorAttribute.of(sprite).set(0x8f8f8fff);

            new StaticSprite()
                    .viewport(s.eraseButton)
                    .metrics(new UIElement.Metrics().scale(0.33f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            // Tab buttons
            sprite = new Sprite(300f / 2250f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x808dffff).alpha(0.05f);

            StaticSprite tabGroup = new StaticSprite()
                    .viewport(s.bars.appbar())
                    .metrics(new UIElement.Metrics().anchorBottom().pan(0, -1))
                    .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                    .attach();

            sprite = new Sprite(1f / 270f, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x808dffff).alpha(0.05f);
            new StaticSprite()
                    .viewport(tabGroup)
                    .metrics(new UIElement.Metrics().anchorBottom())
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .attach();

            float x2 = 0.35f;
            float x1 = 0.12f;

            sprite = new Sprite(10f / 128f, Material.load("apps/calls/tab-select.png"));

            Animation tabPressedAnim = new ColorAnim(0.1f, null, new LinearGraph(0.3f, 0.7f), false);
            Animation tabReleasedAnim = new ColorAnim(0.1f, null, new LinearGraph(0.7f, 0f), false);
            Animation tabInactiveAnim = new ColorAnim(1, null, ConstantGraph.zero);

            Clickable tabButton = new Clickable()
                    .metrics(new UIElement.Metrics().scale(420f / 2250f).anchorTop())
                    .visuals(sprite, SaraRenderer.TARGET_APPBAR)
                    .inputPadding(0.2f, 0.3f, 0.2f, 0.8f)
                    .sound(Sound.load("sounds/general_changetab.ogg"))
                    ;
            StaticSprite tabIcon = new StaticSprite()
                    .viewport(tabButton)
                    .metrics(new UIElement.Metrics().scale(153f / 420f).move(0, -148f / 420f))
                    .target(SaraRenderer.TARGET_APPBAR)
                    .attach();

            // Favourites button
            s.tabFavourites = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabFavourites.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabFavourites.metrics.move(-x2, 0);
            s.tabFavourites.find(tabIcon).visual(Sprite.load("apps/calls/tab-favs-inactive.png"));

            // Recents button
            s.tabRecents = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabRecents.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabRecents.metrics.move(-x1, 0);
            s.tabRecents.find(tabIcon).visual(Sprite.load("apps/calls/tab-recents-inactive.png"));

            // Contacts button
            s.tabContacts = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabContacts.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabContacts.metrics.move(+x1, 0);
            s.tabContacts.find(tabIcon).visual(Sprite.load("apps/calls/tab-contacts-inactive.png"));

            // Dialer button
            s.tabDialer = tabButton.instantiate().viewport(tabGroup).attach();
            s.tabDialer.disable();
//            s.tabDialer.animation(null, tabInactiveAnim, tabPressedAnim, tabReleasedAnim, null);
            s.tabDialer.metrics.move(+x2, 0);
            s.tabDialer.find(tabIcon).visual(Sprite.load("apps/calls/tab-dial-active.png"));


            s.keyButtons = new Clickable[] {
                    key1,
                    key2,
                    key3,
                    key4,
                    key5,
                    key6,
                    key7,
                    key8,
                    key9,
                    keyStar,
                    key0,
                    keyHash
            };
            s.keySounds = new Audio.Sound[] {
                    Sound.load("sounds/phone_numpad_1.ogg"),
                    Sound.load("sounds/phone_numpad_2.ogg"),
                    Sound.load("sounds/phone_numpad_3.ogg"),
                    Sound.load("sounds/phone_numpad_4.ogg"),
                    Sound.load("sounds/phone_numpad_5.ogg"),
                    Sound.load("sounds/phone_numpad_6.ogg"),
                    Sound.load("sounds/phone_numpad_7.ogg"),
                    Sound.load("sounds/phone_numpad_8.ogg"),
                    Sound.load("sounds/phone_numpad_9.ogg"),
                    Sound.load("sounds/phone_numpad_star.ogg"),
                    Sound.load("sounds/phone_numpad_0.ogg"),
                    Sound.load("sounds/phone_numpad_hash.ogg"),
            };
            s.keyCharacters = new char[] {
                    '1',
                    '2',
                    '3',
                    '4',
                    '5',
                    '6',
                    '7',
                    '8',
                    '9',
                    '*',
                    '0',
                    '#'
            };
            s.maxDialedLength = 13;

            s.backSound = Sound.load("sounds/phone_backspace.ogg");

            s.voiceMailName = "Voicemail";

        }


        // Commit
        screen.setInternal(s);
    }

}
