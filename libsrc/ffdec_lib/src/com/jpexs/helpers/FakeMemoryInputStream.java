/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.helpers;

import java.io.IOException;
import java.io.InputStream;

/**
 * Special MemoryInputStream that is not a MemoryInputStream in fact. Input
 * stream to handle some edge cases.
 *
 * @author JPEXS
 */
public class FakeMemoryInputStream extends MemoryInputStream {

    private long pos;

    private final int maxLength;

    private final InputStream is;

    public FakeMemoryInputStream(InputStream is) throws IOException {
        super(new byte[0]);
        this.maxLength = Integer.MAX_VALUE;
        this.is = is;
    }

    @Override
    public byte[] getAllRead() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getPos() {
        return pos;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (pos < 0) {
            throw new IOException("Seek to negative position");
        }
        this.pos = pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        seek(0);
    }

    @Override
    public int read() throws IOException {
        if (pos < maxLength) {
            pos++;
            return is.read();
        }

        return -1;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        if (pos < maxLength) {
            int readCount = is.read(bytes);
            pos += readCount;
            return readCount;
        }

        return -1;
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }
}
