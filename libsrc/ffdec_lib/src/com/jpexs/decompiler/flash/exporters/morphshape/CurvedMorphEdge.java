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
package com.jpexs.decompiler.flash.exporters.morphshape;

/**
 *
 * @author JPEXS
 */
public class CurvedMorphEdge extends StraightMorphEdge implements IMorphEdge {

    private final int controlX;

    private final int controlY;

    private final int controlEndX;

    private final int controlEndY;

    CurvedMorphEdge(int fromX, int fromY, int controlX, int controlY, int toX, int toY,
            int fromEndX, int fromEndY, int controlEndX, int controlEndY, int toEndX, int toEndY, int lineStyleIdx, int fillStyleIdx) {
        super(fromX, fromY, toX, toY, fromEndX, fromEndY, toEndX, toEndY, lineStyleIdx, fillStyleIdx);
        this.controlX = controlX;
        this.controlY = controlY;
        this.controlEndX = controlEndX;
        this.controlEndY = controlEndY;
    }

    public int getControlX() {
        return controlX;
    }

    public int getControlY() {
        return controlY;
    }

    public int getControlEndX() {
        return controlEndX;
    }

    public int getControlEndY() {
        return controlEndY;
    }

    @Override
    public IMorphEdge reverseWithNewFillStyle(int newFillStyleIdx) {
        return new CurvedMorphEdge(toX, toY, controlX, controlY, fromX, fromY, toEndX, toEndY, controlEndX, controlEndY, fromEndX, fromEndY, lineStyleIdx, newFillStyleIdx);
    }
}
