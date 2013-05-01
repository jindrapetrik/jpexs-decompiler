/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags.text;


%%

%public
%class TextLexer
%final
%unicode
%char
%line
%column
%type ParsedSymbol
%throws ParseException

%{

  StringBuffer string = null;
    boolean finish=false;
    String parameterName=null;


    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public TextLexer() {

    }

    public int yychar() {
        return yychar;
    }

    public int yyline() {
        return yyline+1;
    }

%}

Parameter = [a-z0-9_]+
Value = [^ \r\n\]]+
Divider = [ \r\n]+

%state PARAMETER,VALUE

%%

<YYINITIAL> {
  "["                            {
                                    yybegin(PARAMETER);
                                    if(string!=null){
                                        String ret=string.toString();
                                        string = null;
                                        return new ParsedSymbol(SymbolType.TEXT,ret.toString());
                                    }
                                 }
  /* escape sequences */
  "\\["                          { if(string==null) string=new StringBuffer(); string.append( '[' ); }
  "\\]"                          { if(string==null) string=new StringBuffer(); string.append( ']' ); }
  "\\b"                          { if(string==null) string=new StringBuffer(); string.append( '\b' ); }
  "\\t"                          { if(string==null) string=new StringBuffer(); string.append( '\t' ); }
  "\\n"                          { if(string==null) string=new StringBuffer(); string.append( '\n' ); }
  "\\f"                          { if(string==null) string=new StringBuffer(); string.append( '\f' ); }
  "\\r"                          { if(string==null) string=new StringBuffer(); string.append( '\r' ); }
  "\\\""                         { if(string==null) string=new StringBuffer(); string.append( '\"' ); }
  "\\'"                          { if(string==null) string=new StringBuffer(); string.append( '\'' ); }
  "\\\\"                         { if(string==null) string=new StringBuffer(); string.append( '\\' ); }

  /* error cases */
  \\.                            { throw new ParseException("Illegal escape sequence \""+yytext()+"\"",yyline+1); }   
  .                              { if(string==null) string=new StringBuffer(); string.append( yytext() ); }
 <<EOF>>                         { if(finish){return null;}else{finish=true; return new ParsedSymbol(SymbolType.TEXT,string.toString());}}
}

<PARAMETER> {
    {Divider}                          {}
    {Parameter}                  {
                                    parameterName = yytext();
                                    yybegin(VALUE);
                                 }
    "]"                          {
                                    yybegin(YYINITIAL);
                                 }
}

<VALUE> {
    {Divider}                          {}
    {Value}                      {  
                                    yybegin(PARAMETER);                                    
                                    return new ParsedSymbol(SymbolType.PARAMETER,new Object[]{parameterName,yytext()});
                                 }
    "]"                          {
                                    yybegin(YYINITIAL);
                                 }
}

/* error fallback */
.|\n                             { }
<<EOF>>                          { return null; }
