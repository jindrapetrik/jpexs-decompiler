/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.List;

/**
 * Defines a character to be displayed in one or more button states.
 *
 * @author JPEXS
 */
public class BUTTONRECORD implements Serializable, TreeItem, HasSwfAndTag, HasCharacterId {

    /**
     * Reserved
     */
    @Reserved
    @SWFType(value = BasicType.UB, count = 2)
    public int reserved;

    /**
     * Has blend mode?
     *
     * @since SWF 8
     */
    public boolean buttonHasBlendMode;

    /**
     * Has filter list?
     *
     * @since SWF 8
     */
    public boolean buttonHasFilterList;

    /**
     * Present in hit test state
     */
    public boolean buttonStateHitTest;

    /**
     * Present in down state
     */
    public boolean buttonStateDown;

    /**
     * Present in over state
     */
    public boolean buttonStateOver;

    /**
     * Present in up state
     */
    public boolean buttonStateUp;

    /**
     * ID of character to place
     */
    @SWFType(BasicType.UI16)
    public int characterId;

    /**
     * Depth at which to place character
     */
    @SWFType(BasicType.UI16)
    public int placeDepth;

    /**
     * Transformation matrix for character placement
     */
    public MATRIX placeMatrix;

    /**
     * If within DefineButton2Tag: Character color transform
     */
    @Conditional(tags = {DefineButton2Tag.ID})
    public CXFORMWITHALPHA colorTransform;

    /**
     * If within DefineButton2Tag and buttonHasFilterList: List of filters on
     * this button
     */
    @SWFArray("filter")
    @Conditional(value = "buttonHasFilterList", tags = {DefineButton2Tag.ID})
    public List<FILTER> filterList;

    /**
     * If within DefineButton2Tag and buttonHasBlendMode: Blend mode
     */
    @SWFType(BasicType.UI8)
    @Conditional(value = {"buttonHasBlendMode"}, tags = {DefineButton2Tag.ID})
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

    @Internal
    private SWF swf;

    @Internal
    private ButtonTag tag;

    @Internal
    private boolean modified;

    /**
     * Constructor.
     *
     * @param swf SWF
     * @param tag Button tag
     */
    public BUTTONRECORD(SWF swf, ButtonTag tag) {
        this.swf = swf;
        this.tag = tag;
    }

    public BUTTONRECORD(BUTTONRECORD source) {
        this.buttonHasBlendMode = source.buttonHasBlendMode;
        this.buttonHasFilterList = source.buttonHasFilterList;
        this.buttonStateHitTest = source.buttonStateHitTest;
        this.buttonStateDown = source.buttonStateDown;
        this.buttonStateOver = source.buttonStateOver;
        this.buttonStateUp = source.buttonStateUp;
        this.characterId = source.characterId;
        this.placeDepth = source.placeDepth;
        this.placeMatrix = new MATRIX(source.placeMatrix);
        this.colorTransform = source.colorTransform == null ? null : new CXFORMWITHALPHA(source.colorTransform);
        this.filterList = Helper.deepCopy(source.filterList);
        this.blendMode = source.blendMode;
        this.swf = source.swf;
        this.tag = source.tag;
        this.modified = source.modified;
    }

    /**
     * Constructor.
     */
    public BUTTONRECORD() {
        swf = null;
        tag = null;
    }

    @Override
    public String toString() {
        return "BUTTONRECORD (chid: " + characterId + ", dpt: " + placeDepth + ", state: " + ((buttonStateDown ? "down " : "") + (buttonStateHitTest ? "hit " : "") + (buttonStateOver ? "over " : "") + (buttonStateUp ? "up " : "")).trim() + ")";
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    @Override
    public Openable getOpenable() {
        return swf;
    }

    /**
     * Sets the modified flag.
     *
     * @param value Modified flag
     */
    public void setModified(boolean value) {
        modified = value;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public ButtonTag getTag() {
        return tag;
    }

    @Override
    public void setSourceTag(Tag tag) {
        this.swf = tag.getSwf();
        this.tag = (ButtonTag) tag;
    }

    @Override
    public int getCharacterId() {
        return characterId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    /**
     * Enables/disables specific frame
     *
     * @param frame Frame
     * @param value Value
     */
    public void setFrame(int frame, boolean value) {
        switch (frame) {
            case ButtonTag.FRAME_UP:
                buttonStateUp = value;
                break;
            case ButtonTag.FRAME_OVER:
                buttonStateOver = value;
                break;
            case ButtonTag.FRAME_DOWN:
                buttonStateDown = value;
                break;
            case ButtonTag.FRAME_HITTEST:
                buttonStateHitTest = value;
                break;
        }
    }

    /**
     * Has frame
     *
     * @param frame Frame
     * @return True if has
     */
    public boolean hasFrame(int frame) {
        switch (frame) {
            case ButtonTag.FRAME_UP:
                return buttonStateUp;
            case ButtonTag.FRAME_OVER:
                return buttonStateOver;
            case ButtonTag.FRAME_DOWN:
                return buttonStateDown;
            case ButtonTag.FRAME_HITTEST:
                return buttonStateHitTest;
        }
        return false;
    }

    /**
     * Imports placeObject to this BUTTONRECORD
     *
     * @param placeObject Place tag
     */
    public void fromPlaceObject(PlaceObjectTypeTag placeObject) {
        placeDepth = placeObject.getDepth();
        characterId = placeObject.getCharacterId();
        ColorTransform importedColorTrans = placeObject.getColorTransform();
        colorTransform = importedColorTrans == null ? new CXFORMWITHALPHA() : new CXFORMWITHALPHA(placeObject.getColorTransform());
        placeMatrix = placeObject.getMatrix();
        blendMode = placeObject.getBlendMode();
        buttonHasBlendMode = blendMode > 0;
        filterList = placeObject.getFilters();
        buttonHasFilterList = filterList != null && !filterList.isEmpty();
    }

    /**
     * Converts this BUTTONRECORD to a place tag.
     *
     * @return Place tag
     */
    public PlaceObject3Tag toPlaceObject() {
        PlaceObject3Tag placeTag = new PlaceObject3Tag(swf);
        placeTag.depth = placeDepth;
        placeTag.characterId = characterId;
        placeTag.placeFlagHasCharacter = true;
        if (colorTransform != null) {
            placeTag.colorTransform = colorTransform;
            placeTag.placeFlagHasColorTransform = true;
        }

        ButtonTag buttonTag = getTag();
        if (buttonTag instanceof DefineButtonTag) {
            DefineButtonTag button1Tag = (DefineButtonTag) buttonTag;
            DefineButtonCxformTag cxformTag = (DefineButtonCxformTag) button1Tag.getSwf().getCharacterIdTag(button1Tag.getCharacterId(), DefineButtonCxformTag.ID);
            if (cxformTag != null) {
                placeTag.colorTransform = new CXFORMWITHALPHA(cxformTag.buttonColorTransform);
                placeTag.placeFlagHasColorTransform = true;
            }
        }

        placeTag.matrix = new MATRIX(placeMatrix);
        placeTag.placeFlagHasMatrix = true;

        if (buttonHasBlendMode) {
            placeTag.blendMode = blendMode;
            placeTag.placeFlagHasBlendMode = true;
        }
        if (buttonHasFilterList) {
            placeTag.surfaceFilterList = filterList;
            placeTag.placeFlagHasFilterList = true;
        }
        return placeTag;
    }

    public boolean isEmpty() {
        if (buttonStateUp) {
            return false;
        }
        if (buttonStateOver) {
            return false;
        }
        if (buttonStateDown) {
            return false;
        }
        if (buttonStateHitTest) {
            return false;
        }
        return true;
    }
}
