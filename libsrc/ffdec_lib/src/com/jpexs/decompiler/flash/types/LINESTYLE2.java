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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Color;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Line style, v2. Extends functionality of LINESTYLE.
 *
 * @author JPEXS
 */
public class LINESTYLE2 implements NeedsCharacters, Serializable, ILINESTYLE {

    /**
     * Width
     */
    @SWFType(BasicType.UI16)
    public int width;

    /**
     * Start cap style
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_CAP, text = "Round cap")
    @EnumValue(value = NO_CAP, text = "No cap")
    @EnumValue(value = SQUARE_CAP, text = "Square cap")
    public int startCapStyle;

    /**
     * Join style
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_JOIN, text = "Round join")
    @EnumValue(value = BEVEL_JOIN, text = "Bevel join")
    @EnumValue(value = MITER_JOIN, text = "Miter join")
    public int joinStyle;

    /**
     * Cap style - round
     */
    public static final int ROUND_CAP = 0;

    /**
     * Cap style - no cap
     */
    public static final int NO_CAP = 1;

    /**
     * Cap style - square
     */
    public static final int SQUARE_CAP = 2;

    /**
     * Join style - round
     */
    public static final int ROUND_JOIN = 0;

    /**
     * Join style - bevel
     */
    public static final int BEVEL_JOIN = 1;

    /**
     * Join style - miter
     */
    public static final int MITER_JOIN = 2;

    /**
     * Has fill flag
     */
    public boolean hasFillFlag;

    /**
     * No horizontal scale flag
     */
    public boolean noHScaleFlag;

    /**
     * No vertical scale flag
     */
    public boolean noVScaleFlag;

    /**
     * Pixel hinting flag
     */
    public boolean pixelHintingFlag;

    /**
     * Reserved
     */
    @Reserved
    @SWFType(value = BasicType.UB, count = 5)
    public int reserved;

    /**
     * No close flag
     */
    public boolean noClose;

    /**
     * End cap style
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_CAP, text = "Round cap")
    @EnumValue(value = NO_CAP, text = "No cap")
    @EnumValue(value = SQUARE_CAP, text = "Square cap")
    public int endCapStyle;

    /**
     * Miter limit factor
     */
    @SWFType(BasicType.FIXED8)
    @Conditional(value = "joinStyle", options = {MITER_JOIN})
    public float miterLimitFactor;

    /**
     * Color
     */
    @Conditional(value = "!hasFillFlag")
    public RGBA color;

    /**
     * Fill type
     */
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

    /**
     * Checks if this line style is compatible with another line style
     * @param otherLineStyle The other line style
     * @param swf The SWF
     * @return True if compatible, false otherwise
     */
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

    /**
     * Converts this line style to a MORPHLINESTYLE2
     * @return The MORPHLINESTYLE2
     */
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

    /**
     * Converts this line style to a MORPHLINESTYLE2
     * @param endLineStyle The end line style
     * @param swf The SWF
     * @return The MORPHLINESTYLE2
     */
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.width;
        hash = 83 * hash + this.startCapStyle;
        hash = 83 * hash + this.joinStyle;
        hash = 83 * hash + (this.hasFillFlag ? 1 : 0);
        hash = 83 * hash + (this.noHScaleFlag ? 1 : 0);
        hash = 83 * hash + (this.noVScaleFlag ? 1 : 0);
        hash = 83 * hash + (this.pixelHintingFlag ? 1 : 0);
        hash = 83 * hash + this.reserved;
        hash = 83 * hash + (this.noClose ? 1 : 0);
        hash = 83 * hash + this.endCapStyle;
        hash = 83 * hash + Float.floatToIntBits(this.miterLimitFactor);
        hash = 83 * hash + Objects.hashCode(this.color);
        hash = 83 * hash + Objects.hashCode(this.fillType);
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
        final LINESTYLE2 other = (LINESTYLE2) obj;
        if (this.width != other.width) {
            return false;
        }
        if (this.startCapStyle != other.startCapStyle) {
            return false;
        }
        if (this.joinStyle != other.joinStyle) {
            return false;
        }
        if (this.hasFillFlag != other.hasFillFlag) {
            return false;
        }
        if (this.noHScaleFlag != other.noHScaleFlag) {
            return false;
        }
        if (this.noVScaleFlag != other.noVScaleFlag) {
            return false;
        }
        if (this.pixelHintingFlag != other.pixelHintingFlag) {
            return false;
        }
        if (this.reserved != other.reserved) {
            return false;
        }
        if (this.noClose != other.noClose) {
            return false;
        }
        if (this.endCapStyle != other.endCapStyle) {
            return false;
        }
        if (Float.floatToIntBits(this.miterLimitFactor) != Float.floatToIntBits(other.miterLimitFactor)) {
            return false;
        }
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        return Objects.equals(this.fillType, other.fillType);
    }
    
    public LINESTYLE toLineStyle1(int shapeNum) {
        LINESTYLE result = new LINESTYLE();
        if (hasFillFlag) {
            result.color = shapeNum >= 3 ? new RGBA(Color.black) : new RGB(Color.black);
        } else {
            result.color = color;
        }
        result.width = width;
        return result;
    }
    
    public int getMinShapeNum() {
        int shapeNum = 1;
        if (hasFillFlag) {
            return 4;
        }
        if (startCapStyle != ROUND_CAP) {
            return 4;
        }
        if (endCapStyle != ROUND_CAP) {
            return 4;            
        }
        if (joinStyle != ROUND_JOIN) {
            return 4;
        }
        if (noClose) {
            return 4;
        }
        if (noHScaleFlag) {
            return 4;
        }
        if (noVScaleFlag) {
            return 4;
        }
        if (pixelHintingFlag) {
            return 4;
        }
        if (color.alpha != 255) {
            return 3;
        }
        return 1;
    }

}
