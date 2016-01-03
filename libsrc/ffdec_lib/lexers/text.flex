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

    StringBuilder string = null;
    boolean finish = false;
    boolean parameter = false;
    String parameterName = null;

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

Parameter = [a-z0-9_]+
Value = [^ \r\n\]]+
Divider = [ \r\n]+
HexDigit          = [0-9a-fA-F]

%state PARAMETER,VALUE

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
    {Divider}                          {}
    {Parameter}                  {
                                    parameterName = yytext();
                                    yybegin(VALUE);
                                 }
    "]"                          {
                                    yybegin(YYINITIAL);
                                    parameter = false;
                                 }
}

<VALUE> {
    {Divider}                          {}
    {Value}                      {  
                                    yybegin(PARAMETER);                                    
                                    return new ParsedSymbol(SymbolType.PARAMETER, new Object[] {parameterName, yytext()});
                                 }
    "]"                          {
                                    yybegin(YYINITIAL);
                                    parameter = false;
                                 }
}

/* error fallback */
[^]                              { if (!parameter) { if (string == null) string = new StringBuilder(); string.append(yytext()); } }
<<EOF>>                          { return null; }
