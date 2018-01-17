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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharAdvances implements StructureInterface {

    List<Float> advances;
    private long charCount;

    public List<Float> getScales() {
        return advances;
    }

    public IggyCharAdvances(List<Float> advances) {
        this.advances = advances;
        this.charCount = advances.size();
    }

    public IggyCharAdvances(ReadDataStreamInterface stream, long charCount) throws IOException {
        this.charCount = charCount;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        advances = new ArrayList<>();
        for (int i = 0; i < charCount; i++) {
            advances.add(stream.readFloat());
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        for (int i = 0; i < advances.size(); i++) {
            stream.writeFloat(advances.get(i));
        }
    }
}
