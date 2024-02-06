package game31;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.video.VideoPlayer;

import sengine.File;
import sengine.graphics2d.TextureUtils;
import sengine.materials.VideoMaterial;

public class VideoMaterialProvider implements VideoMaterial.PlatformProvider {

    public static double maxDelayTolerance = 0.05;

    public static void init() {
        VideoMaterial.platform = new VideoMaterialProvider();
    }

    public static class VideoMaterialHandle implements VideoMaterial.PlatformHandle {

        private final String filename;

        private VideoPlayer player;
        private double uploadedPosition = -1;

        public VideoMaterialHandle(String filename) {
            this.filename = filename;

            player = new VideoPlayer(File.open(filename));
        }

        @Override
        public Texture upload(VideoMaterial material, Texture existing, float timestamp, boolean ensureLoaded) {
            timestamp += player.offset;     // TODO: hack to fix sync issues
            if(player.isSeekingBack(timestamp)) {
                // To support backwards seek, need to reload video
                player.dispose();
                player = new VideoPlayer(File.open(filename));
            }
            player.fetch(timestamp, ensureLoaded);
            Pixmap pixmap = player.getFetchedFrame();
            double position = player.getFetchedPosition();
            if(!ensureLoaded && (timestamp - position) > maxDelayTolerance) {
                player.fetch(timestamp, true);
                pixmap = player.getFetchedFrame();
                position = player.getFetchedPosition();
            }
            if(pixmap == null || position == uploadedPosition)
                return existing;        // Failed to load, or still loading, or already uploaded
            // Else check texture
            if(existing == null) {
                // Create texture
                existing = new TextureUtils.ManualTexture();
                existing.bind();
                Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(),
                        pixmap.getWidth(), pixmap.getHeight(), 0,
                        pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
                existing.setFilter(material.minFilter, material.magFilter);
                existing.setWrap(material.uWrap, material.vWrap);
            }
            else {
                // Update textures
                existing.bind();
                Gdx.gl.glTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0,
                        pixmap.getWidth(), pixmap.getHeight(),
                        pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
            }
            uploadedPosition = position;
            return existing;
        }

        @Override
        public Pixmap decode(float timestamp) {
            if(player.isSeekingBack(timestamp)) {
                // To support backwards seek, need to reload video
                player.dispose();
                player = new VideoPlayer(File.open(filename));
            }
            player.fetch(timestamp, true);
            return player.getFetchedFrame();
        }

        @Override
        public void dispose() {
            player.dispose();
            player = null;
        }
    }

    @Override
    public VideoMaterial.PlatformHandle open(String filename) {
        return new VideoMaterialHandle(filename);
    }

    @Override
    public VideoMaterial.Metadata inspect(String filename) {
        VideoMaterial.Metadata metadata = null;

        VideoPlayer player = new VideoPlayer(File.open(filename));
        player.fetch(Float.MAX_VALUE, true);
        metadata = new VideoMaterial.Metadata(player.getWidth(), player.getHeight(), (float) player.getNextPosition());
        player.dispose();

        return metadata;
    }
}
