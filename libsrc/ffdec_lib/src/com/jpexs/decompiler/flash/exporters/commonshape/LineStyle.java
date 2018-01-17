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
package com.jpexs.decompiler.flash.exporters.commonshape;

import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;

/**
 *
 * @author JPEXS
 */
public class LineStyle {

    public int width;

    public RGB color;

    public boolean isLineStyle2;

    public boolean noHScaleFlag;

    public boolean noVScaleFlag;

    public boolean pixelHintingFlag;

    public int startCapStyle;

    public int endCapStyle;

    public int joinStyle;

    public boolean noClose;

    public float miterLimitFactor;

    public boolean hasFillFlag;

    public FillStyle fillType;

    public LineStyle(LINESTYLE lineStyle) {
        width = lineStyle.width;
        color = lineStyle.color;
        isLineStyle2 = lineStyle instanceof LINESTYLE2;
        if (isLineStyle2) {
            LINESTYLE2 lineStyle2 = (LINESTYLE2) lineStyle;
            noHScaleFlag = lineStyle2.noHScaleFlag;
            noVScaleFlag = lineStyle2.noVScaleFlag;
            pixelHintingFlag = lineStyle2.pixelHintingFlag;
            startCapStyle = lineStyle2.startCapStyle;
            endCapStyle = lineStyle2.endCapStyle;
            joinStyle = lineStyle2.joinStyle;
            noClose = lineStyle2.noClose;
            miterLimitFactor = lineStyle2.miterLimitFactor;
            hasFillFlag = lineStyle2.hasFillFlag;
            fillType = lineStyle2.fillType == null ? null : new FillStyle(lineStyle2.fillType);
        }
    }
}
