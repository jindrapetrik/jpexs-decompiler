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
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Optional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

/**
 * Adds character to the display list
 *
 * @author JPEXS
 */
public class PlaceObjectTag extends PlaceObjectTypeTag {

    /**
     * ID of character to place
     */
    @SWFType(BasicType.UI16)
    public int characterId;

    /**
     * Depth of character
     */
    @SWFType(BasicType.UI16)
    public int depth;

    /**
     * Transform matrix data
     */
    public MATRIX matrix;

    /**
     * Color transform data
     */
    @Optional
    public CXFORM colorTransform;

    public static final int ID = 4;

    @Override
    public List<FILTER> getFilters() {
        return null;
    }

    @Override
    public int getClipDepth() {
        return -1;
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
            sos.writeUI16(characterId);
            sos.writeUI16(depth);
            sos.writeMatrix(matrix);
            if (colorTransform != null) {
                sos.writeCXFORM(colorTransform);
            }
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public PlaceObjectTag(SWF swf) {
        super(swf, ID, "PlaceObject", null);
        matrix = new MATRIX();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public PlaceObjectTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "PlaceObject", data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterId = sis.readUI16("characterId");
        depth = sis.readUI16("depth");
        matrix = sis.readMatrix("matrix");
        if (sis.available() > 0) {
            colorTransform = sis.readCXFORM("colorTransform");
        }
    }

    public PlaceObjectTag(SWF swf, int characterId, int depth, MATRIX matrix, CXFORM colorTransform) {
        super(swf, ID, "PlaceObject", null);
        this.characterId = characterId;
        this.depth = depth;
        this.matrix = matrix;
        this.colorTransform = colorTransform;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        needed.add(characterId);
    }

    @Override
    public boolean removeCharacter(int characterId) {
        // the place object tag will be removed
        return false;
    }

    @Override
    public int getCharacterId() {
        return characterId;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public MATRIX getMatrix() {
        return matrix;
    }

    @Override
    public String getInstanceName() {
        return null;
    }

    @Override
    public ColorTransform getColorTransform() {
        return colorTransform;
    }

    @Override
    public int getBlendMode() {
        return 0;
    }

    @Override
    public boolean cacheAsBitmap() {
        return false;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public RGBA getBackgroundColor() {
        return null;
    }

    @Override
    public boolean flagMove() {
        return false;
    }

    @Override
    public int getRatio() {
        return -1;
    }

    @Override
    public void setInstanceName(String name) {
        //not supported
    }

    @Override
    public void setClassName(String className) {
        //not supported
    }

    @Override
    public CLIPACTIONS getClipActions() {
        return null;
    }

    @Override
    public void writeTagWithMatrix(SWFOutputStream sos, MATRIX m) throws IOException {
        MATRIX old = matrix;
        matrix = m;
        boolean mod = isModified();
        setModified(true);
        super.writeTag(sos);
        setModified(mod);
        matrix = old;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }
}
