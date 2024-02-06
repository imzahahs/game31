package sengine.ui;

import sengine.Universe;

/**
 * Created by Azmi on 25/7/2016.
 */
public interface OnClick<T extends Universe> {
    void onClick(T v, UIElement<?> view, int button);
}
