/*
 * $Id: EModuleInvoke.java,v 1.4 2004/11/08 06:29:51 eed3si9n Exp $
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

import org.doubletype.ossa.*;
import org.doubletype.ossa.module.*;
import org.doubletype.ossa.xml.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;

/**
 * @author e.e
 */
public class EModuleInvoke extends XModule implements VarStackFrame, GlyphObject, PointAggregate {	
	public static EModuleInvoke create() {
	    return createAt(new Point2D.Double(0, 254.0));
	}
    
    public static EModuleInvoke createAt(Point2D a_point) {
		EModuleInvoke retval = new EModuleInvoke();
		
		retval.setName(org.doubletype.ossa.module.Rectangle.class.getName());	
		retval.setInvoke(EObject.createInvoke(a_point));
		
		double x, y;
		XContourPoint point;
		
		x = 0.0;
		y = 0.0;
		point = new EContourPoint(x, y, true);
		retval.addContourPoint(point);
		
		
		x += 426.0;
		point = new EContourPoint(x, y, true);
		retval.addContourPoint(point);
		
		y -= 254.0;
		point = new EContourPoint(x, y, true);
		retval.addContourPoint(point);
		
		x -= 426.0;
		point = new EContourPoint(x, y, true);
		retval.addContourPoint(point);
		
		return retval;
	}
	
	
	/** environment with invoke args. */
	private Hashtable<String,Double> m_augmented = new Hashtable<>();
	private VarStack m_stack = VarStack.getSingletonInstance();
	
	// --------------------------------------------------------------
		
	public EModuleInvoke() {
	}
	
	public EModuleInvoke(RStack a_stack) {
		setup(a_stack);
	}
	
	// --------------------------------------------------------------
	
	public void display(Graphics2D g, AffineTransform a_trans) {
		if (isSelected()) {
			g.setColor(GlyphColor.SELECTED);	
		} else {
			g.setColor(GlyphColor.MODULE);
		} // if
		
		g.draw(toShape(a_trans, EContour.k_defaultPixelSize));
	}
	
	public boolean isSelected() {
		return EObject.getActives().isSelected(this);
	}
	
	public Shape toShape(AffineTransform a_trans, int a_ppem) {
		EContour contour = toContour(a_trans);
		return contour.toShape(new AffineTransform(), a_ppem);	
	}
	
	// --------------------------------------------------------------
	
	public EContour toContour(AffineTransform a_trans) {
		EContour retval;
		
		GlyphModule module = getModule();
		
		m_stack.push(this);
		EContour untransContour = module.toContour(this);
		m_stack.pop();
		
		XPoint2d pos = getInvoke().getInvokePos().getPoint2d();
		AffineTransform trans = (AffineTransform) a_trans.clone();
		trans.translate(pos.getX(), pos.getY());
		
		retval =  untransContour.transpose(trans);
		
		return retval;
	}
	
	public GlyphModule getModule() {
		return getModuleForName(getName());
	}
	
	private GlyphModule getModuleForName(String a_name) {
		GlyphModule retval = null;
		
		try {
			ModuleManager manager = ModuleManager.getSingletonInstance();
			retval = manager.getModule(a_name);
		} catch (Exception e) {
			e.printStackTrace();
		} // try-catch	
		
		return retval;
	}
	
	public void remove() {
		XBody parent = (XBody) rGetParentRNode();
		parent.removeModule(this);
	}
	
	public void move(Point2D a_delta) {
		XPoint2d point = getInvoke().getInvokePos().getPoint2d();
		EObject.movePoint(point, a_delta);
	}
	
	// PointAggregate
	public void removePoint(EContourPoint a_point) {
		boolean isFirst = (a_point == getContourPoint(0));
		removeContourPoint(a_point);
		
		GlyphModule module = getModuleForName(getName());
		
		if (getContourPoint().length < module.getMinimumPointCount())
		{
			remove();
			return;	
		} // if
		
		if (!isFirst) {
			return;
		} // if
		
		EContourPoint newFirst = (EContourPoint) getContourPoint(0);
		Point2D pos = newFirst.toPoint2D();
		Point2D delta = new Point2D.Double(-pos.getX(), -pos.getY());
		moveModulePoints(delta);
		move(pos);
	}
	
	public void moveModulePoints(Point2D a_delta) {
		XContourPoint [] points = getContourPoint();
		
		int i;
		for (i = 0; i < points.length; i++) {
			EContourPoint point = (EContourPoint) points[i];
			EObject.movePoint(point, a_delta);
		} // for i
	}
	
	// for var stack frame
	public boolean hasVariable(String a_name) {
		return m_augmented.containsKey(a_name);
	}
	
	// for var stack frame.
	public double getValue(String a_name) {		
		Double d = (Double) m_augmented.get(a_name);
		return d.doubleValue();
	}
	
	// for var stack frame. 
	public void beforePush() {
		loadInvoke(getInvoke());
	}
	
	/**
	 * loads invoke arguments into this module to build augmented environment.
	 * @param a_invoke
	 */
	private void loadInvoke(XInvoke a_invoke) {
		copyVarsToAug();
		
		// load varg
		int i;
		XInvokeVarg [] vargs = a_invoke.getInvokeVarg();
		for (i = 0; i < vargs.length; i++) {
			loadVarg(vargs[i]);
		} // for i
	}
	
	private void loadVarg(XInvokeVarg a_varg) {
		String name = a_varg.getName();
		String src = a_varg.getSrc();
		GlyphModule module = getModule();
		
		if (!module.hasVariable(name)) {
			System.out.println(name + " does not exist in " 
				+ module.toString());
			
			return;
		} // if
		
		double value = m_stack.get(src);
		m_augmented.put(name, new Double(value));
	}
	
	private void copyVarsToAug() {
		m_augmented.clear();
		m_augmented.putAll(getModule().getVars());	
	}
	
	public void movePoint(EContourPoint a_point, Point2D a_delta) {
		EObject.movePoint(a_point, a_delta);
		
		if (a_point != getContourPoint(0)) {
			return;
		} // if
						
		Point2D negative = new Point2D.Double(-a_delta.getX(), -a_delta.getY());
		moveModulePoints(negative);
		move(a_delta);
	}
	
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
}
