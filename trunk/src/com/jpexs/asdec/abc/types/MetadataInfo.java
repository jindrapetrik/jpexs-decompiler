/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
