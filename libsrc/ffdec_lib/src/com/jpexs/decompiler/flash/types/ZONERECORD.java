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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import java.io.Serializable;

/**
 * Represents Zone record
 *
 * @author JPEXS
 */
public class ZONERECORD implements Serializable {

    @SWFArray(value = "zone", countField = "numZoneData")
    public ZONEDATA[] zonedata = new ZONEDATA[0];

    public boolean zoneMaskX;

    public boolean zoneMaskY;

    @Override
    public String toString() {
        String ret = "[ZONERECORD data:";
        for (int i = 0; i < zonedata.length; i++) {
            if (i > 0) {
                ret += ", ";
            }
            ret += zonedata[i];
        }
        return ret + ", zoneMaskX:" + zoneMaskX + ", zoneMaskY:" + zoneMaskY + "]";
    }
}
