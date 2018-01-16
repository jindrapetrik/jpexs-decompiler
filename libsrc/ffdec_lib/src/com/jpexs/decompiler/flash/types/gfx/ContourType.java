/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ContourType implements Serializable {

    public int moveToX;

    public int moveToY;

    public EdgeType[] edges;

    public boolean isReference;

    public long reference;

    public ContourType(List<SHAPERECORD> records) {
        int i = 0;
        int divider = 1;
        for (; i < records.size(); i++) {
            SHAPERECORD rec = records.get(i);
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
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

    public ContourType(GFxInputStream sis, long fontOffset) throws IOException {
        moveToX = sis.readSI15("moveToX");
        moveToY = sis.readSI15("moveToY");
        long numEdgesRef = sis.readUI30("numEdgesRef");
        isReference = (numEdgesRef & 1) == 1;
        numEdgesRef >>= 1;
        long oldPos = sis.getPos();
        int numEdges;
        if (isReference) {
            long referencePos = numEdgesRef;
            sis.setPos(fontOffset + referencePos);
            numEdges = (int) (sis.readUI30("numEdges") >> 1);
        } else {
            numEdges = (int) numEdgesRef;
        }

        edges = new EdgeType[(int) numEdges];
        for (int i = 0; i < edges.length; i++) {
            sis.newDumpLevel("edgeType", "EdgeType");
            edges[i] = new EdgeType(sis);
            sis.endDumpLevel();
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
