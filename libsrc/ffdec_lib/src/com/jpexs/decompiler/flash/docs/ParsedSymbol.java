/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
 *
 * @author JPEXS
 */
public class ParsedSymbol {
    public static int TYPE_EOF = 0;
    public static int TYPE_IDENTIFIER = 1;
    public static int TYPE_COLON = 2;
    public static int TYPE_BRACKET_OPEN = 3;
    public static int TYPE_BRACKET_CLOSE = 4;
    public static int TYPE_DOTS = 5;
    public static int TYPE_COMMA = 6;
    public static int TYPE_PIPE = 7;
    public static int TYPE_STAR = 8;
    
    public final int type;
    public final String value;

    public ParsedSymbol(int type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "symbol[type=" + type + ", value=" + value + "]";
    }        
}
