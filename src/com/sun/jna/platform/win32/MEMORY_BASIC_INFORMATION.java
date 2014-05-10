/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
