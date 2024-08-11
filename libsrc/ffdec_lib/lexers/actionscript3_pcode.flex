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

    private int repeatNum = 1;
    private int stringPos = 0;

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
NumberLiteral = (0 | -?[1-9][0-9]*) [ui]?

PositiveNumberLiteral = 0 | [1-9][0-9]*
   
/* floating point literals */        
FloatLiteral =  -?({FLit1}|{FLit2}|{FLit3}) {Exponent}? [mdf]?

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
                                   return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_EXCEPTION_START, Integer.parseInt(s.substring(15, s.length() - 1)));
                                }
  {ExceptionEnd}              {
                                   String s=yytext();
                                   return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_EXCEPTION_END, Integer.parseInt(s.substring(13, s.length() - 1)));
                                }
  {ExceptionTarget}              {
                                   String s=yytext();
                                   return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_EXCEPTION_TARGET,Integer.parseInt(s.substring(16, s.length() - 1)));
                                }
  {Label}                        {
                                    String s = yytext();
                                    return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_LABEL, s.substring(0, s.length() - 1));
                                }
  "name"                        {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NAME, yytext());}  
  "try"                         {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_TRY, yytext());}
  "flag"                        {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FLAG, yytext());}
  "param"                       {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_PARAM, yytext());}
  "paramname"                   {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_PARAMNAME, yytext());}
  "optional"                    {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_OPTIONAL, yytext());}
  "returns"                     {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_RETURNS, yytext());}
  "body"                        {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_BODY, yytext());}
  "maxstack"                    {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_MAXSTACK, yytext());}
  "localcount"                  {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_LOCALCOUNT, yytext());}
  "initscopedepth"              {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_INITSCOPEDEPTH, yytext());}
  "maxscopedepth"               {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_MAXSCOPEDEPTH, yytext());}
  "code"                        {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_CODE, yytext());}
  "trait"                       {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_TRAIT, yytext());}
  "method"                      {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_METHOD, yytext());}
  "metadata"                    {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_METADATA_BLOCK, yytext());}
  "item"                        {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_ITEM, yytext());}
  "instance"                    {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_INSTANCE, yytext());}
  "extends"                     {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_EXTENDS, yytext());}
  "implements"                  {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_IMPLEMENTS, yytext());}
  "protectedns"                 {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_PROTECTEDNS_BLOCK, yytext());}
  "end"                         {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_END, yytext());}
  
/*in params too:*/
  "class"                       {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_CLASS, yytext());}
  "dispid"                      {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_DISPID, yytext());}
  "slotid"                      {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_SLOTID, yytext());}
  "value"                       {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_VALUE, yytext());}
  "type"                        {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_TYPE, yytext());}
  

  
  /* identifiers */ 
  {InstructionName}                   { yybegin(PARAMETERS);
                                        return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_INSTRUCTION_NAME, yytext());
                                      }
  {Comment}                     {return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_COMMENT, yytext().substring(1));}
}

<PARAMETERS> {
  /* string literal */
  \"                             {
                                    isMultiname = false;
                                    stringPos = yychar();
                                    yybegin(STRING);
                                    string.setLength(0);
                                 }
  {Multiname}\"                   {
                                    isMultiname = true;
                                    String s = yytext();
                                    stringPos = yychar();
                                    multinameId = Long.parseLong(s.substring(2, s.length() - 2));
                                    yybegin(STRING);
                                    string.setLength(0);
                                  }
  /* multinames */
  "QName"                      {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_QNAME, yytext());}
  "QNameA"                     {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_QNAMEA, yytext());}
  "RTQName"                    {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_RTQNAME, yytext());}
  "RTQNameA"                   {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_RTQNAMEA, yytext());}
  "RTQNameL"                   {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_RTQNAMEL, yytext());}
  "RTQNameLA"                  {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_RTQNAMELA, yytext());}
  "Multiname"                  {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_MULTINAME, yytext());}
  "MultinameA"                 {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_MULTINAMEA, yytext());}
  "MultinameL"                 {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_MULTINAMEL, yytext());}
  "MultinameLA"                {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_MULTINAMELA, yytext());}
  "TypeName"                   {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_TYPENAME, yytext());}
  "Unknown"                    {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_UNKNOWN, yytext()); }
  "null"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NULL, yytext());}
  "("                          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_PARENT_OPEN, yytext());}
  ")"                          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_PARENT_CLOSE, yytext());}
  "["                          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_BRACKET_OPEN, yytext());}
  "]"                          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_BRACKET_CLOSE, yytext());}
  "<"                          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_LOWERTHAN, yytext());}
  ">"                          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_GREATERTHAN, yytext());}
  "Namespace"                  {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NAMESPACE, yytext());}
  "PrivateNamespace"           {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_PRIVATENAMESPACE, yytext());}
  "PackageNamespace"           {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_PACKAGENAMESPACE, yytext());}
  "PackageInternalNs"          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_PACKAGEINTERNALNS, yytext());}
  "ProtectedNamespace"         {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_PROTECTEDNAMESPACE, yytext());}
  "ExplicitNamespace"          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_EXPLICITNAMESPACE, yytext());}
  "StaticProtectedNs"          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_STATICPROTECTEDNS, yytext());}
  ","                          {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_COMMA, yytext());}

  /*Try*/
  "from"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FROM, yytext());}
  "to"                         {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_TO, yytext());}
  "target"                     {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_TARGET, yytext());}
  "name"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NAME, yytext());}
  "type"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_TYPE, yytext());}
  "end"                        {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_END, yytext());} 
  
  "slot"                        {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_SLOT, yytext());}
  "const"                       {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_CONST, yytext());}
  "method"                      {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_METHOD, yytext());}  
  "getter"                      {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_GETTER, yytext());}
  "setter"                      {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_SETTER, yytext());}
  "class"                       {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_CLASS, yytext());}
  "function"                    {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FUNCTION, yytext());}
  "dispid"                      {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_DISPID, yytext());}
  "slotid"                      {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_SLOTID, yytext());}
  "value"                       {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_VALUE, yytext());}
  "flag"                        {  yybegin(PARAMETERS); return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FLAG, yytext());}
  

  /* Flag - old alias for "NATIVE" */
  "EXPLICIT"                   {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NATIVE, yytext());}  

   /*Flags*/
  "NATIVE"                     {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NATIVE, yytext());}  
  "HAS_OPTIONAL"               {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_HAS_OPTIONAL, yytext());}
  "HAS_PARAM_NAMES"            {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_HAS_PARAM_NAMES, yytext());}
  "IGNORE_REST"                {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_IGNORE_REST, yytext());}
  "NEED_ACTIVATION"            {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NEED_ACTIVATION, yytext());}
  "NEED_ARGUMENTS"             {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NEED_ARGUMENTS, yytext());}
  "NEED_REST"                  {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NEED_REST, yytext());}
  "SET_DXNS"                   {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_SET_DXNS, yytext());}

  /* Value types*/
  "Integer"                    {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_INTEGER, yytext());}
  "UInteger"                   {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_UINTEGER, yytext());}
  "Double"                     {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_DOUBLE, yytext());}
  "Decimal"                    {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_DECIMAL, yytext());}
  "Float"                      {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FLOAT, yytext());}
  "Float4"                     {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FLOAT4, yytext());}
  "Utf8"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_UTF8, yytext());}
  "True"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_TRUE, yytext());}
  "False"                      {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FALSE, yytext());}
  "Void"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_VOID, yytext());}
  "Undefined"                  {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_VOID, yytext());}
   

  "FINAL"                      {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FINAL, yytext());}
  "OVERRIDE"                   {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_OVERRIDE, yytext());}
  "METADATA"                   {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_METADATA, yytext());}
  "SEALED"                     {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_SEALED, yytext());}
  "INTERFACE"                  {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_INTERFACE, yytext());}
  "PROTECTEDNS"                {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_PROTECTEDNS, yytext());}
  "NON_NULLABLE"               {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NON_NULLABLE, yytext());}
  
  "Number"                     {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NUMBER, yytext());}
  "int"                        {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_INT, yytext());}
  "uint"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_UINT, yytext());}
  "NumberContext"              {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_NUMBERCONTEXT, yytext());}
  "CEILING"                    {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_CEILING, yytext());}
  "UP"                         {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_UP, yytext());}
  "HALF_UP"                    {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_HALF_UP, yytext());}
  "HALF_EVEN"                  {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_HALF_EVEN, yytext());}
  "HALF_DOWN"                  {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_HALF_DOWN, yytext());}
  "DOWN"                       {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_DOWN, yytext());}
  "FLOOR"                      {  return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_KEYWORD_FLOOR, yytext());}  

  /* numeric literals */

  {NumberLiteral}            {                                 
                                return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_NUMBER, yytext());                                  
                             }
  {FloatLiteral}             { return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_NUMBER, yytext());  }
  {Identifier}            { return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_IDENTIFIER, yytext());  }
  {LineTerminator}      {yybegin(YYINITIAL);}
  {Comment}             {return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_COMMENT, yytext().substring(1));}
}

<STRING> {
  \"                             {
                                     yybegin(PARAMETERS);
                                     repeatNum = 1;
                                     // length also includes the trailing quote
                                     if (isMultiname){
                                        return new ParsedSymbol(stringPos, ParsedSymbol.TYPE_MULTINAME, multinameId);
                                     } else {
                                        return new ParsedSymbol(stringPos, ParsedSymbol.TYPE_STRING, string.toString());
                                     }
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
  \\.                            { repeatNum = 1; throw new AVM2ParseException("Illegal escape sequence \"" + yytext() + "\"", yyline + 1); }
  {LineTerminator}               { repeatNum = 1; throw new AVM2ParseException("Unterminated string at end of line", yyline + 1); }

}

/* error fallback */
[^]                              { }
<<EOF>>                          { return new ParsedSymbol(yychar(), ParsedSymbol.TYPE_EOF); }
