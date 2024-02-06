package sengine.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import sengine.mass.MassSerializable;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;

public class MemoryFileHandle extends FileHandle implements MassSerializable {

	public final String filename;
	final byte[] data;
	
	public MemoryFileHandle(FileHandle fileHandle) {
		this(fileHandle.path(), fileHandle.readBytes());
	}

	
	@MassConstructor
	public MemoryFileHandle(String filename, byte[] data) {
		super(filename, FileType.Internal);
		
		this.filename = filename;
		this.data = data;
	}

	@Override
	public InputStream read() {
		return new ByteArrayInputStream(data);
	}
	
	@Override
	public OutputStream write(boolean append) {
		throw new UnsupportedOperationException("Cannot write to a MassFileHandle in: " + filename);
	}
	
	
	@Override
	public long length() {
		return data.length;
	}

	@Override
	public Object[] mass() {
		return new Object[] { filename, data };
	}
}
