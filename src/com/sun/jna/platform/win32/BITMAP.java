/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.LONG;
import com.sun.jna.platform.win32.WinDef.LPVOID;
import com.sun.jna.platform.win32.WinDef.WORD;
import java.util.Arrays;
import java.util.List;

/**
 *
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
