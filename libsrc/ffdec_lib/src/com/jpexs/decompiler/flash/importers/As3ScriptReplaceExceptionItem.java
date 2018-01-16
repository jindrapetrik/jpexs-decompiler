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
package com.jpexs.decompiler.flash.importers;

public class As3ScriptReplaceExceptionItem {

    private String file;
    private int line;
    private int col;
    private String message;

    public static final int COL_UNKNOWN = -1;
    public static final int LINE_UNKNOWN = -1;

    public As3ScriptReplaceExceptionItem(String file, String message, int line) {
        this(file, message, line, COL_UNKNOWN);
    }

    public As3ScriptReplaceExceptionItem(String file, String message) {
        this(file, message, LINE_UNKNOWN, COL_UNKNOWN);
    }

    public As3ScriptReplaceExceptionItem(String file, String message, int line, int col) {
        this.file = file;
        this.line = line;
        this.col = col;
        this.message = message;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return (file != null ? "" + file : "") + (line != LINE_UNKNOWN ? ("(" + line + ")") : "") + (col != COL_UNKNOWN ? (" col: " + col) : "");
    }

}
