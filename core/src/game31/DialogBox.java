package game31;

import game31.gb.GBDialogBox;
import sengine.graphics2d.Mesh;
import sengine.ui.Menu;
import sengine.ui.StaticSprite;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 20/8/2016.
 */
public class DialogBox extends Menu<Grid> {


    public interface InterfaceSource {
        Mesh buildDialogBg(float length);
    }

    // Interface source
    private final Builder<InterfaceSource> interfaceSource;


    private UIElement window;
    private StaticSprite dialogBg;
    private UIElement.Group dialogGroup;
    private UIElement inputBlocker;


    public UIElement window() {
        return window;
    }

    public StaticSprite dialogBg() {
        return dialogBg;
    }

    public UIElement.Group dialogGroup() {
        return dialogGroup;
    }

    public void setWindow(UIElement window, StaticSprite dialogBg, UIElement.Group dialogGroup, UIElement inputBlocker
    ) {
        // Activate
        if(this.window != null)
            this.window.detach();
        window.viewport(viewport);
        this.window = window;
        this.dialogBg = dialogBg;
        this.dialogGroup = dialogGroup;
        this.inputBlocker = inputBlocker;

        elements.clear();
        elements.add(inputBlocker);
        elements.add(dialogBg);

        clear();
    }

    public void clear() {
        dialogGroup.detachChilds();
    }

    public void prepare(UIElement content) {
        // clear();

        // Link to group
        dialogGroup.detachChilds();
        content.viewport(dialogGroup).attach();
        dialogGroup.autoLength();

        // Prepare dialog box
        Mesh bgMesh = interfaceSource.build().buildDialogBg(dialogGroup.getLength());
        prepareBackground(bgMesh);
        dialogBg.visual(bgMesh);
    }

    protected void prepareBackground(Mesh bgMesh) {
        // nothing
    }

    public void show() {
        window.attach();
    }

    public DialogBox() {
        // Initialize
        interfaceSource = new Builder<InterfaceSource>(GBDialogBox.class, this);
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
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if((inputType & INPUT_TOUCH) == 0)
            return false;
        // Absorb all
        return true;
    }
}
