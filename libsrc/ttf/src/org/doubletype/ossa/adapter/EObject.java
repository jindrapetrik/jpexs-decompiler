/*
 * $Id: EObject.java,v 1.2 2004/11/08 06:29:51 eed3si9n Exp $
 * 
 * $Copyright: copyright (c) 2003-2004, e.e d3si9n $
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

import java.awt.geom.*;
import java.awt.*;

import org.doubletype.ossa.*;
import org.doubletype.ossa.truetype.TTPixelSize;
import org.doubletype.ossa.xml.*;

/** Decorator class of xml objects.
 * @author e.e
 */
public abstract class EObject {	
	public static final String k_on = "on";
	public static final String k_off = "off";

	private static ActiveList s_actives = null;
	
	
	static ActiveList getActives() {
		if (s_actives == null) {
			s_actives = ActiveList.getSingletonInstance();
		} // if
		
		return s_actives;
	}

	static Shape toShape(XPoint2d a_point, AffineTransform a_trans) {
            
                double ratio= EContour.k_defaultPixelSize/TTPixelSize.getEm();
            
		double x = a_point.getX();
		double y = a_point.getY();
		double e = 3 / ratio;
		Point2D source, dest;
		source = new Point2D.Double(x, y);
		dest = new Point2D.Double();
		dest = a_trans.transform(source, dest);
		x = dest.getX();
		y = dest.getY();
		
		return new Rectangle2D.Double(x - e, y - e, 2 * e + 1, 2 * e + 1);	
	}
	
	static void movePoint(XPoint2d a_point, Point2D a_delta) {
		double x = a_point.getX() + a_delta.getX();
		double y = a_point.getY() + a_delta.getY();

		a_point.setX(x);
		a_point.setY(y);
	}
	
	/**
	 * move the point and hint
	 * @param a_point
	 * @param a_delta
	 */
	public static void movePoint(EContourPoint a_point, Point2D a_delta) {		
		movePoint(a_point.getPoint2d(), a_delta);
		
		XHint [] hints = a_point.getHint();
		for (int i = 0; i < hints.length; i++) {
			XHint hint = hints[i];
			movePoint(hint.getPoint2d(), a_delta);
		} // for i
		
		if (a_point.hasControlPoint1()) {
		    EContourPoint point = (EContourPoint) a_point.getControlPoint1().getContourPoint();
		    movePoint(point, a_delta);
		} // if
		if (a_point.hasControlPoint2()) {
		    EContourPoint point = (EContourPoint) a_point.getControlPoint2().getContourPoint();
		    movePoint(point, a_delta);
		} // if
	}

	public static boolean hit(GlyphObject a_object, Rectangle2D a_rect, AffineTransform a_trans) {
		int ppem = EContour.k_defaultPixelSize;
		Shape hitArea = a_object.toShape(a_trans, ppem);
		if (!hitArea.intersects(a_rect)) {
			return false;
		} // if
			
		EObject.getActives().addActive(a_object);
		return true;
	}
	
	public static XInvoke createInvoke(Point2D a_point) {
		XInvoke retval = new XInvoke();
		retval.setInvokePos(createInvokePos(a_point.getX(), a_point.getY()));
		return retval;
	}
	
	public static XPoint2d createTransform(XPoint2d a_point, AffineTransform a_trans) {
		XPoint2d retval = new XPoint2d();
			
		double x = a_point.getX();
		double y = a_point.getY();
		Point2D source = new Point2D.Double(x, y);
		Point2D dest = new Point2D.Double();
		a_trans.transform(source, dest);
			
		retval.setX(dest.getX());
		retval.setY(dest.getY());
			
		return retval;	
	}
	
	private static XInvokePos createInvokePos(double a_x, double a_y) {
		XInvokePos retval = new XInvokePos();
		retval.setPoint2d(createPoint2d(a_x, a_y));
		return retval;
	}
	
	
	private static XPoint2d createPoint2d(double a_x, double a_y) {
		XPoint2d retval = new XPoint2d();
		retval.setX(a_x);
		retval.setY(a_y);
		return retval;
	}
	
}
