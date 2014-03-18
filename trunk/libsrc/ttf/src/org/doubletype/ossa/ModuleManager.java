/*
 * $Id: ModuleManager.java,v 1.5 2004/01/14 06:49:39 eed3si9n Exp $
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
import org.doubletype.ossa.module.*;
import java.io.*;

/**
 * @author e.e
 */
public class ModuleManager {
	private static ModuleManager s_singleton = null;
	
	public static ModuleManager getSingletonInstance() {
   		if (s_singleton == null)
        	s_singleton = new ModuleManager();
    	return s_singleton;
	}
	
	
	private Hashtable<String,GlyphModule> m_modules = new Hashtable<>();
	private Hashtable<String,GlyphFile> m_files = new Hashtable<>();
	
	private ModuleManager() {	
	}
	
	public void clear() {
		m_modules.clear();
		m_files.clear();	
	}
	
	public GlyphModule getModule(String a_name) throws Exception {
		if (m_modules.containsKey(a_name)) {
			return (GlyphModule) m_modules.get(a_name);
		} // if
		
		GlyphModule retval;
		
		retval = (GlyphModule) Class.forName(a_name).newInstance();
		m_modules.put(a_name, retval);
		
		return retval;	
	}
	
	public GlyphFile getGlyphFile(String a_name) {
		if (m_files.containsKey(a_name)) {
			return (GlyphFile) m_files.get(a_name);
		} // if
				
		return getReloadedGlyphFile(a_name);		
	}
	
	public GlyphFile getReloadedGlyphFile(String a_name) {
		GlyphFile retval;
		
		Engine engine = Engine.getSingletonInstance();
		retval = new GlyphFile(new File(engine.getGlyphPath(), a_name));
		m_files.put(a_name, retval);
		
		return retval;
	}
}
