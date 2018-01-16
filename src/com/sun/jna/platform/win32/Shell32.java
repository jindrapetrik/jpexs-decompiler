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

import com.sun.jna.Native;
import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import com.sun.jna.platform.win32.WinDef.UINT;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Shell32.dll Interface.
 */
public interface Shell32 extends StdCallLibrary {

    Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32", Shell32.class,
            W32APIOptions.UNICODE_OPTIONS);

    /**
     * @param lpExecInfo lpExecInfo
     * @return true if successful. Otherwise false.
     */
    boolean ShellExecuteEx(SHELLEXECUTEINFO lpExecInfo);

    UINT ExtractIconEx(String lpszFile, int nIconIndex, PointerByReference phiconLarge, PointerByReference phiconSmall, UINT nIcons);

    DWORD_PTR SHGetFileInfo(String pszPath, int dwFileAttributes, SHFILEINFO psfi, int cbFileInfo, int uFlags);

    public static final int SHGFI_ICON = 0x000000100;

    public static final int SHGFI_SMALLICON = 0x000000001;
}
