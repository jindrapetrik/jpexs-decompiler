/*
 * $Id: Rectangle.java,v 1.16 2004/09/04 21:54:19 eed3si9n Exp $
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

package org.doubletype.ossa.module;

import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.adapter.*;

import java.awt.geom.*;

/**
 * @author e.e
 *
 */
public class Rectangle extends GlyphModule {
	public static final double k_defaultPen = 68.0;
	public static final double k_defaultWeight = 1.0;
	public static final String k_weight = "weight";
	
	public Rectangle() {
		super();
		
		addVar(k_weight, k_defaultWeight);
	}
	
	public EContour toContour(EModuleInvoke a_moduleInvoke) {
		int i;
		EContour retval = new EContour();
		XContourPoint[] points = a_moduleInvoke.getContourPoint();
		
		// can't make rectangle with one or zero point
		if (points.length < 2) {
			retval.addContourPoint(new EContourPoint(0, 0, true));
			return retval;
		} // if
		
		double pen = m_stack.get(k_weight) * k_defaultPen;		
		boolean steeps[] = new boolean[points.length - 1];
		double thetas[] = new double[points.length - 1];
		Point2D outStartPoints[] = new Point2D[points.length - 1];
		Point2D inStartPoints[] = new Point2D[points.length - 1];
		Point2D outPoints [] = new Point2D[points.length];
		Point2D inPoints[] = new Point2D[points.length];
		
		// treat each segment as a vector (start point and theta)
		for (i = 0; i < points.length - 1; i++) {
			EContourPoint start = (EContourPoint) points[i];
			EContourPoint end = (EContourPoint) points[i + 1];
			
			// calculate theta of the vector                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
			double theta;
			if (start.toPoint2D().distance(end.toPoint2D()) > 0) {
				theta = Math.atan2(end.getY() - start.getY(),
										end.getX() - start.getX());
			} else {
				theta = 0.0;
			} // if
			
			thetas[i] = theta;
			outStartPoints[i] = start.toPoint2D();	
			
			// turn the vector 90 degrees clockwise to create the 
			// starting point of the parallel vector (in-vector)
			inStartPoints[i] = new Point2D.Double(start.getX() + (pen * Math.cos(theta - Math.PI / 2)),
													start.getY() + (pen * Math.sin(theta - Math.PI / 2)) );
		} // for i
		
		for (i = 0; i < points.length; i++) {
			EContourPoint point = (EContourPoint) points[i];			
			
			if (i == 0) {
				// dynamically generate the point using vertical line thru p0 and the ideal line.
				double cuttingTheta;
				if (isSteep(thetas[i])) {
					cuttingTheta = 0.0;
				} else {
					cuttingTheta = Math.PI / 2;
				} // if-else
				
				outPoints[i] = getIntersection(point.toPoint2D(), cuttingTheta,
												outStartPoints[i], thetas[i]);
				inPoints[i] = getIntersection(point.toPoint2D(), cuttingTheta,
												inStartPoints[i], thetas[i]);
				continue;
			} else if (i == points.length - 1) {
				// vertial and ideal.
				double cuttingTheta;
				if (isSteep(thetas[i - 1])) {
					cuttingTheta = 0.0;
				} else {
					cuttingTheta = Math.PI / 2;
				} // if
					
				outPoints[i] = getIntersection(point.toPoint2D(), cuttingTheta,
												outStartPoints[i - 1], thetas[i - 1]);
				inPoints[i] = getIntersection(point.toPoint2D(), cuttingTheta,
												inStartPoints[i - 1], thetas[i - 1]);
				
				continue;
			} // if
			
			if (thetas[i - 1] != thetas[i]) {
				outPoints[i] = getIntersection(outStartPoints[i - 1], thetas[i - 1],
												outStartPoints[i], thetas[i]);
				inPoints[i] = getIntersection(inStartPoints[i - 1], thetas[i - 1],
												inStartPoints[i], thetas[i]);
			} else {
				outPoints[i] = outStartPoints[i];
				inPoints[i] = inStartPoints[i];
			} // if
		} // for i
		
		for (i = 0; i < points.length; i++) {
			EContourPoint point = (EContourPoint) points[i];
			
			retval.addContourPoint(i, point.cloneAt(outPoints[i]));	
			retval.addContourPoint(i + 1, point.cloneAt(inPoints[i]));
		} // if

		return retval;	
	}
	
	private boolean isSteep(double a_theta) {
		return (a_theta > (Math.PI / 4) && a_theta < (3 * Math.PI / 4))
						|| (a_theta < (-Math.PI / 4) && a_theta > (-3 * Math.PI / 4));
	}
	
	/**
	 * Intersection of two lines line0 and line1, defined by a point and slope.
	 * @param a_p0 a point line0 goes thru.
	 * @param a_theta0 the slope of line0, given in radian.  
	 * @param a_p1 a point line1 goes thru.
	 * @param a_theta1 the slope of line1, given in radian.
	 * @return the intersection of line0 and line1, if any; a_p1, otherwise.
	 */
	private Point2D getIntersection(Point2D a_p0, double a_theta0,
			Point2D a_p1, double a_theta1) {
		Point2D retval = new Point2D.Double();
		retval.setLocation(a_p1);
		
		if (a_theta0 == a_theta1)
			return retval;
			
		double cos0 = Math.cos(a_theta0);
		double sin0 = Math.sin(a_theta0);
		double cos1 = Math.cos(a_theta1);
		double sin1 = Math.sin(a_theta1);
		double deltaX = a_p1.getX() - a_p0.getX();
		double deltaY = a_p1.getY() - a_p0.getY();
		
		double r1 = (cos0 * deltaY - sin0 * deltaX)
					/ (sin0 * cos1 - cos0 * sin1);
		double x = a_p1.getX() + cos1 * r1;
		double y = a_p1.getY() + sin1 * r1;
		
		retval.setLocation(x, y);
		
		return retval;
	}	
}
