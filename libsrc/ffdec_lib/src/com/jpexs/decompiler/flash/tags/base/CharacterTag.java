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
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;

/**
 *
 * @author JPEXS
 */
public abstract class CharacterTag extends Tag implements CharacterIdTag {

    protected String className;

    public CharacterTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String getName() {
        String nameAppend = "";
        if (exportName != null) {
            nameAppend = ": " + exportName;
        }
        if (className != null) {
            nameAppend = ": " + className;
        }
        return tagName + " (" + getCharacterId() + nameAppend + ")";
    }

    @Override
    public String getExportFileName() {
        String result = super.getExportFileName();
        return result + "_" + getCharacterId() + (exportName != null ? "_" + exportName : "") + (className != null ? "_" + className : "");
    }

    public String getCharacterExportFileName() {
        return getCharacterId() + (exportName != null ? "_" + exportName : "") + (className != null ? "_" + className : "");
    }

    protected String exportName;

    public void setExportName(String exportName) {
        if ("".equals(exportName)) {
            exportName = null;
        }
        this.exportName = exportName;
    }

    public String getExportName() {
        return exportName;
    }

    public DefineScalingGridTag getScalingGridTag() {
        return (DefineScalingGridTag) swf.getCharacterIdTag(getCharacterId(), DefineScalingGridTag.ID);
    }
}
