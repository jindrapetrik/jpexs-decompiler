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
 * Edge interface.
 *
 * @author JPEXS
 */
public interface IEdge {

    /**
     * Returns the x coordinate of the start point of the edge.
     * @return X coordinate of the start point of the edge
     */
    public int getFromX();

    /**
     * Returns the y coordinate of the start point of the edge.
     * @return Y coordinate of the start point of the edge
     */
    public int getFromY();

    /**
     * Returns the x coordinate of the end point of the edge.
     * @return X coordinate of the end point of the edge
     */
    public int getToX();

    /**
     * Returns the y coordinate of the end point of the edge.
     * @return Y coordinate of the end point of the edge
     */
    public int getToY();

    /**
     * Returns the line style index of the edge.
     * @return Line style index of the edge
     */
    public int getLineStyleIdx();

    /**
     * Returns the fill style index of the edge.
     * @return Fill style index of the edge
     */
    public int getFillStyleIdx();

    /**
     * Gets reverse edge with new fill style.
     * @param newFillStyleIdx New fill style index
     * @return Reverse edge with new fill style
     */
    public IEdge reverseWithNewFillStyle(int newFillStyleIdx);

    /**
     * Gets reverse edge.
     * @return Reverse edge
     */
    public IEdge reverse();

    /**
     * Returns the same edge with new fill style.
     * @param newFillStyleIdx New fill style index
     * @return Same edge with new fill style
     */
    public IEdge sameWithNewFillStyle(int newFillStyleIdx);
}
