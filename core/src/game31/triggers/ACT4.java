package game31.triggers;

import com.badlogic.gdx.Gdx;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.app.browser.BrowserScreen;
import game31.glitch.MpegGlitch;
import game31.renderer.SaraRenderer;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.NullAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.VibrationGraph;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;

public class ACT4 extends Globals {
    private static final String TAG = "ACT4";

    public static void load() {

        if (Globals.checkAllAssets) {
            Sprite.load("content/scares/gateway-forum.png");
            Sprite.load("content/scares/gateway-boxdrop.png");
            Sprite.load("content/scares/gateway-hex.png");
        }

        // Preload sounds
        Sound.load("sounds/glitch_start_low.ogg");
        Sound.load("sounds/glitch_start_medium.ogg");
        Sound.load("sounds/glitch_start_strong.ogg");

        Sound.load("sounds/general_invalid.ogg");

    }

    public static final String STATE_SEEN_PATHOFDECAY_PAGE = "STATE_SEEN_PATHOFDECAY_PAGE";
    public static final String STATE_SHOWED_PATHOFDECAY_JUMPSCARE = "STATE_SHOWED_PATHOFDECAY_JUMPSCARE";
    public static final String STATE_SEEN_GATEWAY_PAGE = "STATE_SEEN_GATEWAY_PAGE";
    public static final String STATE_SEEN_CBB_GREG_SUMMERS_PAGE = "STATE_SEEN_CBB_GREG_SUMMERS_PAGE";
    public static final String STATE_SEEN_CBB_DUNCAN_YONG_PAGE = "STATE_SEEN_CBB_DUNCAN_YONG_PAGE";

    public static final String STATE_GIVEN_INTO_CREDIT_CARD_SCAM = "STATE_GIVEN_INTO_CREDIT_CARD_SCAM";

    public static void browserGatewayGlitch() {
        // Glitch
        MpegGlitch clickGlitch = new MpegGlitch("sounds/glitch_start_low.ogg", null);
        clickGlitch.setGlitchGraph(null, false, new QuadraticGraph(3.0f, 0.14f, 1.5f, 0, true));
        clickGlitch.attach(grid);
        clickGlitch.detachWithAnim();
    }

    public static void solvedExtension() {
        final JumpscareScreen jumpscare = new JumpscareScreen(
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/gateway-hex.png"), false, 0.6f)
        );

        jumpscare.animation(
                new CompoundAnim(0.4f,
                        new ColorAnim(1f, new VibrationGraph(1f, new QuadraticGraph(50f, 0f, true), ConstantGraph.one), null),
                        new ScaleAnim(1f, new VibrationGraph(1f, new QuadraticGraph(3.4f, 0f, true), ConstantGraph.one))
                ),
                new ColorAnim(0.2f, new QuadraticGraph(10f, 1f, true), null),
                new CompoundAnim(0.4f,
                        new ColorAnim(1f, new VibrationGraph(1f, 10f, -5f), null),
                        new ScaleAnim(1f, new VibrationGraph(1f, 0.3f, 1f))
                ),
                new SequenceAnim(
                        new ScaleAnim(0.03f),
                        new ColorAnim(0.09f, 1.2f, 1.2f, 1.2f, 1, false),
                        new ScaleAnim(0.10f),
                        new ColorAnim(0.14f, 1.0f, 1.0f, 1.0f, 1, false),
                        new ScaleAnim(0.07f),
                        new ColorAnim(0.04f, 1.2f, 1.2f, 1.2f, 1, false)
                )
        );


        MpegGlitch glitch = new MpegGlitch("sounds/glitch_start_strong.ogg", null);
        glitch.setGlitchGraph(
                new QuadraticGraph(4.0f, 0.5f, 1.5f, 0, true),
                false,
                new CompoundGraph(
                        new ConstantGraph(0.5f, 2.5f),
                        new LinearGraph(0.5f, 0f, 2f)
                )
        );
        glitch.setScreenBgAnim(new ColorAnim(2f, QuadraticGraph.zeroToOne, null));

        jumpscare.glitch(glitch);

        jumpscare.load();

        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                jumpscare.attach(grid.screensGroup);
                grid.scheduleRunnable(new Runnable() {
                    @Override
                    public void run() {
                        // Achievement
                        ACT1.unlockAchievement(Achievement.ARG_PHONECALL);
                    }
                }, 5f);
            }
        }, 5f);
    }

    public static void browserSeenPathOfDecayPage() {
        if (grid.unlockState(STATE_SEEN_PATHOFDECAY_PAGE)) {
            // Add bookmark
            grid.browserApp.insertBookmarks(0, "pathofdecay.web");
            // Achievement
            ACT1.unlockAchievement(Achievement.ARG_FRONT_WEBSITE);

            // Analytics
            Game.analyticsStartLevel("ARG 2: Gateway 31");
        }

        if(grid.isStateUnlocked(STATE_SHOWED_PATHOFDECAY_JUMPSCARE))
            return;     // no more jumpscare

        Sprite sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0xffffff00);
        final BrowserPageTouchHandler touchHandler = new BrowserPageTouchHandler(12f);
        touchHandler.animation(
                null,
                null,
                new SequenceAnim(
                        new NullAnim(4f),
                        new ColorAnim(7f, null, new QuadraticGraph(0f, 1f, false), false)
                ),
                null,
                new ColorAnim(2f, null, new LinearGraph(1f, 0f), false)
        );
        touchHandler.visuals(sprite, SaraRenderer.TARGET_INTERACTIVE_TEXT_EFFECT);
        touchHandler.attach();

        touchHandler.onActivated(new Runnable() {
            @Override
            public void run() {
                browserHoldOnDecayPage();
                touchHandler.cancelTouch();
                touchHandler.detachWithAnim();
            }
        });

        grid.addTrigger(TRIGGER_LEAVING_BROWSER_PAGE, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                grid.removeTrigger(name);
                touchHandler.detach();
                return true;        // ignored
            }
        });
    }

    private static void browserHoldOnDecayPage() {
        grid.unlockState(STATE_SHOWED_PATHOFDECAY_JUMPSCARE);

        final JumpscareScreen jumpscare = new JumpscareScreen(
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/gateway-forum.png"), false, 0.8f),
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/gateway-boxdrop.png"), false, 0.3f)
        );

        jumpscare.animation(
                new CompoundAnim(0.4f,
                        new ColorAnim(1f, new VibrationGraph(1f, new QuadraticGraph(50f, 0f, true), ConstantGraph.one), null),
                        new ScaleAnim(1f, new VibrationGraph(1f, new QuadraticGraph(3.4f, 0f, true), ConstantGraph.one))
                ),
                new ColorAnim(0.2f, new QuadraticGraph(10f, 1f, true), null),
                new ColorAnim(1f, new VibrationGraph(1f, 3f, 0.5f), null),
                new SequenceAnim(
                        new ScaleAnim(0.03f),
                        new ColorAnim(0.09f, 1.2f, 1.2f, 1.2f, 1, false),
                        new ScaleAnim(0.10f),
                        new ColorAnim(0.14f, 1.0f, 1.0f, 1.0f, 1, false),
                        new ScaleAnim(0.07f),
                        new ColorAnim(0.04f, 1.2f, 1.2f, 1.2f, 1, false)
                )
        );


        MpegGlitch glitch = new MpegGlitch("sounds/glitch_start_medium.ogg", null);
        glitch.setGlitchGraph(
                new QuadraticGraph(4.0f, 0.5f, 1.5f, 0, true),
                false,
                new CompoundGraph(
                        new ConstantGraph(0.5f, 2.5f),
                        new LinearGraph(0.5f, 0f, 2f)
                )
        );
        glitch.setScreenBgAnim(new ColorAnim(2f, QuadraticGraph.zeroToOne, null));

        jumpscare.glitch(glitch);

        jumpscare.load();

        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                jumpscare.attach(grid.screensGroup);

                // Unlock Gateway Reyes email
                grid.unlockState("mail.gateway_reyes.unlocked");
            }
        }, 3f);
    }


    public static void browserDecayPageClickedSubmitForm() {
        BrowserScreen browser = grid.browserApp;

        String name = browser.getInput("name");
        if (name == null || name.isEmpty()) {
            browser.activateField("name");
            return;
        }

        String code = browser.getInput("code");
        if (code == null || code.isEmpty()) {
            browser.activateField("code");
            return;
        }


        String desire = browser.getInput("desire");
        if (desire == null || desire.isEmpty()) {
            browser.activateField("desire");
            return;
        }

        // Trim and cleanup
        name = name.replaceAll("\\s+", " ").trim();
        code = code.replaceAll("\\s+", " ").trim();
        desire = desire.replaceAll("\\s+", " ").trim();

        if (name.equalsIgnoreCase("Aziz Ainsworth") &&
                (code.equalsIgnoreCase("WQR285") || code.equalsIgnoreCase("WOR285")) &&
                (desire.equalsIgnoreCase("Self Destruction") || desire.equalsIgnoreCase("Self-Destruction") || desire.equalsIgnoreCase("SelfDestruction"))
                ) {
            // Open with glitch
            browser.openPage("3.141.59.26");

            // Glitch
            MpegGlitch glitch = new MpegGlitch("sounds/glitch_start_low.ogg", null);
            glitch.setGlitchGraph(null, false, new QuadraticGraph(3.0f, 0.14f, 1.5f, 0, true));
            glitch.attach(grid);
            glitch.detachWithAnim();
        } else {
            // Open not found
            browser.openPage("surfer://404");
        }
    }


    public static void browserSeenGatewayPage() {
        if (grid.unlockState(STATE_SEEN_GATEWAY_PAGE)) {
            // Add bookmark
            grid.browserApp.insertBookmarks(0, "3.141.59.26");
            // Achievement
            ACT1.unlockAchievement(Achievement.ARG_GATEWAY_WEBSITE);

            // Analytics
            Game.analyticsEndLevel("ARG 2: Gateway 31");

            // Open url on back
            grid.addTrigger(Globals.TRIGGER_LEAVING_BROWSER_SCREEN, new Grid.Trigger() {
                @Override
                public boolean trigger(String name) {
                    grid.removeTrigger(name);

                    String url = Globals.helpKaiganURL + "therippleman";
                    Gdx.net.openURI(url);

                    return true;
                }
            });
        }
    }


    public static void browserSeenCbbGregSummersPage() {
        if (grid.unlockState(STATE_SEEN_CBB_GREG_SUMMERS_PAGE)) {
            // Add bookmark
            grid.browserApp.insertBookmarks(0, "cbb.web/profiles/greg-summers");
        }
    }

    public static void browserSeenCbbDuncanYongPage() {
        if (grid.unlockState(STATE_SEEN_CBB_DUNCAN_YONG_PAGE)) {
            // Add bookmark
            grid.browserApp.insertBookmarks(0, "cbb.web/profiles/duncan-yong");
            // Achievement
            ACT1.unlockAchievement(Achievement.GAMEMAKER);
        }
    }


    public static void browserSeenKarenScamWebsite() {
        if(grid.isStateUnlocked(STATE_GIVEN_INTO_CREDIT_CARD_SCAM))
            return;     // already given in

        // Glitch
        final MpegGlitch glitch = new MpegGlitch("sounds/glitch_start_medium.ogg", "sounds/flapee/alarm.ogg");
        glitch.setGlitchGraph(
                new QuadraticGraph(4.0f, 0.5f, 1.5f, 0, true),
                false,
                new LinearGraph(0.5f, 0f, 1f)
        );
        glitch.attach(grid);

        // Glitch
        final MpegGlitch clickGlitch = new MpegGlitch("sounds/glitch_start_low.ogg", null);
        clickGlitch.setGlitchGraph(null, false, new QuadraticGraph(3.0f, 0.14f, 1.5f, 0, true));

        grid.addTrigger(TRIGGER_LEAVING_BROWSER_PAGE, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                grid.removeTrigger(TRIGGER_LEAVING_BROWSER_PAGE);
                grid.removeTrigger(TRIGGER_LEAVING_BROWSER_SCREEN);

                glitch.detachWithAnim();
                return true;        // ignored
            }
        });

        grid.addTrigger(TRIGGER_LEAVING_BROWSER_SCREEN, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                clickGlitch.attach(grid);
                clickGlitch.detachWithAnim();
                return false;       // not allowed
            }
        });
    }

    public static void browserSubmitScamCreditCard(String field) {
        String cc = grid.browserApp.getInput(field);
        if(cc != null) {
            cc = cc.replaceAll("\\s+", "");
            // Make sure all characters are numeric
            int length = cc.length();
            if(length > 4) {
                boolean isNumeric = true;
                for(int c = 0; c < length; c++) {
                    if(!Character.isDigit(cc.charAt(c))) {
                        isNumeric = false;
                        break;
                    }
                }
                if(isNumeric) {
                    // Passed the test
                    grid.unlockState(STATE_GIVEN_INTO_CREDIT_CARD_SCAM);            // Remember to not lock anymore
                    grid.browserApp.openPage("totallyfreegames.web/subscription-confirmed");
                    return;
                }
            }
        }
        // Else fail
        Sound.load("sounds/general_invalid.ogg").play();
        grid.browserApp.activateField(field);
    }


}
