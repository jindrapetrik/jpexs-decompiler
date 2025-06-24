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

import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;

/**
 * Morph line style, v2. Extends functionality of MORPHLINESTYLE.
 *
 * @author JPEXS
 */
public class MORPHLINESTYLE2 implements Serializable {

    /**
     * Start width
     */
    @SWFType(BasicType.UI16)
    public int startWidth;

    /**
     * End width
     */
    @SWFType(BasicType.UI16)
    public int endWidth;

    /**
     * Start cap style
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_CAP, text = "Round cap")
    @EnumValue(value = NO_CAP, text = "No cap")
    @EnumValue(value = SQUARE_CAP, text = "Square cap")
    public int startCapStyle;

    /**
     * End cap style
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_JOIN, text = "Round join")
    @EnumValue(value = BEVEL_JOIN, text = "Bevel join")
    @EnumValue(value = MITER_JOIN, text = "Miter join")
    public int joinStyle;

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
     * Miter limit factor
     */
    @SWFType(value = BasicType.FIXED8)
    @Conditional(value = "joinStyle", options = {MITER_JOIN})
    public float miterLimitFactor;

    /**
     * Start color
     */
    @Conditional("!hasFillFlag")
    public RGBA startColor;

    /**
     * End color
     */
    @Conditional("!hasFillFlag")
    public RGBA endColor;

    /**
     * Fill type
     */
    @Conditional(value = "hasFillFlag")
    public MORPHFILLSTYLE fillType;

    /**
     * Gets line style 2 at given ratio.
     * @param ratio Ratio
     * @return Line style 2
     */
    public LINESTYLE2 getLineStyle2At(int ratio) {
        LINESTYLE2 ret = new LINESTYLE2();
        ret.width = startWidth + (endWidth - startWidth) * ratio / 65535;
        ret.startCapStyle = startCapStyle;
        ret.joinStyle = joinStyle;
        ret.hasFillFlag = hasFillFlag;
        ret.noHScaleFlag = noHScaleFlag;
        ret.noVScaleFlag = noVScaleFlag;
        ret.pixelHintingFlag = pixelHintingFlag;
        ret.noClose = noClose;
        ret.endCapStyle = endCapStyle;
        ret.miterLimitFactor = miterLimitFactor;
        if (hasFillFlag) {
            ret.fillType = fillType.getFillStyleAt(ratio);
        } else {
            ret.color = MORPHGRADIENT.morphColor(startColor, endColor, ratio);
        }
        return ret;
    }

    /**
     * Gets start line style 2.
     * @return Start line style 2
     */
    public LINESTYLE2 getStartLineStyle2() {
        LINESTYLE2 ret = new LINESTYLE2();
        ret.width = startWidth;
        ret.startCapStyle = startCapStyle;
        ret.joinStyle = joinStyle;
        ret.hasFillFlag = hasFillFlag;
        ret.noHScaleFlag = noHScaleFlag;
        ret.noVScaleFlag = noVScaleFlag;
        ret.pixelHintingFlag = pixelHintingFlag;
        ret.noClose = noClose;
        ret.endCapStyle = endCapStyle;
        ret.miterLimitFactor = miterLimitFactor;
        ret.color = startColor;
        if (hasFillFlag) {
            ret.fillType = fillType.getStartFillStyle();
        }
        return ret;
    }

    /**
     * Gets end line style 2.
     * @return End line style 2
     */
    public LINESTYLE2 getEndLineStyle2() {
        LINESTYLE2 ret = new LINESTYLE2();
        ret.width = endWidth;
        ret.startCapStyle = startCapStyle;
        ret.joinStyle = joinStyle;
        ret.hasFillFlag = hasFillFlag;
        ret.noHScaleFlag = noHScaleFlag;
        ret.noVScaleFlag = noVScaleFlag;
        ret.pixelHintingFlag = pixelHintingFlag;
        ret.noClose = noClose;
        ret.endCapStyle = endCapStyle;
        ret.miterLimitFactor = miterLimitFactor;
        ret.color = endColor;
        if (hasFillFlag) {
            ret.fillType = fillType.getEndFillStyle();
        }
        return ret;
    }
}
