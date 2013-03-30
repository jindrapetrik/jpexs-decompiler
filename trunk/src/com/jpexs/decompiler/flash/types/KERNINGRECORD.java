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

/**
 * Represents 24-bit red, green, blue value
 *
 * @author JPEXS
 */
public class KERNINGRECORD {

    public int fontKerningCode1;
    public int fontKerningCode2;
    public int fontKerningAdjustment;

    @Override
    public String toString() {
        return "[KERNINGRECORD fontKerningCode1=" + fontKerningCode1 + ", fontKerningCode2=" + fontKerningCode2 + ", fontKerningAdjustment=" + fontKerningAdjustment + "]";
    }
}
