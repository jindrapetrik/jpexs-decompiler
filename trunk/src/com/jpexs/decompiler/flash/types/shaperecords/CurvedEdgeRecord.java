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

/**
 *
 * @author JPEXS
 */
public class CurvedEdgeRecord extends SHAPERECORD {

    public int typeFlag = 1;
    public int straightFlag = 0;
    public int numBits;
    public int controlDeltaX;
    public int controlDeltaY;
    public int anchorDeltaX;
    public int anchorDeltaY;

    @Override
    public String toString() {
        return "[CurvedEdgeRecord numBits=" + numBits + ", controlDeltaX=" + controlDeltaX + ", controlDeltaY=" + controlDeltaY + ", anchorDeltaX=" + anchorDeltaX + ". anchorDeltaY=" + anchorDeltaY + "]";
    }

    @Override
    public int changeX(int x) {
        return x + (controlDeltaX + anchorDeltaX);
    }

    @Override
    public int changeY(int y) {
        return y + (controlDeltaY + anchorDeltaY);
    }

    @Override
    public void flip() {
        int tmp;
        tmp = controlDeltaX;
        controlDeltaX = -anchorDeltaX;
        anchorDeltaX = -tmp;
        tmp = controlDeltaY;
        controlDeltaY = -anchorDeltaY;
        anchorDeltaY = -tmp;
    }

    @Override
    public boolean isMove() {
        return true;
    }

    @Override
    public void calculateBits() {
        numBits = SWFOutputStream.getNeededBitsS(controlDeltaX, controlDeltaY, anchorDeltaX, anchorDeltaY) - 2;
        if (numBits < 0) {
            numBits = 0;
        }
    }
}
