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
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.Serializable;

/**
 * Advanced shape record with double precision.
 *
 * @author JPEXS
 */
public abstract class ShapeRecordAdvanced implements Serializable {

    public abstract double changeX(double x);

    public abstract double changeY(double y);

    public abstract SHAPERECORD toBasicRecord();

    public static ShapeRecordAdvanced createFromSHAPERECORD(SHAPERECORD rec) {
        if (rec instanceof StyleChangeRecord) {
            return new StyleChangeRecordAdvanced((StyleChangeRecord) rec);
        }
        if (rec instanceof CurvedEdgeRecord) {
            return new CurvedEdgeRecordAdvanced((CurvedEdgeRecord) rec);
        }
        if (rec instanceof StraightEdgeRecord) {
            return new StraightEdgeRecordAdvanced((StraightEdgeRecord) rec);
        }
        return null;
    }

    public abstract void round();
}
