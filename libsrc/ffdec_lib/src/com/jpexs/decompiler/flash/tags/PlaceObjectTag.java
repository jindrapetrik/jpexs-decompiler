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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Optional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Adds character to the display list
 *
 * @author JPEXS
 */
@SWFVersion(from = 1)
public class PlaceObjectTag extends PlaceObjectTypeTag {

    public static final int ID = 4;

    public static final String NAME = "PlaceObject";

    /**
     * Constructor
     *
     * @param swf
     */
    public PlaceObjectTag(SWF swf) {
        super(swf, ID, NAME, null);
        matrix = new MATRIX();
    }

    public PlaceObjectTag(SWF swf, int characterId, int depth, MATRIX matrix, CXFORM colorTransform) {
        super(swf, ID, NAME, null);
        this.characterId = characterId;
        this.depth = depth;
        this.matrix = matrix;
        this.colorTransform = colorTransform;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public PlaceObjectTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
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

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterId);
        sos.writeUI16(depth);
        sos.writeMatrix(matrix);
        if (colorTransform != null) {
            sos.writeCXFORM(colorTransform);
        }
    }

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

    @Override
    public int getPlaceObjectNum() {
        return 1;
    }

    @Override
    public int getClipDepth() {
        return -1;
    }

    @Override
    public List<FILTER> getFilters() {
        return null;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        needed.add(characterId);
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        return false;
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
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
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
    public void setMatrix(MATRIX matrix) {
        this.matrix = matrix;
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
        try {
            super.writeTag(sos);
        } finally {
            setModified(mod);
            matrix = old;
        }
    }

    @Override
    public Amf3Value getAmfData() {
        return null;
    }

    @Override
    public Integer getBitmapCache() {
        return null;
    }

    @Override
    public Integer getVisible() {
        return null;
    }
}
