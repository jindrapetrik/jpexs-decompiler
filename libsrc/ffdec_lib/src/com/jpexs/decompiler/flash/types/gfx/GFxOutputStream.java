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

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author JPEXS
 */
public class GFxOutputStream extends OutputStream {

    public static final int MaxUInt6 = (1 << 6) - 1;

    public static final int MaxUInt7 = (1 << 7) - 1;

    public static final int MaxUInt14 = (1 << 14) - 1;

    public static final int MaxUInt22 = (1 << 22) - 1;

    public static final int MaxUInt30 = (1 << 30) - 1;

    public static final int MinSInt5 = -(1 << 4);

    public static final int MaxSInt5 = (1 << 4) - 1;

    public static final int MinSInt6 = -(1 << 5);

    public static final int MaxSInt6 = (1 << 5) - 1;

    public static final int MinSInt7 = -(1 << 6);

    public static final int MaxSInt7 = (1 << 6) - 1;

    public static final int MinSInt8 = -(1 << 7);

    public static final int MaxSInt8 = (1 << 7) - 1;

    public static final int MinSInt9 = -(1 << 8);

    public static final int MaxSInt9 = (1 << 8) - 1;

    public static final int MinSInt10 = -(1 << 9);

    public static final int MaxSInt10 = (1 << 9) - 1;

    public static final int MinSInt11 = -(1 << 10);

    public static final int MaxSInt11 = (1 << 10) - 1;

    public static final int MinSInt12 = -(1 << 11);

    public static final int MaxSInt12 = (1 << 11) - 1;

    public static final int MinSInt13 = -(1 << 12);

    public static final int MaxSInt13 = (1 << 12) - 1;

    public static final int MinSInt14 = -(1 << 13);

    public static final int MaxSInt14 = (1 << 13) - 1;

    public static final int MinSInt15 = -(1 << 14);

    public static final int MaxSInt15 = (1 << 14) - 1;

    public static final int MinSInt17 = -(1 << 16);

    public static final int MaxSInt17 = (1 << 16) - 1;

    public static final int MinSInt18 = -(1 << 17);

    public static final int MaxSInt18 = (1 << 17) - 1;

    public static final int MinSInt19 = -(1 << 18);

    public static final int MaxSInt19 = (1 << 18) - 1;

    public static final int MinSInt20 = -(1 << 19);

    public static final int MaxSInt20 = (1 << 19) - 1;

    public static final int MinSInt22 = -(1 << 21);

    public static final int MaxSInt22 = (1 << 21) - 1;

    private final OutputStream os;

    private long pos = 0;

    public GFxOutputStream(OutputStream os) {
        this.os = os;
    }

    public long getPos() {
        return pos;
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
        pos++;
    }

    /**
     * Writes SI32 (Signed 32bit integer) value to the stream
     *
     * @param value SI32 value
     * @throws IOException
     */
    public void writeSI32(long value) throws IOException {
        writeUI32(value);
    }

    /**
     * Writes SI16 (Signed 16bit integer) value to the stream
     *
     * @param value SI16 value
     * @throws IOException
     */
    public void writeSI16(int value) throws IOException {
        writeUI16(value);
    }

    /**
     * Writes SI8 (Signed 8bit integer) value to the stream
     *
     * @param value SI8 value
     * @throws IOException
     */
    public void writeSI8(int value) throws IOException {
        writeUI8(value);
    }

    /**
     * Writes UI32 (Unsigned 32bit integer) value to the stream
     *
     * @param value UI32 value
     * @throws IOException
     */
    public void writeUI32(long value) throws IOException {
        write((int) (value & 0xff));
        write((int) ((value >> 8) & 0xff));
        write((int) ((value >> 16) & 0xff));
        write((int) ((value >> 24) & 0xff));
    }

    /**
     * Writes UI16 (Unsigned 16bit integer) value to the stream
     *
     * @param value UI16 value
     * @throws IOException
     */
    public void writeUI16(int value) throws IOException {
        write((int) (value & 0xff));
        write((int) ((value >> 8) & 0xff));
    }

    /**
     * Writes UI8 (Unsigned 8bit integer) value to the stream
     *
     * @param val UI8 value to write
     * @throws IOException
     */
    public void writeUI8(int val) throws IOException {
        write(val & 0xff);
    }

    /**
     * Writes FLOAT (single precision floating point value) value to the stream
     *
     * @param value FLOAT value
     * @throws IOException
     */
    public void writeFLOAT(float value) throws IOException {
        writeUI32(Float.floatToIntBits(value));
    }

    public void writeSI15(int v) throws IOException {
        if (v >= MinSInt7 && v <= MaxSInt7) {
            writeUI8(v << 1);
            return;
        }
        writeUI8((v << 1) | 1);
        writeUI8(v >> 7);
    }

    public void writeUI15(int v) throws IOException {
        if (v <= MaxUInt7) {
            writeUI8(v << 1);
            return;
        }
        writeUI8((v << 1) | 1);
        writeUI8(v >> 7);
    }

    public void writeUI30(int v) throws IOException {
        if (v <= MaxUInt6) {
            writeUI8(v << 2);
            return;
        }
        if (v <= MaxUInt14) {
            writeUI8((v << 2) | 1);
            writeUI8(v >> 6);
            return;
        }
        if (v <= MaxUInt22) {
            writeUI8((v << 2) | 2);
            writeUI8(v >> 6);
            writeUI8(v >> 14);
            return;
        }
        writeUI8((v << 2) | 3);
        writeUI8(v >> 6);
        writeUI8(v >> 14);
        writeUI8(v >> 22);
    }
}
