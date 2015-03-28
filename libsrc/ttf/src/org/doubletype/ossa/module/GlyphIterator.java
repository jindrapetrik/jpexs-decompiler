 /*
 * $Id: GlyphIterator.java,v 1.11 2004/12/27 04:56:03 eed3si9n Exp $
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
 
package org.doubletype.ossa.module;

import java.util.*;

import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.adapter.*;

/** Iterates through contours, module invokes, then include invokes.
 * Used in GlyphFile display
 * @author e.e
 */
public class GlyphIterator implements Iterator {
	protected GlyphFile m_file;
	private int m_index = 0;
	protected List<IRNode> m_list = new ArrayList<>();
	
	public GlyphIterator(GlyphFile a_file) {
		m_file = a_file;
		buildList();
	}
	
	protected void buildList() {
	    addIncludes();
	    addContours();
	    addModules();
	    
	}

	protected void addContours() {
		XContour [] contours = m_file.getGlyph().getBody().getContour();
		for (int i = 0; i < contours.length; i++) {
			m_list.add(contours[i]);
		} // for i
	}
	
	protected void addControlPoints() {
		XContour [] contours = m_file.getGlyph().getBody().getContour();
		for (int i = 0; i < contours.length; i++) {
		    EContour contour = (EContour) contours[i];
		    if (!contour.isCubic()) {
		        continue;
		    } // if
		    
		    addControlPoints(contour, 0, 0);
		} // for i 
	}
	
	private void addControlPoints(PointAggregate a_object, double a_x, double a_y) {	    
	    XContourPoint [] points = a_object.getContourPoint();
	    
	    for (int i = 0; i < points.length; i++) {
		    EContourPoint point = (EContourPoint) points[i];
		    
		    if (!isPointVisible(point)) {
		        continue;
		    } // if
		    
		    if (point.hasControlPoint1()) {
		        EControlPoint controlPoint = (EControlPoint) point.getControlPoint1();
		        controlPoint.setOffset(a_x, a_y);
		        m_list.add(controlPoint);
		    } // if
		    
		    if (point.hasControlPoint2()) {
		        EControlPoint controlPoint = (EControlPoint) point.getControlPoint2();
		        controlPoint.setOffset(a_x, a_y);
		        m_list.add(controlPoint);
		    } // if
		} // for j
	}
	
	protected void addHints() {
		XContour [] contours = m_file.getGlyph().getBody().getContour();
		for (int i = 0; i < contours.length; i++) {
		    addHints((EContour) contours[i], 0, 0);
		} // for i
		
		XModule [] modules = m_file.getGlyph().getBody().getModule();
		for (int i = 0; i < modules.length; i++) {
		    EModuleInvoke module = (EModuleInvoke) modules[i];
		    double x = module.getInvoke().getInvokePos().getPoint2d().getX();
		    double y = module.getInvoke().getInvokePos().getPoint2d().getY();
		    
			addHints(module, x, y);
		} // for i
	}
	
	private void addHints(PointAggregate a_object, double a_x, double a_y) {	    
	    XContourPoint [] points = a_object.getContourPoint();
	    
	    for (int i = 0; i < points.length; i++) {
		    EContourPoint point = (EContourPoint) points[i];
		    
		    if (!isPointVisible(point)) {
		        continue;
		    } // if
		    
		    if (point.hasHintForCurrentPpem()) {
		        EHint hint = point.getCurrentHint();
		        hint.setOffset(a_x, a_y);
		        m_list.add(hint);
		    } // if
		    
		    if (point.hasControlPoint1()) {
		        EContourPoint p = (EContourPoint) point.getControlPoint1().getContourPoint();
		        if (p.hasHintForCurrentPpem()) {
		            EHint hint = p.getCurrentHint();
		            hint.setOffset(a_x, a_y);
		            m_list.add(hint);
		        } // if
		    }
		    
		    if (point.hasControlPoint2()) {
		        EContourPoint p = (EContourPoint) point.getControlPoint2().getContourPoint();
		        if (p.hasHintForCurrentPpem()) {
		            EHint hint = p.getCurrentHint();
		            hint.setOffset(a_x, a_y);
		            m_list.add(hint);
		        } // if
		    } // if
		} // for j
	}
	
	private boolean isPointVisible(EContourPoint a_point) {
	    if (m_file.getPointHost() == null) {
	        return true;
	    } // if
	    
	    if (m_file.getPointHost() == a_point.getParent()) {
	        return true;
	    } // if
	    
	    return false;
	}
	
	protected void addPoints() {
		XContour [] contours = m_file.getGlyph().getBody().getContour();
		for (int i = 0; i < contours.length; i++) {
			addPoints((EContour) contours[i], 0, 0);
		} // for i
		
		XModule [] modules = m_file.getGlyph().getBody().getModule();
		for (int i = 0; i < modules.length; i++) {
		    EModuleInvoke module = (EModuleInvoke) modules[i];
		    double x = module.getInvoke().getInvokePos().getPoint2d().getX();
		    double y = module.getInvoke().getInvokePos().getPoint2d().getY();
		    
			addPoints(module, x, y);
		} // for i
	}
	
	private void addPoints(PointAggregate a_object, double a_x, double a_y) {	    
	    XContourPoint [] points = a_object.getContourPoint();
	    
	    for (int i = 0; i < points.length; i++) {
		    EContourPoint point = (EContourPoint) points[i];
		    
		    if (!isPointVisible(point)) {
		        continue;
		    } // if
		    
		    point.setNumber(i);
		    point.setOffset(a_x, a_y);
		    m_list.add(point);
		} // for j
	}
	
	protected void addModules() {
		XModule [] modules = m_file.getGlyph().getBody().getModule();
		for (int i = 0; i < modules.length; i++) {
			m_list.add(modules[i]);
		} // for i
	}
	
	protected void addIncludes() {
	    XInclude [] includes = m_file.getGlyph().getBody().getInclude();
		for (int i = 0; i < includes.length; i++) {
			m_list.add(includes[i]);	
		} // for i;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {

	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return m_index < m_list.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		return m_list.get(m_index++);
	}
}
