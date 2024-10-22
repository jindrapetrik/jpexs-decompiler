/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.Helper;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.monte.media.jpeg.CMYKJPEGImageReader;
import org.monte.media.jpeg.CMYKJPEGImageReaderSpi;

/**
 * Image helper.
 *
 * @author JPEXS
 */
public class ImageHelper {

    static {
        ImageIO.setUseCache(false);
    }

    /**
     * Reads image from byte array.
     * @param data Image data
     * @return Image
     */
    public static BufferedImage read(byte[] data) throws IOException {
        return read(new ByteArrayInputStream(data));
    }

    /**
     * Reads image from input stream.
     * @param input Input stream
     * @return Image
     * @throws IOException On I/O error
     */
    public static BufferedImage read(InputStream input) throws IOException {
        BufferedImage in;
        byte[] data = Helper.readStream(input);
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            CMYKJPEGImageReader r = new CMYKJPEGImageReader(new CMYKJPEGImageReaderSpi());
            r.setInput(iis);
            in = r.read(0);
        } catch (IOException | ArrayIndexOutOfBoundsException ex) {
            try {
                in = ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(data)));
            } catch (IOException ex1) {
                return null;
            }
        }

        if (in == null) {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics g = img.getGraphics();
            g.setColor(SWF.ERROR_COLOR);
            g.fillRect(0, 0, 1, 1);
            return img;
        }

        int type = in.getType();
        if (type != BufferedImage.TYPE_INT_ARGB_PRE && type != BufferedImage.TYPE_INT_RGB) {
            // convert to ARGB
            int width = in.getWidth();
            int height = in.getHeight();
            int[] imgData = in.getRGB(0, 0, width, height, null, 0, width);
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
            newImage.getRaster().setDataElements(0, 0, width, height, imgData);
            return newImage;
        }

        return in;
    }

    /**
     * Writes image to file.
     * @param image Image
     * @param format Image format
     * @param output Output file
     * @throws IOException On I/O error
     */
    public static void write(BufferedImage image, ImageFormat format, File output) throws IOException {
        String formatName = getImageFormatString(format).toUpperCase(Locale.ENGLISH);
        if (format == ImageFormat.JPEG) {
            image = fixImageIOJpegBug(image);
        }

        ImageIO.write(image, formatName, output);
    }

    /**
     * Writes image to output stream.
     * @param image Image
     * @param format Image format
     * @param output Output stream
     * @throws IOException On I/O error
     */
    public static void write(BufferedImage image, ImageFormat format, OutputStream output) throws IOException {
        String formatName = getImageFormatString(format).toUpperCase(Locale.ENGLISH);
        if (format == ImageFormat.JPEG) {
            image = fixImageIOJpegBug(image);
        }

        ImageIO.write(image, formatName, output);
    }

    /**
     * Writes image to byte array.
     * @param image Image
     * @param format Image format
     * @param output Output byte array
     */
    public static void write(BufferedImage image, ImageFormat format, ByteArrayOutputStream output) {
        String formatName = getImageFormatString(format).toUpperCase(Locale.ENGLISH);
        if (format == ImageFormat.JPEG) {
            image = fixImageIOJpegBug(image);
        } else if (image.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
            BufferedImage image2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            divideAlpha(pixels);

            int[] pixels2 = ((DataBufferInt) image2.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels.length; i++) {
                pixels2[i] = pixels[i];
            }

            image = image2;
        }

        try {
            ImageIO.write(image, formatName, output);
        } catch (IOException ex) {
            Logger.getLogger(ImageHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int max255(float val) {
        if (val > 255) {
            return 255;
        }
        return (int) val;
    }

    private static void divideAlpha(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = divideAlpha(pixels[i]);
        }
    }

    private static int divideAlpha(int value) {
        int a = (value >> 24) & 0xFF;
        int r = (value >> 16) & 0xFF;
        int g = (value >> 8) & 0xFF;
        int b = value & 0xFF;
        float multiplier = a == 0 ? 0 : 255.0f / a;
        r = max255(r * multiplier);
        g = max255(g * multiplier);
        b = max255(b * multiplier);
        return RGBA.toInt(r, g, b, a);
    }

    private static BufferedImage fixImageIOJpegBug(BufferedImage image) {
        int type = image.getType();
        if (type != BufferedImage.TYPE_INT_RGB) {
            // convert to RGB without alpha channel
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[] pixels2 = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
            for (int i = 0; i < pixels.length; i++) {
                pixels2[i] = pixels[i] & 0xffffff;
            }

            return newImage;
        }

        return image;
    }

    /**
     * Gets image format string.
     * @param format Image format
     * @return Image format string
     */
    public static String getImageFormatString(ImageFormat format) {
        switch (format) {
            case UNKNOWN:
                return "unk";
            case JPEG:
                return "jpg";
            case GIF:
                return "gif";
            case PNG:
                return "png";
            case BMP:
                return "bmp";
        }

        throw new Error("Unsupported image format: " + format);
    }

    /**
     * Gets image dimension.
     * @param input Input stream
     * @return Image dimension
     * @throws IOException On I/O error
     */
    public static Dimension getDimension(InputStream input) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(input)) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException ex) {
            //ignored
        }

        BufferedImage image = read(input);
        return new Dimension(image.getWidth(), image.getHeight());
    }
}
