/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * FLV input stream.
 *
 * @author JPEXS
 */
public class FLVInputStream {

    private final DataInputStream is;

    private int bitPos = 0;

    private int tempByte = 0;

    private long pos = 0;

    /**
     * Constructor.
     * @param is Input stream
     */
    public FLVInputStream(InputStream is) {
        this.is = new DataInputStream(is);
    }

    /**
     * Reads header.
     * @param audioPresent Audio present
     * @param videoPresent Video present
     * @throws IOException On I/O error
     */
    public void readHeader(Reference<Boolean> audioPresent, Reference<Boolean> videoPresent) throws IOException {
        byte[] signature = new byte[3];
        is.readFully(signature);
        if (signature[0] != 'F' || signature[1] != 'L' || signature[2] != 'V') {
            throw new IOException("Invalid FLV file - invalid signature");
        }
        pos += 3;
        int version = read();
        if (version != 1) {
            throw new IOException("Unsupported FLV version: " + version + ", only 1 is supported");
        }
        int reserved = (int) readUB(5);
        audioPresent.setVal(readUB(1) == 1L);
        readUB(1);
        videoPresent.setVal(readUB(1) == 1L);
        long headerSize = readUI32();
        if (headerSize != 9) {
            throw new IOException("Invalid header size: " + headerSize);
        }
        readUI32(); //should be 0                
    }

    /**
     * Reads tags
     * @return List of tags
     * @throws IOException On I/O error
     */
    public List<FLVTAG> readTags() throws IOException {
        List<FLVTAG> ret = new ArrayList<>();
        while (available() > 0) {
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
     * @throws IOException On I/O error
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
     * @throws IOException On I/O error
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
     * @throws IOException On I/O error
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

    /**
     * Reads one byte from the stream
     * @return byte
     * @throws IOException On I/O error
     */
    public int read() throws IOException {
        alignByte();
        pos++;
        return is.read();
    }

    /**
     * Reads UI8 (Unsigned 8bit integer) value from the stream
     * @return UI8 value
     * @throws IOException On I/O error
     */
    public int readUI8() throws IOException {
        return read();
    }

    /**
     * Reads one UI24 (Unsigned 24bit integer) value from the stream
     * @return UI24 value
     * @throws IOException On I/O error
     */
    public int readUI24() throws IOException {
        int ret = (readEx() << 16) + (readEx() << 8) + (readEx());
        return ret;
    }

    /**
     * Reads bytes from the stream
     * @param len Number of bytes to read
     * @return Bytes
     * @throws IOException On I/O error
     */
    public byte[] readBytes(int len) throws IOException {
        byte[] data = new byte[len];
        is.readFully(data);
        pos += len;
        return data;
    }

    /**
     * Gets number of available bytes in the stream
     * @return Number of available bytes
     * @throws IOException On I/O error
     */
    public int available() throws IOException {
        return is.available();
    }

    /**
     * Reads one tag from the stream
     * @return Tag
     * @throws IOException On I/O error
     */
    public FLVTAG readTag() throws IOException {
        int tagType = readUI8();
        int dataLen = readUI24();
        int timeStamp = readUI24();
        int timeStampExtended = readUI8();
        int timeStampFull = (timeStampExtended << 24) + timeStamp;
        readUI24(); //streamId, always 0
        byte[] data = readBytes(dataLen);
        readUI32(); //tag size

        FLVInputStream subStream = new FLVInputStream(new ByteArrayInputStream(data));
        switch (tagType) {
            case FLVTAG.DATATYPE_VIDEO:
                return new FLVTAG(timeStampFull, subStream.readVIDEODATA());
            case FLVTAG.DATATYPE_SCRIPT_DATA:
                return new FLVTAG(timeStampFull, subStream.readSCRIPTDATA());
            case FLVTAG.DATATYPE_AUDIO:
                return new FLVTAG(timeStampFull, subStream.readAUDIODATA());
            default:
                return new FLVTAG(timeStampFull, new UnparsedDATA(tagType, data));
        }
    }

    /**
     * Reads one UI16 (Unsigned 16bit integer) value from the stream
     *
     * @return UI16 value
     * @throws IOException On I/O error
     */
    public int readUI16() throws IOException {
        return (readEx() << 8) + readEx();
    }

    /**
     * Reads SCRIPTDATA from the stream
     * @return SCRIPTDATA
     * @throws IOException On I/O error
     */
    public SCRIPTDATA readSCRIPTDATA() throws IOException {
        SCRIPTDATAVALUE name = readSCRIPTDATAVALUE();
        SCRIPTDATAVALUE value = readSCRIPTDATAVALUE();
        return new SCRIPTDATA(name, value);
    }

    /**
     * Reads one DOUBLE (double precision floating point value) value from the
     * stream
     *
     * @return DOUBLE value
     * @throws IOException On I/O error
     */
    public double readDOUBLE() throws IOException {
        long el = readLong();
        double ret = Double.longBitsToDouble(el);
        return ret;
    }

    private long readLong() throws IOException {
        byte[] readBuffer = readBytes(8);
        return (((long) readBuffer[0] << 56)
                + ((long) (readBuffer[1] & 0xff) << 48)
                + ((long) (readBuffer[2] & 0xff) << 40)
                + ((long) (readBuffer[3] & 0xff) << 32)
                + ((long) (readBuffer[4] & 0xff) << 24)
                + ((readBuffer[5] & 0xff) << 16)
                + ((readBuffer[6] & 0xff) << 8)
                + ((readBuffer[7] & 0xff)));
    }

    /**
     * Reads AUDIODATA from the stream
     * @return AUDIODATA
     * @throws IOException On I/O error
     */
    public AUDIODATA readAUDIODATA() throws IOException {
        int soundFormat = (int) readUB(4);
        int soundRate = (int) readUB(2);
        boolean soundSize = (int) readUB(1) == 1;
        boolean soundType = (int) readUB(1) == 1;
        byte[] soundData = readBytes(available());
        return new AUDIODATA(soundFormat, soundRate, soundSize, soundType, soundData);

    }

    /**
     * Reads SCRIPTDATAOBJECT from the stream
     * @return SCRIPTDATAOBJECT
     * @throws IOException On I/O error
     */
    public SCRIPTDATAOBJECT readSCRIPTDATAOBJECT() throws IOException {
        System.out.println("reading obj");
        String objectName = readSCRIPTDATASTRING();
        System.out.println("objectName " + objectName);
        if (objectName.length() == 0) {
            int endMarker = readUI8();
            if (endMarker != 9) {
                throw new IOException("Invalid SCRIPTOBJECT end marker - 9 expected but " + endMarker + " found");
            }
            return null;
        }
        SCRIPTDATAVALUE objectData = readSCRIPTDATAVALUE();
        return new SCRIPTDATAOBJECT(objectName, objectData);
    }

    /**
     * Reads VIDEODATA from the stream
     * @return VIDEODATA
     * @throws IOException On I/O error
     */
    public VIDEODATA readVIDEODATA() throws IOException {
        int frameType = (int) readUB(4);
        int codecId = (int) readUB(4);
        byte[] videoData = readBytes(available());
        return new VIDEODATA(frameType, codecId, videoData);
    }

    /**
     * Reads SCRIPTDATAVALUE from the stream
     * @return SCRIPTDATAVALUE
     * @throws IOException On I/O error
     */
    public SCRIPTDATAVALUE readSCRIPTDATAVALUE() throws IOException {
        int type = readUI8();
        switch (type) {
            //Number type
            case 0:
                return new SCRIPTDATAVALUE(readDOUBLE());
            //Boolean type
            case 1:
                return new SCRIPTDATAVALUE(type, readUI8() == 1 ? Boolean.TRUE : Boolean.FALSE);
            //String type
            case 2:
                return new SCRIPTDATAVALUE(type, readSCRIPTDATASTRING());
            //Object type
            case 3:
                List<SCRIPTDATAOBJECT> objects = new ArrayList<>();
                SCRIPTDATAOBJECT object;
                while ((object = readSCRIPTDATAOBJECT()) != null) {
                    objects.add(object);
                }
                return new SCRIPTDATAVALUE(type, objects);
            //MovieClip type
            case 4:
                return new SCRIPTDATAVALUE(type, readSCRIPTDATASTRING());
            //Null type
            case 5:
            //Undefined type
            case 6:
                return new SCRIPTDATAVALUE(type, null);
            //Reference type
            case 7:
                return new SCRIPTDATAVALUE(type, readUI16());
            //ECMA array type
            case 8:
                int ecmaArrayLength = (int) readUI32();
                List<SCRIPTDATAVARIABLE> variables2 = new ArrayList<>();
                SCRIPTDATAVARIABLE variable2;
                while ((variable2 = readSCRIPTDATAVARIABLE()) != null) {
                    variables2.add(variable2);
                }
                return new SCRIPTDATAVALUE(type, variables2);
            //Strict array type
            case 10:
                int arrayLength = (int) readUI32();
                List<SCRIPTDATAVARIABLE> variables = new ArrayList<>();
                for (int i = 0; i < arrayLength; i++) {
                    variables.add(readSCRIPTDATAVARIABLE());
                }
                return new SCRIPTDATAVALUE(type, variables);
            //Date type
            case 11:
                return new SCRIPTDATAVALUE(type, readSCRIPTDATADATE());
            //Long string type             
            case 12:
                return new SCRIPTDATAVALUE(type, readSCRIPTDATALONGSTRING());
            default:
                return null;
        }
    }

    /**
     * Reads SCRIPTDATALONGSTRING from the stream
     * @return SCRIPTDATALONGSTRING
     * @throws IOException On I/O error
     */
    public String readSCRIPTDATALONGSTRING() throws IOException {
        int len = (int) readUI32(); //? should be unsinged
        return new String(readBytes(len), Utf8Helper.charset);
    }

    /**
     * Reads SCRIPTDATADATE from the stream
     * @return SCRIPTDATADATE
     * @throws IOException On I/O error
     */
    public SCRIPTDATADATE readSCRIPTDATADATE() throws IOException {
        double time = readDOUBLE();
        int localDateTimeOffset = readSI16();
        return new SCRIPTDATADATE(time, localDateTimeOffset);
    }

    /**
     * Reads one SI16 (Signed 16bit integer) value from the stream
     *
     * @return SI16 value
     * @throws IOException On I/O error
     */
    public int readSI16() throws IOException {
        int uval = (readEx() << 8) + readEx();
        if (uval >= 0x8000) {
            uval = -(((~uval) & 0xffff) + 1);
        }
        return uval;
    }

    /**
     * Reads SCRIPTDATAVARIABLE from the stream
     * @return SCRIPTDATAVARIABLE
     * @throws IOException On I/O error
     */
    public SCRIPTDATAVARIABLE readSCRIPTDATAVARIABLE() throws IOException {
        String variableName = readSCRIPTDATASTRING();
        if (variableName.length() == 0) {
            int endMarker = readUI8();
            if (endMarker != 9) {
                throw new IOException("Invalid SCRIPTDATAVARIABLE end marker - 9 expected but " + endMarker + " found");
            }
            return null;
        }
        SCRIPTDATAVALUE variableValue = readSCRIPTDATAVALUE();
        return new SCRIPTDATAVARIABLE(variableName, variableValue);
    }

    /**
     * Reads SCRIPTDATASTRING from the stream
     * @return SCRIPTDATASTRING
     * @throws IOException On I/O error
     */
    public String readSCRIPTDATASTRING() throws IOException {
        int len = readUI16();
        return new String(readBytes(len), Utf8Helper.charset);
    }
}
