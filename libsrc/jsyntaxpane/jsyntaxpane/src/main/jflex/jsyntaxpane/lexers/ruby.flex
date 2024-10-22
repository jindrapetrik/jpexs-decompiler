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
%class RubyLexer
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
    public RubyLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

    private static final byte PAREN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte CURLY     = 3;
    private static final byte WORD      = 4;

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]+

/* comments */
Comment = "#" {InputCharacter}* {LineTerminator}?

/* identifiers */
Identifier = [a-zA-Z][a-zA-Z0-9_]*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*
DecLongLiteral    = {DecIntegerLiteral} [lL]

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]
    
/* floating point literals */        
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]

%state STRING, ML_STRING

%%

<YYINITIAL> {

  /* keywords */
  "BEGIN"                        |
  "ensure"                       |
  "assert"                       |
  "nil"                          |
  "self"                         |
  "when"                         |
  "END"                          |
  "false"                        |
  "not"                          |
  "super"                        |
  "alias"                        |
  "defined"                      |
  "or"                           |
  "then"                         |
  "yield"                        |
  "and"                          |
  "redo"                         |
  "true"                         |
  "else"                         |
  "in"                           |
  "rescue"                       |
  "undef"                        |
  "break"                        |
  "elsif"                        |
  "module"                       |
  "retry"                        |
  "unless"                       |
  "next"                         |
  "return"                       { return token(TokenType.KEYWORD); }

  "begin"                        |
  "case"                         |
  "class"                        |
  "def"                          |
  "for"                          |
  "while"                        |
  "until"                        |
  "do"                           |
  "if"                           { return token(TokenType.KEYWORD,  WORD); }

  "end"                          { return token(TokenType.KEYWORD, -WORD); }


  /* Built-in Types*/
  "self"                         |
  "nil"                          |
  "true"                         |
  "false"                        |
  "__FILE__"                     |
  "__LINE__"                     {  return token(TokenType.TYPE);  }


  
  /* operators */

  "("                            { return token(TokenType.OPERATOR,  PAREN); }
  ")"                            { return token(TokenType.OPERATOR, -PAREN); }
  "{"                            { return token(TokenType.OPERATOR,  CURLY); }
  "}"                            { return token(TokenType.OPERATOR, -CURLY); }
  "["                            { return token(TokenType.OPERATOR,  BRACKET); }
  "]"                            { return token(TokenType.OPERATOR, -BRACKET); }
  "+"                            |
  "-"                            |
  "*"                            |
  "**"                           |
  "/"                            |
  "//"                           |
  "%"                            |
  "<<"                           |
  ">>"                           |
  "&"                            |
  "|"                            |
  "^"                            |
  "~"                            |
  "<"                            |
  ">"                            |
  "<="                           |
  ">="                           |
  "=="                           |
  "!="                           |
  "<>"                           |
  "@"                            |
  ","                            |
  ":"                            |
  "."                            |
  ".."                           |
  "`"                            |
  "="                            |
  ";"                            |
  "+="                           |
  "-="                           |
  "*="                           |
  "/="                           |
  "//="                          |
  "%="                           |
  "&="                           |
  "|="                           |
  "^="                           |
  ">>="                          |
  "<<="                          |
  "**="                          { return token(TokenType.OPERATOR); }
  
  /* string literal */
  \"{3}                          {
                                    yybegin(ML_STRING);
                                    tokenStart = yychar;
                                    tokenLength = 3;
                                 }

  \"                             {
                                    yybegin(STRING);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                 }


  /* numeric literals */

  {DecIntegerLiteral}            |
  {DecLongLiteral}               |
  
  {HexIntegerLiteral}            |
  {HexLongLiteral}               |
 
  {OctIntegerLiteral}            |
  {OctLongLiteral}               |

  {FloatLiteral}                 |
  {DoubleLiteral}                |
  {FloatLiteral}[jJ]             { return token(TokenType.NUMBER); }
  
  /* comments */
  {Comment}                      { return token(TokenType.COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { }

  /* identifiers */ 
  {Identifier}"?"                { return token(TokenType.TYPE2); }
  {Identifier}                   { return token(TokenType.IDENTIFIER); }
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

<ML_STRING> {
  \"{3}                          {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 3);
                                 }

  {StringCharacter}+             { tokenLength += yylength(); }

  \\[0-3]?{OctDigit}?{OctDigit}  { tokenLength += yylength(); }

  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { tokenLength ++;  }
}


/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }

