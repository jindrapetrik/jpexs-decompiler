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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 * DefineShape4 tag - defines shape. Extends functionality of DefineShape3.
 * Adds LINESTYLE2 (joins, caps, scaling), filling stroke, edge bounds,
 * focal gradient, spread mode, interpolation mode, numgradients &gt; 8.
 * 
 * @author JPEXS
 */
@SWFVersion(from = 8)
public class DefineShape4Tag extends ShapeTag {

    public static final int ID = 83;

    public static final String NAME = "DefineShape4";

    public RECT edgeBounds;

    @Reserved
    @SWFType(value = BasicType.UB, count = 5)
    public int reserved;

    public boolean usesFillWindingRule;

    public boolean usesNonScalingStrokes;

    public boolean usesScalingStrokes;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineShape4Tag(SWF swf) {
        super(swf, ID, NAME, null);
        shapeId = swf.getNextCharacterId();
        shapeBounds = new RECT();
        edgeBounds = new RECT();
        shapes = SHAPEWITHSTYLE.createEmpty(4);
    }

    public DefineShape4Tag(SWFInputStream sis, ByteArrayRange data, boolean lazy) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, lazy);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        shapeId = sis.readUI16("shapeId");
        shapeBounds = sis.readRECT("shapeBounds");
        edgeBounds = sis.readRECT("edgeBounds");
        reserved = (int) sis.readUB(5, "reserved");
        usesFillWindingRule = sis.readUB(1, "usesFillWindingRule") == 1;
        usesNonScalingStrokes = sis.readUB(1, "usesNonScalingStrokes") == 1;
        usesScalingStrokes = sis.readUB(1, "usesScalingStrokes") == 1;
        if (!lazy) {
            shapes = sis.readSHAPEWITHSTYLE(getShapeNum(), false, "shapes");
        } else {
            shapeData = new ByteArrayRange(data.getArray(), (int) sis.getPos(), sis.available());
            sis.skipBytes(sis.available());
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(shapeId);
        sos.writeRECT(shapeBounds);
        sos.writeRECT(edgeBounds);
        sos.writeUB(5, reserved);
        sos.writeUB(1, usesFillWindingRule ? 1 : 0);
        sos.writeUB(1, usesNonScalingStrokes ? 1 : 0);
        sos.writeUB(1, usesScalingStrokes ? 1 : 0);
        sos.writeSHAPEWITHSTYLE(getShapes(), getShapeNum());
    }

    @Override
    public int getShapeNum() {
        return 4;
    }

    @Override
    public void updateBounds() {
        super.updateBounds();
        updateEdgeBounds();
    }
    
    public void updateEdgeBounds() {
        edgeBounds = SHAPERECORD.getBounds(shapes.shapeRecords, null, 4, true);
    }

    @Override
    public int getWindingRule() {
        if (usesFillWindingRule) {
            return WIND_NONZERO;
        }
        return WIND_EVEN_ODD;
    }
}
