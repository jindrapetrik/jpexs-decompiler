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
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Glow filter with gradient instead of single color
 *
 * @author JPEXS
 */
public class GRADIENTGLOWFILTER extends FILTER {

    /**
     * Gradient colors
     */
    @SWFType(countField = "numColors")
    public RGBA[] gradientColors = new RGBA[0];

    /**
     * Gradient ratios
     */
    @SWFType(value = BasicType.UI8, countField = "numColors")
    public int[] gradientRatio;

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
     * Radian angle of the gradient glow
     */
    @SWFType(BasicType.FIXED)
    public double angle;

    /**
     * Distance of the gradient glow
     */
    @SWFType(BasicType.FIXED)
    public double distance;

    /**
     * Strength of the gradient glow
     */
    @SWFType(BasicType.FIXED8)
    public float strength;

    /**
     * Inner glow mode
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
    public GRADIENTGLOWFILTER() {
        super(4);
    }

    @Override
    public SerializableImage apply(SerializableImage src) {
        List<Color> colors = new ArrayList<>();
        List<Float> ratios = new ArrayList<>();
        for (int i = 0; i < gradientColors.length; i++) {
            if ((i > 0) && (gradientRatio[i - 1] == gradientRatio[i])) {
                continue;
            }
            colors.add(gradientColors[i].toColor());
            ratios.add(gradientRatio[i] / 255f);
        }
        int type = Filtering.INNER;
        if (onTop && !innerShadow) {
            type = Filtering.FULL;
        } else if (!innerShadow) {
            type = Filtering.OUTER;
        }

        float[] ratiosAr = new float[ratios.size()];
        for (int i = 0; i < ratios.size(); i++) {
            ratiosAr[i] = ratios.get(i);
        }
        return Filtering.gradientGlow(src, (int) blurX, (int) blurY, (int) (angle * 180 / Math.PI), distance, colors.toArray(new Color[colors.size()]), ratiosAr, type, passes, strength, knockout);
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
