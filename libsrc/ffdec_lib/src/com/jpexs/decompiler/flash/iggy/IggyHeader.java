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

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 *
 * little endian all
 *
 * Based of works of somebody called eternity.
 */
public class IggyHeader implements StructureInterface {

    public static int STRUCT_SIZE = 32;

    public static long MAGIC = 0xED0A6749;

    //Must be 0xED0A6749
    @IggyFieldType(DataType.uint32_t)
    private long magic = MAGIC;

    //Assume 0x900
    @IggyFieldType(DataType.uint32_t)
    private long version;

    //Assuming: 1
    @IggyFieldType(value = DataType.uint8_t)
    private int platform1;

    //32/64
    @IggyFieldType(value = DataType.uint8_t)
    private int platform2;

    //Assuming: 1
    @IggyFieldType(value = DataType.uint8_t)
    private int platform3;

    //Usually: 3
    @IggyFieldType(value = DataType.uint8_t)
    private int platform4;

    //flags for platform 64?
    @IggyFieldType(DataType.uint32_t)
    private long unk_0C;

    @IggyArrayFieldType(value = DataType.uint8_t, count = 12)
    private byte[] reserved;

    @IggyFieldType(value = DataType.uint32_t)
    private long numSubfiles;

    public IggyHeader(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    /**
     *
     * @param version
     * @param platform1
     * @param platform2 32/64
     * @param platform3
     * @param platform4
     * @param unk_0C
     * @param reserved
     * @param num_subfiles
     */
    public IggyHeader(long version, int platform1, int platform2, int platform3, int platform4, long unk_0C, byte[] reserved, long num_subfiles) {
        this.version = version;
        this.platform1 = platform1;
        this.platform2 = platform2;
        this.platform3 = platform3;
        this.platform4 = platform4;
        this.unk_0C = unk_0C;
        this.reserved = reserved;
        this.numSubfiles = num_subfiles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("version: ").append(version).append(", ");
        sb.append("platform: ").append(platform1).append(" ").append(platform2).append(" ").append(platform3).append(" ").append(platform4).append(", ");
        sb.append("unk_0C: ").append(String.format("%08X", unk_0C)).append(", ");
        sb.append("reserved: 12 bytes").append(", ");
        sb.append("num_subfiles: ").append(numSubfiles);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        magic = stream.readUI32();
        if (magic != IggyHeader.MAGIC) {
            throw new IOException("Invalid Iggy file");
        }
        version = stream.readUI32();
        platform1 = stream.readUI8();
        platform2 = stream.readUI8(); //32/64
        platform3 = stream.readUI8();
        platform4 = stream.readUI8();
        unk_0C = stream.readUI32();
        reserved = stream.readBytes(12);
        numSubfiles = stream.readUI32();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        s.writeUI32(magic);
        s.writeUI32(version);
        s.writeUI8(platform1);
        s.writeUI8(platform2);
        s.writeUI8(platform3);
        s.writeUI8(platform4);
        s.writeUI32(unk_0C);
        s.writeBytes(reserved);
        s.writeUI32(numSubfiles);
    }

    public boolean is64() {
        return platform2 == 64;
    }

    public long getMagic() {
        return magic;
    }

    public long getVersion() {
        return version;
    }

    public int getPlatform1() {
        return platform1;
    }

    public int getPlatform2() {
        return platform2;
    }

    public int getPlatform3() {
        return platform3;
    }

    public int getPlatform4() {
        return platform4;
    }

    public long getUnk_0C() {
        return unk_0C;
    }

    public byte[] getReserved() {
        return reserved;
    }

    public long getNumSubfiles() {
        return numSubfiles;
    }

}
