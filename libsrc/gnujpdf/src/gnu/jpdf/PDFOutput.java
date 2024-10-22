/*
 * $Id: PDFOutput.java,v 1.3 2007/09/22 12:48:16 gil1 Exp $
 *
 * $Date: 2007/09/22 12:48:16 $
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
 * This class is used to write a PDF document. It acts as a wrapper to a real
 * OutputStream, but is necessary for certain internal PDF structures to be
 * built correctly.
 *
 * @author Peter T. Mount
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.3 $, $Date: 2007/09/22 12:48:16 $
 */
public class PDFOutput {

    /**
     * This is the actual OutputStream used to write to.
     */
    protected OutputStream os;

    /**
     * This is the OutputStream used to write each object to.
     *
     * <p>
     * We use a separate stream, because we need to keep track of how many bytes
     * have been written for each object for the xref table to work correctly.
     */
    protected ByteArrayOutputStream baos;

    /**
     * This is the current position within the stream
     */
    protected int offset;

    /**
     * This vector contains offsets of each object
     */
    protected Vector<PDFXref> offsets;

    /**
     * This is used to track the /Root object (catalog)
     */
    protected PDFObject rootID;

    /**
     * This is used to track the /Info object (info)
     */
    protected PDFObject infoID;

    /**
     * This creates a PDF OutputStream
     *
     * @param os The output stream to write the PDF file to.
     * @throws IOException if there is an I/O error.
     */
    public PDFOutput(OutputStream os) throws IOException {
        this.os = os;
        offset = 0;
        offsets = new Vector<PDFXref>();
        baos = new ByteArrayOutputStream();

        // Now write the PDF header
        //
        // Note: As the encoding is fixed here, we use getBytes().
        //
        baos.write("%PDF-1.2\n".getBytes("UTF-8"));

        // This second comment is advised in the PDF Reference manual
        // page 61
        baos.write("%\342\343\317\323\n".getBytes("UTF-8"));

        offset = baos.size();
        baos.writeTo(os);
    }

    /**
     * This method writes a PDFObject to the stream.
     *
     * @param ob PDFObject Object to write
     * @exception IOException on error
     */
    protected void write(PDFObject ob) throws IOException {
        // Check the object to see if it's one that is needed in the trailer
        // object
        if (ob instanceof PDFCatalog) {
            rootID = ob;
        }
        if (ob instanceof PDFInfo) {
            infoID = ob;
        }

        offsets.addElement(new PDFXref(ob.getSerialID(), offset));
        baos.reset();
        ob.write(baos);
        offset += baos.size();
        baos.writeTo(os);
    }

    /**
     * This closes the Stream, writing the xref table
     */
    protected void close() throws IOException {
        // Make sure everything is written
        os.flush();

        // we use baos to speed things up a little.
        // Also, offset is preserved, and marks the beginning of this block.
        // This is required by PDF at the end of the PDF file.
        baos.reset();
        baos.write("xref\n".getBytes("UTF-8"));

        // Now a single subsection for object 0
        //baos.write("0 1\n0000000000 65535 f \n".getBytes("UTF-8"));
        // Now scan through the offsets list. The should be in sequence,
        // but just in case:
        int firstid = 0;                    // First id in block
        int lastid = -1;                    // The last id used
        Vector<PDFXref> block = new Vector<PDFXref>();        // xrefs in this block

        // We need block 0 to exist
        block.addElement(new PDFXref(0, 0, 65535));

        for (PDFXref x : offsets) {

            if (firstid == -1) {
                firstid = x.id;
            }

            // check to see if block is in range (-1 means empty)
            if (lastid > -1 && x.id != (lastid + 1)) {
                // no, so write this block, and reset
                writeblock(firstid, block);
                block.removeAllElements();
                firstid = -1;
            }

            // now add to block
            block.addElement(x);
            lastid = x.id;
        }

        // now write the last block
        if (firstid > -1) {
            writeblock(firstid, block);
        }

        // now the trailer object
        baos.write("trailer\n<<\n".getBytes("UTF-8"));

        // the number of entries (REQUIRED)
        baos.write("/Size ".getBytes("UTF-8"));
        baos.write(Integer.toString(offsets.size() + 1).getBytes("UTF-8"));
        baos.write("\n".getBytes("UTF-8"));

        // the /Root catalog indirect reference (REQUIRED)
        if (rootID != null) {
            baos.write("/Root ".getBytes("UTF-8"));
            baos.write(rootID.toString().getBytes("UTF-8"));
            baos.write("\n".getBytes("UTF-8"));
        } else {
            throw new IOException("Root object is not present in document");
        }

        // the /Info reference (OPTIONAL)
        if (infoID != null) {
            baos.write("/Info ".getBytes("UTF-8"));
            baos.write(infoID.toString().getBytes("UTF-8"));
            baos.write("\n".getBytes("UTF-8"));
        }

        // end the trailer object
        baos.write(">>\nstartxref\n".getBytes("UTF-8"));
        baos.write(Integer.toString(offset).getBytes("UTF-8"));
        baos.write("\n%%EOF\n".getBytes("UTF-8"));

        // now flush the stream
        baos.writeTo(os);
        os.flush();
    }

    /**
     * Writes a block of references to the PDF file
     *
     * @param firstid ID of the first reference in this block
     * @param block Vector containing the references in this block
     * @exception IOException on write error
     */
    protected void writeblock(int firstid, Vector<PDFXref> block) throws IOException {
        baos.write(Integer.toString(firstid).getBytes("UTF-8"));
        baos.write(" ".getBytes("UTF-8"));
        baos.write(Integer.toString(block.size()).getBytes("UTF-8"));
        baos.write("\n".getBytes("UTF-8"));
        //baos.write("\n0000000000 65535 f\n".getBytes("UTF-8"));

        for (PDFXref x : block) {
            baos.write(x.toString().getBytes("UTF-8"));
            baos.write("\n".getBytes("UTF-8"));
        }
    }
} // end class PDFOutput
