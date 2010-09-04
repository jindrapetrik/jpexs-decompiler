/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.parser;


public class ParsedSymbol {
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
    public static final int TYPE_CONSTANT_LITERAL=17;

    public ParsedSymbol(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ParsedSymbol(int type) {
        this.type = type;
    }

}