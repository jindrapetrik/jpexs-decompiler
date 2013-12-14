/*
 *  Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
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

    public ContourType(List<SHAPERECORD> records) {
        int i = 0;
        int divider = 20;
        for (; i < records.size(); i++) {
            if (records.get(i) instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) records.get(i);
                if (scr.stateMoveTo) {
                    moveToX = scr.moveDeltaX / divider;
                    moveToY = scr.moveDeltaY / divider;
                    break;
                }
            }
        }
        List<EdgeType> edgesList = new ArrayList<>();
        for (; i < records.size(); i++) {
            SHAPERECORD rec = records.get(i);
            if (rec instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                if (ser.generalLineFlag) {
                    edgesList.add(new EdgeType(ser.deltaX / divider, ser.deltaY / divider));
                } else if (ser.vertLineFlag) {
                    edgesList.add(new EdgeType(true, ser.deltaY / divider));
                } else {
                    edgesList.add(new EdgeType(false, ser.deltaX / divider));
                }
            } else if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                edgesList.add(new EdgeType(cer.controlDeltaX / divider, cer.controlDeltaY / divider, cer.anchorDeltaX / divider, cer.anchorDeltaY / divider));
            }
        }
        edges = edgesList.toArray(new EdgeType[edgesList.size()]);
    }

    public ContourType(GFxInputStream sis) throws IOException {
        moveToX = sis.readSI15();
        moveToY = sis.readSI15();
        long numEdgesRef = sis.readUI30();
        isReference = (numEdgesRef & 1) == 1;
        numEdgesRef >>= 1;
        long oldPos = sis.getPos();
        if (isReference) {
            sis.setPos(numEdgesRef);
            numEdgesRef = sis.readUI30();
            numEdgesRef >>= 1;
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
        int multiplier = 1;
        List<SHAPERECORD> recs = new ArrayList<>();
        StyleChangeRecord src = new StyleChangeRecord();
        src.stateMoveTo = true;
        src.moveDeltaX = moveToX * multiplier;
        src.moveDeltaY = moveToY * multiplier;
        src.calculateBits();
        recs.add(src);
        for (EdgeType e : edges) {
            recs.add(e.toSHAPERECORD());
        }
        int x = src.moveDeltaX;
        int y = src.moveDeltaY;
        for (SHAPERECORD rec : recs) {
            x = rec.changeX(x);
            y = rec.changeY(y);
        }
        StraightEdgeRecord closeSer = new StraightEdgeRecord();
        closeSer.generalLineFlag = true;
        closeSer.deltaX = (src.moveDeltaX - x);
        closeSer.deltaY = (src.moveDeltaY - y);
        closeSer.calculateBits();
        recs.add(closeSer);

        return recs;
    }

    public void write(GFxOutputStream sos) throws IOException {
        sos.writeSI15(moveToX);
        sos.writeSI15(moveToY);
        sos.writeUI30(edges.length << 1);
        for (int i = 0; i < edges.length; i++) {
            edges[i].write(sos);
        }
    }
}
