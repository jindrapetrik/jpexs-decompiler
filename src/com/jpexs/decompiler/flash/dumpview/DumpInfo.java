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
package com.jpexs.decompiler.flash.dumpview;

/**
 *
 * @author JPEXS
 */
public class DumpInfo {

    public String name;

    public Object previewValue;

    public int startByte;

    public int startBit;
    
    public int lengthBytes;

    public int lengthBits;
    
    public DumpInfo(int startByte, int lengthBytes) {
        
        this.startByte = startByte;
        this.lengthBytes = lengthBytes;
    }

    public DumpInfo(int startByte, int startBit, int lengthBytes, int lengthBits) {
        
        this.startByte = startByte;
        this.lengthBytes = lengthBytes;
        this.startBit = startBit;
        this.lengthBits = lengthBits;
    }
}
