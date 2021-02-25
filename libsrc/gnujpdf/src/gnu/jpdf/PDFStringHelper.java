/*
 * $Id: PDFStringHelper.java,v 1.2 2007/08/26 18:56:35 gil1 Exp $
 *
 * $Date: 2007/08/26 18:56:35 $
 *
 * Copyright (C) 2001  Eric Z. Beard, ericzbeard@hotmail.com
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package gnu.jpdf;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * String manipulation methods
 *
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.2 $, $Date: 2007/08/26 18:56:35 $
 *
 */
public class PDFStringHelper {

    /**
     * This converts a string into PDF. It prefixes ( or ) with \ and wraps the
     * string in a ( ) pair.
     *
     * @param s String to convert
     * @return String that can be placed in a PDF (or Postscript) stream
     */
    public static String makePDFString(String s) {
        if (s.indexOf("(") > -1) {
            s = replace(s, "(", "\\(");
        }

        if (s.indexOf(")") > -1) {
            s = replace(s, ")", "\\)");
        }

        return "(" + s + ")";
    }

    public static byte[] makeRawPDFString(String s) {
        try {
            byte[] utf16be = s.getBytes("UTF-16BE");
            List<Byte> bytes = new ArrayList<>();
            for (int i = 0; i < utf16be.length; i++) {
                switch (utf16be[i]) {
                    case '\r':
                        bytes.add((byte) '\\');
                        bytes.add((byte) 'r');
                        break;
                    case '(':
                    case ')':
                    case '\\':
                        bytes.add((byte) '\\');
                    default:
                        bytes.add(utf16be[i]);
                }
            }
            byte[] ret = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                ret[i] = bytes.get(i);
            }
            return ret;

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PDFStringHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Helper method for toString()
     *
     * @param s source string
     * @param f string to remove
     * @param t string to replace f
     * @return string with f replaced by t
     */
    private static String replace(String source,
            String removeThis,
            String replaceWith) {
        StringBuffer b = new StringBuffer();
        int p = 0, c = 0;

        while (c > -1) {
            if ((c = source.indexOf(removeThis, p)) > -1) {
                b.append(source.substring(p, c));
                b.append(replaceWith);
                p = c + 1;
            }
        }

        // include any remaining text
        if (p < source.length()) {
            b.append(source.substring(p));
        }

        return b.toString();
    }
} // end class PDFStringHelper
