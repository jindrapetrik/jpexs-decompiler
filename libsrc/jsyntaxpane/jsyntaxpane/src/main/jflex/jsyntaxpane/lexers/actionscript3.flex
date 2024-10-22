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
import javax.swing.text.Segment;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
%%

%public
%class ActionScript3Lexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token


%{
    /**
     * Create an empty lexer, yyreset will be called later to reset and assign
     * the reader
     */
    public ActionScript3Lexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

    private static final byte PAREN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte CURLY     = 3;

    private static String xmlTagName="";

    private Token prevToken = null;

    @Override
    public void parse(Segment segment, int ofst, List<Token> tokens) {
        try {
            CharArrayReader reader = new CharArrayReader(segment.array, segment.offset, segment.count);
            yyreset(reader);
            this.offset = ofst;
            prevToken = null;
            Token t = yylex();
            prevToken = t;
            for (; t != null; t = yylex()) {
                prevToken = t;
                tokens.add(t);
            }
        } catch (IOException ex) {
            Logger.getLogger(DefaultJFlexLexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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

XmlCommentStart = "<!--"
XmlCommentEnd = "-->"

XmlCDataStart = "<![CDATA["
XmlCDataEnd   = "]]>"

/* identifiers */
Identifier = {IdentFirst}{IdentNext}*

IdentifierOrParent = {Identifier} | ".."

Path = "/" | "/"? {IdentifierOrParent} ("/" {IdentifierOrParent})* "/"?

/* identifiers */

IdentifierNs = {Identifier} ":" {Identifier}

TypeNameSpec = ".<" {Identifier} ">"

/* XML */
LetterColon = [:jletter] | ":"
XMLIdentifier = {Identifier} | {IdentifierNs}
XMLAttribute = " "* {XMLIdentifier} " "* "=" " "* (\" {InputCharacter}* \" | "'" {InputCharacter}* "'") " "*
XMLBeginOneTag = "<" {XMLIdentifier} {XMLAttribute}* ">" | "<{" {XMLIdentifier} "}" {XMLAttribute}* " "* ">"
XMLBeginTag = "<" {XMLIdentifier} " " | "<{" {XMLIdentifier} "} "
XMLEndTag = "</" {XMLIdentifier} " "* ">" | "</{" {XMLIdentifier} "}" " "* ">"

/* integer literals */
DecIntegerLiteral = (0 | -?[1-9][0-9]*) [ui]?

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctDigit          = [0-7]

/* floating point literals */
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [mdf]?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

NewVector = "new" {WhiteSpace}* "<"

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]

OIdentifierCharacter = [^\r\n\u00A7\\]
Preprocessor = \u00A7\u00A7 {Identifier}

NamespaceSuffix = "#" {DecIntegerLiteral}

RegExp = \/([^\r\n/]|\\\/)+\/[a-z]*

%state STRING, CHARLITERAL, XMLSTARTTAG, XML, OIDENTIFIER, XMLCOMMENT, XMLCDATA

%%

<YYINITIAL> {

  /* keywords */
  "break"                        |
  "case"                         |
  "continue"                     |
  "default"                      |
  "do"                           |
  "while"                        |
  "else"                         |
  "for"                          |
  "each"                         |
  "in"                           |
  "if"                           |
  "label"                        |
  "return"                       |
  "super"                        |
  "switch"                       |
  "throw"                        |
  "try"                          |
  "catch"                        |
  "finally"                      |
  "while"                        |
  "with"                         |
  "dynamic"                      |
  "final"                        |
  "internal"                     |
  "native"                       |
  "override"                     |
  "private"                      |
  "protected"                    |
  "public"                       |
  "static"                       |
  "class"                        |
  "const"                        |
  "extends"                      |
  "function"                     |
  "get"                          |
  "implements"                   |
  "interface"                    |
  "namespace"                    |
  "package"                      |
  "set"                          |
  "var"                          |
  "import"                       |
  "include"                      |
  "use"                          |
  "false"                        |
  "null"                         |
  "this"                         |
  "true"                         { return token(TokenType.KEYWORD); }


  {RegExp}                       { 
                                    if (prevToken == null || (prevToken.type == TokenType.OPERATOR && prevToken.pairValue >= 0)) {
                                        return token(TokenType.REGEX);
                                    } else {    
                                        int ch = yychar;
                                        yypushback(yylength()-1);
                                        // divide "/" operator
                                        return token(TokenType.OPERATOR,ch,1);
                                    }
                                 }

  {XmlCommentStart}                 {
                                     yybegin(XMLCOMMENT);
                                     return token(TokenType.STRING);
                                 }
  {XmlCDataStart}                   {
                                     yybegin(XMLCDATA);
                                     return token(TokenType.STRING);
                                 }

  /* operators */

  "("                            { return  token(TokenType.OPERATOR,  PAREN); }
  ")"                            { return token(TokenType.OPERATOR, -PAREN); }
  "}"                            { return token(TokenType.OPERATOR, -CURLY); }
  "["                            { return token(TokenType.OPERATOR,  BRACKET); }
  "]"                            { return token(TokenType.OPERATOR, -BRACKET); }
  ";"                            |
  ","                            |
  "..."                          |
  "."                            |
  "="                            |
  "<"                            |
  "!"                            |
  "~"                            |
  "?"                            |
  ":"                            |
  "=="                           |
  "<="                           |
  ">="                           |
  "!="                           |
  "&&"                           |
  "||"                           |
  "++"                           |
  "--"                           |
  "+"                            |
  "-"                            |
  "*"                            |
  "/"                            |
  "&"                            |
  "|"                            |
  "^"                            |
  "%"                            |
  "<<"                           |
  ">>"                           |
  ">>>"                          |
  "+="                           |
  "-="                           |
  "*="                           |
  "/="                           |
  "&="                           |
  "|="                           |
  "^="                           |
  "%="                           |
  "<<="                          |
  ">>="                          |
  ">>>="                         |
  "as"                           |
  "delete"                       |
  "instanceof"                   |
  "is"                           |
  "::"                           |
  "new"                          |
  "typeof"                       |
  "void"                         |
 {NewVector}                     |
  "@"                            { return token(TokenType.OPERATOR); }

  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                 }
  {Preprocessor}                 {
                                    return token(TokenType.ERROR);
                                 }
 "\u00A7"                        {
                                    yybegin(OIDENTIFIER);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                 }

  /* character literal */
  \'                             {
                                    yybegin(CHARLITERAL);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                 }

  /* numeric literals */

  {DecIntegerLiteral}            |

  {HexIntegerLiteral}            |

  {OctIntegerLiteral}            |

  {DoubleLiteral}                |
  {DoubleLiteral}[dD]            { return token(TokenType.NUMBER); }

  // JavaDoc comments need a state so that we can highlight the @ controls

  /* comments */
  {Comment}                      { return token(TokenType.COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { }
  {TypeNameSpec}                 { return token(TokenType.IDENTIFIER); }
  {XMLBeginOneTag}                  {  yybegin(XML);
                                    tokenStart = yychar;
                                    tokenLength = yylength();
                                    String s=yytext();
                                    s=s.substring(1,s.length()-1);
                                    if(s.contains(" ")){
                                       s=s.substring(0,s.indexOf(" "));
                                    }
                                    xmlTagName = s;
                                 }

  ">"                            { return token(TokenType.OPERATOR); }
  
  "{"                            { return token(TokenType.OPERATOR,  CURLY); }
  
  /* identifiers */
  {Identifier}{NamespaceSuffix}  { return token(TokenType.REGEX); }
  {Identifier}                   { return token(TokenType.IDENTIFIER); }
  
}

<XMLSTARTTAG> {
   {XMLAttribute}                { tokenLength += yylength();}
   {WhiteSpace}                   { tokenLength += yylength(); }
   ">"                             { yybegin(XML);  tokenLength += yylength();}
}
<XML> {
   {XMLBeginOneTag}                 { tokenLength += yylength();}
   {XMLEndTag}                   { tokenLength += yylength();
                                   String endtagname=yytext();
                                   endtagname = endtagname.substring(2,endtagname.length()-1);
                                   endtagname = endtagname.trim();
                                   if(endtagname.equals(xmlTagName)){
                                       yybegin(YYINITIAL);
                                       return token(TokenType.STRING, tokenStart, tokenLength);
                                   }
                                 }
   .|\n                          { tokenLength += yylength(); }
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                 }

  {StringCharacter}+             { tokenLength += yylength(); }

  \\[0-3]?{OctDigit}?{OctDigit}  { tokenLength += yylength(); }

  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);  }
}

<OIDENTIFIER> {
   "\u00A7" {NamespaceSuffix}                           {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote + namespace suffix
                                     return token(TokenType.REGEX, tokenStart, tokenLength + yylength());
                                 }

  "\u00A7"                            {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return token(TokenType.REGEX, tokenStart, tokenLength + 1);
                                 }

  {OIdentifierCharacter}+             { tokenLength += yylength(); }


  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);  }
}

<CHARLITERAL> {
  \'                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                 }

  {SingleCharacter}+             { tokenLength += yylength(); }

  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);  }
}

<XMLCOMMENT> {
  {XmlCommentEnd}                   {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.STRING);
                                 }
   ~{XmlCommentEnd}                 {
                                     yypushback(3);
                                     return token(TokenType.STRING);
                                 }
}

<XMLCDATA> {
  {XmlCDataEnd}                     {
                                     yybegin(YYINITIAL);
                                     return token(TokenType.STRING);
                                 }
  ~{XmlCDataEnd}                    {
                                     yypushback(3);
                                     return token(TokenType.STRING);
                                 }
}

/* error fallback */
[^]                              {  }
<<EOF>>                          { return null; }

