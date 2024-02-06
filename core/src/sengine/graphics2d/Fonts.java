package sengine.graphics2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import sengine.File;
import sengine.GarbageCollector;
import sengine.Sys;
import sengine.materials.ColorAttribute;
import sengine.materials.ColoredMaterial;

/**
 * Created by Azmi on 26/7/2016.
 */
public class Fonts implements GarbageCollector.Collectible {
    static final String TAG = "Fonts";

    public static boolean VERBOSE = false;

    public static int defaultSize = 32;
    public static float defaultGarbageTime = 15f;

    public static int resolutionMultiplier = 1;

    /**
     * Total bandwidth allowed per page update. When compacting, all pages have to be uploaded at once.
     */
    public static int defaultTextureSize = 512;

    /**
     * Maximum amount of dead area to keep, if total dead are exceeds this, all pages will be compacted.
     */
    public static int defaultMaxDeadArea = 512 * 512;

    public static int packingBorderSize = 8;    // 20180911 - previously was 2, need to increase to number of mipmap levels if using mipmaps, else there would be edge artifacts


    public static Texture.TextureFilter minFilter = Texture.TextureFilter.MipMapLinearLinear;       // Mipmap
    public static Texture.TextureFilter magFilter = Texture.TextureFilter.Linear;
    public static boolean useMipMaps = true;

    // Global fonts
    public static Fonts fonts;

    // Debug
    public static boolean debugFonts = false;
    public static int debugFontCount = 0;
    public static Color[] debugColors = new Color[] {
            new Color(0xed1c24ff),  new Color(0x0072bcff),
            new Color(0xf26522ff),  new Color(0x0054a6ff),
            new Color(0xf7941dff),  new Color(0x2e3192ff),
            new Color(0xfff200ff),  new Color(0x662d91ff),
            new Color(0x8dc63fff),  new Color(0x92278fff),
            new Color(0x39b54aff),  new Color(0xec008cff),
            new Color(0x00a99dff),

    };
    public static float debugColorsAlpha = 0.25f;

    private static final Pattern newlinePattern = Pattern.compile("[\\r\\n]");

    // temp
    private static final ThreadLocal<Rectangle> tempRectThreaded = new ThreadLocal<Rectangle>() {
        @Override
        protected Rectangle initialValue() {
            return new Rectangle();
        }
    };
    private static final Rectangle tempRectUnsafe = new Rectangle();

    private static final char[] tempCodePointChars = new char[2];
    private static final IntSet tempIntSet = new IntSet();
    private static final Matrix4 tempMat4 = new Matrix4();

    private static boolean multithreading = false;


    public static synchronized void multithreading(boolean enable) {
        multithreading = enable;
    }

    // Helpers
    public static String escapeMarkup(String text) {
        return text.replace("[", "[[").replace("[]", "[[]");
    }

    private class FontSource {


        // Identity
        private final int index;
        private final String filename;
        private final FreeTypeFontGenerator generator;
        private final FreeTypeFontGenerator.FreeTypeFontParameter params;
        private final FontSprites customSprites;

        // Chars list
        private final IntSet loadedChars = new IntSet();
        private final IntSet loadingChars = new IntSet();

        // Loaded data
        private final IntMap<BitmapFont.Glyph> glyphs = new IntMap<BitmapFont.Glyph>();
        private FreeTypeFontGenerator.FreeTypeBitmapFontData data;


        private float getLineHeight(float wrapChars) {
            if (data == null)
                loadPendingGlyphs();            // load if not yet loaded
            return -data.down / (wrapChars * params.size);
        }

        private void makeCompatible(CharSequence seq, int start, int end) {
            boolean compatible = true;
            while (start < end) {
                // Get code point
                int cp = Character.codePointAt(seq, start);
                int length = Character.toChars(cp, tempCodePointChars, 0);
                if (!loadedChars.contains(cp)) {
                    loadingChars.add(cp);
                    compatible = false;
                }
                start += length;
            }
            if (!compatible)
                loadPendingGlyphs();
        }

        private void clear() {
            data = null;
            loadedChars.clear();
            loadingChars.clear();
            glyphs.clear();
        }

        private void refreshUV() {
            // Reload all glyphs if needed
            if (data == null)
                return;
            for (IntMap.Entry<BitmapFont.Glyph> e : glyphs) {
                data.setGlyphRegion(e.value, regions.get(e.value.page));
            }
            // Missing glyph
            if (data.missingGlyph != null)
                data.setGlyphRegion(data.missingGlyph, regions.get(data.missingGlyph.page));
        }

        private void loadPendingGlyphs() {
            // Build chars list
            StringBuilder sb = new StringBuilder();
            IntSet.IntSetIterator iterator = loadingChars.iterator();
            tempIntSet.clear();
            while (iterator.hasNext) {
                int cp = iterator.next();
                // Check if custom sprites is overriding this code point
                if (customSprites != null && customSprites.isOverriding(cp)) {
                    // Custom sprites is overriding, load from custom sprites
                    tempIntSet.add(cp);
                    continue;
                }
                // Else need to load from font
                int length = Character.toChars(cp, tempCodePointChars, 0);
                sb.append(tempCodePointChars, 0, length);
            }
            params.characters = sb.toString();

            // Generate data
            params.packer = packer;
//            Pixmap.Blending blending = Pixmap.getBlending();
//            Pixmap.setBlending(Pixmap.Blending.SourceOver);
            data = generator.generateData(params, new FreeTypeFontGenerator.FreeTypeBitmapFontData() {
                @Override
                public boolean hasGlyph(char ch) {
                    return false;
                }
            });
//            Pixmap.setBlending(blending);

            // Load glyphs
            for (int c = 0, length = params.characters.length(); c < length; c++) {
                char ch = params.characters.charAt(c);
                BitmapFont.Glyph glyph = data.getGlyph(ch);
                if (glyph != null)
                    glyphs.put(ch, glyph);
            }

            // Load overriding glyphs
            iterator = tempIntSet.iterator();
            while (iterator.hasNext) {
                int cp = iterator.next();
                BitmapFont.Glyph glyph = customSprites.load(cp, data, params.size, packer);
                if (glyph != null)
                    glyphs.put(cp, glyph);
            }

            // Recognize loaded chars
            if (VERBOSE)
                Gdx.app.log(TAG, "Loaded " + loadingChars.size + " glyphs from " + filename);
            loadedChars.addAll(loadingChars);
            loadingChars.clear();
            texturesInvalidated = true;
        }

        private FontSource(int index, String filename, String customSpritesFilename, int resolution, Color borderColor, float borderWidth, Color shadowColor, int shadowOffsetX, int shadowOffsetY, Color color, int spaceX, int spaceY) {
            // Load font
            this.index = index;
            this.filename = filename;
            FileHandle fontFile = File.open(filename, true, true);
            generator = new FreeTypeFontGenerator(fontFile);

            customSprites = customSpritesFilename != null ? FontSprites.load(customSpritesFilename) : null;

            // Parameters
            params = new FreeTypeFontGenerator.FreeTypeFontParameter();
            params.characters = "";         // not used yet
            params.size = resolution * resolutionMultiplier;
            params.color = color;
            params.borderColor = borderColor;
            params.borderWidth = borderWidth * resolutionMultiplier;
            params.shadowColor = shadowColor;
            params.shadowOffsetX = shadowOffsetX * resolutionMultiplier;
            params.shadowOffsetY = shadowOffsetY * resolutionMultiplier;
            params.spaceX = spaceX * resolutionMultiplier;
            params.spaceY = spaceY * resolutionMultiplier;
            params.kerning = false;         // TODO: incremental loading does not update previous kerning...
//            // If there is no border specified, add a 1px transparent border with the base color to simulate edge anti-aliasing
//            // This is only because LibGDX uv maps glyphs with pixel accuracy, so I and Ls look much sharper on screen. This is to avoid that.
            if(borderWidth == 0) {
                params.borderWidth = 1;
                params.borderColor = new Color(color);
                params.borderColor.a = 0;
                params.spaceX -= 1;
                params.spaceY -= 1;
            }
//             params.hinting = FreeTypeFontGenerator.Hinting.AutoFull;       // Fonts are a bit thicker and readable
        }

        private void register(String name, Color color) {
            int r = (int) (color.r * 255);
            int g = (int) (color.g * 255);
            int b = (int) (color.b * 255);
            int a = (int) (color.a * 255);
            if (r < 0)
                r = 0;
            else if (r > 255)
                r = 255;
            if (g < 0)
                g = 0;
            else if (g > 255)
                g = 255;
            if (b < 0)
                b = 0;
            else if (b > 255)
                b = 255;
            if (a < 0)
                a = 0;
            else if (a > 255)
                a = 255;
            int hashCode = index;           // Unique identity to this source
            r = ((hashCode & 0xff000000) >>> 8) | (r << 8) | 0xff;
            g = (hashCode & 0x00ff0000) | (g << 8) | 0xff;
            b = ((hashCode & 0x0000ff00) << 8) | (b << 8) | 0xff;
            a = ((hashCode & 0x000000ff) << 16) | (a << 8) | 0xff;

            // Register for libGDX lookup
            Color encoded = new Color(
                    Float.intBitsToFloat(r),
                    Float.intBitsToFloat(g),
                    Float.intBitsToFloat(b),
                    Float.intBitsToFloat(a)
            );
            Colors.put(name, encoded);

            // Register for fast lookup
            encodedFontLookup.put(name, encoded);
        }
    }


    private class DynamicFontData extends BitmapFont.BitmapFontData {

        // Source
        // private final IntMap<FontSource> sources = new IntMap<FontSource>();
        private Array<FontSource> sources = new Array<FontSource>(FontSource.class);
        private FontSource source = null;

        // Scaling
        private float currentScaleX = 1f;
        private float currentScaleY = 1f;

        // Current
        private boolean isPreviousSurrogate = false;
        private char highSurrogate;
        private final IntArray used = new IntArray();
        private FontSource baseSource = null;

        private void begin(float scaleX, float scaleY) {
            this.currentScaleX = scaleX;
            this.currentScaleY = scaleY;
            this.spaceXadvance = 0;
//            spaceWidth = 0;         // 1.9.9-SNAPSHOT changed this to spaceXadvance
            source = null;          // reset
            baseSource = null;
            used.clear();
        }


        private DynamicFontData() {
            imagePaths = new String[0];         // no texture sources
        }

        private Color lookupEncoded(String name) {
            // Check cached
            if(name == cachedEncodedFontName)
                return cachedEncodedFont;

            // Else not cached
            Color fontEncoded = encodedFontLookup.get(name);
            if (fontEncoded == null) {
                // Not found in identity lookup, possible there are duplicate keys as strings, do deep lookup
                for(String key : encodedFontLookup.keys()) {
                    if(key.equals(name)) {
                        // Found a key
                        fontEncoded = encodedFontLookup.get(key);
                        encodedFontLookup.put(name, fontEncoded);       // found in deep lookup, register another identity
                        break;      // found
                    }
                }
                if(fontEncoded == null)
                    return null;                        // Not found in deep lookup either
            }

            // Cache
            cachedEncodedFontName = name;
            cachedEncodedFont = fontEncoded;

            return fontEncoded;
        }

        private FontSource lookup(String name) {
            Color color = lookupEncoded(name);
            if (color == null)
                return null;

            return lookup(color);
        }

        private FontSource lookup(Color color) {
            // Extract font identity
            int r = Float.floatToRawIntBits(color.r);
            int g = Float.floatToRawIntBits(color.g);
            int b = Float.floatToRawIntBits(color.b);
            int a = Float.floatToRawIntBits(color.a);
            int hashCodeR = (r & 0x00ff0000) << 8;
            int hashCodeG = (g & 0x00ff0000);
            int hashCodeB = (b & 0x00ff0000) >> 8;
            int hashCodeA = (a & 0x00ff0000) >> 16;
            int index = hashCodeR | hashCodeG | hashCodeB | hashCodeA;

            return sources.items[index];
        }

        private void switchSource(FontSource source, float scaleX, float scaleY) {
            this.source = source;
            // Copy all from source
            padLeft = source.data.padLeft;
            padTop = source.data.padTop;
            padRight = source.data.padRight;
            padBottom = source.data.padBottom;
            lineHeight = source.data.lineHeight;
            capHeight = source.data.capHeight;
            ascent = source.data.ascent;
            descent = source.data.descent;
            down = source.data.down;
            cursorX = source.data.cursorX;
            missingGlyph = null; // psource.data.missingGlyph;
            spaceXadvance = source.data.spaceXadvance;
//            spaceWidth = source.data.spaceWidth;      // 1.9.9-SNAPSHOT changed this to spaceXadvance
            xHeight = source.data.xHeight;
            breakChars = source.data.breakChars;
            xChars = source.data.xChars;
            capChars = source.data.capChars;
            markupEnabled = true;

            // Scaling
            this.scaleX = 1f;
            this.scaleY = 1f;
            float resolutionScale = (float) size / (float) source.params.size;
            setScale(scaleX * resolutionScale, scaleY * resolutionScale);
        }

        @Override
        public void getGlyphs(GlyphLayout.GlyphRun run, CharSequence str, int start, int end, BitmapFont.Glyph lastGlyph) {
            // Extract font identity
            int r = Float.floatToRawIntBits(run.color.r);
            int g = Float.floatToRawIntBits(run.color.g);
            int b = Float.floatToRawIntBits(run.color.b);
            int a = Float.floatToRawIntBits(run.color.a);
            int hashCodeR = (r & 0x00ff0000) << 8;
            int hashCodeG = (g & 0x00ff0000);
            int hashCodeB = (b & 0x00ff0000) >> 8;
            int hashCodeA = (a & 0x00ff0000) >> 16;
            int index = hashCodeR | hashCodeG | hashCodeB | hashCodeA;

            // Try to switch to specified font
            FontSource source;
            boolean isValidEncoding = (r & 0xFF) == 0xFF && (g & 0xFF) == 0xFF & (b & 0xFF) == 0xFF & (a & 0xFF) == 0xFF;
            if (isValidEncoding)
                source = sources.items[index];
            else
                source = null;              // not proper encoding
            if (source == null) {
                String errorMessage;
                if (!isValidEncoding)
                    errorMessage = String.format("Cannot lookup font with invalid encoding 0x%08X", run.color.toIntBits());
                else {
                    // Unable to find font, for debugging purposes lookup the font name from global colors
                    StringBuilder sb = new StringBuilder();
                    for (ObjectMap.Entry<String, Color> entry : Colors.getColors()) {
                        if (entry.value.r == run.color.r &&
                                entry.value.g == run.color.g &&
                                entry.value.b == run.color.b &&
                                entry.value.a == run.color.a) {
                            if (sb.length() > 0)
                                sb.append(", ");
                            sb.append(entry.key);
                        }
                    }
                    if (sb.length() == 0)
                        errorMessage = String.format("Cannot lookup font 0x%08X", run.color.toIntBits());
                    else
                        errorMessage = String.format("Cannot lookup font 0x%08X registered as %s", run.color.toIntBits(), sb.toString());
                }
                // Try to use previous source
                if (this.source == null)
                    throw new RuntimeException(errorMessage);
                // Else just reuse previous source
                Gdx.app.error(TAG, errorMessage);
                source = this.source;
            }

            // Make source compatible with the specified sequence
            source.makeCompatible(str, start, end);

            // Only change source if not the same
            if (this.source != source) {
                if (baseSource == null)
                    baseSource = source;
                if (source.data == null)
                    source.loadPendingGlyphs();                 // This can only happen if make compatible did not load the source
                switchSource(source, currentScaleX, currentScaleY);
            }

            // Remove font encoding from color
            r = (r & 0x0000ff00) >> 8;
            g = (g & 0x0000ff00) >> 8;
            b = (b & 0x0000ff00) >> 8;
            a = (a & 0x0000ff00) >> 8;
            run.color.set(r / 255f, g / 255f, b / 255f, a / 255f);


            // Reset
            isPreviousSurrogate = false;
            used.add(-1);
            used.add(index);


            // Get glyphs
            super.getGlyphs(run, str, start, end, lastGlyph);

            // Reset
            isPreviousSurrogate = false;
        }

        @Override
        public BitmapFont.Glyph getGlyph(char ch) {
            // Load from source
            if (source == null)
                throw new RuntimeException("Font not set, use getGlyphs() instead!");
            int cp = ch;
            if (isPreviousSurrogate && Character.isSurrogatePair(highSurrogate, ch)) {
                // Is a surrogate pair, return if found
                isPreviousSurrogate = false;
                cp = Character.toCodePoint(highSurrogate, ch);
            }
            if (Character.isHighSurrogate(ch)) {
                isPreviousSurrogate = true;
                highSurrogate = ch;
                return null;            // not yet, can only lookup glyph if we have a pair
            }
            isPreviousSurrogate = false;
            if(ch == '\0')
                return null;
            // Lookup
            BitmapFont.Glyph glyph = source.glyphs.get(cp);
            if (glyph != null)
                used.add(cp);
            return glyph;
        }
    }

    private static class PreparedGlyphLayout implements Pool.Poolable {
        private static Pool<PreparedGlyphLayout> pool = new Pool<PreparedGlyphLayout>() {
            @Override
            protected PreparedGlyphLayout newObject() {
                return new PreparedGlyphLayout();
            }
        };

        // Source
        private String text;
        private final Color color = new Color();
        private float wrapChars;

        // Cached
        private final IntArray used = new IntArray();
        private GlyphLayout layout;
        private GlyphLayout layoutStyled;
        private final Color currentTint = new Color(1, 1, 1, 1);
        private float currentHalign;

        // Cached render
        private float currentMatrixLength = 0f;
        private int currentMatrixAlign = Align.top | Align.left;
        private boolean currentMatrixEnforcedBounds = false;
        private boolean currentMatrixUsed = false;
        private final Matrix4 currentMatrix = new Matrix4();

        private FontSource baseSource;      // Initial source
        private float tLastUsed;

        // Linked list
        private PreparedGlyphLayout next;

        private void prepare(Fonts fonts) {
            // Mark as used
            tLastUsed = Sys.getTime();

            if(layout != null)
                return;     // already prepared

            // Else need to prepare now
            GlyphLayout glyphLayout = Pools.obtain(GlyphLayout.class);

            // Prepare layout
            fonts.fontData.begin(1, 1);
            baseSource = fonts.fontData.lookup(color);
            if(baseSource == null)
                throw new RuntimeException("Unable to lookup encoded font " + color);
            if(baseSource.data == null)
                baseSource.loadPendingGlyphs();
            fonts.fontData.switchSource(baseSource, 1, 1);

            // Set text
            float targetWidth;
            boolean wrap;
            if (wrapChars > 0) {
                targetWidth = wrapChars * fonts.size;
                wrap = true;
            } else if (wrapChars < 0) {
                targetWidth = -wrapChars * fonts.size;
                wrap = false;
            } else {
                targetWidth = 1f;
                wrap = false;
            }
            glyphLayout.setText(fonts.font, text, 0, text.length(), color, targetWidth, Align.left, wrap, null);     // TODO: 1.9.9 changed the behaviour of setText in GlyphLayout, requiring the duplicate code above with a slight change

            // Set layout
            layout = glyphLayout;

            // Base source
            if(fonts.fontData.baseSource != null)
                baseSource = fonts.fontData.baseSource;

            // Used glyphs
            used.clear();
            used.addAll(fonts.fontData.used);

            // Create a copy of layout
            layoutStyled = Pools.obtain(GlyphLayout.class);
            Pool<GlyphLayout.GlyphRun> glyphRunPool = Pools.get(GlyphLayout.GlyphRun.class);
            for(int c = 0; c < layout.runs.size; c++) {
                GlyphLayout.GlyphRun run = layout.runs.get(c);
                GlyphLayout.GlyphRun copy = glyphRunPool.obtain();
                copy.color.set(run.color);
                copy.glyphs.addAll(run.glyphs);
                copy.xAdvances.addAll(run.xAdvances);
                copy.x = run.x;
                copy.y = run.y;
                copy.width = run.width;
                layoutStyled.runs.add(copy);
            }
            layoutStyled.width = layout.width;
            layoutStyled.height = layout.height;
        }

        private GlyphLayout getUnitLayout(Fonts fonts) {
            // Prepare
            prepare(fonts);

            return layout;
        }

        private GlyphLayout getStyledLayout(Fonts fonts, Color tint, int halign) {
            // Prepare
            prepare(fonts);

            // Check style
            if(halign != currentHalign) {
                // Reset to unit layout and scale
                for (int c = 0; c < layout.runs.size; c++) {
                    GlyphLayout.GlyphRun run = layout.runs.get(c);
                    GlyphLayout.GlyphRun copy = layoutStyled.runs.get(c);
                    for (int i = 0; i < run.xAdvances.size; i++)
                        copy.xAdvances.items[i] = run.xAdvances.items[i];
                    copy.x = run.x;
                    copy.y = run.y;
                    copy.width = run.width;
                }
                layoutStyled.width = layout.width;
                layoutStyled.height = layout.height;

                // Update alignment
                if ((halign & Align.left) == 0) {
                    // Calculate max width
                    float targetWidth;
                    if (wrapChars > 0)
                        targetWidth = wrapChars * fonts.size;
                    else if (wrapChars < 0)
                        targetWidth = -wrapChars * fonts.size;
                    else
                        targetWidth = layoutStyled.width;

                    // Not left aligned, so must be center or right aligned.
                    boolean center = (halign & Align.center) != 0;
                    float lineWidth = 0, lineY = Integer.MIN_VALUE;
                    int lineStart = 0, n = layoutStyled.runs.size;
                    for (int i = 0; i < n; i++) {
                        GlyphLayout.GlyphRun run = layoutStyled.runs.get(i);
                        if (run.y != lineY) {
                            lineY = run.y;
                            float shift = targetWidth - lineWidth;
                            if (center) shift /= 2;
                            while (lineStart < i)
                                layoutStyled.runs.get(lineStart++).x += shift;
                            lineWidth = 0;
                        }
                        lineWidth += run.width;
                    }
                    float shift = targetWidth - lineWidth;
                    if (center) shift /= 2;
                    while (lineStart < n)
                        layoutStyled.runs.get(lineStart++).x += shift;
                }

                // Remember
                currentHalign = halign;
            }


            // Tint
            if (tint != null && (tint.r != currentTint.r || tint.g != currentTint.g || tint.b != currentTint.b || tint.a != currentTint.a)) {
                // Reset all run colors and tint again
                for (int c = 0; c < layoutStyled.runs.size; c++) {
                    Color styledColor = layoutStyled.runs.get(c).color;
                    styledColor.set(layout.runs.get(c).color);
                    if(styledColor.r == 1f && styledColor.g == 1f && styledColor.b == 1f && styledColor.a == 1f)
                        styledColor.mul(tint);          // Only multiply tint on white colors ? TODO: is this behaviour ok ?
                }
                // Remember
                currentTint.set(tint);
            }

            return layoutStyled;
        }


        private void invalidate() {
            if (layout != null) {
                Pools.free(layout);
                Pools.free(layoutStyled);
            }
            layout = null;
            layoutStyled = null;
            // Identity
            currentTint.set(Color.WHITE);
            currentHalign = Align.left;
            currentMatrixLength = Float.MAX_VALUE;
        }

        private boolean equalsType(Color withColor, float withTargetWidth) {
            return color.r == withColor.r &&
                    color.g == withColor.g &&
                    color.b == withColor.b &&
                    color.a == withColor.a &&
                    wrapChars == withTargetWidth
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != PreparedGlyphLayout.class)
                return false;
            PreparedGlyphLayout with = (PreparedGlyphLayout) obj;
            if (text != with.text && (text == null || with.text == null || !text.equals(with.text)))
                return false;
            return equalsType(
                    with.color,
                    with.wrapChars
            );
        }

        public void identity(String text, Color color, float wrapChars) {
            this.text = text;
            this.color.set(color);
            this.wrapChars = wrapChars;
        }

        @Override
        public void reset() {
            text = null;
            baseSource = null;
            invalidate();
            // Linked list
            next = null;
        }
    }

    private static class FontRenderInstruction extends MaterialConfiguration implements Pool.Poolable {
        private static Pool<FontRenderInstruction> pool = new Pool<FontRenderInstruction>() {
            @Override
            protected FontRenderInstruction newObject() {
                return new FontRenderInstruction();
            }
        };

        // Font
        private Fonts fonts;
        private PreparedGlyphLayout layout;
        private int halign;
        private Affine2 affine = new Affine2();

        private void renderUnsafe() {
            // Render
            try {
                // Color
                Color color = getColor(ColorAttribute.u_color);
                if (color == null)
                    color = Color.WHITE;
                // Get layout
                GlyphLayout glyphLayout = layout.getStyledLayout(fonts, color, halign);
                // Ensure loaded
                fonts.ensureLoaded();
                // Change source
                fonts.fontData.switchSource(layout.baseSource, 1, 1);

                // Render
                BitmapFontCache cache = fonts.font.getCache();
                cache.addText(glyphLayout, 0, 0);

                // Transform new vertices
                for(int c = 0; c < fonts.regionIndices.length; c++) {
                    int prev = fonts.regionIndices[c];
                    int current = cache.getVertexCount(c);
                    float[] vertices = cache.getVertices(c);
                    for(; prev < current; prev += 5) {
                        float x = vertices[prev];
                        float y = vertices[prev + 1];
                        vertices[prev] = affine.m00 * x + affine.m01 * y + affine.m02;
                        vertices[prev + 1] = affine.m10 * x + affine.m11 * y + affine.m12;
                    }
                    // Remember next offset
                    fonts.regionIndices[c] = current;
                }

            } catch (Throwable e) {
                Gdx.app.error(TAG, "Failed to render layout", e);
            }
        }

        @Override
        public void render(Shader shader) {
            if(multithreading) {
                synchronized(Fonts.class) {
                    renderUnsafe();
                }
            }
            else {
                renderUnsafe();
            }
        }

        @Override
        public void bind(Shader shader) {
            fonts.font.getCache().clear();
            // Clear region indices
            if(fonts.regionIndices != null) {
                for (int c = 0; c < fonts.regionIndices.length; c++)
                    fonts.regionIndices[c] = 0;
            }
        }

        @Override
        public void unbind(Shader shader) {
            fonts.font.getCache().draw(Sys.sb);
        }

        @Override
        public void clear() {
            super.clear();
            // Return back to cache
            pool.free(this);
        }

        @Override
        public void reset() {
            // nothing
        }
    }

    // Identity
    private final int size;
    private final int textureSize;
    private final float tGarbageInterval;
    private final int maxDeadArea;

    // Textures
    private PixmapPacker packer = null;
    private final Array<TextureRegion> regions = new Array<>(TextureRegion.class);
    private int[] regionIndices;
    private int lastRegion = 0;

    // Font
    private final BitmapFont font;
    private final DynamicFontData fontData;

    // Debug
    private final Sprite debugSprite;

    // Current
    private boolean texturesInvalidated = true;
    private float tGarbageScheduled = -1;

    // Font resolver
    private String cachedEncodedFontName = null;
    private Color cachedEncodedFont = null;
    private final IdentityMap<String, Color> encodedFontLookup = new IdentityMap<String, Color>();

    // Compatible strings
    private String cachedLayoutText = null;
    private PreparedGlyphLayout cachedLayout = null;
    private final IdentityMap<String, PreparedGlyphLayout> layoutsLookup = new IdentityMap<String, PreparedGlyphLayout>(); //new ArrayPreparedGlyphLayout>(PreparedGlyphLayout.class);
    private final Array<PreparedGlyphLayout> layoutsList = new Array<PreparedGlyphLayout>(PreparedGlyphLayout.class);

    public boolean existsFont(String name) {
        FontSource source = fontData.lookup(name);
        return source != null;
    }

    public void addFont(String name, String filename, String customSpritesFilename, int resolution, Color borderColor, float borderWidth, Color shadowColor, int shadowOffsetX, int shadowOffsetY, Color color, int spaceX, int spaceY) {
        if (existsFont(name))
            return;
        FontSource source = new FontSource(fontData.sources.size, filename, customSpritesFilename, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, spaceX, spaceY);
        fontData.sources.add(source);
        // Register name
        source.register(name, Color.WHITE);     // default white
    }

    public void colorFont(String name, String from, Color color) {
        if (existsFont(name))
            return;
        FontSource source = fontData.lookup(from);
        if (source == null)
            throw new RuntimeException("Font not found: " + from);
        source.register(name, color);
    }

    public void ensureLoaded() {
        if (!texturesInvalidated)
            return;
        reloadTextures();
    }

    private void reloadTextures() {
        texturesInvalidated = false;         // mark as loaded
        // Dispose existing textures
        if (regions.size > 0) {
            for (int c = lastRegion; c < regions.size; c++)
                regions.get(c).getTexture().dispose();
            regions.removeRange(lastRegion, regions.size - 1);
        }
        // Reload textures
        if (packer == null) {
            if (VERBOSE) Gdx.app.log(TAG, "No textures loaded");
            return;     // nothing to load
        }

        // Fetch bitmap font pages
        Array<PixmapPacker.Page> pages = packer.getPages();
        // Load textures
        int debugLoaded = 0;
        for (int c = lastRegion; c < pages.size; c++) {
            Texture texture = new Texture(pages.get(c).getPixmap(), useMipMaps);
            texture.setFilter(minFilter, magFilter);
            regions.add(new TextureRegion(texture));
            debugLoaded++;
        }
        lastRegion = pages.size - 1;
        if (lastRegion < 0)
            lastRegion = 0;
        // Resize region indices if necessary
        if(regionIndices == null)
            regionIndices = new int[regions.size];
        else if(regionIndices.length != regions.size)
            regionIndices = Arrays.copyOf(regionIndices, regions.size);

        // Update glyph uv's
        for(int c = 0; c < fontData.sources.size; c++)
            fontData.sources.items[c].refreshUV();

        // Require GC
        GarbageCollector.add(this);
        tGarbageScheduled = Sys.getTime() + tGarbageInterval;

        // Debug
        if (VERBOSE) Gdx.app.log(TAG, "Uploaded " + debugLoaded + " of " + pages.size + " textures");
    }

    @Override
    public boolean performGC(boolean forced) {
        if(multithreading) {
            synchronized(Fonts.class) {
                return performGCUnsafe(forced);
            }
        }
        else {
            return performGCUnsafe(forced);
        }
    }


    private boolean performGCUnsafe(boolean forced) {

        float tSysTime = Sys.getTime();
        if (tSysTime < tGarbageScheduled && !forced)
            return false;       // no GC
        // Else perform GC now
        tGarbageScheduled = tSysTime + tGarbageInterval;
        // Invalidate cache lookups
        cachedLayoutText = null;
        cachedLayout = null;
        // Cleanup layouts
        int previousLayouts = layoutsList.size;
        if (forced) {
            // Free all
            PreparedGlyphLayout.pool.freeAll(layoutsList);
            layoutsList.clear();
            layoutsLookup.clear();
        } else {
            Iterator<PreparedGlyphLayout> iterator = layoutsList.iterator();
            while (iterator.hasNext()) {
                // Check if this layout has expired
                PreparedGlyphLayout layout = iterator.next();
                layout.next = null;       // reset link
                float tExpiryTime = layout.tLastUsed + tGarbageInterval;
                if (tExpiryTime < tSysTime) {
                    // Not using this text anymore
                    iterator.remove();
                    PreparedGlyphLayout.pool.free(layout);
                }
            }
            layoutsLookup.clear();
            // Refresh lookups
            iterator = layoutsList.iterator();
            while (iterator.hasNext()) {
                PreparedGlyphLayout layout = iterator.next();
                PreparedGlyphLayout prev = layoutsLookup.put(layout.text, layout);
                if (prev != null)
                    layout.next = prev;
            }
        }
        // If there are no changes in the number of layouts, nothing to free
        if (previousLayouts == layoutsList.size)
            return layoutsList.size == 0;           // if zero, must be previously not loaded as well
        // Need to find dead glyphs (unused glyphs)
        // First, mark all active glyphs
        for (int c = 0; c < layoutsList.size; c++) {
            // Add used glyphs back to sources
            PreparedGlyphLayout layout = layoutsList.items[c];
            FontSource source = null;
            for (int i = 0; i < layout.used.size; i++) {
                if (layout.used.items[i] == -1) {
                    i++;
                    source = fontData.sources.items[layout.used.items[i]];
                    continue;
                }
                // Add glyph
                source.loadingChars.add(layout.used.items[i]);
            }
        }
        // Next, count total dead glyph area
        int deadGlyphArea = 0;
        for (int c = 0; c < fontData.sources.size; c++) {
            FontSource source = fontData.sources.items[c];
            IntSet.IntSetIterator iterator = source.loadedChars.iterator();
            while(iterator.hasNext) {
                int cp = iterator.next();
                if(!source.loadingChars.contains(cp)) {
                    BitmapFont.Glyph glyph = source.glyphs.get(cp);
                    if(glyph != null)
                        deadGlyphArea += (glyph.width + (packingBorderSize * 2)) * (glyph.height + (packingBorderSize * 2));        // glyph not used anymore, calculate area occupied
                }
            }
        }
        // Determine if compacting all pages are worth it
        if(deadGlyphArea < maxDeadArea) {
            // Not worth it, reset loading chars and be done with it
            for (int c = 0; c < fontData.sources.size; c++) {
                FontSource source = fontData.sources.items[c];
                IntSet.IntSetIterator iterator = source.loadingChars.iterator();
                while(iterator.hasNext) {
                    int cp = iterator.next();
                    if(source.loadedChars.contains(cp))
                        iterator.remove();          // aleady loaded, dont load again
                }
            }
            // Done
            if(VERBOSE) {
                deadGlyphArea = (int) Math.round(Math.sqrt(deadGlyphArea));
                Gdx.app.log(TAG, "Keeping " + deadGlyphArea + "x" + deadGlyphArea + " dead glyphs from " + (previousLayouts - layoutsList.size) + " layouts");
            }
        }
        else {
            // Else, totally worth it to compact all pages
            // Dispose existing textures
            for (int c = 0; c < regions.size; c++)
                regions.get(c).getTexture().dispose();
            regions.clear();
            lastRegion = 0;
            // Clear all font sources
            for (int c = 0; c < fontData.sources.size; c++) {
                FontSource source = fontData.sources.items[c];
                source.clear();
            }
            // Reload used glyphs from layouts
            for (int c = 0; c < layoutsList.size; c++) {
                // Add used glyphs back to sources
                PreparedGlyphLayout layout = layoutsList.items[c];
                FontSource source = null;
                for (int i = 0; i < layout.used.size; i++) {
                    if (layout.used.items[i] == -1) {
                        i++;
                        source = fontData.sources.items[layout.used.items[i]];
                        continue;
                    }
                    // Add glyph
                    source.loadingChars.add(layout.used.items[i]);
                }
            }
            // Time to reload glyphs
            // Clear packer
            packer.dispose();
            packer = new PixmapPacker(textureSize, textureSize, Pixmap.Format.RGBA8888, packingBorderSize, false);
            // If do not have any layouts anymore (not being used), nothing to load
            if (layoutsList.size == 0) {
                if (VERBOSE) {
                    deadGlyphArea = (int) Math.round(Math.sqrt(deadGlyphArea));
                    Gdx.app.log(TAG, "Cleaning up " + deadGlyphArea + "x" + deadGlyphArea + " dead glyphs from " + previousLayouts + " layouts");
                }
                return true;
            }
            // Else need to reload
            // Reload all sources
            for (int c = 0; c < fontData.sources.size; c++) {
                FontSource source = fontData.sources.items[c];
                if (source.loadingChars.size > 0)
                    source.loadPendingGlyphs();
            }
            if (VERBOSE) {
                deadGlyphArea = (int) Math.round(Math.sqrt(deadGlyphArea));
                Gdx.app.log(TAG, "Cleaning up " + deadGlyphArea + "x" + deadGlyphArea + " dead glyphs from " + (previousLayouts - layoutsList.size) + " layouts");
            }
            // Invalidate textures
            texturesInvalidated = true;
            // Invalidate layouts
            for (int c = 0; c < layoutsList.size; c++)
                layoutsList.items[c].invalidate();
        }

        // Queue next GC
        // GarbageCollector.add(this);           // TODO: I dun think this is needed
        tGarbageScheduled = Sys.getTime() + tGarbageInterval;
        return false;
    }


    private PreparedGlyphLayout prepare(String text, Color color, float wrapChars, boolean cache) {
        // Find in cached
        PreparedGlyphLayout layout;
        if(cache) {
            if (cachedLayoutText == text)
                layout = cachedLayout;
            else {
                layout = layoutsLookup.get(text);
                if (layout != null) {
                    cachedLayoutText = text;
                    cachedLayout = layout;
                }
            }
            while (layout != null) {
                if (layout.equalsType(color, wrapChars))
                    return layout;
                layout = layout.next;
            }
        }
        // If not in cache, create new
        layout = PreparedGlyphLayout.pool.obtain();
        layout.identity(text, color, wrapChars);
        if (cache) {
            PreparedGlyphLayout prev = layoutsLookup.put(text, layout);
            layoutsList.add(layout);
            if (prev != null) {
                layout.next = prev;
                if(cachedLayout == prev) {
                    cachedLayoutText = null;
                    cachedLayout = null;
                }
            }
        }

        return layout;
    }

    public void fontPrepare(String text, String fontName) {
        if (text == null || text.isEmpty())
            return;            // empty text

        Color fontEncoded = fontData.lookupEncoded(fontName);
        if(fontEncoded == null)
            throw new RuntimeException("Font not found: " + fontName);

        // Prepare layout
        PreparedGlyphLayout layout = prepare(text, fontEncoded, 0, true);
        layout.prepare(this);       // Prepare immediately
    }


    /**
     * Wraps a multiline text, line breaks will be retained.
     *
     * @param text
     * @param fontName
     * @param wrapChars
     * @return
     */
    public String fontWrap(String text, String fontName, float wrapChars) {
        if (text == null || text.isEmpty() || wrapChars <= 0)
            return text;            // empty text or wrapChars indicate no wrapping required
        String[] lines = newlinePattern.split(text);
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < lines.length; c++) {
            if (c > 0)
                sb.append('\n');            // new line
            String line = wrapLine(lines[c], fontName, wrapChars);
            if (line.isEmpty())
                continue;
            sb.append(line);
        }
        return sb.toString();
    }


    /**
     * Wraps a single line, linea breaks will be ignored.
     *
     * @param line
     * @param fontName
     * @param wrapChars
     * @return
     */
    public String wrapLine(String line, String fontName, float wrapChars) {
        if (line == null || line.isEmpty() || wrapChars <= 0)
            return line;            // empty line or wrapChars indicate no wrapping required

        Color fontEncoded = fontData.lookupEncoded(fontName);
        if(fontEncoded == null)
            throw new RuntimeException("Font not found: " + fontName);

        // Prepare layout
        PreparedGlyphLayout layout = prepare(line, fontEncoded, wrapChars, false);        // no need to save
        GlyphLayout glyphLayout = layout.getUnitLayout(this);

        // Rebuild string
        StringBuilder sb = new StringBuilder();
        float y = 0;
        for (int c = 0; c < glyphLayout.runs.size; c++) {
            GlyphLayout.GlyphRun run = glyphLayout.runs.get(c);
            if (c == 0)
                y = run.y;
            else if (y != run.y) {
                y = run.y;
                sb.append('\n');
            }
            for (int i = 0; i < run.glyphs.size; i++) {
                BitmapFont.Glyph glyph = run.glyphs.get(i);
                int length = Character.toChars(glyph.id, tempCodePointChars, 0);
                sb.append(tempCodePointChars, 0, length);
            }
        }

        // Free
        PreparedGlyphLayout.pool.free(layout);

        return sb.toString();
    }

    public String fontEllipsize(String text, String fontName, float wrapChars, int maxLines, String ellipsis) {
        if (maxLines <= 0)
            return "";          // must be at least one line
        if (text == null || text.isEmpty())
            return text;
        // Wrap first
        text = wrap(text, fontName, wrapChars);
        // Split again
        String[] lines = newlinePattern.split(text);
        if (lines.length <= maxLines)
            return text;        // no need to ellipsize
        // Truncate last line if needed
        StringBuilder sb = new StringBuilder();
        if (ellipsis != null && !ellipsis.isEmpty()) {
            String lastLine = lines[maxLines - 1];
            int offset = lastLine.length();
            // Keep truncating char by char, until can be appended with the ellipsis without wrapping again
            while (true) {
                sb.setLength(0);
                sb.append(lastLine, 0, offset);
                sb.append(ellipsis);
                String wrapped = wrap(sb.toString(), fontName, wrapChars);
                if (wrapped.indexOf('\n') == -1 || offset == 0) {
                    // Appended ellipsis without wrapping, or cannot truncate anymore, replace last line
                    lines[maxLines - 1] = sb.toString();
                    sb.setLength(0);        // reset
                    break;
                }
                // Else cannot append ellipsis without wrapping, truncate by one character
                offset--;
            }
        }
        // Rebuild string up to maxLines
        for (int c = 0; c < maxLines; c++) {
            if (c > 0)
                sb.append('\n');
            sb.append(lines[c]);
        }
        return sb.toString();
    }

    public int getFontNumLines(String fontName, float length, float wrapChars) {
        if (wrapChars <= 0)
            return 1;           // no wrapping
        FontSource source = fontData.lookup(fontName);
        if (source == null)
            throw new RuntimeException("Font not found: " + fontName);
        float lineHeight = source.getLineHeight(wrapChars);
        int numLines = (int) (length / lineHeight);
        if (numLines < 1)
            numLines = 1;
        return numLines;
    }

    public Rectangle getPixelBounds(String text, String fontName, float wrapChars) {
        if (text == null || text.isEmpty()) {
            Rectangle tempRect = multithreading ? tempRectThreaded.get() : tempRectUnsafe;
            return tempRect.set(0, 0, 0, 0);
        }

        Color fontEncoded = fontData.lookupEncoded(fontName);
        if(fontEncoded == null)
            throw new RuntimeException("Font not found: " + fontName);

        // Prepare layout
        PreparedGlyphLayout layout = prepare(text, fontEncoded, wrapChars, true);

        return getPixelBounds(layout);
    }

    private Rectangle getPixelBounds(PreparedGlyphLayout layout) {
        GlyphLayout glyphLayout = layout.getUnitLayout(this);

        // Set bounds
        Rectangle tempRect = multithreading ? tempRectThreaded.get() : tempRectUnsafe;
        tempRect.set(0, 0, glyphLayout.width, glyphLayout.height);

        return tempRect;
    }

    public Rectangle getFontBounds(String text, String fontName, float wrapChars, boolean enforceBounds) {
        if (text == null || text.isEmpty()) {
            Rectangle tempRect = multithreading ? tempRectThreaded.get() : tempRectUnsafe;
            return tempRect.set(0, 0, 0, 0);
        }

        Rectangle bounds = getPixelBounds(text, fontName, wrapChars);

        if (wrapChars > 0) {
            float scale = 1f / (wrapChars * size);            // wrapping allowed
            bounds.width *= scale;
            bounds.height *= scale;
        } else if (wrapChars < 0) {
            float scale = 1f / (-wrapChars * size);           // wrapping not allowed
            bounds.width *= scale;
            bounds.height *= scale;
        } else {
            bounds.height /= bounds.width;
            bounds.width = 1f;
        }

        if(enforceBounds && bounds.width > 1f) {
            bounds.height /= bounds.width;
            bounds.width = 1f;
        }

        return bounds;
    }

    public void renderFont(MaterialInstance instance, String text, String fontName, float length, float wrapChars, int align, boolean enforceBounds) {
        if (text == null || text.isEmpty() || length == 0f)
            return;     // nothing to render

        if(debugFonts) {
            Matrices.push();
            Matrices.model.translate(0.5f, -length / 2f, 0);
            Matrices.model.scale(1, length, 1);
            int colorIndex = debugFontCount++;
            colorIndex %= debugColors.length;
            ColorAttribute colorAttribute = ColorAttribute.of(debugSprite);
            colorAttribute.set(debugColors[colorIndex]).alpha(debugColorsAlpha);
            debugSprite.render();
            Matrices.pop();
        }

        // Prepare layout
        Color fontEncoded = fontData.lookupEncoded(fontName);
        if(fontEncoded == null)
            throw new RuntimeException("Font not found: " + fontName);
        PreparedGlyphLayout layout = prepare(text, fontEncoded, wrapChars, true);

        // Calculate bounds to enforce length and valign
        Rectangle rect = getPixelBounds(layout);
//        float rectWidth = rect.width;
//        float rectHeight = rect.height;

        // Calculate targetWidth
        float targetWidth = (wrapChars == 0 ? (rect.width / size) : wrapChars) * size;
        if (targetWidth < 0)
            targetWidth = -targetWidth;


        // Prepare matrix
        if(length != layout.currentMatrixLength || align != layout.currentMatrixAlign || enforceBounds != layout.currentMatrixEnforcedBounds) {
            // Reset
            layout.currentMatrixLength = length;
            layout.currentMatrixAlign = align;
            layout.currentMatrixEnforcedBounds = enforceBounds;
            layout.currentMatrixUsed = false;
            layout.currentMatrix.idt();

            // Normalize bounds
            float scale = 1f / targetWidth;
            rect.width *= scale;
            rect.height *= scale;

            // Normalize width
            if (enforceBounds && rect.width > 1f) {
                layout.currentMatrixUsed = true;
                float widthScale = 1f / rect.width;
                // Fix valign spacing
                if ((align & Align.top) == 0) {
                    if ((align & Align.bottom) != 0)
                        layout.currentMatrix.translate(0, -((1f - widthScale) * length), 0);
                    else        // center
                        layout.currentMatrix.translate(0, -((1f - widthScale) * length) / 2f, 0);
                }
                // Fix halign, dont exactly know why this is needed
                if ((align & Align.left) == 0) {
                    if ((align & Align.right) != 0)
                        layout.currentMatrix.translate((1f - widthScale), 0, 0);
                    else        // center
                        layout.currentMatrix.translate((1f - widthScale) / 2f, 0, 0);
                }
                layout.currentMatrix.scale(widthScale, widthScale, widthScale);
                rect.width = 1f;
                rect.height *= widthScale;
            }

            float lengthScale = length / rect.height;

            // Scale down to preserve length
            if (lengthScale < 1f) {
                layout.currentMatrixUsed = true;
                // Fix halign spacing
                if ((align & Align.left) == 0) {
                    // Not aligning from left, just offset to match width of 1.0
                    if ((align & Align.right) != 0)
                        layout.currentMatrix.translate(1f - lengthScale, 0, 0);
                    else        // center
                        layout.currentMatrix.translate((1f - lengthScale) / 2f, 0, 0);
                }
                layout.currentMatrix.scale(lengthScale, lengthScale, lengthScale);
                rect.width *= lengthScale;
                rect.height *= lengthScale;
            }

            // Enforce valign
            if ((align & Align.top) == 0) {
                // Not aligning from top
                layout.currentMatrixUsed = true;
                if ((align & Align.bottom) != 0)
                    layout.currentMatrix.translate(0, -(length - rect.height), 0);
                else        // center
                    layout.currentMatrix.translate(0, -(length - rect.height) / 2f, 0);
            }
        }

        // TODO: halign with wrapping by libgdx is broken, as it does not eat whitespace to the right

        // Calculate position and scale
        tempMat4.idt().set(Matrices.camera.combined).mul(Matrices.model);
        if(layout.currentMatrixUsed)
            tempMat4.mul(layout.currentMatrix);
        float scale = 1f / targetWidth;
        tempMat4.scale(scale, scale, scale);

//        float scaleX = tempMat4.getScaleX() / targetWidth;
//        float scaleY = tempMat4.getScaleY() / targetWidth;
//        float x = tempMat4.val[Matrix4.M03];
//        float y = tempMat4.val[Matrix4.M13];

//        if (scaleX == 0 || scaleY == 0)
//            return;         // nothing to render
//
//        float pixelWidth = ((rectWidth * scaleX) / 2f) * Sys.system.getWidth();
//        float pixelHeight = ((rectHeight * scaleY) / 2f) * Sys.system.getHeight();
//        float pixelX = ((x + 1f) / 2f) * Sys.system.getWidth();
//        float pixelY = ((y + 1f) / 2f) * Sys.system.getHeight();
//
//        Sys.error(TAG, "D: "  + pixelWidth + "x" + pixelHeight + " " + pixelX + " " + pixelY + " = " + text);

        // Configure render instruction
        FontRenderInstruction r = FontRenderInstruction.pool.obtain();
        r.bindObject = this;
        r.fonts = this;
        r.material = instance.material;
        r.layout = layout;
        r.affine.set(tempMat4);
        r.halign = align & (Align.left | Align.center | Align.right);

        // Upload render config
        instance.configure(r);

        // Add to renderer
        Renderer.renderer.addInstruction(r);
    }

    public Fonts() {
        this(defaultSize, defaultTextureSize, defaultGarbageTime, defaultMaxDeadArea);
    }

    public Fonts(int size, int textureSize, float tGarbageInterval, int maxDeadArea) {

        this.size = size;
        this.textureSize = textureSize;
        this.tGarbageInterval = tGarbageInterval;
        this.maxDeadArea = maxDeadArea;

        fontData = new DynamicFontData();

        // Need a packer
        packer = new PixmapPacker(textureSize, textureSize, Pixmap.Format.RGBA8888, packingBorderSize, false);

        // Create empty bitmap font
        regions.add(null);          // hack to force bitmapfont to not load textures
        font = new BitmapFont(fontData, regions, false);
        regions.clear();

        // Debug sprite
        debugSprite = new Sprite(new ColoredMaterial());
    }


    private static void ensureGlobalFonts() {
        if (Fonts.fonts == null)
            throw new RuntimeException("Fonts.fonts not set!");
    }

    public static void add(String name, String filename, String customSpritesFilename, int resolution, Color borderColor, float borderWidth, Color shadowColor, int shadowOffsetX, int shadowOffsetY, Color color, int spaceX, int spaceY) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                fonts.addFont(name, filename, customSpritesFilename, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, spaceX, spaceY);
            }
        }
        else {
            ensureGlobalFonts();
            fonts.addFont(name, filename, customSpritesFilename, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, spaceX, spaceY);
        }
    }

    public static boolean exists(String name) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                return fonts.existsFont(name);
            }
        }
        else {
            ensureGlobalFonts();
            return fonts.existsFont(name);
        }
    }

    public static void color(String name, String from, Color color) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                fonts.colorFont(name, from, color);
            }
        }
        else {
            ensureGlobalFonts();
            fonts.colorFont(name, from, color);
        }
    }

    public static Rectangle getBounds(String text, String fontName, float wrapChars, boolean enforceBounds) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                return fonts.getFontBounds(text, fontName, wrapChars, enforceBounds);
            }
        }
        else {
            ensureGlobalFonts();
            return fonts.getFontBounds(text, fontName, wrapChars, enforceBounds);
        }
    }

    public static int getNumLines(String fontName, float length, float wrapChars) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                return fonts.getFontNumLines(fontName, length, wrapChars);
            }
        }
        else {
            ensureGlobalFonts();
            return fonts.getFontNumLines(fontName, length, wrapChars);
        }
    }

    public static String ellipsize(String text, String fontName, float wrapChars, int maxLines, String ellipsis) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                return fonts.fontEllipsize(text, fontName, wrapChars, maxLines, ellipsis);
            }
        }
        else {
            ensureGlobalFonts();
            return fonts.fontEllipsize(text, fontName, wrapChars, maxLines, ellipsis);
        }
    }

    public static String wrap(String text, String fontName, float wrapChars) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                return fonts.fontWrap(text, fontName, wrapChars);
            }
        }
        else {
            ensureGlobalFonts();
            return fonts.fontWrap(text, fontName, wrapChars);
        }
    }

    public static void prepare(String text, String fontName) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                fonts.fontPrepare(text, fontName);
            }
        }
        else {
            ensureGlobalFonts();
            fonts.fontPrepare(text, fontName);
        }
    }

    public static void render(MaterialInstance instance, String text, String fontName, float length, float wrapChars, int align, boolean enforceBounds) {
        if(multithreading) {
            synchronized(Fonts.class) {
                ensureGlobalFonts();
                fonts.renderFont(instance, text, fontName, length, wrapChars, align, enforceBounds);
            }
        }
        else {
            ensureGlobalFonts();
            fonts.renderFont(instance, text, fontName, length, wrapChars, align, enforceBounds);
        }
    }
}