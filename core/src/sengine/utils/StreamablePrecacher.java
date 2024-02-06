package sengine.utils;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

import sengine.Entity;
import sengine.Streamable;
import sengine.Universe;

public class StreamablePrecacher extends Entity<Universe> {
	private final Array<Streamable> set = new Array<Streamable>(Streamable.class);
	private int loaded = 0;

	public StreamablePrecacher() {
		// Flow control
		renderingEnabled = false;
		processEnabled = true;
	}

	@Override
	protected synchronized void process(Universe v, float renderTime) {
		// Refresh loaded set
		loaded = 0;
		for(int c = 0; c < set.size; c++) {
			Streamable s = set.items[c];
			s.load();
			if(s.isLoaded())
				loaded++;
		}
	}

	
	public synchronized void add(Streamable streamable) {
		if(!set.contains(streamable, true))
			set.add(streamable);
	}
	
	public synchronized void addAll(Streamable ... streamables) {
		for(int c = 0; c < streamables.length; c++)
			add(streamables[c]);
	}
	
	public synchronized void remove(Streamable streamable) {
		if(set.removeValue(streamable, true))
			loaded--;		// process loop will make sure if this is true later
	}
	
	public synchronized void removeAll(Streamable ... streamables) {
		for(int c = 0; c < streamables.length; c++)
			remove(streamables[c]);
	}
	
	public synchronized void clear() {
		set.clear();
		loaded = 0;
	}
	
	public synchronized boolean isLoadedAll() {
		return loaded == set.size;
	}

	public synchronized void ensureLoadedAll() {
		for(int c = 0; c < set.size; c++) {
			Streamable s = set.items[c];
			s.ensureLoaded();
		}
		loaded = set.size;
	}
}