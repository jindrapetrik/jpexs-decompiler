/*
 * @(#)Colors.java  1.0  2011-03-13
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.color;

import java.awt.image.IndexColorModel;
import static java.lang.Math.*;

/**
 * {@code Colors}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-03-13 Created.
 */
public class Colors {

    /** Prevent instance creation. */
    private Colors() {
    }

    /**
     * The macintosh palette is arranged as follows: there are 256 colours to
     * allocate, an even distribution of colors through the color cube might be
     * desirable but 256 is not the cube of an integer. 6x6x6 is 216 and so the
     * first 216 colors are an equal 6x6x6 sampling of the color cube.
     * This leaves 40 colours to allocate, this has been done by choosing a ramp of
     * 10 shades each for red, green, blue and grey.
     *
     * <p>
     * References:<br>
     * <a href="http://paulbourke.net/texture_colour/colourramp/">http://paulbourke.net/texture_colour/colourramp/</a>
     *
     * @return The Macintosh color palette.
     */
    public static IndexColorModel createMacColors() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];

        // Generate color cube with 216 colors
        int index = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    r[index] = (byte) (255 - 51 * i);
                    g[index] = (byte) (255 - 51 * j);
                    b[index] = (byte) (255 - 51 * k);
                    index++;
                }
            }
        }

        index--; // overwrite last color (black) with color ramp

        // Generate red ramp
        byte[] ramp = {(byte) 238, (byte) 221, (byte) 187, (byte) 170, (byte) 136, (byte) 119, 85, 68, 34, 17};
        for (int i = 0; i < 10; i++) {
            r[index] = ramp[i];
            g[index] = (byte) (0);
            b[index] = (byte) (0);
            index++;
        }
        // Generate green ramp
        for (int j = 0; j < 10; j++) {
            r[index] = (byte) (0);
            g[index] = ramp[j];
            b[index] = (byte) (0);
            index++;
        }
        // Generate blue ramp
        for (int k = 0; k < 10; k++) {
            r[index] = (byte) (0);
            g[index] = (byte) (0);
            b[index] = ramp[k];
            index++;
        }
        // Generate gray ramp
        for (int ijk = 0; ijk < 10; ijk++) {
            r[index] = ramp[ijk];
            g[index] = ramp[ijk];
            b[index] = ramp[ijk];
            index++;
        }
        // last color is black (nothing to do)

        /*
        for (int i=0;i<256;i++) {
        if (i%6==0) System.out.println(); else System.out.print("  ");
        System.out.print(Integer.toHexString(r[i]&0xff)+","+Integer.toHexString(g[i]&0xff)+","+Integer.toHexString(b[i]&0xff));
        }*/

        IndexColorModel icm = new IndexColorModel(8, 256, r, g, b);
        return icm;
    }

    private static void RGBtoYCC(float[] rgb, float[] ycc) {
        float R = rgb[0];
        float G = rgb[1];
        float B = rgb[2];
        float Y = 0.3f * R + 0.6f * G + 0.1f * B;
        float V = R - Y;
        float U = B - Y;
        float Cb = (U / 2f) + 0.5f;
        float Cr = (V / 1.6f) + 0.5f;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    private static void YCCtoRGB(float[] ycc, float[] rgb) {
        float Y = ycc[0];
        float Cb = ycc[1];
        float Cr = ycc[2];
        float U = (Cb - 0.5f) * 2f;
        float V = (Cr - 0.5f) * 1.6f;
        float R = V + Y;
        float B = U + Y;
        float G = (Y - 0.3f * R - 0.1f * B) / 0.6f;
        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }

    /** RGB 8-bit per channel to YCC 16-bit per channel. */
    private static void RGB8toYCC16(int[] rgb, int[] ycc) {
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];
        int Y = 77 * R + 153 * G + 26 * B;
        int V = R * 256 - Y;
        int U = B * 256 - Y;
        int Cb = (U / 2) + 128 * 256;
        int Cr = (V * 5 / 8) + 128 * 256;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    /** RGB 8-bit per channel to YCC 16-bit per channel. */
    private static void RGB8toYCC16(int rgb, int[] ycc) {
        int R = (rgb & 0xff0000) >>> 16;
        int G = (rgb & 0xff00) >>> 8;
        int B = rgb & 0xff;
        int Y = 77 * R + 153 * G + 26 * B;
        int V = R * 256 - Y;
        int U = B * 256 - Y;
        int Cb = (U / 2) + 128 * 256;
        int Cr = (V * 5 / 8) + 128 * 256;
        ycc[0] = Y;
        ycc[1] = Cb;
        ycc[2] = Cr;
    }

    /** YCC 16-bit per channel to RGB 8-bit per channel. */
    private static void YCC16toRGB8(int[] ycc, int[] rgb) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];
        int U = (Cb - 128 * 256) * 2;
        int V = (Cr - 128 * 256) * 8 / 5;
        int R = min(255, max(0, (V + Y) / 256));
        int B = min(255, max(0, (U + Y) / 256));
        int G = min(255, max(0, (Y - 77 * R - 26 * B) / 153));
        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;
    }

    /** YCC 8-bit per channel to RGB 8-bit per channel. 
     */
    private static void YCC8toRGB8(int[] ycc, int[] rgb) {
        int Y = ycc[0];
        int Cb = ycc[1];
        int Cr = ycc[2];
        // Source: JPEG File Interchange Format Version 1.02, September 1, 1992
        //RGB can be computed directly from YCbCr (256 levels) as follows:
        //R = Y + 1.402 (Cr-128)
        //G = Y - 0.34414 (Cb-128) - 0.71414 (Cr-128) 
        //B = Y + 1.772 (Cb-128)
        int R = (1000 * Y + 1402 * (Cr - 128)) / 1000;
        int G = (100000 * Y - 34414 * (Cb - 128) - 71414 * (Cr - 128)) / 100000;
        int B = (1000 * Y + 1772 * (Cb - 128)) / 1000;
        rgb[0] = min(255, max(0, R));
        rgb[1] = min(255, max(0, G));
        rgb[2] = min(255, max(0, B));
    }

    /** YCC 8-bit per channel to RGB 8-bit per channel. 
     */
    private static void RGB8toYCC8(int[] rgb, int[] ycc) {
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];
        // Source: JPEG File Interchange Format Version 1.02, September 1, 1992
        //YCbCr (256 levels) can be computed directly from 8-bit RGB as follows:
        //Y = 0.299R +0.587G +0.114B
        //Cb = - 0.1687 R - 0.3313 G + 0.5 B + 128 
        //Cr = 0.5 R - 0.4187 G - 0.0813 B + 128
        int Y = (299 * R + 587 * G + 114 * B) / 1000;
        int Cb = (-1687 * R - 3313 * G + 5000 * B) / 10000 + 128;
        int Cr = (5000 * R - 4187 * G - 813 * B) / 10000 + 128;
        ycc[0] = min(255, max(0, Y));
        ycc[1] = min(255, max(0, Cb));
        ycc[2] = min(255, max(0, Cr));
    }
}
