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
import java.util.Objects;

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

    public abstract boolean hasImage();

    public abstract Integer getBitmapCache();

    public abstract boolean isVisible();

    public abstract Integer getVisible();

    public abstract RGBA getBackgroundColor();

    public abstract boolean flagMove();

    public abstract int getRatio();

    public abstract CLIPACTIONS getClipActions();

    public abstract void writeTagWithMatrix(SWFOutputStream sos, MATRIX m) throws IOException;

    public abstract Amf3Value getAmfData();

    public abstract void setClipActions(CLIPACTIONS clipActions);

    public abstract void setPlaceFlagHasClipActions(boolean placeFlagHasClipActions);

    public abstract void setPlaceFlagHasMatrix(boolean placeFlagHasMatrix);
    
    public abstract void setPlaceFlagMove(boolean placeFlagMove);
    
    public boolean placeEquals(PlaceObjectTypeTag other) {
        if (getDepth() != other.getDepth()) {
            return false;
        }
        if (!Objects.equals(getMatrix(), other.getMatrix())) {
            return false;
        }
        if (!Objects.equals(getInstanceName(), other.getInstanceName())) {
            return false;
        }
        if (!Objects.equals(getClassName(), other.getClassName())) {
            return false;
        }
        if (cacheAsBitmap() != other.cacheAsBitmap()) {
            return false;
        }
        if (hasImage() != other.hasImage()) {
            return false;
        }
        if (isVisible() != other.isVisible()) {
            return false;
        }
        if (!Objects.equals(getVisible(), other.getVisible())) {
            return false;
        }
        if (!Objects.equals(getBackgroundColor(), other.getBackgroundColor())) {
            return false;
        }
        if (flagMove() != other.flagMove()) {
            return false;
        }
        if (getRatio() != other.getRatio()) {
            return false;
        }
        if (!Objects.equals(getClipActions(), other.getClipActions())) { //?
            return false;
        }
        if (!Objects.equals(getAmfData(), other.getAmfData())) { //?
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        String result = super.getName();
        int charId = getCharacterId();
        String charClassName = getClassName();
        String exportName = null;
        if (charId == -1 && charClassName != null) {
            exportName = charClassName;
            CharacterTag ch = swf.getCharacterByClass(charClassName);
            if (ch != null) {
                charId = ch.getCharacterId();
            }
        } else {
            exportName = swf.getExportName(charId);
        }
        String nameAppend = "";
        if (exportName != null) {
            nameAppend = ": " + exportName;
        }

        if (charId != -1) {
            result += " (" + charId + nameAppend + ")";
        } else if (!nameAppend.isEmpty()) {
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
