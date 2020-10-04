package me.DevTec.TheAPI.Utils.DataKeeper.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import me.DevTec.TheAPI.Utils.DataKeeper.Data.DataHolder;
import me.DevTec.TheAPI.Utils.DataKeeper.Maps.MultiMap;
import me.DevTec.TheAPI.Utils.Json.jsonmaker.Maker;

public class JsonLoader implements DataLoader {
	private boolean l;
	private MultiMap<String, String, DataHolder> data = new MultiMap<>();
	
	@Override
	public MultiMap<String, String, DataHolder> get() {
		return data;
	}
	
	@Override
	public void load(String input) {
		data.clear();
		try {
			ArrayList<?> s = (ArrayList<?>)Maker.objectFromJson(input);
		for(int i = 0; i < s.size(); ++i) {
			HashMap<?,?> o = (HashMap<?,?>) s.get(i);
		for(Entry<?, ?> key : o.entrySet()) {
			Object read = key.getValue();
			data.put(key.getKey().toString().split("\\.")[0], key.getKey().toString(), new DataHolder(read instanceof String?Maker.objectFromJson(read.toString()):read));
		}}
		l=true;
		}catch(Exception er) {
			l=false;
		}
	}

	@Override
	public List<String> getHeader() {
		//NOT SUPPORTED
		return null;
	}

	@Override
	public List<String> getFooter() {
		//NOT SUPPORTED
		return null;
	}

	@Override
	public boolean loaded() {
		return l;
	}
}
