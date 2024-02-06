package sengine.graphics2d.texturefile;

import java.util.Arrays;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import sengine.File;
import sengine.Sys;
import sengine.mass.Mass;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

public class TextureFile extends Mass {
	static final String TAG = "TextureFile";
	
	public static final String VERSION = "TextureFile";

	static TextureFormat<?>[] formats = null;
	
	public static void setFormats(Class<?>[] imageDataTypes, TextureFormat<?>[] formats) {
		registerSerializers(imageDataTypes, formats);
		TextureFile.formats = formats;
	}
	
	public static interface TextureFormatData {
		public boolean load(Texture texture);
		public void release();
	}
	
	public static interface TextureFormat<T extends TextureFormatData> extends Serializer<T> {
		public TextureFormatData convert(Pixmap[] levels, float compressQuality);
		public boolean isSupported();
		public boolean isFinal();
	}
	
	// Indices
	int[][] indices = null;
	
	public int getNumImages() {
		return indices.length;
	}
	
	public TextureFormatData getImageData(int index) {
		// Get a supported image
		int[] formats = indices[index];
		int bestIdx = -1;
		for(int f = 0; f < formats.length; f++) {
			int idx = formats[f];
			TextureFormat<?> format = (TextureFormat<?>) getSerializer(getType(idx));
			if(format.isSupported()) {
				bestIdx = idx;
				break;
			}
			else if(format.isFinal())
				bestIdx = idx;
		}
		if(bestIdx != -1)
			return get(bestIdx, true);

		throw new RuntimeException("All " + formats.length + " formats are not supported in this platform");
	}
	
	@Override
	public void clear() {
		super.clear();
		
		indices = null;
	}
	
	public void load(Input s) {
		load(s, VERSION);
	}
	
	public void save(Output s, Pixmap[][] images, float compressQuality) {
		if(formats == null || formats.length == 0)
			throw new IllegalStateException("Formats must be set first using setFormats()");
		clear();
		indices = new int[images.length][];
		int[] buffer = new int[formats.length];
		for(int i = 0; i < images.length; i++) {
			int formatsUsed = 0;
			for(int f = 0; f < formats.length; f++) {
				TextureFormat<?> format = formats[f];
				try {
					TextureFormatData data = format.convert(images[i], compressQuality);
					if(data == null)
						continue;		// format cannot compress this image
					buffer[formatsUsed] = add(data);
					formatsUsed++;
				} catch(Throwable e) {
					Sys.error(TAG, "Format " + format + " failed for image " + images[i][0], e);
					continue;
				}
				// Successfully converted to specified format, continue if format is not final
				if(format.isFinal())
					break;
			}
			if(formatsUsed == 0)
				throw new RuntimeException("All formats failed to convert image " + images[i][0]);
			indices[i] = Arrays.copyOf(buffer, formatsUsed);
		}
		save(s, VERSION);
	}
	
	public void load(String filename) {
		Sys.info(TAG, "Loading texture: " + filename);
		Input input = new Input(File.open(filename).read());
		load(input);
		input.close();
	}
	
	public void save(String filename, Pixmap[][] images, float compressQuality) {
		Sys.info(TAG, "Saving texture: " + filename);
		Output output = new Output(File.openCache(filename, false).write(false));
		save(output, images, compressQuality);
		output.close();
	}
	
	@Override
	protected void readFormatData(Input s) {
		indices = new int[s.readInt()][];
		for(int l = 0; l < indices.length; l++) {
			indices[l] = new int[s.readInt()];
			for(int i = 0; i < indices[l].length; i++)
				indices[l][i] = s.readInt();
		}
	}
	
	@Override
	protected void writeFormatData(Output s) {
		s.writeInt(indices.length);
		for(int l = 0; l < indices.length; l++) {
			s.writeInt(indices[l].length);
			for(int i = 0; i < indices[l].length; i++)
				s.writeInt(indices[l][i]);
		}
	}
}
