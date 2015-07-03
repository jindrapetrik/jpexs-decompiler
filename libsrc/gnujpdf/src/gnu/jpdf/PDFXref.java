/*
 *
 * $Id: PDFXref.java,v 1.2 2007/08/26 18:56:35 gil1 Exp $
 *
 * $Date: 2007/08/26 18:56:35 $
 *
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

/**
 * <p>This class is used to hold the xref information in the PDF
 * Trailer block.</p>
 *
 * <p>Basically, each object has an id, and an offset in the end file.</p>
 *
 * <p>See the Adobe PDF Manual for more information.  This class will 
 * normally not be used directly by a developer</p>
 *
 * @author Peter T. Mount
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.2 $, $Date: 2007/08/26 18:56:35 $
 *
 */
public class PDFXref
{

  /*
   * NOTE: Originally an inner class in PDF.java (now PDFDocument) written 
   * by Peter Mount for uk.org.retep.pdf
   */

  /**
   * The id of a PDF Object
   */
  public int id;

  /**
   * The offset within the PDF file
   */
  public int offset;
        
  /**
   * The generation of the object, usually 0
   */
  public int generation;
        
  /**
   * Creates a crossreference for a PDF Object
   * @param id The object's ID
   * @param offset The object's position in the file
   */
  public PDFXref(int id,int offset)
  {
    this(id,offset,0);
  }
        
  /**
   * Creates a crossreference for a PDF Object
   *
   * @param id The object's ID
   * @param offset The object's position in the file
   * @param generation The object's generation, usually 0
   */
  public PDFXref(int id,int offset,int generation)
  {
    this.id = id;
    this.offset = offset;
    this.generation = generation;
  }
        
  /**
   * @return The xref in the format of the xref section in the PDF file
   */
  public String toString()
  {
    String of = Integer.toString(offset);
    String ge = Integer.toString(generation);
    String rs = "0000000000".substring(0, 10-of.length()) + 
                of + 
                " " + 
                "00000".substring(0,5-ge.length())+ge;
    if(generation==65535)
      return rs+" f ";
    return rs+" n ";
  }

} // end class PDFXref
    
