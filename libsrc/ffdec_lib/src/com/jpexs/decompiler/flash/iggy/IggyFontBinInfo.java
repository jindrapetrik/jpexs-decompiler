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

/**
 *
 * @author JPEXS
 */
public class IggyFontBinInfo implements StructureInterface {

    public static final int STRUCT_SIZE = 96;

    @IggyFieldType(DataType.uint64_t)
    long size_of_this_info = STRUCT_SIZE;

    @IggyFieldType(value = DataType.uint16_t, count = 4)
    int font_specific[];

    @IggyFieldType(DataType.float_t)
    float normX;

    @IggyFieldType(DataType.float_t)
    float zero;

    @IggyFieldType(DataType.float_t)
    float zero2;

    @IggyFieldType(DataType.float_t)
    float normY;

    @IggyFieldType(DataType.float_t)
    float minSize;

    @IggyFieldType(DataType.float_t)
    float maxSize;

    @IggyFieldType(DataType.uint64_t)
    long order_in_iggy_file;

    @IggyFieldType(DataType.int64_t)
    long address_back; //relative

    @IggyFieldType(value = DataType.uint8_t, count = 40)
    byte pad[];

    public IggyFontBinInfo(ReadDataStreamInterface s) throws IOException {
        readFromDataStream(s);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        size_of_this_info = s.readUI64();
        if (size_of_this_info != 96) {
            throw new IOException(String.format("Wrong iggy font format (bininfo)!"));
        }
        font_specific = new int[4];
        for (int i = 0; i < font_specific.length; i++) {
            font_specific[i] = s.readUI16();
        }
        normX = s.readFloat();
        zero = s.readFloat();
        zero2 = s.readFloat();
        normY = s.readFloat();
        minSize = s.readFloat();
        maxSize = s.readFloat();
        order_in_iggy_file = s.readUI64();
        address_back = s.readSI64();
//if(address_back + s.position() -  8 != text_offsets[i]) Printf("Wrong iggy font format (bininfo-offsetback) (%u)!\n",i);
        pad = s.readBytes(40);
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        s.writeUI64(size_of_this_info);
        for (int i = 0; i < font_specific.length; i++) {
            s.writeUI16(font_specific[i]);
        }
        s.writeFloat(normX);
        s.writeFloat(zero);
        s.writeFloat(zero2);
        s.writeFloat(normY);
        s.writeFloat(minSize);
        s.writeFloat(maxSize);
        s.writeUI64(order_in_iggy_file);
        s.writeSI64(address_back);
        s.writeBytes(pad);
    }
}
