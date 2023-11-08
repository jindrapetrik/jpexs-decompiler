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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author JPEXS
 */
public class DepthState {

    public int depth = -1;

    public int characterId = -1;

    public MATRIX matrix;

    public String instanceName;

    public String className;

    public ColorTransform colorTransForm;

    public boolean cacheAsBitmap = false;

    public int blendMode = 0;

    public List<FILTER> filters = new ArrayList<>();

    public boolean isVisible = true;

    public RGBA backGroundColor;

    public CLIPACTIONS clipActions;

    public byte[] amfData;

    public int ratio = -1;

    public boolean key = false;

    public int clipDepth = -1;

    public int time = 0;

    private final SWF swf;

    public Frame frame;
    
    public Frame placeFrame;

    public PlaceObjectTypeTag placeObjectTag;

    public int minPlaceObjectNum;

    public long instanceId;

    public boolean motionTween = false;

    public boolean hasImage = false;

    private static AtomicLong lastInstanceId = new AtomicLong(0);

    public static long getNewInstanceId() {
        return lastInstanceId.addAndGet(1);
    }

    public DepthState(SWF swf, Frame frame, Frame placeFrame) {
        this.swf = swf;
        this.frame = frame;
        this.placeFrame = placeFrame;
        this.instanceId = getNewInstanceId();
    }

    public DepthState(DepthState obj, Frame frame, Frame placeFrame, boolean sameInstance) {
        this.frame = frame;
        this.placeFrame = placeFrame;
        swf = obj.swf;
        characterId = obj.characterId;
        matrix = obj.matrix;
        instanceName = obj.instanceName;
        colorTransForm = obj.colorTransForm;
        className = obj.className;
        cacheAsBitmap = obj.cacheAsBitmap;
        blendMode = obj.blendMode;
        filters = obj.filters;
        isVisible = obj.isVisible;
        backGroundColor = obj.backGroundColor;
        clipActions = obj.clipActions;
        ratio = obj.ratio;
        clipDepth = obj.clipDepth;
        time = obj.time;
        placeObjectTag = obj.placeObjectTag;
        minPlaceObjectNum = obj.minPlaceObjectNum;
        depth = obj.depth;
        if (sameInstance) {
            time++;
            instanceId = obj.instanceId;
        } else {
            instanceId = getNewInstanceId();
        }
    }

    public void setMATRIX(MATRIX matrix) {
        this.matrix = matrix;
        this.placeObjectTag.setMatrix(matrix);
    }

    public boolean cacheAsBitmap() {
        return (placeObjectTag != null && placeObjectTag.cacheAsBitmap())
                || (filters != null && filters.size() > 0);
    }

    public PlaceObjectTypeTag toPlaceObjectTag(int depth) {
        if (minPlaceObjectNum <= 1) {
            CXFORM cxForm0 = colorTransForm == null ? null : new CXFORM(colorTransForm);
            return new PlaceObjectTag(swf, characterId, depth, matrix, cxForm0);
        } else if (minPlaceObjectNum == 2) {
            CXFORMWITHALPHA cxForm = colorTransForm == null ? null : new CXFORMWITHALPHA(colorTransForm);
            return new PlaceObject2Tag(swf, false, depth, characterId, matrix, cxForm, ratio, instanceName, clipDepth, clipActions);
        } else if (minPlaceObjectNum == 3) {
            CXFORMWITHALPHA cxForm = colorTransForm == null ? null : new CXFORMWITHALPHA(colorTransForm);
            return new PlaceObject3Tag(swf, false, depth, className, characterId, matrix, cxForm, ratio, instanceName, clipDepth, filters, blendMode, cacheAsBitmap ? 1 : 0, isVisible ? 1 : 0, backGroundColor, clipActions, hasImage);
        }

        CXFORMWITHALPHA cxForm = colorTransForm == null ? null : new CXFORMWITHALPHA(colorTransForm);
        return new PlaceObject4Tag(swf, false, depth, className, characterId, matrix, cxForm, ratio, instanceName, clipDepth, filters, blendMode, cacheAsBitmap ? 1 : 0, isVisible ? 1 : 0, backGroundColor, clipActions, null, hasImage);
    }

    public CharacterTag getCharacter() {
        if (characterId == -1) {

            if (className != null) {
                return swf.getCharacterByClass(className);
            }

            return null;
        }

        return swf.getCharacter(characterId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.depth;
        hash = 29 * hash + this.characterId;
        hash = 29 * hash + Objects.hashCode(this.matrix);
        hash = 29 * hash + Objects.hashCode(this.instanceName);
        hash = 29 * hash + Objects.hashCode(this.className);
        hash = 29 * hash + Objects.hashCode(this.colorTransForm);
        hash = 29 * hash + (this.cacheAsBitmap ? 1 : 0);
        hash = 29 * hash + this.blendMode;
        hash = 29 * hash + Objects.hashCode(this.filters);
        hash = 29 * hash + (this.isVisible ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.backGroundColor);
        hash = 29 * hash + Objects.hashCode(this.clipActions);
        hash = 29 * hash + Arrays.hashCode(this.amfData);
        hash = 29 * hash + this.ratio;
        hash = 29 * hash + this.clipDepth;
        hash = 29 * hash + this.time;
        hash = 29 * hash + (this.hasImage ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DepthState other = (DepthState) obj;
        if (this.depth != other.depth) {
            return false;
        }
        if (this.characterId != other.characterId) {
            return false;
        }
        if (this.cacheAsBitmap != other.cacheAsBitmap) {
            return false;
        }
        if (this.blendMode != other.blendMode) {
            return false;
        }
        if (this.isVisible != other.isVisible) {
            return false;
        }
        if (this.ratio != other.ratio) {
            return false;
        }
        if (this.clipDepth != other.clipDepth) {
            return false;
        }
        if (this.hasImage != other.hasImage) {
            return false;
        }
        if (!Objects.equals(this.instanceName, other.instanceName)) {
            return false;
        }
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        if (!Objects.equals(this.matrix, other.matrix)) {
            return false;
        }
        if (!Objects.equals(this.colorTransForm, other.colorTransForm)) {
            return false;
        }
        if (!Objects.equals(this.filters, other.filters)) {
            return false;
        }
        if (!Objects.equals(this.backGroundColor, other.backGroundColor)) {
            return false;
        }
        if (!Objects.equals(this.clipActions, other.clipActions)) {
            return false;
        }
        return Arrays.equals(this.amfData, other.amfData);        
    }        
}
