package sengine.game;

import java.util.ArrayList;
import java.util.HashMap;

import sengine.Sys;

public class ThoughtsTable<T, V> {
	static final String TAG = "ThoughtsTable";
	
	public abstract static class Thought<T, V> {
		public final int id;
		boolean isActive = false;
		
		public Thought(int id) {
			this.id = id;
		}
		
		public final boolean isActive() { return isActive; }
		
		protected abstract boolean reset(T t, V v);
		protected abstract void process(T t, V v, float renderTime);
		protected abstract void render(T t, V v, float r, float renderTime);
		protected abstract boolean stop(T t, V v);
		
		public abstract Thought<T, V> instantiate();
		
	}
	
	final HashMap<Integer, Thought<T, V>> table = new HashMap<Integer, Thought<T, V>>();
	final ArrayList<Thought<T, V>> activeThoughts = new ArrayList<Thought<T, V>>();
	
	public int clear(T t, V v, boolean forced) {
		int unstoppable = 0;
		for(int c = 0; c < activeThoughts.size(); c++) {
			Thought<T, V> thought = activeThoughts.get(c);
			if(!thought.stop(t, v)) {
				unstoppable++;
				if(!forced)
					continue;
			}
			// Else forced stop
			thought.isActive = false;
			activeThoughts.remove(c);
			c--;
		}
		return unstoppable;
	}
	
	public void registerThought(Thought<? extends T, V> thought) {
		table.put(thought.id, (Thought<T, V>)thought);
	}
	
	public boolean unregisterThought(Thought<? extends T, V> thought, T t, V v) {
		Thought<? extends T, V> registered = table.get(thought.id);
		if(registered != thought) { 
			Sys.debug(TAG, "Cannot unregister thought due to mismatch: " + thought + " " + registered + " " + thought.id);
			return false;
		}
		// Try to stop thought
		if(!stopThought(t, v, thought.id)) {
			Sys.debug(TAG, "Cannot unregister thought due to unstoppable thought: " + thought + " " + thought.id);
			return false;
		}
		// Else stopped, so remove
		table.remove(thought.id);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public <U extends Thought<T, V>> U getThought(int id) {
		return (U) table.get(id);
	}
	
	public boolean resetThought(T t, V v, int id) { 
		Thought<T, V> thought = table.get(id);
		if(thought == null)
			return false;		// no thought found
		else if(!thought.reset(t, v))
			return false;		// cannot start
		// Else started
		if(!thought.isActive) {
			thought.isActive = true;
			activeThoughts.add(thought);
		}
		return true;
	}
	
	public void process(T t, V v, float renderTime) { 
		for(int c = 0; c < activeThoughts.size(); c++)
			activeThoughts.get(c).process(t, v, renderTime);
	}
	
	public void render(T t, V v, float r, float renderTime) { 
		for(int c = 0; c < activeThoughts.size(); c++)
			activeThoughts.get(c).render(t, v, r, renderTime);
	}
	
	public boolean stopThought(T t, V v, int id) { 
		Thought<T, V> thought = table.get(id);
		if(thought == null || !thought.isActive)
			return true;		// no thought, or thought is inactive implied success
		else if(!thought.stop(t, v))		// Try to stop thought
			return false;		// cannot stop thought
		thought.isActive = false;
		// Else stopped, remove from list
		activeThoughts.remove(thought);
		return true;
	}
}
