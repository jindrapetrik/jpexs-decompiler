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

package jsyntaxpane.lexers;


import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

%%

%public 
%class XmlLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token

%{
    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public XmlLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
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
  
  "&"  [a-z]+ ";"                |
  "&#" [:digit:]+ ";"            { return token(TokenType.KEYWORD2); }

  {InstrStart}                   {
                                     yybegin(INSTR);
                                     return token(TokenType.TYPE2, INSTR_OPEN);
                                 }
  {OpenTagStart}                 {
                                     yybegin(TAG);
                                     return token(TokenType.TYPE, TAG_OPEN);
                                 }
  {CloseTag}                       {   return token(TokenType.TYPE, TAG_CLOSE); }
  {CommentStart}                 {
                                     yybegin(COMMENT);
                                     return token(TokenType.COMMENT2, COMMENT_OPEN);
                                 }
  {CDataStart}                   {
                                     yybegin(CDATA);
                                     return token(TokenType.COMMENT2, CDATA_OPEN);
                                 }
}

<INSTR> {
  {Attribute}                    { return token(TokenType.IDENTIFIER); }

  \"{DQuoteStringChar}*\"        |
  \'{SQuoteStringChar}*\'        { return token(TokenType.STRING); }

  {InstrEnd}                     {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.TYPE2, INSTR_CLOSE);
                                 }
                                 }

<TAG> {
  {Attribute}                    { return token(TokenType.IDENTIFIER); }

  \"{DQuoteStringChar}*\"        |
  \'{SQuoteStringChar}*\'        { return token(TokenType.STRING); }


  {OpenTagClose}                 {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.TYPE, TAG_CLOSE);
}

  {OpenTagEnd}                   {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.TYPE);
                                 }
}

<COMMENT> {
  {CommentEnd}                   {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.COMMENT2, COMMENT_CLOSE);
                                 }
   ~{CommentEnd}                 {
                                     yypushback(3);
                                     return token(TokenType.COMMENT);
                                 }
}

<CDATA> {
  {CDataEnd}                     {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.COMMENT2, CDATA_CLOSE);
                                 }
  ~{CDataEnd}                    {
                                     yypushback(3);
                                     return token(TokenType.COMMENT);
                                 }
}

<YYINITIAL,TAG,INSTR,CDATA,COMMENT> {
/* error fallback */
   .|\n                          {  }
   <<EOF>>                       { return null; }
}
