/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefineShape2Tag extends ShapeTag {

    @SWFType(BasicType.UI16)
    public int shapeId;
    private final RECT shapeBounds;
    public SHAPEWITHSTYLE shapes;
    public static final int ID = 22;

    private ByteArrayRange shapeData;

    @Override
    public int getShapeNum() {
        return 2;
    }

    @Override
    public SHAPEWITHSTYLE getShapes() {
        if (shapes == null && shapeData != null) {
            try {
                SWFInputStream sis = new SWFInputStream(swf, shapeData.getArray(), 0, shapeData.getPos() + shapeData.getLength());
                sis.seek(shapeData.getPos());
                shapes = sis.readSHAPEWITHSTYLE(2, false, "shapes");
                shapeData = null; // not needed anymore, give it to GC
            } catch (IOException ex) {
                Logger.getLogger(DefineShape2Tag.class.getName()).log(Level.SEVERE, null, ex);
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
    public DefineShape2Tag(SWF swf) {
        super(swf, ID, "DefineShape2", null);
        shapeId = swf.getNextCharacterId();
        shapeBounds = new RECT();
        shapes = SHAPEWITHSTYLE.createEmpty(2);
    }

    public DefineShape2Tag(SWFInputStream sis, ByteArrayRange data, boolean lazy) throws IOException {
        super(sis.getSwf(), ID, "DefineShape2", data);
        shapeId = sis.readUI16("shapeId");
        shapeBounds = sis.readRECT("shapeBounds");
        if (!lazy) {
            shapes = sis.readSHAPEWITHSTYLE(2, false, "shapes");
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
            sos.writeSHAPEWITHSTYLE(getShapes(), 2);
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
