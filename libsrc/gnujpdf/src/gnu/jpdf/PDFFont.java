/*
 * $Id: PDFFont.java,v 1.3 2007/08/26 19:00:11 gil1 Exp $
 *
 * $Date: 2007/08/26 19:00:11 $
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

import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This class defines a font within a PDF document.
 *
 * @author Peter T Mount,http://www.retep.org.uk/pdf/
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @author Gilbert DeLeeuw, gil1@users.sourceforge.net
 * @version $Revision: 1.3 $, $Date: 2007/08/26 19:00:11 $
 */
public class PDFFont extends PDFObject implements Serializable
{

  /*
   * NOTE: The original class is the work of Peter T. Mount, who released it 
   * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as 
   * follows: 
   * The package name was changed to gnu.pdf.  
   * The formatting was changed a little bit
   * It is still licensed under the LGPL.
   */
  
  /**
   * The PDF document name of the font
   */
  private String name;
  
  /**
   * The PDF type of the font, usually /Type1
   */
  private String type;
  
  /**
   * The font's real name
   */
  private String font;
  
  /**
   * The name of the equivalent Java font
   */
  private String javaFont;
  
  /** 
   * The PDF Style, ie: BOLD, ITALIC, etc
   */
  private int style;
  
  /**
   * This constructs a default PDFFont. In this case Helvetica
   */
  protected PDFFont() {
    this("/F1","/Type1","Helvetica",Font.PLAIN);
  }
  
  /**
   * Constructs a PDFFont. This will attempt to map the font from a known
   * Java font name to that in PDF, defaulting to Helvetica if not possible.
   *
   * @param name The document name, ie /F1
   * @param type The pdf type, ie /Type1
   * @param font The font name, ie Helvetica
   * @param style The java.awt.Font style, ie: Font.PLAIN
   */
  public PDFFont(String name,String type,String font,int style) {
    super("/Font");
    this.name = name;
    this.type = type;
    this.style = style;
    
    String f = font.toLowerCase();
    
    // default PDF Font name
//    this.font = base14[0][1];
//    this.javaFont = base14[0][0];
    this.font = font;
    this.javaFont = "/" + font;
    
    // attempt to translate the font name from Java to PDF
    for(int i=0;i<base14.length;i++) {
      if(base14[i][0].equals(f)) {
        this.javaFont = base14[i][0];
        this.font =  base14[i][1+style];
        //System.out.println("Setting a font style to: " + this.font);
        break;
      }
    }
  }
  
  /**
   * This is the most common method to use.
   * @return the Font name within the PDF document.
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return the Font's PDF type
   */
  public String getType() {
    return type;
  }
  
  /**
   * @return The PDF Font name
   */
  public String getFont() {
    return font;
  }
  
  /**
   * @return the font style.
   * @see java.awt.Font
   */
  public int getStyle() {
    return style;
  }
  
  /**
   * @param os OutputStream to send the object to
   * @exception IOException on error
   */
  public void write(OutputStream os) throws IOException {
    // Write the object header
    writeStart(os);
    
    // now the objects body
    os.write("/Subtype ".getBytes());
    os.write(type.getBytes());
    os.write("\n/Name ".getBytes());
    os.write(name.getBytes());
    os.write("\n/BaseFont ".getBytes());
    os.write(font.getBytes());
    // The performance problem in Bug#106693 comments out the
    // encoding line, and removes the /WinAnsiEncoding. I'm going
    // to leave them in, as the Encoding fixes another problem.
    os.write("\n/Encoding ".getBytes());
    os.write("/WinAnsiEncoding".getBytes());
    //os.write(encoding.getBytes());
    os.write("\n".getBytes());
    
    // finish off with its footer
    writeEnd(os);
  }
  
  /**
   * This is used by the PDF and PDFPage classes to compare font names
   *
   * @param type The pdf type, ie /Type1
   * @param font The font name, ie Helvetica
   * @param style The java.awt.Font style, ie: Font.PLAIN
   * @return true if this object is identical to this font's spec
   */
  protected boolean equals(String type,String font,int style) {
    return this.type.equals(type)
      && (this.font.equalsIgnoreCase(font)
          || this.javaFont.equalsIgnoreCase(font));
    // new styles not being picked up - ezb june 6 2001
    // || this.javaFont.equalsIgnoreCase(font));
    
    // Removed in fix for Bug#106693
    //why? - ezb - can't find bug in bug tracker
    //&& this.style==style;
  }
  
  /**
   * This maps the standard JDK1.1 font names and styles to
   * the base 14 PDF fonts
   */
  private static String[][] base14 = {
	// java name    
		// NORMAL          
		// BOLD            
		// ITALIC                  
		// BOLD+ITALIC
		{
			"arial",
			"/Helvetica",
			"/Helvetica-Bold",
			"/Helvetica-Oblique",
			"/Helvetica-BoldOblique" },
		{
			"sansserif",
			"/Helvetica",
			"/Helvetica-Bold",
			"/Helvetica-Oblique",
			"/Helvetica-BoldOblique" },
		{
			"monospaced",
			"/Courier",
			"/Courier-Bold",
			"/Courier-Oblique",
			"/Courier-BoldOblique" },
		{
			"timesroman",
			"/Times-Roman",
			"/Times-Bold",
			"/Times-Italic",
			"/Times-BoldItalic" },
		{
			"courier",
			"/Courier",
			"/Courier-Bold",
			"/Courier-Oblique",
			"/Courier-BoldOblique" },
		{
			"helvetica",
			"/Helvetica",
			"/Helvetica-Bold",
			"/Helvetica-Oblique",
			"/Helvetica-BoldOblique" },
		{
			"dialog",
			"/Courier",
			"/Courier-Bold",
			"/Courier-Oblique",
			"/Courier-BoldOblique" },
		{
			"dialoginput",
			"/Courier",
			"/Courier-Bold",
			"/Courier-Oblique",
			"/Courier-BoldOblique" }, };
  
}

