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

/**
 * Unparsed data.
 *
 * @author JPEXS
 */
public class UnparsedDATA extends DATA {

    /**
     * Data type
     */
    private int dataType;
    /**
     * Data
     */
    private byte[] data;

    /**
     * Constructor.
     * @param dataType Data type
     * @param data Data
     */
    public UnparsedDATA(int dataType, byte[] data) {
        this.dataType = dataType;
        this.data = data;
    }

    @Override
    public byte[] getBytes() {
        return this.data;
    }

    /**
     * Get data type.
     * @return Data type
     */
    public int getDataType() {
        return dataType;
    }

}
