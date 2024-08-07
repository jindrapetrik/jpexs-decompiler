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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Bevel filter with gradient instead of single color.
 *
 * @author JPEXS
 */
public class GRADIENTBEVELFILTER extends FILTER {

    /**
     * Gradient colors
     */
    @SWFArray(value = "color", countField = "numColors")
    public RGBA[] gradientColors = new RGBA[]{
        new RGBA(Color.WHITE),
        new RGBA(255, 0, 0, 0),
        new RGBA(Color.BLACK)
    };

    /**
     * Gradient ratios
     */
    @SWFType(value = BasicType.UI8)
    @SWFArray(value = "ratio", countField = "numColors")
    public int[] gradientRatio = new int[]{
        0, 128, 255
    };

    /**
     * Horizontal blur amount
     */
    @SWFType(BasicType.FIXED)
    public double blurX = 5;

    /**
     * Vertical blur amount
     */
    @SWFType(BasicType.FIXED)
    public double blurY = 5;

    /**
     * Radian angle of the gradient bevel
     */
    @SWFType(BasicType.FIXED)
    public double angle = 45 * Math.PI / 180;

    /**
     * Distance of the gradient bevel
     */
    @SWFType(BasicType.FIXED)
    public double distance = 5;

    /**
     * Strength of the gradient bevel
     */
    @SWFType(BasicType.FIXED8)
    public float strength = 1;

    /**
     * Inner bevel mode
     */
    public boolean innerShadow = true;

    /**
     * Knockout mode
     */
    public boolean knockout = false;

    /**
     * Composite source
     */
    public boolean compositeSource = true;

    /**
     * OnTop mode
     */
    public boolean onTop = false;

    /**
     * Number of blur passes
     */
    @SWFType(value = BasicType.UB, count = 4)
    public int passes = 1;

    public GRADIENTBEVELFILTER() {
        super(7);
    }

    @Override
    public SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH) {
        List<Color> colors = new ArrayList<>();
        List<Float> ratios = new ArrayList<>();
        for (int i = 0; i < gradientColors.length; i++) {
            if ((i > 0) && (gradientRatio[i - 1] == gradientRatio[i])) {
                continue;
            }
            colors.add(gradientColors[i].toColor());
            ratios.add(gradientRatio[i] / 255f);
        }
        float[] ratiosArr = new float[ratios.size()];
        for (int i = 0; i < ratios.size(); i++) {
            ratiosArr[i] = ratios.get(i);
        }

        Color[] colorsArr = colors.toArray(new Color[colors.size()]);
        int type = Filtering.INNER;
        if (onTop && !innerShadow) {
            type = Filtering.FULL;
        } else if (!innerShadow) {
            type = Filtering.OUTER;
        }

        return Filtering.gradientBevel(src, colorsArr, ratiosArr, (int) Math.round(blurX * zoom), (int) Math.round(blurY * zoom), strength, type, (int) (angle * 180 / Math.PI), (float) (distance * zoom), knockout, passes);
    }

    @Override
    public double getDeltaX() {
        return blurX + Math.abs(distance * Math.cos(angle));
    }

    @Override
    public double getDeltaY() {
        return blurY + Math.abs(distance * Math.sin(angle));
    }

    @Override
    public String toSvg(Document document, Element filtersElement, SVGExporter exporter, String in) {
        return null; //NOT SUPPORTED
    }
}
