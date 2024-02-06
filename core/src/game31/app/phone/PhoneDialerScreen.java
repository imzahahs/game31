package game31.app.phone;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.phone.GBPhoneDialerScreen;
import sengine.Entity;
import sengine.Sys;
import sengine.audio.Audio;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 24/8/2016.
 */
public class PhoneDialerScreen extends Menu<Grid> implements OnClick<Grid> {
    private final String TAG = "PhoneDialerScreen";

    public static class Internal {
        // Window
        public UIElement<?> window;
        public ScreenBar bars;

        public TextBox dialedView;
        public Clickable[] keyButtons;
        public Audio.Sound[] keySounds;
        public char[] keyCharacters;
        public int maxDialedLength;
        public Audio.Sound backSound;
        public Clickable voiceMailButton;
        public Clickable callButton;
        public Clickable eraseButton;

        public String voiceMailName;

        public Clickable tabFavourites;
        public Clickable tabRecents;
        public Clickable tabContacts;
        public Clickable tabDialer;
    }


    // App
    private final PhoneApp app;
    private Internal s;


    // Interface source
    private final Builder<Object> interfaceSource;

    // Current
    private String dialed = "";

    public void clear() {
        dialed = "";
        s.dialedView.text().text(dialed);
    }

    public void open(Entity<?> transitionFrom, Entity<?> target) {
        clear();
        ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(transitionFrom, this, target);
        transition.attach(target);
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
    }


    public PhoneDialerScreen(PhoneApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<Object>(GBPhoneDialerScreen.class, this);
        interfaceSource.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        // Clear
        clear();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        // Back button
        if(view == s.bars.backButton() || view == s.bars.homeButton()) {
            v.homescreen.transitionBack(this, v);
            return;
        }
        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        // Tabs
        if(view == s.tabFavourites) {
            app.favScreen.open(this, v.screensGroup);
            return;
        }
        if(view == s.tabRecents) {
            app.recentScreen.open(this, v.screensGroup);
            return;
        }
        if(view == s.tabContacts) {
            app.contactsScreen.open(this, v.screensGroup);
            return;
        }

        // Back
        if(view == s.eraseButton) {
            // Erasing a character
            s.backSound.play();
            if(dialed.length() > 0) {
                dialed = dialed.substring(0, dialed.length() - 1);
                s.dialedView.text().text(dialed);
            }
            return;
        }

        // Dial screen
        if(view == s.callButton) {
            // Calling
            if(dialed.length() > 0)
               app.callNumber(this, dialed);
            return;
        }

        // Voicemail
        if(view == s.voiceMailButton) {
            PhoneContact contact = app.lookup.get(s.voiceMailName);
            if(contact == null)
                Sys.error(TAG, "Unable to find voicemail " + s.voiceMailName);
            else
                app.callContact(this, contact);
            return;
        }

        // Keypad
        for(int c = 0; c < s.keyButtons.length; c++) {
            if(view == s.keyButtons[c]) {
                // Tapped a key
                // Sound
                s.keySounds[c].play();

                if(dialed.length() < s.maxDialedLength) {
                    // Append ch
                    dialed += s.keyCharacters[c];
                    s.dialedView.text().text(dialed);
                }

                return;
            }
        }

    }
}
