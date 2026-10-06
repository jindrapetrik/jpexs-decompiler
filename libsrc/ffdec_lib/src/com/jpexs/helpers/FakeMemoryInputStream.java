/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import java.io.ByteArrayOutputStream;
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

    private final InputStream is;

    private final ByteArrayOutputStream readBuffer = new ByteArrayOutputStream();

    private byte[] bufferedBytes;

    private int count;

    public FakeMemoryInputStream(InputStream is) throws IOException {
        super(new byte[0]);
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
        if (pos <= count) {
            this.pos = pos;
            return;
        }
        this.pos = count;
        byte[] skipBuffer = new byte[8192];
        while (this.pos < pos) {
            int toRead = (int) Math.min(skipBuffer.length, pos - this.pos);
            int readCount = read(skipBuffer, 0, toRead);
            if (readCount < 0) {
                throw new IOException("Seek beyond end of stream");
            }
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        seek(0);
    }

    @Override
    public int read() throws IOException {
        if (pos < count) {
            if (bufferedBytes == null) {
                bufferedBytes = readBuffer.toByteArray();
            }
            return bufferedBytes[(int) pos++] & 0xff;
        }
        int value = is.read();
        if (value >= 0) {
            readBuffer.write(value);
            bufferedBytes = null;
            count++;
            pos++;
        }
        return value;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || length < 0 || length > bytes.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }

        int totalRead = 0;
        if (pos < count) {
            if (bufferedBytes == null) {
                bufferedBytes = readBuffer.toByteArray();
            }
            int bufferedCount = (int) Math.min(length, count - pos);
            System.arraycopy(bufferedBytes, (int) pos, bytes, offset, bufferedCount);
            pos += bufferedCount;
            totalRead += bufferedCount;
        }

        if (totalRead < length && pos == count) {
            int readCount = is.read(bytes, offset + totalRead, length - totalRead);
            if (readCount > 0) {
                readBuffer.write(bytes, offset + totalRead, readCount);
                bufferedBytes = null;
                count += readCount;
                pos += readCount;
                totalRead += readCount;
            }
        }

        return totalRead == 0 ? -1 : totalRead;
    }

    @Override
    public int available() throws IOException {
        long available = count - pos + (long) is.available();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, available));
    }
}
