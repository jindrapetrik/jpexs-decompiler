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
package com.jpexs.decompiler.flash.abc.avm2.parser.pcode;

/**
 *
 * @author JPEXS
 */
public class ParsedSymbol {

    public int type;

    public Object value;

    public static final int TYPE_STRING = 1;

    public static final int TYPE_MULTINAME = 2;

    public static final int TYPE_INSTRUCTION_NAME = 3;

    public static final int TYPE_INTEGER = 4;

    public static final int TYPE_FLOAT = 5;

    public static final int TYPE_IDENTIFIER = 6;

    public static final int TYPE_EOF = 7;

    public static final int TYPE_LABEL = 8;

    public static final int TYPE_COMMENT = 9;

    public static final int TYPE_EXCEPTION_START = 10;

    public static final int TYPE_EXCEPTION_END = 11;

    public static final int TYPE_EXCEPTION_TARGET = 12;

    public static final int TYPE_KEYWORD_QNAME = 13;

    public static final int TYPE_KEYWORD_QNAMEA = 14;

    public static final int TYPE_KEYWORD_RTQNAME = 15;

    public static final int TYPE_KEYWORD_RTQNAMEA = 16;

    public static final int TYPE_KEYWORD_RTQNAMEL = 17;

    public static final int TYPE_KEYWORD_RTQNAMELA = 18;

    public static final int TYPE_KEYWORD_MULTINAME = 19;

    public static final int TYPE_KEYWORD_MULTINAMEA = 20;

    public static final int TYPE_KEYWORD_MULTINAMEL = 21;

    public static final int TYPE_KEYWORD_MULTINAMELA = 22;

    public static final int TYPE_KEYWORD_TYPENAME = 23;

    public static final int TYPE_PARENT_OPEN = 24;

    public static final int TYPE_PARENT_CLOSE = 25;

    public static final int TYPE_COMMA = 26;

    public static final int TYPE_KEYWORD_NULL = 27;

    public static final int TYPE_BRACKET_OPEN = 28;

    public static final int TYPE_BRACKET_CLOSE = 29;

    public static final int TYPE_LOWERTHAN = 30;

    public static final int TYPE_GREATERTHAN = 31;

    public static final int TYPE_KEYWORD_NAMESPACE = 32;

    public static final int TYPE_KEYWORD_PRIVATENAMESPACE = 33;

    public static final int TYPE_KEYWORD_PACKAGENAMESPACE = 34;

    public static final int TYPE_KEYWORD_PACKAGEINTERNALNS = 35;

    public static final int TYPE_KEYWORD_PROTECTEDNAMESPACE = 36;

    public static final int TYPE_KEYWORD_EXPLICITNAMESPACE = 37;

    public static final int TYPE_KEYWORD_STATICPROTECTEDNS = 38;

    public static final int TYPE_KEYWORD_TRY = 39;

    public static final int TYPE_KEYWORD_FROM = 40;

    public static final int TYPE_KEYWORD_TO = 41;

    public static final int TYPE_KEYWORD_TARGET = 42;

    public static final int TYPE_KEYWORD_TYPE = 43;

    public static final int TYPE_KEYWORD_NAME = 44;

    public static final int TYPE_KEYWORD_FLAG = 45;

    public static final int TYPE_KEYWORD_EXPLICIT = 46;

    public static final int TYPE_KEYWORD_HAS_OPTIONAL = 47;

    public static final int TYPE_KEYWORD_HAS_PARAM_NAMES = 48;

    public static final int TYPE_KEYWORD_IGNORE_REST = 49;

    public static final int TYPE_KEYWORD_NEED_ACTIVATION = 50;

    public static final int TYPE_KEYWORD_NEED_ARGUMENTS = 51;

    public static final int TYPE_KEYWORD_NEED_REST = 52;

    public static final int TYPE_KEYWORD_SET_DXNS = 53;

    public static final int TYPE_KEYWORD_PARAM = 54;

    public static final int TYPE_KEYWORD_PARAMNAME = 55;

    public static final int TYPE_KEYWORD_OPTIONAL = 56;

    public static final int TYPE_KEYWORD_RETURNS = 57;

    public static final int TYPE_KEYWORD_BODY = 58;

    public static final int TYPE_KEYWORD_MAXSTACK = 59;

    public static final int TYPE_KEYWORD_LOCALCOUNT = 60;

    public static final int TYPE_KEYWORD_INITSCOPEDEPTH = 61;

    public static final int TYPE_KEYWORD_MAXSCOPEDEPTH = 62;

    public static final int TYPE_KEYWORD_CODE = 63;

    public static final int TYPE_KEYWORD_INTEGER = 64;

    public static final int TYPE_KEYWORD_UINTEGER = 65;

    public static final int TYPE_KEYWORD_DOUBLE = 66;

    public static final int TYPE_KEYWORD_DECIMAL = 67;

    public static final int TYPE_KEYWORD_UTF8 = 68;

    public static final int TYPE_KEYWORD_TRUE = 69;

    public static final int TYPE_KEYWORD_FALSE = 70;

    public static final int TYPE_KEYWORD_UNDEFINED = 71;

    public static final int TYPE_KEYWORD_TRAIT = 72;

    public static final int TYPE_KEYWORD_SLOT = 73;

    public static final int TYPE_KEYWORD_CONST = 74;

    public static final int TYPE_KEYWORD_METHOD = 75;

    public static final int TYPE_KEYWORD_GETTER = 76;

    public static final int TYPE_KEYWORD_SETTER = 77;

    public static final int TYPE_KEYWORD_CLASS = 78;

    public static final int TYPE_KEYWORD_FUNCTION = 79;

    public static final int TYPE_KEYWORD_DISPID = 80;

    public static final int TYPE_KEYWORD_SLOTID = 81;

    public static final int TYPE_KEYWORD_VALUE = 82;

    public static final int TYPE_KEYWORD_FINAL = 83;

    public static final int TYPE_KEYWORD_METADATA = 84;

    public static final int TYPE_KEYWORD_OVERRIDE = 85;

    public static final int TYPE_KEYWORD_METADATA_BLOCK = 86;
    public static final int TYPE_KEYWORD_ITEM = 87;
    public static final int TYPE_KEYWORD_END = 88;

    public ParsedSymbol(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ParsedSymbol(int type) {
        this.type = type;
    }
}
