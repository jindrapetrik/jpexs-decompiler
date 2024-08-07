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
package com.jpexs.decompiler.flash;

/**
 * Parse exception.
 *
 * @author JPEXS
 */
public abstract class ParseException extends Exception {

    /**
     * Line number where the exception occurred.
     */
    public long line;

    /**
     * Text of the exception.
     */
    public String text;

    /**
     * Constructs a new parse exception.
     *
     * @param text Text of the exception
     * @param line Line number where the exception occurred
     */
    public ParseException(String text, long line) {
        super("ParseException:" + text + " on line " + line);
        this.line = line;
        this.text = text;
    }
}
