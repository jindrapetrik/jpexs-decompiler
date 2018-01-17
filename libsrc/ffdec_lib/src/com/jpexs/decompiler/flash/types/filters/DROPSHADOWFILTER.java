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
 * Drop shadow filter based on the same median filter as the blur filter
 *
 * @author JPEXS
 */
public class DROPSHADOWFILTER extends FILTER {

    /**
     * Color of the shadow
     */
    public RGBA dropShadowColor;

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
     * Number of blur passes
     */
    @SWFType(value = BasicType.UB, count = 5)
    public int passes;

    /**
     * Constructor
     */
    public DROPSHADOWFILTER() {
        super(0);
    }

    @Override
    public SerializableImage apply(SerializableImage src) {
        return Filtering.dropShadow(src, (int) blurX, (int) blurY, (int) (angle * 180 / Math.PI), distance, dropShadowColor.toColor(), innerShadow, passes, strength, knockout);
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
