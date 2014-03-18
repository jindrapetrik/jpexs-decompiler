/*
 * $Id: UnicodeBuilder.java,v 1.1 2004/09/11 10:09:07 eed3si9n Exp $
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

package org.doubletype.ossa;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author e.e
 */
public class UnicodeBuilder {
	public static final String k_jisCharset = "ISO-2022-JP";
    
	/**
	 * pops dialog and asks user for unicode.
	 * @return true on success, false otherwise.
	 */
	public static Long askUnicode() {
	    Engine engine = Engine.getSingletonInstance();
	    
		throw new UnsupportedOperationException();
	}
	
    public static Long build(String a_value, int a_option) {        
        if (a_value == null
                || a_value.length() == 0) {
            return null;
        } // if
        
        switch (a_option) {
        	case Engine.SEARCH_BY_EXAMPLE: {
        	    return buildByExample(a_value);
        	} // case
        	
        	case Engine.SEARCH_UNICODE: {
        	    return buildByUnicode(a_value);
        	} // case
        	
        	case Engine.SEARCH_JIS_CODE: {
        	    return buildByJisCode(a_value);
        	} // case
        } // switch
        
        return null;
    }
    
    private static Long buildByExample(String a_value) {
        if (a_value.length() > 1) {
            return null;
        } // if
        
        return new Long((long) a_value.charAt(0));
    }
    
    private static Long buildByUnicode(String a_value) { 
        if (a_value.length() == 1) {
            return buildByExample(a_value);
        } // if
        
		try {
			return new Long(Long.parseLong(a_value, 16));
		} catch (NumberFormatException e) {
			Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null,e);
		}
        
        return null;
    }
    
    private static Long buildByJisCode(String a_value) { 
        if (a_value.length() == 1) {
            return buildByExample(a_value);
        } // if
        
		try {
			Long retval = jisX0208ToUnicode(Long.parseLong(a_value, 16));
			
			if (retval == null) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, "Bad JIS Code.");
			} // if
			
			return retval;
		} catch (NumberFormatException e) {
			Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, e);
		} // try
		
        return null;
    }
    
	/**
	 * converts JIS X0208 code into unicode using ISO-2022-JP decoder.
	 * @param a_value JIS code
	 * @return unicode
	 */
	public static Long jisX0208ToUnicode(long a_value) {
		if (!isJisSupported()) {
			return null;
		} // if
				
		Charset jis = Charset.forName(k_jisCharset);
		ByteBuffer in = ByteBuffer.allocate(5);
		CharsetDecoder jisDecoder;
		jisDecoder = jis.newDecoder();
		
		long high = (0xff00 & a_value) >> 8;
		long low = 0x00ff & a_value;

		if (high < 0x21 || high > 0x7e || low < 0x21 || high > 0x7e) {
			return null;
		} // if

		char c = (char) a_value;

		in.rewind();
		in.put((byte) 0x1b);
		in.put((byte) 0x24);
		in.put((byte) 0x40);

		in.putChar(c);
		in.position(0);

		try {
			CharBuffer out = jisDecoder.decode(in);
			if (out.length() > 0) {
				return new Long(out.get(0));
			} // if
		} catch (CharacterCodingException e) {
		    e.printStackTrace();
		} // try-catch	

		return null;
	}
	
	public static boolean isJisSupported() {
	    return Charset.isSupported(k_jisCharset);
	}
    
    /**
     * 
     */
    public UnicodeBuilder() {
        super();
        // TODO Auto-generated constructor stub
    }

}
