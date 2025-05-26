/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.helpers;

/**
 * Code formatting settings.
 *
 * @author JPEXS
 */
public class CodeFormatting {

    /**
     * New line characters
     */
    public String newLineChars = "\r\n";

    /**
     * Indent string
     */
    public String indentString = "   ";

    /**
     * Begin block on new line
     */
    public boolean beginBlockOnNewLine = true;

    // spaces

    /**
     * Spaces before parentheses in method call
     */
    public boolean spaceBeforeParenthesesMethodCallParentheses = false;

    /**
     * Spaces before parentheses in method call with empty parentheses
     */
    public boolean spaceBeforeParenthesesMethodCallEmptyParentheses = false;

    /**
     * Spaces before array access brackets
     */
    public boolean spaceBeforeArrayAccessBrackets = false;

    /**
     * Spaces before parentheses in method declaration
     */
    public boolean spaceBeforeParenthesesMethodDeclarationParentheses = false;

    /**
     * Spaces before parentheses in method declaration with empty parentheses
     */
    public boolean spaceBeforeParenthesesMethodDeclarationEmptyParentheses = false;

    /**
     * Spaces before parentheses in if parentheses
     */
    public boolean spaceBeforeParenthesesIfParentheses = false;

    /**
     * Spaces before parentheses in with parentheses
     */
    public boolean spaceBeforeParenthesesWithParentheses = false;

    /**
     * Spaces before parentheses in while parentheses
     */
    public boolean spaceBeforeParenthesesWhileParentheses = false;

    /**
     * Spaces before parentheses in catch parentheses
     */
    public boolean spaceBeforeParenthesesCatchParentheses = false;

    /**
     * Spaces before parentheses in switch parentheses
     */
    public boolean spaceBeforeParenthesesSwitchParentheses = false;

    /**
     * Spaces before parentheses in for parentheses
     */
    public boolean spaceBeforeParenthesesForParentheses = false;

    /**
     * Spaces before parentheses in foreach parentheses
     */
    public boolean spaceBeforeParenthesesForEachParentheses = false;

    // around operators
    /**
     * Spaces around assignment operators.
     * =, +=, ...
     */
    public boolean spaceAroundOperatorsAssignmentOperators = false;

    /**
     * Spaces around logical operators.
     * &amp;&amp;, ||
     */
    public boolean spaceAroundOperatorsLogicalOperators = false;

    /**
     * Spaces around equality operators.
     * ==, !=
     */
    public boolean spaceAroundOperatorsEqualityOperators = false;

    /**
     * Spaces around relational operators.
     * &lt;, &gt;, &lt;=, &gt;=
     */
    public boolean spaceAroundOperatorsRelationalOperator = false;

    /**
     * Spaces around bitwise operators.
     * &amp;, |, ^
     */
    public boolean spaceAroundOperatorsBitwiseOperator = false;

    /**
     * Spaces around additive operators.
     * +, -
     */
    public boolean spaceAroundOperatorsAdditiveOperator = false;

    /**
     * Spaces around multiplicative operators.
     * *, /, %
     */
    public boolean spaceAroundOperatorsMultiplicativeOperator = false;

    /**
     * Spaces around shift operators.
     * &lt;&lt;, &gt;&gt;
     */
    public boolean spaceAroundOperatorsShiftOperator = false;

    /**
     * Constructor.
     */
    public CodeFormatting() {

    }
}
