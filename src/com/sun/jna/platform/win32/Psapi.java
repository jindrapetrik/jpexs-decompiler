/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;

/**
 *
 * @author petrik
 */
public interface Psapi extends StdCallLibrary {

    Psapi INSTANCE = (Psapi) Native.loadLibrary("Psapi", Psapi.class);
//For some Windows 7 Versions and older down to XP
    //boolean EnumProcesses(int[] ProcessIDsOut, int size, int[] BytesReturned);

    int GetProcessImageFileNameW(HANDLE Process, char[] outputname, int lenght);
}
