/*
 *  Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.helpers.ReReadableInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class GFxInputStream extends InputStream {

    private ReReadableInputStream is;
    private static final int MaxUInt7 = (1 << 7) - 1;

    public GFxInputStream(InputStream is) {
        this.is = new ReReadableInputStream(is);
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    public void setPos(long pos) throws IOException {
        is.seek(pos);
    }

    public long getPos() {
        return is.getPos();
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    public int readUI8() throws IOException {
        return read();
    }

    public int readUI16() throws IOException {
        return read() + (read() << 8);
    }

    /**
     * Reads one SI16 (Signed 16bit integer) value from the stream
     *
     * @return SI16 value
     * @throws IOException
     */
    public int readSI16() throws IOException {
        int uval = read() + (read() << 8);
        if (uval >= 0x8000) {
            return -(((~uval) & 0xffff) + 1);
        } else {
            return uval;
        }
    }

    public long readUI32() throws IOException {
        return (read() + (read() << 8) + (read() << 16) + (read() << 24)) & 0xffffffff;
    }

    public long readUI30() throws IOException {
        long v;
        int tb = readUI8();
        long t = tb;
        switch (tb & 3) {
            case 0:
                v = t >> 2;
                return v;

            case 1:
                t >>= 2;
                v = t | (readUI8() << 6);
                return v;
            case 2:
                t >>= 2;
                t |= (readUI8() << 6);
                v = t | (readUI8() << 14);
                return v;
        }
        t >>= 2;
        t |= (readUI8() << 6);
        t |= (readUI8() << 14);
        v = t | (readUI8() << 22);
        return v;
    }

    public float readFLOAT() throws IOException {
        int val = (int) readUI32();
        float ret = Float.intBitsToFloat(val);
        return ret;
    }

    public int readSI8() throws IOException {
        int uval = read();
        if (uval >= 0x80) {
            return -(((~uval) & 0xff) + 1);
        } else {
            return uval;
        }
    }

    public int readSI15() throws IOException {
        int t = readSI8();
        int v;
        if ((t & 1) == 0) {
            v = t >> 1;
            return v;
        }
        t = ((t >> 1) & MaxUInt7);
        v = (t | (readSI8() << 7));
        return v;
    }

    public int readUI15() throws IOException {
        int t = readUI8();
        int v;
        if ((t & 1) == 0) {
            v = t >> 1;
            return v;
        }
        t = (t >> 1);
        v = (t | (readUI8() << 7));
        return v;
    }

    /**
     * Reads bytes from the stream
     *
     * @param count Number of bytes to read
     * @return Array of read bytes
     * @throws IOException
     */
    public byte[] readBytes(long count) throws IOException {
        if (count <= 0) {
            return new byte[0];
        }
        byte[] ret = new byte[(int) count];
        for (int i = 0; i < count; i++) {
            ret[i] = (byte) read();
        }
        return ret;
    }
}
