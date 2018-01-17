/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.graph.GraphTargetItem;

/**
 *
 * @author JPEXS
 */
public enum SymbolType {
    //Keywords

    BREAK,
    CASE,
    CONTINUE,
    DEFAULT,
    DO,
    WHILE,
    ELSE,
    FOR,
    EACH,
    IN(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    IF,
    RETURN,
    SUPER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    SWITCH,
    THROW,
    TRY,
    CATCH,
    FINALLY,
    WITH,
    DYNAMIC,
    INTERNAL,
    OVERRIDE,
    PRIVATE,
    PROTECTED,
    PUBLIC,
    STATIC,
    CLASS,
    CONST,
    EXTENDS,
    FUNCTION(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    GET,
    IMPLEMENTS,
    INTERFACE,
    NAMESPACE,
    PACKAGE,
    SET,
    VAR,
    IMPORT,
    USE,
    FALSE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    NULL(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    THIS(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    TRUE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    //Operators
    PARENT_OPEN(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    PARENT_CLOSE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    CURLY_OPEN(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    CURLY_CLOSE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    BRACKET_OPEN(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    BRACKET_CLOSE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    SEMICOLON,
    COMMA(GraphTargetItem.PRECEDENCE_COMMA, false),
    REST,
    DOT(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    ASSIGN(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    GREATER_THAN(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    LOWER_THAN(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    NOT(GraphTargetItem.PRECEDENCE_UNARY, false),
    NEGATE(GraphTargetItem.PRECEDENCE_UNARY, false),
    TERNAR(GraphTargetItem.PRECEDENCE_CONDITIONAL, true, true), /*!! ternar !!!*/
    COLON(GraphTargetItem.PRECEDENCE_CONDITIONAL, false),/*!! ternar !!!*/
    EQUALS(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    STRICT_EQUALS(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    LOWER_EQUAL(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    GREATER_EQUAL(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    NOT_EQUAL(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    STRICT_NOT_EQUAL(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    AND(GraphTargetItem.PRECEDENCE_LOGICALAND, true),
    OR(GraphTargetItem.PRECEDENCE_LOGICALOR, true),
    INCREMENT(GraphTargetItem.PRECEDENCE_POSTFIX, false),//OR Unary
    DECREMENT(GraphTargetItem.PRECEDENCE_POSTFIX, false), //OR Unary
    PLUS(GraphTargetItem.PRECEDENCE_ADDITIVE, true),
    MINUS(GraphTargetItem.PRECEDENCE_ADDITIVE, true), //OR Unary
    MULTIPLY(GraphTargetItem.PRECEDENCE_MULTIPLICATIVE, true),
    DIVIDE(GraphTargetItem.PRECEDENCE_MULTIPLICATIVE, true),
    BITAND(GraphTargetItem.PRECEDENCE_BITWISEAND, true),
    BITOR(GraphTargetItem.PRECEDENCE_BITWISEOR, true),
    XOR(GraphTargetItem.PRECEDENCE_BITWISEXOR, true),
    MODULO(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    SHIFT_LEFT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    SHIFT_RIGHT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    USHIFT_RIGHT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    ASSIGN_PLUS(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_MINUS(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_MULTIPLY(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_DIVIDE(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_BITAND(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_BITOR(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_XOR(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_MODULO(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_SHIFT_LEFT(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_SHIFT_RIGHT(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    ASSIGN_USHIFT_RIGHT(GraphTargetItem.PRECEDENCE_ASSIGMENT, true, true),
    AS(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    DELETE(GraphTargetItem.PRECEDENCE_UNARY, false),
    INSTANCEOF(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    IS(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    NAMESPACE_OP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    NEW(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    TYPEOF(GraphTargetItem.PRECEDENCE_UNARY, false),
    VOID,
    ATTRIBUTE,
    //Other
    STRING(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    COMMENT,
    //XML,
    IDENTIFIER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    INTEGER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    DOUBLE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    TYPENAME(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    EOF,
    //TRACE,
    //GETURL,
    //GOTOANDSTOP,
    //NEXTFRAME,
    //PLAY,
    //PREVFRAME,
    //TELLTARGET,
    //STOP,
    //STOPALLSOUNDS,
    //TOGGLEHIGHQUALITY,
    //ORD,
    //CHR,
    //DUPLICATEMOVIECLIP,
    //STOPDRAG,
    //GETTIMER,
    //LOADVARIABLES,
    //LOADMOVIE,
    //GOTOANDPLAY,
    //MBORD,
    //MBCHR,
    //MBLENGTH,
    //MBSUBSTRING,
    //RANDOM,
    //REMOVEMOVIECLIP,
    //STARTDRAG,
    //SUBSTR,
    //LENGTH, //string.length
    INT(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    //TARGETPATH,
    NUMBER_OP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    STRING_OP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    //IFFRAMELOADED,
    INFINITY(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    //EVAL,
    UNDEFINED(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    //NEWLINE,
    NAN(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    //GETVERSION,
    //CALL,
    //LOADMOVIENUM,
    //LOADVARIABLESNUM,
    //PRINT,
    //PRINTNUM,
    //PRINTASBITMAP,
    //PRINTASBITMAPNUM,
    //UNLOADMOVIE,
    //UNLOADMOVIENUM,
    FINAL,
    XML_STARTTAG_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <xxx
    XML_STARTVARTAG_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <{
    XML_STARTTAG_END(GraphTargetItem.PRECEDENCE_PRIMARY, false), // >
    XML_FINISHVARTAG_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // </{
    XML_FINISHTAG(GraphTargetItem.PRECEDENCE_PRIMARY, false), //  </xxx>
    XML_STARTFINISHTAG_END(GraphTargetItem.PRECEDENCE_PRIMARY, false), // />
    XML_COMMENT(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <!-- ... -->
    XML_CDATA(GraphTargetItem.PRECEDENCE_PRIMARY, false), //<![CDATA[ ... ]]>
    XML_INSTR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <?xxx
    XML_INSTR_END(GraphTargetItem.PRECEDENCE_PRIMARY, false), // ?>
    XML_VAR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // {
    XML_ATTRIBUTENAME(GraphTargetItem.PRECEDENCE_PRIMARY, false), // aaa=
    XML_ATTRIBUTEVALUE(GraphTargetItem.PRECEDENCE_PRIMARY, false), // "vvv"
    XML_TEXT(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    XML_ATTRNAMEVAR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // {...}=
    XML_ATTRVALVAR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // aaa={
    XML_INSTRATTRNAMEVAR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // {...}=
    XML_INSTRATTRVALVAR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // aaa={
    XML_INSTRVARTAG_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <?{
    FILTER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    DESCENDANTS(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    NATIVE,
    PREPROCESSOR(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    REGEXP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    NAMESPACESUFFIX;

    private int precedence = GraphTargetItem.NOPRECEDENCE;

    private boolean binary = false;

    private boolean rightAssociative = false;

    public boolean isBinary() {
        return binary;
    }

    public boolean isRightAssociative() {
        return rightAssociative;
    }

    public int getPrecedence() {
        return precedence;
    }

    private SymbolType(int precedence, boolean binary) {
        this.precedence = precedence;
        this.binary = binary;
    }

    private SymbolType(int precedence, boolean binary, boolean rightAssociative) {
        this.precedence = precedence;
        this.binary = binary;
        this.rightAssociative = rightAssociative;
    }

    private SymbolType() {

    }
}
