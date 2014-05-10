 /*
 * $Id: ActiveList.java,v 1.7 2004/11/15 03:39:38 eed3si9n Exp $
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
 
package org.doubletype.ossa;

import java.util.*;

import org.doubletype.ossa.adapter.*;

/**
 * @author e.e
 */
public class ActiveList {
	private static ActiveList s_actives = null;
	
	public static ActiveList getSingletonInstance() {
		if (s_actives == null)
			s_actives = new ActiveList();
			
		return s_actives;
	}
	
	// --------------------------------------------------------------
	
	public ArrayList<GlyphObject> m_activeObjects = new ArrayList<>();
	
	public void unselectAll() {
		m_activeObjects.clear();
	}
	
	public boolean hasSelected() {
		return size() > 0;
	}
	
	public int size() {
		return m_activeObjects.size();
	}
	
	public GlyphObject get(int a_index) {
		return m_activeObjects.get(a_index);
	}

	public boolean isSelected(Object a_value) {
		int i;
		
		// use equal method to compare this will catch XContour etc...
		for (i = 0; i < size(); i++) {
			if (get(i) == a_value) {
				return true;
			} // if
		} // for i
		
		return false;
	}
	
	public void addActive(GlyphObject a_object) {
		if (isSelected(a_object)) {
			return;
		} // if
		
		m_activeObjects.add(a_object);
	}
	
	public void setActives(ActiveList a_actives) {
		unselectAll();
		
		int i;
		for (i = 0; i < a_actives.size(); i++) {
			addActive(a_actives.get(i));
		} // for i
	}

	private GlyphObject getTheActive() {
		GlyphObject retval = null;
		
		if (m_activeObjects.size() == 1) {
			retval = get(0);
		} // if
		
		return retval;
	}
	
	public boolean hasActiveModule() {		
		return (getTheActive() instanceof EModuleInvoke);
	}
	
	public EModuleInvoke getActiveModule() {
		if (hasActiveModule()) {
			return (EModuleInvoke) getTheActive(); 
		} else {
			return null;
		} // if-else
	}
	
	public boolean hasActiveContour() {			
		return (getTheActive() instanceof EContour);
	}
	
	public EContour getActiveContour() {
		if (getTheActive() instanceof EContour) {
			return (EContour) getTheActive(); 
		} else {
			return null;
		} // if-else	
	}
	
	public boolean hasActiveInclude() {		
		return (getTheActive() instanceof EIncludeInvoke);
	}
	
	public EIncludeInvoke getActiveInclude() {
		if (hasActiveInclude()) {
			return (EIncludeInvoke) getTheActive(); 
		} // if
		
		return null;
	}
	
	public boolean hasActiveControlPoint() {
	    return (getTheActive() instanceof EControlPoint);
	}
	
	public EControlPoint getActiveControlPoint() {
	    if (hasActiveControlPoint()) {
	        return (EControlPoint) getTheActive();
	    } // if
	    
	    return null;
	}
	
	public boolean hasActivePoint() {			
		return (getTheActive() instanceof EContourPoint);
	}
	
	public EContourPoint getActivePoint() {
		if (hasActivePoint()) {
			return (EContourPoint) getTheActive();
		} else {
			return null;
		} // if
	}
	
	public boolean hasActiveHint() {
	    return (getTheActive() instanceof EHint);
	}
	
	public String getSelectedAsString() {
		String retval = "";
		
		if (!hasSelected()) {
			return retval;
		} // if
		
		retval = "<clipboard>";
		
		int i;
		for (i = 0; i < size(); i++) {
			GlyphObject active = get(i);
			retval += active.toString();
		} // for i
		
		retval += "</clipboard>";
		
		return retval;
	}
	
}
