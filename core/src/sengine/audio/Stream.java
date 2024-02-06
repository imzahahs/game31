package sengine.audio;

import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;

import sengine.Processor;

public class Stream {
	static final String TAG = "Stream";
	
	public static enum Status {
		LOADING,
		STARTED,
		STOPPED
	}
	
	public static interface SoundProvider {
		public Sound getSound();
	}
	
	static final int MAX_STREAMS = 16;
	
	static final ArrayList<Stream> streams = new ArrayList<Stream>(MAX_STREAMS);

	// Stream data
	final SoundProvider provider;
	long id = 0;
	float volume = 1f;
	float pitch = 1f;
	float pan = 0f;
	boolean paused = false;
	Status status = Status.LOADING;
	
	
	private Stream(SoundProvider provider) {
		this.provider = provider;
	}
	
	public static void stopAllStreams() {
		Stream[] list = new Stream[streams.size()];
		list = streams.toArray(list);
		for(Stream s : list) {
			if(s != null)
				s.stop();
		}
		streams.clear();
	}
	
	public static Stream create(final SoundProvider provider, final boolean loop, float volume, float pitch, float pan) {
		// Create a new stream from the specified sound
		if(streams.size() == (MAX_STREAMS - 1)) {
			// Stop last sound as max streams are reached
			Stream stream = streams.remove(0);
			stream.stop();
		}
		// Create new sound, but run on audio thread
		final Stream stream = new Stream(provider);
		stream.volume = volume;
		stream.pitch = pitch;
		stream.pan = pan;
		// Run stream in audio thread, not here
		new Processor.Task(true, true, true, true) {
			Sound sound;
			@Override
			protected void processAsync() {
				sound = provider.getSound();
			}
			
			@Override
			protected boolean completeSync() {
				synchronized(stream) {
					if(stream.status == Status.STOPPED)
						return true;
					if(sound == null) {
						// Sound unavailable
						stream.status = Status.STOPPED;
						return true;			
					}
					stream.id = loop ? sound.loop(stream.volume, stream.pitch, stream.pan) : sound.play(stream.volume, stream.pitch, stream.pan);
					stream.status = Status.STARTED;
					if(stream.paused)
					    sound.pause(stream.id);
				}
				return true;
			}
			
		}.start();
		streams.add(stream);
		return stream;
	}
	
	public synchronized void stop() {
		if(status == Status.STOPPED)
			return;		// already stopped
		boolean wasLoading = status == Status.LOADING;
		status = Status.STOPPED;
		if(wasLoading)
			return;
		Sound sound = provider.getSound();
		if(sound == null)
			return;		// unavailable
		sound.stop(id);
		// Remove from active streams
		streams.remove(this);
	}

	public synchronized boolean isPlaying() {
	    return status != Status.STOPPED && !paused;
    }

    public synchronized void pause() {
        if(status != Status.STARTED || paused)
            return;		// cannot set now
        Sound sound = provider.getSound();
        if(sound == null)
            return;		// unavailable
        sound.pause(id);
        paused = true;
    }

    public synchronized void resume() {
        if(status != Status.STARTED || !paused)
            return;		// cannot set now
        Sound sound = provider.getSound();
        if(sound == null)
            return;		// unavailable
        sound.resume(id);
        paused = false;
    }
	
	public synchronized void volume(float volume) {
	    if(this.volume == volume)
	        return;
		this.volume = volume;
		if(status != Status.STARTED)
			return;		// cannot set now
		Sound sound = provider.getSound();
		if(sound == null)
			return;		// unavailable
		sound.setVolume(id, volume);
	}
	
	public synchronized void pitch(float pitch) {
        if(this.pitch == pitch)
            return;
		this.pitch = pitch;
		if(status != Status.STARTED)
			return;		// cannot set now
		Sound sound = provider.getSound();
		if(sound == null)
			return;		// unavailable
		sound.setPitch(id, pitch);
	}
	
	public synchronized void pan(float pan, float volume) {
        if(this.pan == pan && this.volume == volume)
            return;
		this.pan = pan;
		this.volume = volume;
		if(status != Status.STARTED)
			return;		// cannot set now
		Sound sound = provider.getSound();
		if(sound == null)
			return;		// unavailable
		sound.setPan(id, pan, volume);
	}
}
