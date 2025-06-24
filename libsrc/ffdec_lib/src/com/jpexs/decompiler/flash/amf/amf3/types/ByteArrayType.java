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
package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;

/**
 * AMF3 byte array type.
 */
public class ByteArrayType {

    private byte[] data;

    /**
     * Constructor.
     * @param data Byte array data
     */
    public ByteArrayType(byte[] data) {
        this.data = data;
    }

    /**
     * Gets byte array data.
     * @return Byte array data
     */
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return Amf3Exporter.amfToString(this);
    }

}
