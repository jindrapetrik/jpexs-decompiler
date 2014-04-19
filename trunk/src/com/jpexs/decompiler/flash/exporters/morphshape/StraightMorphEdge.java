/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.exporters.morphshape;

import com.jpexs.decompiler.flash.exporters.PointInt;

/**
 *
 * @author JPEXS
 */
public class StraightMorphEdge implements IMorphEdge {

    protected final PointInt from;
    protected final PointInt to;
    protected final PointInt fromEnd;
    protected final PointInt toEnd;
    protected final int lineStyleIdx;
    private final int fillStyleIdx;

    StraightMorphEdge(PointInt from, PointInt to, PointInt fromEnd, PointInt toEnd, int lineStyleIdx, int fillStyleIdx) {
        this.from = from;
        this.to = to;
        this.fromEnd = fromEnd;
        this.toEnd = toEnd;
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
    public PointInt getFromEnd() {
        return fromEnd;
    }

    @Override
    public PointInt getToEnd() {
        return toEnd;
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
        return new StraightMorphEdge(to, from, toEnd, fromEnd, lineStyleIdx, newFillStyleIdx);
    }
}
