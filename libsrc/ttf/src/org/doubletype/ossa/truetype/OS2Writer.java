/*
 * $Id: OS2Writer.java,v 1.11 2004/10/04 02:25:39 eed3si9n Exp $
 *
 * $Copyright: copyright (c) 2003, e.e d3si9n $
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

import java.io.IOException;

/**
 * @author e.e
 */
public class OS2Writer extends FontFormatWriter {

    final int FW_THIN = 100;

    final int FW_EXTRALIGHT = 200;

    final int FW_LIGHT = 300;

    final int FW_NORMAL = 400;

    final int FW_MEDIUM = 500;

    final int FW_SEMIBOLD = 600;

    final int FW_BOLD = 700;

    final int FW_EXTRABOLD = 800;

    final int FW_BLACK = 900;

    final int FWIDTH_NORMAL = 5;

    final int k_editableEmbedding = 0x0008;

    final int k_sansSerif = 0x0800;

    // unicode
    final int k_basicLatin = 0x0001;

    final int k_latin1Supplement = 0x0002;

    final int k_regular = 0x40;

    final int k_basicLatinStart = 0x0020;

    final int k_basciLatinEnd = 0x007e;

    private HeadWriter m_head;

    int m_xAvgCharWidth = 512;

    int m_usWeightClass = FW_NORMAL;

    int m_usWidthClass = FWIDTH_NORMAL;

    int m_fsType = 0;

    int m_ySubscriptXSize = 128;

    int m_ySubscriptYSize = 128;

    int m_ySubscriptXOffset = 0;

    int m_ySubscriptYOffset = -64;

    int m_ySuperscriptXSize = 128;

    int m_ySuperscriptYSize = 128;

    int m_ySuperscriptXOffset = 0;

    int m_ySuperscriptYOffset = 64;

    int m_yStrikeoutSize = 51;

    int m_yStrikeoutPosition = 512;

    int m_sFamilyClass = k_sansSerif;

    long m_ulUnicodeRange1 = k_basicLatin | k_latin1Supplement;

    long m_ulUnicodeRange2 = 0;

    long m_ulUnicodeRange3 = 0;

    long m_ulUnicodeRange4 = 0;

    String m_vendId = "dtyp";

    int m_fsSelection = k_regular;

    int m_usFirstCharIndex = k_basicLatinStart;

    int m_usLastCharIndex = k_basciLatinEnd;

    private int m_sTypoAscender = 1024;

    private int m_sTypoDescender = 0;

    private int m_sTypoLineGap = 0;

    private int m_usWinAscent = 1024;

    private int m_usWinDescent = 0;

    long m_ulCodePageRange1 = 0;

    long m_ulCodePageRange2 = 0;

    private int m_sxHeight = 512;

    private int m_sCapHeight = 1024;

    int m_usDefaultChar = 0x0;

    int m_usBreakChar = 0x20;

    int m_usMaxContext = 1;

    public OS2Writer(HeadWriter a_head) {
        super();

        m_head = a_head;
    }

    public void setCapHeight(int a_value) {
        m_sCapHeight = a_value;
    }

    public void setXHeight(int a_value) {
        m_sxHeight = a_value;
    }

    public void setTypoAscender(int a_value) {
        m_sTypoAscender = a_value;
    }

    public void setTypoDescender(int a_value) {
        m_sTypoDescender = a_value;
    }

    public void setTypoLineGap(int a_value) {
        m_sTypoLineGap = a_value;
    }

    public void setUnicodeRangeFlag(int a_pos) {
        int which = a_pos / 32;
        int where = a_pos % 32;
        long what = 0x1 << where;

        switch (which) {
            case 0: {
                m_ulUnicodeRange1 |= what;
            }
            break;

            case 1: {
                m_ulUnicodeRange2 |= what;
            }
            break;

            case 2: {
                m_ulUnicodeRange3 |= what;
            }
            break;

            case 3: {
                m_ulUnicodeRange4 |= what;
            }
            break;
        } // switch
    }

    public void setCodePageRangeFlag(int a_pos) {
        int which = a_pos / 32;
        int where = a_pos % 32;
        long what = 0x1 << where;

        switch (which) {
            case 0: {
                m_ulCodePageRange1 |= what;
            }
            break;

            case 1: {
                m_ulCodePageRange2 |= what;
            }
            break;
        } // switch
    }

    public void write() throws IOException {
        m_usWinAscent = m_head.getMax().y;
        m_usWinDescent = 0;
        if (m_head.getMin().y < 0) {
            m_usWinDescent = -m_head.getMin().y;
        }

        // table version number
        writeUInt16(0x02);

        writeInt16(m_xAvgCharWidth);
        writeUInt16(m_usWeightClass);
        writeUInt16(m_usWidthClass);
        writeInt16(m_fsType);
        writeInt16(m_ySubscriptXSize);
        writeInt16(m_ySubscriptYSize);
        writeInt16(m_ySubscriptXOffset);
        writeInt16(m_ySubscriptYOffset);
        writeInt16(m_ySuperscriptXSize);
        writeInt16(m_ySuperscriptYSize);
        writeInt16(m_ySuperscriptXOffset);
        writeInt16(m_ySuperscriptYOffset);
        writeInt16(m_yStrikeoutSize);
        writeInt16(m_yStrikeoutPosition);
        writeInt16(m_sFamilyClass);

        writePanose();

        writeUInt32(m_ulUnicodeRange1);
        writeUInt32(m_ulUnicodeRange2);
        writeUInt32(m_ulUnicodeRange3);
        writeUInt32(m_ulUnicodeRange4);
        writeTag(m_vendId);
        writeUInt16(m_fsSelection);
        writeUInt16(m_usFirstCharIndex);
        writeUInt16(m_usLastCharIndex);
        writeUInt16(m_sTypoAscender);
        writeUInt16(m_sTypoDescender);
        writeUInt16(m_sTypoLineGap);
        writeUInt16(m_usWinAscent);
        writeUInt16(m_usWinDescent);
        writeUInt32(m_ulCodePageRange1);
        writeUInt32(m_ulCodePageRange2);
        writeInt16(m_sxHeight);
        writeInt16(m_sCapHeight);
        writeUInt16(m_usDefaultChar);
        writeUInt16(m_usBreakChar);
        writeUInt16(m_usMaxContext);

        pad();
    }

    private void writePanose() throws IOException {
        writeUInt8(0); // family
        writeUInt8(0); // serif
        writeUInt8(0); // weight
        writeUInt8(0); // proportion
        writeUInt8(0); // contrast
        writeUInt8(0); // stroke
        writeUInt8(0); // arm style
        writeUInt8(0); // letterform
        writeUInt8(0); // midline
        writeUInt8(0); // x-height
    }

    protected String getTag() {
        return "OS/2 ";
    }
}
