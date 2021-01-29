/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.script.graphviz;

/**
 *
 * @author jindr
 */
public enum TokenType {

    OPERATOR, // Language operators
    DELIMITER, // Delimiters.  Constructs that are not necessarily operators for a language
    KEYWORD, // language reserved keywords
    KEYWORD2, // Other language reserved keywords, like C #defines
    IDENTIFIER, // identifiers, variable names, class names
    NUMBER, // numbers in various formats
    STRING, // String
    STRING2, // For highlighting meta chars within a String
    COMMENT, // comments
    COMMENT2, // special stuff within comments
    REGEX, // regular expressions
    REGEX2, // special chars within regular expressions
    TYPE, // Types, usually not keywords, but supported by the language
    TYPE2, // Types from standard libraries
    TYPE3, // Types for users
    DEFAULT, // any other text
    WARNING, // Text that should be highlighted as a warning    
    ERROR, // Text that signals an error
    WHITESPACE,
    NEWLINE;
}
