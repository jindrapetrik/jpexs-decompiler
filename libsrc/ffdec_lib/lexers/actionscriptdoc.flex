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
package com.jpexs.decompiler.flash.asdoc;
import java.io.StringReader;

%%

%public
%class ActionScriptDocLexer
%final
%unicode
%char
%type ParsedSymbol
%throws AsDocParseException

%{

    public ActionScriptDocLexer(String sourceCode){
        this(new StringReader(sourceCode));
    }    

    public void begin(int state)
    {
        string.setLength(0);
        yybegin(state);
    }

    int xmlLevel = 0;    
    String tagName;
    StringBuilder string = new StringBuilder();
%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]+

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?


IdentFirst = [\p{Lu}\p{Ll}\p{Lt}\p{Lm}\p{Lo}_$]
IdentNext = {IdentFirst} | [\p{Nl}\p{Mn}\p{Mc}\p{Nd}\p{Pc}]


/* identifiers */
Identifier = {IdentFirst}{IdentNext}*

TypeNameSpec = ".<"

/* XML */

XmlS = (\u0020 | \u0009 | \u000D | \u000A)+

XmlCommentStart = "<!--"
XmlCommentEnd = "-->"

XmlNameStartChar = ":" | [A-Z] | "_" | [a-z]
XmlNameStartCharUnicode = [\u00C0-\u00D6]   |
        [\u00D8-\u00F6] |
        [\u00F8-\u02FF] |
        [\u0370-\u037D] |
        [\u037F-\u1FFF] |
        [\u200C-\u200D] |
        [\u2070-\u218F] |
        [\u2C00-\u2FEF] |
        [\u3001-\uD7FF] |
        [\uF900-\uFDCF] |
        [\uFDF0-\uFFFD] |
        [\u10000-\uEFFFF]

XmlNameChar = {XmlNameStartChar} | "-" | "." | [0-9] | \u00B7
XmlNameCharUnicode = [\u0300-\u036F] | [\u0203F-\u2040]
XmlName = {XmlNameStartChar} {XmlNameChar}*
XmlNameUnicode = ({XmlNameStartChar}|{XmlNameStartCharUnicode}) ({XmlNameChar}|{XmlNameCharUnicode})*

/* XML Processing Instructions */
XmlInstrStart = "<?" {XmlName}
XmlInstrEnd   = "?>"

/* CDATA  */
XmlCDataStart = "<![CDATA["
XmlCDataEnd   = "]]>"

/* Tags */
XmlOpenTagStart = "<" {XmlName}
XmlOpenTagClose = "/>"
XmlOpenTagEnd = ">"

XmlCloseTag = "</" {XmlName} {XmlS}* ">"

/* attribute */
XmlAttribute = {XmlName} "="

/* string and character literals */
XmlDQuoteStringChar = [^\r\n\"]
XmlSQuoteStringChar = [^\r\n\']



/* integer literals */
DecIntegerLiteral = (0 | [1-9][0-9]*) [ui]?

HexIntegerLiteral = 0 [xX] 0* {HexDigit}+
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit}+
OctDigit          = [0-7]

/* floating point literals */
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [md]?

FloatLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? f?

Float4Literal = float4 {WhiteSpace}* \( {WhiteSpace}* {FloatLiteral} {WhiteSpace}* , {WhiteSpace}* {FloatLiteral} {WhiteSpace}* , {WhiteSpace}* {FloatLiteral} {WhiteSpace}* , {WhiteSpace}* {FloatLiteral} {WhiteSpace}* \)

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]
OIdentifierCharacter = [^\r\n\u00A7\\]
Preprocessor = \u00A7\u00A7 {Identifier}

VerbatimStringCharacter = [^\r\n\"]
VerbatimString = "@\"" {VerbatimStringCharacter}* "\""

NamespaceSuffix = "#" {DecIntegerLiteral}

RegExp = \/([^\r\n/]|\\\/)+\/[a-z]*

%state STRING, CHARLITERAL,XMLCDATA,XMLCOMMENT,OIDENTIFIER, ADOC, ADOC_TAG

%%

<YYINITIAL> {  

  {VerbatimString}               {}

  /* operators */
  
  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                 }
  {Preprocessor}                 {
                                 }
  "\u00A7"                       {
                                    yybegin(OIDENTIFIER);
                                 }

  /* character literal */
  \'                             {
                                    yybegin(CHARLITERAL);
                                 }

  /* numeric literals */

  {DecIntegerLiteral}            { 
                                 }

  {HexIntegerLiteral}            {                                    
                                 }
  {OctIntegerLiteral}            { 
                                 }  
  {DoubleLiteral}                { 
                                 }
{FloatLiteral}                   { 
                                 }

{Float4Literal}                  {
                                 }

  // AsDoc comments need a state so that we can highlight the @ controls
  "/**"                          {  
                                    yybegin(ADOC); 
                                    string.setLength(0);
                                    tagName = null;
                                 }
  /* comments */
  {Comment}                      {  }

  {LineTerminator}               {
                                     
                                 }
  /* whitespace */
  {WhiteSpace}                   {  }
  {TypeNameSpec}                 { }
  {XmlOpenTagStart}              {
                                 }
  {XmlCommentStart}              {
                                      yybegin(XMLCOMMENT);
                                 }
  {XmlCDataStart}                {
                                       yybegin(XMLCDATA);
                                 }
  "<{"                           {   }
  /* identifiers */
  {Identifier}                   {  }
  /* regexp */
  {RegExp}                       { }
  {NamespaceSuffix}              { }   
}


<XMLCDATA> {
    {XmlCDataEnd}                 {
                                     yybegin(YYINITIAL);
                                  }
 {LineTerminator}                 { }
    [^]                           { }
}

<XMLCOMMENT> {
   {XmlCommentEnd}                {
                                     yybegin(YYINITIAL);
                                  }
   {LineTerminator}               { }
   [^]                            { }
}

<OIDENTIFIER> {
    "\u00A7"                         {
                                     yybegin(YYINITIAL);
                                 }

  "\\\u00A7"                     { }
  

  \\.                            { }
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);                                     
                                 }

  "\\\""                         {}
  
  \\.                            {  }
  {LineTerminator}               { yybegin(YYINITIAL); yyline++;}
}

<CHARLITERAL> {
  \'                             {
                                     yybegin(YYINITIAL);
                                 }

  "\\'"                          { }
  

  \\.                            { }
  {LineTerminator}               { yybegin(YYINITIAL);}
}

<ADOC> {
  "*/"                           { 
                                     yybegin(YYINITIAL); 
                                     String ret = string.toString().trim();
                                     string.setLength(0);
                                     return new ParsedSymbol(SymbolType.DOC_END, tagName, ret);
                                 }

  "@"                            {   
                                     yybegin(ADOC_TAG);
                                     String ret = string.toString().trim();
                                     string.setLength(0);
                                     return new ParsedSymbol(tagName == null ? SymbolType.DOC_BEGIN : SymbolType.DOC_MIDDLE, tagName, ret);
                                 }
 {LineTerminator} " "* "*" [^/]  {}
  .|\n                          { string.append(yytext()); }

}

<ADOC_TAG> {
  ([:letter:])+                  { tagName = yytext();}

  "*/"                           { 
                                     yybegin(YYINITIAL); 
                                     String prevTag = tagName;
                                     tagName = null;
                                     return new ParsedSymbol(SymbolType.DOC_END, prevTag, "");
                                 }

  .|\n                           {   
                                     yybegin(ADOC);                                      
                                 }
}

/* error fallback */
[^]                              {  }
<<EOF>>                          { return new ParsedSymbol(SymbolType.EOF, null, ""); }
