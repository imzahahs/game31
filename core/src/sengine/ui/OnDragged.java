package sengine.ui;

import sengine.Universe;

/**
 * Created by Azmi on 25/7/2016.
 */
public interface OnDragged<T extends Universe> {
    void onDragged(T v, UIElement<?> view, float x, float y, int button);
}
