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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IggyCharOffset implements StructureInterface {

    public static final int STRUCT_SIZE = 32;

    private static Logger LOGGER = Logger.getLogger(IggyCharOffset.class.getName());

    @IggyFieldType(DataType.uint64_t)
    long zero;
    @IggyFieldType(DataType.uint16_t)
    int ischar1;
    @IggyFieldType(DataType.uint16_t)
    int ischar2;
    @IggyFieldType(DataType.uint32_t)
    long zero2;
    @IggyFieldType(DataType.uint16_t)
    int xscale;
    @IggyFieldType(DataType.uint16_t)
    int yscale;
    @IggyFieldType(DataType.uint32_t)
    long zero3;
    @IggyFieldType(DataType.uint64_t)
    long offset;

    public IggyCharOffset(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    public IggyCharOffset(int ischar1, int ischar2, int xscale, int yscale) {
        this.zero = 0;
        this.ischar1 = ischar1;
        this.ischar2 = ischar2;
        this.zero2 = 0;
        this.xscale = xscale;
        this.yscale = yscale;
        this.zero3 = 0;
        this.offset = 0;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        zero = stream.readUI64();
        ischar1 = stream.readUI16();
        ischar2 = stream.readUI16();
        zero2 = stream.readUI32();
        xscale = stream.readUI16();
        yscale = stream.readUI16();
        zero3 = stream.readUI32();
        offset = stream.readUI64();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        stream.writeUI64(zero);
        stream.writeUI16(ischar1);
        stream.writeUI16(ischar2);
        stream.writeUI32(zero2);
        stream.writeUI16(xscale);
        stream.writeUI16(yscale);
        stream.writeUI32(zero3);
        stream.writeUI64(offset);
    }

    public boolean hasGlyph() {
        return offset > 0;
    }

    public long getZero() {
        return zero;
    }

    public boolean isChar1() {
        return ischar1 > 0;
    }

    public boolean isChar2() {
        return ischar2 > 0;
    }

    public long getZero2() {
        return zero2;
    }

    public int getXscale() {
        return xscale;
    }

    public int getYscale() {
        return yscale;
    }

    public long getZero3() {
        return zero3;
    }

}
