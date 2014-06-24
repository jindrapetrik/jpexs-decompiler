/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.dumpview;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DumpInfo {

    public String name;
    public String type;

    public Object previewValue;

    public long startByte;

    public int startBit;

    public long lengthBytes;

    public int lengthBits;

    public DumpInfo parent;

    public List<DumpInfo> childInfos = new ArrayList<>();

    public DumpInfo(String name, String type, Object value, long startByte, long lengthBytes) {

        this.name = name;
        this.type = type;
        this.previewValue = value;
        this.startByte = startByte;
        this.lengthBytes = lengthBytes;
    }

    public DumpInfo(String name, String type, Object value, long startByte, int startBit, long lengthBytes, int lengthBits) {

        this.name = name;
        this.type = type;
        this.previewValue = value;
        this.startByte = startByte;
        this.lengthBytes = lengthBytes;
        this.startBit = startBit;
        this.lengthBits = lengthBits;
    }

    @Override
    public String toString() {
        String value = previewValue == null ? "" : previewValue.toString();
        return name + " (" + type + ")" + (value.isEmpty() ? "" : " = " + value);
    }
}
