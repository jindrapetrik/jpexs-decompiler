/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.helpers;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Adapted from
 * http://www.javaworld.com/article/2077561/learn-java/java-tip-60--saving-bitmap-files-in-java.html
 */
public class BMPFile extends Component {

    //--- Private constants
    private final static int BITMAPFILEHEADER_SIZE = 14;

    private final static int BITMAPINFOHEADER_SIZE = 40;

    //--- Private variable declaration
    //--- Bitmap file header
    private final byte[] bitmapFileHeader = new byte[14];

    private final byte[] bfType = {'B', 'M'};

    private int bfSize = 0;

    private final int bfReserved1 = 0;

    private final int bfReserved2 = 0;

    private final int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;

    //--- Bitmap info header
    private final byte[] bitmapInfoHeader = new byte[40];

    private final int biSize = BITMAPINFOHEADER_SIZE;

    private int biWidth = 0;

    private int biHeight = 0;

    private final int biPlanes = 1;

    private final int biBitCount = 24;

    private final int biCompression = 0;

    private int biSizeImage = 0x030000;

    private final int biXPelsPerMeter = 0x0;

    private final int biYPelsPerMeter = 0x0;

    private final int biClrUsed = 0;

    private final int biClrImportant = 0;

    //--- Bitmap raw data
    private int[] bitmap;

    //--- File section
    private OutputStream fo;

    //--- Private constructor
    private BMPFile() {
    }

    public static void saveBitmap(Image image, File file) throws IOException {
        BMPFile b = new BMPFile();
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
            b.fo = fos;
            b.save(image, image.getWidth(null), image.getHeight(null));
        }
    }

    /*
     *  The saveMethod is the main method of the process. This method
     *  will call the convertImage method to convert the memory image to
     *  a byte array; method writeBitmapFileHeader creates and writes
     *  the bitmap file header; writeBitmapInfoHeader creates the
     *  information header; and writeBitmap writes the image.
     *
     */
    private void save(Image parImage, int parWidth, int parHeight) throws IOException {
        convertImage(parImage, parWidth, parHeight);
        writeBitmapFileHeader();
        writeBitmapInfoHeader();
        writeBitmap();

    }

    /*
     * convertImage converts the memory image to the bitmap format (BRG).
     * It also computes some information for the bitmap info header.
     *
     */
    private boolean convertImage(Image parImage, int parWidth, int parHeight) {
        int pad;
        bitmap = new int[parWidth * parHeight];
        PixelGrabber pg = new PixelGrabber(parImage, 0, 0, parWidth, parHeight,
                bitmap, 0, parWidth);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            return (false);
        }

        pad = ((4 - ((parWidth * 3) % 4)) % 4) * parHeight;
        biSizeImage = ((parWidth * parHeight) * 3) + pad;
        bfSize = biSizeImage + BITMAPFILEHEADER_SIZE
                + BITMAPINFOHEADER_SIZE;
        biWidth = parWidth;
        biHeight = parHeight;
        return (true);
    }

    /*
     * writeBitmap converts the image returned from the pixel grabber to
     * the format required. Remember: scan lines are inverted in
     * a bitmap file!
     *
     * Each scan line must be padded to an even 4-byte boundary.
     */
    private void writeBitmap() throws IOException {
        byte[] rgb = new byte[3];
        int width = biWidth;
        int height = biHeight;
        int pad = ((4 - ((width * 3) % 4)) % 4);
        int padCount = 0;
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int value = bitmap[y * width + x];
                rgb[0] = (byte) (value & 0xFF);
                rgb[1] = (byte) ((value >> 8) & 0xFF);
                rgb[2] = (byte) ((value >> 16) & 0xFF);
                fo.write(rgb);
            }

            padCount += pad;
            for (int i = 0; i < pad; i++) {
                fo.write(0x00);
            }
        }
        //--- Update the size of the file
        bfSize += padCount;
        biSizeImage += padCount;
    }

    /*
     * writeBitmapFileHeader writes the bitmap file header to the file.
     *
     */
    private void writeBitmapFileHeader() throws IOException {

        fo.write(bfType);
        fo.write(intToDWord(bfSize));
        fo.write(intToWord(bfReserved1));
        fo.write(intToWord(bfReserved2));
        fo.write(intToDWord(bfOffBits));
    }

    /*
     *
     * writeBitmapInfoHeader writes the bitmap information header
     * to the file.
     *
     */
    private void writeBitmapInfoHeader() throws IOException {

        fo.write(intToDWord(biSize));
        fo.write(intToDWord(biWidth));
        fo.write(intToDWord(biHeight));
        fo.write(intToWord(biPlanes));
        fo.write(intToWord(biBitCount));
        fo.write(intToDWord(biCompression));
        fo.write(intToDWord(biSizeImage));
        fo.write(intToDWord(biXPelsPerMeter));
        fo.write(intToDWord(biYPelsPerMeter));
        fo.write(intToDWord(biClrUsed));
        fo.write(intToDWord(biClrImportant));

    }

    /*
     *
     * intToWord converts an int to a word, where the return
     * value is stored in a 2-byte array.
     *
     */
    private byte[] intToWord(int parValue) {
        byte[] retValue = new byte[2];
        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x00FF);
        return (retValue);
    }

    /*
     *
     * intToDWord converts an int to a double word, where the return
     * value is stored in a 4-byte array.
     *
     */
    private byte[] intToDWord(int parValue) {
        byte[] retValue = new byte[4];
        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x000000FF);
        retValue[2] = (byte) ((parValue >> 16) & 0x000000FF);
        retValue[3] = (byte) ((parValue >> 24) & 0x000000FF);
        return (retValue);
    }
}
