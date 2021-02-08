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
 * License along with this library.
 */

package com.jpexs.decompiler.flash.exporters.script.graphviz;


%%

%public
%class FlasmLexer
%extends AbstractLexer
%final
%unicode
%char
%type Token

%{

  StringBuilder string = new StringBuilder();


    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public FlasmLexer() {

    }

    public int yychar() {
        return yychar;
    }

%}

/* main character classes */
LineTerminator = \r|\n|\r\n

InputCharacter = [^\r\n]
Comment = ";" {InputCharacter}*

WhiteSpace = [ \t\f]+


/* identifiers */

Identifier = [:jletter:][:jletterdigit:]*

InstructionName = [:jletter:][:jletterdigit:]*

Label = {Identifier}:

StartOfBlock = "{"

EndOfBlock = "}"

True = "true"
False = "false"
False = "false"
Null = "null"
Undefined = "undefined"



/* integer literals */
NumberLiteral = 0 | -?[1-9][0-9]*
   
/* floating point literals */        
FloatLiteral = -?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

OctDigit          = [0-7]

/* string and character literals */
StringCharacter = [^\r\n\"\\]

Register= register{NumberLiteral}
Constant= constant{NumberLiteral}

%state STRING,PARAMETERS

%%

<YYINITIAL> {
  

  /* whitespace */
  {WhiteSpace}                   { return token(TokenType.WHITESPACE);  }

  {Label}                        {
                                    return token(TokenType.IDENTIFIER,yychar,yylength());
                                }

  /* identifiers */ 
  {InstructionName}                   { yybegin(PARAMETERS);
                                        return token(TokenType.KEYWORD);
                                      }
  {Comment}                           {return token(TokenType.COMMENT);}
  {EndOfBlock}                        {return token(TokenType.WHITESPACE);}
  {LineTerminator}              {return token(TokenType.NEWLINE);}
}

<PARAMETERS> {
  {WhiteSpace}                   { return token(TokenType.WHITESPACE); }
  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                 }

  /* numeric literals */

  {NumberLiteral}            { return token(TokenType.NUMBER);  }
  {FloatLiteral}                 { return token(TokenType.NUMBER);  }
  {LineTerminator}      {yybegin(YYINITIAL); return token(TokenType.NEWLINE);}
  {Comment}             {return token(TokenType.COMMENT);}
  {StartOfBlock}        {return token(TokenType.WHITESPACE);}
  {True}                {return token(TokenType.KEYWORD);}
  {False}                {return token(TokenType.KEYWORD);}
  {Null}                {return token(TokenType.KEYWORD);}
  {Undefined}                {return token(TokenType.KEYWORD);}

  {Register}              { return token(TokenType.KEYWORD2); }
  {Constant}              { return token(TokenType.KEYWORD2);  }
  {Identifier}            { return token(TokenType.IDENTIFIER);  }
      
}

<STRING> {
  \"                             {
                                     yybegin(PARAMETERS);
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                 }

{StringCharacter}+             { tokenLength += yylength(); }

  \\[0-3]?{OctDigit}?{OctDigit}  { tokenLength += yylength(); }

  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);
                                   return token(TokenType.ERROR,tokenStart, tokenLength);}
}

/* error fallback */
[^]                              { }
<<EOF>>                          { return null; }
