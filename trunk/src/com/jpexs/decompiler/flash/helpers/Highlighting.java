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
package com.jpexs.decompiler.flash.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * Turn hignlighting on/off
     */
    public static boolean doHighlight = true;

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
     * @param len Length of highlighted text
     * @param offset Offset of instruction or trait
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
    private static final String METHODOPEN = "[METHOD";
    private static final String METHODCLOSE = "]";
    private static final String METHODEND = "[/METHOD]";
    private static final String CLASSOPEN = "[CLASS";
    private static final String CLASSCLOSE = "]";
    private static final String CLASSEND = "[/CLASS]";

    /**
     * Highlights specified text as instruction by adding special tags
     *
     * @param text Text to highlight
     * @param offset Offset of instruction
     * @return Highlighted text
     */
    public static String hilighOffset(String text, long offset) {
        if (!doHighlight) {
            return text;
        }
        return OFSOPEN + offset + OFSCLOSE + text + OFSEND;
    }

    /**
     * Highlights specified text as method by adding special tags
     *
     * @param text Text to highlight
     * @param index Methodinfo index
     * @return Highlighted text
     */
    public static String hilighMethod(String text, long index) {
        if (!doHighlight) {
            return text;
        }
        return hilighMethodBegin(index) + text + hilighMethodEnd();
    }

    /**
     * Beggining of hilight of method
     *
     * @param index
     * @return
     */
    public static String hilighMethodBegin(long index) {
        if (!doHighlight) {
            return "";
        }
        return METHODOPEN + index + METHODCLOSE;
    }

    /**
     * Ending of hilight of method
     *
     * @return
     */
    public static String hilighMethodEnd() {
        if (!doHighlight) {
            return "";
        }
        return METHODEND;
    }

    /**
     * Highlights specified text as trait by adding special tags
     *
     * @param text Text to highlight
     * @param offset Offset of trait
     * @return Highlighted text
     */
    public static String hilighTrait(String text, long offset) {
        if (!doHighlight) {
            return text;
        }
        return TRAITOPEN + offset + TRAITCLOSE + text + TRAITEND;
    }

    /**
     * Highlights specified text as class by adding special tags
     *
     * @param text Text to highlight
     * @param offset Offset of trait
     * @return Highlighted text
     */
    public static String hilighClass(String text, long offset) {
        if (!doHighlight) {
            return text;
        }
        return CLASSOPEN + offset + CLASSCLOSE + text + CLASSEND;
    }

    /**
     * Strips all highlights from the text
     *
     * @param text Text to strip highlights in
     * @return Text with no highlights
     */
    public static String stripHilights(String text) {
        if (!doHighlight) {
            return text;
        }
        text = stripInstrHilights(text);
        text = stripTraitHilights(text);
        text = stripMethodHilights(text);
        text = stripClassHilights(text);
        return text;
    }

    /**
     * Strips instruction highlights from the text
     *
     * @param text Text to strip instruction highlights in
     * @return Text with no instruction highlights
     */
    public static String stripInstrHilights(String text) {
        return stripSpecificHilights(text, OFSOPEN, OFSCLOSE, OFSEND);
    }

    /**
     * Strips method highlights from the text
     *
     * @param text Text to strip method highlights in
     * @return Text with no method highlights
     */
    public static String stripMethodHilights(String text) {
        return stripSpecificHilights(text, METHODOPEN, METHODCLOSE, METHODEND);
    }

    /**
     * Strips trait highlights from the text
     *
     * @param text Text to strip trait highlights in
     * @return Text with no trait highlights
     */
    public static String stripTraitHilights(String text) {
        return stripSpecificHilights(text, TRAITOPEN, TRAITCLOSE, TRAITEND);
    }

    /**
     * Strips class highlights from the text
     *
     * @param text Text to strip trait highlights in
     * @return Text with no class highlights
     */
    public static String stripClassHilights(String text) {
        return stripSpecificHilights(text, CLASSOPEN, CLASSCLOSE, CLASSEND);
    }

    private static String stripSpecificHilights(String text, String txtOpen, String txtClose, String txtEnd) {
        text = text.replaceAll(Pattern.quote(txtOpen) + "[0-9]+" + Pattern.quote(txtClose), "");
        text = text.replace(txtEnd, "");
        return text;
    }

    private static List<Highlighting> getSpecificHighlights(String text, String txtOpen, String txtClose, String txtEnd) {
        List<Highlighting> ret = new ArrayList<Highlighting>();
        int pos = 0;
        while (true) {
            int openpos = text.indexOf(txtOpen);
            if (openpos == -1) {
                break;
            }
            int closepos = text.indexOf(txtClose, openpos);
            int enpos = text.indexOf(txtEnd, openpos);
            int textlen = enpos - closepos - txtClose.length();

            int nextopenpos = text.indexOf(txtOpen, openpos + 1);
            if (nextopenpos != -1) {
                if (nextopenpos < enpos) {
                    Logger.getLogger(Highlighting.class.getName()).log(Level.SEVERE, "Crossed highligh - str:{0}", text);
                    enpos = nextopenpos;
                }
            }
            long offset = Long.parseLong(text.substring(openpos + txtOpen.length(), closepos));
            Highlighting hl = new Highlighting(pos + openpos, textlen, offset);
            pos += openpos + textlen;
            text = text.substring(enpos + txtEnd.length());
            ret.add(hl);
        }
        return ret;
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
        text = stripMethodHilights(text);
        text = stripClassHilights(text);
        return getSpecificHighlights(text, TRAITOPEN, TRAITCLOSE, TRAITEND);
    }

    /**
     * Gets all method highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of method highlights
     */
    public static List<Highlighting> getMethodHighlights(String text) {
        text = text.replace("\r\n", "\n");
        text = stripInstrHilights(text);
        text = stripTraitHilights(text);
        text = stripClassHilights(text);
        return getSpecificHighlights(text, METHODOPEN, METHODCLOSE, METHODEND);
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
        text = stripMethodHilights(text);
        text = stripClassHilights(text);
        return getSpecificHighlights(text, OFSOPEN, OFSCLOSE, OFSEND);
    }

    /**
     * Gets all class highlight objects from specified text
     *
     * @param text Text to get highlights from
     * @return List of class highlights
     */
    public static List<Highlighting> getClassHighlights(String text) {
        text = text.replace("\r\n", "\n");
        text = stripTraitHilights(text);
        text = stripMethodHilights(text);
        text = stripInstrHilights(text);
        return getSpecificHighlights(text, CLASSOPEN, CLASSCLOSE, CLASSEND);
    }
}
