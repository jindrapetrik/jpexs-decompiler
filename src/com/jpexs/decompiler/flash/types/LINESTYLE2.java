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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class LINESTYLE2 extends LINESTYLE implements Serializable {

    @SWFType(value = BasicType.UB, count = 2)
    public int startCapStyle;
    @SWFType(value = BasicType.UB, count = 2)
    public int joinStyle;
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
    public int endCapStyle;
    public static final int ROUND_CAP = 0;
    public static final int NO_CAP = 1;
    public static final int SQUARE_CAP = 2;

    @SWFType(BasicType.UI16)
    @Conditional(value = "joinStyle", options = MITER_JOIN)
    public int miterLimitFactor;
    public FILLSTYLE fillType;
    
    
    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> ret = new HashSet<>();
        if(hasFillFlag){
            ret.addAll(fillType.getNeededCharacters());
        }
        return ret;
    }
}
