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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.io.Serializable;
import java.util.List;

/**
 * Defines a character to be displayed in one or more button states.
 *
 * @author JPEXS
 */
public class BUTTONRECORD implements Serializable {

    @Reserved
    @SWFType(value = BasicType.UB, count = 2)
    public int reserved;

    /**
     * @since SWF 8 Has blend mode?
     */
    public boolean buttonHasBlendMode;

    /**
     * @since SWF 8 Has filter list?
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
    public CXFORMWITHALPHA colorTransform;

    /**
     * If within DefineButton2Tag and buttonHasFilterList: List of filters on
     * this button
     */
    @SWFArray("filter")
    public List<FILTER> filterList;

    /**
     * If within DefineButton2Tag and buttonHasBlendMode: Blend mode
     */
    @SWFType(BasicType.UI8)
    @Conditional(value = {"buttonHasBlendMode"}, tags = {DefineButton2Tag.ID})
    public int blendMode;

    @Override
    public String toString() {
        return "[BUTTONRECORD character:" + characterId + ", depth:" + placeDepth + ", state:" + ((buttonStateDown ? "down " : "") + (buttonStateHitTest ? "hit " : "") + (buttonStateOver ? "over " : "") + (buttonStateUp ? "up " : "")) + "]";
    }
}
