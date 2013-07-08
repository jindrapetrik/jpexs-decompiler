/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extends the functionality of the PlaceObject2Tag
 *
 * @author JPEXS
 */
public class PlaceObject2Tag extends Tag implements Container, PlaceObjectTypeTag {

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
    public int depth;
    /**
     * If PlaceFlagHasCharacter, ID of character to place
     */
    public int characterId;
    /**
     * If PlaceFlagHasMatrix, Transform matrix data
     */
    public MATRIX matrix;
    /**
     * If PlaceFlagHasColorTransform, Color transform data
     */
    public CXFORMWITHALPHA colorTransform;
    /**
     * If PlaceFlagHasRatio, ratio
     */
    public int ratio;
    /**
     * If PlaceFlagHasName, Name of character
     */
    public String name;
    /**
     * If PlaceFlagHasClipDepth, Clip depth
     */
    public int clipDepth;
    /**
     * @since SWF 5 If PlaceFlagHasClipActions, Clip Actions Data
     */
    public CLIPACTIONS clipActions;
    public static final int ID = 26;

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
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        if (Configuration.DISABLE_DANGEROUS) {
            return super.getData(version);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Configuration.DEBUG_COPY) {
            os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
        }
        SWFOutputStream sos = new SWFOutputStream(os, version);
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
        }
        return baos.toByteArray();
    }

    public PlaceObject2Tag(boolean placeFlagHasClipActions, boolean placeFlagHasClipDepth, boolean placeFlagHasName, boolean placeFlagHasRatio, boolean placeFlagHasColorTransform, boolean placeFlagHasMatrix, boolean placeFlagHasCharacter, boolean placeFlagMove, int depth, int characterId, MATRIX matrix, CXFORMWITHALPHA colorTransform, int ratio, String name, int clipDepth, CLIPACTIONS clipActions) {
        super(ID, "PlaceObject2", new byte[0], 0);
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
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public PlaceObject2Tag(byte data[], int version, long pos) throws IOException {
        super(26, "PlaceObject2", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        placeFlagHasClipActions = sis.readUB(1) == 1;
        placeFlagHasClipDepth = sis.readUB(1) == 1;
        placeFlagHasName = sis.readUB(1) == 1;
        placeFlagHasRatio = sis.readUB(1) == 1;
        placeFlagHasColorTransform = sis.readUB(1) == 1;
        placeFlagHasMatrix = sis.readUB(1) == 1;
        placeFlagHasCharacter = sis.readUB(1) == 1;
        placeFlagMove = sis.readUB(1) == 1;
        depth = sis.readUI16();
        if (placeFlagHasCharacter) {
            characterId = sis.readUI16();
        }
        if (placeFlagHasMatrix) {
            matrix = sis.readMatrix();
        }
        if (placeFlagHasColorTransform) {
            colorTransform = sis.readCXFORMWITHALPHA();
        }
        if (placeFlagHasRatio) {
            ratio = sis.readUI16();
        }
        if (placeFlagHasName) {
            name = sis.readString();
        }
        if (placeFlagHasClipDepth) {
            clipDepth = sis.readUI16();
        }
        if (placeFlagHasClipActions) {
            clipActions = sis.readCLIPACTIONS();
        }
    }

    /**
     * Returns all sub-items
     *
     * @return List of sub-items
     */
    @Override
    public List<Object> getSubItems() {
        List<Object> ret = new ArrayList<>();
        if (placeFlagHasClipActions) {
            ret.addAll(clipActions.clipActionRecords);
        }
        return ret;
    }

    /**
     * Returns number of sub-items
     *
     * @return Number of sub-items
     */
    @Override
    public int getItemCount() {
        if (!placeFlagHasClipActions) {
            return 0;
        }
        return clipActions.clipActionRecords.size();
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        Set<Integer> ret = new HashSet<>();
        if (placeFlagHasCharacter) {
            ret.add(characterId);
        }
        return ret;
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
    public CXFORM getColorTransform() {
        return null;
    }

    @Override
    public CXFORMWITHALPHA getColorTransformWithAlpha() {
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
    public String toString() {
        if (name != null) {
            return super.toString() + " (" + name + ")";
        } else {
            return super.toString();
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
}
