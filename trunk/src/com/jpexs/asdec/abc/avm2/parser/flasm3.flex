/* Flash assembler language lexer specification */

package com.jpexs.asdec.abc.avm2.parser;

%%

%public
%class Flasm3Lexer
%final
%unicode
%char
%line
%column
%type ParsedSymbol
%throws ParseException

%{

  StringBuffer string = new StringBuffer();
  boolean isMultiname=false;
  long multinameId=0;


    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public Flasm3Lexer() {

    }

    public int yychar() {
        return yychar;
    }

    public int yyline() {
        return yyline+1;
    }

%}

/* main character classes */
LineTerminator = \r|\n|\r\n

InputCharacter = [^\r\n]
Comment = ";" {InputCharacter}*

WhiteSpace = [ \t\f]+

Multiname = m\[[0-9]+\]

/* identifiers */

Identifier = [:jletter:][:jletterdigit:]*

InstructionName = [a-z][a-z0-9_]*

Label = {Identifier}:



/* integer literals */
NumberLiteral = 0 | -?[1-9][0-9]*
   
/* floating point literals */        
FloatLiteral =  -?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

OctDigit          = [0-7]

/* string and character literals */
StringCharacter = [^\r\n\"\\]

%state STRING,PARAMETERS

%%

<YYINITIAL> {
  

  /* whitespace */
  {WhiteSpace}                   {  }

  {Label}                        {
                                    String s=yytext();
                                    return new ParsedSymbol(ParsedSymbol.TYPE_LABEL,s.substring(0,s.length()-1));
                                }

  /* identifiers */ 
  {InstructionName}                   { yybegin(PARAMETERS);
                                        return new ParsedSymbol(ParsedSymbol.TYPE_INSTRUCTION_NAME,yytext());
                                      }
}

<PARAMETERS> {
  /* string literal */
  \"                             {
                                    isMultiname=false;
                                    yybegin(STRING);
                                    string.setLength(0);
                                 }
  {Multiname}\"                   {
                                    isMultiname=true;
                                    String s=yytext();
                                    multinameId=Long.parseLong(s.substring(2,s.length()-2));
                                    yybegin(STRING);
                                    string.setLength(0);
                                  }

  /* numeric literals */

  {NumberLiteral}            { return new ParsedSymbol(ParsedSymbol.TYPE_INTEGER,new Long(Long.parseLong((yytext()))));  }
  {FloatLiteral}                 { return new ParsedSymbol(ParsedSymbol.TYPE_FLOAT,new Double(Double.parseDouble((yytext()))));  }
  {Identifier}            { return new ParsedSymbol(ParsedSymbol.TYPE_IDENTIFIER,yytext());  }
  {LineTerminator}      {yybegin(YYINITIAL);}
  {Comment}             {return new ParsedSymbol(ParsedSymbol.TYPE_COMMENT,yytext().substring(1));}
}

<STRING> {
  \"                             {
                                     yybegin(PARAMETERS);
                                     // length also includes the trailing quote
                                     if(isMultiname){
                                        return new ParsedSymbol(ParsedSymbol.TYPE_MULTINAME,new Long(multinameId));
                                     }else{
                                        return new ParsedSymbol(ParsedSymbol.TYPE_STRING,string.toString());
                                     }
                                 }

  {StringCharacter}+             { string.append( yytext() ); }

  /* escape sequences */
  "\\b"                          { string.append( '\b' ); }
  "\\t"                          { string.append( '\t' ); }
  "\\n"                          { string.append( '\n' ); }
  "\\f"                          { string.append( '\f' ); }
  "\\r"                          { string.append( '\r' ); }
  "\\\""                         { string.append( '\"' ); }
  "\\'"                          { string.append( '\'' ); }
  "\\\\"                         { string.append( '\\' ); }
  \\[0-3]?{OctDigit}?{OctDigit}  { char val = (char) Integer.parseInt(yytext().substring(1),8);
                        				   string.append( val ); }

  /* error cases */
  \\.                            { throw new ParseException("Illegal escape sequence \""+yytext()+"\"",yyline+1); }
  {LineTerminator}               { throw new ParseException("Unterminated string at end of line",yyline+1); }

}

/* error fallback */
.|\n                             { }
<<EOF>>                          { return new ParsedSymbol(ParsedSymbol.TYPE_EOF); }
