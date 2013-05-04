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
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class SHAPEWITHSTYLE implements NeedsCharacters {

    public FILLSTYLEARRAY fillStyles;
    public LINESTYLEARRAY lineStyles;
    public int numFillBits;
    public int numLineBits;
    public List<SHAPERECORD> shapeRecords;

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> ret = new HashSet<Integer>();
        ret.addAll(fillStyles.getNeededCharacters());
        for (SHAPERECORD r : shapeRecords) {
            ret.addAll(r.getNeededCharacters());
        }
        return ret;
    }

    /**
     * Converts shape to SVG
     *
     * @param shapeNum Type of DefineShape
     * @return String with XML representation of this shape (SVG)
     */
    public String toSVG(int shapeNum) {
        return SHAPERECORD.shapeToSVG(shapeNum, fillStyles, lineStyles, numFillBits, numLineBits, shapeRecords);
    }

    public RECT getBounds() {
        return SHAPERECORD.getBounds(shapeRecords);
    }
}
