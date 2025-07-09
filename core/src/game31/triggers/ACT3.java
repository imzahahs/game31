package game31.triggers;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Keyboard;
import game31.app.chats.WhatsupContact;
import game31.gb.flapee.GBDemoLevel;
import game31.gb.flapee.GBDemonLevel;
import game31.glitch.MpegGlitch;
import game31.renderer.SaraRenderer;
import sengine.Sys;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.VibrationGraph;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.ui.StaticSprite;
import sengine.ui.Toast;

/**
 * Created by Azmi on 5/18/2017.
 */

public class ACT3 extends Globals {
    private static final String TAG = "ACT3";

    public static void load() {

        if (Globals.checkAllAssets) {
//            grid.flapeeBirdApp.loadLevel(GBDemonLevel.class);
//            grid.flapeeBirdApp.loadLevel(GBDemoLevel.class);
        }

        // Preload triggered sound clips
        Sound.load("sounds/flapee/jumpscare_2.ogg");

    }

    public static float a3_teddy_beat_score_delay = 3f;


    public static JumpscareScreen createJumpscare2() {
        final JumpscareScreen jumpscare = new JumpscareScreen(
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/jumpscare_2_1.png"), false, 0.25f),
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/jumpscare_2_2.png"), false, 0.25f),
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/jumpscare_2_3.png"), false, 0.1f)
        );

        jumpscare.animation(
                new CompoundAnim(0.4f,
                        new ColorAnim(1f, new VibrationGraph(1f, new QuadraticGraph(50f, 0f, true), ConstantGraph.one), null),
                        new ScaleAnim(1f, new VibrationGraph(1f, new QuadraticGraph(3.4f, 0f, true), ConstantGraph.one))
                ),
                new ColorAnim(0.1f, new QuadraticGraph(30f, 1f, true), null),
                new CompoundAnim(0.4f,
                        new ColorAnim(1f, new VibrationGraph(1f, 10f, -5f), null),
                        new ScaleAnim(1f, new VibrationGraph(1f, 0.3f, 1f))
                ),
                new SequenceAnim(
                        new ScaleAnim(0.03f),
                        new ColorAnim(0.14f, 1.2f, 1.2f, 1.2f, 1, false),
                        new ScaleAnim(0.10f),
                        new ColorAnim(0.14f, 1.0f, 1.0f, 1.0f, 1, false),
                        new ScaleAnim(0.07f),
                        new ColorAnim(0.04f, 1.2f, 1.2f, 1.2f, 1, false)
                )
        );

        final MpegGlitch endGlitch = new MpegGlitch("sounds/flapee/jumpscare_2.ogg", null);
        endGlitch.setGlitchGraph(
                new QuadraticGraph(4.0f, 0.5f, 1.5f, 0, true),
                false,
                new CompoundGraph(
                        new ConstantGraph(0.5f, 2.5f),
                        new LinearGraph(0.5f, 0f, 2f)
                )
        );
        endGlitch.setScreenBgAnim(new ColorAnim(2f, QuadraticGraph.zeroToOne, null));

        jumpscare.glitch(endGlitch);

        return jumpscare;
    }

    public static void jumpscareTransitionToTeddy(float time, final String unlockTag, final String contactToDelete) {
        if(!grid.unlockState("ACT3.jumpscareTransitionToTeddy"))
            return;         // done
        grid.addTrigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                return false;       // Don't allow to leave thread screen
            }
        });

        final JumpscareScreen jumpscare = createJumpscare2();
        jumpscare.load();

        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                // Jumpscare image
                jumpscare.open();

                // Open teddy chat
                WhatsupContact teddy = grid.whatsupApp.findContact("Teddy");
                grid.whatsupApp.threadScreen.open(teddy);

                // Remove lock
                grid.removeTrigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN);

                // Unlock next chat
                grid.unlockState(unlockTag);

                // Delete group chat
                WhatsupContact group = grid.whatsupApp.findContact(contactToDelete);
                group.tree.pastMessages.clear();        // Delete all history
                grid.whatsupApp.refreshContact(group);
            }
        }, time);
    }


    public static void scheduleTeddyBeatHighScore() {
        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                grid.flapeeKickNotifyScreen.setOnFinished(new Runnable() {
                    @Override
                    public void run() {
                        teddyBeatHighScore();
                    }
                });
                // Show beat score notification
                grid.flapeeKickNotifyScreen.open();
            }
        }, a3_teddy_beat_score_delay);
    }

    private static void teddyBeatHighScore() {
        // Add new high scores
//        grid.flapeeBirdApp.loadLevel(GBHalfDemonLevel.class);         // TODO: lock flapee bird

        // Reset state
        grid.state.set("chats.teddy.flapee_score_beat", false);

        // Continue teddy chat
        grid.state.set("chats.teddy.a3_after_score_beat", true);

        // Mark as session 3
        grid.state.set("chats.teddy.flapee_session_2", false);

        // Remove previous effect
        ACT2.endGloomEffect();
        ACT2.endBlissEffect();

        // Enter the bliss
        GloomEffect gloomEffect = new GloomEffect(
                "sounds/flapee/gloom_2.ogg", true,
                0.3f,
                0.03f,
                0.03f,
                +0.3f,
                0.5f,
                1.0f,
                1.0f,
                new CompoundGraph(
                        new QuadraticGraph(0.12f, 0.01f, 6f, 0, true),
                        new QuadraticGraph(0.01f, 0.12f, 6f, 0, false)
                ),
                new LinearGraph(0f, 1f, 3f),
                new LinearGraph(1f, 0f, 15f)
        );
        gloomEffect.attach(grid);

        // Sound for transition
        gloomEffect.playVoice("content/vo/demon/giggle_6.ogg");
    }

    public static void teddySharedShowdownChallenge(final boolean teddyTrusts) {
        // Switch states
        grid.state.set("chats.teddy.flapee_session_2", false);
        grid.state.set("chats.teddy.flapee_session_3", true);

        // Load demon level
        grid.flapeeBirdApp.loadLevel(GBDemonLevel.class);
        grid.flapeeBirdApp.setPlayerScore(0);       // reset

        grid.flapeeBirdApp.configureInvites(null);         // can't share during showdown

        // Done updating
        grid.flapeeBirdApp.stopUpdating();

        // Start showdown mode
        if(teddyTrusts)
            grid.flapeeBirdApp.startShowdown("teddy_trust");
        else
            grid.flapeeBirdApp.startShowdown("teddy_no_trust");

        // Deplete all purchased eggs
        int eggs = grid.flapeeBirdApp.getEggs();
        int eggsPurchased = grid.flapeeBirdApp.getEggsPurchased();
        if(eggsPurchased > eggs)
            eggsPurchased = eggs;
        if(eggsPurchased > 0)
            grid.flapeeBirdApp.setEggs(eggs - eggsPurchased);

        // Configure showdown triggers
        grid.addTrigger(Globals.TRIGGER_FLAPEE_SHOWDOWN_GIVEUP, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                grid.removeTrigger(name);
                endFlapeeSession3(teddyTrusts, false);      // Continue to end
                return false;       // ignored
            }
        });

        grid.addTrigger(Globals.TRIGGER_FLAPEE_SHOWDOWN_WON, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                grid.removeTrigger(name);
                endFlapeeSession3(teddyTrusts, true);      // Continue to end
                return false;       // ignored
            }
        });
    }

    public static void finishedFlapeeSession3() {
        // No bliss for session 3
    }

    private static void endFlapeeSession3(boolean teddyTrusts, boolean playerActuallyWon) {
        // Transition to teddy's chat
        WhatsupContact teddy = grid.whatsupApp.findContact("Teddy");
        grid.whatsupApp.threadScreen.open(teddy);

        // Achievement
        if(playerActuallyWon)
            ACT1.unlockAchievement(Achievement.SHOWDOWN_WIN);
        else
            ACT1.unlockAchievement(Achievement.SHOWDOWN_GIVEUP);

        boolean playerUsedSubscription = grid.flapeeBirdApp.isUsingSubscription();

        String voiceTrack;
        final String tagToUnlock;
        final String videoToWatch;

        final boolean isNormalEnding;

        if(teddyTrusts) {
            // Teddy trusted player
            if(playerActuallyWon) {
                if(playerUsedSubscription) {
                    voiceTrack = "content/vo/demon/session3A_acceptsub_winshowdown.ogg";
                    tagToUnlock = "chats.teddy.ending_trust_with_subscription";
                    videoToWatch = "Ending/player-dies";
                    isNormalEnding = true;
                }
                else {
                    voiceTrack = "content/vo/demon/session3A_deniedsub_winshowdown.ogg";
                    tagToUnlock = "chats.teddy.ending_trust_without_subscription";
                    videoToWatch = "Ending/both-survive";
                    isNormalEnding = false;
                }
            }
            else {
                voiceTrack = "content/vo/demon/session3A_deniedsub_giveup.ogg";
                tagToUnlock = "chats.teddy.ending_trust_gave_up";
                videoToWatch = null;
                isNormalEnding = true;
            }
        }
        else {
            // Teddy doesn't trust player
            if(playerActuallyWon) {
                if(playerUsedSubscription) {
                    voiceTrack = "content/vo/demon/session3B_acceptsub_winshowdown.ogg";
                    tagToUnlock = "chats.teddy.ending_no_trust_with_subscription";
                    videoToWatch = null;
                    isNormalEnding = true;
                }
                else {
                    voiceTrack = "content/vo/demon/session3B_deniedsub_winshowdown.ogg";
                    tagToUnlock = "chats.teddy.ending_no_trust_without_subscription";
                    videoToWatch = null;
                    isNormalEnding = true;
                }
            }
            else {
                voiceTrack = "content/vo/demon/session3B_deniedsub_giveup.ogg";
                tagToUnlock = "chats.teddy.ending_no_trust_gave_up";
                videoToWatch = null;
                isNormalEnding = true;
            }
        }

        final MusicHandler ambientHandler = new MusicHandler("sounds/flapee/theme-demon100-jumpscare.ogg", 0f, 20f, 0.3f);
        ambientHandler.attach(grid);

        // Transition
        FlapeeEndingTransition transition = new FlapeeEndingTransition(
                grid.flapeeBirdApp, grid.whatsupApp.threadScreen, grid.screensGroup,
                2f, 2f, voiceTrack
        );
        transition.setOnFinished(new Runnable() {
            @Override
            public void run() {
                // Lock to thread screen
                grid.addTrigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN, new Grid.Trigger() {
                    @Override
                    public boolean trigger(String name) {
                        return false;
                    }
                });

                // Fade out theme
                ambientHandler.detachWithAnim();

                if(!isNormalEnding) {
                    // Reset flapee bird for best ending
                    grid.flapeeBirdApp.clearShowdown();
                    grid.flapeeBirdApp.setAdvancedPlayer(false);
                    grid.flapeeBirdApp.loadLevel(GBDemoLevel.class);
                }

                // Show video if needed
                if(videoToWatch != null) {
                    grid.scheduleRunnable(new Runnable() {
                        @Override
                        public void run() {
                            String codePath = null;
                            String audioCodePath = null;
                            if(!isNormalEnding) {
                                // Good ending, show gateway codes
                                codePath = "content/codes/code-3.png";
                                audioCodePath = "sounds/flapee/morse-1.ogg";
                            }

                            grid.gatewayAdScreen.show(
                                    videoToWatch,
                                    codePath,
                                    audioCodePath,
                                    true,
                                    false);
                            grid.gatewayAdScreen.setOnFinished(new Runnable() {
                                @Override
                                public void run() {
                                    // Tell teddy to continue
                                    grid.unlockState(tagToUnlock);
                                }
                            });

                            grid.gatewayAdScreen.open();
                        }
                    }, 3f);     // wait a while
                }
                else {
                    grid.unlockState(tagToUnlock);
                }
            }
        });

        // Start transition
        transition.attach(grid);
    }

    public static void endingBothSurviveStartTheme() {
        Audio.playMusic("sounds/theme.ogg", true, 0);
        new MusicFadeInTime(10f, 0.3f).attach(grid);
    }

    public static void endingBothSurvive() {
        // Fade out thread screen
        Sprite sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        final StaticSprite fadeView = new StaticSprite()
                .viewport(grid.whatsupApp.threadScreen.viewport)
                .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                .animation(
                        new FadeAnim(2f, LinearGraph.zeroToOne),
                        null,
                        null
                )
                .passThroughInput(false)
                ;

        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                grid.inputEnabled = false;      // stop all input
                fadeView.attach();
                grid.scheduleRunnable(new Runnable() {
                    @Override
                    public void run() {
                        fadeView.detach();
                        openStatScreen(true, false, false);
                    }
                }, 3f);
            }
        }, 3f);

    }

    public static void endingPlayerDiesTeddySurvives() {
        // Fade out thread screen
        Sprite sprite = new Sprite(Sys.system.getLength(), SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        final StaticSprite fadeView = new StaticSprite()
                .viewport(grid.screen.viewport)
                .animation(new FadeAnim(1.5f, LinearGraph.zeroToOne), null, new FadeAnim(2f, LinearGraph.oneToZero))
                .visual(sprite, SaraRenderer.TARGET_SCREEN)
                .passThroughInput(false)
                ;

        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                grid.inputEnabled = false;      // stop all input
                fadeView.attach();
                MusicHandler ambience = grid.iterate(null, MusicHandler.class, false, null);
                if(ambience != null)
                    ambience.detachWithAnim();
                grid.scheduleRunnable(new Runnable() {
                    @Override
                    public void run() {
                        // Stop lcd burn effect
                        LcdBurnEffect effect = getLcdBurnEffect(false);
                        if(effect != null)
                            effect.detach();

                        // Fade back in screen
                        fadeView.detachWithAnim();
                        // Fade back in glow
                        grid.screen.animateGlow(new FadeAnim(5f, LinearGraph.zeroToOne), null);

                        openStatScreen(false, true, false);
                    }
                }, 3f);
            }
        }, 24);
    }

    public static void endingBothDie() {
        scheduleTeddyDiesVideo(24f, true, false);
    }

    public static void endingTeddyDiesPlayerSurvives(boolean givesUp) {
        scheduleTeddyDiesVideo(24f, false, givesUp);
    }

    private static void scheduleTeddyDiesVideo(float time, final boolean usedSubscription, final boolean givesUp) {
        Sprite sprite = new Sprite(Sys.system.getLength(), SaraRenderer.renderer.coloredMaterial);
        ColorAttribute.of(sprite).set(0x000000ff);
        final StaticSprite fadeView = new StaticSprite()
                .viewport(grid.screen.viewport)
                .animation(new FadeAnim(1.5f, LinearGraph.zeroToOne), null, new FadeAnim(2f, LinearGraph.oneToZero))
                .visual(sprite, SaraRenderer.TARGET_SCREEN)
                ;

        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                fadeView.attach();
                MusicHandler ambience = grid.iterate(null, MusicHandler.class, false, null);
                if(ambience != null)
                    ambience.detachWithAnim();
            }
        }, time - 3f);
        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                // Play video
                grid.photoRollApp.fullVideoScreen.show("Ending/teddy-dies");
                grid.photoRollApp.fullVideoScreen.play(new Runnable() {
                    @Override
                    public void run() {
                        grid.photoRollApp.fullVideoScreen.detach();
                        openStatScreen(false, usedSubscription, givesUp);
                    }
                });
                grid.screensGroup.detachChilds();
                grid.photoRollApp.fullVideoScreen.attach(grid.screensGroup);
                // Stop lcd burn effect
                LcdBurnEffect effect = getLcdBurnEffect(false);
                if(effect != null)
                    effect.detach();
                // Fade back in screen
                fadeView.detachWithAnim();
                // Fade back in glow
                grid.screen.animateGlow(new FadeAnim(5f, LinearGraph.zeroToOne), null);
            }
        }, time);
    }

    private static void openStatScreen(final boolean backToHomescreen, final boolean usedSubscription, final boolean givesUp) {
        grid.inputEnabled = true;       // enable input back

        grid.statMenu.show(
                new Runnable() {
                    @Override
                    public void run() {
                        // Configure SIM1 trailer menu
                        grid.simTrailerMenu.setOnClose(new Runnable() {
                            @Override
                            public void run() {
                                if(backToHomescreen) {
                                    Sprite sprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);
                                    ColorAttribute.of(sprite).set(0x000000ff);
                                    new Toast()
                                            .viewport(grid.homescreen.viewport)
                                            .visual(sprite, SaraRenderer.TARGET_OVERLAY_DIALOG)
                                            .animation(
                                                    null,
                                                    null,
                                                    new FadeAnim(3f, LinearGraph.oneToZero)
                                            )
                                            .attach();

                                    // Go back to homescreen (good ending extras)
                                    grid.simTrailerMenu.detach();
                                    grid.homescreen.attach(grid.screensGroup);

                                    grid.removeTrigger(TRIGGER_LEAVE_CHAT_THREAD_SCREEN);

                                    new MusicFadeOutEntity(0f, 5f).attach(grid);

                                    // Start A4
                                    grid.unlockState("chats.teddy.a4_started");
                                }
                                else {
                                    // Open discord if not yet opened
                                    if(Gdx.app.getType() != Application.ApplicationType.Desktop) {
                                        boolean hasOpenedDiscord = Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_HAS_DISCORD_OPENED, false);
                                        boolean hasEnjoyedGame = Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_HAS_OPINION_ENJOYED, false);

                                        if (!hasOpenedDiscord && hasEnjoyedGame) {
                                            //Gdx.net.openURI(Globals.helpDiscordURL);
                                            Game.game.platform.openURI(Globals.helpDiscordURL);

                                            Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_HAS_DISCORD_OPENED, true).flush();
                                        }
                                    }

                                    // Show back main menu
                                    grid = new Grid(true);
                                    Sys.system.activate(grid);
                                }
                            }
                        });

                        // Transition from stat menu to credits
                        grid.statMenu.detach();
                        grid.creditsMenu.show(new Runnable() {
                            @Override
                            public void run() {
                                // Transition from credits to sim1 trailer menu
                                grid.creditsMenu.detach();
                                grid.simTrailerMenu.attach(grid.screensGroup);

                                // Fade out theme music
                                MusicFadeOutEntity fadeOutEntity = new MusicFadeOutEntity(0, 3f);
                                fadeOutEntity.attach(grid);
                            }
                        });
                        grid.creditsMenu.attach(grid.screensGroup);
                    }
                },
                usedSubscription,
                givesUp
        );
        grid.screensGroup.detachChilds();
        grid.statMenu.attach(grid.screensGroup);

        // Theme
        Audio.playMusic("sounds/theme.ogg", true);
    }


    private static LcdBurnEffect getLcdBurnEffect(boolean isRequired) {
        LcdBurnEffect effect = grid.iterate(null, LcdBurnEffect.class, false, null);
        if(effect == null && isRequired) {
            effect = new LcdBurnEffect();
            effect.attach(grid);
        }
        return effect;
    }

    public static void startLcdBurnEffect1() {
        LcdBurnEffect effect = getLcdBurnEffect(true);
        effect.startGraph(
                new LinearGraph(0f, 0.95f, 20f),
                new ConstantGraph(1.25f)
        );
        MusicHandler ambience = new MusicHandler("sounds/flapee/burn-start.ogg", 10f, 10f, 0.3f);
        ambience.attach(effect);
        // Fade out screen background
        grid.screen.animateBackground(new FadeAnim(3f, LinearGraph.oneToZero), new FadeAnim(0f));
    }

    public static void startLcdBurnEffect2() {
        LcdBurnEffect effect = getLcdBurnEffect(true);
        effect.startGraph(
                new LinearGraph(0.95f, 1f, 10f),
                new CompoundGraph(
                        new QuadraticGraph(1.25f, 2f, 10f, 0f, true),
                        new LinearGraph(2f, 10f, 30f)
                )
        );
        MusicHandler ambience = effect.iterate(null, MusicHandler.class, false, null);
        if(ambience != null)
            ambience.detachWithAnim();
        ambience = new MusicHandler("sounds/flapee/burn-end.ogg", 3f, 3f, 1f);
        ambience.attach(effect);
        // Fadeout screen glow
        grid.screen.animateGlow(new FadeAnim(3f, LinearGraph.oneToZero), new FadeAnim(0f));
        grid.notification.hideAccessView();
        grid.inputEnabled = false;      // stop all input
    }

    public static void endLcdBurnEffect() {
        LcdBurnEffect effect = getLcdBurnEffect(false);
        if(effect != null)
            effect.detach();
    }

    private static void restoreNormalGameSpeed() {
        if(tChatTimingMultiplier < 0.9f) {
            tChatTimingMultiplier = 0.9f;
            tKeyboardAnimationSpeedMultiplier = 1.0f;
            grid.keyboard.detach();
            grid.keyboard = new Keyboard();
        }
    }

    public static void openFlapeeShowdown() {
        // Just switch and open flapee bird app
        grid.homescreen.switchApp(Globals.CONTEXT_APP_FLAPEE);
    }


}
