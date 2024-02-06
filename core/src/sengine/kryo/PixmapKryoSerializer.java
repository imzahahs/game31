package sengine.kryo;

import java.nio.ByteBuffer;
import sengine.mass.Mass;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

import com.badlogic.gdx.graphics.Pixmap;

public class PixmapKryoSerializer implements Serializer<Pixmap> {

	@Override
	public Pixmap read(Mass m, Input s, Class<Pixmap> type) {
		int width = s.readInt();
		int height = s.readInt();
		Pixmap.Format format = Pixmap.Format.values()[s.readByteUnsigned()];
		
		Pixmap pixmap = new Pixmap(width, height, format);
		
		ByteBuffer bytes = pixmap.getPixels();
		int capacity = bytes.capacity();
		bytes.position(0);
		s.readBytes(bytes, capacity);
		bytes.position(0);	
		
		return pixmap;
	}

	@Override
	public void write(Mass m, Output s, Pixmap o) {
		s.writeInt(o.getWidth());
		s.writeInt(o.getHeight());
		s.writeByte((byte)o.getFormat().ordinal());
		
		ByteBuffer bytes = o.getPixels();
		bytes.position(0);
		int capacity = bytes.capacity();
		bytes.limit(capacity);
		s.writeBytes(bytes, capacity);
		bytes.position(0);
	}
}
