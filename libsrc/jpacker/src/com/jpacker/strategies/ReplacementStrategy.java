/**
 * Packer version 3.0 (final)
 * Copyright 2004-2007, Dean Edwards
 * Web: {@link http://dean.edwards.name/}
 * 
 * This software is licensed under the MIT license
 * Web: {@link http://www.opensource.org/licenses/mit-license}
 * 
 * Ported to Java by Pablo Santiago based on C# version by Jesse Hansen, <twindagger2k @ msn.com>
 * Email: <pablo.santiago @ gmail.com>
 */
package com.jpacker.strategies;

import java.util.List;
import java.util.regex.Matcher;

import com.jpacker.JPackerPattern;

/**
 * An interface to build replacement strategies
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public interface ReplacementStrategy {

    /**
     * Replacement function. Called once for each match found
     *
     * @param patterns
     *            A List<JPackerPattern> that contains all
     *            {@link JPackerPattern} objects that wrap expressions to be
     *            evaluated
     * @param matcher
     *            A {@link Matcher} object that corresponds to a match in the
     *            script
     */
    public String replace(List<JPackerPattern> patterns, Matcher matcher);
}
