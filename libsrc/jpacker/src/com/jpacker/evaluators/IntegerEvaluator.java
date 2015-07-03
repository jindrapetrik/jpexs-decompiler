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
package com.jpacker.evaluators;

import java.util.regex.Matcher;

/**
 * An {@link Evaluator} implementation for replacement {@link String} objects
 * such as "$1" or "$2". Must be a simple group reference (i.e.: NOT
 * "Hello $3 $2")
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public class IntegerEvaluator extends AbstractEvaluator implements Evaluator {

    private int replacement;

    /**
     * Constructor
     *
     * @param replacement
     *            If replacement String is "$1" then 1 would be the replacement
     *            parameter
     */
    public IntegerEvaluator(int replacement) {
        this.replacement = replacement;
    }

    @Override
    public String evaluate(Matcher matcher, int offset) {
        return matcher.group(replacement + offset);
    }
}
