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
package com.jpexs.decompiler.flash.tags.text;


%%

%public
%class TextLexer
%final
%unicode
%char
%line
%column
%type ParsedSymbol
%throws TextParseException

%{

    private boolean finish = false;
    private boolean parameter = false;

    private StringBuilder string = null;

    private int repeatNum = 1;

    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public TextLexer() {

    }

    public int yychar() {
        return yychar;
    }

    public int yyline() {
        return yyline + 1;
    }

%}

Parameter = [a-z_][a-z0-9_]*
Value = [^ \r\n\]\"\u00A7]+
Divider = [ \r\n]+
HexDigit          = [0-9a-fA-F]
StringCharacter = [^\r\n\"\\]
OIdentifierCharacter = [^\r\n\u00A7\\]
DecIntegerLiteral = (0 | [1-9][0-9]*) [ui]?
LineTerminator = \r|\n|\r\n
PositiveNumberLiteral = 0 | [1-9][0-9]*

%state PARAMETER,VALUE,OIDENTIFIER,STRING

%%

<YYINITIAL> {
  "["                            {
                                    parameter = true;
                                    yybegin(PARAMETER);
                                    if (string != null){
                                        String ret = string.toString();
                                        string = null;
                                        return new ParsedSymbol(SymbolType.TEXT, ret);
                                    }
                                 }
  /* escape sequences */
  "\\["                          { if (string == null) string = new StringBuilder(); string.append('['); }
  "\\]"                          { if (string == null) string = new StringBuilder(); string.append(']'); }
  "\\b"                          { if (string == null) string = new StringBuilder(); string.append('\b'); }
  "\\t"                          { if (string == null) string = new StringBuilder(); string.append('\t'); }
  "\\n"                          { if (string == null) string = new StringBuilder(); string.append('\n'); }
  "\\f"                          { if (string == null) string = new StringBuilder(); string.append('\f'); }
  "\\r"                          { if (string == null) string = new StringBuilder(); string.append('\r'); }
  "\\\""                         { if (string == null) string = new StringBuilder(); string.append('\"'); }
  "\\'"                          { if (string == null) string = new StringBuilder(); string.append('\''); }
  "\\\\"                         { if (string == null) string = new StringBuilder(); string.append('\\'); }
  \\x{HexDigit}{HexDigit}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }

  /* error cases */
  \\.                            { throw new TextParseException("Illegal escape sequence \"" + yytext() + "\"", yyline + 1); }   
  .                              { if (string == null) string = new StringBuilder(); string.append(yytext()); }
 <<EOF>>                         { if (finish) {return null;} else {finish=true; return new ParsedSymbol(SymbolType.TEXT, string == null ? null : string.toString());}}
}

<PARAMETER> {
    {Divider}                    {}
    {Parameter}                  {
                                    yybegin(VALUE);
                                    return new ParsedSymbol(SymbolType.PARAMETER_IDENTIFIER, yytext());
                                 }
    "]"                          {
                                    yybegin(YYINITIAL);
                                    parameter = false;
                                 }
}

<VALUE> {
    {Divider}                          {}

    \"                           {
                                    string = new StringBuilder();
                                    yybegin(STRING);
                                 }

    "\u00A7"                     {
                                    string = new StringBuilder();
                                    yybegin(OIDENTIFIER);
                                 }
    {Parameter}                  {
                                    return new ParsedSymbol(SymbolType.PARAMETER_IDENTIFIER, yytext());
                                 }
    {Value}                      {
                                    return new ParsedSymbol(SymbolType.PARAMETER_VALUE, yytext());
                                 }
    "]"                          {
                                    yybegin(YYINITIAL);
                                    parameter = false;
                                 }
}

<OIDENTIFIER> {
    "\u00A7"                         {
                                     yybegin(VALUE);
                                     repeatNum = 1;
                                     // length also includes the trailing quote
                                     String ret = string.toString();
                                     string = null;
                                     return new ParsedSymbol(SymbolType.PARAMETER_VALUE, ret);
                                 }

  {OIdentifierCharacter}         { for(int r=0;r<repeatNum;r++) string.append(yytext()); repeatNum = 1;}

  /* escape sequences */
  "\\b"                          { for(int r=0;r<repeatNum;r++) string.append('\b'); repeatNum = 1;}
  "\\t"                          { for(int r=0;r<repeatNum;r++) string.append('\t'); repeatNum = 1;}
  "\\n"                          { for(int r=0;r<repeatNum;r++) string.append('\n'); repeatNum = 1;}
  "\\f"                          { for(int r=0;r<repeatNum;r++) string.append('\f'); repeatNum = 1;}
  "\\\u00A7"                     { for(int r=0;r<repeatNum;r++) string.append('\u00A7'); repeatNum = 1;}
  "\\r"                          { for(int r=0;r<repeatNum;r++) string.append('\r'); repeatNum = 1;}
  "\\\\"                         { for(int r=0;r<repeatNum;r++) string.append('\\'); repeatNum = 1;}
  \\x{HexDigit}{2}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   for(int r=0;r<repeatNum;r++) string.append(val); repeatNum = 1;}
  \\u{HexDigit}{4}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   for(int r=0;r<repeatNum;r++) string.append(val); repeatNum = 1;}
  \\\{{DecIntegerLiteral}\}      { repeatNum = Integer.parseInt(yytext().substring(2, yytext().length()-1)); }

  /* escape sequences */

  \\.                            { throw new TextParseException("Illegal escape sequence \"" + yytext() + "\"", yyline + 1);  }
  {LineTerminator}               { yybegin(VALUE);  yyline++;}
}

<STRING> {
  \"                             {
                                     yybegin(VALUE);
                                     repeatNum = 1;
                                     // length also includes the trailing quote
                                     String tos = string.toString();
                                     string = null;
                                     return new ParsedSymbol(SymbolType.PARAMETER_VALUE, tos);
                                 }

  {StringCharacter}             { for(int r=0;r<repeatNum;r++) string.append(yytext()); repeatNum = 1; }

  /* escape sequences */
  "\\b"                          { for(int r=0;r<repeatNum;r++) string.append('\b'); repeatNum = 1;}
  "\\t"                          { for(int r=0;r<repeatNum;r++) string.append('\t'); repeatNum = 1;}
  "\\n"                          { for(int r=0;r<repeatNum;r++) string.append('\n'); repeatNum = 1;}
  "\\f"                          { for(int r=0;r<repeatNum;r++) string.append('\f'); repeatNum = 1;}
  "\\\u00A7"                     { for(int r=0;r<repeatNum;r++) string.append('\u00A7'); repeatNum = 1;}
  "\\r"                          { for(int r=0;r<repeatNum;r++) string.append('\r'); repeatNum = 1;}
  "\\\""                         { for(int r=0;r<repeatNum;r++) string.append('\"'); repeatNum = 1;}
  "\\'"                          { for(int r=0;r<repeatNum;r++) string.append('\''); repeatNum = 1;}
  "\\\\"                         { for(int r=0;r<repeatNum;r++) string.append('\\'); repeatNum = 1;}
  \\x{HexDigit}{2}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   for(int r=0;r<repeatNum;r++) string.append(val); repeatNum = 1; }
  \\u{HexDigit}{4}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   for(int r=0;r<repeatNum;r++) string.append(val); repeatNum = 1; }
  \\\{{PositiveNumberLiteral}\}      { repeatNum = Integer.parseInt(yytext().substring(2, yytext().length()-1)); }


  /* error cases */
  \\.                            { repeatNum = 1; /* ignore illegal character escape */ }
}

/* error fallback */
[^]                              { if (!parameter) { if (string == null) string = new StringBuilder(); string.append(yytext()); } }
<<EOF>>                          { return null; }
