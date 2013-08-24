/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HICON;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SHFILEINFO extends Structure {

    public HICON hIcon;
    public int iIcon;
    public DWORD dwAttributes;
    public char[] szDisplayName = new char[260];
    public char[] szTypeName = new char[80];

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("hIcon", "iIcon", "dwAttributes", "szDisplayName", "szTypeName");
    }
}
