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
package com.jpexs.decompiler.flash.docs;

/**
 * Parsed symbol in documentation.
 *
 * @author JPEXS
 */
public class ParsedSymbol {

    /**
     * End of file
     */
    public static int TYPE_EOF = 0;
    /**
     * Identifier
     */
    public static int TYPE_IDENTIFIER = 1;
    /**
     * Colon
     */
    public static int TYPE_COLON = 2;
    /**
     * Open bracket
     */
    public static int TYPE_BRACKET_OPEN = 3;
    /**
     * Close bracket
     */
    public static int TYPE_BRACKET_CLOSE = 4;
    /**
     * Dots
     */
    public static int TYPE_DOTS = 5;
    /**
     * Comma
     */
    public static int TYPE_COMMA = 6;
    /**
     * Pipe
     */
    public static int TYPE_PIPE = 7;
    /**
     * Star
     */
    public static int TYPE_STAR = 8;

    /**
     * Type
     */
    public final int type;

    /**
     * Value
     */
    public final String value;

    /**
     * Constructor.
     *
     * @param type Type
     * @param value Value
     */
    public ParsedSymbol(int type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "symbol[type=" + type + ", value=" + value + "]";
    }
}
