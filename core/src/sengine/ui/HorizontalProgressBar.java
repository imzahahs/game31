package sengine.ui;

import com.badlogic.gdx.math.Matrix4;

import sengine.Universe;
import sengine.animation.Animation;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Mesh;
import sengine.mass.Mass;

public class HorizontalProgressBar extends StaticSprite {
	
	private Mesh bar = null;
	private Mesh knob = null;
	private float knobSize;
    private float knobPaddingLeft;
    private float knobPaddingRight;
    private float knobOffsetX;
    private float knobOffsetY;

    // Input padding
    private float inputPaddingLeft = 0;
    private float inputPaddingTop = 0;
    private float inputPaddingRight = 0;
    private float inputPaddingBottom = 0;

    // Current
	private float progress = 0f;
    private float indeterminateSeekProgress = -1;
    private float indeterminateSeekSpeed = -1;
    private float seekProgress = -1;
    private float tSeekStarted = -1;
    private float seekFromProgress = -1;
    private float tSeekTime = -1;

    public HorizontalProgressBar stopSeek() {
        indeterminateSeekProgress = -1;
        indeterminateSeekSpeed = -1;
        seekProgress = -1;
        tSeekStarted = -1;
        seekFromProgress = -1;
        tSeekTime = -1;
        return this;
    }

    public HorizontalProgressBar seekIndeterminate(float progress, float speed) {
        stopSeek();
        indeterminateSeekProgress = progress;
        indeterminateSeekSpeed = speed;
        return this;
    }

    public HorizontalProgressBar seek(float progress, float time) {
        stopSeek();
        seekFromProgress = this.progress;
        seekProgress = progress;
        tSeekTime = time;
        tSeekStarted = getRenderTime();
        return this;
    }

    public HorizontalProgressBar inputPadding(float left, float top, float right, float bottom) {
        inputPaddingLeft = left;
        inputPaddingTop = top;
        inputPaddingRight = right;
        inputPaddingBottom = bottom;
        return this;
    }

	public HorizontalProgressBar progress(float progress) {
		if(progress > 1f)
            progress = 1f;
        if(progress < 0f)
            progress = 0f;
        this.progress = progress;
        stopSeek();
        return this;
	}

	public float progress() {
		return progress;
	}

    public boolean isPressing() {
        return touchedPointer != -1;
    }

	public HorizontalProgressBar passThroughInput(boolean passThroughInput) {
		super.passThroughInput(passThroughInput);
		return this;
	}

	public HorizontalProgressBar knob(Mesh knob, float size) {
		return knob(knob, size, 0, 0, 0, 0);
	}

    public HorizontalProgressBar knob(Mesh knob, float size, float paddingLeft, float paddingRight) {
        return knob(knob, size, paddingLeft, paddingRight, 0, 0);
    }

    public HorizontalProgressBar knob(Mesh knob, float size, float paddingLeft, float paddingRight, float knobOffsetX, float knobOffsetY) {
        this.knob = knob;
        this.knobSize = size;
        this.knobPaddingLeft = paddingLeft;
        this.knobPaddingRight = paddingRight;
        this.knobOffsetX = knobOffsetX;
        this.knobOffsetY = knobOffsetY;

        return this;
    }

	public HorizontalProgressBar visual(Mesh bg, Mesh bar, int target) {
		super.visual(bg, target);
		this.bar = bar;
		return this;
	}

	public HorizontalProgressBar bar(Mesh bar) {
		this.bar = bar;
		return this;
	}

	public Mesh bar() {
		return bar;
	}

	public HorizontalProgressBar visual(Mesh mat, int target) {
		super.visual(mat, target);
        return this;
	}


	public StaticSprite visual(Mesh mat) {
		super.visual(mat);
        return this;
	}

	public HorizontalProgressBar target(int target) {
		super.target(target);
		return this;
	}

    public HorizontalProgressBar length(float length) {
        super.length(length);
        return this;
    }

	public HorizontalProgressBar animation(Animation startAnim, Animation idleAnim, Animation endAnim) {
		super.animation(startAnim, idleAnim, endAnim);
		return this;
	}

    public HorizontalProgressBar() {
        // default
    }

    @MassConstructor
	public HorizontalProgressBar(Metrics metrics, String name, float length, UIElement<?>[] childs,
                                 Mesh bg, int target,
                                 Animation startAnim, Animation idleAnim, Animation endAnim,
                                 boolean passThroughInput,
                                 float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom,
                                 Mesh bar, Mesh knob, float knobSize, float knobPaddingLeft, float knobPaddingRight, float knobOffsetX, float knobOffsetY
    ) {
        super(metrics, name, length, childs, bg, target, startAnim, idleAnim, endAnim, passThroughInput);

        visual(bg, bar, target);
        knob(knob, knobSize, knobPaddingLeft, knobPaddingRight, knobOffsetX, knobOffsetY);
        inputPadding(inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom);
	}

    @Override
    public Object[] mass() {
        return Mass.concat(super.mass(),
                // input padding
                inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom,
                // bar
                bar,
                // knob
                knob, knobSize, knobPaddingLeft, knobPaddingRight, knobOffsetX, knobOffsetY
        );
    }

    @Override
    public HorizontalProgressBar instantiate() {
        HorizontalProgressBar i = new HorizontalProgressBar();
        i.name(name);
        i.viewport(viewport);
        i.metrics(metrics.instantiate());
        i.visual(mat, bar, target);
        i.knob(knob, knobSize, knobPaddingLeft, knobPaddingRight, knobOffsetX, knobOffsetY);
        i.animation(
                startAnim != null ? startAnim.anim : null,
                idleAnim != null ? idleAnim.anim : null,
                endAnim != null ? endAnim.anim : null
        );
        i.passThroughInput(passThroughInput);
        i.instantiateChilds(this);
        return i;
    }

    @Override
	public HorizontalProgressBar viewport(UIElement<?> viewport) {
		super.viewport(viewport);
		return this;
	}

	@Override
	public HorizontalProgressBar name(String name) {
		super.name(name);
		return this;
	}

	@Override
	public HorizontalProgressBar windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
		super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
		return this;
	}

	@Override
	public HorizontalProgressBar metrics(Metrics metrics) {
		super.metrics(metrics);
		return this;
	}

	@Override
	public HorizontalProgressBar attach() {
		super.attach();
		return this;
	}

	@Override
	public HorizontalProgressBar attach(int index) {
		super.attach(index);
		return this;
	}

    @Override
    protected void render(Universe v, float r, float renderTime) {
        // Calculate seek progress
        if(indeterminateSeekProgress != -1) {
            float delta = indeterminateSeekProgress - progress;
            delta *= indeterminateSeekSpeed * getRenderDeltaTime();
            progress += delta;
        }
        else if(seekProgress != -1) {
            float elapsed = renderTime - tSeekStarted;
            if(elapsed > tSeekTime) {
                progress = seekProgress;
                stopSeek();
            }
            else
                progress = seekFromProgress + ((seekProgress - seekFromProgress) * (elapsed / tSeekTime));
        }

        // Continue
        super.render(v, r, renderTime);
    }

    @Override
	protected void renderImage(Universe v, float r, float renderTime) {
		if(mat != bar)
			super.renderImage(v, r, renderTime);
		Matrices.push();
		Matrices.scissor.mul(childX - (childScaleX  / 2f) + (childScaleX * progress / 2f), childY, childScaleX * progress, childLength);
		
		bar.render();
		
		Matrices.pop();

        if(knob != null) {
            Matrix4 m = Matrices.model;
            m.translate(-0.5f + knobPaddingLeft + (progress * (1f - (knobPaddingLeft + knobPaddingRight))) + knobOffsetX, knobOffsetY, 0);
            m.scale(knobSize, knobSize, knobSize);

            knob.render();
        }
	}

    @Override
    protected boolean input(Universe v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(passThroughInput || (inputType & INPUT_KEY) != 0 || camera == null)
            return false;

        // Adjust touch position according to camera
        x += camera.position.x;
        y += camera.position.y;

        float cx = (x + childInputX) * childInputScaleX;

        switch(inputType) {
            case INPUT_TOUCH_DOWN:
                if(!checkTouched(x, y, inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom))
                    return false;
                touchedPointer = pointer;
                progress((cx - (childX - (childScaleX / 2f))) / childScaleX);
                touchPressed(v, progress, button);
                return true;

            case INPUT_TOUCH_DRAGGED:
                if(touchedPointer == -1)
                    return false;
                progress((cx - (childX - (childScaleX / 2f))) / childScaleX);
                touchDragged(v, progress, button);
                return true;

            case INPUT_TOUCH_UP:
                if(touchedPointer == -1)
                    return false;
                touchedPointer = -1;
                touchReleased(v, progress, button);
                return true;
        }

        return false;
    }

    public void touchPressed(Universe v, float progress, int button) {
        OnPressed callback = findParent(OnPressed.class);
        if(callback != null)
            callback.onPressed(v, this, progress, 0, button);
    }

    public void touchReleased(Universe v, float progress, int button) {
        OnReleased callback = findParent(OnReleased.class);
        if(callback != null)
            callback.onReleased(v, this, progress, 0, button);

    }

    public void touchDragged(Universe v, float progress, int button) {
        OnDragged callback = findParent(OnDragged.class);
        if(callback != null)
            callback.onDragged(v, this, progress, 0, button);
    }

    @Override
    public <T extends MaterialAttribute> T getAttribute(Class<T> attribType, int layer) {
        int selection = layer & 0x3;
        layer >>>= 2;
        if(selection == 1) {
            // Selecting bar
            return bar != null ? bar.getAttribute(attribType, layer) : null;
        }
        if(selection == 2) {
            // Selecting knob
            return knob != null ? knob.getAttribute(attribType, layer) : null;
        }

        // Background
        return super.getAttribute(attribType, layer);
    }
}
