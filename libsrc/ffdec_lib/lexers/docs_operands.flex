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
package com.jpexs.decompiler.flash.docs;
import java.util.Stack;

%%

%public
%class DocsOperandLexer
%final
%unicode
%char
%type ParsedSymbol
%{

    StringBuilder string = new StringBuilder();
    
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

    private int count(String str, String target) {
        return (str.length() - str.replace(target, "").length()) / target.length();
    }

    ParsedSymbol last;
    public ParsedSymbol lex() throws java.io.IOException {
        ParsedSymbol ret = null;
        if (!pushedBack.isEmpty()){
            ret = last = pushedBack.pop();
        } else {
            ret = last = yylex();
        }
        return ret;
    }

%}

LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]+
Identifier = [:jletter:][:jletterdigit:]*


%%

<YYINITIAL> {
/* operators */

  "..."                          { return new ParsedSymbol(ParsedSymbol.TYPE_DOTS, yytext());  }
  ":"                            { return new ParsedSymbol(ParsedSymbol.TYPE_COLON, yytext());  }
  ","                            { return new ParsedSymbol(ParsedSymbol.TYPE_COMMA, yytext());  }
  "["                            { return new ParsedSymbol(ParsedSymbol.TYPE_BRACKET_OPEN, yytext());  }
  "]"                            { return new ParsedSymbol(ParsedSymbol.TYPE_BRACKET_CLOSE, yytext());  }  
  "|"                            { return new ParsedSymbol(ParsedSymbol.TYPE_PIPE, yytext());  }  
  "*"                            { return new ParsedSymbol(ParsedSymbol.TYPE_STAR, yytext());  }  
  {WhiteSpace}                   { /*ignore*/ }
  {Identifier}                   { return new ParsedSymbol(ParsedSymbol.TYPE_IDENTIFIER, yytext()); }    
}

/* error fallback */
[^]                              {  }
<<EOF>>                          { return new ParsedSymbol(ParsedSymbol.TYPE_EOF, null); }
