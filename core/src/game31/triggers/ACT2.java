package game31.triggers;

import game31.Globals;
import game31.Grid;
import game31.ScreenTransition;
import game31.app.chats.WhatsupContact;
import game31.app.flapee.DialogConfig;
import game31.app.flapee.FlapeeAdScreen;
import game31.app.flapee.FlapeeBirdScreen;
import game31.gb.flapee.GBHalfDemonLevel;
import game31.glitch.MpegGlitch;
import sengine.Sys;
import sengine.animation.Animation;
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
import sengine.calc.Range;
import sengine.calc.VibrationGraph;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 5/18/2017.
 */

public class ACT2 extends Globals {
    private static final String TAG = "ACT2";

    public static void load() {

        if (checkAllAssets) {
//            grid.flapeeBirdApp.loadLevel(GBHalfDemonLevel.class);

        }

        // Preload triggered sound clips
        Sound.load("sounds/flapee/jumpscare_1.ogg");
    }

    public static final String STATE_TEDDY_CLARITY_GONE = "STATE_TEDDY_CLARITY_GONE";


    public static float a2_teddy_call_retry_delay = 5f;
    public static float a2_teddy_beat_score_delay = 6f;

    public static void teddyPhoneCall1() {

        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                grid.phoneApp.handleCall(
                        "calls.teddy.a2_call1",
                        true, true,
                        null,
                        new Runnable() {
                            @Override
                            public void run() {
                                // Ended
                                grid.state.set("chats.teddy.a2_after_call1", true);
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                // Canceled, trigger optional dialog
                                grid.state.set("chats.teddy.a2_after_call1", true);
                                grid.state.set("chats.teddy.a2_call_hung_up", true);
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                // Declined, schedule again
                                grid.scheduleRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        teddyPhoneCall1();
                                    }
                                }, a2_teddy_call_retry_delay);
                            }
                        }
                );
            }
        }, 4f);
    }


    public static void scheduleTeddyPhoneCall2() {
        if(!grid.unlockState("ACT2.scheduleTeddyPhoneCall2"))
            return;         // already done

        final Runnable onFinishedCall = new Runnable() {
            @Override
            public void run() {
                configureFlapeeRatingDialogs();
            }
        };

        grid.addTrigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                grid.removeTrigger(name);   // done

                // Do phone call
                grid.phoneApp.handleCall(
                        "calls.teddy.a2_call2",
                        true, true,
                        null,
                        onFinishedCall,
                        onFinishedCall,
                        onFinishedCall
                );

                // Don't allow leave thread screen
                return false;
            }
        });

    }

    private static boolean canShowAd() {
        // Don't show when showing ads or on call, or is transitioning
        if(grid.phoneApp.callScreen.isAttached())
            return false;
        if(grid.gatewayAdScreen.isAttached())
            return false;
        if(grid.flapeeBirdApp.adScreen.isAttached())
            return false;
        if(grid.iterate(null, ScreenTransition.class, true, null) != null)
            return false;
        WhatsupContact contact = grid.whatsupApp.threadScreen.contact();
        if(contact != null && contact.customMessageSound != null)
            return false;
        return true;        // else can
    }

    public static void showFlapeeAd(final float delay, float delayRf) {
        showFlapeeAd(delay, delayRf, null);
    }

    public static void showFlapeeAd(final float delay, float delayRf, final String unlockTag) {
        // Delay
        final float scheduled = Range.generateFor(delay, delayRf, false);
        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                if(!canShowAd()) {
                    grid.scheduleRunnable(this, scheduled);
                    return;
                }
                grid.flapeeBirdApp.adScreen.show(true);
                if(unlockTag != null) {
                    grid.flapeeBirdApp.adScreen.setOnFinished(new Runnable() {
                        @Override
                        public void run() {
                            grid.unlockState(unlockTag);
                        }
                    });
                }
                grid.flapeeBirdApp.adScreen.open(true);
            }
        }, scheduled);
    }

    public static void showGatewayVideo(float delay, float delayRf, final String mediaName, final String codePath, final String audioCodePath) {
        // Delay
        final float scheduled = Range.generateFor(delay, delayRf, false);
        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                if(!canShowAd()) {
                    grid.scheduleRunnable(this, scheduled);
                    return;
                }
                grid.gatewayAdScreen.show(
                        mediaName,
                        codePath,
                        audioCodePath,
                        true,
                        true);
                grid.gatewayAdScreen.open();

                // Achievement
                ACT1.unlockAchievement(Achievement.TEDDY_WEBCAM_VIDEOS, mediaName);
            }
        }, scheduled);
    }

    public static void endBlissEffect() {
        BlissEffect blissEffect = grid.iterate(null, BlissEffect.class, false, null);
        if(blissEffect != null)
            blissEffect.detachWithAnim();
    }

    public static void endGloomEffect() {
        GloomEffect gloomEffect = grid.iterate(null, GloomEffect.class, false, null);
        if(gloomEffect != null)
            gloomEffect.detachWithAnim();
    }

    public static void teddyClarityGonePrepare() {
        if(!grid.unlockState(STATE_TEDDY_CLARITY_GONE))
            return;     // already done
        // Stop leaving chat screen
        grid.addTrigger(TRIGGER_LEAVE_CHAT_THREAD_SCREEN, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                return false;
            }
        });
    }


    public static void teddyClarityGone() {
        // Play flapee music
        Audio.playMusic("sounds/flapee/theme-demon100-jumpscare.ogg", true, 0.5f);
    }

    public static JumpscareScreen createJumpscare1() {
        final JumpscareScreen jumpscare = new JumpscareScreen(
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/jumpscare_1_1.png"), false, 0.4f),
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/jumpscare_1_2.png"), false, 0.3f),
                new JumpscareScreen.ScareInfo(Sprite.load("content/scares/jumpscare_1_3.png"), false, 0.2f)
        );

        jumpscare.animation(
                new CompoundAnim(0.4f,
                        new ColorAnim(1f, new VibrationGraph(1f, new QuadraticGraph(50f, 0f, true), ConstantGraph.one), null),
                        new ScaleAnim(1f, new VibrationGraph(1f, new QuadraticGraph(3.4f, 0f, true), ConstantGraph.one))
                ),
                new ColorAnim(0.1f, new QuadraticGraph(10f, 1f, true), null),
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

        final MpegGlitch endGlitch = new MpegGlitch("sounds/flapee/jumpscare_1.ogg", null);
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


    public static void teddyClarityGone2(final String unlockTag) {
        final JumpscareScreen jumpscare = createJumpscare1();
        jumpscare.load();

        grid.addTrigger(TRIGGER_LEAVE_CHAT_THREAD_SCREEN, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                // Stop music
                Audio.stopMusic();

                // Jumpscare on pressing back
                jumpscare.open();

                // Done
                grid.removeTrigger(name);
                grid.unlockState(unlockTag);
                return false;
            }
        });
    }

    public static float playBlissVoice(String filename) {
        BlissEffect blissEffect = grid.iterate(null, BlissEffect.class, false, null);
        if(blissEffect == null) {
            Sys.error(TAG, "Bliss not found for voice playback: " + filename);
            return -1;
        }
        else {
            blissEffect.playVoice(filename);
            return blissEffect.getVoiceProfile().duration;
        }
    }


    public static void playGloomVoice(String filename) {
        GloomEffect gloomEffect = grid.iterate(null, GloomEffect.class, false, null);
        if(gloomEffect == null)
            Sys.error(TAG, "Bliss not found for voice playback: " + filename);
        else
            gloomEffect.playVoice(filename);
    }

    private static void configureFlapeeRatingDialogs() {
        // Stop updating
        grid.flapeeBirdApp.stopUpdating();

        final DialogConfig start = new DialogConfig(
                "Looks like you've been enjoying FlapeeBird!\n\nTell us how you feel by dropping us a rating!",
                null,
                null,
                "Sure",
                "Not Now"
        );

        final DialogConfig notNow = new DialogConfig(
                "This is a free game! The least you can do is give us a rating! Otherwise I am forcing you to watch 5 ADs in a row. Uninterrupted",
                null, null,
                "Agree", "Disagree"
        );
        start.negativeButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueDialog(notNow);
            }
        };
        notNow.negativeButtonAction = new Runnable() {
            @Override
            public void run() {
                // Disagree, go back to start
                grid.flapeeBirdApp.queueDialog(start);
            }
        };

        final DialogConfig adsThankYou = new DialogConfig(
                "Thank you!\n\nEnjoy the ads!",
                null, null,
                "I Will", null
        );

        notNow.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueDialog(adsThankYou);
            }
        };


        final DialogConfig endNegative = new DialogConfig(
                "Thank you!\n\nFlapeeBird will now update to serve you more ads!",
                null, null,
                "Bye", null
        );

        adsThankYou.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                // Play 5 ads uninterrupted
                final FlapeeAdScreen adScreen = grid.flapeeBirdApp.adScreen;
                adScreen.show(true, false);
                adScreen.open(true);
                adScreen.setOnFinished(new Runnable() {
                    int c = 0;

                    @Override
                    public void run() {
                        c++;
                        if (c < 5) {
                            adScreen.show(false, false);
                            adScreen.open(false);
                            adScreen.setOnFinished(this);
                        } else
                            grid.flapeeBirdApp.queueDialog(endNegative);        // TODO: check if this works
//                            scheduleTeddyBeatHighScore();
                    }
                });
            }
        };


        String rateDialogString;
        String goodRatingString;
        String averageRatingString;
        String lowRatingString;

        if (grid.isStateUnlocked("chats.teddy.player_is_youtuber")) {
            // Youtuber, special sequence
            rateDialogString = "Just between you and me. Give it a 5 star and I promise you will be handsomely rewarded :)";
            goodRatingString = "No one ever doubts an inflencer. Thanks to your stellar rating this game will go viral!\n\n51 EGGS";
            averageRatingString = "This is awkward... If you are going to be half ass about this then so will I!\n\n1 EGG";
            lowRatingString = "What?! How dare you drag this game down! You are going to pay for that!\n\nLOSE HALF YOUR EGGS";
        }
        else {
            // Normal text
            rateDialogString = "If you give more stars we give you more rewards if not you'll be punished";
            goodRatingString = "I am so glad you enjoy this game. Thanks to your stellar rating, so will other people!\n\n50 EGGS";
            averageRatingString = "If you are going to be half ass about this then so will I! Just take your reward and go away!\n\n1 EGG";
            lowRatingString = "What?! But I worked so hard on this! Don't say I didn't warn you. Here is your punishment.\n\nLOSE HALF YOUR EGGS";

        }


        // Rating dialog
        final DialogConfig rateDialog = new DialogConfig(
                rateDialogString,
                "Sh*t game", "GOTY",
                "Continue", null
        );

        if (grid.isStateUnlocked("chats.teddy.player_is_youtuber")) {
            final DialogConfig youtuber = new DialogConfig(
                    "Oh my! Looks like you are one of those gaming influencers! I hope you are streaming / recording right now ^_^\n\nYour ratings are 100 times more valuable!",
                    null,
                    null,
                    "Yes I Am",
                    null
            );
            youtuber.positiveButtonAction = new Runnable() {
                @Override
                public void run() {
                    grid.flapeeBirdApp.queueDialog(rateDialog);
                }
            };
            start.positiveButtonAction = new Runnable() {
                @Override
                public void run() {
                    grid.flapeeBirdApp.queueDialog(youtuber);
                }
            };
        }
        else {
            start.positiveButtonAction = new Runnable() {
                @Override
                public void run() {
                    grid.flapeeBirdApp.queueDialog(rateDialog);
                }
            };
        }


        final DialogConfig goodRating = new DialogConfig(
                goodRatingString,
                null, null,
                "Claim", null
        );
        final DialogConfig averageRating = new DialogConfig(
                averageRatingString,
                null, null,
                "Claim", null
        );
        final DialogConfig lowRating = new DialogConfig(
                lowRatingString,
                null, null,
                "Claim", null
        );

        rateDialog.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                if(grid.flapeeBirdApp.getDialogStarsSelected() >= 4)
                    grid.flapeeBirdApp.queueDialog(goodRating);
                else if(grid.flapeeBirdApp.getDialogStarsSelected() == 3)
                    grid.flapeeBirdApp.queueDialog(averageRating);
                else // if(grid.flapeeBirdApp.getDialogStarsSelected() <= 2)
                    grid.flapeeBirdApp.queueDialog(lowRating);
            }
        };

        final DialogConfig shareDialog = new DialogConfig(
                "Would you share this game  with your relatives, friends and other humanoids?",
                "Unlikely", "Likely",
                "Continue", null
        );

        goodRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(50);
                grid.flapeeBirdApp.queueDialog(shareDialog);
            }
        };

        averageRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(1);
                grid.flapeeBirdApp.queueDialog(shareDialog);
            }
        };

        lowRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(Math.min(-grid.flapeeBirdApp.getEggs() / 2, -10));
                grid.flapeeBirdApp.queueDialog(shareDialog);
            }
        };


        final DialogConfig shareGoodRating = new DialogConfig(
                "Great! Sharing is caring! Here is your reward!\n\n50 EGGS",
                null, null,
                "Claim", null
        );
        final DialogConfig shareAverageRating = new DialogConfig(
                "You can't be on the fence about this! Mediocre!\n\n1 EGG",
                null, null,
                "Claim", null
        );
        final DialogConfig shareLowRating = new DialogConfig(
                "How can you deny such bliss and joy to other people?\n\nLOSE HALF YOUR EGGS",
                null, null,
                "Claim", null
        );

        shareDialog.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                if(grid.flapeeBirdApp.getDialogStarsSelected() >= 4)
                    grid.flapeeBirdApp.queueDialog(shareGoodRating);
                else if(grid.flapeeBirdApp.getDialogStarsSelected() == 3)
                    grid.flapeeBirdApp.queueDialog(shareAverageRating);
                else // if(grid.flapeeBirdApp.getDialogStarsSelected() <= 2)
                    grid.flapeeBirdApp.queueDialog(shareLowRating);
            }
        };

        final DialogConfig end = new DialogConfig(
                "Thank you!\n\nFlapeeBird will now update to reflect your preferences that suit us!",
                null, null,
                "Bye", null
        );

        shareGoodRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(50);
                grid.flapeeBirdApp.queueDialog(end);
            }
        };

        shareAverageRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(1);
                grid.flapeeBirdApp.queueDialog(end);
            }
        };

        shareLowRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(Math.min(-grid.flapeeBirdApp.getEggs() / 2, -10));
                grid.flapeeBirdApp.queueDialog(end);
            }
        };



        end.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                scheduleTeddyBeatHighScore();
            }
        };


        endNegative.positiveButtonAction = end.positiveButtonAction;


        // Start
        grid.flapeeBirdApp.queueDialog(start);
    }


    private static void scheduleTeddyBeatHighScore() {
        // Start updating
        grid.flapeeBirdApp.startUpdating(8.677f);

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
        }, a2_teddy_beat_score_delay);
    }

    private static void teddyBeatHighScore() {
        grid.flapeeBirdApp.resetEggShopIndex();
        grid.flapeeBirdApp.loadLevel(GBHalfDemonLevel.class);
        grid.flapeeBirdApp.setPlayerScore(0);       // reset

        // Allow sharing now
        grid.flapeeBirdApp.configureInvites(new FlapeeBirdScreen.InviteInfo[]{
                new FlapeeBirdScreen.InviteInfo(
                        "content/profiles/chats/jenny.png",
                        "Jenny",
                        "chats.jenny.fp_invited"
                ),
                new FlapeeBirdScreen.InviteInfo(
                        "content/profiles/chats/karen.png",
                        "Aunt Karen",
                        "chats.auntkaren.fp_invited"
                ),
                new FlapeeBirdScreen.InviteInfo(
                        "content/profiles/chats/liam.png",
                        "Liam",
                        "chats.liam.fp_invited"
                ),
                new FlapeeBirdScreen.InviteInfo(
                        "content/profiles/chats/max.png",
                        "Max",
                        "chats.max.fp_invited"
                ),
                new FlapeeBirdScreen.InviteInfo(
                        "content/profiles/chats/chad.png",
                        "Chad",
                        "chats.chad.fp_invited"
                ),
        });

        // Reset state
        grid.state.set("chats.teddy.flapee_score_beat", false);

        // Continue teddy chat
        grid.state.set("chats.teddy.a2_after_rating", true);

        // Mark as session 2
        grid.state.set("chats.teddy.flapee_session_1", false);
        grid.state.set("chats.teddy.flapee_session_2", true);


        // Remove previous effect
        endGloomEffect();
        endBlissEffect();

        // Enter the bliss
        GloomEffect gloomEffect = new GloomEffect(
                "sounds/flapee/gloom_1.ogg", true,
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
        gloomEffect.playVoice("content/vo/demon/giggle_5.ogg");
    }

    public static void stopGameUpdating() {
        grid.flapeeBirdApp.stopUpdating();
    }

    public static void finishedFlapeeSession2() {
        // Inform teddy
        grid.unlockState("chats.teddy.flapee_score_beat");

        // Achievement
        ACT1.unlockAchievement(Achievement.SECOND_FB_WIN);

        // Remove previous effect
        BlissEffect blissEffect = grid.iterate(null, BlissEffect.class, false, null);
        if(blissEffect != null)
            blissEffect.detach();

        // Enter the bliss
        blissEffect = new BlissEffect(
                "sounds/flapee/bliss_2.ogg", true,
                0.3f,
                0.05f,
                0.2f,
                0.0f,
                3.0f,
                new SequenceAnim(new Animation[] {
                        new FadeAnim(6f, new CompoundGraph(
                                new LinearGraph(0f, 0.8f, 0.05f),
                                new LinearGraph(0.8f, 0.3f, 0.95f)
                        )),
                        new FadeAnim(30f, new LinearGraph(0.3f, 0.2f)),
                }),
                new FadeAnim(0.2f),
                new FadeAnim(15f, new LinearGraph(0.2f, 0f))
        );
        blissEffect.attach(grid);

        // Start update, stop player from playing
        grid.flapeeBirdApp.startUpdating(5.614f);
    }


    public static void configureFlapeeFeedbackDialogs() {
        // Stop updating
        grid.flapeeBirdApp.stopUpdating();

        final DialogConfig start = new DialogConfig(
                "I'm back!\n\nCan you spend a few minutes to tell us how you feel about the game? I might even reward you if I like your answers!",
                null,
                null,
                "Sure",
                "Not Now"
        );


        final DialogConfig notNow = new DialogConfig(
                "I have given you hours of entertainment for free! Comply with this request or I will punish you!",
                null, null,
                "Comply", "Blackmail?"
        );
        start.negativeButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueDialog(notNow);
            }
        };
        notNow.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                // Disagree, go back to start
                grid.flapeeBirdApp.queueDialog(start);
            }
        };


        final DialogConfig blackmail = new DialogConfig(
                "This is my game. I call the shots here.",
                null, null,
                "Okay okay", "NO"
        );
        notNow.negativeButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueDialog(blackmail);
            }
        };
        blackmail.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                // Disagree, go back to start
                grid.flapeeBirdApp.queueDialog(start);
            }
        };

        final DialogConfig getrekt = new DialogConfig(
                "Suit yourself. Get rekt!\n\nLOSE ALL YOUR EGGS",
                null, null,
                "Claim", null
        );
        blackmail.negativeButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueDialog(getrekt);
            }
        };


        // Start positive
        final DialogConfig tellFriendsRate = new DialogConfig(
                "How interested are you in challenging your friends to game showdown?",
                "Unlikely", "Likely",
                "Next", null
        );
        start.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueDialog(tellFriendsRate);
            }
        };


        final DialogConfig tellFriendsGoodRating = new DialogConfig(
                "Fantastic! I like your competitive streak! Here is your reward!\n\n50 EGGS",
                null, null,
                "Claim", null
        );
        final DialogConfig tellFriendsAverageRating = new DialogConfig(
                "Lame! How do you deal with being so average!\n\n1 EGG",
                null, null,
                "Claim", null
        );
        final DialogConfig tellFriendsLowRating = new DialogConfig(
                "Aww, are you a delicate snowflake who doesn't fight to be the best?\n\nLOSE HALF YOUR EGGS",
                null, null,
                "Claim", null
        );

        tellFriendsRate.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                if(grid.flapeeBirdApp.getDialogStarsSelected() >= 4)
                    grid.flapeeBirdApp.queueDialog(tellFriendsGoodRating);
                else if(grid.flapeeBirdApp.getDialogStarsSelected() == 3)
                    grid.flapeeBirdApp.queueDialog(tellFriendsAverageRating);
                else // if(grid.flapeeBirdApp.getDialogStarsSelected() <= 2)
                    grid.flapeeBirdApp.queueDialog(tellFriendsLowRating);
            }
        };

        final DialogConfig payToWinRate = new DialogConfig(
                "Would you like it if we have a pay to win feature in this game?",
                "Dislike", "Like",
                "Next", null
        );

        tellFriendsGoodRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(50);
                grid.flapeeBirdApp.queueDialog(payToWinRate);
            }
        };

        tellFriendsAverageRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(1);
                grid.flapeeBirdApp.queueDialog(payToWinRate);
            }
        };
        tellFriendsLowRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(Math.min(-grid.flapeeBirdApp.getEggs() / 2, -10));
                grid.flapeeBirdApp.queueDialog(payToWinRate);
            }
        };

        final DialogConfig payToWinGoodRating = new DialogConfig(
                "Fantastic! Consider it done!\n\n50 EGGS",
                null, null,
                "Claim", null
        );
        final DialogConfig payToWinAverageRating = new DialogConfig(
                "What is with these half baked choices?! Don't you have any opinions of your own?\n\n1 EGG",
                null, null,
                "Claim", null
        );
        final DialogConfig payToWinLowRating = new DialogConfig(
                "People who can pay are already winners! Winners win more! That's how your flimsy reality works!\n\nLOSE HALF YOUR EGGS",
                null, null,
                "Claim", null
        );

        payToWinRate.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                if(grid.flapeeBirdApp.getDialogStarsSelected() >= 4) {
                    grid.flapeeBirdApp.queueDialog(payToWinGoodRating);
                    grid.unlockState("stats.supportPayToWin");
                }
                else if(grid.flapeeBirdApp.getDialogStarsSelected() == 3)
                    grid.flapeeBirdApp.queueDialog(payToWinAverageRating);
                else // if(grid.flapeeBirdApp.getDialogStarsSelected() <= 2)
                    grid.flapeeBirdApp.queueDialog(payToWinLowRating);
            }
        };


        final DialogConfig adsPayRate = new DialogConfig(
                "Would you prefer watching ADs or paying for this game.",
                "Free", "Pay",
                "Next", null
        );

        payToWinGoodRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(50);
                grid.flapeeBirdApp.queueDialog(adsPayRate);
            }
        };
        payToWinAverageRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(1);
                grid.flapeeBirdApp.queueDialog(adsPayRate);
            }
        };
        payToWinLowRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(Math.min(-grid.flapeeBirdApp.getEggs() / 2, -10));
                grid.flapeeBirdApp.queueDialog(adsPayRate);
            }
        };



        final DialogConfig adsPayGoodRating = new DialogConfig(
                "No nonono\n\nNobody pays for games anymore! How can I have more people playing if people are not willing to buy it?!\n\nLOSE HALF YOUR EGGS",
                null, null,
                "Claim", null
        );
        final DialogConfig adsPayAverageRating = new DialogConfig(
                "This is extremely vexing. Make. A. Damn. Choice.\n\n1 EGG",
                null, null,
                "Claim", null
        );
        final DialogConfig adsPayLowRating = new DialogConfig(
                "Excellent. A free game means it will hit a critical mass and soon millions of people will be exposed to this joy\n\n50 EGGS",
                null, null,
                "Claim", null
        );

        adsPayRate.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                if(grid.flapeeBirdApp.getDialogStarsSelected() >= 4)
                    grid.flapeeBirdApp.queueDialog(adsPayGoodRating);
                else if(grid.flapeeBirdApp.getDialogStarsSelected() == 3)
                    grid.flapeeBirdApp.queueDialog(adsPayAverageRating);
                else // if(grid.flapeeBirdApp.getDialogStarsSelected() <= 2)
                    grid.flapeeBirdApp.queueDialog(adsPayLowRating);
            }
        };


        final DialogConfig end = new DialogConfig(
                "Thank you for your time!\n\nPlease wait as we use your feedback to improve the game!",
                null, null,
                "Bye", null
        );

        adsPayLowRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(50);
                grid.flapeeBirdApp.queueDialog(end);
            }
        };
        adsPayAverageRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(1);
                grid.flapeeBirdApp.queueDialog(end);
            }
        };
        adsPayGoodRating.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(Math.min(-grid.flapeeBirdApp.getEggs() / 2, -10));
                grid.flapeeBirdApp.queueDialog(end);
            }
        };


        end.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                // Continue to teddy group chat
                grid.unlockState("chats.teddygroup.started");
                // Start updating again
                grid.flapeeBirdApp.startUpdating(13.41f);
            }
        };


        final DialogConfig endNegative = new DialogConfig(
                "Like it or not, we'll be making some improvements to enhance your gaming experience!",
                null, null,
                "Bye", null
        );
        endNegative.positiveButtonAction = end.positiveButtonAction;

        getrekt.positiveButtonAction = new Runnable() {
            @Override
            public void run() {
                grid.flapeeBirdApp.queueReward(Math.min(-grid.flapeeBirdApp.getEggs(), -10));
                grid.flapeeBirdApp.queueDialog(endNegative);
            }
        };


        // Start
        grid.flapeeBirdApp.queueDialog(start);
    }

}
