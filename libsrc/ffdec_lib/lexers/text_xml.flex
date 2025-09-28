/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jpexs.decompiler.flash.tags.text.xml;

%%

%public 
%class XmlLexer
%final
%unicode
%char
%type XmlParsedSymbol
%throws XmlException

%{
    public XmlLexer() {
        super();
    }   

    private static final byte TAG_OPEN      =  1;
    private static final byte TAG_CLOSE     = -1;

    private static final byte INSTR_OPEN    =  2;
    private static final byte INSTR_CLOSE   = -2;

    private static final byte CDATA_OPEN    =  3;
    private static final byte CDATA_CLOSE   = -3;

    private static final byte COMMENT_OPEN  =  4;
    private static final byte COMMENT_CLOSE = -4;
%}

%xstate COMMENT, CDATA, TAG, INSTR

/* main character classes */

/* white space */
S = (\u0020 | \u0009 | \u000D | \u000A)+

/* characters */

Char = \u0009 | \u000A | \u000D | [\u0020-\uD7FF] | [\uE000-\uFFFD] | [\u10000-\u10FFFF]

SimpleCharacter = [^><&]

/* comments */
CommentStart = "<!--"
CommentEnd = "-->"

NameStartChar = ":" | [A-Z] | "_" | [a-z]
NameStartCharUnicode = [\u00C0-\u00D6]   |
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

NameChar = {NameStartChar} | "-" | "." | [0-9] | \u00B7
NameCharUnicode = [\u0300-\u036F] | [\u0203F-\u2040]
Name = {NameStartChar} {NameChar}*
NameUnicode = ({NameStartChar}|{NameStartCharUnicode}) ({NameChar}|{NameCharUnicode})*

/* XML Processing Instructions */
InstrStart = "<?" {Name}
InstrEnd   = "?>"

/* CDATA  */
CDataStart = "<![CDATA["
CDataEnd   = "]]>"

/* Tags */
OpenTagStart = "<" {Name}
OpenTagClose = "/>"
OpenTagEnd = ">"

CloseTag = "</" {Name} {S}* ">"

/* attribute */
Attribute = {Name} "="

/* string and character literals */
DQuoteStringChar = [^\r\n\"]
SQuoteStringChar = [^\r\n\']

%%

<YYINITIAL> {
  
   "&"  [a-z]+ ";"                { return new XmlParsedSymbol(XmlSymbolType.ENTITY, yytext().substring(1, yytext().length() - 1), yytext(), yychar); }
  "&#" [:digit:]+ ";"            { return new XmlParsedSymbol(XmlSymbolType.ENTITY_NUMERIC, Integer.parseInt(yytext().substring(1, yytext().length() - 1)), yytext(), yychar); }

  {InstrStart}                   {
                                     yybegin(INSTR);
                                     return new XmlParsedSymbol(XmlSymbolType.INSTR_OPEN, yytext(), yychar);
                                 }
  {OpenTagStart}                 {
                                     yybegin(TAG);
                                     return new XmlParsedSymbol(XmlSymbolType.TAG_OPEN, yytext().substring(1), yytext(), yychar);
                                 }
  {CloseTag}                       {   return new XmlParsedSymbol(XmlSymbolType.TAG_CLOSE, yytext().substring(2, yytext().length() - 1), yytext(), yychar); }
  {CommentStart}                 {
                                     yybegin(COMMENT);
                                     return new XmlParsedSymbol(XmlSymbolType.COMMENT_OPEN, yytext(), yychar);
                                 }
  {CDataStart}                   {
                                     yybegin(CDATA);
                                     return new XmlParsedSymbol(XmlSymbolType.CDATA_OPEN, yytext(), yychar);
                                 }
   {SimpleCharacter}+            { return new XmlParsedSymbol(XmlSymbolType.CHARACTER, yytext(), yytext(), yychar);}
}

<INSTR> {
  {Attribute}                    { return new XmlParsedSymbol(XmlSymbolType.ATTRIBUTE, yytext().substring(0, yytext().length() - 1).trim(), yytext(), yychar); }

  \"{DQuoteStringChar}*\"        |
  \'{SQuoteStringChar}*\'        { return new XmlParsedSymbol(XmlSymbolType.ATTRIBUTE_VALUE, yytext().substring(1, yytext().length() - 1), yytext(), yychar); }

  {InstrEnd}                     {
                                     yybegin(YYINITIAL);
                                     return new XmlParsedSymbol(XmlSymbolType.INSTR_CLOSE, yytext(), yychar);
                                 }
  {S}                            {   return new XmlParsedSymbol(XmlSymbolType.WHITESPACE, yytext(), yychar);}
}

<TAG> {
  {Attribute}                    { return new XmlParsedSymbol(XmlSymbolType.ATTRIBUTE, yytext().substring(0, yytext().length() - 1).trim(), yytext(), yychar); }

  \"{DQuoteStringChar}*\"        |
  \'{SQuoteStringChar}*\'        { return new XmlParsedSymbol(XmlSymbolType.ATTRIBUTE_VALUE, yytext().substring(1, yytext().length() - 1), yytext(), yychar); }


  {OpenTagClose}                 {
                                     yybegin(YYINITIAL);
                                     return new XmlParsedSymbol(XmlSymbolType.TAG_CLOSE, yytext(), yychar);
                                  }

  {OpenTagEnd}                   {
                                     yybegin(YYINITIAL);
                                     return new XmlParsedSymbol(XmlSymbolType.TAG_OPEN_END, yytext(), yychar);
                                 }
  {S}                            {   return new XmlParsedSymbol(XmlSymbolType.WHITESPACE, yytext(), yychar);}
}

<COMMENT> {
  {CommentEnd}                   {
                                     yybegin(YYINITIAL);
                                     return new XmlParsedSymbol(XmlSymbolType.COMMENT_CLOSE, yytext(), yychar);
                                 }
   ~{CommentEnd}                 {
                                     yypushback(3);
                                     return new XmlParsedSymbol(XmlSymbolType.COMMENT, yytext().substring(yytext().length() - 3), yytext().substring(yytext().length() - 3), yychar);
                                 }
}

<CDATA> {
  {CDataEnd}                     {
                                     yybegin(YYINITIAL);
                                     return new XmlParsedSymbol(XmlSymbolType.CDATA_CLOSE, yytext(), yychar);
                                 }
  ~{CDataEnd}                    {
                                     yypushback(3);
                                     return new XmlParsedSymbol(XmlSymbolType.CDATA, yytext().substring(yytext().length() - 3), yytext().substring(yytext().length() - 3), yychar);
                                 }
}

<YYINITIAL,TAG,INSTR,CDATA,COMMENT> {
/* error fallback */
   .|\n                          { throw new XmlException("Incorrect text: \"" + yytext() + "\""); }
   <<EOF>>                       { return new XmlParsedSymbol(XmlSymbolType.EOF, "", -1); }
}

