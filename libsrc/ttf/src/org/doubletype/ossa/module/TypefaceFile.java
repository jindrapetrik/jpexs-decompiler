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

import java.io.*;
import java.util.*;

import org.doubletype.ossa.*;
import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.adapter.*;
import org.doubletype.ossa.truetype.*;
import java.awt.*;
import java.util.List;

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
	private final String k_dotDtyp = ".dtyp";
	private final String k_dotTtf = ".ttf";
	
	private File m_dir;
	private Hashtable<String, Integer> m_nameToIndeces = new Hashtable<>();
	private File m_ttfFile;
	private Font m_font = null;
	private File m_binFolder;
        private Map<String, GlyphFile> m_glyphFiles = new HashMap<>();
	
	public TypefaceFile(String a_name, File a_dir) throws FileNotFoundException {
		super(TypefaceFile.class.getResource(s_emptyFileName));
		
		m_dir = a_dir;
		setGlyphTitle(a_name);
		
		m_fileName = new File(m_dir, a_name + k_dotDtyp);
		initFileName();
	}
	
	public TypefaceFile(File a_file) {
		super(a_file);
		
		m_dir = a_file.getParentFile();
		m_fileName = a_file;
		
		initFileName();
	}
	
	private void initFileName() {
		m_binFolder = new File(m_dir, "bin");
		if (!m_binFolder.exists()) {
		    m_binFolder.mkdir();
		} // if
	    
	    String fileName = getGlyphTitle() + k_dotTtf;
		m_ttfFile = new File(m_binFolder, fileName);
	}
	
	public GlyphFile createGlyph(long a_unicode) {
		String name = Character.getName((int) a_unicode);
		if (name == null) {
			name = "NAC_" + Long.toHexString(a_unicode);
		} // if
		
		name = name.replace(' ', '_');
		return new GlyphFile(
			getGlyphPath(), name, a_unicode);
	}
	
	public boolean addRequiredGlyphs() {
		boolean retval = false;
		
		if (unicodeToFileName(TTUnicodeRange.k_notDef) == null) {
			GlyphFile glyph = new GlyphFile(getGlyphPath(), "NOTDEF", TTUnicodeRange.k_notDef);
			glyph.initNotDef(k_defaultAdvanceWidth);
			addGlyph(0, glyph);
			retval = true;
		} // if
		
		if (unicodeToFileName(TTUnicodeRange.k_null) == null) {
			GlyphFile glyph = new GlyphFile(getGlyphPath(), "NULL", TTUnicodeRange.k_null);
			glyph.initNullGlyph();
			addGlyph(1, glyph);
			retval = true;
		} // if
		
		if (unicodeToFileName(TTUnicodeRange.k_cr) == null) {
			GlyphFile glyph = new GlyphFile(getGlyphPath(), "CR", TTUnicodeRange.k_cr);
			glyph.initSpace(k_defaultAdvanceWidth);
			addGlyph(2, glyph);
			retval = true;
		} // if
		
		if (unicodeToFileName(TTUnicodeRange.k_space) == null) {
			GlyphFile glyph = new GlyphFile(getGlyphPath(), "SPACE", TTUnicodeRange.k_space);
			glyph.initSpace(k_defaultAdvanceWidth);
			addGlyph(3, glyph);
			retval = true;
		} // if
		
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
			} // if
		} // for i
	}
	
	public File getGlyphPath() {
		return m_dir;
	}
	
	public String unicodeToFileName(long a_unicode) {		
		for (XGlyphFile glyphFile: m_glyph.getBody().getGlyphFile()) {	
			if (glyphFile.getUnicode() == a_unicode) {
				return glyphFile.getHref();
			} // if
		} // for i
		
		return null;
	}
	
	/**
	 * change glyph's unicode mapping.
	 * @param a_glyphFile
	 * @param a_unicode
	 */
	public void setGlyphUnicode(GlyphFile a_glyphFile, long a_unicode) {
		int i;
		XGlyphFile [] glyphFiles = m_glyph.getBody().getGlyphFile();
		for (i = 0; i < glyphFiles.length; i++) {
			XGlyphFile glyphFile = glyphFiles[i];
						
			if (glyphFile.getHref().equals(
				a_glyphFile.getShortFileName())) {
				continue;
			} // if
			
			glyphFile.setUnicode(a_unicode);
			a_glyphFile.setUnicode(Long.toHexString(a_unicode));
			
			return;
		} // for i
	}
	
	public void addGlyph(GlyphFile a_file) {		
                String shortFileName = a_file.getShortFileName();
                m_glyphFiles.put(shortFileName, a_file);
		XGlyphFile xglyphFile = new XGlyphFile();
		xglyphFile.setHref(shortFileName);
		xglyphFile.setUnicode(a_file.getUnicodeAsLong());
		m_glyph.getBody().addGlyphFile(xglyphFile);
	}
	
	public void addGlyph(int a_index, GlyphFile a_file) {
                String shortFileName = a_file.getShortFileName();
                m_glyphFiles.put(shortFileName, a_file);
		XGlyphFile xglyphFile = new XGlyphFile();
		xglyphFile.setHref(shortFileName);
		xglyphFile.setUnicode(a_file.getUnicodeAsLong());
		m_glyph.getBody().addGlyphFile(a_index, xglyphFile);
	}
	
	public void removeGlyph(String a_fileName) {		
		for (XGlyphFile file: m_glyph.getBody().getGlyphFile()) {
			if (file.getHref().equals(a_fileName)) {
				m_glyph.getBody().removeGlyphFile(file);
				return;
			} // if
		}
	}
	
	public ArrayList<String> getChildFileNames() {
		ArrayList<String> retval = new ArrayList<>();
		XGlyphFile [] files = m_glyph.getBody().getGlyphFile();
		
		for (int i = 0; i < files.length; i++) {
			XGlyphFile file = files[i];
			retval.add(file.getHref());
		} // for i
		
		return retval;
	}
	
	public Object [] getCodePages() {
		int i;
		Object [] retval;
		String [] codePages = m_glyph.getHead().getCodePage();
		retval = new Object[codePages.length];
		for (i = 0; i < codePages.length; i++) {
			retval[i] = codePages[i];
		} // for i
		
		return retval;
	}
	
	public boolean containsUnicodeRange(String a_unicodeRange) {
		int i;
		String [] unicodeRanges = m_glyph.getHead().getUnicodeRange();
		
		for (i = 0; i < unicodeRanges.length; i++) {
			if (unicodeRanges[i].equals(a_unicodeRange)) {
				return true;
			} // if
		} // for i
		
		return false;
	}
	
	public void addUnicodeRange(String a_unicodeRange) {
		if (containsUnicodeRange(a_unicodeRange)) {
			return;
		} // if
		
		m_glyph.getHead().addUnicodeRange(a_unicodeRange);
	}
	
	public boolean containsCodePage(String a_codePage) {
		int i;
		String [] codePages = m_glyph.getHead().getCodePage();
		
		for (i = 0; i < codePages.length; i++) {
			if (codePages[i].equals(a_codePage)) {
				return true;
			} // if
		} // for i
		
		return false;
	}
	
	public void addCodePage(String a_codePage) {
		if (containsCodePage(a_codePage)) {
			return;
		} // if
		
		m_glyph.getHead().addCodePage(a_codePage);
	}
	
	public void removeCodePage(String a_codePage) {
		if (!containsCodePage(a_codePage)) {
			return;
		} // if
		
		m_glyph.getHead().removeCodePage(a_codePage);
	}
	
	public void setFontFamilyName(String a_value) {
		m_glyph.getHead().setFontFamily(a_value);
	}
	
	public String getFontFamilyName() {
		return m_glyph.getHead().getFontFamily();
	}
	
	public String getVersion() {
		if (m_glyph.getHead().getVersion() == null) {
			m_glyph.getHead().setVersion("0.1");
		} // if
		
		return m_glyph.getHead().getVersion();
	}
	
	public void setSubFamily(String a_value) {
		m_glyph.getHead().setFontSubFamily(a_value);
	}
	
	public void setDefaultMetrics() {
		XHead head = m_glyph.getHead();
		head.setTopSideBearing(k_defaultTopSideBearing);
		head.setAscender(k_defaultAscender);
		head.setXHeight(k_defaultXHeight);
		head.setDescender(k_defaultDescender);
		head.setBottomSideBearing(k_defaultBottomSideBearing);
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
		if (!m_glyph.getHead().checkTopSideBearing()) {
			setDefaultMetrics();
		} // if
		
		return m_glyph.getHead().getTopSideBearing();
	}
	
	public double getAscender() {
		if (!m_glyph.getHead().checkAscender()) {
			setDefaultMetrics();
		} // if
		
		return m_glyph.getHead().getAscender();
	}
	
	public double getXHeight() {
		if (!m_glyph.getHead().checkXHeight()) {
			setDefaultMetrics();
		} // if
		
		return m_glyph.getHead().getXHeight();
	}
	
	public double getDescender() {
		if (!m_glyph.getHead().checkDescender()) {
			setDefaultMetrics();		
		} // if
		
		return m_glyph.getHead().getDescender();
	}
	
	public double getBottomSideBearing() {
		if (!m_glyph.getHead().checkBottomSideBearing()) {
			setDefaultMetrics();
		} // if
		
		return m_glyph.getHead().getBottomSideBearing();
	}
	
	public void setTopSideBearing(double a_value) throws OutOfRangeException {
		checkBoundary(a_value);
		m_glyph.getHead().setTopSideBearing(a_value);
	}
	
	private void checkBoundary(double a_value) throws OutOfRangeException  {
		if (a_value > k_em || a_value < 0) {
			throw new OutOfRangeException(a_value);
		} // if
	}
	
	public void setAscender(double a_value) throws OutOfRangeException {
		checkBoundary(a_value);
		m_glyph.getHead().setAscender(a_value);
	}

	public void setXHeight(double a_value) throws OutOfRangeException {
		checkBoundary(a_value);
		if (a_value > getAscender()) {
			throw new OutOfRangeException(a_value);
		} // if
			
		m_glyph.getHead().setXHeight(a_value);
	}
	
	public void setDescender(double a_value) throws OutOfRangeException {
		checkBoundary(a_value);		
		m_glyph.getHead().setDescender(a_value);
	}
	
	public void setBottomSideBearing(double a_value) throws OutOfRangeException {
		checkBoundary(a_value);
		m_glyph.getHead().setBottomSideBearing(a_value);
	}
	
	public double getBodyHeight() {
		return k_em - getTopSideBearing() - getBottomSideBearing();
	}
		
// --------------------------------------------------------------------	
			
	/**
	 * Calls FontFileWriter to produce TrueType font file.
	 */
	public void buildTTF(boolean a_isDebug) throws Exception {		
		String randomString = UUID.randomUUID().toString().substring(0, 4);
		
		File tempFile = new File(m_binFolder, 
			getGlyphTitle() + "_" + randomString + k_dotTtf);
		File target;
		String fontFamilyName;
		
		if (a_isDebug) {
			target = tempFile;
			fontFamilyName = getGlyphTitle() + " " + randomString;
		} else {
			target = m_ttfFile;
			fontFamilyName = getFontFamilyName();
		} // if-else
		
		target.delete();
		FontFileWriter writer;
		ModuleManager.getSingletonInstance().clear();
		m_stack.clear();
		m_stack.push(this);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(target, "rw")) {
            writer = new FontFileWriter(randomAccessFile);
            
            writer.setFontFamilyName(fontFamilyName);
            writer.setCopyrightYear(getCopyrightYear());
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
		if (!a_isDebug && target.exists()) {
			copyFile(target, tempFile);
		} // if
		
		FileInputStream in = new FileInputStream(tempFile);
		m_font = Font.createFont(Font.TRUETYPE_FONT,
				(InputStream) in);
		in.close();
		
		ModuleManager.getSingletonInstance().clear();
		m_stack.pop(); // pop this
	}
	
	private void copyFile(File a_in, File a_out) throws Exception {
		FileInputStream in = new FileInputStream(a_in);
		FileOutputStream out = new FileOutputStream(a_out);
		byte [] buffer = new byte[1024];
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
		for (String codePageName: m_glyph.getHead().getCodePage()) {
			TTCodePage codePage = TTCodePage.forName(codePageName);
			if (codePage == null) {
				continue;
			} // if
			
			a_writer.setCodeRangeFlag(codePage.getOsTwoFlag());	
		} // for codePageName
	}
	
	private void loadUnicodeRanges(FontFileWriter a_writer) {
		String [] unicodeRanges = m_glyph.getHead().getUnicodeRange();
		int i;
		for (i = 0; i < unicodeRanges.length; i++) {
			if (!TTUnicodeRange.find(unicodeRanges[i])) {
				continue;
			} // if
			
			a_writer.addUnicodeRange(TTUnicodeRange.getLastFound());
		} // for i
	}
	
	private void loadGlyphs(FontFileWriter a_writer) throws Exception {
		m_nameToIndeces.clear();
		
		for (String fileName: getChildFileNames()) {
			GlyphFile glyphFile = nameToGlyphFile(fileName);
			loadGlyph(glyphFile, glyphFile, a_writer);
		} // for
	}
	
	private GlyphFile nameToGlyphFile(String a_fileName) throws FileNotFoundException {
		if (!m_glyphFiles.containsKey(a_fileName)) {
			throw new FileNotFoundException(a_fileName);
		} // if
		
		GlyphFile retval = m_glyphFiles.get(a_fileName);
		
		return retval;
	}
	
	/**
	 * load the glyph into FontFileWriter.
	 * @param a_fileName
	 * @param a_writer
	 * @throws Exception
	 */
	private void loadGlyph(GlyphFile a_glyphFile, VarStackFrame a_frame, FontFileWriter a_writer) throws Exception {
		if (m_nameToIndeces.containsKey(a_glyphFile.getShortFileName())) {
			return;
		} // if
		
		/*
		if (a_glyphFile.getUnicodeAsLong() == TTUnicodeRange.k_null) {
			return;
		} // if
		*/
		
		TTGlyph glyph = null;
		m_stack.push(a_frame);
		
		if (a_glyphFile.isSimple()) {
			// glyph will be null if it is empty
			glyph = a_glyphFile.toSimpleGlyph();	
		} else {
			glyph = createCompoundGlyph(a_glyphFile, a_writer);
		} // if
		
		m_stack.pop();
		
		if (glyph == null && a_glyphFile.isWhiteSpace()) {
			glyph = new TTGlyph();
		} // if
		
		if (glyph == null) {
			return;
		} // if
		
		int glyphIndex = a_writer.addGlyph(glyph);
		m_nameToIndeces.put(a_glyphFile.getShortFileName(), glyphIndex);
		long unicode = a_glyphFile.getUnicodeAsLong();
		
		if (unicode != -1 && glyph != null) {
			long existingIndex = a_writer.getCharacterMapping(unicode);
			if (existingIndex != 0) {
				throw new Exception(Long.toHexString(unicode) + " is mapped already.");
			} // if
			
			a_writer.addCharacterMapping(unicode, glyphIndex);
		} // if
	}
	
	private TTGlyph createCompoundGlyph(GlyphFile a_glyphFile, 
				FontFileWriter a_writer)  throws Exception
	{
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
		} // if
		
		XInclude [] includes = a_glyphFile.m_glyph.getBody().getInclude();
		int i;
		for (i = 0; i < includes.length; i++) {
			EIncludeInvoke include = (EIncludeInvoke) includes[i];
			GlyphFile glyphFile = nameToGlyphFile(include.getHref());
			
			// load the glyph included in this one
			loadGlyph(glyphFile, include, a_writer);
			Integer n = m_nameToIndeces.get(glyphFile.getShortFileName());
			if (n == null) {
				continue;
			} // if
			
			indeces.add(n);
			XPoint2d pos = include.getInvoke().getInvokePos().getPoint2d();
			locs.add(new Point((int) pos.getX(), (int) pos.getY()));
		} // for i
		
		int flag = TTGlyph.ARG_1_AND_2_ARE_WORDS
				| TTGlyph.ARGS_ARE_XY_VALUES
				| TTGlyph.ROUND_XY_TO_GRID;
		int numOfCompositePoints = 0;
		int numOfCompositeContours = 0;
		int componentDepth = 0;
		
		for (int glyfIndex: indeces) {
			TTGlyph glyph = a_writer.getGlyph(glyfIndex);
			numOfCompositePoints += glyph.getNumOfCompositePoints();
			numOfCompositeContours += glyph.getNumOfCompositeContours();
			if (glyph.getComponentDepth() > componentDepth) {
				componentDepth = glyph.getComponentDepth();
			} // if
			
			retval.addGlyfIndex(glyfIndex);
			if (i < indeces.size() - 1) {
				retval.addFlag(flag | TTGlyph.MORE_COMPONENTS);
			} else {
				retval.addFlag(flag);
			} // if-else
			
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
