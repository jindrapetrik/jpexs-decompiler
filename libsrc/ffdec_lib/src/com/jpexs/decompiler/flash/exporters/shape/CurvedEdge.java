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
package com.jpexs.decompiler.flash.exporters.shape;

/**
 *
 * @author JPEXS
 */
public class CurvedEdge extends StraightEdge implements IEdge {

    private final int controlX;

    private final int controlY;

    CurvedEdge(int fromX, int fromY, int controlX, int controlY, int toX, int toY, int lineStyleIdx, int fillStyleIdx) {
        super(fromX, fromY, toX, toY, lineStyleIdx, fillStyleIdx);
        this.controlX = controlX;
        this.controlY = controlY;
    }

    public int getControlX() {
        return controlX;
    }

    public int getControlY() {
        return controlY;
    }

    @Override
    public IEdge reverseWithNewFillStyle(int newFillStyleIdx) {
        return new CurvedEdge(toX, toY, controlX, controlY, fromX, fromY, lineStyleIdx, newFillStyleIdx);
    }
}
