/*
 * $Id: PDFCatalog.java,v 1.2 2007/08/26 18:56:35 gil1 Exp $
 *
 * $Date: 2007/08/26 18:56:35 $
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

import java.io.*;

/**
 * <p>This class implements the PDF Catalog, 
 * also known as the root node</p>
 *
 * @author Peter T. Mount
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.2 $, $Date: 2007/08/26 18:56:35 $
 */
public class PDFCatalog extends PDFObject
{
  /**
   * The pages of the document
   */
  private PDFPageList pdfPageList;
    
  /**
   * The outlines of the document
   */
  private PDFOutline outlines;
  
  /**
   * The initial page mode
   */
  private int pagemode;
  
  /**
   * This constructs a PDF Catalog object
   *
   * @param pdfPageList The PDFPageList object that's the root 
   *        of the documents page tree
   * @param pagemode How the document should appear when opened.
   * Allowed values are USENONE, USEOUTLINES, USETHUMBS or FULLSCREEN.
   */
  public PDFCatalog(PDFPageList pdfPageList,int pagemode) {
    super("/Catalog");
    this.pdfPageList = pdfPageList;
    this.pagemode = pagemode;
  }
  
  /**
   * This sets the root outline object
   * @param outline The root outline
   */
  protected void setOutline(PDFOutline outline) {
    this.outlines = outline;
  }
  
  /**
   * @param os OutputStream to send the object to
   * @exception IOException on error
   */
  public void write(OutputStream os) throws IOException {
    // Write the object header
    writeStart(os);
    
    // now the objects body
    
    // the /Pages object
    os.write("/Pages ".getBytes());
    os.write(pdfPageList.toString().getBytes());
    os.write("\n".getBytes());
            
    // the Outlines object
    if(outlines!=null) {
      //if(outlines.getLast()>-1) {
      os.write("/Outlines ".getBytes());
      os.write(outlines.toString().getBytes());
      os.write("\n".getBytes());
      //}
    }
            
    // the /PageMode setting
    os.write("/PageMode ".getBytes());
    os.write(PDFDocument.PDF_PAGE_MODES[pagemode].getBytes());
    os.write("\n".getBytes());
            
    // finish off with its footer
    writeEnd(os);
  }
} // end class PDFCatalog
    
