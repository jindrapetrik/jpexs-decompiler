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
/* Method info lexer specification */
package com.jpexs.decompiler.flash.abc.methodinfo_parser;

%%

%public
%class MethodInfoLexer
%final
%unicode
%char
%line
%column
%type ParsedSymbol
%throws MethodInfoParseException

%{

    StringBuilder string = new StringBuilder();
    boolean isMultiname = false;
    long multinameId = 0;

    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public MethodInfoLexer() {

    }

    public int yychar() {
        return yychar;
    }

    public int yyline() {
        return yyline + 1;
    }

%}

/* main character classes */
LineTerminator = \r|\n|\r\n

WhiteSpace = [ \t\f]+



/* identifiers */

Identifier = [:jletter:][:jletterdigit:]*

/* integer literals */
NumberLiteral = 0 | -?[1-9][0-9]*

PositiveNumberLiteral = 0 | [1-9][0-9]*

Multiname = m\[{PositiveNumberLiteral}\]

Namespace = ns\{PositiveNumberLiteral}\]
   
/* floating point literals */        
FloatLiteral =  -?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

HexDigit          = [0-9a-fA-F]

/* string and character literals */
StringCharacter = [^\r\n\"\\]

%state STRING

%%

<YYINITIAL> {  
  /* whitespace */
  {WhiteSpace}                   {  }  
  {Multiname}\"                   {
                                    isMultiname = true;
                                    String s = yytext();
                                    multinameId = Long.parseLong(s.substring(2, s.length() - 2));
                                    yybegin(STRING);
                                    string.setLength(0);
                                  }
  /* string literal */
  \"                             {
                                    isMultiname = false;
                                    yybegin(STRING);
                                    string.setLength(0);
                                 }


  /* numeric literals */

  {NumberLiteral}            { return new ParsedSymbol(ParsedSymbol.TYPE_INTEGER, Long.parseLong((yytext())));  }
  {FloatLiteral}                 { return new ParsedSymbol(ParsedSymbol.TYPE_FLOAT, Double.parseDouble((yytext())));  }

  ":"                     {return new ParsedSymbol(ParsedSymbol.TYPE_COLON);}
  ","                     {return new ParsedSymbol(ParsedSymbol.TYPE_COMMA);}
  "..."                   {return new ParsedSymbol(ParsedSymbol.TYPE_DOTS);}
  "*"                     {return new ParsedSymbol(ParsedSymbol.TYPE_STAR);}
  "="                     {return new ParsedSymbol(ParsedSymbol.TYPE_ASSIGN);}

  private               {return new ParsedSymbol(ParsedSymbol.TYPE_PRIVATE);}
  protected               {return new ParsedSymbol(ParsedSymbol.TYPE_PROTECTED);}
  package               {return new ParsedSymbol(ParsedSymbol.TYPE_PACKAGE);}
  internal               {return new ParsedSymbol(ParsedSymbol.TYPE_INTERNAL);}
  static               {return new ParsedSymbol(ParsedSymbol.TYPE_STATIC);}
  explicit               {return new ParsedSymbol(ParsedSymbol.TYPE_EXPLICIT);}
  {Namespace}           {
                                    String s = yytext();
                                    long ns = Long.parseLong(s.substring(3, s.length() - 2));
                                    return new ParsedSymbol(ParsedSymbol.TYPE_NAMESPACE, ns);
                                  }
  true                  {return new ParsedSymbol(ParsedSymbol.TYPE_TRUE);}
  false                  {return new ParsedSymbol(ParsedSymbol.TYPE_FALSE);}
  null                  {return new ParsedSymbol(ParsedSymbol.TYPE_NULL);}
  undefined                  {return new ParsedSymbol(ParsedSymbol.TYPE_UNDEFINED);}
{Identifier}            {
                        return new ParsedSymbol(ParsedSymbol.TYPE_IDENTIFIER, yytext());  }
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     if (isMultiname){
                                        return new ParsedSymbol(ParsedSymbol.TYPE_MULTINAME, multinameId);
                                     } else {
                                        return new ParsedSymbol(ParsedSymbol.TYPE_STRING, string.toString());
                                     }
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

  /* error cases */
  \\.                            { throw new MethodInfoParseException("Illegal escape sequence \"" + yytext() + "\"", yyline + 1); }
  {LineTerminator}               { throw new MethodInfoParseException("Unterminated string at end of line", yyline + 1); }

}

/* error fallback */
[^]                              { }
<<EOF>>                          { return new ParsedSymbol(ParsedSymbol.TYPE_EOF); }
