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
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Drop shadow filter based on the same median filter as the blur filter.
 *
 * @author JPEXS
 */
public class DROPSHADOWFILTER extends FILTER {

    /**
     * Color of the shadow
     */
    public RGBA dropShadowColor = new RGBA(Color.BLACK);

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
     * Radian angle of the drop shadow
     */
    @SWFType(BasicType.FIXED)
    public double angle = 45 * Math.PI / 180;

    /**
     * Distance of the drop shadow
     */
    @SWFType(BasicType.FIXED)
    public double distance = 5;

    /**
     * Strength of the drop shadow
     */
    @SWFType(BasicType.FIXED8)
    public float strength = 1f;

    /**
     * Inner shadow mode
     */
    public boolean innerShadow = false;

    /**
     * Knockout mode
     */
    public boolean knockout = false;

    /**
     * Composite source
     */
    public boolean compositeSource = true;

    /**
     * Number of blur passes
     */
    @SWFType(value = BasicType.UB, count = 5)
    public int passes = 1;

    /**
     * Constructor
     */
    public DROPSHADOWFILTER() {
        super(0);
    }

    @Override
    public SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH) {
        return Filtering.dropShadow(src, (int) Math.round(blurX * zoom), (int) Math.round(blurY * zoom), (int) (angle * 180 / Math.PI), distance * zoom, dropShadowColor.toColor(), innerShadow, passes, strength, knockout, compositeSource);
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
        return dropShadowSvg(distance, angle, dropShadowColor, innerShadow, knockout, compositeSource, blurX, blurY, strength, passes, document, filtersElement, exporter, in);
    }
}
