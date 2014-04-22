/**
 * Packer version 3.0 (final)
 * Copyright 2004-2007, Dean Edwards
 * Web: {@link http://dean.edwards.name/}
 * 
 * This software is licensed under the MIT license
 * Web: {@link http://www.opensource.org/licenses/mit-license}
 * 
 * Ported to Java by Pablo Santiago based on C# version by Jesse Hansen, <twindagger2k @ msn.com>
 * Web: {@link http://jpacker.googlecode.com/}
 * Email: <pablo.santiago @ gmail.com>
 */
package com.jpacker;

import com.jpacker.evaluators.Evaluator;

/**
 * Wrapper class for each pattern
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public class JPackerPattern {

    private String expression;
    private Evaluator evaluator;
    private int length;

    public JPackerPattern() {
    }

    public JPackerPattern(String expression, Evaluator evaluator) {
        this.expression = expression;
        this.evaluator = evaluator;
        evaluator.setJPattern(this);
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public String toString() {
        return "(" + expression + ")";
    }
}
