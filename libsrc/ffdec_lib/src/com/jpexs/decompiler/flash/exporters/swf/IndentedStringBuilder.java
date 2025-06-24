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
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.helpers.Helper;

/**
 * Indented string builder.
 *
 * @author JPEXS
 */
public class IndentedStringBuilder {

    private final StringBuilder builder = new StringBuilder();

    private final String indentString;

    private int indent;

    /**
     * Constructor.
     *
     * @param indentString String to use for indentation
     */
    public IndentedStringBuilder(String indentString) {
        super();
        this.indentString = indentString;
    }

    /**
     * Indents.
     */
    public void indent() {
        indent++;
    }

    /**
     * Unindents.
     */
    public void unindent() {
        indent--;
    }

    /**
     * Appends a line.
     *
     * @param str String to append
     */
    public void appendLine(String str) {
        for (int i = 0; i < indent; i++) {
            builder.append(indentString);
        }

        builder.append(str);
        builder.append(Helper.newLine);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
