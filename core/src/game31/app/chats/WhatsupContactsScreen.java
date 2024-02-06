package game31.app.chats;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import java.util.Locale;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.RefreshAction;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.ScriptState;
import game31.gb.chats.GBWhatsupContactsScreen;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.audio.Sound;
import sengine.graphics2d.Mesh;
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
 * Created by Azmi on 1/7/2016.
 */
public class WhatsupContactsScreen extends Menu<Grid> implements OnClick<Grid> {

    final WhatsupApp app;

    // Window
    UIElement<?> window;
    ScrollableSurface surface;
    ScreenBar bars;
    private Clickable refreshButton;

    // Row
    Clickable row;
    StaticSprite rowProfileView;
    TextBox rowNameView;
    TextBox rowMessageView;
    TextBox rowTimeView;
    StaticSprite rowUnreadBg;
    TextBox rowUnreadTextView;
    private String statusTypingTitleFormat;
    private String statusSelfTypingTitle;
    private StaticSprite onlineIndicator;
    private Mesh onlineIndicatorMesh;
    private Mesh offlineIndicatorMesh;
    private Animation onlineAnim;
    private Animation.Loop onlineAnimHandle;
    private Animation offlineAnim;
    private Animation.Loop offlineAnimHandle;

    private Audio.Sound messageSound;

    // State
    private float surfaceY;
    private final Array<Clickable> rows = new Array<Clickable>(Clickable.class);
    private final Array<Clickable> positions = new Array<Clickable>(Clickable.class);
    private boolean positionsChanged = false;
    private final IntArray rowsUnread = new IntArray();
    private boolean isNotificationsDisabled = false;

    // Sources
    private final Builder<Object> interfaceSource;

    public int countTotalUnread() {
        int total = 0;
        for(int c = 0; c < rowsUnread.size; c++) {
            total += rowsUnread.items[c];
        }
        return total;
    }

    public void pack(ScriptState state) {
        int[] order = new int[positions.size];
        for(int c = 0; c < positions.size; c++) {
            order[c] = rows.indexOf(positions.items[c], true);
        }
        state.set(app.configFilename + ".positions", order);
        state.set(app.configFilename + ".unread", rowsUnread.toArray());
    }

    public void unpack(ScriptState state) {
        int[] order = state.get(app.configFilename + ".positions", null);
        if(order != null) {
            positions.clear();
            for(int index : order)
                positions.add(rows.items[index]);
            positionsChanged = true;
        }
        int[] unread = state.get(app.configFilename + ".unread", null);
        if(unread != null) {
            rowsUnread.clear();
            rowsUnread.addAll(unread);
            for(int c = 0; c < rows.size; c++)
                refreshRowUnread(c);
        }
    }

    public void setNotificationsDisabled(boolean notificationsDisabled) {
        isNotificationsDisabled = notificationsDisabled;
    }

    public void setTitleFormats(String typingTitleFormat, String selfTypingTitle) {
        statusTypingTitleFormat = typingTitleFormat;
        statusSelfTypingTitle = selfTypingTitle;
    }

    public void setRowGroup(Clickable bg, StaticSprite profileView, TextBox nameView, TextBox messageView, TextBox timeView, StaticSprite unreadBg,  TextBox unreadTextView,
                            StaticSprite onlineIndicator,
                            Mesh onlineIndicatorMesh, Mesh offlineIndicatorMesh,
                            Animation onlineAnim, Animation offlineAnim
    ) {
        row = bg;
        rowProfileView = profileView;
        rowNameView = nameView;
        rowMessageView = messageView;
        rowTimeView = timeView;
        rowUnreadBg = unreadBg;
        rowUnreadTextView = unreadTextView;

        this.onlineIndicator = onlineIndicator;

        this.onlineIndicatorMesh = onlineIndicatorMesh;
        this.offlineIndicatorMesh = offlineIndicatorMesh;

        this.onlineAnim = onlineAnim;
        this.onlineAnimHandle = onlineAnim.loopAndReset();
        this.offlineAnim = offlineAnim;
        this.offlineAnimHandle = offlineAnim.loopAndReset();
    }

    public void setWindow(UIElement<?> window, ScrollableSurface surface, ScreenBar bars, Clickable refreshButton) {
        // Activate
        if(this.window != null)
            this.window.detach();
        this.window = window.viewport(viewport).attach();
        this.surface = surface;
        this.bars = bars;

        this.refreshButton = refreshButton;

        // Refresh contacts if there are any
        if(app.contacts.size > 0)
            app.refreshContacts();

    }

    public void clear() {
        // Clear all and reset surface
        rows.clear();
        rowsUnread.clear();
        positions.clear();
        surface.detachChilds();
        surfaceY = (+surface.getLength() / 2f) - surface.paddingTop();
    }

    public void clear(int row) {
        // Get group
        Clickable group = rows.get(row);
        // Mesage
        group.find(rowMessageView).text().text(null);
        // Time
        group.find(rowTimeView).text().text(null);
        // Unread
        clearUnread(row);
    }

    public void clearUnread(WhatsupContact contact) {
        int index = app.contacts.indexOf(contact, true);
        if(index == -1)
            return;         // UB
        Clickable row = rows.items[index];
        index = positions.indexOf(row, true);
        if(index == -1)
            return;
        clearUnread(index);
    }

    public void clearUnread(int row) {
        Clickable group = rows.get(row);
        rowsUnread.items[row] = 0;
        group.find(rowUnreadBg).renderingEnabled = false;
    }

    public void refreshAvailableUserMessages(WhatsupContact contact) {
        int row = app.contacts.indexOf(contact, true);
        if(row == -1)
            return;
        Clickable group = rows.get(row);
        StaticSprite indicator = group.find(onlineIndicator);
        if(contact.tree.isUserMessagesAvailable()) {
            indicator.visual(onlineIndicatorMesh);
            if(indicator.windowAnim != onlineAnimHandle)
                indicator.windowAnimation(onlineAnimHandle, false, true);
        }
        else {
            indicator.visual(offlineIndicatorMesh);
            if(indicator.windowAnim != offlineAnimHandle)
                indicator.windowAnimation(offlineAnimHandle, false, true);
        }
    }

    public void informTyping(int row, String origin) {
        // Get group
        Clickable group = rows.get(row);
        // Message
        if(origin.equals(WhatsupContact.ORIGIN_USER))
            group.find(rowMessageView).text().text(statusSelfTypingTitle);
        else {
            String person;
            if(origin.equals(WhatsupContact.ORIGIN_SENDER))
                person = group.find(rowNameView).text().text;           // contact name
            else
                person = origin;
            // Use the last name (20180819: This is hack workaround for "Evil Teddy" and "Normal Teddy")
            String[] names = person.split(" ", 2);
            if(names.length > 1)
                person = names[1];
            else
                person = names[0];
            group.find(rowMessageView).text().text(String.format(Locale.US, statusTypingTitleFormat, person));
        }
    }

    private void refreshRowUnread(int row) {
        int unread = rowsUnread.items[row];
        Clickable group = rows.items[row];
        if(unread == 0)
            group.find(rowUnreadBg).renderingEnabled = false;
        else {
            group.find(rowUnreadBg).renderingEnabled = true;
            if(unread > 99)
                group.find(rowUnreadTextView).text().text("+99");
            else
                group.find(rowUnreadTextView).text().text(Integer.toString(unread));
        }
    }

    public void addMessage(int row, String message, String time, boolean isCurrentThread) {
        // Get group
        Clickable group = rows.items[row];
        // Mesage
        group.find(rowMessageView).text(message);
        // Time
        group.find(rowTimeView).text(time);
        // Notification
        if(!isCurrentThread && !app.isContactsRefreshing()) {
            if(!isNotificationsDisabled) {
                Audio.Sound customNotificationSound = app.contacts.items[row].customNotificationSound;
                if(customNotificationSound != null)
                    customNotificationSound.play();
                else
                    messageSound.play();
                Mesh profileMesh = group.find(rowProfileView).visual();
                String contactName = group.find(rowNameView).text().text;
                Globals.grid.notification.show(profileMesh, app.icon, -1, contactName, message, Globals.CONTEXT_APP_CHATS);
            }
            Globals.grid.homescreen.addNotification(Globals.CONTEXT_APP_CHATS);
        }
        // Unread
        if(isCurrentThread || app.isContactsRefreshing())
            rowsUnread.items[row] = 0;
        else
            rowsUnread.items[row]++;
        refreshRowUnread(row);
        // Status
        group.find(onlineIndicator).windowAnimation(offlineAnimHandle, false, true);
        // Move this group up
//        if(!isCurrentThread) {        TODO: move up even if current thread?
        if(!app.isContactsRefreshing()) {
            positions.removeValue(group, true);
            positions.insert(0, group);
            positionsChanged = true;
        }
    }

    public void addContact(String name, String profileFilename) {
        Clickable group = row.instantiate();

        // Name
        group.find(rowNameView).text().text(name);
        // Profile
        StaticSprite profile = group.find(rowProfileView);
        profile.visual(Sprite.load(profileFilename));
        // Hide unread
        group.find(rowUnreadBg).renderingEnabled = false;

        // Status
        group.find(onlineIndicator).windowAnimation(offlineAnimHandle, false, true);

        rows.add(group);
        rowsUnread.add(0);
        positions.add(group);
        positionsChanged = true;
    }

    public void refreshPositions() {
        // Clear all rows
        surfaceY = (+surface.getLength() / 2f) - surface.paddingTop();
        surface.detachChilds();
        // First add all unread up
        int index = 0;
        for(int c = 0; c < app.contacts.size; c++) {
            int unread = rowsUnread.items[c];
            if(unread > 0) {
                Clickable row = rows.items[c];
                int current = positions.indexOf(row, true);
                if(current > index) {
                    positions.removeIndex(current);
                    positions.insert(index, row);
                    index++;
                }
            }
        }
        // Move online contacts up too
        for(int c = 0; c < app.contacts.size; c++) {
            if (app.contacts.items[c].tree.isUserMessagesAvailable()) {
                // Move up
                Clickable row = rows.items[c];
                int current = positions.indexOf(row, true);
                if (current > index) {
                    positions.removeIndex(current);
                    positions.insert(index, row);
                    index++;
                }
            }
        }

        // Add one by one
        for(int c = 0; c < positions.size; c++) {
            Clickable group = positions.items[c];
            if(group.find(rowMessageView).text().text == null)
                continue;
            group.metrics.anchorWindowY = surfaceY / surface.getLength();
            group.viewport(surface).attach();
            surfaceY -= group.getLength();
        }
        // Scroll to top
        surface.move(0, -1000);
        positionsChanged = false;
    }

    public WhatsupContactsScreen(WhatsupApp app) {

        this.app = app;

        messageSound = Sound.load("sounds/chat_notify.ogg");        // TODO

        // Initialize
        interfaceSource = new Builder<Object>(GBWhatsupContactsScreen.class, this);
        interfaceSource.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        refreshPositions();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Update animations
        onlineAnimHandle.update(getRenderDeltaTime());
        offlineAnimHandle.update(getRenderDeltaTime());

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
    public void onClick(final Grid v, UIElement<?> button, int b) {
        if(button == surface)
            return;     // not responding
        if(button == bars.backButton()) {
            v.homescreen.transitionBack(this, v);
            return;
        }
        if(button == bars.homeButton()) {
            v.homescreen.transitionBack(this, v);
            return;
        }
        if(button == bars.irisButton()) {
            v.notification.openTracker();
            return;
        }


        if(button == refreshButton) {
            new RefreshAction() {
                @Override
                protected void load() {
                    app.load(Globals.chatsConfigFilename, v.state);
                }
            }.start();
            return;
        }

        if(button instanceof Clickable) {
            int contactIdx = rows.indexOf((Clickable) button, true);
            if (contactIdx != -1) {
                WhatsupContact contact = app.contacts.items[contactIdx];
//            // Transition to labels screen
//            app.labelsScreen.show(contact);
//            ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(this, app.labelsScreen, v.screensGroup);
//            transition.attach(v.screensGroup);

                // Open thread
                app.threadScreen.open(contact);

                // Transition
                ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(this, app.threadScreen, v.screensGroup);
                transition.attach(v.screensGroup);

                clearUnread(contact);

                return;
            }
        }
    }
}
