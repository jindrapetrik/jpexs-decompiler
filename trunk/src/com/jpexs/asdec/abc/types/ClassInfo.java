/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.types.traits.Traits;


public class ClassInfo {

    public int cinit_index; //MethodInfo - static initializer
    public Traits static_traits;

    @Override
    public String toString() {
        return "method_index=" + cinit_index + "\r\n" + static_traits.toString();
    }


    public String toString(ConstantPool constants) {
        return "method_index=" + cinit_index + "\r\n" + static_traits.toString(constants);
    }

    public String getStaticVarsStr(ConstantPool constants) {
        return static_traits.convert(constants, "\tstatic ");
    }
}
