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
package com.jpexs.helpers;

import com.jpexs.helpers.streams.SeekableInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class ReReadableInputStream extends SeekableInputStream {

    InputStream is;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] converted;

    long pos = 0;

    int count = 0;

    public int getCount() {
        return count;
    }

    public byte[] getAllRead() {
        return baos.toByteArray();
    }

    public long getPos() {
        return pos;
    }

    public ReReadableInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (pos > count) {
            this.pos = count;
            skip(pos - count);
        }
        this.pos = pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        seek(0);
    }

    @Override
    public int read() throws IOException {
        if (pos < count) {
            if (converted == null) {
                converted = baos.toByteArray();
            }
            int ret = converted[(int) pos] & 0xff;
            pos++;
            return ret;
        }
        int i = is.read();
        if (i > -1) {
            baos.write(i);
            count++;
        }
        pos++;
        converted = null;

        return i;
    }

    @Override
    public int available() throws IOException {
        return (count + is.available()) - (int) pos;
    }

    public long length() throws IOException {
        return count + is.available();
    }
}
