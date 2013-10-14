/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.helpers.hilight;

import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class Highlighting implements Serializable {

    public String data;
    /**
     * Starting position
     */
    public int startPos;
    /**
     * Length of highlighted text
     */
    public int len;
    private Map<String, String> properties;

    public Long getPropertyLong(String key) {
        String dataStr = getPropertyString(key);
        if (dataStr == null) {
            return null;
        }
        try {
            return Long.parseLong(dataStr);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public String getPropertyString(String key) {
        return properties.get(key);
    }

    public static Highlighting search(List<Highlighting> list, long pos) {
        return search(list, pos, null, null, -1, -1);
    }

    public static Highlighting search(List<Highlighting> list, long pos, String type) {
        return search(list, pos, "type", null, -1, -1);
    }

    public static Highlighting search(List<Highlighting> list, String property, String value) {
        return search(list, -1, property, value, -1, -1);
    }

    public static Highlighting search(List<Highlighting> list, String property, String value, int from, int to) {
        return search(list, -1, property, value, from, to);
    }

    public static Highlighting search(List<Highlighting> list, long pos, String property, String value, long from, long to) {
        Highlighting ret = null;
        for (Highlighting h : list) {
            if (property != null) {
                String v = h.getPropertyString(property);
                if (v == null) {
                    if (value != null) {
                        continue;
                    }
                } else {
                    if (!v.equals(value)) {
                        continue;
                    }
                }
            }
            if (from > -1) {
                if (h.startPos < from) {
                    continue;
                }
            }
            if (to > -1) {
                if (h.startPos > to) {
                    continue;
                }
            }
            if (pos == -1 || (pos >= h.startPos && (pos < h.startPos + h.len))) {
                if (ret == null || h.startPos > ret.startPos) { //get the closest one
                    ret = h;
                }
            }
            if (pos == -1 && ret != null) {
                return ret;
            }
        }
        return ret;
    }

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "" + startPos + "-" + (startPos + len) + " data:" + data;
    }
    
    /**
     *
     * @param startPos Starting position
     * @param len Length of highlighted text
     * @param data Highlighting data
     */
    public Highlighting(int startPos, int len, String data) {
        this.startPos = startPos;
        this.len = len;
        this.data = data;
        parseData();
    }

    private void parseData() {
        properties = new HashMap<>();
        String pairs[];
        if (data.contains(";")) {
            pairs = data.split(";");
        } else {
            pairs = new String[]{data};
        }
        for (String p : pairs) {
            String keyval[];
            if (p.contains("=")) {
                keyval = p.split("=");
            } else {
                keyval = new String[]{p, p};
            }
            properties.put(keyval[0], keyval[1]);
        }
    }

    public Highlighting(int startPos, int len, long offset) {
        this(startPos, len, "");
    }
    public static final String HLOPEN = "<ffdec:\"";
    public static final String HLEND = "\">";
    public static final String HLCLOSE = "</ffdec>";

    public static List<Highlighting> getHilights(String text) {
        return getHilights(text, null);
    }

    public static List<Highlighting> getHilights(String text, String typePrefix) {
        text = text.replace("\r\n", "\n");
        List<HilightToken> tokens = getHilightTokens(text);
        Stack<Integer> positions = new Stack<>();
        Stack<String> datas = new Stack<>();
        int pos = 0;
        List<Highlighting> ret = new ArrayList<>();
        for (HilightToken token : tokens) {
            switch (token.type) {
                case HILIGHTSTART:
                    positions.push(pos);
                    datas.push(token.value);
                    break;
                case HILIGHTEND:
                    int start = positions.pop();
                    String data = datas.pop();
                    Highlighting hl = new Highlighting(start, pos - start, data);
                    if (typePrefix == null || data.startsWith(typePrefix)) {
                        ret.add(hl);
                    }
                    break;
                case TEXT:
                    pos += token.tokenLength;
                    break;
            }
        }
        return ret;
    }

    public static List<HilightToken> getHilightTokens(String text) {
        HilightLexer lexer = new HilightLexer(new StringReader(text));
        HilightToken tok;
        List<HilightToken> ret = new ArrayList<>();
        try {
            while (true) {
                tok = lexer.lex();
                if (tok.type == TokenType.EOF) {
                    break;
                }
                ret.add(tok);
            }
        } catch (ParseException | IOException ex) {
            Logger.getLogger(Highlighting.class.getName()).log(Level.SEVERE, "Error during getting hilight tokens", ex);
        }
        return ret;
    }

    /**
     * Strips all highlights from the text
     *
     * @param text Text to strip highlights in
     * @return Text with no highlights
     */
    public static String stripHilights(String text) {
        List<HilightToken> tokens = getHilightTokens(text);
        StringBuilder ret = new StringBuilder();
        for (HilightToken token : tokens) {
            if (token.type == TokenType.TEXT) {
                ret.append(token.value);
            }
        }
        return ret.toString();
    }

    public static String trim(String highlighted) {

        List<HilightToken> tokens = getHilightTokens(highlighted);
        boolean first = true;
        StringBuilder str = new StringBuilder();
        int lastPos = 0;
        int lastLen = 0;
        for (HilightToken t : tokens) {
            switch (t.type) {
                case TEXT:
                    String s = t.value;
                    if (first) {
                        while ((!s.equals("")) && "\r\n ".contains("" + s.charAt(0))) {
                            s = s.substring(1);
                        }
                        first = false;
                    }
                    String s2 = s;
                    lastLen = 0;
                    while ((!s2.equals("")) && "\r\n ".contains("" + s2.charAt(s2.length() - 1))) {
                        s2 = s2.substring(0, s2.length() - 1);
                        lastLen++;
                    }
                    str.append(s);
                    lastPos = str.length();
                    break;
                case HILIGHTEND:
                    str.append(HLCLOSE);
                    break;
                case HILIGHTSTART:
                    str.append(HLOPEN);
                    str.append(Helper.escapeString(t.value));
                    str.append(HLEND);
                    break;
            }
        }
        if (lastLen > 0) {
            str.replace(lastPos - lastLen, lastPos, "");
        }
        return str.toString();
    }

    /**
     * Gets all trait highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of trait highlights
     */
    public static List<Highlighting> getTraitHighlights(String text) {
        return getHilights(text, "type=trait;");
    }

    /**
     * Gets all method highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of method highlights
     */
    public static List<Highlighting> getMethodHighlights(String text) {
        return getHilights(text, "type=method;");
    }

    /**
     * Gets all special highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of special highlights
     */
    public static List<Highlighting> getSpecialHighlights(String text) {
        return getHilights(text, "type=special;");
    }

    /**
     * Gets all instruction highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of instruction highlights
     */
    public static List<Highlighting> getInstrHighlights(String text) {
        return getHilights(text, "type=instruction;");
    }

    /**
     * Gets all class highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of class highlights
     */
    public static List<Highlighting> getClassHighlights(String text) {
        return getHilights(text, "type=class;");
    }
}
