
package sengine.mass.io;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import sengine.mass.MassException;

public class Output extends OutputStream {
	final Charset UTF8 = Charset.forName("UTF-8");
	
	public static int minCompressionBufferSize = 64;
	
	byte[] buffer;
	int position;
	OutputStream outputStream = null;
	
	public Output(int bufferSize) {
		this.buffer = new byte[bufferSize];
	}
	
	public Output(OutputStream outputStream) {
		this(outputStream, 64);
	}
	
	public Output(OutputStream outputStream, int bufferSize) {
		this.buffer = new byte[bufferSize];
		this.outputStream = outputStream;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public void setPosition(int position) {
		if(position > this.position)
			require(position - this.position);
		this.position = position;
	}
	
	public byte[] getBuffer() { return buffer; }
	public OutputStream getOutputStream() { return outputStream; }
	public int position() { return position; }

	public void clear () {
		position = 0;
	}

	public boolean require (int required) {
		if((buffer.length - position) >= required)
			return false;
		// Try flush and see if buffer is enough
		flush();
		if((buffer.length - position) >= required)
			return false;
		// Else need to expand buffer
		int newSize = buffer.length * 2;
		while(newSize < (position + required))
			newSize *= 2;
		buffer = Arrays.copyOf(buffer, newSize);
		return true;
	}


	// OutputStream
	@Override
	public void flush () {
		if(outputStream == null || position == 0) 
			return;		// cannot flush
		try {
			outputStream.write(buffer, 0, position);
		} catch (Throwable e) {
			throw new MassException("Failed to write " + position + " bytes to stream", e);
		}
		position = 0;
	}

	@Override
	public void close () {
		if(outputStream == null) 
			return;		// nothing to close
		flush();
		try {
            outputStream.flush();
			outputStream.close();
		} catch (Throwable e) {
			throw new MassException("Failed to close stream", e);
		}
	}

	@Override
	public void write (int value) {
		require(1);
		buffer[position++] = (byte)value;
	}

	@Override
	public void write (byte[] bytes) {
		writeBytes(bytes, 0, bytes.length);
	}

	@Override
	public void write (byte[] bytes, int offset, int length) {
		writeBytes(bytes, offset, length);
	}

	// byte

	public void writeByte (byte value) {
		require(1);
		buffer[position++] = (byte)value;
	}
	
	public void writeByte (int value) {
		require(1);
		buffer[position++] = (byte)value;
	}

	public void writeBytes (byte[] bytes) throws MassException {
		writeBytes(bytes, 0, bytes.length);
	}

	public void writeBytes (byte[] bytes, int offset, int count) {
		if(outputStream == null)
			require(count);		// No output stream, just expand buffer as needed
		else if((buffer.length - position) < count) {
			// Else output stream is available but buffer is too small to hold, flush and write directly to stream
			flush();
			try {
				outputStream.write(bytes, offset, count);
			} catch (Throwable e) {
				throw new MassException("Failed to write " + count + " bytes to stream", e);
			}
			return;
		}
		// Else need to write to buffer
		System.arraycopy(bytes, offset, buffer, position, count);
		position += count;
	}
	
	public void writeBytes (ByteBuffer byteBuffer, int count) {
		if(byteBuffer.hasArray()) {
			writeBytes(byteBuffer.array(), byteBuffer.arrayOffset(), count);
			byteBuffer.position(byteBuffer.position() + count);
			return;
		}
		// Else write
		while(count > 0) {
			int write = buffer.length;
			if(write > count)
				write = count;
			require(write);
			byteBuffer.get(buffer, position, write);
			position += write;
			count -= write;
		}
	}
	
	public void writeBytes (Input input, int count) {
		while(count > 0) {
			int read = count > buffer.length ? buffer.length : count;
			require(read);
			input.readBytes(buffer, position, read);
			position += read;
			count -= read;
		}
	}

	// int

	public void writeInt (int value) {
		require(4);
		buffer[position++] = (byte)(value >> 24);
		buffer[position++] = (byte)(value >> 16);
		buffer[position++] = (byte)(value >> 8);
		buffer[position++] = (byte)value;
	}
	
	public void deflate(Deflater deflater, int offset, int count) {
		if(outputStream != null)
			throw new MassException("Cannot deflate buffer while output stream is set");
		else if((offset + count) > position)
			throw new MassException("Overlapping compression buffer: " + offset + " " + count + " " + position);
		deflate(deflater, buffer, offset, count);
	}
	
	// Compression
	public void deflate(Deflater deflater, byte[] uncompressed, int offset, int count) {
		try {
			deflater.setInput(uncompressed, offset, count);
			deflater.finish();
            require(count);		// Would be a maximum of this size
            while(!deflater.finished()) {
				// Expand buffer
				int available = buffer.length - position;
				if(available < minCompressionBufferSize) {
					require(minCompressionBufferSize);
					available = buffer.length - position;
				}
				// Deflate
				position += deflater.deflate(buffer, position, available);
			}
		} catch (Throwable e) {
			throw new MassException("Failed to deflate " + count + " bytes", e);
		} finally {
			deflater.reset();
		}
	}

	public void inflate(Inflater inflater, int offset, int count) {
		if(outputStream != null)
			throw new MassException("Cannot inflate buffer while output stream is set");
		else if((offset + count) > position)
			throw new MassException("Overlapping decompression buffer: " + offset + " " + count + " " + position);
		inflate(inflater, buffer, offset, count);
	}
	
	public void inflate(Inflater inflater, byte[] compressed, int offset, int count) {
		try {
			inflater.setInput(compressed, offset, count);
			require(count);		// Would be a minimum of this size
			while(!inflater.finished()) {
				// Expand buffer
				int available = buffer.length - position;
				if(available < minCompressionBufferSize) {
					require(minCompressionBufferSize);
					available = buffer.length - position;
				}
				// Inflate
				position += inflater.inflate(buffer, position, available);
			}
		} catch (Throwable e) {
			throw new MassException("Failed to inflate " + count + " bytes", e);
		} finally {
			inflater.reset();
		}
	}
	
	// string

	public void writeString (String value) {
		if(value == null) {
			writeInt(-1);
			return;
		}
		else if(value.length() == 0) {
			writeInt(0);
			return;
		}
		byte[] bytes = value.getBytes(UTF8);
		writeInt(bytes.length);
		// Else write string data
		writeBytes(bytes);
	}

	public void writeFixedString (String value) {
		if(value == null || value.length() == 0)
			return;
		byte[] bytes = value.getBytes(UTF8);
		writeBytes(bytes);
	}

	// float
	
	public void writeFloat (float value) {
		writeInt(Float.floatToIntBits(value));
	}

	// short

	public void writeShort (int value) {
		require(2);
		buffer[position++] = (byte)(value >>> 8);
		buffer[position++] = (byte)value;
	}

	// long

	public void writeLong (long value) {
		require(8);
		buffer[position++] = (byte)(value >>> 56);
		buffer[position++] = (byte)(value >>> 48);
		buffer[position++] = (byte)(value >>> 40);
		buffer[position++] = (byte)(value >>> 32);
		buffer[position++] = (byte)(value >>> 24);
		buffer[position++] = (byte)(value >>> 16);
		buffer[position++] = (byte)(value >>> 8);
		buffer[position++] = (byte)value;
	}

	// boolean

	public void writeBoolean (boolean value) {
		writeByte((byte)(value ? 1 : 0));
	}

	// char
	
	public void writeChar (char value) {
		require(2);
		buffer[position++] = (byte)(value >>> 8);
		buffer[position++] = (byte)value;
	}

	// double

	public void writeDouble (double value) {
		writeLong(Double.doubleToLongBits(value));
	}
	
	
	public byte[] toBytes() {
		return Arrays.copyOf(buffer, position);
	}

}
