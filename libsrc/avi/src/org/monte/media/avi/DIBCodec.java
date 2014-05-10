/*
 * @(#)DIBCodec.java 
 *
 * Copyright © 2011-2012 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.avi;

import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferByte;
import org.monte.media.AbstractVideoCodec;
import org.monte.media.Buffer;
import org.monte.media.Format;
import org.monte.media.io.SeekableByteArrayOutputStream;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;

/**
 * {@code DIBCodec} encodes a BufferedImage as a Microsoft Device Independent 
 * Bitmap (DIB) into a byte array.
 * <p>
 * The DIB codec only works with the AVI file format. Other file formats, such
 * as QuickTime, use a different encoding for uncompressed video.
 * <p>
 * This codec currently only supports encoding from a {@code BufferedImage} into 
 * the file format. Decoding support may be added in the future.
 * <p>
 * This codec does not encode the color palette of an image. This must be done
 * separately.
 * <p>
 * The pixels of a frame are written row by row from bottom to top and from
 * the left to the right. 24-bit pixels are encoded as BGR.
 * <p>
 * Supported input formats:
 * <ul>
 * {@code Format} with {@code BufferedImage.class}, any width, any height,
 * depth=4.
 * </ul>
 * Supported output formats:
 * <ul>
 * {@code Format} with {@code byte[].class}, same width and height as input
 * format, depth=4.
 * </ul>
 *
 * @author Werner Randelshofer
 * @version $Id: DIBCodec.java 299 2013-01-03 07:40:18Z werner $
 */
public class DIBCodec extends AbstractVideoCodec {

    public DIBCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_DIB, DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 4), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_DIB, DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 8), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_DIB, DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 24), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_DIB, DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 4), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_DIB, DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 8), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_DIB, DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 24), //
                });
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (outputFormat.get(EncodingKey) == ENCODING_BUFFERED_IMAGE) {
            return decode(in, out);
        } else {
            return encode(in, out);
        }
    }

    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        out.sampleCount = 1;
        BufferedImage img = null;

        int imgType;
        switch (outputFormat.get(DepthKey)) {
            case 4:
                imgType = BufferedImage.TYPE_BYTE_INDEXED;
                break;
            case 8:
                imgType = BufferedImage.TYPE_BYTE_INDEXED;
                break;
            case 24:
                imgType = BufferedImage.TYPE_INT_RGB;
                break;
            default:
                imgType = BufferedImage.TYPE_INT_RGB;
                break;
        }

        if (out.data instanceof BufferedImage) {
            img = (BufferedImage) out.data;
            // Fixme: Handle sub-image
            if (img.getWidth() != outputFormat.get(WidthKey)
                    || img.getHeight() != outputFormat.get(HeightKey)
                    || img.getType() != imgType) {
                img = null;
            }
        }
        if (img == null) {
            img = new BufferedImage(outputFormat.get(WidthKey), outputFormat.get(HeightKey), imgType);
        }
        out.data = img;

        switch (outputFormat.get(DepthKey)) {
            case 4:
                readKey4((byte[]) in.data, in.offset, in.length, img);
                break;
            case 8:
                readKey8((byte[]) in.data, in.offset, in.length, img);
                break;
            case 24:
            default:
                readKey24((int[]) in.data, in.offset, in.length, img);
                break;
        }


        return CODEC_OK;
    }

    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        SeekableByteArrayOutputStream tmp;
        if (out.data instanceof byte[]) {
            tmp = new SeekableByteArrayOutputStream((byte[]) out.data);
        } else {
            tmp = new SeekableByteArrayOutputStream();
        }

        // Handle sub-image
        // FIXME - Scanline stride must be a multiple of four.
        Rectangle r;
        int scanlineStride;
        if (in.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) in.data;
            WritableRaster raster = image.getRaster();
            scanlineStride = raster.getSampleModel().getWidth();
            r = raster.getBounds();
            r.x -= raster.getSampleModelTranslateX();
            r.y -= raster.getSampleModelTranslateY();
            out.header = image.getColorModel();
        } else {
            r = new Rectangle(0, 0, outputFormat.get(WidthKey), outputFormat.get(HeightKey));
            scanlineStride = outputFormat.get(WidthKey);
            out.header = null;
        }

        try {
            switch (outputFormat.get(DepthKey)) {
                case 4: {
                    byte[] pixels = getIndexed8(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }
                    writeKey4(tmp, pixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                case 8: {
                    byte[] pixels = getIndexed8(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }
                    writeKey8(tmp, pixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                case 24: {
                    int[] pixels = getRGB24(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }
                    writeKey24(tmp, pixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
                    break;
                }
                default:
                    out.setFlag(DISCARD);
                    return CODEC_OK;
            }

            out.setFlag(KEYFRAME);
            out.data = tmp.getBuffer();
            out.sampleCount = 1;
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }

    public void readKey4(byte[] in, int offset, int length, BufferedImage img) {
        DataBufferByte buf = (DataBufferByte) img.getRaster().getDataBuffer();
        WritableRaster raster = img.getRaster();
        int scanlineStride = raster.getSampleModel().getWidth();
        Rectangle r = raster.getBounds();
        r.x -= raster.getSampleModelTranslateX();
        r.y -= raster.getSampleModelTranslateY();
        
        throw new UnsupportedOperationException("readKey4 not yet implemented");
    }

    public void readKey8(byte[] in, int offset, int length, BufferedImage img) {
        DataBufferByte buf = (DataBufferByte) img.getRaster().getDataBuffer();
        WritableRaster raster = img.getRaster();
        int scanlineStride = raster.getSampleModel().getWidth();
        Rectangle r = raster.getBounds();
        r.x -= raster.getSampleModelTranslateX();
        r.y -= raster.getSampleModelTranslateY();
        
        int h=img.getHeight();
        int w=img.getWidth();
        int i=offset;
        int j=r.x+r.y*scanlineStride+(h-1)*scanlineStride;
        byte[] out=buf.getData();
        for (int y=0;y<h;y++) {
            System.arraycopy(in,i,out,j,w);
            i+=w;
            j-=scanlineStride;
        }
    }

    public void readKey24(int[] in, int offset, int length, BufferedImage img) {
        DataBufferInt buf = (DataBufferInt) img.getRaster().getDataBuffer();
        WritableRaster raster = img.getRaster();
        int scanlineStride = raster.getSampleModel().getWidth();
        Rectangle r = raster.getBounds();
        r.x -= raster.getSampleModelTranslateX();
        r.y -= raster.getSampleModelTranslateY();
        
        int h=img.getHeight();
        int w=img.getWidth();
        int i=offset;
        int j=r.x+r.y*scanlineStride+(h-1)*scanlineStride;
        int[] out=buf.getData();
        for (int y=0;y<h;y++) {
            System.arraycopy(in,i,out,j,w);
            i+=w;
            j-=scanlineStride;
        }
    }

    /** Encodes a 4-bit key frame.
     *
     * @param out The output stream.
     * @param pixels The image data.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void writeKey4(OutputStream out, byte[] pixels, int width, int height, int offset, int scanlineStride)
            throws IOException {

        byte[] bytes = new byte[width];
        for (int y = (height - 1) * scanlineStride; y >= 0; y -= scanlineStride) { // Upside down
            for (int x = offset, xx = 0, n = offset + width; x < n; x += 2, ++xx) {
                bytes[xx] = (byte) (((pixels[y + x] & 0xf) << 4) | (pixels[y + x + 1] & 0xf));
            }
            out.write(bytes);
        }

    }

    /** Encodes an 8-bit key frame.
     *
     * @param out The output stream.
     * @param pixels The image data.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void writeKey8(OutputStream out, byte[] pixels, int width, int height, int offset, int scanlineStride)
            throws IOException {

        for (int y = (height - 1) * scanlineStride; y >= 0; y -= scanlineStride) { // Upside down
            out.write(pixels, y + offset, width);
        }
    }

    /** Encodes a 24-bit key frame.
     *
     * @param out The output stream.
     * @param pixels The image data.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to append to offset to get to the next scanline.
     */
    public void writeKey24(OutputStream out, int[] pixels, int width, int height, int offset, int scanlineStride)
            throws IOException {
        int w3 = width * 3;
        byte[] bytes = new byte[w3]; // holds a scanline of raw image data with 3 channels of 8 bit data
        for (int xy = (height - 1) * scanlineStride + offset; xy >= offset; xy -= scanlineStride) { // Upside down
            for (int x = 0, xp = 0; x < w3; x += 3, ++xp) {
                int p = pixels[xy + xp];
                bytes[x] = (byte) (p); // Blue
                bytes[x + 1] = (byte) (p >> 8); // Green
                bytes[x + 2] = (byte) (p >> 16); // Red
            }
            out.write(bytes);
        }
    }
}
