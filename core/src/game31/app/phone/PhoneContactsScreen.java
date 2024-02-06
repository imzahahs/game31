package game31.app.phone;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.phone.GBPhoneContactsScreen;
import sengine.Entity;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 24/8/2016.
 */
public class PhoneContactsScreen extends Menu<Grid> implements OnClick<Grid> {

    public static final String ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String MISC_GROUP = "#";


    public static class Internal {
        // Window
        public UIElement window;
        public ScrollableSurface surface;
        public ScreenBar bars;

        public Clickable tabFavourites;
        public Clickable tabRecents;
        public Clickable tabContacts;
        public Clickable tabDialer;

        // Row
        public Clickable row;
        public StaticSprite profileView;
        public TextBox nameView;

        // Row header
        public StaticSprite header;
        public TextBox headerTextView;
    }

    private final PhoneApp app;

    // Sources
    private final Builder<Object> interfaceSource;
    private Internal s;


    // Working
    private float surfaceY;
    private final ObjectMap<UIElement, PhoneContact> lookup = new ObjectMap<UIElement, PhoneContact>();


    public void clear() {
        s.surface.detachChilds();
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        lookup.clear();
    }

    public void refresh() {
        // Clear
        clear();

        // Make a copy
        Array<PhoneContact> contacts = new Array<PhoneContact>(PhoneContact.class);
        contacts.addAll(app.contacts);

        // Remove all hidden
        for(int i = 0; i < contacts.size; i++) {
            PhoneContact contact = contacts.items[i];
            if(contact.isHidden || contact.name == null || contact.name.isEmpty())
                contacts.items[i] = null;       // hidden or invalid name
        }

        // Sort and add according to characters
        for(int c = 0, length = ALPHABETS.length(); c < length; c++) {
            char alphabet = ALPHABETS.charAt(c);

            // Add this header
            StaticSprite headerGroup = s.header.instantiate();
            headerGroup.find(s.headerTextView).text().text(Character.toString(alphabet));

            // Position and add
            headerGroup.metrics.anchorWindowY = surfaceY / s.surface.getLength();
            headerGroup.viewport(s.surface).attach();
            surfaceY -= headerGroup.getLength();

            alphabet = Character.toLowerCase(alphabet);

            // Add all for this alphabet
            for(int i = 0; i < contacts.size; i++) {
                PhoneContact contact = contacts.items[i];
                if(contact == null)
                    continue;       // already added
                // Else check if name starts with the specified character
                char ch = contact.name.charAt(0);
                if(Character.toLowerCase(ch) != alphabet)
                    continue;       // not current alphabet

                // Else add
                Clickable group = s.row.instantiate();
                group.find(s.profileView).visual(contact.profilePic);
                group.find(s.nameView).text().text(contact.name);

                // Position and attach
                group.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                group.viewport(s.surface).attach();
                surfaceY -= group.getLength();

                // Input
                lookup.put(group, contact);

                // Mark as added
                contacts.items[i] = null;
            }
        }

        // Add miscellaneous
        StaticSprite headerGroup = s.header.instantiate();
        headerGroup.find(s.headerTextView).text().text(MISC_GROUP);
        // Position and add
        headerGroup.metrics.anchorWindowY = surfaceY / s.surface.getLength();
        headerGroup.viewport(s.surface).attach();
        surfaceY -= headerGroup.getLength();

        // Add all remaining contacts
        for(int i = 0; i < contacts.size; i++) {
            PhoneContact contact = contacts.items[i];
            if(contact == null)
                continue;       // already added or invalid name
            // Else add
            Clickable group = s.row.instantiate();
            group.find(s.profileView).visual(contact.profilePic);
            group.find(s.nameView).text().text(contact.name);

            // Input
            lookup.put(group, contact);

            // Position and attach
            group.metrics.anchorWindowY = surfaceY / s.surface.getLength();
            group.viewport(s.surface).attach();
            surfaceY -= group.getLength();
        }

        // Done
    }

    public void open(Entity<?> transitionFrom, Entity<?> target) {
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

        if(app.contacts.size > 0)
            refresh();
        else
            clear();
    }

    public PhoneContactsScreen(PhoneApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<Object>(GBPhoneContactsScreen.class, this);
        interfaceSource.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();
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
        if(view == s.tabDialer) {
            app.dialerScreen.open(this, v.screensGroup);
            return;
        }


        // Contacts
        PhoneContact contact = lookup.get(view);
        if(contact != null) {
            // Open info
            app.contactInfoScreen.show(contact);
            app.contactInfoScreen.open(this, v.screensGroup);
            return;
        }

    }
}
