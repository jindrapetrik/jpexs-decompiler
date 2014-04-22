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
package com.jpacker.strategies;

import java.util.List;
import java.util.regex.Matcher;

import com.jpacker.JPackerPattern;

/**
 * Default replacement strategy class.
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public class DefaultReplacementStrategy implements ReplacementStrategy {

    /**
     * Default replacement function. Called once for each match found
     *
     * @param jpatterns
     *            A List<JPackerPattern> that contains all
     *            {@link JPackerPattern} objects that wrap expressions to be
     *            evaluated
     * @param matcher
     *            A {@link Matcher} object that corresponds to a match in the
     *            script
     */
    @Override
    public String replace(List<JPackerPattern> jpatterns, Matcher matcher) {
        int i = 1;
        // loop through the patterns
        for (JPackerPattern jpattern : jpatterns) {
            // do we have a result?
            if (isMatch(matcher.group(i))) {                
                return jpattern.getEvaluator().evaluate(matcher, i);
            } else { // skip over references to sub-expressions
                i += jpattern.getLength();
            }
        }
        return matcher.group(); // should never be hit, but you never know
    }

    // check that match is not an empty string
    private boolean isMatch(String match) {
        return match != null && !match.isEmpty();
    }
}
