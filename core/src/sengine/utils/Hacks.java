package sengine.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Hacks {
	
	public static class ArrayList<E> implements List<E> {
		final java.util.ArrayList<E> l;

		public ArrayList() {
			this.l = new java.util.ArrayList<E>();
		}
		
		@Override
		public boolean add(E object) {
			return l.add(object);
		}

		@Override
		public void add(int location, E object) {
			l.add(location, object);
		}

		@Override
		public boolean addAll(Collection<? extends E> arg0) {
			return l.addAll(arg0);
		}

		@Override
		public boolean addAll(int arg0, Collection<? extends E> arg1) {
			return l.addAll(arg0, arg1);
		}

		@Override
		public void clear() {
			l.clear();
		}

		@Override
		public boolean contains(Object object) {
			return l.contains(object);
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			return l.containsAll(arg0);
		}

		@Override
		public E get(int location) {
			return l.get(location);
		}

		@Override
		public int indexOf(Object object) {
			return l.indexOf(object);
		}

		@Override
		public boolean isEmpty() {
			return l.isEmpty();
		}

		@Override
		public Iterator<E> iterator() {
			return l.iterator();
		}

		@Override
		public int lastIndexOf(Object object) {
			return l.lastIndexOf(object);
		}

		@Override
		public ListIterator<E> listIterator() {
			return l.listIterator();
		}

		@Override
		public ListIterator<E> listIterator(int location) {
			return l.listIterator(location);
		}

		@Override
		public E remove(int location) {
			return l.remove(location);
		}

		@Override
		public boolean remove(Object object) {
			return l.remove(object);
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			return l.removeAll(arg0);
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			return l.retainAll(arg0);
		}

		@Override
		public E set(int location, E object) {
			return l.set(location, object);
		}

		@Override
		public int size() {
			return l.size();
		}

		@Override
		public List<E> subList(int start, int end) {
			return l.subList(start, end);
		}

		@Override
		public Object[] toArray() {
			return l.toArray();
		}

		@Override
		public <T> T[] toArray(T[] array) {
			return l.toArray(array);
		}
	}
}
