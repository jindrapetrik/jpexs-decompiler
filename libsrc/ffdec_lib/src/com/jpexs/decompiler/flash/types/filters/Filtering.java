/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Kernel;
import java.awt.image.SinglePixelPackedSampleModel;

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

    /**
     * Two separable sliding windows. Integer alpha-weighted sums avoid drift
     * when pixels enter and leave the window; edges count only existing pixels.
     */
    private static void boxBlurHorizontal(int[] pixels, int[] result, int w, int h, int radius) {
        int half = radius / 2;
        for (int y = 0; y < h; y++) {
            int row = y * w;
            long sumA = 0;
            long sumR = 0;
            long sumG = 0;
            long sumB = 0;
            int end = Math.min(w, radius - half);
            for (int x = 0; x < end; x++) {
                int pixel = pixels[row + x];
                int a = pixel >>> 24;
                sumA += a;
                sumR += ((pixel >>> 16) & 255) * a;
                sumG += ((pixel >>> 8) & 255) * a;
                sumB += (pixel & 255) * a;
            }
            for (int x = 0; x < w; x++) {
                int count = Math.min(w, x - half + radius) - Math.max(0, x - half);
                result[row + x] = blurPixel(sumA, sumR, sumG, sumB, count);
                int leaving = x - half;
                if (leaving >= 0) {
                    int pixel = pixels[row + leaving];
                    int a = pixel >>> 24;
                    sumA -= a;
                    sumR -= ((pixel >>> 16) & 255) * a;
                    sumG -= ((pixel >>> 8) & 255) * a;
                    sumB -= (pixel & 255) * a;
                }
                int entering = x - half + radius;
                if (entering < w) {
                    int pixel = pixels[row + entering];
                    int a = pixel >>> 24;
                    sumA += a;
                    sumR += ((pixel >>> 16) & 255) * a;
                    sumG += ((pixel >>> 8) & 255) * a;
                    sumB += (pixel & 255) * a;
                }
            }
        }
    }

    private static void boxBlurVertical(int[] pixels, int[] result, int w, int h, int radius) {
        int half = radius / 2;
        for (int x = 0; x < w; x++) {
            long sumA = 0;
            long sumR = 0;
            long sumG = 0;
            long sumB = 0;
            int end = Math.min(h, radius - half);
            for (int y = 0; y < end; y++) {
                int pixel = pixels[y * w + x];
                int a = pixel >>> 24;
                sumA += a;
                sumR += ((pixel >>> 16) & 255) * a;
                sumG += ((pixel >>> 8) & 255) * a;
                sumB += (pixel & 255) * a;
            }
            for (int y = 0; y < h; y++) {
                int count = Math.min(h, y - half + radius) - Math.max(0, y - half);
                result[y * w + x] = blurPixel(sumA, sumR, sumG, sumB, count);
                int leaving = y - half;
                if (leaving >= 0) {
                    int pixel = pixels[leaving * w + x];
                    int a = pixel >>> 24;
                    sumA -= a;
                    sumR -= ((pixel >>> 16) & 255) * a;
                    sumG -= ((pixel >>> 8) & 255) * a;
                    sumB -= (pixel & 255) * a;
                }
                int entering = y - half + radius;
                if (entering < h) {
                    int pixel = pixels[entering * w + x];
                    int a = pixel >>> 24;
                    sumA += a;
                    sumR += ((pixel >>> 16) & 255) * a;
                    sumG += ((pixel >>> 8) & 255) * a;
                    sumB += (pixel & 255) * a;
                }
            }
        }
    }

    private static int blurPixel(long sumA, long sumR, long sumG, long sumB, int count) {
        int alpha = (int) (sumA / count);
        long divisor = (long) count * (alpha == 0 ? 255 : alpha);
        return (alpha << 24)
                | ((int) Math.min(255, sumR / divisor) << 16)
                | ((int) Math.min(255, sumG / divisor) << 8)
                | (int) Math.min(255, sumB / divisor);
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
        int[] pixels = getRGB(src.getBufferedImage(), true);
        int width = src.getWidth();
        int height = src.getHeight();
        blur(pixels, width, height, hRadius, vRadius, iterations);
        BufferedImage ret = new BufferedImage(width, height, src.getType());
        setRGB(ret, width, height, pixels);
        return new SerializableImage(ret);
    }

    private static void blur(int[] src, int width, int height, int hRadius, int vRadius, int iterations) {
        long pixels = (long) width * height;
        long limit = Configuration.boxBlurPixelsLimit.get() * 100000L;
        if (iterations <= 0 || pixels == 0 || pixels > limit) {
            return;
        }
        hRadius = Math.max(1, hRadius);
        vRadius = Math.max(1, vRadius);
        while ((long) hRadius * vRadius > limit / pixels) {
            if (hRadius > 1) {
                hRadius--;
            }
            if (vRadius > 1) {
                vRadius--;
            }
        }
        // One workspace for every iteration, with the vertical pass writing
        // directly back to the input instead of copying a complete image.
        int[] temp = new int[src.length];
        for (int i = 0; i < iterations; i++) {
            boxBlurHorizontal(src, temp, width, height, hRadius);
            boxBlurVertical(temp, src, width, height, vRadius);
        }
    }

    public static SerializableImage bevel(SerializableImage src, int blurX, int blurY, float strength, int type, int highlightColor, int shadowColor, float angle, float distance, boolean knockout, boolean compositeSource, int iterations) {
        return new SerializableImage(gradientBevel(src.getBufferedImage(), new Color[]{
            new Color(shadowColor, true),
            new Color(shadowColor & 0x00ffffff, true),
            new Color(highlightColor & 0x00ffffff, true),
            new Color(highlightColor, true)
        }, new float[]{0, 127f / 255f, 128f / 255f, 1}, blurX, blurY, strength, type, angle, distance, knockout, compositeSource, iterations));
    }

    public static SerializableImage gradientBevel(SerializableImage src, Color[] colors, float[] ratios, int blurX, int blurY, float strength, int type, float angle, float distance, boolean knockout, boolean compositeSource, int iterations) {
        return new SerializableImage(gradientBevel(src.getBufferedImage(), colors, ratios, blurX, blurY, strength, type, angle, distance, knockout, compositeSource, iterations));
    }

    private static BufferedImage gradientBevel(BufferedImage src, Color[] colors, float[] ratios, int blurX, int blurY, float strength, int type, float angle, float distance, boolean knockout, boolean compositeSource, int iterations) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage retImg = new BufferedImage(width, height, src.getType());
        int[] srcPixels = getRGB(src);

        /*
        float middle = 128/255f;
        float realMiddle = 0.5f;
        boolean wasMiddle = false;
        for (int i = 0; i < ratios.length; i++) {
            if (ratios[i] == middle) {
                wasMiddle = true;
            }
            if (!wasMiddle) {
                ratios[i] = ratios[i] * realMiddle / middle;
            } else {
                ratios[i] = realMiddle + (ratios[i] - middle) * realMiddle / (1 - middle);
            }
        }*/
        BufferedImage gradient = new BufferedImage(512, 1, src.getType());
        Graphics2D gg = gradient.createGraphics();

        Point p1 = POINT_0_0;
        Point p2 = POINT_511_0;
        gg.setPaint(new LinearGradientPaint(p1, p2, ratios, colors));
        gg.fill(RECTANGLE_512_1);
        int[] gradientPixels = getRGB(gradient);

        BufferedImage shadowInner;
        BufferedImage hilightInner;

        BufferedImage hilightImInner = dropShadow(src, 0, 0, angle, distance, Color.red, true, iterations, strength, true, true);
        BufferedImage shadowImInner = dropShadow(src, 0, 0, angle + 180, distance, Color.blue, true, iterations, strength, true, true);
        BufferedImage h2Inner = new BufferedImage(width, height, src.getType());
        BufferedImage s2Inner = new BufferedImage(width, height, src.getType());
        Graphics2D hcInner = h2Inner.createGraphics();
        Graphics2D scInner = s2Inner.createGraphics();
        hcInner.drawImage(hilightImInner, 0, 0, null);
        hcInner.setComposite(AlphaComposite.DstOut);
        hcInner.drawImage(shadowImInner, 0, 0, null);

        scInner.drawImage(shadowImInner, 0, 0, null);
        scInner.setComposite(AlphaComposite.DstOut);
        scInner.drawImage(hilightImInner, 0, 0, null);
        shadowInner = s2Inner;
        hilightInner = h2Inner;

        BufferedImage shadowOuter;
        BufferedImage hilightOuter;

        BufferedImage hilightImOuter = dropShadow(src, 0, 0, angle + 180, distance, Color.red, false, iterations, strength, true, true);
        BufferedImage shadowImOuter = dropShadow(src, 0, 0, angle, distance, Color.blue, false, iterations, strength, true, true);
        BufferedImage h2Outer = new BufferedImage(width, height, src.getType());
        BufferedImage s2Outer = new BufferedImage(width, height, src.getType());
        Graphics2D hcOuter = h2Outer.createGraphics();
        Graphics2D scOuter = s2Outer.createGraphics();
        hcOuter.drawImage(hilightImOuter, 0, 0, null);
        hcOuter.setComposite(AlphaComposite.DstOut);
        hcOuter.drawImage(shadowImOuter, 0, 0, null);

        scOuter.drawImage(shadowImOuter, 0, 0, null);
        scOuter.setComposite(AlphaComposite.DstOut);
        scOuter.drawImage(hilightImOuter, 0, 0, null);
        shadowOuter = s2Outer;
        hilightOuter = h2Outer;

        BufferedImage hilightIm;
        BufferedImage shadowIm;
        hilightIm = hilightInner;
        shadowIm = shadowInner;
        Graphics2D hc = hilightIm.createGraphics();
        hc.setComposite(AlphaComposite.SrcOver);
        hc.drawImage(hilightOuter, 0, 0, null);
        Graphics2D sc = shadowIm.createGraphics();
        sc.setComposite(AlphaComposite.SrcOver);
        sc.drawImage(shadowOuter, 0, 0, null);

        Graphics2D retc = retImg.createGraphics();
        retc.setColor(Color.black);
        retc.fillRect(0, 0, width, height);
        retc.setComposite(AlphaComposite.SrcOver);
        retc.drawImage(shadowIm, 0, 0, null);
        retc.drawImage(hilightIm, 0, 0, null);

        int[] bevel = getRGB(retImg);
        blur(bevel, width, height, blurX, blurY, iterations);

        for (int i = 0; i < srcPixels.length; i++) {
            int ah = (int) (((bevel[i] >> 16) & 0xFF) * strength);
            int as = (int) ((bevel[i] & 0xFF) * strength);
            int ra = cut(ah - as, -255, 255);
            bevel[i] = gradientPixels[255 + ra];
        }

        return compose(width, height, src, bevel, type, knockout, compositeSource);
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
            shadow[i] = (cut(color.getAlpha() * alpha / 255f) << 24) | (color.getRGB() & 0xffffff);
        }

        Color colorAlpha = ALPHA;
        double angleRad = angle / 180 * Math.PI;
        double moveX = (distance * Math.cos(angleRad));
        double moveY = (distance * Math.sin(angleRad));
        shadow = moveRGB(width, height, shadow, moveX, moveY, inner ? color : colorAlpha);

        if (blurX > 0 || blurY > 0) {
            blur(shadow, width, height, blurX, blurY, iterations);
        }

        if (strength != 1f) {
            for (int i = 0; i < shadow.length; i++) {
                int alpha = (shadow[i] >> 24) & 0xff;
                alpha = cut(alpha * strength);
                shadow[i] = (shadow[i] & 0xffffff) | (alpha << 24);
            }
        }

        return compose(width, height, src, shadow, inner ? INNER : OUTER, knockout, compositeSource);
    }

    private static BufferedImage compose(int width, int height, BufferedImage srcImage, int[] pixels, int type, boolean knockout, boolean compositeSource) {
        BufferedImage resultImage = new BufferedImage(width, height, srcImage.getType());
        setRGB(resultImage, width, height, pixels);
        Graphics2D g = resultImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (type == FULL && !knockout && compositeSource) {
            g.setComposite(AlphaComposite.DstOver);
            g.drawImage(srcImage, 0, 0, null);
        } else if (type == INNER) {
            if (knockout || !compositeSource) {
                g.setComposite(AlphaComposite.DstIn);
            } else {
                g.setComposite(AlphaComposite.DstAtop);
            }
            g.drawImage(srcImage, 0, 0, null);
        } else if (type == OUTER) {
            if (knockout) {
                g.setComposite(AlphaComposite.DstOut);
                g.drawImage(srcImage, 0, 0, null);
            } else if (compositeSource) {
                g.setComposite(AlphaComposite.SrcOver);
                g.drawImage(srcImage, 0, 0, null);
            }
        }
        return resultImage;
    }

    public static SerializableImage gradientGlow(SerializableImage src, int blurX, int blurY, float angle, double distance, Color[] colors, float[] ratios, int type, int iterations, float strength, boolean knockout, boolean compositeSource) {
        return new SerializableImage(gradientGlow(src.getBufferedImage(), blurX, blurY, angle, distance, colors, ratios, type, iterations, strength, knockout, compositeSource));
    }

    private static BufferedImage gradientGlow(BufferedImage src, int blurX, int blurY, float angle, double distance, Color[] colors, float[] ratios, int type, int iterations, float strength, boolean knockout, boolean compositeSource) {

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

        int[] shadow = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            shadow[i] = 0 + ((cut(strength * ((srcPixels[i] >> 24) & 0xff))) << 24);
        }

        Color colorAlpha = ALPHA;
        shadow = moveRGB(width, height, shadow, moveX, moveY, colorAlpha);

        blur(shadow, width, height, blurX, blurY, iterations);

        for (int i = 0; i < shadow.length; i++) {
            int a = (shadow[i] >> 24) & 0xff;
            int gp = gradientPixels[a];
            shadow[i] = gp;
        }

        return compose(width, height, src, shadow, type, knockout, compositeSource);
    }

    private static int[] getRGB(BufferedImage image) {
        return getRGB(image, false);
    }

    private static int[] getRGB(BufferedImage image, boolean copy) {
        int type = image.getType();
        int[] data = packedPixels(image);
        if (data != null && (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)) {
            return copy ? data.clone() : data;
        }
        if (data != null && type == BufferedImage.TYPE_INT_ARGB_PRE) {
            int[] pixels = new int[data.length];
            ColorModel colors = image.getColorModel();
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = colors.getRGB(data[i]);
            }
            return pixels;
        }
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    /** Returns the backing array only for a complete, contiguous packed image. */
    private static int[] packedPixels(BufferedImage image) {
        if (!(image.getRaster().getDataBuffer() instanceof DataBufferInt)
                || !(image.getSampleModel() instanceof SinglePixelPackedSampleModel)) {
            return null;
        }
        DataBufferInt buffer = (DataBufferInt) image.getRaster().getDataBuffer();
        SinglePixelPackedSampleModel model = (SinglePixelPackedSampleModel) image.getSampleModel();
        int offset = buffer.getOffset() - image.getRaster().getSampleModelTranslateX()
                - image.getRaster().getSampleModelTranslateY() * model.getScanlineStride();
        if (buffer.getNumBanks() != 1 || offset != 0 || model.getScanlineStride() != image.getWidth()) {
            return null;
        }
        int[] pixels = buffer.getData();
        return pixels.length == (long) image.getWidth() * image.getHeight() ? pixels : null;
    }

    public static void setRGB(BufferedImage image, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            image.getRaster().setDataElements(0, 0, width, height, pixels);
        } else if (type == BufferedImage.TYPE_INT_ARGB_PRE && width == image.getWidth() && height == image.getHeight()) {
            int[] data = packedPixels(image);
            if (data == null) {
                image.setRGB(0, 0, width, height, pixels, 0, width);
                return;
            }
            ColorModel colors = image.getColorModel();
            // Keep ColorModel's exact premultiplication and rounding while
            // avoiding a raster method call for every individual pixel.
            int[] element = new int[1];
            for (int i = 0; i < data.length; i++) {
                colors.getDataElements(pixels[i], element);
                data[i] = element[0];
            }
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
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
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
        int[] pixels = getRGB(src.getBufferedImage(), true);
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
        int[] pixels = getRGB(src.getBufferedImage(), true);
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
