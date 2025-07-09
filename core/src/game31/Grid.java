package game31;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.TimeZone;

import bsh.Interpreter;
import game31.app.DownloadsApp;
import game31.app.GatewayAdScreen;
import game31.app.browser.BrowserScreen;
import game31.app.chats.WhatsupApp;
import game31.app.flapee.FlapeeBirdScreen;
import game31.app.flapee.FlapeeKickNotifyScreen;
import game31.app.friends.FriendsApp;
import game31.app.gallery.PhotoRollApp;
import game31.app.homescreen.Homescreen;
import game31.app.mail.MailApp;
import game31.app.phone.PhoneApp;
import game31.app.restore.RestoreImageScreen;
import game31.app.restore.RestorePhraseScreen;
import game31.gb.GBPrecacher;
import game31.renderer.SaraRenderer;
import game31.triggers.ACT1;
import game31.triggers.ACT2;
import game31.triggers.ACT3;
import game31.triggers.ACT4;
import sengine.File;
import sengine.Sys;
import sengine.Universe;
import sengine.audio.Audio;
import sengine.graphics2d.Fonts;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Sprite;
import sengine.mass.MassFile;
import sengine.materials.ColorAttribute;
import sengine.ui.UIElement;
import sengine.utils.Builder;
import sengine.utils.LoadingMenu;
import sengine.utils.StreamablePrecacher;
import sengine.utils.Universe2D;

/**
 * Created by Azmi on 23/6/2016.
 */
public class Grid extends Universe2D implements LoadingMenu.Handler {
    public static final String TAG = "Grid";

    public class ScreenCompositor extends Group {

        public final OrthographicCamera camera;

        public float inputOffsetX;
        public float inputOffsetY;
        public float inputScale;

        private ScreenCompositor() {
            super(true, true, true);

            camera = new OrthographicCamera(1f, Globals.LENGTH);
            // Camera will be positioned at bottom center
            // So for modelview matrix, objects can be placed from x: 0.0f ~ 1.0f, and y: 0.0f (bottom) to length paramter (top)
            camera.position.set(0.5f, Globals.LENGTH / 2.0f, 0.0f);
            camera.update();
        }


        @Override
        protected void render(Universe v, float r, float renderTime) {
            super.render(v, r, renderTime);

            Matrices.camera = camera;

            // Update viewport metrics
            UIElement.Viewport viewport = null;
            while ((viewport = iterate(viewport, UIElement.Viewport.class, false, null)) != null) {
                // Create metrics for viewport if not yet
                if(viewport.metrics == null)
                    viewport.metrics = new UIElement.Metrics();

                // Reset metrics
                viewport.metrics.clear();
                if(!screen.isInFullscreen())
                    viewport.metrics.offsetInput(inputOffsetX, inputOffsetY).scaleInput(inputScale, inputScale);
                else
                    viewport.metrics.offsetInput(0, inputOffsetY).scaleInput(0, inputScale);      // Redirect all input to center (input does not work on rotated screens)
            }
        }

    }

    public static interface Trigger {
        boolean trigger(String name);
    }
    public static class EvalTrigger implements Trigger {
        static final String TAG = "EvalTrigger";

        public final String eval;

        public EvalTrigger(String eval) {
            this.eval = eval;
        }

        @Override
        public boolean trigger(String name) {
            try {
                return (Boolean) Globals.grid.interpreter.eval(eval);
            } catch (Throwable e) {
                return false;
            }
        }
    }


    public static class ScheduledRunnable {
        public final Runnable runnable;
        public final float tDelay;

        private float tTriggerScheduled = -1;

        public ScheduledRunnable(Runnable runnable, float tDelay) {
            this.runnable = runnable;
            this.tDelay = tDelay;
        }
    }

    public static final float TIMESTEP = 1f / 25f;      // 25fps

    // Config
    public boolean skipIntoMainMenu;

    // Scene graph
    public ScreenCompositor compositor;
    public Group underlayGroup;
    public Group screensGroupContainer;
    public Group screensGroup;
    public Group overlayGroup;

    public PhotoRollApp photoRollApp;

    public WhatsupApp whatsupApp;

    public MailApp mailApp;

    public PhoneApp phoneApp;

    public FriendsApp friendsApp;

    public RestoreImageScreen restoreImageApp;
    public RestorePhraseScreen restorePhraseApp;

    public DownloadsApp downloadsApp;

    public BrowserScreen browserApp;

    public FlapeeBirdScreen flapeeBirdApp;

    public GatewayAdScreen gatewayAdScreen;

    public Homescreen homescreen;

    // Services
    public Screen screen;

    public Keyboard keyboard;

    public Notification notification;


    // Special use cases
    public InstallDialog installDialog;
    public NotEnoughSpaceDialog notEnoughSpaceDialog;

    public FlapeeKickNotifyScreen flapeeKickNotifyScreen;

    public IdleScareScheduler idleScare;

    public StatMenu statMenu;

    // Loading
    public LoadingMenu loadingMenu;

    // Main menu
    public MainMenu mainMenu;
    public CreditsMenu creditsMenu;
    public SimTrailerMenu simTrailerMenu;

    // States
    public ScriptState state = new ScriptState();
    private String queuedSavePoint = null;

    public Interpreter interpreter;

    // Time
    private long timeStarted = 0;
    private long timeOffset = Globals.gameTimeOffset;
    private long timeElapsed = 0;
    public TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");

    public final StreamablePrecacher precacher;


    // Battery
    float batteryLevel = Globals.batteryStartLevel;
    private float minBatteryLevel = Globals.batteryMinLevel;
    private float batteryDrain = Globals.batteryDrainRate;

    public Sprite wallpaperSprite;


    public String format(String string) {
        if(string == null)
            return null;
        String s = string;
        int c;
        while((c = s.indexOf(Globals.STATE_PREFIX)) != -1) {
            // Extract var name
            int start = c + Globals.STATE_PREFIX.length();
            int end = s.indexOf(Globals.STATE_SUFFIX, start);
            String name = s.substring(start, end);
            // Resolve var
            String var = state.get(name, null);
            if(var == null)
                Sys.error(TAG, "Unable to state \"" + name + "\" to format \"" + string + "\"");
            // Rebuild string
            s = s.substring(0, c) + var + s.substring(end + 1);
        }
        return s;
    }

    public void setBattery(float level, float minLevel, float drain) {
        this.batteryLevel = level;
        this.minBatteryLevel = minLevel;
        this.batteryDrain = drain;
    }

    public long getSystemTime() {
        return (System.currentTimeMillis() - timeStarted) + timeOffset + timeElapsed;
    }

    public void setSystemTime(long targetTime) {
        timeElapsed += targetTime - getSystemTime();
    }


    public final ObjectMap<String, Trigger> triggers = new ObjectMap<String, Trigger>();
    public final Array<ScheduledRunnable> scheduledRunnables = new Array<ScheduledRunnable>(ScheduledRunnable.class);

    public boolean isStateUnlocked(String name) {
        return state.get(name, false);
    }

    public boolean unlockState(String name) {
        if(state.get(name, false))
            return false;       // already unlocked
        state.set(name, true);
        return true;        // unlocked
    }

    public boolean resetState(String name) {
        boolean previous = state.get(name, false);
        state.set(name, false);
        return previous;
    }

    public boolean trigger(String name) {
        Trigger trigger = triggers.get(name);
        if(trigger == null)
            return true;        // no specific trigger, default behaviour
        // Else outcome depends on trigger
        return trigger.trigger(name);
    }

    public void addTrigger(String name, Trigger trigger) {
        triggers.put(name, trigger);
    }

    public void removeTrigger(String name) {
        triggers.remove(name);
    }

    public void scheduleRunnable(Runnable runnable, float tDelay) {
        ScheduledRunnable scheduledRunnable = new ScheduledRunnable(runnable, tDelay);
        scheduledRunnable.tTriggerScheduled = getRenderTime() + tDelay;
        scheduledRunnables.add(scheduledRunnable);
    }

    public Object eval(String tag, String code) {
        try {
            return interpreter.eval(code);
        } catch (Throwable e) {
            Sys.error(tag, "Unable to eval code:\n" + code, e);
        }
        return null;
    }

    public void stopProcesses() {
        whatsupApp.renderingEnabled = false;
    }

    public void resumeProcesses() {
        whatsupApp.renderingEnabled = true;
    }

    public void stopAmbiance() {
        // Stop all background audio sources
        Audio.pauseMusic();
    }

    public void resumeAmbiance() {
        // Resume all audio sources
        Audio.resumeMusic();
    }

    public Grid() {
        this(false);
    }

    public Grid(boolean skipIntoMainMenu) {
        super(TIMESTEP, -1);

        this.skipIntoMainMenu = skipIntoMainMenu;

        // Register
        Globals.grid = this;

        precacher = new StreamablePrecacher();
        precacher.attach(this);

        // Time
        timeStarted = System.currentTimeMillis();

        // Prepare scene graph
        prepareSceneGraph();

        // Show loading menu
//        loadingMenu = GBLoadingMenu.createLoadingMenu();
//        loadingMenu.setHandler(this);
//        loadingMenu.attach(compositor);

        // Main menu
        mainMenu = new MainMenu();
        creditsMenu = new CreditsMenu();
        simTrailerMenu = new SimTrailerMenu();

        mainMenu.attach(compositor);
    }

    public void startLoad(LoadingMenu menu) {
        // First, during loading, enable multithreaded fonts
        Fonts.multithreading(true);

        loadingMenu = menu;
        loadingMenu.setHandler(this);
        loadingMenu.attach(compositor);
    }

    private void prepareSceneGraph() {
        // Scene graph
        compositor = new ScreenCompositor();
        compositor.attach(this);

        // Scene graph
        underlayGroup = new Group(true, true, true);
        screensGroupContainer = new Group(true, true, true);
        screensGroup = new Group(true, true, true);
        overlayGroup = new Group(true, true, true);

        underlayGroup.attach(compositor);
        screensGroupContainer.attach(compositor);
        screensGroup.attach(screensGroupContainer);
        overlayGroup.attach(compositor);

        screen = new Screen();
        screen.attach(this);
    }



    public void writeSaveGame(String checkpoint) {
        // Pack all states
        queuedSavePoint = checkpoint;
        if(!whatsupApp.pack(state))
            return;
        if(!mailApp.pack(state))
            return;
        if(!phoneApp.pack(state))
            return;
        if(!flapeeBirdApp.pack(state))
            return;

        friendsApp.pack(state);

        browserApp.pack(state);

        notification.pack(state);

        idleScare.pack(state);

        // State
        state.set(Globals.SAVE_CHECKPOINT, checkpoint);

        // Save time
        long currentTimeMillis = System.currentTimeMillis();
        timeElapsed += currentTimeMillis - timeStarted;
        timeStarted = currentTimeMillis;
        state.set(Globals.SAVE_TIME_ELAPSED, timeElapsed);

        // Save battery
        state.set(Globals.SAVE_BATTERY_LEVEL, batteryLevel);

        // Success
        queuedSavePoint = null;

        // Convert to mass
        MassFile saveFile = new MassFile();
        state.save(saveFile);

        // Save file
        Game.game.platform.writeSaveGame(saveFile);

        // Indicator
        notification.showSavedIndicator();
    }

    private void loadSaveGame() {
        state.clear();

        // Load save game
        if(Game.game.platform.existsSaveGame()) {
            try {
                MassFile saveFile = Game.game.platform.readSaveGame();
                state.load(saveFile);
            } catch (Throwable e) {
                Sys.error(TAG, "Unable to load save", e);
                state.clear();
            }
        }

        // Time
        timeElapsed = state.get(Globals.SAVE_TIME_ELAPSED, 0L);
        timeStarted = System.currentTimeMillis();


        // Battery
        batteryLevel = state.get(Globals.SAVE_BATTERY_LEVEL, Globals.batteryStartLevel);
    }

    public void restoreSaveGame() {

        // Load savegame
        String checkpoint = state.get(Globals.SAVE_CHECKPOINT, null);

        if(checkpoint == null) {
            checkpoint = Globals.CHECKPOINT_NONE;
            Sys.info(TAG, "Starting new game checkpoint");
        }
        else
            Sys.info(TAG, "Restoring checkpoint \"" + checkpoint +"\"");

        // Load specific checkpoint
        if(checkpoint.equals(Globals.CHECKPOINT_GENERIC))
            ACT1.checkpointGeneric();
        else        // New game
            ACT1.checkpointNewGame();

    }


    private void prepareContent() {
        // Interpreter and state
        interpreter = File.interpreter();
        try {
            interpreter.set("v", this);
            interpreter.set("state", state);
            // default imports
            interpreter.eval(Game.configConsoleDefaults);
        }
        catch (Throwable e) {
            Sys.error(TAG, "Unable to initialize state interpreter", e);
        }

        // Shared
        wallpaperSprite = new Sprite(Globals.LENGTH, SaraRenderer.renderer.screenNoiseMaterial);
        ColorAttribute.of(wallpaperSprite).set(0x333333ff);

        // Load save game
        loadSaveGame();

        // Services
        keyboard = new Keyboard();
        notification = new Notification();

        // Homescreen
        homescreen = new Homescreen();

        // Apps
        photoRollApp = new PhotoRollApp();
        whatsupApp = new WhatsupApp();
        mailApp = new MailApp();
        phoneApp = new PhoneApp();
        friendsApp = new FriendsApp();
        browserApp = new BrowserScreen();
        flapeeBirdApp = new FlapeeBirdScreen();

        downloadsApp = new DownloadsApp();

        gatewayAdScreen = new GatewayAdScreen();

        restoreImageApp = new RestoreImageScreen();
        restorePhraseApp = new RestorePhraseScreen();

        // Special
        idleScare = new IdleScareScheduler();
        installDialog = new InstallDialog();
        notEnoughSpaceDialog = new NotEnoughSpaceDialog();

        flapeeKickNotifyScreen = new FlapeeKickNotifyScreen();

        statMenu = new StatMenu();

        // Triggers
        ACT1.load();
        ACT2.load();
        ACT3.load();
        ACT4.load();

    }

    @Override
    public void load() {
        Sys.system.minTimeInterval = Globals.loadingFrameRate;

        File.unpackFS("web.fs", "content.fs", "emojis.fs");

        // Configure precacher
        Builder<Object> builder = new Builder<Object>(GBPrecacher.class, precacher);
        builder.build();

        // Prepare game content
        prepareContent();

        Sys.system.minTimeInterval = 0;
    }

    @Override
    public void complete(final LoadingMenu menu) {
        // Finished multithreading
        Fonts.multithreading(false);

        // Inform main menu finished loaded
        mainMenu.doneLoading();
    }


    public void start() {

        // Timers
        if(Gdx.app.getType() != Application.ApplicationType.Desktop) {
            Sys.system.idleMinTimeInterval = Globals.idleMinTimeInterval;
            Sys.system.inputMaxFramerateTime = Globals.inputMaxFramerateTime;
            Sys.system.renderChangeMaxFramerateTime = Globals.renderChangeMaxFramerateTime;
        }

        // Services
//        keyboard.attach(overlayGroup);
        notification.attach(overlayGroup);

        // Apps
        photoRollApp.attach(this);
        whatsupApp.attach(this);
        mailApp.attach(this);
        phoneApp.attach(this);
        friendsApp.attach(this);

        // Special
        idleScare.attach(this);


        // Save state
        restoreSaveGame();

    }

    @Override
    public void exception(LoadingMenu menu, Throwable e) {
        throw new RuntimeException("Initial loading failed", e);
    }


    @Override
    protected void render(Universe v, float r, float renderTime) {
        super.render(v, r, renderTime);

        // Keep trying to save if queued
        if(queuedSavePoint != null)
            writeSaveGame(queuedSavePoint);

        // Battery
        batteryLevel -= batteryDrain * getRenderDeltaTime();
        if(batteryLevel < minBatteryLevel)
            batteryLevel = minBatteryLevel;

        // Runnables
        for(int c = 0; c < scheduledRunnables.size; c++) {
            ScheduledRunnable scheduledRunnable = scheduledRunnables.items[c];
            if(renderTime > scheduledRunnable.tTriggerScheduled) {
                // Execute
                scheduledRunnable.runnable.run();
                // Remove
                scheduledRunnables.removeIndex(c);
                c--;
            }
        }

        // Assets caching
        if(wallpaperSprite != null) {
            wallpaperSprite.load();
        }
    }

    @Override
    protected void process(Universe v, float renderTime) {
        super.process(v, renderTime);

        Game.game.platform.processCallbacks();
    }

    @Override
    protected float resize(int width, int height) {
        float length = super.resize(width, height);

        // Calculate input metrics
        float insetHeight = Globals.topSafeAreaInset + Globals.bottomSafeAreaInset;
        float viewportHeight = height - insetHeight;            // Actual drawable height minus insets
        float viewportLength = viewportHeight / width;

        float inputWidth;
        float paddingY = 0;
        if(viewportLength < Globals.LENGTH)
            inputWidth = viewportLength / Globals.LENGTH;
        else {
            inputWidth = 1f;
            paddingY = (viewportLength - Globals.LENGTH) / 2f;
            viewportLength = Globals.LENGTH;
        }

        compositor.inputScale = 1f / inputWidth;
        compositor.inputOffsetX = -0.5f + (inputWidth / 2f);
        compositor.inputOffsetY = (-Globals.LENGTH / 2f) + (viewportLength / 2f) - ((length - viewportLength) / 2f) + ((float) Globals.topSafeAreaInset / width) + paddingY;

        // Inform renderer to resize
        SaraRenderer.renderer.refreshRenderBuffers();

        return length;
    }

    @Override
    protected void pause() {
        super.pause();

        // Freeze time
        timeElapsed += System.currentTimeMillis() - timeStarted;
    }

    @Override
    protected void resume() {
        super.resume();

        // Reset timer
        timeStarted = System.currentTimeMillis();
    }
}
