package game31.app.phone;

import com.badlogic.gdx.utils.ObjectMap;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.phone.GBPhoneContactInfoScreen;
import sengine.Entity;
import sengine.graphics2d.Material;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.Sprite;
import sengine.materials.MaskMaterial;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 24/8/2016.
 */
public class PhoneContactInfoScreen extends Menu<Grid> implements OnClick<Grid> {

    public static class Internal {
        // Window
        public UIElement window;
        public ScrollableSurface surface;
        public ScreenBar bars;

        // Header
        public UIElement header;
        public StaticSprite profileView;
        public Material maskMaterial;
        public TextBox nameView;

        // Row
        public PatchedTextBox row;
        public TextBox rowTitle;
        public StaticSprite rowActionIcon;
        public Mesh phoneAttribIcon;
        public float rowYInterval;

    }

    private final PhoneApp app;

    // Sources
    private final Builder<Object> interfaceSource;
    private Internal s;

    // Working
    private float surfaceY;
    private Entity<?> transitionFrom;
    private PhoneContact contact;

    private final ObjectMap<UIElement, PhoneContact.Attribute> lookup = new ObjectMap<UIElement, PhoneContact.Attribute>();

    private String contextName;

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void clear() {
        s.surface.detachChilds();
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        lookup.clear();
    }

    public void show(PhoneContact contact) {
        clear();

        // Show
        this.contact = contact;

        // Header
        s.header.metrics.anchorWindowY = surfaceY / s.surface.getLength();
        s.header.viewport(s.surface).attach();

        s.nameView.text().text(contact.name);
        s.profileView.visual(new Sprite(new MaskMaterial(contact.bigProfilePic.getMaterial(), s.maskMaterial)));

        // Add header
        surfaceY -= s.header.getLength();
        surfaceY -= s.rowYInterval;

        // Phone
        // Number
        PatchedTextBox group = s.row.instantiate();
        group.text(contact.number);
        group.find(s.rowTitle).text().text(contact.device);
        group.enable();
        group.refresh();

        group.find(s.rowActionIcon).visual(s.phoneAttribIcon);

        PhoneContact.Attribute attrib = new PhoneContact.Attribute(contact.device, contact.number, PhoneContact.ATTRIB_PHONE);
        lookup.put(group, attrib);      // input

        // Position and add
        group.metrics.anchorWindowY = surfaceY / s.surface.getLength();
        group.viewport(s.surface).attach();
        surfaceY -= group.getLength();
        surfaceY -= s.rowYInterval;

        // Add attributes
        for(int c = 0; c < contact.attributes.length; c++) {
            attrib = contact.attributes[c];

            // Add attrib
            group = s.row.instantiate();
            group.text(attrib.value);
            group.find(s.rowTitle).text().text(attrib.attribute);
            group.refresh();

            // Check attrib action
            if(attrib.action != null && !attrib.action.isEmpty()) {
                if(attrib.action.equals(PhoneContact.ATTRIB_PHONE)) {
                    group.find(s.rowActionIcon).visual(s.phoneAttribIcon);
                    group.enable();
                    lookup.put(group, attrib);      // input
                }
                // TODO Support more
            }

            // Position and add
            group.metrics.anchorWindowY = surfaceY / s.surface.getLength();
            group.viewport(s.surface).attach();
            surfaceY -= group.getLength();
            surfaceY -= s.rowYInterval;
        }

        // Trigger
        if(contact.trigger != null && !contact.trigger.isEmpty())
            Globals.grid.eval(contact.name, contact.trigger);
    }

    public void open(Entity<?> transitionFrom, Entity<?> target) {
        this.transitionFrom = transitionFrom;
        ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(transitionFrom, this, target);
        transition.attach(target);
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        if(contact != null)
            show(contact);
        else
            clear();
    }


    public PhoneContactInfoScreen(PhoneApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<Object>(GBPhoneContactInfoScreen.class, this);
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

        contextName = null;
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        // Back button
        if(view == s.bars.backButton()) {
            ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, transitionFrom, v.screensGroup);
            transition.attach(v);
            transitionFrom = null;
            contact = null;
            contextName = null;
            return;
        }
        if(view == s.bars.homeButton()) {
            v.homescreen.transitionBack(this, v);
            return;
        }
        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        // Attributes
        PhoneContact.Attribute attribute = lookup.get(view);
        if(attribute != null) {
            // Run action
            if(attribute.action.equals(PhoneContact.ATTRIB_PHONE)) {
                // Calling
                app.callContact(this, contact);
            }
            return;
        }
    }

}
