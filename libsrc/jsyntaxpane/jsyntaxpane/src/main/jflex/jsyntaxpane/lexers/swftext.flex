/* Flash assembler language lexer specification */

package jsyntaxpane.lexers;


import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

%%

%public
%class SWFTextLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token

%{

  StringBuilder string = new StringBuilder();
  private int tokenStart = -1;

  private static final byte BRACKET   = 1;
    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public SWFTextLexer() {

    }

    public int yychar() {
        return yychar;
    }

%}


Parameter = [a-z_][a-z0-9_]*
Value = [^ \r\n\"\u00A7\]]+
Divider = [ \r\n]+
StringCharacter = [^\r\n\"\\]
OctDigit          = [0-7]
LineTerminator = \r|\n|\r\n
OIdentifierCharacter = [^\r\n\u00A7\\]

%state PARAMETER,VALUE,STRING,OIDENTIFIER

%%

<YYINITIAL> {
  "["                            {                                    
                                    if(tokenLength>0){  
                                        int lenret=tokenLength;
                                        tokenLength = 0;
                                        yypushback(yylength());
                                        return token(TokenType.STRING, tokenStart, lenret);
                                    }else{
                                        yybegin(PARAMETER);
                                        return token(TokenType.OPERATOR,  BRACKET);
                                    }
                                 }
  "\\["                          { tokenLength += yylength(); }
  .|\n                           { if(tokenStart==-1){tokenStart=yychar();} tokenLength += yylength(); }
 <<EOF>>                         { if(tokenStart==-1){tokenStart=0;} if(tokenLength == 0){return null;}else{int retlen=tokenLength; tokenLength=0; return token(TokenType.STRING, tokenStart, retlen);}}
}

<PARAMETER> {
    {Divider}                          {}
    {Parameter}                  {
                                    yybegin(VALUE);
                                    return token(TokenType.KEYWORD); 
                                 }
    "]"                          {
                                    yybegin(YYINITIAL);
                                    tokenStart = -1;
                                    return token(TokenType.OPERATOR,  -BRACKET);
                                 }
}

<VALUE> {
    {Divider}                    {}
    {Parameter}                  {
                                    return token(TokenType.KEYWORD); 
                                 }
    \"                           { yybegin(STRING);
                                   tokenStart = yychar;
                                   tokenLength = 1; 
                                 }
    "\u00A7"                     { yybegin(OIDENTIFIER);
                                   tokenStart = yychar;
                                   tokenLength = 1; 
                                 }
    {Value}                      {
                                    return token(TokenType.NUMBER);
                                 }
    "]"                          {
                                    yybegin(YYINITIAL);
                                    tokenStart = -1;
                                    return token(TokenType.OPERATOR,  -BRACKET);
                                 }    
}

<STRING> {
  \"                             {
                                     yybegin(VALUE);
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                 }

  {StringCharacter}+             { tokenLength += yylength(); }

  \\[0-3]?{OctDigit}?{OctDigit}  { tokenLength += yylength(); }

  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(PARAMETER);  }
}

<OIDENTIFIER> {
  "\u00A7"                            {
                                     yybegin(PARAMETER);
                                     // length also includes the trailing quote
                                     return token(TokenType.REGEX, tokenStart, tokenLength + 1);
                                 }

  {OIdentifierCharacter}+             { tokenLength += yylength(); }


  /* escape sequences */

  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(PARAMETER);  }
}

/* error fallback */
[^]                              { }
<<EOF>>                          { return null; }
