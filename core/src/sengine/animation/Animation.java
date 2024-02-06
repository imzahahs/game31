package sengine.animation;

import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public abstract class Animation {
	
	public static abstract class Handler implements MassSerializable {
		public final Animation anim;

		float length;

		float progress = -1;
		float renderTime = 0.0f;

		protected Handler(Animation anim) {
			this(anim, anim.length, -1, 0f);
		}

		protected Handler(Animation anim, float length, float progress, float renderTime) {
			this.anim = anim;
			this.length = length;
			this.progress = progress;
			this.renderTime = renderTime;
		}
		
		public final void stop() { 
			progress = -1;
		}
		
		public final void reset() { 
			progress = 0.0f; 
		}
		
		public final float getProgress() {
			return progress;
		}
		
		public final float getLength() { 
			return length;
		}
		
		public final float getElapsed() {
			return progress * length;
		}
		
		public final float getRemainingTime() {
			return (1.0f - progress) * length;
		}

		public final void setProgress(float progress) {
			if(progress >= 1.0f)
				this.progress = 1f;
			else if(progress >= 0f)
				this.progress = progress;
		}
		
		public final void randomizeProgress() {
			this.progress = (float)Math.random();
		}
		
		public final void setLength(float length) {
			if(length > 0.0f)
				this.length = length;
		}
		
		public final void apply(Animatable2D a) {
			anim.apply(renderTime, progress, a);
		}
		
		public final void applyReversed(Animatable2D a) {
			anim.apply(renderTime, 1.0f - progress, a);
		}
		
		public final boolean updateAndApply(Animatable2D a, float deltaTime) { 
			boolean update = update(deltaTime);
			apply(a);
			return update;
		}
		
		public final boolean updateAndApplyReversed(Animatable2D a, float deltaTime) { 
			boolean update = update(deltaTime);
			applyReversed(a);
			return update;
		}
		
		public abstract boolean isActive();
		public abstract float updateAccurate(float deltaTime);
		
		public final boolean update(float deltaTime) {
			return updateAccurate(deltaTime) == 0.0f;
		}
	}
	
	public static class Instance extends Handler {

        public Instance(Animation anim) {
			super(anim);
		}

        @MassConstructor
        public Instance(Animation anim, float length, float progress, float renderTime) {
            super(anim, length, progress, renderTime);
        }

        @Override
        public Object[] mass() {
            return new Object[] { anim, length, progress, renderTime };
        }
		
		@Override
		public float updateAccurate(float deltaTime) {
			if(progress == -1)
				return -1;
			renderTime += deltaTime;
			if(length <= 0)
				return deltaTime;		// invalid length
			progress += (deltaTime / length);
			if(progress >= 1.0f) {
				float overflow = (progress - 1.0f) * length;
				// Anim maxed
				progress = 1.0f;
				return overflow == 0.0f ? Float.MIN_VALUE : overflow;
			}
			// Else anim still running, no overflow
			return 0.0f;
		}

		@Override
		public boolean isActive() {
			return progress != -1 && progress != 1.0f;
		}

	}
	
	public static class Loop extends Handler {
		public Loop(Animation anim) {
			super(anim);
		}

        @MassConstructor
        public Loop(Animation anim, float length, float progress, float renderTime) {
            super(anim, length, progress, renderTime);
        }

        @Override
        public Object[] mass() {
            return new Object[] { anim, length, progress, renderTime };
        }

		@Override
		public float updateAccurate(float deltaTime) {
			if(progress == -1)
				return -1;
			renderTime += deltaTime;
			if(length > 0f) {
				progress += (deltaTime / length);
				progress %= 1.0f;
			}
			return 0.0f;
		}

		@Override
		public boolean isActive() {
			return progress != -1;
		}

	}
	
	public final float length;
	
	protected Animation(float length) {
		this.length = length;
	}
	
	public Instance start() {
		return new Instance(this);
	}
	public Instance startAndReset() {
		Instance instance = new Instance(this);
		instance.reset();
		return instance;
	}
	public Loop loop() {
		return new Loop(this);
	}
	public Loop loopAndReset() {
		Loop loop = new Loop(this);
		loop.reset();
		return loop;
	}

	public abstract void apply(float renderTime, float progress, Animatable2D a);
}
