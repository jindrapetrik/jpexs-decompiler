/*
 * Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.helpers;

/**
 *
 * @author JPEXS
 */
public class CodeFormatting {

    public String newLineChars = "\r\n";
    public String indentString = "   ";
    public boolean beginBlockOnNewLine = true;

    // spaces
    // before parentheses
    public boolean spaceBeforeParenthesesMethodCallParentheses = false;
    public boolean spaceBeforeParenthesesMethodCallEmptyParentheses = false;
    public boolean spaceBeforeArrayAccessBrackets = false;
    public boolean spaceBeforeParenthesesMethodDeclarationParentheses = false;
    public boolean spaceBeforeParenthesesMethodDeclarationEmptyParentheses = false;
    public boolean spaceBeforeParenthesesIfParentheses = false;
    public boolean spaceBeforeParenthesesWithParentheses = false;
    public boolean spaceBeforeParenthesesWhileParentheses = false;
    public boolean spaceBeforeParenthesesCatchParentheses = false;
    public boolean spaceBeforeParenthesesSwitchParentheses = false;
    public boolean spaceBeforeParenthesesForParentheses = false;
    public boolean spaceBeforeParenthesesForEachParentheses = false;

    // around operators
    public boolean spaceAroundOperatorsAssignmentOperators = false; // =, +=,...
    public boolean spaceAroundOperatorsLogicalOperators = false; // &&, ||
    public boolean spaceAroundOperatorsEqualityOperators = false; // ==, !=
    public boolean spaceAroundOperatorsRelationalOperator = false; // <, >, <=, >=
    public boolean spaceAroundOperatorsBitwiseOperator = false; // &, |, ^
    public boolean spaceAroundOperatorsAdditiveOperator = false; // +, -
    public boolean spaceAroundOperatorsMultiplicativeOperator = false; // *, /, %
    public boolean spaceAroundOperatorsShiftOperator = false; // <<, >>
}
