/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ICONINFO extends Structure {

    public boolean fIcon;
    public DWORD xHotspot;
    public DWORD yHotspot;
    public HBITMAP hbmMask;
    public HBITMAP hbmColor;

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("fIcon", "xHotspot", "yHotspot", "hbmMask", "hbmColor");
    }
}
