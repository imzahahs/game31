package game31.app.phone;

import com.badlogic.gdx.utils.ObjectMap;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.phone.GBPhoneFavScreen;
import sengine.Entity;
import sengine.Sys;
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
public class PhoneFavScreen extends Menu<Grid> implements OnClick<Grid> {
    private static final String TAG = "PhoneFavScreen";

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
        public TextBox deviceView;
        public Clickable callButton;

    }


    private final PhoneApp app;

    // Sources
    private final Builder<Object> interfaceSource;
    private Internal s;

    // Working
    private float surfaceY;
    private final ObjectMap<UIElement, PhoneContact> lookupInfo = new ObjectMap<UIElement, PhoneContact>();
    private final ObjectMap<UIElement, PhoneContact> lookupCall = new ObjectMap<UIElement, PhoneContact>();
    private String[] names;

    public void clear() {
        s.surface.detachChilds();
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        lookupInfo.clear();
        lookupCall.clear();
        names = null;
    }

    public void refresh() {
        if(names != null)
            show(names);
        else
            clear();
    }

    public void show(String ... names) {
        // Clear
        clear();

        this.names = names;

        // Resolve each name and add
        for(String name : names) {
            // Resolve name
            PhoneContact contact = app.lookup.get(name);
            if(contact == null) {
                Sys.error(TAG, "Unable to resolve name " + name);
                continue;
            }

            // New row
            Clickable group = s.row.instantiate();

            // Profile
            group.find(s.profileView).visual(contact.profilePic);
            group.find(s.nameView).text().text(contact.name);
            group.find(s.deviceView).text().text(contact.device);

            // Input
            lookupInfo.put(group, contact);
            lookupCall.put(group.find(s.callButton), contact);

            // Position and attach
            group.metrics.anchorWindowY = surfaceY / s.surface.getLength();
            group.viewport(s.surface).attach();
            surfaceY -= group.getLength();
        }
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

        refresh();
    }

    public PhoneFavScreen(PhoneApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<Object>(GBPhoneFavScreen.class, this);
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

        // Call
        PhoneContact contact = lookupCall.get(view);
        if(contact != null) {
            // Call this contact
            app.callContact(this, contact);
            return;
        }

        // Info
        contact = lookupInfo.get(view);
        if(contact != null) {
            // Open info
            app.contactInfoScreen.show(contact);
            app.contactInfoScreen.open(this, v.screensGroup);
            return;
        }

    }

}
