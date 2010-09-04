/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.helpers.Helper;


public class MetadataInfo {

    public int name_index;
    public int keys[];
    public int values[];

    public MetadataInfo(int name_index, int[] keys, int[] values) {
        this.name_index = name_index;
        this.keys = keys;
        this.values = values;
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " keys=" + Helper.intArrToString(keys) + " values=" + Helper.intArrToString(values);
    }

    public String toString(ConstantPool constants) {
        String s = "name=" + constants.constant_string[name_index];
        if (keys.length > 0) s += "\r\n";
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == 0) {
                s += "\"" + constants.constant_string[values[i]] + "\"\r\n";
            } else {
                s += "\"" + constants.constant_string[keys[i]] + "\"=\"" + constants.constant_string[values[i]] + "\"\r\n";
            }
        }
        return s;
    }

}