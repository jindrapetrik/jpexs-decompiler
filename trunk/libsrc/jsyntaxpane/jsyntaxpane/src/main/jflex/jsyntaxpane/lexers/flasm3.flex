/* Flash assembler language lexer specification */

package jsyntaxpane.lexers;


import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

%%

%public
%class Flasm3Lexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token

%{

  StringBuffer string = new StringBuffer();
  boolean isMultiname=false;


    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public Flasm3Lexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

%}

/* main character classes */
LineTerminator = \r|\n|\r\n

WhiteSpace = [ \t\f]+

Multiname = m\[[0-9]+\]

/* identifiers */

Identifier = [:jletter:][:jletterdigit:]*

InstructionName = [a-z][a-z0-9_]*

Label = {Identifier}:



/* integer literals */
NumberLiteral = 0 | -?[1-9][0-9]*
PositiveNumberLiteral = 0 | [1-9][0-9]*

/* floating point literals */        
FloatLiteral = -?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

OctDigit          = [0-7]

InputCharacter = [^\r\n]
Comment = ";" {InputCharacter}* {LineTerminator}?

/* string and character literals */
StringCharacter = [^\r\n\"\\]

ExceptionStart = "exceptionstart"{PositiveNumberLiteral}":"
ExceptionEnd = "exceptionend "{PositiveNumberLiteral}":"
ExceptionTarget = "exceptiontarget "{PositiveNumberLiteral}":"

%state STRING,PARAMETERS

%%

<YYINITIAL> {
  

  /* whitespace */
  {WhiteSpace}                   {  }

  {ExceptionStart}              {
                                   return token(TokenType.KEYWORD);
                                }
  {ExceptionEnd}              {
                                   return token(TokenType.KEYWORD);
                                }
  {ExceptionTarget}              {
                                   return token(TokenType.KEYWORD);
                                }


  {Label}                        {return token(TokenType.IDENTIFIER,yychar,yylength()-1); }

  /* identifiers */ 
  {InstructionName}                   { yybegin(PARAMETERS);
                                        return token(TokenType.KEYWORD); }
}

<PARAMETERS> {
  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                    isMultiname=false;
                                 }
  {Multiname}\"                   {
                                    isMultiname=true;
                                    yybegin(STRING);
                                    tokenStart = yychar;
                                    tokenLength = yylength(); }

  /* numeric literals */

  {NumberLiteral}            { return token(TokenType.NUMBER);  }
  {FloatLiteral}                 { return token(TokenType.NUMBER);  }
  {Identifier}            { return token(TokenType.IDENTIFIER);  }
  {LineTerminator}      {yybegin(YYINITIAL);}
  {Comment}             {yybegin(YYINITIAL); return token(TokenType.COMMENT);}
}

<STRING> {
  \"                             {
                                     yybegin(PARAMETERS);
                                     // length also includes the trailing quote
                                     if(isMultiname){
                                        return token(TokenType.IDENTIFIER, tokenStart, tokenLength + 1);//multiname
                                     }else{
                                        return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                     }
                                 }

  {StringCharacter}+             { tokenLength += yylength(); }

  \\[0-3]?{OctDigit}?{OctDigit}  { tokenLength += yylength(); }

  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);
                                   return token(TokenType.ERROR,tokenStart, tokenLength);}
}

/* error fallback */
.|\n                             { }
<<EOF>>                          { return null; }
