package sengine.ui;

import com.badlogic.gdx.graphics.Camera;

import sengine.Entity;
import sengine.Sys;
import sengine.Universe;
import sengine.animation.Animation;
import sengine.graphics2d.Font;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.Sprite;
import sengine.mass.Mass;

public class StaticSprite extends UIElement<Universe> {
	
	Mesh mat = null;
	Animation.Instance startAnim;
	Animation.Loop idleAnim;
	Animation.Instance endAnim;
	
	boolean passThroughInput = true;
	int touchedPointer = -1;

	int target = 0;

    public StaticSprite passThroughInput(boolean passThroughInput) {
        this.passThroughInput = passThroughInput;
        return this;
    }

    public StaticSprite visual(Mesh mat, int target) {
        return visual(mat).target(target);
	}

    public StaticSprite target(int target) {
        this.target = target;
        return this;
    }

	public int target() { return target; }

	public StaticSprite length(float length) {
		this.length = length;
		return this;
	}

    public StaticSprite visual(Mesh mat) {
        this.mat = mat;
        this.length = mat != null ? mat.getLength() : 1f;
        return this;
    }

	public Mesh visual() {
		return mat;
	}

	public StaticSprite animation(Animation startAnim, Animation idleAnim, Animation endAnim) {
		this.startAnim = startAnim != null ? startAnim.start() : null;
		this.idleAnim = idleAnim != null ? idleAnim.loop() : null;
		if(this.idleAnim != null)
			this.idleAnim.reset();
		this.endAnim = endAnim != null ? endAnim.start() : null;

		return this;
	}

    public StaticSprite() {
        // default
    }

    @MassConstructor
    public StaticSprite(Metrics metrics, String name, float length, UIElement<?>[] childs,
                        Mesh mat, int target,
                        Animation startAnim, Animation idleAnim, Animation endAnim,
                        boolean passThroughInput
    ) {
        super(metrics, name, length, childs);

        visual(mat, target);
        animation(startAnim, idleAnim, endAnim);
        passThroughInput(passThroughInput);
    }

    @Override
    public Object[] mass() {
        return Mass.concat(super.mass(),
                // Visuals
                mat, target,
                // Animation
                startAnim != null ? startAnim.anim : null,
                idleAnim != null ? idleAnim.anim : null,
                endAnim != null ? endAnim.anim : null,
                // Input
                passThroughInput
        );
    }

	@Override
	public StaticSprite instantiate() {
		StaticSprite sprite = new StaticSprite();
		sprite.name(name);
		sprite.viewport(viewport);
		if(metrics != null)
			sprite.metrics(metrics.instantiate());
		sprite.visual(mat, target);
		sprite.animation(
				startAnim != null ? startAnim.anim : null,
				idleAnim != null ? idleAnim.anim : null,
				endAnim != null ? endAnim.anim : null
		);
		sprite.passThroughInput(passThroughInput);
		sprite.instantiateChilds(this);
		return sprite;
	}

	@Override
    public StaticSprite viewport(UIElement<?> viewport) {
        super.viewport(viewport);
        return this;
    }

    @Override
    public StaticSprite name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public StaticSprite windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
        super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
        return this;
    }

    @Override
    public StaticSprite metrics(Metrics metrics) {
        super.metrics(metrics);
        return this;
    }

    @Override
    public StaticSprite attach() {
        super.attach();
        return this;
    }

    @Override
    public StaticSprite attach(int index) {
        super.attach(index);
        return this;
    }

	@Override
	protected void recreate(Universe v) {
		// Reset anims
		if(startAnim != null)
			startAnim.reset();
		if(endAnim != null)
			endAnim.stop();
		inputEnabled = true;
	}

	@Override
	protected void render(Universe v, float r, float renderTime) {
		calculateWindow();

		if(!isVisible())
			return;         // not visible

        Matrices.push();

		// Apply anims if avail
		if(idleAnim != null)
			idleAnim.updateAndApply(this, getRenderDeltaTime());
		if(startAnim != null && startAnim.isActive())
			startAnim.updateAndApply(this, getRenderDeltaTime());
		if(endAnim != null && endAnim.isActive() && !endAnim.updateAndApply(this, getRenderDeltaTime()))
			detach();		// ended

		applyWindowAnim();

		applyMatrix();

		Matrices.target = target;

		renderImage(v, r, renderTime);
		
		Matrices.pop();
	}
	
	protected void renderImage(Universe v, float r, float renderTime) {
		if(mat != null) {
			if(length < 0) {
		        float viewportLength = childLength / childScaleX;
		        if(mat.getLength() != viewportLength) {
                    Sprite sprite = new Sprite(mat.getMaterial());
                    sprite.crop(viewportLength).copyAttributes(mat);
                    mat = sprite;
                }
            }
			mat.render();
		}
	}


	@Override
	public void attach(Entity<?> parent, int index) {
		inputEnabled = true;
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
		if(endAnim != null && isEffectivelyAttached()) {
			if(!endAnim.isActive())
				endAnim.reset();
			inputEnabled = false;
		}
		else
			detach();
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
		if(!renderingEnabled)
			return false;
		if(passThroughInput || (inputType & INPUT_KEY) != 0 || camera == null)
			return false;
		// Adjust touch position according to camera
		x += camera.position.x;
		y += camera.position.y;
		
		switch(inputType) {
		case INPUT_TOUCH_DOWN:
			if(!checkTouched(x, y))
				return false;
			touchedPointer = pointer;
			return true;
			
		case INPUT_TOUCH_DRAGGED:
			if(touchedPointer == -1)
				return false;
			return true;
			
		case INPUT_TOUCH_UP:
			if(touchedPointer == -1)
				return false;
			touchedPointer = -1;
			return true;
		}
		
		return false;
	}

	@Override
	public <T extends MaterialAttribute> T getAttribute(Class<T> attribType, int layer) {
		return mat == null ? null : mat.getAttribute(attribType, layer);
	}
}
