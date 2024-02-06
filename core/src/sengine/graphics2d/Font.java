package sengine.graphics2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;

import sengine.mass.MassSerializable;
import sengine.materials.ColorAttribute;
import sengine.materials.SpriteBatchMaterial;

/**
 * Created by Azmi on 27/7/2016.
 */
public class Font extends MaterialInstance implements Animatable2D, MassSerializable {

    private static final ObjectMap<String, String> fontNameResolver = new ObjectMap<String, String>();

    public void prepare(String text) {
        Fonts.prepare(text, fontName);
    }

    public String wrap(String text, float wrapChars) {
        return Fonts.wrap(text, fontName, wrapChars);
    }

    public String ellipsize(String text, float wrapChars, int maxLines, String ellipsis) {
        return Fonts.ellipsize(text, fontName, wrapChars, maxLines, ellipsis);
    }

    public int getNumLines(float length, float wrapChars) {
        return Fonts.getNumLines(fontName, length, wrapChars);
    }

    public Rectangle getBounds(String text, float wrapChars, boolean enforceBounds) {
        return Fonts.getBounds(text, fontName, wrapChars, enforceBounds);
    }

    public void render(String text, float length, float wrapChars, int align, boolean enforceBounds) {
        Fonts.render(this, text, fontName, length, wrapChars, align, enforceBounds);
    }

    public void name(String name) {
        color(name, Color.WHITE);
    }

    public void color(String name, int color) {
        color(name, new Color(color));
    }

    public void color(String name, float r, float g, float b, float a) {
        color(name, new Color(r, g, b, a));
    }

    public void color(String name, Color color) {
        Fonts.color(name, fontName, color);
    }

    // Identity
    private final String filename;
    private final String customSpritesFilename;
    private final int resolution;
    private final Color borderColor;
    private final float borderWidth;
    private final Color shadowColor;
    private final int shadowOffsetX;
    private final int shadowOffsetY;
    private final Color color;
    private final int spaceX;
    private final int spaceY;

    private final String fontName;

    public String name() {
        return fontName;
    }

    public Font(String filename, int resolution) {
        this(filename, null, resolution, Color.CLEAR, 0, Color.CLEAR, 0, 0, Color.WHITE, 0, 0, null, null);
    }

    public Font(String filename, int resolution, int color) {
        this(filename, null, resolution, Color.CLEAR, 0, Color.CLEAR, 0, 0, Color.WHITE, 0, 0, null, null);
        ColorAttribute.of(this).set(color);
    }

    public Font(String filename, int resolution, Color borderColor, float borderWidth, Color shadowColor, int shadowOffsetX, int shadowOffsetY, Color color) {
        this(filename, null, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, 0, 0, null, null);
    }

    public Font(String filename, String customSpritesFilename, int resolution, Color borderColor, float borderWidth, Color shadowColor, int shadowOffsetX, int shadowOffsetY, Color color) {
        this(filename, customSpritesFilename, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, 0, 0, null, null);
    }


    public Font(String filename, int resolution, Color borderColor, float borderWidth, Color shadowColor, int shadowOffsetX, int shadowOffsetY, Color color, int spaceX, int spaceY) {
        this(filename, null, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, spaceX, spaceY, null, null);
    }

    public Font(String filename, String customSpritesFilename, int resolution, Color borderColor, float borderWidth, Color shadowColor, int shadowOffsetX, int shadowOffsetY, Color color, int spaceX, int spaceY) {
        this(filename, customSpritesFilename, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, spaceX, spaceY, null, null);
    }


    @MassConstructor
    public Font(String filename, String customSpritesFilename, int resolution, Color borderColor, float borderWidth, Color shadowColor, int shadowOffsetX, int shadowOffsetY, Color color, int spaceX, int spaceY, Material material, MaterialAttribute[] attribs) {
        this.filename = filename;
        this.customSpritesFilename = customSpritesFilename;
        this.resolution = resolution;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.shadowColor = shadowColor;
        this.shadowOffsetX = shadowOffsetX;
        this.shadowOffsetY = shadowOffsetY;
        this.color = color;
        this.spaceX = spaceX;
        this.spaceY = spaceY;

        // Check if font exists
        String fullFontName = "#" + filename + "#" + customSpritesFilename + "#" + resolution + "#" + borderColor + "#" + borderWidth + "#"+ shadowColor + "#" + shadowOffsetX + "#" + shadowOffsetY + "#" + color + "#" + spaceX + "#" + spaceY;
        String shortName;
        synchronized (fontNameResolver) {
            shortName = fontNameResolver.get(fullFontName);
            if (shortName == null) {
                // First time encountering this font variant, create a short name
                shortName = "$" + Integer.toHexString(fontNameResolver.size);
                fontNameResolver.put(fullFontName, shortName);
            }
        }
        this.fontName = shortName;
        Fonts.add(fontName, filename, customSpritesFilename, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, spaceX, spaceY);

        // Else font already loaded

        // Material and attributes
        if(material == null)
            material = Material.load(SpriteBatchMaterial.DEFAULT_NAME);
        setMaterial(material, attribs);
    }

    @Override
    public Object[] mass() {
        return new Object[] { filename, customSpritesFilename, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, spaceX, spaceY, material, attribs };
    }

    public Font instantiate() {
        Font f = new Font(filename, customSpritesFilename, resolution, borderColor, borderWidth, shadowColor, shadowOffsetX, shadowOffsetY, color, spaceX, spaceY, material, null);
        f.copyAttributes(this);
        return f;
    }

    // Animatable2D
    @Override
    public void translate(float x, float y) {
        Matrices.model.translate(x, y, 0.0f);
    }

    @Override
    public void rotate(float rotate) {
        Matrices.model.rotate(0, 0, -1, rotate);
    }

    @Override
    public void scale(float x, float y) {
        Matrices.model.scale(x, y, 1.0f);
    }

    @Override
    public void shear(float sx, float sy) {
        Matrices.shear(sx, sy);
    }

    @Override
    public void scissor(float x, float y, float width, float height) {
        Matrices.scissor.set(x, y, width, height);
    }

    @Override
    public void applyGlobalMatrix() {
        // no special matrix to apply
    }

    @Override
    public float getLength() {
        return 1.0f;		// TODO: not really supported
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + fontName;
    }
}
