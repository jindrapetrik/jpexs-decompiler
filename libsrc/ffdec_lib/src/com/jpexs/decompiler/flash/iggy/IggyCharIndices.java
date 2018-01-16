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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharIndices implements StructureInterface {

    @IggyFieldType(value = DataType.wchar_t)
    List<Character> chars;
    @IggyFieldType(DataType.uint32_t)
    long padd;

    public List<Character> getChars() {
        return chars;
    }

    private long charCount;

    public IggyCharIndices(List<Character> chars) {
        this.chars = chars;
        this.charCount = chars.size();
        padd = 0;
    }

    public IggyCharIndices(ReadDataStreamInterface stream, long charCount) throws IOException {
        this.charCount = charCount;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        chars = new ArrayList<>();
        for (int i = 0; i < charCount; i++) {
            chars.add((char) stream.readUI16());
        }
        padd = stream.readUI32();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        for (int i = 0; i < chars.size(); i++) {
            stream.writeUI16(chars.get(i));
        }
        stream.writeUI32(padd);
    }

}
