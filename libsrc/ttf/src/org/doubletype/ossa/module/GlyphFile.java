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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
package org.doubletype.ossa.module;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.doubletype.ossa.adapter.EContour;
import org.doubletype.ossa.adapter.EContourPoint;
import org.doubletype.ossa.truetype.TTGlyph;
import org.doubletype.ossa.truetype.TTUnicodeRange;

/**
 * @author e.e
 */
public class GlyphFile {

    private long m_unicode;

    private String m_author;

    private String m_copyrightYear;

    private Date m_creationDate = new Date();

    private Date m_modificationDate = m_creationDate;

    private String m_license;

    private Integer m_advanceWidth = null;

    private List<EContour> m_contours = new ArrayList<>();

    // --------------------------------------------------------------
    protected long m_modifiedTime = 0;

    protected long m_savedTime = 0;

    private final int k_halfWidth = 512;

    private final int k_fullWidth = 1024;

    // --------------------------------------------------------------
    /**
     * creates new file
     */
    public GlyphFile(File a_dir, long a_unicode) {
        super();

        init();
        setUnicode(a_unicode);

        setAdvanceWidth(k_fullWidth);
    }

    /**
     * creates new file
     *
     * @param a_dir parent dir
     */
    public GlyphFile(File a_dir) {
        super();
        init();
    }

    private void init() {
        m_savedTime = m_modifiedTime;
    }

    /**
     * initialize .notdef
     */
    public void initNotDef(int a_advanceWidth) {
        setAdvanceWidth(a_advanceWidth);

        EContour contour = new EContour();
        contour.setType(EContour.k_cubic);
        contour.addContourPoint(new EContourPoint(0.0, 0.0, true));
        contour.addContourPoint(new EContourPoint(438.0, 0.0, true));
        contour.addContourPoint(new EContourPoint(438.0, 683.0, true));
        contour.addContourPoint(new EContourPoint(0.0, 683.0, true));
        addContour(contour);

        contour = new EContour();
        contour.setType(EContour.k_cubic);
        contour.addContourPoint(new EContourPoint(365.0, 73.0, true));
        contour.addContourPoint(new EContourPoint(73.0, 73.0, true));
        contour.addContourPoint(new EContourPoint(73.0, 610.0, true));
        contour.addContourPoint(new EContourPoint(365.0, 610.0, true));
        addContour(contour);
    }

    public void initNullGlyph() {
        setAdvanceWidth(0);
    }

    public void initSpace(int a_advanceWidth) {
        setAdvanceWidth(a_advanceWidth);
    }

    public void beforePush() {
    }

    // --------------------------------------------------------------
    public boolean hasUnsavedChange() {
        return (m_savedTime != m_modifiedTime);
    }

    public void setAuthor(String a_value) {
        m_author = a_value;
    }

    public String getAuthor() {
        return m_author;
    }

    public void setCopyrightYear(String a_value) {
        m_copyrightYear = a_value;
    }

    public String getCopyrightYear() {
        return m_copyrightYear;
    }

    public void setCreationDate(Date a_value) {
        m_creationDate = a_value;
    }

    public Date getCreationDate() {
        return m_creationDate;
    }

    public void setModificationDate(Date a_value) {
        m_modificationDate = a_value;
    }

    public Date getModificationDate() {
        return m_modificationDate;
    }

    public void setAdvanceWidth(int a_width) {
        m_advanceWidth = a_width;
    }

    public int getAdvanceWidth() {
        if (m_advanceWidth == null) {
            return k_halfWidth;
        }

        return m_advanceWidth;
    }

    // --------------------------------------------------------------
    public static final int k_defaultPixelSize = 16; //PPM

    // --------------------------------------------------------------
    /**
     * Generates array of XContour from local contours and modules.
     * Used for TTF building.
     */
    private EContour[] toContours() {
        EContour[] retval;
        ArrayList<EContour> list = new ArrayList<>();
        for (EContour contour : m_contours) {
            list.add(contour.toQuadratic());
        }

        if (list.size() == 0) {
            return null;
        }

        retval = new EContour[list.size()];
        for (int i = 0; i < list.size(); i++) {
            retval[i] = list.get(i);
        }

        return retval;
    }

    // --------------------------------------------------------------
    /**
     * add contour from clipboard or ContourAction
     *
     * @param a_contour
     */
    public void addContour(EContour a_contour) {
        m_contours.add(a_contour);
    }

    // --------------------------------------------------------------
    public long getUnicode() {
        return m_unicode;
    }

    protected void setUnicode(long a_unicode) {
        m_unicode = a_unicode;
    }

    public boolean isSimple() {
        return true;
    }

    public boolean isWhiteSpace() {
        long unicode = getUnicode();

        if (unicode == 0x0020
                || unicode == 0x00a0
                || unicode == 0x200b
                || unicode == 0x2060
                || unicode == 0x3000
                || unicode == 0xfeff) {
            return true;
        }

        return false;
    }

    public void setLicense(String a_value) {
        m_license = a_value;
    }

    public String getLicense() {
        return m_license;
    }

    public boolean isRequiredGlyph() {
        long unicode = getUnicode();

        return (unicode == TTUnicodeRange.k_notDef
                || unicode == TTUnicodeRange.k_null
                || unicode == TTUnicodeRange.k_cr
                || unicode == TTUnicodeRange.k_space);
    }

    public TTGlyph toSimpleGlyph() {
        // convert the file into array of contours
        EContour[] contours = toContours();
        if ((contours == null) && (!isRequiredGlyph())) {
            return null;
        }

        TTGlyph retval = new TTGlyph();
        retval.setSimple(true);
        retval.setAdvanceWidth(getAdvanceWidth());

        if (contours == null) {
            return retval;
        }

        ArrayList<EContourPoint> points = new ArrayList<>();
        for (int i = 0; i < contours.length; i++) {
            EContour contour = contours[i];
            ArrayList<EContourPoint> contourPoints = contour.getContourPoints();
            for (int j = 0; j < contourPoints.size(); j++) {
                points.add((EContourPoint) contourPoints.get(j));
            } // for j
            retval.addEndPoint(points.size() - 1);
        }

        for (EContourPoint point : points) {
            loadContourPoint(retval, point);
        } // for point

        return retval;
    }

    private void loadContourPoint(TTGlyph a_glyph, EContourPoint a_point) {
        double x = a_point.getX();
        double y = a_point.getY();
        Point p = new Point((int) x, (int) y);
        int flag = 0;
        if (a_point.isOn()) {
            flag = TTGlyph.k_onCurve;
        }

        a_glyph.addPoint(p);
        a_glyph.addFlag(flag);
    }
}
