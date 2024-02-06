package sengine.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;

import sengine.Universe;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Sprite;

public class SliderBar extends UIElement<Universe> {
	
	public static float touchLengthMultiplier = 1.2f;
	
	public final Sprite bar;
	public final Sprite head;
	public final float headSize;
	
	public boolean enabled = true;
	
	public float progress = 1f;
	public float progressIncrements = 0f;
	
	int touchedPointer = -1;
	
	public SliderBar(UIElement<?> viewport, Metrics metrics, Sprite bar, Sprite head, float headSize) {
		viewport(viewport);
		
		this.metrics = metrics;
		this.length = bar.length;
		
		this.bar = bar;
		this.head = head;
		this.headSize = headSize;
	}
	
	
	@Override
	protected void render(Universe v, float r, float renderTime) {
		calculateWindow();

		if(!isVisible())
			return;         // not visible

		applyWindowAnim();

		Matrix4 m = Matrices.model;
		Matrices.push();
		applyMatrix();
		
		Matrices.push();
		
		Matrices.scissor.x = childX - (childScaleX / 2f) + (childScaleX * (progress / 2f));
		Matrices.scissor.y = childY;
		Matrices.scissor.width = childScaleX * progress;
		Matrices.scissor.height = childLength;

		bar.render();
		
		Matrices.pop();
		
		m.translate(progress - 0.5f, 0, 0);
		m.scale(headSize, headSize, headSize);
		
		head.render();
		
		Matrices.pop();
	}
	
	boolean moveSlider(float x) {
		float newProgress = x - (childX - (childScaleX / 2f));
		if(newProgress > childScaleX)
			newProgress = 1f;
		else if(newProgress < 0f)
			newProgress = 0f;
		else
			newProgress /= childScaleX;
		if(progressIncrements > 0) {
			newProgress = Math.round(newProgress / progressIncrements) / (1f / progressIncrements);
			if(newProgress > 1f)
				newProgress = 1f;
		}
		if(newProgress == progress)
			return false;
		progress = newProgress;
		return true;
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
			float length = childLength;
			float size = childScaleX;
			float headLength = head.length * headSize;
			if(headLength > bar.length)
				length *= (headLength / bar.length);
			size += (childScaleX * headSize);
			if(checkTouched(x, y, size, size, length)) {		// TODO: test this
				touchedPointer = pointer;
				if(moveSlider(x))
					slideSet(progress);
				return true;
			}
			return false;
			
		case INPUT_TOUCH_DRAGGED:
			if(touchedPointer != pointer)
				return false;		// not right pointer
			if(moveSlider(x))
				slideSet(progress);
			return true;
		
		case INPUT_TOUCH_UP:
			if(touchedPointer != pointer)
				return false;		// not right pointer
			touchedPointer = -1;
			slideReleased(progress);
			return true;

		default:
			return false;
		}
	}
	
	// Implementation should override these
	public void slideSet(float progress) {
	}
	
	public void slideReleased(float progress) {
	}

}
