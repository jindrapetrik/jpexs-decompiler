/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
/* Flash assembler language lexer specification */
package com.jpexs.decompiler.flash.action.parser.pcode;

import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;

%%

%public
%class FlasmLexer
%final
%unicode
%char
%line
%column
%type ASMParsedSymbol
%throws ActionParseException

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
        return yyline + 1;
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

Infinity = -? "Infinity"

/* integer literals */
PositiveNumberLiteral = 0 | [1-9][0-9]*
NegativeNumberLiteral = - {PositiveNumberLiteral}

NumberLiteral = {PositiveNumberLiteral}|{NegativeNumberLiteral}

/* floating point literals */        
FloatLiteral = "NaN" | {Infinity} | (({FLit1}|{FLit2}|{FLit3}) {Exponent}?)

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

HexDigit          = [0-9a-fA-F]
OctDigit          = [0-7]

/* string and character literals */
StringCharacter = [^\r\n\"\\]

Register= register{PositiveNumberLiteral}
Constant= constant{PositiveNumberLiteral}

%state STRING,PARAMETERS

%%

<YYINITIAL> {
  

  /* whitespace */
  {WhiteSpace}                   {  }

  {Label}                        {
                                    String s=yytext();
                                    return new ASMParsedSymbol(ASMParsedSymbol.TYPE_LABEL, s.substring(0, s.length() - 1));
                                }

  /* identifiers */ 
  {InstructionName}                   { yybegin(PARAMETERS);
                                        return new ASMParsedSymbol(ASMParsedSymbol.TYPE_INSTRUCTION_NAME, yytext());
                                      }
  {EndOfBlock}                        {  return new ASMParsedSymbol(ASMParsedSymbol.TYPE_BLOCK_END); }
}

<PARAMETERS> {
  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    string.setLength(0);
                                 }

  /* numeric literals */

  {NumberLiteral}            { return new ASMParsedSymbol(ASMParsedSymbol.TYPE_INTEGER, Long.parseLong((yytext())));  }
  {FloatLiteral}                 { return new ASMParsedSymbol(ASMParsedSymbol.TYPE_FLOAT, Double.parseDouble((yytext())));  }
  {LineTerminator}      {yybegin(YYINITIAL); return new ASMParsedSymbol(ASMParsedSymbol.TYPE_EOL); }
  {Comment}             {return new ASMParsedSymbol(ASMParsedSymbol.TYPE_COMMENT, yytext().substring(1));}
  {StartOfBlock}                        {  yybegin(YYINITIAL); return new ASMParsedSymbol(ASMParsedSymbol.TYPE_BLOCK_START); }
  {True}                {return new ASMParsedSymbol(ASMParsedSymbol.TYPE_BOOLEAN,Boolean.TRUE);}
  {False}                {return new ASMParsedSymbol(ASMParsedSymbol.TYPE_BOOLEAN,Boolean.FALSE);}
  {Null}                {return new ASMParsedSymbol(ASMParsedSymbol.TYPE_NULL, new Null());}
  {Undefined}                {return new ASMParsedSymbol(ASMParsedSymbol.TYPE_UNDEFINED, new Undefined());}

  {Register}              { return new ASMParsedSymbol(ASMParsedSymbol.TYPE_REGISTER, new RegisterNumber(Integer.parseInt(yytext().substring(8))));  }
  {Constant}              { return new ASMParsedSymbol(ASMParsedSymbol.TYPE_CONSTANT, new ConstantIndex(Integer.parseInt(yytext().substring(8))));  }

  {Identifier}            { return new ASMParsedSymbol(ASMParsedSymbol.TYPE_IDENTIFIER, yytext());  }
      
}

<STRING> {
  \"                             {
                                     yybegin(PARAMETERS);
                                     // length also includes the trailing quote
                                     return new ASMParsedSymbol(ASMParsedSymbol.TYPE_STRING, string.toString());
                                 }

  {StringCharacter}+             { string.append(yytext()); }

  /* escape sequences */
  "\\b"                          { string.append('\b'); }
  "\\t"                          { string.append('\t'); }
  "\\n"                          { string.append('\n'); }
  "\\f"                          { string.append('\f'); }
  "\\r"                          { string.append('\r'); }
  "\\\""                         { string.append('\"'); }
  "\\'"                          { string.append('\''); }
  "\\\\"                         { string.append('\\'); }
  \\x{HexDigit}{HexDigit}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }
  \\[0-3]?{OctDigit}?{OctDigit}  { char val = (char) Integer.parseInt(yytext().substring(1), 8);
                        				   string.append(val); }

  /* error cases */
  \\.                            { throw new ActionParseException("Illegal escape sequence \"" + yytext() + "\"", yyline + 1); }
  {LineTerminator}               { throw new ActionParseException("Unterminated string at end of line", yyline + 1); }

}

/* error fallback */
[^]                              { }
<<EOF>>                          { return new ASMParsedSymbol(ASMParsedSymbol.TYPE_EOF); }
