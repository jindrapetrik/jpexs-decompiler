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
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Extends the functionality of the PlaceObject2Tag
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class PlaceObject2Tag extends PlaceObjectTypeTag implements ASMSourceContainer {

    public static final int ID = 26;

    public static final String NAME = "PlaceObject2";

    /**
     * @since SWF 5 Has clip actions (sprite characters only)
     */
    public boolean placeFlagHasClipActions;

    /**
     * Has clip depth
     */
    public boolean placeFlagHasClipDepth;

    /**
     * Has name
     */
    public boolean placeFlagHasName;

    /**
     * Has ratio
     */
    public boolean placeFlagHasRatio;

    /**
     * Has color transform
     */
    public boolean placeFlagHasColorTransform;

    /**
     * Has matrix
     */
    public boolean placeFlagHasMatrix;

    /**
     * Places a character
     */
    public boolean placeFlagHasCharacter;

    /**
     * Defines a character to be moved
     */
    public boolean placeFlagMove;

    /**
     * Depth of character
     */
    @SWFType(BasicType.UI16)
    public int depth;

    /**
     * If PlaceFlagHasCharacter, ID of character to place
     */
    @SWFType(BasicType.UI16)
    @Conditional("placeFlagHasCharacter")
    public int characterId;

    /**
     * If PlaceFlagHasMatrix, Transform matrix data
     */
    @Conditional("placeFlagHasMatrix")
    public MATRIX matrix;

    /**
     * If PlaceFlagHasColorTransform, Color transform data
     */
    @Conditional("placeFlagHasColorTransform")
    public CXFORMWITHALPHA colorTransform;

    /**
     * If PlaceFlagHasRatio, ratio
     */
    @SWFType(BasicType.UI16)
    @Conditional("placeFlagHasRatio")
    public int ratio;

    /**
     * If PlaceFlagHasName, Name of character
     */
    @Conditional("placeFlagHasName")
    public String name;

    /**
     * If PlaceFlagHasClipDepth, Clip depth
     */
    @Conditional("placeFlagHasClipDepth")
    public int clipDepth;

    /**
     * @since SWF 5 If PlaceFlagHasClipActions, Clip Actions Data
     */
    @Conditional("placeFlagHasClipActions")
    public CLIPACTIONS clipActions;

    /**
     * Constructor
     *
     * @param swf
     */
    public PlaceObject2Tag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public PlaceObject2Tag(SWF swf, boolean placeFlagMove, int depth, int characterId, MATRIX matrix, CXFORMWITHALPHA colorTransform, int ratio, String name, int clipDepth, CLIPACTIONS clipActions) {
        super(swf, ID, NAME, null);
        this.placeFlagHasClipActions = clipActions != null;
        this.placeFlagHasClipDepth = clipDepth >= 0;
        this.placeFlagHasName = name != null;
        this.placeFlagHasRatio = ratio >= 0;
        this.placeFlagHasColorTransform = colorTransform != null;
        this.placeFlagHasMatrix = matrix != null;
        this.placeFlagHasCharacter = characterId >= 0;
        this.placeFlagMove = placeFlagMove;
        this.depth = depth;
        this.characterId = characterId;
        this.matrix = matrix;
        this.colorTransform = colorTransform;
        this.ratio = ratio;
        this.name = name;
        this.clipDepth = clipDepth;
        this.clipActions = clipActions;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public PlaceObject2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        placeFlagHasClipActions = sis.readUB(1, "placeFlagHasClipActions") == 1;
        placeFlagHasClipDepth = sis.readUB(1, "placeFlagHasClipDepth") == 1;
        placeFlagHasName = sis.readUB(1, "placeFlagHasName") == 1;
        placeFlagHasRatio = sis.readUB(1, "placeFlagHasRatio") == 1;
        placeFlagHasColorTransform = sis.readUB(1, "placeFlagHasColorTransform") == 1;
        placeFlagHasMatrix = sis.readUB(1, "placeFlagHasMatrix") == 1;
        placeFlagHasCharacter = sis.readUB(1, "placeFlagHasCharacter") == 1;
        placeFlagMove = sis.readUB(1, "placeFlagMove") == 1;

        depth = sis.readUI16("depth");
        if (placeFlagHasCharacter) {
            characterId = sis.readUI16("characterId");
        }
        if (placeFlagHasMatrix) {
            matrix = sis.readMatrix("matrix");
        }
        if (placeFlagHasColorTransform) {
            colorTransform = sis.readCXFORMWITHALPHA("colorTransform");
        }
        if (placeFlagHasRatio) {
            ratio = sis.readUI16("ratio");
        }
        if (placeFlagHasName) {
            name = sis.readString("name");
        }
        if (placeFlagHasClipDepth) {
            clipDepth = sis.readUI16("clipDepth");
        }
        if (placeFlagHasClipActions) {
            clipActions = sis.readCLIPACTIONS(swf, this, "clipActions");
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
        sos.writeUB(1, placeFlagHasClipActions ? 1 : 0);
        sos.writeUB(1, placeFlagHasClipDepth ? 1 : 0);
        sos.writeUB(1, placeFlagHasName ? 1 : 0);
        sos.writeUB(1, placeFlagHasRatio ? 1 : 0);
        sos.writeUB(1, placeFlagHasColorTransform ? 1 : 0);
        sos.writeUB(1, placeFlagHasMatrix ? 1 : 0);
        sos.writeUB(1, placeFlagHasCharacter ? 1 : 0);
        sos.writeUB(1, placeFlagMove ? 1 : 0);
        sos.writeUI16(depth);
        if (placeFlagHasCharacter) {
            sos.writeUI16(characterId);
        }
        if (placeFlagHasMatrix) {
            sos.writeMatrix(matrix);
        }
        if (placeFlagHasColorTransform) {
            sos.writeCXFORMWITHALPHA(colorTransform);
        }
        if (placeFlagHasRatio) {
            sos.writeUI16(ratio);
        }
        if (placeFlagHasName) {
            sos.writeString(name);
        }
        if (placeFlagHasClipDepth) {
            sos.writeUI16(clipDepth);
        }
        if (placeFlagHasClipActions) {
            sos.writeCLIPACTIONS(clipActions);
        }
    }

    @Override
    public int getPlaceObjectNum() {
        return 2;
    }

    @Override
    public int getClipDepth() {
        if (placeFlagHasClipDepth) {
            return clipDepth;
        }
        return -1;
    }

    @Override
    public List<FILTER> getFilters() {
        return null;
    }

    /**
     * Returns all sub-items
     *
     * @return List of sub-items
     */
    @Override
    public List<CLIPACTIONRECORD> getSubItems() {
        if (placeFlagHasClipActions) {
            return clipActions.clipActionRecords;
        }
        return new ArrayList<>();
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        if (placeFlagHasCharacter) {
            needed.add(characterId);
        }
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
        if (placeFlagHasCharacter) {
            return characterId;
        } else {
            return -1;
        }
    }

    @Override
    public void setCharacterId(int characterId) {
        if (characterId >= 0) {
            placeFlagHasCharacter = true;
            this.characterId = characterId;
        } else {
            placeFlagHasCharacter = false;
            this.characterId = -1;
        }
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public MATRIX getMatrix() {
        if (placeFlagHasMatrix) {
            return matrix;
        } else {
            return null;
        }
    }

    @Override
    public void setMatrix(MATRIX matrix) {
        this.matrix = matrix;
    }

    @Override
    public String getInstanceName() {
        if (placeFlagHasName) {
            return name;
        }
        return null;
    }

    @Override
    public ColorTransform getColorTransform() {
        if (placeFlagHasColorTransform) {
            return colorTransform;
        } else {
            return null;
        }
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
        return placeFlagMove;
    }

    @Override
    public int getRatio() {
        if (!placeFlagHasRatio) {
            return -1;
        }
        return ratio;
    }

    @Override
    public void setInstanceName(String name) {
        placeFlagHasName = true;
        this.name = name;
    }

    @Override
    public void setClassName(String className) {
        //not supported
    }

    @Override
    public String getName() {
        if (placeFlagHasName) {
            return super.getName() + " (" + name + ")";
        } else {
            return super.getName();
        }
    }

    @Override
    public CLIPACTIONS getClipActions() {
        if (placeFlagHasClipActions) {
            return clipActions;
        } else {
            return null;
        }
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
