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
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
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
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Extends the functionality of the PlaceObject2Tag
 *
 * @author JPEXS
 */
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
    @HideInRawEdit //TODO: make editable
    public CLIPACTIONS clipActions;

    /**
     * Constructor
     *
     * @param swf
     */
    public PlaceObject2Tag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public PlaceObject2Tag(SWF swf, boolean placeFlagHasClipActions, boolean placeFlagHasClipDepth, boolean placeFlagHasName, boolean placeFlagHasRatio, boolean placeFlagHasColorTransform, boolean placeFlagHasMatrix, boolean placeFlagHasCharacter, boolean placeFlagMove, int depth, int characterId, MATRIX matrix, CXFORMWITHALPHA colorTransform, int ratio, String name, int clipDepth, CLIPACTIONS clipActions) {
        super(swf, ID, NAME, null);
        this.placeFlagHasClipActions = placeFlagHasClipActions;
        this.placeFlagHasClipDepth = placeFlagHasClipDepth;
        this.placeFlagHasName = placeFlagHasName;
        this.placeFlagHasRatio = placeFlagHasRatio;
        this.placeFlagHasColorTransform = placeFlagHasColorTransform;
        this.placeFlagHasMatrix = placeFlagHasMatrix;
        this.placeFlagHasCharacter = placeFlagHasCharacter;
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
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Configuration.debugCopy.get()) {
            os = new CopyOutputStream(os, new ByteArrayInputStream(getOriginalData()));
        }
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
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
            sos.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
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
        placeFlagHasCharacter = true;
        this.characterId = characterId;
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
        super.writeTag(sos);
        setModified(mod);
        matrix = old;
    }
}
