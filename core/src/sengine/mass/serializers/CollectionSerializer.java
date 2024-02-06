
package sengine.mass.serializers;

import java.util.Collection;

import sengine.mass.Mass;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CollectionSerializer implements Serializer<Collection> {

	public void write (Mass kryo, Output output, Collection collection) {
		output.writeInt(collection.size());
		for (Object element : collection)
			kryo.write(element);
	}

	public Collection read (Mass kryo, Input input, Class<Collection> type) {
		Collection collection = Mass.newInstance(type);
		kryo.reference(collection);
		int length = input.readInt();
		for (int i = 0; i < length; i++)
			collection.add(kryo.read());
		return collection;
	}
}
