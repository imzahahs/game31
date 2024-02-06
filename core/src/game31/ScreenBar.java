package game31;

import com.badlogic.gdx.Input;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import game31.gb.GBScreenBar;
import sengine.Sys;
import sengine.calc.SetDistributedSelector;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnPressed;
import sengine.ui.OnReleased;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 5/13/2017.
 */

public class ScreenBar extends Menu<Grid> implements OnPressed<Grid>, OnReleased<Grid> {
    static final String TAG = "ScreenBar";

    public static class Internal {
        // Appbar (top)
        public StaticSprite appbarView;

        public StaticSprite notifyIndicatorView;

        public UIElement.Group statusGroup;
        public StaticSprite batteryView;
        public StaticSprite wifiView;
        public StaticSprite cellView;
        public UIElement.Metrics statusWithTextMetrics;
        public UIElement.Metrics statusWithoutTextMetrics;

        public Sprite[] batterySprites;
        public float[] batteryLevels;

        public SetDistributedSelector<Sprite> cellSprites;
        public SetDistributedSelector<Sprite> wifiSprites;

        public TextBox timeView;
        public DateFormat timeFormat;
        public DateFormat dateFormat;

        public TextBox titleView;

        public TextBox extTitleView;
        public TextBox extSubtitleView;

        // Control bar (bottom)
        public StaticSprite bottomBarView;
        public Clickable bottomBackButton;
        public Clickable bottomHomeButton;
        public Clickable bottomIrisButton;
        public StaticSprite bottomPressed;
        public StaticSprite bottomIrisIndicatorView;

        // Shadows
        public StaticSprite[] topShadowViews;
        public StaticSprite[] bottomShadowViews;
    }

    // Source
    private final Builder<Object> interfaceSource;
    private Internal s;
    private UIElement.Metrics initialTitleMetrics;
    private UIElement.Metrics initialExtTitleMetrics;
    private UIElement.Metrics initialExtSubtitleMetrics;


    // Current
    private boolean isClockBar;
    private String title;
    private String subtitle;
    private float titleMoveX;
    private float titleMoveY;
    private float subtitleMoveX;
    private float subtitleMoveY;

    private long lastTimeRefresh = Long.MIN_VALUE;

    public TextBox titleView() { return s.titleView; }
    public TextBox extendedTitleView() { return s.extTitleView; }
    public TextBox subtitleView() { return s.extSubtitleView; }

    public Clickable backButton() {
        return s.bottomBackButton;
    }

    public Clickable homeButton() {
        return s.bottomHomeButton;
    }

    public Clickable irisButton() {
        return s.bottomIrisButton;
    }

    public StaticSprite appbar() {
        return s.appbarView;
    }

    public StaticSprite navbar() {
        return s.bottomBarView;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public void showIrisIndicator() {
        s.bottomIrisIndicatorView.attach();
        Globals.grid.postMessage(new Runnable() {
            @Override
            public void run() {
                s.bottomIrisIndicatorView.detachWithAnim();
            }
        });
    }

    public void simulatePressHome() {
        if(s.bottomBarView.isAttached())
            s.bottomHomeButton.simulateClick();
        else
            Sys.error(TAG, "No ScreenBars available");
    }

    public void refresh() {
        Grid v = Globals.grid;

        // Battery
        // Choose battery sprite
        Sprite batterySprite = s.batterySprites[s.batterySprites.length - 1];
        for(int c = s.batteryLevels.length - 1; c >= 0; c--) {
            if(v.batteryLevel < s.batteryLevels[c])
                break;
            // Else accept
            batterySprite = s.batterySprites[c];
        }
        if(s.batteryView.visual() != batterySprite)
            s.batteryView.visual(batterySprite);

        // Cell signal
        float uptime = v.getRenderTime();
        Sprite cellSprite = s.cellSprites.selectOffset(uptime);
        if(s.cellView.visual() != cellSprite)
            s.cellView.visual(cellSprite);

        // Wifi signal
        Sprite wifiSprite = s.wifiSprites.selectOffset(uptime);
        if(s.wifiView.visual() != wifiSprite)
            s.wifiView.visual(wifiSprite);

        long systemTime = v.getSystemTime();
        if(Math.abs(systemTime - lastTimeRefresh) >= (60 * 1000)) {
            // Time difference of 1 minute, refresh
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(v.getSystemTime());
            int seconds = calendar.get(Calendar.SECOND);
            lastTimeRefresh = systemTime + (-seconds * 1000);       // round to previous minute

            // Format time
            Date date = new Date(systemTime);
            if(isClockBar) {
                s.extTitleView.text().text(s.timeFormat.format(date));
                s.extSubtitleView.text().text(s.dateFormat.format(date));
            }
            else {
                s.timeView.text().text(s.timeFormat.format(date));
            }
        }
    }

    public void color(int appbarColor, float appbarAlpha, int navbarColor, float navbarAlpha) {
        ColorAttribute.of(s.appbarView.visual()).set(appbarColor).alpha(appbarAlpha);
        ColorAttribute.of(s.bottomBarView.visual()).set(navbarColor).alpha(navbarAlpha);
    }

    public void showShadows(int color, float alpha) {
        showShadows(color, alpha, color, alpha);
    }

    public void showShadows(int appbarColor, float appbarAlpha, int navbarColor, float navbarAlpha) {
        if(appbarAlpha > 0f) {
            for (int c = 0; c < s.topShadowViews.length; c++) {
                StaticSprite ss = s.topShadowViews[c];
                ColorAttribute.of(ss.visual()).set(appbarColor).alpha(appbarAlpha);
                ss.attach();
            }
        }
        if(navbarAlpha > 0f) {
            for (int c = 0; c < s.bottomShadowViews.length; c++) {
                StaticSprite ss = s.bottomShadowViews[c];
                ColorAttribute.of(ss.visual()).set(navbarColor).alpha(navbarAlpha);
                ss.attach();
            }
        }
    }

    public void showNavbar(boolean allowBack, boolean allowHome, boolean allowIris) {
        s.bottomBarView.viewport(viewport).attach();

        if(allowBack)
            s.bottomBackButton.attach();
        else
            s.bottomBackButton.detach();

        if(allowHome)
            s.bottomHomeButton.attach();
        else
            s.bottomHomeButton.detach();

        if(allowIris)
            s.bottomIrisButton.attach();
        else
            s.bottomIrisButton.detach();
    }

    public void showAppbarClock(float moveX, float moveY) {
        isClockBar = true;

        s.appbarView.viewport(viewport).attach();

        s.extTitleView.attach();
        s.extTitleView.metrics(initialExtTitleMetrics.instantiate().move(moveX, moveY));
        s.extSubtitleView.attach();
        s.extSubtitleView.metrics(initialExtSubtitleMetrics.instantiate().move(moveX, moveY));
        s.titleView.detach();

        s.timeView.detach();
        s.statusGroup.metrics(s.statusWithoutTextMetrics);

        // Reset
        lastTimeRefresh = Long.MIN_VALUE;
    }

    public void showAppbar(String title, String subtitle) {
        showAppbar(title, subtitle, titleMoveX, titleMoveY, subtitleMoveX, subtitleMoveY);
    }


    public void showAppbar(String title, String subtitle, float titleMoveX, float titleMoveY, float subtitleMoveX, float subtitleMoveY) {
        isClockBar = false;

        this.title = title;
        this.subtitle = subtitle;
        this.titleMoveX = titleMoveX;
        this.titleMoveY = titleMoveY;
        this.subtitleMoveX = subtitleMoveX;
        this.subtitleMoveY = subtitleMoveY;

        s.appbarView.viewport(viewport).attach();

        if(subtitle != null) {
            s.extTitleView.attach();
            s.extTitleView.metrics(initialExtTitleMetrics.instantiate().move(titleMoveX, titleMoveY));
            s.extSubtitleView.attach();
            s.extSubtitleView.metrics(initialExtSubtitleMetrics.instantiate().move(subtitleMoveX, subtitleMoveY));
            s.titleView.detach();

            s.extTitleView.text().text(title);
            s.extSubtitleView.text().text(subtitle);
        }
        else {
            s.extTitleView.detach();
            s.extSubtitleView.detach();
            s.titleView.attach();
            s.titleView.metrics(initialTitleMetrics.instantiate().move(titleMoveX, titleMoveY));

            s.titleView.text().text(title);
        }

        s.timeView.attach();
        s.statusGroup.metrics(s.statusWithTextMetrics);

        // Reset
        lastTimeRefresh = Long.MIN_VALUE;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            // Cleanup previous
            s.appbarView.detach();
            s.bottomBarView.detach();
        }

        s = internal;
//        s.appbarView.viewport(viewport).attach();

        initialTitleMetrics = s.titleView.metrics;
        initialExtTitleMetrics = s.extTitleView.metrics;
        initialExtSubtitleMetrics = s.extSubtitleView.metrics;

        // Reset
        lastTimeRefresh = Long.MIN_VALUE;
    }

    public ScreenBar() {
        interfaceSource = new Builder<Object>(GBScreenBar.class, this);
        interfaceSource.build();
    }


    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        // Reset
        lastTimeRefresh = Long.MIN_VALUE;
        s.timeFormat.setTimeZone(grid.timeZone);
        s.dateFormat.setTimeZone(grid.timeZone);
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Refresh
        refresh();
    }

    @Override
    public void onPressed(Grid v, UIElement<?> view, float x, float y, int button) {
        if(view == s.bottomBackButton || view == s.bottomHomeButton || view == s.bottomIrisButton)
            showNavPressed(view);
        else {
            OnPressed onPressed = findParent(OnPressed.class);
            if(onPressed != null)
                onPressed.onPressed(v, view, x, y, button);
        }
    }

    @Override
    public void onReleased(Grid v, UIElement<?> view, float x, float y, int button) {
        if(view == s.bottomBackButton || view == s.bottomHomeButton || view == s.bottomIrisButton)
            cancelNavPressed(view);
        else {
            OnReleased onReleased = findParent(OnReleased.class);
            if(onReleased != null)
                onReleased.onReleased(v, view, x, y, button);
        }
    }

    private void showNavPressed(UIElement<?> view) {
        StaticSprite anim = view.find(s.bottomPressed);
        if(anim != null)
            anim.detach();
        s.bottomPressed.instantiate().viewport(view).attach();
    }

    private void cancelNavPressed(UIElement<?> view) {
        StaticSprite anim = view.find(s.bottomPressed);
        if(anim != null)
            anim.detachWithAnim();
    }


    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(s.bottomBarView.isAttached() && inputType == INPUT_KEY_UP && (key == Input.Keys.BACK || key == Input.Keys.ESCAPE)) {
            s.bottomBackButton.simulateClick();
            return true;
        }
        return false;
    }
}
