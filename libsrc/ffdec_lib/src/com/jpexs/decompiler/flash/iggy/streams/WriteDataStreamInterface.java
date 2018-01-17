/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
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
