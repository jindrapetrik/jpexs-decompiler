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

import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GlyphType implements Serializable {

    public int[] boundingBox;

    public ContourType[] contours;

    public GlyphType(List<SHAPERECORD> records) {
        RECT bounds = SHAPERECORD.getBounds(records);
        boundingBox = new int[4];
        boundingBox[0] = bounds.Xmin;
        boundingBox[1] = bounds.Ymin;
        boundingBox[2] = bounds.Xmax;
        boundingBox[3] = bounds.Ymax;
        List<SHAPERECORD> cont = new ArrayList<>();
        List<ContourType> contoursList = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            SHAPERECORD r = records.get(i);
            if ((r instanceof StyleChangeRecord) && ((StyleChangeRecord) r).stateMoveTo) {
                if (!cont.isEmpty()) {
                    contoursList.add(new ContourType(cont));
                    cont.clear();
                }
            }
            cont.add(r);
        }
        if (!cont.isEmpty()) {
            contoursList.add(new ContourType(cont));
            cont.clear();
        }
        contours = contoursList.toArray(new ContourType[contoursList.size()]);
    }

    public GlyphType(GFxInputStream sis, long fontOffset) throws IOException {
        boundingBox = new int[4];
        for (int i = 0; i < 4; i++) {
            boundingBox[i] = sis.readSI15("boundingBox");
        }
        int numContours = sis.readUI15("numContours");
        contours = new ContourType[numContours];
        for (int i = 0; i < numContours; i++) {
            sis.newDumpLevel("contourType", "ContourType");
            contours[i] = new ContourType(sis, fontOffset);
            sis.endDumpLevel();
        }
    }

    public SHAPE toSHAPE() {
        SHAPE shp = new SHAPE();
        shp.numFillBits = 1;
        shp.numLineBits = 0;
        List<SHAPERECORD> recs = new ArrayList<>();
        StyleChangeRecord scr = new StyleChangeRecord();
        scr.fillStyle0 = 0;
        scr.fillStyle1 = 1;
        scr.stateFillStyle1 = true;
        scr.stateFillStyle0 = false;
        recs.add(scr);
        for (ContourType cnt : contours) {
            recs.addAll(cnt.toSHAPERECORDS());
        }
        recs.add(new EndShapeRecord());
        shp.shapeRecords = recs;
        return shp;
    }

    public void write(GFxOutputStream sos) throws IOException {
        for (int b : boundingBox) {
            sos.writeSI15(b);
        }
        sos.writeUI15(contours.length);
        for (ContourType cnt : contours) {
            cnt.write(sos);
        }
    }
}
