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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class LINESTYLE2 implements NeedsCharacters, Serializable, ILINESTYLE {

    @SWFType(BasicType.UI16)
    public int width;

    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_CAP, text = "Round cap")
    @EnumValue(value = NO_CAP, text = "No cap")
    @EnumValue(value = SQUARE_CAP, text = "Square cap")
    public int startCapStyle;

    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_JOIN, text = "Round join")
    @EnumValue(value = BEVEL_JOIN, text = "Bevel join")
    @EnumValue(value = MITER_JOIN, text = "Miter join")
    public int joinStyle;

    public static final int ROUND_CAP = 0;

    public static final int NO_CAP = 1;

    public static final int SQUARE_CAP = 2;

    public static final int ROUND_JOIN = 0;

    public static final int BEVEL_JOIN = 1;

    public static final int MITER_JOIN = 2;

    public boolean hasFillFlag;

    public boolean noHScaleFlag;

    public boolean noVScaleFlag;

    public boolean pixelHintingFlag;

    @Reserved
    @SWFType(value = BasicType.UB, count = 5)
    public int reserved;

    public boolean noClose;

    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_CAP, text = "Round cap")
    @EnumValue(value = NO_CAP, text = "No cap")
    @EnumValue(value = SQUARE_CAP, text = "Square cap")
    public int endCapStyle;

    @SWFType(BasicType.FIXED8)
    @Conditional(value = "joinStyle", options = {MITER_JOIN})
    public float miterLimitFactor;

    @Conditional(value = "!hasFillFlag")
    public RGBA color;

    @Conditional(value = "hasFillFlag")
    public FILLSTYLE fillType;

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        if (hasFillFlag) {
            fillType.getNeededCharacters(needed, swf);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        if (fillType != null) {
            return fillType.replaceCharacter(oldCharacterId, newCharacterId);
        }
        return false;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        if (fillType != null) {
            return fillType.removeCharacter(characterId);
        }
        return false;
    }

    @Override
    public int getNum() {
        return 2;
    }

    @Override
    public RGB getColor() {
        if (hasFillFlag) {
            return null;
        }
        return color;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setColor(RGB color) {
        if (color instanceof RGBA) {
            this.color = (RGBA) color;
        }
        this.color = new RGBA(color.toColor());
        hasFillFlag = false;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }        
    
    public boolean isCompatibleLineStyle(LINESTYLE2 otherLineStyle, SWF swf) {
        if (startCapStyle != otherLineStyle.startCapStyle) {
            return false;
        }
        if (endCapStyle != otherLineStyle.endCapStyle) {
            return false;
        }
        
        if (joinStyle != otherLineStyle.joinStyle) {
            return false;
        }
        
        if (hasFillFlag != otherLineStyle.hasFillFlag) {
            return false;
        }
        
        if (noVScaleFlag != otherLineStyle.noVScaleFlag) {
            return false;
        }
        if (pixelHintingFlag != otherLineStyle.pixelHintingFlag) {
            return false;
        }
        if (noClose != otherLineStyle.noClose) {
            return false;
        }
        if (miterLimitFactor != otherLineStyle.miterLimitFactor) {
            return false;
        }
        
        if (hasFillFlag) {
            if (!fillType.isCompatibleFillStyle(otherLineStyle.fillType, swf)) {
                return false;
            }
        }
        
        return true;
    }
    
    public MORPHLINESTYLE2 toMorphLineStyle2() {
        MORPHLINESTYLE2 morphLineStyle2 = new MORPHLINESTYLE2();
        morphLineStyle2.startWidth = width;
        morphLineStyle2.endWidth = width;
        morphLineStyle2.startCapStyle = startCapStyle;
        morphLineStyle2.joinStyle = joinStyle;
        morphLineStyle2.hasFillFlag = hasFillFlag;
        morphLineStyle2.noHScaleFlag = noHScaleFlag;
        morphLineStyle2.noVScaleFlag = noVScaleFlag;
        morphLineStyle2.pixelHintingFlag = pixelHintingFlag;
        morphLineStyle2.noClose = noClose;
        morphLineStyle2.endCapStyle = endCapStyle;
        morphLineStyle2.miterLimitFactor = miterLimitFactor;
        if (color != null) {
            morphLineStyle2.startColor = new RGBA(color);
            morphLineStyle2.endColor = new RGBA(color);
        }
        if (fillType != null) {
            morphLineStyle2.fillType = fillType.toMorphStyle();
        }
        return morphLineStyle2;
    }

    public MORPHLINESTYLE2 toMorphLineStyle2(LINESTYLE2 endLineStyle, SWF swf) {
        if (!isCompatibleLineStyle(endLineStyle, swf)) {
            return null;
        }
        
        MORPHLINESTYLE2 morphLineStyle2 = new MORPHLINESTYLE2();
        morphLineStyle2.startWidth = width;
        morphLineStyle2.endWidth = endLineStyle.width;
        morphLineStyle2.startCapStyle = startCapStyle;        
        morphLineStyle2.joinStyle = joinStyle;
        morphLineStyle2.hasFillFlag = hasFillFlag;
        morphLineStyle2.noHScaleFlag = noHScaleFlag;
        morphLineStyle2.noVScaleFlag = noVScaleFlag;
        morphLineStyle2.pixelHintingFlag = pixelHintingFlag;
        morphLineStyle2.noClose = noClose;
        morphLineStyle2.endCapStyle = endCapStyle;
        morphLineStyle2.miterLimitFactor = miterLimitFactor;
        if (color != null) {
            morphLineStyle2.startColor = new RGBA(color);
        }
        if (endLineStyle.color != null) {
            morphLineStyle2.endColor = new RGBA(endLineStyle.color);
        }
        if (hasFillFlag) {
            morphLineStyle2.fillType = fillType.toMorphStyle(endLineStyle.fillType, swf);
        }
        return morphLineStyle2;
    }
}
