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

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 * Header.
 *
 * @author JPEXS
 * <p>
 * little endian all
 * <p>
 * Based of works of somebody called eternity.
 */
public class IggyHeader implements StructureInterface {

    public static int STRUCT_SIZE = 32;

    public static long MAGIC = 0xED0A6749L;

    //Must be 0xED0A6749
    @IggyFieldType(DataType.uint32_t)
    private long magic = MAGIC;

    //Assume 0x900
    @IggyFieldType(DataType.uint32_t)
    private long version;

    //Payload endianness: 1 little-endian, 2 big-endian
    @IggyFieldType(value = DataType.uint8_t)
    private int platform1;

    //32/64
    @IggyFieldType(value = DataType.uint8_t)
    private int platform2;

    //Bitfield packing: 1 native, 2 reversed
    @IggyFieldType(value = DataType.uint8_t)
    private int platform3;

    //Fixup availability: bit 0 endian, bit 1 bitfield packing
    @IggyFieldType(value = DataType.uint8_t)
    private int platform4;

    //Original SWF version; read by the x64 1.2.56 runtime. Zero means unknown.
    @IggyFieldType(DataType.uint32_t)
    private long swfVersion;

    @IggyArrayFieldType(value = DataType.uint8_t, count = 12)
    private byte[] reserved;

    @IggyFieldType(value = DataType.uint32_t)
    private long numSubfiles;

    // 32 legacy files also store the descriptor table in payload byte order.
    // x86 version 1.2.56 and x64 always keep the outer table little-endian.
    private boolean legacy32Table = true;

    public IggyHeader(ReadDataStreamInterface stream) throws IOException {
        this(stream, true);
    }

    IggyHeader(ReadDataStreamInterface stream, boolean legacy32Table) throws IOException {
        this.legacy32Table = legacy32Table;
        readFromDataStream(stream);
    }

    /**
     * Constructor.
     *
     * @param version Version
     * @param platform1 Platform 1
     * @param platform2 Platform 2 (32/64)
     * @param platform3 Platform 3
     * @param platform4 Platform 4
     * @param swfVersion Original SWF version, or zero if unknown
     * @param reserved Reserved
     * @param num_subfiles Number of subfiles
     */
    public IggyHeader(long version, int platform1, int platform2, int platform3, int platform4, long swfVersion, byte[] reserved, long num_subfiles) {
        this.version = version;
        this.platform1 = platform1;
        this.platform2 = platform2;
        this.platform3 = platform3;
        this.platform4 = platform4;
        this.swfVersion = swfVersion;
        this.reserved = reserved;
        this.numSubfiles = num_subfiles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("version: ").append(version).append(", ");
        sb.append("platform: ").append(platform1).append(" ").append(platform2).append(" ").append(platform3).append(" ").append(platform4).append(", ");
        sb.append("swfVersion: ").append(swfVersion).append(", ");
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
        if (version != 0x900) {
            throw new IOException("Unsupported Iggy version: " + version);
        }
        platform1 = stream.readUI8();
        platform2 = stream.readUI8(); //32/64
        platform3 = stream.readUI8();
        platform4 = stream.readUI8();
        if (platform2 != 32 && platform2 != 64) {
            throw new IOException("Invalid Iggy pointer width");
        }
        if (platform1 != 1 && platform1 != 2 || platform3 != 1 && platform3 != 2) {
            throw new IOException("Invalid Iggy data platform");
        }
        if (is64()) {
            requirePlatformFixups();
        }
        swfVersion = stream.readUI32();
        reserved = stream.readBytes(12);
        numSubfiles = stream.readUI32();
        if (legacy32Table && !is64() && platform1 == 2) {
            numSubfiles = Integer.reverseBytes((int) numSubfiles) & 0xffffffffL;
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        s.writeUI32(magic);
        s.writeUI32(version);
        s.writeUI8(platform1);
        s.writeUI8(platform2);
        s.writeUI8(platform3);
        s.writeUI8(platform4);
        s.writeUI32(swfVersion);
        s.writeBytes(reserved);
        s.writeUI32(legacy32Table && !is64() && platform1 == 2 ? Integer.reverseBytes((int) numSubfiles) & 0xffffffffL : numSubfiles);
    }

    /**
     * Validate the platform flags used by the 32-bit and 64-bit readers.
     */
    void requirePlatformFixups() throws IOException {
        if (platform1 != 1 && (platform4 & 1) == 0 || platform3 != 1 && (platform4 & 2) == 0) {
            throw new IOException("Iggy platform has no required fixup data");
        }
    }

    public boolean is64() {
        return platform2 == 64;
    }

    public void setNumSubfiles(long count) {
        numSubfiles = count;
    }

    public void setNativePayload() {
        platform1 = 1;
        platform3 = 1;
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

    /**
     * Get the original SWF version stored at header offset 0x0C.
     *
     * @return The preserved unsigned word; zero means unknown
     */
    public long getSwfVersion() {
        return swfVersion;
    }

    /**
     * Set the original SWF version, or zero for an unknown version.
     *
     * @param swfVersion Original SWF version
     */
    public void setSwfVersion(long swfVersion) {
        if (swfVersion < 0 || swfVersion > 0xffffffffL) {
            throw new IllegalArgumentException("Iggy SWF version must fit an unsigned 32-bit word");
        }
        this.swfVersion = swfVersion;
    }

    /**
     * Get the preserved word at offset 0x0C.
     *
     * @return Original SWF version
     * @deprecated Use {@link #getSwfVersion()}
     */
    @Deprecated
    public long getUnk_0C() {
        return getSwfVersion();
    }

    public byte[] getReserved() {
        return reserved;
    }

    public long getNumSubfiles() {
        return numSubfiles;
    }

}
