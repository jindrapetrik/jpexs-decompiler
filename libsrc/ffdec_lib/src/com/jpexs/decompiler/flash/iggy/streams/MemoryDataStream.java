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
package com.jpexs.decompiler.flash.iggy.streams;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Bounded input or growable output, without temporary files.
 */
public class MemoryDataStream extends AbstractDataStream {

    private byte[] data;
    private int length;
    private int cursor;
    private final boolean readOnly;
    private IggyIndexBuilder indexing;

    public MemoryDataStream() {
        data = new byte[256];
        readOnly = false;
    }

    public MemoryDataStream(byte[] bytes) {
        data = bytes.clone();
        length = bytes.length;
        readOnly = true;
    }

    @Override
    public Long available() {
        return (long) length - cursor;
    }

    @Override
    public Long totalSize() {
        return (long) length;
    }

    @Override
    public long position() {
        return cursor;
    }

    @Override
    public byte[] getAllBytes() {
        return Arrays.copyOf(data, length);
    }

    @Override
    public IggyIndexBuilder getIndexing() {
        return indexing;
    }

    @Override
    public void setIndexing(IggyIndexBuilder value) {
        indexing = value;
    }

    @Override
    public int read() throws IOException {
        if (cursor >= length) {
            throw new EOFException();
        }
        return data[cursor++] & 255;
    }

    @Override
    public byte[] readBytes(int count) throws IOException {
        if (count < 0 || count > length - cursor) {
            throw new EOFException("Invalid Iggy byte range");
        }
        byte[] result = Arrays.copyOfRange(data, cursor, cursor + count);
        cursor += count;
        return result;
    }

    @Override
    public void write(int value) throws IOException {
        if (readOnly) {
            throw new IOException("Read-only Iggy stream");
        }
        if (cursor == Integer.MAX_VALUE) {
            throw new IOException("Iggy stream too large");
        }
        if (cursor >= data.length) {
            data = Arrays.copyOf(data, (int) Math.min(Integer.MAX_VALUE, Math.max((long) cursor + 1, (long) data.length * 2)));
        }
        data[cursor++] = (byte) value;
        length = Math.max(length, cursor);
    }

    @Override
    public void seek(long offset, SeekMode mode) throws IOException {
        long target = mode == SeekMode.SET ? offset : mode == SeekMode.CUR ? cursor + offset : length - offset;
        if (target < 0 || target > Integer.MAX_VALUE || (readOnly && target > length)) {
            throw new IOException("Invalid Iggy seek: " + target);
        }
        if (!readOnly && target > length) {
            cursor = length;
            while (cursor < target) {
                write(0);
            }
        }
        cursor = (int) target;
    }

    @Override
    public void pad8bytes() throws IOException {
        if (readOnly) {
            seek((-cursor) & 7, SeekMode.CUR);
        } else {
            super.pad8bytes();
        }
    }
}
