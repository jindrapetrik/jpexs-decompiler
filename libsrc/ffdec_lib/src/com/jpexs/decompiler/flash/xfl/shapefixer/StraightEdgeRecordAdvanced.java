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
package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;

/**
 * Straight edge record with double precision.
 *
 * @author JPEXS
 */
public class StraightEdgeRecordAdvanced extends ShapeRecordAdvanced {

    public double deltaX;
    public double deltaY;

    public StraightEdgeRecordAdvanced() {
    }

    public StraightEdgeRecordAdvanced(double deltaX, double deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public StraightEdgeRecordAdvanced(StraightEdgeRecord ser) {
        this.deltaX = ser.deltaX;
        this.deltaY = ser.deltaY;
    }

    @Override
    public double changeX(double x) {
        return x + deltaX;
    }

    @Override
    public double changeY(double y) {
        return y + deltaY;
    }

    @Override
    public StraightEdgeRecord toBasicRecord() {
        StraightEdgeRecord ret = new StraightEdgeRecord();
        ret.generalLineFlag = true;
        ret.deltaX = (int) Math.round(deltaX);
        ret.deltaY = (int) Math.round(deltaY);
        ret.simplify();
        return ret;
    }

    @Override
    public void round() {
        deltaX = Math.round(deltaX);
        deltaY = Math.round(deltaY);
    }
}
