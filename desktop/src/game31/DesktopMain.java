package game31;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.badlogic.gdx.backends.lwjgl.audio.OggInputStream;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamLeaderboardEntriesHandle;
import com.codedisaster.steamworks.SteamLeaderboardHandle;
import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamRemoteStorage;
import com.codedisaster.steamworks.SteamRemoteStorageCallback;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUGCHandle;
import com.codedisaster.steamworks.SteamUserStats;
import com.codedisaster.steamworks.SteamUserStatsCallback;
import com.kaigan.game27.desktop.CrashHandler;

import org.oxbow.swingbits.dialog.task.TaskDialog;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Paths;

import javax.swing.JFrame;

import sengine.File;
import sengine.Sys;
import sengine.graphics2d.Fonts;
import sengine.mass.MassFile;
import sengine.mass.io.Input;
import sengine.mass.io.Output;
import sengine.utils.LoadingMenu;

/**
 * Created by Azmi on 9/12/2017.
 */

public class DesktopMain implements Game.PlatformInterface, SteamRemoteStorageCallback, SteamUserStatsCallback {
    private static final String TAG = "DesktopMain";

    public static DesktopMain main;

    public static int hdpiHeightThreshold = 1000;
    public static int textVoiceSampleRate = 30;			// 30 samples per second

    private final SteamRemoteStorage remoteStorage;
    private final SteamUserStats userStats;
    private boolean userStatsRetrieved = false;

    private final LwjglApplication application;

    private static final boolean USE_LOCAL_SAVES = false;
    private static final boolean START_IMMEDIATELY = false;

    private static final int STEAM_APP_ID = 878320;

    public DesktopMain(String ... args) {
        // Keep reference
        main = this;

        // Set crash handler version
        CrashHandler.version = "Pipe Dreams v" + Globals.version;

//        // Autolocking
//        if(System.currentTimeMillis() > 1550275199000L) {
//            // Friday, February 15, 2019 11:59:59 PM
//            remoteStorage = null;
//            userStats = null;
//            application = null;
//
//            // Show dialog
//            // Hack to remove the ugly ass java title icon.. ugh...
//            JFrame frame = new JFrame();
//            frame.setIconImage(null);
//            frame.setAlwaysOnTop(true);
//
//            // Exception Dialog
//            TaskDialog dlg = new TaskDialog(frame, "SIMULACRA: Pipe Dreams");
//
//            dlg.setInstruction("Update Required");
//            dlg.setText("Unable to check files, please allow Steam to update. You may also try the \"Verify Integrity of Game Files\" option in the Steam game properties." );
//
//            dlg.setCommands(
//                    TaskDialog.StandardCommand.OK.derive("Okay")
//            );
//
//            // Dialog
//            dlg.show();
//
//            System.exit(-1);            // Exit
//
//            // Done
//            return;
//        }


        String workingPath = Paths.get(".").toAbsolutePath().normalize().toString();

        // Initialize steam
        SteamRemoteStorage initRemoteStorage;
        SteamUserStats initUserStats;
        try {
            // Initialize steam
            if(!SteamAPI.init(workingPath)) {
                SteamAPI.printDebugInfo(System.out);
                throw new RuntimeException("init() failed");
            }

            // Remote storage
            initRemoteStorage = new SteamRemoteStorage(this);

            // User stats & achievement
            initUserStats = new SteamUserStats(this);
            initUserStats.requestCurrentStats();

        } catch (Throwable e) {
            // Failed to initialize steam, try to restart if necessary
            try {
                if(!SteamAPI.restartAppIfNecessary(STEAM_APP_ID))
                    throw new RuntimeException("Steam refused to restart", e);      // Could also be due to steam_appid.txt
                // Else restarting through steam,
                remoteStorage = null;
                userStats = null;
                application = null;
                System.exit(-1);            // Exit, steam is restarting for us
                return;
            } catch (Throwable restartError) {
                // Give up, initialization failed, and failed to restart through steam
                throw new RuntimeException("Unable to initialize steam and failed to restart", restartError);
            }
        }
        // Steam initialized
        remoteStorage = initRemoteStorage;
        userStats = initUserStats;

        // Desktop settings
        final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        // Audio
        config.audioDeviceBufferCount= 10;
        config.audioDeviceBufferSize = 4096;
        config.audioDeviceSimultaneousSources = 16;

        // Check display mode
        Graphics.DisplayMode display = LwjglApplicationConfiguration.getDesktopDisplayMode();
        if(display.height >= hdpiHeightThreshold)
            Fonts.resolutionMultiplier = 2;         // For 4K
        config.foregroundFPS = 0;
        config.useHDPI = true;
        config.vSyncEnabled = true;

        // Config window
        config.title = "SIMULACRA: Pipe Dreams";
        config.addIcon("launcher128.png", Files.FileType.Internal);
        config.addIcon("launcher64.png", Files.FileType.Internal);
        config.addIcon("launcher32.png", Files.FileType.Internal);
        config.addIcon("launcher16.png", Files.FileType.Internal);

        config.allowSoftwareMode = true;            // Dont know what good this will do
        config.width = display.width;
        config.height = display.height;

        if(!Globals.isWindowed) {
            config.fullscreen = true;
        }
        else {
            config.fullscreen = false;
            config.resizable = true;
        }

        // Platform providers
        VideoMaterialProvider.init();

        // For Desktop
        Globals.g_flapeeShopPurchasedThreshold = 500;

        // Create game
        Game game = new Game(this);

        // Create application
        application = new LwjglApplication(game.applicationListener, config);
    }

    @Override
    public void setWindowed() {
        LwjglGraphics graphics = (LwjglGraphics) Gdx.graphics;
        graphics.setWindowedMode(graphics.getWidth(), graphics.getHeight());
    }

    @Override
    public void removeAds() {
        // nothing
    }

    @Override
    public void checkRemovedAds() {
        // nothing
    }

    @Override
    public void reportLog(String source, String text) {
        Gdx.app.log(source, text);
    }

    @Override
    public void reportLog(String source, String text, Throwable exception) {
        Gdx.app.log(source, text, exception);
    }

    @Override
    public void reportLogDebug(String source, String text) {
        Gdx.app.debug(source, text);
    }

    @Override
    public void reportLogDebug(String source, String text, Throwable exception) {
        Gdx.app.debug(source, text, exception);
    }

    @Override
    public void reportLogError(String source, String text) {
        Gdx.app.error(source, text);
    }

    @Override
    public void reportLogError(String source, String text, Throwable exception) {
        Gdx.app.error(source, text, exception);
    }

    @Override
    public VoiceProfile createTextVoice(String path) {
        // Load file
        OggInputStream input = new OggInputStream(File.open(path).read(1024));
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] readBuffer = new byte[2048];
        while (!input.atEnd()) {
            int length = input.read(readBuffer);
            if (length == -1) break;
            output.write(readBuffer, 0, length);
        }
        // Close
        StreamUtils.closeQuietly(input);

        byte[] pcm = output.toByteArray();
        int channels = input.getChannels();
        int sampleRate = input.getSampleRate();

        int bytes = pcm.length - (pcm.length % (channels > 1 ? 4 : 2));		// remove excess bytes.. there shouldnt be any
        int audioSamples = bytes / (2 * channels);		// actual samples

        float duration = audioSamples / (float)sampleRate;

        // Conver to int16
        ByteBuffer buffer = ByteBuffer.wrap(pcm, 0, bytes);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(pcm, 0, bytes);
        buffer.flip();
        ShortBuffer shorts = buffer.asShortBuffer();

        int sampleDistance = (sampleRate / textVoiceSampleRate) * channels;
        int totalSamples = audioSamples / sampleDistance;
        byte[] samples = new byte[totalSamples];

        int audioIndex = 0;
        int sampleIndex = 0;
        for(; audioIndex < audioSamples && sampleIndex < totalSamples; sampleIndex++) {
            int sampled = 0;
            int highest = 0;
            for(; sampled < sampleDistance && audioIndex < audioSamples; sampled++, audioIndex++) {
                for(int ch = 0; ch < channels; ch++) {
                    int s = shorts.get();
                    if (s < 0)
                        s = -s;
                    if (s > highest)
                        highest = s;
                }
            }
            highest /= (257 / 2);
            if(highest > 255)
                highest = 255;
            samples[sampleIndex] = (byte) highest;          // pack 1 unsigned byte as a signed byte
        }

        return new VoiceProfile(path, samples, duration);
    }

    @Override
    public void linkGameData() {
        // nothing to link
    }


    @Override
    public void prepareSaveGame() {
        if(START_IMMEDIATELY) {
            Globals.grid.mainMenu.detach();
            Globals.grid.loadingMenu = new LoadingMenu(null, null, 0, 0, 0, 0, false, false);
            Globals.grid.load();
            Globals.grid.start();
            return;
        }
        Globals.grid.mainMenu.doneLogin(false, existsSaveGame());          // Saves are synced by Steam before starting
    }

    // Save game
    @Override
    public void writeSaveGame(MassFile save) {
        if(USE_LOCAL_SAVES) {
            FileHandle fileHandle = Gdx.files.local(Globals.SAVE_FILENAME);
            save.save(fileHandle);
            return;
        }
        Sys.info(TAG, "Writing steam save file: " + Globals.SAVE_FILENAME);

        // Write savegame to buffer
        Output output = new Output(256 * 1024);     // start with 256kb buffer
        save.save(output);
        // Convert to direct byte buffer
        int size = output.position();
        byte[] bytes = output.getBuffer();
        ByteBuffer bb = ByteBuffer.allocateDirect(size);
        bb.put(bytes, 0, size);
        bb.position(0);


        // Write to steam storage
        try {
            remoteStorage.fileWrite(Globals.SAVE_FILENAME, bb);
        } catch (SteamException e) {
            throw new RuntimeException("Failed to write steam save file: " + Globals.SAVE_FILENAME, e);
        }
    }

    @Override
    public boolean existsSaveGame() {
        if(USE_LOCAL_SAVES)
            return Gdx.files.local(Globals.SAVE_FILENAME).exists();
        return remoteStorage.fileExists(Globals.SAVE_FILENAME);
    }

    @Override
    public MassFile readSaveGame() {
        if(USE_LOCAL_SAVES) {
            FileHandle fileHandle = Gdx.files.local(Globals.SAVE_FILENAME);
            MassFile saveFile = new MassFile();
            saveFile.load(fileHandle);
            return saveFile;
        }
        // Read from steam storage
        try {
            Sys.info(TAG, "Loading steam save file: " + Globals.SAVE_FILENAME);

            // Prepare buffer
            int fileSize = remoteStorage.getFileSize(Globals.SAVE_FILENAME);
            ByteBuffer bb = ByteBuffer.allocateDirect(fileSize);
            // Read
            remoteStorage.fileRead(Globals.SAVE_FILENAME, bb);
            bb.position(0);
            byte[] bytes = new byte[fileSize];
            bb.get(bytes, 0, fileSize);
            Input input = new Input(bytes);
            // Prepare MassFile
            MassFile saveFile = new MassFile();
            saveFile.load(input);

            return saveFile;
        } catch (SteamException e) {
            throw new RuntimeException("Failed to read steam save file: " + Globals.SAVE_FILENAME, e);
        }
    }

    @Override
    public void deleteSaveGame() {
        if(USE_LOCAL_SAVES) {
            Gdx.files.local(Globals.SAVE_FILENAME).delete();
            return;
        }
        if(remoteStorage.fileExists(Globals.SAVE_FILENAME))
            remoteStorage.fileDelete(Globals.SAVE_FILENAME);
    }

    @Override
    public boolean showGameCenter() {
        return false;
    }

    @Override
    public boolean promptGameCenterLogin() {
        return false;
    }

    @Override
    public void openGameCenter() {
        // not used
    }

    @Override
    public void loginGameCenter() {
        // not used
    }

    @Override
    public void unlockAchievement(Globals.Achievement achievement) {
        Sys.info(TAG, "Unlocking Steam achievement: " + achievement.name());
        if(userStatsRetrieved) {
            try {
                userStats.setAchievement(achievement.name());           // achievement names correspond exactly to steam achievement ids
                userStats.storeStats();
            } catch (Throwable e) {
                Sys.error(TAG, "Unable to unlock achievement: " + achievement.name(), e);
            }
        }
        else
            Sys.error(TAG, "Unable to unlock achievement as user stats not yet retrieved: " + achievement.name());
    }

    @Override
    public void processCallbacks() {
        if(SteamAPI.isSteamRunning())
            SteamAPI.runCallbacks();
    }

    @Override
    public boolean showRewardedVideoAd() {
        return false;       // no ads
    }

    @Override
    public boolean showInterstitialAd() {
        return false;       // no ads
    }

    @Override
    public void openSimulacraAppPage() {
        Gdx.net.openURI("https://store.steampowered.com/app/712730/SIMULACRA/");
    }

    @Override
    public void analyticsStartLevel(String name) {
//        Sys.info(TAG, "analyticsStartLevel: " + name);
    }

    @Override
    public void analyticsEndLevel(String name, int score, boolean success) {
//        Sys.info(TAG, "analyticsEndLevel: " + name + " " + score + " " + success);
    }

    @Override
    public void analyticsEvent(String name) {
//        Sys.info(TAG, "analyticsEvent: " + name);
    }

    @Override
    public void analyticsView(String name, String type, String id) {
//        Sys.info(TAG, "analyticsView: " + name + " " + type + " " + id);
    }

    @Override
    public void analyticsValue(String name, String field, float value) {
//        Sys.info(TAG, "analyticsValue: " + name + " " + field + " " + value);
    }

    @Override
    public void analyticsString(String name, String field, String value) {
//        Sys.info(TAG, "analyticsString: " + name + " " + field + " " + value);
    }

    @Override
    public void exitGame() {
        Gdx.app.exit();
    }

    @Override
    public void openReviewPage() {
        Gdx.net.openURI("https://store.steampowered.com/recommended/recommendgame/878320");
    }

    // Ending game, release steam stuff
    @Override
    public void destroyed() {
        remoteStorage.dispose();
        SteamAPI.shutdown();

    }

    // Steam remote storage
    @Override
    public void onFileShareResult(SteamUGCHandle fileHandle, String fileName, SteamResult result) {
        // not used
    }

    @Override
    public void onDownloadUGCResult(SteamUGCHandle fileHandle, SteamResult result) {
        // not used
    }

    @Override
    public void onPublishFileResult(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) {
        // not used
    }

    @Override
    public void onUpdatePublishedFileResult(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) {
        // not used
    }

    @Override
    public void onPublishedFileSubscribed(SteamPublishedFileID publishedFileID, int appID) {
        // not used
    }

    @Override
    public void onPublishedFileUnsubscribed(SteamPublishedFileID publishedFileID, int appID) {
        // not used
    }

    @Override
    public void onPublishedFileDeleted(SteamPublishedFileID publishedFileID, int appID) {
        // not used
    }

    @Override
    public void onFileWriteAsyncComplete(SteamResult result) {
        // not used
    }

    @Override
    public void onFileReadAsyncComplete(SteamAPICall fileReadAsync, SteamResult result, int offset, int read) {
        // not used
    }

    // SteamUserStats callbacks
    @Override
    public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) {
        // Check if user stats have been successfully retrieved
        if(result == SteamResult.OK) {
            // Success
            userStatsRetrieved = true;
            Sys.info(TAG, "Steam user stats retrieved");
        }
        else
            Sys.error(TAG, "Unable to retrieve steam user stats: " + result);
    }

    @Override
    public void onUserStatsStored(long gameId, SteamResult result) {
        // not used
    }

    @Override
    public void onUserStatsUnloaded(SteamID steamIDUser) {
        // not used
    }

    @Override
    public void onUserAchievementStored(long gameId, boolean isGroupAchievement, String achievementName, int curProgress, int maxProgress) {
        // not used
    }

    @Override
    public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) {
        // not used
    }

    @Override
    public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard, SteamLeaderboardEntriesHandle entries, int numEntries) {
        // not used
    }

    @Override
    public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard, int score, boolean scoreChanged, int globalRankNew, int globalRankPrevious) {
        // not used
    }

    @Override
    public void onGlobalStatsReceived(long gameId, SteamResult result) {
        // not used
    }
}
