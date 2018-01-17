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
package com.jpexs.decompiler.flash.types.filters;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.WritableRaster;

/**
 *
 * @author JPEXS
 */
public final class BlendComposite implements Composite {

    public enum BlendingMode {

        LAYER, //TODO!
        DARKEN,
        MULTIPLY,
        LIGHTEN,
        SCREEN,
        OVERLAY,
        HARD_LIGHT,
        ADD,
        SUBTRACT,
        DIFFERENCE,
        INVERT,
        ALPHA,
        ERASE
    }

    public static final BlendComposite Alpha = new BlendComposite(BlendingMode.ALPHA);

    public static final BlendComposite Erase = new BlendComposite(BlendingMode.ERASE);

    public static final BlendComposite Invert = new BlendComposite(BlendingMode.INVERT);

    public static final BlendComposite Multiply = new BlendComposite(BlendingMode.MULTIPLY);

    public static final BlendComposite Screen = new BlendComposite(BlendingMode.SCREEN);

    public static final BlendComposite Darken = new BlendComposite(BlendingMode.DARKEN);

    public static final BlendComposite Lighten = new BlendComposite(BlendingMode.LIGHTEN);

    public static final BlendComposite Overlay = new BlendComposite(BlendingMode.OVERLAY);

    public static final BlendComposite HardLight = new BlendComposite(BlendingMode.HARD_LIGHT);

    public static final BlendComposite Difference = new BlendComposite(BlendingMode.DIFFERENCE);

    public static final BlendComposite Add = new BlendComposite(BlendingMode.ADD);

    public static final BlendComposite Subtract = new BlendComposite(BlendingMode.SUBTRACT);

    private final float alpha;

    private final BlendingMode mode;

    private BlendComposite(BlendingMode mode) {
        this(mode, 1.0f);
    }

    private BlendComposite(BlendingMode mode, float alpha) {
        this.mode = mode;

        if (alpha < 0.0f || alpha > 1.0f) {
            throw new IllegalArgumentException(
                    "alpha must be comprised between 0.0f and 1.0f");
        }
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    /**
     * <p>
     * Returns the blending mode of this composite.</p>
     *
     * @return the blending mode used by this object
     */
    public BlendingMode getMode() {
        return mode;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Float.floatToIntBits(alpha) * 31 + mode.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlendComposite)) {
            return false;
        }

        BlendComposite bc = (BlendComposite) obj;
        return mode == bc.mode && alpha == bc.alpha;
    }

    private static boolean checkComponentsOrder(ColorModel cm) {
        if (cm instanceof DirectColorModel
                && cm.getTransferType() == DataBuffer.TYPE_INT) {
            DirectColorModel directCM = (DirectColorModel) cm;

            return directCM.getRedMask() == 0x00FF0000
                    && directCM.getGreenMask() == 0x0000FF00
                    && directCM.getBlueMask() == 0x000000FF
                    && (directCM.getNumComponents() != 4
                    || directCM.getAlphaMask() == 0xFF000000);
        }

        return false;
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel,
            ColorModel dstColorModel,
            RenderingHints hints) {
        if (!checkComponentsOrder(srcColorModel)
                || !checkComponentsOrder(dstColorModel)) {
            throw new RasterFormatException("Incompatible color models");
        }

        return new BlendingContext(this);
    }

    private static final class BlendingContext implements CompositeContext {

        private final Blender blender;

        private final BlendComposite composite;

        private BlendingContext(BlendComposite composite) {
            this.composite = composite;
            this.blender = Blender.getBlenderFor(composite);
        }

        @Override
        public void dispose() {
        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());

            float alpha = composite.getAlpha();

            int[] result = new int[4];
            int[] srcPixel = new int[4];
            int[] dstPixel = new int[4];
            int[] retPixel = new int[4];
            int[] srcPixels = new int[width];
            int[] dstPixels = new int[width];

            for (int y = 0; y < height; y++) {
                src.getDataElements(0, y, width, 1, srcPixels);
                dstIn.getDataElements(0, y, width, 1, dstPixels);
                for (int x = 0; x < width; x++) {
                    int pixel = srcPixels[x];
                    srcPixel[0] = (pixel >> 16) & 0xFF;
                    srcPixel[1] = (pixel >> 8) & 0xFF;
                    srcPixel[2] = (pixel) & 0xFF;
                    srcPixel[3] = (pixel >> 24) & 0xFF;

                    pixel = dstPixels[x];
                    dstPixel[0] = (pixel >> 16) & 0xFF;
                    dstPixel[1] = (pixel >> 8) & 0xFF;
                    dstPixel[2] = (pixel) & 0xFF;
                    dstPixel[3] = (pixel >> 24) & 0xFF;

                    blender.blend(srcPixel, dstPixel, result);

                    retPixel[0] = ((int) (dstPixel[0] + (result[0] - dstPixel[0]) * alpha) & 0xFF);
                    retPixel[1] = ((int) (dstPixel[1] + (result[1] - dstPixel[1]) * alpha) & 0xFF);
                    retPixel[2] = (int) (dstPixel[2] + (result[2] - dstPixel[2]) * alpha) & 0xFF;
                    retPixel[3] = ((int) (dstPixel[3] + (result[3] - dstPixel[3]) * alpha) & 0xFF);

                    float af = ((float) srcPixel[3]) / 255f;
                    retPixel[0] = (int) ((1f - af) * dstPixel[0] + af * retPixel[0]);
                    retPixel[1] = (int) ((1f - af) * dstPixel[1] + af * retPixel[1]);
                    retPixel[2] = (int) ((1f - af) * dstPixel[2] + af * retPixel[2]);
                    retPixel[3] = (int) ((1f - af) * dstPixel[3] + af * retPixel[3]);

                    dstPixels[x] = (retPixel[3] << 24)
                            | retPixel[0] << 16
                            | retPixel[1] << 8
                            | retPixel[2];
                }
                dstOut.setDataElements(0, y, width, 1, dstPixels);
            }
        }
    }

    private static abstract class Blender {

        public abstract void blend(int[] src, int[] dst, int[] result);

        public static Blender getBlenderFor(BlendComposite composite) {
            switch (composite.getMode()) {
                case ADD:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = Math.min(255, src[0] + dst[0]);
                            result[1] = Math.min(255, src[1] + dst[1]);
                            result[2] = Math.min(255, src[2] + dst[2]);
                            result[3] = Math.min(255, src[3] + dst[3]);
                        }
                    };
                case INVERT:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = 255 - dst[0];
                            result[1] = 255 - dst[1];
                            result[2] = 255 - dst[2];
                            result[3] = src[3];
                        }
                    };
                case ALPHA:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = src[0];
                            result[1] = src[1];
                            result[2] = src[2];
                            result[3] = dst[3]; //?
                        }
                    };
                case ERASE:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = src[0];
                            result[1] = src[1];
                            result[2] = src[2];
                            result[3] = 255 - dst[3]; //?
                        }
                    };

                case DARKEN:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = Math.min(src[0], dst[0]);
                            result[1] = Math.min(src[1], dst[1]);
                            result[2] = Math.min(src[2], dst[2]);
                            result[3] = Math.min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255);
                        }
                    };
                case DIFFERENCE:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = Math.abs(dst[0] - src[0]);
                            result[1] = Math.abs(dst[1] - src[1]);
                            result[2] = Math.abs(dst[2] - src[2]);
                            result[3] = Math.min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255);
                        }
                    };

                case HARD_LIGHT:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = src[0] < 128 ? dst[0] * src[0] >> 7
                                    : 255 - ((255 - src[0]) * (255 - dst[0]) >> 7);
                            result[1] = src[1] < 128 ? dst[1] * src[1] >> 7
                                    : 255 - ((255 - src[1]) * (255 - dst[1]) >> 7);
                            result[2] = src[2] < 128 ? dst[2] * src[2] >> 7
                                    : 255 - ((255 - src[2]) * (255 - dst[2]) >> 7);
                            result[3] = Math.min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255);
                        }
                    };

                case LIGHTEN:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = Math.max(src[0], dst[0]);
                            result[1] = Math.max(src[1], dst[1]);
                            result[2] = Math.max(src[2], dst[2]);
                            result[3] = Math.min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255);
                        }
                    };
                case MULTIPLY:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = (src[0] * dst[0]) >> 8;
                            result[1] = (src[1] * dst[1]) >> 8;
                            result[2] = (src[2] * dst[2]) >> 8;
                            result[3] = Math.min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255);
                        }
                    };

                case OVERLAY:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = dst[0] < 128 ? dst[0] * src[0] >> 7
                                    : 255 - ((255 - dst[0]) * (255 - src[0]) >> 7);
                            result[1] = dst[1] < 128 ? dst[1] * src[1] >> 7
                                    : 255 - ((255 - dst[1]) * (255 - src[1]) >> 7);
                            result[2] = dst[2] < 128 ? dst[2] * src[2] >> 7
                                    : 255 - ((255 - dst[2]) * (255 - src[2]) >> 7);
                            result[3] = Math.min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255);
                        }
                    };
                case SCREEN:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = 255 - ((255 - src[0]) * (255 - dst[0]) >> 8);
                            result[1] = 255 - ((255 - src[1]) * (255 - dst[1]) >> 8);
                            result[2] = 255 - ((255 - src[2]) * (255 - dst[2]) >> 8);
                            result[3] = Math.min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255);
                        }
                    };

                case SUBTRACT:
                    return new Blender() {
                        @Override
                        public void blend(int[] src, int[] dst, int[] result) {
                            result[0] = Math.max(0, src[0] + dst[0] - 256);
                            result[1] = Math.max(0, src[1] + dst[1] - 256);
                            result[2] = Math.max(0, src[2] + dst[2] - 256);
                            result[3] = Math.min(255, src[3] + dst[3] - (src[3] * dst[3]) / 255);
                        }
                    };
            }
            throw new IllegalArgumentException("Blender not implemented for "
                    + composite.getMode().name());
        }
    }
}
