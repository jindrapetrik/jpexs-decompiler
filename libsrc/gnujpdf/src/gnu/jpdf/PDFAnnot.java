/*
 * $Id: PDFAnnot.java,v 1.2 2007/08/26 18:56:35 gil1 Exp $
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
 */
package gnu.jpdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * <p>This class defines an annotation (commonly known as a Bookmark).</p>
 *
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @author Peter T Mount, http://www.retep.org.uk/pdf/
 * @version $Revision: 1.2 $, $Date: 2007/08/26 18:56:35 $
 */
public class PDFAnnot extends PDFObject implements Serializable
{
  /*
   * NOTE: The original class is the work of Peter T. Mount, who released it 
   * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as 
   * follows: the package name was changed to gnu.pdf.  It is still 
   * licensed under the LGPL.
   */

  /**
   * Solid border. The border is drawn as a solid line.
   */
  public static final short SOLID = 0;
  
  /**
   * The border is drawn with a dashed line.
   */
  public static final short DASHED = 1;
  
  /**
   * The border is drawn in a beveled style (faux three-dimensional) such
   * that it looks as if it is pushed out of the page (opposite of INSET)
   */
  public static final short BEVELED = 2;
  
  /**
   * The border is drawn in an inset style (faux three-dimensional) such
   * that it looks as if it is inset into the page (opposite of BEVELED)
   */
  public static final short INSET = 3;
  
  /**
   * The border is drawn as a line on the bottom of the annotation rectangle
   */
  public static final short UNDERLINED = 4;
  
  /**
   * The subtype of the outline, ie text, note, etc
   */
  private String subtype;
  
  /**
   * The size of the annotation
   */
  private int l,b,r,t;
  
  /**
   * The text of a text annotation
   */
  private String s;
  
  /**
   * flag used to indicate that the destination should fit the screen
   */
  private static final int FULL_PAGE = -9999;
  
  /**
   * Link to the Destination page 
   */
  private PDFObject dest;
  
  /**
   * If fl!=FULL_PAGE then this is the region of the destination page shown.
   * Otherwise they are ignored.
   */
  private int fl,fb,fr,ft;
  
  /**
   * the border for this annotation
   */
  private PDFBorder border;
  
  /**
   * This is used to create an annotation.
   * @param s Subtype for this annotation
   * @param l Left coordinate
   * @param b Bottom coordinate
   * @param r Right coordinate
   * @param t Top coordinate
   */
  protected PDFAnnot(String s,int l,int b,int r,int t) {
    super("/Annot");
    subtype = s;
    this.l = l;
    this.b = b;
    this.r = r;
    this.t = t;
  }
  
  /**
   * Creates a text annotation
   * @param l Left coordinate
   * @param b Bottom coordinate
   * @param r Right coordinate
   * @param t Top coordinate
   * @param s Text for this annotation
   */
  public PDFAnnot(int l,int b,int r,int t,String s) {
    this("/Text",l,b,r,t);
    this.s = s;
  }
  
  /**
   * Creates a link annotation
   * @param l Left coordinate
   * @param b Bottom coordinate
   * @param r Right coordinate
   * @param t Top coordinate
   * @param dest Destination for this link. The page will fit the display.
   */
  public PDFAnnot(int l,int b,int r,int t,PDFObject dest) {
    this("/Link",l,b,r,t);
    this.dest = dest;
    this.fl = FULL_PAGE; // this is used to indicate a full page
  }
  
  /**
   * Creates a link annotation
   * @param l Left coordinate
   * @param b Bottom coordinate
   * @param r Right coordinate
   * @param t Top coordinate
   * @param dest Destination for this link
   * @param fl Left coordinate
   * @param fb Bottom coordinate
   * @param fr Right coordinate
   * @param ft Top coordinate
   * <br><br>Rectangle describing what part of the page to be displayed
   * (must be in User Coordinates)
   */
  public PDFAnnot(int l,int b,int r,int t,
                  PDFObject dest,
                  int fl,int fb,int fr,int ft
                  ) {
    this("/Link",l,b,r,t);
    this.dest = dest;
    this.fl = fl;
    this.fb = fb;
    this.fr = fr;
    this.ft = ft;
  }
  
  /**
   * Sets the border for the annotation. By default, no border is defined.
   *
   * <p>If the style is DASHED, then this method uses PDF's default dash
   * scheme {3}
   *
   * <p>Important: the annotation must have been added to the document before
   * this is used. If the annotation was created using the methods in
   * PDFPage, then the annotation is already in the document.
   *
   * @param style Border style SOLID, DASHED, BEVELED, INSET or UNDERLINED.
   * @param width Width of the border
   */
  public void setBorder(short style,double width) {
    border = new PDFBorder(style,width);
    pdfDocument.add(border);
  }
  
  /**
   * Sets the border for the annotation. Unlike the other method, this
   * produces a dashed border.
   *
   * <p>Important: the annotation must have been added to the document before
   * this is used. If the annotation was created using the methods in
   * PDFPage, then the annotation is already in the document.
   *
   * @param width Width of the border
   * @param dash Array of lengths, used for drawing the dashes. If this
   * is null, then the default of {3} is used.
   */
  public void setBorder(double width,double dash[]) {
    border = new PDFBorder(width,dash);
    pdfDocument.add(border);
  }
  
  /**
   * Should this be public??
   *
   * @param os OutputStream to send the object to
   * @exception IOException on error
   */
  public void write(OutputStream os) throws IOException {
    // Write the object header
    writeStart(os);
    
    // now the objects body
    os.write("/Subtype ".getBytes());
    os.write(subtype.getBytes());
    os.write("\n/Rect [".getBytes());
    os.write(Integer.toString(l).getBytes());
    os.write(" ".getBytes());
    os.write(Integer.toString(b).getBytes());
    os.write(" ".getBytes());
    os.write(Integer.toString(r).getBytes());
    os.write(" ".getBytes());
    os.write(Integer.toString(t).getBytes());
    os.write("]\n".getBytes());
    
    // handle the border
    if(border==null) {
      os.write("/Border [0 0 0]\n".getBytes());
      //if(pdf.defaultOutlineBorder==null)
      //pdf.add(pdf.defaultOutlineBorder = new border(SOLID,0.0));
      //os.write(pdf.defaultOutlineBorder.toString().getBytes());
    } else {
      os.write("/BS ".getBytes());
      os.write(border.toString().getBytes());
      os.write("\n".getBytes());
    }
    
    // Now the annotation subtypes
    if(subtype.equals("/Text")) {
      os.write("/Contents ".getBytes());
      os.write(PDFStringHelper.makePDFString(s).getBytes());
      os.write("\n".getBytes());
    } else if(subtype.equals("/Link")) {
      os.write("/Dest [".getBytes());
      os.write(dest.toString().getBytes());
      if(fl==FULL_PAGE)
        os.write(" /Fit]".getBytes());
      else {
        os.write(" /FitR ".getBytes());
        os.write(Integer.toString(fl).getBytes());
        os.write(" ".getBytes());
        os.write(Integer.toString(fb).getBytes());
        os.write(" ".getBytes());
        os.write(Integer.toString(fr).getBytes());
        os.write(" ".getBytes());
        os.write(Integer.toString(ft).getBytes());
        os.write("]".getBytes());
      }
      os.write("\n".getBytes());
    }
    
    // finish off with its footer
    writeEnd(os);
  }
}
