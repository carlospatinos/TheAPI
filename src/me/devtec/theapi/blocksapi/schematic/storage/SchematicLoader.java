package me.devtec.theapi.blocksapi.schematic.storage;

import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import me.devtec.theapi.utils.Compressors;
import me.devtec.theapi.utils.datakeeper.loader.DataLoader;
import me.devtec.theapi.utils.json.Json;

public class SchematicLoader extends DataLoader {
	private final Map<String, Object[]> data = new LinkedHashMap<>();
	private boolean l;

	@Override
	public Map<String, Object[]> get() {
		return data;
	}

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
	}

	@Override
	public void load(String input) {
		data.clear();
		try {
			byte[] bb = Compressors.decompress(Base64.getDecoder().decode(input));
			ByteArrayDataInput bos = ByteStreams.newDataInput(bb);
			while (true)
				try {
					String key = bos.readUTF();
					String value = bos.readUTF().substring(1);
					String next;
					boolean run = true;
					while (run)
						try {
							next=bos.readUTF();
							if(next==null) {
							}
							else {
								if(next.equals("1")) {
									run=false;
									continue;
								}
								next=next.substring(1);
							}
							value+=next;
						}catch(Exception not) {
						run=false;
					}
					data.put(key, new Object[] {value==null?null:Json.reader().read(value), null, value});
				} catch (Exception e) {
					break;
				}
			if (!data.isEmpty())
				l = true;
		} catch (Exception er) {
			String inputF =input.substring(0, input.length()-2);
			try {
				byte[] bb = Compressors.decompress(Base64.getDecoder().decode(inputF));
				ByteArrayDataInput bos = ByteStreams.newDataInput(bb);
				while (true)
					try {
						String key = bos.readUTF();
						String value = bos.readUTF().substring(1);
						String next;
						boolean run = true;
						while (run)
							try {
								next=bos.readUTF();
								if(next==null) {
								}
								else {
									if(next.equals("1")) {
										run=false;
										continue;
									}
									next=next.substring(1);
								}
								value+=next;
							}catch(Exception not) {
							run=false;
						}
						data.put(key, new Object[] {value==null?null: Json.reader().read(value), null, value});
					} catch (Exception e) {
						break;
					}
				if (!data.isEmpty())
					l = true;
			}catch(Exception era) {
				l=false;
			}
		}
	}

	@Override
	public Collection<String> getHeader() {
		// NOT SUPPORTED
		return null;
	}

	@Override
	public Collection<String> getFooter() {
		// NOT SUPPORTED
		return null;
	}

	@Override
	public boolean isLoaded() {
		return l;
	}
}
