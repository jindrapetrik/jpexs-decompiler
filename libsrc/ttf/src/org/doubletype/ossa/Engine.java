/*
 * $Id: Engine.java,v 1.84 2004/12/27 04:56:03 eed3si9n Exp $
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

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.doubletype.ossa.module.GlyphFile;
import org.doubletype.ossa.module.TypefaceFile;
import org.doubletype.ossa.truetype.TTCodePage;
import org.doubletype.ossa.truetype.TTUnicodeRange;

/**
 * @author e.e
 */
public class Engine {

    private TypefaceFile m_typeface;

    private GlyphFile m_root;

    // --------------------------------------------------------------
    public Engine() {
    }

    public void setAdvanceWidth(int a_value) {
        if (m_root == null) {
            return;
        }

        m_root.setAdvanceWidth(a_value);
    }

    public void buildNewTypeface(String a_name, File a_dir) throws FileNotFoundException {
        if (a_name == null || a_name.equals("")) {
            return;
        }

        TypefaceFile typeface = new TypefaceFile(a_name, a_dir);
        typeface.setAuthor("no body");
        DateFormat format = new SimpleDateFormat("yyyy");
        typeface.setCopyrightYear(format.format(new Date()));
        typeface.setFontFamilyName(a_name);
        typeface.addCodePage(TTCodePage.US_ASCII.toString());
        typeface.addCodePage(TTCodePage.Latin_1.toString());

        setTypeface(typeface);
    }

    public void addDefaultGlyphs() {
        m_typeface.addRequiredGlyphs();
        m_typeface.addBasicLatinGlyphs();
    }

    public void setTypeface(TypefaceFile a_typeface) {
        m_typeface = a_typeface;
    }

    /**
     * Create glyph out of given unicode, and add it to the typeface.
     *
     * @param a_unicode
     */
    public GlyphFile addNewGlyph(long a_unicode) {
        GlyphFile retval;

        retval = m_typeface.createGlyph(a_unicode);
        addGlyphToTypeface(retval);

        return retval;
    }

    public void checkUnicodeBlock(long a_unicode) {
        TTUnicodeRange range = TTUnicodeRange.of(a_unicode);
        if (range == null) {
            return;
        }

        if (m_typeface.containsUnicodeRange(range.toString())) {
            return;
        }
        m_typeface.addUnicodeRange(range.toString());
    }

    private void addGlyphToTypeface(GlyphFile a_file) {
        m_typeface.addGlyph(a_file);

        setRoot(a_file);
    }

    public void buildTrueType() {
        if (m_typeface == null) {
            return;
        }

        try {
            m_typeface.buildTTF();
            m_typeface.getFont();
        } catch (Exception e) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, e);
        }

        return;
    }

    public TypefaceFile getTypeface() {
        return m_typeface;
    }

    public File getGlyphPath() {
        return m_typeface.getGlyphPath();
    }

    public GlyphFile getRoot() {
        return m_root;
    }

    public void setRoot(GlyphFile a_file) {
        m_root = a_file;
    }

    public void addCodePage(String a_codePage) {
        if (m_typeface == null) {
            return;
        }

        m_typeface.addCodePage(a_codePage);
    }

    public void removeCodePage(String a_codePage) {
        if (m_typeface == null) {
            return;
        }

        m_typeface.removeCodePage(a_codePage);
    }

    public void setAuthor(String a_value) {
        if (m_typeface == null) {
            return;
        }

        m_typeface.setAuthor(a_value);
    }

    public void setCopyrightYear(String a_value) {
        if (m_typeface == null) {
            return;
        }

        m_typeface.setCopyrightYear(a_value);
    }

    public void setCreationDate(Date a_value) {
        if (m_typeface == null) {
            return;
        }

        m_typeface.setCreationDate(a_value);
    }

    public void setModificationDate(Date a_value) {
        if (m_typeface == null) {
            return;
        }

        m_typeface.setModificationDate(a_value);
    }

    public void setFontFamilyName(String a_value) {
        if (m_typeface == null) {
            return;
        }

        m_typeface.setFontFamilyName(a_value);
    }

    public void setTypefaceLicense(String a_value) {
        if (m_typeface == null) {
            return;
        }

        m_typeface.setLicense(a_value);
    }

    public void setBaseline(double a_value) {
        if (m_typeface == null) {
            return;
        }

        double min = m_typeface.getBottomSideBearing();
        double max = m_typeface.getMeanline();

        if (a_value < min) {
            a_value = min;
        }

        if (a_value > max) {
            a_value = max;
        }

        try {
            m_typeface.setDescender(a_value - min);
            m_typeface.setAscender(m_typeface.getEm()
                    - m_typeface.getTopSideBearing() - a_value);
            m_typeface.setXHeight(max - a_value);
        } catch (OutOfRangeException e) {
            e.printStackTrace();
        }
    }

    public void setMeanline(double a_value) {
        if (m_typeface == null) {
            return;
        }

        double min = m_typeface.getBaseline();
        double max = m_typeface.getEm()
                - m_typeface.getTopSideBearing();

        if (a_value < min) {
            a_value = min;
        }

        if (a_value > max) {
            a_value = max;
        }

        try {
            m_typeface.setXHeight(a_value - min);
        } catch (OutOfRangeException e) {
            e.printStackTrace();
        }
    }
}
