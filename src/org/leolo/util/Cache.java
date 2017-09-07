package org.leolo.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Cache <K,V> implements java.util.Map<K,V>{

	private HashMap<K,CacheItem<V>> data;
	
	public Cache(){
		data = new HashMap<>();
	}
	
	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public V get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V put(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return data.remove(key).item;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for(K key:m.keySet()){
			put(key, m.get(key));
		}
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public Set<K> keySet() {
		return data.keySet();
	}

	@Override
	public Collection<V> values() {
		Collection<V> values = new Vector<>();
		for(K key:data.keySet()){
			values.add(data.get(key).item);
		}
		return values;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<java.util.Map.Entry<K, V>> set = new HashSet<>();
		for(K key:data.keySet()){
			set.add(new Entry<K,V>(key, data.get(key).item));
		}
		return set;
	}
	
	class CacheItem<E>{
		E item;
		long lastAccess = Long.MIN_VALUE;
	}
	
	class Entry <K,V> implements Map.Entry<K, V>{
		
		private K key;
		private V value;
		
		private Entry(K key, V value){
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
}
