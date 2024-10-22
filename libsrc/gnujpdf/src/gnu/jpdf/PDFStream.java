/* 
 * $Id: PDFStream.java,v 1.2 2007/08/26 18:56:35 gil1 Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.zip.DeflaterOutputStream;

/**
 * This class implements a PDF stream object. In PDF, streams contain data like
 * the graphic operators that render a page, or the pixels of an image.
 *
 * <p>
 * In PDF, a stream can be compressed using several different methods, or left
 * uncompressed. Here we support both uncompressed, and FlateDecode as it's
 * supported by the java core.
 *
 * @author Peter T Mount http://www.retep.org.uk/pdf/
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.2 $, $Date: 2007/08/26 18:56:35 $
 *
 */
public class PDFStream extends PDFObject implements Serializable {


    /*
   * NOTE: The original class is the work of Peter T. Mount, who released it 
   * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as 
   * follows: 
   * The package name was changed to gnu.jpdf.  
   * The formatting was changed a little bit.
   * It is still licensed under the LGPL.
     */
    /**
     * This holds the stream's content.
     */
    transient ByteArrayOutputStream buf;

    /**
     * True if we will compress the stream in the pdf file
     */
    boolean deflate;

    /**
     * Constructs a plain stream.
     * <p>
     * By default, the stream will be compressed.
     */
    public PDFStream() {
        this(null);
    }

    /**
     * Constructs a stream. The supplied type is stored in the stream's header
     * and is used by other objects that extend the PDFStream class (like
     * PDFImage).
     * <p>
     * By default, the stream will be compressed.
     *
     * @param type type for the stream
     * @see PDFImage
     */
    public PDFStream(String type) {
        super(type);
        buf = new ByteArrayOutputStream();

        // default deflate mode
        deflate = false;
    }

    /**
     * @param mode true will FlatDecode the stream
     */
    public void setDeflate(boolean mode) {
        // TODO: Restore the next line of code to allow deflate to occur.
        deflate = mode;
    }

    /**
     * Returns true if the stream will be compressed.
     *
     * @return true if compression is enabled
     */
    public boolean getDeflate() {
        return deflate;
    }

    /**
     * Returns the OutputStream that will append to this stream.
     *
     * @return The stream for this object
     */
    public OutputStream getOutputStream() {
        return (OutputStream) buf;
    }

    /**
     * Creates a PrintWriter that will append to this stream.
     *
     * @return a PrintWriter to write to the stream
     */
    public PrintWriter getWriter() {
        return new PrintWriter(buf, true);
    }

    /**
     * This is for extenders, and provides access to the stream.
     *
     * @return ByteArrayOutputStream containing the contents.
     */
    public ByteArrayOutputStream getStream() {
        return buf;
    }

    /**
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    @Override
    public void write(OutputStream os) throws IOException {
        writeStart(os);
        writeStream(os);
        // Unlike most PDF objects, we dont call writeEnd(os) because we
        // contain a stream
    }

    /**
     * This inserts the Streams length, then the actual stream, finally the end
     * of stream/object markers.
     *
     * <p>
     * This is intended for anyone extending PDFStream, as objects containing
     * streams do no use writeEnd(), and they must be able to write the actual
     * stream.
     *
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    public void writeStream(OutputStream os) throws IOException {
        if (deflate) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(b);
            //,new Deflater(Deflater.BEST_COMPRESSION,true));
            buf.writeTo(dos);
            dos.finish();
            dos.close();

            // FlatDecode is compatible with the java.util.zip.Deflater class
            os.write("/Filter /FlateDecode\n".getBytes("UTF-8"));
            os.write("/Length ".getBytes("UTF-8"));
            os.write(Integer.toString(b.size() + 1).getBytes("UTF-8"));
            os.write("\n>>\nstream\n".getBytes("UTF-8"));
            b.writeTo(os);
            os.write("\n".getBytes("UTF-8"));
        } else {
            // This is a non-deflated stream
            os.write("/Length ".getBytes("UTF-8"));
            os.write(Integer.toString(buf.size()).getBytes("UTF-8"));
            os.write("\n>>\nstream\n".getBytes("UTF-8"));
            buf.writeTo(os);
        }

        os.write("endstream\nendobj\n".getBytes("UTF-8"));

        // Unlike most PDF objects, we dont call writeEnd(os) because we
        // contain a stream
    }

    // Why is this here?  Did it have a specific purpose?
    /**
     * This implements our own special Serialization for this object.
     *
     * <p>
     * Here we write the length of the stream's contents, then a byte array of
     * the contents. We have to do this, as ByteArrayOutputStream is not
     * serializable (hence the transient tag).
     *
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(buf.size());
        out.write(buf.toByteArray());
    }

    /**
     * This implements our own special Serialization for this object
     *
     * <p>
     * Here we read the length of the stream's contents, then a byte array of
     * the contents. Then we recreate a new ByteArrayOutputStream. We have to do
     * this, as ByteArrayOutputStream is not serializable (hence the transient
     * tag).
     *
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException {
        int l = in.readInt();
        byte b[] = new byte[l];
        in.read(b, 0, l);
        buf = new ByteArrayOutputStream(l);
        buf.write(b);
    }

}
