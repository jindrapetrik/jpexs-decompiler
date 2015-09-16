/* Flash assembler language lexer specification */

package jsyntaxpane.lexers;


import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

%%

%public
%class Flasm3MethodInfoLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token

%{

  StringBuilder string = new StringBuilder();
  boolean isMultiname=false;


    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public Flasm3MethodInfoLexer() {
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
WhiteSpace = [ \t\f]+



/* identifiers */

Identifier = [:jletter:][:jletterdigit:]*

/* integer literals */
NumberLiteral = 0 | -?[1-9][0-9]*

PositiveNumberLiteral = 0 | [1-9][0-9]*

Multiname = m\[{PositiveNumberLiteral}\]

Namespace = ns\{PositiveNumberLiteral}\]

/* floating point literals */
FloatLiteral =  -?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

OctDigit          = [0-7]

/* string and character literals */
StringCharacter = [^\r\n\"\\]

%state STRING

%%


<YYINITIAL> {
  /* whitespace */
  {WhiteSpace}                   {  }
  {Multiname}\"                   {
                                    isMultiname=true;
                                    yybegin(STRING);
                                    tokenStart = yychar;
                                    tokenLength = yylength();
                                  }
  /* string literal */
  \"                             {
                                    isMultiname=false;
                                    tokenStart = yychar;
                                    tokenLength = yylength();
                                    yybegin(STRING);
                                    string.setLength(0);
                                 }


  /* numeric literals */

  {NumberLiteral}            { return token(TokenType.NUMBER);  }
  {FloatLiteral}                 { return token(TokenType.NUMBER);  }

  ":"                     {return token(TokenType.DELIMITER);}
  ","                     {return token(TokenType.DELIMITER);}
  "..."                   {return token(TokenType.OPERATOR);}
  "*"                   {return token(TokenType.KEYWORD);}
  "="                   {return token(TokenType.OPERATOR);}

  private               {return token(TokenType.KEYWORD);}
  protected               {return token(TokenType.KEYWORD);}
  package               {return token(TokenType.KEYWORD);}
  internal               {return token(TokenType.KEYWORD);}
  static               {return token(TokenType.KEYWORD);}
  explicit               {return token(TokenType.KEYWORD);}
  {Namespace}           {
                                    return token(TokenType.IDENTIFIER);
                                  }
  true                  {return token(TokenType.KEYWORD);}
  false                  {return token(TokenType.KEYWORD);}
  null                  {return token(TokenType.KEYWORD);}
  undefined                  {return token(TokenType.KEYWORD);}
{Identifier}            {
                        return token(TokenType.IDENTIFIER);  }
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);
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
[^]                              { }
<<EOF>>                          { return null; }