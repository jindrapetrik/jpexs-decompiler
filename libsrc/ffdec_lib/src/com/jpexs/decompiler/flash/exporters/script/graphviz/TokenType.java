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
package com.jpexs.decompiler.flash.exporters.script.graphviz;

/**
 * TokenType
 *
 * @author JPEXS
 */
public enum TokenType {

    /**
     * Language operators
     */
    OPERATOR,
    /**
     * Delimiters.  Constructs that are not necessarily operators for a language
     */
    DELIMITER,
    /**
     * Language reserved keywords
     */
    KEYWORD,
    /**
     * Other language reserved keywords, like C #defines
     */
    KEYWORD2,
    /**
     * Identifiers, variable names, class names
     */
    IDENTIFIER,
    /**
     * Numbers in various formats
     */
    NUMBER,
    /**
     * String
     */
    STRING,
    /**
     * For highlighting meta chars within a String
     */
    STRING2,
    /**
     * Comments
     */
    COMMENT,
    /**
     * Special stuff within comments
     */
    COMMENT2,
    /**
     * Regular expressions
     */
    REGEX,
    /**
     * Special chars within regular expressions
     */
    REGEX2,
    /**
     * Types, usually not keywords, but supported by the language
     */
    TYPE,
    /**
     * Types from standard libraries
     */
    TYPE2,
    /**
     * Types for users
     */
    TYPE3,
    /**
     * Any other text
     */
    DEFAULT,
    /**
     * Text that should be highlighted as a warning
     */
    WARNING,
    /**
     * Text that signals an error
     */
    ERROR,
    /**
     * Whitespace
     */
    WHITESPACE,
    /**
     * Newline
     */
    NEWLINE;
}
