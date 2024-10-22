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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.graph.GraphTargetItem;

/**
 * ActionScript 3 parsed symbol type.
 *
 * @author JPEXS
 */
public enum SymbolType {

    /**
     * Keyword: break
     */
    BREAK,
    /**
     * Keyword: case
     */
    CASE,
    /**
     * Keyword: continue
     */
    CONTINUE,
    /**
     * Keyword: default
     */
    DEFAULT,
    /**
     * Keyword: do
     */
    DO,
    /**
     * Keyword: while
     */
    WHILE,
    /**
     * Keyword: else
     */
    ELSE,
    /**
     * Keyword: for
     */
    FOR,
    /**
     * Keyword: each
     */
    EACH,
    /**
     * Keyword: in
     */
    IN(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    /**
     * Keyword: if
     */
    IF,
    /**
     * Keyword: return
     */
    RETURN,
    /**
     * Keyword: super
     */
    SUPER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Keyword: switch
     */
    SWITCH,
    /**
     * Keyword: throw
     */
    THROW,
    /**
     * Keyword: try
     */
    TRY,
    /**
     * Keyword: catch
     */
    CATCH,
    /**
     * Keyword: finally
     */
    FINALLY,
    /**
     * Keyword: with
     */
    WITH,
    /**
     * Keyword: dynamic
     */
    DYNAMIC,
    /**
     * Keyword: internal
     */
    INTERNAL,
    /**
     * Keyword: override
     */
    OVERRIDE,
    /**
     * Keyword: private
     */
    PRIVATE,
    /**
     * Keyword: protected
     */
    PROTECTED,
    /**
     * Keyword: public
     */
    PUBLIC,
    /**
     * Keyword: static
     */
    STATIC,
    /**
     * Keyword: class
     */
    CLASS,
    /**
     * Keyword: const
     */
    CONST,
    /**
     * Keyword: extends
     */
    EXTENDS,
    /**
     * Keyword: function
     */
    FUNCTION(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Keyword: get
     */
    GET,
    /**
     * Keyword: implements
     */
    IMPLEMENTS,
    /**
     * Keyword: interface
     */
    INTERFACE,
    /**
     * Keyword: namespace
     */
    NAMESPACE,
    /**
     * Keyword: package
     */
    PACKAGE,
    /**
     * Keyword: set
     */
    SET,
    /**
     * Keyword: var
     */
    VAR,
    /**
     * Keyword: import
     */
    IMPORT,
    /**
     * Keyword: use
     */
    USE,
    /**
     * Keyword: false
     */
    FALSE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Keyword: null
     */
    NULL(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Keyword: this
     */
    THIS(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Keyword: true
     */
    TRUE(GraphTargetItem.PRECEDENCE_PRIMARY, false),

    /**
     * Operator: (
     */
    PARENT_OPEN(GraphTargetItem.PRECEDENCE_PRIMARY, false),

    /**
     * Operator: )
     */
    PARENT_CLOSE(GraphTargetItem.PRECEDENCE_PRIMARY, false),

    /**
     * Operator: {
     */
    CURLY_OPEN(GraphTargetItem.PRECEDENCE_PRIMARY, false),

    /**
     * Operator: }
     */
    CURLY_CLOSE(GraphTargetItem.PRECEDENCE_PRIMARY, false),

    /**
     * Operator: [
     */
    BRACKET_OPEN(GraphTargetItem.PRECEDENCE_PRIMARY, false),

    /**
     * Operator: ]
     */
    BRACKET_CLOSE(GraphTargetItem.PRECEDENCE_PRIMARY, false),

    /**
     * Operator: ;
     */
    SEMICOLON,

    /**
     * Operator: ,
     */
    COMMA(GraphTargetItem.PRECEDENCE_COMMA, false),

    /**
     * Operator: ...
     */
    REST,

    /**
     * Operator: .
     */
    DOT(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Operator: =
     */
    ASSIGN(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: &gt;
     */
    GREATER_THAN(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    /**
     * Operator: &lt;
     */
    LOWER_THAN(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    /**
     * Operator: !
     */
    NOT(GraphTargetItem.PRECEDENCE_UNARY, false),
    /**
     * Operator: ~
     */
    NEGATE(GraphTargetItem.PRECEDENCE_UNARY, false),
    /**
     * Operator: ?
     */
    TERNAR(GraphTargetItem.PRECEDENCE_CONDITIONAL, true, true), /*!! ternar !!!*/
    /**
     * Operator: :
     */
    COLON(GraphTargetItem.PRECEDENCE_CONDITIONAL, false), /*!! ternar !!!*/
    /**
     * Operator: ==
     */
    EQUALS(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    /**
     * Operator: ===
     */
    STRICT_EQUALS(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    /**
     * Operator: &lt;=
     */
    LOWER_EQUAL(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    /**
     * Operator: &gt;=
     */
    GREATER_EQUAL(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    /**
     * Operator: !=
     */
    NOT_EQUAL(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    /**
     * Operator: !==
     */
    STRICT_NOT_EQUAL(GraphTargetItem.PRECEDENCE_EQUALITY, true),
    /**
     * Operator: &amp;&amp;
     */
    AND(GraphTargetItem.PRECEDENCE_LOGICALAND, true),
    /**
     * Operator: ||
     */
    OR(GraphTargetItem.PRECEDENCE_LOGICALOR, true),
    /**
     * Operator: ++
     */
    INCREMENT(GraphTargetItem.PRECEDENCE_POSTFIX, false), //OR Unary
    /**
     * Operator: --
     */
    DECREMENT(GraphTargetItem.PRECEDENCE_POSTFIX, false), //OR Unary
    /**
     * Operator: +
     */
    PLUS(GraphTargetItem.PRECEDENCE_ADDITIVE, true),
    /**
     * Operator: -
     */
    MINUS(GraphTargetItem.PRECEDENCE_ADDITIVE, true), //OR Unary
    /**
     * Operator: *
     */
    MULTIPLY(GraphTargetItem.PRECEDENCE_MULTIPLICATIVE, true),
    /**
     * Operator: /
     */
    DIVIDE(GraphTargetItem.PRECEDENCE_MULTIPLICATIVE, true),
    /**
     * Operator: &amp;
     */
    BITAND(GraphTargetItem.PRECEDENCE_BITWISEAND, true),
    /**
     * Operator: |
     */
    BITOR(GraphTargetItem.PRECEDENCE_BITWISEOR, true),
    /**
     * Operator: ^
     */
    XOR(GraphTargetItem.PRECEDENCE_BITWISEXOR, true),
    /**
     * Operator: %
     */
    MODULO(GraphTargetItem.PRECEDENCE_MULTIPLICATIVE, true),
    /**
     * Operator: &lt;&lt;
     */
    SHIFT_LEFT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    /**
     * Operator: &gt;&gt;
     */
    SHIFT_RIGHT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    /**
     * Operator: &gt;&gt;&gt;
     */
    USHIFT_RIGHT(GraphTargetItem.PRECEDENCE_BITWISESHIFT, true),
    /**
     * Operator: +=
     */
    ASSIGN_PLUS(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: -=
     */
    ASSIGN_MINUS(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: *=
     */
    ASSIGN_MULTIPLY(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: /=
     */
    ASSIGN_DIVIDE(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: &amp;=
     */
    ASSIGN_BITAND(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: |=
     */
    ASSIGN_BITOR(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: ^=
     */
    ASSIGN_XOR(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: %=
     */
    ASSIGN_MODULO(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: &lt;&lt;=
     */
    ASSIGN_SHIFT_LEFT(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: &gt;&gt;=
     */
    ASSIGN_SHIFT_RIGHT(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: &gt;&gt;&gt;=
     */
    ASSIGN_USHIFT_RIGHT(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: &amp;&amp;=
     */
    ASSIGN_AND(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: ||=
     */
    ASSIGN_OR(GraphTargetItem.PRECEDENCE_ASSIGNMENT, true, true),
    /**
     * Operator: as
     */
    AS(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    /**
     * Operator: delete
     */
    DELETE(GraphTargetItem.PRECEDENCE_UNARY, false),
    /**
     * Operator: instanceof
     */
    INSTANCEOF(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    /**
     * Operator: is
     */
    IS(GraphTargetItem.PRECEDENCE_RELATIONAL, true),
    /**
     * Operator: ::
     */
    NAMESPACE_OP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Operator: new
     */
    NEW(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Operator: typeof
     */
    TYPEOF(GraphTargetItem.PRECEDENCE_UNARY, false),
    /**
     * Operator: void
     */
    VOID,
    /**
     * Operator: @
     */
    ATTRIBUTE,
    /**
     * Other: String
     */
    STRING(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: Comment
     */
    COMMENT,
    /**
     * Other: Identifier
     */
    IDENTIFIER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: Integer
     */
    INTEGER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: Double
     */
    DOUBLE(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: Decimal
     */
    DECIMAL(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: Float
     */
    FLOAT(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: Float 4
     */
    FLOAT4(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: Type name
     */
    TYPENAME(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: End of file
     */
    EOF,
    /**
     * Other: Number op
     */
    NUMBER_OP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: String op
     */
    STRING_OP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: Infinity
     */
    INFINITY(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: undefined
     */
    UNDEFINED(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: NaN
     */
    NAN(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Keyword: final
     */
    FINAL,
    /**
     * XML: Start tag
     */
    XML_STARTTAG_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <xxx
    /**
     * XML: Start variable tag
     */
    XML_STARTVARTAG_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <{
    /**
     * XML: Start tag end
     */
    XML_STARTTAG_END(GraphTargetItem.PRECEDENCE_PRIMARY, false), // >
    /**
     * XML: Finish variable tag
     */
    XML_FINISHVARTAG_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // </{
    /**
     * XML: Finish tag
     */
    XML_FINISHTAG(GraphTargetItem.PRECEDENCE_PRIMARY, false), //  </xxx>
    /**
     * XML: Start finish tag end
     */
    XML_STARTFINISHTAG_END(GraphTargetItem.PRECEDENCE_PRIMARY, false), // />
    /**
     * XML: Comment
     */
    XML_COMMENT(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <!-- ... -->
    /**
     * XML: CDATA
     */
    XML_CDATA(GraphTargetItem.PRECEDENCE_PRIMARY, false), //<![CDATA[ ... ]]>
    /**
     * XML: Processing instruction
     */
    XML_INSTR(GraphTargetItem.PRECEDENCE_PRIMARY, false), // <?xxx a b c ?>
    /**
     * XML: variable begin
     */
    XML_VAR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // {
    /**
     * XML: attribute name
     */
    XML_ATTRIBUTENAME(GraphTargetItem.PRECEDENCE_PRIMARY, false), // aaa=
    /**
     * XML: attribute value, double quoted
     */
    XML_ATTRIBUTEVALUE(GraphTargetItem.PRECEDENCE_PRIMARY, false), // "vvv"
    /**
     * XML: attribute value, single quoted
     */
    XML_ATTRIBUTEVALUE_SINGLEQUOTED(GraphTargetItem.PRECEDENCE_PRIMARY, false), // 'vvv'
    /**
     * XML: text
     */
    XML_TEXT(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * XML: variable attribute name begin
     */
    XML_ATTRNAMEVAR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // {...}=
    /**
     * XML: variable attribute value end
     */
    XML_ATTRVALVAR_BEGIN(GraphTargetItem.PRECEDENCE_PRIMARY, false), // aaa={
    /**
     * XML: end
     */
    XML_END(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * XML: whitespace
     */
    XML_WHITESPACE(GraphTargetItem.PRECEDENCE_PRIMARY, false), //only when enabled - for example for XML
    /**
     * Operator: .(
     */
    FILTER(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Operator: ..
     */
    DESCENDANTS(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Keyword: native
     */
    NATIVE,
    /**
     * Operator: §§
     */
    PREPROCESSOR(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: regular expression
     */
    REGEXP(GraphTargetItem.PRECEDENCE_PRIMARY, false),
    /**
     * Other: namespace suffix
     */
    NAMESPACESUFFIX;

    private int precedence = GraphTargetItem.NOPRECEDENCE;

    private boolean binary = false;

    private boolean rightAssociative = false;

    /**
     * Checks if the symbol is binary operator.
     * @return True if the symbol is binary operator
     */
    public boolean isBinary() {
        return binary;
    }

    /**
     * Checks if the symbol is right associative.
     * @return True if the symbol is right associative
     */
    public boolean isRightAssociative() {
        return rightAssociative;
    }

    /**
     * Gets the precedence of the symbol.
     * @return Precedence of the symbol
     */
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
