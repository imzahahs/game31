package sengine.model;

public class SkeletalAnimator {
	// Previous frame
	SkeletalFrame prevFrameBase = null;
	SkeletalAnimation prevFrameAnim = null;
	int prevFrameIndex = 0;
	// Next frame
	SkeletalFrame nextFrameBase = null;
	SkeletalAnimation nextFrameAnim = null;
	int nextFrameIndex = 0;
	// Override frame
	SkeletalFrame overrideFrameBase = null;
	SkeletalAnimation overrideFrameAnim = null;
	int overrideFrameIndex = 0;
	float tOverrideFrameTime = -1;
	boolean overrideQueued = false;
	// Frame progress
	float tFrameTime = 0.0f;
	float tCurrentFrameTime = 0.0f;
	
	public void overrideFrame(SkeletalFrame frameBase) {
		overrideFrame(frameBase, 0f);
	}
	public void queueFrame(SkeletalFrame frameBase) {
		queueFrame(frameBase, 0f);
	}
	
	public void overrideFrame(SkeletalFrame frameBase, float tTransitionTime) {
		play(frameBase, null, 0, tTransitionTime, false);
	}
	public void queueFrame(SkeletalFrame frameBase, float tTransitionTime) {
		play(frameBase, null, 0, tTransitionTime, true);
	}
	
	
	public void overrideAnimation(SkeletalAnimation anim) {
		overrideAnimation(anim, 0, anim.getTransitionTime(nextFrameAnim, isAnimating()));
	}
	public void queueAnimation(SkeletalAnimation anim) {
		queueAnimation(anim, 0, anim.getTransitionTime(nextFrameAnim, isAnimating()));
	}
	
	public void overrideAnimation(SkeletalAnimation anim, int frameIndex, float tTransitionTime) {
		play(anim.baseFrame, anim, frameIndex, tTransitionTime, false);
	}
	public void queueAnimation(SkeletalAnimation anim, int frameIndex, float tTransitionTime) {
		play(null, anim, frameIndex, tTransitionTime, true);
	}
	
	public void overrideOverlay(SkeletalAnimation anim) {
		overrideOverlay(anim, 0, anim.getTransitionTime(nextFrameAnim, isAnimating()));
	}
	public void queueOverlay(SkeletalAnimation anim) {
		queueOverlay(anim, 0, anim.getTransitionTime(nextFrameAnim, isAnimating()));
	}
	
	public void overrideOverlay(SkeletalAnimation anim, int frameIndex, float tTransitionTime) {
		play(null, anim, frameIndex, tTransitionTime, false);
	}
	public void queueOverlay(SkeletalAnimation anim, int frameIndex, float tTransitionTime) {
		play(null, anim, frameIndex, tTransitionTime, true);
	}
	
	public void play(SkeletalFrame frameBase, SkeletalAnimation anim, int frameIndex, float tTransitionTime, boolean queued) {
		overrideFrameBase = frameBase;
		overrideFrameAnim = anim;
		overrideFrameIndex = frameIndex;
		tOverrideFrameTime = tTransitionTime;
		overrideQueued = queued;
	}
	
	public void clearOverride() {
		overrideFrameBase = null;
		overrideFrameAnim = null;
		tOverrideFrameTime = -1;
	}
	
	public boolean isOverrideQueued() {
		return tOverrideFrameTime != -1;
	}
	
	public boolean isAnimating() {
		return tFrameTime != 0.0f;
	}
	
	public boolean willOverlay(SkeletalFrame with) {
		// If either prev or next frame base is null, animation will overlay on existing joint data
		// Or if either prev or next frame base is not the same structure, animation will overlay on some parts of existing joint data
		return prevFrameBase == null || nextFrameBase == null || !prevFrameBase.isSameStructure(with) || !nextFrameBase.isSameStructure(with);
	}
	
	public SkeletalAnimation getAnimation() {
		if(overrideFrameAnim != null)
			return overrideFrameAnim;
		else if(nextFrameAnim != null)
			return nextFrameAnim;
		else
			return prevFrameAnim;
	}
	
	public float getRemainingTime() {
		// Calculate the total animation time that is currently queued
		float tTotalTime = tFrameTime - tCurrentFrameTime;		// Remaining time in current frame
		// Add override time
		if(tOverrideFrameTime != -1) {
			// Transition time
			tTotalTime += tOverrideFrameTime;
			// Override anim time
			if(overrideFrameAnim != null)
				tTotalTime += (overrideFrameAnim.numFrames - overrideFrameIndex - 1) * overrideFrameAnim.tFrameInterval;
			// If override is immediate, thats all
			if(!overrideQueued)
				return tTotalTime;
		}
		// Add remaining current animation time
		if(nextFrameAnim != null)
			tTotalTime += (nextFrameAnim.numFrames - nextFrameIndex - 1) * nextFrameAnim.tFrameInterval;
		return tTotalTime;
	}
	
	public boolean nextFrame() {
		// Transfer frame data
		prevFrameBase = nextFrameBase;
		prevFrameAnim = nextFrameAnim;
		prevFrameIndex = nextFrameIndex;

		// Set next frame to override if available
		if(tOverrideFrameTime != -1 && !overrideQueued)
			forceOverride();
		else if(nextFrameAnim != null && (nextFrameIndex + 1) < nextFrameAnim.numFrames) {
			// Else next frame is available, continue with it
			nextFrameIndex++;
			tFrameTime = nextFrameAnim.tFrameInterval;
		}
		else if(tOverrideFrameTime != -1 && overrideQueued)
			forceOverride();
		else {
			// Else no frame to proceed to, no more animation
			tFrameTime = 0.0f;
			return false;
		}
		
		return true;
	}
	
	public boolean forceOverride() {
		if(tOverrideFrameTime == -1)
			return false;
		// Else override is queued, apply now 
		nextFrameBase = overrideFrameBase;
		nextFrameAnim = overrideFrameAnim;
		nextFrameIndex = overrideFrameIndex;
		tFrameTime = tOverrideFrameTime;
		// Clear override data
		clearOverride();
		return true;
	}
	
	public void elapseTime(float tElapsed) {
		tCurrentFrameTime += tElapsed;
	}
	
	public void resetTime() {
		tCurrentFrameTime = 0.0f;
	}
	
	public void reset() {
		prevFrameBase = null;
		prevFrameAnim = null;
		nextFrameBase = null;
		nextFrameAnim = null;
		overrideFrameBase = null;
		overrideFrameAnim = null;
		tOverrideFrameTime = -1;
		tFrameTime = 0.0f;
		tCurrentFrameTime = 0.0f;
	}
	
	public boolean update() {
		// Check if finished frame
		while(tCurrentFrameTime > tFrameTime) {
			float tPrevFrameTime = tFrameTime;
			// Attempt to proceed frame
			if(!nextFrame())
				return false;
			
			// Else proceeded to next frame, adjust current frame time 
			tCurrentFrameTime -= tPrevFrameTime;
		}
		return true;
	}
	
	public void apply(SkeletalFrame frame) {
		// Calculate frame progress
		float frameProgress;
		if(tFrameTime == 0.0f || tCurrentFrameTime >= tFrameTime)
			frameProgress = 1.0f;
		else
			frameProgress = tCurrentFrameTime / tFrameTime;
		// Lerp
		frame.lerp(prevFrameBase, nextFrameBase, frameProgress);
		frame.lerpDeltas(prevFrameAnim, prevFrameIndex, nextFrameAnim, nextFrameIndex, frameProgress);
	}
	
	public SkeletalAnimator set(SkeletalAnimator copyFrom) {
		// Copy
		prevFrameBase = copyFrom.prevFrameBase;
		prevFrameAnim = copyFrom.prevFrameAnim;
		prevFrameIndex = copyFrom.prevFrameIndex;
		nextFrameBase = copyFrom.nextFrameBase;
		nextFrameAnim = copyFrom.nextFrameAnim;
		nextFrameIndex = copyFrom.nextFrameIndex;
		overrideFrameBase = copyFrom.overrideFrameBase;
		overrideFrameAnim = copyFrom.overrideFrameAnim;
		overrideFrameIndex = copyFrom.overrideFrameIndex;
		tOverrideFrameTime = copyFrom.tOverrideFrameTime;
		overrideQueued = copyFrom.overrideQueued;
		tFrameTime = copyFrom.tFrameTime;
		tCurrentFrameTime = copyFrom.tCurrentFrameTime;
		// Return for chaining
		return this;
	}
}