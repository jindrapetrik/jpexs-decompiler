/*
 * $Id: TTUnicodeRange.java,v 1.1 2004/01/25 11:00:10 eed3si9n Exp $
 *
 * $Copyright: copyright (c) 2003-2004, e.e d3si9n $
 * $License:
 * This source code is part of DoubleType.
 * DoubleType is a graphical typeface designer.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, e.e d3si9n gives permission to
 * link the code of this program with any Java Platform that is available
 * to public with free of charge, including but not limited to
 * Sun Microsystem's JAVA(TM) 2 RUNTIME ENVIRONMENT (J2RE),
 * and distribute linked combinations including the two.
 * You must obey the GNU General Public License in all respects for all
 * of the code used other than Java Platform. If you modify this file,
 * you may extend this exception to your version of the file, but you are not
 * obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * $
 */
package org.doubletype.ossa.truetype;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;

/**
 * @author e.e
 */
public class TTUnicodeRange implements Comparable<TTUnicodeRange> {

    public static final long k_notDef = 0x0001;

    public static final long k_null = 0x0000;

    public static final long k_cr = 0x000D;

    public static final long k_space = 0x0020;

    static private boolean s_isInitialized = false;

    static private ArrayList<TTUnicodeRange> s_list = new ArrayList<>();

    static private TTUnicodeRange s_selected = null;

    static public TTUnicodeRange of(long a_unicode) {
        initList();

        TTUnicodeRange retval = null;
        UnicodeBlock block = UnicodeBlock.of((int) a_unicode);
        if (block == null) {
            return retval;
        }

        int i;
        for (i = 0; i < s_list.size(); i++) {
            TTUnicodeRange range = s_list.get(i);
            if (range.m_block.equals(block)) {
                return range;
            }
        }

        return retval;
    }

    static public TTUnicodeRange getLastFound() {
        return s_selected;
    }

    static private void initList() {
        if (s_isInitialized) {
            return;
        }

        s_isInitialized = true;

        s_list.add(new TTUnicodeRange(
                UnicodeBlock.BASIC_LATIN, 0x0020, 0x007F, 0, 63));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.LATIN_1_SUPPLEMENT, 0x0080, 0x00FF, 1, 0));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.LATIN_EXTENDED_A, 0x0100, 0x017f, 2));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.LATIN_EXTENDED_B, 0x0180, 0x024f, 3));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.IPA_EXTENSIONS, 0x0250, 0x02af, 4));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.SPACING_MODIFIER_LETTERS, 0x02B0, 0x02FF, 5));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.COMBINING_DIACRITICAL_MARKS, 0x0300, 0x036F, 6));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.GREEK, 0x0370, 0x03FF, 7, 3));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CYRILLIC, 0x0400, 0x04FF, 9, 2));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ARMENIAN, 0x0530, 0x058F, 10));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.HEBREW, 0x0590, 0x05FF, 11, 5));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ARABIC, 0x0600, 0x06FF, 13, 6));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.SYRIAC, 0x0700, 0x074F, 71));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.THAANA, 0x0780, 0x07BF, 72));

        s_list.add(new TTUnicodeRange(
                UnicodeBlock.DEVANAGARI, 0x0900, 0x097F, 15));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.BENGALI, 0x0980, 0x09FF, 16));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.GURMUKHI, 0x0A00, 0x0A7F, 17));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.GUJARATI, 0x0A80, 0x0AFF, 18));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ORIYA, 0x0B00, 0x0B7F, 19));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.TAMIL, 0x0B80, 0x0BFF, 20));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.TELUGU, 0x0C00, 0x0C7F, 21));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.KANNADA, 0x0C80, 0x0CFF, 22));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.MALAYALAM, 0x0D00, 0x0D7F, 23));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.SINHALA, 0x0D80, 0x0DFF, 73));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.THAI, 0x0E00, 0x0E7F, 24, 16));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.LAO, 0x0E80, 0x0EFF, 25));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.TIBETAN, 0x0F00, 0x0FFF, 70));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.MYANMAR, 0x1000, 0x109F, 74));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.GEORGIAN, 0x10A0, 0x10FF, 26));
        // TODO: wansung or johab?
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.HANGUL_JAMO, 0x1100, 0x11FF, 28, 19));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ETHIOPIC, 0x1200, 0x137F, 75));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CHEROKEE, 0x13A0, 0x13FF, 76));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS, 0x1400, 0x167F, 77));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.OGHAM, 0x1680, 0x169F, 78));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.RUNIC, 0x16A0, 0x16FF, 79));

		// TODO: tagalog, hanunoo, buhid, tagbanwa
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.KHMER, 0x1780, 0x17FF, 80));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.MONGOLIAN, 0x1800, 0x18AF, 81));

		// linbu, tai le, khmer symbol, phonetic extensions,
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.LATIN_EXTENDED_ADDITIONAL, 0x1E00, 0x1EFF, 29));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.GREEK_EXTENDED, 0x1F00, 0x1FFF, 30));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.GENERAL_PUNCTUATION, 0x2000, 0x206F, 31));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS, 0x2070, 0x209F, 32));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CURRENCY_SYMBOLS, 0x20A0, 0x20CF, 33));

        // combining diacritical marks
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS, 0x20D0, 0x20FF, 34));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.LETTERLIKE_SYMBOLS, 0x2100, 0x214F, 35));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.NUMBER_FORMS, 0x2150, 0x218F, 36));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ARROWS, 0x2190, 0x21FF, 37));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.MATHEMATICAL_OPERATORS, 0x2200, 0x22FF, 38));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.MISCELLANEOUS_TECHNICAL, 0x2300, 0x23FF, 39));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CONTROL_PICTURES, 0x2400, 0x243F, 40));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.OPTICAL_CHARACTER_RECOGNITION, 0x2440, 0x245F, 41));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ENCLOSED_ALPHANUMERICS, 0x2460, 0x24FF, 42));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.BOX_DRAWING, 0x2500, 0x257F, 43));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.BLOCK_ELEMENTS, 0x2580, 0x259F, 44));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.GEOMETRIC_SHAPES, 0x25A0, 0x25FF, 45));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.MISCELLANEOUS_SYMBOLS, 0x2600, 0x26FF, 46));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.DINGBATS, 0x2700, 0x27BF, 47));

		// TODO: mics. math symbols A, supplemental arrows A
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.BRAILLE_PATTERNS, 0x2800, 0x28FF, 82));

		// TODO: supplemental arrows B, mics. math symbols B,
        // supplemental math op., mics. symbols and arrows
//		CJKV supplements
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CJK_RADICALS_SUPPLEMENT, 0x2E80, 0x2EFF, 59));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.KANGXI_RADICALS, 0x2F00, 0x2FDF, 59));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS, 0x2FF0, 0x2FFF, 59));

        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION, 0x3000, 0x303f, 48));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.HIRAGANA, 0x3040, 0x309f, 49, 17));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.KATAKANA, 0x30a0, 0x30ff, 50, 17));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.BOPOMOFO, 0x3100, 0x312f, 51));
        // TODO: wansung or johab?
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.HANGUL_COMPATIBILITY_JAMO, 0x3130, 0x0318F, 52, 19));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.KANBUN, 0x3190, 0x319F, 59));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.BOPOMOFO_EXTENDED, 0x31A0, 0x31BF, 51));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS, 0x31F0, 0x31FF, 50, 17));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS, 0x3200, 0x32FF, 54));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CJK_COMPATIBILITY, 0x3300, 0x33ff, 55));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, 0x03400, 0x4dbf, 59));
		// TODO: yijing hex symbols

        // the kanji characters
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS, 0x4e00, 0x9fff, 59, 17));

        s_list.add(new TTUnicodeRange(
                UnicodeBlock.YI_SYLLABLES, 0xA000, 0xA48F, 83));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.YI_RADICALS, 0xA490, 0xA4CF, 83));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.HANGUL_SYLLABLES, 0xAC00, 0xD7AF, 56));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.HIGH_SURROGATES, 0xD800, 0xDB7F, 0));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.HIGH_PRIVATE_USE_SURROGATES, 0xDB80, 0xDBFF, 0));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.LOW_SURROGATES, 0xDC00, 0xDFFF, 0));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.PRIVATE_USE_AREA, 0xE000, 0xF8FF, 60));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS, 0xf900, 0xfaff, 61));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ALPHABETIC_PRESENTATION_FORMS, 0xFB00, 0xFB4F, 62));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ARABIC_PRESENTATION_FORMS_A, 0xFB50, 0xFDFF, 62));

		// TODO: variation selectors
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.COMBINING_HALF_MARKS, 0xFE20, 0xFE2F, 64));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.CJK_COMPATIBILITY_FORMS, 0xFE30, 0xFE4F, 65));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.SMALL_FORM_VARIANTS, 0xFE50, 0xFE6F, 66));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.ARABIC_PRESENTATION_FORMS_B, 0xFE70, 0xFEFF, 67));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS, 0xFF00, 0xFFEF, 68, 17));
        s_list.add(new TTUnicodeRange(
                UnicodeBlock.SPECIALS, 0xFFF0, 0xFFFF, 69));
    }

    static public boolean find(String a_unicodeRange) {
        initList();

        s_selected = null;

        int i;
        for (i = 0; i < s_list.size(); i++) {
            TTUnicodeRange range = (TTUnicodeRange) s_list.get(i);
            if (range.m_block.toString().equals(a_unicodeRange)) {
                s_selected = range;

                return true;
            }
        }

        return false;
    }

    private UnicodeBlock m_block = null;

    private long m_start = 0;

    private long m_end = 0;

    /**
     * http://www.microsoft.com/typography/otspec/os2.htm
     */
    private int m_osTwoFlag = 0;

    /**
     * http://www.microsoft.com/typography/otspec/os2.htm
     */
    private int m_codePageFlag = 0;

    public TTUnicodeRange(UnicodeBlock a_block,
            long a_start,
            long a_end,
            int a_osTwoFlag) {
        m_block = a_block;
        m_start = a_start;
        m_end = a_end;
        m_osTwoFlag = a_osTwoFlag;
    }

    public TTUnicodeRange(UnicodeBlock a_block,
            long a_start,
            long a_end,
            int a_osTwoFlag,
            int a_codePageFlag) {
        m_block = a_block;
        m_start = a_start;
        m_end = a_end;
        m_osTwoFlag = a_osTwoFlag;
        m_codePageFlag = a_codePageFlag;
    }

    public boolean equals(Object a_object) {
        TTUnicodeRange object = (TTUnicodeRange) a_object;
        return (m_start == object.m_start);
    }

    public int compareTo(TTUnicodeRange a_object) {
        TTUnicodeRange object = a_object;
        if (this.m_start < object.m_start) {
            return -1;
        } else if (this.m_start == object.m_start) {
            return 0;
        } else {
            return 1;
        }
    }

    public String toString() {
        return m_block.toString();
    }

    public long getStartCode() {
        return m_start;
    }

    public long getEndCode() {
        return m_end;
    }

    public int getOsTwoFlag() {
        return m_osTwoFlag;
    }

    public int getCodeRangeFlag() {
        return m_codePageFlag;
    }
}
