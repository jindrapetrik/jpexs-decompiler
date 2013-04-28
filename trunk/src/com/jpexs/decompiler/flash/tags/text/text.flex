/* Flash assembler language lexer specification */

package com.jpexs.decompiler.flash.tags.text;

import java.util.regex.*;

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

PositiveNumber = 0 | [1-9][0-9]*
NegativeNumber = - {PositiveNumber}
Number = {PositiveNumber}|{NegativeNumber}
Hex = [0-9a-f][0-9a-f]

Font = "[font " {PositiveNumber} " height " {PositiveNumber} "]"
Color = "[color #" {Hex}{Hex}{Hex} "]"
ColorA = "[color #" {Hex}{Hex}{Hex}{Hex} "]"
X = "[x " {Number} "]"
Y = "[y " {Number} "]"


%%

<YYINITIAL> {
  {Font}                         {
                                    if(string==null){
                                        Pattern pat = Pattern.compile("\\[font ([0-9]+) height ([0-9]+)\\]");
                                        Matcher m=pat.matcher(yytext());
                                        if(m.matches()){
                                            return new ParsedSymbol(SymbolType.FONT,Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)));
                                        }
                                    }else{
                                        yypushback(yylength());
                                        String ret=string.toString();
                                        string = null;
                                        return new ParsedSymbol(SymbolType.TEXT,ret.toString());
                                    }
                                 }
  {Color}|{ColorA}               {
                                    if(string==null){
                                        return new ParsedSymbol(SymbolType.COLOR,yytext().substring(8,yytext().length()-1));
                                    }else{
                                        yypushback(yylength());
                                        String ret=string.toString();
                                        string = null;
                                        return new ParsedSymbol(SymbolType.TEXT,ret.toString());
                                    }
                                 }
  {X}                            {
                                    if(string==null){
                                        return new ParsedSymbol(SymbolType.X,Integer.parseInt(yytext().substring(3,yytext().length()-1)));
                                    }else{
                                        yypushback(yylength());
                                        String ret=string.toString();
                                        string = null;
                                        return new ParsedSymbol(SymbolType.TEXT,ret.toString());
                                    }
                                 }
  {Y}                            {
                                    if(string==null){
                                        return new ParsedSymbol(SymbolType.Y,Integer.parseInt(yytext().substring(3,yytext().length()-1)));
                                    }else{
                                        yypushback(yylength());
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

/* error fallback */
.|\n                             { }
<<EOF>>                          { return null; }
