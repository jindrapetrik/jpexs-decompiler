/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.methodinfoparser;

/**
 * ABC method info P-code parser symbol.
 *
 * @author JPEXS
 */
public class ParsedSymbol {

    /**
     * Type
     */
    public int type;

    /**
     * Value
     */
    public Object value;

    /**
     * Type: Number
     */
    public static final int TYPE_NUMBER = 1;

    /**
     * Type: True
     */
    public static final int TYPE_TRUE = 3;

    /**
     * Type: False
     */
    public static final int TYPE_FALSE = 4;

    /**
     * Type: Null
     */
    public static final int TYPE_NULL = 5;

    /**
     * Type: Undefined
     */
    public static final int TYPE_UNDEFINED = 6;

    /**
     * Type: String
     */
    public static final int TYPE_STRING = 7;

    //8-12 namespace prefix

    /**
     * Type: Private
     */
    public static final int TYPE_PRIVATE = 8;

    /**
     * Type: Package
     */
    public static final int TYPE_PACKAGE = 9;

    /**
     * Type: Internal
     */
    public static final int TYPE_INTERNAL = 10;

    /**
     * Type: Static
     */
    public static final int TYPE_STATIC = 11;

    /**
     * Type: Explicit
     */
    public static final int TYPE_EXPLICIT = 12;

    /**
     * Type: Protected
     */
    public static final int TYPE_PROTECTED = 13;

    /**
     * Type: Namespace
     */
    public static final int TYPE_NAMESPACE = 14;

    /**
     * Type: Colon
     */
    public static final int TYPE_COLON = 15;

    /**
     * Type: Comma
     */
    public static final int TYPE_COMMA = 16;

    /**
     * Type: Dots
     */
    public static final int TYPE_DOTS = 17;

    /**
     * Type: Multiname
     */
    public static final int TYPE_MULTINAME = 18;

    /**
     * Type: Identifier
     */
    public static final int TYPE_IDENTIFIER = 19;

    /**
     * Type: End of file
     */
    public static final int TYPE_EOF = 20;

    /**
     * Type: Star
     */
    public static final int TYPE_STAR = 21;

    /**
     * Type: Assign
     */
    public static final int TYPE_ASSIGN = 22;

    /**
     * Constructor.
     * @param type Type
     * @param value Value
     */
    public ParsedSymbol(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Constructor.
     * @param type Type
     */
    public ParsedSymbol(int type) {
        this.type = type;
    }
}
