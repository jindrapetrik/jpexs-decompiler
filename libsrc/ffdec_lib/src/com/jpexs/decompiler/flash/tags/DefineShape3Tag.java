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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class DefineShape3Tag extends ShapeTag {

    public static final int ID = 32;

    public static final String NAME = "DefineShape3";

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineShape3Tag(SWF swf) {
        super(swf, ID, NAME, null);
        shapeId = swf.getNextCharacterId();
        shapeBounds = new RECT();
        shapes = SHAPEWITHSTYLE.createEmpty(3);
    }

    public DefineShape3Tag(SWFInputStream sis, ByteArrayRange data, boolean lazy) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, lazy);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        shapeId = sis.readUI16("shapeId");
        shapeBounds = sis.readRECT("shapeBounds");
        if (!lazy) {
            shapes = sis.readSHAPEWITHSTYLE(getShapeNum(), false, "shapes");
        } else {
            shapeData = new ByteArrayRange(data.getArray(), (int) sis.getPos(), sis.available());
        }
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, getVersion());
        try {
            sos.writeUI16(shapeId);
            sos.writeRECT(shapeBounds);
            sos.writeSHAPEWITHSTYLE(getShapes(), getShapeNum());
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    @Override
    public int getShapeNum() {
        return 3;
    }
}
