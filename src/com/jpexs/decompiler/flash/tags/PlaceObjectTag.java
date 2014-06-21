/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
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
public class PlaceObjectTag extends CharacterIdTag implements PlaceObjectTypeTag {

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
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param length
     * @param pos
     * @throws IOException
     */
    public PlaceObjectTag(SWFInputStream sis, long pos, int length) throws IOException {
        super(sis.getSwf(), ID, "PlaceObject", pos, length);
        characterId = sis.readUI16();
        depth = sis.readUI16();
        matrix = sis.readMatrix();
        if (sis.available() > 0) {
            colorTransform = sis.readCXFORM();
        }
    }

    public PlaceObjectTag(SWF swf, int characterId, int depth, MATRIX matrix, CXFORM colorTransform) {
        super(swf, ID, "PlaceObject", 0, 0);
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
}
