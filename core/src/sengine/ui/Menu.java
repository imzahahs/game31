package sengine.ui;

import com.badlogic.gdx.utils.Array;

import sengine.Entity;
import sengine.Universe;

public class Menu<V extends Universe> extends Entity<V> {

    // Identity
    public final UIElement.Viewport viewport;

    // Current handler
    public final Array<UIElement<?>> elements = new Array<UIElement<?>>(UIElement.class);
    boolean isDetaching = false;

    public boolean isDetaching() {
        return isDetaching;
    }

    public Menu() {
        // Prepare viewport
        this.viewport = new UIElement.Viewport();
        viewport.attach(this);

        // Enable input
        inputEnabled = true;
    }

    @Override
    public void attach(Entity<?> parent, int index) {
        if(isDetaching) {
            Entity.useStrictLinking = true;
            detach();
            super.attach(parent, index);
            Entity.useStrictLinking = false;
        }
        else
            super.attach(parent, index);
    }

    @Override
    protected void recreate(V v) {
        if(isDetaching) {
            for(int c = 0; c < elements.size; c++)
                elements.items[c].detach();
        }
        isDetaching = false;
        for(int c = 0; c < elements.size; c++)
            elements.items[c].attach();
    }

    @Override
    protected void render(V v, float r, float renderTime) {
        if(isDetaching) {
            for(int c = 0; c < elements.size; c++) {
                if(elements.items[c].isAttached())
                    return;
            }
            // Else all has detached
            detach();
        }
    }

    @Override
    protected void release(V v) {
        // Make sure detached
        for(int c = 0; c < elements.size; c++)
            elements.items[c].detach();
        if(isDetaching)
            detach();
        isDetaching = false;
    }

    public void detachWithAnim() {
        if(isDetaching)
            return;
        isDetaching = true;
        if(elements.size == 0)
            detach();
        else {
            for(int c = 0; c < elements.size; c++)
                elements.items[c].detachWithAnim();
        }
    }
}
