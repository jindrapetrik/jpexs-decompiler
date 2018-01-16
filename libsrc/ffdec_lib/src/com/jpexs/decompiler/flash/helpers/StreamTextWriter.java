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
import com.jpexs.helpers.utf8.Utf8OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class StreamTextWriter extends GraphTextWriter implements AutoCloseable {

    private final Writer writer;

    private boolean newLine = true;

    private int indent;

    private int writtenBytes;

    public StreamTextWriter(CodeFormatting formatting, OutputStream os) {
        super(formatting);
        this.writer = new Utf8OutputStreamWriter(new BufferedOutputStream(os));
    }

    @Override
    public GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue, HighlightData data) {
        writeToOutputStream(text);
        return this;
    }

    @Override
    public StreamTextWriter append(String str) {
        writeToOutputStream(str);
        return this;
    }

    @Override
    public GraphTextWriter appendWithData(String str, HighlightData data) {
        writeToOutputStream(str);
        return this;
    }

    @Override
    public StreamTextWriter append(String str, long offset, long fileOffset) {
        writeToOutputStream(str);
        return this;
    }

    @Override
    public StreamTextWriter appendNoHilight(int i) {
        writeToOutputStream(Integer.toString(i));
        return this;
    }

    @Override
    public StreamTextWriter appendNoHilight(String str) {
        writeToOutputStream(str);
        return this;
    }

    @Override
    public StreamTextWriter indent() {
        indent++;
        return this;
    }

    @Override
    public StreamTextWriter unindent() {
        indent--;
        return this;
    }

    @Override
    public StreamTextWriter newLine() {
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
        try {
            writer.write(str);
            writtenBytes += str.length();
        } catch (IOException ex) {
            Logger.getLogger(StreamTextWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void appendIndent() {
        for (int i = 0; i < indent; i++) {
            writeToOutputStream(formatting.indentString);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
