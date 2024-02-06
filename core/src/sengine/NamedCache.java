package sengine;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class NamedCache<T> extends Cache<T> {
	static final String TAG = "Cache";
	
	static class ObjectInstance<T> {
		final T instance;
		int instances = 1;
		
		public ObjectInstance(T instance) {
			this.instance = instance;
		}
		
	}
	
	final HashMap<String, ObjectInstance<T>> active = new HashMap<String, ObjectInstance<T>>();
	
	public NamedCache(Class<T> clazz) {
		super(clazz, clazz);
	}
	
	public NamedCache(Class<T> clazz, Object id) {
		super(clazz, id);
	}
	
	
	public synchronized boolean remember(T object, String id) {
		ObjectInstance<T> instance = active.get(id);
		if(instance != null) {
			if(instance.instance != object) 
				return false;		// instance of a different object exists
			else
				return true;		// Else object already remembered
		}
		// Else create new ObjectInstance
		instance = new ObjectInstance<T>(object);
		active.put(id, instance);
		return true;
	}
	
	/**
	 * Forgets the specified object, does not consider the object's reference count.
	 * @param hashCode - hashCode of the Object to be forgotten
	 */
	public synchronized void forget(String id) {
		active.remove(id);
	}

	@Override
	public synchronized void clear() {
		active.clear();
	}
	
	/**
	 * Lists all the objects in this cache. Does not increase their reference count.
	 * @return All objects in the cache.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized T[] list() {
		Collection<ObjectInstance<T>> instances = active.values();
		T[] list = (T[])Array.newInstance(clazz, instances.size());
		int c = 0; 
		for(ObjectInstance<T> instance : instances) {
			list[c] = instance.instance;
			c++;
		}
		return list;
	}
	
	public synchronized T get(String id) {
		ObjectInstance<T> instance = active.get(id);
		if(instance == null)
			return null;
		// Else object found
		instance.instances++;
		return instance.instance;
	}
	
	public synchronized boolean release(String id) {
		ObjectInstance<T> instance = active.get(id);
		if(instance == null)
			return true;
		instance.instances--;
		if(instance.instances <= 0) {
			forget(id);
			return true;
		}
		return false;
	}
	
	@Override
	public void remember(T object) {
		remember(object, null);			// save default object
	}
	
	public synchronized void forget(T object) {
		// Get id of object
		for(Entry<String, ObjectInstance<T>> e : active.entrySet()) {
			if(e.getValue().instance == object) {
				forget(e.getKey());
				return;
			}
		}
		// Else not found
	}
	
	public T get() {
		return get(null);				// get default object
	}
}


