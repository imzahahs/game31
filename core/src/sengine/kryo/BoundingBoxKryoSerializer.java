package sengine.kryo;

import sengine.mass.Mass;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class BoundingBoxKryoSerializer implements Serializer<BoundingBox> {
	
	@Override
	public BoundingBox read(Mass m, Input s, Class<BoundingBox> type) {
		Vector3 min = m.read();
		Vector3 max = m.read();
		return new BoundingBox(min, max);
	}

	@Override
	public void write(Mass m, Output s, BoundingBox o) {
		m.write(o.min);
		m.write(o.max);
	}
}
