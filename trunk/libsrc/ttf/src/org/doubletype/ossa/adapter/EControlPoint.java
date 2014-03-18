/*
 * $Id: EControlPoint.java,v 1.6 2004/12/27 04:56:02 eed3si9n Exp $
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.doubletype.ossa.GlyphColor;
import org.doubletype.ossa.xml.*;

/**
 * @author e.e
 */
public class EControlPoint extends XControlPoint implements GlyphObject, EPoint {
	private static final String k_true = "true";
	
    private double m_xOffset = 0;
	private double m_yOffset = 0;
    
	public EControlPoint(boolean a_isFirst, double a_x, double a_y) {
		setSmooth(true);
	    setFirst(a_isFirst);
	    setContourPoint(new EContourPoint(a_x, a_y, false));
	}
	
    /**
     * 
     */
    public EControlPoint() {
        super();
    }

    /**
     * @param stack
     */
    public EControlPoint(RStack stack) {
        super(stack);
    }
    
	public boolean isSelected() {
		return EObject.getActives().isSelected(this);
	}
	
	/** toggle smooth-ness
	 */
	public void convert() {
	    EContourPoint parent = getParent();
	    boolean value = !getSmooth();
	    parent.getControlPoint1().setSmooth(value);
	    parent.getControlPoint2().setSmooth(value);
	    move(new Point2D.Double(0, 0));
	}
	
	public boolean isFirst() {
	    return getFirst();
	}
	
	public boolean hit(Rectangle2D a_rect, AffineTransform a_trans) {
		return EObject.hit(this, a_rect, buildOffsetTrans(a_trans));
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
	
	public void remove() {
		getParent().removeControlPoint(this);
	}
	
	public EContourPoint getParent() {
	    return (EContourPoint) rGetParentRNode();
	}
	
	public Shape toShape(AffineTransform a_trans, int a_ppem) {
		return EObject.toShape(getContourPoint().getPoint2d(), a_trans);
	}
	
	public void move(Point2D a_delta) {
	    // if the parent is active, she will move this.
	    if (getParent().isSelected()) {
	        return;
	    } // if
	    
	    EObject.movePoint((EContourPoint) getContourPoint(), a_delta);
	    	    
	    if (isSmooth() && hasSibling()) {
	        smoothSibling();
	    } // if
	}
	
	public void rotateTo45() {
        double theta = getTheta() + Math.PI / 8;
        theta = Math.PI / 4 * Math.floor(theta / (Math.PI / 4));
        
        rotateToTheta(theta);
	    if (isSmooth() && hasSibling()) {
	        smoothSibling();
	    } // if
	}
	
	private void smoothSibling() {
	    getSibling().rotateToTheta(getTheta() + Math.PI);
	}
	
	private double getTheta() {
	    EContourPoint parent = getParent();
	    return Math.atan2(getY() - parent.getY(),
				getX() - parent.getX());
	}
	
	private void rotateToTheta(double a_theta) {
	    EContourPoint parent = getParent();
	    double d = parent.toPoint2D().distance(toPoint2D());
	    double x = parent.getX() + Math.cos(a_theta) * d;
	    double y = parent.getY() + Math.sin(a_theta) * d;
	    Point2D delta = new Point2D.Double(x - getX(), y - getY());
	    EObject.movePoint((EContourPoint) getContourPoint(), delta);
	}
	
	public Point2D toPoint2D() {
	    EContourPoint point = (EContourPoint) getContourPoint();
	    return point.toPoint2D();
	}
	
	public boolean isSmooth() {
	    return getSmooth();
	}
	
	public boolean hasSibling() {
	    EContourPoint parent = getParent();
	    if (parent.getControlPoint1() == this) {
	        return parent.hasControlPoint2();
	    } else {
	        return parent.hasControlPoint1();
	    } // if
	}
	
	public EControlPoint getSibling() {
	    EContourPoint parent = getParent();
	    if (parent.getControlPoint1() == this) {
	        return (EControlPoint) parent.getControlPoint2();
	    } else {
	        return (EControlPoint) parent.getControlPoint1();
	    } // if 
	}
	
	public void display(Graphics2D g, AffineTransform a_trans) {
	    AffineTransform trans = buildOffsetTrans(a_trans);
	    displayLine(g, trans);
	    
	    Shape rect = toShape(trans, EContour.k_defaultPixelSize);
	    g.setColor(GlyphColor.POINT);
		if (isSelected()) {
			g.setColor(GlyphColor.SELECTED);
		} // if

		g.draw(rect);
	}
	
	private void displayLine(Graphics2D g, AffineTransform a_trans) {
	    EContourPoint parent = getParent();
		Line2D line = new Line2D.Double(parent.getX(),
		        parent.getY(),
				getX(),
				getY());
		AffineTransform oldTrans = g.getTransform();
		
		g.transform(a_trans);
		g.setColor(Color.BLACK);
		g.draw(line);
		g.setTransform(oldTrans);
	}
	
	public EControlPoint transpose(AffineTransform a_trans) {
	    EControlPoint retval = new EControlPoint();
	    EContourPoint point = (EContourPoint) getContourPoint();
	    retval.setContourPoint(point.transpose(a_trans));
	    retval.setSmooth(getSmooth());
	    
	    return retval;
	}
	
	
	public double getX() {
	    EContourPoint point = (EContourPoint) getContourPoint();
		return point.getX();
	}
	
	public double getY() {
	    EContourPoint point = (EContourPoint) getContourPoint();
		return point.getY();
	}
}

