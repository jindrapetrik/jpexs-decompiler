/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action.parser.script;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

%%

%public
%class ActionScriptLexer
%final
%unicode
%char
%type ParsedSymbol
%throws ActionParseException

%{

    StringBuilder string = new StringBuilder();

    private static String xmlTagName = "";

    public int yychar() {
        return yychar;
    }

    private final Stack<ParsedSymbol> pushedBack = new Stack<>();

    public int yyline() {
        return yyline + 1;
    }

    private final List<LexListener> listeners = new ArrayList<>();

    public void addListener(LexListener listener){
        listeners.add(listener);
    }

    public void removeListener(LexListener listener){
        listeners.remove(listener);
    }

    public void informListenersLex(ParsedSymbol s){
        for(LexListener l:listeners){
            l.onLex(s);
        }
    }

    public void informListenersPushBack(ParsedSymbol s){
        for(LexListener l:listeners){
            l.onPushBack(s);
        }
    }

    public void pushback(ParsedSymbol symb) {
        pushedBack.push(symb);
        last = null;
        informListenersPushBack(symb);
    }

    ParsedSymbol last;
    public ParsedSymbol lex() throws java.io.IOException, ActionParseException{
        ParsedSymbol ret = null;
        if (!pushedBack.isEmpty()){
            ret = last = pushedBack.pop();
        } else {
            ret = last = yylex();
        }
        informListenersLex(ret);
        return ret;
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
Identifier = [:jletter:][:jletterdigit:]*

IdentifierOrParent = {Identifier} | ".."

Path = "/" | "/"? {IdentifierOrParent} ("/" {IdentifierOrParent})* "/"?

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctDigit          = [0-7]

/* floating point literals */
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]
OIdentifierCharacter = [^\r\n\u00A7\\]

Preprocessor = \u00A7\u00A7 {Identifier}

%state STRING, CHARLITERAL, XMLSTARTTAG, XML, OIDENTIFIER

%%

<YYINITIAL> {

  /* keywords */
  "break"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.BREAK, yytext()); }
  "case"                         { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.CASE, yytext()); }
  "continue"                     { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.CONTINUE, yytext()); }
  "default"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.DEFAULT, yytext()); }
  "do"                           { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.DO, yytext()); }
  "while"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.WHILE, yytext()); }
  "else"                         { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.ELSE, yytext()); }
  "for"                          { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.FOR, yytext()); }
  "each"                         { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.EACH, yytext()); }
  "in"                           { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.IN, yytext()); }
  "if"                           { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.IF, yytext()); }
  "return"                       { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.RETURN, yytext()); }
  "super"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.SUPER, yytext()); }
  "switch"                       { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.SWITCH, yytext()); }
  "throw"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.THROW, yytext()); }
  "try"                          { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.TRY, yytext()); }
  "catch"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.CATCH, yytext()); }
  "finally"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.FINALLY, yytext()); }
  "while"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.WHILE, yytext()); }
  "with"                         { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.WITH, yytext()); }
  "dynamic"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.DYNAMIC, yytext()); }
  "private"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.PRIVATE, yytext()); }
  "protected"                    { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.PROTECTED, yytext()); }
  "public"                       { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.PUBLIC, yytext()); }
  "static"                       { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.STATIC, yytext()); }
  "class"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.CLASS, yytext()); }
  "const"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.CONST, yytext()); }
  "extends"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.EXTENDS, yytext()); }
  "function"                     { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.FUNCTION, yytext()); }
  "get"                          { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.GET, yytext()); }
  "implements"                   { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.IMPLEMENTS, yytext()); }
  "interface"                    { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.INTERFACE, yytext()); }
  "namespace"                    { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.NAMESPACE, yytext()); }
  "package"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.PACKAGE, yytext()); }
  "set"                          { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.SET, yytext()); }
  "var"                          { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.VAR, yytext()); }
  "import"                       { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.IMPORT, yytext()); }
  "false"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.FALSE, yytext()); }
  "null"                         { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.NULL, yytext()); }
  "this"                         { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.THIS, yytext()); }
  "true"                         { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.TRUE, yytext()); }
  "getUrl"                       { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.GETURL, yytext()); }
  "trace"                        { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.TRACE, yytext()); }
  "gotoAndStop"                  { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.GOTOANDSTOP, yytext()); }
  "nextFrame"                    { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.NEXTFRAME, yytext()); }
  "play"                         { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.PLAY, yytext()); }
  "prevFrame"                    { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.PREVFRAME, yytext()); }
  "tellTarget"                   { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.TELLTARGET, yytext()); }
  "stop"                         { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.STOP, yytext()); }
  "stopAllSounds"                { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.STOPALLSOUNDS, yytext()); }
  "toggleHighQuality"            { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.TOGGLEHIGHQUALITY, yytext()); }
  "ifFrameLoaded"                { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.IFFRAMELOADED, yytext()); }
  "ord"                          { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.ORD, yytext()); }
  "chr"                          { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.CHR, yytext()); }
  "duplicateMovieClip"           { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.DUPLICATEMOVIECLIP, yytext()); }
  "stopDrag"                     { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.STOPDRAG, yytext()); }
  "getTimer"                     { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.GETTIMER, yytext()); }
  "loadVariables"                { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.LOADVARIABLES, yytext()); }
  "loadMovie"                    { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.LOADMOVIE, yytext()); }
  "gotoAndPlay"                  { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.GOTOANDPLAY, yytext()); }
  "mbord"                        { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.MBORD, yytext()); }
  "mbchr"                        { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.MBCHR, yytext()); }
  "mblength"                     { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.MBLENGTH, yytext()); }
  "mbsubstring"                  { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.MBSUBSTRING, yytext()); }
  "random"                       { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.RANDOM, yytext()); }
  "removeMovieClip"              { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.REMOVEMOVIECLIP, yytext()); }
  "startDrag"                    { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.STARTDRAG, yytext()); }
  "substr"                       { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.SUBSTR, yytext()); }
  "length"                       { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.LENGTH, yytext()); }
  "int"                          { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.INT, yytext()); }
  "targetPath"                   { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.TARGETPATH, yytext()); }
  "Number"                       { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.NUMBER_OP, yytext()); }
  "String"                       { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.STRING_OP, yytext()); }
  "eval"                         { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.EVAL, yytext()); }
  "undefined"                    { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.UNDEFINED, yytext()); }
  "newline"                      { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.NEWLINE, yytext()); }
  "Infinity"                     { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.INFINITY, yytext()); }
  "NaN"                          { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.NAN, yytext()); }
  "getVersion"                   { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.GETVERSION, yytext()); }
  "call"                         { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.CALL, yytext()); }
  "loadMovieNum"                 { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.LOADMOVIENUM, yytext()); }
  "loadVariablesNum"             { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.LOADVARIABLESNUM, yytext()); }
  "printAsBitmapNum"             { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.PRINTASBITMAPNUM, yytext()); }
  "printNum"                     { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.PRINTNUM, yytext()); }
  "printAsBitmap"                { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.PRINTASBITMAP, yytext()); }
  "print"                        { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.PRINT, yytext()); }
  "unloadMovie"                  { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.UNLOADMOVIE, yytext()); }
  "unloadMovieNum"               { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.UNLOADMOVIENUM, yytext()); }
  "fscommand"                    { return new ParsedSymbol(SymbolGroup.GLOBALFUNC, SymbolType.FSCOMMAND, yytext()); }


  /* operators */

  "("                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.PARENT_OPEN, yytext());  }
  ")"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.PARENT_CLOSE, yytext());  }
  "{"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.CURLY_OPEN, yytext());  }
  "}"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.CURLY_CLOSE, yytext());  }
  "["                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.BRACKET_OPEN, yytext());  }
  "]"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.BRACKET_CLOSE, yytext());  }
  ";"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.SEMICOLON, yytext());  }
  ","                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.COMMA, yytext());  }
  "..."                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.REST, yytext());  }
  "."                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.DOT, yytext());  }
  "="                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN, yytext());  }
  ">"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.GREATER_THAN, yytext());  }
  "<"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.LOWER_THAN, yytext());  }
  "!"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.NOT, yytext());  }
  "~"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.NEGATE, yytext());  }
  "?"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.TERNAR, yytext());  }
  ":"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.COLON, yytext());  }
  "==="                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.STRICT_EQUALS, yytext());  }
  "=="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.EQUALS, yytext());  }
  "<="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.LOWER_EQUAL, yytext());  }
  ">="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.GREATER_EQUAL, yytext());  }
  "!=="                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.STRICT_NOT_EQUAL, yytext());  }
  "!=" | "<>"                    { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.NOT_EQUAL, yytext());  }
  "&&"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.AND, yytext());  }
  "||"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.OR, yytext());  }
  "++"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.INCREMENT, yytext());  }
  "--"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.DECREMENT, yytext());  }
  "+"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.PLUS, yytext());  }
  "-"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.MINUS, yytext());  }
  "*"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.MULTIPLY, yytext());  }
  "/"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.DIVIDE, yytext());  }
  "&"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.BITAND, yytext());  }
  "|"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.BITOR, yytext());  }
  "^"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.XOR, yytext());  }
  "%"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.MODULO, yytext());  }
  "<<"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.SHIFT_LEFT, yytext());  }
  ">>"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.SHIFT_RIGHT, yytext());  }
  ">>>"                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.USHIFT_RIGHT, yytext());  }
  "+="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_PLUS, yytext());  }
  "-="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_MINUS, yytext());  }
  "*="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_MULTIPLY, yytext());  }
  "/="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_DIVIDE, yytext());  }
  "&="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_BITAND, yytext());  }
  "|="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_BITOR, yytext());  }
  "^="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_XOR, yytext());  }
  "%="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_MODULO, yytext());  }
  "<<="                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_SHIFT_LEFT, yytext());  }
  ">>="                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_SHIFT_RIGHT, yytext());  }
  ">>>="                         { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_USHIFT_RIGHT, yytext());  }
  "as"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.AS, yytext());  }
  "delete"                       { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.DELETE, yytext());  }
  "instanceof"                   { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.INSTANCEOF, yytext());  }
  "is"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.IS, yytext());  }
  "::"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.NAMESPACE_OP, yytext());  }
  "new"                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.NEW, yytext());  }
  "typeof"                       { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.TYPEOF, yytext());  }
  "void"                         { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.VOID, yytext());  }
  "@"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ATTRIBUTE, yytext());  }
  "and"                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.FULLAND, yytext());  }
  "or"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.FULLOR, yytext());  }

  /* string literal */
  \"                             {
                                    string.setLength(0);
                                    yybegin(STRING);
                                 }
  {Preprocessor}                 {
                                    return new ParsedSymbol(SymbolGroup.PREPROCESSOR, SymbolType.PREPROCESSOR, yytext().substring(2));
                                 }

  "\u00A7"                       {
                                    string.setLength(0);
                                    yybegin(OIDENTIFIER);
                                 }


  /* character literal */
  \'                             {
                                    string.setLength(0);
                                    yybegin(CHARLITERAL);
                                 }

  /* numeric literals */

  {DecIntegerLiteral}            { return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Long.parseLong((yytext()))); }

  {HexIntegerLiteral}            { return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Long.parseLong(yytext().substring(2), 16)); }

  {OctIntegerLiteral}            { return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Long.parseLong(yytext(), 8)); }

  {DoubleLiteral}                { return new ParsedSymbol(SymbolGroup.DOUBLE, SymbolType.DOUBLE, Double.parseDouble((yytext()))); }

  /* comments */
  {Comment}                      { /*ignore*/ }

  {LineTerminator}               { yyline++;}
  /* whitespace */
  {WhiteSpace}                   { /*ignore*/ }
  /* identifiers */
  {Identifier}                   { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.IDENTIFIER, yytext()); }
  {Path}                         { return new ParsedSymbol(SymbolGroup.PATH, SymbolType.PATH, yytext()); }
}


<OIDENTIFIER> {
    "\u00A7"                         {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.IDENTIFIER, string.toString());
                                 }

  {OIdentifierCharacter}+             { string.append(yytext()); }

  /* escape sequences */
  "\\b"                          { string.append('\b'); }
  "\\t"                          { string.append('\t'); }
  "\\n"                          { string.append('\n'); }
  "\\f"                          { string.append('\f'); }
  "\\\u00A7"                     { string.append('\u00A7'); }
  "\\r"                          { string.append('\r'); }
  "\\\\"                         { string.append('\\'); }
  \\x{HexDigit}{2}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }
  \\u{HexDigit}{4}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }

  /* escape sequences */

  \\.                            { throw new ActionParseException("Illegal escape sequence \"" + yytext() + "\"", yyline + 1);  }
  {LineTerminator}               { yybegin(YYINITIAL);  yyline++;}
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return new ParsedSymbol(SymbolGroup.STRING, SymbolType.STRING, string.toString());
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
  \\x{HexDigit}{2}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }
  \\u{HexDigit}{4}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }
  \\{OctDigit}{3}         { char val = (char) Integer.parseInt(yytext().substring(1), 8);
                        				   string.append(val); }

  /* escape sequences */

  \\.                            { string.append('\\'); /*illegal escape sequence*/  }
  {LineTerminator}               { yybegin(YYINITIAL);  yyline++;}
}

<CHARLITERAL> {
  \'                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return new ParsedSymbol(SymbolGroup.STRING, SymbolType.STRING, string.toString());
                                 }

  {SingleCharacter}+             { string.append(yytext()); }

  /* escape sequences */
/* escape sequences */
  "\\b"                          { string.append('\b'); }
  "\\t"                          { string.append('\t'); }
  "\\n"                          { string.append('\n'); }
  "\\f"                          { string.append('\f'); }
  "\\r"                          { string.append('\r'); }
  "\\\""                         { string.append('\"'); }
  "\\'"                          { string.append('\''); }
  "\\\\"                         { string.append('\\'); }
  \\x{HexDigit}{2}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }
  \\u{HexDigit}{4}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   string.append(val); }

  /* escape sequences */

  \\.                            { string.append('\\'); /*illegal escape sequence*/  }
  {LineTerminator}               { yybegin(YYINITIAL);  yyline++;}
}

/* error fallback */
[^]                              {  }
<<EOF>>                          { return new ParsedSymbol(SymbolGroup.EOF, SymbolType.EOF, null); }
