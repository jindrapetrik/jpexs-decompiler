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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jpacker.evaluators.DeleteEvaluator;
import com.jpacker.evaluators.Evaluator;
import com.jpacker.evaluators.IntegerEvaluator;
import com.jpacker.evaluators.StringEvaluator;
import com.jpacker.strategies.DefaultReplacementStrategy;
import com.jpacker.strategies.ReplacementStrategy;

/**
 * Parser class that matches RegGrp.js.
 * 
 * This class parses the script using the expressions added via
 * {@link #remove(String)}, {@link #ignore(String)},
 * {@link #replace(String,String)} and {@link #replace(String,Evaluator)}
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public class JPackerParser {

    private static Pattern GROUPS = Pattern.compile("\\(");
    private static Pattern SUB_REPLACE = Pattern.compile("\\$(\\d+)");
    private static Pattern INDEXED = Pattern.compile("^\\$\\d+$");
    private static Pattern ESCAPE = Pattern.compile("\\\\.");
    private static Pattern ESCAPE_BRACKETS = Pattern.compile("\\(\\?[:=!]|\\[[^\\]]+\\]");
    private static Pattern DELETED = Pattern.compile("\\x01[^\\x01]*\\x01");
    private static String IGNORE = "$0";
    private List<JPackerPattern> jpatterns = new ArrayList<JPackerPattern>();

    /**
     * Add an expression to be removed
     *
     * @param expression
     *            Regular expression {@link String}
     */
    public void remove(String expression) {
        replace(expression, "");
    }

    /**
     * Add an expression to be ignored
     *
     * @param expression
     *            Regular expression {@link String}
     */
    public void ignore(String expression) {
        replace(expression, IGNORE);
    }

    /**
     * Add an expression to be replaced with the replacement string
     *
     * @param expression
     *            Regular expression {@link String}
     * @param replacement
     *            Replacement {@link String}. Use $1, $2, etc. for groups
     */
    public void replace(String expression, String replacement) {
        if (replacement.isEmpty()) {
            replace(expression, new DeleteEvaluator());
            return;
        }
        Evaluator evaluator;
        // does the pattern deal with sub-expressions? and a simple lookup (e.g. $2)
        if (SUB_REPLACE.matcher(replacement).matches() && INDEXED.matcher(replacement).matches()) {
            evaluator = new IntegerEvaluator(Integer.parseInt(replacement.substring(1)));
        } else {
            evaluator = new StringEvaluator(replacement);
        }
        JPackerPattern jpattern = new JPackerPattern(expression, evaluator);
        // count the number of sub-expressions
        jpattern.setLength(countSubExpressions(expression));
        jpatterns.add(jpattern);
    }

    /**
     * Add an expression to be replaced using an {@link Evaluator} object
     *
     * @param expression
     *            Regular expression String
     * @param evaluator
     *            The {@link Evaluator} object
     */
    public void replace(String expression, Evaluator evaluator) {
        JPackerPattern jpattern = new JPackerPattern(expression, evaluator);
        // count the number of sub-expressions
        jpattern.setLength(countSubExpressions(expression));
        jpatterns.add(jpattern);
    }

    // builds the patterns into a single regular expression
    private Pattern buildPatterns() {
        StringBuilder rtrn = new StringBuilder();
        for (JPackerPattern jpattern : jpatterns) {
            rtrn.append(jpattern).append("|");
        }
        rtrn.deleteCharAt(rtrn.length() - 1);
        return Pattern.compile(rtrn.toString());
    }

    /**
     * Executes the parser in order to parse the script with the expressions
     * added via {@link #remove(String)}, {@link #ignore(String)},
     * {@link #replace(String,String)} and {@link #replace(String,Evaluator)}
     *
     * @param input
     *            The script to be parsed
     * @return The parsed script
     */
    public String exec(String input) {
        return exec(input, new DefaultReplacementStrategy());
    }

    /**
     * Executes the parser in order to parse the script with the expressions
     * added via {@link #remove(String)}, {@link #ignore(String)},
     * {@link #replace(String,String)} and {@link #replace(String,Evaluator)}.
     * Using a {@link ReplacementStrategy} object, a custom replacement
     * algorithm can be used.
     *
     * @param input
     *            The script to be parsed
     * @param strategy
     *            The {@link ReplacementStrategy} object for custom replacement
     * @return The parsed script
     */
    public String exec(String input, ReplacementStrategy strategy) {
        Matcher matcher = buildPatterns().matcher(input);
        StringBuffer sb = new StringBuffer(input.length());
        while (matcher.find()) {
            String rep = strategy.replace(jpatterns, matcher);
            if (rep != null && !rep.isEmpty()) {
                rep = Matcher.quoteReplacement(rep);
            }
            matcher.appendReplacement(sb, rep);
        }
        matcher.appendTail(sb);
        return DELETED.matcher(sb).replaceAll("");
    }

    // count the number of sub-expressions
    private int countSubExpressions(String expression) {
        int cont = 0;
        Matcher matcher = GROUPS.matcher(internalEscape(expression));
        while (matcher.find()) {
            cont++;
        }
        // - add 1 because each group is itself a sub-expression
        return cont + 1;
    }

    private String internalEscape(String str) {
        return ESCAPE_BRACKETS.matcher(ESCAPE.matcher(str).replaceAll("")).replaceAll("");
    }

    /**
     * The patterns added to this {@link JPackerParser} object as a {@link List}
     * of {@link JPackerPattern}
     *
     * @return The {@link List} of {@link JPackerPattern} objects
     */
    public List<JPackerPattern> getJPatterns() {
        return jpatterns;
    }
}
