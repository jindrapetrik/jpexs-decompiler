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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.math.BigInteger;
import macromedia.asc.util.Decimal128;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.jpexs.decompiler.flash.abc.types.Float4;

%%

%public
%class ActionScriptLexer
%final
%unicode
%char
%type ParsedSymbol
%throws AVM2ParseException

%{

    private String sourceCode;

    private int repeatNum = 1;

    private boolean enableWhiteSpace = false;

    private final Pattern float4Pattern = Pattern.compile("float4.*\\([\r\n \t\f]*(?<f1>[^\r\n \t\f]+)[\r\n \t\f]*,[\r\n \t\f]*(?<f2>[^\r\n \t\f]+)[\r\n \t\f]*,[\r\n \t\f]*(?<f3>[^\r\n \t\f]+)[\r\n \t\f]*,[\r\n \t\f]*(?<f4>[^\r\n \t\f]+)[\r\n \t\f]*\\)", Pattern.MULTILINE);

    public ActionScriptLexer(String sourceCode){
        this(new StringReader(sourceCode));
        this.sourceCode = sourceCode;
    }

    public void yypushbackstr(String s, int state)
    {
        int numLines = count(s, "\n");
        int newYyline = yyline - numLines;
        sourceCode = s + sourceCode.substring(yychar + yylength());
        yyreset(new StringReader(sourceCode));
        yybegin(state);
        yyline = newYyline;
    }

    public void setEnableWhiteSpace(boolean enable)
    {
        this.enableWhiteSpace = enable;
    }

    public void begin(int state)
    {
        string.setLength(0);
        yybegin(state);
    }

    public void yypushbackstr(String s)
    {
        yypushbackstr(s, YYINITIAL);
    }

    StringBuilder string = new StringBuilder();

    private static String xmlTagName = "";

    public int yychar() {
        return yychar;
    }

    private Stack<ParsedSymbol> pushedBack = new Stack<>();

    public int yyline() {
        return yyline + 1;
    }

    private List<LexListener> listeners=new ArrayList<>();

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
    public ParsedSymbol lex() throws java.io.IOException, AVM2ParseException{
        ParsedSymbol ret = null;
        if (!pushedBack.isEmpty()){
            ret = last = pushedBack.pop();
        } else {
            ret = last = yylex();
        }
        informListenersLex(ret);
        return ret;
    }

    private int count(String str, String target) {
        return (str.length() - str.replace(target, "").length()) / target.length();
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


IdentFirst = [\p{Lu}\p{Ll}\p{Lt}\p{Lm}\p{Lo}_$]
IdentNext = {IdentFirst} | [\p{Nl}\p{Mn}\p{Mc}\p{Nd}\p{Pc}]


/* identifiers */
Identifier = {IdentFirst}{IdentNext}*

TypeNameSpec = ".<"

/* XML */

XmlS = (\u0020 | \u0009 | \u000D | \u000A)+

XmlCommentStart = "<!--"
XmlCommentEnd = "-->"

XmlNameStartChar = ":" | [A-Z] | "_" | [a-z]
XmlNameStartCharUnicode = [\u00C0-\u00D6]   |
        [\u00D8-\u00F6] |
        [\u00F8-\u02FF] |
        [\u0370-\u037D] |
        [\u037F-\u1FFF] |
        [\u200C-\u200D] |
        [\u2070-\u218F] |
        [\u2C00-\u2FEF] |
        [\u3001-\uD7FF] |
        [\uF900-\uFDCF] |
        [\uFDF0-\uFFFD] |
        [\u10000-\uEFFFF]

XmlNameChar = {XmlNameStartChar} | "-" | "." | [0-9] | \u00B7
XmlNameCharUnicode = [\u0300-\u036F] | [\u0203F-\u2040]
XmlName = {XmlNameStartChar} {XmlNameChar}*
XmlNameUnicode = ({XmlNameStartChar}|{XmlNameStartCharUnicode}) ({XmlNameChar}|{XmlNameCharUnicode})*

/* XML Processing Instructions */
XmlInstrStart = "<?" {XmlName}
XmlInstrEnd   = "?>"

/* CDATA  */
XmlCDataStart = "<![CDATA["
XmlCDataEnd   = "]]>"

/* Tags */
XmlOpenTagStart = "<" {XmlName}
XmlOpenTagClose = "/>"
XmlOpenTagEnd = ">"

XmlCloseTag = "</" {XmlName} {XmlS}* ">"

/* attribute */
XmlAttribute = {XmlName} "="

/* string and character literals */
XmlDQuoteStringChar = [^\r\n\"]
XmlSQuoteStringChar = [^\r\n\']



/* integer literals */
DecIntegerLiteral = (0 | [1-9][0-9]*) [ui]?

HexIntegerLiteral = 0 [xX] 0* {HexDigit}+
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit}+
OctDigit          = [0-7]

/* floating point literals */
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [md]?

FloatLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? f?

Float4Literal = float4 {WhiteSpace}* \( {WhiteSpace}* {FloatLiteral} {WhiteSpace}* , {WhiteSpace}* {FloatLiteral} {WhiteSpace}* , {WhiteSpace}* {FloatLiteral} {WhiteSpace}* , {WhiteSpace}* {FloatLiteral} {WhiteSpace}* \)

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]
OIdentifierCharacter = [^\r\n\u00A7\\]
Preprocessor = \u00A7\u00A7 {Identifier}

VerbatimStringCharacter = [^\r\n\"]
VerbatimString = "@\"" {VerbatimStringCharacter}* "\""

NamespaceSuffix = "#" {DecIntegerLiteral}

RegExp = \/([^\r\n/]|\\\/)+\/[a-z]*

%state STRING, CHARLITERAL,XMLOPENTAG,XMLCLOSETAGFINISH,XMLOPENTAGATTRIB,XMLINSTR,XMLCDATA,XMLCOMMENT,XML,OIDENTIFIER,XMLCDATAALONE,XMLCOMMENTALONE

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
  "each"                         { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.EACH, yytext()); }
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
  "dynamic"                      { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.DYNAMIC, yytext()); }
  "internal"                     { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.INTERNAL, yytext()); }
  "override"                     { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.OVERRIDE, yytext()); }
  "private"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.PRIVATE, yytext()); }
  "protected"                    { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.PROTECTED, yytext()); }
  "public"                       { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.PUBLIC, yytext()); }
  "static"                       { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.STATIC, yytext()); }
  "class"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.CLASS, yytext()); }
  "const"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.CONST, yytext()); }
  "extends"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.EXTENDS, yytext()); }
  "function"                     { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.FUNCTION, yytext()); }
  "get"                          { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.GET, yytext()); }
  "implements"                   { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.IMPLEMENTS, yytext()); }
  "interface"                    { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.INTERFACE, yytext()); }
  "namespace"                    { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.NAMESPACE, yytext()); }
  "package"                      { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.PACKAGE, yytext()); }
  "set"                          { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.SET, yytext()); }
  "var"                          { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.VAR, yytext()); }
  "import"                       { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.IMPORT, yytext()); }
  "use"                          { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.USE, yytext()); }
  "false"                        { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.FALSE, yytext()); }
  "null"                         { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.NULL, yytext()); }
  "this"                         { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.THIS, yytext()); }
  "true"                         { return new ParsedSymbol(SymbolGroup.KEYWORD, SymbolType.TRUE, yytext()); }
  "undefined"                    { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.UNDEFINED, yytext()); }
  "Infinity"                     { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.INFINITY, yytext()); }
  "NaN"                          { return new ParsedSymbol(SymbolGroup.GLOBALCONST, SymbolType.NAN, yytext()); }
  "final"                        { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.FINAL, yytext()); }
  "native"                       { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.NATIVE, yytext()); }


  {VerbatimString}               { 
                                    String verbatimString = yytext();
                                    verbatimString = verbatimString.substring(2, verbatimString.length() - 1);
                                    return new ParsedSymbol(SymbolGroup.STRING, SymbolType.STRING, verbatimString); }

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
  "?."                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.NULL_DOT, yytext());  }
  "??"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.NULL_COALESCE, yytext());  }
  "?"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.TERNAR, yytext());  }
  ":"                            { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.COLON, yytext());  }
  "==="                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.STRICT_EQUALS, yytext());  }
  "=="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.EQUALS, yytext());  }
  "<="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.LOWER_EQUAL, yytext());  }
  ">="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.GREATER_EQUAL, yytext());  }
  "!=="                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.STRICT_NOT_EQUAL, yytext());  }
  "!="                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.NOT_EQUAL, yytext());  }
  "&&="                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_AND, yytext());  }
  "&&"                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.AND, yytext());  }
  "||="                          { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ASSIGN_OR, yytext());  }
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
  ".("                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.FILTER, yytext()); }
  ".."                           { return new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.DESCENDANTS, yytext()); }

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

  {DecIntegerLiteral}            { 
                                    String ival = yytext();
                                    if (ival.endsWith("i") || ival.endsWith("u")) {
                                        ival = ival.substring(0, ival.length() - 1);
                                    }
                                    try{
                                        return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Integer.parseInt(ival)); 
                                    } catch(NumberFormatException nfe){
                                        //its too long for an Integer var
                                        return new ParsedSymbol(SymbolGroup.DOUBLE, SymbolType.DOUBLE, Double.parseDouble(ival)); 
                                    }
                                 }

  {HexIntegerLiteral}            {                                    
                                    try {
                                        return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Integer.parseInt(yytext().substring(2), 16));
                                    } catch (NumberFormatException nfe) {
                                        //its too long for an Integer var
                                        return new ParsedSymbol(SymbolGroup.DOUBLE, SymbolType.DOUBLE, new BigInteger(yytext().substring(2), 16).doubleValue()); 
                                    }
                                 }
  {OctIntegerLiteral}            { 
                                   try {
                                        return new ParsedSymbol(SymbolGroup.INTEGER, SymbolType.INTEGER, Integer.parseInt(yytext(), 8));
                                   } catch (NumberFormatException nfe) {
                                        //its too long for an Integer var
                                        return new ParsedSymbol(SymbolGroup.DOUBLE, SymbolType.DOUBLE, new BigInteger(yytext(), 8).doubleValue()); 
                                   }
                                 }  
  {DoubleLiteral}                { 
                                    String dval = yytext();
                                    if (dval.endsWith("m")) {
                                        dval = dval.substring(0, dval.length() - 1);
                                        return new ParsedSymbol(SymbolGroup.DECIMAL, SymbolType.DECIMAL, new Decimal128(dval));
                                    }
                                    if (dval.endsWith("f")) {
                                        dval = dval.substring(0, dval.length() - 1);
                                        return new ParsedSymbol(SymbolGroup.FLOAT, SymbolType.FLOAT, Float.parseFloat(dval));
                                    }
                                    if (dval.endsWith("d")) {
                                        dval = dval.substring(0, dval.length() - 1);
                                    }
                                    return new ParsedSymbol(SymbolGroup.DOUBLE, SymbolType.DOUBLE, Double.parseDouble(dval));
                                 }
{FloatLiteral}                   { 
                                    String fval = yytext();
                                    if (fval.endsWith("f")) {
                                        fval = fval.substring(0, fval.length() - 1);
                                    }
                                    return new ParsedSymbol(SymbolGroup.FLOAT, SymbolType.FLOAT, Float.parseFloat(fval));
                                 }

{Float4Literal}                  {
                                    Matcher f4Matcher = float4Pattern.matcher(yytext());
                                    f4Matcher.matches();
                                    float[] fvalues = new float[4];
                                    for (int i = 0; i < 4; i++) {
                                        String fval = f4Matcher.group("f" + (i + 1));
                                        if (fval.endsWith("f")) {
                                            fval = fval.substring(0, fval.length() - 1);
                                        }
                                        fvalues[i] = Float.parseFloat(fval);
                                    }
                                    return new ParsedSymbol(SymbolGroup.FLOAT4, SymbolType.FLOAT4, new Float4(fvalues));
                                 }
  /* comments */
  {Comment}                      { yyline += count(yytext(),"\n"); }

  {LineTerminator}               {
                                    yyline++;
                                    if (enableWhiteSpace) { return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_WHITESPACE, yytext()); }
                                 }
  /* whitespace */
  {WhiteSpace}                   { if (enableWhiteSpace) { return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_WHITESPACE, yytext()); } }
  {TypeNameSpec}                 { return new ParsedSymbol(SymbolGroup.TYPENAME, SymbolType.TYPENAME, yytext()); }
  {XmlOpenTagStart}              {
                                    yybegin(XMLOPENTAG);
                                    string.setLength(0);
                                    return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_STARTTAG_BEGIN, yytext());
                                 }
  {XmlCommentStart}              {
                                       string.setLength(0); string.append(yytext() ); yybegin(XMLCOMMENTALONE);
                                 }
  {XmlCDataStart}                {
                                       string.setLength(0); string.append(yytext() ); yybegin(XMLCDATAALONE);
                                 }
  "<{"                           {  return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_STARTVARTAG_BEGIN, yytext()); }
  /* identifiers */
  {Identifier}                   { return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.IDENTIFIER, yytext()); }
  /* regexp */
  {RegExp}                       { return new ParsedSymbol(SymbolGroup.REGEXP, SymbolType.REGEXP, yytext()); }
  {NamespaceSuffix}              { return new ParsedSymbol(SymbolGroup.NAMESPACESUFFIX, SymbolType.NAMESPACESUFFIX, Integer.parseInt(yytext().substring(1))); }   
}

<XMLCLOSETAGFINISH> {
    ">"                            { 
                                    yybegin(XML);
                                    pushback(new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.GREATER_THAN, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }

   {LineTerminator}               { string.append(yytext());  yyline++;}
   (\u0020 | \u0009)+             { string.append(yytext()); }
}

<XMLOPENTAG> {   
   {XmlAttribute}                 {
                                    yybegin(XMLOPENTAGATTRIB);
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_ATTRIBUTENAME, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }
   "{"                            {
                                    yybegin(YYINITIAL);
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_ATTRNAMEVAR_BEGIN, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }
   {XmlOpenTagEnd}                {
                                    yybegin(XML);
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_STARTTAG_END, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }
   {XmlOpenTagClose}              {
                                    yybegin(XML);
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_STARTFINISHTAG_END, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }      
   {LineTerminator}               { string.append(yytext());  yyline++;}
   {WhiteSpace}                   { string.append(yytext()); }
}



<XMLOPENTAGATTRIB> {
    \"{XmlDQuoteStringChar}*\"      {
                                        yybegin(XMLOPENTAG);
                                        return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_ATTRIBUTEVALUE, yytext());
                                    }
    \'{XmlSQuoteStringChar}*\'      {
                                        yybegin(XMLOPENTAG);
                                        return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_ATTRIBUTEVALUE_SINGLEQUOTED, yytext());
                                    }
    "{"                             {
                                      yybegin(YYINITIAL);
                                      return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_ATTRVALVAR_BEGIN, yytext());
                                    }
}


<XMLINSTR> {
   {XmlInstrEnd}                  {
                                    yybegin(XML);
                                    string.append(yytext());
                                    String tos = string.toString();
                                    string.setLength(0);
                                    return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_INSTR, tos);
                                  }
   {LineTerminator}               { string.append(yytext());  yyline++;}
   [^]                            { string.append(yytext()); }
}

<XMLCDATA> {
    {XmlCDataEnd}                 {
                                     string.append(yytext());
                                     yybegin(XML);
                                     String ret = string.toString();
                                     string.setLength(0);
                                     return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_CDATA, ret);
                                  }
 {LineTerminator}                 { string.append(yytext());  yyline++;}
    [^]                           { string.append(yytext()); }
}

<XMLCDATAALONE> {
    {XmlCDataEnd}                 {
                                     string.append(yytext());
                                     yybegin(YYINITIAL);
                                     String ret = string.toString();
                                     string.setLength(0);
                                     return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_CDATA, ret);
                                  }
 {LineTerminator}                 { string.append(yytext());  yyline++;}
    [^]                           { string.append(yytext()); }
}

<XMLCOMMENT> {
   {XmlCommentEnd}                {
                                     string.append(yytext());
                                     yybegin(XML);
                                     String ret = string.toString();
                                     string.setLength(0);
                                     return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_COMMENT, ret);
                                  }
   {LineTerminator}               { string.append(yytext()); yyline++;}
   [^]                            { string.append(yytext());}
}

<XMLCOMMENTALONE> {
   {XmlCommentEnd}                {
                                     string.append(yytext());
                                     yybegin(YYINITIAL);
                                     String ret = string.toString();
                                     string.setLength(0);
                                     return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_COMMENT, ret);
                                  }
   {LineTerminator}               { string.append(yytext()); yyline++;}
   [^]                            { string.append(yytext());}
}

<XML> {
   {XmlCDataStart}                {
                                    String ret = string.toString(); string.setLength(0); string.append(yytext() ); yybegin(XMLCDATA);
                                    if (!ret.isEmpty()) return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, ret);
                                  }
   {XmlInstrStart}                {
                                    yybegin(XMLINSTR);
                                    if (string.length() > 0){
                                       String tos = string.toString(); 
                                       string.setLength(0);
                                       string.append(yytext());
                                       return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, tos);                                       
                                    }
                                    string.append(yytext());
                                  }   
   {XmlCommentStart}                        {
                                     String ret = string.toString(); string.setLength(0); string.append(yytext()); yybegin(XMLCOMMENT);
                                     if (!ret.isEmpty()) return new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, ret);
                                  }
   {XmlOpenTagStart}              {
                                    yybegin(XMLOPENTAG);
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_STARTTAG_BEGIN, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }
   {XmlCloseTag}                  {
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_FINISHTAG, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }
  
   "<{"                           {
                                    yybegin(YYINITIAL);
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_STARTVARTAG_BEGIN, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }
   "</{"                          {
                                    yybegin(YYINITIAL);
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_FINISHVARTAG_BEGIN, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }
   "{"                            {
                                    yybegin(YYINITIAL);
                                    pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_VAR_BEGIN, yytext()));
                                    if (string.length() > 0){
                                       pushback(new ParsedSymbol(SymbolGroup.XML, SymbolType.XML_TEXT, string.toString()));
                                       string.setLength(0);
                                    }
                                    return lex();
                                  }
   {LineTerminator}               { string.append(yytext());  yyline++;}
   [^]                            { string.append(yytext()); }
}

<OIDENTIFIER> {
    "\u00A7"                         {
                                     yybegin(YYINITIAL);
                                     repeatNum = 1;
                                     // length also includes the trailing quote
                                     return new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.IDENTIFIER, string.toString());
                                 }

  {OIdentifierCharacter}         { for(int r=0;r<repeatNum;r++) string.append(yytext()); repeatNum = 1;}

  /* escape sequences */
  "\\b"                          { for(int r=0;r<repeatNum;r++) string.append('\b'); repeatNum = 1;}
  "\\t"                          { for(int r=0;r<repeatNum;r++) string.append('\t'); repeatNum = 1;}
  "\\n"                          { for(int r=0;r<repeatNum;r++) string.append('\n'); repeatNum = 1;}
  "\\f"                          { for(int r=0;r<repeatNum;r++) string.append('\f'); repeatNum = 1;}
  "\\\u00A7"                     { for(int r=0;r<repeatNum;r++) string.append('\u00A7'); repeatNum = 1;}
  "\\r"                          { for(int r=0;r<repeatNum;r++) string.append('\r'); repeatNum = 1;}
  "\\\\"                         { for(int r=0;r<repeatNum;r++) string.append('\\'); repeatNum = 1;}
  \\x{HexDigit}{2}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   for(int r=0;r<repeatNum;r++) string.append(val); repeatNum = 1;}
  \\u{HexDigit}{4}        { char val = (char) Integer.parseInt(yytext().substring(2), 16);
                        				   for(int r=0;r<repeatNum;r++) string.append(val); repeatNum = 1;}
  \\\{{DecIntegerLiteral}\}      { repeatNum = Integer.parseInt(yytext().substring(2, yytext().length()-1)); }

  /* escape sequences */

  \\.                            { throw new AVM2ParseException("Illegal escape sequence \"" + yytext() + "\"", yyline + 1);  }
  {LineTerminator}               { yybegin(YYINITIAL);  yyline++;}
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     String tos = string.toString();
                                     string.setLength(0);
                                     return new ParsedSymbol(SymbolGroup.STRING, SymbolType.STRING, tos);
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

  /* escape sequences */

  \\.                            { /* ignore illegal character escape */ }
  {LineTerminator}               { yybegin(YYINITIAL); yyline++;}
}

<CHARLITERAL> {
  \'                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return new ParsedSymbol(SymbolGroup.STRING, SymbolType.STRING, string.toString());
                                 }

  {SingleCharacter}+             { string.append(yytext()); }

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

  \\.                            { /* ignore illegal character escape */ }
  {LineTerminator}               { yybegin(YYINITIAL);  yyline++;}
}

/* error fallback */
[^]                              {  }
<<EOF>>                          { return new ParsedSymbol(SymbolGroup.EOF, SymbolType.EOF, null); }
