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
 * Straight edge.
 *
 * @author JPEXS
 */
public class StraightEdge implements IEdge {

    /**
     * X coordinate of the start point
     */
    protected final int fromX;

    /**
     * Y coordinate of the start point
     */
    protected final int fromY;

    /**
     * X coordinate of the end point
     */
    protected final int toX;

    /**
     * Y coordinate of the end point
     */
    protected final int toY;

    /**
     * Line style index
     */
    protected final int lineStyleIdx;

    /**
     * Fill style index
     */
    private final int fillStyleIdx;

    /**
     * Constructor.
     * @param fromX X coordinate of the start point
     * @param fromY Y coordinate of the start point
     * @param toX X coordinate of the end point
     * @param toY Y coordinate of the end point
     * @param lineStyleIdx Line style index
     * @param fillStyleIdx Fill style index
     */
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

    @Override
    public String toString() {
        return "straight[" + fromX / 20f + "," + fromY / 20f + " -> " + toX / 20f + "," + toY / 20f + "]";
    }

    @Override
    public IEdge sameWithNewFillStyle(int newFillStyleIdx) {
        return new StraightEdge(fromX, fromY, toX, toY, lineStyleIdx, newFillStyleIdx);
    }

    @Override
    public IEdge reverse() {
        return new StraightEdge(toX, toY, fromX, fromY, lineStyleIdx, getFillStyleIdx());
    }

}
