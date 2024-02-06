package sengine.graphics2d;

import sengine.GarbageCollector;
import sengine.Sys;
import sengine.graphics2d.texturefile.TextureLoader;
import sengine.mass.MassSerializable;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class StreamedTexture implements GarbageCollector.Collectible, MassSerializable {
	static final String TAG = "StreamedTexture";
	
	public final String filename;
	public final TextureFilter[] minFilter;
	public final TextureFilter[] magFilter;
	public final TextureWrap[] uWrap;
	public final TextureWrap[] vWrap;
	public final Pixmap[] previewTexture;
	public final boolean isStreamed;
	public final float tGarbageTime;
	final Texture[] textures;
	
	
	// Loader
	float tLastUsed = -1;
	TextureLoader loader = null;
	
	
	@MassConstructor
	public StreamedTexture(String filename, TextureFilter[] minFilter, TextureFilter[] magFilter, TextureWrap[] uWrap, TextureWrap[] vWrap, Pixmap[] previewTexture, boolean isStreamed, float tGarbageTime) {
		this.filename = filename;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
		this.previewTexture = previewTexture;
		this.isStreamed = isStreamed;
		this.tGarbageTime = tGarbageTime;
		this.textures = new Texture[previewTexture.length];
	}
	@Override
	public Object[] mass() {
		return new Object[] { filename, minFilter, magFilter, uWrap, vWrap, previewTexture, isStreamed, tGarbageTime };
	}
	
	public void bind(int index, int unit) {
		tLastUsed = Sys.getTime();
		// Check if texture is already loaded
		if(textures[index] != null) {
			textures[index].bind(unit);
			// Return if loaded texture is not previewTexture
			if(loader == null)
				return;
		}
		else if(loader == null) {
			// Create loader if not yet
			loader = new TextureLoader(filename);
			if(!isStreamed)
				loader.finish();
			else {
				loader.start();
				// Load preview textures
				for(int c = 0; c < textures.length; c++) {
					TextureFilter previewTextureMinFilter = minFilter[c];
					TextureFilter previewTextureMagFilter = magFilter[c];
					// Do not use mipmap filters for min textures (since min textures are not mipmapped)
					switch(previewTextureMinFilter) {
					case MipMap:
					case MipMapLinearLinear:
					case MipMapLinearNearest:
						previewTextureMinFilter = TextureFilter.Linear;
						break;
					case MipMapNearestLinear:
					case MipMapNearestNearest:
						previewTextureMinFilter = TextureFilter.Nearest;
					default:
						break;
					}
					switch(previewTextureMagFilter) {
					case MipMap:
					case MipMapLinearLinear:
					case MipMapLinearNearest:
						previewTextureMagFilter = TextureFilter.Linear;
						break;
					case MipMapNearestLinear:
					case MipMapNearestNearest:
						previewTextureMagFilter = TextureFilter.Nearest;
					default:
						break;
					}
					textures[c] = new Texture(previewTexture[c]);
					textures[c].setFilter(previewTextureMinFilter, previewTextureMagFilter);
					textures[c].setWrap(uWrap[c], vWrap[c]);
				}
			}
			// Require garbage collection
			GarbageCollector.add(this);
		}
		// Check loader
		if(loader.isComplete()) {
			Texture[] foundTextures = loader.get();
			int numTextures = foundTextures.length;
			// Validate number of textures found
			if(numTextures != previewTexture.length) {
				String errorMessage = "Unexpected number of textures in " + filename + " found: " + foundTextures.length + " expected: " + previewTexture.length;
				if(numTextures < previewTexture.length) {
					numTextures = previewTexture.length;
					if(!isStreamed)
						throw new RuntimeException(errorMessage);			// No backup textures available if !isStreamed, can't recover from this
				}
				Sys.error(TAG, errorMessage);
			}
			for(int c = 0; c < numTextures; c++) {
				// Release existing texture
				if(textures[c] != null)
					textures[c].dispose();
				textures[c] = foundTextures[c];
				textures[c].setFilter(minFilter[c], magFilter[c]);
				textures[c].setWrap(uWrap[c], vWrap[c]);
			}
			loader = null;
		}
		// Bind texture
		textures[index].bind(unit);
	}

	@Override
	public boolean performGC(boolean forced) {
		float elapsed = Sys.getTime() - tLastUsed; 
		if(elapsed < tGarbageTime && !forced)
			return false;		// no GC
		// Else GC now
		if(loader != null) {
			loader.release();
			loader = null;
		}
		// Release all textures
		for(int c = 0; c < textures.length; c++) {
			if(textures[c] != null) {
				textures[c].dispose();
				textures[c] = null;
			}
		}
		return true;
	}
}
