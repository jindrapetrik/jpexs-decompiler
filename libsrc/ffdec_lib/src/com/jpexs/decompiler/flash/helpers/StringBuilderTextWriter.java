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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class StringBuilderTextWriter extends GraphTextWriter {

    private final StringBuilder writer;

    private boolean newLine = true;

    private int indent;

    private int writtenBytes;

    public StringBuilderTextWriter(CodeFormatting formatting, StringBuilder writer) {
        super(formatting);
        this.writer = writer;
    }

    @Override
    public GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue, HighlightData data) {
        writeToOutputStream(text);
        return this;
    }

    @Override
    public StringBuilderTextWriter append(String str) {
        writeToOutputStream(str);
        return this;
    }

    @Override
    public GraphTextWriter appendWithData(String str, HighlightData data) {
        writeToOutputStream(str);
        return this;
    }

    @Override
    public StringBuilderTextWriter append(String str, long offset, long fileOffset) {
        writeToOutputStream(str);
        return this;
    }

    @Override
    public StringBuilderTextWriter appendNoHilight(int i) {
        writeToOutputStream(Integer.toString(i));
        return this;
    }

    @Override
    public StringBuilderTextWriter appendNoHilight(String str) {
        writeToOutputStream(str);
        return this;
    }

    @Override
    public StringBuilderTextWriter indent() {
        indent++;
        return this;
    }

    @Override
    public StringBuilderTextWriter unindent() {
        indent--;
        return this;
    }

    @Override
    public StringBuilderTextWriter newLine() {
        writeToOutputStream(formatting.newLineChars);
        newLine = true;
        return this;
    }

    @Override
    public int getLength() {
        return writtenBytes;
    }

    @Override
    public int getIndent() {
        return indent;
    }

    private void writeToOutputStream(String str) {
        if (newLine) {
            newLine = false;
            appendIndent();
        }
        writer.append(str);
        writtenBytes += str.length();

    }

    private void appendIndent() {
        for (int i = 0; i < indent; i++) {
            writeToOutputStream(formatting.indentString);
        }
    }

}
