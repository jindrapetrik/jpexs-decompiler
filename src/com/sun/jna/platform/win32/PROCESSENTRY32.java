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
import java.util.Arrays;
import java.util.List;

public class PROCESSENTRY32 extends Structure {

    public static class ByReference extends PROCESSENTRY32 implements Structure.ByReference {

        public ByReference() {
        }

        public ByReference(Pointer memory) {
            super(memory);
        }
    }

    public PROCESSENTRY32() {
        dwSize = new WinDef.DWORD(size());
    }

    public PROCESSENTRY32(Pointer memory) {
        super(memory);
        read();
    }

    /**
     * The size of the structure, in bytes. Before calling the Process32First
     * function, set this member to sizeof(PROCESSENTRY32). If you do not
     * initialize dwSize, Process32First fails.
     */
    public WinDef.DWORD dwSize;

    /**
     * This member is no longer used and is always set to zero.
     */
    public WinDef.DWORD cntUsage;

    /**
     * The process identifier.
     */
    public WinNT.DWORD th32ProcessID;

    /**
     * This member is no longer used and is always set to zero.
     */
    public BaseTSD.ULONG_PTR th32DefaultHeapID;

    /**
     * This member is no longer used and is always set to zero.
     */
    public WinDef.DWORD th32ModuleID;

    /**
     * The number of execution threads started by the process.
     */
    public WinDef.DWORD cntThreads;

    /**
     * The identifier of the process that created this process (its parent
     * process).
     */
    public WinDef.DWORD th32ParentProcessID;

    /**
     * The base priority of any threads created by this process.
     */
    public WinDef.LONG pcPriClassBase;

    /**
     * This member is no longer used, and is always set to zero.
     */
    public WinDef.DWORD dwFlags;

    /**
     * The name of the executable file for the process. To retrieve the full
     * path to the executable file, call the Module32First function and check
     * the szExePath member of the MODULEENTRY32 structure that is returned.
     * However, if the calling process is a 32-bit process, you must call the
     * QueryFullProcessImageName function to retrieve the full path of the
     * executable file for a 64-bit process.
     */
    public char[] szExeFile = new char[WinDef.MAX_PATH];

    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[]{"dwSize", "cntUsage", "th32ProcessID", "th32DefaultHeapID", "th32ModuleID", "cntThreads", "th32ParentProcessID", "pcPriClassBase", "dwFlags", "szExeFile"});
    }
}
