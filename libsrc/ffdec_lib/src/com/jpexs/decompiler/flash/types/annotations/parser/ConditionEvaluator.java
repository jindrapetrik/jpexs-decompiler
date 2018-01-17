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
package com.jpexs.decompiler.flash.types.annotations.parser;

import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.ConditionalType;
import java.io.IOException;
import java.io.StringReader;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class ConditionEvaluator {

    private final String[] values;

    public ConditionEvaluator(Conditional cond) {
        values = cond.value();
    }

    public ConditionEvaluator(ConditionalType cond) {
        values = cond.value();
    }

    private void expressionRest(Map<String, Boolean> fields, Stack<Boolean> stack, ConditionLexer lex) throws IOException, AnnotationParseException {
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

    private void expression(Map<String, Boolean> fields, Stack<Boolean> stack, ConditionLexer lex) throws IOException, AnnotationParseException {
        ConditionToken tok = lex.lex();
        if (tok == null) {
            return;
        }
        switch (tok.type) {
            case FIELD:
                if (!fields.containsKey(tok.value)) {
                    throw new AnnotationParseException("Field not found", lex.yyline());
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
                tok = lex.lex();
                if (tok.type != ConditionTokenType.PARENT_CLOSE) {
                    throw new AnnotationParseException("End of parent expected", lex.yyline());
                }
                expressionRest(fields, stack, lex);
                break;
            default:
                throw new AnnotationParseException("Expression expected", lex.yyline());
        }
    }

    public boolean eval(Map<String, Boolean> fields) throws AnnotationParseException {
        ConditionLexer lex = new ConditionLexer(new StringReader(prepareCond()));

        Stack<Boolean> stack = new Stack<>();

        try {
            expression(fields, stack, lex);
        } catch (IOException | EmptyStackException ex) {
            throw new AnnotationParseException("Invalid condition:" + prepareCond(), lex.yyline());
        }
        if (prepareCond().isEmpty()) {
            return true;
        }
        if (stack.size() != 1) {
            throw new AnnotationParseException("Invalid condition:" + prepareCond(), lex.yyline());
        }

        return stack.pop();
    }

    private String prepareCond() {
        String[] vals = values;
        if (vals == null || vals.length == 0) {
            return "";
        }
        String val = vals[0];
        for (int i = 1; i < vals.length; i++) {
            val = val + "," + vals[i];
        }
        return val;
    }

    public Set<String> getFields() throws AnnotationParseException {
        Set<String> ret = new HashSet<>();
        ConditionLexer lex = new ConditionLexer(new StringReader(prepareCond()));
        ConditionToken tok;
        try {
            while ((tok = lex.lex()) != null) {
                if (tok.type == ConditionTokenType.FIELD) {
                    ret.add(tok.value);
                }
            }
        } catch (IOException ex) {
            throw new AnnotationParseException("Invalid condition:" + prepareCond(), lex.yyline());
        }
        return ret;
    }
}
