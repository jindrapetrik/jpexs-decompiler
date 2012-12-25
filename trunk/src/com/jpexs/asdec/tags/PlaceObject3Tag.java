/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.tags;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.abc.CopyOutputStream;
import com.jpexs.asdec.types.CLIPACTIONS;
import com.jpexs.asdec.types.CXFORMWITHALPHA;
import com.jpexs.asdec.types.MATRIX;
import com.jpexs.asdec.types.filters.FILTER;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends the functionality of the PlaceObject2Tag
 *
 * @author JPEXS
 */
public class PlaceObject3Tag extends Tag implements Container {
    /**
     * @since SWF 5
     *        has clip actions (sprite characters only)
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
     * Has class name or character ID of bitmap to place.
     * If PlaceFlagHasClassName, use ClassName.
     * If PlaceFlagHasCharacter, use CharacterId
     */
    public boolean placeFlagHasImage;
    /**
     * Has class name of object to place
     */
    public boolean placeFlagHasClassName;
    /**
     * Enables bitmap caching
     */
    public boolean placeFlagHasCacheAsBitmap;
    /**
     * Has blend mode
     */
    public boolean placeFlagHasBlendMode;
    /**
     * Has filter list
     */
    public boolean placeFlagHasFilterList;

    /**
     * Depth of character
     */
    public int depth;

    /**
     * If PlaceFlagHasClassName or (PlaceFlagHasImage and PlaceFlagHasCharacter), Name of the class to place
     */
    public String className;

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
     * If PlaceFlagHasRatio, Ratio
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
     * If PlaceFlagHasFilterList, List of filters on this object
     */
    public List<FILTER> surfaceFilterList;
    /**
     * If PlaceFlagHasBlendMode, Blend mode
     */
    public int blendMode;

    /**
     * If PlaceFlagHasCacheAsBitmap, 0 = Bitmap cache disabled, 1-255 = Bitmap cache enabled
     */
    public int bitmapCache;
    /**
     * @since SWF 5
     *        If PlaceFlagHasClipActions, Clip Actions Data
     */
    public CLIPACTIONS clipActions;
	// FIXME bug found in ecoDrive.swf, 
	private boolean bitmapCacheBug;


    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        if (Main.DISABLE_DANGEROUS) return super.getData(version);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Main.DEBUG_COPY) {
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
            sos.writeUB(3, 0);
            sos.writeUB(1, placeFlagHasImage ? 1 : 0);
            sos.writeUB(1, placeFlagHasClassName ? 1 : 0);
            sos.writeUB(1, placeFlagHasCacheAsBitmap ? 1 : 0);
            sos.writeUB(1, placeFlagHasBlendMode ? 1 : 0);
            sos.writeUB(1, placeFlagHasFilterList ? 1 : 0);
            sos.writeUI16(depth);
            if (placeFlagHasClassName || (placeFlagHasImage && placeFlagHasCharacter)) {
                sos.writeString(className);
            }
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
            if (placeFlagHasFilterList) {
                sos.writeFILTERLIST(surfaceFilterList);
            }
            if (placeFlagHasBlendMode) {
                sos.writeUI8(blendMode);
            }
            if (placeFlagHasCacheAsBitmap) {
            	if (!bitmapCacheBug) {
            		sos.writeUI8(bitmapCache);
            	}
            }
            if (placeFlagHasClipActions) {
                sos.writeCLIPACTIONS(clipActions);
            }
            sos.close();
        } catch (IOException ex) {

        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param data    Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public PlaceObject3Tag(byte data[], int version, long pos) throws IOException {
        super(70, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        placeFlagHasClipActions = sis.readUB(1) == 1;
        placeFlagHasClipDepth = sis.readUB(1) == 1;
        placeFlagHasName = sis.readUB(1) == 1;
        placeFlagHasRatio = sis.readUB(1) == 1;
        placeFlagHasColorTransform = sis.readUB(1) == 1;
        placeFlagHasMatrix = sis.readUB(1) == 1;
        placeFlagHasCharacter = sis.readUB(1) == 1;
        placeFlagMove = sis.readUB(1) == 1;
        sis.readUB(3); //reserved
        placeFlagHasImage = sis.readUB(1) == 1;
        placeFlagHasClassName = sis.readUB(1) == 1;
        placeFlagHasCacheAsBitmap = sis.readUB(1) == 1;
        placeFlagHasBlendMode = sis.readUB(1) == 1;
        placeFlagHasFilterList = sis.readUB(1) == 1;

        depth = sis.readUI16();
        if (placeFlagHasClassName || (placeFlagHasImage && placeFlagHasCharacter)) {
            className = sis.readString();
        }
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
        if (placeFlagHasFilterList) {
            surfaceFilterList = sis.readFILTERLIST();
        }
        if (placeFlagHasBlendMode) {
            blendMode = sis.readUI8();
        }
        bitmapCacheBug = false;
        if (placeFlagHasCacheAsBitmap) {
            bitmapCache = sis.readUI8();
            if (bitmapCache == -1) {
            	// EOF
            	bitmapCacheBug = true;
            	bitmapCache = 1;
            }
        }
        if (placeFlagHasClipActions) {
            clipActions = sis.readCLIPACTIONS();
        }
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "PlaceObject3Tag";
    }

    /**
     * Returns all sub-items
     *
     * @return List of sub-items
     */
    public List<Object> getSubItems() {
        List<Object> ret = new ArrayList<Object>();
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
    public int getItemCount() {
        if (!placeFlagHasClipActions) return 0;
        return clipActions.clipActionRecords.size();
    }
}
