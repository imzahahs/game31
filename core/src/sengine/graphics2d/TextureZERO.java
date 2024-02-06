package sengine.graphics2d;

import sengine.mass.MassSerializable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TextureZERO implements MassSerializable {
	static final String TAG = "TextureZERO";
	
	static final Pixmap emptyPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

	final Pixmap source;
	// Current zero 
	Texture zero = null;
	
	public TextureZERO() {
		this(emptyPixmap);
	}
	
	@MassConstructor
	public TextureZERO(Pixmap source) {
		this.source = source;
	}
	@Override
	public Object[] mass() {
		return new Object[] { source };
	}

	
	public boolean bind(Texture real, int unit, Texture.TextureFilter minFilter, Texture.TextureFilter magFilter, Texture.TextureWrap uWrap, Texture.TextureWrap vWrap) {
        if(unit != 0)
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + unit);
        if(real == null) {
			// Real texture does not exist, bind lodZero
			if(zero == null) {
				// Lod zero wasnt loaded, load now
				zero = new Texture(source);
				// Do not use mipmap filters
				switch(minFilter) {
				case MipMap:
				case MipMapLinearLinear:
				case MipMapLinearNearest:
					minFilter = Texture.TextureFilter.Linear;
					break;
				case MipMapNearestLinear:
				case MipMapNearestNearest:
					minFilter = Texture.TextureFilter.Nearest;
				default:
					break;
				}
				switch(magFilter) {
				case MipMap:
				case MipMapLinearLinear:
				case MipMapLinearNearest:
					magFilter = Texture.TextureFilter.Linear;
					break;
				case MipMapNearestLinear:
				case MipMapNearestNearest:
					magFilter = Texture.TextureFilter.Nearest;
				default:
					break;
				}

				zero.setFilter(minFilter, magFilter);
				zero.setWrap(uWrap, vWrap);
			}
			zero.bind();
			return false;		// fully LOD zero texture
		}
		// Else can bind real texture
        real.bind();
        // Check if lodZero was used
		if(zero != null)
			unload();
		return true;		// real texture
	}
	
	public void unload() {
		if(zero == null)
			return;		// already released
		// Else reset
		zero.dispose();
		zero = null;
	}
	
	/*
	TODO Cannot finalize pixmap as a disposed pixmap could be referenced again when this object is ressurected by MassDecoder 
	@Override
	protected void finalize() throws Throwable {
		source.dispose();
		super.finalize();		// TODO: is it needed
	}
	*/
}
