package sengine.graphics2d.texturefile;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import sengine.File;
import sengine.Sys;
import sengine.graphics2d.TextureUtils;
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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class FIFormat implements TextureFile.TextureFormat<FIFormat.FragmentedImageData> {
	static final String TAG = "FIFormat";

	public static int compressBufferSize = 64 * 1024;      // 64 kb
    public static int compressMinSize = 32 * 32;        // minimum size
	
	@DefaultSerializer(value = FIFormat.class)
	public static class FragmentedImageData implements TextureFile.TextureFormatData {
		public final int fragmentSize;
		public final int[] width;
		public final int[] height;
		// Fragments
		public final Pixmap[][] fragments;
		// Current streaming status
		int l = 0;
		int f = 0;
        float compressQuality;
		
		public FragmentedImageData(float compressQuality, int fragmentSize, int[] width, int[] height, Pixmap[][] fragments) {
			this.compressQuality = compressQuality;
			this.fragmentSize = fragmentSize;
			this.width = width;
			this.height = height;
			this.fragments = fragments;
		}

		public Pixmap[] reconstruct() {
			if(l != 0 || f != 0)
				throw new IllegalStateException("Can only reconstruct unused image data");
			Pixmap[] images = new Pixmap[fragments.length];
			while(true) {
				// Lookup fragment
				Pixmap pixmap = fragments[l][f];
				// Check if creating a new level
				if(f == 0) {
					// If level is smaller or equal to fragment size, just upload now
					if(width[l] <= fragmentSize && height[l] <= fragmentSize) {
						images[l] = TextureUtils.duplicate(pixmap);
						// Go to next fragment
						if(nextFragment())
							return images;			// Finished all
						// Else continue
						continue;
					}
					// Else need to create fragmented image
					images[l] = new Pixmap(width[l], height[l], pixmap.getFormat());
				}
				// Get position
				int columns = calculateNumFragments(fragmentSize, width[l]);
				int x = (f % columns) * fragmentSize;
				int y = (f / columns) * fragmentSize;
				// Draw image
				images[l].drawPixmap(pixmap, x, y);
				if(nextFragment())
					return images;
			}
		}

		boolean nextFragment() {
			if(l >= fragments.length)
				return true;	// prevent UB
			// Release fragment
			fragments[l][f].dispose();
			fragments[l][f] = null;
			// Go to next fragment
			f++;
			if(f < fragments[l].length)
				return false;
			// Else done this level, go to next
			f = 0;
			l++;
			if(l < fragments.length)
				return false;
			// Else done all levels
			return true;
		}

		static int d = 0;

		@Override
		public boolean load(Texture texture) {
			if(l >= fragments.length)
				return true;			// already loaded
			int bandwidthAvailable = fragmentSize * fragmentSize;
			// Bind texture
			Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
			while(true) {
				// Lookup fragment
				Pixmap pixmap = fragments[l][f];
				// Check if creating a new level
				if(f == 0) {
                    // Create level
					Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
					// If level is smaller or equal to fragment size, just upload now
					if(width[l] <= fragmentSize && height[l] <= fragmentSize) {
						Gdx.gl.glTexImage2D(
							GL20.GL_TEXTURE_2D, 
							l, 
							pixmap.getGLInternalFormat(), 
							width[l], 
							height[l], 
							0, 
							pixmap.getGLFormat(),
							pixmap.getGLType(), 
							pixmap.getPixels()
						);
						bandwidthAvailable -= width[l] * height[l];
						// Go to next fragment
						if(nextFragment())
							return true;		// uploaded all
						else if(bandwidthAvailable <= 0)
							return false;		// finished available quota
						// Else continue within the same load() order as loading levels lower than fragment size 
						// in multiple cycles is not worth it
						continue;
					}
					// Else need to create fragmented image
					Gdx.gl.glTexImage2D(
						GL20.GL_TEXTURE_2D, 
						l, 
						fragments[l][0].getGLInternalFormat(), 
						width[l], 
						height[l], 
						0, 
						fragments[l][0].getGLFormat(), // GL20.GL_RGBA, 
						fragments[l][0].getGLType(), 
						null
					);
				}
				// Get position
				int columns = calculateNumFragments(fragmentSize, width[l]);
				int x = (f % columns) * fragmentSize;
				int y = (f / columns) * fragmentSize;
				// Load texture
				Gdx.gl.glTexSubImage2D(
					GL20.GL_TEXTURE_2D, 
					l, 
					x, 
					y, 
					pixmap.getWidth(), 
					pixmap.getHeight(), 
					pixmap.getGLFormat(),
					pixmap.getGLType(), 
					pixmap.getPixels()
				);
				return nextFragment();
			}
		}

		@Override
		public void release() {
			// Release unused fragments
			while(!nextFragment());
		}
	}
	
	private static int calculateNumFragments(int fragmentSize, int pixels) {
		return (pixels / fragmentSize) + ((pixels % fragmentSize) != 0 ? 1 : 0);
	}


    private static BufferedImage pixmapToBufferedImage(Pixmap pixmap) {
        int w = pixmap.getWidth();
        int h = pixmap.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                //convert RGBA to RGB
                pixels[(y * w) + x] = pixmap.getPixel(x, y) >>> 8;
            }
        }

        img.setRGB(0, 0, w, h, pixels, 0, w);
        return img;
    }

    private static void compressJPEG(Pixmap pixmap, Output outputBuffer, float compressQuality) {
        try {
            // Convert to BufferedImage used by Java ImageIO
            BufferedImage bufferedImage = pixmapToBufferedImage(pixmap);
            // Save to JPEG
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputBuffer);
            // Configure writer
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam writeParams = writer.getDefaultWriteParam();
            writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParams.setCompressionQuality(compressQuality);
            // Write
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(bufferedImage, null, null), writeParams);
            // Flush
            writer.dispose();
            imageOutputStream.close();
            bufferedImage.flush();
        } catch (Throwable e) {
            Sys.error(TAG, "Unable to convert image to JPEG", e);
            outputBuffer.clear();
        }
    }


    public final int fragmentSize;

	public FIFormat(int fragmentSize) {
		this.fragmentSize = fragmentSize;
	}

	@Override
	public FragmentedImageData read(Mass m, Input s, Class<FragmentedImageData> type) {
		int fragmentSize = s.readInt();
		int numLevels = s.readInt();
		
		int[] widths = new int[numLevels];
		int[] heights = new int[numLevels];
		Pixmap[][] fragments = new Pixmap[numLevels][];

		try {
			for(int l = 0; l < numLevels; l++) {
				int width = widths[l] = s.readInt();
				int height = heights[l] = s.readInt();
				Pixmap.Format format = Pixmap.Format.values()[s.readByteUnsigned()];

                // Determine compression enabled
                boolean compressionAllowed = format == Pixmap.Format.RGB888 || format == Pixmap.Format.RGB565;

                int columns = calculateNumFragments(fragmentSize, width);
				int rows = calculateNumFragments(fragmentSize, height);
				int numFragments = columns * rows;
				
				fragments[l] = new Pixmap[numFragments];
				
				for(int f = 0; f < numFragments; f++) {
					// Determine fragment region
					int x = (f % columns) * fragmentSize;
					int y = (f / columns) * fragmentSize;
					int x2 = x + fragmentSize;
					int y2 = y + fragmentSize;
					if(x2 > width)
						x2 = width;
					if(y2 > height)
						y2 = height;
					int fragmentWidth = x2 - x;
					int fragmentHeight = y2 - y;

					// Read fragment

                    if(compressionAllowed) {
                        // Compression is allowed here, read size
                        int compressedSize = s.readInt();
                        if(compressedSize > 0) {
                            // Read directly from mass buffer
                            byte[] massBuffer = s.getBuffer();
                            int massOffset = s.position();
                            fragments[l][f] = new Pixmap(massBuffer, massOffset, compressedSize);
                            s.skip(compressedSize);
                            continue;
                        }
                        // Else not compressed
                    }

                    // Normal compression
                    Pixmap fragment = new Pixmap(fragmentWidth, fragmentHeight, format);
                    ByteBuffer bytes = fragment.getPixels();
					bytes.position(0);
					s.readBytes(bytes, bytes.capacity());
					bytes.position(0);

                    fragments[l][f] = fragment;
				}
			}
		} catch(Throwable e) {
			// Release fragments
			for(int l = 0; l < fragments.length; l++) {
				if(fragments[l] != null) {
					for(int f = 0; f < fragments[l].length; f++) {
						if(fragments[l][f] != null)
							fragments[l][f].dispose();
					}
				}
			}
			throw new MassException("Failed to read FragmentedImageData", e);
		}
		
		return new FragmentedImageData(1.0f, fragmentSize, widths, heights, fragments);
	}

	@Override
	public void write(Mass m, Output s, FragmentedImageData o) {
		if(o.l > 0 || o.f > 0)
			throw new IllegalStateException("Cannot serialize partially loaded image data");

        Output compressBuffer = null;       // allocate only when needed

        s.writeInt(o.fragmentSize);
		s.writeInt(o.fragments.length);

		for(int l = 0; l < o.fragments.length; l++) {
			Pixmap[] fragments = o.fragments[l];
			
			s.writeInt(o.width[l]);
			s.writeInt(o.height[l]);
            Pixmap.Format format = fragments[0].getFormat();
            s.writeByte(format.ordinal());

            // Determine compression enabled
            boolean compressionAllowed = format == Pixmap.Format.RGB888 || format == Pixmap.Format.RGB565;

            for(int f = 0; f < fragments.length; f++) {
				Pixmap fragment = fragments[f];

                // Check if can compress
                if(compressionAllowed) {
                    // Check if fragment meets minimum compression size
                    if((fragment.getWidth() * fragment.getHeight()) >= compressMinSize && o.compressQuality >= 0) {
                        // Compress now, allocate buffer if not yet and reset
                        if(compressBuffer == null)
                            compressBuffer = new Output(compressBufferSize);
                        compressBuffer.clear();
                        // Compress
                        compressJPEG(fragment, compressBuffer, o.compressQuality);
                        if(compressBuffer.position() > 0) {
                            // Success, save it
                            s.writeInt(compressBuffer.position());          // compressed size
                            s.write(compressBuffer.getBuffer(), 0, compressBuffer.position());
                            continue;       // continue next fragment
                        }
                    }
                    // Else compression failed or did not meet minimum size
                    s.writeInt(-1);
                }

                // Else normal compression
				ByteBuffer bytes = fragment.getPixels();
				bytes.position(0);
				s.writeBytes(bytes, bytes.capacity());
				bytes.position(0);
            }
		}
	}


	@Override
	public FragmentedImageData convert(Pixmap[] levels, float compressQuality) {
		int[] widths = new int[levels.length];
		int[] heights = new int[levels.length];
		Pixmap[][] fragments = new Pixmap[levels.length][];
		
		try {
			// Convert to fragments
			for(int l = 0; l < levels.length; l++) {
				Pixmap level = levels[l];
				int width = widths[l] = levels[l].getWidth();
				int height = heights[l] = levels[l].getHeight();
				int columns = calculateNumFragments(fragmentSize, width);
				int rows = calculateNumFragments(fragmentSize, height);
				int numFragments = columns * rows;
				fragments[l] = new Pixmap[numFragments];
				for(int f = 0; f < numFragments; f++) {
					// Determine fragment region
					int x = (f % columns) * fragmentSize;
					int y = (f / columns) * fragmentSize;
					int x2 = x + fragmentSize;
					int y2 = y + fragmentSize;
					if(x2 > width)
						x2 = width;
					if(y2 > height)
						y2 = height;
					int fragmentWidth = x2 - x;
					int fragmentHeight = y2 - y;
					// Draw fragment
					Pixmap fragment = new Pixmap(fragmentWidth, fragmentHeight, level.getFormat());
                    fragment.setBlending(Pixmap.Blending.None);
					fragment.drawPixmap(level, x, y, fragmentWidth, fragmentHeight, 0, 0, fragmentWidth, fragmentHeight);
					fragments[l][f] = fragment;
				}
			}
		} catch(Throwable e) {
			// Release fragments
			for(int l = 0; l < fragments.length; l++) {
				if(fragments[l] != null) {
					for(int f = 0; f < fragments[l].length; f++) {
						if(fragments[l][f] != null)
							fragments[l][f].dispose();
					}
				}
			}
			throw new RuntimeException("Failed to convert to FragmentedImageData", e);
		}
		
		return new FragmentedImageData(compressQuality, fragmentSize, widths, heights, fragments);
	}


	@Override
	public boolean isSupported() {
		return true;		// Always supported
	}


	@Override
	public boolean isFinal() {
		return true;		// No other compressors necessary
	}
}
