/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.helpers.utf8.charset;

import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.action.parser.script.ParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.script.SymbolType;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractCharsetConverter {

    protected static void readMap(Map<Integer, Integer> data, ActionScriptLexer lexer) throws IOException, ActionParseException {
        ParsedSymbol s;
        lexer.lex(); //identifier;
        lexer.lex(); //=
        lexer.lex(); // {
        int pos1 = 0;
        do {
            s = lexer.lex(); //{                
            if (s.type == SymbolType.CURLY_CLOSE) {
                break;
            }
            s = lexer.lex();
            int key = (int) (long) (Long) s.value;
            lexer.lex(); //,
            s = lexer.lex();
            int value = (int) (long) (Long) s.value;
            data.put(key, value);
            s = lexer.lex(); //}                
            s = lexer.lex();
            pos1++;
        } while ((s.type == SymbolType.COMMA));
        lexer.lex(); //;
    }

    protected static void readOneDimensionalInt(int[] data, ActionScriptLexer lexer) throws IOException, ActionParseException {
        ParsedSymbol s;
        lexer.lex(); //identifier
        lexer.lex(); //=
        lexer.lex(); // {
        int pos = 0;
        do {
            s = lexer.lex();
            if (s.type == SymbolType.CURLY_CLOSE) {
                break;
            }
            boolean negative = false;
            if (s.type == SymbolType.MINUS) {
                negative = true;
                s = lexer.lex();
            }
            data[pos] = (int) (long) (Long) s.value;
            if (negative) {
                data[pos] = -data[pos];
            }
            s = lexer.lex();
            pos++;
        } while (s.type == SymbolType.COMMA);
        lexer.lex(); //;
    }

    protected static void readTwoDimensionalInt(int[][] data, ActionScriptLexer lexer) throws IOException, ActionParseException {
        ParsedSymbol s;
        lexer.lex(); //identifier;
        lexer.lex(); //=
        lexer.lex(); // {
        int pos1 = 0;
        do {
            s = lexer.lex(); //{
            int pos2 = 0;
            do {
                s = lexer.lex();
                if (s.type == SymbolType.CURLY_CLOSE) {
                    break;
                }
                boolean negative = false;
                if (s.type == SymbolType.MINUS) {
                    negative = true;
                    s = lexer.lex();
                }
                data[pos1][pos2] = (int) (long) (Long) s.value;
                if (negative) {
                    data[pos1][pos2] = -data[pos1][pos2];
                }
                s = lexer.lex();
                pos2++;
            } while (s.type == SymbolType.COMMA);
            s = lexer.lex();
            pos1++;
        } while ((s.type == SymbolType.COMMA));
        //lexer.lex(); // }
        lexer.lex(); //;
    }

    public abstract int toUnicode(int codePoint);

    public abstract int fromUnicode(int codePoint);
}
