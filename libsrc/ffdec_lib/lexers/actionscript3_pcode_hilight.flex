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
%class Flasm3Lexer
%extends AbstractLexer
%final
%unicode
%ignorecase
%char
%type Token

%{

  StringBuilder string = new StringBuilder();
  boolean isMultiname=false;


    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public Flasm3Lexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

    private static final byte PAREN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte LESSGREATER   = 3;
%}

/* main character classes */
LineTerminator = \r|\n|\r\n

WhiteSpace = [ \t\f]+

Multiname = m\[[0-9]+\]

/* identifiers */

Identifier = [:jletter:][:jletterdigit:]*

InstructionName = [:jletter:][:jletterdigit:]*

Label = {Identifier}:



/* integer literals */
NumberLiteral = (0 | -?[1-9][0-9]*) [ui]?
PositiveNumberLiteral = 0 | [1-9][0-9]*

/* floating point literals */        
FloatLiteral = -?({FLit1}|{FLit2}|{FLit3}) {Exponent}? [mdf]?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

OctDigit          = [0-7]

InputCharacter = [^\r\n]
Comment = ";" {InputCharacter}* {LineTerminator}?

/* string and character literals */
StringCharacter = [^\r\n\"\\]

ExceptionStart = "exceptionstart"{PositiveNumberLiteral}":"
ExceptionEnd = "exceptionend "{PositiveNumberLiteral}":"
ExceptionTarget = "exceptiontarget "{PositiveNumberLiteral}":"

%state STRING,PARAMETERS

%%

<YYINITIAL> {
  

  /* whitespace */
  {WhiteSpace}                   { return token(TokenType.WHITESPACE); }

  {ExceptionStart}              {
                                   return token(TokenType.KEYWORD);
                                }
  {ExceptionEnd}              {
                                   return token(TokenType.KEYWORD);
                                }
  {ExceptionTarget}              {
                                   return token(TokenType.KEYWORD);
                                }


  {Label}                        {return token(TokenType.IDENTIFIER); }
  ":"                            {return token(TokenType.OPERATOR);}
   


  

  "try"                         |
  "flag"                        |
  "param"                       |
  "paramname"                   |
  "optional"                    |
  "returns"                     |
  "body"                        |
  "maxstack"                    |
  "localcount"                  |
  "initscopedepth"              |
  "maxscopedepth"               |
  "name"                        |
  "trait"                       |
  "method"                      |
  "code"                        {  yybegin(PARAMETERS);
                                 return token(TokenType.KEYWORD);}

  /* identifiers */ 
  {InstructionName}                   { yybegin(PARAMETERS);
                                        return token(TokenType.KEYWORD); }
  {LineTerminator}               {return token(TokenType.NEWLINE);}
  {Comment}                      {return token(TokenType.COMMENT);}                                        
}

<PARAMETERS> {
    {WhiteSpace}                 { return token(TokenType.WHITESPACE); }
    "from"                       |
    "to"                         |
    "target"                     |
    "name"                       |
    "type"                       {  return token(TokenType.KEYWORD);}
    /* multinames */
  "QName"                      |
  "QNameA"                     |
  "RTQName"                    |
  "RTQNameA"                   |
  "RTQNameL"                   |
  "RTQNameLA"                  |
  "Multiname"                  |
  "MultinameL"                 |
  "MultinameLA"                |
  "TypeName"                   |
  "Unknown"                    |
  "null"                       {  return token(TokenType.KEYWORD2);}
  "("                          {  return token(TokenType.OPERATOR,PAREN); }
  ")"                          {  return token(TokenType.OPERATOR,-PAREN); }
  "["                          {  return token(TokenType.OPERATOR,BRACKET); }
  "]"                          {  return token(TokenType.OPERATOR,-BRACKET); }
  "<"                          {  return token(TokenType.OPERATOR,LESSGREATER); }
  ">"                          {  return token(TokenType.OPERATOR,-LESSGREATER); }
  "Namespace"                  |
  "PrivateNamespace"           |
  "PackageNamespace"           |
  "PackageInternalNs"          |
  "ProtectedNamespace"         |
  "ExplicitNamespace"          |
  "StaticProtectedNs"          {  return token(TokenType.KEYWORD2);}
  ","                          {  return token(TokenType.OPERATOR); }
  ":"                          {  return token(TokenType.OPERATOR); }


  /* Flag - old alias for "NATIVE" */
  "EXPLICIT"                   {  return token(TokenType.KEYWORD2);}

  /*Flags*/
  "NATIVE"                     |
  "HAS_OPTIONAL"               |
  "HAS_PARAM_NAMES"            |
  "IGNORE_REST"                |
  "NEED_ACTIVATION"            |
  "NEED_ARGUMENTS"             |
  "NEED_REST"                  |
  "SET_DXNS"                   {  return token(TokenType.KEYWORD2);}

  "dispid"                     |
  "value"                     |
  "slotid"                     {  return token(TokenType.KEYWORD);}
    

  /* Value types*/
  "Integer"                    |
  "UInteger"                   |
  "Double"                     |
  "Decimal"                    |
  "Utf8"                       |
  "True"                       |
  "False"                      |
  "Void"                       |
  "Undefined"                  {  return token(TokenType.KEYWORD2);}
   
  "FINAL"                      |
  "OVERRIDE"                   |
  "METADATA"                   {  return token(TokenType.KEYWORD2);}

  "slot"                        |
  "const"                       |
  "method"                      |
  "getter"                      |
  "setter"                      |
  "class"                       |
  "function"                    {  return token(TokenType.KEYWORD2);}


  "Number"                      |
  "int"                         |
  "uint"                        |
  "NumberContext"               |
  "CEILING"                     |
  "UP"                          |
  "HALF_UP"                     |
  "HALF_EVEN"                   |
  "HALF_DOWN"                   |
  "DOWN"                        |
  "FLOOR"                       {  return token(TokenType.KEYWORD2);  }

  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                    isMultiname=false;
                                 }
  {Multiname}\"                   {
                                    isMultiname=true;
                                    yybegin(STRING);
                                    tokenStart = yychar;
                                    tokenLength = yylength(); }

  /* numeric literals */

  {NumberLiteral}            { return token(TokenType.NUMBER);  }
  {FloatLiteral}                 { return token(TokenType.NUMBER);  }
  {Identifier}            { return token(TokenType.IDENTIFIER);  }
  {LineTerminator}      {yybegin(YYINITIAL); return token(TokenType.NEWLINE);}
  {Comment}             {yybegin(YYINITIAL); return token(TokenType.COMMENT);}
}

<STRING> {
  \"                             {
                                     yybegin(PARAMETERS);
                                     // length also includes the trailing quote
                                     if(isMultiname){
                                        return token(TokenType.IDENTIFIER, tokenStart, tokenLength + 1);//multiname
                                     }else{
                                        return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                     }
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
