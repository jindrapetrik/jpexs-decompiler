/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.script.graphviz;

import java.io.Reader;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractLexer {

    protected int tokenStart;
    protected int tokenLength;
    protected int offset;

    protected Token token(TokenType type, int tStart, int tLength,
            int newStart, int newLength) {
        tokenStart = newStart;
        tokenLength = newLength;
        return new Token(type, tStart + offset, tLength);
    }

    protected Token token(TokenType type, int start, int length) {
        return new Token(type, start + offset, length);
    }

    protected Token token(TokenType type) {
        return new Token(type, yychar() + offset, yylength());
    }

    protected Token token(TokenType type, int pairValue) {
        return new Token(type, yychar() + offset, yylength(), (byte) pairValue);
    }

    public abstract void yyreset(Reader reader);

    public abstract Token yylex() throws java.io.IOException;

    public abstract char yycharat(int pos);

    public abstract int yylength();

    public abstract String yytext();

    public abstract int yychar();
}
