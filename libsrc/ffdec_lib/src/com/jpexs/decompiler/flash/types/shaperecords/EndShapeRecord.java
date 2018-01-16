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

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;

/**
 *
 * @author JPEXS
 */
public class EndShapeRecord extends SHAPERECORD {

    public static final boolean typeFlag = false;

    @SWFType(value = BasicType.UB, count = 5)
    public int endOfShape = 0;

    @Override
    public String toString() {
        return "[EndShapeRecord]";
    }

    @Override
    public int changeX(int x) {
        return x;
    }

    @Override
    public int changeY(int y) {
        return y;
    }

    @Override
    public void flip() {
    }

    @Override
    public boolean isMove() {
        return false;
    }

    @Override
    public void calculateBits() {
    }
}
