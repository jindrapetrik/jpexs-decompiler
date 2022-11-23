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
package com.jpexs.helpers.properties;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

%%

%public
%class PropertiesLexer
%final
%unicode
%char
%type ParsedSymbol
%throws PropertiesParseException

%{

    private String sourceCode;
    public PropertiesLexer(String sourceCode){
        this(new StringReader(sourceCode));
        this.sourceCode = sourceCode;
    }

    public void yypushbackstr(String s, int state)
    {
        sourceCode = s + sourceCode.substring(yychar + yylength());
        yyreset(new StringReader(sourceCode));
        yybegin(state);
    }

    public void yypushbackstr(String s)
    {
        yypushbackstr(s, YYINITIAL);
    }

    StringBuilder string = new StringBuilder();

    private static String xmlTagName = "";

    public int yychar() {
        return yychar;
    }

    private Stack<ParsedSymbol> pushedBack = new Stack<>();

    public int yyline() {
        return yyline + 1;
    }  

    public void pushback(ParsedSymbol symb) {
        pushedBack.push(symb);
        last = null;
    }

    ParsedSymbol last;
    public ParsedSymbol lex() throws java.io.IOException, PropertiesParseException{
        ParsedSymbol ret = null;
        if (!pushedBack.isEmpty()){
            ret = last = pushedBack.pop();
        } else {
            ret = last = yylex();
        }
        return ret;
    }

    private int count(String str, String target) {
        return (str.length() - str.replace(target, "").length()) / target.length();
    }
%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = [ \t\f]+

NonWhiteSpaceChar = [^ \t\f]

Separator = {WhiteSpace} | ({WhiteSpace}? [:=] {WhiteSpace}?)
NewLineEscape = "\\" {LineTerminator} {WhiteSpace}?

Comment = [#!] {InputCharacter}*

UnicodeEscape = "\\" u[0-9a-fA-F]{4}

/*return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.BREAK, yytext());*/
%state COMMENT, KEY, VALUE

%%

<YYINITIAL> {
  {WhiteSpace}                   {}
  {Comment}                      {  yybegin(COMMENT);
                                    return new ParsedSymbol(SymbolType.COMMENT, yytext().substring(1));}
  {LineTerminator}               { return new ParsedSymbol(SymbolType.EMPTY_LINE, null); }
  {NonWhiteSpaceChar}            {string.setLength(0); string.append(yytext()); yybegin(KEY);}
  
}

<COMMENT> {
    {LineTerminator}               {yybegin(YYINITIAL);}
}

<KEY> {
  "\\t"                          { string.append("\\t"); }
  "\\f"                          { string.append("\\f"); }
  "\\r"                          { string.append("\\r"); }
  "\\n"                          { string.append("\\n"); }
  "\\\\"                         { string.append("\\\\"); }
  "\\ "                          { string.append(" "); }
  "\\!"                          { string.append("!"); }
  "\\#"                          { string.append("#"); }
  "\\="                          { string.append("="); }
  "\\:"                          { string.append(":"); } 
  {UnicodeEscape}                { string.append((char)Integer.parseInt(yytext().substring(2), 16));}
  {Separator}                    { String key = string.toString();
                                   yybegin(VALUE);
                                   string.setLength(0);
                                   return new ParsedSymbol(SymbolType.KEY, key);}
  {NonWhiteSpaceChar}            { string.append(yytext());}   
}

<VALUE> {
  "\\t"                          { string.append("\\t"); }
  "\\f"                          { string.append("\\f"); }
  "\\r"                          { string.append("\\r"); }
  "\\n"                          { string.append("\\n"); }
  "\\\\"                         { string.append("\\\\"); }
  "\\ "                          { string.append(" "); }
  {UnicodeEscape}                { string.append((char)Integer.parseInt(yytext().substring(2), 16));}
  {NewLineEscape}                { string.append("\r\n"); }
  {LineTerminator}               {yybegin(YYINITIAL);
                                  return new ParsedSymbol(SymbolType.VALUE, string.toString());}
  {InputCharacter}               { string.append(yytext());}
  <<EOF>>                        {yybegin(YYINITIAL);
                                  return new ParsedSymbol(SymbolType.VALUE, string.toString());}
}

/* error fallback */
[^]                              {  }
<<EOF>>                          { return new ParsedSymbol(SymbolType.EOF, null); }
