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
package com.jpexs.decompiler.flash.flv;

import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FLVOutputStream extends OutputStream {

    private final OutputStream os;

    private int bitPos = 0;

    private int tempByte = 0;

    private long pos = 0;

    public FLVOutputStream(OutputStream os) {
        this.os = os;
    }

    public long getPos() {
        return pos;
    }

    /**
     * Writes byte to the stream
     *
     * @param b byte to write
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException {
        alignByte();
        os.write(b);
        pos++;
    }

    private void alignByte() throws IOException {
        if (bitPos > 0) {
            bitPos = 0;
            write(tempByte);
            tempByte = 0;
        }
    }

    /**
     * Writes UI8 (Unsigned 8bit integer) value to the stream
     *
     * @param val UI8 value to write
     * @throws IOException
     */
    public void writeUI8(int val) throws IOException {
        write(val);
    }

    /**
     * Writes UI24 (Unsigned 24bit integer) value to the stream
     *
     * @param value UI32 value
     * @throws IOException
     */
    public void writeUI24(long value) throws IOException {
        write((int) ((value >> 16) & 0xff));
        write((int) ((value >> 8) & 0xff));
        write((int) (value & 0xff));

    }

    /**
     * Writes UI32 (Unsigned 32bit integer) value to the stream
     *
     * @param value UI32 value
     * @throws IOException
     */
    public void writeUI32(long value) throws IOException {
        write((int) ((value >> 24) & 0xff));
        write((int) ((value >> 16) & 0xff));
        write((int) ((value >> 8) & 0xff));
        write((int) (value & 0xff));
    }

    /**
     * Writes UI16 (Unsigned 16bit integer) value to the stream
     *
     * @param value UI16 value
     * @throws IOException
     */
    public void writeUI16(int value) throws IOException {
        write((int) ((value >> 8) & 0xff));
        write((int) (value & 0xff));
    }

    /**
     * Writes UB[nBits] (Unsigned-bit value) value to the stream
     *
     * @param nBits Number of bits which represent value
     * @param value Unsigned value to write
     * @throws IOException
     */
    public void writeUB(int nBits, long value) throws IOException {
        for (int bit = 0; bit < nBits; bit++) {
            int nb = (int) ((value >> (nBits - 1 - bit)) & 1);
            tempByte += nb * (1 << (7 - bitPos));
            bitPos++;
            if (bitPos == 8) {
                bitPos = 0;
                write(tempByte);
                tempByte = 0;
            }
        }
    }

    public void writeHeader(boolean audio, boolean video) throws IOException {
        write("FLV".getBytes());
        write(1); //version
        writeUB(5, 0); //must be 0
        writeUB(1, audio ? 1 : 0); //audio present
        writeUB(1, 0); //reserved
        writeUB(1, video ? 1 : 0); //video present
        writeUI32(9);  //header size
        writeUI32(0);
    }

    public void writeTag(FLVTAG tag) throws IOException {
        long posBefore = getPos();
        writeUI8(tag.tagType);
        byte[] data = tag.data.getBytes();
        writeUI24(data.length);
        writeUI24(tag.timeStamp & 0xffffff);
        writeUI8((int) ((tag.timeStamp >> 24) & 0xff));
        writeUI24(0);
        write(data); //codecId 4, frameType 1
        long posAfter = getPos();
        long size = posAfter - posBefore;
        writeUI32(size);
    }

    public void writeSCRIPTDATASTRING(String s) throws IOException {
        byte[] bytes = Utf8Helper.getBytes(s);
        writeUI16(bytes.length);
        write(bytes);
    }

    public void writeSCRIPTDATALONGSTRING(String s) throws IOException {
        byte[] bytes = Utf8Helper.getBytes(s);
        writeUI32(bytes.length);
        write(bytes);
    }

    private void writeLong(long value) throws IOException {
        byte[] writeBuffer = new byte[8];
        writeBuffer[3] = (byte) (value >>> 56);
        writeBuffer[2] = (byte) (value >>> 48);
        writeBuffer[1] = (byte) (value >>> 40);
        writeBuffer[0] = (byte) (value >>> 32);
        writeBuffer[7] = (byte) (value >>> 24);
        writeBuffer[6] = (byte) (value >>> 16);
        writeBuffer[5] = (byte) (value >>> 8);
        writeBuffer[4] = (byte) (value);
        write(writeBuffer);
    }

    public void writeDOUBLE(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }

    public void writeSCRIPTDATAOBJECT(SCRIPTDATAOBJECT o) throws IOException {
        writeSCRIPTDATASTRING(o.objectName);
        writeSCRIPTDATAVALUE(o.objectData);
    }

    public void writeSCRIPTDATAVARIABLE(SCRIPTDATAVARIABLE v) throws IOException {
        writeSCRIPTDATASTRING(v.variableName);
        writeSCRIPTDATAVALUE(v.variableData);
    }

    public void writeSCRIPTDATAVALUE(SCRIPTDATAVALUE v) throws IOException {
        writeUI8(v.type);
        switch (v.type) {
            case 0:
                writeDOUBLE((double) (Double) v.value);
                break;
            case 1:
                writeUI8((boolean) (Boolean) v.value ? 1 : 0);
                break;
            case 2:
                writeSCRIPTDATASTRING((String) v.value);
                break;
            case 3:
                @SuppressWarnings("unchecked") List<SCRIPTDATAOBJECT> objects = (List<SCRIPTDATAOBJECT>) v.value;
                for (SCRIPTDATAOBJECT o : objects) {
                    writeSCRIPTDATAOBJECT(o);
                }
                writeUI24(9);//SCRIPTDATAOBJECTEND
                break;
            case 4:
                writeSCRIPTDATASTRING((String) v.value);
                break;
            case 5:
                //null
                break;
            case 6:
                //undefined
                break;
            case 7:
                writeUI16((int) (Integer) v.value);
                break;
            case 8:
                @SuppressWarnings("unchecked") List<SCRIPTDATAVARIABLE> variables = (List<SCRIPTDATAVARIABLE>) v.value;
                writeUI32(variables.size());
                for (SCRIPTDATAVARIABLE var : variables) {
                    writeSCRIPTDATAVARIABLE(var);
                }
                writeUI24(9);//SCRIPTDATAVARIABLEEND
                break;
            case 9:
                //reserved
                break;
            case 10:
                @SuppressWarnings("unchecked") List<SCRIPTDATAVARIABLE> stvariables = (List<SCRIPTDATAVARIABLE>) v.value;
                writeUI32(stvariables.size());
                for (SCRIPTDATAVARIABLE var : stvariables) {
                    writeSCRIPTDATAVARIABLE(var);
                }
                break;
            case 11:
                writeSCRIPTDATADATE((SCRIPTDATADATE) v.value);
                break;
            case 12:
                writeSCRIPTDATALONGSTRING((String) v.value);
                break;

        }
    }

    public void writeSI16(int value) throws IOException {
        writeUI16(value);
    }

    public void writeSCRIPTDATADATE(SCRIPTDATADATE d) throws IOException {
        writeDOUBLE(d.dateTime);
        writeSI16(d.localDateTimeOffset);
    }
}
