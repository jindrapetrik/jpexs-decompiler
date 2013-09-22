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
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GlyphType {

    public int[] boundingBox;
    public ContourType[] contours;

    public GlyphType(GFxInputStream sis) throws IOException {
        boundingBox = new int[4];
        for (int i = 0; i < 4; i++) {
            boundingBox[i] = sis.readSI15();
        }
        int numContours = sis.readUI15();
        contours = new ContourType[numContours];
        for (int i = 0; i < numContours; i++) {
            contours[i] = new ContourType(sis);
        }
    }

    public SHAPE toSHAPE() {
        SHAPE shp = new SHAPE();
        shp.numFillBits = 1;
        shp.numLineBits = 1;
        List<SHAPERECORD> recs = new ArrayList<>();
        StyleChangeRecord scr = new StyleChangeRecord();
        scr.fillStyle0 = 1;
        scr.stateFillStyle0 = true;
        recs.add(scr);
        for (ContourType cnt : contours) {
            recs.addAll(cnt.toSHAPERECORDS());
        }
        shp.shapeRecords = recs;
        return shp;
    }

    public void write(SWFOutputStream sos) throws IOException {
    }
}
