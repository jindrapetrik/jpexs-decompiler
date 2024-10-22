/*
 * $Id: PDFPage.java,v 1.5 2007/09/22 12:58:40 gil1 Exp $
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
 */
package gnu.jpdf;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * <p>
 * This class defines a single page within a document. It is linked to a single
 * PDFGraphics object</p>
 *
 * @author Peter T Mount
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @author Gilbert DeLeeuw, gil1@users.sourceforge.net
 * @version $Revision: 1.5 $, $Date: 2007/09/22 12:58:40 $
 *
 *
 */
public class PDFPage extends PDFObject implements Serializable {

    /*
   * NOTE: The original class is the work of Peter T. Mount, who released it
   * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as
   * follows:
   * The package name was changed to gnu.pdf.
   * The formatting was changed a little bit.
   * It is still licensed under the LGPL.
     */
    /**
     * Default page format (Letter size with 1 inch margins and Portrait
     * orientation)
     */
    private static final PageFormat DEF_FORMAT = new PageFormat();

    /**
     * This is this page format, ie the size of the page, margins, and rotation
     */
    protected PageFormat pageFormat;

    /**
     * This is the pages object id that this page belongs to. It is set by the
     * pages object when it is added to it.
     */
    protected PDFObject pdfPageList;

    /**
     * This holds the contents of the page.
     */
    protected Vector<PDFObject> contents;

    /**
     * Object ID that contains a thumbnail sketch of the page. -1 indicates no
     * thumbnail.
     */
    protected PDFObject thumbnail;

    /**
     * This holds any Annotations contained within this page.
     */
    protected Vector<PDFObject> annotations;

    /**
     * This holds any resources for this page
     */
    protected Vector<String> resources;

    // JM
    protected Vector<String> imageResources;

    protected Vector<String> patternResources;

    protected Vector<String> shadingResources;

    protected Vector<String> extGStateResources;

    /**
     * The fonts associated with this page
     */
    protected Vector<PDFFont> fonts;

    /**
     * The xobjects or other images in the pdf
     */
//    protected Vector xobjects;
    /**
     * These handle the procset for this page. Refer to page 140 of the PDF
     * Reference manual NB: Text is handled when the fonts Vector is null, and a
     * font is created refer to getFont() to see where it's defined
     */
    protected boolean hasImageB, hasImageC, hasImageI;
    protected procset procset;

    /**
     * This constructs a Page object, which will hold any contents for this
     * page.
     *
     * <p>
     * Once created, it is added to the document via the PDF.add() method. (For
     * Advanced use, via the PDFPages.add() method).
     *
     * <p>
     * This defaults to a4 media.
     */
    public PDFPage() {
        super("/Page");
        pageFormat = DEF_FORMAT;
        contents = new Vector<PDFObject>();
        thumbnail = null;
        annotations = new Vector<PDFObject>();
        resources = new Vector<String>();
        // JM
        imageResources = new Vector<String>();
        patternResources = new Vector<>();
        shadingResources = new Vector<>();
        extGStateResources = new Vector<>();
        fonts = new Vector<PDFFont>();
        procset = null;
    }

    /**
     * Constructs a page using A4 media, but using the supplied orientation.
     *
     * @param orientation Orientation: 0, 90 or 270
     * @see PageFormat#PORTRAIT
     * @see PageFormat#LANDSCAPE
     * @see PageFormat#REVERSE_LANDSCAPE
     */
    public PDFPage(int orientation) {
        this();
        setOrientation(orientation);
    }

    /**
     * Constructs a page using the supplied media size and orientation.
     *
     * @param pageFormat PageFormat describing the page size
     */
    public PDFPage(PageFormat pageFormat) {
        this();
        this.pageFormat = pageFormat;
    }

    /**
     * Adds to procset.
     *
     * @param proc the String to be added.
     */
    public void addToProcset(String proc) {
        if (procset == null) {
            addProcset();
        }
        procset.add(proc);
    }

    /**
     * This returns a PDFGraphics object, which can then be used to render on to
     * this page. If a previous PDFGraphics object was used, this object is
     * appended to the page, and will be drawn over the top of any previous
     * objects.
     *
     * @return a new PDFGraphics object to be used to draw this page.
     */
    public PDFGraphics getGraphics() {
        try {
            PDFGraphics g = new PDFGraphics();
            g.init(this);
            return g;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Returns a PDFFont, creating it if not yet used.
     *
     * @param type Font type, usually /Type1
     * @param font Font name
     * @param style java.awt.Font style, ie Font.NORMAL
     * @return a PDFFont object.
     */
    public PDFFont getFont(String type, String font, int style) {
        // Search the fonts on this page, and return one that matches this
        // font.
        // This keeps the number of font definitions down to one per font/style
        for (PDFFont ft : fonts) {
            if (ft.equals(type, font, style)) {
                return ft;
            }
        }

        // Ok, the font isn't in the page, so create one.
        // We need a procset if we are using fonts, so create it (if not
        // already created, and add to our resources
        if (fonts.size() == 0) {
            addProcset();
            procset.add("/Text");
        }

        // finally create and return the font
        PDFFont f = pdfDocument.getFont(type, font, style);
        fonts.addElement(f);
        return f;
    }

    public PDFFont getEmbeddedFont(String font, int style, File file) throws IOException {
        // Search the fonts on this page, and return one that matches this
        // font.
        // This keeps the number of font definitions down to one per font/style
        for (PDFFont ft : fonts) {
            if (ft.equals("/TrueType", font, style)) {
                return ft;
            }
        }

        // Ok, the font isn't in the page, so create one.
        // We need a procset if we are using fonts, so create it (if not
        // already created, and add to our resources
        if (fonts.size() == 0) {
            addProcset();
            procset.add("/Text");
        }

        // finally create and return the font
        PDFFont f = pdfDocument.getEmbeddedFont(font, style, file);
        fonts.addElement(f);
        return f;
    }

    /**
     * Returns the page's PageFormat.
     *
     * @return PageFormat describing the page size in device units (72dpi)
     */
    public PageFormat getPageFormat() {
        return pageFormat;
    }

    /**
     * Gets the dimensions of the page.
     *
     * @return a Dimension object containing the width and height of the page.
     */
    public Dimension getDimension() {
        return new Dimension((int) pageFormat.getWidth(), (int) pageFormat
                .getHeight());
    }

    /**
     * Gets the imageable area of the page.
     *
     * @return a Rectangle containing the bounds of the imageable area.
     */
    public Rectangle getImageableArea() {
        return new Rectangle((int) pageFormat.getImageableX(), (int) pageFormat
                .getImageableY(),
                (int) (pageFormat.getImageableX() + pageFormat
                .getImageableWidth()), (int) (pageFormat
                        .getImageableY() + pageFormat.getImageableHeight()));
    }

    /**
     * Sets the page's orientation.
     *
     * <p>
     * Normally, this should be done when the page is created, to avoid
     * problems.
     *
     * @param orientation a PageFormat orientation constant:
     * PageFormat.PORTRAIT, PageFormat.LANDSACPE or PageFormat.REVERSE_LANDSACPE
     */
    public void setOrientation(int orientation) {
        pageFormat.setOrientation(orientation);
    }

    /**
     * Returns the pages orientation: PageFormat.PORTRAIT, PageFormat.LANDSACPE
     * or PageFormat.REVERSE_LANDSACPE
     *
     * @see java.awt.print.PageFormat
     * @return current orientation of the page
     */
    public int getOrientation() {
        return pageFormat.getOrientation();
    }

    /**
     * This adds an object that describes some content to this page.
     *
     * <p>
     * <b>Note:</b> Objects that describe contents must be added using this
     * method _AFTER_ the PDF.add() method has been called.
     *
     * @param ob PDFObject describing some contents
     */
    public void add(PDFObject ob) {
        contents.addElement(ob);
    }

    /**
     * This adds an Annotation to the page.
     *
     * <p>
     * As with other objects, the annotation must be added to the pdf document
     * using PDF.add() before adding to the page.
     *
     * @param ob Annotation to add.
     */
    public void addAnnotation(PDFObject ob) {
        annotations.addElement(ob);
    }

    /**
     * This method adds a text note to the document.
     *
     * @param note Text of the note
     * @param x Coordinate of note
     * @param y Coordinate of note
     * @param w Width of the note
     * @param h Height of the note
     * @return Returns the annotation, so other settings can be changed.
     */
    public PDFAnnot addNote(String note, int x, int y, int w, int h) {
        int xy1[] = cxy(x, y + h);
        int xy2[] = cxy(x + w, y);
        PDFAnnot ob = new PDFAnnot(xy1[0], xy1[1],
                xy2[0], xy2[1],
                note);
        pdfDocument.add(ob);
        annotations.addElement(ob);
        return ob;
    }

    /**
     * Adds a hyperlink to the document.
     *
     * @param x Coordinate of active area
     * @param y Coordinate of active area
     * @param w Width of the active area
     * @param h Height of the active area
     * @param dest Page that will be displayed when the link is activated. When
     * displayed, the zoom factor will be changed to fit the display.
     * @return Returns the annotation, so other settings can be changed.
     */
    public PDFAnnot addLink(int x, int y, int w, int h, PDFObject dest) {
        int xy1[] = cxy(x, y + h);
        int xy2[] = cxy(x + w, y);
        PDFAnnot ob = new PDFAnnot(xy1[0], xy1[1],
                xy2[0], xy2[1],
                dest
        );
        pdfDocument.add(ob);
        annotations.addElement(ob);
        return ob;
    }

    /**
     * Adds a hyperlink to the document.
     *
     * @param x Coordinate of active area
     * @param y Coordinate of active area
     * @param w Width of the active area
     * @param h Height of the active area
     * @param dest Page that will be displayed when the link is activated
     * @param vx Coordinate of view area
     * @param vy Coordinate of view area
     * @param vw Width of the view area
     * @param vh Height of the view area
     * @return Returns the annotation, so other settings can be changed.
     */
    public PDFAnnot addLink(int x, int y, int w, int h,
            PDFObject dest,
            int vx, int vy, int vw, int vh) {
        int xy1[] = cxy(x, y + h);
        int xy2[] = cxy(x + w, y);
        int xy3[] = cxy(vx, vy + vh);
        int xy4[] = cxy(vx + vw, vy);
        PDFAnnot ob = new PDFAnnot(xy1[0], xy1[1],
                xy2[0], xy2[1],
                dest,
                xy3[0], xy3[1],
                xy4[0], xy4[1]
        );
        pdfDocument.add(ob);
        annotations.addElement(ob);
        return ob;
    }

    /**
     * Contains the text strings for the xobjects.
     */
    private Vector<String> xobjects = new Vector<String>();

    /**
     * This adds an XObject resource to the page. The string should be of the
     * format /Name ObjectNumber RevisionNumber R as in /Image1 13 0 R .
     *
     * @param inxobject the XObject resource to be added.
     */
    public void addXObject(String inxobject) {
        xobjects.addElement(inxobject);
    }

    /**
     * This adds a resource to the page.
     *
     * @param resource String defining the resource
     */
    public void addResource(String resource) {
        resources.addElement(resource);
    }

    // JM
    /**
     * This adds an image resource to the page.
     *
     * @param resource the XObject resource to be added.
     */
    public void addImageResource(String resource) {
        imageResources.addElement(resource);
    }

    public void addPatternResource(String resource) {
        patternResources.add(resource);
    }

    public void addShadingResource(String resource) {
        shadingResources.add(resource);
    }

    public void addExtGStateResource(String resource) {
        if (!extGStateResources.contains(resource)) {
            extGStateResources.add(resource);
        }
    }

    /**
     * This adds an object that describes a thumbnail for this page.
     * <p>
     * <b>Note:</b> The object must already exist in the PDF, as only the object
     * ID is stored.
     *
     * @param thumbnail PDFObject containing the thumbnail
     */
    public void setThumbnail(PDFObject thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * This method attaches an outline to the current page being generated. When
     * selected, the outline displays the top of the page.
     *
     * @param title Outline title to attach
     * @return PDFOutline object created, for addSubOutline if required.
     */
    public PDFOutline addOutline(String title) {
        PDFOutline outline = new PDFOutline(title, this);
        pdfDocument.add(outline);
        pdfDocument.getOutline().add(outline);
        return outline;
    }

    /**
     * This method attaches an outline to the current page being generated. When
     * selected, the outline displays the top of the page.
     *
     * <p>
     * Note: If the outline is not in the top level (ie below another outline)
     * then it must <b>not</b> be passed to this method.
     *
     * @param title Outline title to attach
     * @param x Left coordinate of region
     * @param y Bottom coordinate of region
     * @param w Width of region
     * @param h Height coordinate of region
     * @return PDFOutline object created, for addSubOutline if required.
     */
    public PDFOutline addOutline(String title, int x, int y, int w, int h) {
        int xy1[] = cxy(x, y + h);
        int xy2[] = cxy(x + w, y);
        PDFOutline outline = new PDFOutline(title, this,
                xy1[0], xy1[1],
                xy2[0], xy2[1]);
        pdfDocument.add(outline);
        pdfDocument.getOutline().add(outline);
        return outline;
    }

    /**
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    @Override
    public void write(OutputStream os) throws IOException {
        // Write the object header
        writeStart(os);

        // now the objects body
        // the /Parent pages object
        os.write("/Parent ".getBytes("UTF-8"));
        os.write(pdfPageList.toString().getBytes("UTF-8"));
        os.write("\n".getBytes("UTF-8"));

        // the /MediaBox for the page size
        os.write("/MediaBox [".getBytes("UTF-8"));
        os.write(Integer.toString(0).getBytes("UTF-8"));
        os.write(" ".getBytes("UTF-8"));
        os.write(Integer.toString(0).getBytes("UTF-8"));
        os.write(" ".getBytes("UTF-8"));
        os.write(Integer.toString((int) pageFormat.getWidth()).getBytes("UTF-8"));
        os.write(" ".getBytes("UTF-8"));
        os.write(Integer.toString((int) pageFormat.getHeight()).getBytes("UTF-8"));
        os.write("]\n".getBytes("UTF-8"));

        // Rotation (if not zero)
//        if(rotate!=0) {
//            os.write("/Rotate ".getBytes("UTF-8"));
//            os.write(Integer.toString(rotate).getBytes("UTF-8"));
//            os.write("\n".getBytes("UTF-8"));
//        }
        writeResources(os);

        // The thumbnail
        if (thumbnail != null) {
            os.write("/Thumb ".getBytes("UTF-8"));
            os.write(thumbnail.toString().getBytes("UTF-8"));
            os.write("\n".getBytes("UTF-8"));
        }

        // the /Contents pages object
        if (contents.size() > 0) {
            if (contents.size() == 1) {
                PDFObject ob = (PDFObject) contents.elementAt(0);
                os.write("/Contents ".getBytes("UTF-8"));
                os.write(ob.toString().getBytes("UTF-8"));
                os.write("\n".getBytes("UTF-8"));
            } else {
                os.write("/Contents [".getBytes("UTF-8"));
                os.write(PDFObject.toArray(contents).getBytes("UTF-8"));
                os.write("\n".getBytes("UTF-8"));
            }
        }

        // The /Annots object
        if (annotations.size() > 0) {
            os.write("/Annots ".getBytes("UTF-8"));
            os.write(PDFObject.toArray(annotations).getBytes("UTF-8"));
            os.write("\n".getBytes("UTF-8"));
        }

        // finish off with its footer
        writeEnd(os);
    }

    public void writeResources(OutputStream os) throws IOException {
        // Now the resources
        os.write("/Resources << ".getBytes("UTF-8"));
        // fonts
        if (fonts.size() > 0) {
            //os.write("/Font << ".getBytes("UTF-8"));
            os.write("\n/Font << ".getBytes("UTF-8"));
            for (PDFFont font : fonts) {
                os.write(font.getName().getBytes("UTF-8"));
                os.write(" ".getBytes("UTF-8"));
                os.write(font.toString().getBytes("UTF-8"));
                os.write(" ".getBytes("UTF-8"));
            }
            os.write(">> ".getBytes("UTF-8"));
        }
        // Now the XObjects
        if (xobjects.size() > 0) {
            os.write("\n/XObject << ".getBytes("UTF-8"));
            for (String str : xobjects) {
                os.write(str.getBytes("UTF-8"));
                os.write(" ".getBytes("UTF-8"));
            }
            os.write(">> ".getBytes("UTF-8"));
        }
        // Any other resources
        for (String str : resources) {
            os.write(str.getBytes("UTF-8"));
            os.write(" ".getBytes("UTF-8"));
        }
        // JM
        if (imageResources.size() > 0) {
            os.write("/XObject << ".getBytes("UTF-8"));
            for (String str : imageResources) {
                os.write(str.getBytes("UTF-8"));
                os.write(" ".getBytes("UTF-8"));
            }
            os.write(" >> ".getBytes("UTF-8"));
        }

        if (patternResources.size() > 0) {
            os.write("/Pattern << ".getBytes("UTF-8"));
            for (String str : patternResources) {
                os.write(str.getBytes("UTF-8"));
                os.write(" ".getBytes("UTF-8"));
            }
            os.write(" >> ".getBytes("UTF-8"));
        }

        if (shadingResources.size() > 0) {
            os.write("/Shading << ".getBytes("UTF-8"));
            for (String str : shadingResources) {
                os.write(str.getBytes("UTF-8"));
                os.write(" ".getBytes("UTF-8"));
            }
            os.write(" >> ".getBytes("UTF-8"));
        }

        if (extGStateResources.size() > 0) {
            os.write("/ExtGState << ".getBytes("UTF-8"));
            for (String str : extGStateResources) {
                os.write(str.getBytes("UTF-8"));
                os.write(" ".getBytes("UTF-8"));
            }
            os.write(" >> ".getBytes("UTF-8"));
        }
        os.write(">>\n".getBytes("UTF-8"));
    }

    /**
     * This creates a procset and sets up the page to reference it
     */
    private void addProcset() {
        if (procset == null) {
            pdfDocument.add(procset = new procset());
            resources.addElement("/ProcSet " + procset);
        }
    }

    /**
     * This defines a procset
     */
    public class procset extends PDFObject {

        private Vector<String> set;

        /**
         * Creates a new procset object.
         */
        public procset() {
            super(null);
            set = new Vector<String>();

            // Our default procset (use addElement not add, as we dont want a
            // leading space)
            set.addElement("/PDF");
        }

        /**
         * @param proc Entry to add to the procset
         */
        public void add(String proc) {
            set.addElement(" " + proc);
        }

        /**
         * @param os OutputStream to send the object to
         * @exception IOException on error
         */
        @Override
        public void write(OutputStream os) throws IOException {
            // Write the object header
            //writeStart(os);

            os.write(Integer.toString(objser).getBytes("UTF-8"));
            os.write(" 0 obj\n".getBytes("UTF-8"));

            // now the objects body
            os.write("[".getBytes("UTF-8"));
            for (String str : set) {
                os.write(str.getBytes("UTF-8"));
            }
            os.write("]\n".getBytes("UTF-8"));

            // finish off with its footer
            //writeEnd(os);
            os.write("endobj\n".getBytes("UTF-8"));
        }
    }

    /**
     * This utility method converts the y coordinate from Java to User space
     * within the page.
     *
     * @param x Coordinate in Java space
     * @param y Coordinate in Java space
     * @return y Coordinate in User space
     */
    public int cy(int x, int y) {
        return cxy(x, y)[1];
    }

    /**
     * This utility method converts the y coordinate from Java to User space
     * within the page.
     *
     * @param x Coordinate in Java space
     * @param y Coordinate in Java space
     * @return x Coordinate in User space
     */
    public int cx(int x, int y) {
        return cxy(x, y)[0];
    }

    /**
     * This utility method converts the Java coordinates to User space within
     * the page.
     *
     * @param x Coordinate in Java space
     * @param y Coordinate in Java space
     * @return array containing the x & y Coordinate in User space
     */
    public int[] cxy(int x, int y) {
        int r[] = new int[2];
        r[0] = x;
        r[1] = (int) pageFormat.getHeight() - y;
        return r;
    }

}
