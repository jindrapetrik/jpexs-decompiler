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
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefineShapeTag extends ShapeTag {

    @SWFType(BasicType.UI16)
    public int shapeId;

    public RECT shapeBounds;

    public SHAPEWITHSTYLE shapes;

    public static final int ID = 2;

    private ByteArrayRange shapeData;

    @Override
    public int getShapeNum() {
        return 1;
    }

    @Override
    public SHAPEWITHSTYLE getShapes() {
        if (shapes == null && shapeData != null) {
            try {
                SWFInputStream sis = new SWFInputStream(swf, shapeData.getArray(), 0, shapeData.getPos() + shapeData.getLength());
                sis.seek(shapeData.getPos());
                shapes = sis.readSHAPEWITHSTYLE(1, false, "shapes");
                shapeData = null; // not needed anymore, give it to GC
            } catch (IOException ex) {
                Logger.getLogger(DefineShapeTag.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return shapes;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        getShapes().getNeededCharacters(needed);
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = getShapes().replaceCharacter(oldCharacterId, newCharacterId);
        if (modified) {
            setModified(true);
        }
        return modified;
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
    public RECT getRect(Set<BoundedTag> added) {
        return shapeBounds;
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineShapeTag(SWF swf) {
        super(swf, ID, "DefineShape", null);
        shapeId = swf.getNextCharacterId();
        shapeBounds = new RECT();
        shapes = SHAPEWITHSTYLE.createEmpty(1);
    }

    public DefineShapeTag(SWFInputStream sis, ByteArrayRange data, boolean lazy) throws IOException {
        super(sis.getSwf(), ID, "DefineShape", data);
        readData(sis, data, 0, false, false, lazy);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        shapeId = sis.readUI16("shapeId");
        shapeBounds = sis.readRECT("shapeBounds");
        if (!lazy) {
            shapes = sis.readSHAPEWITHSTYLE(1, false, "shapes");
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
            sos.writeSHAPEWITHSTYLE(getShapes(), 1);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    @Override
    public int getCharacterId() {
        return shapeId;
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.shapeId = characterId;
    }
}
