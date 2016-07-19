/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.importers.amf.amf3;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

%%

%public
%class Amf3Lexer
%final
%unicode
%char
%type ParsedSymbol
%throws Amf3ParseException

%{

    StringBuilder string = new StringBuilder();

    private static String xmlTagName = "";

    public int yychar() {
        return yychar;
    }

    private final Stack<ParsedSymbol> pushedBack = new Stack<>();

    public int yyline() {
        return yyline + 1;
    }

    public void pushback(ParsedSymbol symb) {
        pushedBack.push(symb);
        last = null;
    }

    ParsedSymbol last;
    public ParsedSymbol lex() throws java.io.IOException, Amf3ParseException{
        ParsedSymbol ret = null;
        if (!pushedBack.isEmpty()){
            ret = last = pushedBack.pop();
        } else {
            ret = last = yylex();
        }
        return ret;
    }

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]+

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?



/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

Reference = "#" {Identifier}

/* integer literals */
DecIntegerLiteral = 0 | "-"? [1-9][0-9]*

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctDigit          = [0-7]

/* floating point literals */
DoubleLiteral = "-"? ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]

%state STRING, CHARLITERAL

%%

<YYINITIAL> {

  /* keywords */
  "null"                        { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.NULL, yytext()); }
  "undefined"                   { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.UNDEFINED, yytext()); }
  "false"                       { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.FALSE, yytext()); }
  "true"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.TRUE, yytext()); }
  "unknown"                        { return new ParsedSymbol(SymbolGroup.UNKNOWN, SymbolType.UNKNOWN, yytext()); }


  /* operators */

  "{"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.CURLY_OPEN, yytext());  }
  "}"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.CURLY_CLOSE, yytext());  }
  "["                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.BRACKET_OPEN, yytext());  }
  "]"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.BRACKET_CLOSE, yytext());  }
  ","                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.COMMA, yytext());  }
  ":"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.COLON, yytext());  }

  /* string literal */
  \"                             {
                                    string.setLength(0);
                                    yybegin(STRING);
                                 }


  /* numeric literals */

  {DecIntegerLiteral}            { return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Long.parseLong((yytext()))); }

  {HexIntegerLiteral}            { return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Long.parseLong(yytext().substring(2), 16)); }

  {OctIntegerLiteral}            { return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Long.parseLong(yytext(), 8)); }

  {DoubleLiteral}                { return new ParsedSymbol(SymbolGroup.DOUBLE, SymbolType.DOUBLE, Double.parseDouble((yytext()))); }

  /* comments */
  {Comment}                      { /*ignore*/ }

  {LineTerminator}               { yyline++;}
  /* whitespace */
  {WhiteSpace}                   { /*ignore*/ }

  /* identifiers */
  {Reference}                         { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.REFERENCE, yytext().substring(1));  }
  {Identifier}                   { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.IDENTIFIER, yytext()); }
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return new ParsedSymbol(SymbolGroup.STRING, SymbolType.STRING, string.toString());
                                 }

  {StringCharacter}+             { string.append(yytext()); }

  /* escape sequences */
  "\\b"                          { string.append('\b'); }
  "\\t"                          { string.append('\t'); }
  "\\n"                          { string.append('\n'); }
  "\\f"                          { string.append('\f'); }
  "\\r"                          { string.append('\r'); }
  "\\\""                         { string.append('\"'); }
  "\\'"                          { string.append('\''); }
  "\\\\"                         { string.append('\\'); }
  \\x{HexDigit}{2}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }
  \\u{HexDigit}{4}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }
  \\{OctDigit}{3}         { char val = (char) Integer.parseInt(yytext().substring(1), 8);
                        				   string.append(val); }

  /* escape sequences */

  \\.                            { string.append('\\'); /*illegal escape sequence*/  }
  {LineTerminator}               { yybegin(YYINITIAL);  yyline++;}
}

/* error fallback */
[^]                              {  }
<<EOF>>                          { return new ParsedSymbol(SymbolGroup.EOF, SymbolType.EOF, null); }

