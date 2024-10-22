/*
 * $Id: PDFImage.java,v 1.2 2007/08/26 18:56:35 gil1 Exp $
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

import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * <p>
 * This implements the Image XObject. Calling one of the <code>drawImage</code>
 * methods of <code>PDFGraphics</code> will put all the necessary code into the
 * pdf file, and the image will be encoded in ascii base 85, then deflated in
 * zip format.</p>
 *
 * @author Eric Z. Beard (original version by Peter Mount)
 * @author Matthew Hreljac, mhreljac@hotmail.com
 * @version $Revision: 1.2 $, $Date: 2007/08/26 18:56:35 $
 */
public class PDFImage extends PDFStream implements ImageObserver, Serializable {

    /*
   * NOTE: The original class is the work of Peter T. Mount, who released it
   * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as
   * follows:
   * The package name was changed to gnu.jpdf.
   * The formatting was changed a little bit.
   * Images were not yet implemented, so the core of this
   *   class was mostly rewritten
   * It is still licensed under the LGPL.
   * Got some help with base85 methods from Mathew Hreljac
     */
    // Dimensions of the object.
    private int objwidth;
    private int objheight;

    // Dimensions of the image.
    private int width;
    private int height;
    private Image img;
    private byte[] jpegImageData;
    private String name;

    private String mask;

    private boolean interpolate;

    /**
     * Creates a new <code>PDFImage</code> instance.
     *
     */
    public PDFImage() {
        super("/XObject");
    }

    /**
     * Creates a new <code>PDFImage</code> instance.
     *
     * @param img an <code>Image</code> value
     */
    public PDFImage(Image img, String mask, boolean interpolate) {
        this();
        this.mask = mask;
        this.interpolate = interpolate;
        setImage(img, 0, 0, img.getWidth(this), img.getHeight(this), this);
    }

    public PDFImage(Image img) {
        this(img, null, false);
    }

    public boolean isInterpolate() {
        return interpolate;
    }   
    
    /**
     * Creates a new <code>PDFImage</code> instance.
     *
     * @param img an <code>Image</code> value
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param w an <code>int</code> value
     * @param h an <code>int</code> value
     * @param obs an <code>ImageObserver</code> value
     */
    public PDFImage(Image img, int x, int y, int w, int h, ImageObserver obs, String mask, boolean interpolate) {
        this();
        objwidth = w;
        objheight = h;
        this.mask = mask;
        this.interpolate = interpolate;
        setImage(img, x, y, img.getWidth(this), img.getHeight(this), obs);
    }

    public PDFImage(byte jpegImageData[], int x, int y, int w, int h, ImageObserver obs, String mask, boolean interpolate) {
        this();
        objwidth = w;
        objheight = h;
        this.mask = mask;
        this.interpolate = interpolate;
        setJpegImageData(jpegImageData, w, h);
    }

    public PDFImage(Image img, int x, int y, int w, int h, ImageObserver obs) {
        this(img, x, y, w, h, obs, null, false);
    }

    public PDFImage(byte jpegImageData[], int x, int y, int w, int h, ImageObserver obs) {
        this(jpegImageData, x, y, w, h, obs, null, false);
    }

    /**
     * Get the value of width.
     *
     * @return value of width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the value of width.
     *
     * @param v Value to assign to width.
     */
    public void setWidth(int v) {
        this.width = v;
    }

    /**
     * Get the value of height.
     *
     * @return value of height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set the value of height.
     *
     * @param v Value to assign to height.
     */
    public void setHeight(int v) {
        this.height = v;
    }

    /**
     * Set the name
     *
     * @param n a <code>String</code> value
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * Get the name
     *
     * @return a <code>String</code> value
     */
    public String getName() {
        return name;
    }

    /**
     * Set the image
     *
     * @param img an <code>Image</code> value
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param w an <code>int</code> value
     * @param h an <code>int</code> value
     * @param obs an <code>ImageObserver</code> value
     */
    public void setImage(Image img, int x, int y, int w, int h, ImageObserver obs) {
        this.img = img;
        this.jpegImageData = null;
        width = w;
        height = h;
    }

    public void setJpegImageData(byte[] jpegImageData, int w, int h) {
        this.jpegImageData = jpegImageData;
        this.img = null;
        width = w;
        height = h;
    }

    /**
     * <p>
     * Adobe's base 85 does not follow the format used by ipv6 addresses. It
     * simply starts with 33 and goes straight up without skipping any
     * characters</p>
     *
     * <p>
     * Parts of this method contributed by Mathew Hreljac</p>
     *
     * @param stringToEncode a <code>String</code> value
     * @return a <code>String</code> value
     */
    private String base85Encoding(String stringToEncode)
            throws NumberFormatException {
        if ((stringToEncode == null) || (stringToEncode.length() == 0)) {
            //System.out.println("PDFImage.base85Encoding() null or blank String");
            return "";
        }
        if ((stringToEncode.length() > 8)
                || ((stringToEncode.length() % 2) != 0)) {
            System.out.println("PDFImage.base85Encoding, Incorrect tuple length: "
                    + stringToEncode.length());
            return "";
        }
        //System.out.println("str: " + stringToEncode);
        // String buffer to use to return the String encoding
        StringBuffer sb = new StringBuffer();

        // Deal with a partial tuple (less than 8 hex digits)
        // From Adobe's docs:
        // "Given n (1, 2 or 3) bytes of binary data, the encoding first
        // appends 4 - n zero bytes to make a complete 4-tuple.  This 4-tuple
        // is encoded in the usual way, but without applying the special
        // z-case.  Finally, only the first n+1 characters of the resulting
        // 5-tuple are written out.  Those characters are immediately followed
        // by the EOD marker, ~>"
        int numHexDigits = stringToEncode.length() / 2;
        int numAppendBytes = 4 - numHexDigits;
        for (int i = 0; i < numAppendBytes; i++) {
            stringToEncode += "00";
        }
        Vector<Integer> digitVector = new Vector<Integer>();
        long number = Long.parseLong(stringToEncode, 16);
        int remainder = 0;

        while (number >= 85) {
            remainder = (int) (number % 85);
            number = number / 85;
            digitVector.add(0, new Integer(remainder));
        }
        digitVector.add(0, new Integer((int) number));

        for (int i = 0; i < digitVector.size(); i++) {
            char c = (char) (((Integer) digitVector.elementAt(i)).intValue() + 33);
            sb.append(c);
        }
        String tuple = sb.toString();
        int len = tuple.length();
        switch (len) {
            case 1:
                tuple = "!!!!" + tuple;
                break;
            case 2:
                tuple = "!!!" + tuple;
                break;
            case 3:
                tuple = "!!" + tuple;
                break;
            case 4:
                tuple = "!" + tuple;
                break;
            default:
                break;
        } // end switch
        //System.out.println("enc tuple: " + tuple);

        return (tuple);
    } // end base85encoding

    /**
     * Writes the image to the stream
     *
     * @param os an <code>OutputStream</code> value
     * @exception IOException if an error occurs
     */
    @Override
    public void writeStream(OutputStream os) throws IOException {
        // This is a non-deflated stream
        /*
    os.write("/Length ".getBytes("UTF-8"));
    // Account for stream\n ... >\nendstream
    os.write(Integer.toString(buf.size() + 18).getBytes("UTF-8"));
    os.write("\n/Filter /ASCII85Decode".getBytes("UTF-8"));
    os.write("\n>>\nstream\n".getBytes("UTF-8"));
    buf.writeTo(os);
    os.write(">\nendstream\nendobj\n\n".getBytes("UTF-8"));
         */
        ByteArrayOutputStream b = new ByteArrayOutputStream();

        if (jpegImageData == null) {
            DeflaterOutputStream dos = new DeflaterOutputStream(b);
            buf.writeTo(dos);
            dos.finish();
            dos.close();
        } else {
            buf.writeTo(b);
        }

        // FlatDecode is compatible with the java.util.zip.Deflater class
        //os.write("/Filter [/FlateDecode /ASCIIHexDecode]\n".getBytes("UTF-8"));
        if (jpegImageData != null) {
            os.write("/Filter /DCTDecode\n".getBytes("UTF-8"));
        } else {
            os.write("/Filter [/FlateDecode /ASCII85Decode]\n".getBytes("UTF-8"));
        }
        os.write("/Length ".getBytes("UTF-8"));
        os.write(Integer.toString(b.size()).getBytes("UTF-8"));
        os.write("\n>>\nstream\n".getBytes("UTF-8"));
        b.writeTo(os);
        os.write("\nendstream\nendobj\n".getBytes("UTF-8"));

    } // end writeStream

    /**
     * <p>
     * Compression needs to be improved here</p>
     *
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    @Override
    public void write(OutputStream os) throws IOException {
        writeStart(os);

        // write the extra details
        os.write("/Subtype /Image\n/Name ".getBytes("UTF-8"));
        os.write(name.getBytes("UTF-8"));
        os.write("\n/Width ".getBytes("UTF-8"));
        os.write(Integer.toString(width).getBytes("UTF-8"));
        os.write("\n/Height ".getBytes("UTF-8"));
        os.write(Integer.toString(height).getBytes("UTF-8"));
        os.write("\n/BitsPerComponent 8\n/ColorSpace /DeviceRGB\n".getBytes("UTF-8"));
        if (mask != null) {
            os.write(("/SMask " + mask + "\n").getBytes("UTF-8"));
        }
        if (interpolate) {
            os.write("/Interpolate true\n".getBytes("UTF-8"));
        }

        // write the pixels to the stream
        //System.err.println("Processing image "+width+"x"+height+" pixels");
        ByteArrayOutputStream bos = getStream();

        if (jpegImageData != null) {
            bos.write(jpegImageData);
        } else {
            int w = width;
            int h = height;
            int x = 0;
            int y = 0;
            int[] pixels = new int[w * h];
            PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
            try {
                pg.grabPixels();
            } catch (InterruptedException e) {
                System.err.println("interrupted waiting for pixels!");
                return;
            }
            if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                System.err.println("image fetch aborted or errored");
                return;
            }
            StringBuffer out = new StringBuffer();
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    //System.out.print("p[" + j * w + i+ "]=" + pixels[j * w + i] + ".");
                    out.append(handlePixel(x + i, y + j, pixels[j * w + i]));
                    if (out.toString().length() >= 8) {
                        String tuple = out.substring(0, 8);
                        out.delete(0, 8);
                        // Convert !!!!! to 'z'
                        String encTuple = base85Encoding(tuple);
                        if (encTuple.equals("!!!!!")) {
                            encTuple = "z";
                        }
                        bos.write(encTuple.getBytes("UTF-8"));
                    }
                }
            }
            // This should be the only partial tuple case,

            String lastTuple = base85Encoding(out.toString());
            //System.out.println("lastTuple: " + lastTuple);
            bos.write(lastTuple.getBytes("UTF-8"));
            bos.write("~".getBytes("UTF-8"));
        }

        //System.out.println("Processing done");
        // this will write the actual stream
        setDeflate(false);

        writeStream(os);

        // Note: we do not call writeEnd() on streams!
    }

    /**
     * <p>
     * Converts a pixel to a hex string</p>
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param p an <code>int</code> value
     * @return a <code>String</code> value
     */
    public static String handlePixel(int x, int y, int p) {
        int alpha = (p >> 24) & 0xff;
        int red = (p >> 16) & 0xff;
        int green = (p >> 8) & 0xff;
        int blue = (p) & 0xff;
        String redHex = Integer.toHexString(red);
        String greenHex = Integer.toHexString(green);
        String blueHex = Integer.toHexString(blue);
        if (redHex.length() == 1) {
            redHex = "0" + redHex;
        }
        if (greenHex.length() == 1) {
            greenHex = "0" + greenHex;
        }
        if (blueHex.length() == 1) {
            blueHex = "0" + blueHex;
        }
        return redHex + greenHex + blueHex;
    } // end handlePixel

    /**
     * Describe <code>imageUpdate</code> method here.
     *
     * @param img an <code>Image</code> value
     * @param infoflags an <code>int</code> value
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param w an <code>int</code> value
     * @param h an <code>int</code> value
     * @return a <code>boolean</code> value
     */
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        System.err.println("img=" + img + "\ninfoflags=" + infoflags
                + "\nx=" + x + " y=" + y + " w=" + w + " h=" + h);
        //if(img == this.img) {
        if (infoflags == ImageObserver.WIDTH) {
            width = w;
        }
        if (infoflags == ImageObserver.HEIGHT) {
            height = h;
        }

        //return true;
        //}
        return false;
    }

} // end class PDFImage
