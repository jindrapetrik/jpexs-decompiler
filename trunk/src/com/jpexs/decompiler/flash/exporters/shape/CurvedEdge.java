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
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.exporters.commonshape.PointInt;

/**
 *
 * @author JPEXS
 */
public class CurvedEdge extends StraightEdge implements IEdge {

    private final PointInt control;

    CurvedEdge(PointInt from, PointInt control, PointInt to, int lineStyleIdx, int fillStyleIdx) {
        super(from, to, lineStyleIdx, fillStyleIdx);
        this.control = control;
    }

    public PointInt getControl() {
        return control;
    }

    @Override
    public IEdge reverseWithNewFillStyle(int newFillStyleIdx) {
        return new CurvedEdge(to, control, from, lineStyleIdx, newFillStyleIdx);
    }
}
