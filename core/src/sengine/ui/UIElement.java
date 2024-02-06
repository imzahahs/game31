package sengine.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import sengine.Entity;
import sengine.Sys;
import sengine.Universe;
import sengine.animation.Animation;
import sengine.graphics2d.Animatable2D;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Matrices;
import sengine.mass.Mass;
import sengine.mass.MassSerializable;

public class UIElement<T extends Universe> extends Entity<T> implements Animatable2D, MassSerializable {

    public static final int SIMULATE_TOUCH_POINTER = 1000;

    private static final Affine2 tempAffine = new Affine2();
    private static final Vector2 tempVec2 = new Vector2();

    public static class Group extends UIElement<Universe> {

        private Animation.Instance startAnim;
        private Animation.Loop idleAnim;
        private Animation.Instance endAnim;

        private boolean scissor = false;

        private boolean enabled = false;
        private boolean passThroughInput = false;
        private int touchedPointer = -1;

        public UIElement.Group disable() {
            enabled = false;
            return this;
        }

        public UIElement.Group enable() {
            enabled = true;
            return this;
        }

        public UIElement.Group passThroughInput(boolean passThroughInput) {
            this.passThroughInput = passThroughInput;
            return this;
        }

        public UIElement.Group scissor(boolean scissor) {
            this.scissor = scissor;
            return this;
        }

        public UIElement.Group animation(Animation startAnim, Animation idleAnim, Animation endAnim) {
            this.startAnim = startAnim != null ? startAnim.start() : null;
            this.idleAnim = idleAnim != null ? idleAnim.loop() : null;
            if(this.idleAnim != null)
                this.idleAnim.reset();
            this.endAnim = endAnim != null ? endAnim.start() : null;
            return this;
        }

        public UIElement.Group autoLength() {
            length = 1f;        // reset
            Rectangle bounds = bounds(true, false, false, true, null);
            length = bounds.height; // (bounds.height / bounds.width);          // TODO: 20160817 validate this
            if(metrics != null)
                length *= metrics.scaleY;
            calculateWindow();
            return this;
        }

        public Group() {
            // default
            length = -1;            // follow viewport
        }

        @MassConstructor
        public Group(Metrics metrics, String name, float length, UIElement<?>[] childs, Animation startAnim, Animation idleAnim, Animation endAnim, boolean enabled, boolean passThroughInput, boolean scissor) {
            super(metrics, name, length, childs);

            animation(startAnim, idleAnim, endAnim);
            if(enabled)
                enable();
            passThroughInput(passThroughInput);
            scissor(scissor);
        }

        @Override
        public Object[] mass() {
            return Mass.concat(super.mass(),
                    startAnim != null ? startAnim.anim : null,
                    idleAnim != null ? idleAnim.anim : null,
                    endAnim != null ? endAnim.anim : null,
                    enabled, passThroughInput, scissor
            );
        }

        @Override
        public UIElement.Group instantiate() {
            UIElement.Group group = new UIElement.Group();
            group.name(name);
            group.viewport(viewport);
            if(metrics != null)
                group.metrics(metrics.instantiate());
            group.length(length);
            group.animation(
                    startAnim != null ? startAnim.anim : null,
                    idleAnim != null ? idleAnim.anim : null,
                    endAnim != null ? endAnim.anim : null
            );
            if(enabled)
                group.enable();
            group.passThroughInput(passThroughInput);
            group.scissor(scissor);
            group.instantiateChilds(this);
            return group;
        }

        @Override
        public UIElement.Group viewport(UIElement<?> viewport) {
            super.viewport(viewport);
            return this;
        }

        @Override
        public UIElement.Group name(String name) {
            super.name(name);
            return this;
        }

        @Override
        public UIElement.Group windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
            super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
            return this;
        }

        @Override
        public UIElement.Group metrics(Metrics metrics) {
            super.metrics(metrics);
            return this;
        }

        @Override
        public UIElement.Group attach() {
            super.attach();
            return this;
        }

        @Override
        public UIElement.Group attach(int index) {
            super.attach(index);
            return this;
        }

        public UIElement.Group length(float length) {
			this.length = length;
            return this;
		}

        @Override
		protected void render(Universe v, float r, float renderTime) {

			calculateWindow();

            Matrices.push();

            if(scissor) {
                Matrices.scissor.mul(childX, childY, childScaleX, childLength);
//                Matrices.scissor.x = childX;          // 20180907 - Using mul()  to support cascading scissors
//                Matrices.scissor.y = childY;
//                Matrices.scissor.width = childScaleX;
//                Matrices.scissor.height = childLength;
            }

            // Process stages
            if(startAnim != null && startAnim.isActive())
                startAnim.updateAndApply(this, getRenderDeltaTime());
            else {
                if(idleAnim != null)
                    idleAnim.updateAndApply(this, getRenderDeltaTime());
                // End anim
                if(endAnim != null && endAnim.isActive() && !endAnim.updateAndApply(this, getRenderDeltaTime()))
                    detach();		// ended
            }

            applyWindowAnim();
        }

        @Override
        protected void renderFinish(Universe v, float r, float renderTime) {
            Matrices.pop();
        }

        @Override
        protected void recreate(Universe v) {
            // Reset anim states
            if(startAnim != null)
                startAnim.reset();
            if(endAnim != null)
                endAnim.stop();
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

        @Override
        protected boolean input(Universe v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
            // Adjust touch position according to camera
            if(enabled && (inputType & INPUT_TOUCH) != 0 && camera != null) {
                x += camera.position.x;
                y += camera.position.y;
            }
            else
                return false;

            switch(inputType) {
                case INPUT_TOUCH_DOWN:
                    if(checkTouched(x, y)) {
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
                    if(checkTouched(x, y)) {
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
    }
	
	public static class Viewport extends UIElement<Universe> {
		static final String TAG = "UIElement.Viewport";


        public Viewport() {
            // default
            length = -1;
        }

        @MassConstructor
        public Viewport(Metrics metrics, String name, float length, UIElement<?>[] childs) {
            super(metrics, name, length, childs);
        }

        @Override
        public Object[] mass() {
            return super.mass();        // no changes
        }

        @Override
        public Viewport instantiate() {
            Viewport viewport = new Viewport();
            viewport.name(name);
            viewport.length(length);
            viewport.instantiateChilds(this);
            return viewport;
        }

        @Override
        public Viewport viewport(UIElement<?> viewport) {
            // Ignore
            Sys.debug(TAG, "Cannot set viewport for UIElement.Viewport");
            return this;
        }

        @Override
        public Viewport name(String name) {
            super.name(name);
            return this;
        }

        @Override
        public Viewport windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
            super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
            return this;
        }

        @Override
        public Viewport metrics(Metrics metrics) {
            super.metrics(metrics);
            return this;
        }

        @Override
        public Viewport attach() {
            super.attach();
            return this;
        }

        @Override
        public Viewport attach(int index) {
            super.attach(index);
            return this;
        }

        public Viewport length(float length) {
            this.length = length;
            return this;
        }

        @Override
        public void attach(Entity<?> parent, int index) {
            super.attach(parent, index);
            viewport = null;            // viewport is always null here
        }

        @Override
		protected void render(Universe v, float r, float renderTime) {
            Matrices.push();

            // Calculate window
            calculateWindow();

			// Reset model
			Matrices.model.idt();

            applyWindowAnim();
        }
		
		@Override
		protected void renderFinish(Universe v, float r, float renderTime) {
			// Rest model
			Matrices.pop();
		}
	}
	
	public static class Metrics implements MassSerializable {
		public float x = 0.0f;
		public float y = 0.0f;
		public float scaleY = 1.0f;
		public float scaleX = 1.0f;
        public int scaleIndex = 1;
        public float anchorLength = 0.0f;
        public float anchorWindowY = 0.0f;
		public float anchorWindowX = 0.0f;
        public float anchorY = 0f;
        public float rotate = 0f;
        public float inputX = 0;
        public float inputY = 0;
        public float inputScaleX = 1;
        public float inputScaleY = 1;

		public Metrics() {
		}
		
		public Metrics(Metrics copyFrom) {
			this(copyFrom.x, copyFrom.y, copyFrom.scaleY, copyFrom.scaleX, copyFrom.scaleIndex,
                    copyFrom.anchorLength, copyFrom.anchorWindowY, copyFrom.anchorWindowX, copyFrom.anchorY, copyFrom.rotate,
                    copyFrom.inputX, copyFrom.inputY, copyFrom.inputScaleX, copyFrom.inputScaleY
            );
		}

        @MassConstructor
        public Metrics(float x, float y, float scaleY, float scaleX, int scaleIndex, float anchorLength, float anchorWindowY, float anchorWindowX, float anchorY, float rotate, float inputX, float inputY, float inputScaleX, float inputScaleY) {
			this.x = x;
			this.y = y;
			this.scaleY = scaleY;
			this.scaleX = scaleX;
            this.scaleIndex = scaleIndex;
			this.anchorLength = anchorLength;
			this.anchorWindowY = anchorWindowY;
			this.anchorWindowX = anchorWindowX;
            this.anchorY = anchorY;
            this.rotate = rotate;
            this.inputX = inputX;
            this.inputY = inputY;
            this.inputScaleX = inputScaleX;
            this.inputScaleY = inputScaleY;
		}


        @Override
        public Object[] mass() {
            return new Object[] { x, y, scaleY, scaleX, anchorLength, anchorWindowY, anchorWindowX, anchorY, rotate, inputX, inputY, inputScaleX, inputScaleY};
        }

        public Metrics offsetInput(float dx, float dy) {
            inputX += dx;
            inputY += dy;
            return this;
        }

        public Metrics scaleInput(float scaleX, float scaleY) {
            inputScaleX = scaleX;
            inputScaleY = scaleY;
            return this;
        }

        public Metrics clear() {
            x = y = anchorLength = 0;
            scaleX = scaleY = 1;
            scaleIndex = 1;
            anchorWindowX = anchorWindowY = anchorY = 0;
            rotate = 0;
            inputX = inputY = 0;
            inputScaleX = inputScaleY = 1;
            return this;
        }

        public Metrics fitX(float targetWidth) {
            float scale = targetWidth / scaleX;
            scaleX *= scale;
            scaleY *= scale;
            return this;
        }

        public Metrics fitY(float length, float targetLength) {
            float height = scaleY * length;
            float scale = targetLength / height;
            scaleX *= scale;
            scaleY *= scale;
            return this;
        }

        public Metrics fitXY(float length, float targetWidth, float targetLength) {
            float height = scaleY * length;
            float yscale = targetLength / height;
            float xscale = targetWidth / scaleX;
            float scale = Math.min(xscale, yscale);
            scaleX *= scale;
            scaleY *= scale;
            return this;
        }

        public Metrics move(float dx, float dy) {
            anchorWindowX += dx;
            anchorY += dy;
            return this;
        }

        public Metrics anchor(float dx, float dy) {
            anchorWindowX += dx;
            anchorWindowY += dy;
            return this;
        }

        public Metrics clearAnchor() {
            anchorWindowX = anchorWindowY = 0;
            return this;
        }

        public Metrics offset(float dx, float dy) {
            x += dx;
            y += dy;
            return this;
        }

        public Metrics clearOffset() {
            x = y = 0;
            return this;
        }

        public Metrics pan(float dx, float dy) {
            x += dx;
            anchorLength += dy;
            return this;
        }

        public Metrics clearPan() {
            x = anchorLength = 0;
            return this;
        }

        public Metrics rotate(float degrees) {
            rotate += degrees;
            return this;
        }

        public Metrics clearRotate() {
            rotate = 0;
            return this;
        }

        public Metrics scaleIndex(int index) {
            scaleIndex = index;
            return this;
        }

        public Metrics scale(float scale) {
            return scale(scale, scale);
        }

        public Metrics scale(float scaleX, float scaleY) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            return this;
        }


        public Metrics anchorLeft() {
            x = +0.5f;
            anchorWindowX = -0.5f;
            return this;
        }

        public Metrics anchorRight() {
            x = -0.5f;
            anchorWindowX = +0.5f;
            return this;
        }

        public Metrics anchorBottom() {
            anchorLength = +0.5f;
            anchorWindowY = -0.5f;
            return this;
        }

        public Metrics anchorTop() {
            anchorLength = -0.5f;
            anchorWindowY = +0.5f;
            return this;
        }

        public Metrics instantiate() {
            return new Metrics(this);
        }

    }
	
	protected UIElement<?> viewport;
    // Metrics
	public Metrics metrics = null;
    // Name
    protected String name;
    // Window dependent length
	protected float length = 1.0f;

    // Window anim
    public Animation.Handler windowAnim = null;
    public boolean animateWindowAnim = true;
    public boolean animateKeepAnim = false;
    // Child metrics, result of positionMatrix()
	protected float childX = 0.0f;
	protected float childY = 0.0f;
	protected float childScaleX = 1.0f;
	protected float childScaleY = 1.0f;
	protected float childLength = 1.0f;
	protected final Matrices.ScissorBox childScissor = new Matrices.ScissorBox();
	protected float rotate;
    protected Camera camera = null;
    // Input metrics
    protected float childInputX = 0;
    protected float childInputY = 0;
    protected float childInputScaleX = 1;
    protected float childInputScaleY = 1;


    public Camera camera() {
        return camera;
    }

    public UIElement() {
        processEnabled = false;		// not needed
        inputEnabled = true;		// require input

        name = Integer.toString(System.identityHashCode(this));         // random name
    }

    @MassConstructor
    public UIElement(Metrics metrics, String name, float length, UIElement<?>[] childs) {
        // Set parameters
        metrics(metrics);
        name(name);
        this.length = length;
        // Childs
        for(UIElement<?> child : childs)
            child.viewport(this).attach();
    }

    @Override
    public Object[] mass() {
        UIElement<?>[] childs = getChilds(UIElement.class);
        return new Object[] { metrics, name, length, childs };
    }


    public <T extends UIElement<?>> T find(T named) {
        return find(named.name);
    }

    public <T extends UIElement<?>> T find(String name) {
        if(this.name != null && this.name.equals(name))
            return (T) this;
        UIElement<?> child = null;
        while((child = iterate(child, UIElement.class, true, null)) != null) {
            if(child.name != null && child.name.equals(name))
                return (T) child;
        }
        return null;        // not found
    }

    public void instantiateChilds(UIElement<?> from) {
        UIElement<?> child;
        int idx = 0;
        while((child = from.getChild(UIElement.class, idx)) != null) {
            child.instantiate().viewport(this).attach();        // instantiate and attach to this
            idx++;
        }
    }

    public UIElement<T> instantiate() {
        return this;            // implementors must override this
    }

    public UIElement<T> name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }
	
	public UIElement<T> viewport(UIElement<?> viewport) {
		this.viewport = viewport;
		if(viewport == null) {
			if(isAttached())
				detach();
			return this;
		}
		if(isAttached())
			attach();
        return this;
	}

	public UIElement<?> viewport() {
        return viewport;
    }

    public UIElement<T> metrics(Metrics metrics) {
        this.metrics = metrics;
        return this;
    }

    public UIElement<T> windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
        this.windowAnim = windowAnim;
        this.animateWindowAnim = animateWindowAnim;
        this.animateKeepAnim = animateKeepAnim;
        return this;
    }

    public boolean isWindowAnimating(Animation type) {
        return windowAnim != null && windowAnim.anim == type;
    }

	// UIElements can only be attached to specified viewport
	public UIElement<T> attach() {
		attach(viewport);
        return this;
	}
	
	public UIElement<T> attach(int index) {
		attach(viewport, index);
        return this;
	}
	
	@Override
	public void attach(Entity<?> parent, int index) {
        if(viewport == null) {
            // Automatically set viewport if UIElement
            if(parent instanceof UIElement)
                viewport = (UIElement<?>) parent;
        }
        else if(parent != viewport)
			throw new IllegalArgumentException("Cannot attach UIElement to other than specified viewport: " + this + " " + viewport);
		super.attach(parent, index);
        calculateWindow();
	}
	
	public void calculateWindow() {
        calculateWindow(Matrices.camera);
    }

	public void calculateWindow(Camera camera) {
        this.camera = camera;
        if(viewport == null) {
            if(camera == null) {
                childX = 0;
                childY = 0;
                if(length < 0f)
                    childLength = 1f;       // cannot infer length as there is no camera yet
                else
                    childLength = length;
            }
            else {
                childX = camera.position.x;
                childY = camera.position.y;
                if(length < 0f)
                    childLength = camera.viewportHeight;
                else
                    childLength = length;
            }
            rotate = 0;
            childScaleX = childScaleY = 1f;
            childInputScaleX = childInputScaleY = 1f;
            childInputX = childInputY = 0;

            if(metrics != null) {
                childScaleX *= metrics.scaleX;
                childScaleY *= metrics.scaleY;
                childLength *= metrics.scaleY;
                childInputScaleX *= metrics.inputScaleX;
                childInputScaleY *= metrics.inputScaleY;
                childInputX += metrics.inputX;
                childInputY += metrics.inputY;
                childX += metrics.anchorWindowX + (metrics.x * childScaleX);
                childY += metrics.anchorY + (metrics.anchorWindowY * length) + (metrics.anchorLength * childLength) + (metrics.y * childScaleY);
                rotate = metrics.rotate;
            }

            return;
        }
        float length = this.length;
        // Resolve length if using parent
		if(length < 0f) {
            if(viewport.childScaleY == 0)
                length = 0;
            else
                length = Math.abs(viewport.childLength / viewport.childScaleY);
        }
        int scaleIndex = metrics != null ? metrics.scaleIndex : 1;
        if(scaleIndex == 0) {
            childScaleX = 1f;
            childScaleY = 1f;
            childInputScaleX = 1f;
            childInputScaleY = 1f;
            childLength = length;
        }
        else if(scaleIndex > 0) {
            UIElement<?> ref = this;
            while(scaleIndex > 0 && ref.viewport != null) {
                scaleIndex--;
                ref = ref.viewport;
            }
            childScaleX = ref.childScaleX;
            childScaleY = ref.childScaleY;
            childInputScaleX = ref.childInputScaleX;
            childInputScaleY = ref.childInputScaleY;
            childLength = childScaleY * length;
        }
        else { // if(scaleIndex < 0)
            UIElement<?> ref = this;
            while(scaleIndex < 0 && ref.viewport != null) {
                scaleIndex++;
                ref = ref.viewport;
            }
            childScaleX = ref.childLength;
            childScaleY = (ref.childScaleY / ref.childScaleX) * ref.childLength;
            childInputScaleX = ref.childInputScaleX;
            childInputScaleY = ref.childInputScaleY;
            childLength = childScaleY * length;
        }
		childX = viewport.childX;
		childY = viewport.childY;
        childInputX = viewport.childInputX;
        childInputY = viewport.childInputY;
		rotate = viewport.rotate;
		// Use metrics if available		
		if(metrics != null) {

			childScaleX *= metrics.scaleX;
			childScaleY *= metrics.scaleY;
			childLength *= metrics.scaleY;
            childInputX += metrics.inputX * childInputScaleX;
            childInputY += metrics.inputY * childInputScaleY;
            childInputScaleX *= metrics.inputScaleX;
            childInputScaleY *= metrics.inputScaleY;

            // Use accurate affine transformation if using ratations
            if(rotate != 0) {
                tempAffine.idt();
                tempAffine.translate(childX, childY);
                tempAffine.rotate(-rotate);
                tempAffine.translate(
                        (metrics.anchorWindowX * viewport.childScaleX) + (metrics.x * childScaleX),
                        (metrics.anchorY * viewport.childScaleY) + (metrics.anchorWindowY * viewport.childLength) + (metrics.anchorLength * childLength) + (metrics.y * childScaleY)
                );
                tempAffine.getTranslation(tempVec2);
                childX = tempVec2.x;
                childY = tempVec2.y;
            }
            else {
                childX += (metrics.anchorWindowX * viewport.childScaleX) + (metrics.x * childScaleX);
                childY += (metrics.anchorY * viewport.childScaleY) + (metrics.anchorWindowY * viewport.childLength) + (metrics.anchorLength * childLength) + (metrics.y * childScaleY);
            }

            rotate += metrics.rotate;
		}
		childScissor.set(Matrices.scissor);
	}


	protected void applyWindowAnim() {
		if(windowAnim == null)
			return;
		if(!animateWindowAnim)
			windowAnim.apply(this);
		else if(!windowAnim.updateAndApply(this, getRenderDeltaTime()) && !animateKeepAnim)
			windowAnim = null;
	}
	
	protected void applyMatrix() {
		// Apply to matrix
		Matrices.model.translate(childX, childY, 0.0f);
        Matrices.model.scale(childScaleX, childScaleY, 1.0f);
        if(rotate != 0f)
            Matrices.model.rotate(0, 0, -1, rotate);
    }

    protected boolean isVisible() {
        return (childScaleX != 0 && childScaleY != 0) && (childScissor.isInfinite() || childScissor.overlaps(childX, childY, childScaleX, childLength));
    }

    public boolean checkTouched(float xt, float yt) {
        return checkTouched(xt, yt, 0,0,0,0, childScaleX, childScaleY, childLength);
    }


    public void simulateClick() {
        if(camera == null)
            return;         // can't do this while camera is not set
        /*
            (inputX + camera.position.x + childInputX) * childInputScaleX = childX
            inputX = (childX / childInputScaleX) - childInputX - camera.position.x
        */

        // Check bounds
        float cx = childX - camera.position.x;
        float cy = childY - camera.position.y;
        float halfLength = camera.viewportHeight / 2f;
        if(cx < -0.5f || cx > +0.5f || cy < -halfLength || cy > +halfLength)
            return;           // ignore out of bounds

        final float inputX = (childX / childInputScaleX) - childInputX - camera.position.x;
        final float inputY = (childY / childInputScaleY) - childInputY - camera.position.y;

        // Simulate click
        final Universe v = Sys.system.getUniverse();
        v.postMessage(new Runnable() {
            @Override
            public void run() {
                v.processInput(Entity.INPUT_TOUCH_DOWN, 0, Character.MIN_VALUE, 0, SIMULATE_TOUCH_POINTER, inputX, inputY, Input.Buttons.LEFT);
                v.processInput(Entity.INPUT_TOUCH_UP, 0, Character.MIN_VALUE, 0, SIMULATE_TOUCH_POINTER, inputX, inputY, Input.Buttons.LEFT);

            }
        });
    }

    public boolean checkTouched(float xt, float yt, float childScaleX, float childScaleY, float childLength) {      // TODO: test this
        return checkTouched(xt, yt, 0,0,0,0, childScaleX, childScaleY, childLength);
    }

    public boolean checkTouched(float xt, float yt, float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom) {
        return checkTouched(xt, yt, inputPaddingLeft, inputPaddingTop, inputPaddingRight, inputPaddingBottom, childScaleX, childScaleY, childLength);
    }

    public boolean checkTouched(float xt, float yt, float inputPaddingLeft, float inputPaddingTop, float inputPaddingRight, float inputPaddingBottom, float childScaleX, float childScaleY, float childLength) {
		// Check bounds
        float absScaleX = Math.abs(childScaleX);
        float absScaleY = Math.abs(childScaleY);
        float absLength = Math.abs(childLength);

        float widthHalf = absScaleX / 2f;
        float heightHalf = absLength / 2f;

        xt += childInputX;
        yt += childInputY;
        xt *= childInputScaleX;
        yt *= childInputScaleY;

        float x = childX;
        float y = childY;

        float windowLeft = x - widthHalf - (inputPaddingLeft * absScaleX);
        float windowRight = x + widthHalf + (inputPaddingRight * absScaleX);
        float windowTop = y + heightHalf + (inputPaddingTop * absScaleY);
        float windowBottom = y - heightHalf - (inputPaddingBottom * absScaleY);


//        float windowLeft = x - widthHalf - inputPaddingLeft;
//        float windowRight = x + widthHalf + inputPaddingRight;
//        float windowTop = y + heightHalf + inputPaddingTop;
//        float windowBottom = y - heightHalf - inputPaddingBottom;

        if(!(xt < windowLeft || xt > windowRight || yt > windowTop || yt < windowBottom)) {
			// Check scissor
			if(childScissor.isInfinite() || childScissor.contains(xt, yt))
				return true;
		}
		return false;
	}

    @Override
    public float getLength() {
        if(length == -1)
            return childLength;
        return length;
    }

	public float getX() {
		return childX;
	}
	
	public float getY() { 
		return childY;
	}

    public float getWidth() {
        return childScaleX;
    }

    public float getScaleY() {
        return childScaleY;
    }

    public float getHeight() {
        return childLength;
    }

    public float getInputX() {
        return childInputX;
    }

    public float getInputY() {
        return childInputY;
    }

    public float getInputScaleX() {
        return childInputScaleX;
    }

    public float getInputScaleY() {
        return childInputScaleY;
    }

    public float getLeft() {
        return (childX - (childScaleX / 2f)) - ((camera != null ? camera.viewportWidth : 0f) / 2f);
    }

    public float getRight() {
        return (childX + (childScaleX / 2f)) - ((camera != null ? camera.viewportWidth : 0f) / 2f);
    }

    public float getTop() {
        return (childY + (childLength / 2f)) - ((camera != null ? camera.viewportHeight : 0f) / 2f);
    }

    public float getBottom() {
        return (childY - (childLength / 2f)) - ((camera != null ? camera.viewportHeight : 0f) / 2f);
    }

    /**
     * Calculates the bounds of this element as well as all childs and sub-childs
     * @param recalculateWindow recalculate window before calculating bounds
     * @param includeSelf include this element's bounds
     * @param includeChilds include all childs
     * @return bounds
     */
    public Rectangle bounds(boolean recalculateWindow, boolean keepParentMetrics, boolean includeSelf, boolean includeChilds, Entity<?>[] excluded) {
        if(recalculateWindow) {
            // Calculate without viewport first
            UIElement viewport = this.viewport;
            if(!keepParentMetrics)
                this.viewport = null;
            calculateWindow();
            this.viewport = viewport;
        }

        if(childScaleX == 0 || childScaleY == 0) {
            if(recalculateWindow)
                calculateWindow();
            return Rectangle.tmp.set(childX, childY, 0, 0);     // empty
        }

        float windowLeft;
        float windowRight;
        float windowTop;
        float windowBottom;

        if(includeSelf) {
            windowLeft = childX - Math.abs(childScaleX / 2f);
            windowRight = childX + Math.abs(childScaleX / 2f);
            windowTop = childY + Math.abs(childLength / 2f);
            windowBottom = childY - Math.abs(childLength / 2f);
        }
        else {
            windowLeft = +Float.MAX_VALUE;
            windowRight = -Float.MAX_VALUE;
            windowTop = -Float.MAX_VALUE;
            windowBottom = +Float.MAX_VALUE;
        }

        if(includeChilds) {
            UIElement<?> window = null;
            while ((window = iterate(window, UIElement.class, true, excluded)) != null) {
                if (recalculateWindow)
                    window.calculateWindow();
                // Calculate bounds
                float left = window.childX - Math.abs(window.childScaleX / 2.0f);
                float right = window.childX + Math.abs(window.childScaleX / 2.0f);
                float top = window.childY + Math.abs(window.childLength / 2.0f);
                float bottom = window.childY - Math.abs(window.childLength / 2.0f);
                if (left < windowLeft)
                    windowLeft = left;
                if (right > windowRight)
                    windowRight = right;
                if (top > windowTop)
                    windowTop = top;
                if (bottom < windowBottom)
                    windowBottom = bottom;
            }
        }

        return Rectangle.tmp.set(windowLeft, windowBottom, windowRight - windowLeft, windowTop - windowBottom);
    }

	@Override
	public void translate(float x, float y) {
		childX += (x * childScaleX);			 
		childY += (y * childScaleY);
		/*		TODO: 20150402- Doesnt work well with Keep Fishin as ScaleAnim's position doenst work
		childX += x; 
		childY += y;
		*/
	}

	@Override
	public void rotate(float rotate) {
		this.rotate += rotate;
	}

	@Override
	public void scale(float x, float y) {
		childScaleX *= x;
		childScaleY *= y;
		childLength *= y;
	}

    @Override
    public void shear(float sx, float sy) {
        // shear not supported
    }

    @Override
	public void applyGlobalMatrix() {
		Matrices.model.translate(childX, childY, 0.0f);
        Matrices.model.scale(childScaleX, childScaleY, 1.0f);
        if(rotate != 0f)
            Matrices.model.rotate(0, 0, -1, rotate);
    }
	
	@Override
	public void scissor(float x, float y, float width, float height) {
		Matrices.scissor.mul(x, y, width, height);          // 20180907: previously was set() not mul()
	}

	// User implementation
	public void detachWithAnim() {
		detach();
	}

	@Override
	public <A extends MaterialAttribute> A getAttribute(Class<A> attribType, int layer) {
		return null;
	}
}