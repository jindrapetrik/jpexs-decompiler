package com.jpexs.decompiler.flash.iggy.streams;

import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public interface WriteDataStreamInterface extends AutoCloseable {

    /**
     * Available bytes
     *
     * @return null if unknown, long value otherwise
     */
    public Long available();

    public Long totalSize();

    public long position();

    public boolean writeUI64(long val) throws IOException;

    public boolean writeUI32(long val) throws IOException;

    public boolean writeUI16(int val) throws IOException;

    public boolean writeUI8(int val) throws IOException;

    public void write(int val) throws IOException;

    public void writeBytes(byte[] data) throws IOException;

    public boolean writeFloat(float val) throws IOException;

    public void seek(long pos, SeekMode mode) throws IOException;

    @Override
    public void close();
}
