/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 *
 * @author JPEXS
 */
public class Filtering {

    public static final int INNER = 1;
    public static final int OUTER = 2;
    public static final int FULL = 3;

    private static void boxBlurHorizontal(int[] pixels, int[] mask, int w, int h, int radius) {
        int index = 0;
        int[] newColors = new int[w];

        for (int y = 0; y < h; y++) {
            int hits = 0;
            long r = 0;
            long g = 0;
            long b = 0;
            long a = 0;
            for (int x = -radius; x < w; x++) {
                int oldPixel = x - radius - 1;
                if (oldPixel >= 0) {

                    int color = pixels[index + oldPixel];
                    if ((mask == null) || (((mask[index + oldPixel] >> 24) & 0xff) > 0)) {
                        if (color != 0) {
                            a -= (color >> 24) & 0xff;
                            r -= ((color >> 16) & 0xff);
                            g -= ((color >> 8) & 0xff);
                            b -= ((color) & 0xff);

                        }
                        hits--;
                    }
                }

                int newPixel = x + radius;
                if (newPixel < w) {
                    int color = pixels[index + newPixel];
                    if ((mask == null) || (((mask[index + newPixel] >> 24) & 0xff) > 0)) {
                        if (color != 0) {
                            a += (color >> 24) & 0xff;
                            r += ((color >> 16) & 0xff);
                            g += ((color >> 8) & 0xff);
                            b += ((color) & 0xff);
                        }
                        hits++;
                    }
                }

                if (x >= 0) {
                    if ((mask == null) || (((mask[index + x] >> 24) & 0xff) > 0)) {
                        if (hits == 0) {
                            newColors[x] = 0;
                        } else {
                            newColors[x] = new Color((int) (r / hits) & 0xff, (int) (g / hits) & 0xff, (int) (b / hits) & 0xff, (int) (a / hits)).getRGB();

                        }
                    } else {
                        newColors[x] = 0;
                    }
                }
            }
            System.arraycopy(newColors, 0, pixels, index, w);

            index += w;
        }
    }

    private static void boxBlurVertical(int[] pixels, int[] mask, int w, int h, int radius) {
        int[] newColors = new int[h];
        int oldPixelOffset = -(radius + 1) * w;
        int newPixelOffset = (radius) * w;

        for (int x = 0; x < w; x++) {
            int hits = 0;
            long r = 0;
            long g = 0;
            long b = 0;
            long a = 0;
            int index = -radius * w + x;
            for (int y = -radius; y < h; y++) {
                int oldPixel = y - radius - 1;
                if (oldPixel >= 0) {
                    int color = pixels[index + oldPixelOffset];
                    if ((mask == null) || (((mask[index + oldPixelOffset] >> 24) & 0xff) > 0)) {
                        if (color != 0) {
                            a -= (color >> 24) & 0xff;
                            r -= ((color >> 16) & 0xff);
                            g -= ((color >> 8) & 0xff);
                            b -= ((color) & 0xff);

                        }
                        hits--;
                    }

                }

                int newPixel = y + radius;
                if (newPixel < h) {
                    if ((mask == null) || (((mask[index + newPixelOffset] >> 24) & 0xff) > 0)) {
                        int color = pixels[index + newPixelOffset];
                        if (color != 0) {
                            a += (color >> 24) & 0xff;
                            r += ((color >> 16) & 0xff);
                            g += ((color >> 8) & 0xff);
                            b += ((color) & 0xff);

                        }
                        hits++;
                    }
                }

                if (y >= 0) {
                    if ((mask == null) || (((mask[y * w + x] >> 24) & 0xff) > 0)) {
                        if (hits == 0) {
                            newColors[y] = 0;
                        } else {
                            newColors[y] = new Color((int) (r / hits) & 0xff, (int) (g / hits) & 0xff, (int) (b / hits) & 0xff, (int) (a / hits) & 0xff).getRGB();
                        }
                    } else {
                        newColors[y] = 0;
                    }
                }

                index += w;
            }

            for (int y = 0; y < h; y++) {
                pixels[y * w + x] = newColors[y];
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
        return blur(src, hRadius, vRadius, iterations, null);
    }

    private static SerializableImage blur(SerializableImage src, int hRadius, int vRadius, int iterations, int[] mask) {
        int width = src.getWidth();
        int height = src.getHeight();

        SerializableImage dst = new SerializableImage(width, height, src.getType());

        int[] inPixels = getRGB(src, 0, 0, width, height);
        premultiply(inPixels);

        for (int i = 0; i < iterations; i++) {
            boxBlurHorizontal(inPixels, mask, width, height, hRadius / 2);
            boxBlurVertical(inPixels, mask, width, height, vRadius / 2);
        }
        unpremultiply(inPixels);
        setRGB(dst, 0, 0, width, height, inPixels);
        return dst;
    }

    public static SerializableImage bevel(SerializableImage src, int blurX, int blurY, float strength, int type, Color highlightColor, Color shadowColor, float angle, float distance, boolean knockout, int iterations) {
        return gradientBevel(src, new Color[]{
            highlightColor,
            new Color(highlightColor.getRed(), highlightColor.getGreen(), highlightColor.getBlue(), 0),
            new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), 0),
            shadowColor
        }, new float[]{0, 127f / 255f, 128f / 255f, 1}, blurX, blurY, strength, type, angle, distance, knockout, iterations);
    }

    public static SerializableImage gradientBevel(SerializableImage src, Color[] colors, float[] ratios, int blurX, int blurY, float strength, int type, float angle, float distance, boolean knockout, int iterations) {
        int width = src.getWidth();
        int height = src.getHeight();
        SerializableImage retImg = new SerializableImage(width, height, src.getType());
        if (type == FULL) {
            SerializableImage partIn = gradientBevel(src, colors, ratios, blurX, blurY, strength, INNER, angle, distance, true, iterations);
            SerializableImage partOut = gradientBevel(src, colors, ratios, blurX, blurY, strength, OUTER, angle, distance, true, iterations);
            Graphics2D g = (Graphics2D) retImg.getGraphics();
            g.drawImage(partIn.getBufferedImage(), 0, 0, null);
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(partOut.getBufferedImage(), 0, 0, null);
        } else {
            boolean inner = type == INNER;

            int[] srcPixels = getRGB(src, 0, 0, width, height);
            SerializableImage gradient = new SerializableImage(512, 1, SerializableImage.TYPE_INT_ARGB);
            Graphics2D gg = (Graphics2D) gradient.getGraphics();
            Point pnt1 = new Point(0, 0);
            Point pnt2 = new Point(512, 0);
            gg.setPaint(new LinearGradientPaint(inner ? pnt1 : pnt2, inner ? pnt2 : pnt1, ratios, colors));
            gg.fill(new Rectangle(512, 1));
            int[] gradientPixels = getRGB(gradient, 0, 0, gradient.getWidth(), gradient.getHeight());

            SerializableImage hilightIm = dropShadow(src, blurX, blurY, angle, distance, Color.black, inner, iterations, strength, true);//new DropShadowFilter(blurX, blurY, strength, inner ? highlightColor : shadowColor, angle, distance, inner, true, iterations).filter(src);
            SerializableImage shadowIm = dropShadow(src, blurX, blurY, angle + 180, distance, Color.black, inner, iterations, strength, true); //new DropShadowFilter(blurX, blurY, strength, inner ? shadowColor : highlightColor, angle + 180, distance, inner, true, iterations).filter(src);
            int[] hilight = getRGB(hilightIm, 0, 0, width, height);
            int[] shadow = getRGB(shadowIm, 0, 0, width, height);
            for (int i = 0; i < srcPixels.length; i++) {
                int ha = (hilight[i] >> 24) & 0xff;
                int sa = (shadow[i] >> 24) & 0xff;
                hilight[i] = gradientPixels[255 - ha];
                shadow[i] = gradientPixels[256 + sa];
            }
            for (int i = 0; i < srcPixels.length; i++) {
                int ah = (hilight[i] >> 24) & 0xff;
                int as = (shadow[i] >> 24) & 0xff;
                int ao = (srcPixels[i] >> 24) & 0xff;
                if ((ao == 0) || ((ah > 0) && (as > 0))) {
                    hilight[i] = (hilight[i] & 0x00ffffff) + ((255 - as) << 24);
                    shadow[i] = (shadow[i] & 0x00ffffff) + ((255 - ah) << 24);
                }
            }
            setRGB(shadowIm, 0, 0, width, height, shadow);
            setRGB(hilightIm, 0, 0, width, height, hilight);
            shadow = getRGB(shadowIm, 0, 0, width, height);
            hilight = getRGB(hilightIm, 0, 0, width, height);
            int[] ret = new int[width * height];
            for (int i = 0; i < ret.length; i++) {
                int ah = (hilight[i] >> 24) & 0xff;
                int as = (shadow[i] >> 24) & 0xff;
                if (as >= ah) {
                    ret[i] = (shadow[i] & 0x00ffffff) + ((as - ah) << 24);
                } else {
                    ret[i] = (hilight[i] & 0x00ffffff) + ((ah - as) << 24);
                }
            }
            setRGB(retImg, 0, 0, width, height, ret);
        }
        if (!knockout) {
            Graphics2D g = (Graphics2D) retImg.getGraphics();
            g.setComposite(AlphaComposite.DstOver);
            g.drawImage(src.getBufferedImage(), 0, 0, null);
        }
        return retImg;
    }

    public static SerializableImage glow(SerializableImage src, int blurX, int blurY, float strength, Color color, boolean inner, boolean knockout, int iterations) {
        return dropShadow(src, blurX, blurY, 45, 0, color, inner, iterations, strength, knockout);
    }

    public static SerializableImage dropShadow(SerializableImage src, int blurX, int blurY, float angle, double distance, Color color, boolean inner, int iterations, float strength, boolean knockout) {
        return gradientGlow(src, blurX, blurY, angle, distance, new Color[]{new Color(color.getRed(), color.getGreen(), color.getBlue(), 0), color}, new float[]{0, 1}, inner ? INNER : OUTER, iterations, strength, knockout);
    }

    public static SerializableImage gradientGlow(SerializableImage src, int blurX, int blurY, float angle, double distance, Color[] colors, float[] ratios, int type, int iterations, float strength, boolean knockout) {
        int width = src.getWidth();
        int height = src.getHeight();
        SerializableImage retImg = new SerializableImage(width, height, src.getType());

        if (type == FULL) {
            SerializableImage partIn = gradientGlow(src, blurX, blurY, angle, distance, colors, ratios, INNER, iterations, strength, true);
            SerializableImage partOut = gradientGlow(src, blurX, blurY, angle, distance, colors, ratios, OUTER, iterations, strength, true);
            Graphics2D g = (Graphics2D) retImg.getGraphics();
            g.drawImage(partIn.getBufferedImage(), 0, 0, null);
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(partOut.getBufferedImage(), 0, 0, null);
        } else {
            boolean inner = type == INNER;

            SerializableImage gradient = new SerializableImage(256, 1, SerializableImage.TYPE_INT_ARGB);
            Graphics2D gg = (Graphics2D) gradient.getGraphics();
            gg.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(256, 0), ratios, colors));
            gg.fill(new Rectangle(256, 1));
            int[] gradientPixels = getRGB(gradient, 0, 0, gradient.getWidth(), gradient.getHeight());

            double angleRad = angle / 180 * Math.PI;
            double moveX = (distance * Math.cos(angleRad));
            double moveY = (distance * Math.sin(angleRad));

            int[] srcPixels = getRGB(src, 0, 0, width, height);
            int[] revPixels = new int[srcPixels.length];
            for (int i = 0; i < srcPixels.length; i++) {
                int alpha = (srcPixels[i] >> 24) & 0xff;
                alpha = 255 - alpha;
                revPixels[i] = (srcPixels[i] & 0x00ffffff) + (alpha << 24);
            }
            int[] shadow = new int[srcPixels.length];
            for (int i = 0; i < srcPixels.length; i++) {
                int alpha = (srcPixels[i] >> 24) & 0xff;
                if (inner) {
                    alpha = 255 - alpha;
                }
                shadow[i] = 0 + (Math.round(alpha * strength) << 24);
            }
            Color colorFirst = Color.black;
            Color colorAlpha = new Color(0, 0, 0, 0);
            shadow = moveRGB(width, height, shadow, moveX, moveY, inner ? colorFirst : colorAlpha);

            setRGB(retImg, 0, 0, width, height, shadow);

            retImg = blur(retImg, blurX, blurY, iterations, inner ? srcPixels : revPixels);//new BoxBlurFilter(blurX, blurY, iterations, inner ? srcPixels : revPixels).filter(ret);
            shadow = getRGB(retImg, 0, 0, width, height);

            for (int i = 0; i < shadow.length; i++) {
                int a = (shadow[i] >> 24) & 0xff;
                shadow[i] = gradientPixels[a];
            }

            for (int i = 0; i < shadow.length; i++) {
                int srcA = (srcPixels[i] >> 24) & 0xff;
                if (!inner) {
                    srcA = 255 - srcA;
                }
                int shadA = (shadow[i] >> 24) & 0xff;
                shadow[i] = (shadow[i] & 0x00ffffff) + (Math.min(srcA, shadA) << 24);
            }

            setRGB(retImg, 0, 0, width, height, shadow);
        }
        if (!knockout) {
            Graphics2D g = (Graphics2D) retImg.getGraphics();
            //g.setComposite(inner ? AlphaComposite.DstOver : AlphaComposite.SrcOver);
            g.setComposite(AlphaComposite.DstOver);
            g.drawImage(src.getBufferedImage(), 0, 0, null);
        }

        return retImg;
    }

    private static int[] getRGB(SerializableImage image, int x, int y, int width, int height) {
        int type = image.getType();
        if (type == SerializableImage.TYPE_INT_ARGB || type == SerializableImage.TYPE_INT_RGB) {
            return (int[]) image.getRaster().getDataElements(x, y, width, height, null);
        }
        return image.getRGB(x, y, width, height, null, 0, width);
    }

    private static void setRGB(SerializableImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == SerializableImage.TYPE_INT_ARGB || type == SerializableImage.TYPE_INT_RGB) {
            image.getRaster().setDataElements(x, y, width, height, pixels);
        } else {
            image.setRGB(x, y, width, height, pixels, 0, width);
        }
    }

    private static int[] moveRGB(int width, int height, int[] rgb, double deltaX, double deltaY, Color fill) {
        SerializableImage img = new SerializableImage(width, height, SerializableImage.TYPE_INT_ARGB);
        setRGB(img, 0, 0, width, height, rgb);
        SerializableImage retImg = new SerializableImage(width, height, SerializableImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) retImg.getGraphics();
        g.setPaint(fill);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setTransform(AffineTransform.getTranslateInstance(deltaX, deltaY));
        g.setComposite(AlphaComposite.Src);
        g.drawImage(img.getBufferedImage(), 0, 0, null);
        return getRGB(retImg, 0, 0, width, height);
    }

    public static SerializableImage convolution(SerializableImage src, float[] matrix, int w, int h) {
        SerializableImage dst = new SerializableImage(src.getWidth(), src.getHeight(), src.getType());
        BufferedImageOp op = new ConvolveOp(new Kernel(w, h, matrix), ConvolveOp.EDGE_ZERO_FILL, new RenderingHints(null));
        op.filter(src.getBufferedImage(), dst.getBufferedImage());
        return dst;
    }

    public static SerializableImage colorMatrix(SerializableImage src, float[][] matrix) {
        BandCombineOp changeColors = new BandCombineOp(matrix, new RenderingHints(null));
        Raster sourceRaster = src.getRaster();
        WritableRaster displayRaster = sourceRaster.createCompatibleWritableRaster();
        changeColors.filter(sourceRaster, displayRaster);
        return new SerializableImage(src.getColorModel(), displayRaster, true, null);
    }

    private static int cut(double val) {
        int i = (int) val;
        if (i < 0) {
            i = 0;
        }
        if (i > 255) {
            i = 255;
        }
        return i;
    }

    public static Color colorEffect(Color color,
            int redAddTerm, int greenAddTerm, int blueAddTerm, int alphaAddTerm,
            int redMultTerm, int greenMultTerm, int blueMultTerm, int alphaMultTerm) {
        int rgb = color.getRGB();
        int a = (rgb >> 24) & 0xff;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;
        r = Math.max(0, Math.min(((r * redMultTerm) / 255) + redAddTerm, 255));
        g = Math.max(0, Math.min(((g * greenMultTerm) / 255) + greenAddTerm, 255));
        b = Math.max(0, Math.min(((b * blueMultTerm) / 255) + blueAddTerm, 255));
        a = Math.max(0, Math.min(((a * alphaMultTerm) / 255) + alphaAddTerm, 255));
        return new Color(r, g, b, a);
    }

    public static SerializableImage colorEffect(SerializableImage src,
            int redAddTerm, int greenAddTerm, int blueAddTerm, int alphaAddTerm,
            int redMultTerm, int greenMultTerm, int blueMultTerm, int alphaMultTerm) {
        SerializableImage dst = new SerializableImage(src.getWidth(), src.getHeight(), src.getType());
        int rgb[] = getRGB(src, 0, 0, src.getWidth(), src.getHeight());
        for (int i = 0; i < rgb.length; i++) {
            int a = (rgb[i] >> 24) & 0xff;
            int r = (rgb[i] >> 16) & 0xff;
            int g = (rgb[i] >> 8) & 0xff;
            int b = (rgb[i]) & 0xff;
            r = Math.max(0, Math.min(((r * redMultTerm) / 256) + redAddTerm, 255));
            g = Math.max(0, Math.min(((g * greenMultTerm) / 256) + greenAddTerm, 255));
            b = Math.max(0, Math.min(((b * blueMultTerm) / 256) + blueAddTerm, 255));
            a = Math.max(0, Math.min(((a * alphaMultTerm) / 256) + alphaAddTerm, 255));
            rgb[i] = (a << 24) | (r << 16) | (g << 8) | (b);
        }
        setRGB(dst, 0, 0, src.getWidth(), src.getHeight(), rgb);
        return dst;
    }
}
