/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ContourType {

    public int moveToX;
    public int moveToY;
    public EdgeType[] edges;
    public boolean isReference;
    public long reference;

    public ContourType(GFxInputStream sis) throws IOException {
        moveToX = sis.readSI15();
        moveToY = sis.readSI15();
        long numEdgesRef = sis.readUI30();
        isReference = (numEdgesRef & 1) == 1;
        numEdgesRef = numEdgesRef >> 1;
        long oldPos = sis.getPos();
        if (isReference) {
            sis.setPos(numEdgesRef);
            numEdgesRef = sis.readUI30();
            numEdgesRef = numEdgesRef >> 1;
        }

        edges = new EdgeType[(int) numEdgesRef];
        for (int i = 0; i < edges.length; i++) {
            edges[i] = new EdgeType(sis);
        }
        if (isReference) {
            sis.setPos(oldPos);
        }

    }

    public List<SHAPERECORD> toSHAPERECORDS() {
        int multiplier = 20;
        List<SHAPERECORD> recs = new ArrayList<>();
        StyleChangeRecord src = new StyleChangeRecord();
        src.stateMoveTo = true;
        src.moveDeltaX = moveToX * multiplier;
        src.moveDeltaY = moveToY * multiplier;
        recs.add(src);
        for (EdgeType e : edges) {
            recs.add(e.toSHAPERECORD());
        }
        return recs;
    }

    public void write(SWFOutputStream sos) throws IOException {
    }
}
