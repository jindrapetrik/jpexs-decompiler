/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.avm2.ConstantPool;


public class Traits {
    public Trait traits[] = new Trait[0];

    @Override
    public String toString() {
        String s = "";
        for (int t = 0; t < traits.length; t++) {
            if (t > 0) s += "\r\n";
            s += traits[t].toString();
        }
        return s;
    }


    public String toString(ConstantPool constants) {
        String s = "";
        for (int t = 0; t < traits.length; t++) {
            if (t > 0) s += "\r\n";
            s += traits[t].toString(constants);
        }
        return s;
    }

    public String convert(ConstantPool constants, String prefix) {
        String s = "";
        for (int t = 0; t < traits.length; t++) {
            if (t > 0) s += "\r\n";
            s += prefix + traits[t].convert(constants, null);
        }
        return s;
    }


}