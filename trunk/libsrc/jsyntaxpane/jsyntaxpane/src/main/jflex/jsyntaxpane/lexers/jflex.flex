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
%class JFlexLexer
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
    public JFlexLexer() {
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

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

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
SingleCharacter = [^\r\n\'\\]

%state STRING, CHARLITERAL, JDOC, JDOC_TAG

%%

<YYINITIAL> {

  /* keywords */
  "abstract"                     |
  "boolean"                      |
  "break"                        |
  "byte"                         |
  "case"                         |
  "catch"                        |
  "char"                         |
  "class"                        |
  "const"                        |
  "continue"                     |
  "do"                           |
  "double"                       |
  "enum"                         |
  "else"                         |
  "extends"                      |
  "final"                        |
  "finally"                      |
  "float"                        |
  "for"                          |
  "default"                      |
  "implements"                   |
  "import"                       |
  "instanceof"                   |
  "int"                          |
  "interface"                    |
  "long"                         |
  "native"                       |
  "new"                          |
  "goto"                         |
  "if"                           |
  "public"                       |
  "short"                        |
  "super"                        |
  "switch"                       |
  "synchronized"                 |
  "package"                      |
  "private"                      |
  "protected"                    |
  "transient"                    |
  "return"                       |
  "void"                         |
  "static"                       |
  "while"                        |
  "this"                         |
  "throw"                        |
  "throws"                       |
  "try"                          |
  "volatile"                     |
  "strictfp"                     |
  "true"                         |
  "false"                        |
  "null"                         { return token(TokenType.KEYWORD); }

  /* JFlex special types */
  "<<EOF>>"                      |
  "[:jletter:]"                  |
  "[:jletterdigit:]"             |
  "[:letter:]"                   |
  "[:digit:]"                    |
  "[:uppercase:]"                |
  "[:lowercase:]"                |
  "<" [a-zA-Z][a-zA-Z0-9_]* ">"  { return token(TokenType.TYPE2); }

  /* JFlex Specials */
  "%%"                           |
  "%{"                           |
  "%}"                           |
  "%class"                       |
  "%implements"                  |
  "%extends"                     |
  "%public"                      |
  "%final"                       |
  "%abstract"                    |
  "%apiprivate"                  |
  "%init{"                       |
  "%init}"                       |
  "%initthrow{"                  |
  "%initthrow}"                  |
  "%initthrow"                   |
  "%ctorarg"                     |
  "%scanerror"                   |
  "%buffer"                      |
  "%include"                     |
  "%function"                    |
  "%integer"                     |
  "%int"                         |
  "%intwrap"                     |
  "%yylexthrow{"                 |
  "%yylexthrow}"                 |
  "%yylexthrow"                  |
  "%eofval{"                     |
  "%eofval}"                     |
  "%eof{"                        |
  "%eof}"                        |
  "%eofthrow{"                   |
  "%eofthrow}"                   |
  "%eofthrow"                    |
  "%eofclose"                    |
  "%debug"                       |
  "%standalone"                  |
  "%cup"                         |
  "%cupsym"                      |
  "%cupdebug"                    |
  "%byacc"                       |
  "%switch"                      |
  "%table"                       |
  "%pack"                        |
  "%7bit"                        |
  "%8bit"                        |
  "%full"                        |
  "%unicode"                     |
  "%16bit"                       |
  "%caseless"                    |
  "%ignorecase"                  |
  "%char"                        |
  "%line"                        |
  "%column"                      |
  "%notunix"                     |
  "%yyeof"                       |
  "%s"                           |
  "%state"                       |
  "%x"                           |
  "%xstate"                      |
  "%type"                        { return token(TokenType.KEYWORD2); }


  /* Java Built in types and wrappers */
  "Boolean"                      |
  "Byte"                         |
  "Double"                       |
  "Float"                        |
  "Integer"                      |
  "Object"                       |
  "Short"                        |
  "String"                       { return token(TokenType.TYPE); }
  
  /* operators */

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
  ">>>="                         { return token(TokenType.OPERATOR); } 
  
  /* string literal */
  \"                             {  
                                    yybegin(STRING); 
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
  {DecLongLiteral}               |
  
  {HexIntegerLiteral}            |
  {HexLongLiteral}               |
 
  {OctIntegerLiteral}            |
  {OctLongLiteral}               |
  
  {FloatLiteral}                 |
  {DoubleLiteral}                |
  {DoubleLiteral}[dD]            { return token(TokenType.NUMBER); }
  
  // JavaDoc comments need a state so that we can highlight the @ controls
  "/**"                          {  
                                    yybegin(JDOC); 
                                    tokenStart = yychar; 
                                    tokenLength = 3; 
                                 }

  /* comments */
  {Comment}                      { return token(TokenType.COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { }

  /* identifiers */ 
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

<JDOC> {
  "*/"                           { 
                                     yybegin(YYINITIAL); 
                                     return token(TokenType.COMMENT, tokenStart, tokenLength + 2);
                                 }

  "@"                            {   
                                     yybegin(JDOC_TAG); 
                                     int start = tokenStart;
                                     tokenStart = yychar;
                                     int len = tokenLength;
                                     tokenLength = 1;
                                     return token(TokenType.COMMENT, start, len);
                                 }

  .|\n                           { tokenLength ++; }

}

<JDOC_TAG> {
  ([:letter:])+ ":"?             { tokenLength += yylength(); }

  "*/"                           { 
                                     yybegin(YYINITIAL); 
                                     return token(TokenType.COMMENT, tokenStart, tokenLength + 2);
                                 }

  .|\n                           {   
                                     yybegin(JDOC); 
                                     // length also includes the trailing quote
                                     int start = tokenStart;
                                     tokenStart = yychar;
                                     int len = tokenLength;
                                     tokenLength = 1;
                                     return token(TokenType.COMMENT2, start, len);
                                 }
}


/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }

