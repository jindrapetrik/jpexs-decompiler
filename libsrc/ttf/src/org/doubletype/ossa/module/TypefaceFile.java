/*
 * $Copyright: copyright (c) 2003-2008, e.e d3si9n $
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

import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.doubletype.ossa.OutOfRangeException;
import org.doubletype.ossa.truetype.FontFileWriter;
import org.doubletype.ossa.truetype.TTCodePage;
import org.doubletype.ossa.truetype.TTGlyph;
import org.doubletype.ossa.truetype.TTUnicodeRange;

/**
 * @author e.e
 */
public class TypefaceFile extends GlyphFile {

    private final double k_defaultTopSideBearing = 170; // 2 px

    private final double k_defaultAscender = 683; // 8 px

    private final double k_defaultXHeight = 424; // 5 px

    private final double k_defaultDescender = 171; // 2 px

    private final double k_defaultBottomSideBearing = 0; // 0 px

    private final double k_em = 1024;

    private final int k_defaultAdvanceWidth = 512;

    private final String k_dotTtf = ".ttf";

    private File m_dir;

    private File m_ttfFile;

    private Font m_font = null;

    private List<GlyphFile> m_glyphFiles = new ArrayList<>();

    private List<String> m_unicodeRanges = new ArrayList<>();

    private List<String> m_codePages = new ArrayList<>();

    private String m_fontFamilyName;

    private String m_version;

    private Double m_topSideBearing = null;

    private Double m_ascender = null;

    private Double m_xHeight = null;

    private Double m_descender = null;

    private Double m_bottomSideBearing = null;

    private String m_name;

    public TypefaceFile(String a_name, File a_dir) throws FileNotFoundException {
        super(a_dir);

        m_dir = a_dir;
        m_name = a_name;
        initFileName();
    }

    public TypefaceFile(File a_file) {
        super(a_file);

        m_dir = a_file.getParentFile();
        m_name = a_file.getName();

        initFileName();
    }

    private void initFileName() {

        String fileName = m_name + k_dotTtf;
        m_ttfFile = new File(m_dir, fileName);
    }

    public GlyphFile createGlyph(long a_unicode) {
        return new GlyphFile(getGlyphPath(), a_unicode);
    }

    private GlyphFile getGlyphFileByUnicode(long code) {
        for (GlyphFile glyphFile : m_glyphFiles) {
            if (glyphFile.getUnicode() == code) {
                return glyphFile;
            }
        }

        return null;
    }

    public boolean addRequiredGlyphs() {
        boolean retval = false;

        if (getGlyphFileByUnicode(TTUnicodeRange.k_notDef) == null) {
            GlyphFile glyph = new GlyphFile(getGlyphPath(), TTUnicodeRange.k_notDef);
            glyph.initNotDef(k_defaultAdvanceWidth);
            addGlyph(0, glyph);
            retval = true;
        }

        if (getGlyphFileByUnicode(TTUnicodeRange.k_null) == null) {
            GlyphFile glyph = new GlyphFile(getGlyphPath(), TTUnicodeRange.k_null);
            glyph.initNullGlyph();
            addGlyph(1, glyph);
            retval = true;
        }

        if (getGlyphFileByUnicode(TTUnicodeRange.k_cr) == null) {
            GlyphFile glyph = new GlyphFile(getGlyphPath(), TTUnicodeRange.k_cr);
            glyph.initSpace(k_defaultAdvanceWidth);
            addGlyph(2, glyph);
            retval = true;
        }

        if (getGlyphFileByUnicode(TTUnicodeRange.k_space) == null) {
            GlyphFile glyph = new GlyphFile(getGlyphPath(), TTUnicodeRange.k_space);
            glyph.initSpace(k_defaultAdvanceWidth);
            addGlyph(3, glyph);
            retval = true;
        }

        return retval;
    }

    public void addBasicLatinGlyphs() {
        String basicLatin = Character.UnicodeBlock.BASIC_LATIN.toString();
        TTUnicodeRange.find(basicLatin);
        TTUnicodeRange range = TTUnicodeRange.getLastFound();
        addUnicodeRange(basicLatin);
        for (long i = range.getStartCode(); i <= range.getEndCode(); i++) {
            if (i != 0x0020) {
                addGlyph(createGlyph(i));
            }
        }
    }

    public File getGlyphPath() {
        return m_dir;
    }

    /**
     * change glyph's unicode mapping.
     *
     * @param a_glyphFile
     * @param a_unicode
     */
    public void setGlyphUnicode(GlyphFile a_glyphFile, long a_unicode) {
        a_glyphFile.setUnicode(a_unicode);
    }

    public void addGlyph(GlyphFile a_file) {
        m_glyphFiles.add(a_file);
    }

    public void addGlyph(int a_index, GlyphFile a_file) {
        m_glyphFiles.add(a_index, a_file);
    }

    public Object[] getCodePages() {
        int i;
        Object[] retval;
        retval = new Object[0];

        return retval;
    }

    public boolean containsUnicodeRange(String a_unicodeRange) {
        return m_unicodeRanges.contains(a_unicodeRange);
    }

    public void addUnicodeRange(String a_unicodeRange) {
        if (containsUnicodeRange(a_unicodeRange)) {
            return;
        }

        m_unicodeRanges.add(a_unicodeRange);
    }

    public boolean containsCodePage(String a_codePage) {
        return m_codePages.contains(a_codePage);
    }

    public void addCodePage(String a_codePage) {
        if (containsCodePage(a_codePage)) {
            return;
        }

        m_codePages.add(a_codePage);
    }

    public void removeCodePage(String a_codePage) {
        if (!containsCodePage(a_codePage)) {
            return;
        }

        m_codePages.remove(a_codePage);
    }

    public void setFontFamilyName(String a_value) {
        m_fontFamilyName = a_value;
    }

    public String getFontFamilyName() {
        return m_fontFamilyName;
    }

    public String getVersion() {
        return m_version == null ? "0.1" : m_version;
    }

    public void setVersion(String a_value) {
        m_version = a_value;
    }

    public void setDefaultMetrics() {
        m_topSideBearing = k_defaultTopSideBearing;
        m_ascender = k_defaultAscender;
        m_xHeight = k_defaultXHeight;
        m_descender = k_defaultDescender;
        m_bottomSideBearing = k_defaultBottomSideBearing;
    }

    public double getEm() {
        return k_em;
    }

    public double getBaseline() {
        return getBottomSideBearing() + getDescender();
    }

    public double getMeanline() {
        return getBottomSideBearing()
                + getDescender() + getXHeight();
    }

    public double getBodyBottom() {
        return getBottomSideBearing();
    }

    public double getBodyTop() {
        return getEm() - getTopSideBearing();
    }

    public double getTopSideBearing() {
        if (m_topSideBearing == null) {
            setDefaultMetrics();
        }

        return m_topSideBearing;
    }

    public double getAscender() {
        if (m_ascender == null) {
            setDefaultMetrics();
        }

        return m_ascender;
    }

    public double getXHeight() {
        if (m_xHeight == null) {
            setDefaultMetrics();
        }

        return m_xHeight;
    }

    public double getDescender() {
        if (m_descender == null) {
            setDefaultMetrics();
        }

        return m_descender;
    }

    public double getBottomSideBearing() {
        if (m_bottomSideBearing == null) {
            setDefaultMetrics();
        }

        return m_bottomSideBearing;
    }

    public void setTopSideBearing(double a_value) throws OutOfRangeException {
        checkBoundary(a_value);
        m_topSideBearing = a_value;
    }

    private void checkBoundary(double a_value) throws OutOfRangeException {
        if (a_value > k_em || a_value < 0) {
            throw new OutOfRangeException(a_value);
        }
    }

    public void setAscender(double a_value) throws OutOfRangeException {
        checkBoundary(a_value);
        m_ascender = a_value;
    }

    public void setXHeight(double a_value) throws OutOfRangeException {
        checkBoundary(a_value);
        if (a_value > getAscender()) {
            throw new OutOfRangeException(a_value);
        }

        m_xHeight = a_value;
    }

    public void setDescender(double a_value) throws OutOfRangeException {
        checkBoundary(a_value);
        m_descender = a_value;
    }

    public void setBottomSideBearing(double a_value) throws OutOfRangeException {
        checkBoundary(a_value);
        m_bottomSideBearing = a_value;
    }

    public double getBodyHeight() {
        return k_em - getTopSideBearing() - getBottomSideBearing();
    }

    // --------------------------------------------------------------------
    /**
     * Calls FontFileWriter to produce TrueType font file.
     */
    public void buildTTF() throws Exception {
        String randomString = UUID.randomUUID().toString().substring(0, 4);

        File tempFile = new File(m_dir,
                m_name + "_" + randomString + k_dotTtf);
        File target;
        String fontFamilyName;

        target = m_ttfFile;
        fontFamilyName = getFontFamilyName();

        target.delete();
        FontFileWriter writer;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(target, "rw")) {
            writer = new FontFileWriter(randomAccessFile);

            writer.setFontFamilyName(fontFamilyName);
            writer.setCopyrightYear(getCopyrightYear());
            writer.setCreationDate(getCreationDate());
            writer.setModificationDate(getModificationDate());
            writer.setFontVersion(getVersion());
            writer.setManufacturer(getAuthor());
            writer.setAscent((int) getAscender());
            writer.setXHeight((int) getXHeight());
            writer.setDescent((int) getDescender());
            writer.setLineGap((int) (getTopSideBearing() + getBottomSideBearing()));

            loadCodePages(writer);
            loadUnicodeRanges(writer);
            loadGlyphs(writer);
            writer.write();
        }
        if (target.exists()) {
            copyFile(target, tempFile);
        }

        FileInputStream in = new FileInputStream(tempFile);
        m_font = Font.createFont(Font.TRUETYPE_FONT,
                (InputStream) in);
        in.close();
    }

    private void copyFile(File a_in, File a_out) throws Exception {
        FileInputStream in = new FileInputStream(a_in);
        FileOutputStream out = new FileOutputStream(a_out);
        byte[] buffer = new byte[1024];
        int i = 0;
        while ((i = in.read(buffer)) != -1) {
            out.write(buffer, 0, i);
        } // while

        in.close();
        out.close();
    }

    public Font getFont() {
        return m_font;
    }

    private void loadCodePages(FontFileWriter a_writer) {
        for (String codePageName : m_codePages) {
            TTCodePage codePage = TTCodePage.forName(codePageName);
            if (codePage == null) {
                continue;
            }

            a_writer.setCodeRangeFlag(codePage.getOsTwoFlag());
        } // for codePageName
    }

    private void loadUnicodeRanges(FontFileWriter a_writer) {
        for (String unicodeRange : m_unicodeRanges) {
            if (!TTUnicodeRange.find(unicodeRange)) {
                continue;
            }

            a_writer.addUnicodeRange(TTUnicodeRange.getLastFound());
        }
    }

    private void loadGlyphs(FontFileWriter a_writer) throws Exception {
        for (GlyphFile glyphFile : m_glyphFiles) {
            loadGlyph(glyphFile, a_writer);
        }
    }

    /**
     * load the glyph into FontFileWriter.
     *
     * @param a_fileName
     * @param a_writer
     * @throws Exception
     */
    private void loadGlyph(GlyphFile a_glyphFile, FontFileWriter a_writer) throws Exception {
        TTGlyph glyph = null;

        if (a_glyphFile.isSimple()) {
            // glyph will be null if it is empty
            glyph = a_glyphFile.toSimpleGlyph();
        } else {
            glyph = createCompoundGlyph(a_glyphFile, a_writer);
        }

        if (glyph == null && a_glyphFile.isWhiteSpace()) {
            glyph = new TTGlyph();
        }

        if (glyph == null) {
            return;
        }

        int glyphIndex = a_writer.addGlyph(glyph);
        long unicode = a_glyphFile.getUnicode();

        if (unicode != -1) {
            long existingIndex = a_writer.getCharacterMapping(unicode);
            if (existingIndex != 0) {
                throw new Exception(Long.toHexString(unicode) + " is mapped already.");
            }

            a_writer.addCharacterMapping(unicode, glyphIndex);
        }
    }

    private TTGlyph createCompoundGlyph(GlyphFile a_glyphFile,
            FontFileWriter a_writer) throws Exception {
        TTGlyph retval = new TTGlyph();
        ArrayList<Point> locs = new ArrayList<>();
        ArrayList<Integer> indeces = new ArrayList<>();

        retval.setSimple(false);
        retval.setAdvanceWidth(a_glyphFile.getAdvanceWidth());

        TTGlyph simple = a_glyphFile.toSimpleGlyph();
        if (simple != null) {
            int glyphIndex = a_writer.addGlyph(simple);

            locs.add(new Point(0, 0));
            indeces.add(glyphIndex);
        }

        int i = 0;

        int flag = TTGlyph.ARG_1_AND_2_ARE_WORDS
                | TTGlyph.ARGS_ARE_XY_VALUES
                | TTGlyph.ROUND_XY_TO_GRID;
        int numOfCompositePoints = 0;
        int numOfCompositeContours = 0;
        int componentDepth = 0;

        for (int glyfIndex : indeces) {
            TTGlyph glyph = a_writer.getGlyph(glyfIndex);
            numOfCompositePoints += glyph.getNumOfCompositePoints();
            numOfCompositeContours += glyph.getNumOfCompositeContours();
            if (glyph.getComponentDepth() > componentDepth) {
                componentDepth = glyph.getComponentDepth();
            }

            retval.addGlyfIndex(glyfIndex);
            if (i < indeces.size() - 1) {
                retval.addFlag(flag | TTGlyph.MORE_COMPONENTS);
            } else {
                retval.addFlag(flag);
            }

            Point loc = locs.get(i);
            retval.addArg1(loc.x);
            retval.addArg2(loc.y);
        } // for

        retval.setNumOfCompositePoints(numOfCompositePoints);
        retval.setNumOfCompositeContours(numOfCompositeContours);
        retval.setComponentDepth(componentDepth + 1);

        return retval;
    }
}
