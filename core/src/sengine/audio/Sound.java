package sengine.audio;

import sengine.File;
import sengine.GarbageCollector;
import sengine.Sys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Sound extends Audio.Sound implements Stream.SoundProvider, GarbageCollector.Collectible {
	static final String TAG = "Sound";
	
	
	public static float tDefaultGCTime = 300.0f;
	// Android always uses seperate thread to load files, first sound being played might fail
	public static boolean preloadSounds = false;

	// Identity
	com.badlogic.gdx.audio.Sound sound = null;
	// GC time
	float tLastUsed = -1;
	
	@MassConstructor
	public Sound(String filename, Audio.Hints hints) {
		super(filename, hints);
		if(preloadSounds)
			ensureLoaded();
	}
	@Override
	public Object[] mass() {
		return new Object[] { filename, hints };
	}
	
	@Override
	protected Stream createStream(boolean loop, float volume, float pitch, float pan) {
		return Stream.create(this, loop, volume, pitch, pan);
	}
	
	@Override
	public com.badlogic.gdx.audio.Sound getSound() {
		// Load sound if not avail
		ensureLoaded();
		return sound;
	}
	
	@Override
	public boolean performGC(boolean forced) {
		// Do not GC if preload, also prevent force GC on pause() events if preloading is enabled
		// Cache will directly call unload() upon exit
		if(preloadSounds)
			return false;		// no GC
		float elapsed = Sys.getTime() - tLastUsed;
		if(elapsed < Sound.tDefaultGCTime && !forced)
			return false;		// no GC
		// Else GC now
		unload();
		return true;
	}

	@Override
	public synchronized void ensureLoaded() {
		tLastUsed = Sys.getTime();
		if(sound != null)
			return;		// already loaded
		// Load actual sound file, if not yet loaded
		String filename = Sound.this.filename;
		Sys.info(TAG, "Loading Sound: " + filename);
		FileHandle handle = File.open(filename, true, true);
		if(Audio.synchronizedIO) {
			synchronized (File.class) {
				sound = Gdx.audio.newSound(handle);
			}
		}
		else
			sound = Gdx.audio.newSound(handle);
		// Remember to GC
		GarbageCollector.add(this);
	}

	@Override
	public synchronized void unload() {
		if(sound == null)
			return;
		sound.dispose();
		sound = null;
	}
	
	
	public static Audio.Sound load(String filename) {
		// 20130321 - Static method maintained for backwards support for Sound.load() calls
		return Audio.load(filename);
	}
}
