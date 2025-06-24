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

import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Point;
import java.io.Serializable;

/**
 * Rectangular region.
 *
 * @author JPEXS
 */
public class RECT implements Serializable {

    /**
     * X minimum position for rectangle in twips
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int Xmin;

    /**
     * X maximum position for rectangle in twips
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int Xmax;

    /**
     * Y minimum position for rectangle in twips
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int Ymin;

    /**
     * Y maximum position for rectangle in twips
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int Ymax;

    /**
     * Number of bits
     */
    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    public int nbits;

    /**
     * Constructor.
     * @param Xmin X minimum position for rectangle in twips
     * @param Xmax X maximum position for rectangle in twips
     * @param Ymin Y minimum position for rectangle in twips
     * @param Ymax Y maximum position for rectangle in twips
     */
    public RECT(int Xmin, int Xmax, int Ymin, int Ymax) {
        this.Xmin = Xmin;
        this.Xmax = Xmax;
        this.Ymin = Ymin;
        this.Ymax = Ymax;
    }

    /**
     * Constructor.
     */
    public RECT() {
    }

    /**
     * Copy constructor.
     * @param r RECT to copy
     */
    public RECT(RECT r) {
        Xmin = r.Xmin;
        Xmax = r.Xmax;
        Ymin = r.Ymin;
        Ymax = r.Ymax;
    }

    @Override
    public String toString() {
        return "[RECT x=" + Xmin + " to " + Xmax + ", y=" + Ymin + " to " + Ymax + "]";
    }

    /**
     * Gets width of rectangle.
     * @return Width of rectangle
     */
    public int getWidth() {
        return (Xmax - Xmin) < 0 ? 0 : Xmax - Xmin;
    }

    /**
     * Gets height of rectangle.
     * @return Height of rectangle
     */
    public int getHeight() {
        return (Ymax - Ymin) < 0 ? 0 : Ymax - Ymin;
    }

    /**
     * Gets top left point of rectangle.
     * @return Top left point of rectangle
     */
    public Point getTopLeft() {
        return new Point(Xmin, Ymin);
    }

    /**
     * Gets bottom right point of rectangle.
     * @return Bottom right point of rectangle
     */
    public Point getBottomRight() {
        return new Point(Xmax, Ymax);
    }

    /**
     * Calculates number of bits needed to store this RECT.
     */
    public void calculateBits() {
        nbits = SWFOutputStream.getNeededBitsS(Xmin, Xmax, Ymin, Ymax);
    }

    /**
     * Checks if this RECT is too large to number of bits be stored in 5 bits.
     * @return True if this RECT is too large
     */
    public boolean isTooLarge() {
        calculateBits();
        return !SWFOutputStream.fitsInUB(5, nbits);
    }
}
