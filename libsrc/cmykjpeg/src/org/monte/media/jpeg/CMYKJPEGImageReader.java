/*
 * @(#)CMYJKJPEGImageReader.java  
 * 
 * Copyright (c) 2010-2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.jpeg;

import com.sun.imageio.plugins.jpeg.JPEGImageReader;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.metadata.IIOMetadata;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.io.ImageInputStreamAdapter;
//import com.sun.imageio.plugins.jpeg.JPEGImageReader;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.*;
import static java.lang.Math.*;

/**
 * Reads a JPEG image with colors in the CMYK color space.
 *
 * @author Werner Randelshofer
 * @version $Id: CMYKJPEGImageReader.java 308 2013-01-06 11:24:06Z werner $
 */
public class CMYKJPEGImageReader extends ImageReader {

    private boolean isIgnoreICCProfile = false;
    private boolean isYCCKInversed = true;
    private static DirectColorModel RGB = new DirectColorModel(24, 0xff0000, 0xff00, 0xff, 0x0);
    /**
     * When we read the header, we read the whole image.
     */
    private BufferedImage image;

    public CMYKJPEGImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        readHeader();
        return image.getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        readHeader();
        return image.getHeight();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        readHeader();
        LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
        l.add(new ImageTypeSpecifier(RGB, RGB.createCompatibleSampleModel(image.getWidth(), image.getHeight())));
        return l.iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if (imageIndex > 0) {
            throw new IndexOutOfBoundsException();
        }
        readHeader();
        return image;
    }

    /**
     * Reads the PGM header. Does nothing if the header has already been loaded.
     */
    private void readHeader() throws IOException {
        if (image == null) {

            ImageInputStream iis = null;
            Object in = getInput();
            /* No need for JMF support in CMYKJPEGImageReader.
             if (in instanceof Buffer) {
             in = ((Buffer) in).getData();
             }*/

            if (in instanceof byte[]) {
                iis = new ByteArrayImageInputStream((byte[]) in);
            } else if (in instanceof ImageInputStream) {
                iis = (ImageInputStream) in;
            } else if (in instanceof InputStream) {
                iis = new MemoryCacheImageInputStream((InputStream) in);
            } else {
                throw new IOException("Can't handle input of type " + in);
            }
            image = read(iis, isYCCKInversed, isIgnoreICCProfile);
        }
    }

    /**
     * @return the YCCKInversed property.
     */
    public boolean isYCCKInversed() {
        return isYCCKInversed;
    }

    /**
     * @param newValue the new value
     */
    public void setYCCKInversed(boolean newValue) {
        this.isYCCKInversed = newValue;
    }

    public boolean isIgnoreICCProfile() {
        return isIgnoreICCProfile;
    }

    public void setIgnoreICCProfile(boolean newValue) {
        this.isIgnoreICCProfile = newValue;
    }

    public static BufferedImage read(ImageInputStream in, boolean inverseYCCKColors, boolean isIgnoreColorProfile) throws IOException {
        // Seek to start of input stream
        in.seek(0);

        // Extract metadata from the JFIF stream.
        // --------------------------------------
        // In particular, we are interested into the following fields:
        int samplePrecision = 0;
        int numberOfLines = 0;
        int numberOfSamplesPerLine = 0;
        int numberOfComponentsInFrame = 0;
        int app14AdobeColorTransform = 0;
        ByteArrayOutputStream app2ICCProfile = new ByteArrayOutputStream();
        // Browse for marker segments, and extract data from those
        // which are of interest.
        JFIFInputStream fifi = new JFIFInputStream(new ImageInputStreamAdapter(in));
        for (JFIFInputStream.Segment seg = fifi.getNextSegment(); seg != null; seg = fifi.getNextSegment()) {
            if (0xffc0 <= seg.marker && seg.marker <= 0xffc3
                    || 0xffc5 <= seg.marker && seg.marker <= 0xffc7
                    || 0xffc9 <= seg.marker && seg.marker <= 0xffcb
                    || 0xffcd <= seg.marker && seg.marker <= 0xffcf) {
                // SOF0 - SOF15: Start of Frame Header marker segment
                DataInputStream dis = new DataInputStream(fifi);
                samplePrecision = dis.readUnsignedByte();
                numberOfLines = dis.readUnsignedShort();
                numberOfSamplesPerLine = dis.readUnsignedShort();
                numberOfComponentsInFrame = dis.readUnsignedByte();
                // ...the rest of SOF header is not important to us.
                // In fact, by encounterint a SOF header, we have reached
                // the end of the metadata section we are interested in.
                // Thus we can abort here.
                break;

            } else if (seg.marker == 0xffe2) {
                // APP2: Application-specific marker segment
                if (seg.length >= 26) {
                    DataInputStream dis = new DataInputStream(fifi);
                    // Check for 12-bytes containing the null-terminated string: "ICC_PROFILE".
                    if (dis.readLong() == 0x4943435f50524f46L && dis.readInt() == 0x494c4500) {
                        // Skip 2 bytes
                        dis.skipBytes(2);

                        // Read Adobe ICC_PROFILE int buffer. The profile is split up over
                        // multiple APP2 marker segments.
                        byte[] b = new byte[512];
                        for (int count = dis.read(b); count != -1; count = dis.read(b)) {
                            app2ICCProfile.write(b, 0, count);
                        }
                    }
                }
            } else if (seg.marker == 0xffee) {
                // APP14: Application-specific marker segment
                if (seg.length == 12) {
                    DataInputStream dis = new DataInputStream(fifi);
                    // Check for 6-bytes containing the null-terminated string: "Adobe".
                    if (dis.readInt() == 0x41646f62L && dis.readUnsignedShort() == 0x6500) {
                        int version = dis.readUnsignedByte();
                        int app14Flags0 = dis.readUnsignedShort();
                        int app14Flags1 = dis.readUnsignedShort();
                        app14AdobeColorTransform = dis.readUnsignedByte();
                    }
                }
            }
        }
        //fifi.close();

        // Read the image data
        BufferedImage img = null;
        if (numberOfComponentsInFrame != 4) {
            // Read image with YCC color encoding.
            in.seek(0);
            img = ImageIO.read(in); // JPEXS: It's not CMYK, then do Java do it's job
            //img = readRGBImageFromYCC(new ImageInputStreamAdapter(in), null);
        } else if (numberOfComponentsInFrame == 4) {

            // Try to instantiate an ICC_Profile from the app2ICCProfile
            ICC_Profile profile = null;
            if (!isIgnoreColorProfile && app2ICCProfile.size() > 0) {
                try {
                    profile = ICC_Profile.getInstance(new ByteArrayInputStream(app2ICCProfile.toByteArray()));
                } catch (Throwable ex) {
                    // icc profile is corrupt
                    ex.printStackTrace();
                }
            }

            switch (app14AdobeColorTransform) {
                case 0:
                default:
                    // Read image with RGBA color encoding.
                    in.seek(0);
                    img = readRGBAImageFromRGBA(new ImageInputStreamAdapter(in), profile);
                    break;
                case 1:
                    throw new IOException("YCbCr not supported");
                case 2:
                    // Read image with inverted YCCK color encoding.
                    // FIXME - How do we determine from the JFIF file whether
                    // YCCK colors are inverted?

                    // We must have a color profile in order to perform a 
                    // conversion from CMYK to RGB.
                    // I case none has been supplied, we create a default one here.
                    if (profile == null) {
                        profile = ICC_Profile.getInstance(CMYKJPEGImageReader.class.getResourceAsStream("Generic CMYK Profile.icc"));
                    }
                    in.seek(0);
                    if (inverseYCCKColors) {
                        img = readRGBImageFromInvertedYCCK(new ImageInputStreamAdapter(in), profile);
                    } else {
                        img = readRGBImageFromYCCK(new ImageInputStreamAdapter(in), profile);
                    }
                    break;
            }
        }

        return img;
    }

    private static ImageReader createNativeJPEGReader() {
        return new JPEGImageReader(new CMYKJPEGImageReaderSpi());
        /*
         for (Iterator<ImageReader> i =
         ImageIO.getImageReadersByFormatName("jpeg"); i.hasNext();) {
         ImageReader r = i.next();
         if (!(r instanceof CMYKJPEGImageReader)
         && !r.getClass().getName().contains("CMYKJPEGImageReader")) {
         return r;
         }
         }
        
         return null;
         * 
         */
    }

    /**
     * Reads a CMYK JPEG image from the provided InputStream, converting the
     * colors to RGB using the provided CMYK ICC_Profile. The image data must be
     * in the CMYK color space.
     * <p>
     * Use this method, if you have already determined that the input stream
     * contains a CMYK JPEG image.
     *
     * @param in An InputStream, preferably an ImageInputStream, in the JPEG
     * File Interchange Format (JFIF).
     * @param cmykProfile An ICC_Profile for conversion from the CMYK color
     * space to the RGB color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage containing the decoded image converted into the
     * RGB color space.
     * @throws java.io.IOException
     */
    public static BufferedImage readRGBImageFromCMYK(InputStream in, ICC_Profile cmykProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = createNativeJPEGReader();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        BufferedImage image = createRGBImageFromCMYK(raster, cmykProfile);
        return image;
    }

    /**
     * Reads a RGBA JPEG image from the provided InputStream, converting the
     * colors to RGBA using the provided RGBA ICC_Profile. The image data must
     * be in the RGBA color space.
     * <p>
     * Use this method, if you have already determined that the input stream
     * contains a RGBA JPEG image.
     *
     * @param in An InputStream, preferably an ImageInputStream, in the JPEG
     * File Interchange Format (JFIF).
     * @param rgbaProfile An ICC_Profile for conversion from the RGBA color
     * space to the RGBA color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage containing the decoded image converted into the
     * RGB color space.
     * @throws java.io.IOException
     */
    public static BufferedImage readRGBAImageFromRGBA(InputStream in, ICC_Profile rgbaProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = createNativeJPEGReader();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        BufferedImage image = createRGBAImageFromRGBA(raster, rgbaProfile);
        return image;
    }

    public static BufferedImage readRGBImageFromRGB(InputStream in, ICC_Profile rgbaProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = createNativeJPEGReader();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        BufferedImage image = createRGBImageFromRGB(raster, rgbaProfile);
        return image;
    }

    public static BufferedImage readRGBImageFromYCC(InputStream in, ICC_Profile rgbaProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = createNativeJPEGReader();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        BufferedImage image = createRGBImageFromYCC(raster, rgbaProfile);
        return image;
    }

    /**
     * Reads a YCCK JPEG image from the provided InputStream, converting the
     * colors to RGB using the provided CMYK ICC_Profile. The image data must be
     * in the YCCK color space.
     * <p>
     * Use this method, if you have already determined that the input stream
     * contains a YCCK JPEG image.
     *
     * @param in An InputStream, preferably an ImageInputStream, in the JPEG
     * File Interchange Format (JFIF).
     * @param cmykProfile An ICC_Profile for conversion from the CMYK color
     * space to the RGB color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage containing the decoded image converted into the
     * RGB color space.
     * @throws java.io.IOException
     */
    public static BufferedImage readRGBImageFromYCCK(InputStream in, ICC_Profile cmykProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = createNativeJPEGReader();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        BufferedImage image = createRGBImageFromYCCK(raster, cmykProfile);
        return image;
    }

    /**
     * Reads an inverted-YCCK JPEG image from the provided InputStream,
     * converting the colors to RGB using the provided CMYK ICC_Profile. The
     * image data must be in the inverted-YCCK color space.
     * <p>
     * Use this method, if you have already determined that the input stream
     * contains an inverted-YCCK JPEG image.
     *
     * @param in An InputStream, preferably an ImageInputStream, in the JPEG
     * File Interchange Format (JFIF).
     * @param cmykProfile An ICC_Profile for conversion from the CMYK color
     * space to the RGB color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage containing the decoded image converted into the
     * RGB color space.
     * @throws java.io.IOException
     */
    public static BufferedImage readRGBImageFromInvertedYCCK(InputStream in, ICC_Profile cmykProfile) throws IOException {
        ImageInputStream inputStream = null;
        ImageReader reader = createNativeJPEGReader();
        inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
        reader.setInput(inputStream);
        Raster raster = reader.readRaster(0, null);
        raster = convertInvertedYCCKToCMYK(raster);
        BufferedImage image = createRGBImageFromCMYK(raster, cmykProfile);
        return image;
    }

    /**
     * Creates a buffered image from a raster in the YCCK color space,
     * converting the colors to RGB using the provided CMYK ICC_Profile.
     *
     * @param ycckRaster A raster with (at least) 4 bands of samples.
     * @param cmykProfile An ICC_Profile for conversion from the CMYK color
     * space to the RGB color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage in the RGB color space.
     * @throws NullPointerException.
     */
    public static BufferedImage createRGBImageFromYCCK(Raster ycckRaster, ICC_Profile cmykProfile) {
        BufferedImage image;
        if (cmykProfile != null) {
            ycckRaster = convertYCCKtoCMYK(ycckRaster);
            image = createRGBImageFromCMYK(ycckRaster, cmykProfile);
        } else {
            int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();
            int[] rgb = new int[w * h];
            int[] Y = ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] Cb = ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] Cr = ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            int[] K = ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null);

            float vr, vg, vb;
            for (int i = 0, imax = Y.length; i < imax; i++) {
                // FIXME - Use integer arithmetic to improve performance
                float k = K[i], y = Y[i], cb = Cb[i], cr = Cr[i];
                vr = y + 1.402f * (cr - 128) - k;
                vg = y - 0.34414f * (cb - 128) - 0.71414f * (cr - 128) - k;
                vb = y + 1.772f * (cb - 128) - k;
                rgb[i] = (0xff & (vr < 0.0f ? 0 : vr > 255.0f ? 0xff : (int) (vr + 0.5f))) << 16
                        | (0xff & (vg < 0.0f ? 0 : vg > 255.0f ? 0xff : (int) (vg + 0.5f))) << 8
                        | (0xff & (vb < 0.0f ? 0 : vb > 255.0f ? 0xff : (int) (vb + 0.5f)));
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = RGB;//new DirectColorModel(cs, 24, 0xff0000, 0xff00, 0xff, 0x0, false, DataBuffer.TYPE_INT);

            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }

    /**
     * Creates a buffered image from a raster in the inverted YCCK color space,
     * converting the colors to RGB using the provided CMYK ICC_Profile.
     *
     * @param ycckRaster A raster with (at least) 4 bands of samples.
     * @param cmykProfile An ICC_Profile for conversion from the CMYK color
     * space to the RGB color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage in the RGB color space.
     */
    public static BufferedImage createRGBImageFromInvertedYCCK(Raster ycckRaster, ICC_Profile cmykProfile) {
        BufferedImage image;
        if (cmykProfile != null) {
            ycckRaster = convertInvertedYCCKToCMYK(ycckRaster);
            image = createRGBImageFromCMYK(ycckRaster, cmykProfile);
        } else {
            int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();
            int[] rgb = new int[w * h];

            PixelInterleavedSampleModel pix;
            // if (Adobe_APP14 and transform==2) then YCCK else CMYK
            int[] Y = ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] Cb = ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] Cr = ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            int[] K = ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null);
            float vr, vg, vb;
            for (int i = 0, imax = Y.length; i < imax; i++) {
                // FIXME - Use integer arithmetic to improve performance
                float k = 255 - K[i], y = 255 - Y[i], cb = 255 - Cb[i], cr = 255 - Cr[i];
                vr = y + 1.402f * (cr - 128) - k;
                vg = y - 0.34414f * (cb - 128) - 0.71414f * (cr - 128) - k;
                vb = y + 1.772f * (cb - 128) - k;
                rgb[i] = (0xff & (vr < 0.0f ? 0 : vr > 255.0f ? 0xff : (int) (vr + 0.5f))) << 16
                        | (0xff & (vg < 0.0f ? 0 : vg > 255.0f ? 0xff : (int) (vg + 0.5f))) << 8
                        | (0xff & (vb < 0.0f ? 0 : vb > 255.0f ? 0xff : (int) (vb + 0.5f)));
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = RGB;//new DirectColorModel(cs, 24, 0xff0000, 0xff00, 0xff, 0x0, false, DataBuffer.TYPE_INT);

            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }

    /**
     * Creates a buffered image from a raster in the CMYK color space,
     * converting the colors to RGB using the provided CMYK ICC_Profile.
     *
     * As seen from a comment made by 'phelps' at
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4799903
     *
     * @param cmykRaster A raster with (at least) 4 bands of samples.
     * @param cmykProfile An ICC_Profile for conversion from the CMYK color
     * space to the RGB color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage in the RGB color space.
     */
    public static BufferedImage createRGBImageFromCMYK(Raster cmykRaster, ICC_Profile cmykProfile) {
        BufferedImage image;
        int w = cmykRaster.getWidth();
        int h = cmykRaster.getHeight();

        if (cmykProfile != null) {
            ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
            image = new BufferedImage(w, h,
                    BufferedImage.TYPE_INT_RGB);
            WritableRaster rgbRaster = image.getRaster();
            ColorSpace rgbCS = image.getColorModel().getColorSpace();
            ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
            cmykToRgb.filter(cmykRaster, rgbRaster);
        } else {

            int[] rgb = new int[w * h];

            int[] C = cmykRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] M = cmykRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] Y = cmykRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            int[] K = cmykRaster.getSamples(0, 0, w, h, 3, (int[]) null);

            for (int i = 0, imax = C.length; i < imax; i++) {
                int k = K[i];
                rgb[i] = (255 - min(255, C[i] + k)) << 16
                        | (255 - min(255, M[i] + k)) << 8
                        | (255 - min(255, Y[i] + k));
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = RGB;//new DirectColorModel(cs, 24, 0xff0000, 0xff00, 0xff, 0x0, false, DataBuffer.TYPE_INT);
            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }

    /**
     * Creates a buffered image from a raster in the RGBA color space,
     * converting the colors to RGB using the provided CMYK ICC_Profile.
     *
     * As seen from a comment made by 'phelps' at
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4799903
     *
     * @param rgbaRaster A raster with (at least) 4 bands of samples.
     * @param rgbaProfile An ICC_Profile for conversion from the CMYK color
     * space to the RGB color space. If this parameter is null, a default
     * profile is used.
     * @return a BufferedImage in the RGB color space.
     */
    public static BufferedImage createRGBAImageFromRGBA(Raster rgbaRaster, ICC_Profile rgbaProfile) {
        BufferedImage image;
        int w = rgbaRaster.getWidth();
        int h = rgbaRaster.getHeight();

        if (rgbaProfile != null) {
            ColorSpace rgbaCS = new ICC_ColorSpace(rgbaProfile);
            image = new BufferedImage(w, h,
                    BufferedImage.TYPE_INT_RGB);
            WritableRaster rgbRaster = image.getRaster();
            ColorSpace rgbCS = image.getColorModel().getColorSpace();
            ColorConvertOp cmykToRgb = new ColorConvertOp(rgbaCS, rgbCS, null);
            cmykToRgb.filter(rgbaRaster, rgbRaster);
        } else {

            int[] rgb = new int[w * h];

            int[] R = rgbaRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] G = rgbaRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] B = rgbaRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            int[] A = rgbaRaster.getSamples(0, 0, w, h, 3, (int[]) null);

            for (int i = 0, imax = R.length; i < imax; i++) {
                rgb[i] = A[i] << 24 | R[i] << 16 | G[i] << 8 | B[i];
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = ColorModel.getRGBdefault();//new DirectColorModel(cs, 32, 0xff0000, 0xff00, 0xff, 0x0ff000000, false, DataBuffer.TYPE_INT);
            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }

    public static BufferedImage createRGBImageFromRGB(Raster rgbaRaster, ICC_Profile rgbaProfile) {
        BufferedImage image;
        int w = rgbaRaster.getWidth();
        int h = rgbaRaster.getHeight();

        // ICC_Profile currently not supported
        rgbaProfile = null;
        if (rgbaProfile != null) {
            ColorSpace rgbaCS = new ICC_ColorSpace(rgbaProfile);
            image = new BufferedImage(w, h,
                    BufferedImage.TYPE_INT_RGB);
            WritableRaster rgbRaster = image.getRaster();
            ColorSpace rgbCS = image.getColorModel().getColorSpace();
            ColorConvertOp cmykToRgb = new ColorConvertOp(rgbaCS, rgbCS, null);
            cmykToRgb.filter(rgbaRaster, rgbRaster);
        } else {

            int[] rgb = new int[w * h];

            int[] R = rgbaRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] G = rgbaRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] B = rgbaRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            //int[] A = rgbaRaster.getSamples(0, 0, w, h, 3, (int[]) null);

            for (int i = 0, imax = R.length; i < imax; i++) {
                rgb[i] = 0xff << 24 | R[i] << 16 | G[i] << 8 | B[i];
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = ColorModel.getRGBdefault();//new DirectColorModel(cs, 32, 0xff0000, 0xff00, 0xff, 0x0ff000000, false, DataBuffer.TYPE_INT);
            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }

    public static BufferedImage createRGBImageFromYCC(Raster rgbaRaster, ICC_Profile rgbaProfile) {
        BufferedImage image;
        int w = rgbaRaster.getWidth();
        int h = rgbaRaster.getHeight();

        // ICC_Profile currently not supported
        rgbaProfile = null;
        if (rgbaProfile != null) {
            ColorSpace rgbaCS = new ICC_ColorSpace(rgbaProfile);
            image = new BufferedImage(w, h,
                    BufferedImage.TYPE_INT_RGB);
            WritableRaster rgbRaster = image.getRaster();
            ColorSpace rgbCS = image.getColorModel().getColorSpace();
            ColorConvertOp cmykToRgb = new ColorConvertOp(rgbaCS, rgbCS, null);
            cmykToRgb.filter(rgbaRaster, rgbRaster);
        } else {

            int[] rgb = new int[w * h];

            int[] Y = rgbaRaster.getSamples(0, 0, w, h, 0, (int[]) null);
            int[] Cb = rgbaRaster.getSamples(0, 0, w, h, 1, (int[]) null);
            int[] Cr = rgbaRaster.getSamples(0, 0, w, h, 2, (int[]) null);
            //int[] A = rgbaRaster.getSamples(0, 0, w, h, 3, (int[]) null);

            for (int i = 0, imax = Y.length; i < imax; i++) {
                int Yi, Cbi, Cri;
                int R, G, B;

                //RGB can be computed directly from YCbCr (256 levels) as follows:
                //R = Y + 1.402 (Cr-128)
                //G = Y - 0.34414 (Cb-128) - 0.71414 (Cr-128) 
                //B = Y + 1.772 (Cb-128)
                Yi = Y[i];
                Cbi = Cb[i];
                Cri = Cr[i];
                R = (1000 * Yi + 1402 * (Cri - 128)) / 1000;
                G = (100000 * Yi - 34414 * (Cbi - 128) - 71414 * (Cri - 128)) / 100000;
                B = (1000 * Yi + 1772 * (Cbi - 128)) / 1000;

                R = min(255, max(0, R));
                G = min(255, max(0, G));
                B = min(255, max(0, B));

                rgb[i] = 0xff << 24 | R << 16 | G << 8 | B;
            }

            Raster rgbRaster = Raster.createPackedRaster(
                    new DataBufferInt(rgb, rgb.length),
                    w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cm = ColorModel.getRGBdefault();//new DirectColorModel(cs, 32, 0xff0000, 0xff00, 0xff, 0x0ff000000, false, DataBuffer.TYPE_INT);
            image = new BufferedImage(cm, (WritableRaster) rgbRaster, true, null);
        }
        return image;
    }
    /**
     * Define tables for YCC->RGB color space conversion.
     */
    private final static int SCALEBITS = 16;
    private final static int MAXJSAMPLE = 255;
    private final static int CENTERJSAMPLE = 128;
    private final static int ONE_HALF = 1 << (SCALEBITS - 1);
    private final static int[] Cr_r_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cb_b_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cr_g_tab = new int[MAXJSAMPLE + 1];
    private final static int[] Cb_g_tab = new int[MAXJSAMPLE + 1];

    /*
     * Initialize tables for YCC->RGB colorspace conversion.
     */
    private static synchronized void buildYCCtoRGBtable() {
        if (Cr_r_tab[0] == 0) {
            for (int i = 0, x = -CENTERJSAMPLE; i <= MAXJSAMPLE; i++, x++) {
                /* i is the actual input pixel value, in the range 0..MAXJSAMPLE */
                /* The Cb or Cr value we are thinking of is x = i - CENTERJSAMPLE */
                /* Cr=>R value is nearest int to 1.40200 * x */
                Cr_r_tab[i] = (int) ((1.40200 * (1 << SCALEBITS) + 0.5) * x + ONE_HALF) >> SCALEBITS;
                /* Cb=>B value is nearest int to 1.77200 * x */
                Cb_b_tab[i] = (int) ((1.77200 * (1 << SCALEBITS) + 0.5) * x + ONE_HALF) >> SCALEBITS;
                /* Cr=>G value is scaled-up -0.71414 * x */
                Cr_g_tab[i] = -(int) (0.71414 * (1 << SCALEBITS) + 0.5) * x;
                /* Cb=>G value is scaled-up -0.34414 * x */
                /* We also add in ONE_HALF so that need not do it in inner loop */
                Cb_g_tab[i] = -(int) ((0.34414) * (1 << SCALEBITS) + 0.5) * x + ONE_HALF;
            }
        }
    }

    /*
     * Adobe-style YCCK->CMYK conversion.
     * We convert YCbCr to R=1-C, G=1-M, and B=1-Y using the same
     * conversion as above, while passing K (black) unchanged.
     * We assume build_ycc_rgb_table has been called.
     */
    private static Raster convertInvertedYCCKToCMYK(Raster ycckRaster) {
        buildYCCtoRGBtable();

        int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();
        int[] ycckY = ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null);
        int[] ycckCb = ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null);
        int[] ycckCr = ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null);
        int[] ycckK = ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null);
        int[] cmyk = new int[ycckY.length];

        for (int i = 0; i < ycckY.length; i++) {
            int y = 255 - ycckY[i];
            int cb = 255 - ycckCb[i];
            int cr = 255 - ycckCr[i];
            int cmykC, cmykM, cmykY;
            // Range-limiting is essential due to noise introduced by DCT losses.
            cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);	// red
            cmykM = MAXJSAMPLE - (y + // green
                    (Cb_g_tab[cb] + Cr_g_tab[cr]
                    >> SCALEBITS));
            cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);	// blue
      /* K passes through unchanged */
            cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
                    | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
                    | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
                    | 255 - ycckK[i];
        }

        Raster cmykRaster = Raster.createPackedRaster(
                new DataBufferInt(cmyk, cmyk.length),
                w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
        return cmykRaster;

    }

    private static Raster convertYCCKtoCMYK(Raster ycckRaster) {
        buildYCCtoRGBtable();

        int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();
        int[] ycckY = ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null);
        int[] ycckCb = ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null);
        int[] ycckCr = ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null);
        int[] ycckK = ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null);

        int[] cmyk = new int[ycckY.length];

        for (int i = 0; i < ycckY.length; i++) {
            int y = ycckY[i];
            int cb = ycckCb[i];
            int cr = ycckCr[i];
            int cmykC, cmykM, cmykY;
            // Range-limiting is essential due to noise introduced by DCT losses.
            cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);	// red
            cmykM = MAXJSAMPLE - (y + // green
                    (Cb_g_tab[cb] + Cr_g_tab[cr]
                    >> SCALEBITS));
            cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);	// blue
      /* K passes through unchanged */
            cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
                    | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
                    | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
                    | ycckK[i];
        }

        return Raster.createPackedRaster(
                new DataBufferInt(cmyk, cmyk.length),
                w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
    }

    /**
     * Reads a JPEG image from the provided InputStream. The image data must be
     * in the YUV or the Gray color space.
     * <p>
     * Use this method, if you have already determined that the input stream
     * contains a YCC or Gray JPEG image.
     *
     * @param in An InputStream, preferably an ImageInputStream, in the JPEG
     * File Interchange Format (JFIF).
     * @return a BufferedImage containing the decoded image converted into the
     * RGB color space.
     * @throws java.io.IOException
     */
    public static BufferedImage readImageFromYCCorGray(ImageInputStream in) throws IOException {
        ImageReader r = createNativeJPEGReader();
        r.setInput(in);
        BufferedImage img = r.read(0);
        return img;
    }
}
