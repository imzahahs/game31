package sengine.ui;

import sengine.Entity;
import sengine.Universe;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.mass.Mass;

public class Clickable extends UIElement<Universe> {
	static final String TAG = "Clickable";


    // Visuals
	Mesh buttonUpTex = null;
	Mesh buttonDownTex = null;
    int target = 0;
	private float visualScaleX = 1f;
	private float visualScaleY = 1f;
	Animation.Instance startAnim;
	Animation.Loop idleAnim;
	Animation.Instance pressedAnim;
	Animation.Instance releasedAnim;
	Animation.Instance endAnim;

	private Audio.Sound activateSound;
	private Audio.Sound pressSound;
    private Audio.Sound releaseSound;

	// Text
    Text text = null;

    // Input
    boolean passThroughInput = false;
    float maxTouchMoveDistance = Float.MAX_VALUE;

	// Input padding
	private float inputPaddingLeft = 0;
	private float inputPaddingTop = 0;
	private float inputPaddingRight = 0;
	private float inputPaddingBottom = 0;
	private boolean inputIgnoreAnimation = true;

	// Current state
    int touchPressing = -1;
	int touchedPointer = -1;
	float touchX;
	float touchY;
	boolean enabled = true;

	public Clickable inputIgnoreAnimation(boolean inputIgnoreAnimation) {
		this.inputIgnoreAnimation = inputIgnoreAnimation;
		return this;
	}

	public Clickable inputPadding(float left, float top, float right, float bottom) {
		inputPaddingLeft = left;
		inputPaddingTop = top;
		inputPaddingRight = right;
		inputPaddingBottom = bottom;
		return this;
	}
	
	public Mesh buttonUp() { return buttonUpTex; }
	public Mesh buttonDown() { return buttonDownTex; }

	public Clickable sound(Audio.Sound activateSound) {
		sound(activateSound, null, null);
        return this;
	}

    public Clickable sound(Audio.Sound activateSound, Audio.Sound pressSound, Audio.Sound releaseSound) {
        this.activateSound = activateSound;
        this.pressSound = pressSound;
        this.releaseSound = releaseSound;
        return this;
    }

    public Clickable passThroughInput(boolean passThroughInput) {
        this.passThroughInput = passThroughInput;
        return this;
    }

    public Clickable maxTouchMoveDistance(float maxTouchMoveDistance) {
        this.maxTouchMoveDistance = maxTouchMoveDistance;
        return this;
    }

    public Clickable target(int target) {
        this.target = target;
        return this;
    }

    public int target() {
        return target;
    }

	public Clickable visuals(Mesh buttonTex) {
		return visuals(buttonTex, buttonTex, target);
	}

    public Clickable visuals(Mesh buttonTex, int target) {
		return visuals(buttonTex, buttonTex, target);
	}
	
	public Clickable visuals(Mesh buttonUpTex, Mesh buttonDownTex, int target) {
		this.buttonUpTex = buttonUpTex;
		this.buttonDownTex = buttonDownTex;
		if(buttonUpTex != null)
			this.length = buttonUpTex.getLength();
		if(buttonDownTex != null)
			this.length = buttonDownTex.getLength();
		this.target = target;
        return this;
	}

    public Clickable visualScale(float scale) {
        return visualScale(scale, scale);
    }


    public Clickable visualScale(float scaleX, float scaleY) {
		this.visualScaleX = scaleX;
		this.visualScaleY = scaleY;
		return this;
	}

	public Clickable length(float length) {
		this.length = length;
		return this;
	}

    public Clickable animation(Animation startAnim, Animation idleAnim, Animation pressedAnim, Animation releasedAnim, Animation endAnim) {
        this.startAnim = startAnim != null ? startAnim.start() : null;
        this.idleAnim = idleAnim != null ? idleAnim.loop() : null;
        if(this.idleAnim != null)
            this.idleAnim.reset();
        this.pressedAnim = pressedAnim != null ? pressedAnim.start() : null;
        this.releasedAnim = releasedAnim != null ? releasedAnim.start() : null;
        this.endAnim = endAnim != null ? endAnim.start() : null;
        return this;
    }


	public Text text() {
		return text;
	}

	public Clickable text(String text) {
		if(this.text == null)
			throw new RuntimeException("text not set!");
		this.text.text(text);
		return this;
	}

    public Clickable text(Text text) {
        this.text = text;
        return this;
    }

    public Clickable disable() {
        cancelTouch();
        enabled = false;
        return this;
    }

    public Clickable enable() {
        enabled = true;
        return this;
    }


    public Clickable() {
        // default
    }

    @MassConstructor
    public Clickable(Metrics metrics, String name, float length, UIElement<?>[] childs,
                     Sprite buttonUpTex, Sprite buttonDownTex, int target, float visualScaleX, float visualScaleY,
                     Animation startAnim, Animation idleAnim, Animation pressedAnim, Animation releasedAnim, Animation endAnim,
                     Text text,
                     Audio.Sound activateSound, Audio.Sound pressSound, Audio.Sound releaseSound,
                     boolean enabled, boolean passThroughInput, float maxTouchMoveDistance,
                     float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom, boolean inputIgnoreAnimation
    ) {
        super(metrics, name, length, childs);

        visuals(buttonUpTex, buttonDownTex, target);
        length(length);
		visualScale(visualScaleX, visualScaleY);
        animation(startAnim, idleAnim, pressedAnim, releasedAnim, endAnim);
        text(text);
        sound(activateSound, pressSound, releaseSound);
        if(!enabled)
            disable();
        passThroughInput(passThroughInput);
        maxTouchMoveDistance(maxTouchMoveDistance);
        inputPadding(inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom);
		inputIgnoreAnimation(inputIgnoreAnimation);
    }

    @Override
    public Object[] mass() {
        return Mass.concat(super.mass(),
                // Visuals
                buttonUpTex, buttonDownTex, target, visualScaleX, visualScaleY,
                // Animation
                startAnim != null ? startAnim.anim : null,
                idleAnim != null ? idleAnim.anim : null,
                pressedAnim != null ? pressedAnim.anim : null,
                releasedAnim != null ? releasedAnim.anim : null,
                endAnim != null ? endAnim.anim : null,
                // Text
                text,
                // Sounds
                activateSound, pressSound, releaseSound,
                // Input
                enabled, passThroughInput, maxTouchMoveDistance,
                // input padding
                inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom, inputIgnoreAnimation
        );
    }

    @Override
	public Clickable instantiate() {
		Clickable clickable = new Clickable();
		clickable.name(name);
		clickable.viewport(viewport);
        if(metrics != null)
		    clickable.metrics(metrics.instantiate());
		clickable.visuals(buttonUpTex, buttonDownTex, target);
		clickable.visualScale(visualScaleX, visualScaleY);
		clickable.length(length);
		if(text != null)
        	clickable.text(text.instantiate());
		clickable.animation(
				startAnim != null ? startAnim.anim : null,
                idleAnim != null ? idleAnim.anim : null,
                pressedAnim != null ? pressedAnim.anim : null,
                releasedAnim != null ? releasedAnim.anim : null,
                endAnim != null ? endAnim.anim : null
		);
        clickable.sound(activateSound, pressSound, releaseSound);
		if(!isEnabled())
			clickable.disable();
		clickable.passThroughInput(passThroughInput);
		clickable.maxTouchMoveDistance(maxTouchMoveDistance);
		clickable.inputPadding(inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom);
		clickable.inputIgnoreAnimation(inputIgnoreAnimation);
		clickable.instantiateChilds(this);
		return clickable;
	}

	@Override
    public Clickable viewport(UIElement<?> viewport) {
        super.viewport(viewport);
        return this;
    }

    @Override
    public Clickable name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public Clickable windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
        super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
        return this;
    }

    @Override
    public Clickable metrics(Metrics metrics) {
        super.metrics(metrics);
        return this;
    }

    @Override
    public Clickable attach() {
        super.attach();
        return this;
    }

    @Override
    public Clickable attach(int index) {
        super.attach(index);
        return this;
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

	public Clickable detachWithAnim(Animation endAnim) {
		this.endAnim = endAnim != null ? endAnim.start() : null;
		detachWithAnim();
		return this;
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
	
	public void cancelTouch() {
		touchedPointer = -1;
		if(touchPressing != -1) {
		    touchPressing = -1;
			if(releasedAnim != null)
				releasedAnim.reset();
			if(releaseSound != null)
				releaseSound.play();
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
	protected void render(Universe v, float r, float renderTime) {
		calculateWindow();

		if(!isVisible())
			return;         // not visible

		Matrices.push();

		// Process pressing
		if(touchPressing != -1)
			touchPressing(v, touchPressing);

		// Process stages
		if(startAnim != null && startAnim.isActive())
			startAnim.updateAndApply(this, getRenderDeltaTime());
//		else {			// TODO: 20180907... not sure why this isnt the way by default, allowing all animation to be run concurrently
			if(idleAnim != null)
				idleAnim.updateAndApply(this, getRenderDeltaTime());
			// End anim
			if(endAnim != null && endAnim.isActive() && !endAnim.updateAndApply(this, getRenderDeltaTime()))
				detach();		// ended
//		}

		// Current press states
		if(pressedAnim != null && touchPressing != -1)
			pressedAnim.updateAndApply(this, getRenderDeltaTime());
		else if(releasedAnim != null && releasedAnim.isActive())
			releasedAnim.updateAndApply(this, getRenderDeltaTime());

        // Position matrix
        applyWindowAnim();

		applyMatrix();

        Matrices.target = target;

		if(visualScaleX != 1f || visualScaleY != 1f)
			Matrices.model.scale(visualScaleX, visualScaleY, 1f);

		renderButton(v, r, renderTime);

		renderText(v, r, renderTime);

		renderOverlay(v, r, renderTime);

		Matrices.pop();
	}
	
	protected void renderButton(Universe v, float r, float renderTime) {
		// Render sprite
		Mesh tex = (touchPressing != -1 || (releasedAnim != null && releasedAnim.isActive())) ? buttonDownTex : buttonUpTex;
        if(tex != null)
		    tex.render();
	}

	protected void renderText(Universe v, float r, float renderTime) {
		if(text != null)
            text.render();
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
		if(!inputIgnoreAnimation && touchedPointer == -1 && ((startAnim != null && startAnim.isActive()) || (endAnim != null && endAnim.isActive())))
			return false;		// havent started responding to touches and either start/end anim is animating, ignore
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
				if(pressSound != null)
					pressSound.play();
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
					if(pressSound != null)
						pressSound.play();
					if(releasedAnim != null)
						releasedAnim.stop();
				}
			}
			else {
				if(touchPressing != -1) {
					touchPressing = -1;
					if(releasedAnim != null)
						releasedAnim.reset();
					if(releaseSound != null)
						releaseSound.play();
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
				if(releaseSound != null)
					releaseSound.play();
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
	public <U extends MaterialAttribute> U getAttribute(Class<U> attribType, int layer) {
        int selection = layer & 0x1;
        layer >>>= 1;
		if(selection == 1) {
            // Selecting font
			return text != null ? text.font.getAttribute(attribType, layer) : null;
		}

		Mesh mat = (touchPressing != -1 || (releasedAnim != null && releasedAnim.isActive())) ? buttonDownTex : buttonUpTex;
		return mat != null ? mat.getAttribute(attribType, layer) : null;
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

	protected void renderOverlay(Universe v, float r, float renderTime) {
	}
}
