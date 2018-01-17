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
public class StraightMorphEdge implements IMorphEdge {

    protected final int fromX;

    protected final int fromY;

    protected final int toX;

    protected final int toY;

    protected final int fromEndX;

    protected final int fromEndY;

    protected final int toEndX;

    protected final int toEndY;

    protected final int lineStyleIdx;

    private final int fillStyleIdx;

    StraightMorphEdge(int fromX, int fromY, int toX, int toY, int fromEndX, int fromEndY, int toEndX, int toEndY, int lineStyleIdx, int fillStyleIdx) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.fromEndX = fromEndX;
        this.fromEndY = fromEndY;
        this.toEndX = toEndX;
        this.toEndY = toEndY;
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
    public int getFromEndX() {
        return fromEndX;
    }

    @Override
    public int getFromEndY() {
        return fromEndY;
    }

    @Override
    public int getToEndX() {
        return toEndX;
    }

    @Override
    public int getToEndY() {
        return toEndY;
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
    public IMorphEdge reverseWithNewFillStyle(int newFillStyleIdx) {
        return new StraightMorphEdge(toX, toY, fromX, fromY, toEndX, toEndY, fromEndX, fromEndY, lineStyleIdx, newFillStyleIdx);
    }
}
