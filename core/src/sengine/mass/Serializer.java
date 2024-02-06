
package sengine.mass;

import sengine.mass.io.Input;
import sengine.mass.io.Output;

public interface Serializer<T> {

	public T read(Mass m, Input s, Class<T> type);

	public void write(Mass m, Output s, T o);

}
