package game31.gb.phone;

import com.badlogic.gdx.Gdx;

import java.util.Locale;

import game31.Globals;
import game31.ScreenBar;
import game31.app.phone.PhoneCallScreen;
import game31.renderer.SaraRenderer;
import sengine.File;
import sengine.animation.Animation;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.Range;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.materials.ColorAttribute;
import sengine.materials.MaskMaterial;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;

/**
 * Created by Azmi on 24/8/2016.
 */
public class GBPhoneCallScreen implements PhoneCallScreen.InterfaceSource {


    private Clickable createDialKey(UIElement.Metrics metrics, String key, String alphabets) {
        Clickable key1 = new Clickable()
                .metrics(metrics.scale(208f / 640f))            // 215f
//                .visuals(keyPatch, SaraRenderer.TARGET_INTERACTIVE)
                .target(SaraRenderer.TARGET_INTERACTIVE)
                .length(373f / 553f)
                .animation(null, null, keyPressedAnim, keyReleasedAnim, null);

        if(key != null) {
            TextBox key1number = new TextBox()
                    .viewport(key1)
                    .metrics(new UIElement.Metrics().scale(87f / 215f).anchor(0, +15f / 137f))
                    .text(new Text()
                            .font(keyNumberFont, SaraRenderer.TARGET_APPBAR_TEXT)
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
                            .font(keyAlphaFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(17f / 87f, 0)
                            .text(alphabets)
                    )
                    .attach();
        }

        return key1;
    }

    private Animation keyPressedAnim;
    private Animation keyReleasedAnim;
    private Font keyNumberFont;
    private Font keyAlphaFont;

    public GBPhoneCallScreen(PhoneCallScreen screen) {
        PhoneCallScreen.Internal s = new PhoneCallScreen.Internal();

        Font regularFontLarge = new Font("opensans-regular.ttf", 64);

        Font boldFont = new Font("opensans-bold.ttf", 32);

        Animation buttonPressedAnim = new ScaleAnim(0.15f, new QuadraticGraph(1f, 1.2f, 0.8f, true));
        Animation buttonReleasedAnim = new FadeAnim(0.3f, new QuadraticGraph(0f, 1f, false));

        Font titleFont = new Font("opensans-light.ttf", 48, 0xf4f9fcff);
        Font nameFont = new Font("opensans-semibold.ttf", 48, 0xf4f9fcff);
        Font buttonFont = new Font("opensans-light.ttf", 32, 0xf4f9fcff);


        keyNumberFont = new Font("opensans-light.ttf", 64, 0xf8f8f8ff);
        keyAlphaFont = new Font("opensans-light.ttf", 32, 0x8f8f8fff);
        keyPressedAnim = new ScaleAnim(0.15f, new QuadraticGraph(0.5f, 1.1f, 1.2f, true));
        keyReleasedAnim = new ScaleAnim(0.2f, new QuadraticGraph(1.1f, 1f, -0.9f, false));

        Sprite sprite;

        // Window
        {
            // Window
            sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
            ColorAttribute.of(sprite).set(0x0e162dff);

            s.window = new UIElement.Group();

            // Background
            new StaticSprite()
                    .viewport(s.window)
                    .visual(sprite, SaraRenderer.TARGET_BG)
                    .attach();

            // Profile
            s.defaultProfileSprite = Sprite.load("system/profile-big.png");
            s.profileView = new StaticSprite()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.5f).move(0, +0.27f))
                    .visual(s.defaultProfileSprite, SaraRenderer.TARGET_INTERACTIVE_OVERLAY)
                    .attach();
            s.maskMaterial = Material.load("system/circle-big.png");

            if(Globals.checkAllAssets)
                new MaskMaterial(s.maskMaterial, s.maskMaterial);       // Hack to load mask material shader

            s.callingIndicatorGroup = new UIElement.Group().viewport(s.profileView)
                    .animation(null, null, new ScaleAnim(0.2f, LinearGraph.oneToZero))
            ;
            {
                sprite = Sprite.load("system/circle-big.png").instantiate();
                ColorAttribute.of(sprite).set(0xf1f145ff);
                new StaticSprite()
                        .viewport(s.callingIndicatorGroup)
                        .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                        .animation(
                                null,
                                new ScaleAnim(1.5f, new QuadraticGraph(1f, 1.5f, true)),
                                null
                        )
                        .attach();

                new StaticSprite()
                        .viewport(s.callingIndicatorGroup)
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                        .animation(
                                null,
                                new ScaleAnim(1.5f, new SineGraph(1f, 1f, 0, 0.03f, 1.06f)),
                                null
                        )
                        .attach();

                sprite = sprite.instantiate();
                ColorAttribute.of(sprite).set(0x0e162dff);
                new StaticSprite()
                        .viewport(s.callingIndicatorGroup)
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                        .animation(
                                null,
                                new ScaleAnim(1.5f, new QuadraticGraph(0.8f, 1.6f, true)),
                                null
                        )
                        .attach();

            }

            s.incallIndicatorGroup = new UIElement.Group().viewport(s.profileView)
                    .metrics(new UIElement.Metrics())
                    .animation(null, null, new ScaleAnim(0.2f, LinearGraph.oneToZero))
            ;
            {
                sprite = Sprite.load("system/circle-big.png").instantiate();
                ColorAttribute.of(sprite).set(0x0fd32aff);
                new StaticSprite()
                        .viewport(s.incallIndicatorGroup)
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE_SUB)
                        .animation(
                                null,
                                new ScaleAnim(3f, new SineGraph(1f, 1f, 0, 0.01f, 1.05f)),
                                null
                        )
                        .attach();

            }
            s.incallStartSize = 1f;
            s.incallMaxSize = 1.1f;

            s.callEndIndicatorGroup = new UIElement.Group().viewport(s.profileView).attach();
            {
                sprite = Sprite.load("system/circle-big.png").instantiate();
                ColorAttribute.of(sprite).set(0xc11840ff);
                new StaticSprite()
                        .viewport(s.callEndIndicatorGroup)
                        .visual(sprite, SaraRenderer.TARGET_BG_SHADOWS)
                        .animation(
                                new ScaleAnim(0.8f, new QuadraticGraph(1f, 1.7f, true)),
                                null,
                                null
                        )
                        .attach();

                sprite = sprite.instantiate();
                ColorAttribute.of(sprite).set(0x0e162dff);
                new StaticSprite()
                        .viewport(s.callEndIndicatorGroup)
                        .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                        .animation(
                                new ScaleAnim(0.8f, new QuadraticGraph(0.25f, 1.8f, true)),
                                null,
                                null
                        )
                        .attach();

            }

            // Status bar
            s.bars = new ScreenBar();
            s.bars.showAppbar("Incoming Call", null);
            s.bars.attach(screen);

            s.incomingTitle = "Incoming Call";
            s.callingTitle = "Calling";


            // Dialed
            s.deviceView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.9f).move(0, -0.19f))
                    .text(new Text()
                            .font(titleFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(12f / 302f, 0)
                            .text("Incoming Call")
                    )
                    .animation(null, new FadeAnim(1f, new SineGraph(1f, 1f, 0, 0.2f, 0.8f)), null)
                    .attach();

            s.nameView = new TextBox()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().scale(0.9f).move(0, -0.10f))
                    .text(new Text()
                            .font(nameFont, SaraRenderer.TARGET_INTERACTIVE_TEXT)
                            .position(20f / 302f, 0)
                            .text("Greg")
                    )
                    .attach();



            // Decision group
            s.decisionGroup = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().move(0, +0.15f))
                    .length(0.3f)
                    .animation(
                            new MoveAnim(0.5f, null, new QuadraticGraph(-1f, 0, true)),
                            null,
                            new MoveAnim(0.5f, null, new QuadraticGraph(0, -1f, true)))
                    ; // .attach();

            s.declineButton = new Clickable()
                    .viewport(s.decisionGroup)
                    .metrics(new UIElement.Metrics().scale(0.19f).anchor(-0.24f, 0))
                    .visuals(Sprite.load("apps/calls/phone-decline.png"), SaraRenderer.TARGET_APPBAR)
                    .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null)
                    .text(new Text()
                            .font(buttonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(0, -0.8f, 1f, 0.15f, 0)
                            .text("Decline")
                    )
                    .sound(Sound.load("sounds/phone_endcall.ogg"))
                    .attach();

            s.acceptButton = new Clickable()
                    .viewport(s.decisionGroup)
                    .metrics(new UIElement.Metrics().scale(0.19f).anchor(+0.24f, 0))
                    .visuals(Sprite.load("apps/calls/phone-accept.png"), SaraRenderer.TARGET_APPBAR)
                    .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null)
                    .text(new Text()
                            .font(buttonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(0, -0.8f, 1f, 0.15f, 0)
                            .text("Accept")
                    )
                    .sound(Sound.load("sounds/phone_acceptcall.ogg"))
                    .attach();

            // Call-control group
            s.controlGroup = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().move(0, +0.15f))
                    .length(0.3f)
                    .animation(
                            new MoveAnim(0.5f, null, new QuadraticGraph(-1f, 0, true)),
                            null,
                            new MoveAnim(0.5f, null, new QuadraticGraph(0, -1f, true))
                    )
                    .attach();


            s.endButton = new Clickable()
                    .viewport(s.controlGroup)
                    .metrics(new UIElement.Metrics().scale(0.19f).anchor(0, 0))
                    .visuals(Sprite.load("apps/calls/call-end.png"), SaraRenderer.TARGET_APPBAR)
                    .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null)
                    .text(new Text()
                            .font(buttonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(0, -0.8f, 1f, 0.15f, 0)
                            .text("End")
                    )
                    .sound(Sound.load("sounds/phone_endcall.ogg"))
                    .attach();

            // Call options group
            s.callOptionsGroup = new UIElement.Group()
                    .viewport(s.window)
                    .metrics(new UIElement.Metrics().anchorBottom().move(0, +0.15f))
                    .length(0.3f)
                    .animation(
                            new MoveAnim(1f, null, new QuadraticGraph(-1f, 0, true)),
                            null,
                            new MoveAnim(1f, null, new QuadraticGraph(0, -1f, true))
                    )
                    .attach();


            s.padActiveSprite = Sprite.load("apps/calls/call-dialpad-active.png");
            s.padInactiveSprite = Sprite.load("apps/calls/call-dialpad.png");

            s.padButton = new Clickable()
                    .viewport(s.callOptionsGroup)
                    .metrics(new UIElement.Metrics().scale(0.16f).anchor(-0.3f, 0))
                    .visuals(s.padInactiveSprite, SaraRenderer.TARGET_APPBAR)
                    .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null)
                    .text(new Text()
                            .font(buttonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(0, -0.8f, 1f, 0.15f, 0)
                            .text("Dialpad")
                    )
                    .attach();

            // User input indicator
            sprite = Sprite.load("system/circle-big.png").instantiate();
            ColorAttribute.of(sprite).set(0x0fd32aff);

            s.padIndicator = new StaticSprite()
                    .viewport(s.padButton)
                    .metrics(new UIElement.Metrics().scale(1.1f))
                    .visual(sprite, SaraRenderer.TARGET_INTERACTIVE)
                    .animation(
                            null,
                            new ScaleAnim(1f, new CompoundGraph(new Graph[] {
                                    new ConstantGraph(1f, 0.5f),
                                    new ConstantGraph(0f, 0.5f)
                            })),
                            null
                    )
                    ;

            s.mutedSprite = Sprite.load("apps/calls/call-muted.png");
            s.unmutedSprite = Sprite.load("apps/calls/call-unmuted.png");
            s.muteText = "Mute";
            s.unmuteText = "Unmute";

            s.muteButton = new Clickable()
                    .viewport(s.callOptionsGroup)
                    .metrics(new UIElement.Metrics().scale(0.16f).anchor(+0.3f, 0))
                    .visuals(s.mutedSprite, SaraRenderer.TARGET_APPBAR)
                    .animation(null, null, buttonPressedAnim, buttonReleasedAnim, null)
                    .text(new Text()
                            .font(buttonFont, SaraRenderer.TARGET_APPBAR_TEXT)
                            .position(0, -0.8f, 1f, 0.15f, 0)
                            .text(s.muteText)
                    )
                    .attach();

            // Pad group
            {
                sprite = new Sprite(1.15f, SaraRenderer.renderer.coloredMaterial);
                ColorAttribute.of(sprite).set(0x091027ff);

                s.padGroup = new UIElement.Group()
                        .viewport(s.window)
                        .metrics(new UIElement.Metrics().anchorBottom().move(0, +0.465f)) // .anchorTop().move(0, -0.167f))
                        .length(sprite.length)
                        .animation(
                                new CompoundAnim(0.5f, new Animation[] {
                                        new MoveAnim(1, null, new QuadraticGraph(-sprite.getLength(), 0, true)),
                                        new ScissorAnim(1, new Animation[] {
                                                new MoveAnim(1, null, new QuadraticGraph(+sprite.getLength(), 0, true))
                                        })
                                }),
                                null,
                                new CompoundAnim(0.5f, new Animation[] {
                                        new MoveAnim(1, null, new QuadraticGraph(0, -sprite.getLength(), true)),
                                        new ScissorAnim(1, new Animation[] {
                                                new MoveAnim(1, null, new QuadraticGraph(0, +sprite.getLength(), true))
                                        })
                                })
                        );

                // Bg
                new StaticSprite()
                        .viewport(s.padGroup)
                        .visual(sprite, SaraRenderer.TARGET_APPBAR_BG)
                        .attach();

                s.dialedView = new TextBox()
                        .viewport(s.padGroup)
                        .metrics(new UIElement.Metrics().scale(0.9f).anchor(0, +0.395f))
                        .text(new Text()
                                .font(regularFontLarge, SaraRenderer.TARGET_APPBAR_TEXT)
                                .position(64f / 640f, -9f)
                                .text("911#")
                        )
                        .attach();

                // Line
                sprite = new Sprite(5f / 1500f, SaraRenderer.renderer.coloredMaterial);
                ColorAttribute.of(sprite).set(0x8c8ca4ff);

                new StaticSprite()
                        .viewport(s.dialedView)
                        .metrics(new UIElement.Metrics().anchorBottom().scale(0.9f).move(0, -0.05f))
                        .visual(sprite, SaraRenderer.TARGET_APPBAR)
                        .attach();


                float rowOffset = -0.23f;
                float row1y = +0.50f - (71f / 674f) + rowOffset;
                float row2y = +0.50f - (196f / 674f) + rowOffset;
                float row3y = +0.50f - (321f / 674f) + rowOffset;
                float row4y = +0.50f - (446f / 674f) + rowOffset;
                float row5y = +0.50f - (573f / 674f) + rowOffset;
                float rowX = 0.03f;



                Clickable key1 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row1y), "1", null)
                        .viewport(s.padGroup)
                        .attach();

                Clickable key2 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchor(0, row1y), "2", "ABC")
                        .viewport(s.padGroup)
                        .attach();

                Clickable key3 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row1y), "3", "DEF")
                        .viewport(s.padGroup)
                        .attach();

                Clickable key4 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row2y), "4", "GHI")
                        .viewport(s.padGroup)
                        .attach();

                Clickable key5 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchor(0, row2y), "5", "JKL")
                        .viewport(s.padGroup)
                        .attach();

                Clickable key6 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row2y), "6", "MNO")
                        .viewport(s.padGroup)
                        .attach();

                Clickable key7 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row3y), "7", "PQRS")
                        .viewport(s.padGroup)
                        .attach();

                Clickable key8 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchor(0, row3y), "8", "TUV")
                        .viewport(s.padGroup)
                        .attach();

                Clickable key9 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row3y), "9", "WXYZ")
                        .viewport(s.padGroup)
                        .attach();

                Clickable keyStar = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorLeft().anchor(+rowX, row4y), "*", null)
                        .viewport(s.padGroup)
                        .attach();

                Clickable key0 = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchor(0, row4y), "0", "+")
                        .viewport(s.padGroup)
                        .attach();

                Clickable keyHash = createDialKey(new UIElement.Metrics().scale(200f / 640f).anchorRight().anchor(-rowX, row4y), "#", null)
                        .viewport(s.padGroup)
                        .attach();


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
                s.maxDialedLength = 12;

                s.padShowSound = Sound.load("sounds/keyboard_open.ogg");
                s.padHideSound = Sound.load("sounds/chat_select.ogg");

            }


            s.tStartInterval = 0.4f;
            s.tEndInterval = 1f;
            s.tCallTime = new Range(2f, 1f);
            s.tUnansweredCallTime = new Range(9f, 2f);

            s.dialSound = Gdx.audio.newMusic(File.open("sounds/phone_dialing.ogg"));
            s.dialSound.setLooping(true);
            s.busySound = Gdx.audio.newMusic(File.open("sounds/phone_linebusy.ogg"));
            s.busySound.setLooping(true);

            s.endCallSound = Sound.load("sounds/phone_endcall.ogg");

            s.dialingText = "Connecting";
            s.lineBusyText = "No Answer";
            s.callEndedText = "Call Ended";
        }

        // Commit
        screen.setInternal(s);
    }

    public String convertSeconds(int seconds) {
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }
}
