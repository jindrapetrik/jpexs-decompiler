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
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class GFxInputStream {

    private final MemoryInputStream is;

    private static final int MaxUInt7 = (1 << 7) - 1;

    public DumpInfo dumpInfo;

    public GFxInputStream(MemoryInputStream is) {
        this.is = is;
    }

    public DumpInfo newDumpLevel(String name, String type) {
        if (dumpInfo != null) {
            long startByte = is.getPos();
            DumpInfo di = new DumpInfo(name, type, null, startByte, 0, 0, 0);
            di.parent = dumpInfo;
            dumpInfo.getChildInfos().add(di);
            dumpInfo = di;
        }

        return dumpInfo;
    }

    public void endDumpLevel() {
        endDumpLevel(null);
    }

    public void endDumpLevel(Object value) {
        if (dumpInfo != null) {
            dumpInfo.lengthBytes = is.getPos() - dumpInfo.startByte;
            dumpInfo.previewValue = value;
            dumpInfo = dumpInfo.parent;
        }
    }

    public int available() throws IOException {
        return is.available();
    }

    public void setPos(long pos) throws IOException {
        is.seek(pos);
    }

    public long getPos() {
        return is.getPos();
    }

    private int read() throws IOException {
        return is.read();
    }

    public int readUI8(String name) throws IOException {
        newDumpLevel(name, "UI8");
        int ret = read();
        endDumpLevel(ret);
        return ret;
    }

    public int readUI16(String name) throws IOException {
        newDumpLevel(name, "UI8");
        int ret = read() + (read() << 8);
        endDumpLevel(ret);
        return ret;
    }

    /**
     * Reads one SI16 (Signed 16bit integer) value from the stream
     *
     * @param name
     * @return SI16 value
     * @throws IOException
     */
    public int readSI16(String name) throws IOException {
        newDumpLevel(name, "SI16");
        int uval = read() + (read() << 8);
        if (uval >= 0x8000) {
            uval = -(((~uval) & 0xffff) + 1);
        }
        endDumpLevel(uval);
        return uval;
    }

    private long readUI32Internal() throws IOException {
        long ret = (read() + (read() << 8) + (read() << 16) + (read() << 24)) & 0xffffffff;
        return ret;
    }

    public long readUI32(String name) throws IOException {
        newDumpLevel(name, "UI32");
        long ret = readUI32Internal();
        endDumpLevel(ret);
        return ret;
    }

    public long readUI30(String name) throws IOException {
        newDumpLevel(name, "UI30");
        long v;
        int tb = read();
        long t = tb;
        switch (tb & 3) {
            case 0:
                v = t >> 2;
                endDumpLevel(v);
                return v;

            case 1:
                t >>= 2;
                v = t | (read() << 6);
                endDumpLevel(v);
                return v;
            case 2:
                t >>= 2;
                t |= (read() << 6);
                v = t | (read() << 14);
                endDumpLevel(v);
                return v;
        }
        t >>= 2;
        t |= (read() << 6);
        t |= (read() << 14);
        v = t | (read() << 22);
        endDumpLevel(v);
        return v;
    }

    public float readFLOAT(String name) throws IOException {
        newDumpLevel(name, "UI32");
        int val = (int) readUI32Internal();
        float ret = Float.intBitsToFloat(val);
        endDumpLevel(ret);
        return ret;
    }

    public int readSI8(String name) throws IOException {
        newDumpLevel(name, "SI8");
        int uval = readSI8Internal();
        endDumpLevel(uval);
        return uval;
    }

    private int readSI8Internal() throws IOException {
        int uval = read();
        if (uval >= 0x80) {
            uval = -(((~uval) & 0xff) + 1);
        }
        return uval;
    }

    public int readSI15(String name) throws IOException {
        newDumpLevel(name, "SI15");
        int t = readSI8Internal();
        int v;
        if ((t & 1) == 0) {
            v = t >> 1;
            endDumpLevel(v);
            return v;
        }
        t = ((t >> 1) & MaxUInt7);
        v = (t | (readSI8Internal() << 7));
        endDumpLevel(v);
        return v;
    }

    public int readUI15(String name) throws IOException {
        newDumpLevel(name, "UI15");
        int t = read();
        int v;
        if ((t & 1) == 0) {
            v = t >> 1;
            endDumpLevel(v);
            return v;
        }
        t = (t >> 1);
        v = (t | (read() << 7));
        endDumpLevel(v);
        return v;
    }

    /**
     * Reads bytes from the stream
     *
     * @param count Number of bytes to read
     * @param name
     * @return Array of read bytes
     * @throws IOException
     */
    public byte[] readBytes(long count, String name) throws IOException {
        if (count <= 0) {
            return SWFInputStream.BYTE_ARRAY_EMPTY;
        }
        newDumpLevel(name, "bytes");
        byte[] ret = new byte[(int) count];
        for (int i = 0; i < count; i++) {
            ret[i] = (byte) read();
        }
        endDumpLevel();
        return ret;
    }

    void read(byte[] bytes) throws IOException {
        is.read(bytes);
    }

    /**
     * Reads one string value from the stream
     *
     * @param name
     * @return String value
     * @throws IOException
     */
    public String readString(String name) throws IOException {
        newDumpLevel(name, "string");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int r;
        while (true) {
            r = read();
            if (r == 0) {
                String res = new String(baos.toByteArray(), Utf8Helper.charset);
                endDumpLevel(res);
                return res;
            }
            baos.write(r);
        }
    }
}
