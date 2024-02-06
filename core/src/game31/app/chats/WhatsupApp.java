package game31.app.chats;

import com.badlogic.gdx.utils.Array;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import game31.Globals;
import game31.Grid;
import game31.app.homescreen.Homescreen;
import game31.JsonSource;
import game31.Media;
import game31.ScriptState;
import game31.model.WhatsupModel;
import sengine.Entity;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 20/7/2016.
 */
public class WhatsupApp extends Entity<Grid> implements Homescreen.App {
    static final String TAG = "WhatsupApp";

    public static boolean clearStateOnEdit = true;

    // Time format
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;


    public final WhatsupContactsScreen contactsScreen;
//    public final WhatsupLabelsScreen labelsScreen;
    public final WhatsupThreadScreen threadScreen;
    private boolean isUserMessagesAvailable = false;


    public final Sprite icon;

    final Array<WhatsupContact> contacts = new Array<WhatsupContact>(WhatsupContact.class);

    // Sources
    String configFilename;
    private boolean isContactsRefreshing = true;

    public boolean pack(ScriptState state) {
        // Update read messages
        if(threadScreen.contact != null)
            threadScreen.contact.readMessages = threadScreen.currentMessages;
        for(WhatsupContact contact : contacts) {
            if(!contact.pack(state))
                return false;
        }
        contactsScreen.pack(state);
        return true;
    }

    public boolean isContactsRefreshing() {
        return isContactsRefreshing;
    }

    public String getCurrentDateText() {
        return dateFormat.format(new Date(Globals.grid.getSystemTime()));
    }

    public String getCurrentTimeText() {
        return timeFormat.format(new Date(Globals.grid.getSystemTime()));
    }

    public void refreshAvailableUserMessages(WhatsupContact contact) {
        contactsScreen.refreshAvailableUserMessages(contact);
        if(threadScreen.contact == contact)
            threadScreen.refreshAvailableUserMessages();
    }

    public void clearContact(WhatsupContact contact) {
        // Get row
        int index = contacts.indexOf(contact, true);
        if(index == -1)
            throw new RuntimeException("Unknown contact " + contact);
        contactsScreen.clear(index);
        if(threadScreen.contact == contact)
            threadScreen.clear();
    }

    public void informTyping(WhatsupContact contact, String origin) {
        // Get row
        int index = contacts.indexOf(contact, true);
        if(index == -1)
            throw new RuntimeException("Unknown contact " + contact);
        // Inform thread screen or contacts list
        if(threadScreen.contact == contact)
            threadScreen.informTyping(origin);
        else
            contactsScreen.informTyping(index, origin);
    }

    public void addMessage(WhatsupContact contact, String origin, String message, String timeText, String dateText, String trigger, float tTriggerTime) {
        // Format message
        message = Globals.grid.format(message);
        // Get row
        int index = contacts.indexOf(contact, true);
        if(index == -1)
            throw new RuntimeException("Unknown contact " + contact);
        // Inform contacts list
        if(message.startsWith(Globals.CORRUPTED_PREFIX)) {
            contactsScreen.addMessage(index, "Message corrupted", timeText, threadScreen.contact == contact);
        }
        else if(message.startsWith(Globals.BROWSER_PREFIX)) {
            if(origin.contentEquals(WhatsupContact.ORIGIN_USER))
                contactsScreen.addMessage(index, "You sent a link", timeText, threadScreen.contact == contact);
            else {
                String firstname = contact.name.split(" ", 2)[0];          // TODO: could be from 3rd person
                contactsScreen.addMessage(index, firstname + " sent a link", timeText, threadScreen.contact == contact);
            }
        }
        else if(message.startsWith(Globals.SECTION_PREFIX)) {
            String section = message.substring(Globals.SECTION_PREFIX.length());
            if(section.startsWith(Globals.GROUP_ADD_PREFIX))
                section = section.substring(Globals.GROUP_ADD_PREFIX.length());
            else if(section.startsWith(Globals.GROUP_REMOVE_PREFIX))
                section = section.substring(Globals.GROUP_REMOVE_PREFIX.length());
            contactsScreen.addMessage(index, section, timeText, threadScreen.contact == contact);
        }
        else if(message.startsWith(Globals.PHOTOROLL_PREFIX)) {
            String path = message.substring(Globals.PHOTOROLL_PREFIX.length());

            // Unlock and get this media
            Media media = Globals.grid.photoRollApp.unlock(path, false);

            if(origin.contentEquals(WhatsupContact.ORIGIN_USER)) {
                if(media.isVideo())
                    contactsScreen.addMessage(index, "You sent a video", timeText, threadScreen.contact == contact);
                else if(media.isAudio())
                    contactsScreen.addMessage(index, "You sent an audio recording", timeText, threadScreen.contact == contact);
                else
                    contactsScreen.addMessage(index, "You sent a photo", timeText, threadScreen.contact == contact);
            }
            else {
                String firstname = contact.name.split(" ", 2)[0];          // TODO: could be from 3rd person
                if(media.isVideo())
                    contactsScreen.addMessage(index, firstname + " sent a video", timeText, threadScreen.contact == contact);
                else if(media.isAudio())
                    contactsScreen.addMessage(index, firstname + " sent an audio recording", timeText, threadScreen.contact == contact);
                else
                    contactsScreen.addMessage(index, firstname + " sent a photo", timeText, threadScreen.contact == contact);
            }
        }
        else if (message.startsWith(Globals.INVITE_PREFIX)) {
            // Invite
            String[] data = message.substring(Globals.INVITE_PREFIX.length()).split(Globals.CHATS_SPLIT_TOKEN, 4);
            if (data.length != 4)
                throw new RuntimeException("Malformed invite message: " + message);
            String actualMessage = data[2];
            contactsScreen.addMessage(index, actualMessage, timeText, threadScreen.contact == contact);
        }
        else {
            String actualMessage;
            if(message.startsWith(Globals.CHATS_FONT_PREFIX))
                actualMessage = message.split(Globals.CHATS_SPLIT_TOKEN, 2)[1];      // TODO: add support in thread screen
            else
                actualMessage = message;
            contactsScreen.addMessage(index, actualMessage, timeText, threadScreen.contact == contact);
        }
        // Inform thread
        if(threadScreen.contact == contact) {
            if(origin.contentEquals(WhatsupContact.ORIGIN_USER))
                threadScreen.addUserMessage(message, timeText, dateText, trigger, tTriggerTime);
            else
                threadScreen.addSenderMessage(message, timeText, dateText, origin, trigger, tTriggerTime);          // TODO: could be from 3rd person
        }
    }

    public void refreshContacts() {
        contactsScreen.clear();
        threadScreen.clear();

        // Reset notifications
        isUserMessagesAvailable = false;
        Globals.grid.homescreen.setIndefiniteNotification(Globals.CONTEXT_APP_CHATS, false);

        for (int c = 0; c < contacts.size; c++) {
            WhatsupContact contact = contacts.items[c];
            contactsScreen.addContact(contact.name, contact.profilePicFilename);
            isContactsRefreshing = true;
            contact.refresh(this);
            isContactsRefreshing = false;
        }
    }

    public void refreshContact(WhatsupContact contact) {
        isContactsRefreshing = true;
        contact.refresh(this);
        isContactsRefreshing = false;
    }

    public WhatsupContact getContact(int index) {
        return contacts.items[index];
    }

    public WhatsupContact findContact(String name) {
        for(WhatsupContact contact : contacts) {
            if(contact.name.equals(name))
                return contact;
        }
        return null;
    }

    public void addContact(WhatsupModel.ContactModel model, final ScriptState state) {
        // Add Google source if available
        if(model.googleSheetUrl != null && Globals.allowChatsOnlineSources)
            Globals.addGoogleSource("assets", model.dialogue_tree_path, model.googleSheetUrl);

        WhatsupContact contact = new WhatsupContact(model, state);

        // Add
        contacts.add(contact);
    }

    public void load(final String filename, final ScriptState state) {
        configFilename = filename;

        // Clear
        contactsScreen.clear();
        threadScreen.clear();
        contacts.clear();


        // Load config
        JsonSource<WhatsupModel> configSource = new JsonSource<WhatsupModel>(filename, WhatsupModel.class);
        WhatsupModel config = configSource.load();

        // Load contacts
        for(int c = 0; c < config.contacts.length; c++)
            addContact(config.contacts[c], state);

        // Refresh contacts
        refreshContacts();

        // Contact positions
        contactsScreen.unpack(state);
    }

    public WhatsupApp() {
        // Date formats
        timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        DateFormatSymbols lowercaseAMPM = new DateFormatSymbols(Locale.US);
        lowercaseAMPM.setAmPmStrings(new String[] { "am", "pm" });
        timeFormat.setDateFormatSymbols(lowercaseAMPM);

        dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        timeFormat.setTimeZone(Globals.grid.timeZone);
        dateFormat.setTimeZone(Globals.grid.timeZone);

        contactsScreen = new WhatsupContactsScreen(this);
//        labelsScreen = new WhatsupLabelsScreen(this);
        threadScreen = new WhatsupThreadScreen(this);

        icon = Sprite.load("apps/chats/icon.png");        // TODO

        // Load
        load(Globals.chatsConfigFilename, Globals.grid.state);

    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        // Check if is user messages are available
        // Update all contacts
        boolean foundUserMessages = false;
        for(int c = 0; c < contacts.size; c++) {
            WhatsupContact contact = contacts.items[c];
            contact.update(this);
            if(contact.tree.isUserMessagesAvailable())
                foundUserMessages = true;

        }
        if(isUserMessagesAvailable != foundUserMessages) {
            isUserMessagesAvailable = foundUserMessages;
            v.homescreen.setIndefiniteNotification(Globals.CONTEXT_APP_CHATS, isUserMessagesAvailable);
        }
    }

    @Override
    public Entity<?> open() {
        return contactsScreen;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        homescreen.clearNotifications(Globals.CONTEXT_APP_CHATS);
        int unread = contactsScreen.countTotalUnread();
        if(unread > 0)
            homescreen.addNotification(Globals.CONTEXT_APP_CHATS, unread);
    }
}
