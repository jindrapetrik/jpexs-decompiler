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
package com.jpexs.helpers;

import com.jpexs.decompiler.flash.SWFInputStream;
import java.io.Serializable;

/**
 * Byte array range.
 *
 * @author JPEXS
 */
public class ByteArrayRange implements Serializable {

    /**
     * Empty byte array range.
     */
    public static final ByteArrayRange EMPTY = new ByteArrayRange(SWFInputStream.BYTE_ARRAY_EMPTY);

    private final byte[] array;

    private final int pos;

    private final int length;

    /**
     * Constructor.
     * @param array Byte array
     */
    public ByteArrayRange(byte[] array) {
        this.array = array;
        this.pos = 0;
        this.length = array.length;
    }

    /**
     * Constructor.
     * @param array Byte array
     * @param pos Position
     * @param length Length
     */
    public ByteArrayRange(byte[] array, int pos, int length) {
        this.array = array;
        this.pos = pos;
        this.length = length;
    }

    /**
     * Constructor.
     * @param hexString Hex string
     */
    public ByteArrayRange(String hexString) {
        byte[] array = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length() / 2; i++) {
            array[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        this.array = array;
        this.pos = 0;
        this.length = array.length;
    }

    /**
     * Gets the array.
     * @return The byte array
     */
    public byte[] getArray() {
        return array;
    }

    /**
     * Gets position.
     * @return Position
     */
    public int getPos() {
        return pos;
    }

    /**
     * Gets length.
     * @return Length
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets byte at index.
     * @param index Index
     * @return Byte at index
     */
    public byte get(int index) {
        return array[pos + index];
    }

    /**
     * Gets range data.
     * @return Range data
     */
    public byte[] getRangeData() {
        byte[] data = new byte[length];
        System.arraycopy(array, pos, data, 0, length);
        return data;
    }

    /**
     * Gets range data.
     * @param pos Position
     * @param length Length
     * @return Range data
     */
    public byte[] getRangeData(int pos, int length) {
        byte[] data = new byte[length];
        System.arraycopy(array, this.pos + pos, data, 0, length);
        return data;
    }

    /**
     * Gets sub range.
     * @param pos Position
     * @param length Length
     * @return Sub range
     */
    public ByteArrayRange getSubRange(int pos, int length) {
        return new ByteArrayRange(array, this.pos + pos, length);
    }
}
