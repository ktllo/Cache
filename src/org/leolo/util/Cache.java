package org.leolo.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class Cache<K, V> implements java.util.Map<K, V> {

	class CacheItem<E> {
		E item;

		long lastAccess = Long.MIN_VALUE;
		public CacheItem(E value) {
			item = value;
			lastAccess = System.currentTimeMillis();
		}
	}

	class DataCollection extends AbstractCollection<V> {

		class Enumerator implements Iterator<V> {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index != data.length;
			}

			@SuppressWarnings("unchecked")
			@Override
			public V next() {
				if (expectedModCount == modCount && hasNext()) {
					return (V) data[index++];
				} else if (!hasNext()) {
					throw new NoSuchElementException();
				} else {
					throw new ConcurrentModificationException();
				}
			}

		}
		Object[] data;

		private int expectedModCount;

		DataCollection() {
			synchronized (Cache.this) {
				data = new Object[Cache.this.data.size()];
				int i = 0;
				for (CacheItem<V> ci : Cache.this.data.values()) {
					data[i++] = ci.item;
				}
				expectedModCount = Cache.this.modCount;
			}
		}

		@Override
		public Iterator<V> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return data.length;
		}
	}

	@SuppressWarnings("hiding")
	class Entry<K, V> implements Map.Entry<K, V> {

		private K key;
		private V value;

		private Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

	}

	private Hashtable<K, CacheItem<V>> data;

	private transient int modCount = 0;

	public Cache() {
		data = new Hashtable<>();
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		synchronized (this) {
			for (CacheItem<V> ci : data.values()) {
				if (value == null && ci.item == null)
					return true;
				if (value.equals(ci.item))
					return true;
			}
		}
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<java.util.Map.Entry<K, V>> set = new HashSet<>();
		for (K key : data.keySet()) {
			set.add(new Entry<K, V>(key, data.get(key).item));
		}
		return set;
	}

	@Override
	public V get(Object key) {
		++modCount;
		CacheItem<V> ci;
		synchronized (this) {
			ci = data.get(key);
			ci.lastAccess = System.currentTimeMillis();
		}
		return ci.item;
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return data.keySet();
	}

	@Override
	public V put(K key, V value) {
		++modCount;
		V tmp;
		synchronized (this) {
			tmp = get(key);
			data.put(key, new CacheItem<V>(value));
		}
		return tmp;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key : m.keySet()) {
			put(key, m.get(key));
		}
	}

	@Override
	public V remove(Object key) {
		++modCount;
		synchronized (this) {
			return data.remove(key).item;
		}
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public Collection<V> values() {
		return new DataCollection();
	}
}
