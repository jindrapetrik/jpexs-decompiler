/*
 * $Id: Emulator.java,v 1.6 2004/12/27 04:56:02 eed3si9n Exp $
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

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import org.doubletype.ossa.xml.*;

/** Emulator emulates font engine.
 * @author e.e
 */
public class Emulator {
    private static Emulator s_instance = null;
    
    // singleton pattern
    public static Emulator getInstance() {
        if (s_instance == null) {
            s_instance = new Emulator();
        } // if
        
        return s_instance;
    }
    
    private Emulator() {
        super();
    }

    public Shape createShape(EContour a_contour, int a_ppem) {
        if (a_contour.isCubic()) {
            // return createBezierCurve(a_contour);
            EContour contour = a_contour.toQuadratic();
            return createShape(contour, a_ppem);
        } // if
        
        return concretePointsToShape(createConcretePoints(
                createAdjustedPoints(a_contour, a_ppem)));
    }
        
    private Shape createBezierCurve(EContour a_contour) {
        GeneralPath retval = new GeneralPath();
        XContourPoint [] points = a_contour.getContourPoint();
        
		if (points.length < 2) {
			return retval;
		} // if
		
		EContourPoint startPoint = (EContourPoint) points[points.length - 1];
		retval.moveTo((float) startPoint.getX(), (float) startPoint.getY());
		
		for (int i = 0; i < points.length; i++) {
		    EContourPoint toPoint = (EContourPoint) points[i];
		    EControlPoint controlPoint1 = (EControlPoint) startPoint.getControlPoint2();
		    EControlPoint controlPoint2 = (EControlPoint) toPoint.getControlPoint1();
		    
		    // line segment
		    if ((controlPoint1 == null) && (controlPoint2 == null)) {
		        retval.lineTo((float) toPoint.getX(), (float) toPoint.getY());

		    } // if
		    
		    if ((controlPoint1 != null) && (controlPoint2 == null)) {
			    retval.curveTo((float) controlPoint1.getX(), (float) controlPoint1.getY(),
			            (float) toPoint.getX(), (float) toPoint.getY(),
			            (float) toPoint.getX(), (float) toPoint.getY());
		    } // if
		    
		    if ((controlPoint1 == null) && (controlPoint2 != null)) {
			    retval.curveTo((float) startPoint.getX(), (float) startPoint.getY(),
			            (float) controlPoint2.getX(), (float) controlPoint2.getY(),
			            (float) toPoint.getX(), (float) toPoint.getY());
		    } // if
		    
		    if ((controlPoint1 != null) && (controlPoint2 != null)) {
			    retval.curveTo((float) controlPoint1.getX(), (float) controlPoint1.getY(),
			            (float) controlPoint2.getX(), (float) controlPoint2.getY(),
			            (float) toPoint.getX(), (float) toPoint.getY());
		    } // if
		    	
	        startPoint = toPoint;
		} // for
        
        return retval;
    }
        
	private Shape concretePointsToShape(List a_listOfPoints) {
		GeneralPath retval = new GeneralPath();
		EContourPoint p;
		
		if (a_listOfPoints.size() < 3)
			return retval;
			
		p = (EContourPoint) a_listOfPoints.get(0);
		retval.moveTo((float) p.getX(), (float) p.getY());
		
		int i = 0;
		while (i < a_listOfPoints.size() - 1) {
			i++;
			EContourPoint nextPoint = (EContourPoint) a_listOfPoints.get(i);
			
			if (nextPoint.isOn()) {
				retval.lineTo((float) nextPoint.getX(), (float) nextPoint.getY());
			} else {
				EContourPoint controlPoint = nextPoint;
				i++;
				EContourPoint toPoint = (EContourPoint) a_listOfPoints.get(i);
				
				retval.quadTo((float) controlPoint.getX(), 
					(float) controlPoint.getY(),
					(float) toPoint.getX(), (float) toPoint.getY());
			} // if-else
		} // for
		
		return retval;
	}
    
	private List<EContourPoint> createConcretePoints(List a_points) {
		List<EContourPoint> retval = new ArrayList<>();
		
		if (a_points.size() < 3)
			return retval;
		
		EContourPoint lastPoint = (EContourPoint) a_points.get(a_points.size() - 1);
		for (int i = 0; i < a_points.size(); i++) {
			EContourPoint point = (EContourPoint) a_points.get(i);
			
			if (!point.isOn() && !lastPoint.isOn()) {
				double xMidpoint = (point.getX() + lastPoint.getX()) / 2;
				double yMidpoint = (point.getY() + lastPoint.getY()) / 2;
				retval.add(new EContourPoint(xMidpoint, yMidpoint, true));
			} // if
			
			retval.add(new EContourPoint(point.getX(), point.getY(), point.isOn()));
			lastPoint = point;
		} // for i
		
		// all contours should start with on-curve point
		// move off-curve point to the end if a contour starts with one.
		EContourPoint point = (EContourPoint) retval.get(0);
		if (!point.isOn()) {
			retval.remove(0);
			retval.add(point);
		} // if
		
		point = (EContourPoint) retval.get(0);
		retval.add(point);
		
		return retval;
	}
    
    private List<EContourPoint> createAdjustedPoints(EContour a_contour, int a_ppem) {
        List<EContourPoint> retval = new ArrayList<>();
        
	    XContourPoint [] points = a_contour.getContourPoint();
	    if (points.length < 3) {
	        return retval;
	    } // if
	    
	    boolean isRounded = false;
	    for (int i = 0; i < points.length; i++) {
	        EContourPoint point = (EContourPoint) points[i];
	        point.resetAdjusted();
	        
	        if (point.isRounded()) {
	            isRounded = true;
	            point.roundAdjusted(a_ppem);
	        } // if
	        
	        retval.add(point);
	    } // for i
	    
	    if (isRounded) {
	        interpolate(retval, true);
	        interpolate(retval, false);
	    } // if
	    
	    for (int i = 0; i < retval.size(); i++) {
	        EContourPoint point = (EContourPoint) retval.get(i);
	        if (point.hasHintForPpem(a_ppem)) {
	            point.hintAdjusted(a_ppem);
	        } // if
	    } // for i
	    
	    return promoteAdjustedToMain(retval);  
    }
    
	private List<EContourPoint> promoteAdjustedToMain(List<EContourPoint> a_points) {
	    List<EContourPoint> retval = new ArrayList<>();
	    for (int i = 0; i < a_points.size(); i++) {
	        EContourPoint original = (EContourPoint) a_points.get(i);
	        EContourPoint adjusted = new EContourPoint(original.getAdjusted().getX(),
		        	original.getAdjusted().getY(),
		        	original.isOn());
		        
		    retval.add(adjusted);
	    } // for i
 	    
	    return retval;
	}
		
	private void interpolate(List<EContourPoint> a_points, boolean a_isX) {
	    Collection<EContourPoint> sorted = sortPoints(a_points, a_isX);
	    
	    Iterator itr = sorted.iterator();
	    
	    EContourPoint first = (EContourPoint) itr.next(); 
	    if (!first.isRounded()) {
	        shiftPoint(first, a_points, a_isX);
	    } // if
	    
	    EContourPoint second = (EContourPoint) itr.next();
	    EContourPoint third = null;
	    while (itr.hasNext()) {
	        third = (EContourPoint) itr.next();
	        
	        if (!second.isRounded()) {
		        if (first.isRounded()
		                && third.isRounded()) {        
		            interpolate(first, second, third, a_isX);
		        } else {
		            shiftPoint(second, a_points, a_isX);
		        } // if-else
	        } // if
	        
	        first = second;
	        second = third;
	    } // while
	    
	    EContourPoint last = third;
	    if (!last.isRounded()) {
	        shiftPoint(last, a_points, a_isX);
	    } // if
	}
	
	private Collection<EContourPoint> sortPoints(List<EContourPoint> a_points, boolean a_isX) {
	    Map<Double,EContourPoint> sorted = new TreeMap<>();
	    
	    for (int i = 0; i < a_points.size(); i++) {
	        EContourPoint original = (EContourPoint) a_points.get(i);
	        Double value = new Double(getPointValue(original, a_isX));
	        
	        while (sorted.containsKey(value)) {
	            value = new Double(value.doubleValue() + 0.01);
	        } // while 
	        sorted.put(value, original);
	    } // for i 
	    
	    return sorted.values();
	}
	
	private void interpolate(EContourPoint a_first, EContourPoint a_second, EContourPoint a_third, boolean a_isX) {
	    double firstValue = getPointValue(a_first, a_isX);
	    double secondValue = getPointValue(a_second, a_isX);
	    double thirdValue = getPointValue(a_third, a_isX);
	    double adjustedFirst = getAdjustedValue(a_first, a_isX);
	    double adjustedThird = getAdjustedValue(a_third, a_isX);
	    
	    double ratio = (secondValue - firstValue) / (thirdValue - firstValue);
	    
	    secondValue = adjustedFirst + ratio * (adjustedThird - adjustedFirst);
	    setAdjusted(a_second, secondValue, a_isX);
	}
	
	private void shiftPoint(EContourPoint a_point, List<EContourPoint> a_points, boolean a_isX) {
	    EContourPoint closest = closestTouchedPoint(a_point, a_points);
	    if (closest == null) {
	        return;
	    } // if
	    
	    double delta = getAdjustedValue(closest, a_isX) - getPointValue(closest, a_isX);
	    double value = getPointValue(a_point, a_isX) + delta;
	    setAdjusted(a_point, value, a_isX);
	}
	
	private void setAdjusted(EContourPoint a_point, double a_value, boolean a_isX) {
	    double x = a_point.getX();
	    double y = a_point.getY();
	    
	    if (a_isX) {
	        x = a_value;
	    } else {
	        y = a_value;
	    } // if-else
	    
	    a_point.setAdjusted(x, y);
	}
	
	private double getPointValue(EContourPoint a_point, boolean a_isX) {
	    if (a_isX) {
	        return a_point.getX();
	    } else {
	        return a_point.getY();
	    } // if-else
	}
	
	private double getAdjustedValue(EContourPoint a_point, boolean a_isX) {
	    if (a_isX) {
	        return a_point.getAdjusted().getX();
	    } else {
	        return a_point.getAdjusted().getY();
	    } // if-else
	}
	
	private EContourPoint closestTouchedPoint(EContourPoint a_point, List<EContourPoint> a_points) {
	    EContourPoint retval = null;
	    double min = Double.MAX_VALUE;
	    for (int i = 0; i < a_points.size(); i++) {
	        EContourPoint target = (EContourPoint) a_points.get(i);
	        if (target == a_point) {
	            continue;
	        } // if
	        
	        if (!target.isRounded()) {
	            continue;
	        } // if
	        
	        double d = a_point.toPoint2D().distance(target.toPoint2D());
	        if (d < min) {
	            min = d;
	            retval = target;
	        } // if

	    } // for i
	    
	    return retval;
	}
}
