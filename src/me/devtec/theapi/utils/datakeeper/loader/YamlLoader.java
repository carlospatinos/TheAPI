package me.devtec.theapi.utils.datakeeper.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.devtec.theapi.utils.json.Json;

public class YamlLoader extends DataLoader {
	private static final Pattern pattern = Pattern.compile("([ ]*)(['\\\"][^'\\\"]+['\\\"]|[^\\\"']?\\\\w+[^\\\"']?|.*?):[ ]*(.*)");
	private final Map<String, Object[]> data = new LinkedHashMap<>();
	private List<String> header = new LinkedList<>(), footer = new LinkedList<>();
	private boolean l;

	public Set<String> getKeys() {
		return data.keySet();
	}

	public void set(String key, Object[] holder) {
		if (key == null)
			return;
		if (holder == null) {
			remove(key);
			return;
		}
		data.put(key, holder);
	}

	public void remove(String key) {
		if (key == null)
			return;
		data.remove(key);
	}

	public void reset() {
		data.clear();
		header.clear();
		footer.clear();
	}
	
	@Override
	public Map<String, Object[]> get() {
		return data;
	}
	
	public void load(File file) {
		reset();
		try {
			String key = "";
			int last = 0;
			BuilderType type = null;
			List<Object> items=null;
			StringBuilder builder=null;
			boolean wasEmpty = false;
			LinkedList<String> comments = new LinkedList<>();
			
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), 8192);
			String line;
			while((line=r.readLine())!=null) {
				String trim = line.trim();
				if(trim.isEmpty()||trim.startsWith("#")) {
					comments.add(line.substring(removeSpaces(line)));
					continue;
				}

				String e = line.substring(removeSpaces(line));
				if(wasEmpty && e.startsWith("- ")) {
					if(items==null)items=new LinkedList<>();
					items.add(Json.reader().read(r(e.substring(2))));
					continue;
				}
				
				Matcher match = pattern.matcher(line);
				if(match.find()) {
					if(type!=null) {
						if(type==BuilderType.LIST) {
							data.put(key, new Object[] {new LinkedList<>(items), comments.isEmpty()?null:new LinkedList<>(comments)});
							comments.clear();
							items=null;
						}else {
							data.put(key, new Object[] {builder.toString(), comments.isEmpty()?null:new LinkedList<>(comments)});
							comments.clear();
							builder=null;
						}
						type=null;
					}else {
						if(items!=null) {
							data.put(key, new Object[] {new LinkedList<>(items), comments.isEmpty()?null:new LinkedList<>(comments)});
							comments.clear();
							items=null;
						}
					}
					
					int sub = match.group(1).length();
					String keyr = r(match.group(2));
					String value = match.group(3);
					
					if (sub <= last) {
						if (sub==0)
							key = "";
						if (sub == last) {
							String[] ff = split(key);
							String lastr = ff[ff.length - 1] + 1;
							int remove = key.length() - lastr.length();
							if (remove > 0)
								key = key.substring(0, remove);
						} else {
							for (int i = 0; i < Math.abs(last - sub) / 2 + 1; ++i) {
								String[] ff = split(key);
								String lastr = ff[ff.length - 1] + 1;
								int remove = key.length() - lastr.length();
								if (remove < 0)
									break;
								key = key.substring(0, remove);
							}
						}
					}
					
					last=sub;
					if(!key.isEmpty())key+=".";
					key += keyr;

					boolean before = wasEmpty;
					if(before)comments.clear();
					if(wasEmpty=value.trim().isEmpty())
						continue;
					
					value=r(value);
					
					if (value.equals("|")) {
						type=BuilderType.STRING;
						builder=new StringBuilder();
						continue;
					}
					if (value.equals("|-")) {
						type=BuilderType.LIST;
						items=new LinkedList<>();
						continue;
					}
					if (value.equals("[]")) {
						data.put(key, new Object[] {Collections.EMPTY_LIST, comments.isEmpty()?null:new LinkedList<>(comments)});
						comments.clear();
						continue;
					}
					data.put(key, new Object[] {Json.reader().read(value), comments.isEmpty()?null:new LinkedList<>(comments)});
					comments.clear();
				}else {
					if(type!=null) {
						if(type==BuilderType.LIST) {
							items.add(Json.reader().read(r(line.substring(removeSpaces(line)))));
						}else {
							builder.append(line.substring(removeSpaces(line)));
						}
					}
				}
			}
			if(type!=null) {
				if(type==BuilderType.LIST) {
					data.put(key, new Object[] {items, comments.isEmpty()?null:comments});
				}else {
					data.put(key, new Object[] {builder.toString(), comments.isEmpty()?null:comments});
				}
			}else if(items!=null)
				data.put(key, new Object[] {items, comments.isEmpty()?null:comments});
			l = true;
			r.close();
		} catch (Exception e) {
			e.printStackTrace();
			reset();
		}
	}
	
	@Override
	public void load(String input) {
		reset();
		try {
			String key = "";
			int last = 0;
			BuilderType type = null;
			List<Object> items=null;
			StringBuilder builder=null;
			boolean wasEmpty = false;
			LinkedList<String> comments = new LinkedList<>();
			
			for(String line : input.split(System.lineSeparator())) {
				String trim = line.trim();
				if(trim.isEmpty()||trim.startsWith("#")) {
					comments.add(line.substring(removeSpaces(line)));
					continue;
				}

				String e = line.substring(removeSpaces(line));
				if(wasEmpty && e.startsWith("- ")) {
					if(items==null)items=new LinkedList<>();
					items.add(Json.reader().read(r(e.substring(2))));
					continue;
				}
				
				Matcher match = pattern.matcher(line);
				if(match.find()) {
					if(type!=null) {
						if(type==BuilderType.LIST) {
							data.put(key, new Object[] {new LinkedList<>(items), comments.isEmpty()?null:new LinkedList<>(comments)});
							comments.clear();
							items=null;
						}else {
							data.put(key, new Object[] {builder.toString(), comments.isEmpty()?null:new LinkedList<>(comments)});
							comments.clear();
							builder=null;
						}
						type=null;
					}else {
						if(items!=null) {
							data.put(key, new Object[] {new LinkedList<>(items), comments.isEmpty()?null:new LinkedList<>(comments)});
							comments.clear();
							items=null;
						}
					}
					
					int sub = match.group(1).length();
					String keyr = r(match.group(2));
					String value = match.group(3);
					
					if (sub <= last) {
						if (sub==0)
							key = "";
						if (sub == last) {
							String[] ff = split(key);
							String lastr = ff[ff.length - 1] + 1;
							int remove = key.length() - lastr.length();
							if (remove > 0)
								key = key.substring(0, remove);
						} else {
							for (int i = 0; i < Math.abs(last - sub) / 2 + 1; ++i) {
								String[] ff = split(key);
								String lastr = ff[ff.length - 1] + 1;
								int remove = key.length() - lastr.length();
								if (remove < 0)
									break;
								key = key.substring(0, remove);
							}
						}
					}
					
					last=sub;
					if(!key.isEmpty())key+=".";
					key += keyr;

					boolean before = wasEmpty;
					if(before)comments.clear();
					if(wasEmpty=value.trim().isEmpty())
						continue;
					
					value=r(value);
					
					if (value.equals("|")) {
						type=BuilderType.STRING;
						builder=new StringBuilder();
						continue;
					}
					if (value.equals("|-")) {
						type=BuilderType.LIST;
						items=new LinkedList<>();
						continue;
					}
					if (value.equals("[]")) {
						data.put(key, new Object[] {Collections.EMPTY_LIST, comments.isEmpty()?null:new LinkedList<>(comments)});
						comments.clear();
						continue;
					}
					data.put(key, new Object[] {Json.reader().read(value), comments.isEmpty()?null:new LinkedList<>(comments)});
					comments.clear();
				}else {
					if(type!=null) {
						if(type==BuilderType.LIST) {
							items.add(Json.reader().read(r(line.substring(removeSpaces(line)))));
						}else {
							builder.append(line.substring(removeSpaces(line)));
						}
					}
				}
			}
			if(type!=null) {
				if(type==BuilderType.LIST) {
					data.put(key, new Object[] {items, comments.isEmpty()?null:comments});
				}else {
					data.put(key, new Object[] {builder.toString(), comments.isEmpty()?null:comments});
				}
			}else if(items!=null)
				data.put(key, new Object[] {items, comments.isEmpty()?null:comments});
			l = true;
		} catch (Exception er) {
			l = false;
		}
	}
	
	public enum BuilderType {
		STRING, LIST
	}

	@Override
	public Collection<String> getHeader() {
		return header;
	}

	@Override
	public Collection<String> getFooter() {
		return footer;
	}

	private static int removeSpaces(String s) {
		int i = 0;
		for(int d = 0; d < s.length(); ++d) {
			if(s.charAt(d)==' ') {
				++i;
			}else break;
		}
		return i;
	}
	
	private static String[] split(String text) {
        int off = 0, next = text.indexOf('.', off);
        if(next==-1)
            return new String[] {text};
        ArrayList<String> list = new ArrayList<>();
        while (next != -1) {
            list.add(text.substring(off, next));
            off = next + 1;
            next = text.indexOf('.', off);
        }
        list.add(text.substring(off, text.length()));
        return list.toArray(new String[list.size()]);
	}

	private static String r(String key) {
		String k = key.trim();
		return k.length() > 1 && (k.startsWith("\"") && k.endsWith("\"")||k.startsWith("'") && k.endsWith("'"))?key.substring(1, key.length()-1-removeLastSpaces(key)):key;
	}

	private static int removeLastSpaces(String s) {
		int i = 0;
		for(int d = s.length()-1; d > 0; --d) {
			if(s.charAt(d)==' ') {
				++i;
			}else break;
		}
		return i;
	}

	@Override
	public boolean isLoaded() {
		return l;
	}
}
