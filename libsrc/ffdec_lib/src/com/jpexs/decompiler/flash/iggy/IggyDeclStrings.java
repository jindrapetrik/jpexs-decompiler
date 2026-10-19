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
import java.io.IOException;

/**
 * One node of the AS3/ABC linked list. ABC includes its four version bytes.
 */
public class IggyDeclStrings implements StructureInterface {

    private long address;
    private long nextAddress;
    private byte[] data;
    private boolean is64 = true;

    public IggyDeclStrings(ReadDataStreamInterface stream) throws IOException {
        this(stream, true);
    }

    public IggyDeclStrings(ReadDataStreamInterface stream, boolean is64) throws IOException {
        this.is64 = is64;
        readFromDataStream(stream);
    }

    public IggyDeclStrings(byte[] abc) {
        data = abc.clone();
    }

    public byte[] getData() {
        return data.clone();
    }

    public void setData(byte[] abc) {
        data = abc.clone();
    }

    public long getAddress() {
        return address;
    }

    public long getNextAddress() {
        return nextAddress;
    }

    public void setNextAddress(long next) {
        nextAddress = next;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        address = stream.position();
        nextAddress = IggyLayout.readPointer(stream, is64);
        long size = stream.readUI32();
        if (size < 4 || size > Integer.MAX_VALUE || size > stream.available()) {
            throw new IOException("Invalid Iggy ABC length");
        }
        data = stream.readBytes((int) size);
        validate();
    }

    public void validate() throws IOException {
        if (data.length < 4) {
            throw new IOException("Truncated Iggy ABC header");
        }
        int minor = (data[0] & 255) | (data[1] & 255) << 8;
        int major = (data[2] & 255) | (data[3] & 255) << 8;
        if (major != 46 || minor != 16 && minor != 18) {
            throw new IOException("Unsupported Iggy ABC version: " + major + "." + minor);
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        validate();
        IggyLayout.writePointer(stream, is64, nextAddress);
        stream.writeUI32(data.length);
        stream.getIndexing().writeLengthCustom(is64 ? 12 : 8, new int[]{0, is64 ? 8 : 4}, new int[]{2, 5});
        stream.writeBytes(data);
        stream.getIndexing().writeLengthUI32(data.length);
        stream.pad8bytes();
        stream.getIndexing().writeLengthUI32((-stream.getIndexing().getPosition()) & 7);
    }
}
