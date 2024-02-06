package game31.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

import game31.model.LayoutModel;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.graphics2d.Font;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.mass.MassSerializable;
import sengine.materials.SimpleMaterial;

/**
 * Created by Azmi on 7/5/2017.
 */

public class LayoutDescriptor implements MassSerializable {
    private static final String TAG = "LayoutDescriptor";


    private static final String TRANSIENT_PREFIX = "transient/";
    private static final String PNG_SUFFIX = ".png";

    private static final String LAYOUT_FILE_SUFFIX = "_layout.png";
    private static final String CONTENT_FILE_SUFFIX = "_content.png";
    private static final String REGION_FILE_SUFFIX = "_region";

    private static final String ALIGNMENT_TOPLEFT = "top left";
    private static final String ALIGNMENT_TOPCENTER = "top center";
    private static final String ALIGNMENT_TOPRIGHT = "top right";
    private static final String ALIGNMENT_LEFT = "left";
    private static final String ALIGNMENT_CENTER = "center";
    private static final String ALIGNMENT_RIGHT = "right";
    private static final String ALIGNMENT_BOTTOMLEFT = "bottom left";
    private static final String ALIGNMENT_BOTTOMCENTER = "bottom center";
    private static final String ALIGNMENT_BOTTOMRIGHT = "bottom right";


    public static class LayoutNamespace implements MassSerializable {
        public final String namespace;
        public final int sectionSize;
        public int count;

        public LayoutNamespace(String namespace, int sectionSize) {
            this.namespace = namespace;
            this.sectionSize = sectionSize;
        }

        @MassConstructor
        public LayoutNamespace(String namespace, int sectionSize, int count) {
            this.namespace = namespace;
            this.sectionSize = sectionSize;
            this.count = count;
        }

        @Override
        public Object[] mass() {
            return new Object[] { namespace, sectionSize, count };
        }
    }

    private static boolean pixmapEquals(Pixmap source, int fromX, int fromY, int toX, int toY, int color) {
        for(int y = fromY; y < toY; y++) {
            for(int x = fromX; x < toX; x++) {
                int pixel = source.getPixel(x, y);
                if(pixel != color)
                    return false;
            }
        }
        // Else all pixels match this color
        return true;
    }

    private static boolean pixmapEquals(Pixmap source, int sourceX, int sourceY, Pixmap with, int withX, int withY, int width, int height) {
        if((sourceX + width) > source.getWidth() || (sourceY + height) > source.getHeight())
            return false;       // source image is smaller
        if((withX + width) > with.getWidth() || (withY + height) > with.getHeight())
            return false;       // with image is smaller
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int pixel1 = source.getPixel(sourceX + x, sourceY + y);
                int pixel2 = with.getPixel(withX + x, withY + y);
                if(pixel1 != pixel2)
                    return false;
            }
        }
        return true;
    }

    private static int parseColor(String hex) {
        if(hex.length() == 6)
            hex = hex + "ff";
        else if(hex.length() != 8)
            throw new IllegalArgumentException("Invalid color hex: " + hex);
        return (int)Long.parseLong(hex, 16);
    }

    public static class ContentSprite implements MassSerializable {
        public final Sprite sprite;
        public final float x;
        public final float y;
        public final float size;
        public final float width;
        public final float height;

        public ContentSprite(String contentName, int fileIndex, float x, float y, float width, float height, float layoutWidth, float layoutHeight) {
            this.sprite = Sprite.load(contentName + fileIndex + PNG_SUFFIX);

            float layoutLength = layoutHeight / layoutWidth;
            this.x = -0.5f + ((x + (width / 2f)) / layoutWidth);
            this.y = +(layoutLength / 2f) - (((y + (height / 2f)) / layoutHeight) * layoutLength);
            this.size = width / layoutWidth;

            this.width = width;
            this.height = height;
        }

        @MassConstructor
        public ContentSprite(Sprite sprite, float x, float y, float size, float width, float height) {
            this.sprite = sprite;
            this.x = x;
            this.y = y;
            this.size = size;
            this.width = width;
            this.height = height;
        }

        @Override
        public Object[] mass() {
            return new Object[] { sprite, x, y, size, width, height };
        }
    }

    public static class LayoutText implements MassSerializable {
        public final int color;
        public final float x;
        public final float y;
        public final float size;
        public final Text text;
        public final float wrapChars;
        public final Animation startAnim;
        public final Animation idleAnim;
        public final Animation endAnim;


        public LayoutText(ObjectMap<String, Font> fonts, String fontName, int fontSize, String color,
                          float x, float y, float width, float height, float layoutWidth, float layoutHeight,
                          String alignment, String textString, float wrapChars,
                          Animation startAnim, Animation idleAnim, Animation endAnim
        ) {
            // Find font
            String fontID = fontName + "," + fontSize;
            Font font = fonts.get(fontID);
            if (font == null) {
                String[] data = fontName.split(",", 2);
                if(data.length == 2)
                    font = new Font(data[0], data[1], fontSize, Color.CLEAR, 0, Color.CLEAR, 0, 0, Color.WHITE);
                else
                    font = new Font(fontName, fontSize);
                fonts.put(fontID, font);
            }

            this.color = parseColor(color);

            float layoutLength = layoutHeight / layoutWidth;
            this.x = -0.5f + ((x + (width / 2f)) / layoutWidth);
            this.y = +(layoutLength / 2f) - (((y + (height / 2f)) / layoutHeight) * layoutLength);
            this.size = width / layoutWidth;

            this.wrapChars = wrapChars;

            float length = height / width;

            text = new Text()
                    .font(font)
                    .position(length, 0)
                    .text(textString);

            if(alignment.equalsIgnoreCase(ALIGNMENT_TOPLEFT))
                text.topLeft();
            else if(alignment.equalsIgnoreCase(ALIGNMENT_TOPCENTER))
                text.topCenter();
            else if(alignment.equalsIgnoreCase(ALIGNMENT_TOPRIGHT))
                text.topRight();
            else if(alignment.equalsIgnoreCase(ALIGNMENT_LEFT))
                text.centerLeft();
            else if(alignment.equalsIgnoreCase(ALIGNMENT_CENTER))
                text.center();
            else if(alignment.equalsIgnoreCase(ALIGNMENT_RIGHT))
                text.centerRight();
            else if(alignment.equalsIgnoreCase(ALIGNMENT_BOTTOMLEFT))
                text.bottomLeft();
            else if(alignment.equalsIgnoreCase(ALIGNMENT_BOTTOMCENTER))
                text.bottomCenter();
            else if(alignment.equalsIgnoreCase(ALIGNMENT_BOTTOMRIGHT))
                text.bottomRight();
            else
                throw new RuntimeException("Unrecognized alignment \"" + alignment + "\"");

            this.startAnim = startAnim;
            this.idleAnim = idleAnim;
            this.endAnim = endAnim;
        }

        @MassConstructor
        public LayoutText(int color, float x, float y, float size, Text text, float wrapChars, Animation startAnim, Animation idleAnim, Animation endAnim) {
            this.color = color;
            this.x = x;
            this.y = y;
            this.size = size;
            this.text = text;
            this.wrapChars = wrapChars;
            this.startAnim = startAnim;
            this.idleAnim = idleAnim;
            this.endAnim = endAnim;
        }

        @Override
        public Object[] mass() {
            return new Object[] { color, x, y, size, text, wrapChars, startAnim, idleAnim, endAnim };
        }
    }


    public static class LayoutLink implements MassSerializable {
        public final float x;
        public final float y;
        public final float width;
        public final float height;
        public final String action;

        public LayoutLink(float x, float y, float width, float height, float layoutWidth, float layoutHeight, String action) {
            float layoutLength = layoutHeight / layoutWidth;
            this.x = -0.5f + ((x + (width / 2f)) / layoutWidth);
            this.y = +(layoutLength / 2f) - (((y + (height / 2f)) / layoutHeight) * layoutLength);
            this.width = width / layoutWidth;
            this.height = height / layoutWidth;

            this.action = action;
        }

        @MassConstructor
        public LayoutLink(float x, float y, float width, float height, String action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.action = action;
        }

        @Override
        public Object[] mass() {
            return new Object[] { x, y, width, height, action };
        }
    }

    public static class LayoutCheckbox implements MassSerializable {
        public final String name;
        public final String groupName;
        public final int color;
        public final float x;
        public final float y;
        public final float size;
        public final String action;
        public final float inputPaddingLeft;
        public final float inputPaddingTop;
        public final float inputPaddingRight;
        public final float inputPaddingBottom;

        public LayoutCheckbox(String name, String groupName, String color, String action, float x, float y, float width, float height, float layoutWidth, float layoutHeight, float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom) {
            this.name = name;

            this.groupName = groupName;

            this.color = parseColor(color);

            this.action = action;

            float layoutLength = layoutHeight / layoutWidth;
            this.x = -0.5f + ((x + (width / 2f)) / layoutWidth);
            this.y = +(layoutLength / 2f) - (((y + (height / 2f)) / layoutHeight) * layoutLength);
            this.size = width / layoutWidth;

            this.inputPaddingLeft = inputPaddingLeft;
            this.inputPaddingTop = inputPaddingTop;
            this.inputPaddingRight = inputPaddingRight;
            this.inputPaddingBottom = inputPaddingBottom;
        }

        @MassConstructor
        public LayoutCheckbox(String name, String groupName, int color, String action, float x, float y, float size, float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom) {
            this.name = name;
            this.groupName = groupName;
            this.color = color;
            this.action = action;
            this.x = x;
            this.y = y;
            this.size = size;
            this.inputPaddingLeft = inputPaddingLeft;
            this.inputPaddingTop = inputPaddingTop;
            this.inputPaddingRight = inputPaddingRight;
            this.inputPaddingBottom = inputPaddingBottom;
        }

        @Override
        public Object[] mass() {
            return new Object[] { name, groupName, color, action, x, y, size, inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom };
        }
    }

    public static class LayoutInput implements MassSerializable {

        public final String name;
        public final Font font;
        public final int color;
        public final float x;
        public final float y;
        public final float size;
        public final float length;
        public final float wrapChars;
        public final String confirmText;
        public final String confirmAction;
        public final String inputType;
        public final float inputPaddingLeft;
        public final float inputPaddingTop;
        public final float inputPaddingRight;
        public final float inputPaddingBottom;


        public LayoutInput(String name, ObjectMap<String, Font> fonts, String fontName, int fontSize, String color, float x, float y, float width, float height, float layoutWidth, float layoutHeight, float wrapChars, String confirmText, String confirmAction, String inputType, float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom) {
            this.name = name;

            // Find font
            String fontID = fontName + "," + fontSize;
            Font font = fonts.get(fontID);
            if (font == null) {
                String[] data = fontName.split(",", 2);
                if(data.length == 2)
                    font = new Font(data[0], data[1], fontSize, Color.CLEAR, 0, Color.CLEAR, 0, 0, Color.WHITE);
                else
                    font = new Font(fontName, fontSize);
                fonts.put(fontID, font);
            }
            this.font = font;

            this.color = parseColor(color);

            float layoutLength = layoutHeight / layoutWidth;
            this.x = -0.5f + ((x + (width / 2f)) / layoutWidth);
            this.y = +(layoutLength / 2f) - (((y + (height / 2f)) / layoutHeight) * layoutLength);
            this.size = width / layoutWidth;

            this.length = height / width;

            this.wrapChars = wrapChars;

            this.confirmText = confirmText;
            this.confirmAction = confirmAction;

            this.inputType = inputType;

            this.inputPaddingLeft = inputPaddingLeft;
            this.inputPaddingTop = inputPaddingTop;
            this.inputPaddingRight = inputPaddingRight;
            this.inputPaddingBottom = inputPaddingBottom;
        }

        @MassConstructor
        public LayoutInput(String name, Font font, int color, float x, float y, float size, float length, float wrapChars, String confirmText, String confirmAction, String inputType, float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom) {
            this.name = name;

            this.font = font;
            this.color = color;
            this.x = x;
            this.y = y;
            this.size = size;
            this.length = length;
            this.wrapChars = wrapChars;

            this.confirmText = confirmText;
            this.confirmAction = confirmAction;

            this.inputType = inputType;

            this.inputPaddingLeft = inputPaddingLeft;
            this.inputPaddingTop = inputPaddingTop;
            this.inputPaddingRight = inputPaddingRight;
            this.inputPaddingBottom = inputPaddingBottom;
        }

        @Override
        public Object[] mass() {
            return new Object[] { name, font, color, x, y, size, length, wrapChars, confirmText, confirmAction, inputType, inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom };
        }
    }

    // Metrics
    public final String layoutName;
    public final String contentName;
    public final int bgColor;
    public final int width;
    public final int height;
    public final int sectionSize;
    public final int verticalSections;
    public final int horizontalSections;

    // Layout
    public final LayoutMesh[] layoutMeshes;

    // Content
    public final ContentSprite[] contentSprites;

    // Texts
    public final LayoutText[] texts;

    // Links
    public final LayoutLink[] links;

    // Inputs
    public final LayoutInput[] inputs;

    // Checkboxes
    public final LayoutCheckbox[] checkboxes;

    // Trigger
    public final String trigger;        // on show

    public LayoutDescriptor(LayoutModel model) {
        this.layoutName = model.layoutName;
        this.contentName = model.contentName;
        this.sectionSize = model.sectionSize;
        int contentSectionSize = model.contentSectionSize;

        Pixmap layoutPixmap = null;
        Pixmap contentPixmap = null;
        Array<Pixmap> tiles = new Array<Pixmap>(Pixmap.class);

        // Background color
        this.bgColor = parseColor(model.bgColor);

        try {
            Sys.debug(TAG, "Loading layout files for " + layoutName + ", " + contentName);

            layoutPixmap = new Pixmap(File.open(model.filename + LAYOUT_FILE_SUFFIX));
            contentPixmap = new Pixmap(File.open(model.filename + CONTENT_FILE_SUFFIX));

            width = layoutPixmap.getWidth();
            height = layoutPixmap.getHeight();
            if (contentPixmap != null && (width != contentPixmap.getWidth() || height != contentPixmap.getHeight()))
                throw new IllegalArgumentException("layout and content dimensions does not match");

            // Load tiles
            LayoutNamespace layoutNamespace = File.getHints(layoutName, false);
            if (layoutNamespace == null)
                layoutNamespace = new LayoutNamespace(layoutName, sectionSize);
            else if (layoutNamespace.sectionSize != sectionSize)
                throw new IllegalArgumentException("layout section size does not match existing layout namespace");
            for (int c = 0; c < layoutNamespace.count; c++) {
                // Load all tiles
                Pixmap tile = new Pixmap(File.open(TRANSIENT_PREFIX + layoutName + c + PNG_SUFFIX));
                tiles.add(tile);
            }

            // Calculate number of vertical and horizontal sections
            int verticalSections = height / sectionSize;
            if (height % sectionSize != 0)
                verticalSections++;
            int horizontalSections = width / sectionSize;
            if (width % sectionSize != 0)
                horizontalSections++;
            this.verticalSections = verticalSections;
            this.horizontalSections = horizontalSections;
            Sys.debug(TAG, "Processing layout tiles " + horizontalSections + "x" + verticalSections);
            // Prepare buffers
            int[][] indices = new int[verticalSections][horizontalSections];
            IntArray usedIndices = new IntArray();
            // Process images
            for (int v = 0; v < verticalSections; v++) {
                for (int h = 0; h < horizontalSections; h++) {
                    // Calculate tile bounds
                    int x = h * sectionSize;
                    int y = v * sectionSize;
                    int x2 = x + sectionSize;
                    if (x2 > width)
                        x2 = width;
                    int y2 = y + sectionSize;
                    if (y2 > height)
                        y2 = height;
                    int tileWidth = x2 - x;
                    int tileHeight = y2 - y;
                    // Check if this tile is same as background
                    if(pixmapEquals(layoutPixmap, x, y, x2, y2, bgColor)) {
                        // Tile only consists of one color
                        indices[v][h] = -1;
                        continue;
                    }
                    // Compare this tile to all extracted tiles
                    boolean matched = false;
                    for (int c = 0; c < tiles.size; c++) {
                        Pixmap tile = tiles.items[c];
                        if (pixmapEquals(layoutPixmap, x, y, tile, 0, 0, tileWidth, tileHeight)) {
                            // Tile matches
                            matched = true;
                            indices[v][h] = c;          // Track tile index
                            if(!usedIndices.contains(c))
                                usedIndices.add(c);
                            break;
                        }
                    }
                    // If no match, add new tile
                    if (!matched) {
                        Pixmap tile = new Pixmap(tileWidth, tileHeight, layoutPixmap.getFormat());
                        tile.drawPixmap(layoutPixmap, 0, 0, x, y, tileWidth, tileHeight);
                        tiles.add(tile);
                        int layer = tiles.size - 1;
                        indices[v][h] = layer;           // Track tile index
                        usedIndices.add(layer);
                    }
                }
            }
            // Save new tiles and commit to layoutnamespace
            if (tiles.size > layoutNamespace.count) {
                Sys.debug(TAG, "Saving " + (tiles.size - layoutNamespace.count) + " new tiles");

                for (int c = layoutNamespace.count; c < tiles.size; c++) {
                    // Save pixmap to transient
                    String imageFilename = TRANSIENT_PREFIX + layoutName + c + PNG_SUFFIX;
                    String textureFilename = layoutName + c + PNG_SUFFIX;
                    PixmapIO.writePNG(File.openCache(imageFilename), tiles.items[c]);
                    // Save texture as simple material
                    SimpleMaterial.CompileConfig config = SimpleMaterial.CompileConfig.load(layoutName);
                    Material texture = new SimpleMaterial(textureFilename, imageFilename, config);         // Precompile texture
                    File.saveHints(textureFilename, texture);
                }
                // Commit layout namespace
                layoutNamespace.count = tiles.size;
                File.saveHints(layoutName, layoutNamespace);
            }
            // For all used indices, prepare mesh
            layoutMeshes = new LayoutMesh[usedIndices.size];
            for(int c = 0; c < usedIndices.size; c++) {
                layoutMeshes[c] = LayoutMesh.create(layoutName, width, height, sectionSize, indices, usedIndices.items[c]);
            }
            Sys.debug(TAG, "Prepared " + usedIndices.size + " layers for layout");

            // Prepare content image
            Array<ContentSprite> contents = new Array<ContentSprite>(ContentSprite.class);
            if (contentPixmap != null) {
                // Recalculate vertical and horizontal sections for content image
                int contentTilesCount = 0;
                int contentVerticalSections = height / contentSectionSize;
                if (height % contentSectionSize != 0)
                    contentVerticalSections++;
                int contentHorizontalSections = width / contentSectionSize;
                if (width % contentSectionSize != 0)
                    contentHorizontalSections++;
                boolean[][] contentFound = new boolean[contentVerticalSections][contentHorizontalSections];
                // Find content images
                for (int v = 0; v < contentVerticalSections; v++) {
                    for (int h = 0; h < contentHorizontalSections; h++) {
                        // Calculate tile bounds
                        int x = h * contentSectionSize;
                        int y = v * contentSectionSize;
                        int x2 = x + contentSectionSize;
                        if (x2 > width)
                            x2 = width;
                        int y2 = y + contentSectionSize;
                        if (y2 > height)
                            y2 = height;
                        int tileWidth = x2 - x;
                        int tileHeight = y2 - y;
                        // Compare with content image
                        if (!pixmapEquals(layoutPixmap, x, y, contentPixmap, x, y, tileWidth, tileHeight)) {
                            contentFound[v][h] = true;
                            contentTilesCount++;
                        }
                    }
                }
                int totalTiles = contentVerticalSections * contentHorizontalSections;
                Sys.debug(TAG, "Grouping " + contentTilesCount + " / " + totalTiles + " content tiles (" + Math.round(((float)contentTilesCount / (float)totalTiles) * 100f) + "%)");
                // Group content images
                while (true) {
                    // Find a group of content tiles to group
                    boolean groupFound = false;
                    for (int v = 0; v < contentVerticalSections; v++) {
                        for (int h = 0; h < contentHorizontalSections; h++) {
                            if (!contentFound[v][h])
                                continue;
                            int boundsTopV = v;
                            int boundsBottomV = v;
                            int boundsLeftH = h;
                            int boundsRightH = h;
                            for (int v2 = v; v2 < contentVerticalSections; v2++) {
                                boolean found = false;
                                // First check if this line still contains content tiles
                                for(int h2 = boundsLeftH; h2 <= boundsRightH; h2++) {
                                    if (contentFound[v2][h2]) {
                                        found = true;
                                        break;
                                    }
                                }
                                if(!found)
                                    break;          // no more left
                                // Else expand bounds
                                boundsBottomV = v2;           // always below
                                // Expand bounds to the left
                                while(boundsLeftH > 0 && contentFound[v2][boundsLeftH - 1])
                                    boundsLeftH--;
                                // Expand bounds to the right
                                while(boundsRightH < (contentHorizontalSections - 1) && contentFound[v2][boundsRightH + 1])
                                    boundsRightH++;
                            }
                            // Extract this content image
                            int x = boundsLeftH * contentSectionSize;
                            int y = boundsTopV * contentSectionSize;
                            int x2 = (boundsRightH + 1) * contentSectionSize;
                            int y2 = (boundsBottomV + 1) * contentSectionSize;
                            if (x2 > width)
                                x2 = width;
                            if (y2 > height)
                                y2 = height;
                            int tileWidth = x2 - x;
                            int tileHeight = y2 - y;
                            Pixmap tile = new Pixmap(tileWidth, tileHeight, contentPixmap.getFormat());
                            try {
                                tile.drawPixmap(contentPixmap, 0, 0, x, y, tileWidth, tileHeight);
                                // Save pixmap to transient
                                String imageFilename = TRANSIENT_PREFIX + contentName + contents.size + PNG_SUFFIX;
                                String textureFilename = contentName + contents.size + PNG_SUFFIX;
                                PixmapIO.writePNG(File.openCache(imageFilename), tile);
                                // Save texture as simple material
                                SimpleMaterial.CompileConfig config = SimpleMaterial.CompileConfig.load(contentName);
                                Material texture = new SimpleMaterial(textureFilename, imageFilename, config);         // Precompile texture
                                File.saveHints(textureFilename, texture);
                            } finally {
                                tile.dispose();         // not used anymore for now
                            }
                            // Update content buffer
                            ContentSprite content = new ContentSprite(
                                    contentName,
                                    contents.size,
                                    x, y, tileWidth, tileHeight,
                                    width, height
                            );
                            contents.add(content);
                            // Reset found buffer for this region
                            for (int v2 = boundsTopV; v2 <= boundsBottomV; v2++) {
                                for (int h2 = boundsLeftH; h2 <= boundsRightH; h2++) {
                                    contentFound[v2][h2] = false;
                                }
                            }
                            // Mark and try again
                            groupFound = true;
                            break;
                        }
                        if (groupFound)
                            break;
                    }
                    if (!groupFound)
                        break;          // no groups were found
                    // Else try again
                }
            }
            // Convert content buffer
            contentSprites = contents.toArray();
            Sys.debug(TAG, "Prepared " + contents.size + " content meshes");

        } finally {
            // Clear all memory
            if(layoutPixmap != null)
                layoutPixmap.dispose();
            if(contentPixmap != null)
                contentPixmap.dispose();
            // Clear all tiles
            for (int c = 0; c < tiles.size; c++)
                tiles.items[c].dispose();
            tiles.clear();
        }

        // Now for regions
        int cacheColor = 0xffffffff;
        Rectangle cacheRegion = null;
        IntMap<Rectangle> regions = new IntMap<Rectangle>();
        for(int c = 0; c < model.numRegionFiles; c++) {
            String filename = model.filename + REGION_FILE_SUFFIX + c + PNG_SUFFIX;
            Pixmap regionPixmap = new Pixmap(File.open(filename));
            try {
                int width = regionPixmap.getWidth();
                int height = regionPixmap.getHeight();
                for(int y = 0; y < height; y++) {
                    for(int x = 0; x < width; x++) {
                        int color = regionPixmap.getPixel(x, y);
                        if(color == 0xffffffff)
                            continue;           // ignore white
                        // Else expand region
                        if(color != cacheColor) {
                            cacheColor = color;
                            cacheRegion = regions.get(color);
                            if(cacheRegion == null) {
                                cacheRegion = new Rectangle(x, y, 1, 1);
                                regions.put(color, cacheRegion);
                                continue;
                            }
                        }
                        cacheRegion.merge(x,y);
                    }
                }
            } finally {
                regionPixmap.dispose();
            }
        }

        // Texts
        this.texts = new LayoutText[model.texts.length];
        ObjectMap<String, Font> fonts = new ObjectMap<String, Font>();
        for(int c = 0; c < model.texts.length; c++ ) {
            LayoutModel.LayoutTextModel textModel = model.texts[c];
            // Find region
            int regionColor = parseColor(textModel.region);
            Rectangle region = regions.get(regionColor);
            if(region == null)
                throw new RuntimeException("Region " + textModel.region + " not found for text \"" + textModel.text + "\"");
            LayoutText text = new LayoutText(
                    fonts,
                    textModel.font,
                    textModel.fontSize,
                    textModel.fontColor,
                    region.x, region.y, region.width, region.height,
                    width, height,
                    textModel.alignment,
                    textModel.text,
                    textModel.wrapChars,
                    textModel.startAnim, textModel.idleAnim, textModel.endAnim
            );
            texts[c] = text;
        }

        // Links
        this.links = new LayoutLink[model.links.length];
        for(int c = 0; c < model.links.length; c++) {
            LayoutModel.LayoutLink layoutModel = model.links[c];
            // Find region
            int regionColor = parseColor(layoutModel.region);
            Rectangle region = regions.get(regionColor);
            if(region == null)
                throw new RuntimeException("Region " + layoutModel.region + " not found for action \"" + layoutModel.action + "\"");
            LayoutLink link = new LayoutLink(
                    region.x, region.y, region.width, region.height,
                    width, height,
                    layoutModel.action
            );
            links[c] = link;
        }

        // Inputs
        this.inputs = new LayoutInput[model.inputs.length];
        for(int c = 0; c < model.inputs.length; c++ ) {
            LayoutModel.LayoutInput inputModel = model.inputs[c];
            // Find region
            int regionColor = parseColor(inputModel.region);
            Rectangle region = regions.get(regionColor);
            if(region == null)
                throw new RuntimeException("Region " + inputModel.region + " not found for input \"" + inputModel.name + "\"");
            LayoutInput input = new LayoutInput(
                    inputModel.name,
                    fonts,
                    inputModel.font,
                    inputModel.fontSize,
                    inputModel.fontColor,
                    region.x, region.y, region.width, region.height,
                    width, height,
                    inputModel.wrapChars,
                    inputModel.confirmText, inputModel.confirmAction,
                    inputModel.inputType,
                    inputModel.inputPaddingLeft, inputModel.inputPaddingTop, inputModel.inputPaddingRight, inputModel.inputPaddingBottom
            );
            inputs[c] = input;
        }

        // Checkboxes
        this.checkboxes = new LayoutCheckbox[model.checkboxes.length];
        for(int c = 0; c < model.checkboxes.length; c++ ) {
            LayoutModel.LayoutCheckbox inputCheckbox = model.checkboxes[c];
            // Find region
            int regionColor = parseColor(inputCheckbox.region);
            Rectangle region = regions.get(regionColor);
            if(region == null)
                throw new RuntimeException("Region " + inputCheckbox.region + " not found for checkbox \"" + inputCheckbox.name + "\"");
            LayoutCheckbox checkbox = new LayoutCheckbox(
                    inputCheckbox.name,
                    inputCheckbox.groupName,
                    inputCheckbox.color,
                    inputCheckbox.action,
                    region.x, region.y, region.width, region.height,
                    width, height,
                    inputCheckbox.inputPaddingLeft, inputCheckbox.inputPaddingTop, inputCheckbox.inputPaddingRight, inputCheckbox.inputPaddingBottom
            );
            checkboxes[c] = checkbox;
        }

        this.trigger = model.trigger;
    }


    @MassConstructor
    public LayoutDescriptor(String layoutName, String contentName, int bgColor, int width, int height, int sectionSize, int verticalSections, int horizontalSections, LayoutMesh[] layoutMeshes, ContentSprite[] contentSprites, LayoutText[] texts, LayoutLink[] links, LayoutInput[] inputs, LayoutCheckbox[] checkboxes, String trigger) {
        this.layoutName = layoutName;
        this.contentName = contentName;
        this.bgColor = bgColor;
        this.width = width;
        this.height = height;
        this.sectionSize = sectionSize;
        this.verticalSections = verticalSections;
        this.horizontalSections = horizontalSections;
        this.layoutMeshes = layoutMeshes;
        this.contentSprites = contentSprites;
        this.texts = texts;
        this.links = links;
        this.inputs = inputs;
        this.trigger = trigger;
        this.checkboxes = checkboxes;
    }


    @Override
    public Object[] mass() {
        return new Object[] { layoutName, contentName, bgColor, width, height, sectionSize, verticalSections, horizontalSections, layoutMeshes, contentSprites, texts, links, inputs, checkboxes, trigger };
    }

}
