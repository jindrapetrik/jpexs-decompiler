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
package com.jpexs.decompiler.flash.types.shaperecords;

import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class StyleChangeRecord extends SHAPERECORD implements Cloneable {

    public boolean typeFlag = false;
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
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(StyleChangeRecord.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void calculateBits() {
        moveBits = SWFOutputStream.getNeededBitsS(moveDeltaX, moveDeltaY);
    }
}
