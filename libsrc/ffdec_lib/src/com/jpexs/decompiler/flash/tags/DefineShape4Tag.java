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
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefineShape4Tag extends ShapeTag {

    @SWFType(BasicType.UI16)
    public int shapeId;

    public RECT shapeBounds;

    public RECT edgeBounds;

    @Reserved
    @SWFType(value = BasicType.UB, count = 5)
    public int reserved;

    public boolean usesFillWindingRule;

    public boolean usesNonScalingStrokes;

    public boolean usesScalingStrokes;

    public SHAPEWITHSTYLE shapes;

    public static final int ID = 83;

    private ByteArrayRange shapeData;

    @Override
    public int getShapeNum() {
        return 4;
    }

    @Override
    public SHAPEWITHSTYLE getShapes() {
        if (shapes == null && shapeData != null) {
            try {
                SWFInputStream sis = new SWFInputStream(swf, shapeData.getArray(), 0, shapeData.getPos() + shapeData.getLength());
                sis.seek(shapeData.getPos());
                shapes = sis.readSHAPEWITHSTYLE(4, false, "shapes");
                shapeData = null; // not needed anymore, give it to GC
            } catch (IOException ex) {
                Logger.getLogger(DefineShape4Tag.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return shapes;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        getShapes().getNeededCharacters(needed);
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = getShapes().removeCharacter(characterId);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public int getCharacterId() {
        return shapeId;
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return shapeBounds;
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineShape4Tag(SWF swf) {
        super(swf, ID, "DefineShape4", null);
        shapeId = swf.getNextCharacterId();
        shapeBounds = new RECT();
        edgeBounds = new RECT();
        shapes = SHAPEWITHSTYLE.createEmpty(4);
    }

    public DefineShape4Tag(SWFInputStream sis, ByteArrayRange data, boolean lazy) throws IOException {
        super(sis.getSwf(), ID, "DefineShape4", data);
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
            shapes = sis.readSHAPEWITHSTYLE(4, false, "shapes");
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
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(shapeId);
            sos.writeRECT(shapeBounds);
            sos.writeRECT(edgeBounds);
            sos.writeUB(5, reserved);
            sos.writeUB(1, usesFillWindingRule ? 1 : 0);
            sos.writeUB(1, usesNonScalingStrokes ? 1 : 0);
            sos.writeUB(1, usesScalingStrokes ? 1 : 0);
            sos.writeSHAPEWITHSTYLE(getShapes(), 4);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }
}
