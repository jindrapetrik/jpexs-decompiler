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
package com.jpexs.decompiler.flash.exporters;

/**
 *
 * @author JPEXS
 */
public class CurvedMorphEdge extends StraightMorphEdge implements IMorphEdge {

    private final PointInt control;
    private final PointInt controlEnd;

    CurvedMorphEdge(PointInt from, PointInt control, PointInt to, 
            PointInt fromEnd, PointInt controlEnd, PointInt toEnd, int lineStyleIdx, int fillStyleIdx) {
        super(from, to, fromEnd, toEnd, lineStyleIdx, fillStyleIdx);
        this.control = control;
        this.controlEnd = controlEnd;
    }

    public PointInt getControl() {
        return control;
    }

    public PointInt getControlEnd() {
        return controlEnd;
    }

    @Override
    public IMorphEdge reverseWithNewFillStyle(int newFillStyleIdx) {
        return new CurvedMorphEdge(to, control, from, toEnd, controlEnd, fromEnd, lineStyleIdx, newFillStyleIdx);
    }
}
