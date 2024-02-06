package sengine.ui;

import sengine.Entity;
import sengine.Universe;
import sengine.animation.Animation;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.Sprite;
import sengine.mass.Mass;

public class Toast extends StaticSprite {
	
	private float time = 0f;

	// Current
	private float tDetachScheduled = Float.MAX_VALUE;

    public Toast time(float time) {
        this.time = time;
        return this;
    }

	@Override
	public Toast passThroughInput(boolean passThroughInput) {
		super.passThroughInput(passThroughInput);
		return this;
	}

	@Override
	public Toast visual(Mesh mat, int target) {
		super.visual(mat, target);
		return this;
	}

    @Override
    public Toast target(int target) {
        super.target(target);
        return this;
    }

    @Override
    public Toast length(float length) {
        super.length(length);
        return this;
    }

    @Override
    public Toast visual(Mesh mat) {
        super.visual(mat);
        return this;
    }

    @Override
    public Toast animation(Animation startAnim, Animation idleAnim, Animation endAnim) {
        super.animation(startAnim, idleAnim, endAnim);
        return this;
    }

    public Toast() {
        // default
    }

    @MassConstructor
    public Toast(Metrics metrics, String name, float length, UIElement<?>[] childs,
                        Mesh mat, int target,
                        Animation startAnim, Animation idleAnim, Animation endAnim,
                        boolean passThroughInput, float time
    ) {
        super(metrics, name, length, childs, mat, target, startAnim, idleAnim, endAnim, passThroughInput);

        time(time);
    }

    @Override
    public Object[] mass() {
        return Mass.concat(super.mass(),
                time
        );
    }

    @Override
	public Toast instantiate() {
        Toast toast = new Toast();
        toast.name(name);
        toast.viewport(viewport);
        if(metrics != null)
			toast.metrics(metrics.instantiate());
		toast.visual(mat, target);
		toast.animation(
				startAnim != null ? startAnim.anim : null,
				idleAnim != null ? idleAnim.anim : null,
				endAnim != null ? endAnim.anim : null
		);
		toast.passThroughInput(passThroughInput);
		toast.instantiateChilds(this);
        toast.time(time);
		return toast;
	}

	@Override
	public Toast viewport(UIElement<?> viewport) {
		super.viewport(viewport);
		return this;
	}

	@Override
	public Toast name(String name) {
		super.name(name);
		return this;
	}

	@Override
	public Toast windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
		super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
		return this;
	}

	@Override
	public Toast metrics(Metrics metrics) {
		super.metrics(metrics);
		return this;
	}

	@Override
	public Toast attach() {
		super.attach();
		return this;
	}

	@Override
	public Toast attach(int index) {
		super.attach(index);
		return this;
	}

    @Override
    public void attach(Entity<?> parent, int index) {
        super.attach(parent, index);

        tDetachScheduled = getRenderTime() + (startAnim != null ? startAnim.anim.length : 0) + time;
    }

    @Override
    protected void recreate(Universe v) {
        super.recreate(v);

        tDetachScheduled = getRenderTime() + (startAnim != null ? startAnim.anim.length : 0) + time;
    }

    @Override
	protected void render(Universe v, float r, float renderTime) {
		if(renderTime >= tDetachScheduled) {
            detachWithAnim();
        }
		
		super.render(v, r, renderTime);
	}
}
