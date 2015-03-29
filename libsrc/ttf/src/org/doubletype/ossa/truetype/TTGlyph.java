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

import java.awt.Point;
import java.util.ArrayList;

/**
 * @author e.e
 */
public class TTGlyph {

    // --------------------------------------------------------------
    public static final int k_onCurve = 0x1;

    public static final int ARG_1_AND_2_ARE_WORDS = 0x1;

    public static final int ARGS_ARE_XY_VALUES = 0x2;

    public static final int ROUND_XY_TO_GRID = 0x4;

    public static final int WE_HAVE_A_SCALE = 0x8;

    // 0x10 is obsolete
    public static final int MORE_COMPONENTS = 0x20;

    public static final int WE_HAVE_AN_X_AND_Y_SCALE = 0x40;

    public static final int WE_HAVE_A_TWO_BY_TWO = 0x80;

    public static final int WE_HAVE_INSTRUCTIONS = 0x100;

    public static final int USE_MY_METRICS = 0x200;

    public static final int OVERLAP_COMPOUND = 0x400;

    /**
     * Move Direct Absolute Point - do not round
     */
    public static final int MDAP0 = 0x2E;

    /**
     * Move Direct Absolute Point - round the value
     */
    public static final int MDAP1 = 0x2F;

    /**
     * Interpolate Untouched Points through the outline - y-direction
     */
    public static final int IUP0 = 0x30;

    /**
     * Interpolate Untouched Points through the outline - x-direction
     */
    public static final int IUP1 = 0x31;

    /**
     * push one byte
     */
    public static final int PUSHB000 = 0xB0;

    /**
     * push two bytes
     */
    public static final int PUSHB001 = 0xB1;

    /**
     * push three bytes
     */
    public static final int PUSHB010 = 0xB2;

    public static final int PUSHB011 = 0xB3;

    public static final int PUSHB100 = 0xB4;

    public static final int PUSHB101 = 0xB5;

    public static final int PUSHB110 = 0xB6;

    public static final int PUSHB111 = 0xB7;

    /**
     * set vector to y-axis.
     */
    public static final int SVTCA0 = 0x00;

    /**
     * set vector to x-axis.
     */
    public static final int SVTCA1 = 0x01;

    public static final int SPVFS = 0x0A; // set projection vector

    public static final int SFVFS = 0x0B; // set freedom vector

    public static final int SFVTPV = 0x0E;

    public static final int DELTAP1 = 0x5D;

    public static final int DELTAP2 = 0x71;

    public static final int DELTAP3 = 0x72;

    /**
     * set delta base
     */
    public static final int SDB = 0x5E;

    // --------------------------------------------------------------
    /**
     * converts double into F2Dot14, 16 bit fixed point format.
     *
     * @param a_value
     * @return
     */
    public static int toF2Dot14(double a_value) {
        int retval = 0;

        if (a_value >= 2.0 || a_value < -2.0) {
            throw new RuntimeException(Double.toString(a_value) + " out of range");
        }

        int mantissa = (int) Math.floor(a_value);
        int fraction = (int) Math.floor((a_value - mantissa) * 16384);

        int twoBitPart = mantissa;
        if (mantissa < 0) {
            twoBitPart = 4 + mantissa;
        }

        retval = (twoBitPart << 14) | fraction;

        return retval;
    }

    public static int toDeltaArg(int a_relativePpem, int a_step) {
        int retval = 0;

        if (a_step < -8 || a_step > 8 || a_step == 0) {
            throw new RuntimeException("Out of range");
        }

        int selector = 0;
        if (a_step > 0) {
            selector = a_step + 7;
        } else {
            selector = a_step + 8;
        }

        retval = (a_relativePpem << 4) | (selector);

        return retval;
    }

    // --------------------------------------------------------------
    private ArrayList<Point> m_points = new ArrayList<>();

    private ArrayList<Integer> m_endPtsOfContours = new ArrayList<>();

    private ArrayList<Integer> m_instructions = new ArrayList<>();

    private ArrayList<Integer> m_flags = new ArrayList<>();

    private ArrayList<Integer> m_glyfIndeces = new ArrayList<>();

    private ArrayList<Integer> m_arg1s = new ArrayList<>();

    private ArrayList<Integer> m_arg2s = new ArrayList<>();

    private boolean m_isSimple = true;

    private int m_advanceWidth = 512;

    private int m_numOfCompositePoints = 0;

    private int m_numOfCompositeContours = 0;

    private int m_componentDepth = 0;

    private Point m_min = null;

    private Point m_max = null;

    // --------------------------------------------------------------
    public TTGlyph() {
        init();
    }

    public void init() {
        m_isSimple = true;
        m_points.clear();
        m_endPtsOfContours.clear();
        m_instructions.clear();
        m_flags.clear();
        m_glyfIndeces.clear();
        m_arg1s.clear();
        m_arg2s.clear();
        m_advanceWidth = 512;
    }

    public void buildCompound() {
        init();

        m_isSimple = false;

        addFlag(ARG_1_AND_2_ARE_WORDS
                | ARGS_ARE_XY_VALUES
                | ROUND_XY_TO_GRID
                | MORE_COMPONENTS);
        addFlag(ARG_1_AND_2_ARE_WORDS
                | ARGS_ARE_XY_VALUES
                | ROUND_XY_TO_GRID);

        addGlyfIndex(3);
        addGlyfIndex(3);

        addArg1(0);
        addArg2(0);
        addArg1(0);
        addArg2(500);
    }

    /**
     * add the index of the last point in the contour
     *
     * @param a_index
     */
    public void addEndPoint(int a_value) {
        m_endPtsOfContours.add(a_value);
    }

    public int getNumOfContours() {
        if (isSimple()) {
            return m_endPtsOfContours.size();
        } else {
            return -1;
        }
    }

    public int getEndPoint(int a_index) {
        return m_endPtsOfContours.get(a_index);
    }

    public int getAdvanceWidth() {
        return m_advanceWidth;
    }

    public void setAdvanceWidth(int a_value) {
        m_advanceWidth = a_value;
    }

    public Point getMax() {
        if (m_max == null) {
            m_max = new Point(0, 0);
        }

        return m_max;
    }

    public Point getMin() {
        if (m_min == null) {
            m_min = new Point(0, 0);
        }

        return m_min;
    }

    public boolean isSimple() {
        return m_isSimple;
    }

    public void setSimple(boolean a_isSimple) {
        m_isSimple = a_isSimple;
    }

    public void addInstruction(int a_value) {
        m_instructions.add(a_value);
    }

    public int getInstruction(int a_index) {
        return m_instructions.get(a_index);
    }

    public int getNumOfInstructions() {
        return m_instructions.size();
    }

    public void addFlag(int a_value) {
        m_flags.add(a_value);
    }

    public int getFlag(int a_index) {
        return m_flags.get(a_index);
    }

    public int getNumOfFlags() {
        return m_flags.size();
    }

    public void addPoint(Point a_value) {
        m_points.add(a_value);
        updateMinMax(a_value);
    }

    private void updateMinMax(Point a_value) {
        if (m_max == null) {
            m_max = new Point(a_value);
        }

        if (m_min == null) {
            m_min = new Point(a_value);
        }

        if (a_value.x > m_max.x) {
            m_max.x = a_value.x;
        }

        if (a_value.x < m_min.x) {
            m_min.x = a_value.x;
        }

        if (a_value.y > m_max.y) {
            m_max.y = a_value.y;
        }

        if (a_value.y < m_min.y) {
            m_min.y = a_value.y;
        }
    }

    public Point getPoint(int a_index) {
        return m_points.get(a_index);
    }

    public int getNumOfPoints() {
        return m_points.size();
    }

    public int getLastIndex() {
        return m_points.size() - 1;
    }

    public void addGlyfIndex(int a_value) {
        m_glyfIndeces.add(a_value);
    }

    public int getGlyfIndex(int a_index) {
        return m_glyfIndeces.get(a_index);
    }

    public void addArg1(int a_value) {
        m_arg1s.add(a_value);
    }

    public int getArg1(int a_index) {
        return m_arg1s.get(a_index);
    }

    public void addArg2(int a_value) {
        m_arg2s.add(a_value);
    }

    public int getArg2(int a_index) {
        return m_arg2s.get(a_index);
    }

    public int getNumOfCompositePoints() {
        if (isSimple()) {
            return getNumOfPoints();
        } else {
            return m_numOfCompositePoints;
        }
    }

    public void setNumOfCompositePoints(int a_value) {
        m_numOfCompositePoints = a_value;
    }

    public int getNumOfCompositeContours() {
        if (isSimple()) {
            return getNumOfContours();
        } else {
            return m_numOfCompositeContours;
        }
    }

    public void setNumOfCompositeContours(int a_value) {
        m_numOfCompositeContours = a_value;
    }

    public int getComponentDepth() {
        if (isSimple()) {
            return 0;
        } else {
            return m_componentDepth;
        }
    }

    public void setComponentDepth(int a_value) {
        m_componentDepth = a_value;
    }

    public int getLeftSideBearing() {
        return getMin().x;
    }

    public int getRightSideBearing() {
        return getAdvanceWidth() - getMax().x;
    }
}
