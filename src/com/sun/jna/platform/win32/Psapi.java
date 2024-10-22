/*
 *  Copyright (C) 2010-2024 JPEXS
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

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;

/**
 * @author JPEXS
 */
public interface Psapi extends StdCallLibrary {

    Psapi INSTANCE = (Psapi) Native.loadLibrary("Psapi", Psapi.class);
//For some Windows 7 Versions and older down to XP
    //boolean EnumProcesses(int[] ProcessIDsOut, int size, int[] BytesReturned);

    int GetProcessImageFileNameW(HANDLE Process, char[] outputname, int length);
}
