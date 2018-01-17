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
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class PlaceObjectTypeTag extends Tag implements CharacterIdTag {

    public PlaceObjectTypeTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract int getPlaceObjectNum();

    public abstract int getDepth();

    public abstract MATRIX getMatrix();

    public abstract void setMatrix(MATRIX matrix);

    public abstract String getInstanceName();

    public abstract void setInstanceName(String name);

    public abstract void setClassName(String className);

    public abstract ColorTransform getColorTransform();

    public abstract int getBlendMode();

    public abstract List<FILTER> getFilters();

    public abstract int getClipDepth();

    public abstract String getClassName();

    public abstract boolean cacheAsBitmap();

    public abstract Integer getBitmapCache();

    public abstract boolean isVisible();

    public abstract Integer getVisible();

    public abstract RGBA getBackgroundColor();

    public abstract boolean flagMove();

    public abstract int getRatio();

    public abstract CLIPACTIONS getClipActions();

    public abstract void writeTagWithMatrix(SWFOutputStream sos, MATRIX m) throws IOException;

    public abstract Amf3Value getAmfData();

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
        String result = super.getExportFileName() + "_" + getCharacterId();
        String exportName = swf.getExportName(getCharacterId());
        if (exportName != null) {
            result += "_" + exportName;
        }

        result += "_" + getDepth();
        return result;
    }
}
