/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.exceptions;

/**
 *
 * @author JPEXS
 */
public class AVM2VerifyErrorException extends AVM2ExecutionException {

    public static final int ILLEGAL_OPCODE = 1011;

    public static final int BRANCH_TARGET_INVALID_INSTRUCTION = 1021;

    public static final int CPOOL_INDEX_OUT_OF_RANGE = 1032;

    public AVM2VerifyErrorException(int code, boolean debug) {
        super(codeToMessage(code, debug, null));
    }

    public AVM2VerifyErrorException(int code, boolean debug, Object[] params) {
        super(codeToMessage(code, debug, params));
    }

    private static String codeToMessage(int code, boolean debug, Object[] params) {
        String msg = null;
        switch (code) {
            case ILLEGAL_OPCODE:
                msg = "Method " + params[0] + " contained illegal opcode " + params[1] + " at offset " + params[2] + ".";
                break;
            case 1014:
                msg = "class could not be found";
                break;
            case BRANCH_TARGET_INVALID_INSTRUCTION:
                msg = "At least one branch target was not on a valid instruction in the method.";
                break;
            case 1030:
                msg = "Stack depth is unbalanced";
                break;
            case CPOOL_INDEX_OUT_OF_RANGE:
                msg = "Cpool index " + params[0] + " is out of range " + params[1] + ".";
                break;
        }

        String result = "VerifyError: Error #" + code;
        if (debug && msg != null) {
            result += ": " + msg;
        }

        return result;
    }
}
