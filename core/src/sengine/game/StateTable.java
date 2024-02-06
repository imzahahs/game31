package sengine.game;

import java.util.Iterator;

import sengine.Sys;

import com.badlogic.gdx.utils.IntMap;

public class StateTable<T, V, S extends StateTable.State<T, V>> implements Iterable<S> {
	static final String TAG = "StateTable";
	
	public static final int NULL_STATE_ID = 0;
	
	public static enum StopResponse {
		STOPPED,
		ABSORBED,
		UNSTOPPABLE,
	}
	
	public static class State<T, V> {
		public final int id;
		
		public State(int id) {
			if(id == NULL_STATE_ID)
				throw new IllegalArgumentException("State id of 0 is reserved for null state!");
			this.id = id;
		}
		
		protected void start(T t, V v) {
			// nothing
		}
		protected void process(T t, V v, float renderTime) {
			// nothing
		}
		protected void render(T t, V v, float r, float renderTime) {
			// nothing
		}
		protected StopResponse stop(T t, V v, int successorID) {
			// Default behaviour, absorb on same id, else stop
			if(successorID == id)
				return StopResponse.ABSORBED;
			return StopResponse.STOPPED;
		}
		
		public State<T, V> instantiate() {
			return this;		// assume immutable
		}
	}

	
	final IntMap<S> table = new IntMap<S>();
	S currentState = null;
	
	@Override
	public Iterator<S> iterator() {
		return table.values();
	}
	
	public boolean clear(T t, V v, boolean forced) {
		boolean stopped = true;
		if(currentState != null && currentState.stop(t, v, NULL_STATE_ID) != StopResponse.STOPPED) {
			if(forced)
				stopped = false;
			else	// not forced
				return false;
		}
		// Stopped
		currentState = null;
		table.clear();
		return stopped;
	}
	
	public void registerState(S state) {
		table.put(state.id, state);
	}
	
	public boolean unregisterState(S state) {
		S registered = table.get(state.id);
		if(registered != state) {
			Sys.debug(TAG, "Cannot unregister state due to mismatch: " + state + " " + registered + " " + state.id);
			return false;
		}
		// Else can be removed
		table.remove(state.id);
		return true;
	}
	
	public S getState(int id) {
		return table.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public <U extends S> U getState(int id, Class<U> clazz) {
		State<? extends T, V> state = table.get(id);
		if(state == null)
			return null;
		else if(clazz.isAssignableFrom(state.getClass()))
			return (U)state;
		else
			return null;
	}
	
	public S getCurrentState() { 
		return currentState;
	}
	
	public boolean startState(T t, V v, int id) {
		// Find new state first
		S state = table.get(id);
		if(state == null) {
			Sys.debug(TAG, "Unable to find state for startState: " + id);
			return false;
		}
		// Try to stop existing state first
		if(currentState != null) {
			StopResponse response = currentState.stop(t, v, id);
			if(response == StopResponse.ABSORBED)
				return true;		// current state absorbed requested state
			else if(response == StopResponse.UNSTOPPABLE)
				return false;		// cannot be stopped;
			// else stopped, so continue
			currentState = null;
		}
		// Else existing state stopped, start new state
		state.start(t, v);
		// Only update if current state is not already set (due to recursive call to startState)
		if(currentState != null)
			return false;		// state did not start, another did
		// Else state started
		currentState = state;
		return true;
	}
	
	public void process(T t, V v, float renderTime) { 
		if(currentState != null)
			currentState.process(t, v, renderTime);
	}
	
	public void render(T t, V v, float r, float renderTime) { 
		if(currentState != null)
			currentState.render(t, v, r, renderTime);
	}
	
	public boolean stopState(T t, V v) {
		// Try to stop current state
		if(currentState == null)
			return true;		// no state to stop, implied stop is successfull
		// Try to stop current state
		switch(currentState.stop(t, v, NULL_STATE_ID)) {
		case STOPPED:
			currentState = null;		// clean stop
		case ABSORBED:
			return true;	// aborbed
		default:
		case UNSTOPPABLE:
			return false;
		}
	}
}
