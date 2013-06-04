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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class MORPHFILLSTYLE implements NeedsCharacters {

    public int fillStyleType;
    public static final int SOLID = 0x0;
    public static final int LINEAR_GRADIENT = 0x10;
    public static final int RADIAL_GRADIENT = 0x12;
    public static final int REPEATING_BITMAP = 0x40;
    public static final int CLIPPED_BITMAP = 0x41;
    public static final int NON_SMOOTHED_REPEATING_BITMAP = 0x42;
    public static final int NON_SMOOTHED_CLIPPED_BITMAP = 0x43;
    public RGBA startColor;
    public RGBA endColor;
    public MATRIX startGradientMatrix;
    public MATRIX endGradientMatrix;
    public MORPHGRADIENT gradient;
    public int bitmapId;
    public MATRIX startBitmapMatrix;
    public MATRIX endBitmapMatrix;

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> ret = new HashSet<>();
        if ((fillStyleType == REPEATING_BITMAP)
                || (fillStyleType == CLIPPED_BITMAP)
                || (fillStyleType == NON_SMOOTHED_REPEATING_BITMAP)
                || (fillStyleType == NON_SMOOTHED_CLIPPED_BITMAP)) {
            ret.add(bitmapId);
        }
        return ret;
    }

    public FILLSTYLE getStartFillStyle() {
        FILLSTYLE ret = new FILLSTYLE();
        ret.bitmapId = bitmapId;
        ret.bitmapMatrix = startBitmapMatrix;
        ret.colorA = startColor;
        ret.fillStyleType = fillStyleType;
        if (gradient != null) {
            ret.gradient = gradient.getStartGradient();
        }
        ret.gradientMatrix = startGradientMatrix;
        return ret;
    }

    public FILLSTYLE getEndFillStyle() {
        FILLSTYLE ret = new FILLSTYLE();
        ret.bitmapId = bitmapId;
        ret.bitmapMatrix = endBitmapMatrix;
        ret.colorA = endColor;
        ret.fillStyleType = fillStyleType;
        if (gradient != null) {
            ret.gradient = gradient.getEndGradient();
        }
        ret.gradientMatrix = endGradientMatrix;
        return ret;
    }
}
