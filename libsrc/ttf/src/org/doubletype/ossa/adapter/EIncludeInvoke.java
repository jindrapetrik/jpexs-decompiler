/*
 * $Id: EIncludeInvoke.java,v 1.1 2004/09/04 21:54:07 eed3si9n Exp $
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
import org.doubletype.ossa.module.*;
import java.util.*;

/**
 * @author e.e
 */
public class EIncludeInvoke extends XInclude implements VarStackFrame, GlyphObject {
	public static EIncludeInvoke create(String a_fileName) {
		EIncludeInvoke retval = new EIncludeInvoke();		
		retval.setHref(a_fileName);
		retval.setInvoke(EObject.createInvoke(new Point2D.Double()));
		
		return retval;
	}
	
	/** environment with invoke args. */
	private Hashtable<String,Double> m_augmented = new Hashtable<>();
	private VarStack m_stack = VarStack.getSingletonInstance();
	
	public EIncludeInvoke() {
	}
	
	public EIncludeInvoke(RStack a_stack) {
		setup(a_stack);
	}
	
	public void display(Graphics2D g, AffineTransform a_trans) {
		g.setColor(GlyphColor.INCLUDE);
		if (isSelected()) {
			g.setColor(GlyphColor.SELECTED);	
		} // if
			
		g.draw(toShape(a_trans,EContour.k_defaultPixelSize));
	}
	
	public boolean isSelected() {
		return EObject.getActives().isSelected(this);
	}
	
	public Shape toShape(AffineTransform a_trans, int a_ppem) {
		Shape retval;
		
		ModuleManager manager = ModuleManager.getSingletonInstance();
		GlyphFile file = manager.getGlyphFile(getHref());
				
		XPoint2d pos = getInvoke().getInvokePos().getPoint2d();		
		
		AffineTransform trans = new AffineTransform();
		trans.setTransform(a_trans);
		trans.translate(pos.getX(), pos.getY());
		
		m_stack.push(this);
		retval = file.toShape(trans);
		m_stack.pop();
		
		return retval;	
	}
	
	public GlyphModule getModule() {
		ModuleManager manager = ModuleManager.getSingletonInstance();
		return manager.getGlyphFile(getHref());
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
		getModule().beforePush();
		loadInvoke(getInvoke());
	}
	
	public void remove() {
		XBody parent = (XBody) rGetParentRNode();
		parent.removeInclude(this);
	}
	
	public void move(Point2D a_delta) {
		XPoint2d point = getInvoke().getInvokePos().getPoint2d();
		EObject.movePoint(point, a_delta);	
	}
	
	/**
	 * loads invoke arguments into this module to build augmented environment.
	 * @param a_invoke
	 */
	private void loadInvoke(XInvoke a_invoke) {
		copyVarsToAug();
		
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
			return;
		} // if
		
		double value = m_stack.get(src);
		m_augmented.put(name, new Double(value));
	}
	
	private void copyVarsToAug() {
		m_augmented.clear();
		m_augmented.putAll(getModule().getVars());	
	}
	
	public boolean hit(Rectangle2D a_rect, AffineTransform a_trans) {
		return EObject.hit(this, a_rect, a_trans);
	}
}
