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
package com.jpexs.decompiler.flash.dumpview;

/**
 *
 * @author JPEXS
 */
public class DumpInfoSpecial extends DumpInfo {

    public DumpInfoSpecialType specialType;

    public Object specialValue;

    public DumpInfoSpecial(String name, String type, Object value, long startByte, int startBit, long lengthBytes, int lengthBits, DumpInfoSpecialType specialType) {
        super(name, type, value, startByte, startBit, lengthBytes, lengthBits);
        this.specialType = specialType;
    }

    public DumpInfoSpecial(String name, String type, Object value, long startByte, int startBit, long lengthBytes, int lengthBits, DumpInfoSpecialType specialType, Object specialValue) {
        super(name, type, value, startByte, startBit, lengthBytes, lengthBits);
        this.specialType = specialType;
        this.specialValue = specialValue;
    }
}
