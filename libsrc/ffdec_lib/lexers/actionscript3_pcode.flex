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
/* Flash assembler language lexer specification */
package com.jpexs.decompiler.flash.abc.avm2.parser.pcode;

import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import java.util.Stack;
%%

%public
%class Flasm3Lexer
%final
%unicode
%ignorecase
%char
%line
%column
%type ParsedSymbol
%throws AVM2ParseException

%{

    StringBuilder string = new StringBuilder();
    boolean isMultiname = false;
    long multinameId = 0;

    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public Flasm3Lexer() {

    }

    public int yychar() {
        return yychar;
    }

    public int yyline() {
        return yyline + 1;
    }

    private Stack<ParsedSymbol> pushedBack = new Stack<>();

    public void pushback(ParsedSymbol symb) {
        pushedBack.push(symb);
        last = null;
    }

    ParsedSymbol last;
    public ParsedSymbol lex() throws java.io.IOException, AVM2ParseException{
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
Comment = ";" {InputCharacter}*

WhiteSpace = [ \t\f]+

Multiname = m\[[0-9]+\]

/* identifiers */

Identifier = [:jletter:][:jletterdigit:]*

InstructionName = [:jletter:][:jletterdigit:]*

Label = {Identifier}:



/* integer literals */
NumberLiteral = 0 | -?[1-9][0-9]*

PositiveNumberLiteral = 0 | [1-9][0-9]*
   
/* floating point literals */        
FloatLiteral =  -?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

HexDigit          = [0-9a-fA-F]
OctDigit          = [0-7]

/* string and character literals */
StringCharacter = [^\r\n\"\\]

ExceptionStart = "exceptionstart "{PositiveNumberLiteral}":"
ExceptionEnd = "exceptionend "{PositiveNumberLiteral}":"
ExceptionTarget = "exceptiontarget "{PositiveNumberLiteral}":"

%state STRING,PARAMETERS

%%

<YYINITIAL> {
  

  /* whitespace */
  {WhiteSpace}                   {  }

  {ExceptionStart}              {
                                   String s=yytext();
                                   return new ParsedSymbol(ParsedSymbol.TYPE_EXCEPTION_START, Integer.parseInt(s.substring(15, s.length() - 1)));
                                }
  {ExceptionEnd}              {
                                   String s=yytext();
                                   return new ParsedSymbol(ParsedSymbol.TYPE_EXCEPTION_END, Integer.parseInt(s.substring(13, s.length() - 1)));
                                }
  {ExceptionTarget}              {
                                   String s=yytext();
                                   return new ParsedSymbol(ParsedSymbol.TYPE_EXCEPTION_TARGET,Integer.parseInt(s.substring(16, s.length() - 1)));
                                }
  {Label}                        {
                                    String s = yytext();
                                    return new ParsedSymbol(ParsedSymbol.TYPE_LABEL, s.substring(0, s.length() - 1));
                                }
  "name"                        {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_NAME, yytext());}  
  "try"                         {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_TRY, yytext());}
  "flag"                        {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_FLAG, yytext());}
  "param"                       {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_PARAM, yytext());}
  "paramname"                   {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_PARAMNAME, yytext());}
  "optional"                    {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_OPTIONAL, yytext());}
  "returns"                     {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_RETURNS, yytext());}
  "body"                        {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_BODY, yytext());}
  "maxstack"                    {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_MAXSTACK, yytext());}
  "localcount"                  {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_LOCALCOUNT, yytext());}
  "initscopedepth"              {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_INITSCOPEDEPTH, yytext());}
  "maxscopedepth"               {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_MAXSCOPEDEPTH, yytext());}
  "code"                        {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_CODE, yytext());}
  "trait"                       {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_TRAIT, yytext());}
  "method"                      {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_METHOD, yytext());}
  "metadata"                    {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_METADATA_BLOCK, yytext());}
  "item"                        {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_ITEM, yytext());}
  "end"                         {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_END, yytext());}
  
/*in params too:*/
  "dispid"                      {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_DISPID, yytext());}
  "slotid"                      {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_SLOTID, yytext());}
  "value"                       {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_VALUE, yytext());}
  "type"                        {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_TYPE, yytext());}
  

  
  /* identifiers */ 
  {InstructionName}                   { yybegin(PARAMETERS);
                                        return new ParsedSymbol(ParsedSymbol.TYPE_INSTRUCTION_NAME, yytext());
                                      }
  {Comment}                     {return new ParsedSymbol(ParsedSymbol.TYPE_COMMENT, yytext().substring(1));}
}

<PARAMETERS> {
  /* string literal */
  \"                             {
                                    isMultiname = false;
                                    yybegin(STRING);
                                    string.setLength(0);
                                 }
  {Multiname}\"                   {
                                    isMultiname = true;
                                    String s = yytext();
                                    multinameId = Long.parseLong(s.substring(2, s.length() - 2));
                                    yybegin(STRING);
                                    string.setLength(0);
                                  }
  /* multinames */
  "QName"                      {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_QNAME, yytext());}
  "QNameA"                     {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_QNAMEA, yytext());}
  "RTQName"                    {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_RTQNAME, yytext());}
  "RTQNameA"                   {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_RTQNAMEA, yytext());}
  "RTQNameL"                   {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_RTQNAMEL, yytext());}
  "RTQNameLA"                  {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_RTQNAMELA, yytext());}
  "Multiname"                  {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_MULTINAME, yytext());}
  "MultinameA"                 {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_MULTINAMEA, yytext());}
  "MultinameL"                 {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_MULTINAMEL, yytext());}
  "MultinameLA"                {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_MULTINAMELA, yytext());}
  "TypeName"                   {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_TYPENAME, yytext());}
  "null"                       {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_NULL, yytext());}
  "("                          {  return new ParsedSymbol(ParsedSymbol.TYPE_PARENT_OPEN, yytext());}
  ")"                          {  return new ParsedSymbol(ParsedSymbol.TYPE_PARENT_CLOSE, yytext());}
  "["                          {  return new ParsedSymbol(ParsedSymbol.TYPE_BRACKET_OPEN, yytext());}
  "]"                          {  return new ParsedSymbol(ParsedSymbol.TYPE_BRACKET_CLOSE, yytext());}
  "<"                          {  return new ParsedSymbol(ParsedSymbol.TYPE_LOWERTHAN, yytext());}
  ">"                          {  return new ParsedSymbol(ParsedSymbol.TYPE_GREATERTHAN, yytext());}
  "Namespace"                  {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_NAMESPACE, yytext());}
  "PrivateNamespace"           {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_PRIVATENAMESPACE, yytext());}
  "PackageNamespace"           {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_PACKAGENAMESPACE, yytext());}
  "PackageInternalNs"          {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_PACKAGEINTERNALNS, yytext());}
  "ProtectedNamespace"         {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_PROTECTEDNAMESPACE, yytext());}
  "ExplicitNamespace"          {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_EXPLICITNAMESPACE, yytext());}
  "StaticProtectedNs"          {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_STATICPROTECTEDNS, yytext());}
  ","                          {  return new ParsedSymbol(ParsedSymbol.TYPE_COMMA, yytext());}

  /*Try*/
  "from"                       {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_FROM, yytext());}
  "to"                         {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_TO, yytext());}
  "target"                     {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_TARGET, yytext());}
  "name"                       {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_NAME, yytext());}
  "type"                       {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_TYPE, yytext());}
  
  "slot"                        {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_SLOT, yytext());}
  "const"                       {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_CONST, yytext());}
  "method"                      {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_METHOD, yytext());}  
  "getter"                      {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_GETTER, yytext());}
  "setter"                      {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_SETTER, yytext());}
  "class"                       {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_CLASS, yytext());}
  "function"                    {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_FUNCTION, yytext());}
  "dispid"                      {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_DISPID, yytext());}
  "slotid"                      {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_SLOTID, yytext());}
  "value"                       {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_VALUE, yytext());}
  "flag"                        {  yybegin(PARAMETERS); return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_FLAG, yytext());}
  


   /*Flags*/
  "EXPLICIT"                   {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_EXPLICIT, yytext());}
  "HAS_OPTIONAL"               {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_HAS_OPTIONAL, yytext());}
  "HAS_PARAM_NAMES"            {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_HAS_PARAM_NAMES, yytext());}
  "IGNORE_REST"                {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_IGNORE_REST, yytext());}
  "NEED_ACTIVATION"            {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_NEED_ACTIVATION, yytext());}
  "NEED_ARGUMENTS"             {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_NEED_ARGUMENTS, yytext());}
  "NEED_REST"                  {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_NEED_REST, yytext());}
  "SET_DXNS"                   {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_SET_DXNS, yytext());}

  /* Value types*/
  "Integer"                    {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_INTEGER, yytext());}
  "UInteger"                   {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_UINTEGER, yytext());}
  "Double"                     {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_DOUBLE, yytext());}
  "Decimal"                    {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_DECIMAL, yytext());}
  "Utf8"                       {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_UTF8, yytext());}
  "True"                       {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_TRUE, yytext());}
  "False"                      {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_FALSE, yytext());}
  "Undefined"                  {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_UNDEFINED, yytext());}
   

  "FINAL"                      {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_FINAL, yytext());}
  "OVERRIDE"                   {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_OVERRIDE, yytext());}
  "METADATA"                   {  return new ParsedSymbol(ParsedSymbol.TYPE_KEYWORD_METADATA, yytext());}
  
  /* numeric literals */

  {NumberLiteral}            { return new ParsedSymbol(ParsedSymbol.TYPE_INTEGER, Long.parseLong((yytext())));  }
  {FloatLiteral}                 { return new ParsedSymbol(ParsedSymbol.TYPE_FLOAT, Double.parseDouble((yytext())));  }
  {Identifier}            { return new ParsedSymbol(ParsedSymbol.TYPE_IDENTIFIER, yytext());  }
  {LineTerminator}      {yybegin(YYINITIAL);}
  {Comment}             {return new ParsedSymbol(ParsedSymbol.TYPE_COMMENT, yytext().substring(1));}
}

<STRING> {
  \"                             {
                                     yybegin(PARAMETERS);
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
  \\.                            { throw new AVM2ParseException("Illegal escape sequence \"" + yytext() + "\"", yyline + 1); }
  {LineTerminator}               { throw new AVM2ParseException("Unterminated string at end of line", yyline + 1); }

}

/* error fallback */
[^]                              { }
<<EOF>>                          { return new ParsedSymbol(ParsedSymbol.TYPE_EOF); }
