package sengine.ui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;

import sengine.Entity;
import sengine.Sys;
import sengine.Universe;
import sengine.animation.Animation;
import sengine.graphics2d.Font;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Sprite;
import sengine.graphics2d.Text;
import sengine.mass.Mass;

public class TextBox extends UIElement<Universe> {

	Text text;

	Animation.Instance startAnim;
	Animation.Loop idleAnim;
	Animation.Instance endAnim;

	public TextBox animation(Animation startAnim, Animation idleAnim, Animation endAnim) {
		this.startAnim = startAnim != null ? startAnim.start() : null;
		this.idleAnim = idleAnim != null ? idleAnim.loop() : null;
		if(this.idleAnim != null)
			this.idleAnim.reset();
		this.endAnim = endAnim != null ? endAnim.start() : null;

		return this;
	}

	public TextBox text(String text) {
		if(this.text == null)
			throw new RuntimeException("text not set!");
		this.text.text(text);
		return this;
	}

	public TextBox text(Text text) {
		this.text = text;
        if(text != null)
		    length = text.length;
		return this;
	}

    public Text text() {
        return text;
    }

    public TextBox autoLengthText(String text) {
        if(this.text == null)
            throw new RuntimeException("text not set!");
        this.text().text(text).autoLength();
        length = this.text.length;
        calculateWindow();
        return this;
    }

    public TextBox autoWidthText(String text) {
        if(this.text == null)
            throw new RuntimeException("text not set!");
        if(metrics == null)
            metrics = new Metrics();
        Rectangle rect = this.text.text(text).bounds();
//        int align = this.text.align;
//		this.text.center();         // Will only work on center as we trimming all excess space
//		this.text.align = align;

        // Scale rect
        metrics.scaleX *= rect.width;
        metrics.scaleY *= rect.width;

        this.text.wrapChars *= rect.width;              // previously is text.size = 1f / rect.width, changing to the new method allows to retain alignment

        this.text.length = length = rect.height / rect.width;

        calculateWindow();
        return this;
    }

    public TextBox length(float length) {
		if(this.text == null)
			throw new RuntimeException("text not set!");
		text.length(length);
		this.length = text.length;
		return this;
	}

	public TextBox() {
		// default
	}

	@MassConstructor
	public TextBox(Metrics metrics, String name, float length, UIElement<?>[] childs,
				   Text text,
				   Animation startAnim, Animation idleAnim, Animation endAnim
	) {
		super(metrics, name, length, childs);

		text(text);
		animation(startAnim, idleAnim, endAnim);
	}

	@Override
	public Object[] mass() {
		return Mass.concat(super.mass(),
				// Visuals
				text,
				// Animation
				startAnim != null ? startAnim.anim : null,
				idleAnim != null ? idleAnim.anim : null,
				endAnim != null ? endAnim.anim : null
		);
	}

	@Override
	public TextBox instantiate() {
		TextBox box = new TextBox();
		box.name(name);
		box.viewport(viewport);
		if(metrics != null)
			box.metrics(metrics.instantiate());
		if(text != null)
			box.text(text.instantiate());
		box.animation(
				startAnim != null ? startAnim.anim : null,
				idleAnim != null ? idleAnim.anim : null,
				endAnim != null ? endAnim.anim : null
		);
		box.instantiateChilds(this);
		return box;
	}

	@Override
	public TextBox viewport(UIElement<?> viewport) {
		super.viewport(viewport);
		return this;
	}

	@Override
	public TextBox name(String name) {
		super.name(name);
		return this;
	}

	@Override
	public TextBox windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
		super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
		return this;
	}

	@Override
	public TextBox metrics(Metrics metrics) {
		super.metrics(metrics);
		return this;
	}

	@Override
	public TextBox attach() {
		super.attach();
		return this;
	}

	@Override
	public TextBox attach(int index) {
		super.attach(index);
		return this;
	}

	@Override
	protected void recreate(Universe v) {
		// font = font.instantiate();   20160705 - dont think this is necessary
		// Reset anims
		if(startAnim != null)
			startAnim.reset();
		if(endAnim != null)
			endAnim.stop();
	}

	@Override
	protected void render(Universe v, float r, float renderTime) {
		if(text == null)
			return;		// nothing to render

        // Get length from text
        length = text.length;
        calculateWindow();

		if(!isVisible())
			return;         // not visible

		Matrices.push();

        // Apply anims if avail
        if(idleAnim != null)
            idleAnim.updateAndApply(this, getRenderDeltaTime());
        if(startAnim != null && startAnim.isActive())
            startAnim.updateAndApply(this, getRenderDeltaTime());
        else if(endAnim != null && endAnim.isActive() && !endAnim.updateAndApply(this, getRenderDeltaTime()))
            detach();		// ended

		applyWindowAnim();

        applyMatrix();

		// Render
		text.render();

		Matrices.pop();
	}

	@Override
	protected void release(Universe v) {
		// Make sure to detach if already detaching with anim
		if(endAnim != null && endAnim.isActive())
			detach();
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
		if(endAnim != null) {
			if(!endAnim.isActive())
				endAnim.reset();
		}
		else
			detach();
	}

	@Override
	public <T extends MaterialAttribute> T getAttribute(Class<T> attribType, int layer) {
		return text.font.getAttribute(attribType, layer);
	}
}