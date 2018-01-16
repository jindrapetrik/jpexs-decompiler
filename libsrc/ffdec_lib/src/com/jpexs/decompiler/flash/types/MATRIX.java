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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Point;
import java.io.Serializable;

/**
 * Represents a standard 2x3 transformation matrix of the sort commonly used in
 * 2D graphics
 *
 * @author JPEXS
 */
public class MATRIX implements Serializable {

    /**
     * Has scale values
     */
    public boolean hasScale;

    /**
     * X scale value
     */
    @Conditional("hasScale")
    @SWFType(value = BasicType.FB, countField = "nScaleBits")
    public int scaleX;

    /**
     * Y scale value
     */
    @Conditional("hasScale")
    @SWFType(value = BasicType.FB, countField = "nScaleBits")
    public int scaleY;

    /**
     * Has rotate and skew values
     */
    public boolean hasRotate;

    /**
     * First rotate and skew value
     */
    @Conditional("hasRotate")
    @SWFType(value = BasicType.FB, countField = "nRotateBits")
    public int rotateSkew0;

    /**
     * Second rotate and skew value
     */
    @Conditional("hasRotate")
    @SWFType(value = BasicType.FB, countField = "nRotateBits")
    public int rotateSkew1;

    /**
     * X translate value in twips
     */
    @SWFType(value = BasicType.SB, countField = "nTranslateBits")
    public int translateX;

    /**
     * Y translate value in twips
     */
    @SWFType(value = BasicType.SB, countField = "nTranslateBits")
    public int translateY;

    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    public int nTranslateBits;

    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    public int nRotateBits;

    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    public int nScaleBits;

    public MATRIX() {
    }

    public MATRIX(MATRIX m) {
        if (m == null) {
            return;
        }
        hasScale = m.hasScale;
        hasRotate = m.hasRotate;
        scaleX = m.scaleX;
        scaleY = m.scaleY;
        rotateSkew0 = m.rotateSkew0;
        rotateSkew1 = m.rotateSkew1;
        translateX = m.translateX;
        translateY = m.translateY;
    }

    @Override
    public String toString() {
        return "[MATRIX scale:" + getScaleXFloat() + "," + getScaleYFloat() + ", rotate:" + getRotateSkew0Float() + "," + getRotateSkew1Float() + ", translate:" + translateX + "," + translateY + "]";
    }

    private float toFloat(int i) {
        return ((float) i) / (1 << 16);
    }

    public Point apply(Point p) {
        Point ret = new Point();
        ret.x = (int) (p.x * (hasScale ? toFloat(scaleX) : 1) + p.y * (hasRotate ? toFloat(rotateSkew1) : 0) + translateX);
        ret.y = (int) (p.x * (hasRotate ? toFloat(rotateSkew0) : 0) + p.y * (hasScale ? toFloat(scaleY) : 1) + translateY);
        return ret;
    }

    public RECT apply(RECT r) {
        Point topLeft = apply(r.getTopLeft());
        Point bottomRight = apply(r.getBottomRight());
        int Xmin = Math.min(topLeft.x, bottomRight.x);
        int Ymin = Math.min(topLeft.y, bottomRight.y);
        int Xmax = Math.max(topLeft.x, bottomRight.x);
        int Ymax = Math.max(topLeft.y, bottomRight.y);
        return new RECT(Xmin, Xmax, Ymin, Ymax);

    }

    public int getRotateSkew0() {
        return hasRotate ? rotateSkew0 : 0;
    }

    public int getRotateSkew1() {
        return hasRotate ? rotateSkew1 : 0;
    }

    public float getRotateSkew0Float() {
        return (hasRotate ? toFloat(rotateSkew0) : 0);
    }

    public float getRotateSkew1Float() {
        return (hasRotate ? toFloat(rotateSkew1) : 0);
    }

    public float getScaleXFloat() {
        return (hasScale ? toFloat(scaleX) : 1);
    }

    public float getScaleYFloat() {
        return (hasScale ? toFloat(scaleY) : 1);
    }

    public int getScaleX() {
        return (hasScale ? (scaleX) : (1 << 16));
    }

    public int getScaleY() {
        return (hasScale ? (scaleY) : (1 << 16));
    }

    public boolean isEmpty() {
        return (translateX == 0) && (translateY == 0) && (!hasRotate) && (!hasScale);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + getScaleX();
        hash = 37 * hash + getScaleY();
        hash = 37 * hash + getRotateSkew0();
        hash = 37 * hash + getRotateSkew1();
        hash = 37 * hash + translateX;
        hash = 37 * hash + translateY;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MATRIX other = (MATRIX) obj;
        if (getScaleX() != other.getScaleX()) {
            return false;
        }
        if (getScaleY() != other.getScaleY()) {
            return false;
        }
        if (getRotateSkew0() != other.getRotateSkew0()) {
            return false;
        }
        if (getRotateSkew1() != other.getRotateSkew1()) {
            return false;
        }
        if (translateX != other.translateX) {
            return false;
        }
        if (translateY != other.translateY) {
            return false;
        }
        return true;
    }
}
