/*
 * $Id: HheaWriter.java,v 1.5 2004/09/26 09:15:48 eed3si9n Exp $
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
 * HheaWriter depends on HtmxWriter.
 *
 * @author e.e
 */
public class HheaWriter extends FontFormatWriter {

    private GlyfWriter m_glyf;

    private HeadWriter m_head;

    private int m_lineGap = 0;

    private int m_maxAdvanceWidth = 0;

    private int m_minRightSideBearing = 0;

    public HheaWriter(GlyfWriter a_glyf, HeadWriter a_head) {
        super();

        m_glyf = a_glyf;
        m_head = a_head;
    }

    public void setLineGap(int a_value) {
        m_lineGap = a_value;
    }

    public void setMaxAdvanceWidth(int a_value) {
        m_maxAdvanceWidth = a_value;
    }

    public void setMinRightSideBearing(int a_value) {
        m_minRightSideBearing = a_value;
    }

    public void write() throws IOException {
        // table version number
        writeFixed32(1.0);

        writeFWord(m_head.getMax().y);
        writeFWord(m_head.getMin().y);
        writeFWord(m_lineGap);
        writeUFWord(m_maxAdvanceWidth);

        int minLeftSideBearing = m_head.getMin().x;
        writeFWord(minLeftSideBearing);
        writeFWord(m_minRightSideBearing);

        int xMaxExtent = m_head.getMax().x - m_head.getMin().x;
        writeFWord(xMaxExtent);

        // caratSlopeRise
        writeInt16(1);
        writeInt16(0);

        // reserved
        for (int i = 0; i < 5; i++) {
            writeInt16(0);
        }

        writeInt16(0);

        int numOfHMetrics = m_glyf.numOfGlyph();
        writeUInt16(numOfHMetrics);

        pad();
    }

    protected String getTag() {
        return "hhea";
    }
}
