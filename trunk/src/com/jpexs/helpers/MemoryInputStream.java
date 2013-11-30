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

    byte[] buffer;
    long pos = 0;
    int count = 0;

    public MemoryInputStream(byte[] buffer) {
        this.buffer = buffer;
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
    public int read() throws IOException {
        if (pos > count) {
            count = (int) pos;
        }
        
        if (pos < buffer.length) {
            int ret = buffer[(int) pos] & 0xff;
            pos++;
            return ret;
        }

        return -1;
    }

    @Override
    public int available() throws IOException {
        return buffer.length - (int) pos;
    }

    public long length() throws IOException {
        return buffer.length;
    }
}
