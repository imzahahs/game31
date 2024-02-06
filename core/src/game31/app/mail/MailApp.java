package game31.app.mail;

import com.badlogic.gdx.utils.Array;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import game31.Globals;
import game31.Grid;
import game31.JsonSource;
import game31.Media;
import game31.ScriptState;
import game31.app.homescreen.Homescreen;
import game31.model.MailModel;
import sengine.Entity;

/**
 * Created by Azmi on 15/8/2016.
 */
public class MailApp extends Entity<Grid> implements Homescreen.App {
    static final String TAG = "MailApp";

    // Time format
    private DateFormat timeFormat;
    private DateFormat dateFormat;

    public final MailInboxScreen inboxScreen;
    public final MailThreadScreen threadScreen;

    public final Array<MailConversation> conversations = new Array<MailConversation>(MailConversation.class);

    public String loginName;
    public String loginEmail;

    public boolean isContactsRefreshing = false;

    // Sources
    String configFilename;


    public boolean pack(ScriptState state) {
        for(MailConversation conversation : conversations) {
            if(!conversation.pack(state))
                return false;
        }
        inboxScreen.pack(state);
        return true;
    }

    public String getCurrentDateText() {
        return dateFormat.format(new Date(Globals.grid.getSystemTime()));
    }

    public String getCurrentTimeText() {
        return timeFormat.format(new Date(Globals.grid.getSystemTime()));
    }


    public boolean isContactsRefreshing() {
        return isContactsRefreshing;
    }

    public void clearContact(MailConversation conversation) {
        // Get row
        int index = conversations.indexOf(conversation, true);
        if(index == -1)
            throw new RuntimeException("Unknown conversation " + conversation);
        inboxScreen.clear(index);
        if(threadScreen.conversation == conversation)
            threadScreen.clear();
    }

    public void refreshAvailableUserMessages(MailConversation conversation) {
        if(threadScreen.conversation == conversation)
            threadScreen.refreshAvailableUserMessages();
    }

    public void addMessage(MailConversation contact, String origin, String message, String timeText, String dateText, String trigger, float tTriggerTime) {
        // Format message
        message = Globals.grid.format(message);
        String actualMessage = message;

        // Find plaintext block for inbox preview
        String[] blocks = message.split(Globals.MAIL_BLOCK_TOKEN);
        for(int c = 0; c < blocks.length; c++) {
            String block = blocks[c];
            if(!block.startsWith(Globals.MAIL_GRAPHIC_PREFIX)) {
                message = block;
                break;
            }
        }
        message = message.trim();

        // Get row
        int index = conversations.indexOf(contact, true);           // TODO: reorder according to time
        if(index == -1)
            throw new RuntimeException("Unknown contact " + contact);
        // Inform contacts list
        if(message.startsWith(Globals.CORRUPTED_PREFIX)) {
            if(origin.contentEquals(Globals.ORIGIN_USER))
                inboxScreen.addMessage(index, contact.name, loginEmail, contact.subject, "E-mail corrupted", timeText, threadScreen.conversation == contact);
            else
                inboxScreen.addMessage(index, contact.name, contact.email, contact.subject, "E-mail corrupted", timeText, threadScreen.conversation == contact);
        }
        else if(message.startsWith(Globals.PHOTOROLL_PREFIX)) {
            String path = message.substring(Globals.PHOTOROLL_PREFIX.length());

            // Unlock and get this media
            Media media = Globals.grid.photoRollApp.unlock(path, false);

            if(origin.contentEquals(Globals.ORIGIN_USER)) {
                if(media.isVideo())
                    inboxScreen.addMessage(index, contact.name, loginEmail, contact.subject, "You sent a video", timeText, threadScreen.conversation == contact);
                else
                    inboxScreen.addMessage(index, contact.name, loginEmail, contact.subject, "You sent a photo", timeText, threadScreen.conversation == contact);
            }
            else {
                String firstname = contact.name.split(" ", 2)[0];          // TODO: could be from 3rd person
                if(media.isVideo())
                    inboxScreen.addMessage(index, contact.name, contact.email, contact.subject, firstname + " sent a video", timeText, threadScreen.conversation == contact);
                else
                    inboxScreen.addMessage(index, contact.name, contact.email, contact.subject, firstname + " sent a photo", timeText, threadScreen.conversation == contact);
            }
        }
        else if(origin.contentEquals(Globals.ORIGIN_USER))
            inboxScreen.addMessage(index, contact.name, loginEmail, contact.subject, message, timeText, threadScreen.conversation == contact);
        else
            inboxScreen.addMessage(index, contact.name, contact.email, contact.subject, message, timeText, threadScreen.conversation == contact);
        // Inform thread
        if(threadScreen.conversation == contact)
            threadScreen.addMessage(origin, actualMessage, timeText, dateText, trigger, tTriggerTime);
    }

    public void refreshConversations() {
        inboxScreen.clear();
        threadScreen.clear();

        isContactsRefreshing = true;

        for (int c = 0; c < conversations.size; c++) {
            MailConversation contact = conversations.items[c];
            inboxScreen.addConversation();
            contact.refresh(this);
        }

        isContactsRefreshing = false;
    }

    public void addConversation(MailModel.ConversationModel model, final ScriptState state) {
        // Check online source
        if (model.google_sheet_url != null && Globals.allowMailOnlineSources)
            Globals.addGoogleSource("assets", model.dialogue_tree_path, model.google_sheet_url);

        MailConversation contact = new MailConversation(model, state);

        // Add
        conversations.add(contact);
    }


    public void load(final String filename, final ScriptState state) {
        configFilename = filename;

        // Clear
        inboxScreen.clear();
        threadScreen.clear();
        conversations.clear();


        // Load config
        JsonSource<MailModel> configSource = new JsonSource<MailModel>(filename, MailModel.class);
        MailModel config = configSource.load();

        // Config
        loginName = config.name;
        loginEmail = config.email;

        // Load contacts
        for(int c = 0; c < config.conversations.length; c++)
            addConversation(config.conversations[c], state);

        // Refresh contacts
        refreshConversations();

        inboxScreen.unpack(state);
    }

    public MailApp() {
        // Date formats
        timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        timeFormat.setTimeZone(Globals.grid.timeZone);
        dateFormat.setTimeZone(Globals.grid.timeZone);

        // Screens
        inboxScreen = new MailInboxScreen(this);
        threadScreen = new MailThreadScreen(this);

        load(Globals.mailConfigFilename, Globals.grid.state);
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        // Update all contacts
        for(int c = 0; c < conversations.size; c++)
            conversations.items[c].update(this);
    }

    @Override
    public Entity<?> open() {
        return inboxScreen;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        homescreen.clearNotifications(Globals.CONTEXT_APP_MAIL);
        int unread = inboxScreen.countUnreadThreads();
        if(unread > 0)
            homescreen.addNotification(Globals.CONTEXT_APP_MAIL, unread);
    }
}
