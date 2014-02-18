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

import java.io.Serializable;

/**
 * Represents 32-bit alpha, red, green and blue value
 *
 * @author JPEXS
 */
public class ZONERECORD implements Serializable{

    public ZONEDATA[] zonedata = new ZONEDATA[0];
    public boolean zoneMaskX;
    public boolean zoneMaskY;

    @Override
    public String toString() {
        String ret = "[ZONERECORD data:";
        for (int i = 0; i < zonedata.length; i++) {
            if (i > 0) {
                ret += ", ";
            }
            ret += zonedata[i];
        }
        return ret + ", zoneMaskX:" + zoneMaskX + ", zoneMaskY:" + zoneMaskY + "]";
    }
}
