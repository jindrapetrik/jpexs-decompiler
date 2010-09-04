/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.types.traits.Traits;


public class ScriptInfo {

    public int init_index; //MethodInfo
    public Traits traits;

    @Override
    public String toString() {
        return "method_index=" + init_index + "\r\n" + traits.toString();
    }


    public String toString(ConstantPool constants) {
        return "method_index=" + init_index + "\r\n" + traits.toString(constants);
    }


}
