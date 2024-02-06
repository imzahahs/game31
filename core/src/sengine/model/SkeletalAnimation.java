package sengine.model;

import java.util.Map;
import sengine.mass.MassSerializable;

public class SkeletalAnimation implements MassSerializable {
	
	public static final String TAG_THIS = "this";
	public static final String TAG_ARBITRARY = "arbitrary";
	public static final String TAG_DEFAULT = "default";
	
	public static SkeletalAnimation load(String filename) {
		return MD5Loader.loadAnimation(filename);
	}
	
	// Identity
	public final float tFrameInterval;
	public final int numFrames;
	public final float tAnimTime;

	// Animation data
	public final SkeletalFrame baseFrame;
	public final float[] deltas;
	public final int[] frameOffsets;
	
	// Transition times
	public final String[] tags;
	public final Map<String, Float> tNormalTransitionTimes;
	public final Map<String, Float> tInterruptedTransitionTimes;
	

	public float getTransitionTime(SkeletalAnimation from, boolean interrupted) {
		// Select lookup
		Map<String, Float> lookup = interrupted ? tInterruptedTransitionTimes : tNormalTransitionTimes;
		Float tTransitionTime = null;
		// Check if transitioning from animation
		if(from != null) {
			// Check if transitioning from own animation
			if(from == this)
				tTransitionTime = lookup.get(TAG_THIS);
			else {
				// Else from another animation
				for(int c = 0; c < from.tags.length; c++) {
					tTransitionTime = lookup.get(from.tags[c]);
					if(tTransitionTime != null)
						return tTransitionTime;
				}
			}
		}
		else
			tTransitionTime = lookup.get(TAG_ARBITRARY);		// Else from an arbitrary pose
		// If found transition time, return
		if(tTransitionTime != null)
			return tTransitionTime;
		// If no cant find transition time, return default
		tTransitionTime = lookup.get(TAG_DEFAULT);
		return tTransitionTime != null ? tTransitionTime : 0.0f;
	}
	
	public void applyFrame(SkeletalFrame frame, int index) {
		index %= numFrames;
		frame.set(baseFrame);
		frame.unpackDeltas(deltas, frameOffsets[index], frameOffsets[index + 1], baseFrame);
	}
	
	public void apply(SkeletalFrame frame, float time) {
		float tWrapped = time % tAnimTime;
		
		int fromFrame = (int)(tWrapped / tFrameInterval);
		int toFrame = fromFrame + 1;
		float r = (tWrapped % tFrameInterval) / tFrameInterval;

		// Lerp frame
		frame.set(baseFrame);
		frame.lerpDeltas(deltas, frameOffsets[fromFrame], frameOffsets[fromFrame + 1], baseFrame,
			deltas, frameOffsets[toFrame], frameOffsets[toFrame + 1], baseFrame, r);
	}

	@MassConstructor
	public SkeletalAnimation(
		float tFrameInterval, 
		SkeletalFrame baseFrame, float[] deltas, int[] frameOffsets,
		String[] tags, Map<String, Float> tNormalTransitionTimes, Map<String, Float> tInterruptedTransitionTimes 
	) {
		this.tFrameInterval = tFrameInterval;
		this.numFrames = frameOffsets.length - 1;
		this.tAnimTime = tFrameInterval * (numFrames - 1);
		
		this.baseFrame = baseFrame;
		this.deltas = deltas;
		this.frameOffsets = frameOffsets;

		this.tags = tags;
		this.tNormalTransitionTimes = tNormalTransitionTimes;
		this.tInterruptedTransitionTimes = tInterruptedTransitionTimes;
	}
	
	@Override
	public Object[] mass() {
		return new Object[] { tFrameInterval, baseFrame, deltas, frameOffsets, tags, tNormalTransitionTimes, tInterruptedTransitionTimes};
	}
}
