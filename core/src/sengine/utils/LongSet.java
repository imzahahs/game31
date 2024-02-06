/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package sengine.utils;


import java.util.NoSuchElementException;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.StringBuilder;

/** An unordered map that uses long keys. This implementation is a cuckoo hash map using 3 hashes, random walking, and a small
 * stash for problematic keys. Null values are allowed. No allocation is done except when growing the table size. <br>
 * <br>
 * This map performs very fast get, containsKey, and remove (typically O(1), worst case O(log(n))). Put may be a bit slower,
 * depending on hash collisions. Load factors greater than 0.91 greatly increase the chances the map will have to rehash to the
 * next higher POT size.
 * @author Nathan Sweet */
public class LongSet {
//	private static final int PRIME1 = 0xbe1f14b1;
	private static final int PRIME2 = 0xb4b82e39;
	private static final int PRIME3 = 0xced1c241;
	private static final int EMPTY = 0;

	public int size;

	long[] keyTable;
	int capacity, stashSize;
	boolean hasZeroValue;

	private float loadFactor;
	private int hashShift, mask, threshold;
	private int stashCapacity;
	private int pushIterations;

	private LongSetIterator iterator1, iterator2;

	/** Creates a new map with an initial capacity of 32 and a load factor of 0.8. This map will hold 25 items before growing the
	 * backing table. */
	public LongSet () {
		this(32, 0.8f);
	}

	/** Creates a new map with a load factor of 0.8. This map will hold initialCapacity * 0.8 items before growing the backing
	 * table. */
	public LongSet (int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	/** Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity * loadFactor items
	 * before growing the backing table. */
	public LongSet (int initialCapacity, float loadFactor) {
		if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
		if (initialCapacity > 1 << 30) throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
		capacity = MathUtils.nextPowerOfTwo(initialCapacity);

		if (loadFactor <= 0) throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
		this.loadFactor = loadFactor;

		threshold = (int)(capacity * loadFactor);
		mask = capacity - 1;
		hashShift = 63 - Long.numberOfTrailingZeros(capacity);
		stashCapacity = Math.max(3, (int)Math.ceil(Math.log(capacity)) * 2);
		pushIterations = Math.max(Math.min(capacity, 8), (int)Math.sqrt(capacity) / 8);

		keyTable = new long[capacity + stashCapacity];
	}

	/** Creates a new map identical to the specified map. */
	public LongSet (LongSet map) {
		this(map.capacity, map.loadFactor);
		stashSize = map.stashSize;
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		size = map.size;
		hasZeroValue = map.hasZeroValue;
	}

	public boolean add (long key) {
		if (key == 0) {
			if (hasZeroValue) return false;
			hasZeroValue = true;
			size++;
			return true;
		}

		long[] keyTable = this.keyTable;

		// Check for existing keys.
		int index1 = (int)(key & mask);
		long key1 = keyTable[index1];
		if (key1 == key) return false;

		int index2 = hash2(key);
		long key2 = keyTable[index2];
		if (key2 == key) return false;

		int index3 = hash3(key);
		long key3 = keyTable[index3];
		if (key3 == key) return false;

		// Update key in the stash.
		for (int i = capacity, n = i + stashSize; i < n; i++)
			if (keyTable[i] == key) return false;

		// Check for empty buckets.
		if (key1 == EMPTY) {
			keyTable[index1] = key;
			if (size++ >= threshold) resize(capacity << 1);
			return true;
		}

		if (key2 == EMPTY) {
			keyTable[index2] = key;
			if (size++ >= threshold) resize(capacity << 1);
			return true;
		}

		if (key3 == EMPTY) {
			keyTable[index3] = key;
			if (size++ >= threshold) resize(capacity << 1);
			return true;
		}

		push(key, index1, key1, index2, key2, index3, key3);
		return true;
	}


	public void addAll (LongArray array) {
		addAll(array, 0, array.size);
	}

	public void addAll (LongArray array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll (long... array) {
		addAll(array, 0, array.length);
	}

	public void addAll (long[] array, int offset, int length) {
		ensureCapacity(length);
		for (int i = offset, n = i + length; i < n; i++)
			add(array[i]);
	}

	public void addAll (LongSet set) {
		ensureCapacity(set.size);
		LongSetIterator iterator = set.iterator();
		while (iterator.hasNext)
			add(iterator.next());
	}

	/** Skips checks for existing keys. */
	private void addResize (long key) {
		if (key == 0) {
			hasZeroValue = true;
			return;
		}

		// Check for empty buckets.
		int index1 = (int)(key & mask);
		long key1 = keyTable[index1];
		if (key1 == EMPTY) {
			keyTable[index1] = key;
			if (size++ >= threshold) resize(capacity << 1);
			return;
		}

		int index2 = hash2(key);
		long key2 = keyTable[index2];
		if (key2 == EMPTY) {
			keyTable[index2] = key;
			if (size++ >= threshold) resize(capacity << 1);
			return;
		}

		int index3 = hash3(key);
		long key3 = keyTable[index3];
		if (key3 == EMPTY) {
			keyTable[index3] = key;
			if (size++ >= threshold) resize(capacity << 1);
			return;
		}

		push(key, index1, key1, index2, key2, index3, key3);
	}

	private void push (long insertKey, int index1, long key1, int index2, long key2, int index3, long key3) {
		long[] keyTable = this.keyTable;
		int mask = this.mask;

		// Push keys until an empty bucket is found.
		long evictedKey;
		int i = 0, pushIterations = this.pushIterations;
		do {
			// Replace the key and value for one of the hashes.
			switch (MathUtils.random(2)) {
			case 0:
				evictedKey = key1;
				keyTable[index1] = insertKey;
				break;
			case 1:
				evictedKey = key2;
				keyTable[index2] = insertKey;
				break;
			default:
				evictedKey = key3;
				keyTable[index3] = insertKey;
				break;
			}

			// If the evicted key hashes to an empty bucket, put it there and stop.
			index1 = (int)(evictedKey & mask);
			key1 = keyTable[index1];
			if (key1 == EMPTY) {
				keyTable[index1] = evictedKey;
				if (size++ >= threshold) resize(capacity << 1);
				return;
			}

			index2 = hash2(evictedKey);
			key2 = keyTable[index2];
			if (key2 == EMPTY) {
				keyTable[index2] = evictedKey;
				if (size++ >= threshold) resize(capacity << 1);
				return;
			}

			index3 = hash3(evictedKey);
			key3 = keyTable[index3];
			if (key3 == EMPTY) {
				keyTable[index3] = evictedKey;
				if (size++ >= threshold) resize(capacity << 1);
				return;
			}

			if (++i == pushIterations) break;

			insertKey = evictedKey;
		} while (true);

		addStash(evictedKey);
	}

	private void addStash (long key) {
		if (stashSize == stashCapacity) {
			// Too many pushes occurred and the stash is full, increase the table size.
			resize(capacity << 1);
			add(key);
			return;
		}
		// Store key in the stash.
		int index = capacity + stashSize;
		keyTable[index] = key;
		stashSize++;
		size++;
	}

	public boolean remove (long key) {
		if (key == 0) {
			if (!hasZeroValue) return false;
			hasZeroValue = false;
			size--;
			return true;
		}

		int index = (int)(key & mask);
		if (keyTable[index] == key) {
			keyTable[index] = EMPTY;
			size--;
			return true;
		}

		index = hash2(key);
		if (keyTable[index] == key) {
			keyTable[index] = EMPTY;
			size--;
			return true;
		}

		index = hash3(key);
		if (keyTable[index] == key) {
			keyTable[index] = EMPTY;
			size--;
			return true;
		}

		return removeStash(key);
	}

	boolean removeStash (long key) {
		long[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (keyTable[i] == key) {
				removeStashIndex(i);
				size--;
				return true;
			}
		}
		return false;
	}

	void removeStashIndex (int index) {
		// If the removed location was not last, move the last tuple to the removed location.
		stashSize--;
		int lastIndex = capacity + stashSize;
		if (index < lastIndex) keyTable[index] = keyTable[lastIndex];
	}

	/** Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is already less, nothing is
	 * done. If the map contains more items than the specified capacity, the next highest power of two capacity is used instead. */
	public void shrink (int maximumCapacity) {
		if (maximumCapacity < 0) throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
		if (size > maximumCapacity) maximumCapacity = size;
		if (capacity <= maximumCapacity) return;
		maximumCapacity = MathUtils.nextPowerOfTwo(maximumCapacity);
		resize(maximumCapacity);
	}

	/** Clears the map and reduces the size of the backing arrays to be the specified capacity if they are larger. */
	public void clear (int maximumCapacity) {
		if (capacity <= maximumCapacity) {
			clear();
			return;
		}
		hasZeroValue = false;
		size = 0;
		resize(maximumCapacity);
	}

	public void clear () {
		long[] keyTable = this.keyTable;
		for (int i = capacity + stashSize; i-- > 0;)
			keyTable[i] = EMPTY;
		size = 0;
		stashSize = 0;
		hasZeroValue = false;
	}

	public boolean contains (long key) {
		if (key == 0) return hasZeroValue;
		int index = (int)(key & mask);
		if (keyTable[index] != key) {
			index = hash2(key);
			if (keyTable[index] != key) {
				index = hash3(key);
				if (keyTable[index] != key) return containsKeyStash(key);
			}
		}
		return true;
	}

	private boolean containsKeyStash (long key) {
		long[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++)
			if (keyTable[i] == key) return true;
		return false;
	}

	public long first () {
		if (hasZeroValue) return 0;
		long[] keyTable = this.keyTable;
		for (int i = 0, n = capacity + stashSize; i < n; i++)
			if (keyTable[i] != EMPTY) return keyTable[i];
		throw new IllegalStateException("IntSet is empty.");
	}

	/** Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes. */
	public void ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= threshold) resize(MathUtils.nextPowerOfTwo((int)(sizeNeeded / loadFactor)));
	}

	private void resize (int newSize) {
		int oldEndIndex = capacity + stashSize;

		capacity = newSize;
		threshold = (int)(newSize * loadFactor);
		mask = newSize - 1;
		hashShift = 63 - Long.numberOfTrailingZeros(newSize);
		stashCapacity = Math.max(3, (int)Math.ceil(Math.log(newSize)) * 2);
		pushIterations = Math.max(Math.min(newSize, 8), (int)Math.sqrt(newSize) / 8);

		long[] oldKeyTable = keyTable;

		keyTable = new long[newSize + stashCapacity];

		int oldSize = size;
		size = hasZeroValue ? 1 : 0;
		stashSize = 0;
		if (oldSize > 0) {
			for (int i = 0; i < oldEndIndex; i++) {
				long key = oldKeyTable[i];
				if (key != EMPTY) addResize(key);
			}
		}
	}

	private int hash2 (long h) {
		h *= PRIME2;
		return (int)((h ^ h >>> hashShift) & mask);
	}

	private int hash3 (long h) {
		h *= PRIME3;
		return (int)((h ^ h >>> hashShift) & mask);
	}

	public String toString () {
		if (size == 0) return "[]";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		long[] keyTable = this.keyTable;
		int i = keyTable.length;
		if (hasZeroValue)
			buffer.append("0");
		else {
			while (i-- > 0) {
				long key = keyTable[i];
				if (key == EMPTY) continue;
				buffer.append(key);
				break;
			}
		}
		while (i-- > 0) {
			long key = keyTable[i];
			if (key == EMPTY) continue;
			buffer.append(", ");
			buffer.append(key);
		}
		buffer.append(']');
		return buffer.toString();
	}
	
	/** Returns an iterator for the keys in the set. Remove is supported. Note that the same iterator instance is returned each time
	 * this method is called. Use the {@link IntSetIterator} constructor for nested or multithreaded iteration. */
	public LongSetIterator iterator () {
		if (iterator1 == null) {
			iterator1 = new LongSetIterator(this);
			iterator2 = new LongSetIterator(this);
		}
		if (!iterator1.valid) {
			iterator1.reset();
			iterator1.valid = true;
			iterator2.valid = false;
			return iterator1;
		}
		iterator2.reset();
		iterator2.valid = true;
		iterator1.valid = false;
		return iterator2;
	}

	static public LongSet with (long... array) {
		LongSet set = new LongSet();
		set.addAll(array);
		return set;
	}

	static public class Entry<V> {
		public long key;
		public V value;

		public String toString () {
			return key + "=" + value;
		}
	}

	static public class LongSetIterator {
		static final int INDEX_ILLEGAL = -2;
		static final int INDEX_ZERO = -1;

		public boolean hasNext;

		final LongSet set;
		int nextIndex, currentIndex;
		boolean valid = true;

		public LongSetIterator (LongSet set) {
			this.set = set;
			reset();
		}

		public void reset () {
			currentIndex = INDEX_ILLEGAL;
			nextIndex = INDEX_ZERO;
			if (set.hasZeroValue)
				hasNext = true;
			else
				findNextIndex();
		}

		void findNextIndex () {
			hasNext = false;
			long[] keyTable = set.keyTable;
			for (int n = set.capacity + set.stashSize; ++nextIndex < n;) {
				if (keyTable[nextIndex] != EMPTY) {
					hasNext = true;
					break;
				}
			}
		}

		public void remove () {
			if (currentIndex == INDEX_ZERO && set.hasZeroValue) {
				set.hasZeroValue = false;
			} else if (currentIndex < 0) {
				throw new IllegalStateException("next must be called before remove.");
			} else if (currentIndex >= set.capacity) {
				set.removeStashIndex(currentIndex);
				nextIndex = currentIndex - 1;
				findNextIndex();
			} else {
				set.keyTable[currentIndex] = EMPTY;
			}
			currentIndex = INDEX_ILLEGAL;
			set.size--;
		}

		public long next () {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			long key = nextIndex == INDEX_ZERO ? 0 : set.keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		/** Returns a new array containing the remaining keys. */
		public LongArray toArray () {
			LongArray array = new LongArray(true, set.size);
			while (hasNext)
				array.add(next());
			return array;
		}
	}
}
