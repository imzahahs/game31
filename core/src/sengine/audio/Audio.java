package sengine.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import sengine.File;
import sengine.NamedCache;
import sengine.Sys;
import sengine.calc.Range;
import sengine.mass.MassSerializable;

public class Audio {
	static final String TAG = "Audio";
	
	public static float defaultExclusiveTime = 0.25f;
	
//	public static float minSameSoundInterval = 0.1f;
	public static float soundVolume = 1.0f;
	public static float musicVolume = 1.0f;
	public static Range defaultPitchRange = new Range(1.0f, 0.2f); 
	
	public static String inferredHintsFile = "defaultSoundHints";

	public static boolean synchronizedIO = false;

	
	// Just a dummy cache for static initialization	
	// Sounds
	static final NamedCache<Sound> cache = new NamedCache<Sound>(Sound.class) {
		@Override
		protected void cacheRecreated(boolean persistentReferences) {
			// Reset
			Audio.reset();
			if(persistentReferences) {
				// System is android and requires preloading, need to load all sounds now 
				Sound[] sounds = list();
				for(Sound c : sounds)
					c.ensureLoaded();
			}
		}

		@Override
		protected void cacheReleased(boolean persistentReferences) {
			// Do not clear sounds, just cleanup them
			Sound[] sounds = list();
			for(Sound c : sounds) {
				c.reset();
				c.unload();
			}
			if(!persistentReferences)
				clear();
			// Streams
			Stream.streams.clear();
			// Music
            if(music != null) {
                music.dispose();
                music = null;
            }
            musicPath = null;
		}
	};

	public static final void reset() {
		if(music != null)
			music.dispose();
		music = null;
		musicPath = null;
		musicLoop = false;
		musicPlaying = false;
	}
	
	static Music music = null;
	static String musicPath = null;
	static boolean musicLoop = false;
	static boolean musicPlaying = false;
	
	public static void stopMusic() {
        if(music == null)
            return;
        music.stop();
        music.dispose();
        music = null;
        musicPath = null;
        musicLoop = false;
        musicPlaying = false;
	}
	
	public static void playMusic(String path, final boolean loop) {
		playMusic(path, loop, 1f);
	}
	
	public static void playMusic(String path, final boolean loop, final float volume) {
        if(musicPath != null && path.contentEquals(musicPath) && (loop == musicLoop)) {
            if(!musicPlaying || !music.isPlaying()) {
                music.stop();
                music.play();		// resume music
                musicPlaying = true;
            }
            music.setVolume(volume * musicVolume);
            return;		// playing the same music
        }
        if(music != null)
            music.dispose();
        // Save implied music length
        music = Gdx.audio.newMusic(File.open(path, true, true));
        music.setLooping(loop);
        music.setVolume(volume * musicVolume);
        music.play();
        musicPath = path;
        musicLoop = loop;
        musicPlaying = true;
	}

	/**
	 * This method is not guaranteed to return consistent results as music is started on another thread.
	 * @return true if music is playing
	 */
	public static boolean isMusicPlaying() {
		return music != null && music.isPlaying();
	}
	
	public static void setMusicVolume(final float volume) {
        if(music == null)
            return;
        music.setVolume(volume * musicVolume);
	}

	public static float getMusicVolume() {
		if(music == null)
			return musicVolume;
		else
			return music.getVolume();
	}

	public static void setMusicPosition(final float position) {
		if(music == null)
			return;
		music.setPosition(position);
	}

	public static float getMusicPosition() {
		if(music == null)
			return 0;
		return music.getPosition();
	}
	
	public static void pauseMusic() {
        if(music == null || !music.isPlaying())
            return;
        music.pause();
        musicPlaying = false;
	}
	public static void resumeMusic() {
		resumeMusic(false);
	}
	
	public static void resumeMusic(final boolean replay) {
        if(music == null || musicPlaying)
            return;		// no music
        if(replay)
            music.stop();
        music.play();
        musicPlaying = true;
	}
	
	// Sound base class
	public interface Provider {
		Sound create(String filename, Hints hints);
	}
	
	public static class Hints {
		public float length;
		public float exclusiveTime;
		public Range baseVolume;
		public Range basePitch;
		// Source
		public Provider provider;
		
		public Hints() { 
		}

		public Hints(float length, float exclusiveTime) {
			this(length, exclusiveTime, null, null, null);
		}
		
		public Hints(float length, float exclusiveTime, Range baseVolume, Range basePitch) {
			this(length, exclusiveTime, baseVolume, basePitch, null);
		}

		public Hints(float length, float exclusiveTime, Range baseVolume, Range basePitch, Provider provider) {
			this.length = length;
			this.exclusiveTime = exclusiveTime;
			this.baseVolume = baseVolume;
			this.basePitch = basePitch;
			this.provider = provider;
		}
	}
	
	public static Hints getHints(String filename) {
		Hints hints = File.getHints(filename, false);
		if(hints != null)
			return hints;		// hints already found
		// Else hints not directly associated with this file, infer it from 'defaultSoundHints.hints' from each parent directory
		if(!filename.startsWith("/"))
			filename = "/" + filename;		// enforce root
		String[] paths = filename.split("/");
		for(int c = paths.length - 2; c >= 0; c--) {
			String inferred = paths[0];
			for(int i = 1; i <= c; i++)
				inferred += "/" + paths[i];
			inferred += "/" + inferredHintsFile;
			// Try to get inferred hints
			Hints inferredHints = File.getHints(inferred, false);
			if(inferredHints == null)
				continue;
			// Else inferred hints was found, infer this specified sound hint
			hints = new Hints(
				inferredHints.length, 
				inferredHints.exclusiveTime, 
				inferredHints.baseVolume, 
				inferredHints.basePitch, 
				null		// default provider
			);
			// Save hints
			File.saveHints(filename, hints);
			return hints;
		}
		// Else hints were never found
		hints = new Hints(defaultExclusiveTime, defaultExclusiveTime);
		File.saveHints(filename, hints);
		return hints;
	}

	public static abstract class Sound implements MassSerializable {
		// Identity
		public final String filename;
		public final Hints hints;
		
		// Stream control
		float lastPlayed = -1;
		Stream lastPlayedStream = null;
		
		public void reset() {
			lastPlayed = -1;
			lastPlayedStream = null;
		}
		
		protected Sound(String filename, Hints hints) {
			this.filename = filename;
			this.hints = hints;
			// Add to cache
			cache.remember(this, filename);
		}

		public void stop() {
            if(lastPlayedStream == null)
                return;
            lastPlayedStream.stop();
            reset();
        }

		public Stream lastPlayed() {
			return lastPlayedStream;
		}
		
		public Stream play() { return play(false, 1.0f, defaultPitchRange.generate(), 0.0f); }
		public Stream play(float volume) { return play(false, volume, defaultPitchRange.generate(), 0.0f); }
		public Stream play(float volume, float pitch) { return play(false, volume, pitch, 0.0f); }
		public Stream play(float volume, float pitch, float pan) { return play(false, volume, pitch, pan); }
		
		public Stream loop() { return play(true, 1.0f, 1.0f, 0.0f); }
		public Stream loop(float volume) { return play(true, volume, 1.0f, 0.0f); }
		public Stream loop(float volume, float pitch) { return play(true, volume, pitch, 0.0f); }
		public Stream loop(float volume, float pitch, float pan) { return play(true, volume, pitch, pan); }
		
		Stream play(boolean loop, float volume, float pitch, float pan) {
			volume *= soundVolume;
			// Adjust to hints volume and pitch
			if(hints.baseVolume != null)
				volume *= hints.baseVolume.generate();
			if(hints.basePitch != null)
				pitch *= hints.basePitch.generate();
			// If exactly same time, do not create new stream
			if(Sys.getTime() == lastPlayed && !loop)
				return lastPlayedStream;
			// Add new stream
			Stream stream = createStream(loop, volume, pitch, pan);
			// Update last played
			if(!loop) {
				lastPlayed = Sys.getTime();
				lastPlayedStream = stream;
			}
			return stream;
		}
		
		@Override
		public String toString() {
			return "Sound#" + filename;
		}
		
		public abstract void ensureLoaded();
		public abstract void unload();
		
		protected abstract Stream createStream(boolean loop, float volume, float pitch, float pan);
		
		@Override
		public void finalize() {
			unload();
		}
	}
	
	public static Sound load(String filename) {
		// Find an already loaded sound first
		Sound sound = cache.get(filename); 
		if(sound != null)
			return sound;
		// Else load new sound
		Hints hints = Audio.getHints(filename);
		if(hints.provider == null)
			sound = new sengine.audio.Sound(filename, hints);
		else
			sound = hints.provider.create(filename, hints);
		return sound;
	}
}
