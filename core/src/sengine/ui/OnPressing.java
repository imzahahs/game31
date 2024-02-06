package sengine.ui;

import sengine.Universe;

/**
 * Created by Azmi on 25/7/2016.
 */
public interface OnPressing<T extends Universe> {
    void onPressing(T v, UIElement<?> view, int button);
}
