package sengine.ui;

import sengine.Universe;

/**
 * Created by Azmi on 25/7/2016.
 */
public interface OnReleased<T extends Universe> {
    void onReleased(T v, UIElement<?> view, float x, float y, int button);
}
