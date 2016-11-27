package com.jpexs.decompiler.flash.iggy.streams;

import java.io.EOFException;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractDataStream implements DataStreamInterface {

    /**
     * Available bytes
     *
     * @return null if unknown, long value otherwise
     */
    @Override
    public abstract Long available();

    @Override
    public abstract long position();

    @Override
    public long readUI64() throws IOException {
        try {
            return (readUI32() + (readUI32() << 32)) & 0xffffffffffffffffL;
        } catch (EOFException ex) {
            return -1;
        }
    }

    @Override
    public boolean writeUI64(long val) throws IOException {
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

    @Override
    public long readUI32() throws IOException {
        try {
            return (readUI8() + (readUI8() << 8) + (readUI8() << 16) + (readUI8() << 24));
        } catch (EOFException ex) {
            return -1;
        }
    }

    @Override
    public boolean writeUI32(long val) throws IOException {
        write((int) (val & 0xff));
        write((int) ((val >> 8) & 0xff));
        write((int) ((val >> 16) & 0xff));
        write((int) ((val >> 24) & 0xff));
        return true;
    }

    @Override
    public int readUI16() throws IOException {
        try {
            return (readUI8() + (readUI8() << 8)) & 0xffff;
        } catch (EOFException ex) {
            return -1;
        }
    }

    @Override
    public boolean writeUI16(int val) throws IOException {
        write(val & 0xff);
        write((val >> 8) & 0xff);
        return true;
    }

    @Override
    public int readUI8() throws IOException {
        try {
            return read() & 0xff;
        } catch (EOFException ex) {
            return -1;
        }
    }

    @Override
    public boolean writeUI8(int val) throws IOException {
        write(val);
        return true;
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat((int) readUI32());
    }

    @Override
    public boolean writeFloat(float val) throws IOException {
        return writeUI32(Float.floatToIntBits(val));
    }

    @Override
    public byte[] readBytes(int numBytes) throws IOException {
        byte[] ret = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            ret[i] = (byte) read();
        }
        return ret;
    }

    public byte[] getAllBytes() throws IOException {
        long oldPos = position();
        seek(0, SeekMode.SET);
        byte[] ret = readBytes((int) (long) available());
        seek(oldPos, SeekMode.SET);
        return ret;
    }

    @Override
    public void writeBytes(byte[] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            write(data[i] & 0xff);
        }
    }

    @Override
    public abstract int read() throws IOException;

    @Override
    public abstract void seek(long pos, SeekMode mode) throws IOException;

    @Override
    public void write(int val) throws IOException {
        //nothing
    }

    @Override
    public void close() {
        //nothing
    }

}
