/*
 * $Copyright: copyright (c) 2003-2008, e.e d3si9n $
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
import java.util.ArrayList;

/**
 * HtmxWriter depends on GlyfWriter.
 *
 * @author e.e
 */
public class HdmxWriter extends FontFormatWriter {

    static public int getNumOfPixelSizes() {
        return TTPixelSize.getList().size();
    }

    static public ArrayList<TTPixelSize> getPixelSizes() {
        return TTPixelSize.getList();
    }

    private int m_numGlyphs = 98; // set by GlyfWriter

    public HdmxWriter() {
        super();
    }

    /**
     * set the number of glyphs in the font
     */
    public void setNumGlyphs(int a_value) {
        m_numGlyphs = a_value;

        for (TTPixelSize pixelSize : getPixelSizes()) {
            pixelSize.setPixelWidthsSize(a_value);
        } // for pixelSize
    }

    public void updatePixelWidth(int a_glyphIndex, TTGlyph a_glyph) {
        double advanceWidth = a_glyph.getAdvanceWidth();
        double em = TTPixelSize.getEm();

        for (TTPixelSize pixelSize : TTPixelSize.getList()) {
            int width = (int) Math.round(((double) pixelSize.getPixel() * advanceWidth) / em);
            pixelSize.setPixelWidth(a_glyphIndex, width);
        } // pixelSize
    }

    /**
     * writes htmx record.
     * The size of a device record is calculated to align it to 32bit boundary.
     */
    public void write() throws IOException {
        int numOfPads = 4 - ((m_numGlyphs + 2) % 4);
        if (numOfPads == 4) {
            numOfPads = 0;
        }
        int size = m_numGlyphs + 2 + numOfPads; // 2 comes from the ppem and max

        // format version number
        writeInt16(0);

        // number of device records
        writeInt16(getNumOfPixelSizes());
		//System.out.printf("num of pixel sizes %d\n", getNumOfPixelSizes());
        // size of device record
        writeInt32(size);
        //System.out.printf("num of glyphs %d\n", m_numGlyphs);
        for (TTPixelSize pixelSize : getPixelSizes()) {
            writeUInt8(pixelSize.getPixel());
            writeUInt8(pixelSize.getMaxPixelWidth());
            for (int pixelWidth : pixelSize.getPixelWidths()) {
                writeUInt8(pixelWidth);
            } // for pixelWidth

            for (int j = 0; j < numOfPads; j++) {
                writeUInt8(0);
            } // for j
        } // for pixelSize

        pad();
    }

    protected String getTag() {
        return "hdmx";
    }
}
