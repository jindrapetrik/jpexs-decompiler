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
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Point;
import java.io.Serializable;

/**
 * A rectangle value represents a rectangular region.
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

    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    public int nbits;

    public RECT(int Xmin, int Xmax, int Ymin, int Ymax) {
        this.Xmin = Xmin;
        this.Xmax = Xmax;
        this.Ymin = Ymin;
        this.Ymax = Ymax;
    }

    public RECT() {
    }

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

    public int getWidth() {
        return (Xmax - Xmin) < 0 ? 0 : Xmax - Xmin;
    }

    public int getHeight() {
        return (Ymax - Ymin) < 0 ? 0 : Ymax - Ymin;
    }

    public Point getTopLeft() {
        return new Point(Xmin, Ymin);
    }

    public Point getBottomRight() {
        return new Point(Xmax, Ymax);
    }
}
