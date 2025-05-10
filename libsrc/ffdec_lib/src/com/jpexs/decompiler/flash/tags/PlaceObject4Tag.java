/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.EndOfStreamException;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.amf.amf3.NoSerializerExistsException;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.BlendMode;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PlaceObject4 tag - adds character to the display list. Extends the
 * functionality of the PlaceObject3Tag - adds AMF3 data.
 *
 * @author JPEXS
 */
@SWFVersion(from = 19)
public class PlaceObject4Tag extends PlaceObjectTypeTag implements ASMSourceContainer {

    public static final int ID = 94;

    public static final String NAME = "PlaceObject4";

    /**
     * @since SWF 5 has clip actions (sprite characters only)
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
     * Has class name or character ID of bitmap to place. If
     * PlaceFlagHasClassName, use ClassName. If PlaceFlagHasCharacter, use
     * CharacterId
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
     * Has opaque background. SWF 11 and higher.
     */
    public boolean placeFlagOpaqueBackground;

    /**
     * Has visibility flag. SWF 11 and higher.
     */
    public boolean placeFlagHasVisible;

    /**
     * Depth of character
     */
    @SWFType(BasicType.UI16)
    public int depth;

    /**
     * If PlaceFlagHasClassName or (PlaceFlagHasImage and
     * PlaceFlagHasCharacter), Name of the class to place
     */
    @Conditional("placeFlagHasClassName")
    public String className;

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
     * If PlaceFlagHasRatio, Ratio
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
    @SWFType(BasicType.UI16)
    @Conditional("placeFlagHasClipDepth")
    public int clipDepth;

    /**
     * If PlaceFlagHasFilterList, List of filters on this object
     */
    @Conditional("placeFlagHasFilterList")
    @SWFArray("filter")
    public List<FILTER> surfaceFilterList;

    /**
     * If PlaceFlagHasBlendMode, Blend mode
     */
    @SWFType(BasicType.UI8)
    @Conditional("placeFlagHasBlendMode")
    @EnumValue(value = 0, text = "normal")
    @EnumValue(value = BlendMode.NORMAL, text = "normal")
    @EnumValue(value = BlendMode.LAYER, text = "layer")
    @EnumValue(value = BlendMode.MULTIPLY, text = "multiply")
    @EnumValue(value = BlendMode.SCREEN, text = "screen")
    @EnumValue(value = BlendMode.LIGHTEN, text = "lighten")
    @EnumValue(value = BlendMode.DARKEN, text = "darken")
    @EnumValue(value = BlendMode.DIFFERENCE, text = "difference")
    @EnumValue(value = BlendMode.ADD, text = "add")
    @EnumValue(value = BlendMode.SUBTRACT, text = "subtract")
    @EnumValue(value = BlendMode.INVERT, text = "invert")
    @EnumValue(value = BlendMode.ALPHA, text = "alpha")
    @EnumValue(value = BlendMode.ERASE, text = "erase")
    @EnumValue(value = BlendMode.OVERLAY, text = "overlay")
    @EnumValue(value = BlendMode.HARDLIGHT, text = "hardlight")
    public int blendMode;

    /**
     * If PlaceFlagHasCacheAsBitmap, 0 = Bitmap cache disabled, 1-255 = Bitmap
     * cache enabled
     */
    @SWFType(BasicType.UI8)
    @Conditional("placeFlagHasCacheAsBitmap")
    public int bitmapCache;

    /**
     * @since SWF 5 If PlaceFlagHasClipActions, Clip Actions Data
     */
    @Conditional(value = "placeFlagHasClipActions", minSwfVersion = 5)
    public CLIPACTIONS clipActions;

    /**
     * If PlaceFlagHasVisible, 0 = Place invisible, 1 = Place visible
     */
    @Conditional("placeFlagHasVisible")
    public int visible;

    /**
     * If PlaceFlagHasVisible, Background color
     */
    @Conditional("placeFlagOpaqueBackground")
    public RGBA backgroundColor;

    // FIXME bug found in ecoDrive.swf,
    @Internal
    private boolean bitmapCacheBug;

    @Reserved
    public boolean reserved;

    public Amf3Value amfData;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public PlaceObject4Tag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public PlaceObject4Tag(SWF swf, boolean placeFlagMove, int depth, String className, int characterId, MATRIX matrix, CXFORMWITHALPHA colorTransform, int ratio, String name, int clipDepth, List<FILTER> surfaceFilterList, int blendMode, Integer bitmapCache, Integer visible, RGBA backgroundColor, CLIPACTIONS clipActions, Amf3Value amfData, boolean placeFlagHasImage) {
        super(swf, ID, NAME, null);
        this.placeFlagHasClassName = className != null;
        this.placeFlagHasFilterList = surfaceFilterList != null;
        this.placeFlagHasBlendMode = blendMode >= 0;
        this.placeFlagHasCacheAsBitmap = bitmapCache != null;
        this.placeFlagHasVisible = visible != null;
        this.placeFlagOpaqueBackground = backgroundColor != null;
        this.placeFlagHasClipActions = clipActions != null;
        this.placeFlagHasClipDepth = clipDepth >= 0;
        this.placeFlagHasName = name != null;
        this.placeFlagHasRatio = ratio >= 0;
        this.placeFlagHasColorTransform = colorTransform != null;
        this.placeFlagHasMatrix = matrix != null;
        this.placeFlagHasCharacter = characterId >= 0;
        this.placeFlagMove = placeFlagMove;
        this.depth = depth;
        this.className = className;
        this.characterId = characterId;
        this.matrix = matrix;
        this.colorTransform = colorTransform;
        this.ratio = ratio;
        this.name = name;
        this.clipDepth = clipDepth;
        this.surfaceFilterList = surfaceFilterList;
        this.blendMode = blendMode;
        this.bitmapCache = bitmapCache == null ? 0 : bitmapCache;
        this.visible = visible == null ? 0 : visible;
        this.backgroundColor = backgroundColor;
        this.clipActions = clipActions;
        this.amfData = amfData;
        this.placeFlagHasImage = placeFlagHasImage;
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public PlaceObject4Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
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
        reserved = sis.readUB(1, "reserved") == 1;
        placeFlagOpaqueBackground = sis.readUB(1, "placeFlagOpaqueBackground") == 1; //SWF11
        placeFlagHasVisible = sis.readUB(1, "placeFlagHasVisible") == 1;       //SWF11
        placeFlagHasImage = sis.readUB(1, "placeFlagHasImage") == 1;
        placeFlagHasClassName = sis.readUB(1, "placeFlagHasClassName") == 1;
        placeFlagHasCacheAsBitmap = sis.readUB(1, "placeFlagHasCacheAsBitmap") == 1;
        placeFlagHasBlendMode = sis.readUB(1, "placeFlagHasBlendMode") == 1;
        placeFlagHasFilterList = sis.readUB(1, "placeFlagHasFilterList") == 1;

        depth = sis.readUI16("depth");
        if (placeFlagHasClassName) {
            className = sis.readString("className");
        }
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
        if (placeFlagHasFilterList) {
            surfaceFilterList = sis.readFILTERLIST("surfaceFilterList");
        }
        if (placeFlagHasBlendMode) {
            blendMode = sis.readUI8("blendMode");
        }
        bitmapCacheBug = false;
        if (placeFlagHasCacheAsBitmap) {
            try {
                bitmapCache = sis.readUI8("bitmapCache");
            } catch (EndOfStreamException eex) {
                bitmapCacheBug = true;
                bitmapCache = 1;
            }
        }

        if (placeFlagHasVisible) {
            visible = sis.readUI8("visible");
        }
        if (placeFlagOpaqueBackground) {
            backgroundColor = sis.readRGBA("backgroundColor");
        }

        if (placeFlagHasClipActions) {
            clipActions = sis.readCLIPACTIONS(swf, this, "clipActions");
        }
        if (sis.available() > 0) {
            try {
                amfData = sis.readAmf3Object("amfValue");
            } catch (NoSerializerExistsException nse) {
                amfData = new Amf3Value(nse.getIncompleteData());
                Logger.getLogger(PlaceObject4Tag.class.getName()).log(Level.WARNING, "AMFData in PlaceObject4 contains IExternalizable object which cannot be read. Data object is truncated.", nse);
            }
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
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
        sos.writeUB(1, reserved ? 1 : 0);
        sos.writeUB(1, placeFlagOpaqueBackground ? 1 : 0); //SWF11
        sos.writeUB(1, placeFlagHasVisible ? 1 : 0); //SWF11
        sos.writeUB(1, placeFlagHasImage ? 1 : 0);
        sos.writeUB(1, placeFlagHasClassName ? 1 : 0);
        sos.writeUB(1, placeFlagHasCacheAsBitmap ? 1 : 0);
        sos.writeUB(1, placeFlagHasBlendMode ? 1 : 0);
        sos.writeUB(1, placeFlagHasFilterList ? 1 : 0);
        sos.writeUI16(depth);

        if (placeFlagHasClassName) {
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
        if (placeFlagHasVisible) {
            sos.writeUI8(visible);
        }
        if (placeFlagOpaqueBackground) {
            sos.writeRGBA(backgroundColor);
        }
        if (placeFlagHasClipActions) {
            sos.writeCLIPACTIONS(clipActions);
        }
        if (amfData != null && amfData.getValue() != null) {
            try {
                sos.writeAmf3Object(amfData);
            } catch (NoSerializerExistsException ex) {
                throw new IOException("Class \"" + ex.getClassName() + "\" implements IExternalizable, it cannot be saved");
            }
        }
    }

    @Override
    public int getPlaceObjectNum() {
        return 4;
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
        if (placeFlagHasFilterList) {
            return surfaceFilterList;
        } else {
            return null;
        }
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
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        if (placeFlagHasCharacter) {
            needed.add(characterId);
        }
        if (placeFlagHasClassName) {
            int chId = swf.getCharacterId(swf.getCharacterByClass(className));
            if (chId != -1) {
                needed.add(chId);
            }
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
    public void setDepth(int depth) {
        this.depth = depth;
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
        return blendMode;
    }

    @Override
    public String getClassName() {
        if (placeFlagHasClassName) {
            return className;
        }
        return null;
    }

    @Override
    public boolean cacheAsBitmap() {
        return placeFlagHasCacheAsBitmap && bitmapCache == 1;
    }

    @Override
    public boolean isVisible() {
        if (placeFlagHasVisible) {
            return visible == 1;
        }
        return true;
    }

    @Override
    public RGBA getBackgroundColor() {
        if (placeFlagOpaqueBackground) {
            return backgroundColor;
        }
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
        placeFlagHasClassName = true;
        this.className = className;
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        if (placeFlagHasName) {
            ret.put("nm", "" + name);
        }
        return ret;
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
        return amfData;
    }

    @Override
    public Integer getBitmapCache() {
        if (placeFlagHasCacheAsBitmap) {
            return bitmapCache;
        }
        return null;
    }

    @Override
    public Integer getVisible() {
        if (placeFlagHasVisible) {
            return visible;
        }
        return null;
    }

    @Override
    public void setClipActions(CLIPACTIONS clipActions) {
        this.clipActions = clipActions;
    }

    @Override
    public void setPlaceFlagHasClipActions(boolean placeFlagHasClipActions) {
        this.placeFlagHasClipActions = placeFlagHasClipActions;
    }

    @Override
    public void setPlaceFlagHasMatrix(boolean placeFlagHasMatrix) {
        this.placeFlagHasMatrix = placeFlagHasMatrix;
    }

    @Override
    public void setPlaceFlagMove(boolean placeFlagMove) {
        this.placeFlagMove = placeFlagMove;
    }

    @Override
    public boolean hasImage() {
        return placeFlagHasImage;
    }
    
    @Override
    public void setColorTransform(ColorTransform colorTransform) {
        this.colorTransform = new CXFORMWITHALPHA(colorTransform);
        placeFlagHasColorTransform = true;
    }
    
    @Override
    public void setVisible(int value) {
        this.visible = value;
        placeFlagHasVisible = true;
    }        

    @Override
    public void setPlaceFlagHasVisible(boolean value) {
        this.placeFlagHasVisible = value;
    }
    
    @Override
    public void setBlendMode(int value) {
        this.blendMode = value;
        this.placeFlagHasBlendMode = true;
    }

    @Override
    public void setPlaceFlagHasBlendMode(boolean value) {
        this.placeFlagHasBlendMode = value;
    }
    
    @Override
    public void setBitmapCache(int value) {
        this.bitmapCache = value;
        this.placeFlagHasCacheAsBitmap = true;
    }

    @Override
    public void setPlaceFlagHasCacheAsBitmap(boolean value) {
        this.placeFlagHasCacheAsBitmap = value;
    }
    
    @Override
    public void setBackgroundColor(RGBA value) {
        this.backgroundColor = value;
        this.placeFlagOpaqueBackground = true;
    }

    @Override
    public void setPlaceFlagOpaqueBackground(boolean value) {
        this.placeFlagOpaqueBackground = value;
    }
    
    @Override
    public void setPlaceFlagHasFilterList(boolean placeFlagHasFilterList) {
        this.placeFlagHasFilterList = placeFlagHasFilterList;
    }

    @Override
    public void setFilters(List<FILTER> filters) {
        this.surfaceFilterList = new ArrayList<>(filters);
        this.placeFlagHasFilterList = true;
    }
}
