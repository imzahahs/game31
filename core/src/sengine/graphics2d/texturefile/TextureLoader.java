package sengine.graphics2d.texturefile;

import com.badlogic.gdx.graphics.Texture;

import sengine.File;
import sengine.Processor;
import sengine.graphics2d.TextureUtils;

public class TextureLoader extends Processor.Task {
	static final String TAG = "TextureLoader";

	public static boolean synchronizedIO = false;


	public final String filename;
	// Loader
	TextureFile.TextureFormatData[] images = null;
	Texture[] textures = null;
	int loaded = 0;
	// Current state
	boolean released = false;
	
	public TextureLoader(String filename) {
		this.filename = filename;
		
		async = true;
		sync = true;
		ignoreException = true;
	}
	
	public Texture[] get() {
		if(isComplete())
			return textures;
		return null;
	}
	
	public void release() {
		released = true;
		// If loading has completed, release now
		if(isComplete())
			unsafeRelease();
	}

	@Override
	protected void processAsync() {
		// Load texture file
		ignoreException = true;		// Ignore exceptions for texture loading
		TextureFile reader = new TextureFile();
		if(synchronizedIO) {
		    synchronized (File.class) {
                reader.load(filename);
            }
        }
        else
            reader.load(filename);
        int numImages = reader.getNumImages();
        images = new TextureFile.TextureFormatData[numImages];
        for (int c = 0; c < images.length; c++)
            images[c] = reader.getImageData(c);
        textures = new Texture[images.length];
        ignoreException = false;    // Should not ignore exceptions for GL texture loading
	}
	
	private void unsafeRelease() {
		// Releases jet and textures
		if(textures != null) {
			for(int c = 0; c < textures.length; c++) {
				if(textures[c] != null) {
					textures[c].dispose();
					textures[c] = null;
				}
			}
		}
		if(images != null) {
			for(int c = 0; c < images.length; c++) {
				if(images[c] != null) {
					images[c].release();
					images[c] = null;
				}
			}
		}
	}

	@Override
	protected boolean completeSync() {
		if(released) {
			// Textures were released
			unsafeRelease();
			return true;		// done
		}
		// Create texture first it not yet created
		if(textures[loaded] == null) {
			Texture texture = new TextureUtils.ManualTexture();
			texture.setWrap(texture.getUWrap(), texture.getVWrap());
			textures[loaded] = texture;
		}
		if(images[loaded].load(textures[loaded])) {
			images[loaded] = null;
			loaded++;
		}
		return loaded == images.length;
	}
	
	@Override
	public String toString() {
		return TAG + "#" + filename;
	}
}
