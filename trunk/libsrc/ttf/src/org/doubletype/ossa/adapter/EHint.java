/*
 * $Id: EHint.java,v 1.4 2004/12/27 04:56:03 eed3si9n Exp $
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

import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.*;

/**
 * @author e.e
 */
public class EHint extends XHint implements GlyphObject, EPoint {	
	/** offset of this from the global coord.
	 * For contours, it will be 0.
	 * For module invoke, it will be pos.x.
	 * @see EContourPoint
	 */
	private double m_xOffset = 0;
	private double m_yOffset = 0;
    
    public EHint() {
	}
	
	public EHint(double a_x, double a_y, long a_ppem) {
		XPoint2d point = new XPoint2d();
		point.setX(a_x);
		point.setY(a_y);
		
		setPoint2d(point);
		setPpem(a_ppem);
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
	
	public void display(Graphics2D g, AffineTransform a_trans) {
	    AffineTransform trans = buildOffsetTrans(a_trans);
	    
		EContourPoint point = (EContourPoint) rGetParentRNode();
		
		Shape rect = toShape(trans, EContour.k_defaultPixelSize);		
		Line2D line = new Line2D.Double(point.getX(),
			point.getY(),
			getX(),
			getY());
		
		AffineTransform oldTrans = g.getTransform();
		
		g.transform(trans);
		g.setColor(Color.BLACK);
		g.draw(line);
		g.setTransform(oldTrans);
				
		g.setColor(GlyphColor.HINT);
		if (isSelected()) {
			g.setColor(GlyphColor.SELECTED);
		} // if
		
		if (point.isOn())
			g.fill(rect);
		else
			g.draw(rect);	
	}
	
	public boolean isSelected() {
		return EObject.getActives().isSelected(this);
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
	
	public Point2D toPoint2D() {
	    return new Point2D.Double(getX(), getY());
	}
	
	/**
	 * @see org.doubletype.ossa.adapter.DObject#move(java.awt.geom.Point2D)
	 */
	public void move(Point2D a_delta) {
		EObject.movePoint(getPoint2d(), a_delta);
		
		double gridWidth = ((double) Engine.getEm()) / getPpem();
		EContourPoint point = (EContourPoint) rGetParentRNode();
		
		XPoint2d posHint = getPoint2d();
		XPoint2d posPoint = point.getPoint2d();
		double xDelta = posHint.getX() - posPoint.getX();
		double yDelta = posHint.getY() - posPoint.getY();
		
		xDelta = correctDelta(xDelta, gridWidth);
		yDelta = correctDelta(yDelta, gridWidth);
		
		posHint.setX(posPoint.getX() + xDelta);
		posHint.setY(posPoint.getY() + yDelta);
	}
	
	private double correctDelta(double a_delta, double a_width) {
		double retval = a_delta;
		
		if (retval > a_width) {
			retval = a_width;
		} // if
		
		if (retval < -a_width) {
			retval = -a_width;
		} // if
		
		return retval;	
	}
	
	/**
	 * @see org.doubletype.ossa.adapter.DObject#remove()
	 */
	public void remove() {
		getParent().removeHint(this);
	}
	
	public EContourPoint getParent() {
	    return (EContourPoint) rGetParentRNode();
	}
	
	public PointAggregate getPointHost() {
	    return getParent().getParent();
	}
	
	public boolean hit(Rectangle2D a_rect, AffineTransform a_trans) {
		return EObject.hit(this, a_rect, buildOffsetTrans(a_trans));
	}
	
	public EHint createTransform(AffineTransform a_trans) {
		EHint retval = new EHint();
		
		retval.setPpem(getPpem());
		retval.setPoint2d(EObject.createTransform(getPoint2d(), a_trans));
		
		return retval;
	}
}
