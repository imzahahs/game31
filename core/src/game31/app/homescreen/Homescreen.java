package game31.app.homescreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.homescreen.GBHomescreen;
import game31.renderer.SaraRenderer;
import sengine.Entity;
import sengine.Sys;
import sengine.animation.ScaleAnim;
import sengine.audio.Audio;
import sengine.graphics2d.Sprite;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.OnPressed;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 27/6/2016.
 */
public class Homescreen extends Menu<Grid> implements OnClick<Grid>, OnPressed<Grid> {
    private static final String TAG = "Homescreen";

    public interface App {
        Entity<?> open();
        void refreshNotification(Homescreen homescreen);
    }

    public static class Internal {
        public UIElement window;
        public StaticSprite bg;

        public ScrollableSurface surface;

        public Clickable appButton;
        public TextBox appTitleView;
        public UIElement.Metrics[][] appMetrics;

        public UIElement dockView;
        public Clickable dockAppButton;
        public StaticSprite dockAppButtonReflection;
        public TextBox dockAppTitleView;
        public UIElement.Metrics[] dockAppMetrics;

        public StaticSprite notificationView;
        public TextBox notificationTextView;
        public StaticSprite notificationLockedView;
        public StaticSprite notificationErrorView;

        public Audio.Sound openSound;
        public Audio.Sound closeSound;

        public float tQueuedAppTimeout;
    }

    // Source
    private final Builder<Object> interfaceSource;
    private Internal s;

    // Current
    private final Array<Clickable> buttons = new Array<Clickable>(Clickable.class);
    private final Array<App> apps = new Array<App>(App.class);
    private final Array<String> contexts = new Array<String>(String.class);

    public final Array<String> notificationApps = new Array<String>(String.class);
    public final IntArray notifications = new IntArray();
    public final Array<String> lastOpenedList = new Array<String>(String.class);

    private Entity<?> queuedScareGlitch = null;
    private Sprite queuedScareWallpaper = null;
    private float queuedScareOverlayProbability = 0;

    private Clickable lastOpenedAppButton = null;
    public String lastOpenedApp = null;

    private String queuedApp = null;
    private float tQueuedAppTimeout = -1;

    private boolean hasOpened = false;

    private float tSecretQueued = Float.MAX_VALUE;
    private Runnable secret = null;

    public void queueSecret(Runnable runnable, float tDelay) {
        tSecretQueued = getRenderTime() + tDelay;
        secret = runnable;
    }

    public void clearSecret() {
        tSecretQueued = Float.MAX_VALUE;
        secret = null;
    }

    public void queueScare(Entity<?> glitch, Sprite wallpaper, float overlayProbability) {
        queuedScareGlitch = glitch;
        queuedScareWallpaper = wallpaper;
        queuedScareOverlayProbability = overlayProbability;
        if(isAttached()) {
            // If homescreen is already active, start
            queuedScareGlitch.attach(Globals.grid);            // Attach directly to grid
            queuedScareWallpaper.ensureLoaded();               // Force load
        }
    }

    public void switchApp(String contextName) {
        if(!inputEnabled)       //  || (lastOpenedApp != null && lastOpenedApp.equals(contextName))
            return;     // is transitioning
        queuedApp = contextName;
        tQueuedAppTimeout = Sys.getTime() + s.tQueuedAppTimeout;
        if(!isAttached()) {
            // Find an a ScreenBar to return to home, so that can start queued app
            ScreenBar bars = Globals.grid.screensGroup.iterate(null, ScreenBar.class, false, null);
            if(bars != null)
                bars.simulatePressHome();
            else
                Sys.error(TAG, "No ScreenBar available");
        }
    }

    public void queueAppOnShow(String name) {
        queueAppOnShow(name, s.tQueuedAppTimeout);
    }

    public void queueAppOnShow(String name, float tTimeout) {
        queuedApp = name;
        tQueuedAppTimeout = Sys.getTime() + tTimeout;
    }

    public void cancelQueuedAppOnShow() {
        queuedApp = null;
        tQueuedAppTimeout = -1;
    }

    public void setInternal(Internal internal) {
        // Remove existing source
        if(s != null) {
            s.window.detach();
        }

        // Switch
        s = internal;

        // Use this source
        s.window.viewport(viewport).attach();

        // Clear
        clear();
    }

    public void clear() {
        for(Clickable button : buttons)
            button.detach();
        buttons.clear();
        apps.clear();
        contexts.clear();

        notificationApps.clear();
        notifications.clear();
        lastOpenedList.clear();
    }


    public ScrollableSurface surface() {
        return s.surface;
    }

    public void transitionBack(Entity<?> from, Grid v) {
        if(!v.trigger(Globals.TRIGGER_BACK_TO_HOMESCREEN))
            return;

        // Hide quick access
        v.notification.hideAccessView();

        if(queuedScareGlitch != null) {
            // Scare was queued, start glitch
            queuedScareGlitch.attach(v);
            queuedScareWallpaper.ensureLoaded();
            cancelQueuedAppOnShow();
            // Transition immediately
            attach(v.screensGroup);
            from.detach();
        }
        else { // if(lastOpenedAppButton != null) {              // 20171104 - this could be null for some reason?
            // Else transition peacefully
            if(lastOpenedAppButton == null) {
                // Transition to center
                if(Gdx.app.getType() == Application.ApplicationType.iOS) {          // TODO: for iOS only, try to remove this and make it consistent
                    ScreenTransition transition = ScreenTransitionFactory.createAltHomescreenInTransition(
                            from, this, v.screensGroup
                    );
                    transition.attach(v);
                }
                else {
                    ScreenTransition transition = ScreenTransitionFactory.createHomescreenInTransition(
                            from, this, v.screensGroup,
                            0.5f,
                            Globals.LENGTH / 2f,
                            s.appMetrics[0][0].scaleX
                    );
                    transition.attach(v);
                }
            }
            else {
                if(Gdx.app.getType() == Application.ApplicationType.iOS) {          // TODO: for iOS only, try to remove this and make it consistent
                    ScreenTransition transition = ScreenTransitionFactory.createAltHomescreenInTransition(
                            from, this, v.screensGroup
                    );
                    transition.attach(v);
                }
                else {
                    ScreenTransition transition = ScreenTransitionFactory.createHomescreenInTransition(
                            from, this, v.screensGroup,
                            lastOpenedAppButton.getX(),
                            lastOpenedAppButton.getY(),
                            lastOpenedAppButton.getWidth()
                    );
                    transition.attach(v);
                }
            }

            s.closeSound.play();
        }
    }

    public void removeApp(String contextName) {
        int index = contexts.indexOf(contextName, false);
        if(index != -1) {
            buttons.removeIndex(index).detach();      // remove button
            apps.removeIndex(index);
            contexts.removeIndex(index);
        }
    }

    public void openApp(String contextName) {
        int index = contexts.indexOf(contextName, false);
        if(index != -1) {
            openAppButton(buttons.items[index]);
            return;
        }
    }

    public boolean containsApp(App app) {
        return apps.contains(app, true);
    }

    public App findApp(String contextName) {
        int index = contexts.indexOf(contextName, false);
        if(index != -1)
            return apps.items[index];
        return null;    // not found
    }

    public Clickable resolveButton(String contextName) {
        int index = contexts.indexOf(contextName, false);
        if(index != -1)
            return buttons.items[index];
        return null;
    }

    public void showDisabled(String contextName, Sprite icon) {
        Clickable button = resolveButton(contextName);
        if(icon != null)
            button.visuals(icon);
        TextBox title = button.iterate(null, TextBox.class, false, null);
        title.windowAnimation(ScaleAnim.gone.startAndReset(), true, true);
    }

    public void showEnabled(String contextName, String launcherFilename) {
        Clickable button = resolveButton(contextName);
        button.visuals(Sprite.load(launcherFilename));
        TextBox title = button.iterate(null, TextBox.class, false, null);
        title.windowAnimation(null, false, false);
    }


    public void replaceApp(String contextName, App with) {
        int index = contexts.indexOf(contextName, false);
        if(index != -1)
            apps.items[index] = with;
    }

    public void showLocked(String contextName) {
        Clickable button = resolveButton(contextName);
        // If icon not present, add new
        if(button.find(s.notificationLockedView) == null)
            s.notificationLockedView.instantiate().viewport(button).attach();
    }

    public void showError(String contextName) {
        Clickable button = resolveButton(contextName);
        // If icon not present, add new
        if(button.find(s.notificationErrorView) == null)
            s.notificationErrorView.instantiate().viewport(button).attach();
    }

    public void removeLocked(String contextName) {
        int index = contexts.indexOf(contextName, false);
        if(index != -1) {
            // Remove lock badge
            StaticSprite badge = buttons.items[index].find(s.notificationLockedView);
            if(badge != null)
                badge.detachWithAnim();
        }
    }

    public void removeError(String contextName) {
        int index = contexts.indexOf(contextName, false);
        if(index != -1) {
            // Remove error badge
            StaticSprite badge = buttons.items[index].find(s.notificationErrorView);
            if(badge != null)
                badge.detachWithAnim();
        }
    }

    public void addNotification(String contextName) {
        addNotification(contextName, 0);
    }

    public int getTotalNotifications(String contextName) {
        int index  = notificationApps.indexOf(contextName, false);
        if(index == -1)
            return 0;       // not found
        int current = notifications.items[index];
        if(current < 0)
            return -(current + 1);
        else
            return current;
    }

    public void setIndefiniteNotification(String contextName, boolean isIndefinite) {
        if(isIndefinite)
            addNotification(contextName, -1);
        else {
            // Else remove indefinite notification
            int index  = notificationApps.indexOf(contextName, false);
            if(index == -1)
                return;       // not found
            int current = notifications.items[index];
            if(current < 0)
                current = -(current + 1);
            notifications.items[index] = current;
        }
    }

    public void addNotification(String contextName, int total) {
        if(total == 0)
            total = 1;      // Increment by default
        int index  = notificationApps.indexOf(contextName, false);
        if(index == -1) {
            // Insert as first
            notificationApps.insert(0, contextName);
            notifications.insert(0, 0);
        }
        else {
            // Move to first
            int current = notifications.removeIndex(index);
            notificationApps.removeIndex(index);
            notifications.insert(0, current);
            notificationApps.insert(0, contextName);
        }
        int current = notifications.items[0];
        if(total < 0) {
            // Make notifications indefinite if not already
            if(current == 0)
                current = -1;
            else if(current > 0)
                current = -(current + 1);
            else // if(current < 0)
                return;     // already indefinite
            notifications.items[0] = current;
        }
        else { // if(total > 0)
            // Increment
            if(current < 0)
                current -= total;
            else if(current >= 0)
                current += total;
            notifications.items[0] = current;
        }
        // Refresh
        refreshNotifications(contextName);
        Globals.grid.notification.refreshQuickAccess();
    }

    public String getNotificationText(String contextName) {
        int index  = notificationApps.indexOf(contextName, false);
        int current = index == -1 ? 0 : notifications.items[index];
        if(current == 0)
            return null;        // no notification
        // Indefinite notifications
        if(current < 0) {
            if (current <= -100)
                return "99+";
            else if (current < -1)
                return Integer.toString(-(current + 1));
            else // if(current == -1)
                return "!";
        }
        else {      // if(current > 0)
            // Normal notifications
            if(current >= 99)
                return "99+";
            else
                return Integer.toString(current);
        }
    }

    private void refreshNotifications(String contextName) {
        int index = contexts.indexOf(contextName, false);
        if(index == -1)
            return;

        String text = getNotificationText(contextName);
        Clickable button = buttons.items[index];
        if(text == null) {
            StaticSprite view = button.find(s.notificationView);
            if(view != null)
                view.detachWithAnim();
            return;
        }
        // If notification not present, add new
        TextBox view = button.find(s.notificationTextView);
        if(view == null) {
            s.notificationView.instantiate().viewport(button).attach();
            view = button.find(s.notificationTextView);
        }

        // Update text
        view.text().text(text);
    }

    public void clearNotifications(String contextName) {
        int index  = notificationApps.indexOf(contextName, false);
        if(index == -1)
            return;     // no notifications anyway
        int current = notifications.items[index];
        if(current < 0)
            notifications.items[index] = -1;
        else {
            // No more notifications
            notificationApps.removeIndex(index);
            notifications.removeIndex(index);
        }
        // Refresh
        refreshNotifications(contextName);
        Globals.grid.notification.refreshQuickAccess();
    }

    public Clickable getButton(String contextName) {
        int index = contexts.indexOf(contextName, false);
        if(index != -1)
            return buttons.items[index];
        return null;        // not found
    }

    public void addApp(int screen, int row, int column, Sprite icon, String titleText, String contextName, App app) {
        // Button
        UIElement.Metrics metrics = s.appMetrics[row][column].instantiate().move(screen, 0);
        Clickable button = s.appButton.instantiate().viewport(s.surface).visuals(icon).metrics(metrics).attach();

        // Title
        button.find(s.appTitleView).text().text(titleText);

        // Register
        contexts.add(contextName);
        buttons.add(button);
        apps.add(app);

        if(!lastOpenedList.contains(contextName, false))
            lastOpenedList.add(contextName);

        if(app != null && isAttached())
            app.refreshNotification(this);          // refresh as already attached
    }

    public void addDockApp(int column, Sprite icon, String titleText, String contextName, App app) {
        // Button
        Clickable button = s.dockAppButton.instantiate().viewport(s.dockView).visuals(icon).metrics(s.dockAppMetrics[column]).attach();

        // Title
        button.find(s.dockAppTitleView).text().text(titleText);

        // Reflection
        button.find(s.dockAppButtonReflection).visual(icon);

        // Register
        contexts.add(contextName);
        buttons.add(button);
        apps.add(app);

        if(!lastOpenedList.contains(contextName, false))
            lastOpenedList.add(contextName);

        if(app != null && isAttached())
            app.refreshNotification(this);          // refresh as already attached
    }

    public Homescreen() {
        // Initialize
        interfaceSource = new Builder<Object>(GBHomescreen.class, this);
        interfaceSource.build();
    }


    @Override
    protected void recreate(Grid v) {
        super.recreate(v);

        interfaceSource.start();

        if(lastOpenedApp != null) {
            clearNotifications(lastOpenedApp);
            lastOpenedAppButton = null;
            lastOpenedApp = null;
        }

        // Refresh all notifications
        for(int c = 0; c < apps.size; c++) {
            App app = apps.items[c];
            if(app != null)
                app.refreshNotification(this);
        }

        // Show tracker by default
        v.notification.showTracker();

        // Allow scares
        v.idleScare.reschedule();
    }

    @Override
    protected void release(Grid v) {
        super.release(v);

        interfaceSource.stop();

        // Stop showing tracker by default
        v.notification.hideTracker();

        hasOpened = false;              // Reset
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        super.render(v, r, renderTime);

        if(!hasOpened && renderTime > 0) {
            v.trigger(Globals.TRIGGER_HOMESCREEN_OPENED);       // ignore return
            hasOpened = true;
        }

        // Background
        Sprite bgSprite = v.wallpaperSprite;
        if(queuedScareGlitch != null) {
            // Use scare wallpaper if available
            if(queuedScareGlitch.isAttached()) {
                bgSprite = queuedScareWallpaper;
                inputEnabled = false;           // disable all input on scare
                if(Math.random() < queuedScareOverlayProbability)
                    s.bg.target(SaraRenderer.TARGET_OVERLAY_DIALOG);
                else
                    s.bg.target(SaraRenderer.TARGET_BG);
            }
            else {
                // Finished glitch, clear
                queuedScareWallpaper = null;
                queuedScareGlitch = null;
                inputEnabled = true;
                s.bg.target(SaraRenderer.TARGET_BG);
            }
        }
        if(s.bg.visual() == null || s.bg.visual().getMaterial() != bgSprite.getMaterial()) {
            if (bgSprite.length != Globals.LENGTH) {
                bgSprite = new Sprite(bgSprite.length, bgSprite.getMaterial());
                bgSprite.crop(Globals.LENGTH);
            }
            s.bg.visual(bgSprite);
        }

        // Queued app
        if(queuedApp != null && renderTime > 0) {
            if(Sys.getTime() < tQueuedAppTimeout) {
                // Trigger open app
                Clickable button = resolveButton(queuedApp);
                if(button == null)
                    Sys.error(TAG, "Queued app not found: " + queuedApp);
                else
                    button.simulateClick();
            }
            // Clear
            queuedApp = null;
            tQueuedAppTimeout = -1;
        }

        // Secret
        if(renderTime > tSecretQueued) {
            secret.run();
            secret = null;
            tSecretQueued = Float.MAX_VALUE;
        }
    }

    @Override
    public void onPressed(Grid v, UIElement<?> view, float x, float y, int b) {
        if(view instanceof Clickable) {
            Clickable button = (Clickable)view;
            int index = buttons.indexOf(button, true);
            if(index != -1) {
                return;
            }
        }
    }

    @Override
    public void onClick(Grid v, UIElement<?> button, int b) {

        if(!(button instanceof Clickable))
            return;

        openAppButton((Clickable) button);
    }


    private void openAppButton(Clickable button) {
        Grid v = Globals.grid;

        // Stop idle scare
        v.idleScare.stop();

        int index = buttons.indexOf(button, true);
        if(index == -1)
            return;     // not found
        App app = apps.items[index];
        if(app == null)
            return;        // no app associated

        Entity<?> screen = app.open();

        if(screen != null) {
            lastOpenedAppButton = button;
            lastOpenedApp = contexts.items[index];
            // Move to front
            index = lastOpenedList.indexOf(lastOpenedApp, false);
            if(index != -1)
                lastOpenedList.removeIndex(index);
            lastOpenedList.insert(0, lastOpenedApp);

            if(Gdx.app.getType() == Application.ApplicationType.iOS) {          // TODO: for iOS only, try to remove this and make it consistent
                ScreenTransition transition = ScreenTransitionFactory.createAltHomescreenOutTransition(
                        Homescreen.this, screen, v.screensGroup
                );
                transition.attach(v);
            }
            else {
                ScreenTransition transition = ScreenTransitionFactory.createHomescreenOutTransition(
                        Homescreen.this, screen, v.screensGroup,
                        button.getX(),
                        button.getY(),
                        button.getWidth()
                );
                transition.attach(v);
            }


            s.openSound.play();
        }
    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(inputType == INPUT_KEY_UP && (key == Input.Keys.BACK || key == Input.Keys.ESCAPE)) {
            v.notification.openTracker();
            return true;
        }
        return false;
    }

}
