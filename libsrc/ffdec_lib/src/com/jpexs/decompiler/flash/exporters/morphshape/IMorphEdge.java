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
 * Interface for morph edges.
 *
 * @author JPEXS
 */
public interface IMorphEdge {

    /**
     * Gets the start X coordinate of the edge.
     * @return Start X coordinate
     */
    public int getFromX();

    /**
     * Gets the start Y coordinate of the edge.
     * @return Start Y coordinate
     */
    public int getFromY();

    /**
     * Gets the end X coordinate of the edge.
     * @return End X coordinate
     */
    public int getToX();

    /**
     * Gets the end Y coordinate of the edge.
     * @return End Y coordinate
     */
    public int getToY();

    /**
     * Gets the start X coordinate of the edge in the end state.
     * @return Start X coordinate in the end state
     */
    public int getFromEndX();

    /**
     * Gets the start Y coordinate of the edge in the end state.
     * @return Start Y coordinate in the end state
     */
    public int getFromEndY();

    /**
     * Gets the end X coordinate of the edge in the end state.
     * @return End X coordinate in the end state
     */
    public int getToEndX();

    /**
     * Gets the end Y coordinate of the edge in the end state.
     * @return End Y coordinate in the end state
     */
    public int getToEndY();

    /**
     * Gets the line style index of the edge.
     * @return Line style index
     */
    public int getLineStyleIdx();

    /**
     * Gets the fill style index of the edge.
     * @return Fill style index
     */
    public int getFillStyleIdx();

    /**
     * Gets reverse edge with new fill style.
     * @param newFillStyleIdx New fill style index
     * @return Reversed edge with new fill style
     */
    public IMorphEdge reverseWithNewFillStyle(int newFillStyleIdx);
}
