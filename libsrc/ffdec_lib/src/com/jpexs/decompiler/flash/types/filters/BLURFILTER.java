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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Blur filter based on a sub-pixel precise median filter.
 *
 * @author JPEXS
 */
public class BLURFILTER extends FILTER {

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
     * Number of blur passes
     */
    @SWFType(value = BasicType.UB, count = 5)
    public int passes = 1;

    /**
     * Reserved
     */
    @Reserved
    @SWFType(value = BasicType.UB, count = 3)
    public int reserved;

    /**
     * Constructor.
     */
    public BLURFILTER() {
        super(1);
    }

    @Override
    public SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH) {
        return Filtering.blur(src, (int) Math.round(blurX * zoom), (int) Math.round(blurY * zoom), passes);
    }

    @Override
    public double getDeltaX() {
        return blurX;
    }

    @Override
    public double getDeltaY() {
        return blurY;
    }

    @Override
    public String toSvg(Document document, Element filtersElement, SVGExporter exporter, String in) {
        return blurSvg(blurX, blurY, passes, document, filtersElement, exporter, in);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.blurX) ^ (Double.doubleToLongBits(this.blurX) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.blurY) ^ (Double.doubleToLongBits(this.blurY) >>> 32));
        hash = 37 * hash + this.passes;
        hash = 37 * hash + this.reserved;
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
        final BLURFILTER other = (BLURFILTER) obj;
        if (Double.doubleToLongBits(this.blurX) != Double.doubleToLongBits(other.blurX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.blurY) != Double.doubleToLongBits(other.blurY)) {
            return false;
        }
        if (this.passes != other.passes) {
            return false;
        }
        return this.reserved == other.reserved;
    }
    
    
}
