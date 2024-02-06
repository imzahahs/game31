package sengine.graphics2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import sengine.Sys;

public class TextureUtils {
	static final String TAG = "TextureUtils";
	
	
	public static int maxTextureSize = 2048;		// should be set by game implementation
	public static float resolutionGainThreshold = 0.49f;			// Slightly prefer downsampling
    public static int maxResizeStep = 128;

	public static int maxAlphaColorSampleDistance = 10;             // Search up to the distance of 10 pixels to find the nearest color value from this point

	public static class ManualTexture extends Texture {		// TODO: new LibGDX can extend GLTexture instead of texture

		private static final IntBuffer buffer = BufferUtils.newIntBuffer(1);
		
		public ManualTexture() {
			super(manualTextureData);
		}

		public ManualTexture(int glTarget) {
			super(glTarget, Gdx.gl.glGenTexture(), manualTextureData);
		}

		@Override
		public void load(TextureData data) {
			// not loading here
		}
		
		@Override
		public void dispose() {
			int glHandle = getTextureObjectHandle();
			buffer.put(0, glHandle);
			Gdx.gl.glDeleteTextures(1, buffer);
		}
	}
	
	private static class ManualTextureData implements TextureData {
		@Override
		public void consumeCustomData(int target) {
			// should never happen
		}

		@Override
		public Pixmap consumePixmap() {
			return null;		// should never be called
		}

		@Override
		public boolean disposePixmap() {
			return true;		// should never be called
		}

		@Override
		public Format getFormat() {
			return null;		// should never be called
		}

		@Override
		public int getWidth() {
			return 0;		// should never be called
		}

		@Override
		public int getHeight() {
			return 0;		// should never be called
		}

		@Override
		public TextureDataType getType() {
			return TextureDataType.Custom;
		}
		
		@Override
		public boolean isManaged() {
			return false;		// should never be called
		}

		@Override
		public boolean isPrepared() {
			return true;		// should never be called
		}

		@Override
		public void prepare() {
			// should never be called
		}

		@Override
		public boolean useMipMaps() {
			return false;		// should never be called
		}
	}
	public static final ManualTextureData manualTextureData = new ManualTextureData();
	
	// Utilities
	public static int getLevels(int width, int height) {
		int levels = 1;
		// Calculate number of levels
		while(!(width == 1 && height == 1)) {
			if(width > 1)
				width /= 2;
			if(height > 1)
				height /= 2;
			levels++;
		}
		return levels;
	}
	
	public static boolean isPowerOfTwo(int value) {
		return (value & -value) == value;
	}

	public static int nearestPowerOfTwo(int number) {
		return nearestPowerOfTwo(number, resolutionGainThreshold);
	}
	
	public static int nearestPowerOfTwo(int number, float resolutionGainThreshold) {
		if(number <= 2)
			return number;
		int nextPot = 2, prevPot = 2;
		while(nextPot < number) {
			prevPot = nextPot;
			nextPot *= 2;
		}
		// See which is nearest, the previous or the next
		float gain = ((float)(nextPot - number)) / (float)(nextPot - prevPot);
		return gain <= resolutionGainThreshold ? nextPot : prevPot; 
	}

	public static Pixmap premultiplyAlpha(Pixmap pixmap) {
		Pixmap newPixmap = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				// Current pixel
				int pixel = pixmap.getPixel(x, y);
				int r = (pixel >> 24) & 0xff;
				int g = (pixel >> 16) & 0xff;
				int b = (pixel >> 8) & 0xff;
				int a = pixel & 0xff;
				// Premultiply alpha
				r = (r * a) / 255;
				g = (g * a) / 255;
				b = (b * a) / 255;
				// Save pixel
				pixel = (r << 24) | (g << 16) | (b << 8) | a;
				newPixmap.drawPixel(x, y, pixel);
			}
		}
		pixmap.dispose();
		return newPixmap;
	}

	
	public static Pixmap resizeHalf(Pixmap pixmap, float gamma) {
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();
		if(width > 1) {
			if(width % 2 == 1)
				throw new IllegalArgumentException("Invalid dimensions to divide half: " + pixmap.getWidth() + "x" + pixmap.getHeight());
			width /= 2;
		}
		if(height > 1) {
			if(height % 2 == 1)
				throw new IllegalArgumentException("Invalid dimensions to divide half: " + pixmap.getWidth() + "x" + pixmap.getHeight());
			height /= 2;
		}
		return resize(pixmap, width, height);           // TODO: 20180303 this avoid black haloes on images resized containing alpha
//		Pixmap newPixmap = new Pixmap(width, height, pixmap.getFormat());
//		for(int y = 0; y < height; y++) {
//			for(int x = 0; x < width; x++) {
//				// Averaged pixels
//				double ar = 0;
//				double ag = 0;
//				double ab = 0;
//				double aa = 0;
//				// Top left
//				int pixel = pixmap.getPixel((x * 2), (y * 2));
//				int samples = 0;
//				if((pixel & 0xff) > 0) {
//					ar += Math.pow(((pixel >> 24) & 0xff), gamma);
//					ag += Math.pow(((pixel >> 16) & 0xff), gamma);
//					ab += Math.pow(((pixel >> 8) & 0xff), gamma);
//					aa += pixel & 0xff;
//					samples++;
//				}
//				pixel = pixmap.getPixel((x * 2) + 1, (y * 2));
//				if((pixel & 0xff) > 0) {
//					ar += Math.pow(((pixel >> 24) & 0xff), gamma);
//					ag += Math.pow(((pixel >> 16) & 0xff), gamma);
//					ab += Math.pow(((pixel >> 8) & 0xff), gamma);
//					aa += pixel & 0xff;
//					samples++;
//				}
//				pixel = pixmap.getPixel((x * 2), (y * 2) + 1);
//				if((pixel & 0xff) > 0) {
//					ar += Math.pow(((pixel >> 24) & 0xff), gamma);
//					ag += Math.pow(((pixel >> 16) & 0xff), gamma);
//					ab += Math.pow(((pixel >> 8) & 0xff), gamma);
//					aa += pixel & 0xff;
//					samples++;
//				}
//				pixel = pixmap.getPixel((x * 2) + 1, (y * 2) + 1);
//				if((pixel & 0xff) > 0) {
//					ar += Math.pow(((pixel >> 24) & 0xff), gamma);
//					ag += Math.pow(((pixel >> 16) & 0xff), gamma);
//					ab += Math.pow(((pixel >> 8) & 0xff), gamma);
//					aa += pixel & 0xff;
//					samples++;
//				}
//				if(samples > 0) {
//					ar /= samples;
//					ag /= samples;
//					ab /= samples;
//					aa /= samples;
//				}
//				int r = (int) Math.round(Math.pow(ar, 1.0f / gamma));
//				int g = (int) Math.round(Math.pow(ag, 1.0f / gamma));
//				int b = (int) Math.round(Math.pow(ab, 1.0f / gamma));
//				int a = (int) Math.round(aa);
//				if(r > 255)
//					r = 255;
//				if(g > 255)
//					g = 255;
//				if(b > 255)
//					b = 255;
//				if(a > 255)
//					a = 255;
//				// Rebuild reduced pixel
//				pixel = (r << 24) | (g << 16) | (b << 8) | a;
//				newPixmap.drawPixel(x, y, pixel);
//			}
//		}
//		pixmap.dispose();
//		return newPixmap;
	}
	
	public static Pixmap duplicate(Pixmap pixmap) {
		Pixmap copy = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
        // Memcpy
        // Get bytes
        ByteBuffer pixmapBytes = pixmap.getPixels();
        ByteBuffer copyBytes = copy.getPixels();
        // Move to start
        int position = pixmapBytes.position();
        pixmapBytes.position(0);
        copyBytes.position(0);
        // Copy
        copyBytes.put(pixmapBytes);
        // Reset position
        copyBytes.position(position);
        pixmapBytes.position(position);
//		copy.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, copy.getWidth(), copy.getHeight());
		return copy;
	}

	
	public static Pixmap standardizeFormat(Pixmap pixmap) {
		Format format = pixmap.getFormat();
        if(format == Format.RGBA4444 || format == Format.RGBA8888) {
            // Already good, but extend alpha color values to avoid those white halos
            return extendAlphaColorValues(pixmap);
        }
		if(format == Format.RGB565 || format == Format.RGB888)
			return pixmap;		// Already standard format
		Sys.info(TAG, "Standardizing format: " + format);
		// Else default to RGB888
		Pixmap newPixmap = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGB888);
		newPixmap.drawPixmap(pixmap, 0, 0);
		pixmap.dispose();
		return newPixmap;
	}

	public static Pixmap extendAlphaColorValues(Pixmap pixmap) {
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();
        pixmap.setBlending(Pixmap.Blending.None);

        boolean hasAlpha = false;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int pixel = pixmap.getPixel(x, y);
                int alpha = pixel & 0xff;
                if(alpha == 255)
                    continue;       // opaque
                hasAlpha = true;    // is translucent
				if(alpha != 0)
                    continue;        // there is still color information here
                // Else is totally transparent
                int color = findNearestColor(pixmap, x, y, 0, height, width, 0);
                pixel = color & 0xffffff00;
                pixmap.drawPixel(x, y, pixel);
			}
		}

		if(!hasAlpha) {
            // This image is as good as without alpha channel, so remove it
            pixmap = removeAlphaChannel(pixmap);
        }

		return pixmap;
	}

	public static int findNearestColor(Pixmap pixmap, int x, int y, int left, int top, int right, int bottom) {
		int width = right - left;
		int height = top - bottom;
        int size = Math.max(width, height);
		if(size > maxAlphaColorSampleDistance)
			size = maxAlphaColorSampleDistance;

        for(int c = 0; c < size; c++) {
            int dleft = x - c - 1;
            int dright = x + c + 1;
            int dbottom = y - c - 1;
            int dtop = y + c + 1;

            if(dbottom < top && dbottom >= bottom) {
                for(int dx = dleft; dx <= dright; dx++) {
                    if(dx < right && dx >= left) {
                        int lookup = pixmap.getPixel(dx, dbottom);
                        if((lookup & 0xff) != 0)
                            return lookup;
                    }
                }
            }

            for(int dy = dbottom + 1; dy < dtop; dy++) {
                if(dy < top && dy >= bottom) {
                    if(dleft < right && dleft >= left) {
                        int lookup = pixmap.getPixel(dleft, dy);
                        if((lookup & 0xff) != 0)
                            return lookup;
                    }
                    if(dright < right && dright >= left) {
                        int lookup = pixmap.getPixel(dright, dy);
                        if((lookup & 0xff) != 0)
                            return lookup;
                    }
                }
            }

            if(dtop < top && dtop >= bottom) {
                for(int dx = dleft; dx <= dright; dx++) {
                    if(dx < right && dx >= left) {
                        int lookup = pixmap.getPixel(dx, dtop);
                        if((lookup & 0xff) != 0)
                            return lookup;
                    }
                }
            }
        }

        // Unable to find color
        return pixmap.getPixel(x, y);
	}
	
	public static Pixmap duplicateAlphaChannel(Pixmap pixmap) {
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();
		Pixmap alpha = new Pixmap(width, height, Format.Alpha);
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++)
				alpha.drawPixel(x, y, pixmap.getPixel(x, y) & 0xff);
		}
		return alpha;
	}
	
	public static Pixmap removeAlphaChannel(Pixmap pixmap) {
		Format format = pixmap.getFormat();
		if(format == Format.RGBA4444)
			format = Format.RGB565;
		else if(format == Format.RGBA8888)
			format = Format.RGB888;
		else {
			Sys.debug(TAG, "Cannot remove alpha from format: " + format);
			return pixmap;		// Unsupported or already no alpha channel format
		}
        int width = pixmap.getWidth();
        int height = pixmap.getHeight();
        Pixmap rgb = new Pixmap(width, height, format);
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++)
                rgb.drawPixel(x, y, (pixmap.getPixel(x, y) & 0xffffff00) | 0xff);
        }
		pixmap.dispose();
		return rgb;
	}
	
	public static Pixmap reduceBitPrecision(Pixmap pixmap, int bitPrecision) {
		// Prevent UB
		if(bitPrecision < 1 || bitPrecision > 8)
			throw new IllegalArgumentException("Bit precision must be >= 1 && <= 8: " + bitPrecision);
		if(bitPrecision == 8)
			return pixmap;		// no need reduction
		int q = 1;
		for(int c = 0; c < bitPrecision; c++)
			q *= 2;
		q--;
		
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();

		// Do a DataProfile
		int[] p = new int[q + 1];
		for(int c = 0; c <= q; c++)
			p[c] = (int)Math.round((float)c / (float)q * 255.0f);			

		Pixmap newPixmap = new Pixmap(width, height, pixmap.getFormat());
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				// Current pixel
				int pixel = pixmap.getPixel(x, y);
				// Zero all totally transparent pixels
				if((pixel & 0xff) == 0)
					pixel = 0;
				int r = (pixel >> 24) & 0xff;
				int g = (pixel >> 16) & 0xff;
				int b = (pixel >> 8) & 0xff;
				int a = pixel & 0xff;

				// New pixel
				int rn = selectBest(r, p);
				int gn = selectBest(g, p);
				int bn = selectBest(b, p);
				int an = selectBest(a, p);
				// Error
				int re = r - rn;
				int ge = g - gn;
				int be = b - bn;
				int ae = a - an;
				// Rebuild reduced pixel
				pixel = (rn << 24) | (gn << 16) | (bn << 8) | an;
				newPixmap.drawPixel(x, y, pixel);
				// Floyd-steingberg error distribution
				
				if(x < (width - 1))
					floydSteinbergDistribute(pixmap, re, ge, be, ae, x + 1, y, 7.0f / 16.0f);
				if(y < (height - 1)) {
					if(x > 0)
						floydSteinbergDistribute(pixmap, re, ge, be, ae, x - 1, y + 1, 3.0f / 16.0f);
					floydSteinbergDistribute(pixmap, re, ge, be, ae, x, y + 1, 5.0f / 16.0f);
					if(x < (width - 1))
						floydSteinbergDistribute(pixmap, re, ge, be, ae, x + 1, y + 1, 1.0f / 16.0f);
				}
				
			}
		}
		pixmap.dispose();
		return newPixmap;
	}
	
	static int selectBest(int i, int[] p) {
		int best = 0;
		int bestDelta = 1000;
		for(int c = 0; c < p.length; c++) {
			int delta = Math.abs(p[c] - i); 
			if(delta < bestDelta) {
				// Found better representation
				best = p[c];
				bestDelta = delta;
			}
		}
		return best;
	}

	static void floydSteinbergDistribute(Pixmap pixmap, int re, int ge, int be, int ae, int x, int y, float fraction) {
		// Neighbouring pixel
		int pixel = pixmap.getPixel(x, y);
		int rn = (pixel >> 24) & 0xFF;
		int gn = (pixel >> 16) & 0xFF;
		int bn = (pixel >> 8) & 0xFF;
		int an = pixel & 0xFF;
		rn += Math.round((float)re * fraction);
		gn += Math.round((float)ge * fraction);
		bn += Math.round((float)be * fraction);
		an += Math.round((float)ae * fraction);
		// Use logical operators instead of bit operators cuz previous addition might turn 1's to 0's
		if(rn > 255)
			rn = 255;
		else if(rn < 0)
			rn = 0;
		if(gn > 255)
			gn = 255;
		else if(gn < 0)
			gn = 0;
		if(bn > 255)
			bn = 255;
		else if(bn < 0)
			bn = 0;
		if(an > 255)
			an = 255;
		else if(an < 0)
			an = 0;
		pixel = (rn << 24) | (gn << 16) | (bn << 8) | an;
		pixmap.drawPixel(x, y, pixel);
	}

	public static Pixmap validateDimensions(Pixmap pixmap, float size, int minSize, int maxSize, boolean pot, boolean square, boolean keepPixmap) {
		if(minSize < 0)
            minSize = 0;
        if(maxSize < 0)
            maxSize = Integer.MAX_VALUE;
        // Now that pixmap is loaded, rescale
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();
		int newWidth = width;
		int newHeight = height;
		// Calculate optimum width based on size parameter if required
		if(size > 0.0f) {
            if(width > height) {
                int optimumWidth = (int) ((float) width * size);
                if (optimumWidth == 0)
                    optimumWidth = 1;
                if(optimumWidth < minSize)
                    optimumWidth = minSize;
                else if(optimumWidth > maxSize)
                    optimumWidth = maxSize;
                // Downscale bitmap to optimum width if required size is smaller, never upscale
                if (optimumWidth < width) {
                    newHeight = (int) ((float) newHeight / (float) newWidth * (float) optimumWidth);
                    if (newHeight == 0)
                        newHeight = 1;
                    newWidth = optimumWidth;
                }
            }
            else {
                int optimumHeight = (int) ((float) height * size);
                if (optimumHeight == 0)
                    optimumHeight = 1;
                if(optimumHeight < minSize)
                    optimumHeight = minSize;
                else if(optimumHeight > maxSize)
                    optimumHeight = maxSize;
                if (optimumHeight < height) {
                    newWidth = (int) ((float) newWidth / (float) newHeight * (float) optimumHeight);
                    if (newWidth == 0)
                        newWidth = 1;
                    newHeight = optimumHeight;
                }
            }
		}
		// Rescale to maximum texture size constraint
		if(newWidth > maxTextureSize) { 
			newHeight = (int)((float)newHeight / (float)newWidth * (float)maxTextureSize);
			newWidth = maxTextureSize;
		}
		if(newHeight > maxTextureSize) {
			newWidth = (int)((float)newWidth / (float)newHeight * (float)maxTextureSize);
			newHeight = maxTextureSize;
		}
		// Also consider power of two if needed
		if(pot) {
			newWidth = nearestPowerOfTwo(newWidth);
			newHeight = nearestPowerOfTwo(newHeight);
			if(newWidth > maxTextureSize)
				newWidth = maxTextureSize;
			if(newHeight > maxTextureSize)
				newHeight = maxTextureSize;
		}
		// Validate square requirements
		// Always choose the lowest to make it square as choosing the higher will cause double the gain
		if(square && newWidth != newHeight)
			newWidth = newHeight = (newWidth > newHeight ? newWidth : newHeight);
		// Now rescale pixmap if new dimensions does not match existing dimensions
		if(newWidth == width && newHeight == height)
			return pixmap;		// no need to resize
		if(keepPixmap)
			return null;
		// Else must resize
		return resize(pixmap, newWidth, newHeight);
	}

	/**
	 * Crops center, resize to fit
	 * @param pixmap - source
	 * @param length - height / width ratio
     * @return source or a new pixmap with the specified length
     */
	public static Pixmap crop(Pixmap pixmap, float length) {
        if(length <= 0)
            return pixmap;          // pixmap
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();
		int expectedWidth = width;
		int expectedHeight = Math.round(expectedWidth * length);
		if(expectedHeight > height) {
            expectedWidth = Math.round(width * ((float) height / (float) expectedHeight));        // Crop to fit
            expectedHeight = height;
        }
		if(expectedWidth == width && expectedHeight == height)
			return pixmap;
		// Else resize
		int srcx = Math.round((width - expectedWidth) / 2f);
		int srcy = Math.round((height - expectedHeight) / 2f);
		Pixmap cropped = new Pixmap(expectedWidth, expectedHeight, pixmap.getFormat());
		cropped.drawPixmap(pixmap, 0, 0, srcx, srcy, expectedWidth, expectedHeight);
		pixmap.dispose();
		return cropped;
	}

	public static Pixmap resize(Pixmap pixmap, int newWidth, int newHeight) {
		int srcWidth = pixmap.getWidth();
        int srcHeight = pixmap.getHeight();
        if(srcWidth == newWidth && srcHeight == newHeight)
			return pixmap;		// no changes
        if(newWidth > srcWidth || newHeight > srcHeight) {
            // Upscaling
            Pixmap newPixmap = new Pixmap(newWidth, newHeight, pixmap.getFormat());
            newPixmap.setBlending(Pixmap.Blending.None);
            newPixmap.drawPixmap(pixmap, 0, 0, srcWidth, srcHeight, 0, 0, newWidth, newHeight);
            pixmap.dispose();
            return newPixmap;
        }
        // Downscaling
        while((srcWidth - newWidth) > maxResizeStep || (srcHeight - newHeight) > maxResizeStep) {
            int width;
            int height;
            if((srcWidth - newWidth) > maxResizeStep) {
                width = srcWidth - maxResizeStep;
                height = Math.round(((float)newHeight / (float)newWidth) * (float)width);
            }
            else {
                height = srcHeight - maxResizeStep;
                width = Math.round(((float)newWidth / (float)newHeight) * (float)height);
            }
            Pixmap newPixmap = new Pixmap(width, height, pixmap.getFormat());
            newPixmap.setBlending(Pixmap.Blending.None);
            newPixmap.drawPixmap(pixmap, 0, 0, srcWidth, srcHeight, 0, 0, width - 1, height - 1);

            // Replace alpha of resized pixmap's edge with the nearest alpha of actual pixmap's edge
            for(int y = 0; y < height; y++) {
                int nearestY = Math.round((float)y / (float)height * (float)srcHeight);
                if(nearestY >= srcHeight)
                    nearestY = srcHeight - 1;
                int pixel = pixmap.getPixel(srcWidth - 1, nearestY);
                newPixmap.drawPixel(width - 1, y, pixel);
            }
            for(int x = 0; x < width; x++) {
                int nearestX = Math.round((float)x / (float)width * (float)srcWidth);
                if(nearestX >= srcWidth)
                    nearestX = srcWidth - 1;
                int pixel = pixmap.getPixel(nearestX, srcHeight - 1);
                newPixmap.drawPixel(x, height - 1, pixel);
            }
            pixmap.dispose();
            pixmap = newPixmap;
            srcWidth = width;
            srcHeight = height;
        }
        Pixmap newPixmap = new Pixmap(newWidth, newHeight, pixmap.getFormat());
        newPixmap.setBlending(Pixmap.Blending.None);
        newPixmap.drawPixmap(pixmap, 0, 0, srcWidth, srcHeight, 0, 0, newWidth - 1, newHeight - 1);
        for(int y = 0; y < newHeight; y++) {
            int nearestY = Math.round((float)y / (float)newHeight * (float)srcHeight);
            if(nearestY >= srcHeight)
                nearestY = srcHeight - 1;
            int pixel = pixmap.getPixel(srcWidth - 1, nearestY);
            newPixmap.drawPixel(newWidth - 1, y, pixel);
        }
        for(int x = 0; x < newWidth; x++) {
            int nearestX = Math.round((float)x / (float)newWidth * (float)srcWidth);
            if(nearestX >= srcWidth)
                nearestX = srcWidth - 1;
            int pixel = pixmap.getPixel(nearestX, srcHeight - 1);
            newPixmap.drawPixel(x, newHeight - 1, pixel);
        }
        pixmap.dispose();
        return newPixmap;
	}
	
	public static Pixmap flipY(Pixmap pixmap) {
		int width = pixmap.getWidth();
		int height = pixmap.getHeight();

		Pixmap newPixmap = new Pixmap(width, height, pixmap.getFormat());
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int pixel = pixmap.getPixel(x, y);
				newPixmap.drawPixel(x, height - y - 1, pixel);
			}
		}

		pixmap.dispose();
		
		return newPixmap;
	}
}
