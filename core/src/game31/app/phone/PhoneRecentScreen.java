package game31.app.phone;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;


import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.phone.GBPhoneRecentScreen;
import game31.model.PhoneAppModel;
import sengine.Entity;
import sengine.graphics2d.Mesh;
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
public class PhoneRecentScreen extends Menu<Grid> implements OnClick<Grid> {
    private static final String TAG = "PhoneRecentScreen";

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
        public TextBox timeView;
        public StaticSprite callTypeIcon;
        public Clickable callButton;
        public Mesh outgoingIconMesh;
        public UIElement.Metrics outgoingIconMetrics;
        public Mesh incomingIconMesh;
        public UIElement.Metrics incomingIconMetrics;
        public Mesh missedIconMesh;
        public UIElement.Metrics missedIconMetrics;
    }

    // Sources
    private final Builder<Object> interfaceSource;
    private Internal s;

    private final PhoneApp app;


    // Working
    private float surfaceY;
    private final ObjectMap<UIElement, PhoneContact> lookupInfo = new ObjectMap<UIElement, PhoneContact>();
    private final ObjectMap<UIElement, PhoneContact> lookupCall = new ObjectMap<UIElement, PhoneContact>();
    private final ObjectMap<UIElement, PhoneAppModel.PhoneRecentModel> lookupRecents = new ObjectMap<UIElement, PhoneAppModel.PhoneRecentModel>();

    private final Array<PhoneAppModel.PhoneRecentModel> calls = new Array<PhoneAppModel.PhoneRecentModel>(PhoneAppModel.PhoneRecentModel.class);
    private final Array<Clickable> rows = new Array<Clickable>(Clickable.class);


    public void clear() {
        s.surface.detachChilds();
        lookupInfo.clear();
        lookupCall.clear();
        lookupRecents.clear();
        calls.clear();
        rows.clear();
    }

    public void refresh() {
        if(calls.size > 0) {
            Array<PhoneAppModel.PhoneRecentModel> calls = new Array<PhoneAppModel.PhoneRecentModel>(PhoneAppModel.PhoneRecentModel.class);
            calls.addAll(this.calls);

            clear();

            for (PhoneAppModel.PhoneRecentModel call : calls)
                add(call);

            refreshPositions();
        }
        else
            clear();
    }

    public void refreshPositions() {
        s.surface.detachChilds();
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();

        // Add from last
        for(int c = rows.size - 1; c >= 0; c--) {
            // Position and attach
            Clickable row = rows.items[c];
            row.metrics.anchorY = surfaceY;
            row.viewport(s.surface).attach();
            surfaceY -= row.getLength();
        }

        s.surface.stop();
        s.surface.move(0, -1000);
    }


    public void add(PhoneAppModel.PhoneRecentModel call) {
        // Compare last call made
        Clickable row = s.row.instantiate();
        rows.add(row);
        calls.add(call);

        // Lookup contact
        PhoneContact contact = app.lookup.get(call.name);

        if(contact != null) {
            row.find(s.profileView).visual(contact.profilePic);
            row.find(s.nameView).text(contact.name);
            row.find(s.deviceView).text().text(contact.device);

            // Input
            lookupInfo.put(row, contact);
            lookupCall.put(row.find(s.callButton), contact);
        }
        else {
            row.find(s.nameView).text(call.name);

            lookupRecents.put(row, call);
            lookupRecents.put(row.find(s.callButton), call);
        }

        row.find(s.timeView).text().text(call.time);

        // Call type
        StaticSprite typeView = row.find(s.callTypeIcon);
        if(call.type.equals(PhoneAppModel.RECENT_INCOMING)) {
            typeView.visual(s.incomingIconMesh);
            typeView.metrics(s.incomingIconMetrics);
        }
        else if(call.type.equals(PhoneAppModel.RECENT_OUTGOING)) {
            typeView.visual(s.outgoingIconMesh);
            typeView.metrics(s.outgoingIconMetrics);
        }
        else { // if(recent.type.equals(PhoneAppModel.RECENT_MISSED)) {
            typeView.visual(s.missedIconMesh);
            typeView.metrics(s.missedIconMetrics);
        }
    }



    public void refresh(Array<PhoneAppModel.PhoneRecentModel> recents) {
        // Clear
        clear();

        // Resolve each name and add
        for(int c = 0; c < recents.size; c++)
            add(recents.items[c]);

        refreshPositions();
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

        // Refresh all
        refresh();
    }


    public PhoneRecentScreen(PhoneApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<Object>(GBPhoneRecentScreen.class, this);
        interfaceSource.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        // Move up
        s.surface.move(0, -1000);
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

        // Unknown contact call
        PhoneAppModel.PhoneRecentModel call = lookupRecents.get(view);
        if(call != null) {
            app.callNumber(this, call.name);
            return;
        }

        // Calls
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
