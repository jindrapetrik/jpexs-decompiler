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
package com.jpexs.decompiler.flash.importers.svg.css;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

%%

%public
%class CssLexer
%final
%unicode
%char
%ignorecase
%type CssParsedSymbol

%{

    StringBuilder string = new StringBuilder();

    private static String xmlTagName = "";

    public int yychar() {
        return yychar;
    }

    private final Stack<CssParsedSymbol> pushedBack = new Stack<>();

    public int yyline() {
        return yyline + 1;
    }

    private boolean doBuffer = false;
    private List<CssParsedSymbol> buffer = new ArrayList<>();

    public void startBuffer(){
        doBuffer = true;
    }

    public String getAndClearBuffer(){
        StringBuilder sb = new StringBuilder();
        for(CssParsedSymbol s:buffer){
            sb.append(s.value);
        }
        buffer.clear();
        doBuffer = false;
        return sb.toString();
    }
    
    public int getPos(){
        int pos = yychar() + yylength();
        for(CssParsedSymbol p:pushedBack) {
            pos -= p.value.length();
        }
        return pos;
    }

    public void pushback(CssParsedSymbol symb) {
        pushedBack.push(symb);
        if(!buffer.isEmpty()){
            buffer.remove(buffer.size()-1);
        }
        last = null;
    }

    CssParsedSymbol last;
    public CssParsedSymbol lex() throws java.io.IOException {
        CssParsedSymbol ret = null;
        if (!pushedBack.isEmpty()){
            ret = last = pushedBack.pop();
        } else {
            ret = last = yylex();
        }
        if (doBuffer){
            buffer.add(ret);
        }
        return ret;
    }

%}


h=		[0-9a-fA-F]
nonascii=	[\240-\377]
unicode=		\\{h}{1,6}(\r\n|[ \t\r\n\f])?
escape=		{unicode}|\\[^\r\n\f0-9a-fA-F]
nmstart=		[_a-zA-Z]|{nonascii}|{escape}
nmchar=		[_a-zA-Z0-9-]|{nonascii}|{escape}
string1=		"\"" ([^\n\r\f\\\"]|\\{nl}|{escape})* "\""
string2=		\'([^\n\r\f\\']|\\{nl}|{escape})*\'
badstring1=      "\"" ([^\n\r\f\\\"]|\\{nl}|{escape})*\\?
badstring2=      \'([^\n\r\f\\']|\\{nl}|{escape})*\\?
badcomment1=     \/\*[^*]*\*+([^/*][^*]*\*+)*
badcomment2=     \/\*[^*]*(\*+[^/*][^*]*)*
baduri1=         {U}{R}{L}\({w}([!#$%&*-\[\]-~]|{nonascii}|{escape})*{w}
baduri2=         {U}{R}{L}\({w}{string}{w}
baduri3=         {U}{R}{L}\({w}{badstring}
comment=		\/\*[^*]*\*+([^/*][^*]*\*+)*\/
ident=		-?{nmstart}{nmchar}*
name=		{nmchar}+
num=		[-+]?[0-9]+|[-+]?[0-9]*"."[0-9]+
string=		{string1}|{string2}
badstring=       {badstring1}|{badstring2}
badcomment=      {badcomment1}|{badcomment2}
baduri=          {baduri1}|{baduri2}|{baduri3}
url=		([!#$%&*-~]|{nonascii}|{escape})*
s=		[ \t\r\n\f]+
w=		{s}?
nl=		\n|\r\n|\r|\f

A=		A|a|\\0{0,4}(41|61)(\r\n|[ \t\r\n\f])?
C=		C|c|\\0{0,4}(43|63)(\r\n|[ \t\r\n\f])?
D=		D|d|\\0{0,4}(44|64)(\r\n|[ \t\r\n\f])?
E=		E|e|\\0{0,4}(45|65)(\r\n|[ \t\r\n\f])?
G=		G|g|\\0{0,4}(47|67)(\r\n|[ \t\r\n\f])?|\\g
H=		H|h|\\0{0,4}(48|68)(\r\n|[ \t\r\n\f])?|\\h
I=		I|i|\\0{0,4}(49|69)(\r\n|[ \t\r\n\f])?|\\i
K=		K|k|\\0{0,4}(4b|6b)(\r\n|[ \t\r\n\f])?|\\k
L=              L|l|\\0{0,4}(4c|6c)(\r\n|[ \t\r\n\f])?|\\l
M=		M|m|\\0{0,4}(4d|6d)(\r\n|[ \t\r\n\f])?|\\m
N=		N|n|\\0{0,4}(4e|6e)(\r\n|[ \t\r\n\f])?|\\n
O=		O|o|\\0{0,4}(4f|6f)(\r\n|[ \t\r\n\f])?|\\o
P=		P|p|\\0{0,4}(50|70)(\r\n|[ \t\r\n\f])?|\\p
R=		R|r|\\0{0,4}(52|72)(\r\n|[ \t\r\n\f])?|\\r
S=		S|s|\\0{0,4}(53|73)(\r\n|[ \t\r\n\f])?|\\s
T=		T|t|\\0{0,4}(54|74)(\r\n|[ \t\r\n\f])?|\\t
U=              U|u|\\0{0,4}(55|75)(\r\n|[ \t\r\n\f])?|\\u
X=		X|x|\\0{0,4}(58|78)(\r\n|[ \t\r\n\f])?|\\x
Z=		Z|z|\\0{0,4}(5a|7a)(\r\n|[ \t\r\n\f])?|\\z

%%

<YYINITIAL> {

  {s}			{return new CssParsedSymbol(yytext(), CssSymbolType.S);}

\/\*[^*]*\*+([^/*][^*]*\*+)*\/		/* ignore comments */
{badcomment}                         /* unclosed comment at EOF */

"<!--"		{return new CssParsedSymbol(yytext(), CssSymbolType.CDO);}
"-->"			{return new CssParsedSymbol(yytext(), CssSymbolType.CDC);}
"~="			{return new CssParsedSymbol(yytext(), CssSymbolType.INCLUDES);}
"|="			{return new CssParsedSymbol(yytext(), CssSymbolType.DASHMATCH);}

{string}		{return new CssParsedSymbol(yytext(), CssSymbolType.STRING);}
{badstring}             {return new CssParsedSymbol(yytext(), CssSymbolType.BAD_STRING);}

{ident}			{return new CssParsedSymbol(yytext(), CssSymbolType.IDENT);}

"#"{name}		{return new CssParsedSymbol(yytext(), CssSymbolType.HASH);}

@{I}{M}{P}{O}{R}{T}	{return new CssParsedSymbol(yytext(), CssSymbolType.IMPORT_SYM);}
@{P}{A}{G}{E}		{return new CssParsedSymbol(yytext(), CssSymbolType.PAGE_SYM);}
@{M}{E}{D}{I}{A}	{return new CssParsedSymbol(yytext(), CssSymbolType.MEDIA_SYM);}
"@charset "		{return new CssParsedSymbol(yytext(), CssSymbolType.CHARSET_SYM);}

"!"({w}|{comment})*{I}{M}{P}{O}{R}{T}{A}{N}{T}	{return new CssParsedSymbol(yytext(), CssSymbolType.IMPORTANT_SYM);}

{num}{E}{M}		{return new CssParsedSymbol(yytext(), CssSymbolType.EMS);}
{num}{E}{X}		{return new CssParsedSymbol(yytext(), CssSymbolType.EXS);}
{num}{P}{X}		{return new CssParsedSymbol(yytext(), CssSymbolType.LENGTH);}
{num}{C}{M}		{return new CssParsedSymbol(yytext(), CssSymbolType.LENGTH);}
{num}{M}{M}		{return new CssParsedSymbol(yytext(), CssSymbolType.LENGTH);}
{num}{I}{N}		{return new CssParsedSymbol(yytext(), CssSymbolType.LENGTH);}
{num}{P}{T}		{return new CssParsedSymbol(yytext(), CssSymbolType.LENGTH);}
{num}{P}{C}		{return new CssParsedSymbol(yytext(), CssSymbolType.LENGTH);}
{num}{D}{E}{G}		{return new CssParsedSymbol(yytext(), CssSymbolType.ANGLE);}
{num}{R}{A}{D}		{return new CssParsedSymbol(yytext(), CssSymbolType.ANGLE);}
{num}{G}{R}{A}{D}	{return new CssParsedSymbol(yytext(), CssSymbolType.ANGLE);}
{num}{M}{S}		{return new CssParsedSymbol(yytext(), CssSymbolType.TIME);}
{num}{S}		{return new CssParsedSymbol(yytext(), CssSymbolType.TIME);}
{num}{H}{Z}		{return new CssParsedSymbol(yytext(), CssSymbolType.FREQ);}
{num}{K}{H}{Z}		{return new CssParsedSymbol(yytext(), CssSymbolType.FREQ);}
{num}{ident}		{return new CssParsedSymbol(yytext(), CssSymbolType.DIMENSION);}

{num}%			{return new CssParsedSymbol(yytext(), CssSymbolType.PERCENTAGE);}
{num}			{return new CssParsedSymbol(yytext(), CssSymbolType.NUMBER);}

{U}{R}{L}"("{w}{string}{w}")" {return new CssParsedSymbol(yytext(), CssSymbolType.URI);}
{U}{R}{L}"("{w}{url}{w}")"    {return new CssParsedSymbol(yytext(), CssSymbolType.URI);}
{baduri}                {return new CssParsedSymbol(yytext(), CssSymbolType.BAD_URI);}

{ident}"("		{return new CssParsedSymbol(yytext(), CssSymbolType.FUNCTION);}
{comment}               {}
.                       {return new CssParsedSymbol(yytext(), CssSymbolType.OTHER);}
}

<<EOF>>                          { return new CssParsedSymbol("", CssSymbolType.EOF); }
