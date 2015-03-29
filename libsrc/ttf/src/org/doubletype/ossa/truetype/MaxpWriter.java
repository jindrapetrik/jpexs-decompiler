/*
 * $Id: MaxpWriter.java,v 1.11 2004/06/27 07:26:46 eed3si9n Exp $
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
public class MaxpWriter extends FontFormatWriter {

    private int m_numGlyphs = 98; // set by GlyfWriter

    private int m_maxPoints = 0;

    private int m_maxContours = 0;

    private int m_maxCompositePoints = 0;

    private int m_maxCompositeContours = 0;

    private int m_maxZones = 2;

    private int m_maxTwilightPoints = 128;

    private int m_maxStorage = 64;

    private int m_maxFunctionDefs = 128;

    private int m_maxInstructionDefs = 128;

    private int m_maxStackElements = 128;

    private int m_maxSizeOfInstructions = 128;

    private int m_maxComponentElements = 128;

    private int m_maxComponentDepth = 0;

    public MaxpWriter() {
        super();
    }

    public void write() throws IOException {
        writeFixed32(1.0);
        writeUInt16(m_numGlyphs);
        writeUInt16(m_maxPoints);
        writeUInt16(m_maxContours);
        writeUInt16(m_maxCompositePoints);
        writeUInt16(m_maxCompositeContours);
        writeUInt16(m_maxZones);
        writeUInt16(m_maxTwilightPoints);
        writeUInt16(m_maxStorage);
        writeUInt16(m_maxFunctionDefs);
        writeUInt16(m_maxInstructionDefs);
        writeUInt16(m_maxStackElements);
        writeUInt16(m_maxSizeOfInstructions);
        writeUInt16(m_maxComponentElements);
        writeUInt16(m_maxComponentDepth);
        pad();
    }

    protected String getTag() {
        return "maxp";
    }

    /**
     * set the number of glyphs in the font
     */
    public void setNumGlyphs(int a_value) {
        m_numGlyphs = a_value;
    }

    /**
     * update points in non-compound glyph
     */
    public void updateNumOfPoints(int a_value) {
        if (a_value > m_maxPoints) {
            m_maxPoints = a_value;
        }
    }

    /**
     * update points in non-compound glyph
     */
    public void updateNumOfContours(int a_value) {
        if (a_value > m_maxContours) {
            m_maxContours = a_value;
        }
    }

    public void updateNumOfCompositePoints(int a_value) {
        if (a_value > m_maxCompositePoints) {
            m_maxCompositePoints = a_value;
        }
    }

    public void updateNumOfCompositeContours(int a_value) {
        if (a_value > m_maxCompositeContours) {
            m_maxCompositeContours = a_value;
        }
    }

    /**
     * update byte count for glyph instructions
     */
    public void updateSizeOfInstructions(int a_value) {
        if (a_value > m_maxSizeOfInstructions) {
            m_maxSizeOfInstructions = a_value;
        }
    }

    public void updateNumOfComponentElements(int a_value) {
        if (a_value > m_maxComponentElements) {
            m_maxComponentElements = a_value;
        }
    }

    public void updateComponentDepth(int a_value) {
        if (a_value > m_maxComponentDepth) {
            m_maxComponentDepth = a_value;
        }
    }
}
