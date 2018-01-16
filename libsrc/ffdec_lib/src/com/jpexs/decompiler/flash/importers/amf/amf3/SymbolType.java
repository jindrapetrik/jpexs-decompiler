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
package com.jpexs.decompiler.flash.importers.amf.amf3;

/**
 *
 * @author JPEXS
 */
public enum SymbolType {
    NULL,
    UNDEFINED,
    FALSE,
    TRUE,
    CURLY_OPEN,
    CURLY_CLOSE,
    BRACKET_OPEN,
    BRACKET_CLOSE,
    COMMA,
    COLON,
    UNKNOWN,
    REFERENCE,
    IDENTIFIER,
    INTEGER,
    STRING,
    DOUBLE,
    EOF
}
