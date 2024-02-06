package sengine.ui;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.regex.Pattern;

import sengine.Universe;
import sengine.animation.Animation;
import sengine.graphics2d.Font;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Matrices;
import sengine.mass.Mass;

/**
 * A single line inputfield that expands indefinitely
 */
public class InputField extends UIElement<Universe> {

    private static final Pattern linebreakPattern = Pattern.compile("\\r?\\n");


    public interface TextDecorator {
        void renderBackground(Universe v, float r, float renderTime);
        void renderOverlay(Universe v, float r, float renderTime);
    }

    public static final float DEFAULT_INTERVAL = 0.5f;

    public static String defaultCursor1(Font font) {
        return "[" + font.name() + "]|[]";
    }

    public static String defaultCursor2(Font font) {
        String transparentFontName = font.name() + "$transparent";
        font.color(transparentFontName, 0x00000000);               // TODO: This affects global fonts, find better way

        return "[" + transparentFontName + "]|[]";
    }

    private Font font;
    private float wrapChars = -4.5f;
    private int maxLines = -1;
    private int target;
    private String cursor1;
    private String cursor2;
    private float tCursorInterval = DEFAULT_INTERVAL;

    private String text;
    private String text1;
    private String text2;

    private TextDecorator decorator = null;


    private boolean enabled = false;
    private boolean passThroughInput = false;
    private boolean isCursorAnimated = true;

    // Input padding
    private float inputPaddingLeft = 0;
    private float inputPaddingTop = 0;
    private float inputPaddingRight = 0;
    private float inputPaddingBottom = 0;

    private int alignment = Align.left | Align.top;

    // Current
    private float width;
    private boolean refreshRequired = true;

    private int touchedPointer = -1;

    public boolean isCursorAnimated() {
        return isCursorAnimated;
    }

    public InputField alignment(int alignment) {
        this.alignment = alignment;
        return this;
    }

    public InputField target(int target) {
        this.target = target;
        return this;
    }

    public InputField maxLines(int maxLines) {
        this.maxLines = maxLines;
        return this;
    }

    public int target() {
        return target;
    }

    public InputField animateCursor(boolean animate) {
        isCursorAnimated = animate;
        return this;
    }

    public InputField inputPadding(float left, float top, float right, float bottom) {
        inputPaddingLeft = left;
        inputPaddingTop = top;
        inputPaddingRight = right;
        inputPaddingBottom = bottom;
        return this;
    }


    public InputField decorator(TextDecorator decorator) {
        this.decorator = decorator;
        return this;
    }

    public InputField font(Font font, float length, float wrapChars, int target) {
        if(font != null) {
            this.font = font;

            String transparentFontName = font.name() + "$transparent";
            font.color(transparentFontName, 0x00000000);               // TODO: This affects global fonts, find better way

            this.cursor1 = defaultCursor1(font);
            this.cursor2 = defaultCursor2(font);
        }

        this.length = length;
        this.wrapChars = wrapChars;
        this.target = target;
        refreshRequired = true;
        return this;
    }

    public InputField cursorInterval(float tCursorInterval) {
        this.tCursorInterval = tCursorInterval;
        return this;
    }

    public InputField cursor(String cursor1, String cursor2, float tCursorInterval) {
        this.cursor1 = cursor1;
        this.cursor2 = cursor2;
        this.tCursorInterval = tCursorInterval;
        refreshRequired = true;
        return this;
    }

    public InputField text(String text) {
        this.text = text;
        if(text == null || text.isEmpty()) {
            text1 = cursor1;
            text2 = cursor2;
        }
        else {
            // Update max lines
            if(maxLines != -1) {
                text = font.wrap(text, wrapChars);
                String[] lines = linebreakPattern.split(text);
                int start = lines.length - maxLines;
                if(start < 0)
                    start = 0;
                StringBuilder sb = new StringBuilder();
                for(int c = start; c < lines.length; c++) {
                    if(sb.length() > 0)
                        sb.append("\n");
                    sb.append(lines[c]);
                }
                text = sb.toString();
            }
            text1 = text + cursor1;
            text2 = text + cursor2;
        }
        refreshRequired = true;
        return this;
    }

    public String text() {
        return text;
    }

    public float wrapChars() {
        return wrapChars;
    }

    public InputField autoLengthText(String text) {
        text(text);
        autoLength();
        return this;
    }

    public InputField autoLength() {
        Rectangle bounds = font.getBounds(text2, wrapChars, false);
        length = bounds.height;
        return this;
    }

    public Rectangle calculateTextBounds(String text) {
        Rectangle bounds = font.getBounds(text, wrapChars, false);
        float scale = length / bounds.height;
        if(scale > 1f)
            scale = 1f;
        bounds.width *= scale;
        bounds.height *= scale;
        return bounds;
    }

    public InputField refresh() {
        // Mark refreshed
        refreshRequired = false;
        if(font == null || text1 == null)
            return this;     // nothing to refresh

        // Calculate text length
        Rectangle bounds = calculateTextBounds(text1);
        width = bounds.width;

        return this;
    }

    public InputField passThroughInput(boolean passThroughInput) {
        this.passThroughInput = passThroughInput;
        return this;
    }

    public InputField disable() {
        enabled = false;
        return this;
    }

    public InputField enable() {
        enabled = true;
        return this;
    }

    public InputField() {
        // default
    }

    @MassConstructor
    public InputField(Metrics metrics, String name, float length, UIElement<?>[] childs,
                      String cursor1, String cursor2, float tCursorInterval,
                      Font font, float wrapChars, int target, int maxLines, String text,
                      boolean enabled, boolean passThroughInput, boolean isCursorAnimated,
                      float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom,
                      int alignment,
                      TextDecorator decorator
    ) {
        super(metrics, name, length, childs);

        cursor(cursor1, cursor2, tCursorInterval);
        font(font, length, wrapChars, target);
        maxLines(maxLines);
        text(text);
        if(enabled)
            enable();
        passThroughInput(passThroughInput);
        animateCursor(isCursorAnimated);
        inputPadding(inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom);
        alignment(alignment);
        decorator(decorator);
        refresh();
    }

    @Override
    public Object[] mass() {
        return Mass.concat(super.mass(),
                // Cursor
                cursor1, cursor2, tCursorInterval,
                // Font and text
                font, wrapChars, target, maxLines, text,
                enabled, passThroughInput, isCursorAnimated,
                // input padding
                inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom,
                alignment,
                // Decorator
                decorator
        );
    }

    @Override
    public InputField instantiate() {
        InputField field = new InputField();
        field.name(name);
        field.viewport(viewport);
        if(metrics != null)
            field.metrics(metrics.instantiate());
        field.cursor(cursor1, cursor2, tCursorInterval);
        field.font(font, length, wrapChars, target);
        field.maxLines(maxLines);
        field.text(text);
        if(enabled)
            field.enable();
        field.passThroughInput(passThroughInput);
        field.inputPadding(inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom);
        field.alignment(alignment);
        field.decorator(decorator);
        field.refresh();
        field.instantiateChilds(this);
        return field;
    }

    @Override
    public InputField viewport(UIElement<?> viewport) {
        super.viewport(viewport);
        return this;
    }

    @Override
    public InputField name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public InputField windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
        super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
        return this;
    }

    @Override
    public InputField metrics(Metrics metrics) {
        super.metrics(metrics);
        return this;
    }

    @Override
    public InputField attach() {
        super.attach();
        return this;
    }

    @Override
    public InputField attach(int index) {
        super.attach(index);
        return this;
    }

    @Override
    protected void recreate(Universe v) {
        super.recreate(v);

        touchedPointer = -1;
    }

    @Override
    protected void render(Universe v, float r, float renderTime) {
        calculateWindow();

        if(font == null || text1 == null || text == null)
            return;     // not enough

        // Refresh if required
        if(refreshRequired)
            refresh();

        if(!isVisible())
            return;         // not visible

        applyWindowAnim();


        Matrix4 m = Matrices.model;
        Matrices.push();
        applyMatrix();

        float xoffset = 1f - width;

        if(xoffset > 0f)
            xoffset = 0;
        else
            Matrices.scissor.set(childX, childY, childScaleX, camera.viewportHeight);        // scissor viewport because height does not include characters like j, g, q, y
        m.translate(-0.5f + xoffset, +length / 2f, 0f);


        String text;
        if(isCursorAnimated) {
            if ((((int) (renderTime / tCursorInterval)) % 2) == 1)
                text = text1;
            else
                text = text2;
        }
        else
            text = text2;

        Matrices.target = target;

        if(decorator != null)
            decorator.renderBackground(v, r, renderTime);
        font.render(text, length, wrapChars, alignment, false);
        if(decorator != null)
            decorator.renderOverlay(v, r, renderTime);

        Matrices.pop();
    }

    @Override
    protected boolean input(Universe v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(!renderingEnabled || camera == null)
            return false;
        // Adjust touch position according to camera
        if(enabled && (inputType & INPUT_TOUCH) != 0) {
            x += camera.position.x;
            y += camera.position.y;
        }
        else
            return false;

        switch(inputType) {
            case INPUT_TOUCH_DOWN:
                if(checkTouched(x, y, inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom)) {
                    touchedPointer = pointer;
                    return !passThroughInput;
                }
                return false;

            case INPUT_TOUCH_DRAGGED:
                if(touchedPointer != pointer)
                    return false;		// not right pointer
                return !passThroughInput;

            case INPUT_TOUCH_UP:
                if(touchedPointer != pointer)
                    return false;		// not right pointer
                touchedPointer = -1;
                if(checkTouched(x, y, inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom)) {
                    activated(v, button);
                }
                return !passThroughInput;

            default:
                return false;
        }
    }

    public void activated(Universe v, int button) {
        OnClick callback = findParent(OnClick.class);
        if(callback != null)
            callback.onClick(v, this, button);
    }

    @Override
    public <A extends MaterialAttribute> A getAttribute(Class<A> attribType, int layer) {
        return font.getAttribute(attribType, layer);
    }
}
