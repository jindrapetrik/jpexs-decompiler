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

import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class LINESTYLE2 extends LINESTYLE implements Serializable {

    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_CAP, text = "Round cap")
    @EnumValue(value = NO_CAP, text = "No cap")
    @EnumValue(value = SQUARE_CAP, text = "Square cap")
    public int startCapStyle;

    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_JOIN, text = "Round join")
    @EnumValue(value = BEVEL_JOIN, text = "Bevel join")
    @EnumValue(value = MITER_JOIN, text = "Miter join")
    public int joinStyle;

    public static final int ROUND_CAP = 0;

    public static final int NO_CAP = 1;

    public static final int SQUARE_CAP = 2;

    public static final int ROUND_JOIN = 0;

    public static final int BEVEL_JOIN = 1;

    public static final int MITER_JOIN = 2;

    public boolean hasFillFlag;

    public boolean noHScaleFlag;

    public boolean noVScaleFlag;

    public boolean pixelHintingFlag;

    @Reserved
    @SWFType(value = BasicType.UB, count = 5)
    public int reserved;

    public boolean noClose;

    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = ROUND_CAP, text = "Round cap")
    @EnumValue(value = NO_CAP, text = "No cap")
    @EnumValue(value = SQUARE_CAP, text = "Square cap")
    public int endCapStyle;

    @SWFType(BasicType.FIXED8)
    @Conditional(value = "joinStyle", options = {MITER_JOIN})
    public float miterLimitFactor;

    public FILLSTYLE fillType;

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        if (hasFillFlag) {
            fillType.getNeededCharacters(needed);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        if (fillType != null) {
            return fillType.replaceCharacter(oldCharacterId, newCharacterId);
        }
        return false;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        if (fillType != null) {
            return fillType.removeCharacter(characterId);
        }
        return false;
    }
}
