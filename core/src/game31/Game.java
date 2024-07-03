package game31;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

import game31.renderer.SaraRenderer;
import sengine.File;
import sengine.Sys;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.calc.Range;
import sengine.graphics2d.Font;
import sengine.graphics2d.Fonts;
import sengine.graphics2d.Material;
import sengine.graphics2d.Renderer;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.TextureUtils;
import sengine.graphics2d.texturefile.FIFormat;
import sengine.graphics2d.texturefile.TextureFile;
import sengine.mass.MassFile;
import sengine.materials.ColorAttribute;
import sengine.materials.ColoredMaterial;
import sengine.utils.Console;

/**
 * Created by Azmi on 23/6/2016.
 */
public class Game extends Sys {
    static final String TAG = "Game31";

    public static boolean recompileFileSystems = false;
    public static String configExternalOverridePath = "game31/";

    public static String configConsoleDefaults =
            "import com.badlogic.gdx.*;"
            + "import sengine.*;"
            + "import sengine.graphics2d.*;"
            + "import sengine.animation.*;"
            + "import sengine.calc.*;"
            + "import sengine.utils.*;"
            + "import game31.*;"
            + "import game31.gb.*;"
            + "import game31.glitch.*;"
            + "import game31.model.*;"
            + "import game31.renderer.*;"
            + "import game31.triggers.*;"
            // Explicit imports (for CodeBlob)
            + "import game31.Globals;"
            + "import game31.triggers.Triggers;"
            + "import game31.triggers.ACT1;"
            + "import game31.triggers.ACT2;"
            + "import game31.triggers.ACT3;"
            ;

    public static String configConsoleRestartUniverseCode =
            "Globals.grid = new Grid();" +
            "Matrices.reset();" +
            "Sys.system.activate(Globals.grid);";


    public static final String AUTOEXEC_FILENAME = "autoexec.cfg";

    public static Game game;


    public interface PlatformInterface {

        void reportLog(String source, String text);
        void reportLog(String source, String text, Throwable exception);
        void reportLogDebug(String source, String text);
        void reportLogDebug(String source, String text, Throwable exception);
        void reportLogError(String source, String text);
        void reportLogError(String source, String text, Throwable exception);

        VoiceProfile createTextVoice(String path);

        void linkGameData();
        void prepareSaveGame();
        boolean existsSaveGame();
        void writeSaveGame(MassFile save);
        MassFile readSaveGame();
        void deleteSaveGame();

        // Game Center
        boolean showGameCenter();
        boolean promptGameCenterLogin();
        void openGameCenter();
        void loginGameCenter();
        void unlockAchievement(Globals.Achievement achievement);
        void processCallbacks();

        // Ads
        boolean showRewardedVideoAd();
        boolean showInterstitialAd();
        void openSimulacraAppPage();

        // Analytics
        void analyticsStartLevel(String name);
        void analyticsEndLevel(String name, int score, boolean success);
        void analyticsEvent(String name);
        void analyticsView(String name, String type, String id);
        void analyticsValue(String name, String field, float value);
        void analyticsString(String name, String field, String value);

        void exitGame();

        void openReviewPage();

        void destroyed();

        void setWindowed();

        void removeAds();
        void checkRemovedAds();

        boolean openURI(String URI);
    }

    public static void analyticsStartLevel(String name) {
        Game.game.platform.analyticsStartLevel(name);
    }
    public static void analyticsEndLevel(String name) {
        Game.game.platform.analyticsEndLevel(name, -1, true);
    }
    public static void analyticsEndLevel(String name, int score, boolean success) {
        Game.game.platform.analyticsEndLevel(name, score, success);
    }
    public static void analyticsEvent(String name) {
        Game.game.platform.analyticsEvent(name);
    }
    public static void analyticsView(String name) {
        Game.game.platform.analyticsView(name, name, name);
    }
    public static void analyticsView(String name, String type) {
        Game.game.platform.analyticsView(name, type, type + ": " + name);
    }
    public static void analyticsView(String name, String type, String id) {
        Game.game.platform.analyticsView(name, type, id);
    }
    public static void analyticsValue(String name, String field, float value) {
        Game.game.platform.analyticsValue(name, field, value);
    }
    public static void analyticsString(String name, String field, String value) {
        Game.game.platform.analyticsString(name, field, value);
    }


    public final PlatformInterface platform;


    public Game(PlatformInterface platform) {
        this.platform = platform;
    }

    @Override
    public void log(String source, String text) {
        platform.reportLog(source, text);
    }

    @Override
    public void log(String source, String text, Throwable exception) {
        platform.reportLog(source, text, exception);
    }

    @Override
    public void logDebug(String source, String text) {
        platform.reportLogDebug(source, text);
    }

    @Override
    public void logDebug(String source, String text, Throwable exception) {
        platform.reportLogDebug(source, text, exception);
    }

    @Override
    public void logError(String source, String text) {
        platform.reportLogError(source, text);
    }

    @Override
    public void logError(String source, String text, Throwable exception) {
        platform.reportLogError(source, text, exception);
    }

    @Override
    protected void created() {
        // Link game data files
        platform.linkGameData();

        // Register
        game = this;

        // Set aspect ratio
        float length = (float)(Sys.system.getHeight() - Globals.topSafeAreaInset - Globals.bottomSafeAreaInset) / (float)Sys.system.getWidth();
        if(length < Globals.MIN_LENGTH)
            Globals.LENGTH = Globals.MIN_LENGTH;
        else
            Globals.LENGTH = length;

        // Initialize platform specific

        if(Gdx.app.getType() == Application.ApplicationType.Desktop) {
            // Desktop

            // Configure external override
            File.allowExternalOverride = true;
            File.externalOverridePath = configExternalOverridePath;

            // External override
//            URL codePath = File.class.getProtectionDomain().getCodeSource().getLocation();
//            if (codePath.getFile().endsWith(".jar") || codePath.getFile().endsWith(".exe") || codePath.getFile().endsWith(".dat")) {
//                File.externalOverrideIsAbsolute = true;
//                File.externalOverridePath = (new java.io.File(codePath.getPath()).getParent()) + "/";
//            }

            // Configure main directory
            File.optimizedCacheDir = File.openExternal("compiled");

            // Load main filesystem if exists
            File.unpackFS("main.fs", "videos.fs");      // Videos needed for simulacra trailer

            Fonts.fonts = new Fonts();      // Setup fonts

            if(File.exists(AUTOEXEC_FILENAME)) {
                // Default console
                Console.interpreterDefaults = configConsoleDefaults;
                Font consoleFont = new Font("inconsolata.otf", 32);

                Sprite bgMat = new Sprite(Globals.LENGTH, new ColoredMaterial());
                ColorAttribute.of(bgMat).set(0, 0, 0, 0.75f);

                // Console
                Console.console = new Console(bgMat, consoleFont, Globals.consoleChars, SaraRenderer.TARGET_CONSOLE);
                Console.console.showPreview(true);
                Console.console.evalFile(AUTOEXEC_FILENAME, false);
            }

            // Set fullscreen mode
//            if(!Gdx.graphics.isFullscreen()) {
//                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
//                Gdx.graphics.setVSync(true);
//            }
        }
        else {
            // Else mobile

            // Load main filesystem
            File.unpackFS("main.fs", "videos.fs");      // Videos needed for simulacra trailer

            Fonts.fonts = new Fonts();      // Setup fonts


//            // Default console
//            Console.interpreterDefaults = configConsoleDefaults;
//            Font consoleFont = new Font("inconsolata.otf", 32);
//
//            Sprite bgMat = new Sprite(Grid.LENGTH, new ColoredMaterial());
//            ColorAttribute.of(bgMat).set(0, 0, 0, 0.75f);
//
//            // Console
//            Console.console = new Console(bgMat, consoleFont, 32f, SaraRenderer.TARGET_CONSOLE);
//            Console.console.showPreview(true);
        }

        if(recompileFileSystems)
            File.unpackFS("web.fs", "content.fs", "emojis.fs");     // Force load all now

        // Always preload sounds, android is unreliable in sound loading
        Sound.preloadSounds = true;

        // Set formats
        TextureFile.setFormats(new Class<?>[] {
                //ETC1Format.ETC1ImageData.class,
                FIFormat.FragmentedImageData.class,
        }, new TextureFile.TextureFormat<?>[] {
                //new ETC1Format(),
                new FIFormat(512)		// TODO: needs more testing
        });
        TextureUtils.maxTextureSize = 2048;         // Safe number, cross platform
        // Default material extension
        Material.defaultMaterialType = Material.simpleMaterialType;
        // Create standard renderer
        Renderer.renderer = new SaraRenderer();
        SaraRenderer.renderer.refreshRenderBuffers();

        // Disable default pitch
        Audio.defaultPitchRange = new Range(1f, 0);

        // Done initializing framework
        Sys.debug(TAG, "Framework initialized");

        // Start universe
        Globals.grid = new Grid();

        activate(Globals.grid);

        // Inform platform to prepare savegame
        platform.prepareSaveGame();
    }

    @Override
    protected void destroyed() {
        // Save compiled fs
        if(recompileFileSystems) {
            Sys.debug(TAG, "Recompiling File Systems...");
            File.packFS(
                    "videos.fs", "content/videos",
                    "web.fs", "content/web",
                    "content.fs", "content/",
                    "emojis.fs", "emojis/",
                    "main.fs"
            );
            Sys.debug(TAG, "All File Systems recompiled");
        }

        // Inform platform
        platform.destroyed();

        // Free
        game = null;
    }



}
