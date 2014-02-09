/*
 * Copyright (C) 2014 JPEXS
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
package com.jpexs.decompiler.flash.types.annotations.parser;

import com.jpexs.decompiler.flash.types.annotations.Conditional;
import java.io.IOException;
import java.io.StringReader;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ConditionEvaluator {

    private Conditional cond;

    public ConditionEvaluator(Conditional cond) {
        this.cond = cond;
    }

    private void expressionRest(Map<String, Boolean> fields, Stack<Boolean> stack, ConditionLexer lex) throws IOException, ParseException {
        ConditionToken tok = lex.lex();
        if (tok == null) {
            return;
        }
        switch (tok.type) {
            case AND:
                Boolean andOp1 = stack.pop();
                expression(fields, stack, lex);
                Boolean andOp2 = stack.pop();
                stack.push(andOp1 && andOp2);
                break;
            case OR:
                Boolean orOp1 = stack.pop();
                expression(fields, stack, lex);
                Boolean orOp2 = stack.pop();
                stack.push(orOp1 || orOp2);
                break;
            default:
                lex.pushback(tok);
        }

    }

    private void expression(Map<String, Boolean> fields, Stack<Boolean> stack, ConditionLexer lex) throws IOException, ParseException {
        ConditionToken tok = lex.yylex();
        if (tok == null) {
            return;
        }
        switch (tok.type) {
            case FIELD:
                if (!fields.containsKey(tok.value)) {
                    throw new ParseException("Field not found", lex.yyline());
                } else {
                    stack.push(fields.get(tok.value));
                }
                expressionRest(fields, stack, lex);
                break;
            case NOT:
                expression(fields, stack, lex);
                Boolean invOp = stack.pop();
                stack.push(!invOp);
                break;
            case PARENT_OPEN:
                expression(fields, stack, lex);
                tok = lex.yylex();
                if (tok.type != ConditionTokenType.PARENT_CLOSE) {
                    throw new ParseException("End of parent expected", lex.yyline());
                }
                expressionRest(fields, stack, lex);
                break;
            default:
                throw new ParseException("Expression expected", lex.yyline());
        }
    }

    public boolean eval(Map<String, Boolean> fields) throws ParseException {
        ConditionLexer lex = new ConditionLexer(new StringReader(prepareCond()));

        Stack<Boolean> stack = new Stack<>();

        try {
            expression(fields, stack, lex);
        } catch (IOException | EmptyStackException ex) {
            throw new ParseException("Invalid condition", lex.yyline());
        }
        if (stack.size() != 1) {
            throw new ParseException("Invalid condition", lex.yyline());
        }

        return stack.pop();
    }

    private String prepareCond() {
        String vals[] = cond.value();
        if (vals == null || vals.length == 0) {
            return "";
        }
        String val = vals[0];
        for (int i = 1; i < vals.length; i++) {
            val = val + "," + vals[i];
        }
        return val;
    }

    public Set<String> getFields() throws ParseException {
        Set<String> ret = new HashSet<>();
        ConditionLexer lex = new ConditionLexer(new StringReader(prepareCond()));
        ConditionToken tok;
        try {
            while ((tok = lex.yylex()) != null) {
                if (tok.type == ConditionTokenType.FIELD) {
                    ret.add(tok.value);
                }
            }
        } catch (IOException ex) {
            throw new ParseException("Invalid condition", lex.yyline());
        }
        return ret;
    }
}
