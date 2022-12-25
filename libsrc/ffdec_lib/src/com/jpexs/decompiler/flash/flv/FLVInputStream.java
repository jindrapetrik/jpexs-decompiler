/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.flv;

import com.jpexs.decompiler.flash.EndOfStreamException;
import com.jpexs.helpers.Reference;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FLVInputStream {
    private final DataInputStream is;
    
    private int bitPos = 0;

    private int tempByte = 0;
    
    private long pos = 0;
    
    public FLVInputStream(InputStream is) {
        this.is = new DataInputStream(is);
    }
    
    public void readHeader(Reference<Boolean> audioPresent, Reference<Boolean> videoPresent) throws IOException {
        byte signature[] = new byte[3];
        is.readFully(signature);
        if (signature[0] != 'F' || signature[1] != 'L' || signature[2] != 'V') {
            throw new IOException("Invalid FLV file - invalid signature");
        }
        pos+=3;
        int version = read();
        if (version != 1) {
            throw new IOException("Unsupported FLV version: "+version+", only 1 is supported");
        }        
        int reserved = (int)readUB(5);
        audioPresent.setVal(readUB(1)==1L);
        readUB(1);
        videoPresent.setVal(readUB(1) == 1L);
        long headerSize = readUI32();
        if (headerSize != 9) {
            throw new IOException("Invalid header size: "+headerSize);
        }
        readUI32(); //should be 0                
    }
    
    public List<FLVTAG> readTags() throws IOException {
        List<FLVTAG> ret = new ArrayList<>();
        while(available() > 0) {
            ret.add(readTag());
        }
        return ret;
    }
    
    private long readUI32() throws IOException {
        return ((readEx() << 24) + (readEx() << 16) + (readEx() << 8) + (readEx())) & 0xffffffffL;
    }
    
    /**
     * Reads one byte from the stream
     *
     * @return byte
     * @throws IOException
     */
    private int readEx() throws IOException {
        bitPos = 0;
        return readNoBitReset();
    }
    
    /**
     * Reads UB[nBits] (Unsigned-bit value) value from the stream
     *
     * @param nBits Number of bits which represent value
     * @return Unsigned value
     * @throws IOException
     */
    public long readUB(int nBits) throws IOException {
        if (nBits == 0) {
            return 0;
        }
        long ret = readUBInternal(nBits);
        return ret;
    }
    
    private int readNoBitReset() throws IOException, EndOfStreamException {
        int r = is.read();
        if (r == -1) {
            throw new EndOfStreamException();
        }

        pos++;
        return r;
    }

    /**
     * Reads UB[nBits] (Unsigned-bit value) value from the stream
     *
     * @param nBits Number of bits which represent value
     * @return Unsigned value
     * @throws IOException
     */
    private long readUBInternal(int nBits) throws IOException {
        if (nBits == 0) {
            return 0;
        }
        long ret = 0;
        if (bitPos == 0) {
            tempByte = readNoBitReset();
        }
        for (int bit = 0; bit < nBits; bit++) {
            int nb = (tempByte >> (7 - bitPos)) & 1;
            ret += (nb << (nBits - 1 - bit));
            bitPos++;
            if (bitPos == 8) {
                bitPos = 0;
                if (bit != nBits - 1) {
                    tempByte = readNoBitReset();
                }
            }
        }
        return ret;
    }
    
    private void alignByte() {
        bitPos = 0;
    }
    
    public int read() throws IOException {
        alignByte();
        pos++;
        return is.read();
    }
    
    public int readUI8() throws IOException {
        return read();
    }
    
    public int readUI24() throws IOException {
        int ret = (readEx() << 16) + (readEx() << 8) + (readEx());
        return ret;
    }
    
    public byte[] readBytes(int len) throws IOException {
        byte[] data = new byte[len];
        is.readFully(data);
        pos += len;
        return data;
    }
    
    public int available() throws IOException {
        return is.available();
    }
    
    public FLVTAG readTag() throws IOException {       
        long posBefore = pos;
        int tagType = readUI8();
        int dataLen = readUI24();
        //System.out.println("reading tag of type "+ tagType+" datalen="+dataLen);
        int timeStamp = readUI24();
        int timeStampExtended = readUI8();
        int timeStampFull = (timeStampExtended << 24) + timeStamp;
        //System.out.println("reding timestamp "+timeStampFull);
        readUI24(); //streamId, always 0
        byte data[] = new byte[dataLen];
        is.readFully(data);
        readUI32(); //tag size
        
        switch(tagType) {
            case FLVTAG.DATATYPE_VIDEO:
                FLVInputStream subStream = new FLVInputStream(new ByteArrayInputStream(data));
                int frameType = (int)subStream.readUB(4);
                int codecId = (int)subStream.readUB(4);
                byte[] videoData = subStream.readBytes(subStream.available());                
                return new FLVTAG(timeStampFull, new VIDEODATA(frameType, codecId, videoData));                
            case FLVTAG.DATATYPE_AUDIO:
            case FLVTAG.DATATYPE_SCRIPT_DATA:
            default:
                return new FLVTAG(timeStampFull, new UnparsedDATA(tagType, data));
        }       
    }
    
    /**
     * Reads one UI16 (Unsigned 16bit integer) value from the stream
     *
     * @return UI16 value
     * @throws IOException
     */
    public int readUI16() throws IOException {
        return (readEx() << 8) + readEx();
    }
}
