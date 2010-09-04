/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2;


public class ConvertException extends Exception {
    public int line;
    public ConvertException(String s,int line) {
        super(s+" on line "+line);
        this.line=line;
    }
}
