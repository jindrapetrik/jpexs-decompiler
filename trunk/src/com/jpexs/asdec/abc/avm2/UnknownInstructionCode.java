/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2;


public class UnknownInstructionCode extends RuntimeException {
    public int code;

    public UnknownInstructionCode(int code) {
        super("Unknown instruction code:" + Integer.toHexString(code));
        this.code = code;
    }
}
