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
package com.jpexs.decompiler.flash.action.parser.pcode;

/**
 *
 * @author JPEXS
 */
public class ASMParsedSymbol {

    public int type;

    public Object value;

    public static final int TYPE_STRING = 1;

    public static final int TYPE_BLOCK_END = 2;

    public static final int TYPE_INSTRUCTION_NAME = 3;

    public static final int TYPE_INTEGER = 4;

    public static final int TYPE_FLOAT = 5;

    public static final int TYPE_BOOLEAN = 11;

    public static final int TYPE_IDENTIFIER = 6;

    public static final int TYPE_EOF = 7;

    public static final int TYPE_LABEL = 8;

    public static final int TYPE_COMMENT = 9;

    public static final int TYPE_BLOCK_START = 10;

    public static final int TYPE_REGISTER = 12;

    public static final int TYPE_CONSTANT = 13;

    public static final int TYPE_NULL = 14;

    public static final int TYPE_UNDEFINED = 15;

    public static final int TYPE_EOL = 16;

    public static final int TYPE_CONSTANT_LITERAL = 17;

    public ASMParsedSymbol(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ASMParsedSymbol(int type) {
        this.type = type;
    }
}
