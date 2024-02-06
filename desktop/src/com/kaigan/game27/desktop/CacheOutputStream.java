package com.kaigan.game27.desktop;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Azmi on 9/13/2017.
 */
public class CacheOutputStream extends OutputStream {

    private final int size;
    private final byte[] buffer;
    private final OutputStream output;

    private int position = 0;


    public CacheOutputStream(int size, OutputStream output) {
        this.size = size;
        this.buffer = new byte[size * 2];       // Double the exact size, to be used as the overflow buffer

        this.output = output;
    }

    @Override
    public void write(int value) throws IOException {
        if(output != null)
            output.write(value);

        if (position == buffer.length)
            compact();
        buffer[position++] = (byte) value;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        if(output != null)
            output.write(bytes, offset, length);

        if (length >= size) {
            // Just take the last bytes
            System.arraycopy(bytes, offset + (length - size), buffer, 0, size);
            position = size;
            return;
        }
        // Else should fit at least in the overflow buffer
        int available = buffer.length - position;
        if (available < length)
            compact();          // Need to compact first
        System.arraycopy(bytes, offset, buffer, position, length);
        position += length;
    }

    private void compact() {
        if (position > size) {
            System.arraycopy(buffer, size, buffer, 0, position - size);
            position -= size;
        }
    }

    public byte[] toArray() {
        if (position > size) {
            // Use the last bytes only
            byte[] out = new byte[size];
            System.arraycopy(buffer, position - size, out, 0, size);
            return out;
        }
        // Else smaller than limit
        byte[] out = new byte[position];
        System.arraycopy(buffer, 0, out, 0, position);
        return out;
    }
}
