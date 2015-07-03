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

public class StringEvaluator extends AbstractEvaluator implements Evaluator {

    private String replacement;

    public StringEvaluator(String replacement) {
        this.replacement = replacement;
    }

    /**
     * Replacement function for complicated lookups (e.g. Hello $3 $2)
     *
     */
    @Override
    public String evaluate(Matcher matcher, int offset) {
        int length = getJPattern().getLength();
        String result = replacement;
        while (length-- > 0) {
            String mg = matcher.group(offset + length);
            result = result.replace("$" + length, mg == null ? "" : mg);
        }
        return result;
    }
}
