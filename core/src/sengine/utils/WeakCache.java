package sengine.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class WeakCache<K, V> extends AbstractMap<K, V> {
	
	class KeyedWeakReference extends WeakReference<V> {
		final K key;

		public KeyedWeakReference(K key, V value) {
			super(value, queue);
			
			this.key = key;
		}
	}
	
	class CachedEntry implements Entry<K, V> {
		final K key;
		
		CachedEntry(K key) {
			this.key = key;
		}
		
		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return get(key);
		}

		@Override
		public V setValue(V value) {
			return put(key, value);
		}
	}
	
	class CachedSet implements Set<Entry<K, V>> {
		class SetIterator implements Iterator<Entry<K, V>> {
			Iterator<Entry<K, KeyedWeakReference>> iterator = cache.entrySet().iterator();
			K lastKey = null;

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Entry<K, V> next() {
				lastKey = iterator.next().getKey();
				return new CachedEntry(lastKey);
			}

			@Override
			public void remove() {
				WeakCache.this.remove(lastKey);
			}
		}

		@Override
		public boolean add(Entry<K, V> e) {
			V v = put(e.getKey(), e.getValue());
			return v != e.getValue();
		}

		@Override
		public boolean addAll(Collection<? extends Entry<K, V>> c) {
			boolean changed = false;
			for(Entry<K, V> e : c) {
				if(add(e))
					changed = true;
			}
			return changed;
		}

		@Override
		public void clear() {
			WeakCache.this.clear();
		}

		@Override
		public boolean contains(Object o) {
			return WeakCache.this.containsValue(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for(Object o : c) {
				if(!contains(o))
					return false;
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			return cache.isEmpty();
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new SetIterator();
		}

		@Override
		public boolean remove(Object o) {
			for(KeyedWeakReference e : cache.values()) {
				if(e.get() == o) {
					WeakCache.this.remove(e.key);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean changed = false;
			for(Object o : c) {
				if(remove(o))
					changed = true;
			}
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			HashSet<Object> removals = new HashSet<Object>();
			for(KeyedWeakReference e : cache.values()) {
				Object o = e.get();
				if(o != null && !c.contains(o))
					removals.add(o);
			}
			if(removals.size() > 0) {
				removeAll(removals);
				return true;
			}
			return false;
		}

		@Override
		public int size() {
			return cache.size();
		}

		@Override
		public Object[] toArray() {
			Object[] a = new Object[cache.size()];
			int c = 0;
			for(KeyedWeakReference e : cache.values()) {
				a[c] = e.get();
				c++;
			}
			return a;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] a) {
			int size = cache.size();
			if(size > a.length)
				a = Arrays.copyOf(a, size);
			int c = 0;
			for(KeyedWeakReference e : cache.values()) {
				a[c] = (T) e.get();
				c++;
			}
			return a;
		}
	}
	
	
	final ReferenceQueue<V> queue = new ReferenceQueue<V>();
	final ConcurrentHashMap<K, KeyedWeakReference> cache = new ConcurrentHashMap<K, KeyedWeakReference>();
	final WeakHashMap<V, K> keys = new WeakHashMap<V, K>();
	final HashSet<V> strongCache = new HashSet<V>();
	
	@SuppressWarnings("unchecked")
	void cleanup() {
		KeyedWeakReference e = null;
		while((e = (KeyedWeakReference)queue.poll()) != null) {
			KeyedWeakReference ce = cache.get(e.key);
			// Only remove if reference is same with current reference
			if(e == ce)
				cache.remove(e.key);
		}
	}

	@Override
	public V put(K key, V value) { 
		cleanup();
		if(value == null)
			return remove(key);
		else {
			// Create new reference
			KeyedWeakReference e = new KeyedWeakReference(key, value);
			e = cache.put(key, e);
			keys.put(value, key);
			if(e != null) {
				// Replacing value
				V v = e.get();
				if(v != null) {
					strongCache.remove(v);
					return v;
				}
			}
			return null;
		}
	}

	@Override
	public V get(Object key) {
		cleanup();
		KeyedWeakReference e = cache.get(key);
		return e == null ? null : e.get();
	}
	
	@Override
	public V remove(Object key) {
		cleanup();
		KeyedWeakReference e = cache.remove(key);
		if(e != null) {
			V v = e.get();
			if(v != null)
				keys.remove(v);
			strongCache.remove(v);
			return v;
		}
		return null;	// does not exist
	}
	
	@Override
	public void clear() {
		// Clear all references
		clearStrongRefs();
		cache.clear();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new CachedSet();
	}
	
	@Override
	public boolean containsKey(Object key) {
		KeyedWeakReference e = cache.get(key);
		return e != null && e.get() != null;
	}
	
	public K findKey(V value) {
		return keys.get(value);
	}

	// Strong references
	public void setStrongRef(V value) {
		strongCache.add(value);
	}
	
	public void removeStrongRef(V value) {
		strongCache.remove(value);
	}
	
	public void clearStrongRefs() {
		strongCache.clear();
	}
}
