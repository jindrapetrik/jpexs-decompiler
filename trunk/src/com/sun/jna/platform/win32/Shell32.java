/* Copyright (c) 2007 Timothy Wall, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
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
     * @return true if successful. Otherwise false.
     */
    boolean ShellExecuteEx(SHELLEXECUTEINFO lpExecInfo);

    UINT ExtractIconEx(String lpszFile, int nIconIndex, PointerByReference phiconLarge, PointerByReference phiconSmall, UINT nIcons);

    DWORD_PTR SHGetFileInfo(String pszPath, int dwFileAttributes, SHFILEINFO psfi, int cbFileInfo, int uFlags);
    public static final int SHGFI_ICON = 0x000000100;
    public static final int SHGFI_SMALLICON = 0x000000001;
}
