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

import org.doubletype.ossa.*;
import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.adapter.*;
import org.doubletype.ossa.truetype.*;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/**
 * @author e.e
 */
public class GlyphFile extends GlyphModule {
	protected static String s_emptyFileName = "empty.glyph";
	private static final String k_dotGlyph = ".glyph";
	private static Transformer s_transformer = null;
	
	static {
	    GlyphFactory.setFactory(EGlyphFactory.getFactory());
	}
	
	public static File createFileName(File a_dir, String a_name) {
		return new File(a_dir, a_name + k_dotGlyph);
	}
	
	/**
	 * http://www.atmarkit.co.jp/fxml/rensai2/xmltool04/02.html
	 * @return
	 */
	private static Transformer getTransformer() {
		if (s_transformer == null) {
			TransformerFactory transFactory
				= TransformerFactory.newInstance();
			try {
				s_transformer = transFactory.newTransformer();
			}
			catch (TransformerConfigurationException e) {
			}
			
			s_transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			s_transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} // if
		
		return s_transformer;
	}
	
	private static XStartGlyphElement loadGlyphElement(URL a_url) {
		IGlyphFactory factory = GlyphFactory.getFactory();
		
		XStartGlyphElement retval = null;
		
		try {			
			retval = factory.createXStartGlyphElement(a_url);
		} catch( SAXException | ParserConfigurationException | IOException e) {
			e.printStackTrace();
		}
			
		return retval;
	}
	
	private static XStartGlyphElement loadGlyphElement(InputStream a_in) {
		IGlyphFactory factory = GlyphFactory.getFactory();
		XStartGlyphElement retval = null;
			
		try {			
			retval = factory.createXStartGlyphElement(a_in);	
		} catch( SAXException | ParserConfigurationException | IOException e) {
			e.printStackTrace();
		}
				
		return retval;		
	}
	// --------------------------------------------------------------
	
	protected XStartGlyphElement m_glyph;
	protected File m_fileName;
	protected long m_modifiedTime = 0;
	protected long m_savedTime = 0;
	protected HistoryList m_history;
	
	private final int k_halfWidth = 512;
	private final int k_fullWidth = 1024;	
	private String m_selectedNodeName = "";
	private boolean m_isMoving = false;
	private PointAggregate m_pointHost;

	
	// --------------------------------------------------------------
	
	/**
	 * creates new file
	 */
	public GlyphFile(File a_dir, String a_name, long a_unicode) throws FileNotFoundException {
		super();
		
		m_fileName = createFileName(a_dir, a_name);
		
		init(getClass().getResource(s_emptyFileName));
		setGlyphTitle(a_name);
		setUnicode(Long.toHexString(a_unicode));
		
		/*int eastAsianWidth = UCharacter.getIntPropertyValue(
			(int) a_unicode,
			0x1004); //UProperty.EAST_ASIAN_WIDTH);
        */
        int eastAsianWidth = 5; //??
		if (eastAsianWidth == 5 || eastAsianWidth == 1) {
			setAdvanceWidth(k_fullWidth);
		} // if
		
		saveGlyphFile();
	}
	
	/**
	 * creates new file
	 * @param a_dir parent dir
	 * @param a_name glyph name
	 */
	public GlyphFile(File a_dir, String a_name) throws FileNotFoundException {
		super();
		init(getClass().getResource(s_emptyFileName));
		setGlyphTitle(a_name);
		m_fileName = createFileName(a_dir, a_name);
		saveGlyphFile();
	}
	
	/**
	 * opens existing file
	 * @param a_file
	 */
	public GlyphFile(File a_file) {
		super();
		
		m_fileName = a_file;
		
		URL url = null;
		
		try {
			url = a_file.toURI().toURL();	
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
		
		init(url);
	}
	
	protected GlyphFile(URL a_url) {
		super();
		init(a_url);
	}
	
	private void init(URL a_url) {
		m_glyph = loadGlyphElement(a_url);		
		ModuleManager.getSingletonInstance().clear();
		
		//m_display = Engine.getSingletonInstance().getDisplay();
		m_history = new HistoryList(this);
		
		m_history.record("loadFile");
		m_savedTime = m_modifiedTime;
	}
	
	/**
	 * initialize .notdef
	 */
	public void initNotDef(int a_advanceWidth) throws FileNotFoundException {
		setAdvanceWidth(a_advanceWidth);
		
		EContour contour = new EContour();
		contour.setType(EContour.k_cubic);
		contour.addContourPoint(new EContourPoint(0.0, 0.0, true));
		contour.addContourPoint(new EContourPoint(438.0, 0.0, true));
		contour.addContourPoint(new EContourPoint(438.0, 683.0, true));
		contour.addContourPoint(new EContourPoint(0.0, 683.0, true));
		addContour(contour);
		
		contour = new EContour();
		contour.setType(EContour.k_cubic);
		contour.addContourPoint(new EContourPoint(365.0, 73.0, true));
		contour.addContourPoint(new EContourPoint(73.0, 73.0, true));
		contour.addContourPoint(new EContourPoint(73.0, 610.0, true));
		contour.addContourPoint(new EContourPoint(365.0, 610.0, true));
		addContour(contour);
		
		saveGlyphFile();
	}
	
	public void initNullGlyph() throws FileNotFoundException {
		setAdvanceWidth(0);
		saveGlyphFile();
	}
	
	public void initSpace(int a_advanceWidth) throws FileNotFoundException {
		setAdvanceWidth(a_advanceWidth);
		saveGlyphFile();
	}
	
	public void beforePush() {
		loadVar();
	}
	
	public void undo() {
	    m_history.undo();
	}
	
	public void redo() {
	    m_history.redo();
	}
	
	public void restore(Memento a_memento) {
		m_glyph = loadGlyphElement(a_memento.getData());
		ModuleManager.getSingletonInstance().clear();
	}
	
	// --------------------------------------------------------------
	
	public XStartGlyphElement getGlyph() {
		return m_glyph;	
	}
	
	// --------------------------------------------------------------
	
	public void saveGlyphFile() throws FileNotFoundException {	    
	        saveGlyphFile(m_fileName);
	}
	
	protected void saveGlyphFile(File a_file) throws FileNotFoundException {
            FileOutputStream output = new FileOutputStream(a_file);
		saveGlyphFile(output);     
            try {
                output.close(); //JPEXS
            } catch (IOException ex) {                
                Logger.getLogger(GlyphFile.class.getName()).log(Level.SEVERE, null, ex);
            }
	}
	
	public void saveGlyphFile(OutputStream a_output) {
	    try {
		    Transformer transformer = getTransformer();
			Document document = m_glyph.makeDocument();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(a_output);	
			transformer.transform(source, result);
	    } catch (ParserConfigurationException | TransformerException e) {
	        e.printStackTrace();
	    }
		
		m_savedTime = System.currentTimeMillis();
		m_modifiedTime = m_savedTime;		
	}
	
	public Memento createMemento(String a_description) {
		m_modifiedTime = System.currentTimeMillis();
		byte [] bytes = m_glyph.makeTextDocument().getBytes();
		return new Memento(a_description, bytes);
	}
	
	public boolean hasUnsavedChange() {
		return (m_savedTime != m_modifiedTime);
	}
	
	public void setAuthor(String a_value) {
		m_glyph.getHead().setAuthor(a_value);
		m_history.record("setAuthor");
	}
	
	public String getAuthor() {
		return m_glyph.getHead().getAuthor();
	}
	
	public void setCopyrightYear(String a_value) {
		m_glyph.getHead().setCopyright(a_value);
		m_history.record("setCopyrightYear"); 
	}
	
	public String getCopyrightYear() {
		return m_glyph.getHead().getCopyright();
	}
	
	public void setAdvanceWidth(int a_width) {
		m_glyph.getHead().setAdvanceWidth(a_width);
		m_history.record("setAdvanceWidth");
	}
	
	public int getAdvanceWidth() {
		if (!m_glyph.getHead().checkAdvanceWidth()) {
			setAdvanceWidth(k_halfWidth);
		} // if
		
		return (int) m_glyph.getHead().getAdvanceWidth();
	}
	
	public Iterator createIterator() {
		return new GlyphIterator(this);
	}
	
		
	// --------------------------------------------------------------
	
	public void display(Graphics2D g, AffineTransform a_trans)
	{			
		Iterator i = createIterator();
		while (i.hasNext()) {
			GlyphObject object = (GlyphObject) i.next();
			object.display(g, a_trans);
		} // while
	}
	
	// --------------------------------------------------------------
	
        public static final int k_defaultPixelSize = 16; //PPM
        
	/** converts this glyph into Shape.
	 * It could be called for root's preview mode or by include invoke.
	 * Pushing either this GlyphFile or DIncludeInvoke should be
	 * handled before this.
	 */
	public Shape toShape(AffineTransform a_trans) {
            
            
		int ppem = k_defaultPixelSize;
		
		GeneralPath retval = new GeneralPath();
		Iterator i = createIterator();
		while (i.hasNext()) {
			GlyphObject object = (GlyphObject) i.next();
			
			if (object instanceof EContourPoint
			        || object instanceof EHint) {
			    continue;
			} // if
			
			retval.append(object.toShape(a_trans, ppem), false);
		} // if
		
		return retval;
	}
	
	// --------------------------------------------------------------
	
	/**
	 * Generates array of XContour from local contours and modules.
	 * Used for TTF building.
	 */
	private XContour [] toContours() {
		XContour [] retval;
		ArrayList<XContour> list = new ArrayList<>();
		XContour [] contours = m_glyph.getBody().getContour();
		for (int i = 0; i < contours.length; i++) {
			EContour contour = (EContour) contours[i];
			list.add(contour.toQuadratic());
		} // for i
		
		XModule [] modules = m_glyph.getBody().getModule();
		for (int i = 0; i < modules.length; i++) {
			EModuleInvoke module = (EModuleInvoke) modules[i];
			
			// push and pop happens inside toContour
			list.add(module.toContour(new AffineTransform()));
		} // for i
		
		if (list.size() == 0)
			return null;
		
		retval = new XContour[list.size()];	
		for (int i = 0; i < list.size(); i++) {
			retval[i] = list.get(i);
		} // for i
		
		return retval;
	}
	
	// --------------------------------------------------------------
	
	public boolean isMoving() {
	    return m_isMoving;
	}
	
	// --------------------------------------------------------------
	
	public void beginMove() {
		m_isMoving = true;
	}
	
	// --------------------------------------------------------------

	public void endMove() {
		if (!m_isMoving) {
			return;
		} // if
		
		m_isMoving = false;
		
		m_history.record("move");
	}
	
	//	--------------------------------------------------------------
	
	public void move(Point2D a_delta) {
		int i;
		for (i = 0; i < m_actives.size(); i++) {
			GlyphObject active = m_actives.get(i);
			active.move(a_delta);
		} // for i
	}
	
	
	//	--------------------------------------------------------------
	
	public void fitMove() {
		if (m_actives.hasActiveControlPoint()) {
		    EControlPoint controlPoint = m_actives.getActiveControlPoint();
		    controlPoint.rotateTo45();
		} // if
	}

	// --------------------------------------------------------------
	
	public boolean isHittingSelected(Point2D a_point) {
		ActiveList oldActiveObjects = new ActiveList();
		oldActiveObjects.setActives(m_actives);
		
		m_actives.unselectAll();
		hit(a_point, false, false);
		
		int i, j;
		for (i = 0; i < m_actives.size(); i++) {
			GlyphObject selected = m_actives.get(i);
			for (j = 0; j < oldActiveObjects.size(); j++) {
				GlyphObject old = oldActiveObjects.get(j);
				
				if (selected == old) {
					m_actives.setActives(oldActiveObjects);
					return true;
				} // if
			} // for j
		} // for i
		
		m_actives.setActives(oldActiveObjects);
		
		return false;
	}
	
	// --------------------------------------------------------------
	
	/** hits a point to add them in active object queue.
	 * @param a_single when it is true, the method returns after 
	 * finding the first hit. 
	 */
	public boolean hit(Point2D a_point, 
	        boolean a_isAppend, 
	        boolean a_isSelectOnlyOne) {
		if (a_point == null) {
		    return false;
		} // if
	    
	    Rectangle2D rect = new Rectangle2D.Double(
			a_point.getX(), a_point.getY(),
			1.0, 1.0);
		return hit(rect, a_isAppend, a_isSelectOnlyOne);
	}
	
	public void selectNext() {
		Iterator i = new SelectionIterator(this);
		
		if (m_actives.size() == 1) {
		    boolean isFound = false;
		    
			// loop the iterator in reverse order of drawing
			while (i.hasNext()) {
			    GlyphObject object = (GlyphObject) i.next();

			    if (m_actives.isSelected(object)) {
			        isFound = true;
			        continue;
			    } // if
			    
			    if (!isFound) {
			        continue;
			    } // if
			    
			    m_actives.unselectAll();
			    m_actives.addActive(object);
			    return;
			} // while i
		} // if
		
		i = new SelectionIterator(this);
	    if (i.hasNext()) {
	        GlyphObject object = (GlyphObject) i.next();
	        m_actives.unselectAll();
	        m_actives.addActive(object);
	        return;
	    } // if
	}

	// --------------------------------------------------------------
	
	public boolean hit(Rectangle2D a_rect,
	        boolean a_isAppend, 
	        boolean a_single) {

		
		if (!a_isAppend) {
			m_actives.unselectAll();
		} // if
		
		return hitObjects(a_rect, a_single);
	}
	
	// --------------------------------------------------------------
	
	private boolean hitObjects(Rectangle2D a_rect, boolean a_isSelectOnlyOne) {
		boolean retval = false;
		
		Iterator i = new SelectionIterator(this);
		
		// loop the iterator in reverse order of drawing
		while (i.hasNext()) {
		    GlyphObject object = (GlyphObject) i.next();

		    if (!object.hit(a_rect, new AffineTransform())) {
		        continue;
		    } // if
		    
	        retval = true;
	        
	        if (a_isSelectOnlyOne) {
	            return true;
	        } // if
		} // while i
		
		return retval;
	}
	
	// --------------------------------------------------------------
	
	/** used for cut and paste.
	 */
	public void addObjectFromClipboard(String a_value) throws CircularIncludeException {
		Reader reader = new StringReader(a_value);
		Document document = null;
		try {
			document = UJAXP.getDocument(reader);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} // try-catch
		
		Element root = document.getDocumentElement();
		if (!root.getNodeName().equals("clipboard")) {
			return;
		} // if
		
		Node child;
		for (child = root.getFirstChild(); child != null;
				child = child.getNextSibling()) {
			if (!(child instanceof Element)) {
				continue;
			} // if
			Element element = (Element) child;
			
			IGlyphFactory factory = GlyphFactory.getFactory();
			
			
			if (XModule.isMatch(element)) {
				EModuleInvoke module = (EModuleInvoke) factory.createXModule(element);
				addModule(module);
				continue;
			} // if
		
			if (XContour.isMatch(element)) {
				EContour contour = (EContour) factory.createXContour(element);
				addContour(contour);
				continue;
			} // if
		
			if (XInclude.isMatch(element)) {
				EIncludeInvoke include = (EIncludeInvoke) factory.createXInclude(element);
				addInclude(include);
				continue;
			} // if
		} // while
	}
		
	//	--------------------------------------------------------------
	
	private boolean isCircularInclude(String a_href) {
		if (getShortFileName().equals(a_href)) {
			return true;
		} // if
		
		ModuleManager manager = ModuleManager.getSingletonInstance();
		XInclude [] includes = m_glyph.getBody().getInclude();
		int i;
		for (i = 0; i < includes.length; i++) {
			XInclude include = includes[i];
			GlyphFile child = manager.getGlyphFile(include.getHref());
			
			if (child.isCircularInclude(a_href)) {
				return true;
			} // if
		} // for i
		
		return false;
	}
	
	// --------------------------------------------------------------
	
	public void addInclude(String a_fileName) throws CircularIncludeException {		
		addInclude(EIncludeInvoke.create(a_fileName));	
	}
	
	public void addInclude(XInclude a_include) throws CircularIncludeException {
		if (isCircularInclude(a_include.getHref())) {
			throw new CircularIncludeException();
		} // if
		
		m_actives.unselectAll();
		m_actives.addActive((GlyphObject) a_include);
		m_glyph.getBody().addInclude(a_include);
		m_history.record("addInclude");		
	}
		
	// --------------------------------------------------------------
	
	/**
	 * pasted module from clipboard or ModuleInvokeAction
	 * @param a_module
	 */
	public XModule addModule(EModuleInvoke a_module) {
		m_actives.unselectAll();
		m_actives.addActive(a_module);
		m_glyph.getBody().addModule(a_module);
		m_history.record("addModule");
		
		return a_module;		
	}
	
	// --------------------------------------------------------------
	
	/** add contour from clipboard or ContourAction
	 * @param a_contour
	 */
	public void addContour(EContour a_contour) {
		m_actives.unselectAll();
		m_actives.addActive(a_contour);
		m_glyph.getBody().addContour(a_contour);
		m_history.record("addContour");		
	}
	

	// --------------------------------------------------------------
	
	
	public void addPoint() {
		if (!m_actives.hasActivePoint()) {
			return;
		} // if
		
		EContourPoint contourPoint = m_actives.getActivePoint();
		m_actives.unselectAll();
		m_actives.addActive(contourPoint.add());
		
		m_history.record("addPoint");		
	}
	
	// --------------------------------------------------------------
	
	public void addHint(int a_ppem) {
		if (!m_actives.hasActivePoint())
			return;
		
		EContourPoint contourPoint = m_actives.getActivePoint();
		m_actives.unselectAll();
		m_actives.addActive(contourPoint.addHint(a_ppem));
		
		if (contourPoint.hasControlPoint1()) {
		    EContourPoint p = (EContourPoint) contourPoint.getControlPoint1().getContourPoint();
		    m_actives.addActive(p.addHint(a_ppem));
		} // if
		
		if (contourPoint.hasControlPoint2()) {
		    EContourPoint p = (EContourPoint) contourPoint.getControlPoint2().getContourPoint();
		    m_actives.addActive(p.addHint(a_ppem));
		} // if
		
		
		m_history.record("addHint");					
	}
	
	public void toggleRounded() {
	    if (!m_actives.hasActivePoint())
	        return;
	    
	   EContourPoint contourPoint = m_actives.getActivePoint();
	   contourPoint.toggleRounded();
	   
	   m_history.record("toggleGridfit");
	}
	
	public void convertControlPoint() {
	    if (!m_actives.hasActiveControlPoint()) {
	        return;
	    } // if
	    
	    EControlPoint controlPoint = m_actives.getActiveControlPoint();
	    controlPoint.convert();
	    
	    m_history.record("convertControlPoint");
	}
	
	public void convertContour() {
	    if (!m_actives.hasActiveContour()) {
	        return;
	    } // if
	    
	    EContour contour = m_actives.getActiveContour();
	    contour.convert();
	    
	    m_history.record("convertContour");
	}
	
	// --------------------------------------------------------------
	
	public void remove() {
		if (!m_actives.hasSelected())
			return;
		
		int i;
		for (i = 0; i < m_actives.size(); i++) {
			GlyphObject active = m_actives.get(i);
			active.remove();
		} // for i
				
		m_history.record("remove");
				
		m_actives.unselectAll();	
	}
			
	// --------------------------------------------------------------
	
	public void toggleOnOff() {
		if (!m_actives.hasSelected())
			return;
			
		int i;
		for (i = 0; i < m_actives.size(); i++) {
			GlyphObject active = m_actives.get(i);
			if (!(active instanceof EContourPoint)) {
				continue;
			} // if			
			
			EContourPoint point = (EContourPoint) active;
			point.toggleOnCurve();
		} // for i
				
		m_history.record("toggleOnOff");	
	}
		
	private void loadVar() {
		XParamListParam [] params = m_glyph.getHead().getHeadGlobal().getParamListParam();
		int i;
		for (i = 0; i < params.length; i++) {
			XParamListParam param = params[i];
			addVar(param.getName(), param.getContent());
		} // for i
	}
	
	public String getSelectedNodeName() {
		return m_selectedNodeName;	
	}
	
	public void addFileVar() {
		XParamListParam param = new XParamListParam();
		param.setName("New parameter");
		param.setContent(0.0);
		m_history.record("addFileVar");
		
		m_glyph.getHead().getHeadGlobal().addParamListParam(param);
	}
	
	public void removeFileVar(int a_index) {
		m_history.record("removeFileVar");
		
		m_glyph.getHead().getHeadGlobal().removeParamListParam(a_index);	
	}
	
	
	public void addInvokeArg(int a_type) {
		
	}
	
	public void removeInvokeArg(int a_type, int a_index) {
			
	}
	
	public String getIncludeName() {
		String retval = "";
		
		if (!m_actives.hasActiveInclude())
			return retval;
		retval = m_actives.getActiveInclude().getHref();	
		
		return retval;
	}
	
	public void setIncludeName(String a_name) {
		if (!m_actives.hasActiveInclude())
			return;
		
		m_actives.getActiveInclude().setHref(a_name);
		m_history.record("setIncludeName");		
	}
	
	public void setGlyphTitle(String a_title) {
		m_glyph.getHead().setTitle(a_title);
		m_history.record("setGlyphTitle");
	}
	
	public String getGlyphTitle() {
		String retval = "";
		
		retval = m_glyph.getHead().getTitle();
		
		if (retval.equals("empty")) {
			retval = "";	
		} // if
		
		return retval;
	}
	
	public String getModuleName() {
		String retval = "";
		
		if (!m_actives.hasActiveModule())
			return retval;
		retval = m_actives.getActiveModule().getName();	
		
		return retval;
	}
	
	public String getUnicode() {
		return getUnicodeAsString();
	}
	
	public String getUnicodeAsString() {
		return m_glyph.getHead().getUnicode();
	}
	
	public long getUnicodeAsLong() {
		long retval = -1;
		
		String s = getUnicodeAsString();
		if (s.equals("")) {
			return retval;
		} // if
		
		try {
			retval = Long.parseLong(s, 16);
		} catch (NumberFormatException e) {
			retval = -1;
		} // try-catch
		
		return retval;
	}
	
	protected void setUnicode(String a_unicode) {
		m_glyph.getHead().setUnicode(a_unicode);
		m_history.record("setUnicode");
	}
	
	public boolean isSimple() {
		return (m_glyph.getBody().getInclude().length == 0);
	}
	
	public boolean isWhiteSpace() {
		long unicode = getUnicodeAsLong();
		
		if (unicode == 0x0020
			|| unicode == 0x00a0
			|| unicode == 0x200b
			|| unicode == 0x2060
			|| unicode == 0x3000
			|| unicode == 0xfeff)
		{
			return true;
		} // if
		
		return false;
	}
	
	public String getShortFileName() {
		return m_fileName.getName();
	}
	
	public void setLicense(String a_value) {
		m_glyph.getHead().setLicense(a_value);
		m_history.record("setLicense");
	}
	
	public String getLicense() {
		return m_glyph.getHead().getLicense();
	}
	
	public PointAggregate getPointHost() {
	    return m_pointHost;
	}
	
	public void buildPointHost() {
	    m_pointHost = null;
	    
	    /*if (!GlyphAction.isPointVisible()) {
	        return;
	    } // if*/
	    
	    int i;
	    for (i = 0; i < m_actives.size(); i++) {
	        GlyphObject object = (GlyphObject) m_actives.get(i);
	        PointAggregate host = null;
	        
	        if (object instanceof EContourPoint) {
	            EContourPoint point = (EContourPoint) object;
	            host = point.getParent();
	            
	            if (host instanceof EContourPoint) {
	                EContourPoint hostPoint = (EContourPoint) host;
	                host = hostPoint.getParent();
	            } // if
	        } else if (object instanceof EHint) {
	            EHint hint = (EHint) object;
	            host = hint.getPointHost();
	        } // if-else
	        
	        if (host == null) {
	            continue;
	        } // if
	        
	        if (m_pointHost == null) {
	            m_pointHost = host;
	        } else {
	            if (m_pointHost != host) {
	                m_pointHost = null;
	                return;
	            } // if
	        } // if
	    } // while
	}
	
	public boolean isRequiredGlyph() {
		long unicode = getUnicodeAsLong();
		
		return (unicode == TTUnicodeRange.k_notDef
				|| unicode == TTUnicodeRange.k_null
				|| unicode == TTUnicodeRange.k_cr
				|| unicode == TTUnicodeRange.k_space);
	}
	
	public TTGlyph toSimpleGlyph() {
		// convert the file into array of contours
		XContour [] contours = toContours();
		if ((contours == null) && (!isRequiredGlyph())) {
			return null;
		} // if
		
		TTGlyph retval = new TTGlyph();
		retval.setSimple(true);
		retval.setAdvanceWidth(getAdvanceWidth());
		
		if (contours == null) {
			return retval;
		} // if
		
		ArrayList<EContourPoint> points = new ArrayList<>();
		for (int i = 0; i < contours.length; i++) {
			XContour contour = contours[i];
			XContourPoint [] contourPoints = contour.getContourPoint();
			for (int j = 0; j < contourPoints.length; j++) {
			    points.add((EContourPoint) contourPoints[j]);
			} // for j
			retval.addEndPoint(points.size() - 1);
		} // for i
		
		for (EContourPoint point: points) {
			loadContourPoint(retval, point);
		} // for point
		
		boolean hasGridfit = false;	
		// I need int i here.
		for (int i = 0; i < points.size(); i++) {
		    EContourPoint point = points.get(i);
		    
		    if (!point.isRounded()) {
		        continue;
		    } // if
		    
		    hasGridfit = true;
		    loadGridfit(retval, point, i);
		} // for i
		
		if (hasGridfit) {
		    retval.addInstruction(TTGlyph.IUP1);
		    retval.addInstruction(TTGlyph.IUP0);
		} // if
		
		// I need int i here.
		for (int i = 0; i < points.size(); i++) {
		    EContourPoint point = points.get(i);
		    if (point.getHint().length == 0) {
		        continue;
		    } // if
		    
		    loadHint(retval, point, i);
		} // for i
		
		return retval;
	}
	
	private void loadContourPoint(TTGlyph a_glyph, EContourPoint a_point) {
		double x = a_point.getX();
		double y = a_point.getY();
		Point p = new Point((int) x, (int) y);
		int flag = 0;
		if (a_point.isOn()) {
			flag = TTGlyph.k_onCurve;
		} // if
				
		a_glyph.addPoint(p);
		a_glyph.addFlag(flag);    
	}
	
	private void loadGridfit(TTGlyph a_glyph, EContourPoint a_point, int a_index) {
	    if (!a_point.isRounded()) {
	        return;
	    } // if
	    
	    a_glyph.addInstruction(TTGlyph.PUSHB000);
	    a_glyph.addInstruction(a_index);
	    a_glyph.addInstruction(TTGlyph.MDAP1);
	}
	
	
	private void loadHint(TTGlyph a_glyph, EContourPoint a_point, int a_index) {
		double x = a_point.getX();
		double y = a_point.getY();
		
		XHint [] hints = a_point.getHint();
		
		for (int i = 0; i < hints.length; i++) {
			EHint hint = (EHint) hints[i];
			double xHint = hint.getX();
			double yHint = hint.getY();
			
			if (x == xHint && y == yHint) {
				continue;
			} // if
			
			double xDelta = xHint - x;
			double yDelta = yHint - y;
			int instruction = TTGlyph.DELTAP1;
			double deltaStep = ((double) Engine.getEm()) / hint.getPpem() / 8;
			int xShift = (int) Math.round(xDelta / deltaStep);
			int yShift = (int) Math.round(yDelta / deltaStep);
			
			if (xShift == 0 && yShift == 0) {
				continue;		
			} // if
			
			a_glyph.addInstruction(TTGlyph.PUSHB000);
			a_glyph.addInstruction((int) hint.getPpem());
			a_glyph.addInstruction(TTGlyph.SDB);
			
			if (xShift != 0) {
				a_glyph.addInstruction(TTGlyph.SVTCA1);
				a_glyph.addInstruction(TTGlyph.PUSHB010);
				a_glyph.addInstruction(TTGlyph.toDeltaArg(0, xShift));
				a_glyph.addInstruction(a_index);
				a_glyph.addInstruction(1);
				a_glyph.addInstruction(TTGlyph.DELTAP1);
			} // if
			
			if (yShift != 0) {
				a_glyph.addInstruction(TTGlyph.SVTCA0);
				a_glyph.addInstruction(TTGlyph.PUSHB010);
				a_glyph.addInstruction(TTGlyph.toDeltaArg(0, yShift));
				a_glyph.addInstruction(a_index);
				a_glyph.addInstruction(1);
				a_glyph.addInstruction(TTGlyph.DELTAP1);
			} // if
		} // for i
	}
	
	public class CircularIncludeException extends Exception {}
}