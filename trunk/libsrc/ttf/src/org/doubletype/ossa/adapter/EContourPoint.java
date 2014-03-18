/*
 * $Id: EContourPoint.java,v 1.10 2004/12/27 04:56:02 eed3si9n Exp $
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

import org.doubletype.ossa.*;
import org.doubletype.ossa.xml.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * @author e.e
 */
public class EContourPoint extends XContourPoint implements GlyphObject, EPoint {	
	public static String k_on = "on";
	public static String k_off = "off";
	public static String k_true = "true";
	public static String k_false = "false";
	
	private int m_number = 0;
	
	/** offset of this from the global coord.
	 * For contours, it will be 0.
	 * For module invoke, it will be pos.x.
	 */
	private double m_xOffset = 0;
	private double m_yOffset = 0;
	
	private Point2D m_adjusted;
	
	public EContourPoint() {
	    super();
	}
	
	public EContourPoint(RStack a_stack) {
	    super(a_stack);
	    correctControlPoint();
	}
	
	public EContourPoint(double a_x, double a_y, boolean a_isOn) {
		setPoint2d(new XPoint2d());
		getPoint2d().setX(a_x);
		getPoint2d().setY(a_y);
		
		setOn(a_isOn);
	}
	
	/** Move the second control point if it was incorrectly set to the first one.
	 * 
	 */
	private void correctControlPoint() {
	    if (!hasControlPoint1()) {
	        return;
	    } // if
	    
	    EControlPoint controlPoint = (EControlPoint) getControlPoint1();
	    if (controlPoint.isFirst()) {
	        return;
	    } // if
	    
	    setControlPoint2(controlPoint);
	    setControlPoint1(null);
	}
	
	public EContourPoint transpose(AffineTransform a_trans) {
		EContourPoint retval = new EContourPoint();
		
		retval.setPoint2d(EObject.createTransform(getPoint2d(), a_trans));
		retval.setOn(isOn());
		retval.setRounded(isRounded());
		
		if (hasControlPoint1()) {
		    EControlPoint controlPoint = (EControlPoint) getControlPoint1();
		    retval.setControlPoint1(controlPoint.transpose(a_trans));
		} // if
		if (hasControlPoint2()) {
		    EControlPoint controlPoint = (EControlPoint) getControlPoint2();
		    retval.setControlPoint2(controlPoint.transpose(a_trans));
		} // if
		
		int i;
		XHint [] hints = getHint();
		for (i = 0; i < hints.length; i++) {
			EHint hint = (EHint) hints[i];
			retval.addHint(hint.createTransform(a_trans));
		} // for i
		
		return retval;
	}
	
	public void setNumber(int a_number) {
		m_number = a_number;
	}
	
	public void setOffset(double a_x, double a_y) { 
	    m_xOffset = a_x;
	    m_yOffset = a_y;
	}
	
	private AffineTransform buildOffsetTrans(AffineTransform a_trans) {
	    if (m_xOffset == 0 && m_yOffset == 0) {
	        return a_trans;
	    } // if 
	    
	    AffineTransform retval = (AffineTransform) a_trans.clone();
	    retval.translate(m_xOffset, m_yOffset);
	    
	    return retval;
	}
	public static final int k_defaultPixelSize = 16;

	public void display(Graphics2D g, AffineTransform a_trans) {		
	    AffineTransform trans = buildOffsetTrans(a_trans);
		
	    int ppem = k_defaultPixelSize;	
		Shape rect = toShape(trans, ppem);	
		g.setColor(GlyphColor.POINT);
		if (isRounded()) {
		    g.setColor(GlyphColor.GRAY);
		} // if
		
		if (isSelected()) {
			g.setColor(GlyphColor.SELECTED);
		} // if

		if (isOn())
			g.fill(rect);
		else
			g.draw(rect);
	}
	

	
	public boolean isSelected() {
		return EObject.getActives().isSelected(this);
	}
	
	public void resetAdjusted() {
	    setAdjusted(getX(), getY());
	}
	
	public void roundAdjusted(int a_ppem) {
	    if (!isRounded()) {
	        return;
	    } // if
	    
        setAdjusted(round(getX(), a_ppem),
            	round(getY(), a_ppem));
	}
	
	public void hintAdjusted(int a_ppem) {
	    if (!hasHintForPpem(a_ppem)) {
	        return;
	    } // if
	    
        EHint hint = getHintForPpem(a_ppem);
        setAdjusted(hint.getX(), hint.getY());
	}
	
	public void setAdjusted(double a_x, double a_y) {
	    m_adjusted = new Point2D.Double(a_x, a_y);
	}
	
	public Point2D getAdjusted() {
	    return m_adjusted;
	}
	
	public Shape toShape(AffineTransform a_trans, int a_ppem) {
		return EObject.toShape(getPoint2d(), a_trans);
	}
	
	public double getX() {
		return getPoint2d().getX();
	}
	
	public double getY() {
		return getPoint2d().getY();
	}
	
	public boolean isOn() {
		return getType().equals(k_on);
	}
	
	public boolean isRounded() {	    
	    return getRounded();
	}
	
	public void toggleRounded() {
	    if (hasHintForCurrentPpem()) {
	        return;
	    } // if
	    
	    setRounded(!isRounded());
	}
	
	public Point2D toPoint2D() {
		return new Point2D.Double(getX(), getY());
	}
	
	public boolean hasHint() {
	    return (getHint().length > 0);
	}
	
	public boolean hasHintForCurrentPpem() {
	    return hasHintForPpem(EContour.k_defaultPixelSize);
	}
	
	public boolean hasHintForPpem(long a_ppem) {		
		return getHintForPpem(a_ppem) != null;
	}
	
	public EPoint getHintIfPossible(long a_ppem) {
	    if (hasHintForPpem(a_ppem)) {
	        return getHintForPpem(a_ppem);
	    } // if
	    
	    return this;
	}
	
	public EHint getCurrentHint() {
	    return getHintForPpem(EContour.k_defaultPixelSize);
	}
	
	public EHint getHintForPpem(long a_ppem) {
		int i;
		XHint[] hints = getHint();
		for (i = 0; i < hints.length; i++) {
			EHint hint = (EHint) hints[i];
			if (hint.getPpem() == a_ppem) {
				return hint;
			} // if
		} // for i
		
		return null;
	}
		
	public EHint addHint(int a_ppem) {
		if (hasHintForPpem(a_ppem)) {
			return getHintForPpem(a_ppem);
		} // if
				
		EHint retval = new EHint(getX() + Engine.getEm() / a_ppem, getY(), a_ppem);
		addHint(retval);
		return retval;
	}
	
	public GlyphObject add() {
		return getParent().insertPoint(this);
	}
	
	public void remove() {
		getParent().removePoint(this);
	}
	
	public PointAggregate getParent() {
	    if (rGetParentRNode() instanceof EControlPoint) {
	        EControlPoint controlPoint = (EControlPoint) rGetParentRNode();
	        return controlPoint.getParent().getParent();
	    }
	    
	    if (rGetParentRNode() instanceof PointAggregate) {
	        return (PointAggregate) rGetParentRNode();
	    } // if
	    
		return (PointAggregate) rGetParentRNode();
	}
	
	public XContourPoint cloneAt(Point2D a_point) {
		EContourPoint retval = (EContourPoint) clone();
		Point2D delta = new Point2D.Double(a_point.getX() - this.getX(), a_point.getY() - this.getY());
		EObject.movePoint(retval, delta);
		return retval;
	}
	
	/** Moves point by the specified delta. A GlyphObject method.
	 * Delegates the implementation to the host since a point
	 * moves differently depending on the host.
	 * For example the first point hosted by a module invoke would
	 * always be (0, 0) so the invoke position would move instead.
	 */
	public void move(Point2D a_delta) {
		PointAggregate parent = (PointAggregate) rGetParentRNode();
		parent.movePoint(this, a_delta);
	}
	
	public void toggleOnCurve() {
	    if (isControlPoint()) {
	        return;
	    } // if
	    
	    if (getParent() instanceof EContour) {
	        EContour contour = (EContour) getParent();
	        if (contour.isCubic()) {	            
	            toggleCubicControlPoint();
	            return;
	        } // if
	    } // if
	    
		setOn(!isOn());
	}
	
	public boolean isControlPoint() {
	    return (getParent() instanceof EContourPoint);
	}
	
	private void toggleCubicControlPoint() {
	    if (hasControlPoint1() || hasControlPoint2()) {
	        setControlPoint1(null);
	        setControlPoint2(null);
	    } else {
	        setControlPoint1(new EControlPoint(true, getX() + 100, getY()));
	        setControlPoint2(new EControlPoint(false, getX() - 100, getY()));
	    } // if
	}
	
	public boolean hasControlPoint1() {
	    return (getControlPoint1() != null);
	}
	
	public boolean hasControlPoint2() {
	    return (getControlPoint2() != null);
	}
	
	public void setOn(boolean a_isOnCurve) {
		if (a_isOnCurve) {
			setType(k_on);
		} else {
			setType(k_off);
		} // if
	}
	
	public boolean hitHint(Rectangle2D a_rect, AffineTransform a_trans) {
		int ppem = EContour.k_defaultPixelSize;
		if (!hasHintForPpem(ppem))
			return false;
			
		EHint hint = getHintForPpem(ppem);
		
		Shape hitArea = hint.toShape(a_trans, ppem);
		if (!hitArea.intersects(a_rect)) {
			return false;					
		} // if
		
		EObject.getActives().addActive(hint);		
		return true;
	}
	
	public boolean hit(Rectangle2D a_rect, AffineTransform a_trans) {
		return EObject.hit(this, a_rect, buildOffsetTrans(a_trans));
	}
	
	/** Round the value after converting to pixel coordinate system.
	 */
	private double round(double a_value, long a_ppem) {	    
	    double em = Engine.getEm();
	    double funitInGrid = em / a_ppem;
	    
	    double pixelValue = a_value / funitInGrid;
	    return funitInGrid * roundSymetric(pixelValue);
	}
	
	/** round symetrically about zero.
	 * Math.round(0.5) returns 1. Math.round(-0.5) returns 0, 
	 * since it's Math.floor(a_value + -0.5).
	 * -0.5 should round to -1 if it's symetric about zero.
	 * @param a_value number to be rounded.
	 * @return round value.
	 */
	private int roundSymetric(double a_value) {
	    if (a_value < 0) {
	        int retval = (int) Math.round(-a_value);
	        return -retval;
	    } else {
	        return (int) Math.round(a_value);
	    } // if-else
	}
	
	public void removeControlPoint(EControlPoint a_point) {
	    if (hasControlPoint1() && (getControlPoint1() == a_point)) {
	        setControlPoint1(null);
	    } // if
	    
	    if (hasControlPoint2() && (getControlPoint2() == a_point)) {
	        setControlPoint2(null);
	    } // if
	}
}