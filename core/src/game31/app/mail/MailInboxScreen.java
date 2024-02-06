package game31.app.mail;

import com.badlogic.gdx.utils.Array;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScriptState;
import game31.gb.mail.GBMailInboxScreen;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.graphics2d.Sprite;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 15/8/2016.
 */
public class MailInboxScreen extends Menu<Grid> implements OnClick<Grid> {

    public interface InterfaceSource {
        String buildUnreadTitleString(int totalUnread);
    }
    
    public static class Internal {

        // Row group
        public Clickable row;
        public TextBox rowNameView;
        public Animation rowNameUnreadAnim;
        public TextBox rowSubjectView;
        public TextBox rowMessageView;
        public TextBox rowTimeView;
        public StaticSprite rowUnreadIcon;

        // Window
        public UIElement<?> window;
        public ScrollableSurface surface;
        public ScreenBar bars;

        // Notification
        public Audio.Sound messageSound;
        public Sprite notificationIcon;

    }
    
    // App
    private final MailApp app;

    // Interface source
    private final Builder<InterfaceSource> interfaceSource;
    private Internal s;

    // Current
    private float surfaceY;

    private final Array<Clickable> rows = new Array<Clickable>(Clickable.class);
    private final Array<Clickable> positions = new Array<Clickable>(Clickable.class);
    private boolean positionsChanged = false;
    private boolean isNotificationsDisabled = false;

    public int countUnreadThreads() {
        int totalUnread = 0;
        for(int c = 0; c < rows.size; c++) {
            if(rows.items[c].find(s.rowUnreadIcon).renderingEnabled)
                totalUnread++;
        }
        return totalUnread;
    }

    public void pack(ScriptState state) {
        int[] order = new int[positions.size];
        for(int c = 0; c < positions.size; c++) {
            order[c] = rows.indexOf(positions.items[c], true);
        }
        state.set(app.configFilename + ".positions", order);
        boolean[] unread = new boolean[rows.size];
        for(int c = 0; c < unread.length; c++) {
            unread[c] = rows.items[c].find(s.rowUnreadIcon).renderingEnabled;
        }
        state.set(app.configFilename + ".unread", unread);
    }

    public void unpack(ScriptState state) {
        int[] order = state.get(app.configFilename + ".positions", null);
        if(order != null && order.length == rows.size) {
            positions.clear();
            for(int index : order)
                positions.add(rows.items[index]);
            positionsChanged = true;
        }
        boolean[] unread = state.get(app.configFilename + ".unread", null);
        if(unread != null && unread.length == rows.size) {
            for(int c = 0; c < unread.length; c++) {
                if(unread[c]) {
                    rows.items[c].find(s.rowUnreadIcon).renderingEnabled = true;
                    rows.items[c].find(s.rowNameView).windowAnimation(s.rowNameUnreadAnim.loopAndReset(), true, true);
                }
                else {
                    rows.items[c].find(s.rowUnreadIcon).renderingEnabled = false;
                    rows.items[c].find(s.rowNameView).windowAnimation(null, false, false);
                }
            }
        }
    }

    public void setNotificationsDisabled(boolean notificationsDisabled) {
        isNotificationsDisabled = notificationsDisabled;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        if(rows.size > 0)
            app.refreshConversations();
    }


    public void clear() {
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        s.surface.detachChilds();
        rows.clear();
        positions.clear();
        positionsChanged = true;
    }

    public void clear(int row) {
        // Get group
        Clickable group = rows.get(row);
        // Mesage
        group.find(s.rowMessageView).text().text(null);
        // Time
        group.find(s.rowTimeView).text().text(null);
        // Unread
        clearUnread(row);
    }

    private void clearUnread(int row) {
        Clickable group = rows.get(row);
        group.find(s.rowUnreadIcon).renderingEnabled = false;
        group.find(s.rowNameView).windowAnimation(null, false, false);
    }

    public void addMessage(int row, String from, String email, String subject, String message, String time, boolean isCurrentThread) {
        // Get group
        Clickable group = rows.get(row);
        // From
        group.find(s.rowNameView).text(from);
        // Subject
        group.find(s.rowSubjectView).text(subject);
        // Mesage
        group.find(s.rowMessageView).text(message);
        // Time
        group.find(s.rowTimeView).text().text(time);
        // Unread
//        boolean wasUnread = group.find(s.rowUnreadIcon).renderingEnabled;
        if(isCurrentThread || app.isContactsRefreshing()) {         // No need new email marker if its currently viewed thread or during start of game (contacts refreshing)
            group.find(s.rowUnreadIcon).renderingEnabled = false;
            group.find(s.rowNameView).windowAnimation(null, false, false);
        }
        else {
            group.find(s.rowUnreadIcon).renderingEnabled = true;
            group.find(s.rowNameView).windowAnimation(s.rowNameUnreadAnim.loopAndReset(), true, true);
        }
        // Move this group up
        if(!isCurrentThread) {
            positions.removeValue(group, true);
            positions.insert(0, group);
            positionsChanged = true;
            app.threadScreen.refreshNavigationTitle();

//            if(!app.isContactsRefreshing() && !wasUnread) {
            if(!app.isContactsRefreshing()) {
                // Add notification
                if(!isNotificationsDisabled) {
                    s.messageSound.play();
                    Globals.grid.notification.show(s.notificationIcon, null, -1, "Mail", subject, Globals.CONTEXT_APP_MAIL);
                }
                Globals.grid.homescreen.addNotification(Globals.CONTEXT_APP_MAIL);
            }
        }
    }

    public void addConversation() {
        Clickable group = s.row.instantiate();

        // Hide unread
        group.find(s.rowUnreadIcon).renderingEnabled = false;

        rows.add(group);
    }

    public int rowIndexOf(MailConversation conversation) {
        int index = app.conversations.indexOf(conversation, true);
        if(index == -1)
            return -1;      // not found
        Clickable group = rows.items[index];
        return positions.indexOf(group, true);
    }

    public int totalVisibleRows() {
        return positions.size;
    }

    public int conversationIndexOf(int row) {
        Clickable group = positions.items[row];
        return rows.indexOf(group, true);
    }

    private void refreshPositions() {
        // Clear all rows
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        s.surface.detachChilds();
        // Add one by one
        for(int c = 0; c < positions.size; c++) {
            Clickable group = positions.items[c];
            group.metrics.anchorWindowY = surfaceY / s.surface.getLength();
            group.viewport(s.surface).attach();
            surfaceY -= group.getLength();
        }
        // Scroll to top
        s.surface.move(0, -1000);
        positionsChanged = false;
        refreshUnreadTitle();
    }

    private void refreshUnreadTitle() {
        // Count total unread
        int totalUnread = 0;
        for(int c = 0; c < positions.size; c++) {
            Clickable group = positions.items[c];
            if(group.find(s.rowUnreadIcon).renderingEnabled)
                totalUnread++;
        }
        // Update title
        s.bars.showAppbar(interfaceSource.build().buildUnreadTitleString(totalUnread), null);
    }

    public MailInboxScreen(MailApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<InterfaceSource>(GBMailInboxScreen.class, this);
        interfaceSource.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Update positions
        if(positionsChanged)
            refreshPositions();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();
    }

    @Override
    public void onClick(Grid v, UIElement<?> button, int b) {
        if(button == s.bars.backButton() || button == s.bars.homeButton()) {
            v.homescreen.transitionBack(this, v);
            return;
        }

        if(button == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        // Conversations
        for(int c = 0; c < rows.size; c++) {
            if(rows.items[c] == button) {
                app.threadScreen.show(app.conversations.items[c]);
                app.threadScreen.open(this, v.screensGroup);
                // Refresh title
                refreshUnreadTitle();
                return;
            }
        }

        // TODO
    }
}
