package sengine.graphics2d;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.StringBuilder;

import sengine.File;
import sengine.mass.MassSerializable;
import sengine.utils.Config;

/**
 * Created by Azmi on 26/7/2016.
 */
public class FontSprites implements MassSerializable {

    public static final String FONTSPRITES_EXTENSION = ".FontSprites";

    private final IntMap<byte[]> map = new IntMap<byte[]>();
    private final float scale;
    private final int yOffset;

    public static FontSprites load(String filename) {
        FontSprites sprites = File.getHints(filename, true, false);
        if (sprites == null) {
            sprites = new FontSprites(filename);
            File.saveHints(filename, sprites);
        }
        return sprites;
    }

    public boolean isOverriding(int codePoint) {
        return map.containsKey(codePoint);
    }

    public BitmapFont.Glyph load(int cp, BitmapFont.BitmapFontData data, int size, PixmapPacker packer) {
        byte[] encoded = map.get(cp);
        if (encoded == null)
            return null;       // this character was not found
        // Load this pixmap
        Pixmap pixmap = new Pixmap(encoded, 0, encoded.length);
        // Resize
        int width = Math.round(size * scale);
        int height = Math.round(((float) pixmap.getHeight() / (float) pixmap.getWidth()) * (float) width);
        pixmap = TextureUtils.resize(pixmap, width, height);

        BitmapFont.Glyph glyph = new BitmapFont.Glyph();
        glyph.id = cp;
        glyph.width = pixmap.getWidth();
        glyph.height = pixmap.getHeight();
        glyph.xoffset = 0;
        // glyph.yoffset = (int)(flip ? +(-fontData.descent + height) : -(-fontData.descent + height));
        glyph.yoffset = (int) data.down + yOffset;
        glyph.xadvance = pixmap.getWidth();

        Rectangle rect = packer.pack(pixmap);
        glyph.page = packer.getPages().size - 1; // Glyph is always packed into the last page for now.
        glyph.srcX = (int) rect.x;
        glyph.srcY = (int) rect.y;

        pixmap.dispose();

        return glyph;
    }

    public FontSprites(String filename) {
        // Configure
        IntMap<String> lookup = new IntMap<String>();
        Config config = new Config(filename + FONTSPRITES_EXTENSION, true, "map", lookup, "scale", 1f, "yOffset", 0);
        lookup = config.get("map");
        scale = config.get("scale");
        yOffset = config.get("yOffset");
        // Load all
        for (IntMap.Entry<String> e : lookup.entries()) {
            String spriteFilename = e.value;
            byte[] encoded = File.open(spriteFilename).readBytes();
            // Save
            map.put(e.key, encoded);
        }
    }

    @MassConstructor
    public FontSprites(int[] codePoints, byte[][] encodedSprites, float scale, int yOffset) {
        for (int c = 0; c < encodedSprites.length; c++) {
            map.put(codePoints[c], encodedSprites[c]);
        }
        this.scale = scale;
        this.yOffset = yOffset;
    }

    @Override
    public Object[] mass() {
        int[] codePoints = new int[map.size];
        byte[][] encodedSprites = new byte[map.size][];
        int c = 0;
        for (IntMap.Entry<byte[]> e : map) {
            codePoints[c] = e.key;
            encodedSprites[c] = e.value;
            c++;
        }
        return new Object[] { codePoints, encodedSprites, scale, yOffset };
    }


/*

    void splitCharacterList(CharSequence list, StringBuilder found, StringBuilder notFound) {
        for (int c = 0, size = list.length(); c < size; ) {
            int cp = Character.codePointAt(list, c);
            char[] chs = Character.toChars(cp);
            c += chs.length;
            if (map.containsKey(cp))
                found.append(chs);
            else
                notFound.append(chs);
        }
    }


    void load(CharSequence list, BitmapFont.BitmapFontData fontData, PixmapPacker packer, int size, boolean flip) {
        for (int c = 0, length = list.length(); c < length; ) {
            int cp = Character.codePointAt(list, c);
            char[] chs = Character.toChars(cp);
            c += chs.length;
            byte[] encoded = map.get(cp);
            if (encoded == null)
                continue;       // this character was not found
            // Load this pixmap
            Pixmap pixmap = new Pixmap(encoded, 0, encoded.length);
            // Resize
            int width = Math.round(size * scale);
            int height = Math.round(((float) pixmap.getHeight() / (float) pixmap.getWidth()) * (float) width);
            pixmap = TextureUtils.resize(pixmap, width, height);

            BitmapFont.Glyph glyph = new BitmapFont.Glyph();
            glyph.id = c;
            glyph.width = pixmap.getWidth();
            glyph.height = pixmap.getHeight();
            glyph.xoffset = 0;
            // glyph.yoffset = (int)(flip ? +(-fontData.descent + height) : -(-fontData.descent + height));
            glyph.yoffset = (int) fontData.down;
            glyph.xadvance = pixmap.getWidth();

            Rectangle rect = packer.pack(pixmap);
            glyph.page = packer.getPages().size - 1; // Glyph is always packed into the last page for now.
            glyph.srcX = (int) rect.x;
            glyph.srcY = (int) rect.y;

            pixmap.dispose();

            fontData.setGlyph(cp, glyph);
        }
    }

    */
}
