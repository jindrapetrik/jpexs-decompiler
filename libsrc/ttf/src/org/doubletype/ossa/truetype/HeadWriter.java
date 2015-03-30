/*
 * $Id: HeadWriter.java,v 1.7 2004/09/26 09:15:48 eed3si9n Exp $
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

import java.awt.Point;
import java.io.IOException;
import java.util.Date;

/**
 * @author e.e
 */
public class HeadWriter extends FontFormatWriter {

    static public final int k_yZeroIsBaseLine = 0x1; // bit 0

    static public final int k_xLeftMostBlackIsLsb = 0x2; // bit 1

    static public final int k_scaledPointDiffer = 0x4; // bit 2

    static public final int k_useIntegerScaling = 0x8; // bit 3

    // used by microsoft
    static public final int k_scaleLinear = 0x10;

    // for vertical fonts
    static public final int k_xZeroIsBaseLine = 0x20;

    // 0x40
    static public final int k_linguisticRendering = 0x80;

    static public final int k_defaultMetamorphosis = 0x100;

    static public final int k_rightToLeft = 0x200;

    static public final int k_indicRearrangement = 0x400;

    private final long k_magicNumber = 0x5f0f3cf5;

    private long m_checkSumAdjustment = 0;

    private Point m_min = new Point(0, 0);

    private Point m_max = new Point(0, 0);
    
    private Date m_creationDate = new Date();

    private Date m_modificationDate = m_creationDate;

    public HeadWriter() {
        super();
    }

    void setCheckSumAdjustment(long a_value) {
        m_checkSumAdjustment = a_value;
    }

    public Point getMin() {
        return m_min;
    }

    public Point getMax() {
        return m_max;
    }

    public void updateMin(Point a_value) {
        if (a_value.x < m_min.x) {
            m_min.x = a_value.x;
        }

        if (a_value.y < m_min.y) {
            m_min.y = a_value.y;
        }
    }

    public void updateMax(Point a_value) {
        if (a_value.x > m_max.x) {
            m_max.x = a_value.x;
        }

        if (a_value.y > m_max.y) {
            m_max.y = a_value.y;
        }
    }
    
    public void setCreationDate(Date a_date) {
        m_creationDate = a_date;
    }

    public void setModificationDate(Date a_date) {
        m_modificationDate = a_date;
    }

    public void write() throws IOException {
        // table version number
        writeFixed32(1.0);

        // fontRevision
        writeFixed32(1.0);

        writeUInt32(m_checkSumAdjustment);
        writeUInt32(k_magicNumber);

        // LSB is the distance from 0, 0 to the left of the glyph bounds.
        // flags
        writeUInt16(k_yZeroIsBaseLine
                | k_xLeftMostBlackIsLsb
                | k_scaledPointDiffer);;

        // unitsPerEm
        writeUInt16(1024);

        // created, modified
        writeLongDateTime(m_creationDate);
        writeLongDateTime(m_modificationDate);

        writeFWord(m_min.x);
        writeFWord(m_min.y);
        writeFWord(m_max.x);
        writeFWord(m_max.y);

        // macStyle
        writeUInt16(0);

        // lowestRecPPEM
        writeUInt16(11);

        // font direction hint
        // 2, for strongly left to right
        // but also contains neutrals
        writeInt16(2);

        // indexToLocFormat. 1, for long
        writeInt16(1);

        // glyfDataFormat
        writeInt16(0);
        pad();
    }

    protected String getTag() {
        return "head";
    }
}
