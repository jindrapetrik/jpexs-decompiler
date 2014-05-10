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

import java.util.*;
import java.awt.geom.*;

import org.doubletype.ossa.xml.*;

/**
 * @author e.e
 */
public class CubicSegment {
    public static final int LINE = 0;
    public static final int CURVE = 1;
    
    private static final double k_tolerance = 5;
    private static final double k_minTolerance = 10;
    private static final int k_maxTrial = 3;
    
    /**
     * converts cubic contour into cubic segments.
     * @param a_contour
     * @return
     */
    public static ArrayList<CubicSegment> toSegments(EContour a_contour) {
        ArrayList<CubicSegment> retval = new ArrayList<>();
        XContourPoint [] points = a_contour.getContourPoint();
		if (points.length < 2) {
			return retval;
		} // if
		
		EContourPoint startPoint = (EContourPoint) points[points.length - 1];
		for (int i = 0; i < points.length; i++) {
		    EContourPoint endPoint = (EContourPoint) points[i];
		    retval.add(new CubicSegment(startPoint, endPoint));		    
	        startPoint = endPoint;
		} // for
		
        return retval;
    }
    
    /**
     * converts cubic segments into cubic contour.
     * @param a_segments
     * @return
     */
    public static EContour toContour(ArrayList a_segments) {
	    EContour retval = new EContour();
	    retval.setType(EContour.k_cubic);
        
	    EControlPoint controlPoint1 = null;
        for (int i = 0; i < a_segments.size(); i++) {
            CubicSegment segment = (CubicSegment) a_segments.get(i);
            EContourPoint startPoint = (EContourPoint) segment.m_startPoint.clone();
            
            if (controlPoint1 != null) {
                startPoint.setControlPoint1(controlPoint1);
                controlPoint1 = null;
            } // if
            
            if (segment.m_controlPoint1 != null) {
                EControlPoint controlPoint2 = new EControlPoint(false,
                        segment.m_controlPoint1.getX(),
                        segment.m_controlPoint1.getY());
                startPoint.setControlPoint2(controlPoint2);
            } // if
            
            retval.addContourPoint(startPoint);
            
            if (segment.m_controlPoint2 != null) {
                controlPoint1 = new EControlPoint(true, 
                        segment.m_controlPoint2.getX(),
                        segment.m_controlPoint2.getY());
            }
        } // for i
	    
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
        } // if
        
        if (a_startPoint.hasControlPoint2() || a_endPoint.hasControlPoint1()) {
            m_type = CURVE;
            
            if (a_startPoint.hasControlPoint2()) {
                m_controlPoint1 = (EContourPoint) a_startPoint.getControlPoint2().getContourPoint();
            } else {
                m_controlPoint1 = a_startPoint;
            } // if-else
            
            if (a_endPoint.hasControlPoint1()) {
                m_controlPoint2 = (EContourPoint) a_endPoint.getControlPoint1().getContourPoint();
            } else {
                m_controlPoint2 = a_endPoint;
            } // if-else 
        } // if
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
        } // if
        
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
              
        a_trial++;
        
        double[][] quadCoords =approximateCubic(new double[]{m_startPoint.getX(),m_startPoint.getY(),m_controlPoint1.getX(),m_controlPoint1.getY(),m_controlPoint2.getX(),m_controlPoint2.getY(),m_endPoint.getX(),m_endPoint.getY()});
        EContourPoint lastPoint=m_startPoint;
        for (int i = 0; i < quadCoords.length; i++) {            
                        retval.add(new QuadraticSegment(
                                lastPoint,
                                new EContourPoint(quadCoords[i][0], quadCoords[i][1], true),                                 
                                new EContourPoint(quadCoords[i][2], quadCoords[i][3], false)));
                        lastPoint =  new EContourPoint(quadCoords[i][2],quadCoords[i][3],true);
                    }
        /*EContourPoint intersection = calculateIntersection();
        
        double deltaX = 0.125 * (m_startPoint.getX() + m_endPoint.getX() + 4 * intersection.getX()
                		- 3 * (m_controlPoint1.getX() + m_controlPoint2.getX()));
        double deltaY = 0.125 * (m_startPoint.getY() + m_endPoint.getY() + 4 * intersection.getY()
        		- 3 * (m_controlPoint1.getY() + m_controlPoint2.getY()));
        double deltaSqr = (deltaX * deltaX + deltaY * deltaY);
        
        if (deltaSqr > (k_minTolerance * k_minTolerance)
             || ((a_trial < k_maxTrial) && (deltaSqr > (k_tolerance * k_tolerance)))) {
	            return toSplitQuadraticSegments(a_trial);
        } // if
        
        retval.add(new QuadraticSegment(m_startPoint, intersection, m_endPoint));*/
        return retval;
    }
    
    private ArrayList<QuadraticSegment> toSplitQuadraticSegments(int a_trial) {
        EContourPoint p01 = midPoint(m_startPoint, m_controlPoint1);
        EContourPoint p12 = midPoint(m_controlPoint1, m_controlPoint2);
        EContourPoint p23 = midPoint(m_controlPoint2, m_endPoint);
        
        EContourPoint pA = midPoint(p01, p12);
        EContourPoint pB = midPoint(p12, p23);
        
        EContourPoint pAB = midPoint(pA, pB);
        
        CubicSegment firstCubicSegment = new CubicSegment(m_startPoint, p01, pA, pAB);
        CubicSegment secondCubicSegment = new CubicSegment(pAB, pB, p23, m_endPoint);
        
        ArrayList<QuadraticSegment> firstHalf = firstCubicSegment.toQuadraticSegments(a_trial);
        ArrayList<QuadraticSegment> secondHalf = secondCubicSegment.toQuadraticSegments(a_trial);
        
        firstHalf.addAll(secondHalf);
        
        return firstHalf;
    }
    
    private EContourPoint midPoint(EContourPoint a_start, EContourPoint a_end) {
        Point2D p = calculateMidPoint(a_start, a_end);
        EContourPoint retval =  new EContourPoint(p.getX(), p.getY(), true);
        
        ArrayList ppems = getPpems(a_start, a_end);
        EPoint startPoint, endPoint;
        for (int i = 0; i < ppems.size(); i++) {
            Long ppemObject = (Long) ppems.get(i);
            long ppem = ppemObject.longValue();
            
            if (retval.hasHintForPpem(ppem)) {
                continue;
            } // if
            
            startPoint = a_start.getHintIfPossible(ppem);
            endPoint = a_end.getHintIfPossible(ppem);
            
            p = calculateMidPoint(startPoint, endPoint);
            EHint hint = new EHint(p.getX(), p.getY(), ppem);
            retval.addHint(hint);
        } // for i
        
        return retval;
    }
    
    private Point2D calculateMidPoint(EPoint a_p1, EPoint a_p2) {
        Point2D retval = new Point2D.Double((a_p1.getX() + a_p2.getX()) / 2.0, 
                (a_p1.getY() + a_p2.getY()) / 2.0);
        return retval;
    }
    
    /**
     * calculate intersection of line (a_start, a_controlPoint1) and 
     * line (a_end, a_controlPoint2) 
     * @return
     */
    private EContourPoint calculateIntersection() {
        EContourPoint retval = new EContourPoint(m_startPoint.getX(), m_startPoint.getY(), false);
        Point2D intersection = calculateIntersection(m_startPoint, m_controlPoint1, m_controlPoint2, m_endPoint);
        retval.getPoint2d().setX(intersection.getX());
        retval.getPoint2d().setY(intersection.getY());
        
        EPoint startPoint, controlPoint1, controlPoint2, endPoint;
        ArrayList ppems = getPpems(m_startPoint, m_controlPoint1, m_controlPoint2, m_endPoint);
        for (int i = 0; i < ppems.size(); i++) {
            Long ppemObject = (Long) ppems.get(i);
            long ppem = ppemObject.longValue();
            
            if (retval.hasHintForPpem(ppem)) {
                continue;
            } // if
            
            startPoint = m_startPoint.getHintIfPossible(ppem);
            controlPoint1 = m_controlPoint1.getHintIfPossible(ppem);
            controlPoint2 = m_controlPoint2.getHintIfPossible(ppem);
            endPoint = m_endPoint.getHintIfPossible(ppem);
            
            Point2D p = calculateIntersection(startPoint, controlPoint1, controlPoint2, endPoint);
            EHint hint = new EHint(p.getX(), p.getY(), ppem);
            retval.addHint(hint);
        } // for i
        
        return retval;
    }
    
    private Point2D calculateIntersection(EPoint a_p1, EPoint a_p2, EPoint a_p3, EPoint a_p4) {
        Point2D retval = new Point2D.Double(a_p1.getX(), a_p1.getY());
        
        double deltaX1 = a_p2.getX() - a_p1.getX();
        double deltaX2 = a_p3.getX() - a_p4.getX();
        if (deltaX1 == 0) {
            deltaX1 = 0.1;
        } // if
        if (deltaX2 == 0) {
            deltaX2 = 0.2;
        } // if
        
        double incline1 = (a_p2.getY() - a_p1.getY()) / deltaX1;
        double incline2 = (a_p3.getY() - a_p4.getY()) / deltaX2;
        if (incline1 == incline2) {
            return retval;
        } // if
        
        double x = (-incline2 * a_p4.getX() + a_p4.getY()
            	+incline1 * a_p1.getX() - a_p1.getY()) / (incline1 - incline2);
        double y = incline1 * (x - a_p1.getX()) + a_p1.getY();
        
        retval.setLocation(x, y);
        return retval;
    }
    
    private ArrayList<Long> getPpems(EContourPoint a_start, EContourPoint a_end) {
    	ArrayList<Long> retval = new ArrayList<>();
        collectPpemsFromHints(retval, a_start.getHint());
        collectPpemsFromHints(retval, a_end.getHint());        
        return retval;
    }
    
    private void collectPpemsFromHints(ArrayList<Long> a_ppems, XHint[] a_hints) {
        for (XHint hint: a_hints) {
            if (!a_ppems.contains(hint.getPpem())) {
            	a_ppems.add(hint.getPpem());
            } // if
        } // for i
    }
    
    private ArrayList<Long> getPpems(EContourPoint a_start, EContourPoint a_controlPoint1,
            EContourPoint a_controlPoint2, EContourPoint a_end) {
        ArrayList<Long> retval = new ArrayList<>();
        collectPpemsFromHints(retval, a_start.getHint()); 
        collectPpemsFromHints(retval, a_controlPoint1.getHint()); 
        collectPpemsFromHints(retval, a_controlPoint2.getHint());
        collectPpemsFromHints(retval, a_end.getHint());
        return retval;
    }
    
}
