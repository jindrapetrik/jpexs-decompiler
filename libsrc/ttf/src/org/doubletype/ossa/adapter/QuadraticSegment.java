/*
 * $Id: QuadraticSegment.java,v 1.1 2004/12/15 11:54:18 eed3si9n Exp $
 *
 * $Copyright: copyright (c) 2004, e.e d3si9n $
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
package org.doubletype.ossa.adapter;

import java.util.ArrayList;

/**
 * @author e.e
 */
public class QuadraticSegment {

    public static final int LINE = 0;

    public static final int CURVE = 1;

    /**
     * converts quadratic contour into a list of quadratic segments.
     *
     * @param a_contour
     * @return
     */
    public static ArrayList<QuadraticSegment> toSegments(EContour a_contour) {
        ArrayList<QuadraticSegment> retval = new ArrayList<>();

        ArrayList points = toConcreatePoints(a_contour);
        if (points.size() < 3) {
            return retval;
        }

        for (int i = 0; i < points.size() - 1; i++) {
            EContourPoint startPoint = (EContourPoint) points.get(i);
            EContourPoint nextPoint = (EContourPoint) points.get(i + 1);

            // if this is a line segment
            if (nextPoint.isOn()) {
                retval.add(new QuadraticSegment(startPoint, null, nextPoint));
            } else {
                EContourPoint offCurvePoint = nextPoint;
                i++;
                nextPoint = (EContourPoint) points.get(i + 1);
                retval.add(new QuadraticSegment(startPoint, offCurvePoint, nextPoint));
            }
        }

        return retval;
    }

    /**
     * converts a list of quadratic segments into a quadratic contour.
     *
     * @param a_segments
     * @return
     */
    public static EContour toContour(ArrayList<QuadraticSegment> a_segments) {
        EContour retval = new EContour();
        retval.setType(EContour.k_quadratic);

        for (QuadraticSegment segment : a_segments) {
            EContourPoint p = segment.m_startPoint;
            EContourPoint startPoint = new EContourPoint(p.getX(), p.getY(), p.isOn());
            startPoint.setControlPoint1(null);
            startPoint.setControlPoint2(null);

            if (segment.m_type == LINE) {
                retval.addContourPoint(startPoint);

            } else {
                retval.addContourPoint(startPoint);
                retval.addContourPoint(segment.m_offCurvePoint);
            }
        }

        return retval;
    }

    /**
     * converts quadratic contour to concreate points by inserting
     * on-curve point between off-curve points.
     *
     * @param a_contour
     * @return
     */
    private static ArrayList<EContourPoint> toConcreatePoints(EContour a_contour) {
        ArrayList<EContourPoint> retval = new ArrayList<>();

        ArrayList<EContourPoint> points = a_contour.getContourPoints();
        if (points.size() < 3) {
            return retval;
        }

        EContourPoint fromPoint = (EContourPoint) points.get(points.size() - 1);
        for (EContourPoint point : points) {
            EContourPoint toPoint = (EContourPoint) point;
            if (!toPoint.isOn() && !fromPoint.isOn()) {
                double xMidpoint = (toPoint.getX() + fromPoint.getX()) / 2;
                double yMidpoint = (toPoint.getY() + fromPoint.getY()) / 2;
                retval.add(new EContourPoint(xMidpoint, yMidpoint, true));
            }

            retval.add(toPoint);
            fromPoint = toPoint;
        }

        // all contours should start with on-curve point
        // move off-curve point to the end if a contour starts with one.
        EContourPoint firstPoint = (EContourPoint) retval.get(0);
        if (!firstPoint.isOn()) {
            retval.remove(0);
            retval.add(firstPoint);
        }

        retval.add(retval.get(0));

        return retval;
    }

    private EContourPoint m_startPoint;

    private EContourPoint m_offCurvePoint;

    private EContourPoint m_endPoint;

    private int m_type = LINE;

    /**
     *
     */
    public QuadraticSegment(EContourPoint a_startPoint, EContourPoint a_offCurvePoint,
            EContourPoint a_endPoint) {
        m_startPoint = a_startPoint;
        m_offCurvePoint = a_offCurvePoint;
        m_endPoint = a_endPoint;

        if (m_offCurvePoint != null) {
            m_type = CURVE;
        }
    }

    /**
     * Convert quadratic segment to cubic.
     *
     * @param a_segment
     * @return
     */
    public CubicSegment toCubicSegment() {
        // if the segment is a line
        if (m_type == LINE) {
            return new CubicSegment(m_startPoint, m_endPoint);
        }

        double x, y;
        x = m_startPoint.getX() + 2.0 / 3.0 * (m_offCurvePoint.getX() - m_startPoint.getX());
        y = m_startPoint.getY() + 2.0 / 3.0 * (m_offCurvePoint.getY() - m_startPoint.getY());
        EContourPoint controlPoint1 = new EContourPoint(x, y, false);

        x = m_offCurvePoint.getX() + 1.0 / 3.0 * (m_endPoint.getX() - m_offCurvePoint.getX());
        y = m_offCurvePoint.getY() + 1.0 / 3.0 * (m_endPoint.getY() - m_offCurvePoint.getY());
        EContourPoint controlPoint2 = new EContourPoint(x, y, false);

        return new CubicSegment(m_startPoint, controlPoint1, controlPoint2, m_endPoint);
    }
}
