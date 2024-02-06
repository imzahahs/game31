package sengine.audio;

import sengine.File;
import sengine.audio.Audio.Hints;
import sengine.audio.Audio.Sound;
import sengine.calc.SetSelector;
import sengine.mass.MassSerializable;

public class SoundsSet extends Sound {
	static final String TAG = "SoundSet";
	
	public static Hints createHints(SetSelector<Sound> sounds) {
		// Calculate average length and exclusive time
		float averageLength = 0.0f;
		float averageExclusiveTime = 0.0f;
		for(int c = 0; c < sounds.set.length; c++) {
			averageLength += (sounds.set[c].hints.length / sounds.set.length);
			averageExclusiveTime += (sounds.set[c].hints.exclusiveTime / sounds.set.length);
		}
		// Create provider
		Provider provider = new Provider(sounds);
		return new Hints(
			averageLength, 
			averageExclusiveTime, 
			null, 
			null, 
			provider
		);
	}
	
	public static class Provider implements Audio.Provider, MassSerializable {
		
		public final SetSelector<Sound> sounds;
		
		@MassConstructor
		public Provider(SetSelector<Sound> sounds) {
			this.sounds = sounds;
		}
		@Override
		public Object[] mass() {
			return new Object[] { sounds };
		}

		@Override
		public Sound create(String filename, Hints hints) {
			return new SoundsSet(filename, hints, sounds);
		}
	}
	
	public static SoundsSet create(String filename, SetSelector<Sound> sounds) {
		if(File.getHints(filename, false) == null)
			File.saveHints(filename, createHints(sounds));
		return (SoundsSet)Audio.load(filename);
	}
	
	public final SetSelector<Sound> sounds;
	
	@MassConstructor
	public SoundsSet(String filename, Hints hints, SetSelector<Sound> sounds) {
		super(filename, hints);
		this.sounds = sounds;
	}
	@Override
	public Object[] mass() {
		return new Object[] { filename, hints, sounds };
	}

	@Override
	protected Stream createStream(boolean loop, float volume, float pitch, float pan) {
		Sound selected = sounds.select();
		return selected.createStream(loop, volume, pitch, pan);
	}

	@Override
	public void ensureLoaded() {
		for(Sound s : sounds.set)
			s.ensureLoaded();
	}

	@Override
	public void unload() {
		for(Sound s : sounds.set)
			s.unload();
	}
}
