/*
 * 
 * $Id: PDFObject.java,v 1.3 2007/09/22 12:48:16 gil1 Exp $
 *
 * $Date: 2007/09/22 12:48:16 $
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
 */
package gnu.jpdf;

import java.io.*;
import java.util.*;

/**
 * This is the base class for all Objects that form the PDF document.
 *
 * @author Peter T Mount, http://www.retep.org.uk/pdf/
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.3 $, $Date: 2007/09/22 12:48:16 $
 */
public abstract class PDFObject implements Serializable
{

  /*
   * NOTE: The original class is the work of Peter T. Mount, who released it 
   * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as 
   * follows: 
   * The package name was changed to gnu.pdf.  
   * The formatting was changed a little bit.
   * It is still licensed under the LGPL.
   */

  /**
   * This is the object's PDF Type
   */
  private String type;
    
  /**
   * This is the unique serial number for this object.
   */
  protected int objser;
    
  /**
   * This allows any PDF object to refer to the document being constructed.
   */
  protected PDFDocument pdfDocument;
    
      
  /**
   * This is usually called by extensors to this class, and sets the
   * PDF Object Type
   * @param type the PDF Object Type
   */
  public PDFObject(String type)
  {
    this.type = type;
  }
    
  /**
   * Returns the PDF Type of this object
   * @return The PDF Type of this object
   */
  public String getType()
  {
    return type;
  }
    
  /**
   * Returns the unique serial number of this object.
   * @return Unique serial number of this object.
   */
  public final int getSerialID()
  {
    return objser;
  }
    
  /**
   * Returns the PDF document this object belongs to.
   * @return PDF containing this object
   */
  public final PDFDocument getPDFDocument()
  {
    return pdfDocument;
  }
    
  /**
   * <p>Writes the object to the output stream.
   * This method must be overidden.</p>
   *
   * <p><b>Note:</b> It should not write any other objects, even if they are
   * it's Kids, as they will be written by the calling routine.</p>
   *
   * @param os OutputStream to send the object to
   * @exception IOException on error
   */
  public abstract void write(OutputStream os) throws IOException;
    
  /**
   * The write method should call this before writing anything to the
   * OutputStream. This will send the standard header for each object.
   *
   * <p>Note: There are a few rare cases where this method is not called.
   *
   * @param os OutputStream to write to
   * @exception IOException on error
   */
  public final void writeStart(OutputStream os) throws IOException
  {
    os.write(Integer.toString(objser).getBytes());
    os.write(" 0 obj\n<<\n".getBytes());
    if(type!=null) {
      os.write("/Type ".getBytes());
      os.write(type.getBytes());
      os.write("\n".getBytes());
    }
  }



    
  /**
   * The write method should call this after writing anything to the
   * OutputStream. This will send the standard footer for each object.
   *
   * <p>Note: There are a few rare cases where this method is not called.
   *
   * @param os OutputStream to write to
   * @exception IOException on error
   */
  public final void writeEnd(OutputStream os) throws IOException
  {
    os.write(">>\nendobj\n".getBytes());
  }
    
  /**
   * Returns the unique serial number in PDF format
   * @return the serial number in PDF format
   */
  public String toString()
  {
    return ""+objser+" 0 R";
  }
    
  /**
   * This utility method returns a String containing an array definition
   * based on a Vector containing PDFObjects
   * @param v Vector containing PDFObjects
   * @return String containing a PDF array
   */
  public static String toArray(Vector<? extends PDFObject> v)
  {
    if(v.size()==0)
      return "";
        
    StringBuffer b = new StringBuffer();
    String bs = "[";
    for(PDFObject x : v) {
      b.append(bs);
      b.append(x.toString());
      bs = " ";
    }
    b.append("]");
    return b.toString();
  }
}
