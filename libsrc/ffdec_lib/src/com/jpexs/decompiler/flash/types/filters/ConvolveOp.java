/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ImagingOpException;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;

/**
 * Convolution filter.
 */
public class ConvolveOp implements BufferedImageOp, RasterOp {

    private final Kernel kernel;
    private final RenderingHints hints;
    private final boolean preserveAlpha;
    private final float bias;
    private final Color defaultColor;
    private final boolean clamp;
    private final float divisor;
    private final int srcX;
    private final int srcY;
    private final int srcWidth;
    private final int srcHeight;

    public ConvolveOp(Kernel kernel,
            RenderingHints hints,
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
        this.kernel = kernel;
        this.hints = hints;
        this.bias = bias;
        this.defaultColor = defaultColor;
        this.clamp = clamp;
        this.preserveAlpha = preserveAlpha;
        this.divisor = divisor;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcWidth = srcWidth;
        this.srcHeight = srcHeight;
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src,
            ColorModel dstCM) {
        if (dstCM != null) {
            return new BufferedImage(dstCM,
                    src.getRaster().createCompatibleWritableRaster(),
                    src.isAlphaPremultiplied(), null);
        }

        return new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
    }

    @Override
    public final RenderingHints getRenderingHints() {
        return hints;
    }

    public final Kernel getKernel() {
        return (Kernel) kernel.clone();
    }

    @Override
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (src == dst) {
            throw new IllegalArgumentException("Source and destination images "
                    + "cannot be the same.");
        }

        if (dst == null) {
            dst = createCompatibleDestImage(src, src.getColorModel());
        }

        BufferedImage src1 = src;
        BufferedImage dst1 = dst;
        if (src1.getColorModel().getColorSpace().getType() != dst.getColorModel().getColorSpace().getType()) {
            dst1 = createCompatibleDestImage(src, src.getColorModel());
        }

        filter(src1.getRaster(), dst1.getRaster());

        if (dst1 != dst) {
            new ColorConvertOp(hints).filter(dst1, dst);
        }

        return dst;
    }

    @Override
    public final WritableRaster filter(Raster src, WritableRaster dest) {
        if (src == dest) {
            throw new IllegalArgumentException("src == dest is not allowed.");
        }
        if (dest == null) {
            dest = createCompatibleDestRaster(src);
        } else if (src.getNumBands() != dest.getNumBands()) {
            throw new ImagingOpException("src and dest have different band counts.");
        }

        int kWidth = kernel.getWidth();
        int kHeight = kernel.getHeight();
        int left = kernel.getXOrigin();
        int top = kernel.getYOrigin();

        int[] maxValue = src.getSampleModel().getSampleSize();
        for (int i = 0; i < maxValue.length; i++) {
            maxValue[i] = (int) Math.pow(2, maxValue[i]) - 1;
        }

        float[] kvals = kernel.getKernelData(null);

        for (int x = srcX; x < srcX + srcWidth; x++) {
            for (int y = srcY; y < srcY + srcHeight; y++) {
                float a;
                if (preserveAlpha) {
                    boolean outSide = false;
                    int fsrcX = x;
                    int fsrcY = y;
                    if (fsrcX < srcX) {
                        fsrcX = srcX;
                        outSide = true;
                    }
                    if (fsrcY < srcY) {
                        fsrcY = srcY;
                        outSide = true;
                    }
                    if (fsrcX >= srcX + srcWidth) {
                        fsrcX = srcX + srcWidth - 1;
                        outSide = true;
                    }
                    if (fsrcY >= srcY + srcHeight) {
                        fsrcY = srcY + srcHeight - 1;
                        outSide = true;
                    }
                    if (outSide) {
                        if (clamp) {
                            a = src.getSample(fsrcX, fsrcY, 3);
                        } else {
                            a = defaultColor.getAlpha();
                        }
                    } else {
                        a = src.getSample(x, y, 3);
                    }
                    dest.setSample(x, y, 3, a);
                } else {
                    a = calculateBand(src, dest, maxValue, kvals, kWidth, kHeight, left, top, x, y, 3, 255f, false);
                }

                for (int b = 0; b < 3; b++) {
                    calculateBand(src, dest, maxValue, kvals, kWidth, kHeight, left, top, x, y, b, a, true);
                }
            }
        }
        return dest;
    }

    private float calculateBand(
            Raster src,
            WritableRaster dest,
            int[] maxValue,
            float[] kvals,
            int kWidth,
            int kHeight,
            int left,
            int top,
            int x,
            int y,
            int b,
            float alpha,
            boolean multiply
    ) {
        float nv = 0;
        for (int i = 0; i < kHeight; i++) {
            for (int j = 0; j < kWidth; j++) {
                int nSrcX = x - left + j;
                int nSrcY = y - top + i;
                boolean outSide = false;
                if (nSrcX < srcX) {
                    nSrcX = srcX;
                    outSide = true;
                }
                if (nSrcX >= srcX + srcWidth) {
                    nSrcX = srcX + srcWidth - 1;
                    outSide = true;
                }
                if (nSrcY < srcY) {
                    nSrcY = srcY;
                    outSide = true;
                }
                if (nSrcY >= srcY + srcHeight) {
                    nSrcY = srcY + srcHeight - 1;
                    outSide = true;
                }

                float v = 0;
                if (outSide && !clamp) {
                    switch (b) {
                        case 0:
                            v = defaultColor.getRed() * maxValue[0] / 255f;
                            break;
                        case 1:
                            v = defaultColor.getGreen() * maxValue[1] / 255f;
                            break;
                        case 2:
                            v = defaultColor.getBlue() * maxValue[2] / 255f;
                            break;
                        case 3:
                            v = defaultColor.getAlpha() * maxValue[3] / 255f;
                            break;
                    }
                } else {

                    int srcRealX = nSrcX;
                    int srcRealY = nSrcY;

                    v = src.getSample(srcRealX, srcRealY, b);

                    if (multiply) {
                        float sa = src.getSample(srcRealX, srcRealY, 3);
                        if (sa == 0f) {
                            v = 0;
                        } else {
                            v = v * 255f / sa;
                        }
                    }
                }

                nv += v * kvals[i * kWidth + j];
            }
        }
        nv /= divisor;
        nv += bias;

        if (nv > maxValue[b]) {
            nv = maxValue[b];
        } else if (nv < 0) {
            nv = 0;
        }
        if (multiply) {
            nv = nv * alpha / 255f;
        }
        nv = Math.round(nv);
        int destX = x;
        int destY = y;

        dest.setSample(destX, destY, b, nv);
        return nv;
    }

    @Override
    public WritableRaster createCompatibleDestRaster(Raster src) {
        return src.createCompatibleWritableRaster();
    }

    @Override
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return src.getRaster().getBounds();
    }

    @Override
    public final Rectangle2D getBounds2D(Raster src) {
        return src.getBounds();
    }

    @Override
    public final Point2D getPoint2D(Point2D src, Point2D dst) {
        if (dst == null) {
            return (Point2D) src.clone();
        }
        dst.setLocation(src);
        return dst;
    }
}
