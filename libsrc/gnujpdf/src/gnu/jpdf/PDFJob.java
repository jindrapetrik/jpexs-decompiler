/*
 * $Id: PDFJob.java,v 1.3 2007/08/26 18:56:35 gil1 Exp $
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.PrintGraphics;
import java.awt.PrintJob;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * <p>
 * This class extends awt's PrintJob, to provide a simple method of writing PDF
 * documents.</p>
 *
 * <p>
 * You can use this with any code that uses Java's printing mechanism. It does
 * include a few extra methods to provide access to some of PDF's features like
 * annotations, or outlines.</p>
 *
 *
 * @author Peter T Mount, http://www.retep.org.uk/pdf/
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.3 $, $Date: 2007/08/26 18:56:35 $
 */
public class PDFJob extends PrintJob implements Serializable {

    /*
   * NOTE: The original class is the work of Peter T. Mount, who released it 
   * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as 
   * follows: 
   * The package name was changed to gnu.jpdf
   * The formatting was changed a little bit.
   * This used to subclass an abstract class with the same name in 
   *   another package to support jdk1.1. Now it's one concrete class, 
   *   with no jdk1.1 support
   * Instances of PDFJob come directly from constructors, not 
   *   static methods in PDFDocument (which used to be PDF)
   * It is still licensed under the LGPL.
     */
    /**
     * This is the OutputStream the PDF file will be written to when complete
     * Note: This is transient, as it's not valid after being Serialized.
     */
    protected transient OutputStream os;

    /**
     * This is the PDF file being constructed
     */
    protected PDFDocument pdfDocument;

    /**
     * This is the current page being constructed by the last getGraphics() call
     */
    protected PDFPage page;

    /**
     * This is the page number of the current page
     */
    protected int pagenum;

    // Constructors
    /**
     * <p>
     * This constructs the job. This method must be used when creating a
     * template pdf file, ie one that is Serialised by one application, and then
     * restored by another.</p>
     *
     * <p>
     * ezb 20011115 - Haven't done anything with templates yet, don't know
     * how/if they are implemented</p>
     */
    public PDFJob() {
        this(null);
    }

    /**
     * <p>
     * This constructs the job. This is the primary constructor that will be
     * used for creating pdf documents with this package. The specified output
     * stream is a handle to the .pdf file you wish to create.</p>
     *
     * @param os - <code>OutputStream</code> to use for the pdf output
     */
    public PDFJob(OutputStream os) {
        this(os, "PDF Doc");
    }

    /**
     * <p>
     * This constructs the job. This is the primary constructor that will be
     * used for creating pdf documents with this package. The specified output
     * stream is a handle to the .pdf file you wish to create.</p>
     *
     * <p>
     * Use this constructor if you want to give the pdf document a name other
     * than the default of "PDF Doc"</p>
     *
     * @param os - <code>OutputStream</code> to use for the pdf output
     * @param title a <code>String</code> value
     */
    public PDFJob(OutputStream os, String title) {
        this.os = os;
        this.pdfDocument = new PDFDocument();
        pagenum = 0;
        pdfDocument.getPDFInfo().setTitle(title);
    }

    /**
     * <p>
     * This returns a graphics object that can be used to draw on a page. In
     * PDF, this will be a new page within the document.</p>
     *
     * @param orient - the <code>int</code> Orientation of the new page, as
     * defined in <code>PDFPage</code>
     * @return Graphics object to draw.
     * @see PageFormat#PORTRAIT
     * @see PageFormat#LANDSCAPE
     * @see PageFormat#REVERSE_LANDSCAPE
     */
    public Graphics getGraphics(int orient) {
        // create a new page
        page = new PDFPage(orient);
        pdfDocument.add(page);
        pagenum++;

        // Now create a Graphics object to draw onto the page
        return new graphic(page, this);
    }

    /**
     * <p>
     * This returns a graphics object that can be used to draw on a page. In
     * PDF, this will be a new page within the document.</p>
     *
     * @param pageFormat PageFormat describing the page size
     * @return Graphics object to draw.
     */
    public Graphics getGraphics(PageFormat pageFormat) {
        // create a new page
        page = new PDFPage(pageFormat);
        pdfDocument.add(page);
        pagenum++;

        // Now create a Graphics object to draw onto the page
        return new graphic(page, this);
    }

    /**
     * <p>
     * This writes the PDF document to the OutputStream, finishing the
     * document.</p>
     */
    @Override
    public void end() {
        try {
            pdfDocument.write(os);
        } catch (IOException ioe) {
            // Ideally we should throw this. However, PrintJob doesn't throw
            // anything, so we will print the Stack Trace instead.
            ioe.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // This should mark us as dead
        os = null;
        pdfDocument = null;
    }

    /**
     * <p>
     * This returns a graphics object that can be used to draw on a page. In
     * PDF, this will be a new page within the document.</p>
     *
     * <p>
     * This new page will by default be oriented as a portrait</p>
     *
     * @return a <code>Graphics</code> object to draw to.
     */
    @Override
    public Graphics getGraphics() {
        return getGraphics(PageFormat.PORTRAIT);
    }

    /**
     * <p>
     * Returns the page dimension</p>
     *
     * @return a <code>Dimension</code> instance, the size of the page
     */
    @Override
    public Dimension getPageDimension() {
        if (page == null) {
            System.err.println("PDFJob.getPageDimension(), page is null");
        }
        return page.getDimension();
    }

    /**
     * <p>
     * How about a setPageDimension(Rectangle media) ?? </p>
     */
    /**
     * This returns the page resolution.
     *
     * <p>
     * This is the PDF (and Postscript) device resolution of 72 dpi (equivalent
     * to 1 point).</p>
     *
     * @return an <code>int</code>, the resolution in pixels per inch
     */
    @Override
    public int getPageResolution() {
        return 72;
    }

    /**
     * <p>
     * In AWT's PrintJob, this would return true if the user requested that the
     * file is printed in reverse order. For PDF's this is not applicable, so it
     * will always return false.</p>
     *
     * @return false
     */
    @Override
    public boolean lastPageFirst() {
        return false;
    }

    //======== END OF PrintJob extension ==========
    /**
     * Returns the PDFDocument object for this document. Useful for gaining
     * access to the internals of PDFDocument.
     *
     * @return the PDF object
     */
    public PDFDocument getPDFDocument() {
        return pdfDocument;
    }

    /**
     * <p>
     * Returns the current PDFPage being worked on. Useful for working on
     * Annotations (like links), etc.</p>
     *
     * @return the <code>PDFPage</code> currently being constructed
     */
    public PDFPage getCurrentPage() {
        return page;
    }

    /**
     * <p>
     * Returns the current page number. Useful if you need to include one in the
     * document</p>
     *
     * @return the <code>int</code> current page number
     */
    public int getCurrentPageNumber() {
        return pagenum;
    }

    /**
     * <p>
     * This method attaches an outline to the current page being generated. When
     * selected, the outline displays the top of the page.</p>
     *
     * @param title a <code>String</code>, the title of the Outline
     * @return a <code>PDFOutline</code> object that was created, for adding
     * sub-outline's if required.
     */
    public PDFOutline addOutline(String title) {
        return page.addOutline(title);
    }

    /**
     * <p>
     * This method attaches an outline to the current page being generated. When
     * selected, the outline displays the specified region.</p>
     *
     * @param title Outline title to attach
     * @param x Left coordinate of region
     * @param y Top coordinate of region
     * @param w width of region
     * @param h height of region
     * @return the <code>PDFOutline</code> object created, for adding
     * sub-outline's if required.
     */
    public PDFOutline addOutline(String title, int x, int y, int w, int h) {
        return page.addOutline(title, x, y, w, h);
    }

    /**
     * Convenience method: Adds a text note to the document.
     *
     * @param note Text of the note
     * @param x Coordinate of note
     * @param y Coordinate of note
     * @param w Width of the note
     * @param h Height of the note
     * @return Returns the annotation, so other settings can be changed.
     */
    public PDFAnnot addNote(String note, int x, int y, int w, int h) {
        return page.addNote(note, x, y, w, h);
    }

    /**
     * <p>
     * This inner class extends PDFGraphics for the PrintJob.</p>
     *
     * <p>
     * Like with java.awt, Graphics instances created with PrintJob implement
     * the PrintGraphics interface. Here we implement that method, and override
     * PDFGraphics.create() method, so all instances have this interface.</p>
     */
    class graphic extends PDFGraphics implements PrintGraphics {

        /**
         * The PDFJob we are linked with
         */
        private PDFJob job;

        /**
         * @param page to attach to
         * @param job PDFJob containing this graphic
         */
        graphic(PDFPage page, PDFJob job) {
            super();
            this.init(page);
            this.job = job;
        }

        /**
         * This is used by our version of create()
         */
        graphic(PDFPage page, PDFJob job, RawPrintWriter pw) {
            super();
            this.init(page, pw);
            this.job = job;
        }

        /**
         * This returns a child instance of this Graphics object. As with AWT,
         * the affects of using the parent instance while the child exists, is
         * not determined.
         *
         * <p>
         * This method is used to make a new Graphics object without going to a
         * new page</p>
         *
         * <p>
         * Once complete, the child should be released with it's dispose()
         * method which will restore the graphics state to it's parent.
         *
         * @return Graphics object
         */
        @Override
        public Graphics create() {
            closeBlock();
            graphic g = new graphic(getPage(), job, getWriter());

            // The new instance inherits a few items
            g.clipRectangle = new Rectangle(clipRectangle);

            return (Graphics) g;
        }

        /**
         * This is the PrintGraphics interface
         *
         * @return PrintJob for this object
         */
        @Override
        public PrintJob getPrintJob() {
            return (PrintJob) job;
        }

    } // end inner class graphic

} // end class PDFJob

