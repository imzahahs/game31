package sengine.ui;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

import sengine.Entity;
import sengine.Universe;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.graphics2d.Font;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.PatchedSprite;
import sengine.mass.Mass;

/**
 * Created by Azmi on 30/6/2016.
 */
public class PatchedTextBox extends UIElement<Universe> {

    String patchFilename;
    int target;

    float patchCornerSize;
    float patchCornerLeftSize;
    float patchCornerRightSize;
    float patchCornerTopSize;
    float patchCornerBottomSize;
    float minWidth = -1;
    float minHeight = -1;
    float leftPadding;
    float topPadding;
    float rightPadding;
    float bottomPadding;

    Font font;
    float wrapChars;
    float maxLength;
    int fontTarget;
    int align = Align.center;            // default align center


    Animation.Instance startAnim;
    Animation.Loop idleAnim;
    Animation.Instance pressedAnim;
    Animation.Instance releasedAnim;
    Animation.Instance endAnim;

    private Audio.Sound activateSound;

    String text;


    // Input padding
    private float inputPaddingLeft = 0;
    private float inputPaddingTop = 0;
    private float inputPaddingRight = 0;
    private float inputPaddingBottom = 0;

    // Input
    boolean passThroughInput = false;
    float maxTouchMoveDistance = Float.MAX_VALUE;

    // Current state
    int touchPressing = -1;
    int touchedPointer = -1;
    float touchX;
    float touchY;
    boolean enabled = false;

    // Current
    private float metricsScaleX = 1f;
    private float metricsScaleY = 1f;
    PatchedSprite bg;
    float textWidth;
    float textSize;
    float textOffset;
    float textLength;
    boolean refreshRequired = false;

    public int touchedPointer() {
        return touchedPointer;
    }

    public PatchedTextBox inputPadding(float left, float top, float right, float bottom) {
        inputPaddingLeft = left;
        inputPaddingTop = top;
        inputPaddingRight = right;
        inputPaddingBottom = bottom;
        return this;
    }

    public PatchedTextBox sound(Audio.Sound activateSound) {
        this.activateSound = activateSound;
        return this;
    }

    public PatchedTextBox topLeft() {
        align = Align.top | Align.left;
        return this;
    }

    public PatchedTextBox topCenter() {
        align = Align.top | Align.center;
        return this;
    }

    public PatchedTextBox topRight() {
        align = Align.top | Align.right;
        return this;
    }

    public PatchedTextBox centerLeft() {
        align = Align.left;
        return this;
    }

    public PatchedTextBox center() {
        align = Align.center;
        return this;
    }

    public PatchedTextBox centerRight() {
        align = Align.right;
        return this;
    }

    public PatchedTextBox bottomLeft() {
        align = Align.bottom | Align.left;
        return this;
    }

    public PatchedTextBox bottomCenter() {
        align = Align.bottom | Align.center;
        return this;
    }

    public PatchedTextBox bottomRight() {
        align = Align.bottom | Align.right;
        return this;
    }

    public PatchedTextBox align(int align) {
        this.align = align;
        return this;
    }

    public PatchedTextBox padding(float leftPadding, float topPadding, float rightPadding, float bottomPadding) {
        this.leftPadding = leftPadding;
        this.topPadding = topPadding;
        this.rightPadding = rightPadding;
        this.bottomPadding = bottomPadding;
        refreshRequired = true;
        return this;
    }

    public Font font() {
        return font;
    }

    public PatchedTextBox font(Font font) {
        this.font = font;
        refreshRequired = true;
        return this;
    }

    public PatchedTextBox font(Font font, float wrapChars, float maxLength, int fontTarget) {
        this.font = font;
        this.wrapChars = wrapChars;
        this.maxLength = maxLength;
        this.fontTarget = fontTarget;
        refreshRequired = true;
        return this;
    }

    public String text() {
        return text;
    }

    public PatchedTextBox text(String text) {
        text(text, true);
        return this;
    }

    public PatchedTextBox text(String text, boolean requireRefresh) {
        // text = font.wrapLines(text, wrapChars);       TODO: is this required???
        this.text = text;
        refreshRequired = requireRefresh;
        return this;
    }

    public String visualName() {
        return patchFilename;
    }

    public int target() {
        return target;
    }

    public PatchedTextBox target(int target) {
        this.target = target;
        return this;
    }

    public int fontTarget() {
        return fontTarget;
    }

    public PatchedTextBox fontTarget(int target) {
        this.fontTarget = target;
        return this;
    }

    public float maxLength() {
        return maxLength;
    }

    public PatchedSprite visual() {
        return bg;
    }

    public PatchedTextBox visual(String filename, float cornerSize, int target) {
        return visual(filename, cornerSize, cornerSize, cornerSize, cornerSize, cornerSize, target);
    }

    public PatchedTextBox visual(String filename, float cornerSize, float cornerLeftSize, float cornerRightSize, float cornerTopSize, float cornerBottomSize, int target) {
        visual(filename, target);
        this.patchCornerSize = cornerSize;
        this.patchCornerLeftSize = cornerLeftSize;
        this.patchCornerRightSize = cornerRightSize;
        this.patchCornerTopSize = cornerTopSize;
        this.patchCornerBottomSize = cornerBottomSize;
        refreshRequired = true;
        return this;
    }

    public PatchedTextBox visual(String filename, int target) {
        PatchedSprite.load(filename);        // load first
        this.patchFilename = filename;
        this.target = target;
        bg = null;      // Clear
        refreshRequired = true;
        return this;
    }

    public PatchedTextBox visual(String filename) {
        visual(filename, target);
        return this;
    }

    public PatchedTextBox minSize(float minWidth, float minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        return this;
    }

    public float leftPaddingSize() {
        return leftPadding * patchCornerSize;
    }

    public float rightPaddingSize() {
        return rightPadding * patchCornerSize;
    }

    public float topPaddingSize() {
        return topPadding * patchCornerSize;
    }

    public float bottomPaddingSize() {
        return bottomPadding * patchCornerSize;
    }

    public float horizontalPaddingSize() {
        return (leftPadding + rightPadding) * patchCornerSize;
    }

    public float verticalPaddingSize() {
        return (topPadding + bottomPadding) * patchCornerSize;
    }


    public PatchedTextBox refresh() {
        if(patchFilename == null)
            return this;            // not enough info
        // Get text bounds if available
        float textHeight;
        if(font == null || text == null) {
            textWidth = 0;
            textHeight = 0;
        }
        else {
            Rectangle bounds = font.getBounds(text, wrapChars, true);
            textWidth = bounds.width;
            textHeight = bounds.height;
        }

        // Calculate patch dimensions based on text bounds
        float horizontalPadding = horizontalPaddingSize();
        float verticalPadding = verticalPaddingSize();

        // Add padding
        float width = textWidth + horizontalPadding;
        float height = textHeight + verticalPadding;

        float minWidth = this.minWidth + horizontalPadding;
        float minHeight = this.minHeight + verticalPadding;
        if(width < minWidth) {
            textOffset = minWidth - width;
            width = minWidth;
        }
        else
            textOffset = 0f;

        if(height < minHeight)
            height = minHeight;

        // Adjust element size to negate padding
        metrics.scaleX = metricsScaleX * width;
        metrics.scaleY = metricsScaleY * width;

        // Create bg, maintain corner sizes relative to element size
        float actualCornerLeftSize = patchCornerLeftSize / width;
        float actualCornerRightSize = patchCornerRightSize / width;
        float actualCornerTopSize = patchCornerTopSize / width;
        float actualCornerBottomSize = patchCornerBottomSize / width;
        // Calculate minimum height - text can be smaller than patched sprite's minimum height
        float minLength = actualCornerTopSize + actualCornerBottomSize;
        // Choose height and create sprite
        length = Math.max(height / width, minLength);

        // Check if need to refresh bg
        if(bg != null) {
            if(bg.length != length || bg.cornerLeftSize != actualCornerLeftSize || bg.cornerRightSize != actualCornerRightSize || bg.cornerTopSize != actualCornerTopSize || bg.cornerBottomSize != actualCornerBottomSize)
                bg = PatchedSprite.create(patchFilename, length, actualCornerLeftSize, actualCornerRightSize, actualCornerTopSize, actualCornerBottomSize);         // different metrics
        }
        else
            bg = PatchedSprite.create(patchFilename, length, actualCornerLeftSize, actualCornerRightSize, actualCornerTopSize, actualCornerBottomSize);             // first time

        // Due to min height, recalculate textHeight
        textHeight = length * width;
        textHeight -= verticalPadding;

        // Keep text dimensions
        textSize = width;
        textLength = textHeight;

        // Mark refreshed
        refreshRequired = false;
        return this;
    }

    public PatchedTextBox animation(Animation startAnim, Animation idleAnim, Animation endAnim) {
        return animation(startAnim, idleAnim, null, null, endAnim);
    }

    public PatchedTextBox animation(Animation startAnim, Animation idleAnim, Animation pressedAnim, Animation releasedAnim, Animation endAnim) {
        this.startAnim = startAnim != null ? startAnim.start() : null;
        this.idleAnim = idleAnim != null ? idleAnim.loop() : null;
        if(this.idleAnim != null)
            this.idleAnim.reset();
        this.pressedAnim = pressedAnim != null ? pressedAnim.start() : null;
        this.releasedAnim = releasedAnim != null ? releasedAnim.start() : null;
        this.endAnim = endAnim != null ? endAnim.start() : null;
        return this;
    }


    public PatchedTextBox passThroughInput(boolean passThroughInput) {
        this.passThroughInput = passThroughInput;
        return this;
    }

    public PatchedTextBox maxTouchMoveDistance(float maxTouchMoveDistance) {
        this.maxTouchMoveDistance = maxTouchMoveDistance;
        return this;
    }

    public PatchedTextBox disable() {
        cancelTouch();
        enabled = false;
        return this;
    }

    public PatchedTextBox enable() {
        enabled = true;
        return this;
    }

    public PatchedTextBox() {
        // default
    }

    @MassConstructor
    public PatchedTextBox(Metrics metrics, String name, float length, UIElement<?>[] childs,
                          String patchFilename, float cornerSize, float cornerLeftSize, float cornerRightSize, float cornerTopSize, float cornerBottomSize, int target,
                          float minWidth, float minHeight,
                          Font font, float wrapChars, float maxLength, int fontTarget, int align,
                          float leftPadding, float topPadding, float rightPadding, float bottomPadding,
                          Animation startAnim, Animation idleAnim, Animation pressedAnim, Animation releasedAnim, Animation endAnim,
                          String text,
                          Audio.Sound activateSound,
                          boolean enabled, boolean passThroughInput, float maxTouchMoveDistance,
                          float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom
    ) {
        super(metrics, name, length, childs);

        visual(patchFilename, cornerSize, cornerLeftSize, cornerRightSize, cornerTopSize, cornerBottomSize, target);
        minSize(minWidth, minHeight);
        font(font, wrapChars, maxLength, fontTarget);
        align(align);
        padding(leftPadding, topPadding, rightPadding, bottomPadding);
        animation(startAnim, idleAnim, pressedAnim, releasedAnim, endAnim);
        if(text != null)
            text(text);
        sound(activateSound);
        if(enabled)
            enable();
        passThroughInput(passThroughInput);
        maxTouchMoveDistance(maxTouchMoveDistance);
        inputPadding(inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom);
        refresh();
    }

    @Override
    public Object[] mass() {
        return Mass.concat(super.mass(),
                // Visuals
                patchFilename, patchCornerSize, patchCornerLeftSize, patchCornerRightSize, patchCornerTopSize, patchCornerBottomSize, target,
                minWidth, minHeight,
                font, wrapChars, maxLength, fontTarget, align,
                leftPadding, topPadding, rightPadding, bottomPadding,
                // Animation
                startAnim != null ? startAnim.anim : null,
                idleAnim != null ? idleAnim.anim : null,
                pressedAnim != null ? pressedAnim.anim : null,
                releasedAnim != null ? releasedAnim.anim : null,
                endAnim != null ? endAnim.anim : null,
                // Text
                text,
                // Sounds
                activateSound,
                // Input
                enabled, passThroughInput, maxTouchMoveDistance,
                // input padding
                inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom
        );
    }

    @Override
    public PatchedTextBox instantiate() {
        PatchedTextBox textBox = new PatchedTextBox();
        textBox.name(name);
        textBox.viewport(viewport);
        textBox.metrics(metrics.instantiate());
        textBox.visual(patchFilename, patchCornerSize, patchCornerLeftSize, patchCornerRightSize, patchCornerTopSize, patchCornerBottomSize, target);
        textBox.align(align);
        textBox.minSize(minWidth, minHeight);
        textBox.font(font, wrapChars, maxLength, fontTarget);
        textBox.passThroughInput(passThroughInput);
        textBox.maxTouchMoveDistance(maxTouchMoveDistance);
        textBox.inputPadding(inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom);
        textBox.animation(
                startAnim != null ? startAnim.anim : null,
                idleAnim != null ? idleAnim.anim : null,
                pressedAnim != null ? pressedAnim.anim : null,
                releasedAnim != null ? releasedAnim.anim : null,
                endAnim != null ? endAnim.anim : null
        );
        textBox.padding(leftPadding, topPadding, rightPadding, bottomPadding);
        if(text != null)
            textBox.text(text);
        textBox.sound(activateSound);
        if(isEnabled())
            textBox.enable();
        textBox.refresh();
        textBox.instantiateChilds(this);
        return textBox;
    }



    @Override
    public PatchedTextBox viewport(UIElement<?> viewport) {
        super.viewport(viewport);
        return this;
    }

    @Override
    public PatchedTextBox name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public PatchedTextBox windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
        super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
        return this;
    }

    @Override
    public PatchedTextBox metrics(Metrics metrics) {
        super.metrics(metrics);
        if(metrics != null) {
            metricsScaleX = metrics.scaleX;
            metricsScaleY = metrics.scaleY;
        }
        else
            metricsScaleX = metricsScaleY = 1f;
        return this;
    }

    @Override
    public PatchedTextBox attach() {
        super.attach();
        return this;
    }

    @Override
    public PatchedTextBox attach(int index) {
        super.attach(index);
        return this;
    }


    @Override
    protected void render(Universe v, float r, float renderTime) {
        if(patchFilename == null)
            return;     // not enough
        // Refresh if required
        if(refreshRequired)
            refresh();

        // Process pressing
        if(touchPressing != -1)
            touchPressing(v, touchPressing);

        calculateWindow();

        if(!isVisible())
            return;         // not visible

        Matrix4 m = Matrices.model;
        Matrices.push();

        // Process stages
        if(startAnim != null && startAnim.isActive())
            startAnim.updateAndApply(this, getRenderDeltaTime());
        else if(endAnim != null && endAnim.isActive() && !endAnim.updateAndApply(this, getRenderDeltaTime()))   // End anim
            detach();		// ended
        if(idleAnim != null)
            idleAnim.updateAndApply(this, getRenderDeltaTime());


        // Current press states
        if(pressedAnim != null && touchPressing != -1)
            pressedAnim.updateAndApply(this, getRenderDeltaTime());
        else if(releasedAnim != null && releasedAnim.isActive())
            releasedAnim.updateAndApply(this, getRenderDeltaTime());

        applyWindowAnim();

        applyMatrix();

        Matrices.target = target;

        bg.render();

        // Render text if available
        if(font != null && text != null) {

            m.translate(-0.5f, +length / 2f, 0f);
            float scale = 1f / textSize;
            m.scale(scale, scale, scale);

            if((align & Align.center) != 0)
                m.translate(-((1f - textWidth) / 2f) + (textOffset / 2f), 0, 0f);
            else if((align & Align.right) != 0) {
                m.translate(-(1f - textWidth) + textOffset, 0, 0f);
            }

            m.translate((+leftPadding * patchCornerSize), -topPadding * patchCornerSize, 0);

            Matrices.target = fontTarget;

            font.render(text, textLength, wrapChars, align, true);
        }

        Matrices.pop();
    }

    @Override
    public void attach(Entity<?> parent, int index) {
        // Check if is detaching
        if(endAnim != null && endAnim.isActive()) {
            // Reset end anim
            endAnim.stop();
            if(startAnim != null)
                startAnim.reset();
        }
        super.attach(parent, index);
    }

    @Override
    public void detachWithAnim() {
        if(endAnim != null && endAnim.isActive())
            return;		// already not attached, or end animation is running
        if(startAnim != null)
            startAnim.stop();		// stop start anim
        if(endAnim != null && isEffectivelyAttached())
            endAnim.reset();
        else
            detach();
    }

    public void simulateTouch(Universe v, int pointer, int button) {
        cancelTouch();
        touchedPointer = pointer;
        touchX = childX;
        touchY = childY;
        touchPressed(v, touchX, touchY, button);
        touchPressing = button;
        if(pressedAnim != null)
            pressedAnim.reset();
        if(releasedAnim != null)
            releasedAnim.stop();
    }

    public void cancelTouch() {
        touchedPointer = -1;
        if(touchPressing != -1) {
            touchPressing = -1;
            if(releasedAnim != null)
                releasedAnim.reset();
            if(pressedAnim != null)
                pressedAnim.stop();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPressing() {
        return touchPressing != -1;
    }

    @Override
    protected void recreate(Universe v) {
        // Reset anim states
        if(startAnim != null)
            startAnim.reset();
        if(endAnim != null)
            endAnim.stop();
        if(pressedAnim != null)
            pressedAnim.stop();
        if(releasedAnim != null)
            releasedAnim.stop();
        touchedPointer = -1;
        touchPressing = -1;
    }

    @Override
    protected void release(Universe v) {
        // Make sure to detach if already detaching with anim
        if(endAnim != null && endAnim.isActive()) {
            endAnim.stop();
            detach();
        }
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
                    touchX = x;
                    touchY = y;
                    touchPressed(v, x, y, button);
                    if(touchPressing == -1)
                        touchPressing = button;
                    if(pressedAnim != null)
                        pressedAnim.reset();
                    if(releasedAnim != null)
                        releasedAnim.stop();
                    return !passThroughInput;
                }
                return false;

            case INPUT_TOUCH_DRAGGED:
                if(touchedPointer != pointer)
                    return false;		// not right pointer
                touchDragged(v, x, y, button);
                // Calculate distance
                float maxDistance2 = maxTouchMoveDistance * maxTouchMoveDistance;
                float deltaX = x - touchX;
                float deltaY = y - touchY;
                if(((deltaX * deltaX) + (deltaY * deltaY)) > maxDistance2) {
                    cancelTouch();
                    return !passThroughInput;
                }

                if(checkTouched(x,y, inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom)) {
                    if(touchPressing == -1) {
                        touchPressing = button;
                        if(pressedAnim != null)
                            pressedAnim.reset();
                        if(releasedAnim != null)
                            releasedAnim.stop();
                    }
                }
                else {
                    if(touchPressing != -1) {
                        touchPressing = -1;
                        if(releasedAnim != null)
                            releasedAnim.reset();
                        if(pressedAnim != null)
                            pressedAnim.stop();
                    }
                }
                return !passThroughInput;

            case INPUT_TOUCH_UP:
                if(touchedPointer != pointer)
                    return false;		// not right pointer
                touchedPointer = -1;
                touchReleased(v, x, y, button);
                if(checkTouched(x, y, inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom)) {
                    if(releasedAnim != null)
                        releasedAnim.reset();
                    if(pressedAnim != null)
                        pressedAnim.stop();
                    if(enabled)
                        activated(v, button);
                }
                touchPressing = -1;
                return !passThroughInput;

            default:
                return false;
        }
    }

    @Override
    public <T extends MaterialAttribute> T getAttribute(Class<T> attribType, int layer) {
        int selection = layer & 0x1;
        layer >>>= 1;
        if(selection == 1) {
            // Selecting font
            return font != null ? font.getAttribute(attribType, layer) : null;
        }
        // Background
        return bg.getAttribute(attribType, layer);
    }

    // Implementation should override these
    public void activated(Universe v, int button) {
        OnClick callback = findParent(OnClick.class);
        if(activateSound != null)
            activateSound.play();
        if(callback != null)
            callback.onClick(v, this, button);
    }
    public void touchPressed(Universe v, float x, float y, int button) {
        OnPressed callback = findParent(OnPressed.class);
        if(callback != null)
            callback.onPressed(v, this, x, y, button);
    }
    public void touchReleased(Universe v, float x, float y, int button) {
        OnReleased callback = findParent(OnReleased.class);
        if(callback != null)
            callback.onReleased(v, this, x, y, button);

    }
    public void touchDragged(Universe v, float x, float y, int button) {
        OnDragged callback = findParent(OnDragged.class);
        if(callback != null)
            callback.onDragged(v, this, x, y, button);
    }
    public void touchPressing(Universe v, int button) {
        OnPressing callback = findParent(OnPressing.class);
        if(callback != null)
            callback.onPressing(v, this, button);
    }
}
