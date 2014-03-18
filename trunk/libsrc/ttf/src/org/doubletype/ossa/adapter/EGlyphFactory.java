/*
 * $Id: EGlyphFactory.java,v 1.3 2004/11/10 01:49:49 eed3si9n Exp $
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

import org.doubletype.ossa.xml.*;

/**
 * @author e.e
 */
public class EGlyphFactory extends DefaultGlyphFactory {
	public static IGlyphFactory s_factory = null;
	
	public static IGlyphFactory getFactory() {
		if (s_factory == null) {
			s_factory =	new EGlyphFactory();
		} // if
		
		return s_factory;
	}
	
	public EGlyphFactory() {
	}
	
	public XModule createXModule() {
		return new EModuleInvoke();
	}
	
	public Class getXModuleClass() {
		return (EModuleInvoke.class);
	}
	
	public XContour createXContour() {
		return new EContour();
	}
	
	public Class getXContourClass() {
		return (EContour.class);
	}
	
	public XInclude createXInclude() {
		return new EIncludeInvoke();
	}
	
	public Class getXIncludeClass() {
		return (EIncludeInvoke.class);
	}
	
	public XHint createXHint() {
		return new EHint();
	}
	
	public Class getXHintClass() {
		return (EHint.class);
	}
	
	public XContourPoint createXContourPoint() {
		return new EContourPoint();
	}
    
	public XContourPoint createXContourPoint(RStack a_stack) {
        return new EContourPoint(a_stack);
    }
	
	public Class getXContourPointClass() {
		return (EContourPoint.class);
	}
	
	public XControlPoint createXControlPoint() {
	    return new EControlPoint();
	}
	

	public Class getXControlPointClass() {
	    return (EControlPoint.class);
	}
}
