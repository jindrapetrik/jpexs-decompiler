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
package com.jpexs.decompiler.flash.importers.svg;

/**
 * SVG path reader.
 *
 * @author JPEXS
 */
public class SvgPathReader {

    private final String str;

    private int pos;

    /**
     * Constructor.
     * @param str String to read.
     */
    public SvgPathReader(String str) {
        this.str = str;
    }

    /**
     * Checks if there are more characters to read.
     * @return True if there are more characters to read.
     */
    public boolean hasNext() {
        return pos < str.length();
    }

    /**
     * Peeks the next character.
     * @return Next character.
     */
    public char peek() {
        return str.charAt(pos);
    }

    /**
     * Reads character.
     * @return Next character.
     */
    public char readChar() {
        char ch = str.charAt(pos);
        pos++;
        return ch;
    }

    /**
     * Reads command.
     * @return Next command.
     */
    public char readCommand() {
        if (!hasNext()) {
            return 0;
        }

        readWhiteSpaces();
        char ch = peek();
        char command = 0;
        if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
            command = ch;
            pos++;
            readSeparators();
        }

        return command;
    }

    private void digitSequence() {
        while (hasNext()) {
            char ch = str.charAt(pos);

            if (ch >= '0' && ch <= '9') {
                //empty
            } else {
                break;
            }

            pos++;
        }
    }

    /**
     * Reads double.
     * @return Double.
     */
    public double readDouble() {
        int startPos = pos;

        readWhiteSpaces();

        char ch;
        ch = str.charAt(pos);
        if (ch == '-') {
            pos++;
        }

        digitSequence();
        if (hasNext()) {
            ch = str.charAt(pos);
            if (ch == '.') {
                pos++;
                digitSequence();
            }
        }

        if (hasNext()) {
            ch = str.charAt(pos);
            if (ch == 'e') {
                pos++;
                ch = str.charAt(pos);
                if (ch == '-') {
                    pos++;
                }

                digitSequence();
            }
        }

        boolean ok = false;
        try {
            double result = Double.parseDouble(str.substring(startPos, pos));
            readSeparators();
            ok = true;
            return result;
        } finally {
            if (!ok) {
                pos = startPos;
            }
        }
    }

    private void readWhiteSpaces() {
        while (hasNext()) {
            char ch = peek();
            if (ch != ' ' && ch != '\r' && ch != '\n') {
                return;
            }

            readChar();
        }
    }

    private void readSeparators() {
        while (hasNext()) {
            char ch = peek();
            if (ch != ' ' && ch != ',' && ch != '\r' && ch != '\n') {
                return;
            }

            readChar();
        }
    }
}
