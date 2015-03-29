/*
 * $Id: HmtxWriter.java,v 1.10 2004/10/04 02:25:39 eed3si9n Exp $
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
 * HtmxWriter depends on GlyfWriter.
 *
 * @author e.e
 */
public class HmtxWriter extends FontFormatWriter {

    HheaWriter m_hhea;

    GlyfWriter m_glyf;

    public HmtxWriter(GlyfWriter a_glyf, HheaWriter a_hhea) {
        super();

        m_hhea = a_hhea;
        m_glyf = a_glyf;
    }

    public void write() throws IOException {
        int i;

        TTGlyph glyphZero = m_glyf.getGlyph(0);
        int maxWidth = glyphZero.getAdvanceWidth();
        int minRightSideBearing = glyphZero.getRightSideBearing();
        for (i = 0; i < m_glyf.numOfGlyph(); i++) {
            TTGlyph glyph = m_glyf.getGlyph(i);

            if (glyph.getAdvanceWidth() > maxWidth) {
                maxWidth = glyph.getAdvanceWidth();
            }

            if (glyph.getRightSideBearing() < minRightSideBearing) {
                minRightSideBearing = glyph.getRightSideBearing();
            }

            writeUFWord(glyph.getAdvanceWidth());
            writeFWord(glyph.getLeftSideBearing());
        }

        writeFWord(0);

        m_hhea.setMaxAdvanceWidth(maxWidth);
        m_hhea.setMinRightSideBearing(minRightSideBearing);

        pad();
    }

    protected String getTag() {
        return "hmtx";
    }
}
