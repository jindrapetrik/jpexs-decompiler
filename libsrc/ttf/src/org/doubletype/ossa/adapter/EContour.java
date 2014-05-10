/*
 * $Id: EContour.java,v 1.11 2004/12/17 04:13:17 eed3si9n Exp $
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

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.*;
import org.doubletype.ossa.module.Rectangle;

/**
 * @author e.e
 */
public class EContour extends XContour implements GlyphObject, PointAggregate {
	public static final String k_quadratic = "quadratic";
	public static final String k_cubic = "cubic";
        public static final int k_defaultPixelSize = 16;
    
    public static EContour createAt(Point2D a_point) {
		EContour retval = new EContour();
		retval.setType(k_cubic);
		
		EContourPoint point;
		double x, y;
		
		x = a_point.getX();
		y = a_point.getY();
		point = new EContourPoint(x, y, true);
		retval.addContourPoint(point);
		
		
		x += Rectangle.k_defaultPen;
		point = new EContourPoint(x, y, true);
		retval.addContourPoint(point);
		
		y += Rectangle.k_defaultPen;
		point = new EContourPoint(x, y, true);
		retval.addContourPoint(point);
		
		x -= Rectangle.k_defaultPen;
		point = new EContourPoint(x, y, true);
		retval.addContourPoint(point);
		
		return retval;		
	}
	
	// --------------------------------------------------------------
	
	public EContour() {
	    initDefaults();
	}
	
	public EContour(RStack a_stack) {
	    super(a_stack);
		initDefaults();
	}
	
	/** set default values
	 */
	private void initDefaults() {
	    if (getType() == null) {
	        setType(k_quadratic);
	    } // if
	}
	
	public boolean isCubic() {
	    return getType().equals(k_cubic);
	}
	
	public EContour transpose(AffineTransform a_trans) {
		EContour retval = new EContour();
		retval.setType(this.getType());
		
		XContourPoint[] points = getContourPoint();
		int i;
		for (i = 0; i < points.length; i++) {
			EContourPoint point = (EContourPoint) points[i];			
			retval.addContourPoint(point.transpose(a_trans));
		} // if
		
		return retval;
	}
	
	// --------------------------------------------------------------
	
	public void display(Graphics2D g, AffineTransform a_trans) {
		g.setColor(GlyphColor.CONTOUR);
		
		if (isSelected()) {
			g.setColor(GlyphColor.SELECTED);	
		} // if
		
		int ppem =EContour.k_defaultPixelSize;
		g.draw(toShape(a_trans, ppem));
	}
	
	public boolean isSelected() {
		return EObject.getActives().isSelected(this);
	}
	
	public Shape toShape(AffineTransform a_trans, int a_ppem) {
		return transpose(a_trans).toShape(a_ppem);
	}
	
	private Shape toShape(int a_ppem) {
	    Emulator emulator = Emulator.getInstance();
	    
		return emulator.createShape(this, a_ppem);
	}
	
	public void move(Point2D a_delta) {		
		movePoints(getContourPoint(), a_delta);
	}
	
	private void movePoints(XContourPoint [] a_points, Point2D a_delta) {
		int i;
		for (i = 0; i < a_points.length; i++) {
			EContourPoint point = (EContourPoint) a_points[i];
			EObject.movePoint(point, a_delta);
		} // for i
	}
	
	public void remove() {		
		XBody parent = (XBody) rGetParentRNode();
		parent.removeContour(this);
	}
	
	// PointAggregate
	public void removePoint(EContourPoint a_point) {
		removeContourPoint(a_point);
		
		if (getContourPoint().length < 2) {
			remove();
		} // if
	}
	
	// PointAggregate
	public void movePoint(EContourPoint a_point, Point2D a_delta) {	    
	    EObject.movePoint(a_point, a_delta);
	}
	
	// PointAggregate
	public GlyphObject insertPoint(EContourPoint a_point) {
		int i;		
		int len = getContourPoint().length;
		
		for (i = 0; i < len; i++) {
			if (getContourPoint(i) == a_point) {
				break;
			} // if
		} // for
		
		EContourPoint nextPoint;
		nextPoint = (EContourPoint) getContourPoint((i + 1) % len);
			
		double x = (a_point.getX() + nextPoint.getX()) / 2;
		double y = (a_point.getY() + nextPoint.getY()) / 2;
		
		EContourPoint retval = new EContourPoint(x, y, true);
		addContourPoint(i + 1, retval);
		
		return retval;
	}
	
	public boolean hit(Rectangle2D a_rect, AffineTransform a_trans) {
		return EObject.hit(this, a_rect, a_trans);
	}
	
	public void convert() {
		XBody parent = (XBody) rGetParentRNode();
		
		if (isCubic()) {
		    parent.addContour(toQuadratic());  
		} else {
		    parent.addContour(toCubic());
		} // else
		
		remove();
	}
	
    public EContour toQuadratic() {
        if (!isCubic()) {
            return this;
        } // if
        
		ArrayList<QuadraticSegment> quadraticSegments = new ArrayList<>();
		for (CubicSegment segment: CubicSegment.toSegments(this)) {
		    quadraticSegments.addAll(segment.toQuadraticSegments());
		} // for i
		
        return QuadraticSegment.toContour(quadraticSegments);
    }
    
    public EContour toCubic() {
	    if (isCubic()) {
	        return this;
	    } // if
	    
	    ArrayList quadraticSegments = QuadraticSegment.toSegments(this);
	    ArrayList<CubicSegment> cubicSegments = new ArrayList<>();
        for (int i = 0; i < quadraticSegments.size(); i++) {
            QuadraticSegment quadraticSegment = (QuadraticSegment) quadraticSegments.get(i);
            cubicSegments.add(quadraticSegment.toCubicSegment());
        } // for i
        
        return CubicSegment.toContour(cubicSegments);
    }
}
