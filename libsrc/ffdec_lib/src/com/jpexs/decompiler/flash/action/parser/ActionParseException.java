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
package com.jpexs.decompiler.flash.action.parser;

import com.jpexs.decompiler.flash.ParseException;

/**
 * Exception for action parsing errors
 *
 * @author JPEXS
 */
public class ActionParseException extends ParseException {

    /**
     * Constructs a new parse exception.
     * @param text Text of the exception
     * @param line Line number where the exception occurred
     */
    public ActionParseException(String text, long line) {
        super(text, line);
    }
    
    /**
     * Constructs a new parse exception.
     * @param text Text of the exception
     * @param line Line number where the exception occurred
     * @param position Position where the exception occured
     */
    public ActionParseException(String text, long line, long position) {
        super(text, line, position);
    }
}
