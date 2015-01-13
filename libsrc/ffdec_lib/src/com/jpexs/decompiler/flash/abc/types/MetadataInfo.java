/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.helpers.Helper;

public class MetadataInfo {

    public int name_index;
    public int[] keys;
    public int[] values;

    public MetadataInfo(int name_index, int[] keys, int[] values) {
        this.name_index = name_index;
        this.keys = keys;
        this.values = values;
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " keys=" + Helper.intArrToString(keys) + " values=" + Helper.intArrToString(values);
    }

    public String toString(AVM2ConstantPool constants) {
        String s = "name=" + constants.getString(name_index);
        if (keys.length > 0) {
            s += "\r\n";
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == 0) {
                s += "\"" + constants.getString(values[i]) + "\"\r\n";
            } else {
                s += "\"" + constants.getString(keys[i]) + "\"=\"" + constants.getString(values[i]) + "\"\r\n";
            }
        }
        return s;
    }
}
