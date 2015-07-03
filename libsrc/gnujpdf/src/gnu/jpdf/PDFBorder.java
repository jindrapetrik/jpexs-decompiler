/*
 * $Id: PDFBorder.java,v 1.2 2007/08/26 18:56:35 gil1 Exp $
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

/**
 * <p>A border around an annotation </p>
 *
 *
 * @author Peter T Mount, http://www.retep.org.uk/pdf/
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.2 $
 */
public class PDFBorder extends PDFObject
{
    /*
     * NOTE: The original class is the work of Peter T. Mount, who released it 
     * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as 
     * follows: the package name was changed to gnu.pdf.  It is still 
     * licensed under the LGPL.
   */

    /**
     * The style of the border
     */
    private short style;
    
    /**
     * The width of the border
     */
    private double width;
    
    /**
     * This array allows the definition of a dotted line for the border
     */
    private double dash[];
    
    /**
     * Creates a border using the predefined styles in PDFAnnot.
     * <p>Note: Do not use PDFAnnot.DASHED with this method.
     * Use the other constructor.
     *
     * @param style The style of the border
     * @param width The width of the border
     * @see PDFAnnot
     */
    public PDFBorder(short style,double width) {
        super("/Border");
        this.style = style;
        this.width = width;
    }
    
    /**
     * Creates a border of style PDFAnnot.DASHED
     *
     * @param width The width of the border
     * @param dash The line pattern definition
     */
    public PDFBorder(double width,double dash[]) {
        super("/Border");
        this.style = PDFAnnot.DASHED;
        this.width = width;
        this.dash = dash;
    }
    
    /**
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    public void write(OutputStream os) throws IOException {
        //writeStart(os);
        os.write(Integer.toString(objser).getBytes());
        os.write(" 0 obj\n".getBytes());
      
        os.write("[/S /".getBytes());
        os.write("SDBIU".substring(style,style+1).getBytes());
        os.write(" /W ".getBytes());
        os.write(Double.toString(width).getBytes());
        if(dash!=null) {
            os.write(" /D [".getBytes());
            os.write(Double.toString(dash[0]).getBytes());
            for(int i=1;i<dash.length;i++) {
                os.write(" ".getBytes());
                os.write(Double.toString(dash[i]).getBytes());
            }
            os.write("] ".getBytes());
        }
        os.write("]\n".getBytes());
      
        //writeEnd(os);
        os.write("endobj\n".getBytes());
    }  
} // end class PDFBorder
