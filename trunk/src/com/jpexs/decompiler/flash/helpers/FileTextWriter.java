/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.helpers.utf8.Utf8OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class FileTextWriter extends GraphTextWriter implements AutoCloseable {

    private final Writer writer;
    private boolean newLine = true;
    private int indent;
    private int writtenBytes;

    public FileTextWriter(FileOutputStream fos) {
        this.writer = new BufferedWriter(new Utf8OutputStreamWriter(fos));
    }

    @Override
    public FileTextWriter hilightSpecial(String text, String type) {
        writeToFile(text);
        return this;
    }

    @Override
    public FileTextWriter hilightSpecial(String text, String type, int index) {
        writeToFile(text);
        return this;
    }

    @Override
    public FileTextWriter append(String str) {
        writeToFile(str);
        return this;
    }

    @Override
    public FileTextWriter append(String str, long offset) {
        writeToFile(str);
        return this;
    }

    @Override
    public FileTextWriter appendNoHilight(int i) {
        writeToFile(Integer.toString(i));
        return this;
    }

    @Override
    public FileTextWriter appendNoHilight(String str) {
        writeToFile(str);
        return this;
    }

    @Override
    public FileTextWriter indent() {
        indent++;
        return this;
    }

    @Override
    public FileTextWriter unindent() {
        indent--;
        return this;
    }

    @Override
    public FileTextWriter newLine() {
        writeToFile(NEW_LINE);
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

    private void writeToFile(String str) {
        if (newLine) {
            newLine = false;
            appendIndent();
        }
        try {
            writer.write(str);
            writtenBytes += str.length();
        } catch (IOException ex) {
            Logger.getLogger(FileTextWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void appendIndent() {
        for (int i = 0; i < indent; i++) {
            writeToFile(INDENT_STRING);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
