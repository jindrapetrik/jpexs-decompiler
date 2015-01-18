/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.exporters.commonshape.PointInt;

/**
 *
 * @author JPEXS
 */
public class StraightEdge implements IEdge {

    protected final PointInt from;

    protected final PointInt to;

    protected final int lineStyleIdx;

    private final int fillStyleIdx;

    StraightEdge(PointInt from, PointInt to, int lineStyleIdx, int fillStyleIdx) {
        this.from = from;
        this.to = to;
        this.lineStyleIdx = lineStyleIdx;
        this.fillStyleIdx = fillStyleIdx;
    }

    @Override
    public PointInt getFrom() {
        return from;
    }

    @Override
    public PointInt getTo() {
        return to;
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
        return new StraightEdge(to, from, lineStyleIdx, newFillStyleIdx);
    }
}
