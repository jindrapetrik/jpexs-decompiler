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
package com.jpexs.decompiler.flash.exporters.morphshape;

/**
 * Straight morph edge.
 *
 * @author JPEXS
 */
public class StraightMorphEdge implements IMorphEdge {

    /**
     * From X coordinate.
     */
    protected final int fromX;

    /**
     * From Y coordinate.
     */
    protected final int fromY;

    /**
     * To X coordinate.
     */
    protected final int toX;

    /**
     * To Y coordinate.
     */
    protected final int toY;

    /**
     * From end X coordinate.
     */
    protected final int fromEndX;

    /**
     * From end Y coordinate.
     */
    protected final int fromEndY;

    /**
     * To end X coordinate.
     */
    protected final int toEndX;

    /**
     * To end Y coordinate.
     */
    protected final int toEndY;

    /**
     * Line style index.
     */
    protected final int lineStyleIdx;

    /**
     * Fill style index.
     */
    private final int fillStyleIdx;

    /**
     * Constructor.
     * @param fromX From X coordinate.
     * @param fromY From Y coordinate.
     * @param toX To X coordinate.
     * @param toY To Y coordinate.
     * @param fromEndX From end X coordinate.
     * @param fromEndY From end Y coordinate.
     * @param toEndX To end X coordinate.
     * @param toEndY To end Y coordinate.
     * @param lineStyleIdx Line style index.
     * @param fillStyleIdx Fill style index.
     */
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
