package game31;

import com.badlogic.gdx.Net;
import com.google.gson.Gson;

import java.util.Locale;

import game31.app.flapee.FlapeeBirdScreen;
import game31.gb.flapee.GBDemoLevel;
import sengine.utils.LiveEditor;
import sengine.utils.NetRequest;
import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 28/6/2016.
 */
public class Globals {



    public enum Achievement {
        INSTALLED_FB,                   // Best. Game. Ever.
        FIRST_FB_WIN,                   // Number one
        SECOND_FB_WIN,                  // Number one, again
        SHOWDOWN_WIN,                   // You're better than him
        SHOWDOWN_GIVEUP,                // You gave up.. chicken.. buck buck buck buckukk

        TEDDY_VLOGS(9),                 // My name is Teddy
        TEDDY_WEBCAM_VIDEOS(6),         // Voyeur
        IRIS_ADS(6),                    // Obsessed

        SHARED_FB_TO_ALL(5),            // Viral Video Game

        DIE_IN_FB(50),                 // Suck at this
        SCORED_IN_FB(300),              // Good at this
        REVIVE_IN_FB(50),              // One more game...
        JETSTREAM_IN_FB(50),           // Gushing through

        BAD_HEALTH,                     // Get that checked

        ABSTAIN_FROM_CHOICE(8),         // Passive Player

        LIFEHOURS_DRAINED,              // Subscription Expired

        SECRET_DEMON_CALLS(4),          // Cold Cases

        CHEATED_FB,                     // No cheating!

        NAME_TEDDY,                     // Teddyception
        NAME_EXPLETIVE,                 // Potty name
        NAME_YOUTUBER,                  // Influencer

        ARG_PHONECALL,                  // After phonecall
        ARG_FRONT_WEBSITE,              // Discovered the Path of Decay website
        ARG_GATEWAY_WEBSITE,            // Discovered the Gateway 31 website

        GAMEMAKER,                      // Uncovered the story of Duncan Yong

        ENDING_BOTH_DIE,
        ENDING_TEDDY_DIES,
        ENDING_PLAYER_DIES,
        ENDING_BOTH_SURVIVE,

        ;

        public final int count;

        Achievement() {
            this(1);            // default 1
        }

        Achievement(int count) {
            this.count = count;
        }
    }


    public static int buildNumber = 48;
    public static String version = "1.0." + buildNumber; //this gets set in MainActivity.java just before the startGame() call on Android.

    public static float MIN_LENGTH = 16f / 9f;          // 16f / 9f
    public static float LENGTH = 18.5f / 9f;

    public static int topSafeAreaInset = 0;
    public static int bottomSafeAreaInset = 0;

    public static float r_godraysResolution = 0.25f;            // 0.5
    public static float r_diffusionEffectResolution = 0.5f;
    public static float r_liquidCrystalEffectResolution = 1f;
    public static boolean r_highQuality = true;

//    public static boolean g_hasAdsRemoved = false;
//    public static boolean g_hasAdsRemoved = true;

    public static final String MAIL_BLOCK_TOKEN = "#block#";
    public static final String MAIL_ACTION_PREFIX = "action://";
    public static final String MAIL_GRAPHIC_PREFIX = "graphic://";
    public static final String MAIL_ALIGN_CENTER_PREFIX = "align://center/";
    public static final String MAIL_ALIGN_RIGHT_PREFIX = "align://right/";

    public static final String PHOTOROLL_PREFIX = "photoroll://";
    public static final String SECTION_PREFIX = "chats://section/";
    public static final String INVITE_PREFIX = "chats://invite/";
    public static final String GROUP_ADD_PREFIX = "groupadd/";
    public static final String GROUP_REMOVE_PREFIX = "groupremove/";
    public static final String BROWSER_PREFIX = "browser://";
    public static final String CHATS_FONT_PREFIX = "font://";
    public static final String CHATS_SPLIT_TOKEN = "=";
    public static final String VOICE_PREFIX = "vo://";

    public static final String ATTACHMENT_TITLE_TOKEN = "=";
    public static final String ATTACHMENT_WILDCARD = "photoroll://?";

    public static final String KEYBOARD_WILDCARD = "keyboard://";
    public static final String KEyBOARD_ALPHANUMERIC_WILDCARD = "keyboard://alphanumeric";
    public static final String KEYBOARD_NUMERIC_WILDCARD = "keyboard://numeric";
    public static final String DIALOG_TIMER = "timer://";

    public static final String BROWSER_NOT_FOUND_PAGE = "surfer://404";

    public static final String CORRUPTED_PREFIX = "corrupted://";

    public static final String ORIGIN_USER = "user";
    public static final String ORIGIN_SENDER = "sender";

    public static final String TIME_AUTO = "auto";

    public static final String STATE_PREFIX = "#state:";
    public static final String STATE_SUFFIX = "#";

    public static final String FRIENDS_STATE_PREFIX = "friends.";

    public static final String FLAPEE_BIRD_ADS_ALBUM = "Ads";

    public static boolean isWindowed = false;

    public static String helpTwitterURL = "https://twitter.com/playsimulacra";
    public static String helpKaiganURL = "https://kaigangames.com/";
    public static String helpMailURL = helpKaiganURL + "contact/";
    public static String helpFacebookURL = "https://www.facebook.com/playsimulacra/";
    public static String helpPrivacyPolicyURL = "https://privacypolicies.com/privacy/view/28562127727456bc4d6929b805fa6d5a";

    public static String helpDiscordURL = "https://discord.com/invite/kaigangames";

    public static Grid grid;

    public static float consoleChars = 18;

    public static float maxTouchMoveDistance = 0.05f;           // for some buttons
    public static float minTouchMoveDistance = 0.01f;           // for some surfaces

    public static float minWordDragDistance = 0.06f;
    public static float maxWordSnapDistance = 0.2f;

    public static float loadingFrameRate = 1f / 10f;        // 10fps when loading

    public static float surfaceSnapDistance = 0.08f;            // to decide whether to use smooth scrolling or move

    public static int layoutSectionSize = 192;                  // Default values on the basis of 1080p source resolution
    public static int layoutContentSectionSize = 16;

    public static float s_mediaSeekResolution = 1f;       // 1 second
    public static float s_mediaMinSeekDiff = 0.5f;

    public static boolean d_ignoreJabbrSaves = false;
    public static boolean d_ignoreSurferSaves = false;
    public static boolean d_showDevTools = false;
    public static boolean d_showBetaFeedback = false;

    public static String d_betaFeedbackLink = "https://docs.google.com/forms/d/e/1FAIpQLSe6MQcYx4CI9LLTJ9zXLah34yjGn7U7uWEVtbakhOr3mP6Jmw/viewform?usp=sf_link";


    public static float doubleClickTime = 0.5f;
    public static float singleClickTime = 0.2f;


    public static float tFriendsTriggerInterval = 5f;


    public static float tCorruptedTextInterval = 0.08f;
    public static int corruptedMessageDuplicates = 10;

    public static boolean compileVoiceProfiles = false;
    public static boolean recompileVoiceProfiles = false;
    public static boolean compileCallVoiceProfiles = true;

    public static float idleMinTimeInterval = 0.1f;
    public static float inputMaxFramerateTime = 5f;
    public static float renderChangeMaxFramerateTime = 5f;

    public static long gameTimeOffset = 1529817438000L;         // Sunday, June 24, 2018 1:17:18 PM GMT+08:00
    public static float batteryStartLevel = 1.0f;
    public static float batteryDrainRate = 1f / (3600f * 3.5f);           // Drains to min in about 3.5 hours
    public static float batteryMinLevel = 0.15f;

    public static float tRestoreSkipDelay = 10f;

    public static int g_flapeeDefaultEggs = 50;
    public static int g_flapeeShopPurchasedThreshold = 100;

    public static Class<? extends FlapeeBirdScreen.LevelSource> g_flapeeDefaultLevel = GBDemoLevel.class;

    public static int g_teddyTrustSplit = 0;

//    public static boolean g_showRealAds = true; //Ad removed
    public static boolean g_showRealAds = false;

    public static Gson gson = new Gson();
    public static SheetsParser sheets = new SheetsParser();

    public static final String CONTEXT_APP_CHATS = "CONTEXT_APP_CHATS";
    public static final String CONTEXT_APP_FRIENDS = "CONTEXT_APP_FRIENDS";
    public static final String CONTEXT_APP_CALLS = "CONTEXT_APP_CALLS";
    public static final String CONTEXT_APP_GALLERY = "CONTEXT_APP_GALLERY";
    public static final String CONTEXT_APP_MAIL = "CONTEXT_APP_MAIL";
    public static final String CONTEXT_APP_SPARK = "CONTEXT_APP_SPARK";
    public static final String CONTEXT_APP_BROWSER = "CONTEXT_APP_BROWSER";
    public static final String CONTEXT_APP_VLOGGR = "CONTEXT_APP_VLOGGR";
    public static final String CONTEXT_APP_FLAPEE = "CONTEXT_APP_FLAPEE";
    public static final String CONTEXT_APP_DOWNLOADS = "CONTEXT_APP_DOWNLOADS";
    public static final String CONTEXT_APP_CHEAT_GAMESPEED = "CONTEXT_APP_CHEAT_GAMESPEED";
    public static final String CONTEXT_APP_CHEAT_EGGS = "CONTEXT_APP_CHEAT_EGGS";


    public static final String TRIGGER_OPEN_VIDEO_FROM_MESSAGES = "TRIGGER_OPEN_VIDEO_FROM_MESSAGES";
    public static final String TRIGGER_BACK_TO_INBOX = "TRIGGER_BACK_TO_INBOX";
    public static final String TRIGGER_LEAVE_CHAT_THREAD_SCREEN = "TRIGGER_LEAVE_CHAT_THREAD_SCREEN";
    public static final String TRIGGER_BACK_TO_HOMESCREEN = "TRIGGER_BACK_TO_HOMESCREEN";
    public static final String TRIGGER_HOMESCREEN_OPENED = "TRIGGER_HOMESCREEN_OPENED";
    public static final String TRIGGER_PHONECALL_ENDED = "TRIGGER_PHONECALL_ENDED";
    public static final String TRIGGER_PHONECALL_CANCELLED = "TRIGGER_PHONECALL_CANCELLED";
    public static final String TRIGGER_PHONECALL_ACCEPTED = "TRIGGER_PHONECALL_ACCEPTED";
    public static final String TRIGGER_PHONECALL_DECLINED = "TRIGGER_PHONECALL_DECLINED";
    public static final String TRIGGER_BACK_FROM_VIDEO_PLAYER = "TRIGGER_BACK_FROM_VIDEO_PLAYER";
    public static final String TRIGGER_VIDEO_PLAYBACK_FINISHED = "TRIGGER_VIDEO_PLAYBACK_FINISHED";
    public static final String TRIGGER_LEAVING_VIDEO_SCREEN = "TRIGGER_LEAVING_VIDEO_SCREEN";

    public static final String TRIGGER_LEAVING_BROWSER_PAGE = "TRIGGER_LEAVING_BROWSER_PAGE";
    public static final String TRIGGER_LEAVING_BROWSER_SCREEN = "TRIGGER_LEAVING_BROWSER_SCREEN";

    public static final String TRIGGER_OPEN_GALLERY_FROM_HOMESCREEN = "TRIGGER_OPEN_GALLERY_FROM_HOMESCREEN";
    public static final String TRIGGER_PHOTOROLL_VIEWER_NAVIGATE = "TRIGGER_PHOTOROLL_VIEWER_NAVIGATE";
    public static final String TRIGGER_PHOTOROLL_OPEN_MEDIA = "TRIGGER_PHOTOROLL_OPEN_MEDIA";

    public static final String TRIGGER_FLAPEE_FINISHED_SESSION = "TRIGGER_FLAPEE_FINISHED_SESSION";
    public static final String TRIGGER_FLAPEE_SHOWDOWN_GIVEUP = "TRIGGER_FLAPEE_SHOWDOWN_GIVEUP";
    public static final String TRIGGER_FLAPEE_SHOWDOWN_WON = "TRIGGER_FLAPEE_SHOWDOWN_WON";
    public static final String TRIGGER_FLAPEE_SHOW_MENU = "TRIGGER_FLAPEE_SHOW_MENU";
    public static final String TRIGGER_FLAPEE_SCORED_POINT = "TRIGGER_FLAPEE_SCORED_POINT";


    public static final String TRIGGER_FRIENDS_FORGOT_PASSWORD = "TRIGGER_FRIENDS_FORGOT_PASSWORD";
    public static final String TRIGGER_FRIENDS_LOGIN_OPENED = "TRIGGER_FRIENDS_LOGIN_OPENED";

    public static final String TRIGGER_LOCKSCREEN_KEY_SHOWN = "TRIGGER_LOCKSCREEN_KEY_SHOWN";
    public static final String TRIGGER_LOCKSCREEN_KEY_CLICKED = "TRIGGER_LOCKSCREEN_KEY_CLICKED";
    public static final String TRIGGER_LOCKSCREEN_UNLOCK = "TRIGGER_LOCKSCREEN_UNLOCK";

    public static final String TRIGGER_FLAPEE_LEAVE_SCREEN = "TRIGGER_FLAPEE_LEAVE_SCREEN";
    public static final String TRIGGER_FLAPEE_NEW_HIGH_SCORE = "TRIGGER_FLAPEE_NEW_HIGH_SCORE";
    public static final String TRIGGER_FLAPEE_DEMON_SPEAKS = "TRIGGER_FLAPEE_DEMON_SPEAKS";
    public static final String TRIGGER_FLAPEE_POWERUPS_USED = "TRIGGER_FLAPEE_POWERUPS_USED";
    public static final String TRIGGER_FLAPEE_HOURS_PURCHASED = "TRIGGER_FLAPEE_HOURS_PURCHASED";
    public static final String TRIGGER_FLAPEE_SHOP_OPENED = "TRIGGER_FLAPEE_SHOP_OPENED";

    // Save states
    public static final String SAVE_FILENAME = "save20180928.fs";

    public static final String PREF_FILENAME = "preferences";
    public static final String STATE_FINISHED_GAME = "STATE_FINISHED_GAME";
    public static final String STATE_SUBTITLE_ENABLED = "STATE_SUBTITLE_ENABLED";
    public static final String STATE_HQ_VIDEOS_ENABLED = "STATE_HQ_VIDEOS_ENABLED";
    public static final String STATE_HAS_PLAYED = "STATE_HAS_PLAYED";
    public static final String STATE_HAS_DISCORD_OPENED = "STATE_HAS_DISCORD_OPENED";
    public static final String STATE_HAS_OPINION_ENJOYED = "STATE_HAS_OPINION_ENJOYED";
    public static final String STATE_HAS_ADS_REMOVED = "STATE_HAS_ADS_REMOVED";

    public static final String SAVE_CHECKPOINT = "SAVE_CHECKPOINT";
    public static final String SAVE_TIME_ELAPSED = "SAVE_TIME_ELAPSED";
    public static final String SAVE_BATTERY_LEVEL = "SAVE_BATTERY_LEVEL";

    public static final String STATE_FLAPEEBIRD_DEMON_CHATTER = "flapeebird.demonchatter.";

    public static final String STATE_ACHIEVEMENTS_PREFIX = "ACHIEVEMENTS.";
    public static final String STATE_ACHIEVEMENTS_ITEM_PREFIX = "ACHIEVEMENTS_ITEM.";

    public static final String STATE_IDLE_SCARE_COUNT = "STATE_IDLE_SCARE_COUNT";
    public static final String STATE_IDLE_SCARE_LAST_TIME = "STATE_IDLE_SCARE_LAST_TIME";

    public static final String STATE_FLAPEE_SHOWN_FINDING_FRIENDS = "STATE_FLAPEE_SHOWN_FINDING_FRIENDS";
    public static final String STATE_FLAPEE_PERMISSIONS_ACCEPTED = "STATE_FLAPEE_PERMISSIONS_ACCEPTED";

    public static final String STATE_TEDDY_TRUST = "STATE_TEDDY_TRUST";

    public static final String ANALYTICS_CONTENT_TYPE_CHATS = "Chats";
    public static final String ANALYTICS_CONTENT_TYPE_CALLS = "Calls";
    public static final String ANALYTICS_CONTENT_TYPE_GALLERY = "Gallery";
    public static final String ANALYTICS_CONTENT_TYPE_MAIL = "Mail";
    public static final String ANALYTICS_CONTENT_TYPE_BROWSER = "Browser";
    public static final String ANALYTICS_CONTENT_TYPE_JABBR = "Jabbr";
    public static final String ANALYTICS_CONTENT_TYPE_FLAPEEBIRD = "FlapeeBird";
    public static final String ANALYTICS_CONTENT_TYPE_IRIS_ADS = "Iris ADS";
    public static final String ANALYTICS_CONTENT_TYPE_TEDDY_ADS = "Teddy ADS";
    public static final String ANALYTICS_CONTENT_TYPE_MAIN_MENU = "Main Menu";
    public static final String ANALYTICS_CONTENT_MAIN_MENU_SIMPROMO = "SIM Promo";
    public static final String ANALYTICS_CONTENT_MAIN_MENU_CREDITS = "Credits";
    public static final String ANALYTICS_CONTENT_MAIN_MENU_STATS = "Stats";
    public static final String ANALYTICS_EVENT_FLAPEEBIRD_BUY = "FB Buy";
    public static final String ANALYTICS_EVENT_FLAPEEBIRD_BUY_FIELD = "Lifehours";
    public static final String ANALYTICS_EVENT_FLAPEEBIRD_REVIVED = "FB Revived";
    public static final String ANALYTICS_EVENT_FLAPEEBIRD_JETSTREAM = "FB Jetstream";
    public static final String ANALYTICS_EVENT_SIMPROMO = "Promo Opened";
    public static final String ANALYTICS_EVENT_RATED = "Game Rated";
    public static final String ANALYTICS_EVENT_SHARED = "Shared";
    public static final String ANALYTICS_EVENT_SHARED_FIELD = "Platform";
    public static final String ANALYTICS_EVENT_SHARED_FB = "Facebook";
    public static final String ANALYTICS_EVENT_SHARED_TWITTER = "Twitter";
    public static final String ANALYTICS_EVENT_ACHIEVEMENT = "Achievement";
    public static final String ANALYTICS_EVENT_ACHIEVEMENT_FIELD = "Type";

    public static final String GALLERY_DOWNLOADS_ALBUM = "Downloads";

    public static final String STATE_GALLERY_OPENED_MEDIA_PREFIX = "gallery.opened.";


    public static final String JABBR_IRIS_PROFANITY_MAIL = "mail.irisprofanity.show";


    public static final String CHECKPOINT_NONE = "CHECKPOINT_NONE";

    // Act1
    public static final String CHECKPOINT_GENERIC = "CHECKPOINT_GENERIC";


    // Quest names
    // Act 1
    public static final String QUEST_1 = "QUEST_1";



    // Debug

    public static float tKeyboardAnimationSpeedMultiplier = 1.0f;       // 1.0f
    public static float tChatTimingMultiplier = 0.9f; // 0.9f;
//    public static boolean allowSkipChatVoice = false;
    public static float tChatTypingTimeSkipMultiplier = 10f;

    public static boolean ignoreSaveErrors = false;

    public static boolean autoresolveRestoreScreens = false;
    public static float restoreWinDelay = 3f;


    public static float restoreImageLength = 1f;
    public static int restoreImageFragments = 9;
    public static int restoreImageMaxProfiles = 6;


    // States

    public static String subtitlesConfigFilename = "content/subtitles/config";
    public static String galleryConfigFilename = "content/gallery/config";
    public static String chatsConfigFilename = "content/chats/config";
    public static String mailConfigFilename = "content/mail/config";
    public static String phoneConfigFilename = "content/calls/config";
    public static String jabbrConfigFilename = "content/jabbr/config";
    public static String restoreImageConfigFilename = "content/restimage/config";
    public static String restorePhraseConfigFilename = "content/restphrase/config";
    public static String browserConfigFilename = "content/web/config";

    public static String introChatsConfigFilename = "content/chats/introconfig";

    public static String irisQuestSave = "iris.quests";


    public static boolean loadDialogueTreeSourcesFirst = false;

    public static boolean allowGlobalOnlineSources = false;

    public static boolean allowCallsOnlineSources = false;
    public static boolean allowChatsOnlineSources = false;
    public static boolean allowMailOnlineSources = false;

    public static boolean rebuildAllWebsites = false;
    public static boolean allowWebsiteRefresh = false;

    public static boolean checkAllAssets = false;


    public static void addGoogleSource(String basePath, String filename, String sheetUrl) {
        if(LiveEditor.editor == null || !allowGlobalOnlineSources)
            return;     // Not allowed

        String[] paths = sheetUrl.split("/");
        if(paths.length != 2)
            throw new IllegalArgumentException("Malformed url: " + sheetUrl);

        // Get csv download link
        String url = String.format(Locale.US, "https://docs.google.com/spreadsheets/d/%s/export?format=csv&id=%s&gid=%s",
                paths[0],
                paths[0],
                paths[1]
        );

        // Generate NetRequest
        NetRequest request = new NetRequest();
        request.setUrl(url);
        request.setMethod(Net.HttpMethods.GET);

        // Add to live editor
        LiveEditor.editor.addOnlineSource(basePath, filename, request);
    }

    public static String[] splitCSV(String csv) {
        String[] tags = csv.trim().split("\\s*,\\s*");
        if(tags.length == 1 && tags[0].isEmpty())
            return new String[0];
        return tags;
    }

}
