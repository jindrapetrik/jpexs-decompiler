/*
 * Fontastic A font file writer to create TTF
 * http://code.andreaskoller.com/libraries/fontastic
 *
 * Copyright (C) 2013 Andreas Koller http://andreaskoller.com
 *
 * Uses: doubletype http://sourceforge.net/projects/doubletype/ for TTF creation
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * @author Andreas Koller http://andreaskoller.com
 * @modified 03/16/2014 JPEXS - removed Woff
 * @version 0.4 (4)
 */
package fontastic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.doubletype.ossa.Engine;
import org.doubletype.ossa.OutOfRangeException;
import org.doubletype.ossa.adapter.EContour;
import org.doubletype.ossa.adapter.EContourPoint;
import org.doubletype.ossa.module.GlyphFile;
import org.doubletype.ossa.module.TypefaceFile;

/**
 * Fontastic A font file writer to create TTF and WOFF (Webfonts).
 * http://code.andreaskoller.com/libraries/fontastic
 *
 */
public class Fontastic {

    private org.doubletype.ossa.Engine m_engine;

    private String fontName;

    private List<FGlyph> glyphs;

    private int advanceWidth = 512;

    private File ttfFile;

    private File outFile;

    private File tempDir;

    /**
     * Constructor
     *
     * @example Fontastic f = new Fontastic(this, "MyFont");
     *
     * @param fontName Font name
     * @param outFile
     *
     */
    public Fontastic(String fontName, File outFile) throws IOException {
        this.fontName = fontName;
        this.outFile = outFile;
        intitialiseFont();
        this.glyphs = new ArrayList<>();
    }

    /**
     * Returns the font name.
     *
     * @return String
     */
    public String getFontname() {
        return fontName;
    }

    private static File createTempDirectory()
            throws IOException {
        File temp;
        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdirs())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    /**
     * Creates and initialises a new typeface. Font data is put into sketch
     * folder data/fontname.
     */
    private void intitialiseFont() throws IOException {

        tempDir = createTempDirectory();

        m_engine = new Engine();
        m_engine.buildNewTypeface(fontName, tempDir);

        this.setFontFamilyName(fontName);
        this.setVersion("CC BY-SA 3.0 http://creativecommons.org/licenses/by-sa/3.0/"); // default
        // license

        String directoryName = tempDir + File.separator;

        ttfFile = new File(directoryName + fontName + ".ttf");
    }

    /**
     * Builds the font and writes the .ttf and the .woff file as well as a HTML
     * template for previewing the WOFF. If debug is set (default is true) then
     * you'll see the .ttf and .woff file name in the console.
     */
    public void buildFont() {
        // Create TTF file with doubletype
        //m_engine.fireAction();
        //m_engine.addDefaultGlyphs();

        for (FGlyph glyph : glyphs) {

            m_engine.checkUnicodeBlock(glyph.getGlyphChar());
            GlyphFile glyphFile = m_engine.addNewGlyph(glyph.getGlyphChar());
            glyphFile.setAdvanceWidth(glyph.getAdvanceWidth());

            for (FContour contour : glyph.getContours()) {

                EContour econtour = new EContour();
                econtour.setType(EContour.k_quadratic);

                for (FPoint point : contour.points) {

                    EContourPoint e = new EContourPoint(point.x, point.y, true);
                    if (point.hasControlPoint1()) {

                        econtour.addContourPoint(new EContourPoint(point.controlPoint.x, point.controlPoint.y, false));
                        /*EControlPoint cp1 = new EControlPoint(true,
                         point.controlPoint1.x, point.controlPoint1.y);
                         e.setControlPoint1(cp1);
                         */
                    }
                    /*
                     if (point.hasControlPoint2()) {
                     EControlPoint cp2 = new EControlPoint(false,
                     point.controlPoint2.x, point.controlPoint2.y);
                     e.setControlPoint2(cp2);
                     }                      */

                    econtour.addContourPoint(e);
                }

                glyphFile.addContour(econtour);
            }
        }

        m_engine.getTypeface().addRequiredGlyphs();
        m_engine.buildTrueType();

        // End TTF creation
        if (outFile.exists()) {
            outFile.delete();
        }
        ttfFile.renameTo(outFile);
        cleanup();
    }

    /**
     * Deletes all the glyph files created by doubletype in your data/fontname
     * folder.
     */
    private void cleanup() {
        File[] filesToExclude = new File[0];

        deleteFolderContents(tempDir, true, filesToExclude);

    }

    /**
     * Sets the author of the font.
     */
    public void setAuthor(String author) {
        m_engine.setAuthor(author);
    }

    /**
     * Sets the copyright year of the font.
     */
    public void setCopyrightYear(String copyrightYear) {
        m_engine.setCopyrightYear(copyrightYear);
    }

    public void setCreationDate(Date date) {
        m_engine.setCreationDate(date);
    }

    public void setModificationDate(Date date) {
        m_engine.setModificationDate(date);
    }

    /**
     * Sets the version of the font (default is "0.1").
     */
    public void setVersion(String version) {
        m_engine.getTypeface().setVersion(version);
    }

    /**
     * Sets the font family name of the font. Also called in the constructor. If
     * changed with setFontFamilyName() it won't affect folder the font is
     * stored in.
     */
    public void setFontFamilyName(String fontFamilyName) {
        m_engine.setFontFamilyName(fontFamilyName);
    }

    /**
     * Sets the license of the font (default is "CC BY-SA 3.0
     * http://creativecommons.org/licenses/by-sa/3.0/")
     */
    public void setTypefaceLicense(String typefaceLicense) {
        m_engine.setTypefaceLicense(typefaceLicense);
    }

    /**
     * Sets the baseline of the font.
     */
    public void setBaseline(float baseline) {
        m_engine.setBaseline(baseline);
    }

    /**
     * Sets the meanline of the font.
     */
    public void setMeanline(float meanline) {
        m_engine.setMeanline(meanline);
    }

    /**
     * Sets the advanceWidth of the font. Can be changed for every glyph
     * individually. Won't affect already created glyphs.
     */
    public void setAdvanceWidth(int advanceWidth) {
        m_engine.setAdvanceWidth(advanceWidth);
        this.advanceWidth = advanceWidth;
    }

    public void setTopSideBearing(float topSideBearing) {
        try {
            m_engine.getTypeface().setTopSideBearing(topSideBearing);
        } catch (OutOfRangeException e) {
            System.out
                    .println("Error while setting aopSideBearing (must be within range "
                            + m_engine.getTypeface().getEm());
            e.printStackTrace();
        }
    }

    public void setBottomSideBearing(float bottomSideBearing) {
        try {
            m_engine.getTypeface().setBottomSideBearing(bottomSideBearing);
        } catch (OutOfRangeException e) {
            System.out
                    .println("Error while setting bottomSideBearing (must be within range "
                            + m_engine.getTypeface().getEm());
            e.printStackTrace();
        }
    }

    public void setAscender(float ascender) {
        try {
            m_engine.getTypeface().setAscender(ascender);
        } catch (OutOfRangeException e) {
            System.out
                    .println("Error while setting ascender (must be within range 0 to "
                            + m_engine.getTypeface().getEm() + ")");
            e.printStackTrace();
        }
    }

    public void setDescender(float descender) {
        try {
            m_engine.getTypeface().setDescender(descender);
        } catch (OutOfRangeException e) {
            System.out
                    .println("Error while setting descender (must be within range 0 to "
                            + m_engine.getTypeface().getEm() + ")");
            e.printStackTrace();
        }
    }

    public void setXHeight(float xHeight) {
        try {
            m_engine.getTypeface().setXHeight(xHeight);
        } catch (OutOfRangeException e) {
            System.out
                    .println("Error while setting xHeight (must be within range 0 to "
                            + m_engine.getTypeface().getEm()
                            + " as well as lower than the ascender "
                            + m_engine.getTypeface().getAscender() + ")");
            e.printStackTrace();
        }
    }

    /**
     * Sets the default metrics for the typeface: setTopSideBearing(170); // 2
     * px setAscender(683); // 8 px setXHeight(424); // 5 px setDescender(171);
     * // 2 px setBottomSideBearing(0); // 0px
     *
     */
    public void setDefaultMetrics() {
        m_engine.getTypeface().setDefaultMetrics();
    }

    /**
     * Add a glyph
     *
     * @param c Character of the glyph.
     *
     * @return FGlyph that has been created.
     *
     */
    public FGlyph addGlyph(char c) {

        FGlyph glyph = new FGlyph(c);
        glyph.setAdvanceWidth(advanceWidth);
        glyphs.add(glyph);
        return glyph;

    }

    /**
     * Add a glyph and its one contour
     *
     * @param c Character of the glyph.
     *
     * @param contour Shape of the glyph as FContour.
     *
     * @return The glyph FGlyph that has been created. You can use this to store
     * the glyph and add contours afterwards. Alternatively, you can call
     * getGlyph(char c) to retrieve it.
     */
    public FGlyph addGlyph(char c, FContour contour) {

        FGlyph glyph = new FGlyph(c);
        glyphs.add(glyph);
        glyph.addContour(contour);

        glyph.setAdvanceWidth(advanceWidth);
        return glyph;

    }

    /**
     * Add a glyph and its contours
     *
     * @param c Character of the glyph.
     *
     * @param contours Shape of the glyph in an array of FContour.
     *
     * @return The FGlyph that has been created. You can use this to store the
     * glyph and add contours afterwards. Alternatively, you can call
     * getGlyph(char c) to retrieve it.
     */
    public FGlyph addGlyph(char c, FContour[] contours) {

        FGlyph glyph = new FGlyph(c);
        glyphs.add(glyph);

        for (FContour contour : contours) {
            glyph.addContour(contour);
        }
        glyph.setAdvanceWidth(advanceWidth);
        return glyph;

    }

    /**
     * Get glyph by character
     *
     * @param c The character of the glyph
     *
     * @return The glyph
     */
    public FGlyph getGlyph(char c) {

        FGlyph glyph = null;
        for (int i = 0; i < glyphs.size(); i++) {
            if (glyphs.get(i).getGlyphChar() == c) {
                glyph = glyphs.get(i);
                break;
            }
        }
        return glyph;

    }

    /**
     * Engine getter
     *
     * @return The doubletype Engine used for font creation, so that you can
     * access all functions of doubletype in case you need them.
     */
    public Engine getEngine() {
        return m_engine;
    }

    /**
     * Returns the TypefaceFile
     *
     * @return The doubletype TypefaceFile used for font creation, so that you
     * can access functions of doubletype in case you need them.
     */
    public TypefaceFile getTypefaceFile() {
        return m_engine.getTypeface();
    }

    /**
     * Returns the .ttf file name
     *
     * @return The .ttf file name, which is being created when you call build()
     */
    public String getTTFfilename() {
        return ttfFile.toString();
    }

    private static void deleteFolderContents(File folder,
            boolean deleteFolderItself) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolderContents(f, true);
                    f.delete();
                } else {
                    f.delete();
                }
            }
        }
        if (deleteFolderItself) {
            folder.delete();
        }
    }

    private static void deleteFolderContents(File folder,
            boolean deleteFolderItself, File[] exceptions) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                boolean deleteFile = true;
                for (File exceptfile : exceptions) {
                    if (f.equals(exceptfile)) {
                        deleteFile = false;
                    }
                }
                if (deleteFile) {
                    if (f.isDirectory()) {
                        deleteFolderContents(f, true, exceptions);
                        f.delete();
                    } else {
                        f.delete();
                    }
                }
            }
        }
        if (deleteFolderItself) {
            folder.delete();
        }
    }
}
