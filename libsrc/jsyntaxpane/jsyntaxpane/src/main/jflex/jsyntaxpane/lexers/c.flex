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
/*
 * Copyright 2006 Arnout Engelen <arnouten[remove] at bzzt dot net>.
 * Copyright 2000-2006 Omnicore Software, Hans Kratz & Dennis Strein GbR,
 *                     Geert Bevin <gbevin[remove] at uwyn dot com>.
 * Distributed under the terms of either:
 * - the common development and distribution license (CDDL), v1.0; or
 * - the GNU Lesser General Public License, v2.1 or later
 */
package jsyntaxpane.lexers;


import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

%%

%public
%class CLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token


%{

    public CLexer() {
        super();
    }

    private static final byte PAREN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte CURLY     = 3;

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

ConstantIdentifier = {SimpleConstantIdentifier}
SimpleConstantIdentifier = [#A-Z0-9_]+

Identifier = [:jletter:][:jletterdigit:]*

TypeIdentifier = {SimpleTypeIdentifier}
SimpleTypeIdentifier = [A-Z][:jletterdigit:]*

/* int literals */

DecLiteral = 0 | [1-9][0-9]* {IntegerSuffix}

HexLiteral    = 0 [xX] 0* {HexDigit}* {IntegerSuffix}
HexDigit      = [0-9a-fA-F]

OctLiteral    = 0+ {OctDigit}* {IntegerSuffix}
OctDigit          = [0-7]

IntegerSuffix = [uU]? [lL]? [uU]?
	
/* float literals */

FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}|{FLit4}) ([fF]|[dD])?

FLit1 = [0-9]+ \. [0-9]* {Exponent}?
FLit2 = \. [0-9]+ {Exponent}?
FLit3 = [0-9]+ {Exponent}
FLit4 = [0-9]+ {Exponent}?

Exponent = [eE] [+\-]? [0-9]+

%%

<YYINITIAL> {

  /* keywords */
  "break" |
  "case" |
  "catch" |
  "continue" |
  "default" |
  "do" |
  "else" |
  "for" |
  "goto" |
  "enum" |
  "if" |
  "inline" |
  "mutable" |
  "noinline" |
  "return" |
  "safecast" |
  "sealed" |
  "selectany" |
  "sizeof" |
  "static_cast" |
  "switch" |
  "template" |
  "this" |
  "thread" |
  "throw" |
  "try" |
  "typedef" |
  "typeid" |
  "typename" |
  "using" |
  "uuid" |
  "value" |
  "virtual" |
  "while"
     { return token(TokenType.KEYWORD); }
     
  "static" |
  "struct" |
  "union" |
  "volatile" |
  "register" |
  "extern" |
  "const" |
  "signed" |
  "unsigned" |
  "bool" |
  "char" |
  "double" |
  "int" |
  "long" |
  "float" |
  "short" |
  "void" { return token(TokenType.TYPE); }

  /* literals */
  

  (\" ( [^\"\n\\] | \\[^\n] )* (\n | \\\n | \")) |
  (\' ( [^\'\n\\] | \\[^\n] )* (\n | \\\n | \')) 
 	{ return token(TokenType.STRING); }
 	
  "true" |
  "false" |
  {DecLiteral} |
  {OctLiteral} |
  {HexLiteral} |

  {FloatLiteral}
	 { return token(TokenType.NUMBER); }

  /* preprocessor symbols */
  "#define" |
  "#elif" |
  "#else" |
  "#endif" |
  "#error" |
  "#ifdef" |
  "#ifndef" |
  "#if" |
  "#import" |
  "#include" |
  "#line" |
  "#pragma" |
  "#undef" |
  "#using"
  	{ return token(TokenType.KEYWORD2); }

  
  /* separators */
  "("                            { return token(TokenType.OPERATOR,  PAREN); }
  ")"                            { return token(TokenType.OPERATOR, -PAREN); }
  "{"                            { return token(TokenType.OPERATOR,  CURLY); }
  "}"                            { return token(TokenType.OPERATOR, -CURLY); }
  "["                            { return token(TokenType.OPERATOR,  BRACKET); }
  "]"                            { return token(TokenType.OPERATOR, -BRACKET); }

  /* operators */
  "=" |
  ";" |
  "," |
  "." |
  ">" |
  "<" |
  "!" |
  "~" |
  "?" |
  ":" |
  "+" |
  "-" |
  "*" |
  "/" |
  "&" |
  "|" |
  "^" |
  ">>"       |
  "<<"       |
  "%"                      { return token(TokenType.OPERATOR); }

  {ConstantIdentifier}                    { return token(TokenType.IDENTIFIER); }

  {TypeIdentifier}  { return token(TokenType.IDENTIFIER); }

  \n |
  {Identifier} |
  {WhiteSpace}                   { return token(TokenType.IDENTIFIER); }



  {Comment}   	{ return token(TokenType.COMMENT); }

}



/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }
