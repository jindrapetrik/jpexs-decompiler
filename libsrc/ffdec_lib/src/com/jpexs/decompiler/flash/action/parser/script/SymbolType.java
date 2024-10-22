/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.graph.GraphTargetItem;

/**
 * ActionScript 1/2 symbol type.
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
    PRIVATE,
    PUBLIC,
    STATIC,
    CLASS,
    EXTENDS,
    FUNCTION(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    GET,
    IMPLEMENTS,
    INTERFACE,
    SET,
    VAR,
    IMPORT,
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
    ASSIGN(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    GREATER_THAN(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    LOWER_THAN(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    NOT(GraphTargetItem.PRECEDENCE_UNARY, false),
    NEGATE(GraphTargetItem.PRECEDENCE_UNARY, false),
    TERNAR(GraphTargetItem.PRECEDENCE_CONDITIONAL, true, true), /*!! ternar !!!*/
    COLON(GraphTargetItem.PRECEDENCE_CONDITIONAL, false), /*!! ternar !!!*/
    EQUALS(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    STRICT_EQUALS(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    LOWER_EQUAL(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    GREATER_EQUAL(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    NOT_EQUAL(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    STRICT_NOT_EQUAL(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    AND(GraphTargetItem.PRECEDENCE_LOGICALAND, true),
    OR(GraphTargetItem.PRECEDENCE_LOGICALOR, true),
    FULLAND(GraphTargetItem.PRECEDENCE_LOGICALAND, true),
    FULLOR(GraphTargetItem.PRECEDENCE_LOGICALOR, true),
    INCREMENT(GraphTargetItem.PRECEDENCE_POSTFIX, false), //OR Unary
    DECREMENT(GraphTargetItem.PRECEDENCE_POSTFIX, false), //OR Unary
    PLUS(GraphTargetItem.PRECEDENCE_ADDITIVE, true),
    MINUS(GraphTargetItem.PRECEDENCE_ADDITIVE, true), //OR Unary
    MULTIPLY(GraphTargetItem.PRECEDENCE_MULTIPLICATIVE, true),
    DIVIDE(GraphTargetItem.PRECEDENCE_MULTIPLICATIVE, true),
    BITAND(GraphTargetItem.PRECEDENCE_BITWISEAND, true),
    BITOR(GraphTargetItem.PRECEDENCE_BITWISEOR, true),
    XOR(GraphTargetItem.PRECEDENCE_BITWISEXOR, true),
    MODULO(GraphTargetItem.PRECEDENCE_MULTIPLICATIVE, true),
    SHIFT_LEFT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    SHIFT_RIGHT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    USHIFT_RIGHT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    ASSIGN_PLUS(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_MINUS(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_MULTIPLY(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_DIVIDE(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_BITAND(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_BITOR(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_XOR(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_MODULO(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_SHIFT_LEFT(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_SHIFT_RIGHT(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    ASSIGN_USHIFT_RIGHT(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    DELETE(GraphTargetItem.PRECEDENCE_UNARY, false),
    INSTANCEOF(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    NEW(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    TYPEOF(GraphTargetItem.PRECEDENCE_UNARY, false),
    VOID,
    ATTRIBUTE,
    //Other
    STRING(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    COMMENT,
    IDENTIFIER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    INTEGER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    DOUBLE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    TYPENAME(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    EOF,
    TRACE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    GETURL(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    GOTOANDSTOP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    NEXTFRAME(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    PLAY(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    PREVFRAME(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    TELLTARGET(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    STOP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    STOPALLSOUNDS(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    TOGGLEHIGHQUALITY(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    ORD(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    CHR(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    DUPLICATEMOVIECLIP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    STOPDRAG(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    GETTIMER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    LOADVARIABLES(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    LOADMOVIE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    GOTOANDPLAY(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    MBORD(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    MBCHR(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    MBLENGTH(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    MBSUBSTRING(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    RANDOM(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    REMOVEMOVIECLIP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    STARTDRAG(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    SUBSTR(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    LENGTH(GraphTargetItem.PRECEDENCE_PRIMARY, false), //string.length
    INT(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    TARGETPATH(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    NUMBER_OP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    STRING_OP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    IFFRAMELOADED,
    EVAL(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    UNDEFINED(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    NEWLINE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    GETVERSION(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    CALL(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    LOADMOVIENUM(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    LOADVARIABLESNUM(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    PRINT(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    PRINTNUM(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    PRINTASBITMAP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    PRINTASBITMAPNUM(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    UNLOADMOVIE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    UNLOADMOVIENUM(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    FSCOMMAND(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    PREPROCESSOR(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    FSCOMMAND2(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    DIRECTIVE(GraphTargetItem.PRECEDENCE_PRIMARY, false);
    
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
