package me.devtec.theapi.utils.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Maker extends ArrayList<Object> {
	private static final long serialVersionUID = 1L;

	public Maker() {
	}

	@SuppressWarnings("unchecked")
	public Maker(String json) {
		addAll((Collection<Object>) Json.reader().simpleRead(json));
	}

	public Maker(Collection<Object> obj) {
		addAll(obj);
	}

	public void addSerilized(String json) {
		add(Json.reader().simpleRead(json));
	}

	public void removeSerilized(String json) {
		remove(Json.reader().simpleRead(json));
	}

	public MakerObject create() {
		return new MakerObject();
	}

	public String toString() {
		return Json.writer().simpleWrite(this);
	}

	public static class MakerObject extends HashMap<Object, Object> {

		private static final long serialVersionUID = 1L;

		public MakerObject add(Object key, Object item) {
			super.put(key, item);
			return this;
		}

		public MakerObject put(Object key, Object item) {
			return add(key, item);
		}

		public MakerObject remove(Object key) {
			super.remove(key);
			return this;
		}

		public String toString() {
			return Json.writer().simpleWrite(this);
		}
	}
}
