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

import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Point;
import java.io.Serializable;

/**
 * Represents a standard 2x3 transformation matrix of the sort commonly used in
 * 2D graphics.
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
    public float scaleX;

    /**
     * Y scale value
     */
    @Conditional("hasScale")
    @SWFType(value = BasicType.FB, countField = "nScaleBits")
    public float scaleY;

    /**
     * Has rotate and skew values
     */
    public boolean hasRotate;

    /**
     * First rotate and skew value
     */
    @Conditional("hasRotate")
    @SWFType(value = BasicType.FB, countField = "nRotateBits")
    public float rotateSkew0;

    /**
     * Second rotate and skew value
     */
    @Conditional("hasRotate")
    @SWFType(value = BasicType.FB, countField = "nRotateBits")
    public float rotateSkew1;

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

    /**
     * Number of bits used for the translate values
     */
    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    public int nTranslateBits;

    /**
     * Number of bits used for the rotate values
     */
    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    public int nRotateBits;

    /**
     * Number of bits used for the scale values
     */
    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    public int nScaleBits;

    /**
     * Constructor.
     */
    public MATRIX() {
    }

    /**
     * Constructor.
     * @param m Matrix to copy
     */
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

    /**
     * Converts (fixed point) integer to float
     * @param i Integer
     * @return Float
     */
    public static float toFloat(int i) {
        return ((float) i) / (1 << 16);
    }

    /**
     * Applies the matrix to a point
     * @param p Point
     * @return Transformed point
     */
    public Point apply(Point p) {
        Point ret = new Point();
        ret.x = (int) (p.x * (hasScale ? scaleX : 1) + p.y * (hasRotate ? rotateSkew1 : 0) + translateX);
        ret.y = (int) (p.x * (hasRotate ? rotateSkew0 : 0) + p.y * (hasScale ? scaleY : 1) + translateY);
        return ret;
    }

    /**
     * Applies the matrix to a rectangle
     * @param r Rectangle
     * @return Transformed rectangle
     */
    public RECT apply(RECT r) {
        Point topLeft = apply(r.getTopLeft());
        Point bottomRight = apply(r.getBottomRight());
        int Xmin = Math.min(topLeft.x, bottomRight.x);
        int Ymin = Math.min(topLeft.y, bottomRight.y);
        int Xmax = Math.max(topLeft.x, bottomRight.x);
        int Ymax = Math.max(topLeft.y, bottomRight.y);
        return new RECT(Xmin, Xmax, Ymin, Ymax);

    }

    private int fromFloat(double f) {
        return (int) (f * (1 << 16));
    }

    /**
     * Gets the rotate skew 0 value as an integer
     * @return Integer
     */
    public int getRotateSkew0Integer() {
        return hasRotate ? fromFloat(rotateSkew0) : 0;
    }

    /**
     * Gets the rotate skew 1 value as an integer
     * @return Integer
     */
    public int getRotateSkew1Integer() {
        return hasRotate ? fromFloat(rotateSkew1) : 0;
    }

    /**
     * Gets rotate skew 0 as a float
     * @return Float
     */
    public float getRotateSkew0Float() {
        return (hasRotate ? rotateSkew0 : 0);
    }

    /**
     * Gets rotate skew 1 as a float
     * @return Float
     */
    public float getRotateSkew1Float() {
        return (hasRotate ? rotateSkew1 : 0);
    }

    /**
     * Gets the scale X value as a float
     * @return Float
     */
    public float getScaleXFloat() {
        return (hasScale ? scaleX : 1);
    }

    /**
     * Gets the scale Y value as a float
     * @return Float
     */
    public float getScaleYFloat() {
        return (hasScale ? scaleY : 1);
    }

    /**
     * Gets the scale X value as an integer
     * @return Integer
     */
    public int getScaleXInteger() {
        return (hasScale ? fromFloat(scaleX) : (1 << 16));
    }

    /**
     * Gets the scale Y value as an integer
     * @return Integer
     */
    public int getScaleYInteger() {
        return (hasScale ? fromFloat(scaleY) : (1 << 16));
    }

    /**
     * Checks if the matrix is empty
     * @return True if empty
     */
    public boolean isEmpty() {
        return (translateX == 0) && (translateY == 0) && (!hasRotate) && (!hasScale);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + getScaleXInteger();
        hash = 37 * hash + getScaleYInteger();
        hash = 37 * hash + getRotateSkew0Integer();
        hash = 37 * hash + getRotateSkew1Integer();
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
        if (getScaleXInteger() != other.getScaleXInteger()) {
            return false;
        }
        if (getScaleYInteger() != other.getScaleYInteger()) {
            return false;
        }
        if (getRotateSkew0Integer() != other.getRotateSkew0Integer()) {
            return false;
        }
        if (getRotateSkew1Integer() != other.getRotateSkew1Integer()) {
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
