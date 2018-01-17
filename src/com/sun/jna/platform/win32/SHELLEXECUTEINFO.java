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

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinReg.HKEY;
import java.util.Arrays;
import java.util.List;

public class SHELLEXECUTEINFO extends Structure {

    public int cbSize = size();

    public int fMask;

    public HWND hwnd;

    public WString lpVerb;

    public WString lpFile;

    public WString lpParameters;

    public WString lpDirectory;

    public int nShow;

    public HINSTANCE hInstApp;

    public Pointer lpIDList;

    public WString lpClass;

    public HKEY hKeyClass;

    public int dwHotKey;

    public HANDLE hMonitor;

    public HANDLE hProcess;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[]{"cbSize", "fMask", "hwnd", "lpVerb", "lpFile", "lpParameters", "lpDirectory", "nShow", "hInstApp", "lpIDList",
            "lpClass", "hKeyClass", "dwHotKey", "hMonitor", "hProcess"});
    }
}
