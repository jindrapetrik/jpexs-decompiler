/* Flash assembler language lexer specification */

package com.jpexs.decompiler.flash.helpers.hilight;

import java.util.Stack;
%%

%public
%class HilightLexer
%final
%unicode
%char
%line
%column
%type HilightToken
%throws ParseException

%{

  StringBuffer string = new StringBuffer();

    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public HilightLexer() {

    }

    public int yychar() {
        return yychar;
    }

    public int yyline() {
        return yyline+1;
    }

    private boolean doHilightEnd=false;

    private Stack<HilightToken> pushedBack=new Stack<>();

    private int tokenLength=0;

    public void pushforward(HilightToken symb) {
        pushedBack.add(0,symb);
    }

    public void pushback(HilightToken symb) {
        pushedBack.push(symb);
    }
    public HilightToken lex() throws java.io.IOException, ParseException{
        HilightToken ret=null;
        if(!pushedBack.isEmpty()){
            ret = pushedBack.pop();
        }else{
            ret = yylex();
        }
        return ret;
    }

%}

StringCharacter = [^\r\n\"\\]
LineTerminator = \r|\n|\r\n

%state STRING

%%

<YYINITIAL> {

  /* string literal */
  "<ffdec:\""                    {
                                    if(string.length()>0)
                                        pushforward(new HilightToken(TokenType.TEXT,string.toString()));                                    
                                    yybegin(STRING);
                                    string.setLength(0);
                                    tokenLength=yylength();
                                 }
  "</ffdec>"                     {  
                                    if(string.length()>0)
                                    pushforward(new HilightToken(TokenType.TEXT,string.toString()));
                                    string.setLength(0);
                                    pushforward(new HilightToken(TokenType.HILIGHTEND,yytext()));
                                    return lex();
                                 }
  .|\n                           {                                    
                                    string.append( yytext() );
                                 }
  
}

<STRING> {
  \">                             {
                                     yybegin(YYINITIAL);
                                     tokenLength++;
                                     pushforward(new HilightToken(TokenType.HILIGHTSTART,string.toString(),tokenLength));
                                     string.setLength(0);
                                     return lex();
                                 }

  {StringCharacter}+             { tokenLength++; string.append( yytext() ); }

  /* escape sequences */
  "\\b"                          { tokenLength+=2; string.append( '\b' ); }
  "\\t"                          { tokenLength+=2; string.append( '\t' ); }
  "\\n"                          { tokenLength+=2; string.append( '\n' ); }
  "\\f"                          { tokenLength+=2; string.append( '\f' ); }
  "\\r"                          { tokenLength+=2; string.append( '\r' ); }
  "\\\""                         { tokenLength+=2; string.append( '\"' ); }
  "\\'"                          { tokenLength+=2; string.append( '\'' ); }
  "\\\\"                         { tokenLength+=2; string.append( '\\' ); }
  
  /* error cases */
  \\.                            { throw new ParseException("Illegal escape sequence \""+yytext()+"\"",yyline+1); }
  {LineTerminator}               { throw new ParseException("Unterminated string at end of line",yyline+1); }

}

<<EOF>>                          { 
                                    if(string.length()>0){
                                       pushforward(new HilightToken(TokenType.TEXT,string.toString()));                                    
                                       string.setLength(0);
                                    }
                                    pushforward(new HilightToken(TokenType.EOF));
                                    return lex();
                                  }
