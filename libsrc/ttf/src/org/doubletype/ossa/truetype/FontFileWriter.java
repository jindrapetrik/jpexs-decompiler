/*
 * $Id: FontFileWriter.java,v 1.15 2004/10/04 02:25:39 eed3si9n Exp $
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
package org.doubletype.ossa.truetype;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * @author e.e
 */
public class FontFileWriter extends FontFormatWriter {

    private CmapWriter m_cmap;

    private GlyfWriter m_glyf;

    private LocaWriter m_loca;

    private HeadWriter m_head;

    private HdmxWriter m_hdmx;

    private HheaWriter m_hhea;

    private HmtxWriter m_hmtx;

    private MaxpWriter m_maxp;

    private NameWriter m_name;

    private PostWriter m_post;

    private OS2Writer m_os2;

    protected RandomAccessFile m_file;

    private ArrayList<FontFormatWriter> m_tables = new ArrayList<>();

    public FontFileWriter(RandomAccessFile a_file) {
        super();

        m_file = a_file;

        m_loca = new LocaWriter();
        m_maxp = new MaxpWriter();
        m_head = new HeadWriter();
        m_hdmx = new HdmxWriter();
        m_os2 = new OS2Writer(m_head);
        m_cmap = new CmapWriter(m_os2);

        m_glyf = new GlyfWriter(m_loca, m_maxp,
                m_head, m_hdmx);

        m_hhea = new HheaWriter(m_glyf, m_head);
        m_hmtx = new HmtxWriter(m_glyf, m_hhea);
        m_name = new NameWriter();
        m_post = new PostWriter();

        // http://www.microsoft.com/typography/otspec/recom.htm
        // head, hhea, maxp, OS/2, hmtx, LTSH, VDMX, hdmx, cmap,
        // fpgm, prep, cvt, loca, glyf, kern, name, post, gasp, PCLT, DSIG
		/*
         m_tables.add(m_head);
         m_tables.add(m_hhea);
         m_tables.add(m_maxp);
         m_tables.add(m_os2);
         m_tables.add(m_hmtx);
         m_tables.add(m_hdmx);
         m_tables.add(m_cmap);
         m_tables.add(m_loca);
         m_tables.add(m_glyf);
         m_tables.add(m_name);
         m_tables.add(m_post);
         */
        // Verdana has head, hhea, maxp, OS/2, gasp, name, cmap, loca
        // LTSH, VDMX, prep, fpgm, cvt, hmtx, hdmx, glyf, post, kern, edt0, DSIG
        m_tables.add(m_head);
        m_tables.add(m_hhea);
        m_tables.add(m_maxp);
        m_tables.add(m_os2);
        m_tables.add(m_name);
        m_tables.add(m_cmap);
        m_tables.add(m_loca);

        m_tables.add(m_hmtx);
        m_tables.add(m_hdmx);
        m_tables.add(m_glyf);
        m_tables.add(m_post);
    }

    /**
     * write TrueType file to the random access file
     */
    public void write() throws IOException {
        m_cmap.write();
        // hmtx must be written before hhea
        m_hmtx.write();
        m_hhea.write();

        m_glyf.write();
        m_loca.write();

        m_head.setCheckSumAdjustment(0);
        m_head.write();
        m_maxp.write(); // must be written after m_glyf
        m_hdmx.write();
        m_name.write();
        m_post.write();
        m_os2.write();

        writeTableDirectory();
        byte[] tableDir = toByteArray();
        for (FontFormatWriter table : m_tables) {
            m_buffer.write(table.toByteArray());
        } // for table

        long checkSum = 0xb1b0afba - (0xffffffff & getCheckSum());
        m_head.setCheckSumAdjustment(checkSum);
        m_head.reset();
        m_head.write();

        reset();

        m_buffer.write(tableDir);
        for (FontFormatWriter table : m_tables) {
            m_buffer.write(table.toByteArray());
        } // for table

        m_file.write(toByteArray());
        m_file.close();
    }

    public void setAscent(int a_value) {
        m_os2.setTypoAscender(a_value);
        m_os2.setCapHeight(a_value);
    }

    public void setDescent(int a_value) {
        m_os2.setTypoDescender(-a_value);
    }

    public void setXHeight(int a_value) {
        m_os2.setXHeight(a_value);
    }

    public void setLineGap(int a_value) {
        m_os2.setTypoLineGap(a_value);
        m_hhea.setLineGap(a_value);
    }

    public void setFontFamilyName(String a_name) {
        m_name.m_familyName = a_name;
    }

    public void setCopyrightYear(String a_year) {
        m_name.m_year = a_year;
    }

    public void setManufacturer(String a_manufacturer) {
        m_name.m_manufacturer = a_manufacturer;
    }

    public void setFontVersion(String a_version) {
        m_name.m_version = a_version;
    }

    public void addUnicodeRange(TTUnicodeRange a_range) {
        m_cmap.addUnicodeRange(a_range);
    }

    /**
     * http://www.microsoft.com/typography/otspec/os2.htm
     *
     * @param a_codeRange position of the bit. For example, JIS will be 17.
     */
    public void setCodeRangeFlag(int a_codeRange) {
        m_os2.setCodePageRangeFlag(a_codeRange);
    }

    /**
     * adds glyph to the 'glyf' subtable.
     *
     * @param a_glyph the glyph to be added.
     * @return 'glyf' index of the added glyph.
     */
    public int addGlyph(TTGlyph a_glyph) {
        return m_glyf.add(a_glyph);
    }

    public TTGlyph getGlyph(int a_index) {
        return m_glyf.getGlyph(a_index);
    }
    
    public void setCreationDate(Date a_date) {
        m_head.setCreationDate(a_date);
    }

    public void setModificationDate(Date a_date) {
        m_head.setModificationDate(a_date);
    }

    /**
     * adds character mapping to
     *
     * @param a_unicode unicode of the character
     * @param a_glyfIndex 'glyf' index obtained from #addGlyph
     */
    public void addCharacterMapping(long a_unicode, long a_glyfIndex) {
        m_cmap.addMapping(a_unicode, a_glyfIndex);
    }

    public long getCharacterMapping(long a_unicode) {
        return m_cmap.getGlyfIndex(new Long(a_unicode));
    }

    /**
     * writes table directory.
     *
     * @throws IOException
     */
    private void writeTableDirectory() throws IOException {
        int headerLength = m_tables.size() * 16 + 16;
        int tableOffset = headerLength;
        for (FontFormatWriter table : m_tables) {
            table.setOffset(tableOffset);
            tableOffset += table.size();
        } // for table

        @SuppressWarnings("unchecked")
        ArrayList<FontFormatWriter> tables = (ArrayList<FontFormatWriter>) m_tables.clone();
        Collections.sort(tables, new Comparator<FontFormatWriter>() {
            public int compare(FontFormatWriter a_lhs, FontFormatWriter a_rhs) {
                return a_lhs.getTag().compareTo(a_rhs.getTag());
            }

            public boolean equals(Object a_value) {
                return false;
            }
        });

        writeFixed32(1.0);

        int numOfTables = tables.size();
        writeUInt16(numOfTables);
        int searchRange = getSearchRange(numOfTables);
        writeUInt16(searchRange);
        int entrySelector = getEntrySelector(numOfTables);
        writeUInt16(entrySelector);
        writeUInt16(numOfTables * 16 - searchRange);

        for (FontFormatWriter table : tables) {
            writeTag(table.getTag());
            writeUInt32(table.getCheckSum());
            writeUInt32(table.getOffset());
            writeUInt32(table.size());
        } // for

        // padding is always 4 zeros
        for (int i = 0; i < 4; i++) {
            writeUInt8(0);
        }
    }

    private int getSearchRange(int a_value) {
        int retval
                = (int) (Math.pow(2, Math.floor(Math.log(a_value) / Math.log(2))));
        return 16 * retval;
    }

    private int getEntrySelector(int a_value) {
        int retval
                = (int) Math.floor(Math.log(a_value) / Math.log(2));
        return retval;
    }
}
