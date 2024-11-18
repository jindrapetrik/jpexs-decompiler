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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DataBufferInt;
import java.awt.image.Kernel;

/**
 * Filter application.
 *
 * @author JPEXS
 */
public class Filtering {

    public static final int INNER = 1;

    public static final int OUTER = 2;

    public static final int FULL = 3;

    private static final Color ALPHA = new Color(0, 0, 0, 0);

    private static final Point POINT_0_0 = new Point(0, 0);

    private static final Point POINT_255_0 = new Point(255, 0);

    private static final Point POINT_511_0 = new Point(511, 0);

    private static final Rectangle RECTANGLE_256_1 = new Rectangle(256, 1);

    private static final Rectangle RECTANGLE_512_1 = new Rectangle(512, 1);

    private static void boxBlurSingleIteration(int[] pixels, int[] mask, int[] newColors, int w, int h, int radiusX, int radiusY) {

        if (true) {
            boxBlurSingleIterationTwoPass(pixels, mask, newColors, w, h, radiusX, radiusY);
            return;
        }

        if (radiusX == 0) {
            radiusX = 1;
        }
        if (radiusY == 0) {
            radiusY = 1;
        }

        long limit = Configuration.boxBlurPixelsLimit.get() * 10000L;

        if ((long) w * (long) h > limit) {
            return;
        }

        while (((long) radiusY * (long) radiusX * (long) w * (long) h) > limit) {
            // decrease radius
            if (radiusY > 1) {
                radiusY--;
            }
            if (radiusX > 1) {
                radiusX--;
            }
        }

        int radiusXHalf = radiusX / 2;
        int radiusYHalf = radiusY / 2;
        double divisor = radiusX * radiusY;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double sumR = 0;
                double sumG = 0;
                double sumB = 0;
                double sumA = 0;

                int index = y * w + x;
                for (int i = y - radiusYHalf; i < y - radiusYHalf + radiusY; i++) {
                    for (int j = x - radiusXHalf; j < x - radiusXHalf + radiusX; j++) {
                        int index2 = i * w + j;
                        int v;
                        if (i < 0 || j < 0 || i >= h || j >= w || (mask != null && (((mask[index2] >> 24) & 0xff) == 0))) {
                            v = 0;
                        } else {
                            v = pixels[index2];
                        }
                        double a = (v >> 24) & 0xff;
                        double r = ((v >> 16) & 0xff);
                        double g = ((v >> 8) & 0xff);
                        double b = ((v) & 0xff);

                        r = r * a / 255.0;
                        g = g * a / 255.0;
                        b = b * a / 255.0;

                        sumA += a;
                        sumR += r;
                        sumG += g;
                        sumB += b;
                    }
                }
                int da = (int) Math.floor(sumA / divisor);
                int da_mod = da == 0 ? 255 : da;
                int dr = (int) Math.floor(sumR / divisor * 255.0 / (double) da_mod);
                int dg = (int) Math.floor(sumG / divisor * 255.0 / (double) da_mod);
                int db = (int) Math.floor(sumB / divisor * 255.0 / (double) da_mod);

                if (dr > 255) {
                    dr = 255;
                }
                if (dg > 255) {
                    dg = 255;
                }
                if (db > 255) {
                    db = 255;
                }

                newColors[index] = RGBA.toInt(dr, dg, db, da);
            }
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int index = y * w + x;
                pixels[index] = newColors[index];
            }
        }
    }

    private static void boxBlurSingleIterationTwoPass(int[] pixels, int[] mask, int[] newColors, int w, int h, int radiusX, int radiusY) {

        if (radiusX == 0) {
            radiusX = 1;
        }
        if (radiusY == 0) {
            radiusY = 1;
        }

        long limit = Configuration.boxBlurPixelsLimit.get() * 100000L;

        if ((long) w * (long) h > limit) {
            return;
        }

        while (((long) radiusY * (long) radiusX * (long) w * (long) h) > limit) {
            // decrease radius
            if (radiusY > 1) {
                radiusY--;
            }
            if (radiusX > 1) {
                radiusX--;
            }
        }

        int[] secondPass = new int[w * h];
        boxBlurHorizontal(pixels, mask, secondPass, w, h, radiusX);
        boxBlurVertical(secondPass, mask, newColors, w, h, radiusY);
        System.arraycopy(newColors, 0, pixels, 0, newColors.length);
    }

    private static void boxBlurHorizontal(int[] pixels, int[] mask, int[] result, int w, int h, int radius) {
        if (radius == 0) {
            radius = 1;
        }

        int radiusHalf = radius / 2;
        double divisor = radius;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                double sumR = 0;
                double sumG = 0;
                double sumB = 0;
                double sumA = 0;

                for (int j = x - radiusHalf; j < x - radiusHalf + radius; j++) {
                    int index2 = y * w + j;
                    int v;
                    if (j < 0 || j >= w || (mask != null && (((mask[index2] >> 24) & 0xff) == 0))) {
                        v = 0;
                    } else {
                        v = pixels[index2];
                    }
                    double a = (v >> 24) & 0xff;
                    double r = ((v >> 16) & 0xff);
                    double g = ((v >> 8) & 0xff);
                    double b = ((v) & 0xff);

                    r = r * a / 255.0;
                    g = g * a / 255.0;
                    b = b * a / 255.0;

                    sumA += a;
                    sumR += r;
                    sumG += g;
                    sumB += b;
                }

                int da = (int) Math.floor(sumA / divisor);
                int da_mod = da == 0 ? 255 : da;
                int dr = (int) Math.floor(sumR / divisor * 255.0 / (double) da_mod);
                int dg = (int) Math.floor(sumG / divisor * 255.0 / (double) da_mod);
                int db = (int) Math.floor(sumB / divisor * 255.0 / (double) da_mod);

                if (dr > 255) {
                    dr = 255;
                }
                if (dg > 255) {
                    dg = 255;
                }
                if (db > 255) {
                    db = 255;
                }

                int index = y * w + x;

                result[index] = RGBA.toInt(dr, dg, db, da);
            }
        }
    }

    private static void boxBlurVertical(int[] pixels, int[] mask, int[] result, int w, int h, int radius) {
        if (radius == 0) {
            radius = 1;
        }

        int radiusHalf = radius / 2;
        double divisor = radius;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {

                double sumR = 0;
                double sumG = 0;
                double sumB = 0;
                double sumA = 0;

                for (int j = y - radiusHalf; j < y - radiusHalf + radius; j++) {
                    int index2 = j * w + x;
                    int v;
                    if (j < 0 || j >= h || (mask != null && (((mask[index2] >> 24) & 0xff) == 0))) {
                        v = 0;
                    } else {
                        v = pixels[index2];
                    }
                    double a = (v >> 24) & 0xff;
                    double r = ((v >> 16) & 0xff);
                    double g = ((v >> 8) & 0xff);
                    double b = ((v) & 0xff);

                    r = r * a / 255.0;
                    g = g * a / 255.0;
                    b = b * a / 255.0;

                    sumA += a;
                    sumR += r;
                    sumG += g;
                    sumB += b;
                }

                int da = (int) Math.floor(sumA / divisor);
                int da_mod = da == 0 ? 255 : da;
                int dr = (int) Math.floor(sumR / divisor * 255.0 / (double) da_mod);
                int dg = (int) Math.floor(sumG / divisor * 255.0 / (double) da_mod);
                int db = (int) Math.floor(sumB / divisor * 255.0 / (double) da_mod);

                if (dr > 255) {
                    dr = 255;
                }
                if (dg > 255) {
                    dg = 255;
                }
                if (db > 255) {
                    db = 255;
                }

                int index = y * w + x;

                result[index] = RGBA.toInt(dr, dg, db, da);
            }
        }
    }

    private static void premultiply(int[] p) {
        int length = p.length;
        int offset = 0;
        length += offset;
        for (int i = offset; i < length; i++) {
            int rgb = p[i];
            int a = rgb >> 24 & 0xff;
            int r = rgb >> 16 & 0xff;
            int g = rgb >> 8 & 0xff;
            int b = rgb & 0xff;
            float f = (float) a * 0.003921569F;
            r = (int) ((float) r * f);
            g = (int) ((float) g * f);
            b = (int) ((float) b * f);
            p[i] = a << 24 | r << 16 | g << 8 | b;
        }
    }

    private static void unpremultiply(int[] p) {
        int length = p.length;
        int offset = 0;
        length += offset;
        for (int i = offset; i < length; i++) {
            int rgb = p[i];
            int a = rgb >> 24 & 0xff;
            int r = rgb >> 16 & 0xff;
            int g = rgb >> 8 & 0xff;
            int b = rgb & 0xff;
            if (a == 0 || a == 255) {
                continue;
            }
            float f = 255F / (float) a;
            r = (int) ((float) r * f);
            g = (int) ((float) g * f);
            b = (int) ((float) b * f);
            if (r > 255) {
                r = 255;
            }
            if (g > 255) {
                g = 255;
            }
            if (b > 255) {
                b = 255;
            }
            p[i] = a << 24 | r << 16 | g << 8 | b;
        }
    }

    public static SerializableImage blur(SerializableImage src, int hRadius, int vRadius, int iterations) {
        int[] pixels = (int[]) getRGB(src.getBufferedImage()).clone();
        int width = src.getWidth();
        int height = src.getHeight();
        blur(pixels, width, height, hRadius, vRadius, iterations, null);
        BufferedImage ret = new BufferedImage(width, height, src.getType());
        setRGB(ret, width, height, pixels);
        return new SerializableImage(ret);
    }

    private static void blur(int[] src, int width, int height, int hRadius, int vRadius, int iterations, int[] mask) {
        int[] inPixels = src;
        int[] temp = new int[width * height];
        for (int i = 0; i < iterations; i++) {
            boxBlurSingleIteration(inPixels, mask, temp, width, height, hRadius, vRadius);
        }
    }

    public static SerializableImage bevel(SerializableImage src, int blurX, int blurY, float strength, int type, int highlightColor, int shadowColor, float angle, float distance, boolean knockout, int iterations) {
        return new SerializableImage(gradientBevel(src.getBufferedImage(), new Color[]{
            new Color(shadowColor, true),
            new Color(shadowColor & 0x00ffffff, true),
            new Color(highlightColor & 0x00ffffff, true),
            new Color(highlightColor, true)
        }, new float[]{0, 127f / 255f, 128f / 255f, 1}, blurX, blurY, strength, type, angle, distance, knockout, iterations));
    }

    public static SerializableImage gradientBevel(SerializableImage src, Color[] colors, float[] ratios, int blurX, int blurY, float strength, int type, float angle, float distance, boolean knockout, int iterations) {
        return new SerializableImage(gradientBevel(src.getBufferedImage(), colors, ratios, blurX, blurY, strength, type, angle, distance, knockout, iterations));
    }

    private static BufferedImage gradientBevel(BufferedImage src, Color[] colors, float[] ratios, int blurX, int blurY, float strength, int type, float angle, float distance, boolean knockout, int iterations) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage retImg = new BufferedImage(width, height, src.getType());
        int[] srcPixels = getRGB(src);

        int[] revPixels = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            revPixels[i] = (srcPixels[i] & 0xffffff) + ((255 - ((srcPixels[i] >> 24) & 0xff)) << 24);
        }

        BufferedImage gradient = new BufferedImage(512, 1, src.getType());
        Graphics2D gg = gradient.createGraphics();

        Point p1 = POINT_0_0;
        Point p2 = POINT_511_0;
        gg.setPaint(new LinearGradientPaint(p1, p2, ratios, colors));
        gg.fill(RECTANGLE_512_1);
        int[] gradientPixels = getRGB(gradient);

        BufferedImage shadowInner = null;
        BufferedImage hilightInner = null;
        if (type != OUTER) {
            BufferedImage hilightIm = dropShadow(src, 0, 0, angle, distance, Color.red, true, iterations, strength, true, true);
            BufferedImage shadowIm = dropShadow(src, 0, 0, angle + 180, distance, Color.blue, true, iterations, strength, true, true);
            BufferedImage h2 = new BufferedImage(width, height, src.getType());
            BufferedImage s2 = new BufferedImage(width, height, src.getType());
            Graphics2D hc = h2.createGraphics();
            Graphics2D sc = s2.createGraphics();
            hc.drawImage(hilightIm, 0, 0, null);
            hc.setComposite(AlphaComposite.DstOut);
            hc.drawImage(shadowIm, 0, 0, null);

            sc.drawImage(shadowIm, 0, 0, null);
            sc.setComposite(AlphaComposite.DstOut);
            sc.drawImage(hilightIm, 0, 0, null);
            shadowInner = s2;
            hilightInner = h2;
        }

        BufferedImage shadowOuter = null;
        BufferedImage hilightOuter = null;
        if (type != INNER) {
            BufferedImage hilightIm = dropShadow(src, 0, 0, angle + 180, distance, Color.red, false, iterations, strength, true, true);
            BufferedImage shadowIm = dropShadow(src, 0, 0, angle, distance, Color.blue, false, iterations, strength, true, true);
            BufferedImage h2 = new BufferedImage(width, height, src.getType());
            BufferedImage s2 = new BufferedImage(width, height, src.getType());
            Graphics2D hc = h2.createGraphics();
            Graphics2D sc = s2.createGraphics();
            hc.drawImage(hilightIm, 0, 0, null);
            hc.setComposite(AlphaComposite.DstOut);
            hc.drawImage(shadowIm, 0, 0, null);

            sc.drawImage(shadowIm, 0, 0, null);
            sc.setComposite(AlphaComposite.DstOut);
            sc.drawImage(hilightIm, 0, 0, null);
            shadowOuter = s2;
            hilightOuter = h2;
        }

        BufferedImage hilightIm = null;
        BufferedImage shadowIm = null;
        switch (type) {
            case OUTER:
                hilightIm = hilightOuter;
                shadowIm = shadowOuter;
                break;
            case INNER:
                hilightIm = hilightInner;
                shadowIm = shadowInner;
                break;
            case FULL:
                hilightIm = hilightInner;
                shadowIm = shadowInner;
                Graphics2D hc = hilightIm.createGraphics();
                hc.setComposite(AlphaComposite.SrcOver);
                hc.drawImage(hilightOuter, 0, 0, null);
                Graphics2D sc = shadowIm.createGraphics();
                sc.setComposite(AlphaComposite.SrcOver);
                sc.drawImage(shadowOuter, 0, 0, null);
                break;
        }

        int[] mask = null;
        if (type == INNER) {
            mask = srcPixels;
        }
        if (type == OUTER) {
            mask = revPixels;
        }

        Graphics2D retc = retImg.createGraphics();
        retc.setColor(Color.black);
        retc.fillRect(0, 0, width, height);
        retc.setComposite(AlphaComposite.SrcOver);
        retc.drawImage(shadowIm, 0, 0, null);
        retc.drawImage(hilightIm, 0, 0, null);

        int[] ret = getRGB(retImg);
        blur(ret, width, height, blurX, blurY, iterations, mask);

        for (int i = 0; i < srcPixels.length; i++) {
            int ah = (int) (((ret[i] >> 16) & 0xFF) * strength);
            int as = (int) ((ret[i] & 0xFF) * strength);
            int ra = cut(ah - as, -255, 255);
            ret[i] = gradientPixels[255 + ra];
        }

        setRGB(retImg, width, height, ret);

        if (!knockout) {
            Graphics2D g = retImg.createGraphics();
            g.setComposite(AlphaComposite.DstOver);
            g.drawImage(src, 0, 0, null);
        }
        return retImg;
    }

    public static SerializableImage glow(SerializableImage src, int blurX, int blurY, float strength, Color color, boolean inner, boolean knockout, int iterations) {
        return new SerializableImage(dropShadow(src.getBufferedImage(), blurX, blurY, 45, 0, color, inner, iterations, strength, knockout, true));
    }

    private static Color over(Color a, Color b) {
        int resultA = a.getAlpha() + b.getAlpha() * (255 - a.getAlpha()) / 255;
        int resultR = cut((a.getRed() * (a.getAlpha() / 255.0) + b.getRed() * (b.getAlpha() / 255.0) * (1 - (a.getAlpha() / 255.0))) / (resultA / 255.0));
        int resultG = cut((a.getGreen() * (a.getAlpha() / 255.0) + b.getGreen() * (b.getAlpha() / 255.0) * (1 - (a.getAlpha() / 255.0))) / (resultA / 255.0));
        int resultB = cut((a.getBlue() * (a.getAlpha() / 255.0) + b.getBlue() * (b.getAlpha() / 255.0) * (1 - (a.getAlpha() / 255.0))) / (resultA / 255.0));
        return new Color(resultR, resultG, resultB, resultA);
    }

    public static SerializableImage dropShadow(SerializableImage src, int blurX, int blurY, float angle, double distance, Color color, boolean inner, int iterations, float strength, boolean knockout, boolean compositeSource) {
        return new SerializableImage(dropShadow(src.getBufferedImage(), blurX, blurY, angle, distance, color, inner, iterations, strength, knockout, compositeSource));
    }

    private static BufferedImage dropShadow(BufferedImage src, int blurX, int blurY, float angle, double distance, Color color, boolean inner, int iterations, float strength, boolean knockout, boolean compositeSource) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] srcPixels = getRGB(src);
        int[] shadow = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            int alpha = (srcPixels[i] >> 24) & 0xff;
            if (inner) {
                alpha = 255 - alpha;
            }
            Color shadowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), cut(color.getAlpha() * alpha / 255f));
            shadow[i] = shadowColor.getRGB();
        }

        Color colorAlpha = ALPHA;
        double angleRad = angle / 180 * Math.PI;
        double moveX = (distance * Math.cos(angleRad));
        double moveY = (distance * Math.sin(angleRad));
        shadow = moveRGB(width, height, shadow, moveX, moveY, inner ? color : colorAlpha);

        if (blurX > 0 || blurY > 0) {
            blur(shadow, width, height, blurX, blurY, iterations, null);
        }

        if (strength != 1f) {
            for (int i = 0; i < shadow.length; i++) {
                int alpha = (shadow[i] >> 24) & 0xff;
                alpha = cut(alpha * strength);
                shadow[i] = (shadow[i] & 0xffffff) | (alpha << 24);
            }
        }

        if (knockout || inner) {
            for (int i = 0; i < shadow.length; i++) {
                int mask = (srcPixels[i] >> 24) & 0xff;
                if (!inner) {
                    mask = 255 - mask;
                }

                if (inner && compositeSource && !knockout) {
                    Color shadowColor = new Color(shadow[i], true);
                    Color srcColor = new Color(srcPixels[i], true);
                    srcColor = new Color(srcColor.getRed(), srcColor.getGreen(), srcColor.getBlue(), 255);

                    Color resultColor = over(shadowColor, srcColor);

                    shadow[i] = resultColor.getRGB();
                }
                shadow[i] = (shadow[i] & 0xffffff) + ((mask * ((shadow[i] >> 24) & 0xff) / 255) << 24);
            }
        }

        BufferedImage retCanvas = new BufferedImage(width, height, src.getType());
        setRGB(retCanvas, width, height, shadow);

        if (!knockout && compositeSource && !inner) {
            Graphics2D g = retCanvas.createGraphics();
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(src, 0, 0, null);
        }

        return retCanvas;
    }

    public static SerializableImage gradientGlow(SerializableImage src, int blurX, int blurY, float angle, double distance, Color[] colors, float[] ratios, int type, int iterations, float strength, boolean knockout) {
        return new SerializableImage(gradientGlow(src.getBufferedImage(), blurX, blurY, angle, distance, colors, ratios, type, iterations, strength, knockout));
    }

    private static BufferedImage gradientGlow(BufferedImage src, int blurX, int blurY, float angle, double distance, Color[] colors, float[] ratios, int type, int iterations, float strength, boolean knockout) {

        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage gradCanvas = new BufferedImage(256, 1, src.getType());
        Graphics2D gg = gradCanvas.createGraphics();

        Point p1 = POINT_0_0;
        Point p2 = POINT_255_0;
        gg.setPaint(new LinearGradientPaint(p1, p2, ratios, colors));
        gg.fill(RECTANGLE_256_1);
        int[] gradientPixels = getRGB(gradCanvas);

        double angleRad = angle / 180 * Math.PI;
        double moveX = (distance * Math.cos(angleRad));
        double moveY = (distance * Math.sin(angleRad));
        int[] srcPixels = getRGB(src);
        int[] revPixels = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            revPixels[i] = (srcPixels[i] & 0xffffff) + ((255 - ((srcPixels[i] >> 24) & 0xff)) << 24);
        }

        int[] shadow = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            shadow[i] = 0 + ((cut(strength * ((srcPixels[i] >> 24) & 0xff))) << 24);
        }

        Color colorAlpha = ALPHA;
        shadow = moveRGB(width, height, shadow, moveX, moveY, colorAlpha);

        int[] mask = null;
        if (type == INNER) {
            mask = srcPixels;
        }
        if (type == OUTER) {
            mask = revPixels;
        }

        blur(shadow, width, height, blurX, blurY, iterations, mask);

        if (mask != null) {
            for (int i = 0; i < mask.length; i++) {
                int m = (mask[i] >> 24);
                if (m == 0) {
                    shadow[i] = 0;
                }
            }
        }

        for (int i = 0; i < shadow.length; i++) {
            int a = (shadow[i] >> 24) & 0xff;
            shadow[i] = gradientPixels[a];
        }

        BufferedImage retCanvas = new BufferedImage(width, height, src.getType());
        setRGB(retCanvas, width, height, shadow);

        if (!knockout) {
            Graphics2D retImg = retCanvas.createGraphics();
            retImg.setComposite(AlphaComposite.DstOver);
            retImg.drawImage(src, 0, 0, null);
        }

        return retCanvas;
    }

    private static int[] getRGB(BufferedImage image) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        }
        int width = image.getWidth();
        return image.getRGB(0, 0, width, image.getHeight(), null, 0, width);
    }

    public static void setRGB(BufferedImage image, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            image.getRaster().setDataElements(0, 0, width, height, pixels);
        } else {
            image.setRGB(0, 0, width, height, pixels, 0, width);
        }
    }

    private static int[] moveRGB(int width, int height, int[] rgb, double deltaX, double deltaY, Color fill) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setRGB(img, width, height, rgb);
        BufferedImage retImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) retImg.getGraphics();
        g.setPaint(fill);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setTransform(AffineTransform.getTranslateInstance(deltaX, deltaY));
        g.setComposite(AlphaComposite.Src);
        g.drawImage(img, 0, 0, null);
        return getRGB(retImg);
    }

    public static SerializableImage convolution(
            SerializableImage src,
            float[] matrix,
            int w,
            int h,
            float divisor,
            float bias,
            Color defaultColor,
            boolean clamp,
            boolean preserveAlpha,
            int srcX,
            int srcY,
            int srcWidth,
            int srcHeight
    ) {
        Kernel kernel = new Kernel(w, h, matrix);
        BufferedImage dst = new BufferedImage(src.getWidth() + 1, src.getHeight() + 1, src.getType());
        BufferedImageOp op = new ConvolveOp(
                kernel,
                new RenderingHints(null),
                divisor,
                bias,
                defaultColor,
                clamp,
                preserveAlpha,
                srcX,
                srcY,
                srcWidth,
                srcHeight
        );
        op.filter(src.getBufferedImage(), dst);
        return new SerializableImage(dst);
    }

    public static SerializableImage colorMatrix(SerializableImage src, float[][] matrix) {
        /*BandCombineOp changeColors = new BandCombineOp(matrix, new RenderingHints(null));
         Raster sourceRaster = src.getRaster();
         WritableRaster displayRaster = sourceRaster.createCompatibleWritableRaster();
         changeColors.filter(sourceRaster, displayRaster);
         return new SerializableImage(src.getColorModel(), displayRaster, true, null);*/
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        int[] pixels = getRGB(src.getBufferedImage()).clone();
        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];
            int a = (rgb >> 24) & 0xff;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            float[] mr = matrix[0];
            int r2 = cut(mr[0] * r + mr[1] * g + mr[2] * b + mr[3] * a + mr[4]);
            float[] mg = matrix[1];
            int g2 = cut(mg[0] * r + mg[1] * g + mg[2] * b + mg[3] * a + mg[4]);
            float[] mb = matrix[2];
            int b2 = cut(mb[0] * r + mb[1] * g + mb[2] * b + mb[3] * a + mb[4]);
            float[] ma = matrix[3];
            int a2 = cut(ma[0] * r + ma[1] * g + ma[2] * b + ma[3] * a + ma[4]);
            pixels[i] = (a2 << 24) | (r2 << 16) | (g2 << 8) | b2;
        }
        setRGB(dst, src.getWidth(), src.getHeight(), pixels);
        return new SerializableImage(dst);
    }

    private static int cut(int val, int min, int max) {
        if (val > max) {
            val = max;
        }
        if (val < min) {
            val = min;
        }
        return val;
    }

    private static int cut(double val) {
        int i = (int) Math.round(val);
        if (i < 0) {
            i = 0;
        }
        if (i > 255) {
            i = 255;
        }
        return i;
    }

    public static int colorEffect(int rgb,
            int redAddTerm, int greenAddTerm, int blueAddTerm, int alphaAddTerm,
            int redMultTerm, int greenMultTerm, int blueMultTerm, int alphaMultTerm) {
        int a = (rgb >> 24) & 0xff;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        r = cut(((r * redMultTerm) / 256) + redAddTerm);
        g = cut(((g * greenMultTerm) / 256) + greenAddTerm);
        b = cut(((b * blueMultTerm) / 256) + blueAddTerm);
        a = cut(((a * alphaMultTerm) / 256) + alphaAddTerm);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static SerializableImage colorEffect(SerializableImage src,
            int redAddTerm, int greenAddTerm, int blueAddTerm, int alphaAddTerm,
            int redMultTerm, int greenMultTerm, int blueMultTerm, int alphaMultTerm) {
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        int[] pixels = getRGB(src.getBufferedImage()).clone();
        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];
            int a = (rgb >> 24) & 0xff;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            r = cut(((r * redMultTerm) / 256) + redAddTerm);
            g = cut(((g * greenMultTerm) / 256) + greenAddTerm);
            b = cut(((b * blueMultTerm) / 256) + blueAddTerm);
            a = cut(((a * alphaMultTerm) / 256) + alphaAddTerm);
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        setRGB(dst, src.getWidth(), src.getHeight(), pixels);
        return new SerializableImage(dst);
    }
}
