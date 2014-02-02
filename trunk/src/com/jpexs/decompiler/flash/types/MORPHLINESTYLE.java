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

/**
 *
 * @author JPEXS
 */
public class MORPHLINESTYLE {

    public int startWidth;
    public int endWidth;
    public RGBA startColor;
    public RGBA endColor;

    public LINESTYLE getStartLineStyle() {
        LINESTYLE ret = new LINESTYLE();
        ret.color = startColor;
        ret.width = startWidth;
        return ret;
    }

    public LINESTYLE getLineStyleAt(int ratio) {
        LINESTYLE ret = new LINESTYLE();
        ret.color = MORPHGRADIENT.morphColor(startColor, endColor, ratio);
        ret.width = startWidth + (endWidth - startWidth) * ratio / 65535;
        return ret;
    }

    public LINESTYLE getEndLineStyle() {
        LINESTYLE ret = new LINESTYLE();
        ret.color = endColor;
        ret.width = endWidth;
        return ret;
    }
}
