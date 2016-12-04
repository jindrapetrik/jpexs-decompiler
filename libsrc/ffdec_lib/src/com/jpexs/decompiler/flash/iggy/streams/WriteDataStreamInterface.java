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

    public void setOlderOffsetToThisPos(long savedPos) throws IOException;

    public void setOlderOffsetToThisPosCheck(long savedPos, long expected) throws IOException;

    public boolean writeUI64(long val) throws IOException;

    public boolean writeSI64(long val) throws IOException;

    public boolean writeUI32(long val) throws IOException;

    public boolean writeUI16(int val) throws IOException;

    public boolean writeWChar(String val) throws IOException;

    public void pad8bytes() throws IOException;

    public boolean writeUI8(int val) throws IOException;

    public void write(int val) throws IOException;

    public void writeBytes(byte[] data) throws IOException;

    public boolean writeFloat(float val) throws IOException;

    public void seek(long pos, SeekMode mode) throws IOException;

    public byte[] getAllBytes() throws IOException;

    @Override
    public void close();

    public void setIndexing(IggyIndexBuilder indexing);

    public IggyIndexBuilder getIndexing();
}
