/*
 * $Id: GlyfWriter.java,v 1.17 2004/09/23 07:47:39 eed3si9n Exp $
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
import java.util.ArrayList;

/**
 * @author e.e
 */
public class GlyfWriter extends FontFormatWriter {

    private ArrayList<TTGlyph> m_glyphs;

    private LocaWriter m_loca;

    private MaxpWriter m_maxp;

    private HeadWriter m_head;

    private HdmxWriter m_hdmx;

    public GlyfWriter(LocaWriter a_loca, MaxpWriter a_maxp,
            HeadWriter a_head, HdmxWriter a_hdmx) {
        super();

        m_loca = a_loca;
        m_maxp = a_maxp;
        m_head = a_head;
        m_hdmx = a_hdmx;
        m_glyphs = new ArrayList<>();
    }

    public void write() throws IOException {
        m_hdmx.setNumGlyphs(numOfGlyph());
        m_maxp.setNumGlyphs(numOfGlyph());
        m_loca.m_offsets.clear();

        for (int i = 0; i < m_glyphs.size(); i++) {
            TTGlyph glyph = m_glyphs.get(i);
            writeGlyph(glyph);
            m_hdmx.updatePixelWidth(i, glyph);
        }

        m_loca.m_offsets.add(size());
    }

    public int add(TTGlyph a_glyph) {
        m_head.updateMax(a_glyph.getMax());
        m_head.updateMin(a_glyph.getMin());

        m_glyphs.add(a_glyph);
        return m_glyphs.size() - 1;
    }

    public int numOfGlyph() {
        return m_glyphs.size();
    }

    public TTGlyph getGlyph(int a_index) {
        return m_glyphs.get(a_index);
    }

    private void writeGlyph(TTGlyph a_glyph) throws IOException {
        m_loca.m_offsets.add(size());

        if (a_glyph == null) {
            return;
        }

        if (a_glyph.isSimple()) {
            writeSimpleGlyph(a_glyph);
        } else {
            writeCompoundGlyph(a_glyph);
        }

        pad();
    }

    /**
     * @param a_glyph
     * @throws IOException
     */
    private void writeSimpleGlyph(TTGlyph a_glyph) throws IOException {
        if (a_glyph.getNumOfContours() == 0) {
            return;
        }

        m_maxp.updateNumOfContours(a_glyph.getNumOfContours());
        writeInt16(a_glyph.getNumOfContours());
        writeMinMax(a_glyph);

        int i;
        for (i = 0; i < a_glyph.getNumOfContours(); i++) {
            writeUInt16(a_glyph.getEndPoint(i));
        }

        int numOfInst = a_glyph.getNumOfInstructions();
        m_maxp.updateSizeOfInstructions(numOfInst);

        writeUInt16(numOfInst);
        for (i = 0; i < numOfInst; i++) {
            writeUInt8(a_glyph.getInstruction(i));
        }

        for (i = 0; i < a_glyph.getNumOfFlags(); i++) {
            int flag = a_glyph.getFlag(i);
            writeUInt8(flag);
        }

        // update num of points
        m_maxp.updateNumOfPoints(a_glyph.getNumOfPoints());

        int lastX = 0;
        for (i = 0; i < a_glyph.getNumOfPoints(); i++) {
            Point point = a_glyph.getPoint(i);

            writeInt16(point.x - lastX);
            lastX = point.x;
        }

        int lastY = 0;
        for (i = 0; i < a_glyph.getNumOfPoints(); i++) {
            Point point = a_glyph.getPoint(i);

            writeInt16(point.y - lastY);
            lastY = point.y;
        }
    }

    /**
     * @param a_glyph
     * @throws IOException
     */
    private void writeCompoundGlyph(TTGlyph a_glyph) throws IOException {
        int i;

        m_maxp.updateNumOfCompositePoints(a_glyph.getNumOfCompositePoints());
        m_maxp.updateNumOfCompositeContours(a_glyph.getNumOfCompositeContours());

        writeInt16(-1);
        writeMinMax(a_glyph);

        int numOfGlyphs = a_glyph.getNumOfFlags();
        m_maxp.updateNumOfComponentElements(numOfGlyphs);
        m_maxp.updateComponentDepth(a_glyph.getComponentDepth());

        for (i = 0; i < numOfGlyphs; i++) {
            writeUInt16(a_glyph.getFlag(i));
            writeUInt16(a_glyph.getGlyfIndex(i));
            writeInt16(a_glyph.getArg1(i));
            writeInt16(a_glyph.getArg2(i));
        }
    }

    /**
     * @param a_glyph
     * @throws IOException
     */
    private void writeMinMax(TTGlyph a_glyph) throws IOException {
        Point min = a_glyph.getMin();
        Point max = a_glyph.getMax();

        writeFWord(min.x);
        writeFWord(min.y);
        writeFWord(max.x);
        writeFWord(max.y);
    }

    protected String getTag() {
        return "glyf";
    }
}
