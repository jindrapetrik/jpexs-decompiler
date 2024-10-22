/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.amf.amf3.Amf3OutputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.amf.amf3.NoSerializerExistsException;
import com.jpexs.decompiler.flash.amf.amf3.ObjectTypeSerializeHandler;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.ALPHABITMAPDATA;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.BITMAPDATA;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CLIPEVENTFLAGS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHFOCALGRADIENT;
import com.jpexs.decompiler.flash.types.MORPHGRADIENT;
import com.jpexs.decompiler.flash.types.MORPHGRADRECORD;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.PIX15;
import com.jpexs.decompiler.flash.types.PIX24;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.SOUNDENVELOPE;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.ZONEDATA;
import com.jpexs.decompiler.flash.types.ZONERECORD;
import com.jpexs.decompiler.flash.types.filters.BEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.BLURFILTER;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.types.filters.GLOWFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Class for writing data into SWF file.
 *
 * @author JPEXS
 */
public class SWFOutputStream extends OutputStream {

    /**
     * Output stream
     */
    private final OutputStream os;

    /**
     * SWF version
     */
    private final int version;

    /**
     * Current position in the stream
     */
    private long pos = 0;

    /**
     * Current bit position in the stream
     */
    private int bitPos = 0;

    /**
     * Temporary byte for writing bits
     */
    private int tempByte = 0;

    /**
     * Charset for writing strings. (SWF version 5 and lower is not using UTF-8)
     */
    private String charset;

    /**
     * Gets current position in the stream.
     *
     * @return Current position in the stream
     */
    public long getPos() {
        return pos;
    }

    /**
     * Constructs new SWFOutputStream.
     *
     * @param os OutputStream for writing data
     * @param version Version of SWF
     */
    public SWFOutputStream(OutputStream os, int version, String charset) {
        this.version = version;
        this.os = os;
        this.charset = charset;
    }

    /**
     * Writes byte to the stream.
     *
     * @param b Byte to write
     * @throws IOException On I/O error
     */
    @Override
    public void write(int b) throws IOException {
        alignByte();
        os.write(b);
        pos++;
    }

    /**
     * Writes byte array to the stream.
     *
     * @param b The data.
     * @throws IOException On I/O error
     */
    @Override
    public void write(byte[] b) throws IOException {
        alignByte();
        os.write(b);
        pos += b.length;
    }

    /**
     * Writes byte array to the stream.
     *
     * @param b The data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     * @throws IOException On I/O error
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        alignByte();
        os.write(b, off, len);
        pos += len;
    }

    /**
     * Writes ByteArrayRange to the stream.
     *
     * @param b The data.
     * @throws IOException On I/O error
     */
    public void write(ByteArrayRange b) throws IOException {
        alignByte();
        os.write(b.getArray(), b.getPos(), b.getLength());
        pos += b.getLength();
    }

    /**
     * Aligns to byte position.
     *
     * @throws IOException On I/O error
     */
    private void alignByte() throws IOException {
        if (bitPos > 0) {
            bitPos = 0;
            write(tempByte);
            tempByte = 0;
        }
    }

    /**
     * Writes UI8 (Unsigned 8bit integer) value to the stream.
     *
     * @param value UI8 value to write
     * @throws IOException On I/O error
     */
    public void writeUI8(int value) throws IOException {
        if (value > 0xff) {
            throw new ValueTooLargeException("UI8", value);
        }

        write(value);
    }

    /**
     * Writes null terminated string value to the stream.
     *
     * @param value String value
     * @throws IOException On I/O error
     */
    public void writeString(String value) throws IOException {
        byte[] data;
        if ("UTF-8".equals(charset)) {
            data = Utf8Helper.getBytes(value);
        } else {
            data = value.getBytes(charset);
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                throw new IOException("String should not contain null character.");
            }
        }

        write(data);
        write(0);
    }

    /**
     * Writes netstring (length + string) value to the stream.
     *
     * @param value String value
     * @throws IOException On I/O error
     */
    public void writeNetString(String value) throws IOException {
        byte[] data = value.getBytes(charset);
        writeUI8(data.length);
        write(data);
    }

    /**
     * Writes UI32 (Unsigned 32bit integer) value to the stream.
     *
     * @param value UI32 value
     * @throws IOException On I/O error
     */
    public void writeUI32(long value) throws IOException {
        if (value > 0xffffffffL) {
            throw new ValueTooLargeException("UI32", value);
        }

        write((int) (value & 0xff));
        write((int) ((value >> 8) & 0xff));
        write((int) ((value >> 16) & 0xff));
        write((int) ((value >> 24) & 0xff));
    }

    /**
     * Writes UI16 (Unsigned 16bit integer) value to the stream.
     *
     * @param value UI16 value
     * @throws IOException On I/O error
     */
    public void writeUI16(int value) throws IOException {
        if (value > 0xffff) {
            throw new ValueTooLargeException("UI16", value);
        }

        write((int) (value & 0xff));
        write((int) ((value >> 8) & 0xff));
    }

    /**
     * Writes SI32 (Signed 32bit integer) value to the stream.
     *
     * @param value SI32 value
     * @throws IOException On I/O error
     */
    public void writeSI32(long value) throws IOException {
        if (value > 0x7fffffffL) {
            throw new ValueTooLargeException("SI32", value);
        }

        writeUI32(value);
    }

    /**
     * Writes SI16 (Signed 16bit integer) value to the stream.
     *
     * @param value SI16 value
     * @throws IOException On I/O error
     */
    public void writeSI16(int value) throws IOException {

        if (value > 0x7fff) {
            throw new ValueTooLargeException("SI16", value);
        }

        writeUI16(value);
    }

    /**
     * Writes SI8 (Signed 8bit integer) value to the stream.
     *
     * @param value SI8 value
     * @throws IOException On I/O error
     */
    public void writeSI8(int value) throws IOException {
        if (value > 0x7ff) {
            throw new ValueTooLargeException("SI8", value);
        }

        writeUI8(value);
    }

    /**
     * Writes FIXED (Fixed point 16.16) value to the stream.
     *
     * @param value FIXED value
     * @throws IOException On I/O error
     */
    public void writeFIXED(double value) throws IOException {
        long valueLong = (long) (value * (1 << 16));
        writeSI32(valueLong);
    }

    /**
     * Writes FIXED8 (Fixed point 8.8) value to the stream.
     *
     * @param value FIXED8 value
     * @throws IOException On I/O error
     */
    public void writeFIXED8(float value) throws IOException {
        int valueInt = (int) (value * (1 << 8));
        writeSI16(valueInt);
    }

    /**
     * Writes long value to the stream.
     *
     * @param value The value.
     * @throws IOException On I/O error
     */
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

    /**
     * Writes DOUBLE (double precision floating point value) value to the
     * stream.
     *
     * @param value DOUBLE value
     * @throws IOException On I/O error
     */
    public void writeDOUBLE(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }

    /**
     * Writes FLOAT (single precision floating point value) value to the stream.
     *
     * @param value FLOAT value
     * @throws IOException On I/O error
     */
    public void writeFLOAT(float value) throws IOException {
        writeUI32(Float.floatToIntBits(value));
    }

    /**
     * Writes FLOAT16 (16bit floating point value) value to the stream.
     *
     * @param value FLOAT16 value
     * @throws IOException On I/O error
     */
    public void writeFLOAT16(float value) throws IOException {
        int bits = Float.floatToRawIntBits(value);
        int sign = bits >> 31;
        int exponent = (bits >> 22) & 0xff;
        int mantisa = bits & 0x3FFFFF;
        mantisa >>= 13;
        writeUI16((sign << 15) + (exponent << 10) + mantisa);
    }

    /**
     * Writes EncodedU32 (Encoded unsigned 32bit value) value to the stream.
     *
     * @param value U32 value
     * @throws IOException On I/O error
     */
    public void writeEncodedU32(long value) throws IOException {
        boolean loop = true;
        value &= 0xFFFFFFFF;
        do {
            int ret = (int) (value & 0x7F);
            if (value < 0x80) {
                loop = false;
            }
            if (value > 0x7F) {
                ret += 0x80;
            }
            write(ret);
            value >>= 7;
        } while (loop);
    }

    /**
     * Flushes data to underlying stream.
     *
     * @throws IOException On I/O error
     */
    @Override
    public void flush() throws IOException {
        if (bitPos > 0) {
            bitPos = 0;
            write(tempByte);
            tempByte = 0;
        }
        os.flush();
    }

    /**
     * Closes the stream.
     *
     * @throws IOException On I/O error
     */
    @Override
    public void close() throws IOException {
        flush();
        os.close();
    }

    /**
     * Writes UB[nBits] (Unsigned-bit value) value to the stream.
     *
     * @param nBits Number of bits which represent value
     * @param value Unsigned value to write
     * @throws IOException On I/O error
     */
    public void writeUB(int nBits, long value) throws IOException {
        if (!fitsInUB(nBits, value)) {
            throw new ValueTooLargeException("UB[" + nBits + "]", value);
        }
        writeNBInternal(nBits, value, "UB");
    }

    /**
     * Checks if value fits in SB[nBits] (Signed-bit value).
     *
     * @param nBits Number of bits
     * @param value Value
     * @return True if value fits in SB[nBits]
     */
    public static boolean fitsInSB(int nBits, long value) {
        long min = -1L << (nBits - 1);
        long max = (1L << (nBits - 1)) - 1;
        return value >= min && value <= max;
    }

    /**
     * Checks if value fits in UB[nBits] (Unsigned-bit value).
     *
     * @param nBits Number of bits
     * @param value Value
     * @return True if value fits in UB[nBits]
     */
    public static boolean fitsInUB(int nBits, long value) {
        long min = 0;
        long max = (1L << nBits) - 1;
        return value >= min && value <= max;
    }

    /**
     * Writes NB[nBits] (Bit value) value to the stream.
     *
     * @param nBits Number of bits which represent value
     * @param value Value to write
     * @param type Type of value
     * @throws IOException On I/O error
     */
    private void writeNBInternal(int nBits, long value, String type) throws IOException {
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

    /**
     * Writes SB[nBits] (Signed-bit value) value to the stream.
     *
     * @param nBits Number of bits which represent value
     * @param value Signed value to write
     * @throws IOException On I/O error
     */
    public void writeSB(int nBits, long value) throws IOException {
        if (!fitsInSB(nBits, value)) {
            throw new ValueTooLargeException("SB[" + nBits + "]", value);
        }
        writeNBInternal(nBits, value, "SB");
    }

    /**
     * Writes FB[nBits] (Signed fixed-point bit value) value to the stream.
     *
     * @param nBits Number of bits which represent value
     * @param value Double value to write
     * @throws IOException On I/O error
     */
    public void writeFB(int nBits, double value) throws IOException {
        if (nBits == 0) {
            return;
        }
        long longVal = (long) (value * (1 << 16));
        writeSB(nBits, longVal);
    }

    /**
     * Writes RECT value to the stream.
     *
     * @param value RECT value
     * @throws IOException On I/O error
     */
    public void writeRECT(RECT value) throws IOException {
        int nBits = 0;

        if (Configuration._debugCopy.get()) {
            nBits = Math.max(nBits, value.nbits);
        }

        int xMin = truncateTo31Bit(value.Xmin);
        int xMax = truncateTo31Bit(value.Xmax);
        int yMin = truncateTo31Bit(value.Ymin);
        int yMax = truncateTo31Bit(value.Ymax);
        nBits = enlargeBitCountS(nBits, xMin);
        nBits = enlargeBitCountS(nBits, xMax);
        nBits = enlargeBitCountS(nBits, yMin);
        nBits = enlargeBitCountS(nBits, yMax);

        if (Configuration._debugCopy.get()) {
            nBits = Math.max(nBits, value.nbits);
        }

        writeUB(5, nBits);
        writeSB(nBits, xMin);
        writeSB(nBits, xMax);
        writeSB(nBits, yMin);
        writeSB(nBits, yMax);
        alignByte();
    }

    /**
     * Truncates value to 31 bits.
     *
     * @param value Value
     * @return Truncated value
     */
    private int truncateTo31Bit(int value) {
        if (value > 0x3fffffff) {
            value = 0x3fffffff;
        }
        if (value < -0x3fffffff) {
            value = -0x3fffffff;
        }
        return value;
    }

    /**
     * Writes list of Tag values to the stream.
     *
     * @param tags List of tag values
     * @throws IOException On I/O error
     */
    public void writeTags(Iterable<Tag> tags) throws IOException {
        for (Tag tag : tags) {
            tag.writeTag(this);
        }
    }

    /**
     * Calculates number of bits needed for representing signed value.
     *
     * @param v Signed value
     * @return Number of bits
     */
    private static int getNeededBitsS(int v) {
        int counter = 32;
        int mask = 0x80000000;
        final int val = (v < 0) ? -v : v;
        while (((val & mask) == 0) && (counter > 0)) {
            mask >>>= 1;
            counter -= 1;
        }
        return counter + 1;
    }

    /**
     * Calculates number of bits needed for representing signed values.
     *
     * @param first First Signed value
     * @param params Next Signed values
     * @return Number of bits
     */
    public static int getNeededBitsS(int first, int... params) {
        int nBits = 0;
        nBits = enlargeBitCountS(nBits, first);
        for (int i = 0; i < params.length; i++) {
            nBits = enlargeBitCountS(nBits, params[i]);
        }
        return nBits;
    }

    /**
     * Calculates number of bits needed for representing unsigned value.
     *
     * @param value Unsigned value
     * @return Number of bits
     */
    public static int getNeededBitsU(int value) {
        if (value == 0) {
            return 0;
        }

        value = Math.abs(value);
        long x = 1;
        int nBits;

        for (nBits = 1; nBits <= 64; nBits++) {
            x <<= 1;
            if (x > value) {
                break;
            }
        }
        return nBits;
    }

    /**
     * Calculates number of bits needed for representing unsigned values.
     *
     * @param first First Unsigned value
     * @param params Next Unsigned values
     * @return Number of bits
     */
    public static int getNeededBitsU(int first, int... params) {
        int nBits = 0;
        nBits = enlargeBitCountU(nBits, first);
        for (int i = 0; i < params.length; i++) {
            nBits = enlargeBitCountU(nBits, params[i]);
        }
        return nBits;
    }

    /**
     * Calculates number of bits needed for representing unsigned value.
     *
     * @param value Unsigned value
     * @return Number of bits
     */
    public static int unsignedSize(final int value) {

        final int val = (value < 0) ? -value - 1 : value;
        int counter = 32;
        int mask = 0x80000000;

        while (((val & mask) == 0) && (counter > 0)) {
            mask >>>= 1;
            counter -= 1;
        }
        return counter;
    }

    /**
     * Calculates number of bits needed for representing fixed-point value.
     *
     * @param value Fixed-point value
     * @return Number of bits
     */
    public static int getNeededBitsF(float value) {
        // 0.26213074  16bits
        // 0.5 17bits
        // 1.3476715 18bits
        int k = (int) value;
        return getNeededBitsS(k) + 16;
    }

    /**
     * Enlarges bit count for signed values.
     *
     * @param currentBitCount Current bit count
     * @param value Value
     * @return New bit count
     */
    public static int enlargeBitCountS(int currentBitCount, int value) {
        if (value == 0) {
            return currentBitCount;
        }
        int neededNew = getNeededBitsS(value);
        if (neededNew > currentBitCount) {
            return neededNew;
        }
        return currentBitCount;
    }

    /**
     * Enlarges bit count for unsigned values.
     *
     * @param currentBitCount Current bit count
     * @param value Value
     * @return New bit count
     */
    public static int enlargeBitCountU(int currentBitCount, int value) {
        if (value == 0) {
            return currentBitCount;
        }
        int neededNew = getNeededBitsU(value);
        if (neededNew > currentBitCount) {
            return neededNew;
        }
        return currentBitCount;
    }

    /**
     * Writes MATRIX value to the stream.
     *
     * @param value MATRIX value
     * @throws IOException On I/O error
     */
    public void writeMatrix(MATRIX value) throws IOException {
        writeUB(1, value.hasScale ? 1 : 0);
        if (value.hasScale) {
            int nBits = 0;
            nBits = enlargeBitCountS(nBits, value.getScaleXInteger());
            nBits = enlargeBitCountS(nBits, value.getScaleYInteger());

            if (Configuration._debugCopy.get()) {
                nBits = Math.max(nBits, value.nScaleBits);
            }

            writeUB(5, nBits);
            writeFB(nBits, value.scaleX);
            writeFB(nBits, value.scaleY);
        }
        writeUB(1, value.hasRotate ? 1 : 0);
        if (value.hasRotate) {
            int nBits = 0;
            nBits = enlargeBitCountS(nBits, value.getRotateSkew0Integer());
            nBits = enlargeBitCountS(nBits, value.getRotateSkew1Integer());

            if (Configuration._debugCopy.get()) {
                nBits = Math.max(nBits, value.nRotateBits);
            }

            writeUB(5, nBits);
            writeFB(nBits, value.rotateSkew0);
            writeFB(nBits, value.rotateSkew1);
        }
        int NTranslateBits = 0;
        NTranslateBits = enlargeBitCountS(NTranslateBits, value.translateX);
        NTranslateBits = enlargeBitCountS(NTranslateBits, value.translateY);

        if (Configuration._debugCopy.get()) {
            NTranslateBits = Math.max(NTranslateBits, value.nTranslateBits);
        }

        writeUB(5, NTranslateBits);

        writeSB(NTranslateBits, value.translateX);
        writeSB(NTranslateBits, value.translateY);
        alignByte();

    }

    /**
     * Writes CXFORM value to the stream.
     *
     * @param value CXFORM value
     * @throws IOException On I/O error
     */
    public void writeCXFORM(CXFORM value) throws IOException {
        writeUB(1, value.hasAddTerms ? 1 : 0);
        writeUB(1, value.hasMultTerms ? 1 : 0);
        int Nbits = 1;
        if (value.hasMultTerms) {
            Nbits = enlargeBitCountS(Nbits, value.redMultTerm);
            Nbits = enlargeBitCountS(Nbits, value.greenMultTerm);
            Nbits = enlargeBitCountS(Nbits, value.blueMultTerm);
        }
        if (value.hasAddTerms) {
            Nbits = enlargeBitCountS(Nbits, value.redAddTerm);
            Nbits = enlargeBitCountS(Nbits, value.greenAddTerm);
            Nbits = enlargeBitCountS(Nbits, value.blueAddTerm);
        }

        if (Configuration._debugCopy.get()) {
            Nbits = Math.max(Nbits, value.nbits);
        }

        writeUB(4, Nbits);
        if (value.hasMultTerms) {
            writeSB(Nbits, value.redMultTerm);
            writeSB(Nbits, value.greenMultTerm);
            writeSB(Nbits, value.blueMultTerm);
        }
        if (value.hasAddTerms) {
            writeSB(Nbits, value.redAddTerm);
            writeSB(Nbits, value.greenAddTerm);
            writeSB(Nbits, value.blueAddTerm);
        }
        alignByte();
    }

    /**
     * Writes CXFORMWITHALPHA value to the stream.
     *
     * @param value CXFORMWITHALPHA value
     * @throws IOException On I/O error
     */
    public void writeCXFORMWITHALPHA(CXFORMWITHALPHA value) throws IOException {
        writeUB(1, value.hasAddTerms ? 1 : 0);
        writeUB(1, value.hasMultTerms ? 1 : 0);
        int Nbits = 1;
        if (value.hasMultTerms) {
            Nbits = enlargeBitCountS(Nbits, value.redMultTerm);
            Nbits = enlargeBitCountS(Nbits, value.greenMultTerm);
            Nbits = enlargeBitCountS(Nbits, value.blueMultTerm);
            Nbits = enlargeBitCountS(Nbits, value.alphaMultTerm);
        }
        if (value.hasAddTerms) {
            Nbits = enlargeBitCountS(Nbits, value.redAddTerm);
            Nbits = enlargeBitCountS(Nbits, value.greenAddTerm);
            Nbits = enlargeBitCountS(Nbits, value.blueAddTerm);
            Nbits = enlargeBitCountS(Nbits, value.alphaAddTerm);
        }

        if (Configuration._debugCopy.get()) {
            Nbits = Math.max(Nbits, value.nbits);
        }

        writeUB(4, Nbits);
        if (value.hasMultTerms) {
            writeSB(Nbits, value.redMultTerm);
            writeSB(Nbits, value.greenMultTerm);
            writeSB(Nbits, value.blueMultTerm);
            writeSB(Nbits, value.alphaMultTerm);
        }
        if (value.hasAddTerms) {
            writeSB(Nbits, value.redAddTerm);
            writeSB(Nbits, value.greenAddTerm);
            writeSB(Nbits, value.blueAddTerm);
            writeSB(Nbits, value.alphaAddTerm);
        }
        alignByte();
    }

    /**
     * Writes CLIPEVENTFLAGS value to the stream.
     *
     * @param value CLIPEVENTFLAGS value
     * @throws IOException On I/O error
     */
    public void writeCLIPEVENTFLAGS(CLIPEVENTFLAGS value) throws IOException {
        writeUB(1, value.clipEventKeyUp ? 1 : 0);
        writeUB(1, value.clipEventKeyDown ? 1 : 0);
        writeUB(1, value.clipEventMouseUp ? 1 : 0);
        writeUB(1, value.clipEventMouseDown ? 1 : 0);
        writeUB(1, value.clipEventMouseMove ? 1 : 0);
        writeUB(1, value.clipEventUnload ? 1 : 0);
        writeUB(1, value.clipEventEnterFrame ? 1 : 0);
        writeUB(1, value.clipEventLoad ? 1 : 0);
        writeUB(1, value.clipEventDragOver ? 1 : 0);
        writeUB(1, value.clipEventRollOut ? 1 : 0);
        writeUB(1, value.clipEventRollOver ? 1 : 0);
        writeUB(1, value.clipEventReleaseOutside ? 1 : 0);
        writeUB(1, value.clipEventRelease ? 1 : 0);
        writeUB(1, value.clipEventPress ? 1 : 0);
        writeUB(1, value.clipEventInitialize ? 1 : 0);
        writeUB(1, value.clipEventData ? 1 : 0);
        if (version >= 6) {
            writeUB(5, value.reserved);
            writeUB(1, value.clipEventConstruct ? 1 : 0);
            writeUB(1, value.clipEventKeyPress ? 1 : 0);
            writeUB(1, value.clipEventDragOut ? 1 : 0);
            writeUB(8, value.reserved2);
        }
    }

    /**
     * Writes CLIPACTIONRECORD value to the stream.
     *
     * @param value CLIPACTIONRECORD value
     * @throws IOException On I/O error
     */
    public void writeCLIPACTIONRECORD(CLIPACTIONRECORD value) throws IOException {
        writeCLIPEVENTFLAGS(value.eventFlags);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SWFOutputStream sos = new SWFOutputStream(baos, version, charset)) {
            if (value.eventFlags.clipEventKeyPress) {
                sos.writeUI8(value.keyCode);
            }
            sos.write(value.actionBytes);
        }
        byte[] data = baos.toByteArray();
        writeUI32(data.length); // actionRecordSize
        write(data);
    }

    /**
     * Writes CLIPACTIONS value to the stream.
     *
     * @param value CLIPACTIONS value
     * @throws IOException On I/O error
     */
    public void writeCLIPACTIONS(CLIPACTIONS value) throws IOException {
        writeUI16(value.reserved);
        writeCLIPEVENTFLAGS(value.allEventFlags);
        for (CLIPACTIONRECORD car : value.clipActionRecords) {
            writeCLIPACTIONRECORD(car);
        }
        if (version <= 5) {
            writeUI16(0);
        } else {
            writeUI32(0);
        }
    }

    /**
     * Writes COLORMATRIXFILTER value to the stream.
     *
     * @param value COLORMATRIXFILTER value
     * @throws IOException On I/O error
     */
    public void writeCOLORMATRIXFILTER(COLORMATRIXFILTER value) throws IOException {
        for (int i = 0; i < 20; i++) {
            writeFLOAT(value.matrix[i]);
        }
    }

    /**
     * Writes RGBA value to the stream.
     *
     * @param value RGBA value
     * @throws IOException On I/O error
     */
    public void writeRGBA(RGBA value) throws IOException {
        writeUI8(value.red);
        writeUI8(value.green);
        writeUI8(value.blue);
        writeUI8(value.alpha);
    }

    /**
     * Writes ARGB value to the stream.
     *
     * @param value ARGB value
     * @throws IOException On I/O error
     */
    public void writeARGB(ARGB value) throws IOException {
        writeUI8(value.alpha);
        writeUI8(value.red);
        writeUI8(value.green);
        writeUI8(value.blue);
    }

    /**
     * Writes ARGB value to the stream.
     *
     * @param value ARGB value
     * @throws IOException On I/O error
     */
    public void writeARGB(int value) throws IOException {
        writeUI8((value >> 24) & 0xff);
        writeUI8((value >> 16) & 0xff);
        writeUI8((value >> 8) & 0xff);
        writeUI8(value & 0xff);
    }

    /**
     * Writes RGB value to the stream.
     *
     * @param value RGB value
     * @throws IOException On I/O error
     */
    public void writeRGB(RGB value) throws IOException {
        writeUI8(value.red);
        writeUI8(value.green);
        writeUI8(value.blue);
    }

    /**
     * Writes CONVOLUTIONFILTER value to the stream.
     *
     * @param value CONVOLUTIONFILTER value
     * @throws IOException On I/O error
     */
    public void writeCONVOLUTIONFILTER(CONVOLUTIONFILTER value) throws IOException {
        writeUI8(value.matrixX);
        writeUI8(value.matrixY);
        writeFLOAT(value.divisor);
        writeFLOAT(value.bias);
        for (int y = 0; y < value.matrixY; y++) {
            for (int x = 0; x < value.matrixX; x++) {
                writeFLOAT(value.matrix[y * value.matrixX + x]);
            }
        }
        writeRGBA(value.defaultColor);
        writeUB(6, value.reserved);
        writeUB(1, value.clamp ? 1 : 0);
        writeUB(1, value.preserveAlpha ? 1 : 0);
    }

    /**
     * Writes BLURFILTER value to the stream.
     *
     * @param value BLURFILTER value
     * @throws IOException On I/O error
     */
    public void writeBLURFILTER(BLURFILTER value) throws IOException {
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeUB(5, value.passes);
        writeUB(3, value.reserved);
    }

    /**
     * Writes DROPSHADOWFILTER value to the stream.
     *
     * @param value DROPSHADOWFILTER value
     * @throws IOException On I/O error
     */
    public void writeDROPSHADOWFILTER(DROPSHADOWFILTER value) throws IOException {
        writeRGBA(value.dropShadowColor);
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED(value.angle);
        writeFIXED(value.distance);
        writeFIXED8(value.strength);
        writeUB(1, value.innerShadow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(5, value.passes);
    }

    /**
     * Writes GLOWFILTER value to the stream.
     *
     * @param value GLOWFILTER value
     * @throws IOException On I/O error
     */
    public void writeGLOWFILTER(GLOWFILTER value) throws IOException {
        writeRGBA(value.glowColor);
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED8(value.strength);
        writeUB(1, value.innerGlow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(5, value.passes);
    }

    /**
     * Writes BEVELFILTER value to the stream.
     *
     * @param value BEVELFILTER value
     * @throws IOException On I/O error
     */
    public void writeBEVELFILTER(BEVELFILTER value) throws IOException {
        writeRGBA(value.highlightColor);
        writeRGBA(value.shadowColor);
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED(value.angle);
        writeFIXED(value.distance);
        writeFIXED8(value.strength);
        writeUB(1, value.innerShadow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(1, value.onTop ? 1 : 0);
        writeUB(4, value.passes);
    }

    /**
     * Writes GRADIENTGLOWFILTER value to the stream.
     *
     * @param value GRADIENTGLOWFILTER value
     * @throws IOException On I/O error
     */
    public void writeGRADIENTGLOWFILTER(GRADIENTGLOWFILTER value) throws IOException {
        writeUI8(value.gradientColors.length);
        for (int i = 0; i < value.gradientColors.length; i++) {
            writeRGBA(value.gradientColors[i]);
        }
        for (int i = 0; i < value.gradientColors.length; i++) {
            writeUI8(value.gradientRatio[i]);
        }
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED(value.angle);
        writeFIXED(value.distance);
        writeFIXED8(value.strength);
        writeUB(1, value.innerShadow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(1, value.onTop ? 1 : 0);
        writeUB(4, value.passes);
    }

    /**
     * Writes GRADIENTBEVELFILTER value to the stream.
     *
     * @param value GRADIENTBEVELFILTER value
     * @throws IOException On I/O error
     */
    public void writeGRADIENTBEVELFILTER(GRADIENTBEVELFILTER value) throws IOException {
        writeUI8(value.gradientColors.length);
        for (int i = 0; i < value.gradientColors.length; i++) {
            writeRGBA(value.gradientColors[i]);
        }
        for (int i = 0; i < value.gradientColors.length; i++) {
            writeUI8(value.gradientRatio[i]);
        }
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED(value.angle);
        writeFIXED(value.distance);
        writeFIXED8(value.strength);
        writeUB(1, value.innerShadow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(1, value.onTop ? 1 : 0);
        writeUB(4, value.passes);
    }

    /**
     * Writes list of FILTER values to the stream.
     *
     * @param list List of FILTER values
     * @throws IOException On I/O error
     */
    public void writeFILTERLIST(List<FILTER> list) throws IOException {
        writeUI8(list.size());
        for (int i = 0; i < list.size(); i++) {
            writeFILTER(list.get(i));
        }
    }

    /**
     * Writes FILTER value to the stream.
     *
     * @param value FILTER value
     * @throws IOException On I/O error
     */
    public void writeFILTER(FILTER value) throws IOException {
        writeUI8(value.id);
        if (value instanceof DROPSHADOWFILTER) {
            writeDROPSHADOWFILTER((DROPSHADOWFILTER) value);
        }
        if (value instanceof BLURFILTER) {
            writeBLURFILTER((BLURFILTER) value);
        }
        if (value instanceof GLOWFILTER) {
            writeGLOWFILTER((GLOWFILTER) value);
        }
        if (value instanceof BEVELFILTER) {
            writeBEVELFILTER((BEVELFILTER) value);
        }
        if (value instanceof GRADIENTGLOWFILTER) {
            writeGRADIENTGLOWFILTER((GRADIENTGLOWFILTER) value);
        }
        if (value instanceof CONVOLUTIONFILTER) {
            writeCONVOLUTIONFILTER((CONVOLUTIONFILTER) value);
        }
        if (value instanceof COLORMATRIXFILTER) {
            writeCOLORMATRIXFILTER((COLORMATRIXFILTER) value);
        }
        if (value instanceof GRADIENTBEVELFILTER) {
            writeGRADIENTBEVELFILTER((GRADIENTBEVELFILTER) value);
        }
    }

    /**
     * Writes list of BUTTONRECORD values to the stream.
     *
     * @param list List of BUTTONRECORD values
     * @param inDefineButton2 Whether write inside of DefineButton2Tag or not
     * @throws IOException On I/O error
     */
    public void writeBUTTONRECORDList(List<BUTTONRECORD> list, boolean inDefineButton2) throws IOException {
        for (BUTTONRECORD brec : list) {
            writeBUTTONRECORD(brec, inDefineButton2);
        }
        writeUI8(0);
    }

    /**
     * Writes BUTTONRECORD value to the stream.
     *
     * @param value BUTTONRECORD value
     * @param inDefineButton2 Whether write inside of DefineButton2Tag or not
     * @throws IOException On I/O error
     */
    public void writeBUTTONRECORD(BUTTONRECORD value, boolean inDefineButton2) throws IOException {
        writeUB(2, value.reserved);
        writeUB(1, value.buttonHasBlendMode ? 1 : 0);
        writeUB(1, value.buttonHasFilterList ? 1 : 0);
        writeUB(1, value.buttonStateHitTest ? 1 : 0);
        writeUB(1, value.buttonStateDown ? 1 : 0);
        writeUB(1, value.buttonStateOver ? 1 : 0);
        writeUB(1, value.buttonStateUp ? 1 : 0);
        writeUI16(value.characterId);
        writeUI16(value.placeDepth);
        writeMatrix(value.placeMatrix);
        if (inDefineButton2) {
            writeCXFORMWITHALPHA(value.colorTransform);
            if (value.buttonHasFilterList) {
                writeFILTERLIST(value.filterList);
            }
            if (value.buttonHasBlendMode) {
                writeUI8(value.blendMode);
            }
        }
    }

    /**
     * Writes list of BUTTONCONDACTION values to the stream.
     *
     * @param list List of BUTTONCONDACTION values
     * @throws IOException On I/O error
     */
    public void writeBUTTONCONDACTIONList(List<BUTTONCONDACTION> list) throws IOException {
        for (int i = 0; i < list.size(); i++) {
            writeBUTTONCONDACTION(list.get(i), i == list.size() - 1);
        }
    }

    /**
     * Writes BUTTONCONDACTION value to the stream.
     *
     * @param value BUTTONCONDACTION value
     * @param isLast True if it is last on the list
     * @throws IOException On I/O error
     */
    public void writeBUTTONCONDACTION(BUTTONCONDACTION value, boolean isLast) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SWFOutputStream sos = new SWFOutputStream(baos, version, charset)) {
            sos.writeUB(1, value.condIdleToOverDown ? 1 : 0);
            sos.writeUB(1, value.condOutDownToIdle ? 1 : 0);
            sos.writeUB(1, value.condOutDownToOverDown ? 1 : 0);
            sos.writeUB(1, value.condOverDownToOutDown ? 1 : 0);
            sos.writeUB(1, value.condOverDownToOverUp ? 1 : 0);
            sos.writeUB(1, value.condOverUpToOverDown ? 1 : 0);
            sos.writeUB(1, value.condOverUpToIddle ? 1 : 0);
            sos.writeUB(1, value.condIdleToOverUp ? 1 : 0);
            sos.writeUB(7, value.condKeyPress);
            sos.writeUB(1, value.condOverDownToIdle ? 1 : 0);
            sos.write(value.actionBytes);
        }
        byte[] data = baos.toByteArray();
        if (isLast) {
            writeUI16(0);
        } else {
            writeUI16(data.length + 2);
        }
        write(data);
    }

    /**
     * Writes FILLSTYLE value to the stream.
     *
     * @param value FILLSTYLE value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeFILLSTYLE(FILLSTYLE value, int shapeNum) throws IOException {
        writeUI8(value.fillStyleType);
        if (value.fillStyleType == FILLSTYLE.SOLID) {
            if (shapeNum >= 3) {
                writeRGBA((RGBA) value.color);
            } else if (shapeNum == 1 || shapeNum == 2) {
                writeRGB(value.color);
            }
        }
        if ((value.fillStyleType == FILLSTYLE.LINEAR_GRADIENT)
                || (value.fillStyleType == FILLSTYLE.RADIAL_GRADIENT)
                || (value.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT)) {
            writeMatrix(value.gradientMatrix);
        }
        if ((value.fillStyleType == FILLSTYLE.LINEAR_GRADIENT)
                || (value.fillStyleType == FILLSTYLE.RADIAL_GRADIENT)) {
            writeGRADIENT(value.gradient, shapeNum);
        } else if (value.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
            writeFOCALGRADIENT((FOCALGRADIENT) value.gradient, shapeNum);
        }

        if ((value.fillStyleType == FILLSTYLE.REPEATING_BITMAP)
                || (value.fillStyleType == FILLSTYLE.CLIPPED_BITMAP)
                || (value.fillStyleType == FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP)
                || (value.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP)) {
            writeUI16(value.bitmapId);
            writeMatrix(value.bitmapMatrix);
        }
    }

    /**
     * Writes FILLSTYLEARRAY value to the stream.
     *
     * @param value FILLSTYLEARRAY value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeFILLSTYLEARRAY(FILLSTYLEARRAY value, int shapeNum) throws IOException {
        int fillStyleCount = value.fillStyles.length;
        if (shapeNum > 1) {
            if (fillStyleCount >= 0xff) {
                writeUI8(0xff);
                writeUI16(fillStyleCount);
            } else {
                writeUI8(fillStyleCount);
            }
        } else {
            writeUI8(fillStyleCount);
        }
        for (int i = 0; i < value.fillStyles.length; i++) {
            writeFILLSTYLE(value.fillStyles[i], shapeNum);
        }
    }

    /**
     * Writes FOCALGRADIENT value to the stream.
     *
     * @param value FILLSTYLEARRAY value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeFOCALGRADIENT(FOCALGRADIENT value, int shapeNum) throws IOException {
        writeUB(2, value.spreadMode);
        writeUB(2, value.interpolationMode);
        writeUB(4, value.gradientRecords.length);
        for (int i = 0; i < value.gradientRecords.length; i++) {
            writeGRADRECORD(value.gradientRecords[i], shapeNum);
        }
        writeFIXED8(value.focalPoint);
    }

    /**
     * Writes GRADIENT value to the stream.
     *
     * @param value GRADIENT value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeGRADIENT(GRADIENT value, int shapeNum) throws IOException {
        writeUB(2, value.spreadMode);
        writeUB(2, value.interpolationMode);
        writeUB(4, value.gradientRecords.length);
        for (int i = 0; i < value.gradientRecords.length; i++) {
            writeGRADRECORD(value.gradientRecords[i], shapeNum);
        }
    }

    /**
     * Writes GRADRECORD value to the stream.
     *
     * @param value GRADRECORD value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeGRADRECORD(GRADRECORD value, int shapeNum) throws IOException {
        writeUI8(value.ratio);
        if (shapeNum >= 3) {
            writeRGBA((RGBA) value.color);
        } else {
            writeRGB(value.color);
        }
    }

    /**
     * Writes LINESTYLE value to the stream.
     *
     * @param value LINESTYLE value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeLINESTYLE(LINESTYLE value, int shapeNum) throws IOException {
        writeUI16(value.width);
        if (shapeNum == 1 || shapeNum == 2) {
            writeRGB(value.color);
        } else if (shapeNum == 3) {
            writeRGBA((RGBA) value.color);
        }
    }

    /**
     * Writes LINESTYLE2 value to the stream.
     *
     * @param value LINESTYLE2 value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeLINESTYLE2(LINESTYLE2 value, int shapeNum) throws IOException {
        writeUI16(value.width);
        writeUB(2, value.startCapStyle);
        writeUB(2, value.joinStyle);
        writeUB(1, value.hasFillFlag ? 1 : 0);
        writeUB(1, value.noHScaleFlag ? 1 : 0);
        writeUB(1, value.noVScaleFlag ? 1 : 0);
        writeUB(1, value.pixelHintingFlag ? 1 : 0);
        writeUB(5, value.reserved);
        writeUB(1, value.noClose ? 1 : 0);
        writeUB(2, value.endCapStyle);
        if (value.joinStyle == LINESTYLE2.MITER_JOIN) {
            writeFIXED8(value.miterLimitFactor);
        }
        if (!value.hasFillFlag) {
            writeRGBA((RGBA) value.color);
        } else {
            writeFILLSTYLE(value.fillType, shapeNum);
        }
    }

    /**
     * Writes LINESTYLEARRAY value to the stream.
     *
     * @param value FILLSTYLEARRAY value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeLINESTYLEARRAY(LINESTYLEARRAY value, int shapeNum) throws IOException {
        int lineStyleCount;
        if (shapeNum <= 3) {
            lineStyleCount = value.lineStyles.length;
            if (lineStyleCount >= 0xff) {
                writeUI8(0xff);
                writeUI16(lineStyleCount);
            } else {
                writeUI8(lineStyleCount);
            }
            for (int i = 0; i < lineStyleCount; i++) {
                writeLINESTYLE((LINESTYLE) value.lineStyles[i], shapeNum);
            }
        } else {
            lineStyleCount = value.lineStyles2.length;
            if (lineStyleCount >= 0xff) {
                writeUI8(0xff);
                writeUI16(lineStyleCount);
            } else {
                writeUI8(lineStyleCount);
            }
            for (int i = 0; i < lineStyleCount; i++) {
                writeLINESTYLE2((LINESTYLE2) value.lineStyles2[i], shapeNum);
            }
        }
    }

    /**
     * Writes SHAPE value to the stream.
     *
     * @param value SHAPE value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeSHAPE(SHAPE value, int shapeNum) throws IOException {
        calculateSHAPEFillLineBits(value);
        writeUB(4, value.numFillBits);
        writeUB(4, value.numLineBits);
        writeSHAPERECORDS(value.shapeRecords, value.numFillBits, value.numLineBits, shapeNum);
    }

    /**
     * Calculates SHAPE fill and line bits.
     *
     * @param value SHAPE value
     */
    private void calculateSHAPEFillLineBits(SHAPE value) {
        int numFillBits = 0;
        int numLineBits = 0;

        for (SHAPERECORD r : value.shapeRecords) {
            if (r instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) r;
                if (scr.stateFillStyle0) {
                    numFillBits = Math.max(numFillBits, getNeededBitsU(scr.fillStyle0));
                }
                if (scr.stateFillStyle1) {
                    numFillBits = Math.max(numFillBits, getNeededBitsU(scr.fillStyle1));
                }
                if (scr.stateLineStyle) {
                    numLineBits = Math.max(numLineBits, getNeededBitsU(scr.lineStyle));
                }
                if (scr.stateNewStyles) {
                    break;
                }
            }
        }
        if (Configuration._debugCopy.get()) {
            numFillBits = Math.max(numFillBits, value.numFillBits);
            numLineBits = Math.max(numLineBits, value.numLineBits);
        }

        value.numFillBits = numFillBits;
        value.numLineBits = numLineBits;
    }

    /**
     * Writes SHAPEWITHSTYLE value to the stream.
     *
     * @param value SHAPEWITHSTYLE value
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    public void writeSHAPEWITHSTYLE(SHAPEWITHSTYLE value, int shapeNum) throws IOException {
        writeFILLSTYLEARRAY(value.fillStyles, shapeNum);
        writeLINESTYLEARRAY(value.lineStyles, shapeNum);
        calculateSHAPEFillLineBits(value);
        writeUB(4, value.numFillBits);
        writeUB(4, value.numLineBits);
        writeSHAPERECORDS(value.shapeRecords, value.numFillBits, value.numLineBits, shapeNum);
    }

    /**
     * Writes SHAPERECORDs value to the stream.
     *
     * @param value SHAPERECORDS value
     * @param fillBits Fill bits
     * @param lineBits Line bits
     * @param shapeNum 1 in DefineShape, 2 in DefineShape2,...
     * @throws IOException On I/O error
     */
    private void writeSHAPERECORDS(List<SHAPERECORD> value, int fillBits, int lineBits, int shapeNum) throws IOException {
        for (SHAPERECORD sh : value) {
            if (sh instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) sh;
                writeUB(1, 1); // typeFlag
                writeUB(1, 0); // curvedEdge
                int numBits = Math.max(getNeededBitsS(cer.controlDeltaX, cer.controlDeltaY, cer.anchorDeltaX, cer.anchorDeltaY) - 2, 0);
                if (Configuration._debugCopy.get()) {
                    numBits = Math.max(numBits, cer.numBits);
                }

                cer.numBits = numBits;
                writeUB(4, cer.numBits);
                writeSB(cer.numBits + 2, cer.controlDeltaX);
                writeSB(cer.numBits + 2, cer.controlDeltaY);
                writeSB(cer.numBits + 2, cer.anchorDeltaX);
                writeSB(cer.numBits + 2, cer.anchorDeltaY);
            } else if (sh instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) sh;
                writeUB(1, 1); // typeFlag
                writeUB(1, 1); // straightEdge
                int numBits = Math.max(getNeededBitsS(ser.deltaX, ser.deltaY) - 2, 0);
                if (Configuration._debugCopy.get()) {
                    numBits = Math.max(numBits, ser.numBits);
                }

                ser.numBits = numBits;
                writeUB(4, ser.numBits);
                writeUB(1, ser.generalLineFlag ? 1 : 0);
                if (!ser.generalLineFlag) {
                    writeUB(1, ser.vertLineFlag ? 1 : 0);
                }
                if (ser.generalLineFlag || (!ser.vertLineFlag)) {
                    writeSB(ser.numBits + 2, ser.deltaX);
                }
                if (ser.generalLineFlag || ser.vertLineFlag) {
                    writeSB(ser.numBits + 2, ser.deltaY);
                }
            } else if (sh instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) sh;
                writeUB(1, 0); // typeFlag
                writeUB(1, scr.stateNewStyles ? 1 : 0);
                writeUB(1, scr.stateLineStyle ? 1 : 0);
                writeUB(1, scr.stateFillStyle1 ? 1 : 0);
                writeUB(1, scr.stateFillStyle0 ? 1 : 0);
                writeUB(1, scr.stateMoveTo ? 1 : 0);
                if (scr.stateMoveTo) {
                    int moveBits = getNeededBitsS(scr.moveDeltaX, scr.moveDeltaY);
                    if (Configuration._debugCopy.get()) {
                        moveBits = Math.max(moveBits, scr.moveBits);
                    }

                    scr.moveBits = moveBits;
                    writeUB(5, scr.moveBits);
                    writeSB(scr.moveBits, scr.moveDeltaX);
                    writeSB(scr.moveBits, scr.moveDeltaY);
                }
                if (scr.stateFillStyle0) {
                    writeUB(fillBits, scr.fillStyle0);
                }
                if (scr.stateFillStyle1) {
                    writeUB(fillBits, scr.fillStyle1);
                }
                if (scr.stateLineStyle) {
                    writeUB(lineBits, scr.lineStyle);
                }
                if (scr.stateNewStyles) {
                    writeFILLSTYLEARRAY(scr.fillStyles, shapeNum);
                    writeLINESTYLEARRAY(scr.lineStyles, shapeNum);
                    fillBits = getNeededBitsU(scr.fillStyles.fillStyles.length);
                    lineBits = getNeededBitsU(shapeNum <= 3 ? scr.lineStyles.lineStyles.length : scr.lineStyles.lineStyles2.length);

                    if (Configuration._debugCopy.get()) {
                        fillBits = Math.max(fillBits, scr.numFillBits);
                        lineBits = Math.max(lineBits, scr.numLineBits);
                    }

                    scr.numFillBits = fillBits;
                    scr.numLineBits = lineBits;
                    writeUB(4, scr.numFillBits);
                    writeUB(4, scr.numLineBits);
                }

            } else if (sh instanceof EndShapeRecord) {
                writeUB(1, 0); // typeFlag
                writeUB(5, 0); // end of shape flag
            }
        }
        alignByte();
    }

    /**
     * Writes SOUNDINFO value to the stream.
     *
     * @param value SOUNDINFO value
     * @throws IOException On I/O error
     */
    public void writeSOUNDINFO(SOUNDINFO value) throws IOException {
        writeUB(2, value.reserved);
        writeUB(1, value.syncStop ? 1 : 0);
        writeUB(1, value.syncNoMultiple ? 1 : 0);
        writeUB(1, value.hasEnvelope ? 1 : 0);
        writeUB(1, value.hasLoops ? 1 : 0);
        writeUB(1, value.hasOutPoint ? 1 : 0);
        writeUB(1, value.hasInPoint ? 1 : 0);
        if (value.hasInPoint) {
            writeUI32(value.inPoint);
        }
        if (value.hasOutPoint) {
            writeUI32(value.outPoint);
        }
        if (value.hasLoops) {
            writeUI16(value.loopCount);
        }
        if (value.hasEnvelope) {
            writeUI8(value.envelopeRecords.length);
            for (SOUNDENVELOPE env : value.envelopeRecords) {
                writeSOUNDENVELOPE(env);
            }
        }
    }

    /**
     * Writes SOUNDENVELOPE value to the stream.
     *
     * @param value SOUNDENVELOPE value
     * @throws IOException On I/O error
     */
    public void writeSOUNDENVELOPE(SOUNDENVELOPE value) throws IOException {
        writeUI32(value.pos44);
        writeUI16(value.leftLevel);
        writeUI16(value.rightLevel);
    }

    /**
     * Writes TEXTRECORD value to the stream.
     *
     * @param value TEXTRECORD value
     * @param defineTextNum 1 in DefineText, 2 in DefineText2,...
     * @param glyphBits Glyph bits
     * @param advanceBits Advance bits
     * @throws IOException On I/O error
     */
    public void writeTEXTRECORD(TEXTRECORD value, int defineTextNum, int glyphBits, int advanceBits) throws IOException {
        writeUB(1, 1);
        writeUB(3, 0);
        writeUB(1, value.styleFlagsHasFont ? 1 : 0);
        writeUB(1, value.styleFlagsHasColor ? 1 : 0);
        writeUB(1, value.styleFlagsHasYOffset ? 1 : 0);
        writeUB(1, value.styleFlagsHasXOffset ? 1 : 0);
        if (value.styleFlagsHasFont) {
            writeUI16(value.fontId);
        }
        if (value.styleFlagsHasColor) {
            if (defineTextNum == 2) {
                writeRGBA(value.textColorA);
            } else {
                writeRGB(value.textColor);
            }
        }
        if (value.styleFlagsHasXOffset) {
            writeSI16(value.xOffset);
        }
        if (value.styleFlagsHasYOffset) {
            writeSI16(value.yOffset);
        }
        if (value.styleFlagsHasFont) {
            writeUI16(value.textHeight);
        }
        writeUI8(value.glyphEntries.size());
        for (GLYPHENTRY ge : value.glyphEntries) {
            writeGLYPHENTRY(ge, glyphBits, advanceBits);
        }
        alignByte();
    }

    /**
     * Writes GLYPHENTRY value to the stream.
     *
     * @param value GLYPHENTRY value
     * @param glyphBits Glyph bits
     * @param advanceBits Advance bits
     * @throws IOException On I/O error
     */
    public void writeGLYPHENTRY(GLYPHENTRY value, int glyphBits, int advanceBits) throws IOException {
        writeUB(glyphBits, value.glyphIndex);
        writeSB(advanceBits, value.glyphAdvance);
    }

    /**
     * Writes MORPHFILLSTYLE value to the stream.
     *
     * @param value MORPHFILLSTYLE value
     * @param shapeNum 1 in DefineMorphShape, 2 in DefineMorphShape2,...
     * @throws IOException On I/O error
     */
    public void writeMORPHFILLSTYLE(MORPHFILLSTYLE value, int shapeNum) throws IOException {
        writeUI8(value.fillStyleType);
        if (value.fillStyleType == MORPHFILLSTYLE.SOLID) {
            writeRGBA(value.startColor);
            writeRGBA(value.endColor);
        }
        if ((value.fillStyleType == MORPHFILLSTYLE.LINEAR_GRADIENT)
                || (value.fillStyleType == MORPHFILLSTYLE.RADIAL_GRADIENT)
                || (value.fillStyleType == MORPHFILLSTYLE.FOCAL_RADIAL_GRADIENT)) {
            writeMatrix(value.startGradientMatrix);
            writeMatrix(value.endGradientMatrix);
        }
        if ((value.fillStyleType == MORPHFILLSTYLE.LINEAR_GRADIENT)
                || (value.fillStyleType == MORPHFILLSTYLE.RADIAL_GRADIENT)) {
            writeMORPHGRADIENT(value.gradient, shapeNum);
        }
        if (value.fillStyleType == MORPHFILLSTYLE.FOCAL_RADIAL_GRADIENT) {
            writeMORPHFOCALGRADIENT((MORPHFOCALGRADIENT) value.gradient, shapeNum);
        }

        if ((value.fillStyleType == MORPHFILLSTYLE.REPEATING_BITMAP)
                || (value.fillStyleType == MORPHFILLSTYLE.CLIPPED_BITMAP)
                || (value.fillStyleType == MORPHFILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP)
                || (value.fillStyleType == MORPHFILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP)) {
            writeUI16(value.bitmapId);
            writeMatrix(value.startBitmapMatrix);
            writeMatrix(value.endBitmapMatrix);
        }
    }

    /**
     * WritesMORPH FILLSTYLEARRAY value to the stream.
     *
     * @param value MORPHFILLSTYLEARRAY value
     * @param morphShapeNum 1 on DefineMorphShape, 2 on DefineMorphShape
     * @throws IOException On I/O error
     */
    public void writeMORPHFILLSTYLEARRAY(MORPHFILLSTYLEARRAY value, int morphShapeNum) throws IOException {
        int fillStyleCount = value.fillStyles.length;
        if (fillStyleCount >= 0xff) {
            writeUI8(0xff);
            writeUI16(fillStyleCount);
        } else {
            writeUI8(fillStyleCount);
        }
        for (int i = 0; i < value.fillStyles.length; i++) {
            writeMORPHFILLSTYLE(value.fillStyles[i], morphShapeNum);
        }
    }

    /**
     * Writes MORPHGRADIENT value to the stream.
     *
     * @param value MORPHGRADIENT value
     * @param shapeNum 1 in DefineMorphShape, 2 in DefineMorphShape2,...
     * @throws IOException On I/O error
     */
    public void writeMORPHGRADIENT(MORPHGRADIENT value, int shapeNum) throws IOException {
        // Despite of documentation (UI8 1-8), there are two fields
        // spreadMode and interpolationMode which are same as in GRADIENT
        writeUB(2, value.spreadMode);
        writeUB(2, value.interpolationMode);
        writeUB(4, value.gradientRecords.length);
        for (int i = 0; i < value.gradientRecords.length; i++) {
            writeMORPHGRADRECORD(value.gradientRecords[i]);
        }
    }

    /**
     * Writes MORPHFOCALGRADIENT value to the stream.
     *
     * <p>
     * Undocumented feature
     *
     * @param value MORPHGRADIENT value
     * @param shapeNum 1 in DefineMorphShape, 2 in DefineMorphShape2,...
     * @throws IOException On I/O error
     */
    public void writeMORPHFOCALGRADIENT(MORPHFOCALGRADIENT value, int shapeNum) throws IOException {
        writeUB(2, value.spreadMode);
        writeUB(2, value.interpolationMode);
        writeUB(4, value.gradientRecords.length);
        for (int i = 0; i < value.gradientRecords.length; i++) {
            writeMORPHGRADRECORD(value.gradientRecords[i]);
        }
        writeFIXED8(value.startFocalPoint);
        writeFIXED8(value.endFocalPoint);
    }

    /**
     * Writes MORPHGRADRECORD value to the stream.
     *
     * @param value MORPHGRADRECORD value
     * @throws IOException On I/O error
     */
    public void writeMORPHGRADRECORD(MORPHGRADRECORD value) throws IOException {
        writeUI8(value.startRatio);
        writeRGBA(value.startColor);
        writeUI8(value.endRatio);
        writeRGBA(value.endColor);
    }

    /**
     * Writes MORPHLINESTYLE value to the stream.
     *
     * @param value LINESTYLE value
     * @param shapeNum 1 in DefineMorphShape, 2 in DefineMorphShape2,...
     * @throws IOException On I/O error
     */
    public void writeMORPHLINESTYLE(MORPHLINESTYLE value, int shapeNum) throws IOException {
        writeUI16(value.startWidth);
        writeUI16(value.endWidth);
        writeRGBA(value.startColor);
        writeRGBA(value.endColor);

    }

    /**
     * Writes MORPHLINESTYLE2 value to the stream.
     *
     * @param value MORPHLINESTYLE2 value
     * @param shapeNum 1 in DefineMorphShape, 2 in DefineMorphShape2,...
     * @throws IOException On I/O error
     */
    public void writeMORPHLINESTYLE2(MORPHLINESTYLE2 value, int shapeNum) throws IOException {
        writeUI16(value.startWidth);
        writeUI16(value.endWidth);
        writeUB(2, value.startCapStyle);
        writeUB(2, value.joinStyle);
        writeUB(1, value.hasFillFlag ? 1 : 0);
        writeUB(1, value.noHScaleFlag ? 1 : 0);
        writeUB(1, value.noVScaleFlag ? 1 : 0);
        writeUB(1, value.pixelHintingFlag ? 1 : 0);
        writeUB(5, value.reserved);
        writeUB(1, value.noClose ? 1 : 0);
        writeUB(2, value.endCapStyle);
        if (value.joinStyle == LINESTYLE2.MITER_JOIN) {
            writeFIXED8(value.miterLimitFactor);
        }
        if (!value.hasFillFlag) {
            writeRGBA(value.startColor);
            writeRGBA(value.endColor);
        } else {
            writeMORPHFILLSTYLE(value.fillType, shapeNum);
        }
    }

    /**
     * Writes MORPHLINESTYLEARRAY value to the stream.
     *
     * @param value MORPHFILLSTYLEARRAY value
     * @param morphShapeNum 1 in DefineMorphShape, 2 in DefineMorphShape2,...
     * @throws IOException On I/O error
     */
    public void writeMORPHLINESTYLEARRAY(MORPHLINESTYLEARRAY value, int morphShapeNum) throws IOException {
        int lineStyleCount;
        if (morphShapeNum == 1) {
            lineStyleCount = value.lineStyles.length;
            if (lineStyleCount >= 0xff) {
                writeUI8(0xff);
                writeUI16(lineStyleCount);
            } else {
                writeUI8(lineStyleCount);
            }
            for (int i = 0; i < lineStyleCount; i++) {
                writeMORPHLINESTYLE(value.lineStyles[i], morphShapeNum);
            }
        } else if (morphShapeNum == 2) {
            lineStyleCount = value.lineStyles2.length;
            if (lineStyleCount >= 0xff) {
                writeUI8(0xff);
                writeUI16(lineStyleCount);
            } else {
                writeUI8(lineStyleCount);
            }
            for (int i = 0; i < lineStyleCount; i++) {
                writeMORPHLINESTYLE2(value.lineStyles2[i], morphShapeNum);
            }
        }
    }

    /**
     * Writes KERNINGRECORD value to the stream.
     *
     * @param value KERNINGRECORD value
     * @param fontFlagsWideCodes Font flags wide codes
     * @throws IOException On I/O error
     */
    public void writeKERNINGRECORD(KERNINGRECORD value, boolean fontFlagsWideCodes) throws IOException {
        if (fontFlagsWideCodes) {
            writeUI16(value.fontKerningCode1);
            writeUI16(value.fontKerningCode2);
        } else {
            writeUI8(value.fontKerningCode1);
            writeUI8(value.fontKerningCode2);
        }
        writeSI16(value.fontKerningAdjustment);
    }

    /**
     * Writes LANGCODE value to the stream.
     *
     * @param value LANGCODE value
     * @throws IOException On I/O error
     */
    public void writeLANGCODE(LANGCODE value) throws IOException {
        writeUI8(value.languageCode);
    }

    /**
     * Writes ZONERECORD value to the stream.
     *
     * @param value ZONERECORD value
     * @throws IOException On I/O error
     */
    public void writeZONERECORD(ZONERECORD value) throws IOException {
        writeUI8(value.zonedata.length);
        for (int i = 0; i < value.zonedata.length; i++) {
            writeZONEDATA(value.zonedata[i]);
        }
        writeUB(6, 0);
        writeUB(1, value.zoneMaskY ? 1 : 0);
        writeUB(1, value.zoneMaskX ? 1 : 0);
    }

    /**
     * Writes ZONEDATA value to the stream.
     *
     * @param value ZONEDATA value
     * @throws IOException On I/O error
     */
    public void writeZONEDATA(ZONEDATA value) throws IOException {
        writeUI16(value.alignmentCoordinate);
        writeUI16(value.range);
    }

    /**
     * Writes ZLIB compressed data to the stream.
     *
     * @param data Data to compress
     * @throws IOException On I/O error
     */
    public void writeBytesZlib(byte[] data) throws IOException {
        DeflaterOutputStream deflater = new DeflaterOutputStream(this, new Deflater(9));
        deflater.write(data);
        deflater.finish();
    }

    /**
     * Compresses byte array using ZLIB.
     *
     * @param data Data to compress
     * @return Compressed data
     * @throws IOException On I/O error
     */
    public static byte[] compressByteArray(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream deflater = new DeflaterOutputStream(baos, new Deflater(9));
        deflater.write(data);
        deflater.finish();
        return baos.toByteArray();
    }

    /**
     * Reads one BITMAPDATA value from the stream.
     *
     * @param value BITMAPDATA value
     * @param bitmapFormat Format of the bitmap
     * @param bitmapWidth Width of the bitmap
     * @param bitmapHeight Height of the bitmap
     * @throws IOException On I/O error
     */
    public void writeBITMAPDATA(BITMAPDATA value, int bitmapFormat, int bitmapWidth, int bitmapHeight) throws IOException {
        int dataLen = 0;
        int pos = 0;
        for (int y = 0; y < bitmapHeight; y++) {
            int x = 0;
            for (; x < bitmapWidth; x++) {
                if (bitmapFormat == DefineBitsLosslessTag.FORMAT_15BIT_RGB) {
                    dataLen += 2;
                    writePIX15(value.bitmapPixelDataPix15[pos]);
                }
                if (bitmapFormat == DefineBitsLosslessTag.FORMAT_24BIT_RGB) {
                    dataLen += 4;
                    writePIX24(value.bitmapPixelDataPix24[pos]);
                }
                pos++;
            }
            while ((dataLen % 4) != 0) {
                dataLen++;
                writeUI8(0);
            }
        }
    }

    /**
     * Reads one ALPHABITMAPDATA value from the stream.
     *
     * @param value ALPHABITMAPDATA value
     * @param bitmapFormat Format of the bitmap
     * @param bitmapWidth Width of the bitmap
     * @param bitmapHeight Height of the bitmap
     * @throws IOException On I/O error
     */
    public void writeALPHABITMAPDATA(ALPHABITMAPDATA value, int bitmapFormat, int bitmapWidth, int bitmapHeight) throws IOException {
        int pos = 0;
        for (int y = 0; y < bitmapHeight; y++) {
            for (int x = 0; x < bitmapWidth; x++) {
                writeARGB(value.bitmapPixelData[pos]);
                pos++;
            }
        }
    }

    /**
     * Writes PIX24 value to the stream.
     *
     * @param value PIX24 value
     * @throws IOException On I/O error
     */
    public void writePIX24(PIX24 value) throws IOException {
        writeUI8(value.reserved);
        writeUI8(value.red);
        writeUI8(value.green);
        writeUI8(value.blue);
    }

    /**
     * Writes PIX24 value to the stream.
     *
     * @param value PIX24 value
     * @throws IOException On I/O error
     */
    public void writePIX24(int value) throws IOException {
        writeUI8((value >> 24) & 0xff);
        writeUI8((value >> 16) & 0xff);
        writeUI8((value >> 8) & 0xff);
        writeUI8(value & 0xff);
    }

    /**
     * Writes PIX15 value to the stream.
     *
     * @param value PIX15 value
     * @throws IOException On I/O error
     */
    public void writePIX15(PIX15 value) throws IOException {
        writeUB(1, value.reserved);
        writeUB(5, value.red);
        writeUB(5, value.green);
        writeUB(5, value.blue);
    }

    /**
     * Writes PIX15 value to the stream.
     *
     * @param value PIX15 value
     * @throws IOException On I/O error
     */
    public void writePIX15(int value) throws IOException {
        writeUB(1, (value >> 24) & 0xff);
        writeUB(5, (value >> 19) & 0xff);
        writeUB(5, (value >> 11) & 0xff);
        writeUB(5, (value >> 3) & 0xff);
    }

    /**
     * Writes AMF3 encoded value. Warning: Correct serializer needs to be passed
     * as second parameter when using IExternalizable.
     *
     * @param value AMF3 encoded value
     * @param serializers Map className=>Serializer for classes implementing
     * IExternalizable
     * @throws IOException On I/O error
     * @throws NoSerializerExistsException When no serializer exists for the value
     */
    public void writeAmf3Object(Amf3Value value, Map<String, ObjectTypeSerializeHandler> serializers) throws IOException, NoSerializerExistsException {
        Amf3OutputStream ao = new Amf3OutputStream(os);
        ao.writeValue(value.getValue(), serializers);
    }

    /**
     * Writes AMF3 encoded value. Warning: When the object implements
     * IExternalizable, you need to pass serializer as second parameter.
     *
     * @param value AMF3 encoded value
     * @throws IOException On I/O error
     * @throws NoSerializerExistsException When no serializer exists for the value
     */
    public void writeAmf3Object(Amf3Value value) throws IOException, NoSerializerExistsException {
        writeAmf3Object(value, new HashMap<>());
    }
}
