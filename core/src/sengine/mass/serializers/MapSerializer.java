
package sengine.mass.serializers;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import sengine.mass.Mass;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapSerializer implements Serializer<Map> {

	@Override
	public void write (Mass kryo, Output output, Map map) {
		int length = map.size();
		output.writeInt(length);
		
		for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
			Entry entry = (Entry)iter.next();
			kryo.write(entry.getKey());
			kryo.write(entry.getValue());
		}
	}

	public Map read (Mass kryo, Input input, Class<Map> type) {
		Map map = Mass.newInstance(type);
		kryo.reference(map);
		
		int length = input.readInt();

		for (int i = 0; i < length; i++) {
			Object key = kryo.read();
			Object value = kryo.read();
			map.put(key, value);
		}
		return map;
	}
}
