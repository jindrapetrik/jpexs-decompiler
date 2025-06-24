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
package com.jpexs.decompiler.flash.xfl;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Bin data file output stream.
 * @author JPEXS
 */
public class BinDataOutputStream extends OutputStream {

    private final OutputStream os;

    public BinDataOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    public void write(int... values) throws IOException {
        for (int i : values) {
            os.write(i);
        }
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    public void writeUI16(int value) throws IOException {
        write(value & 0xFF);
        write((value >> 8) & 0xFF);
    }  

    public void writeUI32(long value) throws IOException {
        write((int) (value & 0xFF));
        write((int) ((value >> 8) & 0xFF));
        write((int) ((value >> 16) & 0xFF));
        write((int) ((value >> 24) & 0xFF));
    }

    public void writeUI64(long value) throws IOException {
        write((int) (value & 0xFF));
        write((int) ((value >> 8) & 0xFF));
        write((int) ((value >> 16) & 0xFF));
        write((int) ((value >> 24) & 0xFF));
        write((int) ((value >> 32) & 0xFF));
        write((int) ((value >> 40) & 0xFF));
        write((int) ((value >> 48) & 0xFF));
        write((int) ((value >> 56) & 0xFF));
    }

    public void writeFloat(float val) throws IOException {
        writeUI32(Float.floatToIntBits(val));
    }

    public void writeDouble(double val) throws IOException {
        writeUI64(Double.doubleToLongBits(val));
    }
}
