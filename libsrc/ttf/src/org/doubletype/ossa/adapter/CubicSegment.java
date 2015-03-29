/*
 * $Id: CubicSegment.java,v 1.2 2004/12/27 04:56:02 eed3si9n Exp $
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
package org.doubletype.ossa.adapter;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * @author e.e
 */
public class CubicSegment {

    public static final int LINE = 0;

    public static final int CURVE = 1;

    /**
     * converts cubic contour into cubic segments.
     *
     * @param a_contour
     * @return
     */
    public static ArrayList<CubicSegment> toSegments(EContour a_contour) {
        ArrayList<CubicSegment> retval = new ArrayList<>();
        ArrayList<EContourPoint> points = a_contour.getContourPoints();
        if (points.size() < 2) {
            return retval;
        }

        EContourPoint startPoint = (EContourPoint) points.get(points.size() - 1);
        for (int i = 0; i < points.size(); i++) {
            EContourPoint endPoint = (EContourPoint) points.get(i);
            retval.add(new CubicSegment(startPoint, endPoint));
            startPoint = endPoint;
        } // for

        return retval;
    }

    private EContourPoint m_startPoint = null;

    private EContourPoint m_controlPoint1 = null;

    private EContourPoint m_controlPoint2 = null;

    private EContourPoint m_endPoint = null;

    private int m_type = LINE;

    public CubicSegment(EContourPoint a_startPoint, EContourPoint a_endPoint) {
        m_startPoint = a_startPoint;
        m_endPoint = a_endPoint;

        if ((!a_startPoint.hasControlPoint2()) && (!a_endPoint.hasControlPoint1())) {
            m_type = LINE;
            return;
        }

        if (a_startPoint.hasControlPoint2() || a_endPoint.hasControlPoint1()) {
            m_type = CURVE;

            if (a_startPoint.hasControlPoint2()) {
                m_controlPoint1 = a_startPoint.getControlPoint2().getContourPoint();
            } else {
                m_controlPoint1 = a_startPoint;
            }

            if (a_endPoint.hasControlPoint1()) {
                m_controlPoint2 = a_endPoint.getControlPoint1().getContourPoint();
            } else {
                m_controlPoint1 = a_endPoint;
            }
        }
    }

    public CubicSegment(EContourPoint a_startPoint, EContourPoint a_controlPoint1,
            EContourPoint a_controlPoint2, EContourPoint a_endPoint) {
        m_startPoint = a_startPoint;
        m_controlPoint1 = a_controlPoint1;
        m_controlPoint2 = a_controlPoint2;
        m_endPoint = a_endPoint;
        m_type = CURVE;
    }

    public ArrayList<QuadraticSegment> toQuadraticSegments() {
        ArrayList<QuadraticSegment> retval = new ArrayList<>();

        if (m_type == LINE) {
            retval.add(new QuadraticSegment(m_startPoint, null, m_endPoint));
            return retval;
        }

        return toQuadraticSegments(0);
    }

    //JPEXS start
    private static Point2D.Double movePoint(Point2D point, double dx, double dy) {
        return new Point2D.Double(point.getX() + dx, point.getY() + dy);
    }

    private static Point2D getMidPoint(Point2D p0, Point2D p1) {
        return getPointOnSegment(p0, p1, 0.5);
    }

    private static Point2D getPointOnSegment(Point2D p0, Point2D p1, double ratio) {
        double x = p0.getX() + ((p1.getX() - p0.getX()) * ratio);
        double y = p0.getY() + ((p1.getY() - p0.getY()) * ratio);
        return new Point2D.Double(x, y);
    }

    private static double[][] approximateCubic(double[] cubicControlPointCoords) {
        if (cubicControlPointCoords.length < 8) {
            throw new IllegalArgumentException("Must have at least 8 coordinates");
        }

        //extract point objects from source array
        Point2D p0 = new Point2D.Double(cubicControlPointCoords[0], cubicControlPointCoords[1]);
        Point2D p1 = new Point2D.Double(cubicControlPointCoords[2], cubicControlPointCoords[3]);
        Point2D p2 = new Point2D.Double(cubicControlPointCoords[4], cubicControlPointCoords[5]);
        Point2D p3 = new Point2D.Double(cubicControlPointCoords[6], cubicControlPointCoords[7]);

        //calculates the useful base points
        Point2D pa = getPointOnSegment(p0, p1, 3.0 / 4.0);
        Point2D pb = getPointOnSegment(p3, p2, 3.0 / 4.0);

        //get 1/16 of the [P3, P0] segment
        double dx = (p3.getX() - p0.getX()) / 16.0;
        double dy = (p3.getY() - p0.getY()) / 16.0;

        //calculates control point 1
        Point2D pc1 = getPointOnSegment(p0, p1, 3.0 / 8.0);

        //calculates control point 2
        Point2D pc2 = getPointOnSegment(pa, pb, 3.0 / 8.0);
        pc2 = movePoint(pc2, -dx, -dy);

        //calculates control point 3
        Point2D pc3 = getPointOnSegment(pb, pa, 3.0 / 8.0);
        pc3 = movePoint(pc3, dx, dy);

        //calculates control point 4
        Point2D pc4 = getPointOnSegment(p3, p2, 3.0 / 8.0);

        //calculates the 3 anchor points
        Point2D pa1 = getMidPoint(pc1, pc2);
        Point2D pa2 = getMidPoint(pa, pb);
        Point2D pa3 = getMidPoint(pc3, pc4);

        //return the points for the four quadratic curves
        return new double[][]{
            {pc1.getX(), pc1.getY(), pa1.getX(), pa1.getY()},
            {pc2.getX(), pc2.getY(), pa2.getX(), pa2.getY()},
            {pc3.getX(), pc3.getY(), pa3.getX(), pa3.getY()},
            {pc4.getX(), pc4.getY(), p3.getX(), p3.getY()}};
    }

    //JPEXS end
    private ArrayList<QuadraticSegment> toQuadraticSegments(int a_trial) {
        ArrayList<QuadraticSegment> retval = new ArrayList<>();

        double[][] quadCoords = approximateCubic(new double[]{m_startPoint.getX(), m_startPoint.getY(), m_controlPoint1.getX(), m_controlPoint1.getY(), m_controlPoint2.getX(), m_controlPoint2.getY(), m_endPoint.getX(), m_endPoint.getY()});
        EContourPoint lastPoint = m_startPoint;
        for (int i = 0; i < quadCoords.length; i++) {
            retval.add(new QuadraticSegment(
                    lastPoint,
                    new EContourPoint(quadCoords[i][0], quadCoords[i][1], true),
                    new EContourPoint(quadCoords[i][2], quadCoords[i][3], false)));
            lastPoint = new EContourPoint(quadCoords[i][2], quadCoords[i][3], true);
        }
        return retval;
    }
}
