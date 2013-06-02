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
package com.jpexs.decompiler.flash.types.shaperecords;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class StyleChangeRecord extends SHAPERECORD {

    public int typeFlag = 0;
    public boolean stateNewStyles;
    public boolean stateLineStyle;
    public boolean stateFillStyle1;
    public boolean stateFillStyle0;
    public boolean stateMoveTo;
    public int moveBits;
    public int moveDeltaX;
    public int moveDeltaY;
    public int fillStyle0;
    public int fillStyle1;
    public int lineStyle;
    public FILLSTYLEARRAY fillStyles;
    public LINESTYLEARRAY lineStyles;
    public int numFillBits;
    public int numLineBits;

    @Override
    public Set<Integer> getNeededCharacters() {
        Set<Integer> ret = super.getNeededCharacters();
        if (stateNewStyles) {
            ret.addAll(fillStyles.getNeededCharacters());
        }
        return ret;
    }

    @Override
    public String toString() {
        return "[StyleChangeRecord stateNewStyles=" + stateNewStyles + ", stateLineStyle=" + stateLineStyle + ",stateFillStyle1=" + stateFillStyle1 + ","
                + " stateFillStyle0=" + stateFillStyle0 + ", stateMoveTo=" + stateMoveTo + ", moveBits=" + moveBits + ", moveDeltaX=" + moveDeltaX + ", moveDeltaY=" + moveDeltaY + ","
                + " fillStyle0=" + fillStyle0 + ", fillStyle1=" + fillStyle1 + ", lineStyle=" + lineStyle + ", fillStyles=" + fillStyles + ", lineStyles=" + lineStyles + ", numFillBits=" + numFillBits + ", numLineBits=" + numLineBits + "]";
    }

    @Override
    public String toSWG(int oldX, int oldY) {
        if (stateMoveTo) {
            return "M " + SWF.twipToPixel(moveDeltaX) + " " + SWF.twipToPixel(moveDeltaY);
        }
        return "";
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
}
