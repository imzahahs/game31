package sengine;

import java.util.HashMap;

public abstract class Cache<T> {
	
	public static boolean persistentReferences = true;
	
	static final HashMap<Object, Cache<?>> cache = new HashMap<Object, Cache<?>>();
	
	static void initializeTables() {
		synchronized(cache) {
			// Inform all existing caches that it was recreated
			for(Cache<?> objectsCache : cache.values())
				objectsCache.cacheRecreated(persistentReferences);		// This cache was recreated
		}
	}
	
	static void releaseTables() {
		synchronized(cache) {
			// Inform all caches that it was released
			for(Cache<?> objectsCache : cache.values())
				objectsCache.cacheReleased(persistentReferences);
		}
	}
	
	/**
	 * Gets the cache with the specified id from the globals cache list. 
	 * @param id - Object, id of the cache.
	 * @return {@link NamedCache} with the specified id.
	 */
	@SuppressWarnings("unchecked")
	public static <T> NamedCache<T> get(Object id) {
		synchronized(cache) {
			return (NamedCache<T>)cache.get(id);
		}
	}
	
	public final Class<T> clazz;
	public final Object id;

	/**
	 * <p>Create a new Cache with the specified class as the id and saves in global caches list.</p>
	 * <p>The Class information is used by {@link #list()} for runtime array creation.</p> 
	 * <p>Throws {@link RuntimeException} if id already exists in global caches list.</p>
	 * @param id - Object, id of this cache.
	 * @param clazz - {@link Class} of the cached objects, used for runtime array creation. 
	 */
	public Cache(Class<T> clazz) {
		this(clazz, clazz);
	}
	
	/**
	 * <p>Create a new Cache with the specified id and saves in global caches list.</p>
	 * <p>The Class information is used by {@link #list()} for runtime array creation.</p> 
	 * <p>Throws {@link RuntimeException} if id already exists in global caches list.</p>
	 * @param id - Object, id of this cache.
	 * @param clazz - {@link Class} of the cached objects, used for runtime array creation. 
	 */
	public Cache(Class<T> clazz, Object id) {
		this.clazz = clazz;
		this.id = id;
		// Save this cache in global caches list
		synchronized(cache) {
			if(cache.containsKey(id))
				throw new RuntimeException("Cache with the specified id already exists!");
			cache.put(id, this);
		}
	}
	
	// Global cache lifetime callbacks
	/**
	 * Called when {@link Sys} is recreated. Used to release previously instantiated objects.
	 */
	protected abstract void cacheRecreated(boolean persistentReferences);
	/**
	 * Called when {@link Sys} is released.
	 */
	protected abstract void cacheReleased(boolean persistentReferences);
	
	// Cache handling
	public abstract void remember(T object);
	public abstract void forget(T object);
	public abstract T get();
	public abstract T[] list();
	public abstract void clear();
	
	/**
	 * Releases all reference to objects and unregisters from global cache list.
	 */
	public void release() {
		clear();
		cacheReleased(persistentReferences);
		synchronized(cache) {
			cache.remove(id);
		}
	}
}
