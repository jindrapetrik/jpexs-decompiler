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
package com.jpexs.decompiler.flash.abc.methodinfo_parser;

/**
 *
 * @author JPEXS
 */
public class ParsedSymbol {

    public int type;

    public Object value;

    public static final int TYPE_INTEGER = 1;

    public static final int TYPE_FLOAT = 2;

    public static final int TYPE_TRUE = 3;

    public static final int TYPE_FALSE = 4;

    public static final int TYPE_NULL = 5;

    public static final int TYPE_UNDEFINED = 6;

    public static final int TYPE_STRING = 7;

    //8-12 namespace prefix
    public static final int TYPE_PRIVATE = 8;

    public static final int TYPE_PACKAGE = 9;

    public static final int TYPE_INTERNAL = 10;

    public static final int TYPE_STATIC = 11;

    public static final int TYPE_EXPLICIT = 12;

    public static final int TYPE_PROTECTED = 13;

    public static final int TYPE_NAMESPACE = 14;

    public static final int TYPE_COLON = 15;

    public static final int TYPE_COMMA = 16;

    public static final int TYPE_DOTS = 17;

    public static final int TYPE_MULTINAME = 18;

    public static final int TYPE_IDENTIFIER = 19;

    public static final int TYPE_EOF = 20;

    public static final int TYPE_STAR = 21;

    public static final int TYPE_ASSIGN = 22;

    public ParsedSymbol(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ParsedSymbol(int type) {
        this.type = type;
    }
}
