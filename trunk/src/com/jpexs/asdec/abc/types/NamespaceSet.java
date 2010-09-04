/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;


public class NamespaceSet {

    public int namespaces[];

    public String toString(ConstantPool constants) {
        String s = "";
        for (int i = 0; i < this.namespaces.length; i++) {
            if (i > 0) s += ", ";
            s += constants.constant_namespace[namespaces[i]].getNameWithKind(constants);
        }
        return s;
    }


}
