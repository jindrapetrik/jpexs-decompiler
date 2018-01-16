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
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;

/**
 *
 * @author JPEXS
 */
public class StraightEdgeRecord extends SHAPERECORD {

    /*
     if (!ser.generalLineFlag) {
     ser.vertLineFlag = readUB(1) == 1;
     }
     if (ser.generalLineFlag || (!ser.vertLineFlag)) {
     ser.deltaX = (int) readSB(ser.numBits + 2);
     }
     if (ser.generalLineFlag || (ser.vertLineFlag)) {
     ser.deltaY = (int) readSB(ser.numBits + 2);
     }
     */
    public static final boolean typeFlag = true;

    public static final boolean straightFlag = true;

    @Calculated
    @SWFType(value = BasicType.UB, count = 4)
    public int numBits;

    public boolean generalLineFlag;

    @Conditional("!generalLineFlag")
    public boolean vertLineFlag;

    @SWFType(value = BasicType.SB, countField = "numBits", countAdd = 2)
    @Conditional("generalLineFlag|!vertLineFlag")
    public int deltaX;

    @SWFType(value = BasicType.SB, countField = "numBits", countAdd = 2)
    @Conditional("generalLineFlag|vertLineFlag")
    public int deltaY;

    @Override
    public String toString() {
        return "[StraightEdgeRecord numBits=" + numBits + ", generalLineFlag=" + generalLineFlag + ", vertLineFlag=" + vertLineFlag + ", deltaX=" + deltaX + ", deltaY=" + deltaY + "]";
    }

    @Override
    public int changeX(int x) {
        if (generalLineFlag) {
            return x + deltaX;
        } else if (vertLineFlag) {
            return x;
        } else {
            return x + deltaX;
        }
    }

    @Override
    public int changeY(int y) {
        if (generalLineFlag) {
            return y + deltaY;
        } else if (vertLineFlag) {
            return y + deltaY;
        } else {
            return y;
        }
    }

    @Override
    public void flip() {
        deltaX = -deltaX;
        deltaY = -deltaY;
    }

    @Override
    public boolean isMove() {
        return true;
    }

    @Override
    public void calculateBits() {
        numBits = SWFOutputStream.getNeededBitsS(deltaX, deltaY) - 2;
        if (numBits < 0) {
            numBits = 0;
        }
    }

    public void simplify() {
        if (generalLineFlag) {
            if (deltaX == 0) {
                generalLineFlag = false;
                vertLineFlag = true;
            } else if (deltaY == 0) {
                generalLineFlag = false;
                vertLineFlag = false;
            }
        }
    }
}
