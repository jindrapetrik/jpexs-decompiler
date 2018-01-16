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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagStub;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class DumpInfo implements TreeItem {

    public String name;

    public String type;

    public TagStub tagToResolve = null;

    public Tag resolvedTag = null;

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

    public void sortChildren() {
        if (childInfos == null) {
            return;
        }

        Collections.sort(childInfos, new Comparator<DumpInfo>() {
            @Override
            public int compare(DumpInfo o1, DumpInfo o2) {
                int res = Long.compare(o1.startByte, o2.startByte);
                if (res != 0) {
                    return res;
                }

                return Integer.compare(o1.startBit, o1.startBit);
            }
        });
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

    public void resolveTag() {
        if (tagToResolve != null) {
            TagStub tagStub = tagToResolve;
            try {
                SWFInputStream sis = tagStub.getDataStream();
                sis.seek(tagStub.getDataPos());
                sis.dumpInfo = this;
                resolvedTag = SWFInputStream.resolveTag(tagStub, 0, false, true, false);
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(DumpInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
            tagToResolve = null;
        }
    }

    public Tag getTag() {
        resolveTag();
        return resolvedTag;
    }

    @Override
    public SWF getSwf() {
        Tag tag = tagToResolve != null ? tagToResolve : resolvedTag;
        if (tag != null) {
            return tag.getSwf();
        }

        return DumpInfoSwfNode.getSwfNode(this).getSwf();
    }

    @Override
    public boolean isModified() {
        return false;
    }
}
