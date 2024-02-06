package sengine.graphics2d.texturefile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import sengine.Sys;
import sengine.mass.DefaultSerializer;
import sengine.mass.Mass;
import sengine.mass.MassException;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.BufferUtils;

public class PVRFormat implements TextureFile.TextureFormat<PVRFormat.PVRImageData> {
	static final String TAG = "PVRFormat";

	// OpenGL constants
	public static final String GL_IMG_texture_compression_pvrtc = "GL_IMG_texture_compression_pvrtc";
	public static final int GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG = 0x8C00;
	public static final int GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG = 0x8C01;
	public static final int GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
	public static final int GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = 0x8C03;

	static final int PVR_HEADER_SIZE = 13 * 4;					// 13 x 4-byte integers
	static final int PVR_HEADER_DATA_LENGTH_OFFSET = 5 * 4;		// data length is the 5th integer

	@DefaultSerializer(value = PVRFormat.class)
	public static class PVRImageData implements TextureFile.TextureFormatData {
		public final int[] widths;
		public final int[] heights;
		public final ByteBuffer[] fragments;
		// Current
		int l = 0;
		
		public PVRImageData(int[] widths, int[] heights, ByteBuffer[] fragments) {
			this.widths = widths;
			this.heights = heights;
			this.fragments = fragments;
		}

		@Override
		public boolean load(Texture texture) {
			if(l >= fragments.length)
				return true;		// already loaded, prevent ub
			// Bind texture
			Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
			// Load compressed data
			Gdx.gl.glCompressedTexImage2D(GL20.GL_TEXTURE_2D, 0, GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG, 
				widths[l], heights[l], 0, fragments[l].capacity(), fragments[l]);
			// Release data
			BufferUtils.disposeUnsafeByteBuffer(fragments[l]);
			fragments[l] = null;
			l++;
			return l >= fragments.length;
		}

		@Override
		public void release() {
			// Release remaining fragments
			for(l = 0; l < fragments.length; l++) {
				if(fragments[l] != null) {
					BufferUtils.disposeUnsafeByteBuffer(fragments[l]);
					fragments[l] = null;
				}
			}
		}
	}
	
	public final boolean isSupported;
	public final String pvrToolCommand; 		// = "C:/sengine/tools/pvrtextool.exe -fOGLPVRTC4 -yflip0 -iC:/sengine/tools/image.png -oC:/sengine/tools/image.pvr";
	public final String pvrToolInputPath;		// = "C:/sengine/tools/image.png";
	public final String pvrToolOutputPath; 		// = "C:/sengine/tools/image.pvr";
	public final String pvrExpectedOutput; 		// = "File C:\\sengine\\tools\\image.pvr written.";
	public final boolean isFinal;
	

	public PVRFormat(String pvrToolCommand, String pvrToolInputPath, String pvrToolOutputPath, String pvrExpectedOutput, boolean isFinal) {
		
		// Check support
		this.isSupported = Gdx.graphics.supportsExtension(GL_IMG_texture_compression_pvrtc);
		Sys.info(TAG, "Support for " + GL_IMG_texture_compression_pvrtc + ": " + isSupported);
		
		// PVRTexTool information
		this.pvrToolCommand = pvrToolCommand;
		this.pvrToolInputPath = pvrToolInputPath;
		this.pvrToolOutputPath = pvrToolOutputPath;
		this.pvrExpectedOutput = pvrExpectedOutput;
		
		this.isFinal = isFinal;
	}


	@Override
	public PVRImageData read(Mass m, Input s, Class<PVRImageData> type) {
		int numLevels = s.readInt();
		int[] widths = new int[numLevels];
		int[] heights = new int[numLevels];
		ByteBuffer[] fragments = new ByteBuffer[numLevels];
		
		try {
			for(int l = 0; l < numLevels; l++) {
				widths[l] = s.readInt();
				heights[l] = s.readInt();
				int size = s.readInt();
				ByteBuffer bytes = BufferUtils.newUnsafeByteBuffer(size);
				s.readBytes(bytes, size);
				bytes.position(0);
				fragments[l] = bytes;
			}
		} catch(Throwable e) {
			// Release buffers
			for(int l = 0; l < fragments.length; l++) {
				if(fragments[l] != null)
					BufferUtils.disposeUnsafeByteBuffer(fragments[l]);
			}
			throw new MassException("Failed to read PVRImageData", e);
		}
		
		return new PVRImageData(widths, heights, fragments);
	}


	@Override
	public void write(Mass m, Output s, PVRImageData o) {
		if(o.l > 0)
			throw new IllegalStateException("Cannot serialize partially loaded image data");
		s.writeInt(o.fragments.length);
		for(int l = 0; l < o.fragments.length; l++) {
			s.writeInt(o.widths[l]);
			s.writeInt(o.heights[l]);
			// Data
			ByteBuffer bytes = o.fragments[l];
			s.writeInt(bytes.capacity());
			bytes.position(0);
			s.writeBytes(bytes, bytes.capacity());
			bytes.position(0);
		}
	}

	@Override
	public PVRImageData convert(Pixmap[] levels, float compressQuality) {
		// Check if levels have alpha
		for(int l = 0; l < levels.length; l++) {
			Pixmap level = levels[l];
			// Validate format
			if(level.getFormat() != Pixmap.Format.RGB888 && level.getFormat() != Pixmap.Format.RGB565)
				return null;			// Do not convert images with alpha
		}
		
		int[] widths = new int[levels.length];
		int[] heights = new int[levels.length];
		ByteBuffer[] fragments = new ByteBuffer[levels.length];
		
		// PVR Header buffer
		byte[] header = new byte[PVR_HEADER_SIZE];
		
		try {
			// Convert all levels
			for(int l = 0; l < levels.length; l++) {
				Pixmap level = levels[l];
				
				widths[l] = level.getWidth();
				heights[l] = level.getHeight();

				// Try to convert this level
				try {
					// Write this image as pvrtools input image
					FileHandle f = Gdx.files.absolute(pvrToolInputPath);
					PixmapIO.writePNG(f, level);
					// Delete output image
					f = Gdx.files.absolute(pvrToolOutputPath);
					f.delete();
					// Convert
					Process pvrToolProcess = Runtime.getRuntime().exec(pvrToolCommand);
					BufferedReader r = new BufferedReader(new InputStreamReader(pvrToolProcess.getErrorStream()));
					String line = r.readLine();
					while(line != null) {
						if(!pvrExpectedOutput.equals(line))
							Sys.error(TAG, line);
						line = r.readLine();
					}
					pvrToolProcess.waitFor();
					// Read PVR file
					Input s = new Input(f.read());
					s.readBytes(header);
					// Get data length, header is encoded in little-endian order
					int n1 = (int)header[PVR_HEADER_DATA_LENGTH_OFFSET] & 0xff;
					int n2 = (int)header[PVR_HEADER_DATA_LENGTH_OFFSET + 1] & 0xff;
					int n3 = (int)header[PVR_HEADER_DATA_LENGTH_OFFSET + 2] & 0xff;
					int n4 = (int)header[PVR_HEADER_DATA_LENGTH_OFFSET + 3] & 0xff;
					int size = n1 | (n2 << 8) | (n3 << 16) | (n4 << 24); 
					// Read compressed PVRTC
					ByteBuffer bytes = BufferUtils.newUnsafeByteBuffer(size);
					s.readBytes(bytes, size);
					bytes.position(0);
					s.close();
					
					fragments[l] = bytes;
				} catch (Throwable e) {
					throw new RuntimeException("Conversion failed for level-" + l, e);
				}
			}
		} catch (Throwable e) {
			// Release buffers
			for(int l = 0; l < fragments.length; l++) {
				if(fragments[l] != null)
					BufferUtils.disposeUnsafeByteBuffer(fragments[l]);
			}
			throw new RuntimeException("Failed to convert to PVR format", e);
		}
		
		return new PVRImageData(widths, heights, fragments);
	}


	@Override
	public boolean isSupported() {
		return isSupported;
	}


	@Override
	public boolean isFinal() {
		return isFinal;			// There is no software decompressor for PVR, up to user implementation to declare final
	}

}
