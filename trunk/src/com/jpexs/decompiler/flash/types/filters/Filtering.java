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
import java.awt.image.BufferedImage;
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
        return new SerializableImage(blur(src.getBufferedImage(), hRadius, vRadius, iterations, null));
    }

    private static BufferedImage blur(BufferedImage src, int hRadius, int vRadius, int iterations, int[] mask) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage dst = new BufferedImage(width, height, src.getType());

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
        return new SerializableImage(gradientBevel(src.getBufferedImage(), new Color[]{
            shadowColor,
            new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), 0),
            new Color(highlightColor.getRed(), highlightColor.getGreen(), highlightColor.getBlue(), 0),
            highlightColor
        }, new float[]{0, 127f / 255f, 128f / 255f, 1}, blurX, blurY, strength, type, angle, distance, knockout, iterations));
    }

    public static SerializableImage gradientBevel(SerializableImage src, Color[] colors, float[] ratios, int blurX, int blurY, float strength, int type, float angle, float distance, boolean knockout, int iterations) {
        return new SerializableImage(gradientBevel(src.getBufferedImage(), colors, ratios, blurX, blurY, strength, type, angle, distance, knockout, iterations));
    }

    private static BufferedImage gradientBevel(BufferedImage src, Color[] colors, float[] ratios, int blurX, int blurY, float strength, int type, float angle, float distance, boolean knockout, int iterations) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage retImg = new BufferedImage(width,height,src.getType());
        int srcPixels[] = getRGB(src, 0, 0, width, height);

        int revPixels[] = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            revPixels[i] = (srcPixels[i]&0xffffff)+((255-((srcPixels[i]>>24) & 0xff))<<24);
        }  

        BufferedImage gradient = new BufferedImage(512, 1,src.getType());            
        Graphics2D gg = gradient.createGraphics();
        
        
        Point p1 = new Point(0, 0);
        Point p2 = new Point(511, 0);
        gg.setPaint(new LinearGradientPaint(p1,p2, ratios, colors));
        gg.fill(new Rectangle(512, 1));
        int[] gradientPixels = getRGB(gradient, 0, 0, gradient.getWidth(), gradient.getHeight());
        
        BufferedImage shadowInner = null;
        BufferedImage hilightInner = null;
        if(type!=OUTER){
            BufferedImage hilightIm = dropShadow(src,0, 0, angle, distance, Color.red, true, iterations, strength, true);//new DropShadowFilter(blurX, blurY, strength, inner ? highlightColor : shadowColor, angle, distance, inner, true, iterations).filter(src
            BufferedImage shadowIm = dropShadow(src, 0, 0, angle + 180, distance, Color.blue, true, iterations, strength, true); //new DropShadowFilter(blurX, blurY, strength, inner ? shadowColor : highlightColor, angle + 180, distance, inner, true, iterations).filter(src);            
            BufferedImage h2 = new BufferedImage(width, height, src.getType());
            BufferedImage s2 = new BufferedImage(width, height, src.getType());
            Graphics2D hc=h2.createGraphics();
            Graphics2D sc=s2.createGraphics();
            hc.drawImage(hilightIm,0,0,null);
            hc.setComposite(AlphaComposite.DstOut);
            hc.drawImage(shadowIm,0,0,null);

            sc.drawImage(shadowIm,0,0,null);
            sc.setComposite(AlphaComposite.DstOut);
            sc.drawImage(hilightIm,0,0,null);
            shadowInner = s2;
            hilightInner = h2;     
        }
        BufferedImage shadowOuter = null;
        BufferedImage hilightOuter = null;
        if(type!=INNER){
            BufferedImage hilightIm = dropShadow(src,0, 0, angle + 180, distance, Color.red, false, iterations, strength, true);//new DropShadowFilter(blurX, blurY, strength, inner ? highlightColor : shadowColor, angle, distance, inner, true, iterations).filter(src
            BufferedImage shadowIm = dropShadow(src, 0, 0, angle, distance, Color.blue, false, iterations, strength, true); //new DropShadowFilter(blurX, blurY, strength, inner ? shadowColor : highlightColor, angle + 180, distance, inner, true, iterations).filter(src);            
            BufferedImage h2 = new BufferedImage(width, height, src.getType());
            BufferedImage s2 = new BufferedImage(width, height, src.getType());
            Graphics2D hc=h2.createGraphics();
            Graphics2D sc=s2.createGraphics();
            hc.drawImage(hilightIm,0,0,null);
            hc.setComposite(AlphaComposite.DstOut);
            hc.drawImage(shadowIm,0,0,null);

            sc.drawImage(shadowIm,0,0,null);
            sc.setComposite(AlphaComposite.DstOut);
            sc.drawImage(hilightIm,0,0,null);
            shadowOuter = s2;
            hilightOuter = h2;
        }

        BufferedImage hilightIm = null;
        BufferedImage shadowIm = null;
        switch(type)
        {               
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
              Graphics2D hc=hilightIm.createGraphics();
              hc.setComposite(AlphaComposite.SrcOver);
              hc.drawImage(hilightOuter,0,0,null);
              Graphics2D sc=shadowIm.createGraphics();
              sc.setComposite(AlphaComposite.SrcOver);
              sc.drawImage(shadowOuter,0,0,null);
              break;
        }

        int mask[] = null;
        if(type == INNER){
            mask = srcPixels;
        }
        if(type == OUTER){
            mask = revPixels;
        }                                   

        Graphics2D retc = retImg.createGraphics();
        retc.setColor(Color.black);
        retc.fillRect(0,0,width,height);
        retc.setComposite(AlphaComposite.SrcOver);
        retc.drawImage(shadowIm,0,0,null);
        retc.drawImage(hilightIm,0,0,null);

        /*if(true)
        return retImg;
        */
        retImg = blur(retImg,blurX,blurY,iterations,mask);
        int ret[] = getRGB(retImg, 0, 0, width, height);

        for (int i = 0; i < srcPixels.length; i++) {
            int ah = (int) (new Color(ret[i]).getRed()* strength);
            int as = (int) (new Color(ret[i]).getBlue() * strength);
            int ra = cut(ah-as,-255,255);
            ret[i] = gradientPixels[255 + ra];
        }
        setRGB(retImg, 0, 0, width, height, ret);


        if (!knockout) {
            Graphics2D g = retImg.createGraphics();
            g.setComposite(AlphaComposite.DstOver);
            g.drawImage(src, 0, 0, null);
        }
        return retImg;
    }

    public static SerializableImage glow(SerializableImage src, int blurX, int blurY, float strength, Color color, boolean inner, boolean knockout, int iterations) {
        return new SerializableImage(dropShadow(src.getBufferedImage(), blurX, blurY, 45, 0, color, inner, iterations, strength, knockout));
    }

    public static SerializableImage dropShadow(SerializableImage src, int blurX, int blurY, float angle, double distance, Color color, boolean inner, int iterations, float strength, boolean knockout) {
        return new SerializableImage(dropShadow(src.getBufferedImage(), blurX, blurY, angle, distance, color, inner, iterations, strength, knockout));
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

    private static BufferedImage dropShadow(BufferedImage src, int blurX, int blurY, float angle, double distance, Color color, boolean inner, int iterations, float strength, boolean knockout) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] srcPixels = getRGB(src, 0, 0, width, height);
        int shadow[] = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            int alpha = (srcPixels[i]>>24) & 0xff;
            if (inner) {
                alpha = 255 - alpha;
            }
            shadow[i] = new Color(color.getRed(), color.getGreen(), color.getBlue(), cut(color.getAlpha() * alpha * strength)).getRGB();
        }
        Color colorFirst = Color.BLACK;
        Color colorAlpha = new Color(0, 0, 0, 0);
        double angleRad = angle / 180 * Math.PI;
        double moveX = (distance * Math.cos(angleRad));
        double moveY = (distance * Math.sin(angleRad));
        shadow = moveRGB(width, height, shadow, moveX, moveY, inner ? colorFirst : colorAlpha);

        BufferedImage retCanvas = new BufferedImage(width, height, src.getType());
        setRGB(retCanvas, 0, 0, width, height, shadow);
        if (blurX > 0 || blurY > 0) {
            retCanvas = blur(retCanvas, blurX, blurY, iterations, null);
        }
        shadow = getRGB(retCanvas, 0, 0, width, height);

        for (int i = 0; i < shadow.length; i++) {
            int mask = (srcPixels[i]>>24) & 0xff;
            if (!inner) {
                mask = 255 - mask;
            }
            shadow[i] = shadow[i] & 0xffffff + ((mask * ((shadow[i]>>24) & 0xff) / 255)<<24);
        }
        setRGB(retCanvas, 0, 0, width, height, shadow);

        if (!knockout) {
            Graphics2D g = retCanvas.createGraphics();
            g.setComposite(AlphaComposite.DstOver);
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
        BufferedImage retCanvas = new BufferedImage(width, height, src.getType());
        Graphics2D retImg = retCanvas.createGraphics();

        BufferedImage gradCanvas = new BufferedImage(256,1,src.getType());

        Graphics2D gg = gradCanvas.createGraphics();
        
        
        Point p1 = new Point(0, 0);
        Point p2 = new Point(255, 0);
        gg.setPaint(new LinearGradientPaint(p1,p2, ratios, colors));
        gg.fill(new Rectangle(256, 1));
        int[] gradientPixels = getRGB(gradCanvas, 0, 0, gradCanvas.getWidth(), gradCanvas.getHeight());
        
        double angleRad = angle / 180 * Math.PI;
        double moveX = (distance * Math.cos(angleRad));
        double moveY = (distance * Math.sin(angleRad));            
        int srcPixels[] = getRGB(src, 0, 0, width, height);
        int revPixels[] = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            revPixels[i] = (srcPixels[i]&0xffffff)+((255-((srcPixels[i]>>24) & 0xff))<<24);
        }            
        int shadow[] = new int[srcPixels.length];
        for (int i = 0; i < srcPixels.length; i++) {
            shadow[i] = 0+((cut(strength*((srcPixels[i]>>24) & 0xff)))<<24);
        }
        Color colorAlpha = new Color(0,0,0,0);
        shadow = moveRGB(width, height, shadow, moveX, moveY, colorAlpha);

        setRGB(retCanvas, 0, 0, width, height, shadow);

        int mask[] = null;
        if(type == INNER){
            mask = srcPixels;
        }
        if(type == OUTER){
            mask = revPixels;
        }


        retCanvas = blur(retCanvas, blurX, blurY, iterations,mask);
        shadow = getRGB(retCanvas, 0, 0, width, height);

        if(mask!=null){
           for (int i = 0; i < mask.length; i++) {
               int m = (mask[i]>>24);
              if(m == 0){
                 shadow[i] = 0;
              }
           }
        }





        for (int i = 0; i < shadow.length; i++) {
           int a = (shadow[i]>>24) & 0xff;
           shadow[i] = gradientPixels[a];              
        }

        setRGB(retCanvas,0,0,width,height,shadow);

        if (!knockout) {
           retImg = retCanvas.createGraphics();
           retImg.setComposite(AlphaComposite.DstOver);
           retImg.drawImage(src,0,0,null);
        }

        return retCanvas;
    }

    private static int[] getRGB(BufferedImage image, int x, int y, int width, int height) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            return (int[]) image.getRaster().getDataElements(x, y, width, height, null);
        }
        return image.getRGB(x, y, width, height, null, 0, width);
    }

    private static void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
            image.getRaster().setDataElements(x, y, width, height, pixels);
        } else {
            image.setRGB(x, y, width, height, pixels, 0, width);
        }
    }

    private static int[] moveRGB(int width, int height, int[] rgb, double deltaX, double deltaY, Color fill) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setRGB(img, 0, 0, width, height, rgb);
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
        return getRGB(retImg, 0, 0, width, height);
    }

    public static SerializableImage convolution(SerializableImage src, float[] matrix, int w, int h) {
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        BufferedImageOp op = new ConvolveOp(new Kernel(w, h, matrix), ConvolveOp.EDGE_ZERO_FILL, new RenderingHints(null));
        op.filter(src.getBufferedImage(), dst);
        return new SerializableImage(dst);
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
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        int rgb[] = getRGB(src.getBufferedImage(), 0, 0, src.getWidth(), src.getHeight());
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
        return new SerializableImage(dst);
    }
}
