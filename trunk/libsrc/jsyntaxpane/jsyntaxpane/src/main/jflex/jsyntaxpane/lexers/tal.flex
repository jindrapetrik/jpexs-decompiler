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
%class TALLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token
%caseless


%{
    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public TALLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }
%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]+

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "!" [^\r\n!]* ( "!" | {LineTerminator} )
EndOfLineComment = "--" {InputCharacter}* {LineTerminator}?

/* identifiers */
Identifier = [A-Za-z_][A-Za-z0-9\^_]*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = "%" [1-3]? {OctDigit} {1,15}
OctLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]
    
FixedLiteral  = DecIntegerLiteral [fF]
DoubleLiteral = DecIntegerLiteral [dD]

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]

%%

<YYINITIAL> {

  /* keywords */
  "begin"                        |
  "end"                          |
  "struct"                       |
  "fieldalign"                   |
  "shared"                       |
  "shared2"                      |
  "literal"                      |
  "for"                          |
  "do"                           |
  "while"                        |
  "?page"                        |
  "?section"                     { return token(TokenType.KEYWORD); }

  "int"                          |
  "string"                       |
  "int(32)"                      |
  "fixed"                        |
  "byte"                         |
  "float"                        |
  "filler"                       { return token(TokenType.TYPE); }


  "("                            |
  ")"                            |
  "{"                            | 
  "}"                            | 
  "["                            | 
  "]"                            | 
  ";"                            | 
  ","                            | 
  "."                            | 
  "="                            | 
  ">"                            | 
  "<"                            |
  "!"                            | 
  "?"                            | 
  ":"                            | 
  ":="                           | 
  "':='"                         | 
  "'=:'"                         | 
  "<>"                           | 
  "+"                            | 
  "-"                            | 
  "*"                            | 
  "/"                            | 
  "<<"                           | 
  ">>"                           { return token(TokenType.OPERATOR); } 
  
  /* string literal */
  \"{StringCharacter}+\"         { return token(TokenType.STRING); }

  /* character literal */
  \'{SingleCharacter}\'          { return token(TokenType.STRING); }

  /* numeric literals */

  {DecIntegerLiteral}            |
  
  {HexIntegerLiteral}            |
  {HexLongLiteral}               |
 
  {OctIntegerLiteral}            |
  {OctLongLiteral}               |
  
  {FixedLiteral}                 |
  {DoubleLiteral}                { return token(TokenType.NUMBER); }
  
  /* comments */
  {Comment}                      { return token(TokenType.COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { }

  /* identifiers */ 
  {Identifier}                   { return token(TokenType.IDENTIFIER); }
}


/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }

