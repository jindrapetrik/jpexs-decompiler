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
package com.jpexs.decompiler.flash.types.annotations.parser;

import java.util.Stack;

%%

%public
%class ConditionLexer
%final
%unicode
%char
%line
%column
%type ConditionToken
%throws AnnotationParseException

%{

    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public ConditionLexer() {

    }

    public int yychar() {
        return yychar;
    }

    public int yyline() {
        return yyline + 1;
    }

    private Stack<ConditionToken> pushedBack = new Stack<>();

    public void pushback(ConditionToken symb) {
        pushedBack.push(symb);
    }

    public ConditionToken lex() throws java.io.IOException, AnnotationParseException{
        ConditionToken ret = null;
        if (!pushedBack.isEmpty()){
            ret = pushedBack.pop();
        } else {
            ret = yylex();
        }
        return ret;
    }

%}

Field = [A-Za-z0-9_\.]+

%%

<YYINITIAL> {
  {Field}                        {
                                    return new ConditionToken(ConditionTokenType.FIELD, yytext());
                                 }  
   "||" | "|"                    {  return new ConditionToken(ConditionTokenType.OR, yytext()); }     
   "&&" | "," | "&"              {  return new ConditionToken(ConditionTokenType.AND, yytext()); } 
   "!"                           {  return new ConditionToken(ConditionTokenType.NOT, yytext()); } 
   "("                           {  return new ConditionToken(ConditionTokenType.PARENT_OPEN, yytext()); }
   ")"                           {  return new ConditionToken(ConditionTokenType.PARENT_CLOSE, yytext()); }
   .                             { }
 <<EOF>>                         {return null;}
}