/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.morphshape;

import com.jpexs.decompiler.flash.exporters.commonshape.PointInt;

/**
 *
 * @author JPEXS
 */
public class CurvedMorphEdge extends StraightMorphEdge implements IMorphEdge {

    private final PointInt control;
    private final PointInt controlEnd;

    CurvedMorphEdge(PointInt from, PointInt control, PointInt to,
            PointInt fromEnd, PointInt controlEnd, PointInt toEnd, int lineStyleIdx, int fillStyleIdx) {
        super(from, to, fromEnd, toEnd, lineStyleIdx, fillStyleIdx);
        this.control = control;
        this.controlEnd = controlEnd;
    }

    public PointInt getControl() {
        return control;
    }

    public PointInt getControlEnd() {
        return controlEnd;
    }

    @Override
    public IMorphEdge reverseWithNewFillStyle(int newFillStyleIdx) {
        return new CurvedMorphEdge(to, control, from, toEnd, controlEnd, fromEnd, lineStyleIdx, newFillStyleIdx);
    }
}
