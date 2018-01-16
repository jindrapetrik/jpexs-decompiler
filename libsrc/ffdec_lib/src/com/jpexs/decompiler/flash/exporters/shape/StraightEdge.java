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
public class StraightEdge implements IEdge {

    protected final int fromX;

    protected final int fromY;

    protected final int toX;

    protected final int toY;

    protected final int lineStyleIdx;

    private final int fillStyleIdx;

    StraightEdge(int fromX, int fromY, int toX, int toY, int lineStyleIdx, int fillStyleIdx) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.lineStyleIdx = lineStyleIdx;
        this.fillStyleIdx = fillStyleIdx;
    }

    @Override
    public int getFromX() {
        return fromX;
    }

    @Override
    public int getFromY() {
        return fromY;
    }

    @Override
    public int getToX() {
        return toX;
    }

    @Override
    public int getToY() {
        return toY;
    }

    @Override
    public int getLineStyleIdx() {
        return lineStyleIdx;
    }

    @Override
    public int getFillStyleIdx() {
        return fillStyleIdx;
    }

    @Override
    public IEdge reverseWithNewFillStyle(int newFillStyleIdx) {
        return new StraightEdge(toX, toY, fromX, fromY, lineStyleIdx, newFillStyleIdx);
    }
}
