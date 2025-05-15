/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for place object tags.
 *
 * @author JPEXS
 */
public abstract class PlaceObjectTypeTag extends Tag implements CharacterIdTag, DepthTag {

    /**
     * Constructor.
     * @param swf SWF
     * @param id Id
     * @param name Name
     * @param data Data
     */
    public PlaceObjectTypeTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    /**
     * Gets place object number.
     * PlaceObject = 1, PlaceObject2 = 2, ...
     * @return Place object number
     */
    public abstract int getPlaceObjectNum();

    /**
     * Gets matrix.
     * @return Matrix
     */
    public abstract MATRIX getMatrix();

    /**
     * Sets matrix.
     * @param matrix Matrix
     */
    public abstract void setMatrix(MATRIX matrix);

    /**
     * Gets instance name.
     * @return Instance name
     */
    public abstract String getInstanceName();

    /**
     * Sets instance name.
     * @param name Instance name
     */
    public abstract void setInstanceName(String name);

    /**
     * Sets class name.
     * @param className Class name
     */
    public abstract void setClassName(String className);

    /**
     * Gets color transform.
     * @return Color transform
     */
    public abstract ColorTransform getColorTransform();

    /**
     * Gets blend mode.
     * @return Blend mode
     */
    public abstract int getBlendMode();

    /**
     * Gets filters.
     * @return Filters
     */
    public abstract List<FILTER> getFilters();

    /**
     * Gets clip depth.
     * @return Clip depth
     */
    public abstract int getClipDepth();

    /**
     * Gets class name.
     * @return Class name
     */
    public abstract String getClassName();

    /**
     * Checks if cache as bitmap.
     * @return True if cache as bitmap
     */
    public abstract boolean cacheAsBitmap();

    /**
     * Checks if has image.
     * @return True if has image
     */
    public abstract boolean hasImage();

    /**
     * Gets bitmap cache.
     * @return Bitmap cache
     */
    public abstract Integer getBitmapCache();

    /**
     * Checks if visible.
     * @return True if visible
     */
    public abstract boolean isVisible();

    /**
     * Gets visible.
     * @return Visible
     */
    public abstract Integer getVisible();

    /**
     * Gets background color.
     * @return Background color
     */
    public abstract RGBA getBackgroundColor();

    /**
     * Checks if flag move.
     * @return True if flag move
     */
    public abstract boolean flagMove();

    /**
     * Gets ratio.
     * @return Ratio
     */
    public abstract int getRatio();

    /**
     * Gets clip actions.
     * @return Clip actions
     */
    public abstract CLIPACTIONS getClipActions();

    /**
     * Writes tag with matrix.
     * @param sos SWF output stream
     * @param m Matrix
     * @throws IOException On I/O error
     */
    public abstract void writeTagWithMatrix(SWFOutputStream sos, MATRIX m) throws IOException;

    /**
     * Gets AMF data.
     * @return AMF data
     */
    public abstract Amf3Value getAmfData();

    /**
     * Sets clip actions.
     * @param clipActions Clip actions
     */
    public abstract void setClipActions(CLIPACTIONS clipActions);

    /**
     * Sets place flag has clip actions.
     * @param placeFlagHasClipActions Place flag has clip actions
     */
    public abstract void setPlaceFlagHasClipActions(boolean placeFlagHasClipActions);

    /**
     * Sets place flag has matrix.
     * @param placeFlagHasMatrix Place flag has matrix
     */
    public abstract void setPlaceFlagHasMatrix(boolean placeFlagHasMatrix);

    /**
     * Sets place flag move.
     * @param placeFlagMove Place flag move
     */
    public abstract void setPlaceFlagMove(boolean placeFlagMove);
    
    /**
     * Sets color transform.
     * @param colorTransform Color transform 
     */
    public abstract void setColorTransform(ColorTransform colorTransform);
    
    public void setVisible(int visible) {
        throw new UnsupportedOperationException();
    }
    
    public void setPlaceFlagHasVisible(boolean value) {
        throw new UnsupportedOperationException();
    }
    
    public void setBlendMode(int value) {
        throw new UnsupportedOperationException();
    }
    
    public void setPlaceFlagHasBlendMode(boolean value) {        
        throw new UnsupportedOperationException();
    }
    
    public void setBitmapCache(int value) {
        throw new UnsupportedOperationException();
    }

    public void setPlaceFlagHasCacheAsBitmap(boolean value) {
        throw new UnsupportedOperationException();
    }

    public void setBackgroundColor(RGBA value) {
        throw new UnsupportedOperationException();
    }

    public void setPlaceFlagOpaqueBackground(boolean value) {
        throw new UnsupportedOperationException();
    }
    
    public void setPlaceFlagHasFilterList(boolean value) {
        throw new UnsupportedOperationException();
    }
    
    public void setFilters(List<FILTER> filters) {
        throw new UnsupportedOperationException();
    }
    
    public void setPlaceFlagHasRatio(boolean placeFlagHasRatio) {
        throw new UnsupportedOperationException();
    }
    
    public void setRatio(int ratio) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Checks if place equals.
     * @param other Other place object type tag
     * @return True if place equals
     */
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
        if (!Objects.equals(getFilters(), other.getFilters())) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, String> getNameProperties() {
        SWF swf = getSwf();
        if (swf == null) {
            return new HashMap<>();
        }
        Map<String, String> ret = super.getNameProperties();
        int charId = getCharacterId();
        String charClassName = getClassName();
        if (charId == -1 && charClassName != null) {
            ret.put("cls", charClassName);
            CharacterTag ch = swf.getCharacterByClass(charClassName);
            if (ch != null) {
                charId = ch.getCharacterId();
                ret.put("chid", "" + charId);
            }
        } else {
            String exportName = swf.getExportName(charId);
            if (charId > -1) {
                ret.put("chid", "" + charId);
            }
            if (exportName != null) {
                ret.put("exp", exportName);
            }
        }

        int depth = getDepth();
        ret.put("dpt", "" + depth);
        int clipDepth = getClipDepth();
        if (clipDepth > -1) {
            ret.put("cdp", "" + clipDepth);
        }
        return ret;
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
