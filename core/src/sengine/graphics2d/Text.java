package sengine.graphics2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

import sengine.Sys;
import sengine.mass.MassSerializable;

/**
 * Created by Azmi on 28/7/2016.
 */
public class Text implements MassSerializable {
    private static final String TAG = "Text";

    public static final String DEFAULT_ELLIPSIS = "...";

    public Font font = null;
    public int target = -1;
    public String text = null;
    public int maxLines = -1;
    public String ellipsis = DEFAULT_ELLIPSIS;
    public float x = 0;
    public float y = 0;
    public float size = 1f;
    public float length = 1f;
    public float wrapChars = 0;
    public int align = Align.center;            // default align center

    public Text text(String text) {
        if(maxLines != -1 && text != null) {
            if(font == null)
                throw new RuntimeException("Font not set!");
            if(wrapChars < 1)
                wrapChars = -wrapChars;
            String ellipsized = font.ellipsize(text, wrapChars, maxLines, ellipsis);
            wrapChars = -wrapChars;
            // Try to retain formating
//            if(ellipsized.endsWith(ellipsis)) {
//                int length = ellipsized.length() - ellipsis.length();
//                int offset = 0;
//                for (int c = 0; c < length; c++) {
//                    char ch = ellipsized.charAt(c);
//                    int nextOffset = text.indexOf(ch, offset) + 1;
//                    if(nextOffset == -1)
//                        break;
//                    offset = nextOffset;
////                    if(offset == 0) {
////                        // Something is wrong, should be impossible to reach here
////                        Sys.error(TAG, "Unable to ellipsize \"" + text + "\" with \"" + ellipsis + "\"");
////                        this.text = text;
////                        return this;
////                    }
//                }
//
//            }
            int length = ellipsized.length();
            boolean endsWithEllipsis = ellipsized.endsWith(ellipsis);
            if(endsWithEllipsis)
                length -= ellipsis.length();
            int offset = 0;
            for (int c = 0; c < length; c++) {
                char ch = ellipsized.charAt(c);
                int nextOffset = text.indexOf(ch, offset);
                if (nextOffset == -1)
                    break;
                offset = nextOffset + 1;
            }
            if(endsWithEllipsis)
                text = text.substring(0, offset) + ellipsis;
            else
                text = text.substring(0, offset);
        }
        this.text = text;
        return this;
    }

    public Text font(Font font) {
        this.font = font;
        return this;
    }

    public Text font(Font font, int target) {
        font(font);
        target(target);
        return this;
    }

    public Text target(int target) {
        this.target = target;
        return this;
    }

    public Rectangle bounds() {
        if(font == null)
            throw new RuntimeException("Font not set!");
        if(text == null)
            throw new RuntimeException("text not set!");

        Rectangle rect = font.getBounds(text, wrapChars, true);

        // Enforce length
        if(rect.height > length) {
            float scale = length / rect.height;
            rect.width *= scale;
            rect.height *= scale;
        }

        return rect;
    }

    public Text wrap() {
        if(font == null)
            throw new RuntimeException("Font not set!");
        if(text == null)
            throw new RuntimeException("text not set!");
        text = font.wrap(text, wrapChars);
        return this;
    }

    public Text escape() {
        if(text != null)
            text = Fonts.escapeMarkup(text);
        return this;
    }

    public Text ellipsize(int maxLines) {
        return ellipsize(maxLines, ellipsis);
    }

    public Text ellipsize(int maxLines, String ellipsis) {
        this.maxLines = maxLines;
        this.ellipsis = ellipsis;
        return this;
    }

    public Text topLeft() {
        align = Align.top | Align.left;
        return this;
    }

    public Text topCenter() {
        align = Align.top | Align.center;
        return this;
    }

    public Text topRight() {
        align = Align.top | Align.right;
        return this;
    }

    public Text centerLeft() {
        align = Align.left;
        return this;
    }

    public Text center() {
        align = Align.center;
        return this;
    }

    public Text centerRight() {
        align = Align.right;
        return this;
    }

    public Text bottomLeft() {
        align = Align.bottom | Align.left;
        return this;
    }

    public Text bottomCenter() {
        align = Align.bottom | Align.center;
        return this;
    }

    public Text bottomRight() {
        align = Align.bottom | Align.right;
        return this;
    }

    public Text align(int align) {
        this.align = align;
        return this;
    }

    public Text wrapChars(float wrapChars) {
        this.wrapChars = wrapChars;
        return this;
    }

    public Text length(float length) {
        this.length = length;
        return this;
    }

    public Text autoLength() {
        if(font == null)
            throw new RuntimeException("Font not set!");
        if(text == null) {
            length = 0f;
            return this;
        }
        length = Float.MAX_VALUE;       // allow max first
        Rectangle bounds = bounds();
        length = bounds.height;         // trim
        return this;
    }

    public Text position(float length, float wrapChars) {
        return position(0, 0, 1f, length, wrapChars);
    }

    public Text position(float size, float length, float wrapChars) {
        return position(0, 0, size, length, wrapChars);
    }

    public Text position(float x, float y, float size, float length, float wrapChars) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.length = length;
        this.wrapChars = wrapChars;

        return this;
    }


    public void render() {
        if(text == null || text.isEmpty())
            return;         // nothing to render
        if(font == null)
            throw new RuntimeException("Font not set");
        Matrices.push();
        Matrix4 m = Matrices.model;

        m.translate(x - (size / 2f), y + (length * size / 2f), 0);
        m.scale(size, size, size);

        if(target >= 0)
            Matrices.target = target;

        font.render(text, length, wrapChars, align, true);

        Matrices.pop();
    }


    public Text() {
        // nothing
    }

    @MassConstructor
    public Text(Font font, int target, String text, int maxLines, String ellipsis, float x, float y, float size, float length, float wrapChars, int align) {
        font(font);
        target(target);
        position(x, y, size, length, wrapChars);
        ellipsize(maxLines, ellipsis);
        align(align);
        text(text);
    }

    @Override
    public Object[] mass() {
        return new Object[] { font, target, text, maxLines, ellipsis, x, y, size, length, wrapChars, align};
    }

    public Text instantiate() {
        return new Text(font, target, text, maxLines, ellipsis, x, y, size, length, wrapChars, align);
    }

}
