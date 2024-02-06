package game31.app.phone;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import game31.Game;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import game31.Globals;
import game31.Grid;
import game31.JsonSource;
import game31.Keyboard;
import game31.ScriptState;
import game31.app.homescreen.Homescreen;
import game31.model.PhoneAppModel;
import game31.renderer.SaraRenderer;
import sengine.Entity;
import sengine.Sys;
import sengine.graphics2d.Font;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;
import sengine.materials.ColoredMaterial;
import sengine.utils.Console;

/**
 * Created by Azmi on 24/8/2016.
 */
public class PhoneApp extends Entity<Grid> implements Homescreen.App {

    public static final String STATE_PHONE_RECENTS = "/recents";

    private final SimpleDateFormat timeFormat;

    public final PhoneDialerScreen dialerScreen;
    public final PhoneRecentScreen recentScreen;
    public final PhoneFavScreen favScreen;
    public final PhoneContactsScreen contactsScreen;
    public final PhoneContactInfoScreen contactInfoScreen;
    public final PhoneCallScreen callScreen;

    // Current
    public final Array<PhoneContact> contacts = new Array<PhoneContact>(PhoneContact.class);
    public final ObjectMap<String, PhoneContact> lookup = new ObjectMap<String, PhoneContact>();
    public final ObjectMap<String, PhoneContact> lookupNumber = new ObjectMap<String, PhoneContact>();
    public final ObjectSet<String> emergencyNumbers = new ObjectSet<String>();

    public final Array<PhoneAppModel.PhoneRecentModel> recents = new Array<PhoneAppModel.PhoneRecentModel>(PhoneAppModel.PhoneRecentModel.class);

    // Sources
    private JsonSource<PhoneAppModel> configSource;
    private String filename;
    private String ringtonePath = null;

    public String ringtonePath() {
        return ringtonePath;
    }

    public void informUserInputAvailable(PhoneContact contact) {
        // TODO
    }

    public void informHasMessage(PhoneContact contact) {

    }

    public String getCurrentTimeText() {
        return timeFormat.format(new Date(Globals.grid.getSystemTime()));
    }

    public void handleCall(String tag, final boolean canDecline, final boolean canCancel, final Runnable onAccepted, final Runnable onComplete, final Runnable onCanceled, final Runnable onDeclined) {
        final Grid v = Globals.grid;

        Globals.grid.state.set(tag, true);

        v.addTrigger(Globals.TRIGGER_PHONECALL_ACCEPTED, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                if(onAccepted != null)
                    onAccepted.run();
                return true;
            }
        });
        v.addTrigger(Globals.TRIGGER_PHONECALL_DECLINED, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                if(onDeclined != null)
                    onDeclined.run();

                if(canDecline) {
                    v.removeTrigger(Globals.TRIGGER_PHONECALL_ACCEPTED);
                    v.removeTrigger(Globals.TRIGGER_PHONECALL_DECLINED);
                    v.removeTrigger(Globals.TRIGGER_PHONECALL_CANCELLED);
                    v.removeTrigger(Globals.TRIGGER_PHONECALL_ENDED);
                    return true;
                }
                return false;
            }
        });
        v.addTrigger(Globals.TRIGGER_PHONECALL_CANCELLED, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                if(onCanceled != null)
                    onCanceled.run();

                if(canCancel) {
                    v.removeTrigger(Globals.TRIGGER_PHONECALL_ACCEPTED);
                    v.removeTrigger(Globals.TRIGGER_PHONECALL_DECLINED);
                    v.removeTrigger(Globals.TRIGGER_PHONECALL_CANCELLED);
                    v.removeTrigger(Globals.TRIGGER_PHONECALL_ENDED);
                    return true;
                }
                return false;
            }
        });
        v.addTrigger(Globals.TRIGGER_PHONECALL_ENDED, new Grid.Trigger() {
            @Override
            public boolean trigger(String name) {
                if(onComplete != null)
                    onComplete.run();

                v.removeTrigger(Globals.TRIGGER_PHONECALL_ACCEPTED);
                v.removeTrigger(Globals.TRIGGER_PHONECALL_DECLINED);
                v.removeTrigger(Globals.TRIGGER_PHONECALL_CANCELLED);
                v.removeTrigger(Globals.TRIGGER_PHONECALL_ENDED);
                return true;
            }
        });

        // TODO: what if command didnt create any calls
    }

    public PhoneApp() {
        // Date formats
        timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        DateFormatSymbols lowercaseAMPM = new DateFormatSymbols(Locale.US);
        lowercaseAMPM.setAmPmStrings(new String[] { "am", "pm" });
        timeFormat.setDateFormatSymbols(lowercaseAMPM);
        timeFormat.setTimeZone(Globals.grid.timeZone);

        this.dialerScreen = new PhoneDialerScreen(this);
        this.recentScreen = new PhoneRecentScreen(this);
        this.favScreen = new PhoneFavScreen(this);
        this.contactsScreen = new PhoneContactsScreen(this);
        this.contactInfoScreen = new PhoneContactInfoScreen(this);
        this.callScreen = new PhoneCallScreen(this);

        load(Globals.phoneConfigFilename);
    }

    public PhoneContact find(String name) {
        return lookup.get(name);
    }

    public void callNumber(Entity<?> transitionFrom, String number) {
        // Resolve number to contact
        String numberWithoutWhitespace = number.replaceAll("\\s+","");
        // Lookup emergency numbers
        if(emergencyNumbers.contains(numberWithoutWhitespace))
            numberWithoutWhitespace = "911";            // Calling an emergency number

        // Cheats
        if(numberWithoutWhitespace.equals("575953515001")) {
            Sys.error("CHEATS", "Enabled fast mode");
            Globals.tChatTimingMultiplier = 0.1f;
            Globals.tKeyboardAnimationSpeedMultiplier = 0.1f;
            Globals.grid.keyboard.detach();
            Globals.grid.keyboard = new Keyboard();
            number = "FAST MODE ENABLED";
        }
        else if(numberWithoutWhitespace.equals("575953515002")) {
            Sys.error("CHEATS", "Console mode");
            if(Console.console == null) {
                // Default console
                Console.interpreterDefaults = Game.configConsoleDefaults;
                Font consoleFont = new Font("inconsolata.otf", 32);

                Sprite bgMat = new Sprite(Globals.LENGTH, new ColoredMaterial());
                ColorAttribute.of(bgMat).set(0, 0, 0, 0.75f);

                // Console
                Console.console = new Console(bgMat, consoleFont, Globals.consoleChars, SaraRenderer.TARGET_CONSOLE);
                Console.console.showPreview(true);
                Console.console.attach(Globals.grid);
            }
            number = "CONSOLE ENABLED";
        }
        else if(numberWithoutWhitespace.equals("575953515003")) {
            Sys.error("CHEATS", "Enabled skip decodes");
            Globals.autoresolveRestoreScreens = true;
            number = "SKIP DECODES ENABLED";
        }

        PhoneContact contact = lookupNumber.get(numberWithoutWhitespace);
        if(contact != null)
            callScreen.showCalling(contact.bigProfilePic, contact.name, contact);
        else
            callScreen.showCalling(null, number, null);
        callScreen.open(transitionFrom, Globals.grid.screensGroup);
    }

    public void callContact(Entity<?> transitionFrom, PhoneContact contact) {
        callScreen.showCalling(contact.bigProfilePic, contact.name, contact);
        callScreen.open(transitionFrom, Globals.grid.screensGroup);
    }


    public boolean pack(ScriptState state) {
        for(PhoneContact contact : contacts) {
            if(!contact.pack(state))
                return false;
        }
        // Pack recents
        Globals.grid.state.set(filename + STATE_PHONE_RECENTS, recents.toArray());
        return true;
    }

    public void load(final String filename) {
        this.filename = filename;

        // Clear
        contacts.clear();
        lookup.clear();
        lookupNumber.clear();
        recents.clear();
        emergencyNumbers.clear();

        // Load config
        if(configSource != null)
            configSource.stop();
        configSource = new JsonSource<PhoneAppModel>(filename, PhoneAppModel.class);
        configSource.listener(new JsonSource.OnChangeListener<PhoneAppModel>() {
            @Override
            public void onChangeDetected(JsonSource<PhoneAppModel> source) {
                // On change, just reload this file
                load(filename);
            }
        });
        configSource.start();

        PhoneAppModel config = configSource.load();

        emergencyNumbers.addAll(config.emergency_numbers);
        ringtonePath = config.default_ringtone;

        // Convert
        for(int c = 0; c < config.contacts.length; c++) {
            PhoneContact contact = new PhoneContact(config.contacts[c]);
            contacts.add(contact);
            lookup.put(contact.name, contact);
            String number = contact.number.replaceAll("\\s+","");
            lookupNumber.put(number, contact);
        }

        // Config favourites
        favScreen.show(config.favourites);

        // Recent calls
        PhoneAppModel.PhoneRecentModel[] savedRecents = Globals.grid.state.get(filename + STATE_PHONE_RECENTS, null);
        if(savedRecents != null)
            recents.addAll(savedRecents);           // from save
        else
            recents.addAll(config.recents);         // from config

        // Update
        recentScreen.refresh(recents);

        // Config contacts
        contactsScreen.refresh();
    }

    void addRecentCall(String name, String type) {
        // Time
        String time = getCurrentTimeText();
        recents.add(new PhoneAppModel.PhoneRecentModel(name, time, type));
        recentScreen.refresh(recents);
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        // Update all contacts
        PhoneContact callingContact = null;
        for(int c = 0; c < contacts.size; c++) {
            PhoneContact contact = contacts.items[c];
            contact.update(this);
            if(callingContact == null && contact.message() != null)
                callingContact = contact;
        }
        // If there is a contact making a call, check if call screen is available
        if(callingContact != null && callScreen.contact() == null) {
            // Receive a call
            callScreen.showReceiving(callingContact.bigProfilePic, callingContact.name, callingContact.device, callingContact);
            callScreen.openAbrupt(Globals.grid.screensGroup, Globals.grid.screensGroupContainer);
        }
    }

    @Override
    public Entity<?> open() {
        // Analytics
        Game.analyticsView(Globals.ANALYTICS_CONTENT_TYPE_CALLS);

        return dialerScreen;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing
    }
}
