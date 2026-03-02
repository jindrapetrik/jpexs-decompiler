/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The Bevel filter creates a smooth bevel on display list objects.
 *
 * @author JPEXS
 */
public class BEVELFILTER extends FILTER {

    /**
     * Color of the shadow
     */
    public RGBA shadowColor = new RGBA(Color.BLACK);

    /**
     * Color of the highlight
     */
    public RGBA highlightColor = new RGBA(Color.WHITE);

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
    @SWFType(BasicType.UFIXED8)
    public float strength = 1f;

    /**
     * Inner shadow mode
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

    /**
     * Constructor
     */
    public BEVELFILTER() {
        super(3);
    }

    @Override
    public SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH) {
        int type = Filtering.INNER;
        if (onTop && !innerShadow) {
            type = Filtering.FULL;
        } else if (!innerShadow) {
            type = Filtering.OUTER;
        }
        return Filtering.bevel(src, (int) Math.round(blurX * zoom), (int) Math.round(blurY * zoom), strength, type, highlightColor.toInt(), shadowColor.toInt(), (int) (angle * 180 / Math.PI), (float) (distance * zoom), knockout, compositeSource, passes);
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
        RGBA shadowColorTransparent = new RGBA(shadowColor);
        shadowColorTransparent.alpha = 0;
        RGBA highlightColorTransparent = new RGBA(highlightColor);
        highlightColorTransparent.alpha = 0;
        return bevelSvg(distance, angle, new RGBA[]{shadowColor, shadowColorTransparent, highlightColorTransparent, highlightColor}, new int[]{0, 127, 128, 255}, knockout, onTop, innerShadow, blurX, blurY, strength, passes, document, filtersElement, exporter, in);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.shadowColor);
        hash = 47 * hash + Objects.hashCode(this.highlightColor);
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.blurX) ^ (Double.doubleToLongBits(this.blurX) >>> 32));
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.blurY) ^ (Double.doubleToLongBits(this.blurY) >>> 32));
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.angle) ^ (Double.doubleToLongBits(this.angle) >>> 32));
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.distance) ^ (Double.doubleToLongBits(this.distance) >>> 32));
        hash = 47 * hash + Float.floatToIntBits(this.strength);
        hash = 47 * hash + (this.innerShadow ? 1 : 0);
        hash = 47 * hash + (this.knockout ? 1 : 0);
        hash = 47 * hash + (this.compositeSource ? 1 : 0);
        hash = 47 * hash + (this.onTop ? 1 : 0);
        hash = 47 * hash + this.passes;
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
        final BEVELFILTER other = (BEVELFILTER) obj;
        if (Double.doubleToLongBits(this.blurX) != Double.doubleToLongBits(other.blurX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.blurY) != Double.doubleToLongBits(other.blurY)) {
            return false;
        }
        if (Double.doubleToLongBits(this.angle) != Double.doubleToLongBits(other.angle)) {
            return false;
        }
        if (Double.doubleToLongBits(this.distance) != Double.doubleToLongBits(other.distance)) {
            return false;
        }
        if (Float.floatToIntBits(this.strength) != Float.floatToIntBits(other.strength)) {
            return false;
        }
        if (this.innerShadow != other.innerShadow) {
            return false;
        }
        if (this.knockout != other.knockout) {
            return false;
        }
        if (this.compositeSource != other.compositeSource) {
            return false;
        }
        if (this.onTop != other.onTop) {
            return false;
        }
        if (this.passes != other.passes) {
            return false;
        }
        if (!Objects.equals(this.shadowColor, other.shadowColor)) {
            return false;
        }
        return Objects.equals(this.highlightColor, other.highlightColor);
    }

}
