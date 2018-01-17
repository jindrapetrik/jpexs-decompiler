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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;

/**
 *
 * @author JPEXS
 */
public abstract class RemoveTag extends Tag {

    public RemoveTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract int getDepth();

    @Override
    public String getName() {
        String result = super.getName();
        String exportName = swf.getExportName(getCharacterId());
        String nameAppend = "";
        if (exportName != null) {
            nameAppend = ": " + exportName;
        }

        if (getCharacterId() != -1) {
            result += " (" + getCharacterId() + nameAppend + ")";
        }

        if (!nameAppend.isEmpty()) {
            result += " (" + nameAppend + ")";
        }

        return result + " Depth: " + getDepth();
    }

    @Override
    public String getExportFileName() {
        String result = super.getExportFileName();
        if (getCharacterId() != -1) {
            result += "_" + getCharacterId();
        }

        String exportName = swf.getExportName(getCharacterId());
        if (exportName != null) {
            result += "_" + exportName;
        }

        result += "_" + getDepth();
        return result;
    }

    private int getCharacterId() {
        if (this instanceof CharacterIdTag) {
            return ((CharacterIdTag) this).getCharacterId();
        }

        return -1;
    }
}
