package me.devtec.theapi.utils.datakeeper;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.devtec.theapi.utils.StringUtils;
import me.devtec.theapi.utils.datakeeper.loader.DataLoader;
import me.devtec.theapi.utils.datakeeper.loader.EmptyLoader;
import me.devtec.theapi.utils.datakeeper.loader.YamlLoader;
import me.devtec.theapi.utils.json.Json;
import me.devtec.theapi.utils.theapiutils.Validator;

public class Data implements me.devtec.theapi.utils.datakeeper.abstracts.Data {
	protected DataLoader loader;
	protected List<String> keys;
	protected File file;
	
	public Data() {
		loader = new EmptyLoader();
		keys = new LinkedList<>();
	}
	
	public Data(String filePath) {
		this(new File(filePath.startsWith("/") ? filePath.substring(1) : filePath), true);
	}
	
	public Data(String filePath, boolean load) {
		this(new File(filePath.startsWith("/") ? filePath.substring(1) : filePath), load);
	}
	
	public Data(File file) {
		this(file, true);
	}
	
	public Data(File file, boolean load) {
		this.file = file;
		keys = new LinkedList<>();
		if (load)
			reload(file);
	}
	
	// CLONE
	public Data(Data data) {
		file = data.file;
		keys = data.keys;
		loader=data.loader;
	}

	public synchronized boolean exists(String path) {
		for (String k : loader.getKeys())
			if (k.startsWith(path))
				return true;
		return false;
	}

	public synchronized boolean existsKey(String path) {
		return loader.get().containsKey(path);
	}

	public synchronized Data setFile(File file) {
		this.file = file;
		return this;
	}

	public synchronized Object[] getOrCreateData(String key) {
		Object[] h = loader.get().get(key);
		if (h == null) {
			String ss = splitFirst(key);
			if (!keys.contains(ss))
				keys.add(ss);
			loader.get().put(key, h = new Object[2]);
		}
		return h;
	}

	private static String splitFirst(String text) {
        int next = text.indexOf('.', 0);
        return next!=-1?text.substring(0, next):text;
	}
	
	public synchronized boolean setIfAbsent(String key, Object value) {
		if (key == null || value==null)
			return false;
		if(!existsKey(key)) {
			requireSave=true;
			Object[] data = getOrCreateData(key);
			data[0]=value;
			return true;
		}
		return false;
	}

	public synchronized boolean setIfAbsent(String key, Object value, List<String> comments) {
		if (key == null || value==null)
			return false;
		if(!existsKey(key)) {
			requireSave=true;
			Object[] data = getOrCreateData(key);
			data[0]=value;
			data[1]=comments;
			return true;
		}
		Object[] data = getOrCreateData(key);
		if(data[1]==null && comments!=null && !comments.isEmpty() || data[1]!=null && ((List<?>) data[1]).isEmpty() && comments!=null && !comments.isEmpty()) {
			data[1]=comments;
			requireSave=true;
			return true;
		}
		return false;
	}

	public Data set(String key, Object value) {
		if (key == null)
			return this;
		requireSave=true;
		if (value == null) {
			String sf = splitFirst(key);
			keys.remove(sf);
			loader.remove(key);
			return this;
		}
		Object[] o = getOrCreateData(key);
		o[0]=value;
		switch(o.length) {
		case 4:
			o[2]=null;
			o[3]=null;
			break;
		case 3:
			o[2]=null;
			break;
		}
		return this;
	}

	public synchronized Data remove(String key) {
		if (key == null)
			return this;
		requireSave=true;
		String sf = splitFirst(key);
		keys.remove(sf);
		loader.remove(key);
		for(String d : new ArrayList<>(loader.get().keySet()))
			if (d.startsWith(key) && d.substring(key.length()).startsWith("."))
				loader.get().remove(d);
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized List<String> getComments(String key) {
		if (key == null)
			return null;
		requireSave=true;
		Object obj = getOrCreateData(key)[1];
		return obj==null?null:(List<String>)obj;
	}

	public synchronized Data setComments(String key, List<String> value) {
		if (key == null)
			return this;
		requireSave=true;
		getOrCreateData(key)[1]=value;
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized Data addComments(String key, List<String> value) {
		if (value == null || key == null)
			return this;
		requireSave=true;
		Object[] g = getOrCreateData(key);
		if(g[1]==null)g[1]=value;
		else
		((List<String>) g[1]).addAll(value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized Data addComment(String key, String value) {
		if (value == null || key == null)
			return this;
		requireSave=true;
		Object[] g = getOrCreateData(key);
		if(g[0]==null)g[1]=new ArrayList<>();
		((List<String>) g[1]).add(value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized Data removeComments(String key, List<String> value) {
		if (value == null || key == null)
			return this;
		requireSave=true;
		Object[] g = getOrCreateData(key);
		if(g[0]!=null)
			((List<String>) g[1]).removeAll(value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized Data removeComment(String key, String value) {
		if (value == null || key == null)
			return this;
		requireSave=true;
		Object[] g = getOrCreateData(key);
		if(g[0]!=null)
			((List<String>) g[1]).remove(value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized Data removeComment(String key, int line) {
		if (line < 0 || key == null)
			return this;
		requireSave=true;
		Object[] h = getOrCreateData(key);
		if (h[1] != null)
			((List<String>) h[1]).remove(line);
		return this;
	}

	public File getFile() {
		return file;
	}

	public synchronized Data setHeader(Collection<String> lines) {
		requireSave=true;
		loader.getHeader().clear();
		loader.getHeader().addAll(lines);
		return this;
	}

	public synchronized Data setFooter(Collection<String> lines) {
		requireSave=true;
		loader.getFooter().clear();
		loader.getFooter().addAll(lines);
		return this;
	}

	public synchronized Collection<String> getHeader() {
		return loader.getHeader();
	}

	public synchronized Collection<String> getFooter() {
		return loader.getFooter();
	}

	public synchronized Data reload(String input) {
		requireSave=true;
		keys.clear();
		loader = DataLoader.findLoaderFor(input); // get & load
		for (String k : loader.getKeys()) {
			String g = splitFirst(k);
			if (!keys.contains(g))
				keys.add(g);
		}
		return this;
	}

	public synchronized Data reload(File f) {
		if (!f.exists()) {
			requireSave=true;
			loader=new YamlLoader();
			keys.clear();
			return this;
		}
		requireSave=true;
		keys.clear();
		loader = DataLoader.findLoaderFor(f); // get & load
		for (String k : loader.getKeys()) {
			String g = splitFirst(k);
			if (!keys.contains(g))
				keys.add(g);
		}
		return this;
	}

	public synchronized Object get(String key) {
		try {
			return loader.get().get(key)[0];
		} catch (Exception e) {
			return null;
		}
	}

	public synchronized <E> E getAs(String key, Class<? extends E> clazz) {
		try {
		if(clazz==String.class||clazz==CharSequence.class)return clazz.cast(getString(key));
		} catch (Exception e) {
		}
		try {
			return clazz.cast(loader.get().get(key)[0]);
		} catch (Exception e) {
		}
		return null;
	}

	public synchronized String getString(String key) {
		Object[] a = loader.get().get(key);
		if(a==null)return null;
		if(a.length>=3) {
			Object s = a[2];
			if(s!=null)return s+"";
		}
		return a[0] instanceof String ? (String)a[0] : (a[0]==null?null:a[0]+"");
	}

	public synchronized int getInt(String key) {
		try {
			return ((Number)get(key)).intValue();
		} catch (Exception notNumber) {
			return StringUtils.getInt(getString(key));
		}
	}

	public synchronized double getDouble(String key) {
		try {
			return ((Number)get(key)).doubleValue();
		} catch (Exception notNumber) {
			return StringUtils.getDouble(getString(key));
		}
	}

	public synchronized long getLong(String key) {
		try {
			return ((Number)get(key)).longValue();
		} catch (Exception notNumber) {
			return StringUtils.getLong(getString(key));
		}
	}

	public synchronized float getFloat(String key) {
		try {
			return ((Number)get(key)).floatValue();
		} catch (Exception notNumber) {
			return StringUtils.getFloat(getString(key));
		}
	}

	public synchronized byte getByte(String key) {
		try {
			return ((Number)get(key)).byteValue();
		} catch (Exception notNumber) {
			return StringUtils.getByte(getString(key));
		}
	}

	public synchronized boolean getBoolean(String key) {
		try {
			return (boolean)get(key);
		} catch (Exception notNumber) {
			return StringUtils.getBoolean(getString(key));
		}
	}

	public synchronized short getShort(String key) {
		try {
			return ((Number)get(key)).shortValue();
		} catch (Exception notNumber) {
			return StringUtils.getShort(getString(key));
		}
	}
	
	public synchronized Collection<Object> getList(String key) {
		Object g = get(key);
		return g != null && g instanceof Collection ? new ArrayList<>((Collection<?>) g) : new ArrayList<>();
	}

	public synchronized <E> List<E> getListAs(String key, Class<? extends E> clazz) {
		List<E> list = new ArrayList<>();
		for (Object o : getList(key))
			try {
				list.add(o == null ? null : clazz.cast(o));
			} catch (Exception er) {
			}
		return list;
	}

	public synchronized List<String> getStringList(String key) {
		Collection<Object> items = getList(key);
		List<String> list=new ArrayList<>(items.size());
		for (Object o : items)
			if (o != null)
				list.add("" + o);
			else
				list.add(null);
		return list;
	}

	public synchronized List<Boolean> getBooleanList(String key) {
		Collection<Object> items = getList(key);
		List<Boolean> list=new ArrayList<>(items.size());
		for (Object o : items)
			list.add(o != null && (o instanceof Boolean ? (Boolean) o : StringUtils.getBoolean(o.toString())));
		return list;
	}

	public synchronized List<Integer> getIntegerList(String key) {
		Collection<Object> items = getList(key);
		List<Integer> list=new ArrayList<>(items.size());
		for (Object o : items)
			list.add(o == null ? 0 : o instanceof Number ? ((Number)o).intValue():StringUtils.getInt(o.toString()));
		return list;
	}

	public synchronized List<Double> getDoubleList(String key) {
		Collection<Object> items = getList(key);
		List<Double> list=new ArrayList<>(items.size());
		for (Object o : items)
			list.add(o == null ? 0.0 : o instanceof Number ? ((Number)o).doubleValue():StringUtils.getDouble(o.toString()));
		return list;
	}

	public synchronized List<Short> getShortList(String key) {
		Collection<Object> items = getList(key);
		List<Short> list=new ArrayList<>(items.size());
		for (Object o : items)
			list.add(o == null ? 0 : o instanceof Number ? ((Number)o).shortValue():StringUtils.getShort(o.toString()));
		return list;
	}

	public synchronized List<Byte> getByteList(String key) {
		Collection<Object> items = getList(key);
		List<Byte> list=new ArrayList<>(items.size());
		for (Object o : items)
			list.add(o == null ? 0 : o instanceof Number ? ((Number)o).byteValue():StringUtils.getByte(o.toString()));
		return list;
	}

	public synchronized List<Float> getFloatList(String key) {
		Collection<Object> items = getList(key);
		List<Float> list=new ArrayList<>(items.size());
		for (Object o : items)
			list.add(o == null ? 0 : o instanceof Number ? ((Number)o).floatValue():StringUtils.getFloat(o.toString()));
		return list;
	}

	public synchronized List<Long> getLongList(String key) {
		Collection<Object> items = getList(key);
		List<Long> list=new ArrayList<>(items.size());
		for (Object o : items)
			list.add(o == null ? 0 : StringUtils.getLong(o.toString()));
		return list;
	}

	@SuppressWarnings("unchecked")
	public synchronized <K, V> List<Map<K, V>> getMapList(String key) {
		Collection<Object> items = getList(key);
		List<Map<K, V>> list=new ArrayList<>(items.size());
		for (Object o : items) {
			if(o==null)
				list.add(null);
			else {
				Object re = Json.reader().read(o.toString());
				list.add(re instanceof Map ? (Map<K, V>) re : new HashMap<>());
			}
		}
		return list;
	}

	protected boolean isSaving, requireSave;
	public synchronized Data save(DataType type) {
		if (file == null)
			return this;
		if(isSaving)return this;
		if(!requireSave)return this;
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
			} catch (Exception e) {
			}
			try {
				file.createNewFile();
			} catch (Exception e) {
			}
		}
		isSaving=true;
		requireSave=false;
		try {
			try {
				OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
				w.write(toString(type));
				w.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			isSaving=false;
		} catch (Exception e1) {
		}
		return this;
	}
	
	public synchronized void save() {
		save(DataType.YAML);
	}

	public synchronized Set<String> getKeys() {
		return new LinkedHashSet<>(keys);
	}

	public synchronized Set<String> getKeys(boolean subkeys) {
		if (subkeys)
			return loader.getKeys();
		return new LinkedHashSet<>(keys);
	}

	public synchronized Set<String> getKeys(String key) {
		return getKeys(key, false);
	}

	public synchronized boolean isKey(String key) {
		for (String k : loader.getKeys()) {
			if (k.startsWith(key)) {
				String r = k.substring(key.length());
				if (r.startsWith(".") || r.trim().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public synchronized Set<String> getKeys(String key, boolean subkeys) {
		Set<String> a = new LinkedHashSet<>();
		for (String d : loader.getKeys())
			if (d.startsWith(key)) {
				String c = d.substring(key.length());
				if (!c.startsWith("."))
					continue;
				c = c.substring(1);
				if(!subkeys)
				c =  splitFirst(c);
				if (c.trim().isEmpty())
					continue;
				if (!a.contains(c))
					a.add(c);
			}
		return a;
	}

	public synchronized String toString() {
		return toString(DataType.BYTE);
	}

	protected synchronized void addKeys(List<Map<String, String>> list, String key) {
		Object o = get(key);
		if (o != null) {
			Map<String, String> a = new HashMap<>();
			a.put(key, Json.writer().write(o));
			list.add(a);
		}
		for (String keyer : getKeys(key))
			addKeys(list, key + "." + keyer);
	}

	public synchronized String toString(DataType type) {
		switch(type) {
		case PROPERTIES: {
			int size = loader.get().size();
			StringBuilder d = new StringBuilder(size*8);
			if(loader.getHeader()!=null)
			try {
				for (String h : loader.getHeader())
					d.append(h).append(System.lineSeparator());
			} catch (Exception er) {
				Validator.send("Saving Data to YAML", er);
			}
			for (Entry<String, Object[]> key : loader.get().entrySet()) {
				if(key.getValue()==null || key.getValue()[0]==null)continue;
				d.append(key.getKey()+": "+Json.writer().write(key.getValue()[0]));
			}
			if(loader.getFooter()!=null)
			try {
				for (String h : loader.getFooter())
					d.append(h).append(System.lineSeparator());
			} catch (Exception er) {
				Validator.send("Saving Data to YAML", er);
			}
			return d.toString();
		}
		case BYTE:
			try {
				ByteArrayDataOutput bos = ByteStreams.newDataOutput(loader.get().size());
				bos.writeInt(2);
				bos.writeUTF("1");
				for (Entry<String, Object[]> key : loader.get().entrySet())
					try {
						bos.writeUTF(key.getKey());
						if(key.getValue()[0]==null) {
							bos.writeUTF("null");
							bos.writeUTF("0");
						}else {
							if(key.getValue().length >2)
								if(key.getValue()[2]!=null && key.getValue()[2] instanceof String) {
									String write = (String)key.getValue()[2];
									if(write==null) {
										bos.writeUTF("null");
										bos.writeUTF("0");
										continue;
									}
									while(write.length()>40000) {
										String wr = write.substring(0, 39999);
										bos.writeUTF("0"+wr);
										write=write.substring(39999);
									}
									bos.writeUTF("0"+write);
									bos.writeUTF("0");
									continue;
								}
							Object val = key.getValue()[0];
							String write = val instanceof String ? (String)val:Json.writer().write(val);
							if(write==null) {
								bos.writeUTF("null");
								bos.writeUTF("0");
								continue;
							}
							while(write.length()>40000) {
								String wr = write.substring(0, 39999);
								bos.writeUTF("0"+wr);
								write=write.substring(39999);
							}
							bos.writeUTF("0"+write);
							bos.writeUTF("0");
							continue;
						}
					} catch (Exception er) {
						er.printStackTrace();
					}
				return Base64.getEncoder().encodeToString(bos.toByteArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		case JSON:
			List<Map<String, String>> list = new ArrayList<>();
			for (String key : Collections.unmodifiableList(keys))
				addKeys(list, key);
			return Json.writer().simpleWrite(list);
		case YAML:
			int size = loader.get().size();
			StringBuilder d = new StringBuilder(size*16);
			if(loader.getHeader()!=null)
			try {
				for (String h : loader.getHeader())
					d.append(h).append(System.lineSeparator());
			} catch (Exception er) {
				Validator.send("Saving Data to YAML", er);
			}
			
			//BUILD KEYS & SECTIONS
			SectionBuilder builder = new SectionBuilder(keys, loader.get());
			builder.write(d);
			
			if(loader.getFooter()!=null)
			try {
				for (String h : loader.getFooter())
					d.append(h).append(System.lineSeparator());
			} catch (Exception er) {
				Validator.send("Saving Data to YAML", er);
			}
			return d.toString();
		}
		return null;
	}
	
	@Override
	public String getDataName() {
		HashMap<String, Object> s = new HashMap<>();
		if(file!=null)s.put("file", file.getPath()+"/"+file.getName());
		s.put("loader", loader.getDataName());
		return Json.writer().simpleWrite(s);
	}

	public synchronized Data clear() {
		keys.clear();
		loader.get().clear();
		return this;
	}

	public synchronized Data reset() {
		keys.clear();
		loader.reset();
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized boolean merge(Data f, boolean addHeader, boolean addFooter) {
		boolean change = false;
		try {
		if(addHeader)
			if(f.loader.getHeader()==null || f.loader.getHeader()!=null && !f.loader.getHeader().isEmpty() && (loader.getHeader().isEmpty()||!f.loader.getHeader().containsAll(loader.getHeader()))) {
				loader.getHeader().clear();
				loader.getHeader().addAll(f.loader.getHeader());
				change = true;
			}
		if(addFooter)
			if(f.loader.getFooter()==null || f.loader.getFooter()!=null && !f.loader.getFooter().isEmpty() && (loader.getFooter().isEmpty()||!f.loader.getFooter().containsAll(loader.getFooter()))) {
				loader.getFooter().clear();
				loader.getFooter().addAll(f.loader.getFooter());
				change = true;
			}
		}catch(Exception nope) {}
		try {
		for(Entry<String, Object[]> s : f.loader.get().entrySet()) {
			Object[] o = getOrCreateData(s.getKey());
			if(o[0]==null && s.getValue()[0]!=null) {
				o[0]=s.getValue()[0];
				try {
				o[2]=s.getValue()[2];
				}catch(Exception outOfBoud) {}
				change = true;
			}
			if(s.getValue()[1]!=null && !((List<String>) s.getValue()[1]).isEmpty()) {
				List<String> cc = (List<String>)o[1];
	    		if(cc==null || cc.isEmpty()) {
	    			if(getHeader()!=null && !getHeader().isEmpty() && ((List<String>)s.getValue()[1]).containsAll(getHeader())
	    					|| getFooter()!=null && !getFooter().isEmpty() && ((List<String>) s.getValue()[1]).containsAll(getFooter()))continue;
	    			o[1]=(List<String>)s.getValue()[1];
	    			change = true;
	    		}
			}
		}
		}catch(Exception err) {}
		if(change)
			requireSave=true;
		return change;
	}

	public DataLoader getDataLoader() {
		return loader;
	}
}