
package sengine.mass.io;

import java.io.EOFException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import sengine.mass.MassException;

public class Input extends InputStream {
	final Charset UTF8 = Charset.forName("UTF-8");

	byte[] buffer = null;
	int position = 0;
	int limit = 0;
	InputStream inputStream = null;
	
	public Input() {
		
	}
	
	public Input(int bufferSize) {
		setBuffer(new byte[bufferSize]);
	}
	
	public Input(byte[] buffer) {
		setBuffer(buffer);
	}
	
	public Input(InputStream inputStream) {
		this(inputStream, 64);
	}
	
	public Input(InputStream inputStream, int bufferSize) {
		setBuffer(new byte[bufferSize]);
		setInputStream(inputStream);
	}


	public void setBuffer (byte[] buffer) {
		setBuffer(buffer, 0, buffer.length);
	}

	public void setBuffer (byte[] buffer, int position, int limit) {
		if(buffer == null) {
			this.buffer = null;
			this.position = 0;
			this.limit = 0;
			return;
		}
		this.buffer = buffer;
		setLimit(limit);
		setPosition(position);
		inputStream = null;
	}
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		clear();
	}
	
	public void setPosition(int position) { 
		if(position > limit)
			throw new ArrayIndexOutOfBoundsException("Cannot set position " + position + " past limit of " + limit);
		this.position = position;
	}
	public void setLimit(int limit) {
		if(limit > buffer.length)
			throw new ArrayIndexOutOfBoundsException("Cannot set limit " + limit + " past buffer size of " + buffer.length);
		this.limit = limit;
	}
	
	public byte[] getBuffer() { return buffer; }
	public InputStream getInputStream() { return inputStream; }
	public int position() { return position; }
	public int limit() { return limit; }
	public int remaining() { return limit - position; }
	
	protected int streamSkip (int minCount, int maxCount) {
		int skipped = 0;
		// Else skip inputstream
		try {
			while(skipped < maxCount) {
				int bytesSkipped = (int) inputStream.skip(maxCount - skipped);
				if(bytesSkipped <= 0) {
					if(skipped < minCount)
						throw new EOFException();
					break;
				}
				skipped += bytesSkipped;
			}
		} catch (Throwable e) {
			if(skipped < minCount)
				throw new MassException("Failed to skip remaining " + (minCount - skipped) + " bytes from stream", e); 
		}
		return skipped;
	}

	protected int streamFill (byte[] buffer, int offset, int minCount, int maxCount) {
		int read = 0;
		try {
			while(read < maxCount) {
				int bytesRead = inputStream.read(buffer, offset, maxCount - read);
				if(bytesRead == -1) {
					if(read < minCount)
						throw new EOFException();
					break;
				}
				read += bytesRead;
				offset += bytesRead;
			}
		} catch (Throwable e) {
			if(read < minCount)
				throw new MassException("Failed to read remaining " + (minCount - read) + " bytes from stream", e);
		}
		return read;
	}

	int require (int minCount, int maxCount) {
		int available = limit - position;
		if(available >= maxCount)
			return available;
		// Check if can read the remaining from stream
		if(inputStream == null) {
			if(available >= minCount)
				return available;
			else
				throw new MassException("Buffer underflow for remaining " + (minCount - available) + " bytes");
		}
		// Compact if needed
		if((buffer.length - position) < maxCount) {
			if(buffer.length < maxCount)
				throw new MassException("Buffer of " + buffer.length + " bytes too small for required " + maxCount + " bytes");
			System.arraycopy(buffer, position, buffer, 0, available);		// Required bytes can fit into buffer, just need to compact
			position = 0;
			limit = available;
		}
		// Else required bytes can fit into remaining buffer
		// Read from stream
		limit += streamFill(buffer, limit, minCount - available, buffer.length - limit);
		return limit - position;
	}
	
	public void clear() {
		limit = 0;
		position = 0;
	}

	// InputStream
	@Override
	public int read()  {
		if(require(0, 1) == 0)
			return -1;
		return buffer[position++] & 0xFF;
	}

	@Override
	public int read (byte[] bytes) {
		return readBytes(bytes, 0, 0, bytes.length);
	}

	@Override
	public int read (byte[] bytes, int offset, int count) {
		return readBytes(bytes, offset, 0, count);
	}
	
	@Override
	public long skip (long count) {
		if(count <= 0)
			return 0;
		// Drain buffer first
		int skipped = limit - position;
		if(skipped > count)
			skipped = (int) count;
		position += skipped;
		// Skip inputstream if available
		if(inputStream != null)
			skipped += streamSkip(0, (int) (count - skipped));
		return skipped;
	}
	
	@Override
	public void close() {
		if(inputStream == null)
			return;
		try {
			inputStream.close();
		} catch(Throwable e) {
			throw new MassException("Failed to close stream", e);
		}
	}

	// byte

	public byte readByte () {
		require(1, 1);
		return buffer[position++];
	}

	public int readByteUnsigned () {
		require(1, 1);
		return buffer[position++] & 0xFF;
	}

	public byte[] readBytes (int length) {
		byte[] bytes = new byte[length];
		readBytes(bytes, 0, length, length);
		return bytes;
	}
	
	public void readBytes (byte[] bytes) {
		readBytes(bytes, 0, bytes.length, bytes.length);
	}

	public void readBytes (byte[] bytes, int offset, int count) {
		readBytes(bytes, offset, count, count);
	}
	
	public int readBytes (byte[] bytes, int offset, int minCount, int maxCount) {
		if(minCount > maxCount)
			maxCount = minCount;
		if(maxCount <= 0)
			return 0;
		else if((maxCount + offset) > bytes.length)
			throw new ArrayIndexOutOfBoundsException("Insufficient byte array size: " + bytes.length + " required: " + (maxCount + offset));
		// Drain existing buffer first
		int read = limit - position;
		if(read > maxCount)
			read = maxCount;
		if(read > 0) {
			System.arraycopy(buffer, position, bytes, offset, read);
			position += read;
			offset += read;
		}
		if(read < maxCount) {
			// Need to read remaining from stream if needed
			if(inputStream == null) {
				if(read >= minCount)
					return read;
				else
					throw new MassException("Buffer underflow for remaining " + (minCount - read) + " bytes");
			}
			read += streamFill(bytes, offset, minCount - read, maxCount - read);
		}
		if(read == 0)
			return -1;
		return read;
	}

	public void readBytes (ByteBuffer byteBuffer, int count) {
		if(byteBuffer.hasArray()) {
			readBytes(byteBuffer.array(), byteBuffer.arrayOffset(), count, count);
			byteBuffer.position(byteBuffer.position() + count);
			return;
		}
		// Else read 
		while(count > 0) {
			int read = buffer.length;
			if(read > count)
				read = count;
			require(read, read);
			byteBuffer.put(buffer, position, read);
			position += read;
			count -= read;
		}
	}
	
	public boolean checkBytes(byte[] bytes) {
		return checkBytes(bytes, 0, bytes.length);
	}
	
	public boolean checkBytes(byte[] bytes, int offset, int count) {
		if((count + offset) > bytes.length)
			throw new ArrayIndexOutOfBoundsException("Insufficient byte array size: " + bytes.length + " required: " + (count + offset));
		else if(count > buffer.length)
			throw new MassException("Buffer of " + buffer.length + " bytes too small for required " + count + " bytes");
		if(require(0, count) < count)
			return false;
		for(int c = 0; c < count; c++) {
			if(buffer[position + c] != bytes[offset + c])
				return false;
		}
		position += count;
		return true;
	}
	
	// int

	public int readInt () {
		require(4, 4);
		int c = (buffer[position++] & 0xFF) << 24;
		c |= (buffer[position++] & 0xFF) << 16;
		c |= (buffer[position++] & 0xFF) << 8;
		c |= (buffer[position++] & 0xFF);
		return c;
	}
	
	// string
	
	public String readString () {
		int length = readInt();
		if(length < 0)
			return null;
		else if(length == 0)
			return "";
		String s;
		if(buffer.length >= length) {
			require(length, length);
			s = new String(buffer, position, length, UTF8);
			position += length;
		} else {
			byte[] buffer = readBytes(length);
			s = new String(buffer, UTF8);
		}
		return s;
	}
	
	public boolean readFixedString(String value) {
		if(value == null || value.length() == 0)
			return true;
		byte[] bytes = value.getBytes(UTF8);
		return checkBytes(bytes);
	}

	// float
	public float readFloat () {
		return Float.intBitsToFloat(readInt());
	}

	// short
	public short readShort () {
		require(2, 2);
		int c = (buffer[position++] & 0xFF) << 8;
		c |= (buffer[position++] & 0xFF);
		return (short)c;
	}

	// long

	/** Reads an 8 byte long. */
	public long readLong () throws MassException {
		require(8, 8);
		long c = (long)(buffer[position++] & 0xFF) << 56;
		c |= (long)(buffer[position++] & 0xFF) << 48;
		c |= (long)(buffer[position++] & 0xFF) << 40;
		c |= (long)(buffer[position++] & 0xFF) << 32;
		c |= (long)(buffer[position++] & 0xFF) << 24;
		c |= (long)(buffer[position++] & 0xFF) << 16;
		c |= (long)(buffer[position++] & 0xFF) << 8;
		c |= (long)(buffer[position++] & 0xFF);
		return c;
	}

	// boolean
	/** Reads a 1 byte boolean. */
	public boolean readBoolean () {
		return readByte() != 0;
	}

	// char

	public char readChar () throws MassException {
		require(2, 2);
		int c = (buffer[position++] & 0xFF) << 8;
		c |= (buffer[position++] & 0xFF);
		return (char)c;
	}

	// double

	/** Reads an 8 bytes double. */
	public double readDouble () throws MassException {
		return Double.longBitsToDouble(readLong());
	}
}
