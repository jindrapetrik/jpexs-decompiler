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
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class RawIggyPart extends IggyTag {

    byte[] rawData;
    int tagType;
    private int length;

    public RawIggyPart(int tagType, ReadDataStreamInterface stream, int length) throws IOException {
        this.length = length;
        this.tagType = tagType;
        readFromDataStream(stream);
    }

    @Override
    public int getTagType() {
        return tagType;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        rawData = stream.readBytes(length);
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        stream.writeBytes(rawData);
    }

}
