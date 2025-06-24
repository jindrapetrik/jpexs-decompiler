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
package com.jpexs.decompiler.flash.exporters.shape;

/**
 * Curved edge.
 *
 * @author JPEXS
 */
public class CurvedEdge extends StraightEdge implements IEdge {

    private final int controlX;

    private final int controlY;

    /**
     * Constructor.
     * @param fromX From X
     * @param fromY From Y
     * @param controlX Control X
     * @param controlY Control Y
     * @param toX To X
     * @param toY To Y
     * @param lineStyleIdx Line style index
     * @param fillStyleIdx Fill style index
     */
    CurvedEdge(int fromX, int fromY, int controlX, int controlY, int toX, int toY, int lineStyleIdx, int fillStyleIdx) {
        super(fromX, fromY, toX, toY, lineStyleIdx, fillStyleIdx);
        this.controlX = controlX;
        this.controlY = controlY;
    }

    /**
     * Gets control X.
     * @return Control X
     */
    public int getControlX() {
        return controlX;
    }

    /**
     * Gets control Y.
     * @return Control Y
     */
    public int getControlY() {
        return controlY;
    }

    @Override
    public IEdge reverseWithNewFillStyle(int newFillStyleIdx) {
        return new CurvedEdge(toX, toY, controlX, controlY, fromX, fromY, lineStyleIdx, newFillStyleIdx);
    }

    @Override
    public IEdge sameWithNewFillStyle(int newFillStyleIdx) {
        return new CurvedEdge(fromX, fromY, controlX, controlY, toX, toY, lineStyleIdx, newFillStyleIdx);
    }

    @Override
    public String toString() {
        return "curved[" + fromX / 20f + "," + fromY / 20f + " -> " + toX / 20f + "," + toY / 20f + " control:" + controlX / 20f + "," + controlY / 20f + "]";
    }

    @Override
    public IEdge reverse() {
        return new CurvedEdge(toX, toY, controlX, controlY, fromX, fromY, lineStyleIdx, getFillStyleIdx());
    }
}
