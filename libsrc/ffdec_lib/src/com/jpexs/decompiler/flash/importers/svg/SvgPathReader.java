/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
 *
 * @author JPEXS
 */
public class SvgPathReader {

    private String str;

    private int pos;

    public SvgPathReader(String str) {
        this.str = str;
    }

    public boolean hasNext() {
        return pos < str.length();
    }

    public char peek() {
        return str.charAt(pos);
    }

    public char readChar() {
        char ch = str.charAt(pos);
        pos++;
        return ch;
    }

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

    public double readDouble() {
        int startPos = pos;

        readWhiteSpaces();
        if (peek() == '-') {
            pos++;
        }

        boolean pointFound = false;
        while (hasNext()) {
            char ch = str.charAt(pos);
            if (ch == '.') {
                if (pointFound) {
                    break;
                }

                pointFound = true;
            } else if (ch >= '0' && ch <= '9') {
            } else {
                break;
            }

            pos++;
        }

        double result = Double.parseDouble(str.substring(startPos, pos));
        readSeparators();
        return result;
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
