/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.sun.jna.platform.win32;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class MEMORY_BASIC_INFORMATION extends Structure {

    public Pointer baseAddress;

    public Pointer allocationBase;

    public NativeLong allocationProtect;

    public SIZE_T regionSize;

    public NativeLong state;

    public NativeLong protect;

    public NativeLong type;

    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[]{"baseAddress", "allocationBase", "allocationProtect",
            "regionSize", "state", "protect", "type"});
    }
}
