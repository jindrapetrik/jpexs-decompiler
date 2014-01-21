/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.helpers;

import com.jpexs.helpers.streams.SeekableInputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class MemoryInputStream extends SeekableInputStream {

    private final byte[] buffer;
    private long pos = 0;
    private int count = 0;
    private int startPos = 0;
    private int maxLength = -1;

    public MemoryInputStream(byte[] buffer) {
        this.buffer = buffer;
    }

    public MemoryInputStream(byte[] buffer, int startPos) throws IOException {
        this.buffer = buffer;
        if (startPos >= buffer.length) {
            throw new IOException("Invalid startPos");
        }
        this.startPos = startPos;
    }

    public MemoryInputStream(byte[] buffer, int startPos, int maxLength) throws IOException {
        this.buffer = buffer;
        this.startPos = startPos;
        if (startPos >= buffer.length) {
            throw new IOException("Invalid startPos");
        }
        this.maxLength = maxLength;
        if (startPos + maxLength >= buffer.length) {
            throw new IOException("Invalid maxLength");
        }
    }

    public int getCount() {
        return count;
    }

    public byte[] getAllRead() {
        return buffer;
    }

    public long getPos() {
        return pos;
    }

    @Override
    public void seek(long pos) throws IOException {
        this.pos = pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        seek(0);
    }
    
    @Override
    public int read() throws IOException {
        if (pos > count) {
            count = (int) pos;
        }
        
        if (pos < getLength()) {
            int ret = buffer[(int) pos + startPos] & 0xff;
            pos++;
            return ret;
        }

        return -1;
    }
    
    private int getLength() {
        if (maxLength == -1) {
            return buffer.length - startPos;
        }
        return maxLength;
    }

    @Override
    public int available() throws IOException {
        return buffer.length - (int) pos;
    }

    public long length() throws IOException {
        return buffer.length;
    }
}
