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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.dumpview;

import com.jpexs.decompiler.flash.tags.TagStub;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DumpInfo {

    public String name;

    public String type;

    public TagStub tagToResolve = null;

    public Object previewValue;

    public long startByte;

    public int startBit;

    public long lengthBytes;

    public int lengthBits;

    public DumpInfo parent;

    private List<DumpInfo> childInfos;

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

    public int getChildCount() {
        return childInfos == null ? 0 : childInfos.size();
    }

    public List<DumpInfo> getChildInfos() {
        if (childInfos == null) {
            childInfos = new ArrayList<>();
        }
        return childInfos;
    }

    public long getEndByte() {
        int end = (int) startByte;
        if (lengthBytes != 0) {
            end += lengthBytes;
        } else {
            int bits = startBit + lengthBits;
            end += bits / 8;
            if (bits % 8 != 0) {
                end++;
            }
        }
        return end - 1;
    }

    @Override
    public String toString() {
        String value = previewValue == null ? "" : previewValue.toString();
        return name + " (" + type + ")" + (value.isEmpty() ? "" : " = " + value);
    }
}
