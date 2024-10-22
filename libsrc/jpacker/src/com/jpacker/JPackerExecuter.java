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
import java.util.Formatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jpacker.encoders.BasicEncoder;
import com.jpacker.encoders.Encoder;
import com.jpacker.strategies.DefaultReplacementStrategy;
import com.jpacker.strategies.ReplacementStrategy;

/**
 * Packer class.
 * 
 * Main jPacker class that packs the script.
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public class JPackerExecuter {

    private JPackerEncoding encoding;
    private static final String UNPACK = "eval(function(p,a,c,k,e,r){e=%5$s;if(!''.replace(/^/,String)){while(c--)r[%6$s]=k[c]"
            + "||%6$s;k=[function(e){return r[e]}];e=function(){return'\\\\w+'};c=1};while(c--)if(k[c])p=p."
            + "replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c]);return p}('%1$s',%2$s,%3$s,'%4$s'.split('|'),0,{}))";

    /**
     * Constructor
     *
     * @param encoding
     *            The encoding level for this instance
     */
    public JPackerExecuter(JPackerEncoding encoding) {
        setEncoding(encoding);
    }

    /**
     * Packs the script
     *
     * @param script
     *            The script to pack
     * @param minifyOnly
     *            True if script should only be minified and not encoded and/or
     *            its variables shrunk, false otherwise.
     * @param shrinkVariables
     *            True if variables should be shrunk, false otherwise. If
     *            minifyOnly is true, this option has no side effect.
     * @return The packed script
     */
    public String pack(String script, boolean minifyOnly, boolean shrinkVariables) {
        script += "\n";
        script = minify(script);        
        if (!minifyOnly) {
            if (shrinkVariables) {
                script = shrinkVariables(script);
            }            
            if (encoding != JPackerEncoding.NONE) {
                script = encode(script);
            }
        }
        return script;
    }

    // zero encoding - just removal of whitespace and comments
    private String minify(String script) {
        JPackerParser parser = new JPackerParser();
        ReplacementStrategy defaultStrategy = new DefaultReplacementStrategy();
        // protect data
        parser = addDataRegEx(parser);
        script = parser.exec(script, defaultStrategy);
        // remove white-space
        parser = addWhiteSpaceRegEx(parser);
        script = parser.exec(script, defaultStrategy);
        // clean
        parser = addCleanUpRegEx(parser);
        script = parser.exec(script, defaultStrategy);
        // done
        return script;
    }

    private JPackerParser addDataRegEx(JPackerParser parser) {
        final String COMMENT1 = "(\\/\\/|;;;)[^\\n]*";
        final String COMMENT2 = "\\/\\*[^*]*\\*+([^\\/][^*]*\\*+)*\\/";
        final String REGEX = "\\/(\\\\[\\/\\\\]|[^*\\/])(\\\\.|[^\\/\\n\\\\])*\\/[gim]*";

        // Packer.CONTINUE
        parser.remove("\\\\\\r?\\n");

        parser.ignore("'(\\\\.|[^'\\\\])*'");
        parser.ignore("\"(\\\\.|[^\"\\\\])*\"");
        parser.ignore("\\/\\*@|@\\*\\/|\\/\\/@[^\\n]*\\n");
        parser.replace("(" + COMMENT1 + ")\\n\\s*(" + REGEX + ")?", "\n$3");
        parser.replace("(" + COMMENT2 + ")\\s*(" + REGEX + ")?", " $3");
        parser.replace("([\\[\\(\\^=,{}:;&|!*?])\\s*(" + REGEX + ")", "$1$2");
        return parser;
    }

    private JPackerParser addCleanUpRegEx(JPackerParser parser) {
        parser.replace("\\(\\s*;\\s*;\\s*\\)", "(;;)");
        parser.ignore("throw[};]+[};]"); // safari 1.3 bug
        parser.replace(";+\\s*([};])", "$1");
        parser.remove(";;[^\\n\\r]+[\\n\\r]");
        return parser;
    }

    private JPackerParser addWhiteSpaceRegEx(JPackerParser parser) {
        parser.replace("(\\d)\\s+(\\.\\s*[a-z\\$_\\[\\(])", "$1 $2");
        parser.replace("([+\\-])\\s+([+\\-])", "$1 $2");
        parser.replace("(\\b|\\$)\\s+(\\b|\\$)", "$1 $2");
        parser.replace("\\b\\s+\\$\\s+\\b", " $ ");
        parser.replace("\\$\\s+\\b", "$ ");
        parser.replace("\\b\\s+\\$", " $");
        parser.replace("\\b\\s+\\b", " ");
        parser.remove("\\s+");
        return parser;
    }

    private String shrinkVariables(String script) {
        final Pattern pattern = Pattern.compile("^[^'\"]\\/");
        // identify blocks, particularly identify function blocks (which define
        // scope)
        Pattern blockPattern = Pattern.compile("(function\\s*[\\w$]*\\s*\\(\\s*([^\\)]*)\\s*\\)\\s*)?(\\{([^{}]*)\\})");
        List<String> blocks = new ArrayList<String>(); // store program blocks
        // (anything between
        // braces {})

        final List<String> data = new ArrayList<String>(); // encoded strings
        // and regular
        // expressions

        JPackerParser parser = new JPackerParser();
        parser = addDataRegEx(parser);
        script = parser.exec(script, new ReplacementStrategy() {

            @Override
            public String replace(List<JPackerPattern> patterns, Matcher matcher) {
                String replacement = "#" + data.size();
                String string = matcher.group();
                if (pattern.matcher(string).find()) {
                    replacement = string.charAt(0) + replacement;
                    string = string.substring(1);
                }
                data.add(string);
                return replacement;
            }
        });

        do {
            // put the blocks back
            Matcher blockMatcher = blockPattern.matcher(script);
            StringBuffer sb = new StringBuffer();
            while (blockMatcher.find()) {
                blockMatcher.appendReplacement(sb, encodeBlock(blockMatcher, blocks));
            }
            blockMatcher.appendTail(sb);
            script = sb.toString();
        } while (blockPattern.matcher(script).find());

        while (Pattern.compile("~(\\d+)~").matcher(script).find()) {
            script = decodeBlock(script, blocks);
        }
        // put strings and regular expressions back
        Matcher storeMatcher = Pattern.compile("#(\\d+)").matcher(script);
        StringBuffer sb2 = new StringBuffer();
        while (storeMatcher.find()) {
            int num = Integer.parseInt(storeMatcher.group(1));
            storeMatcher.appendReplacement(sb2, Matcher.quoteReplacement(data.get(num)));
        }
        storeMatcher.appendTail(sb2);

        return sb2.toString();
    }

    private String encode(String script) {
        JPackerWords words = new JPackerWords(script, encoding);

        Pattern wordsPattern = Pattern.compile("\\w+");
        Matcher wordsMatcher = wordsPattern.matcher(script);
        StringBuffer sb = new StringBuffer();
        while (wordsMatcher.find()) {
            JPackerWord tempWord = new JPackerWord(wordsMatcher.group());
            wordsMatcher.appendReplacement(sb, words.find(tempWord).getEncoded());
        }
        wordsMatcher.appendTail(sb);

        int ascii = Math.min(Math.max(words.getWords().size(), 2), encoding.getEncodingBase());

        String p = escape(sb.toString());
        String a = String.valueOf(ascii);
        String c = String.valueOf(words.getWords().size());
        String k = words.toString();
        String e = getEncode(ascii);
        String r = ascii > 10 ? "e(c)" : "c";

        return new Formatter().format(UNPACK, p, a, c, k, e, r).toString();
    }

    // encoder for program blocks
    private String encodeBlock(Matcher matcher, List<String> blocks) {

        String block = matcher.group();
        String func = matcher.group(1);
        String args = matcher.group(2);

        if (func != null && !func.isEmpty()) { // the block is a function block
            // decode the function block (THIS IS THE IMPORTANT BIT)
            // We are retrieving all sub-blocks and will re-parse them in light
            // of newly shrunk variables
            while (Pattern.compile("~(\\d+)~").matcher(block).find()) {
                block = decodeBlock(block, blocks);
            }

            // create the list of variable and argument names
            Pattern varNamePattern = Pattern.compile("var\\s+[\\w$]+");
            Matcher varNameMatcher = varNamePattern.matcher(block);
            StringBuilder sb = new StringBuilder();
            while (varNameMatcher.find()) {
                sb.append(varNameMatcher.group()).append(",");
            }

            String vars = "";
            if (!sb.toString().isEmpty()) {
                vars = sb.deleteCharAt(sb.length() - 1).toString().replaceAll("var\\s+", "");
            }

            String[] ids = concat(args.split("\\s*,\\s*"), vars.split("\\s*,\\s*"));
            Set<String> idList = new LinkedHashSet<String>();
            for (String s : ids) {
                if (!s.isEmpty()) {
                    idList.add(s);
                }
            }
            // process each identifier
            int count = 0;
            String shortId;
            for (String id : idList) {
                id = id.trim();
                if (id.length() > 1) { // > 1 char
                    id = Matcher.quoteReplacement(id);
                    // find the next free short name (check everything in the
                    // current scope)
                    Encoder e = new BasicEncoder();
                    do {
                        shortId = e.encode(count++);
                    } while (Pattern.compile("[^\\w$.]" + shortId + "[^\\w$:]").matcher(block).find());
                    // replace the long name with the short name
                    while (Pattern.compile("([^\\w$.])" + id + "([^\\w$:])").matcher(block).find()) {
                        block = block.replaceAll("([^\\w$.])" + id + "([^\\w$:])", "$1" + shortId + "$2");
                    }
                    block = block.replaceAll("([^{,\\w$.])" + id + ":", "$1" + shortId + ":");
                }
            }
        }
        String replacement = "~" + blocks.size() + "~";
        blocks.add(block);
        return replacement;
    }

    private String decodeBlock(String block, List<String> blocks) {
        Matcher encoded = Pattern.compile("~(\\d+)~").matcher(block);
        StringBuffer sbe = new StringBuffer();
        while (encoded.find()) {
            int num = Integer.parseInt(encoded.group(1));
            encoded.appendReplacement(sbe, Matcher.quoteReplacement(blocks.get(num)));
        }
        encoded.appendTail(sbe);
        return sbe.toString();
    }

    private String[] concat(String[] a, String[] b) {
        String[] c = new String[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private String getEncode(int ascii) {
        if (ascii > 96) {
            return JPackerEncoding.HIGH_ASCII.getEncode();
        } else if (ascii > 36) {
            return JPackerEncoding.NORMAL.getEncode();
        } else if (ascii > 10) {
            return JPackerEncoding.MID.getEncode();
        } else {
            return JPackerEncoding.NUMERIC.getEncode();
        }
    }

    private String escape(String input) {
        // single quotes wrap the final string so escape them
        // also escape new lines required by conditional comments
        return input.replaceAll("([\\\\'])", "\\\\$1").replaceAll("[\\r\\n]+", "\\n");
    }

    /**
     * Encoding level. Options are: {@link JPackerEncoding#NONE},
     * {@link JPackerEncoding#NUMERIC}, {@link JPackerEncoding#MID},
     * {@link JPackerEncoding#NORMAL} and {@link JPackerEncoding#HIGH_ASCII}.
     *
     * @return The current encoding level
     */
    public JPackerEncoding getEncoding() {
        return encoding;
    }

    /**
     * Set the encoding level to use.
     *
     * @param encoding
     */
    public final void setEncoding(JPackerEncoding encoding) {
        this.encoding = encoding;
    }
}
