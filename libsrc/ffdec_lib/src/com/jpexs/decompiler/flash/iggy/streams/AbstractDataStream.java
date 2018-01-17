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
package com.jpexs.decompiler.flash.iggy.streams;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractDataStream implements DataStreamInterface {

    /**
     * Available bytes
     *
     * @return null if unknown, long value otherwise
     */
    @Override
    public abstract Long available();

    @Override
    public abstract long position();

    @Override
    public long readUI64() throws IOException {
        long lsb = readUI32();
        long msb = readUI32();
        long result = msb << 32 | lsb;
        return result & 0xffffffffffffffffL;
    }

    @Override
    public long readSI64() throws IOException {
        long lsb = readUI32();
        long msb = readUI32();
        return msb << 32 | lsb;
    }

    @Override
    public boolean writeUI64(long val) throws IOException {
        write((int) (val & 0xff));
        write((int) ((val >> 8) & 0xff));
        write((int) ((val >> 16) & 0xff));
        write((int) ((val >> 24) & 0xff));

        write((int) ((val >> 32) & 0xff));
        write((int) ((val >> 40) & 0xff));
        write((int) ((val >> 48) & 0xff));
        write((int) ((val >> 56) & 0xff));
        return true;
    }

    @Override
    public boolean writeSI64(long val) throws IOException {
        write((int) (val & 0xff));
        write((int) ((val >> 8) & 0xff));
        write((int) ((val >> 16) & 0xff));
        write((int) ((val >> 24) & 0xff));

        write((int) ((val >> 32) & 0xff));
        write((int) ((val >> 40) & 0xff));
        write((int) ((val >> 48) & 0xff));
        write((int) ((val >> 56) & 0xff));
        return true;
    }

    @Override
    public long readUI32() throws IOException {
        try {
            return (readUI8() | (readUI8() << 8) | (readUI8() << 16) | (readUI8() << 24));
        } catch (EOFException ex) {
            return -1;
        }
    }

    @Override
    public boolean writeUI32(long val) throws IOException {
        write((int) (val & 0xff));
        write((int) ((val >> 8) & 0xff));
        write((int) ((val >> 16) & 0xff));
        write((int) ((val >> 24) & 0xff));
        return true;
    }

    @Override
    public int readUI16() throws IOException {
        try {
            return (readUI8() | (readUI8() << 8)) & 0xffff;
        } catch (EOFException ex) {
            return -1;
        }
    }

    @Override
    public boolean writeUI16(int val) throws IOException {
        write(val & 0xff);
        write((val >> 8) & 0xff);
        return true;
    }

    @Override
    public int readUI8() throws IOException {
        try {
            return read() & 0xff;
        } catch (EOFException ex) {
            return -1;
        }
    }

    @Override
    public boolean writeUI8(int val) throws IOException {
        write(val);
        return true;
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat((int) readUI32());
    }

    @Override
    public boolean writeFloat(float val) throws IOException {
        return writeUI32(Float.floatToIntBits(val));
    }

    @Override
    public byte[] readBytes(int numBytes) throws IOException {
        byte[] ret = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            ret[i] = (byte) read();
        }
        return ret;
    }

    public byte[] getAllBytes() throws IOException {
        long oldPos = position();
        seek(0, SeekMode.SET);
        byte[] ret = readBytes((int) (long) available());
        seek(oldPos, SeekMode.SET);
        return ret;
    }

    @Override
    public void writeBytes(byte[] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            write(data[i] & 0xff);
        }
    }

    @Override
    public abstract int read() throws IOException;

    @Override
    public abstract void seek(long pos, SeekMode mode) throws IOException;

    @Override
    public void write(int val) throws IOException {
        //nothing
    }

    @Override
    public void close() {
        //nothing
    }

    @Override
    public int readUI8(long addr) throws IOException {
        long curPos = position();
        seek(addr, SeekMode.SET);
        int val = readUI8();
        seek(curPos, SeekMode.SET);
        return val;
    }

    @Override
    public boolean writeWChar(String name) throws IOException {
        for (int i = 0; i < name.length(); i++) {
            writeUI16(name.charAt(i));
        }
        writeUI16(0);
        return true;
    }

    @Override
    public String readWChar() throws IOException {
        StringBuilder strBuilder = new StringBuilder();
        do {
            char c = (char) readUI16();
            if (c == '\0') {
                break;
            }
            strBuilder.append(c);
        } while (true);
        return strBuilder.toString();
    }

    @Override
    public void pad8bytes() throws IOException {
        int pad8 = (int) (position() % 8);
        switch (pad8) {
            case 1:
                write(0);
            case 2:
                write(0);
            case 3:
                write(0);
            case 4:
                write(0);
            case 5:
                write(0);
            case 6:
                write(0);
            case 7:
                write(0);
        }
    }

    @Override
    public void setOlderOffsetToThisPos(long savedPos) throws IOException {
        long curPos = position();
        long actual = curPos - savedPos;

        seek(savedPos, SeekMode.SET);
        writeUI64(actual);
        seek(curPos, SeekMode.SET);
    }

    public void setOlderOffsetToThisPosCheck(long savedPos, long expected) throws IOException {
        if (expected == 1) {
            return;
        }
        long curPos = position();
        long actual = curPos - savedPos;
        if (actual != expected) {
            throw new RuntimeException("Expected " + expected + " but found actual " + actual + ". Diff:" + ((actual - expected) > 0 ? "+" : "") + (actual - expected));
        }
    }
}
