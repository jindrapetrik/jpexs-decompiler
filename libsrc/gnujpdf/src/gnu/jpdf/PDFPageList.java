/*
 * $Id: PDFPageList.java,v 1.2 2007/08/26 18:56:35 gil1 Exp $
 *
 * $Date: 2007/08/26 18:56:35 $
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

import java.io.*;
import java.util.*;

/**
 * This object contains the document's pages.
 *
 * @author Peter T. Mount
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.2 $, $Date: 2007/08/26 18:56:35 $
 */
public class PDFPageList extends PDFObject 
{
  
  /**
   * This holds the pages
   */
  private Vector<PDFPage> pages;
        
  /**
   * This constructs a PDF Pages object.
   */
  public PDFPageList() {
    super("/Pages");
    pages = new Vector<PDFPage>();
  }
        
  /**
   * This adds a page to the document.
   *
   * @param page PDFPage to add
   */
  public void add(PDFPage page) {
    pages.addElement(page);
            
    // Tell the page of ourselves
    page.pdfPageList = this;
  }
        
  /**
   * This returns a specific page. Used by the PDF class.
   * @param page page number to return
   * @return PDFPage at that position
   */
  public PDFPage getPage(int page) {
    return (PDFPage)(pages.elementAt(page));
  }
        
  /**
   * @param os OutputStream to send the object to
   * @exception IOException on error
   */
  public void write(OutputStream os) throws IOException {
    // Write the object header
    writeStart(os);
            
    // now the objects body
            
    // the Kids array
    os.write("/Kids ".getBytes());
    os.write(PDFObject.toArray(pages).getBytes());
    os.write("\n".getBytes());
            
    // the number of Kids in this document
    os.write("/Count ".getBytes());
    os.write(Integer.toString(pages.size()).getBytes());
    os.write("\n".getBytes());
            
    // finish off with its footer
    writeEnd(os);
  }
 
} // end class PDFPageList
