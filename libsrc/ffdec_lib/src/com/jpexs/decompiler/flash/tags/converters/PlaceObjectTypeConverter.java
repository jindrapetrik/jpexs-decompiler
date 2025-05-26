/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.converters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.Helper;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class PlaceObjectTypeConverter {

    /**
     * Gets minimum PlaceObject type number (PlaceObject, PlaceObject2, ...) to
     * cover all features that passed placeTag has
     *
     * @param placeTag PlaceObject tag
     * @return Minimum type number
     */
    public int getMinPlaceNum(PlaceObjectTypeTag placeTag) {
        int minPlaceNum = 1;
        if (placeTag.flagMove()) {
            minPlaceNum = 2;
        }
        if (placeTag.getMatrix() == null) {
            minPlaceNum = 2;
        }
        if (placeTag.getColorTransform() instanceof CXFORMWITHALPHA) {
            minPlaceNum = 2;
        }
        if (placeTag.getClipDepth() > -1) {
            minPlaceNum = 2;
        }
        if (placeTag.getName() != null) {
            minPlaceNum = 2;
        }
        if (placeTag.getRatio() > -1) {
            minPlaceNum = 2;
        }
        if (placeTag.getClassName() != null) {
            minPlaceNum = 3;
        }
        if (placeTag.getBackgroundColor() != null) {
            minPlaceNum = 3;
        }
        if (placeTag.getBitmapCache() != null) {
            minPlaceNum = 3;
        }
        if (placeTag.getBlendMode() > 0) {
            minPlaceNum = 3;
        }
        if (placeTag.getFilters() != null && !placeTag.getFilters().isEmpty()) {
            minPlaceNum = 3;
        }
        if (placeTag.getVisible() != null) {
            minPlaceNum = 3;
        }
        if (placeTag.hasImage()) {
            minPlaceNum = 3;
        }
        if (placeTag.getAmfData() != null) {
            minPlaceNum = 4;
        }
        return minPlaceNum;
    }

    /**
     * Converts versions of PlaceObject tag (PlaceObject, PlaceObject2, ...)
     *
     * @param sourcePlaceTag Source place tag
     * @param targetSWF Target SWF
     * @param targetPlaceNum Target place type number
     * @param deepClone Deep clone all items
     * @return Place object of target type
     */
    public PlaceObjectTypeTag convertTagType(PlaceObjectTypeTag sourcePlaceTag, SWF targetSWF, int targetPlaceNum, boolean deepClone) {
        ColorTransform colorTransform = sourcePlaceTag.getColorTransform();
        CLIPACTIONS clipActions = sourcePlaceTag.getClipActions();
        MATRIX matrix = sourcePlaceTag.getMatrix();
        Integer bitmapCache = sourcePlaceTag.getBitmapCache();
        List<FILTER> filters = sourcePlaceTag.getFilters();
        Integer visible = sourcePlaceTag.getVisible();
        Amf3Value amfData = sourcePlaceTag.getAmfData();
        switch (targetPlaceNum) {
            case 1:
                PlaceObjectTag place1 = new PlaceObjectTag(targetSWF);
                place1.characterId = sourcePlaceTag.getCharacterId();
                if (colorTransform != null) {
                    place1.colorTransform = !deepClone && colorTransform.getClass() == CXFORM.class ? (CXFORM) colorTransform : new CXFORM(colorTransform);
                }
                place1.depth = sourcePlaceTag.getDepth();
                if (matrix != null) {
                    place1.matrix = deepClone ? new MATRIX(matrix) : matrix;
                } else {
                    place1.matrix = new MATRIX();
                }

                return place1;
            case 2:
                PlaceObject2Tag place2 = new PlaceObject2Tag(targetSWF);
                place2.placeFlagMove = sourcePlaceTag.flagMove();
                place2.characterId = sourcePlaceTag.getCharacterId();
                if (place2.characterId != -1) {
                    place2.placeFlagHasCharacter = true;
                }
                if (clipActions != null) {
                    place2.clipActions = deepClone ? Helper.deepCopy(clipActions) : clipActions;
                    place2.placeFlagHasClipActions = true;
                }
                place2.clipDepth = sourcePlaceTag.getClipDepth();
                if (place2.clipDepth > -1) {
                    place2.placeFlagHasClipDepth = true;
                }
                if (colorTransform != null) {
                    place2.colorTransform = !deepClone && colorTransform.getClass() == CXFORMWITHALPHA.class ? (CXFORMWITHALPHA) colorTransform : new CXFORMWITHALPHA(colorTransform);
                    place2.placeFlagHasColorTransform = true;
                }
                place2.depth = sourcePlaceTag.getDepth();
                if (matrix != null) {
                    place2.matrix = deepClone ? new MATRIX(matrix) : matrix;
                    place2.placeFlagHasMatrix = true;
                }
                place2.name = sourcePlaceTag.getInstanceName();
                if (place2.name != null) {
                    place2.placeFlagHasName = true;
                }
                place2.ratio = sourcePlaceTag.getRatio();
                if (place2.ratio > -1) {
                    place2.placeFlagHasRatio = true;
                }
                return place2;
            case 3:
                PlaceObject3Tag place3 = new PlaceObject3Tag(targetSWF);
                place3.placeFlagMove = sourcePlaceTag.flagMove();
                place3.characterId = sourcePlaceTag.getCharacterId();
                if (place3.characterId != -1) {
                    place3.placeFlagHasCharacter = true;
                }
                if (clipActions != null) {
                    place3.clipActions = deepClone ? Helper.deepCopy(clipActions) : clipActions;
                    place3.placeFlagHasClipActions = true;
                }
                place3.clipDepth = sourcePlaceTag.getClipDepth();
                if (place3.clipDepth > -1) {
                    place3.placeFlagHasClipDepth = true;
                }
                if (colorTransform != null) {
                    place3.colorTransform = !deepClone && colorTransform.getClass() == CXFORMWITHALPHA.class ? (CXFORMWITHALPHA) colorTransform : new CXFORMWITHALPHA(colorTransform);
                    place3.placeFlagHasColorTransform = true;
                }
                place3.depth = sourcePlaceTag.getDepth();
                if (matrix != null) {
                    place3.matrix = new MATRIX(matrix);
                    place3.placeFlagHasMatrix = true;
                }
                place3.name = sourcePlaceTag.getInstanceName();
                if (place3.name != null) {
                    place3.placeFlagHasName = true;
                }
                place3.ratio = sourcePlaceTag.getRatio();
                if (place3.ratio > -1) {
                    place3.placeFlagHasRatio = true;
                }
                place3.className = sourcePlaceTag.getClassName();
                if (place3.className != null) {
                    place3.placeFlagHasClassName = true;
                }
                place3.backgroundColor = sourcePlaceTag.getBackgroundColor();
                if (place3.backgroundColor != null) {
                    place3.placeFlagOpaqueBackground = true;
                }
                if (bitmapCache != null) {
                    place3.bitmapCache = bitmapCache;
                    place3.placeFlagHasCacheAsBitmap = true;
                }
                place3.blendMode = sourcePlaceTag.getBlendMode();
                if (place3.blendMode > 0) {
                    place3.placeFlagHasBlendMode = true;
                }
                if (filters != null && !filters.isEmpty()) {
                    place3.surfaceFilterList = deepClone ? Helper.deepCopy(filters) : filters;
                    place3.placeFlagHasFilterList = true;
                }
                if (visible != null) {
                    place3.visible = visible;
                    place3.placeFlagHasVisible = true;
                }
                if (sourcePlaceTag.hasImage()) {
                    place3.placeFlagHasImage = true;
                }
                return place3;
            case 4:
                PlaceObject4Tag place4 = new PlaceObject4Tag(targetSWF);
                place4.placeFlagMove = sourcePlaceTag.flagMove();
                place4.characterId = sourcePlaceTag.getCharacterId();
                if (place4.characterId != -1) {
                    place4.placeFlagHasCharacter = true;
                }
                if (clipActions != null) {
                    place4.clipActions = deepClone ? Helper.deepCopy(clipActions) : clipActions;
                    place4.placeFlagHasClipActions = true;
                }
                place4.clipDepth = sourcePlaceTag.getClipDepth();
                if (place4.clipDepth > -1) {
                    place4.placeFlagHasClipDepth = true;
                }
                if (colorTransform != null) {
                    place4.colorTransform = !deepClone && colorTransform.getClass() == CXFORMWITHALPHA.class ? (CXFORMWITHALPHA) colorTransform : new CXFORMWITHALPHA(colorTransform);
                    place4.placeFlagHasColorTransform = true;
                }
                place4.depth = sourcePlaceTag.getDepth();
                if (matrix != null) {
                    place4.matrix = new MATRIX(matrix);
                    place4.placeFlagHasMatrix = true;
                }
                place4.name = sourcePlaceTag.getInstanceName();
                if (place4.name != null) {
                    place4.placeFlagHasName = true;
                }
                place4.ratio = sourcePlaceTag.getRatio();
                if (place4.ratio > -1) {
                    place4.placeFlagHasRatio = true;
                }
                place4.className = sourcePlaceTag.getClassName();
                if (place4.className != null) {
                    place4.placeFlagHasClassName = true;
                }
                place4.backgroundColor = sourcePlaceTag.getBackgroundColor();
                if (place4.backgroundColor != null) {
                    place4.placeFlagOpaqueBackground = true;
                }
                if (bitmapCache != null) {
                    place4.bitmapCache = bitmapCache;
                    place4.placeFlagHasCacheAsBitmap = true;
                }
                place4.blendMode = sourcePlaceTag.getBlendMode();
                if (place4.blendMode > 0) {
                    place4.placeFlagHasBlendMode = true;
                }
                if (filters != null && !filters.isEmpty()) {
                    place4.surfaceFilterList = deepClone ? Helper.deepCopy(filters) : filters;
                    place4.placeFlagHasFilterList = true;
                }
                if (visible != null) {
                    place4.visible = visible;
                    place4.placeFlagHasVisible = true;
                }
                if (amfData != null) {
                    place4.amfData = deepClone ? Helper.deepCopy(amfData) : amfData;
                }
                if (sourcePlaceTag.hasImage()) {
                    place4.placeFlagHasImage = true;
                }
                return place4;
            default:
                throw new IllegalArgumentException("PlaceNum must be between 1 and 4. Provided: " + targetPlaceNum);
        }
    }

    /**
     * Converts versions of PlaceObject tag (PlaceObject, PlaceObject2, ...) and
     * place result in the position where original tag was.
     *
     * @param placeTag Place tag
     * @param targetPlaceNum Target place num
     * @return Converted place tag
     */
    public PlaceObjectTypeTag convertTagType(PlaceObjectTypeTag placeTag, int targetPlaceNum) {
        PlaceObjectTypeTag converted = convertTagType(placeTag, placeTag.getSwf(), targetPlaceNum, false);
        converted.setTimelined(placeTag.getTimelined());
        placeTag.getTimelined().replaceTag(placeTag, converted);
        placeTag.getTimelined().resetTimeline();
        return converted;
    }
}
