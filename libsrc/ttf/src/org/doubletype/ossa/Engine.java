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

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.doubletype.ossa.adapter.*;
import org.doubletype.ossa.module.*;
import org.doubletype.ossa.truetype.*;

/**
 * @author e.e
 */
public class Engine {
	// --------------------------------------------------------------	

	// used by findFile
	public static final int USER_CANCELLED = -1;
	public static final int FILE_NOT_FOUND = 0;
	public static final int FILE_FOUND = 1;
	
	// used by Find
	public static final int SEARCH_BY_EXAMPLE = 0;
	public static final int SEARCH_UNICODE = 1;
	public static final int SEARCH_JIS_CODE = 2;
	
	// public static final double k_fontSizes [] = {9, 10, 11, 12, 14, 18, 24, 36, 72};
	public static final int k_defaultPixelSize = 16;
	public static final int k_resolutions [] = {96, 72, 75, 100};
	public static final int k_defaultResolution = 96;
	public static final int k_zooms [] = {25, 50, 100};
	public static final int k_defaultZoom = 100;
	
	// --------------------------------------------------------------

	private static int s_em = 1024;
	private static Engine s_singleton = null;

	public static Engine getSingletonInstance() {
		if (s_singleton == null)
			s_singleton = new Engine();
		return s_singleton;
	}

	public static int getEm() {
		return TTPixelSize.getEm();
	}

	// --------------------------------------------------------------

	private UiBridge m_ui;
	
	private TypefaceFile m_typeface;
	private GlyphFile m_root;
	private ActiveList m_actives;
	private ArrayList<ActionListener> m_listeners = new ArrayList<>();
	
	private String m_foundFileName;
	private Clipboard m_clipboard;
	private JFileChooser m_gifChooser;
	private JFileChooser m_chooser;
	
	private Action m_deleteAction;
	private Action m_addPointAction;
	private Action m_toggleAction;
	private Action m_hintAction;
	private Action m_contourAction;
	private Action m_moduleAction;
	private Action m_includeAction;
	private Action m_selectNextAction;
	private Action m_roundAction;
	private Action m_propertyAction;
	private Action m_convertControlPointAction;
	private Action m_convertContourAction;

	private String m_msgAlreadyExists = " already exists!";
	private String m_msgNoTypeface = "no typeface";
	private String m_msgEmptyGlyphTitle = "glyph title is empty";
	private String m_msgGlyphName = "glyph name?";
	private String m_msgCircular = "circular include!";
	private String m_msgNoJis = "Charset ISO-2022-JP is not supported.\n"
		+ "Please include charsets.jar in classpath.";

	// --------------------------------------------------------------

	private Engine() {
		//GlyphFactory.setFactory(new EGlyphFactory());
		


		m_typeface = null;
		m_root = null;

		m_clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		m_actives = ActiveList.getSingletonInstance();
		
		initActions();
	}
	
	private void initActions() {
            
	}

	public Action [] buildCommands() {
		ArrayList actions = buildCommandsArrayList();
		Action [] retval = new Action[actions.size()];
		int i;
		for (i = 0; i < actions.size(); i++) {
			retval[i] = (Action) actions.get(i);
		} // for i
	    
		return retval;
	}
	
	private ArrayList buildCommandsArrayList() {
		ArrayList<Action> retval = new ArrayList<>();
	    
		if (m_root == null) {
			return retval;	
		} // if
		
		if (m_actives.hasActiveContour()) {
		    retval.add(m_convertContourAction);
		}
	    
		if (m_actives.hasActiveControlPoint()) {
		    EControlPoint controlPoint = m_actives.getActiveControlPoint();
		    retval.add(m_convertControlPointAction);
		}
		
		if (m_actives.hasActivePoint()) {
		    EContourPoint point = m_actives.getActivePoint();
		    
		    retval.add(m_toggleAction);
		    
		    if (!point.isRounded()) {
		        retval.add(m_hintAction);
		    } // if
		    
			if (!point.hasHintForCurrentPpem()) {
			    retval.add(m_roundAction);
			} // if
		} // if
		
		if (m_actives.size() > 0) {
		    // retval.add(m_propertyAction);
		    retval.add(m_deleteAction);
		} // if
		
		if (m_actives.hasActivePoint()) {
		    retval.add(m_addPointAction);
		} // if
		
		/*if (!GlyphAction.isPointVisible()) {
			retval.add(m_moduleAction);
			retval.add(m_contourAction);
			retval.add(m_includeAction);
		} // if*/
		
		return retval;
	}
	
	public void localize(ResourceBundle a_bundle) {
		m_msgAlreadyExists = a_bundle.getString("msgAlreadyExists");
		m_msgNoTypeface = a_bundle.getString("msgNoTypeface");
		m_msgEmptyGlyphTitle = a_bundle.getString("msgEmptyGlyphTitle");
		m_msgGlyphName = a_bundle.getString("msgGlyphName");
		m_msgCircular = a_bundle.getString("msgCircular");
	}
	
	private void showPropertyDialog() {
	    if (m_actives.size() != 1) {
	        return;
	    } // if
	    
	    m_ui.showPropertyDialog(m_actives.get(0));
	    fireAction();
	}
	
	public void setUi(UiBridge a_ui) {
	    m_ui = a_ui;
	}
	

	
	public void selectNext() {
	    if (m_root == null) {
	        return;
	    } // if
	    
	    m_root.selectNext();
	    fireAction();
	}
	
	public void delete() {
		if (m_root == null)
			return;
		if (!m_actives.hasSelected()) {
			return;
		} // if
		
		m_root.remove();
			
		fireAction();
	}

	public void cutToClipboard() {
		if (m_root == null)
			return;
		if (!m_actives.hasSelected()) {
			return;
		} // if
		
		copyToClipboard();
		delete();
		
		fireAction();
	}

	public void copyToClipboard() {
		if (m_root == null)
			return;
		if (!m_actives.hasSelected()) {
			return;
		} // if

		String s = m_actives.getSelectedAsString();
		if (s.equals("")) {
			return;
		} // if

		StringSelection selection = new StringSelection(s);

		try {
			m_clipboard.setContents(selection, selection);
		} catch (Exception e) {
			e.printStackTrace();
		} // try-catch

		fireAction();
	}

	public void pasteFromClipboard() {
		if (m_root == null)
			return;
		Transferable content = null;
		String s = "";

		try {
			content = m_clipboard.getContents(this);
			if (content == null)
				return;
			s = (String) content.getTransferData(DataFlavor.stringFlavor);
			if (s.equals("")) {
				return;
			} // if
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} // try-catch

		try {
			m_root.addObjectFromClipboard(s);
		} catch (GlyphFile.CircularIncludeException e) {
			         Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, m_msgCircular, e);
		}

		fireAction();
	}

	public void undo() {
		if (m_root == null)
			return;

		m_root.undo();
		fireAction();
	}

	public void redo() {
		if (m_root == null)
			return;

		m_root.redo();
		fireAction();
	}

	public void setAdvanceWidth(int a_value) {
		if (m_root == null)
			return;

		m_root.setAdvanceWidth(a_value);
		fireAction();
	}
	
	public void moveLeft() {
	    move(new Point2D.Double(-1, 0));
	}
	
	public void moveUp() {
	   move(new Point2D.Double(0, 1)); 
	}
	
	public void moveDown() {
	    move(new Point2D.Double(0, -1));
	}
	
	public void moveRight() {
	    move(new Point2D.Double(1, 0));
	}
	
	private void move(Point2D a_delta) {
	    if (m_root == null) {
	        return;
	    } // if
	    
	    m_root.move(a_delta);
	}

	public void buildNewTypeface(String a_name, File a_dir) throws FileNotFoundException {
		if (a_name == null || a_name.equals("")) {
			return;
		} // if
		
		TypefaceFile typeface = new TypefaceFile(a_name, a_dir);
		typeface.setAuthor("no body");
		DateFormat format = new SimpleDateFormat("yyyy");
		typeface.setCopyrightYear(format.format(new Date()));
		typeface.setFontFamilyName(a_name);
		typeface.setSubFamily("Regular");
		typeface.addCodePage(TTCodePage.US_ASCII.toString());
		typeface.addCodePage(TTCodePage.Latin_1.toString());

		setTypeface(typeface);
	}

	public void addDefaultGlyphs() throws FileNotFoundException {
		m_typeface.addRequiredGlyphs();
		m_typeface.addBasicLatinGlyphs();

		fireAction();
	}

	public void openTypeface() throws FileNotFoundException {
	    if (m_chooser == null) {
	        m_chooser = new JFileChooser(new File(AppSettings.getLastTypefaceDir()));
	    } // if
	    
	    m_chooser.setFileFilter(new TypefaceFileFilter());
		int returnVal = m_chooser.showOpenDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		} // if
		
		AppSettings.setLastTypefaceDir(m_chooser.getSelectedFile().toString());
		openTypeface(m_chooser.getSelectedFile());
	}
	
	private void openTypeface(File a_file) throws FileNotFoundException {
	    setTypeface(new TypefaceFile(a_file));
	    
	    if (m_typeface.addRequiredGlyphs()) {
	    	fireAction();
	    } // if
	}
	
	public void setTypeface(TypefaceFile a_typeface) {
		m_typeface = a_typeface;
		fireAction();
	}

	public void changeUnicode(long a_unicode) throws FileNotFoundException {
		if (m_typeface == null || m_root == null) {
			return;
		} // if

		m_typeface.setGlyphUnicode(m_root, a_unicode);
		fireAction();
	}
	
	public int findFile(long a_unicode) {
		if (m_typeface == null)
			return USER_CANCELLED;
		
		m_foundFileName = m_typeface.unicodeToFileName(a_unicode);
		if (m_foundFileName != null) {
			return FILE_FOUND;
		} // if

		return FILE_NOT_FOUND;
	}
	
	public String getFoundFileName() {
		return m_foundFileName;
	}
	

	/**
	 * Create glyph out of given unicode, and add it to the typeface.
	 * @param a_unicode
	 */
	public GlyphFile addNewGlyph(long a_unicode) throws FileNotFoundException {
		GlyphFile retval;

		retval = m_typeface.createGlyph(a_unicode);
		addGlyphToTypeface(retval);

		return retval;
	}

	public void checkUnicodeBlock(long a_unicode) throws FileNotFoundException {
		TTUnicodeRange range = TTUnicodeRange.of(a_unicode);
		if (range == null){
			return;
                }

		if (m_typeface.containsUnicodeRange(range.toString())){
			return;
                }
		m_typeface.addUnicodeRange(range.toString());
	}

	private void addGlyphToTypeface(GlyphFile a_file) throws FileNotFoundException {
		m_typeface.addGlyph(a_file);
		m_typeface.saveGlyphFile();

		setRoot(a_file);
	}

	public GlyphFile openGlyphFile(String a_fileName) {
		ModuleManager manager = ModuleManager.getSingletonInstance();
		GlyphFile retval = manager.getReloadedGlyphFile(a_fileName);
		setRoot(retval);

		return retval;
	}

	public void removeGlyphFromTypeface(String a_fileName) {
		if (m_typeface == null)
			return;

		m_typeface.removeGlyph(a_fileName);
		fireAction();
	}

	public Font buildTrueType(boolean a_isDebug) {
		Font retval = null;

		if (m_typeface == null)
			return retval;



		try {
			m_typeface.buildTTF(a_isDebug);
			retval = m_typeface.getFont();
		} catch (Exception e) {
			Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null,e);
		} // try-catch

		return retval;
	}

	public void saveGlyph() throws FileNotFoundException {
		if (m_root == null)
			return;

		if (m_root.getGlyphTitle().equals("")) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, m_msgEmptyGlyphTitle);
			return;
		} // if

		m_root.saveGlyphFile();
		fireAction();
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
		fireAction();
	}

	public void addActionListener(ActionListener a_listener) {
		m_listeners.add(a_listener);
	}

	public void fireAction() {
		ActionEvent e = new ActionEvent(this, Event.ACTION_EVENT, "foo");
		
		for (ActionListener listener: m_listeners) {
			listener.actionPerformed(e);
		} // for listener	
	}

	public String includeFileName() {
		String retval = "";

		JFileChooser chooser = new JFileChooser(getGlyphPath());
		chooser.setFileFilter(new GlyphFileFilter());

		int returnVal = chooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			retval = chooser.getSelectedFile().getName().toString();
		} // if

		return retval;
	}

	public void addCodePage(String a_codePage) throws FileNotFoundException {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.addCodePage(a_codePage);
		fireAction();
	}

	public void removeCodePage(String a_codePage) throws FileNotFoundException {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.removeCodePage(a_codePage);
		fireAction();
	}

	public void setAuthor(String a_value) throws FileNotFoundException {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.setAuthor(a_value);
		m_typeface.saveGlyphFile();
		fireAction();
	}

	public void setCopyrightYear(String a_value) throws FileNotFoundException {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.setCopyrightYear(a_value);
		m_typeface.saveGlyphFile();
		fireAction();
	}

	public void setFontFamilyName(String a_value) throws FileNotFoundException {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.setFontFamilyName(a_value);
		fireAction();
	}

	public void setTypefaceLicense(String a_value) throws FileNotFoundException {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.setLicense(a_value);
		m_typeface.saveGlyphFile();
		fireAction();
	}

	public void setBaseline(double a_value) throws FileNotFoundException {
		if (m_typeface == null) {
			return;
		} // if
		
		double min = m_typeface.getBottomSideBearing();
		double max = m_typeface.getMeanline();
				
		if (a_value < min) {
			a_value = min;
		} // if
		
		if (a_value > max) {
			a_value = max;
		} // if
		
		try {
			m_typeface.setDescender(a_value - min);
			m_typeface.setAscender(m_typeface.getEm()
				- m_typeface.getTopSideBearing() - a_value);
			m_typeface.setXHeight(max - a_value);
		}
		catch (OutOfRangeException e) {
			e.printStackTrace();	
		} // try-catch
		
		fireAction();
	}
	
	public void setMeanline(double a_value) throws FileNotFoundException {
		if (m_typeface == null) {
			return;
		} // if
		
		double min = m_typeface.getBaseline();
		double max = m_typeface.getEm()
			- m_typeface.getTopSideBearing();
				
		if (a_value < min) {
			a_value = min;
		} // if
		
		if (a_value > max) {
			a_value = max;
		} // if
		
		try {
			m_typeface.setXHeight(a_value - min);
		}
		catch (OutOfRangeException e) {
			e.printStackTrace();	
		} // try-catch
		
		fireAction();
	}

	public void mousePressed(MouseEvent a_event) {
		
	}
	

	public void mouseDragged(MouseEvent a_event) {
		
	}

	public void mouseReleased(MouseEvent a_event) {
		
	}
	
	public void setAction(String a_key) {		
		
	}

	public void keyPressed(KeyEvent a_event) {
		if (a_event.getModifiers() == 0) {
		    if (a_event.getKeyCode() == KeyEvent.VK_TAB) {
		    
		        m_selectNextAction.actionPerformed(null);
		    } // if
		} else if (a_event.getModifiers() == KeyEvent.SHIFT_MASK) {
		    
		} // if

		fireAction();
	}
	
	private JFileChooser createImageChooser() {
	    JFileChooser retval;
	    
	    retval = new JFileChooser(new File(AppSettings.getLastTypefaceDir()));
	    retval.setFileFilter(new javax.swing.filechooser.FileFilter() {	
			public boolean accept(File a_file) {
				if (a_file.isDirectory())
					return true;
				
				String s = a_file.toString().toLowerCase();
				if (s.endsWith(".gif")
				        || s.endsWith(".jpg")
				        || s.endsWith(".jpeg")
				        || s.endsWith(".png"))
					return true;
		        
				return false;
			}
		
			//The description of this filter
			public String getDescription() {
				return "Image Files";
			}
		});
	    
	    return retval; 
	}
	
	

	
	public ArrayList<TTPixelSize> getPixelSizes() {
		return TTPixelSize.getList();
	}
	
	class MyTreeListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent a_event) {
			TreePath path = a_event.getPath();
			Object obj = path.getLastPathComponent();
			String s = obj.toString();

			TreePath parent = path.getParentPath();
			if (parent != null) {
				obj = parent.getLastPathComponent();
				s = obj.toString() + "->" + s;
			} // if

		}
	}
	
}
