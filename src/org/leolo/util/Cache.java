package org.leolo.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

public class Cache<K, V> implements java.util.Map<K, V> {

	class CacheItem<E> {
		final long CREATED_TIME;
		E item;
		long lastAccess = Long.MIN_VALUE;
		public CacheItem(E value) {
			CREATED_TIME = System.currentTimeMillis();
			item = value;
			lastAccess = CREATED_TIME;
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
			return new Enumerator();
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
	
	/**
	 * Set the policy for removing the entry in the cache
	 * @author leolo@leolo.org
	 *
	 */
	public enum RemovalPolicy{
		/**
		 * Standard last recently used
		 */
		LEAST_RECENTLY_USED,
		/**
		 * Least recently used, but entry older than specified age will be always be
		 *  removed
		 */
		LEAST_RECENTLY_USER_WITH_MAXIMUM_AGE,
		/**
		 * Least recently used, but entry last used within specified period will
		 * always be retained
		 */
		LEAST_RECENTLY_USER_WITH_MINIMUM_TIME,
		/**
		 * Least recently used, but entry last used within specified period will
		 * always be retained, and entry older than specified age will always be removed
		 */
		LEAST_RECENTLY_USER_WITH_MINIMUM_TIME_MAXIMUM_AGE,
		/**
		 * The oldest entry will be removed
		 */
		OLDEST;
		
	}
	
	/**
	 * Indicate can an entry be removed.
	 * @author leolo
	 *
	 */
	protected enum RemovalStatus{
		MAY_REMOVE,
		MUST_KEEP,
		MUST_REMOVE;
	}
	
	private class LRUComparator implements Comparator<K>{

		@Override
		public int compare(K o1, K o2) {
			return Long.compare( data.get(o1).lastAccess, data.get(o2).lastAccess);
		}

		
		
	}
	
	private class AgeComparator implements Comparator<K>{

		@Override
		public int compare(K o1, K o2) {
			return Long.compare( data.get(o1).CREATED_TIME, data.get(o2).CREATED_TIME);
		}

		
		
	}
	
	private boolean autoPurge = false;
	
	private Hashtable<K, CacheItem<V>> data;
	
	private long maxAge = 86_400_000;
	
	private int maxSize = 100;
	
	private long minTime = 60_000;

	private transient int modCount = 0;

	private RemovalPolicy policy = RemovalPolicy.LEAST_RECENTLY_USED;
	
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
			if(ci==null)
				return null;
			ci.lastAccess = System.currentTimeMillis();
		}
		return ci.item;
	}

	public long getMaxAge() {
		return maxAge;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public long getMinTime() {
		return minTime;
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return data.keySet();
	}

	public void purge(){
		ArrayList<K> mustRemove = new ArrayList<>();
		ArrayList<K> mayRemove = new ArrayList<>();
		//Stage 1: classifsy they cache item status
		for(K key:data.keySet()){
			CacheItem<V> ci = data.get(key);
			if(policy == RemovalPolicy.LEAST_RECENTLY_USED ||
					policy == RemovalPolicy.OLDEST){
				mayRemove.add(key);
			}else if(policy == RemovalPolicy.LEAST_RECENTLY_USER_WITH_MAXIMUM_AGE){
				if(System.currentTimeMillis() - ci.CREATED_TIME > maxAge){
					mustRemove.add(key);
				}else{
					mayRemove.add(key);
				}
			}else if(policy == RemovalPolicy.LEAST_RECENTLY_USER_WITH_MINIMUM_TIME_MAXIMUM_AGE){
				if(System.currentTimeMillis() - ci.CREATED_TIME > maxAge){
					mustRemove.add(key);
				}else if(System.currentTimeMillis() - ci.lastAccess > minTime){
					mayRemove.add(key);
				}
			}else if(policy == RemovalPolicy.LEAST_RECENTLY_USER_WITH_MINIMUM_TIME){
				if(System.currentTimeMillis() - ci.lastAccess > minTime){
					mayRemove.add(key);
				}
			}
		}
		//Stage 2: Remove all item from must remove
		for(K ci:mustRemove){
			remove(ci);
		}
		if(size()>maxSize){
			if(policy == RemovalPolicy.OLDEST){
				mayRemove.sort(new AgeComparator());
			}else{
				mayRemove.sort(new LRUComparator());
			}
			while(size()>maxSize && mayRemove.size()>0){
				remove(mayRemove.get(0));
				if(mayRemove.size()==1){
					return;
				}else{
					mayRemove.remove(0);
				}
			}
		}
	}

	private V __put(K key, V value) {
		++modCount;
		V tmp;
		synchronized (this) {
			tmp = get(key);
			data.put(key, new CacheItem<V>(value));
		}
		return tmp;
	}
	
	@Override
	public V put(K key, V value) {
		V val = __put(key, value);
		if(autoPurge) purge();
		return val;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key : m.keySet()) {
			__put(key, m.get(key));
		}
		if(autoPurge) purge();
	}

	@Override
	public V remove(Object key) {
		++modCount;
		synchronized (this) {
			CacheItem<V> ci = data.remove(key);
			return ci==null?null:ci.item;
		}
	}

	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}

	@Override
	public int size() {
		return data.size();
	}
	
	@Override
	public Collection<V> values() {
		return new DataCollection();
	}

	public RemovalPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(RemovalPolicy policy) {
		this.policy = policy;
	}

	public boolean isAutoPurge() {
		return autoPurge;
	}

	public void setAutoPurge(boolean autoPurge) {
		this.autoPurge = autoPurge;
	}
}
