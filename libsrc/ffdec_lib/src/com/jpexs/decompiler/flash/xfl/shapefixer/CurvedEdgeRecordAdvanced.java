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

import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;

/**
 * Advanced curved edge record with double precision.
 *
 * @author JPEXS
 */
public class CurvedEdgeRecordAdvanced extends ShapeRecordAdvanced {

    public double controlDeltaX;
    public double controlDeltaY;
    public double anchorDeltaX;
    public double anchorDeltaY;

    public CurvedEdgeRecordAdvanced() {
    }

    public CurvedEdgeRecordAdvanced(int controlDeltaX, int controlDeltaY, int anchorDeltaX, int anchorDeltaY) {
        this.controlDeltaX = controlDeltaX;
        this.controlDeltaY = controlDeltaY;
        this.anchorDeltaX = anchorDeltaX;
        this.anchorDeltaY = anchorDeltaY;
    }

    public CurvedEdgeRecordAdvanced(CurvedEdgeRecord cer) {
        this.controlDeltaX = cer.controlDeltaX;
        this.controlDeltaY = cer.controlDeltaY;
        this.anchorDeltaX = cer.anchorDeltaX;
        this.anchorDeltaY = cer.anchorDeltaY;
    }

    @Override
    public double changeX(double x) {
        return x + controlDeltaX + anchorDeltaX;
    }

    @Override
    public double changeY(double y) {
        return y + controlDeltaY + anchorDeltaY;
    }

    @Override
    public CurvedEdgeRecord toBasicRecord() {
        CurvedEdgeRecord ret = new CurvedEdgeRecord();
        ret.controlDeltaX = (int) Math.round(controlDeltaX);
        ret.controlDeltaY = (int) Math.round(controlDeltaY);
        ret.anchorDeltaX = (int) Math.round(anchorDeltaX);
        ret.anchorDeltaY = (int) Math.round(anchorDeltaY);
        ret.calculateBits();
        return ret;
    }

    @Override
    public void round() {
        controlDeltaX = Math.round(controlDeltaX);
        controlDeltaY = Math.round(controlDeltaY);
        anchorDeltaX = Math.round(anchorDeltaX);
        anchorDeltaY = Math.round(anchorDeltaY);
    }
}
