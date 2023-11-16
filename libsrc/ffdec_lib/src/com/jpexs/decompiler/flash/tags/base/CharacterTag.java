/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.util.LinkedHashSet;

/**
 *
 * @author JPEXS
 */
public abstract class CharacterTag extends Tag implements CharacterIdTag {

    protected LinkedHashSet<String> classNames = new LinkedHashSet<>();

    public CharacterTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public void setClassNames(LinkedHashSet<String> classNames) {
        this.classNames = new LinkedHashSet<>(classNames);
    }

    public LinkedHashSet<String> getClassNames() {
        return new LinkedHashSet<>(classNames);
    }

    public void addClassName(String className) {
        classNames.add(className);
    }

    @Override
    public String getName() {
        String nameAppend = "";
        if (exportName != null) {
            nameAppend = ": " + Helper.escapePCodeString(exportName);
        }
        if (!classNames.isEmpty()) {
            nameAppend = ": " + Helper.joinEscapePCodeString(", ", classNames);
        }
        return tagName + " (" + getCharacterId() + nameAppend + ")";
    }

    @Override
    public String getExportFileName() {
        String result = super.getExportFileName();
        result += "_" + getCharacterId();
        if (exportName != null) {
            result += "_" + exportName;
        }
        if (classNames.size() == 1) {
            result += "_" + classNames.iterator().next();
        }
        return result;
    }

    public String getCharacterExportFileName() {
        String result = "" + getCharacterId();
        if (exportName != null) {
            result += "_" + exportName;
        }
        if (classNames.size() == 1) {
            result += "_" + classNames.iterator().next();
        }
        return result;
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
        if (swf == null) { //???
            return null;
        }
        return (DefineScalingGridTag) swf.getCharacterIdTag(getCharacterId(), DefineScalingGridTag.ID);
    }
}
