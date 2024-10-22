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
package com.jpacker.evaluators;

import java.util.regex.Matcher;

import com.jpacker.JPackerParser;
import com.jpacker.JPackerPattern;
import com.jpacker.strategies.DefaultReplacementStrategy;

/**
 * After expressions have been added to a {@link JPackerParser} object and a
 * ReplacementStrategy has been set, each {@link JPackerPattern} should have an
 * {@link Evaluator} object that'll evaluate a certain match of a pattern
 * expression and return a suitable replacement String. Commonly, after all
 * expressions have been added the {@link JPackerParser} object, a one-line
 * String that contains all expressions is created by the {@link JPackerParser}
 * object using the {@link JPackerParser#getJPatterns()} method. In the
 * {@link #evaluate(Matcher, int)} method, the offset integer parameter
 * represents the position in such String that corresponds to a
 * {@link JPackerPattern} object.
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public interface Evaluator {

    /**
     * Sets the {@link JPackerPattern} object to use
     *
     * @param jpattern
     *            The {@link JPackerPattern} object for the {@link Evaluator}
     *            implementations to use
     */
    public void setJPattern(JPackerPattern jpattern);

    /**
     * Gets the {@link JPackerPattern} object
     *
     * @return The {@link JPackerPattern} object if it has been set, null
     *         otherwise
     */
    public JPackerPattern getJPattern();

    /**
     * Evaluates the string matched by the {@link Matcher} object and returns a
     * suitable replacement String.
     *
     * @param matcher
     *            The {@link Matcher} object that contains a match (and its
     *            groups)
     * @param offset
     *            The offset in the String object returned by the
     *            {@link JPackerParser#getJPatterns()} method in the
     *            {@link JPackerParser} object
     * @return A replacement string (either text or group expressions, i.e.: $1)
     * @see DefaultReplacementStrategy
     */
    public String evaluate(Matcher matcher, int offset);
}
