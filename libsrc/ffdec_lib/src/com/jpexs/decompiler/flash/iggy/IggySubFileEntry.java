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

import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.IOException;

/**
 *
 * @author JPEXS
 *
 * Based of works of somebody called eternity.
 */
public class IggySubFileEntry implements StructureInterface {

    public static final int STRUCTURE_SIZE = 16;

    public static final int TYPE_INDEX = 0;
    public static final int TYPE_FLASH = 1;

    @SWFType(BasicType.UI32)
    long type;

    @SWFType(BasicType.UI32)
    long size;

    //apparently same as size, maybe (un)compressed (?)
    @SWFType(BasicType.UI32)
    long size2;

    //absolute offset
    @SWFType(BasicType.UI32)
    long offset;

    public IggySubFileEntry(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    public IggySubFileEntry(long type, long size, long size2, long offset) {
        this.type = type;
        this.size = size;
        this.size2 = size2;
        this.offset = offset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("id: ").append(type).append(", ");
        sb.append("size: ").append(size).append(", ");
        sb.append("size2: ").append(size2).append(", ");
        sb.append("offset: ").append(offset);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        type = stream.readUI32();
        size = stream.readUI32();
        size2 = stream.readUI32();
        offset = stream.readUI32();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        stream.writeUI32(type);
        stream.writeUI32(size);
        stream.writeUI32(size2);
        stream.writeUI32(offset);
    }

}
