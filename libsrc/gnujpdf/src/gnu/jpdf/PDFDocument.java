/*
 * $Id: PDFDocument.java,v 1.4 2007/09/22 12:58:40 gil1 Exp $
 *
 * $Date: 2007/09/22 12:58:40 $
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * <p>This class is the base of the PDF generator. A PDFDocument class is 
 * created for a document, and each page, object, annotation, 
 * etc is added to the document.
 * Once complete, the document can be written to an OutputStream, and the PDF
 * document's internal structures are kept in sync.</p>
 *
 * <p>Note that most programmers using this package will NEVER access 
 * one of these objects directly.  Most everything can be done using 
 * <code>PDFJob</code> and <code>PDFGraphics</code>, so you don't need 
 * to directly instantiate a <code>PDFDocument</code></p>  
 *
 * <p>ezb - 20011115 - Wondering if the constructors should even be public.
 * When would someone want to make one of these and manipulate it outside 
 * the context of a job and graphics object?</p>
 *
 * @author Peter T Mount, http://www.retep.org.uk/pdf/
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @author Gilbert DeLeeuw, gil1@users.sourceforge.net
 * @version $Revision: 1.4 $, $Date: 2007/09/22 12:58:40 $
 */
public class PDFDocument implements Serializable
{
	
  /*
   * NOTE: This class originated in uk.org.retep.pdf, by Peter T. Mount, and it 
   * has been modified by Eric Z. Beard, ericzbeard@hotmail.com.  The 
   * package name was changed to gnu.jpdf and several inner classes were 
   * moved out into their own files.
   */

  /**
   * This is used to allocate objects a unique serial number in the document.
   */
  protected int objser;
    
  /**
   * This vector contains each indirect object within the document.
   */
  protected Vector<PDFObject> objects;
    
  /**
   * This is the Catalog object, which is required by each PDF Document
   */
  private PDFCatalog catalog;
    
  /**
   * This is the info object. Although this is an optional object, we
   * include it.
   */
  private PDFInfo info;
    
  /**
   * This is the Pages object, which is required by each PDF Document
   */
  private PDFPageList pdfPageList;
    
  /**
   * This is the Outline object, which is optional
   */
  private PDFOutline outline;
    
  /**
   * This holds a PDFObject describing the default border for annotations.
   * It's only used when the document is being written.
   */
  protected PDFObject defaultOutlineBorder;
    
  /**
   * <p>This page mode indicates that the document 
   * should be opened just with the page visible.  This is the default</p>
   */
  public static final int USENONE = 0;
    
  /**
   * <p>This page mode indicates that the Outlines 
   * should also be displayed when the document is opened.</p>
   */
  public static final int USEOUTLINES = 1;
    
  /**
   * <p>This page mode indicates that the Thumbnails should be visible when the
   * document first opens.</p>
   */
  public static final int USETHUMBS = 2;
    
  /**
   * <p>
   * This page mode indicates that when the document is opened, it is displayed
   * in full-screen-mode. There is no menu bar, window controls nor any other
   * window present.</p>
   */
  public static final int FULLSCREEN = 3;
    
  /**
   * <p>
   * These map the page modes just defined to the pagemodes setting of PDF.
   * </p>
   */
  public static final String PDF_PAGE_MODES[] = {
    "/UseNone",
    "/UseOutlines",
    "/UseThumbs",
    "/FullScreen"
  };
    
  /**
   * This is used to provide a unique name for a font
   */
  private int fontid = 0;
    
  /**
   * <p>This is used to provide a unique name for an image</p>
   */
  private int imageid = 0;
    
  /**
   * This holds the current fonts
   */
  private Vector<PDFFont> fonts;
    
      
  /**
   * <p>This creates a PDF document with the default pagemode</p>
   */
  public PDFDocument() {
    this(USENONE);
  }
    
  /**
   * <p>This creates a PDF document</p>
   * @param pagemode an int, determines how the document will present itself to
   *        the viewer when it first opens.
   */
  public PDFDocument(int pagemode) {
    objser = 1;
    objects = new Vector<PDFObject>();
    fonts = new Vector<PDFFont>();
        
    // Now create some standard objects
    add(pdfPageList = new PDFPageList());
    add(catalog = new PDFCatalog(pdfPageList,pagemode));
    add(info = new PDFInfo());
        
    // Acroread on linux seems to die if there is no root outline
    add(getOutline());
  }
    


  /**
   * This adds a top level object to the document.
   *
   * <p>Once added, it is allocated a unique serial number.
   *
   * <p><b>Note:</b> Not all object are added directly using this method.
   * Some objects which have Kids (in PDF sub-objects or children are
   * called Kids) will have their own add() method, which will call this
   * one internally.
   *
   * @param obj The PDFObject to add to the document
   * @return the unique serial number for this object.
   */
  public synchronized int add(PDFObject obj)
  {
    objects.addElement(obj);
    obj.objser=objser++; // create a new serial number
    obj.pdfDocument = this;  // so they can find the document they belong to
        
    // If its a page, then add it to the pages collection
    if(obj instanceof PDFPage)
      pdfPageList.add((PDFPage)obj);
        
    return obj.objser;
  }
    
  /**
   * <p>This returns a specific page. It's used mainly when using a
   * Serialized template file.</p>
   *
   * ?? How does a serialized template file work ???
   *
   * @param page page number to return
   * @return PDFPage at that position
   */
  public PDFPage getPage(int page) {
    return pdfPageList.getPage(page);
  }
    

  /**
   * @return the root outline
   */
  public PDFOutline getOutline()
  {
    if(outline==null) {
      outline = new PDFOutline();
      catalog.setOutline(outline);
    }
    return outline;
  }
    
  /**
   * This returns a font of the specified type and font. If the font has
   * not been defined, it creates a new font in the PDF document, and
   * returns it.
   *
   * @param type PDF Font Type - usually "/Type1"
   * @param font Java font name
   * @param style java.awt.Font style (NORMAL, BOLD etc)
   * @return PDFFont defining this font
   */
  public PDFFont getFont(String type,String font,int style) {
    for(PDFFont ft : fonts) {
      if(ft.equals(type,font,style))
        return ft;
    }
        
    // the font wasn't found, so create it
    fontid++;
    PDFFont ft = new PDFFont("/F"+fontid,type,font,style);
    add(ft);
    fonts.addElement(ft);
    return ft;
  }
    
  /**
   * Sets a unique name to a PDFImage
   * @param img PDFImage to set the name of
   * @return the name given to the image
   */
  public String setImageName(PDFImage img) {
    imageid++;
    img.setName("/Image"+imageid);
    return img.getName();
  }


  /**
   * <p>Set the PDFInfo object, which contains author, title, 
   * keywords, etc</p>
   * @param info a PDFInof object
   */
  public void setPDFInfo(PDFInfo info) {
    this.info = info;
  }
    

  /**
   * <p>Get the PDFInfo object, which contains author, title, keywords,
   * etc</p>
   * @return the PDFInfo object for this document.
   */
  public PDFInfo getPDFInfo() {
    return this.info;
  }


  /**
   * This writes the document to an OutputStream.
   *
   * <p><b>Note:</b> You can call this as many times as you wish, as long as
   * the calls are not running at the same time.
   *
   * <p>Also, objects can be added or amended between these calls.
   *
   * <p>Also, the OutputStream is not closed, but will be flushed on
   * completion. It is up to the caller to close the stream.
   *
   * @param os OutputStream to write the document to
   * @exception IOException on error
   */
  public void write(OutputStream os) throws IOException
  {
    PDFOutput pos = new PDFOutput(os);
        
    // Write each object to the OutputStream. We call via the output
    // as that builds the xref table
    for(PDFObject o : objects) {
      pos.write(o);
    }
        
    // Finally close the output, which writes the xref table.
    pos.close();
        
    // and flush the output stream to ensure everything is written.
    os.flush();
  }
          
} // end class PDFDocument
