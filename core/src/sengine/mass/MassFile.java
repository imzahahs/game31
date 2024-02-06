package sengine.mass;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectIntMap;

import sengine.File;
import sengine.mass.io.Input;
import sengine.mass.io.Output;
import sengine.mass.util.IdentityObjectIntMap;
import sengine.utils.WeakCache;


public class MassFile extends Mass {
	public static final String FORMAT = "FileSystem";
	
	public void load(String filename) {
		load(File.open(filename));
	}
	
	public void load(FileHandle file) {
		Input input = new Input(file.read());
		load(input);
		input.close();
	}
	
	public void load(Input s) {
		load(s, FORMAT);
	}
	
	public void save(String filename) {
		save(File.openCache(filename, false));
	}
	
	public void save(FileHandle file) {
		Output output = new Output(file.write(false));
		save(output);
		output.close();
	}
	
	public void save(Output s) {
		// TODO: override save to cleanup dead objects
		save(s, FORMAT);
	}
	
	
	// Caches
	final WeakCache<Integer, Object> deserializedCache = new WeakCache<Integer, Object>();
	final IdentityObjectIntMap<Object> serializedCache = new IdentityObjectIntMap<Object>();
	// Lookup
	final ObjectIntMap<String> names = new ObjectIntMap<String>();
	
	public String[] names() {
		String[] namesList = new String[names.size];
		int c = 0;
		for(String name : names.keys())
			namesList[c++] = name;
		return namesList;
	}
	
	public void rebuild() {
		HashMap<String, Object> mass = new HashMap<String, Object>();
		for(String name : names.keys())
			mass.put(name, get(name));
		clear();
		for(Entry<String, Object> e : mass.entrySet())
			add(e.getKey(), e.getValue());
	}
	
	public void remove(String name) {
		names.remove(name, -1);
	}
	
	public void add(String name, Object object) {
		try {
			int id = add(object);
			names.put(name, id);
		} catch (Throwable e) {
			throw new RuntimeException("Failed to serialize object with name: " + name, e);
		}
	}

	public <T> T get(String name, T defaultValue) {
		int id = names.get(name, -1);
		if(id == -1)
			return defaultValue;	// not found
		try {
			return get(id, true);
		} catch (Throwable e) {
			throw new RuntimeException("Failed to decode object[" + id + "] name: " + name, e);
		}
	}

	public <T> T get(String name) {
		return get(name, null);
	}
	
	@Override
	public void clear() {
		super.clear();
		deserializedCache.clear();
		serializedCache.clear();
	}

	@Override
	protected void cacheSerializedObject(Object object, int idx) {
		serializedCache.put(object, idx);
	}
	@Override
	protected int findSerializedObject(Object object) {
		return serializedCache.get(object, NOT_FOUND);
	}
	
	@Override
	protected void cacheDeserializedObject(int idx, Object object) {
		deserializedCache.put(idx, object);
	}
	@Override
	protected Object findDeserializedObject(int idx) {
		return deserializedCache.get(idx);
	}
	
	@Override
	protected int findUniqueId(Object o) {
		// TODO Auto-generated method stub
		return super.findUniqueId(o);
	}
	@Override
	protected Object getUniqueObject(int id) {
		// TODO Auto-generated method stub
		return super.getUniqueObject(id);
	}

	@Override
	protected void writeFormatData(Output output) {
		output.writeInt(names.size);
		for(ObjectIntMap.Entry<String> e : names.entries()) {
			output.writeString(e.key);
			output.writeInt(e.value);
		}
	}

	@Override
	protected void readFormatData(Input input) {
		int size = input.readInt();
		for(int c = 0; c < size; c++) {
			String name = input.readString();
			int id = input.readInt();
			names.put(name, id);
		}
	}

	@Override
	protected boolean shouldInlineObject(Object object) {
		// TODO Auto-generated method stub
		return super.shouldInlineObject(object);
	}
}
