 /*
 * $Id: Memento.java,v 1.1 2004/09/05 17:08:03 eed3si9n Exp $
 * 
 * $Copyright: copyright (c) 2003 - 2004, e.e d3si9n $
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

import java.io.*;
import java.util.zip.*;

/**
 * @author e.e
 */
public class Memento {
	private String m_description;
	private byte [] m_compressed;
	private int m_originalSize;
	private long m_entryTime;
	
	public Memento(String a_description, byte [] a_data) {
		m_description = a_description;
		m_originalSize = a_data.length;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(out);
		
		try {
			zip.putNextEntry(new ZipEntry(a_description));
			zip.write(a_data);
			zip.closeEntry();
			zip.close();	
		}
		catch (IOException ioe) {
			ioe.printStackTrace();			
		}
		
		m_compressed = out.toByteArray();
		m_entryTime = System.currentTimeMillis();
	}
	
	public InputStream getData() {		
		ZipInputStream zip = null;
		
		try {
			zip = new ZipInputStream(
				new ByteArrayInputStream(m_compressed));
			ZipEntry entry = zip.getNextEntry();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();	
		}
		
		return zip;	
	}
	
	public String toString() {
		return m_description;
	}
}
