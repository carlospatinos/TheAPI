package me.devtec.theapi.utils.datakeeper.maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.devtec.theapi.utils.datakeeper.abstracts.Data;
import me.devtec.theapi.utils.json.Json;

public class MultiMap<K, T, V> implements Data {
	private final Map<K, Map<T, V>> data = new HashMap<>();

	public MultiMap() {
	}

	public MultiMap(MultiMap<K, T, V> map) {
		putAll(map);
	}

	public int size() {
		return data.size();
	}

	public void clear() {
		data.clear();
	}

	public void remove(K key, T thread) {
		data.get(key).remove(thread);
		if (data.get(key).isEmpty())
			data.remove(key);
	}

	public void remove(K key) {
		data.remove(key);
	}

	public boolean containsKey(K key) {
		return data.containsKey(key);
	}

	public boolean containsThread(K key, T thread) {
		if (containsKey(key))
			return data.get(key).containsKey(thread);
		return false;
	}

	public boolean containsValue(K key, V value) {
		if (containsKey(key))
			return data.get(key).containsValue(value);
		return false;
	}

	public V replace(K key, T thread, V value) {
		return put(key, thread, value);
	}

	public V put(K key, T thread, V value) {
		Map<T, V> map = data.get(key);
		if (map == null)
			data.put(key, map = new HashMap<>());
		map.put(thread, value);
		return value;
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public V get(K key, T thread) {
		try {
			Map<T, V> t = data.get(key);
			return t.get(thread);
		} catch (Exception er) {
			return null;
		}
	}

	public V getOrDefault(K key, T thread, V def) {
		return containsThread(key,thread)?get(key, thread):def;
	}

	public void putAll(MultiMap<K, T, V> map) {
		map.entrySet().forEach(Entry -> put(Entry.k, Entry.t, Entry.v));
	}

	public Collection<K> keySet() {
		return data.keySet();
	}

	public Collection<T> threadSet(K key) {
		return data.containsKey(key) ? data.get(key).keySet() : new HashSet<>();
	}

	public Collection<V> values(K key, T thread) {
		return data.containsKey(key) ? data.get(key).values() : new HashSet<>();
	}

	public Collection<Entry<K, T, V>> entrySet() {
		Set<Entry<K, T, V>> entries = new HashSet<>(data.size());
		for (K key : keySet())
			for (T thread : threadSet(key))
				entries.add(new Entry<>(key, thread, get(key, thread)));
		return entries;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		for (Entry<K, T, V> e : entrySet()) {
			builder.append(builder.toString().trim().isEmpty() ? "" : ", ").append('(').append(e.toString()).append(')');
		}
		return builder.append('}').toString();
	}

	public static class Entry<K, T, V> {
		private K k;
		private T t;
		private V v;

		public Entry(K key, T thread, V value) {
			k = key;
			t = thread;
			v = value;
		}

		public K getKey() {
			return k;
		}

		public T getThread() {
			return t;
		}

		public V getValue() {
			return v;
		}

		public void setKey(K key) {
			k = key;
		}

		public void setThread(T thread) {
			t = thread;
		}

		public void setValue(V value) {
			v = value;
		}

		public String toString() {
			return k+"="+t+"="+v;
		}
	}

	@Override
	public String getDataName() {
		HashMap<String, Object> s = new HashMap<>();
		s.put("name", this.getClass().getCanonicalName());
		s.put("sorted", false);
		s.put("size", size());
		return Json.writer().simpleWrite(s);
	}
}
