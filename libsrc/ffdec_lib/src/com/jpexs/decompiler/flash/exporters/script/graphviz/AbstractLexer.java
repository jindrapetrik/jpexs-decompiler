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
package com.jpexs.decompiler.flash.exporters.script.graphviz;

import java.io.Reader;

/**
 * Abstract lexer class.
 *
 * @author JPEXS
 */
public abstract class AbstractLexer {

    /**
     * Token start position
     */
    protected int tokenStart;
    /**
     * Token length
     */
    protected int tokenLength;
    /**
     * Offset
     */
    protected int offset;

    /**
     * Constructor.
     */
    public AbstractLexer() {
    }

    /**
     * Token.
     * @param type Token type
     * @param tStart Token start
     * @param tLength Token length
     * @param newStart New start
     * @param newLength New length
     * @return Token
     */
    protected Token token(TokenType type, int tStart, int tLength,
            int newStart, int newLength) {
        tokenStart = newStart;
        tokenLength = newLength;
        return new Token(type, tStart + offset, tLength);
    }

    /**
     * Token.
     * @param type Token type
     * @param start Start
     * @param length Length
     * @return Token
     */
    protected Token token(TokenType type, int start, int length) {
        return new Token(type, start + offset, length);
    }

    /**
     * Token.
     * @param type Token type
     * @return Token
     */
    protected Token token(TokenType type) {
        return new Token(type, yychar() + offset, yylength());
    }

    /**
     * Token.
     * @param type Token type
     * @param pairValue Pair value
     * @return Token
     */
    protected Token token(TokenType type, int pairValue) {
        return new Token(type, yychar() + offset, yylength(), (byte) pairValue);
    }

    /**
     * Reset.
     * @param reader Reader
     */
    public abstract void yyreset(Reader reader);

    /**
     * Lex.
     * @return Token
     * @throws java.io.IOException On I/O error
     */
    public abstract Token yylex() throws java.io.IOException;

    /**
     * Char at.
     * @param pos Position
     * @return Char
     */
    public abstract char yycharat(int pos);

    /**
     * Length.
     * @return Length
     */
    public abstract int yylength();

    /**
     * Text.
     * @return Text
     */
    public abstract String yytext();

    /**
     * Char.
     * @return Char
     */
    public abstract int yychar();
}
