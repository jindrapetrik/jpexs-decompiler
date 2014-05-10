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

import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Color;
import java.io.Serializable;

/**
 * Represents 24-bit red, green, blue value
 *
 * @author JPEXS
 */
public class RGB implements Serializable {

    /**
     * Red color value
     */
    @SWFType(BasicType.UI8)
    public int red;
    /**
     * Green color value
     */
    @SWFType(BasicType.UI8)
    public int green;
    /**
     * Blue color value
     */
    @SWFType(BasicType.UI8)
    public int blue;

    public RGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public RGB() {
    }

    public String toHexRGB() {
        String rh = Integer.toHexString(red);
        if (rh.length() < 2) {
            rh = "0" + rh;
        }
        String gh = Integer.toHexString(green);
        if (gh.length() < 2) {
            gh = "0" + gh;
        }
        String bh = Integer.toHexString(blue);
        if (bh.length() < 2) {
            bh = "0" + bh;
        }
        return "#" + rh + gh + bh;
    }

    public Color toColor() {
        return new Color(red, green, blue);
    }

    public RGB(Color color) {
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
    }

    @Override
    public String toString() {
        return "[RGB red:" + red + ", green:" + green + ", blue:" + blue + "]";
    }
}
