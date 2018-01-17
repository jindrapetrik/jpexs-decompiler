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
public interface ReadDataStreamInterface extends AutoCloseable {

    /**
     * Available bytes
     *
     * @return null if unknown, long value otherwise
     */
    public Long available();

    public Long totalSize();

    public long position();

    public long readUI64() throws IOException;

    public long readSI64() throws IOException;

    public long readUI32() throws IOException;

    public int readUI16() throws IOException;

    public int readUI8() throws IOException;

    public int readUI8(long addr) throws IOException;

    public int read() throws IOException;

    public byte[] readBytes(int numBytes) throws IOException;

    public float readFloat() throws IOException;

    public void seek(long pos, SeekMode mode) throws IOException;

    public byte[] getAllBytes() throws IOException;

    public String readWChar() throws IOException;

    public void pad8bytes() throws IOException;

    @Override
    public void close();
}
