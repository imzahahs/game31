package game31.triggers;

import com.badlogic.gdx.files.FileHandle;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Notification;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.VoiceProfile;
import game31.app.browser.BrowserScreen;
import game31.app.chats.WhatsupContact;
import game31.gb.flapee.GBDemoLevel;
import game31.gb.flapee.GBDemonLevel;
import game31.gb.flapee.GBHalfDemonLevel;
import game31.gb.flapee.GBNormalLevel;
import game31.glitch.MpegGlitch;
import game31.renderer.DiffuseGeneratorMaterial;
import game31.renderer.GloomScreenMaterial;
import game31.renderer.LightingCompositorMaterial;
import game31.renderer.LightingGeneratorMaterial;
import game31.renderer.LiquidCrystalGeneratorMaterial;
import game31.renderer.LsdScreenMaterial;
import game31.renderer.TextEffectCompositor;
import game31.renderer.TextEffectGenerator;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.StaticSprite;

/**
 * Created by Azmi on 5/18/2017.
 */

public class ACT1 extends Globals {
    private static final String TAG = "ACT1";

    public static void load() {

        if (checkAllAssets) {
//            grid.flapeeBirdApp.loadLevel(GBNormalLevel.class);

            Material.load("system/square.png.NoiseMaterial");

            Sprite.load("apps/flapee/icon.png");
            Sprite.load("apps/flapee/icon-disabled.png");

            // Codes
            Sprite.load("content/codes/code-1.png.NoiseMaterial");
            Sprite.load("content/codes/code-2.png.NoiseMaterial");
            Sprite.load("content/codes/code-3.png.NoiseMaterial");
            Sprite.load("content/codes/code-4.png.NoiseMaterial");
            Sprite.load("content/codes/code-5.png.NoiseMaterial");
            Sprite.load("content/codes/code-6.png.NoiseMaterial");
            Sprite.load("content/codes/code-7.png.NoiseMaterial");

            // Invites
            Sprite.load("content/invites/showdown.png");
            Sprite.load("content/invites/flapee-invitation.png");
            Sprite.load("content/invites/spindollarz.png");
            Sprite.load("content/invites/zombie-finger-smash.png");
            Sprite.load("content/invites/pirate-lords.png");
            Sprite.load("content/invites/lord-of-pirates.png");
            Sprite.load("content/invites/cute-space-bunnies.png");
            Sprite.load("content/invites/candy-crushing-pirates.png");

            // Wallpapers
            Sprite.load("content/gallery/docs/good.png");
            Sprite.load("content/gallery/docs/bad.png");

            // Jumpscares
            Sprite.load("content/scares/jumpscare_1_1.png");
            Sprite.load("content/scares/jumpscare_1_2.png");
            Sprite.load("content/scares/jumpscare_1_3.png");
            Sprite.load("content/scares/jumpscare_2_1.png");
            Sprite.load("content/scares/jumpscare_2_2.png");
            Sprite.load("content/scares/jumpscare_2_3.png");

            // Demon teddy effects
            Sound.load("sounds/chat_reply_distort.ogg");
            Sprite.load("content/profiles/chats/teddy-demon.png");

            grid.whatsupApp.pack(grid.state);
            grid.whatsupApp.load(Globals.introChatsConfigFilename, grid.state);

            // Reset
            grid.whatsupApp.pack(grid.state);
            grid.whatsupApp.load(Globals.chatsConfigFilename, grid.state);

            // Load all levels
            new GBDemoLevel().buildLevel();
            new GBNormalLevel().buildLevel();
            new GBHalfDemonLevel().buildLevel();
            new GBDemonLevel().buildLevel();

            // Load materials
            new DiffuseGeneratorMaterial();
            new GloomScreenMaterial();
            new LightingGeneratorMaterial();
            new LightingCompositorMaterial();
            new LiquidCrystalGeneratorMaterial();
            new LsdScreenMaterial();
            new TextEffectGenerator();
            new TextEffectCompositor();

            // Load all demon vo voice profiles
            FileHandle directory = File.open("content/vo/demon", false, false);
            if (directory != null && directory.isDirectory()) {
                for (FileHandle file : directory.list()) {
                    if (file.name().endsWith(".ogg")) {
                        VoiceProfile.load("content/vo/demon/" + file.name());
                    }
                }
            }

            // Ambiance
            VoiceProfile.load("sounds/flapee/bliss_1.ogg");
            VoiceProfile.load("sounds/flapee/bliss_2.ogg");
            VoiceProfile.load("sounds/flapee/gloom_1.ogg");
            VoiceProfile.load("sounds/flapee/gloom_2.ogg");


//            // Code to dump .texture files
//            directory = File.open("recon", false, false);
//            if(directory != null && directory.isDirectory()) {
//                for(FileHandle file : directory.list()) {
//                    String path = "recon/" + file.name();
//                    TextureFile t = new TextureFile();
//                    t.load(path);
//                    FIFormat.FragmentedImageData data = (FIFormat.FragmentedImageData) t.getImageData(0);
//                    Pixmap pixmap = data.reconstruct()[0];
//                    PixmapIO.writePNG(File.openCache("dump/" + path.substring(0, path.length() - 4)), pixmap);
//                    pixmap.dispose();
//                    data.release();
//                    t.clear();
//                }
//            }
        }

        // Preload sound clips
        Sound.load("sounds/chat_reply_distort.ogg");
        Sound.load("sounds/chat_notify_distort.ogg");

        // Custom fonts
        Font slyflyBold = new Font("roboto-medium.ttf", 40);
        slyflyBold.color("SLYFLY_BOLD", 0x000000ff);
    }


    // https://www.ssa.gov/oact/STATS/table4c6.html
    public static float[] maleLifeExpectancyTable = new float[]{
            76.15f,
            75.63f,
            74.67f,
            73.69f,
            72.71f,
            71.72f,
            70.73f,
            69.74f,
            68.75f,
            67.76f,
            66.76f,
            65.77f,
            64.78f,
            63.79f,
            62.8f,
            61.82f,
            60.84f,
            59.88f,
            58.91f,
            57.96f,
            57.01f,
            56.08f,
            55.14f,
            54.22f,
            53.29f,
            52.37f,
            51.44f,
            50.52f,
            49.59f,
            48.67f,
            47.75f,
            46.82f,
            45.9f,
            44.98f,
            44.06f,
            43.14f,
            42.22f,
            41.3f,
            40.38f,
            39.46f,
            38.54f,
            37.63f,
            36.72f,
            35.81f,
            34.9f,
            34f,
            33.11f,
            32.22f,
            31.34f,
            30.46f,
            29.6f,
            28.75f,
            27.9f,
            27.07f,
            26.25f,
            25.43f,
            24.63f,
            23.83f,
            23.05f,
            22.27f,
            21.51f,
            20.75f,
            20f,
            19.27f,
            18.53f,
            17.81f,
            17.09f,
            16.38f,
            15.68f,
            14.98f,
            14.3f,
            13.63f,
            12.97f,
            12.33f,
            11.7f,
            11.08f,
            10.48f,
            9.89f,
            9.33f,
            8.77f,
            8.24f,
            7.72f,
            7.23f,
            6.75f,
            6.3f,
            5.87f,
            5.45f,
            5.06f,
            4.69f,
            4.35f,
            4.03f,
            3.73f,
            3.46f,
            3.21f,
            2.99f,
            2.8f,
            2.63f,
            2.48f,
            2.34f,
            2.22f,
            2.11f,
            2f,
            1.89f,
            1.79f,
            1.69f,
            1.59f,
            1.5f,
            1.41f,
            1.33f,
            1.25f,
            1.17f,
            1.1f,
            1.03f,
            0.96f,
            0.89f,
            0.83f,
            0.77f,
            0.71f,
            0.66f,
            0.61f,
    };
    public static float[] femaleLifeExpectancyTable = new float[]{
            80.97f,
            80.41f,
            79.44f,
            78.45f,
            77.47f,
            76.48f,
            75.48f,
            74.49f,
            73.5f,
            72.51f,
            71.51f,
            70.52f,
            69.53f,
            68.53f,
            67.54f,
            66.56f,
            65.57f,
            64.59f,
            63.61f,
            62.63f,
            61.65f,
            60.67f,
            59.7f,
            58.73f,
            57.76f,
            56.79f,
            55.82f,
            54.85f,
            53.88f,
            52.92f,
            51.95f,
            50.99f,
            50.03f,
            49.07f,
            48.11f,
            47.16f,
            46.2f,
            45.25f,
            44.3f,
            43.35f,
            42.41f,
            41.46f,
            40.52f,
            39.59f,
            38.65f,
            37.72f,
            36.8f,
            35.88f,
            34.96f,
            34.06f,
            33.15f,
            32.26f,
            31.37f,
            30.49f,
            29.61f,
            28.74f,
            27.88f,
            27.02f,
            26.17f,
            25.32f,
            24.48f,
            23.64f,
            22.81f,
            21.99f,
            21.17f,
            20.36f,
            19.55f,
            18.76f,
            17.98f,
            17.2f,
            16.44f,
            15.69f,
            14.96f,
            14.24f,
            13.54f,
            12.85f,
            12.17f,
            11.51f,
            10.86f,
            10.24f,
            9.63f,
            9.04f,
            8.48f,
            7.93f,
            7.41f,
            6.91f,
            6.43f,
            5.98f,
            5.54f,
            5.14f,
            4.76f,
            4.41f,
            4.09f,
            3.8f,
            3.54f,
            3.3f,
            3.09f,
            2.9f,
            2.73f,
            2.57f,
            2.42f,
            2.27f,
            2.14f,
            2f,
            1.88f,
            1.76f,
            1.64f,
            1.53f,
            1.43f,
            1.33f,
            1.24f,
            1.15f,
            1.06f,
            0.98f,
            0.9f,
            0.83f,
            0.77f,
            0.71f,
            0.66f,
            0.61f,
    };

    public static String[] youtuberNames = new String[]{
            "PewDiePie",
            "TheSyndicateProject",
            "CaptainSparklez",
            "roosterteeth",
            "markiplier",
            "SeaNanners",
            "theRadBrad",
            "Gronkh",
            "iHasCupquake",
            "UberHaxorNova",
            "LeFloid",
            "AntVenom",
            "GameGrumps",
            "Cryaotic",
            "AngryJoe",
            "JoeVargas",
            "AngryJoeShow",
            "CinnamonToastKen",
            "PietSmiet",
            "XpertThief",
            "Robbaz",
            "jacksepticeye",
            "samgladiator",
            "kubzscouts",
            "ManlyBadassHero",
            "Razzbowski",
            "HarshlyCritical",
            "JohnWolfe",
            "NightMind",
            "JesseCox",
            "SwingPoynt",
            "CoryxKenshin",
            "CoryKenshin",
            "RiskRim",
            "8-bitryan",
            "8bitryan",
            "geekremix",
            "BijuuMike",
            "Bijuu",
            "LoeyLane",
            "lanokirx",
            "lanokir",
            "cellbit",
            "pokopow",
            "metalbear",
            "mandzio",
            "grimyaz",
            "windy31",
            "maugly",
            "brysen",
            "pandorya",
            "dxarmy",
            "dxfan619",
    };

    public static String[] profanityNames = new String[]{
            "fuck",
            "fucker",
            "fucks",
            "fucking",
            "fuckin",
            "motherfucker",
            "shit",
            "bitch",
            "damn",
            "crap",
            "piss",
            "dick",
            "cock",
            "pussy",
            "asshole",
            "arsehole",
            "faggot",
            "bastard",
            "slut",
            "douche",
            "bloody",
            "cunt",
            "bollocks",
            "tosser",
            "wanker",
            "chav",
            "muppet",
            "dimwit",
            "nutter",
            "pillock",
            "plonker",
            "trollop",
            "skiver",
            "arse",
            "maggot",
            "anal",
            "anus",
            "assfuck",
            "retard",
            "whore"

    };


    public static boolean a1_allow_skip_introvid = false;
    public static float a1_flapee_install_time = 4f;        // 8f

    public static float flapeeBirdPageMinLifeExpectancy = 10f;
    public static float flapeeBirdPageExcitedLifeExpectancy = 30f;

    public static final String STATE_SEEN_FLAPEE_STORE_PAGE = "STATE_SEEN_FLAPEE_STORE_PAGE";
    public static final String STATE_SEEN_FLAPEE_ROYALE_PAGE = "STATE_SEEN_FLAPEE_ROYALE_PAGE";
    public static final String STATE_FLAPEE_INSTALLING = "STATE_FLAPEE_INSTALLING";
    public static final String STATE_FLAPEE_INSTALLED = "STATE_FLAPEE_INSTALLED";
    public static final String STATE_FLAPEE_ROYALE_UNLOCKED = "STATE_FLAPEE_ROYALE_UNLOCKED";

    public static final String STATE_FLAPEE_EGGSHOP_UNLOCKED = "STATE_FLAPEE_EGGSHOP_UNLOCKED";
    public static final String STATE_FLAPEE_FIRST_TIME_MENU = "STATE_FLAPEE_FIRST_TIME_MENU";

    public static final String STATE_SHOWED_TEDDY_MAIL_VID = "STATE_SHOWED_TEDDY_MAIL_VID";
    public static final String STATE_SHOWED_TEDDY_CALL_VID = "STATE_SHOWED_TEDDY_CALL_VID";

    public static final String STATE_DUNCAN_YONG_WHISPERED = "STATE_DUNCAN_YONG_WHISPERED";

    private static void configureHomescreen() {
        // Configure homescreen
        updateTeddyTrustWallpaper();

        // Malware icon
        Sprite malwareIcon = Sprite.load("system/app-icon.png.NoiseMaterial");
        ColorAttribute.of(malwareIcon).set(1f, 0.0f, 0.0f, 1f);

        // Apps
        grid.homescreen.clear();
        grid.homescreen.addDockApp(0, Sprite.load("apps/calls/icon.png"), "Phone", Globals.CONTEXT_APP_CALLS, grid.phoneApp);
        grid.homescreen.addDockApp(1, Sprite.load("apps/chats/icon.png"), "Chats", Globals.CONTEXT_APP_CHATS, grid.whatsupApp);
        grid.homescreen.addDockApp(2, Sprite.load("apps/downloads/icon.png"), "Downloads", Globals.CONTEXT_APP_DOWNLOADS, grid.downloadsApp);
        grid.homescreen.addDockApp(3, Sprite.load("apps/mail/icon.png"), "Mail", Globals.CONTEXT_APP_MAIL, grid.mailApp);
        grid.homescreen.addApp(0, 4, 2, Sprite.load("apps/jabbr/icon.png"), "Jabbr", Globals.CONTEXT_APP_FRIENDS, grid.friendsApp);
        grid.homescreen.addApp(0, 4, 3, Sprite.load("apps/browser/icon.png"), "Surfer", Globals.CONTEXT_APP_BROWSER, grid.browserApp);

        if (grid.isStateUnlocked(STATE_FLAPEE_INSTALLED))
            flapeeBirdInstalled();

        if (d_showDevTools) {
            grid.homescreen.addApp(1, 3, 3, Sprite.load("apps/kaigan/icon.png"), "Fast Mode", CONTEXT_APP_CHEAT_GAMESPEED, new CheatGameSpeed());
            grid.homescreen.addApp(1, 2, 3, Sprite.load("apps/kaigan/icon.png"), "Give Eggs", CONTEXT_APP_CHEAT_EGGS, new CheatGiveEggs());
            grid.homescreen.addApp(1, 1, 3, Sprite.load("apps/kaigan/icon.png"), "Gallery", Globals.CONTEXT_APP_GALLERY, grid.photoRollApp);
            grid.homescreen.addApp(1, 1, 2, Sprite.load("apps/kaigan/icon.png"), "KickAnim", Globals.CONTEXT_APP_GALLERY, grid.flapeeKickNotifyScreen);

            grid.homescreen.addApp(1, 2, 2, Sprite.load("apps/kaigan/icon.png"), "Jump 1", Globals.CONTEXT_APP_GALLERY, ACT2.createJumpscare1());
            grid.homescreen.addApp(1, 3, 2, Sprite.load("apps/kaigan/icon.png"), "Jump 2", Globals.CONTEXT_APP_GALLERY, ACT3.createJumpscare2());
        }

        // Setup powerup, hours and high score listener
        grid.addTrigger(TRIGGER_FLAPEE_POWERUPS_USED, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                // Inform teddy
                grid.unlockState("chats.teddy.flapee_powerups_used");       // check first, dun need to refresh whole dialogue tree
                return true;
            }
        });
        grid.addTrigger(TRIGGER_FLAPEE_HOURS_PURCHASED, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                // Inform teddy
                grid.unlockState("chats.teddy.flapee_hours_used");       // check first, dun need to refresh whole dialogue tree
                // Check balance
                if (grid.flapeeBirdApp.getLifehours() == 0) {
                    // Start ending
                    ACT3.startLcdBurnEffect1();

                    // Disable egg store
                    grid.flapeeBirdApp.setEggShopAllowed(false);

                    // Don't allow to exit
                    grid.addTrigger(Globals.TRIGGER_FLAPEE_LEAVE_SCREEN, new Grid.Trigger() {
                        @Override
                        public boolean trigger(String name) {
                            return false;
                        }
                    });

                    // Schedule full burn and ending
                    grid.scheduleRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // Start full burn
                            ACT1.modifyTeddyTrust(-1000);
                            ACT3.startLcdBurnEffect2();
                            ACT3.endingBothDie();
                        }
                    }, 20f);

                    // Achievement
                    unlockAchievement(Achievement.LIFEHOURS_DRAINED);
                }

                return true;        // ignored
            }
        });
        grid.addTrigger(TRIGGER_FLAPEE_NEW_HIGH_SCORE, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                int playerRank = grid.flapeeBirdApp.getPlayerRank();
                if (playerRank == 0) {
                    if (grid.isStateUnlocked("chats.teddy.flapee_session_1"))
                        ACT1.finishedFlapeeSession1();
                    else if (grid.isStateUnlocked("chats.teddy.flapee_session_2"))
                        ACT2.finishedFlapeeSession2();
                    else if (grid.isStateUnlocked("chats.teddy.flapee_session_3"))
                        ACT3.finishedFlapeeSession3();
                    else
                        Sys.error(TAG, "Unable to determine which flapee session has completed");
                } else if (playerRank < (grid.flapeeBirdApp.getLeaderboardLength() - 1)) {
                    // Beat at least the last person
                    grid.unlockState("chats.teddy.score_check_intermission");
                }

                return true;
            }
        });
        grid.addTrigger(TRIGGER_FLAPEE_SCORED_POINT, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                int score = grid.flapeeBirdApp.getCurrentScore();
                int highestScore = grid.flapeeBirdApp.getLeaderboardHighestScore() / 2;

                if (score >= (highestScore / 2))     // halfway across
                    grid.unlockState("chats.teddy.score_check_intermission");

                return true;        // ignored
            }
        });
        grid.addTrigger(TRIGGER_FLAPEE_SHOP_OPENED, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                grid.unlockState("chats.teddy.flapee_shop_seen");
                return true;        // ignored
            }
        });


        // Unlock eggshop only after demon speaks for first time
        if (!grid.isStateUnlocked(STATE_FLAPEE_EGGSHOP_UNLOCKED)) {
            grid.addTrigger(TRIGGER_FLAPEE_DEMON_SPEAKS, new Grid.Trigger() {
                @Override
                public boolean trigger(String name) {
                    finishedFlapeeSessionAllowEggShop();
                    return true;       // ignored
                }
            });
        }

        // On first time showing flapee bird menu, show ad
        if (!grid.isStateUnlocked(STATE_FLAPEE_FIRST_TIME_MENU)) {
            grid.addTrigger(TRIGGER_FLAPEE_SHOW_MENU, new Grid.Trigger() {
                @Override
                public boolean trigger(String name) {
                    // Done
                    grid.unlockState(STATE_FLAPEE_FIRST_TIME_MENU);
                    grid.removeTrigger(name);

                    // Show ad
                    grid.flapeeBirdApp.adScreen.show(false, true, "Ads/ninjafox");
                    grid.flapeeBirdApp.adScreen.open(false);

                    return true;       // ignored
                }
            });
        }
    }

    public static void checkpointGeneric() {
        configureHomescreen();

        ScreenTransition transition = ScreenTransitionFactory.createStartTransition(grid.loadingMenu, grid.homescreen, grid.screensGroup);
        transition.attach(grid);

        // 20181102 - Broken ending checkpoint check
        if(grid.notification.getQuest("QUEST_SHOWDOWN") != null
                && !grid.isStateUnlocked("chats.teddy.a4_started"))
        {
            // This broken checkpoint only happens right before good ending, after beating FB

            boolean teddyTrusts = getTeddyTrustPoints() > g_teddyTrustSplit;

            grid.flapeeBirdApp.setPlayerScore(0);
            grid.flapeeBirdApp.setAdvancedPlayer(true);

            ACT3.teddySharedShowdownChallenge(teddyTrusts);
        }
    }

    public static void checkpointNewGame() {
        configureHomescreen();

        grid.loadingMenu.detach();

        IntroSequence introSequence = new IntroSequence();
        introSequence.attach(grid);

        // Quests
        grid.notification.addQuest("QUEST1", Notification.QuestType.FAILED, "Restore your phone from a previous backup. (No backup detected)");
        grid.notification.addQuest("QUEST2", Notification.QuestType.SUCCESS, "Renew your phone insurance with Digilink");
        grid.notification.addQuest("QUEST3", Notification.QuestType.SUCCESS, "Please collect your dinner from Penny's Pizza at 7pm");
        grid.notification.addQuest("QUEST4", Notification.QuestType.SUCCESS, "Teddy tried to call you. Call him back");
        grid.notification.addQuest("QUEST5", Notification.QuestType.NOTE, "Paid Ad: Fork Knights! The ultimate medieval utensil based battle royale game! Check it out now!");
        grid.notification.addQuest("QUEST6", Notification.QuestType.FAILED, "68% of your spam came from Karen (Aunt) in the past month. It is highly recommended that you block her.");
        grid.notification.addQuest("QUEST_SAVE_TEDDY", Notification.QuestType.PENDING, "Teddy sounds upset, you should check on him");
    }

    public static void questSuccess(String tag) {
        questSuccess(tag, null);
    }

    public static void questSuccess(String tag, String description) {
        grid.notification.addQuest(tag, Notification.QuestType.SUCCESS, description);
    }

    public static void questFailed(String tag) {
        questFailed(tag, null);
    }

    public static void questFailed(String tag, String description) {
        grid.notification.addQuest(tag, Notification.QuestType.FAILED, description);
    }

    public static void questNote(String tag) {
        questNote(tag, null);
    }

    public static void questNote(String tag, String description) {
        grid.notification.addQuest(tag, Notification.QuestType.NOTE, description);
    }

    public static void questPending(String tag) {
        questPending(tag, null);
    }

    public static void questPending(String tag, String description) {
        grid.notification.addQuest(tag, Notification.QuestType.PENDING, description);
    }

    public static void saveGame() {
        grid.writeSaveGame(CHECKPOINT_GENERIC);
    }


    public static void browserSeenFlapeeBirdStorePage() {
        if (!grid.unlockState(STATE_SEEN_FLAPEE_STORE_PAGE))
            return;     // already seen

        // Add to favourites for easy access
        grid.browserApp.insertMostVisited(0, "flapeebird.web");       // show as first for gameplay
        grid.browserApp.addBookmark("flapeebird.web");
    }

    public static void browserInstallFlapeeBird() {
        if (grid.isStateUnlocked(STATE_FLAPEE_INSTALLING)) {
            grid.homescreen.switchApp(CONTEXT_APP_FLAPEE);
            return;     // already installed
        }

        // Go to homescreen, show authorization view
        // Configure install dialog
        grid.installDialog.attach(grid.homescreen);
        grid.installDialog.setOnAuthorized(new Runnable() {
            @Override
            public void run() {
                installFlapeeBird();
            }
        });
        grid.homescreen.transitionBack(grid.browserApp, grid);

    }

    private static void flapeeBirdInstalled() {
        grid.homescreen.addApp(0, 4, 0, Sprite.load("apps/flapee/icon.png"), "Flapee Bird", CONTEXT_APP_FLAPEE, grid.flapeeBirdApp);

        // Allow duncan yong whisper
        if(!grid.isStateUnlocked(STATE_DUNCAN_YONG_WHISPERED)) {
            grid.homescreen.queueSecret(new Runnable() {
                @Override
                public void run() {
                    // Start whisper and inform done
                    grid.unlockState(STATE_DUNCAN_YONG_WHISPERED);

                    MusicHandler handler = new MusicHandler("content/vo/demon/whisper_duncan.ogg", 0, 0, 1f, false);
                    handler.attach(grid.homescreen);
                }
            }, 20f);
        }
    }

    private static void installFlapeeBird() {
        Sys.debug(TAG, "Installing FlapeeBird");

        grid.unlockState(STATE_FLAPEE_INSTALLING);

        // Add new app
        final Sprite flapeeIcon = Sprite.load("apps/flapee/icon.png");
        Sprite flapeeDisabledIcon = Sprite.load("apps/flapee/icon-disabled.png");
        grid.homescreen.addApp(0, 4, 0, flapeeDisabledIcon, "", CONTEXT_APP_FLAPEE, null);
        Clickable button = grid.homescreen.getButton(CONTEXT_APP_FLAPEE);
        button.windowAnimation(new ScaleAnim(
                0.3f, new QuadraticGraph(0f, 1f, 1.4f, true)
        ).startAndReset(), true, false);

        StaticSprite downloadView = new StaticSprite()
                .viewport(button)
                .visual(flapeeIcon, button.target())
                .attach();
        Animation downloadingAnim = new ScissorAnim(a1_flapee_install_time, new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, ConstantGraph.one, LinearGraph.zeroToOne));
        downloadView.windowAnimation(downloadingAnim.startAndReset(), true, false);
        grid.scheduleRunnable(new Runnable() {
            @Override
            public void run() {
                grid.homescreen.removeApp(CONTEXT_APP_FLAPEE);
                flapeeBirdInstalled();

                grid.notification.show(flapeeIcon, null, -1, "FlapeeBird", "Installed, tap to open", CONTEXT_APP_FLAPEE);

                // For save game
                grid.unlockState(STATE_FLAPEE_INSTALLED);
                // Achievement
                unlockAchievement(Achievement.INSTALLED_FB);

                // Animate done
                Clickable button = grid.homescreen.getButton(CONTEXT_APP_FLAPEE);
                button.windowAnimation(new MoveAnim(
                                0.35f,
                                new SineGraph(1f, 3f, 0f, new LinearGraph(0.09f, 0f), null, null),
                                null
                        ).startAndReset(),
                        true,
                        false
                );

                // Request inform playing a session
                grid.addTrigger(TRIGGER_FLAPEE_FINISHED_SESSION, new Grid.Trigger() {
                    @Override
                    public boolean trigger(String name) {
                        grid.removeTrigger(name);

                        // Unlock next chat
                        grid.state.set("chats.teddy.a1_first_flapee_session", true);

                        return true;
                    }
                });

            }
        }, a1_flapee_install_time);
    }

    public static void browserSeenFlapeeBirdRoyalePage() {
        if (!grid.unlockState(STATE_SEEN_FLAPEE_ROYALE_PAGE))
            return;     // already seen

        // Add to favourites for easy access
        grid.browserApp.insertMostVisited(0, "flapeebird.web/royale");       // show as first for gameplay
        grid.browserApp.addBookmark("flapeebird.web/royale");
    }

    public static void browserRoyalePageClickedSubmitForm() {
        BrowserScreen browser = grid.browserApp;

        if (!browserRoyalePageProcessName()) {
            browser.activateField("name");
            return;
        }
        String ageText = browser.getInput("age");
        if (ageText == null || ageText.isEmpty()) {
            browser.activateField("age");
            return;
        }
        // Evaluate age
        try {
            int age = Integer.parseInt(ageText);
            if (age < 0 || age > 122) {
                browser.activateField("age");
                Sound.load("sounds/general_invalid.ogg").play();
                return;
            }
        } catch (Throwable e) {
            // Unable to parse int
            browser.activateField("age");
            Sound.load("sounds/general_invalid.ogg").play();
            return;
        }
        String sex = browser.getCheckbox("sex");
        if (sex == null) {
            browser.centerCheckbox("male");
            return;
        }
        String gamehours = browser.getCheckbox("gamehours");
        if (gamehours == null) {
            browser.centerCheckbox("gamehours_1");
            return;
        }
        String weight = browser.getCheckbox("weight");
        if (weight == null) {
            browser.centerCheckbox("weight_1");
            return;
        }
        String cigarrettes = browser.getCheckbox("cigarettes");
        if (cigarrettes == null) {
            browser.centerCheckbox("cigarettes_1");
            return;
        }
        String alcohol = browser.getCheckbox("alcohol");
        if (alcohol == null) {
            browser.centerCheckbox("alcohol_1");
            return;
        }
        String seatbelt = browser.getCheckbox("seatbelt");
        if (seatbelt == null) {
            browser.centerCheckbox("seatbelt_1");
            return;
        }
        String sexual = browser.getCheckbox("sexual");
        if (sexual == null) {
            browser.centerCheckbox("sexual_1");
            return;
        }
        String fastfood = browser.getCheckbox("fastfood");
        if (fastfood == null) {
            browser.centerCheckbox("fastfood_1");
            return;
        }

        // Calculate life expectancy
        float lifeExpectancy = calculateRoyalePageLifeExpectancy();
        if (lifeExpectancy < flapeeBirdPageMinLifeExpectancy) {
            // Rejected
            browser.openPage("flapeebird.web/rejected");
        } else {
            // Set flapeebird life hours
            if (!grid.isStateUnlocked(STATE_FLAPEE_ROYALE_UNLOCKED)) {
                int lifehours = Math.round(lifeExpectancy * 365f * 24f);
                grid.flapeeBirdApp.setLifehours(lifehours);
                grid.state.set("stats.startingLifehours", lifehours);
            }

            // Accepted
            browser.openPage("flapeebird.web/accepted");
        }
    }

    private static float calculateRoyalePageLifeExpectancy() {
        BrowserScreen browser = grid.browserApp;

        // Sex
        String sex = browser.getCheckbox("sex");
        if (sex == null)
            return -1;      // required
        boolean isFemale = sex.equals("female");

        // Age
        String ageText = browser.getInput("age");
        int age;
        if (ageText == null || ageText.isEmpty())
            return -1;      // required
        try {
            age = Integer.parseInt(ageText);
        } catch (Throwable e) {
            return -1;      // required, invalid text
        }

        // Lookup remaining life expectancy by gender
        float[] lifeExpectancyTable = isFemale ? femaleLifeExpectancyTable : maleLifeExpectancyTable;
        if (age < 0)
            age = 0;
        if (age > lifeExpectancyTable.length)
            age = lifeExpectancyTable.length - 1;
        float lifeExpectancy = lifeExpectancyTable[age];

        // Weight
        String weight = browser.getCheckbox("weight");
        if (weight == null)
            weight = "weight_3";        // Assume worst
        // Years of life lost ranged from 6.5 years for participants with a BMI of 40-44.9 to 13.7 years for a BMI of 55-59.9
        if (weight.equals("weight_2"))
            lifeExpectancy -= 6.5f;
        else if (weight.equals("weight_3"))
            lifeExpectancy -= 13.7f;

        // Cigarettes
        String cigarettes = browser.getCheckbox("cigarettes");
        if (cigarettes == null)
            cigarettes = "cigarettes_3";        // Assume worst
        // Every year a man smokes a pack a day, he shortens his life by almost 2 months., University of California, Berkeley Wellness Letter, April 2000
        if (!cigarettes.equals("cigarettes_1")) {
            // Is a smoker
            float daysLostPerPack = 60f / 365f;     // 60 days lost for each year, 1 pack each day
            if (cigarettes.equals("cigarettes_2"))       // 1 packs a day average
                daysLostPerPack *= 1;
            else if (cigarettes.equals("cigarettes_3"))      // 4 packs a day average
                daysLostPerPack *= 3;
            lifeExpectancy *= (1f - daysLostPerPack);       // Assume smokes everyday
        }

        // Alcohol
        String alcohol = browser.getCheckbox("alcohol");
        if (alcohol == null)
            alcohol = "alcohol_3";        // Assume worst
        // The research found that drinking more than 100 grams of alcohol per week -- equal to roughly seven standard drinks in the United States or five to six glasses of wine in the UK -- increases your risk of death from all causes and in turn lowers your life expectancy.
        // Compared to drinking under 100 grams of alcohol per week, drinking 100 to 200 grams was estimated to shorten the life span of a 40-year-old by six months. Drinking 200 to 350 grams per week was estimated to reduce life span by one to two years and drinking more than 350 grams per week by four to five years.
        if (!alcohol.equals("alcohol_1")) {
            if (alcohol.equals("alcohol_2"))       // 1 ~ 14 glasses
                lifeExpectancy -= 1.5f;
            else if (alcohol.equals("alcohol_3"))      // 15 glasses or more
                lifeExpectancy -= 4.5f;
        }

        // Seatbelt
        String seatbelt = browser.getCheckbox("seatbelt");
        if (seatbelt == null)
            seatbelt = "seatbelt_3";        // Assume worst
        if (!seatbelt.equals("seatbelt_1")) {
            // In 2016 alone, seat belts saved an estimated 14,668 lives and could have saved an additional 2,456 people if they had been wearing seat belts.
            // US population for 2016: 323.4m
            float magic = 3000000;
            float deathRate = (14668 + 2456 + magic) / 323000000f;
            if (seatbelt.equals("seatbelt_2"))
                deathRate *= 0.5f;      // Half the time
            float chanceOfSurvivingWithoutFatalAccident = (float) Math.pow(1f - deathRate, lifeExpectancy);
            lifeExpectancy *= chanceOfSurvivingWithoutFatalAccident;
        }


        String sexual = browser.getCheckbox("sexual");
        if (sexual == null)
            sexual = "sexual_3";        // Assume worst
        if (!sexual.equals("sexual_1")) {
            // The life expectancy of a coke addict is 44 years – this means the average cocaine addict loses 44% of their life.
            if (sexual.equals("sexual_2"))
                lifeExpectancy *= 0.88f;
            else
                lifeExpectancy *= 0.66f;
        }


        String fastfood = browser.getCheckbox("fastfood");
        if (fastfood == null)
            fastfood = "fastfood_3";    // Assume worst
        if (!fastfood.equals("fastfood_1")) {
            // https://www.everydayhealth.com/diet-nutrition/a=burger-for-lunch-shaves-30-minutes-a-day-off-your-life.aspx
            float mealsPerWeek;
            if (fastfood.equals("fastfood_2"))
                mealsPerWeek = 30f;          // 3 meals per week, magic number
            else
                mealsPerWeek = 80f;         // 10 meals per week, magic number
            float yearsLostPerYear = (30f * (mealsPerWeek / 7f) * 365f) / (60f * 24f * 365f);       // 30 mins lost per meal
            lifeExpectancy -= yearsLostPerYear * lifeExpectancy;
        }

        Sys.debug(TAG, "Calculated life expectancy: " + lifeExpectancy);
        return lifeExpectancy;
    }

    private static boolean compareName(String name, String[] names) {
        for (String n : names) {
            if (n.compareToIgnoreCase(name) == 0)
                return true;
        }
        return false;
    }

    private static boolean comparePartialName(String name, String[] names) {
        name = name.toLowerCase();
        for (String n : names) {
            if (name.startsWith(n) || name.endsWith(n))
                return true;
        }
        return false;
    }


    private static boolean browserRoyalePageProcessName() {
        BrowserScreen browser = grid.browserApp;
        String fullName = browser.getInput("name");
        if (fullName == null)
            return false;
        // Reset
        if (grid.isStateUnlocked("chats.teddy.player_name_is_teddy"))
            grid.state.set("chats.teddy.player_name_is_teddy", false);
        if (grid.isStateUnlocked("chats.teddy.player_name_is_profanity"))
            grid.state.set("chats.teddy.player_name_is_profanity", false);
        if (grid.isStateUnlocked("chats.teddy.player_is_youtuber"))
            grid.state.set("chats.teddy.player_is_youtuber", false);
        // Check if player's name contains profanity
        String[] names = fullName.trim().split("\\s+");
        String trimmedName = "";
        for (String name : names) {
            if (!grid.isStateUnlocked(STATE_FLAPEE_ROYALE_UNLOCKED) && comparePartialName(name, profanityNames))
                grid.unlockState("chats.teddy.player_name_is_profanity");
            trimmedName += name;
        }
        // Check if player is a youtuber
        if (!grid.isStateUnlocked(STATE_FLAPEE_ROYALE_UNLOCKED) && compareName(trimmedName, youtuberNames))
            grid.unlockState("chats.teddy.player_is_youtuber");
        // Find longest word
        for (String name : names) {
            if (name.length() >= 3) {
                // Found a valid name
                if (!grid.isStateUnlocked(STATE_FLAPEE_ROYALE_UNLOCKED)) {
                    grid.state.set("player.name", name);
                    // Check if name is teddy
                    if (name.equalsIgnoreCase("Teddy"))
                        grid.unlockState("chats.teddy.player_name_is_teddy");
                }
                return true;
            }
        }
        return false;       // no proper name found
    }

    public static void browserRoyalePageTickedForm() {
        if (grid.isStateUnlocked(STATE_FLAPEE_ROYALE_UNLOCKED))
            return;     // already unlocked

        BrowserScreen browser = grid.browserApp;

        float lifeExpectancy = calculateRoyalePageLifeExpectancy();
        if (lifeExpectancy >= flapeeBirdPageExcitedLifeExpectancy && browserRoyalePageProcessName()) {
            // Set flapeebird life hours
            int lifehours = Math.round(lifeExpectancy * 365f * 24f);
            grid.flapeeBirdApp.setLifehours(lifehours);
            grid.state.set("stats.startingLifehours", lifehours);

            // Life expectancy is very good (almost exciting) and the name is valid, glitch straight into accepted page
            browser.openPage("flapeebird.web/accepted");
            // Glitch
            MpegGlitch glitch = new MpegGlitch("sounds/glitch_start_low.ogg", null);
            glitch.setGlitchGraph(null, false, new QuadraticGraph(3.0f, 0.14f, 1.5f, 0, true));
            glitch.attach(grid);
            glitch.detachWithAnim();
        }
    }

    public static void browserRoyaleOpenApp() {
        grid.homescreen.transitionBack(grid.browserApp, grid);
        grid.homescreen.queueAppOnShow(Globals.CONTEXT_APP_FLAPEE);
    }

    public static void unlockFlapeeRoyaleMode() {
        if (!grid.unlockState(STATE_FLAPEE_ROYALE_UNLOCKED))
            return;     // already unlocked

        grid.notification.show(Sprite.load("apps/flapee/icon.png"), null, -1, "FlapeeBird", "Royale mode unlocked", CONTEXT_APP_FLAPEE);
        grid.flapeeBirdApp.setAdvancedPlayer(true);
        grid.flapeeBirdApp.loadLevel(GBNormalLevel.class);
        grid.flapeeBirdApp.setPlayerScore(0);           // reset

        // Reset state
        grid.state.set("chats.teddy.flapee_score_beat", false);
        grid.state.set("chats.teddy.flapee_session_1", true);       // Mark as session 1

        // Achievement
        if (grid.isStateUnlocked("chats.teddy.player_name_is_teddy"))
            unlockAchievement(Achievement.NAME_TEDDY);
        if (grid.isStateUnlocked("chats.teddy.player_name_is_profanity"))
            unlockAchievement(Achievement.NAME_EXPLETIVE);
        if (grid.isStateUnlocked("chats.teddy.player_is_youtuber"))
            unlockAchievement(Achievement.NAME_YOUTUBER);

        // Save
        saveGame();
    }

    public static void mailKarenSpam1() {
        // Open page
        grid.browserApp.clearPageTab();
        grid.browserApp.showPage(grid.browserApp.getPage("totallyfreegames.web/FREE-KRYSTALS"));
        grid.browserApp.open(grid.mailApp.threadScreen, grid.screensGroup, -1, -1, -1);
    }

    public static void mailOpenFlapeeBird() {
        if (grid.unlockState(STATE_SHOWED_TEDDY_MAIL_VID)) {
            // Show video
            ACT2.showGatewayVideo(0, 0, "Webcam/screaming", "content/codes/code-2.png", "sounds/flapee/morse-6.ogg");
        } else {
            // Open page
            grid.browserApp.clearPageTab();
            grid.browserApp.showPage(grid.browserApp.getPage("flapeebird.web/royale"));
            grid.browserApp.open(grid.mailApp.threadScreen, grid.screensGroup, -1, -1, -1);
        }
    }


    public static void mailOpenCbbGregPage() {
        // Open page
        grid.browserApp.clearPageTab();
        grid.browserApp.showPage(grid.browserApp.getPage("cbb.web/profiles/greg-summers"));
        grid.browserApp.open(grid.mailApp.threadScreen, grid.screensGroup, -1, -1, -1);
    }

    public static void callsShowTeddyHiddenVideo() {
        if (!grid.isStateUnlocked(STATE_FLAPEE_INSTALLED))
            return;     // wait for flapee bird to be installed
        if (!grid.unlockState(STATE_SHOWED_TEDDY_CALL_VID))
            return;
        // On call end, schedule show video
        ACT2.showGatewayVideo(3f, 5f, "Webcam/banghead", "content/codes/code-5.png", "sounds/flapee/morse-0.ogg");          // Code 5 of 1618033
    }

    public static void finishedFlapeeSessionAllowEggShop() {
        if (!grid.unlockState(STATE_FLAPEE_EGGSHOP_UNLOCKED))
            return;     // already unlocked eggshop

        // Glitch a bit
        MpegGlitch shopBuyGlitch = new MpegGlitch(null, "sounds/flapee/theme-demon100-muted.ogg");
        shopBuyGlitch.setGlitchGraph(
                null,
                false,
                new CompoundGraph(new Graph[]{
                        new ConstantGraph(2.8f, 2.3f),
                        new ConstantGraph(0.0f, 0.1f),
                        new ConstantGraph(2.8f, 1.2f),
                        new ConstantGraph(0.0f, 0.15f),
                        new ConstantGraph(2.8f, 0.7f),
                        new ConstantGraph(0.0f, 0.12f),
                        new ConstantGraph(2.8f, 0.3f)
                })
        );
        shopBuyGlitch.setGlitchLoopThreshold(0.5f);
        shopBuyGlitch.setLsdEffect(0.5f, 0);
        shopBuyGlitch.setGlitchLoopStopsPlayback(true);
        shopBuyGlitch.setInputBlockTime(4f);
        shopBuyGlitch.attach(grid);
        shopBuyGlitch.detachWithAnim();

        grid.flapeeBirdApp.setEggShopAllowed(true);
        grid.flapeeBirdApp.startBirdHovering();

        // Parody jabbr post
        grid.unlockState("friends.after_demon_reveal");

        grid.removeTrigger(TRIGGER_FLAPEE_DEMON_SPEAKS);
    }

    public static void finishedFlapeeSession1() {
        // Inform teddy
        grid.unlockState("chats.teddy.flapee_score_beat");

        // Achievement
        ACT1.unlockAchievement(Achievement.FIRST_FB_WIN);

        // Remove previous effect
        BlissEffect blissEffect = grid.iterate(null, BlissEffect.class, false, null);
        if (blissEffect != null)
            blissEffect.detach();

        // Enter the bliss
        blissEffect = new BlissEffect(
                "sounds/flapee/bliss_1.ogg", true,
                0.3f,
                0.05f,
                0.2f,
                0.0f,
                3.0f,
                new SequenceAnim(new Animation[]{
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
        grid.flapeeBirdApp.startUpdating(1.353f);

        // Make sure egg shop is unlocked this moment on
        grid.flapeeBirdApp.setEggShopAllowed(true);
        grid.unlockState(STATE_FLAPEE_EGGSHOP_UNLOCKED);
        grid.removeTrigger(TRIGGER_FLAPEE_DEMON_SPEAKS);
    }

    public static void modifyTeddyTrust(int value) {
        int current = grid.state.get(STATE_TEDDY_TRUST, 0);
        grid.state.set(STATE_TEDDY_TRUST, current + value);
        updateTeddyTrustWallpaper();
        // TODO: save game
    }

    public static int getTeddyTrustPoints() {
        return grid.state.get(STATE_TEDDY_TRUST, 0);
    }

    public static void evaluateTeddyTrust(String trustTag, String dontTrustTag) {
        if (getTeddyTrustPoints() > g_teddyTrustSplit)
            grid.unlockState(trustTag);
        else
            grid.unlockState(dontTrustTag);
    }

    private static void updateTeddyTrustWallpaper() {
        int current = grid.state.get(STATE_TEDDY_TRUST, 0);
        if (current > g_teddyTrustSplit)
            grid.wallpaperSprite = Sprite.load("content/gallery/docs/good.png");
        else
            grid.wallpaperSprite = Sprite.load("content/gallery/docs/bad.png");
    }


    public static void browserBoxDropFind() {
        String dropId = grid.browserApp.getInput("dropID");
        if (dropId == null || (dropId = dropId.trim()).isEmpty())
            grid.browserApp.activateField("dropID");        // activate field again
        else {
            // Find if the page exists
            String url = "boxdrop.web/" + dropId;
            if (grid.browserApp.getPage(url) != null)
                grid.browserApp.openPage(url);
            else
                grid.browserApp.openPage("boxdrop.web/!");      // not found

        }
    }

    public static void browserBoxDropDownload(String media) {
        browserBoxDropDownload(media, 10f);     // default 10 seconds
    }

    public static void browserBoxDropDownload(final String media, float tDownloadTime) {
        if(grid.photoRollApp.isUnlocked(media)) {
            // Already downloaded
            grid.homescreen.switchApp(Globals.CONTEXT_APP_DOWNLOADS);
        }
        else {
            Sys.info(TAG, "Downloading " + media);
            grid.notification.startDownload("DOWNLOAD " + media, tDownloadTime,
                    new Runnable() {
                        @Override
                        public void run() {
                            grid.photoRollApp.unlock(media, true);
                            grid.downloadsApp.refreshNotification(grid.homescreen);
                            // Achievement
                            unlockAchievement(Achievement.TEDDY_VLOGS, media);
                            // Inform teddy
                            grid.unlockState("chats.teddy.a2_seen_videos");
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            grid.homescreen.switchApp(Globals.CONTEXT_APP_DOWNLOADS);
                        }
                    }
            );
            grid.browserApp.openPage("boxdrop.web");
        }
    }



    public static void openKarenInvite() {
        // Open not enough space dialog
        grid.notEnoughSpaceDialog.open();
    }

    public static void openFlapeeInvite() {
        grid.homescreen.switchApp(CONTEXT_APP_FLAPEE);      // open flapee bird
    }



    public static void startDemonTeddy(String soundFilename) {
        startDemonTeddy("Teddy", soundFilename);
    }

    public static void startDemonTeddy(String contactName, final String soundFilename) {
        // Get contact
        WhatsupContact contact = grid.whatsupApp.findContact(contactName);

        // Clear custom sounds
        contact.customMessageSound = Sound.load("sounds/chat_reply_distort.ogg");
        contact.customNotificationSound = Sound.load("sounds/chat_notify_distort.ogg");

        // Play audio if needed
        if(soundFilename != null) {
            grid.idleScare.reschedule();
            grid.scheduleRunnable(new Runnable() {
                @Override
                public void run() {
                    Audio.playMusic(soundFilename, false);

                    // Check if there is an effect going
                    if(grid.iterate(null, GloomEffect.class, false, null) == null &&
                            grid.iterate(null, BlissEffect.class, false, null) == null &&
                            grid.iterate(null, MpegGlitch.class, false, null) == null &&
                            grid.iterate(null, LcdBurnEffect.class, false, null) == null) {
                        // Add affect
                        MpegGlitch glitch = new MpegGlitch(null, null);
                        glitch.setGlitchGraph(null, false, new QuadraticGraph(1.5f, 0f, 0.5f, 0, true));
                        glitch.attach(grid);
                        glitch.detachWithAnim();
                    }

                }
            }, 1.5f);     // Delay a bit
        }

    }

    public static void endDemonTeddy() {
        endDemonTeddy("Teddy");
    }

    public static void endDemonTeddy(String contactName) {
        // Get contact
        WhatsupContact contact = grid.whatsupApp.findContact(contactName);

        // Clear custom sounds
        contact.customMessageSound = null;
        contact.customNotificationSound = null;
    }

    public static void unlockWhenSeenMessage(String tag) {
        if(!grid.unlockState("triggers.unlockWhenSeenMessage." + tag))
            return;     // Already done
        grid.unlockState(tag);
    }

    public static void unlockAchievement(Globals.Achievement achievement) {
        unlockAchievement(achievement, 1, null);
    }

    public static void unlockAchievement(Globals.Achievement achievement, String itemId) {
        unlockAchievement(achievement, 1, itemId);
    }

    public static void unlockAchievement(Globals.Achievement achievement, int items) {
        unlockAchievement(achievement, items, null);
    }

    public static void unlockAchievement(Globals.Achievement achievement, int items, String itemId) {
        // Get count
        Grid v = Globals.grid;
        String stateName = Globals.STATE_ACHIEVEMENTS_PREFIX + achievement.name();
        int count = v.state.get(stateName, 0);
        if(count < 0)
            return;         // already unlocked, UB
        // Check item
        if(itemId != null && !v.unlockState(Globals.STATE_ACHIEVEMENTS_ITEM_PREFIX + itemId))
            return;     // this item was already unlocked
        count += items;
        if(count >= achievement.count) {
            // Unlock achievement
            v.state.set(stateName, -1);         // Mark as unlocked
            Game.game.platform.unlockAchievement(achievement);
            // Analytics
            Game.analyticsString(Globals.ANALYTICS_EVENT_ACHIEVEMENT, Globals.ANALYTICS_EVENT_ACHIEVEMENT_FIELD, achievement.name());
        }
        else {
            v.state.set(stateName, count);      // Set updated count
            Sys.info(TAG, "Increasing achievement counter " + achievement + ": " + count + "/" + achievement.count);
        }

    }
}
