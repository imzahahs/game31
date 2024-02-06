package sengine.game;

import sengine.Sys;

import java.util.HashMap;

public class CollisionTable {
	static final String TAG = "CollisionTable";
	
	public static abstract class Handler<T, U, V> {
		public final int tID;
		public final int uID;
		
		public Handler(int tID, int uID) {
			this.tID = tID;
			this.uID = uID;
		}
		
		protected abstract boolean process(T t, U u, V v);
		
		public final boolean collide(Object t, Object u, V v) {
			try {
				return process((T)t, (U)u, v);
			}
			catch(ClassCastException e) {
				Sys.error(TAG, "Collision handler class mismatch", e);
				return false;
			}
		}
		
		public abstract Handler<T, U, V> instantiate();
	}
	
	final HashMap<Integer, HashMap<Integer, Handler<?, ?, ?>>> table;
	
	public void clear() {
		table.clear();
	}
	
	public CollisionTable() {
		this.table = new HashMap<Integer, HashMap<Integer, Handler<?, ?, ?>>>();
	}
	
	public void registerHandler(Handler<?,?, ?> handler) {
		// Register in look up table
		HashMap<Integer, Handler<?, ?, ?>> handlers = table.get(handler.tID);
		if(handlers == null) {
			// t's handler table was never created, so create now
			handlers = new HashMap<Integer, Handler<?, ?, ?>>();
			// Check if handler exists
			table.put(handler.tID, handlers);
		}
		if(handlers.get(handler.uID) != null)
			Sys.debug(TAG + "#registerHandler", "Look up exists for: " + handler + " : " + handler.tID + " " + handler.uID);
		handlers.put(handler.uID, handler);
		// Make sure there is no duplicate reverse lookup (only applicable where t is not equals to u)
		if(handler.tID != handler.uID) {
			handlers = table.get(handler.uID);
			if(handlers != null && handlers.get(handler.tID) != null) {
				// Reverse lookup exists
				Sys.debug(TAG + "#registerHandler", "Reverse look up exists for: " + handler + " : " + handler.tID + " " + handler.uID);
				handlers.remove(handler.tID);
			}
		}
	}

	public void unregisterHandler(Handler<?, ?, ?> handler) {
		// Only unregister when registered lookup matches specified handler
		HashMap<Integer, Handler<?, ?, ?>> handlers = table.get(handler.tID);
		if(handlers == null)
			Sys.debug(TAG + "#unregisterHandler", "Lookup failed for: " + handler + " : " + handler.tID + " " + handler.uID);
		else if(handlers.get(handler.uID) != handler)
			Sys.debug(TAG + "#unregisterHandler", "Different handler exists: " + handler + " : " + handler.tID + " " + handler.uID);
		else
			handlers.remove(handler.uID);
	}
	
	public final Handler<?, ?, ?> getHandler(int tID, int uID) {
		Handler<?, ?, ?> handler;
		// Retrieve look up table
		HashMap<Integer, Handler<?, ?, ?>> handlers = table.get(tID);
		if(handlers != null && (handler = handlers.get(uID)) != null)
			return handler;
		else {
			// Try reverse look up
			handlers = table.get(uID);
			if(handlers != null && (handler = handlers.get(tID)) != null)
				return handler;
			else
				return null;
		}
	}
	
	public boolean collide(Object t, int tID, Object u, int uID, Object v) {
		Handler handler;
		// Retrieve look up table
		HashMap<Integer, Handler<?, ?, ?>> handlers = table.get(tID);
		if(handlers != null && (handler = handlers.get(uID)) != null)
			return handler.collide(t, u, v);
		// Else try reverse look up
		handlers = table.get(uID);
		if(handlers != null && (handler = handlers.get(tID)) != null)
			return handler.collide(u, t, v);
		// No handler available
		return false;
	}
}
