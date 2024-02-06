package sengine;

import java.lang.reflect.Array;
import java.util.ArrayList;

public abstract class ListCache<T> extends Cache<T> {

	final ArrayList<T> active = new ArrayList<T>();
	
	public ListCache(Class<T> clazz, Object id) {
		super(clazz, id);
	}

	public ListCache(Class<T> clazz) {
		super(clazz);
	}

	@Override
	public synchronized void remember(T object) {
		active.add(object);
	}

	@Override
	public synchronized void forget(T object) {
		active.remove(object);
	}

	@Override
	public synchronized T get() {
		int size = active.size();
		return size > 0 ? active.remove(size - 1) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized T[] list() {
		int size = active.size();
		T[] array = (T[])Array.newInstance(clazz, size);
		return active.toArray(array);
	}

	@Override
	public synchronized void clear() {
		active.clear();
	}
}
