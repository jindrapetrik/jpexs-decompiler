/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class Highlighting {
    /**
     * Starting position
     */
    public int startPos;
    /**
     * Length of highlighted text
     */
    public int len;
    /**
     * Offset of instruction or trait
     */
    public long offset;

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "" + startPos + "-" + (startPos + len) + " ofs" + offset;
    }

    /**
     * Constructor
     *
     * @param startPos Starting position
     * @param len      Length of highlighted text
     * @param offset   Offset of instruction or trait
     */
    public Highlighting(int startPos, int len, long offset) {
        this.startPos = startPos;
        this.len = len;
        this.offset = offset;
    }


    private static final String OFSOPEN = "[OFS";
    private static final String OFSCLOSE = "]";
    private static final String OFSEND = "[/OFS]";

    private static final String TRAITOPEN = "[TRAIT";
    private static final String TRAITCLOSE = "]";
    private static final String TRAITEND = "[/TRAIT]";


    /**
     * Highlights specified text as instruction by adding special tags
     *
     * @param text   Text to highlight
     * @param offset Offset of instruction
     * @return Highlighted text
     */
    public static String hilighOffset(String text, long offset) {
        return OFSOPEN + offset + OFSCLOSE + text + OFSEND;
    }

    /**
     * Highlights specified text as trait by adding special tags
     *
     * @param text   Text to highlight
     * @param offset Offset of trait
     * @return Highlighted text
     */
    public static String hilighTrait(String text, long offset) {
        return TRAITOPEN + offset + TRAITCLOSE + text + TRAITEND;
    }

    /**
     * Strips all highlights from the text
     *
     * @param text Text to strip highlights in
     * @return Text with no highlights
     */
    public static String stripHilights(String text) {
        text = stripInstrHilights(text);
        text = stripTraitHilights(text);
        return text;
    }

    /**
     * Strips instruction highlights from the text
     *
     * @param text Text to strip instruction highlights in
     * @return Text with no instruction highlights
     */
    public static String stripInstrHilights(String text) {
        text = text.replaceAll(Pattern.quote(OFSOPEN) + "[0-9]+" + Pattern.quote(OFSCLOSE), "");
        text = text.replace(OFSEND, "");
        return text;
    }

    /**
     * Strips trait highlights from the text
     *
     * @param text Text to strip trait highlights in
     * @return Text with no trait highlights
     */
    public static String stripTraitHilights(String text) {
        text = text.replaceAll(Pattern.quote(TRAITOPEN) + "[0-9]+" + Pattern.quote(TRAITCLOSE), "");
        text = text.replace(TRAITEND, "");
        return text;
    }

    /**
     * Gets all trait highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of trait highlights
     */
    public static List<Highlighting> getTraitHighlights(String text) {
        text = text.replace("\r\n", "\n");
        text = stripInstrHilights(text);
        List<Highlighting> ret = new ArrayList<Highlighting>();
        int pos = 0;
        while (true) {
            int openpos = text.indexOf(TRAITOPEN);
            if (openpos == -1) break;
            int closepos = text.indexOf(TRAITCLOSE, openpos);
            int enpos = text.indexOf(TRAITEND, openpos);
            int textlen = enpos - closepos - TRAITCLOSE.length();

            int nextopenpos = text.indexOf(TRAITOPEN, openpos + 1);
            if (nextopenpos != -1) {
                if (nextopenpos < closepos) {
                    System.err.println(text);
                    throw new RuntimeException("Crossed highlight");
                }
            }
            long offset = Long.parseLong(text.substring(openpos + TRAITOPEN.length(), closepos));
            Highlighting hl = new Highlighting(pos + openpos, textlen, offset);
            pos += openpos + textlen;
            text = text.substring(enpos + TRAITEND.length());
            ret.add(hl);
        }
        return ret;
    }

    /**
     * Gets all instruction highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of instruction highlights
     */
    public static List<Highlighting> getInstrHighlights(String text) {
        text = text.replace("\r\n", "\n");
        text = stripTraitHilights(text);
        List<Highlighting> ret = new ArrayList<Highlighting>();
        int pos = 0;
        while (true) {
            int openpos = text.indexOf(OFSOPEN);
            if (openpos == -1) break;
            int closepos = text.indexOf(OFSCLOSE, openpos);
            int enpos = text.indexOf(OFSEND, openpos);
            int textlen = enpos - closepos - OFSCLOSE.length();

            int nextopenpos = text.indexOf(OFSOPEN, openpos + 1);
            if (nextopenpos != -1) {
                if (nextopenpos < closepos) {
                    System.err.println(text);
                    throw new RuntimeException("Crossed highlight");
                }
            }
            long offset = Long.parseLong(text.substring(openpos + OFSOPEN.length(), closepos));
            Highlighting hl = new Highlighting(pos + openpos, textlen, offset);
            pos += openpos + textlen;
            text = text.substring(enpos + OFSEND.length());
            ret.add(hl);
        }
        return ret;
    }
}
