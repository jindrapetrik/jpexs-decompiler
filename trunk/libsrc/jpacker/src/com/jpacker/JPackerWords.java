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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper class for a {@link JPackerWord} list built based on script's keywords (later
 * wrapped into a JPackerWord list)
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public final class JPackerWords {

    private JPackerEncoding encoding;
    private static final Pattern WORDS = Pattern.compile("\\w+");
    private List<JPackerWord> words = new ArrayList<JPackerWord>();

    /**
     * Constructor
     *
     * @param script
     *            The input script to look up for keywords
     * @param encoding
     *            The encoding level to use
     */
    public JPackerWords(String script, JPackerEncoding encoding) {
        this.encoding = encoding;
        Matcher matcher = WORDS.matcher(script);
        while (matcher.find()) {            
            add(new JPackerWord(matcher.group()));
        }
        encode();
    }

    private void add(JPackerWord word) {
        if (!words.contains(word)) {
            words.add(word);
        }
        JPackerWord w = find(word);
        w.setCount(w.getCount() + 1);
    }

    private void encode() {
        // sort by frequency
        Collections.sort(words, new Comparator<JPackerWord>() {

            @Override
            public int compare(JPackerWord x, JPackerWord y) {
                return y.getCount() - x.getCount();
            }
        });

        // a dictionary of encoding base -> base10
        Map<String, Integer> encoded = new HashMap<String, Integer>();

        for (int i = 0; i < words.size(); i++) {
            encoded.put(encoding.getEncoder().encode(i), i);
        }

        int index = 0;
        for (JPackerWord word : words) {
            if (encoded.containsKey(word.getWord())) {
                word.setIndex(encoded.get(word.getWord()));
                word.setReplacement("");
            } else {
                while (words.contains(new JPackerWord(encoding.getEncoder().encode(index)))) {
                    index++;
                }
                word.setIndex(index++);
                word.setReplacement(word.getWord());
            }
            word.setEncoded(encoding.getEncoder().encode(word.getIndex()));
        }



        // sort by encoding
        Collections.sort(words, new Comparator<JPackerWord>() {

            @Override
            public int compare(JPackerWord x, JPackerWord y) {
                return x.getIndex() - y.getIndex();
            }
        });
    }

    /**
     * Find a word in the JPackerWord list
     *
     * @param word
     *            The JPackerWord object to find in the list
     * @return The JPackerWord object if found, null otherwise
     */
    public JPackerWord find(JPackerWord word) {
        Iterator<JPackerWord> it = words.iterator();
        while (it.hasNext() == true) {
            JPackerWord pw = it.next();
            if (pw.equals(word)) {
                return pw;
            }
        }
        return null;
    }

    /**
     * Gets the list of JPackerWord objects
     *
     * @return The list of JPackerWord objects
     */
    public List<JPackerWord> getWords() {
        return words;
    }

    /**
     * This method has been overridden to return the list of JPackerWord objects
     * as a single String object separated by the '|' character
     *
     * @return A List of JPackerWord objects as a single String object separated
     *         by the '|' character
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (JPackerWord word : words) {
            sb.append(word.getReplacement()).append('|');
        }        
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
