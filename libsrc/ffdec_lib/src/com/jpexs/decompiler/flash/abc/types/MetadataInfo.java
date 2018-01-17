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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.helpers.Helper;

/**
 *
 * @author JPEXS
 */
public class MetadataInfo {

    public int name_index;

    public int[] keys;

    public int[] values;

    public MetadataInfo() {
        this.name_index = 0;
        this.keys = new int[0];
        this.values = new int[0];
    }

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
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(constants.getString(name_index));
        if (keys.length > 0) {
            sb.append("\r\n");
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == 0) {
                sb.append("\"").append(constants.getString(values[i])).append("\"\r\n");
            } else {
                sb.append("\"").append(constants.getString(keys[i])).append("\"=\"").append(constants.getString(values[i])).append("\"\r\n");
            }
        }
        return sb.toString();
    }
}
