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
%class GroovyLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token


%{
    /**
     * Default constructor is needed as we will always call the yyreset
     */
    public GroovyLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

    private static final byte PARAN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte CURLY     = 3;
%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} 

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* Groovy and generally Java types have first UpperCase Letter */
// Type = [:uppercase:][:jletterdigit:]*

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
StringCharacter = [^\r\n\"\\\$]
SingleCharacter = [^\r\n\'\\]
RegexCharacter  = [^\r\n\/]

%state STRING, CHARLITERAL, REGEX, GSTRING_EXPR, CHARLITERAL, JDOC, JDOC_TAG
%state ML_STRING, ML_STRING_EXPR

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

  /* Groovy reserved words not in Java */
  "as"                           |
  "asssert"                      | 
  "def"                          |
  "in"                           |
  "threadsafe"                   |

  /* Booleans and null */
  "true"                         |
  "false"                        |
  "null"                         { return token(TokenType.KEYWORD); }


  /* Builtin Types and Object Wrappers */
  "Boolean"                      |
  "Byte"                         |
  "Character"                    |
  "Double"                       |
  "Float"                        |
  "Integer"                      |
  "Object"                       |
  "Short"                        |
  "String"                       |
  "Void"                         |
  "Class"                        |
  "Number"                       |
  "Package"                      |
  "StringBuffer"                 |
  "StringBuilder"                |
  "CharSequence"                 |
  "Thread"                       |
  "Regex"                        { return token(TokenType.TYPE); }
  
  /* Some Java standard Library Types */
  "Throwable"                    |
  "Cloneable"                    |
  "Comparable"                   |
  "Serializable"                 |
  "Runnable"                     { return token(TokenType.TYPE); }

  /* Groovy commonly used methods */
  "print"                        |
  "println"                      { return token(TokenType.KEYWORD); }

  /* Frequently used Standard Exceptions */
  "ArithmeticException"              |
  "ArrayIndexOutOfBoundsException"   |
  "ClassCastException"               |
  "ClassNotFoundException"           |
  "CloneNotSupportedException"       |
  "Exception"                        |
  "IllegalAccessException"           |
  "IllegalArgumentException"         |
  "IllegalStateException"            |
  "IllegalThreadStateException"      |
  "IndexOutOfBoundsException"        |
  "InstantiationException"           |
  "InterruptedException"             |
  "NegativeArraySizeException"       |
  "NoSuchFieldException"             |
  "NoSuchMethodException"            |
  "NullPointerException"             |
  "NumberFormatException"            |
  "RuntimeException"                 |
  "SecurityException"                |
  "StringIndexOutOfBoundsException"  |
  "UnsupportedOperationException"    { return token(TokenType.TYPE2); }

  /* operators */
  "("                            { return token(TokenType.OPERATOR,  PARAN); }
  ")"                            { return token(TokenType.OPERATOR, -PARAN); }
  "{"                            { return token(TokenType.OPERATOR,  CURLY); }
  "}"                            { return token(TokenType.OPERATOR, -CURLY); }
  "["                            { return token(TokenType.OPERATOR,  BRACKET); }
  "]"                            { return token(TokenType.OPERATOR, -BRACKET); }

  ";"                            | 
  ","                            | 
  "."                            | 
  "@"                            | 
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

  "~="                           | 
  "?."                           { return token(TokenType.OPERATOR); } 

  /* string literal */
  \"{3}                          {
                                    yybegin(ML_STRING);
                                    tokenStart = yychar;
                                    tokenLength = 3;
                                 }

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
  {WhiteSpace}+                  { /* skip */ }

  /* identifiers */ 
  {Identifier}                   { return token(TokenType.IDENTIFIER); }

  /*
     Groovy Regex -- state cannot be easily used here due to / by itself being
     a valid operator.  So if we flip into the REGEX state, we cannot distinguish
     a regular /
  */
  "/" [^*] {RegexCharacter}+ "/"  { return token(TokenType.REGEX); }

}


<STRING> {

  \"                             { 
                                    yybegin(YYINITIAL); 
                                    // length also includes the trailing quote
                                    return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                 }

  "${"                           { 
                                    yybegin(GSTRING_EXPR); 
                                    // length also includes the trailing quote
                                    int s = tokenStart;
                                    int l = tokenLength;
                                    tokenStart = yychar;
                                    tokenLength = 2;
                                    return token(TokenType.STRING, s, l);
                                 }
  
  {StringCharacter}+             { tokenLength += yylength(); }

  \\[0-3]?{OctDigit}?{OctDigit}  { tokenLength += yylength(); }
  
  /* escape sequences */


  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);  }

}

<GSTRING_EXPR> {
    "}"                          { 
                                    yybegin(STRING); 
                                    // length also includes the trailing quote
                                    int s = tokenStart;
                                    int l = tokenLength + 1;
                                    tokenStart = yychar + 1;
                                    tokenLength = 0;
                                    return token(TokenType.STRING2, s, l);
                                 }

  {StringCharacter}              { tokenLength ++; }
}

<ML_STRING> {

  \"{3}                          {
                                    yybegin(YYINITIAL);
                                    // length also includes the trailing quote
                                    return token(TokenType.STRING, tokenStart, tokenLength + 3);
                                 }

  "${"                           {
                                    yybegin(ML_STRING_EXPR);
                                    // length also includes the trailing quote
                                    int s = tokenStart;
                                    int l = tokenLength;
                                    tokenStart = yychar;
                                    tokenLength = 2;
                                    return token(TokenType.STRING, s, l);
                                 }

  \\[0-3]?{OctDigit}?{OctDigit}  { tokenLength += yylength(); }

  /* escape sequences */

  \\.                            { tokenLength += 2; }
  .|{LineTerminator}             { tokenLength += yylength(); }

}

<ML_STRING_EXPR> {
    "}"                          {
                                    yybegin(ML_STRING);
                                    // length also includes the trailing quote
                                    int s = tokenStart;
                                    int l = tokenLength + 1;
                                    tokenStart = yychar + 1;
                                    tokenLength = 0;
                                    return token(TokenType.STRING2, s, l);
                                 }

  .|\n|\r                        { tokenLength ++; }
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

<REGEX> {
  "/"                            { 
                                     yybegin(YYINITIAL); 
                                     // length also includes the trailing quote
                                     return token(TokenType.REGEX, tokenStart, tokenLength + 1);
                                 }
  
  {RegexCharacter}+             { tokenLength += yylength(); }
  
  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);  }
}

/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }

