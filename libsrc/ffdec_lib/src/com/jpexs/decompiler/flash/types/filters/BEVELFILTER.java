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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;

/**
 * The Bevel filter creates a smooth bevel on display list objects.
 *
 * @author JPEXS
 */
public class BEVELFILTER extends FILTER {

    /**
     * Color of the shadow
     */
    public RGBA shadowColor;

    /**
     * Color of the highlight
     */
    public RGBA highlightColor;

    /**
     * Horizontal blur amount
     */
    @SWFType(BasicType.FIXED)
    public double blurX;

    /**
     * Vertical blur amount
     */
    @SWFType(BasicType.FIXED)
    public double blurY;

    /**
     * Radian angle of the drop shadow
     */
    @SWFType(BasicType.FIXED)
    public double angle;

    /**
     * Distance of the drop shadow
     */
    @SWFType(BasicType.FIXED)
    public double distance;

    /**
     * Strength of the drop shadow
     */
    @SWFType(BasicType.FIXED8)
    public float strength;

    /**
     * Inner shadow mode
     */
    public boolean innerShadow;

    /**
     * Knockout mode
     */
    public boolean knockout;

    /**
     * Composite source
     */
    public boolean compositeSource;

    /**
     * OnTop mode
     */
    public boolean onTop;

    /**
     * Number of blur passes
     */
    @SWFType(value = BasicType.UB, count = 4)
    public int passes;

    /**
     * Constructor
     */
    public BEVELFILTER() {
        super(3);
    }

    @Override
    public SerializableImage apply(SerializableImage src) {
        int type = Filtering.INNER;
        if (onTop && !innerShadow) {
            type = Filtering.FULL;
        } else if (!innerShadow) {
            type = Filtering.OUTER;
        }
        return Filtering.bevel(src, (int) blurX, (int) blurY, strength, type, highlightColor.toInt(), shadowColor.toInt(), (int) (angle * 180 / Math.PI), (float) distance, knockout, passes);
    }

    @Override
    public double getDeltaX() {
        return blurX + (distance * Math.cos(angle));
    }

    @Override
    public double getDeltaY() {
        return blurY + (distance * Math.sin(angle));
    }
}
