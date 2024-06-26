package sengine.video;


import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import sengine.materials.VideoMaterial;

public interface PlatformHandle {
    Texture upload(VideoMaterial material, Texture existing, float timestamp, boolean ensureLoaded);
    Pixmap decode(float timestamp);
    void dispose();
}
