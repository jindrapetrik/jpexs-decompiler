/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.parser.pcode;

/**
 * ActionScript 1-2 P-code parsed symbol.
 *
 * @author JPEXS
 */
public class ASMParsedSymbol {

    /**
     * Position
     */
    public int pos;

    /**
     * Type
     */
    public int type;

    /**
     * Value
     */
    public Object value;

    /**
     * Type: String
     */
    public static final int TYPE_STRING = 1;

    /**
     * Type: Block end
     */
    public static final int TYPE_BLOCK_END = 2;

    /**
     * Type: Instruction name
     */
    public static final int TYPE_INSTRUCTION_NAME = 3;

    /**
     * Type: Integer
     */
    public static final int TYPE_INTEGER = 4;

    /**
     * Type: Float
     */
    public static final int TYPE_FLOAT = 5;

    /**
     * Type: Boolean
     */
    public static final int TYPE_BOOLEAN = 11;

    /**
     * Type: Identifier
     */
    public static final int TYPE_IDENTIFIER = 6;

    /**
     * Type: End of file
     */
    public static final int TYPE_EOF = 7;

    /**
     * Type: Label
     */
    public static final int TYPE_LABEL = 8;

    /**
     * Type: Comment
     */
    public static final int TYPE_COMMENT = 9;

    /**
     * Type: Block start
     */
    public static final int TYPE_BLOCK_START = 10;

    /**
     * Type: Register
     */
    public static final int TYPE_REGISTER = 12;

    /**
     * Type: Constant
     */
    public static final int TYPE_CONSTANT = 13;

    /**
     * Type: Null
     */
    public static final int TYPE_NULL = 14;

    /**
     * Type: Undefined
     */
    public static final int TYPE_UNDEFINED = 15;

    /**
     * Type: End of line
     */
    public static final int TYPE_EOL = 16;

    /***
     * Type: Constant literal
     */
    public static final int TYPE_CONSTANT_LITERAL = 17;

    /***
     * Type: Comma
     */
    public static final int TYPE_COMMA = 18;

    /**
     * Constructor.
     * @param pos Position
     * @param type Type
     * @param value Value
     */
    public ASMParsedSymbol(int pos, int type, Object value) {
        this.pos = pos;
        this.type = type;
        this.value = value;
    }

    /**
     * Constructor.
     * @param pos Position
     * @param type Type
     */
    public ASMParsedSymbol(int pos, int type) {
        this.pos = pos;
        this.type = type;
    }

    @Override
    public String toString() {
        return "symbol[type=" + type + ", value=" + value + "]";
    }
}
