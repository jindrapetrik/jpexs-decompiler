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
%class XHTMLLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token
%ignorecase

%{
    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public XHTMLLexer() {
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

%xstate COMMENT, CDATA, TAG, INSTR, DOCTYPE

/* main character classes */

/* white space */
S = (\u0020 | \u0009 | \u000D | \u000A)+

/* characters */
// Char = \u0009 | \u000A | \u000D | [\u0020-\uD7FF] | [\uE000-\uFFFD] | [\u10000-\u10FFFF]

/* comments */
CommentStart = "<!--"
CommentEnd = "-->"

NameStartChar = ":" | [A-Z] | "_" | [a-z]
NameChar = {NameStartChar} | "-" | "." | [0-9] | \u00B7
Name = {NameStartChar} {NameChar}*

/* XML Processing Instructions */
InstrStart = "<?" {Name}
InstrEnd   = "?>"

DocTypeStart = "<!doctype"

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

/* HTML specifics */
HTMLTagName = 
    "address"        |
    "applet"         |
    "area"           |
    "a"              |
    "b"              |
    "base"           |
    "basefont"       |
    "big"            |
    "blockquote"     |
    "body"           |
    "br"             |
    "caption"        |
    "center"         |
    "cite"           |
    "code"           |
    "dd"             |
    "dfn"            |
    "dir"            |
    "div"            |
    "dl"             |
    "dt"             |
    "font"           |
    "form"           |
    "h"[1-6]         |
    "head"           |
    "hr"             |
    "html"           |
    "img"            |
    "input"          |
    "isindex"        |
    "kbd"            |
    "li"             |
    "link"           |
    "LINK"           |
    "map"            |
    "META"           |
    "menu"           |
    "meta"           |
    "ol"             |
    "option"         |
    "param"          |
    "pre"            |
    "p"              |
    "samp"           |
    "span"           |
    "select"         |
    "small"          |
    "strike"         |
    "sub"            |
    "sup"            |
    "table"          |
    "td"             |
    "textarea"       |
    "th"             |
    "title"          |
    "tr"             |
    "tt"             |
    "ul"             |
    "var"            |
    "xmp"            |
    "script"         |
    "noscript"       |
    "style"          

HTMLAttrName = 
    "action"            |
    "align"             |
    "alink"             |
    "alt"               |
    "archive"           |
    "background"        |
    "bgcolor"           |
    "border"            |
    "bordercolor"       |
    "cellpadding"       |
    "cellspacing"       |
    "checked"           |
    "class"             |
    "clear"             |
    "code"              |
    "codebase"          |
    "color"             |
    "cols"              |
    "colspan"           |
    "content"           |
    "coords"            |
    "enctype"           |
    "face"              |
    "gutter"            |
    "height"            |
    "hspace"            |
    "href"              |
    "id"                |
    "link"              |
    "lowsrc"            |
    "marginheight"      |
    "marginwidth"       |
    "maxlength"         |
    "method"            |
    "name"              |
    "prompt"            |
    "rel"               |
    "rev"               |
    "rows"              |
    "rowspan"           |
    "scrolling"         |
    "selected"          |
    "shape"             |
    "size"              |
    "src"               |
    "start"             |
    "target"            |
    "text"              |
    "type"              |
    "url"               |
    "usemap"            |
    "ismap"             |
    "valign"            |
    "value"             |
    "vlink"             |
    "vspace"            |
    "width"             |
    "wrap"              |
    "abbr"              |
    "accept"            |
    "accesskey"         |
    "axis"              |
    "char"              |
    "charoff"           |
    "charset"           |
    "cite"              |
    "classid"           |
    "codetype"          |
    "compact"           |
    "data"              |
    "datetime"          |
    "declare"           |
    "defer"             |
    "dir"               |
    "disabled"          |
    "for"               |
    "frame"             |
    "headers"           |
    "hreflang"          |
    "lang"              |
    "language"          |
    "longdesc"          |
    "multiple"          |
    "nohref"            |
    "nowrap"            |
    "object"            |
    "profile"           |
    "readonly"          |
    "rules"             |
    "scheme"            |
    "scope"             |
    "span"              |
    "standby"           |
    "style"             |
    "summary"           |
    "tabindex"          |
    "valuetype"         |
    "version"

HTMLOpenTagStart = "<" {HTMLTagName}
HTMLCloseTag = "</" {HTMLTagName} {S}* ">"
HTMLAttribute = {HTMLAttrName} "="

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
  {DocTypeStart}                 {
                                     yybegin(DOCTYPE);
                                     return token(TokenType.TYPE2, INSTR_OPEN);
                                 }
  {HTMLOpenTagStart}             {
                                     yybegin(TAG);
                                     return token(TokenType.KEYWORD2, TAG_OPEN);
                                 }
  {HTMLCloseTag}                 {   return token(TokenType.KEYWORD2, TAG_CLOSE); }
  {OpenTagStart}                 {
                                     yybegin(TAG);
                                     return token(TokenType.KEYWORD, TAG_OPEN);
                                 }
  {CloseTag}                     {   return token(TokenType.KEYWORD, TAG_CLOSE); }
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

<DOCTYPE> {
  [^>]*                          { }

  {OpenTagEnd}                   {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.TYPE2, INSTR_CLOSE);
                                 }
}

<TAG> {
  {HTMLAttribute}                { return token(TokenType.KEYWORD2); }
  {Attribute}                    { return token(TokenType.IDENTIFIER); }

  \"{DQuoteStringChar}*\"        |
  \'{SQuoteStringChar}*\'        { return token(TokenType.STRING); }


  {OpenTagClose}                 {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.KEYWORD, TAG_CLOSE);
}

  {OpenTagEnd}                   {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.KEYWORD);
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
