/* Flash assembler language lexer specification */

package com.jpexs.asdec.action.parser;

import com.jpexs.asdec.action.swf4.ConstantIndex;
import com.jpexs.asdec.action.swf4.RegisterNumber;
import com.jpexs.asdec.action.swf4.Undefined;
import com.jpexs.asdec.action.swf4.Null;

%%

%public
%class FlasmLexer
%final
%unicode
%char
%line
%column
%type ParsedSymbol
%throws ParseException

%{

  StringBuffer string = new StringBuffer();

    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public FlasmLexer() {

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


/* identifiers */

Identifier = [:jletter:][:jletterdigit:]*

InstructionName = [a-zA-Z][a-zA-Z0-9_]*

Label = {Identifier}:

StartOfBlock = "{"

EndOfBlock = "}"

True = "true"
False = "false"
False = "false"
Null = "null"
Undefined = "undefined"


/* integer literals */
NumberLiteral = 0 | [1-9][0-9]*
   
/* floating point literals */        
FloatLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

OctDigit          = [0-7]

/* string and character literals */
StringCharacter = [^\r\n\"\\]

Register= register{NumberLiteral}

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
  {EndOfBlock}                        {  return new ParsedSymbol(ParsedSymbol.TYPE_BLOCK_END); }
}

<PARAMETERS> {
  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    string.setLength(0);
                                 }

  /* numeric literals */

  {NumberLiteral}            { return new ParsedSymbol(ParsedSymbol.TYPE_INTEGER,new Long(Long.parseLong((yytext()))));  }
  {FloatLiteral}                 { return new ParsedSymbol(ParsedSymbol.TYPE_FLOAT,new Double(Double.parseDouble((yytext()))));  }
  {LineTerminator}      {yybegin(YYINITIAL); return new ParsedSymbol(ParsedSymbol.TYPE_EOL); }
  {Comment}             {return new ParsedSymbol(ParsedSymbol.TYPE_COMMENT,yytext().substring(1));}
  {StartOfBlock}                        {  return new ParsedSymbol(ParsedSymbol.TYPE_BLOCK_START); }
  {True}                {return new ParsedSymbol(ParsedSymbol.TYPE_BOOLEAN,Boolean.TRUE);}
  {False}                {return new ParsedSymbol(ParsedSymbol.TYPE_BOOLEAN,Boolean.FALSE);}
  {Null}                {return new ParsedSymbol(ParsedSymbol.TYPE_NULL,new Null());}
  {Undefined}                {return new ParsedSymbol(ParsedSymbol.TYPE_UNDEFINED,new Undefined());}

  {Register}              { return new ParsedSymbol(ParsedSymbol.TYPE_REGISTER,new RegisterNumber(Integer.parseInt(yytext().substring(8))));  }
  {Identifier}            { return new ParsedSymbol(ParsedSymbol.TYPE_IDENTIFIER,yytext());  }
      
}

<STRING> {
  \"                             {
                                     yybegin(PARAMETERS);
                                     // length also includes the trailing quote
                                     return new ParsedSymbol(ParsedSymbol.TYPE_STRING,string.toString());
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
