package com.jpexs.decompiler.flash.iggy;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author JPEXS
 */
public class ByteArrayDataStream extends AbstractDataStream {

    private byte[] data;
    private int pos;

    public ByteArrayDataStream(int initialSize) {
        this(new byte[initialSize]);
    }

    public ByteArrayDataStream(byte data[]) {
        this.data = data;
        pos = 0;
    }

    @Override
    protected int read() throws IOException {
        if (pos >= data.length) {
            throw new EOFException("End of stream reached");
        }
        return data[pos++] & 0xff;
    }

    public void resize(int newsize) {
        data = Arrays.copyOf(data, newsize);
        if (pos > data.length) {
            pos = data.length;
        }
    }

    @Override
    protected void write(int val) throws IOException {
        if (pos >= data.length) {
            throw new EOFException("End of stream reached");
        }
        data[pos++] = (byte) val;
    }

    @Override
    protected void seek(long pos, SeekMode mode) throws IOException {
        long newpos = pos;
        if (mode == SeekMode.CUR) {
            newpos = this.pos + pos;
        } else if (mode == SeekMode.END) {
            newpos = data.length - pos;
        }
        if (newpos > data.length) {
            throw new ArrayIndexOutOfBoundsException("Position outside bounds accessed: " + pos + ". Size: " + data.length);
        } else if (newpos < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative position accessed: " + pos);
        } else {
            this.pos = (int) newpos;
        }
    }

    @Override
    public Long available() {
        return (long) (data.length - pos);
    }

}
