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
package com.jpexs.decompiler.flash.types.shaperecords;

import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public final class StyleChangeRecord extends SHAPERECORD implements Cloneable {

    public static final boolean typeFlag = false;

    public boolean stateNewStyles;

    public boolean stateLineStyle;

    public boolean stateFillStyle1;

    public boolean stateFillStyle0;

    public boolean stateMoveTo;

    @Calculated
    @SWFType(value = BasicType.UB, count = 5)
    @Conditional("stateMoveTo")
    public int moveBits;

    @SWFType(value = BasicType.SB, countField = "moveBits")
    @Conditional("stateMoveTo")
    public int moveDeltaX;

    @SWFType(value = BasicType.SB, countField = "moveBits")
    @Conditional("stateMoveTo")
    public int moveDeltaY;

    @SWFType(value = BasicType.UB, countField = "fillBits") //last defined fillBits
    @Conditional("stateFillStyle0")
    public int fillStyle0;

    @SWFType(value = BasicType.UB, countField = "fillBits") //last defined fillBits
    @Conditional("stateFillStyle1")
    public int fillStyle1;

    @SWFType(value = BasicType.UB, countField = "lineBits") //last defined lineBits
    @Conditional("stateLineStyle")
    public int lineStyle;

    @Conditional("stateNewStyles")
    public FILLSTYLEARRAY fillStyles;

    @Conditional("stateNewStyles")
    public LINESTYLEARRAY lineStyles;

    @Calculated
    @Conditional("stateNewStyles")
    public int numFillBits;

    @Calculated
    @Conditional("stateNewStyles")
    public int numLineBits;

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        if (stateNewStyles) {
            fillStyles.getNeededCharacters(needed);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        if (fillStyles != null) {
            return fillStyles.replaceCharacter(oldCharacterId, newCharacterId);
        }
        return false;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        if (fillStyles != null) {
            return fillStyles.removeCharacter(characterId);
        }
        return false;
    }

    @Override
    public String toString() {
        return "[StyleChangeRecord stateNewStyles=" + stateNewStyles + ", stateLineStyle=" + stateLineStyle + ",stateFillStyle1=" + stateFillStyle1 + ","
                + " stateFillStyle0=" + stateFillStyle0 + ", stateMoveTo=" + stateMoveTo + ", moveBits=" + moveBits + ", moveDeltaX=" + moveDeltaX + ", moveDeltaY=" + moveDeltaY + ","
                + " fillStyle0=" + fillStyle0 + ", fillStyle1=" + fillStyle1 + ", lineStyle=" + lineStyle + ", fillStyles=" + fillStyles + ", lineStyles=" + lineStyles + ", numFillBits=" + numFillBits + ", numLineBits=" + numLineBits + "]";
    }

    @Override
    public int changeX(int x) {
        if (stateMoveTo) {
            return moveDeltaX;
        }
        return x;
    }

    @Override
    public int changeY(int y) {
        if (stateMoveTo) {
            return moveDeltaY;
        }
        return y;
    }

    @Override
    public void flip() {
    }

    @Override
    public boolean isMove() {
        return stateMoveTo;

    }

    @Override
    public StyleChangeRecord clone() {
        return (StyleChangeRecord) super.clone();
    }

    @Override
    public void calculateBits() {
        moveBits = SWFOutputStream.getNeededBitsS(moveDeltaX, moveDeltaY);
    }
}
