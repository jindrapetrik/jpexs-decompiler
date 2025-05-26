/*
 *  Copyright (C) 2010-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui.jna.platform.win32;

import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinDef.LONG;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinDef.LPVOID;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinDef.WORD;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

/**
 * @author JPEXS
 */
public class BITMAP extends Structure {

    public LONG bmType;

    public LONG bmWidth;

    public LONG bmHeight;

    public LONG bmWidthBytes;

    public WORD bmPlanes;

    public WORD bmBitsPixel;

    public LPVOID bmBits;

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("bmType", "bmWidth", "bmHeight", "bmWidthBytes", "bmPlanes", "bmBitsPixel", "bmBits");
    }
}
