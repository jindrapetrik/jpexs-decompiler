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
 * An {@link Evaluator} implementation to have a {@link String} matched by an
 * expression removed from the script
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public class DeleteEvaluator extends AbstractEvaluator implements Evaluator {

    @Override
    public String evaluate(Matcher matcher, int offset) {
        return "\u0001" + matcher.group(offset) + "\u0001";
    }
}
