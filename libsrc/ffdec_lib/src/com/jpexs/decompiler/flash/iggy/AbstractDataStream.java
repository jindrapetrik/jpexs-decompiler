package com.jpexs.decompiler.flash.iggy;

import java.io.EOFException;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractDataStream {

    /**
     * Available bytes
     *
     * @return null if unknown, long value otherwise
     */
    public abstract Long available();

    protected long readUI64() throws IOException {
        try {
            return (readUI32() + (readUI32() << 32)) & 0xffffffffffffffffL;
        } catch (EOFException ex) {
            return -1;
        }
    }

    protected boolean writeUI64(long val) throws IOException {
        write((int) (val & 0xff));
        write((int) ((val >> 8) & 0xff));
        write((int) ((val >> 16) & 0xff));
        write((int) ((val >> 24) & 0xff));

        write((int) ((val >> 32) & 0xff));
        write((int) ((val >> 40) & 0xff));
        write((int) ((val >> 48) & 0xff));
        write((int) ((val >> 56) & 0xff));
        return true;
    }

    protected long readUI32() throws IOException {
        try {
            return (readUI8() + (readUI8() << 8) + (readUI8() << 16) + (readUI8() << 24)) & 0xffffffff;
        } catch (EOFException ex) {
            return -1;
        }
    }

    protected boolean writeUI32(long val) throws IOException {
        write((int) (val & 0xff));
        write((int) ((val >> 8) & 0xff));
        write((int) ((val >> 16) & 0xff));
        write((int) ((val >> 24) & 0xff));
        return true;
    }

    protected int readUI16() throws IOException {
        try {
            return (readUI8() + (readUI8() << 8)) & 0xffff;
        } catch (EOFException ex) {
            return -1;
        }
    }

    protected boolean writeUI16(int val) throws IOException {
        write(val & 0xff);
        write((val >> 8) & 0xff);
        return true;
    }

    protected int readUI8() throws IOException {
        try {
            return read() & 0xff;
        } catch (EOFException ex) {
            return -1;
        }
    }

    protected boolean writeUI8(int val) throws IOException {
        write(val);
        return true;
    }

    protected float readFloat() throws IOException {
        return Float.intBitsToFloat((int) readUI32());
    }

    protected boolean writeFloat(float val) throws IOException {
        return writeUI32(Float.floatToIntBits(val));
    }

    protected byte[] readBytes(int numBytes) throws IOException {
        byte[] ret = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            ret[i] = (byte) read();
        }
        return ret;
    }

    protected abstract int read() throws IOException;

    protected abstract void seek(long pos, SeekMode mode) throws IOException;

    protected void write(int val) throws IOException {
        //nothing
    }
}
