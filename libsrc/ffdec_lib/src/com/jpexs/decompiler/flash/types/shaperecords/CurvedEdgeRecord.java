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
package com.jpexs.decompiler.flash.types.shaperecords;

import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.SWFType;

/**
 *
 * @author JPEXS
 */
public class CurvedEdgeRecord extends SHAPERECORD {

    public static final boolean typeFlag = true;

    public static final boolean straightFlag = false;

    @Calculated
    @SWFType(value = BasicType.UB, count = 4)
    public int numBits;

    @SWFType(value = BasicType.SB, countField = "numBits", countAdd = 2)
    public int controlDeltaX;

    @SWFType(value = BasicType.SB, countField = "numBits", countAdd = 2)
    public int controlDeltaY;

    @SWFType(value = BasicType.SB, countField = "numBits", countAdd = 2)
    public int anchorDeltaX;

    @SWFType(value = BasicType.SB, countField = "numBits", countAdd = 2)
    public int anchorDeltaY;

    @Override
    public String toString() {
        return "[CurvedEdgeRecord numBits=" + numBits + ", controlDeltaX=" + controlDeltaX + ", controlDeltaY=" + controlDeltaY + ", anchorDeltaX=" + anchorDeltaX + ". anchorDeltaY=" + anchorDeltaY + "]";
    }

    @Override
    public int changeX(int x) {
        return x + (controlDeltaX + anchorDeltaX);
    }

    @Override
    public int changeY(int y) {
        return y + (controlDeltaY + anchorDeltaY);
    }

    @Override
    public void flip() {
        int tmp;
        tmp = controlDeltaX;
        controlDeltaX = -anchorDeltaX;
        anchorDeltaX = -tmp;
        tmp = controlDeltaY;
        controlDeltaY = -anchorDeltaY;
        anchorDeltaY = -tmp;
    }

    @Override
    public boolean isMove() {
        return true;
    }

    @Override
    public void calculateBits() {
        numBits = SWFOutputStream.getNeededBitsS(controlDeltaX, controlDeltaY, anchorDeltaX, anchorDeltaY) - 2;
        if (numBits < 0) {
            numBits = 0;
        }
    }
}
