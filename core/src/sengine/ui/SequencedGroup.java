package sengine.ui;

import sengine.Universe;
import sengine.audio.Audio;

public class SequencedGroup extends UIElement<Universe> {
	
	public static class SoundQueue extends UIElement<Universe> {
		
		public final Audio.Sound sound;

		public SoundQueue(UIElement<?> viewport, Audio.Sound sound) {
			viewport(viewport);
			
			this.sound = sound;
		}

		@Override
		protected void recreate(Universe v) {
			// Play
			sound.play();
			detach();		// no more use
		}
	}
	
	public static class MusicQueue extends UIElement<Universe> {
		
		public final String musicPath;
		public final boolean musicLoop;

		public MusicQueue(UIElement<?> viewport, String musicPath, boolean musicLoop) {
			viewport(viewport);
			
			this.musicPath = musicPath;
			this.musicLoop = musicLoop;
		}

		@Override
		protected void recreate(Universe v) {
			// Play
			Audio.playMusic(musicPath, musicLoop);
			detach();		// no more use
		}
	}
	
	public final UIElement<?>[] elements;
	public final float[] tAttachSchedule;
	
	// Current
	final boolean[] attachStatus;
	boolean isDone = false;
	boolean isDetaching = false;
	
	public SequencedGroup(UIElement<?> viewport, UIElement<?>[] elements, float[] tAttachSchedule) {
		viewport(viewport);
		
		if(elements.length != tAttachSchedule.length)
			throw new IllegalArgumentException("Invalid array lengths: " + elements.length + ", " + tAttachSchedule.length);
		
		this.elements = elements;
		this.tAttachSchedule = tAttachSchedule;
		
		this.attachStatus = new boolean[elements.length];
	}

	@Override
	protected void recreate(Universe v) {
		// Reset
		isDone = false;
		isDetaching = false;
		for(int c = 0; c < attachStatus.length; c++)
			attachStatus[c] = false;
		render(v, getRenderTimeR(), getRenderTime());
	}
	
	@Override
	protected void render(Universe v, float r, float renderTime) {
		calculateWindow();
		if(isDetaching) {
			// Check if all elements have detached
			for(int c = 0; c < elements.length; c++)
				if(elements[c].isEffectivelyRendering())
					return;		// not yet
			// Else all elements have detached
			detach();
			return;
		}
		else if(isDone)
			return;		// nothing to do
		// Else is attaching
		int attached = 0;
		for(int c = 0; c < elements.length; c++) {
			if(attachStatus[c]) {
				attached++;
				continue;		// already attached
			}
			// Check time
			if(renderTime >= tAttachSchedule[c]) {
				// Time to attach this element
				attached++;
				elements[c].attach();
				attachStatus[c] = true;
			}
		}
		if(attached == elements.length)
			isDone = true;		// all done
	}
	
	@Override
	public void detachWithAnim() {
		// Detach all with anim
		for(int c = 0; c < elements.length; c++)
			elements[c].detachWithAnim();
		isDetaching = true;
	}

	@Override
	protected void release(Universe v) {
		// Make sure all elements are detached
		for(int c = 0; c < elements.length; c++)
			elements[c].detach();
	}
}
