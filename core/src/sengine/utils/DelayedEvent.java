package sengine.utils;

import sengine.Entity;
import sengine.Universe;

public abstract class DelayedEvent<T extends Universe> extends Entity<T> {
	
	public final float tDelayTime;

	public DelayedEvent(float tDelayTime) {
		this(tDelayTime, false);		// default not precise, use process loop
	}

	public DelayedEvent(float tDelayTime, boolean preciseTiming) {
		this.tDelayTime = tDelayTime;
		// Determine flow control
		if(preciseTiming) {
			// Whichever comes first
			renderingEnabled = true;
			processEnabled = true;
		}
		else {
			// Only use process loop
			renderingEnabled = false;
			processEnabled = true;
		}
	}

	@Override
	protected void process(T v, float renderTime) {
		if(renderTime < tDelayTime)
			return;
		// Trigger
		trigger(v);
		detach();
	}

	@Override
	protected void render(T v, float r, float renderTime) {
		if(renderTime < tDelayTime)
			return;
		// Trigger
		trigger(v);
		detach();
	}
	
	// Trigger
	protected abstract void trigger(T v);
}
