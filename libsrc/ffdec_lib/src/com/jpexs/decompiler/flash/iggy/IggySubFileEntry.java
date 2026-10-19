/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.IOException;

/**
 * Sub file entry.
 *
 * @author JPEXS
 * <p>
 * Based of works of somebody called eternity.
 */
public class IggySubFileEntry implements StructureInterface {

    public static final int STRUCTURE_SIZE = 16;

    public static final int TYPE_INDEX = 0;
    public static final int TYPE_FLASH = 1;

    @SWFType(BasicType.UI32)
    long type;
    int copyFlag;
    int targetIndex;
    int metadata;

    public int getType() {
        return (int) type;
    }

    public int getCopyFlag() {
        return copyFlag;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public int getMetadata() {
        return metadata;
    }

    public long getSize() {
        return size;
    }

    public long getOffset() {
        return offset;
    }

    @SWFType(BasicType.UI32)
    long size;

    //Preserved descriptor word at +8; its semantics are not known.
    @SWFType(BasicType.UI32)
    long size2;

    //absolute offset
    @SWFType(BasicType.UI32)
    long offset;

    public IggySubFileEntry(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    public IggySubFileEntry(long type, long size, long size2, long offset) {
        this.type = type & 255;
        this.copyFlag = (int) (type >>> 8) & 255;
        this.targetIndex = (int) (type >>> 16) & 255;
        this.metadata = (int) (type >>> 24) & 255;
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
        type = stream.readUI8();
        copyFlag = stream.readUI8();
        targetIndex = stream.readUI8();
        metadata = stream.readUI8();
        size = stream.readUI32();
        size2 = stream.readUI32();
        offset = stream.readUI32();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        stream.writeUI8((int) type);
        stream.writeUI8(copyFlag);
        stream.writeUI8(targetIndex);
        stream.writeUI8(metadata);
        stream.writeUI32(size);
        stream.writeUI32(size2);
        stream.writeUI32(offset);
    }

}
