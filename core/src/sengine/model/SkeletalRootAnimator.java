package sengine.model;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class SkeletalRootAnimator extends SkeletalAnimator {
	
	
	static final Vector3 vec1 = new Vector3();
	static final Vector3 vec2 = new Vector3();
	static final Quaternion quat1 = new Quaternion();
	static final Quaternion quat2 = new Quaternion();
	
	// Root 
	final Vector3 prevDeltaVec = new Vector3();
	final Quaternion prevDeltaQuat = new Quaternion();
	final Vector3 currentDeltaVec = new Vector3();
	final Quaternion currentDeltaQuat = new Quaternion();
	boolean isNewSequence = false;
	
	public void extractDelta(Vector3 deltaVec) {
		extractDelta(deltaVec, null);
	}
	
	public void extractDelta(Quaternion deltaQuat) {
		extractDelta(null, deltaQuat);
	}
	
	public void extractDelta(Vector3 deltaVec, Quaternion deltaQuat) {
		// Calculate frame progress
		float frameProgress;
		if(tFrameTime == 0.0f || tCurrentFrameTime >= tFrameTime)
			frameProgress = 1.0f;
		else
			frameProgress = tCurrentFrameTime / tFrameTime;
		// Determine current delta
		if(isNewSequence)
			SkeletalFrame.calculateJointDelta(vec1, quat1, prevFrameAnim, prevFrameIndex, nextFrameAnim, nextFrameIndex, 0, prevDeltaVec, prevDeltaQuat, frameProgress);
		else
			SkeletalFrame.calculateJointDelta(vec1, quat1, prevFrameAnim, prevFrameIndex, nextFrameAnim, nextFrameIndex, 0, null, null, frameProgress);
		// Determine base root
		SkeletalFrame.getJointDelta(vec2, quat2, prevFrameAnim, 0, 0);
		// Calculate new delta
		vec1.sub(vec2);
		quat1.mulLeft(-quat2.x, -quat2.y, -quat2.z, quat2.w);
		// Determine delta accumulated and set new delta
		if(deltaVec != null) {
			deltaVec.set(vec1).sub(currentDeltaVec);
			currentDeltaVec.set(vec1);
		}
		if(deltaQuat != null) {
			deltaQuat.set(quat1).mulLeft(-currentDeltaQuat.x, -currentDeltaQuat.y, -currentDeltaQuat.z, currentDeltaQuat.w).nor();
			currentDeltaQuat.set(quat1);
		}
	}
	
	@Override
	public boolean nextFrame() {
		if(isNewSequence) {
			isNewSequence = false;
			// Update root
			currentDeltaVec.sub(prevDeltaVec);
			currentDeltaQuat.mulLeft(-prevDeltaQuat.x, -prevDeltaQuat.y, -prevDeltaQuat.z, prevDeltaQuat.w);
		}

		return super.nextFrame();
	}
	
	@Override
	public boolean forceOverride() {
		if(!super.forceOverride())
			return false;
		// Remember current delta
		isNewSequence = true;
		prevDeltaVec.set(currentDeltaVec);
		prevDeltaQuat.set(currentDeltaQuat);
		return true;
	}
	
	@Override
	public void apply(SkeletalFrame frame) {
		// Calculate frame progress
		float frameProgress;
		if(tFrameTime == 0.0f || tCurrentFrameTime >= tFrameTime)
			frameProgress = 1.0f;
		else
			frameProgress = tCurrentFrameTime / tFrameTime;
		// Lerp
		frame.lerp(prevFrameBase, nextFrameBase, frameProgress);
		frame.getJoint(0, vec2, quat2);
		frame.lerpDeltas(prevFrameAnim, prevFrameIndex, nextFrameAnim, nextFrameIndex, frameProgress);
		// Recalculate root
		if(isNewSequence)
			SkeletalFrame.calculateJointDelta(vec1, quat1, prevFrameAnim, prevFrameIndex, nextFrameAnim, nextFrameIndex, 0, prevDeltaVec, prevDeltaQuat, frameProgress);
		else
			SkeletalFrame.calculateJointDelta(vec1, quat1, prevFrameAnim, prevFrameIndex, nextFrameAnim, nextFrameIndex, 0, null, null, frameProgress);
		// Negate delta
		vec1.sub(currentDeltaVec);
		quat1.mul(-currentDeltaQuat.x, -currentDeltaQuat.y, -currentDeltaQuat.z, currentDeltaQuat.w);
		vec2.add(vec1);
		quat2.mul(quat1);
		frame.setJoint(0, vec2, quat2);
	}
	
	public void resetDeltas() {
		currentDeltaVec.setZero();
		currentDeltaQuat.idt();
		prevDeltaVec.setZero();
		prevDeltaQuat.idt();
	}
	
	@Override
	public void reset() {
		super.reset();
		resetDeltas();
	}
	
	@Override
	public SkeletalAnimator set(SkeletalAnimator copyFrom) {
		super.set(copyFrom);
		resetDeltas();
		return this;
	}
	
	public SkeletalRootAnimator set(SkeletalRootAnimator copyFrom) {
		super.set(copyFrom);
		// Set deltas
		prevDeltaVec.set(copyFrom.prevDeltaVec);
		prevDeltaQuat.set(copyFrom.prevDeltaQuat);
		currentDeltaVec.set(copyFrom.currentDeltaVec);
		currentDeltaQuat.set(copyFrom.currentDeltaQuat);
		isNewSequence = copyFrom.isNewSequence;
		// Return for chaining
		return this;
	}
}
