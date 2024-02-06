package sengine.graphics2d.texturefile;

import java.nio.ByteBuffer;

import sengine.Sys;
import sengine.graphics2d.TextureUtils;
import sengine.graphics2d.texturefile.TextureFile.TextureFormatData;
import sengine.graphics2d.texturefile.TextureFile.TextureFormat;
import sengine.mass.DefaultSerializer;
import sengine.mass.Mass;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ETC1;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.utils.BufferUtils;

public class ETC1Format implements TextureFormat<ETC1Format.ETC1ImageData> {
	static final String TAG = "ETC1Format";
	
	// OpenGL constants
	public static final String GL_OES_compressed_ETC1_RGB8_texture = "GL_OES_compressed_ETC1_RGB8_texture";
	
	@DefaultSerializer(value = ETC1Format.class)
	public static class ETC1ImageData implements TextureFormatData {
		
		final ETC1Data[] data;
		final Pixmap.Format[] formats;
		final Pixmap[] fragments;
		// Current decompression
		int l = 0;
		
		public ETC1ImageData(ETC1Data[] data, Pixmap.Format[] formats) {
			this.data = data;
			this.formats = formats;
			
			if(!isSupported) {
				fragments = new Pixmap[data.length];
				for(int c = 0; c < data.length; c++)
					fragments[c] = ETC1.decodeImage(data[c], formats[c]);
			}
			else
				fragments = null;
		}

		@Override
		public boolean load(Texture texture) {
			if(l >= data.length)
				return true;		// already loaded, prevent ub
			if(!isSupported) {
				// Bind texture
				Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
				// Load a level
				Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
				Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, l, fragments[l].getGLInternalFormat(),
					fragments[l].getWidth(), fragments[l].getHeight(), 0, 
					fragments[l].getGLFormat(), fragments[l].getGLType(), fragments[l].getPixels());
                // Release
                fragments[l].dispose();
                fragments[l] = null;
			}
			else {
				// Bind texture
				Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
				// Load a level
				Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
				// Load compressed data
				Gdx.gl.glCompressedTexImage2D(GL20.GL_TEXTURE_2D, l, ETC1.ETC1_RGB8_OES,
						data[l].width, data[l].height, 0,
						data[l].compressedData.capacity(), data[l].compressedData);
				// Release data
				data[l].dispose();
				data[l] = null;
			}
			l++;
			return l >= data.length;
		}

		@Override
		public void release() {
			// Release remaining fragments
			for(l = 0; l < data.length; l++) {
				if(data[l] != null) {
					data[l].dispose();
					data[l] = null;
				}
			}
            if(fragments != null) {
                for(l = 0; l < fragments.length; l++) {
                    if(fragments[l] != null) {
                        fragments[l].dispose();
                        fragments[l] = null;
                    }
                }
            }
		}
	}

	
	static boolean isSupported;
	
	public ETC1Format() {
		isSupported = Gdx.graphics.supportsExtension(GL_OES_compressed_ETC1_RGB8_texture);
		Sys.info(TAG, "Support for " + GL_OES_compressed_ETC1_RGB8_texture + ": " + isSupported);
	}

	@Override
	public ETC1ImageData read(Mass m, Input s, Class<ETC1ImageData> type) {
		int levels = s.readInt();
		ETC1Data[] data = new ETC1Data[levels];
		Pixmap.Format[] formats = new Pixmap.Format[levels];
		
		try {
			for(int l = 0; l < levels; l++) {
				int width = s.readInt();
				int height = s.readInt();
				int size = ETC1.getCompressedDataSize(width, height);
				ByteBuffer bytes = BufferUtils.newUnsafeByteBuffer(size);
				s.readBytes(bytes, size);
				bytes.position(0);
				// Finished reading this level
				data[l] = new ETC1Data(width, height, bytes, 0);
				formats[l] = Pixmap.Format.values()[s.readInt()];
			}
		} catch(Throwable e) {
			for(int l = 0; l < data.length; l++) {
				if(data[l] != null)
					data[l].dispose();
			}
			throw new RuntimeException("Failed to read ETC1ImageData", e);
		}

		return new ETC1ImageData(data, formats);
	}

	@Override
	public void write(Mass m, Output s, ETC1ImageData o) {
		if(o.l > 0)
			throw new IllegalStateException("Cannot serialize partially loaded image data");
		s.writeInt(o.data.length);
		for(int l = 0; l < o.data.length; l++) {
			ETC1Data data = o.data[l];
			s.writeInt(data.width);
			s.writeInt(data.height);
			int size = ETC1.getCompressedDataSize(data.width, data.height);
			ByteBuffer bytes = data.compressedData;
			int position = bytes.position();
			bytes.position(0);
			s.writeBytes(bytes, size);
			bytes.position(position);
			s.writeInt(o.formats[l].ordinal());
		}
	}

	@Override
	public boolean isSupported() {
		return isSupported;
	}

	@Override
	public boolean isFinal() {
		return true;			// Can decode using software decoder as well
	}
	
	@Override
	public ETC1ImageData convert(Pixmap[] levels, float compressQuality) {
		// Encode all in ETC1
		ETC1Data[] data = new ETC1Data[levels.length];
		Pixmap.Format[] formats = new Pixmap.Format[levels.length];
		
		try {
			for(int l = 0; l < levels.length; l++) {
				Pixmap pixmap = levels[l];
				if(pixmap.getFormat() != Pixmap.Format.RGB888 && pixmap.getFormat() != Pixmap.Format.RGB565)
					return null;			// Cannot encode other than these formats
				formats[l] = pixmap.getFormat();
				Pixmap newPixmap = TextureUtils.validateDimensions(pixmap, 1f, -1, -1, true, false, true);
				if(newPixmap == null) {
					newPixmap = TextureUtils.duplicate(pixmap);
					newPixmap = TextureUtils.validateDimensions(newPixmap, 1f, -1, -1, true, false, false);
				}
				data[l] = ETC1.encodeImage(newPixmap);
				if(newPixmap != pixmap)
					newPixmap.dispose();
			}
		} catch(Throwable e) {
			for(int l = 0; l < data.length; l++) {
				if(data[l] != null)
					data[l].dispose();
			}
			throw new RuntimeException("Failed to convert to ETC1ImageData", e);
		}
		
		return new ETC1ImageData(data, formats);
	}
}