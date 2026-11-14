/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License version 3 or later.
 */
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.helpers.SerializableImage;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.Random;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FilteringTest {

    // Deliberately simple O(pixels * radius) oracle, independent of the
    // optimized sliding window. Sum straight colors weighted by integer alpha.
    private int[] pass(int[] input, int w, int h, int radius, boolean horizontal) {
        radius = Math.max(1, radius);
        int[] result = new int[input.length];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                long[] sums = new long[4];
                int count = 0;
                for (int d = -radius / 2; d < radius - radius / 2; d++) {
                    int sx = horizontal ? x + d : x;
                    int sy = horizontal ? y : y + d;
                    if (sx >= 0 && sx < w && sy >= 0 && sy < h) {
                        int color = input[sy * w + sx];
                        int a = color >>> 24;
                        sums[3] += a;
                        for (int c = 0; c < 3; c++) {
                            sums[c] += ((color >>> (c * 8)) & 255) * a;
                        }
                        count++;
                    }
                }
                int alpha = (int) (sums[3] / count);
                int color = alpha << 24;
                for (int c = 0; c < 3; c++) {
                    color |= (int) Math.min(255, sums[c] / ((long) count * (alpha == 0 ? 255 : alpha))) << (c * 8);
                }
                result[y * w + x] = color;
            }
        }
        return result;
    }

    @Test
    public void blurMatchesWeightedReferenceAtEdgesAndAcrossIterations() {
        Random random = new Random(982137);
        for (int trial = 0; trial < 160; trial++) {
            int w = 1 + random.nextInt(24);
            int h = 1 + random.nextInt(19);
            int rx = random.nextInt(40);
            int ry = random.nextInt(40);
            int iterations = random.nextInt(5);
            int[] pixels = new int[w * h];
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = random.nextInt();
            }
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, w, h, pixels, 0, w);
            int[] expected = pixels.clone();
            for (int i = 0; i < iterations; i++) {
                expected = pass(pass(expected, w, h, rx, true), w, h, ry, false);
            }
            BufferedImage actual = Filtering.blur(new SerializableImage(image), rx, ry, iterations).getBufferedImage();
            Assert.assertEquals(actual.getRGB(0, 0, w, h, null, 0, w), expected, "trial " + trial);
            Assert.assertEquals(image.getRGB(0, 0, w, h, null, 0, w), pixels, "source modified");
        }
    }

    @Test
    public void blurRespectsWorkLimit() {
        int old = Configuration.boxBlurPixelsLimit.get();
        try {
            Configuration.boxBlurPixelsLimit.set(0);
            BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, 0x80ff0000);
            image.setRGB(1, 0, 0xff00ff00);
            BufferedImage actual = Filtering.blur(new SerializableImage(image), 10, 10, 3).getBufferedImage();
            Assert.assertEquals(actual.getRGB(0, 0, 2, 1, null, 0, 2), image.getRGB(0, 0, 2, 1, null, 0, 2));
        } finally {
            Configuration.boxBlurPixelsLimit.set(old);
        }
    }

    @Test
    public void premultipliedConversionsMatchBufferedImageForEveryAlpha() throws Exception {
        int w = 256;
        int h = 13;
        Random random = new Random(8137);
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = (x << 24) | (random.nextInt() & 0xffffff);
            }
        }
        Method getRGB = Filtering.class.getDeclaredMethod("getRGB", BufferedImage.class);
        getRGB.setAccessible(true);
        for (boolean subimage : new boolean[]{false, true}) {
            BufferedImage expectedParent = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB_PRE);
            BufferedImage actualParent = new BufferedImage(w + 4, h + 4, BufferedImage.TYPE_INT_ARGB_PRE);
            BufferedImage expected = subimage ? expectedParent.getSubimage(2, 2, w, h)
                    : new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
            BufferedImage actual = subimage ? actualParent.getSubimage(2, 2, w, h)
                    : new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
            expected.setRGB(0, 0, w, h, pixels, 0, w);
            Filtering.setRGB(actual, w, h, pixels);
            Assert.assertEquals(actual.getRaster().getDataElements(0, 0, w, h, null),
                    expected.getRaster().getDataElements(0, 0, w, h, null));
            Assert.assertEquals((int[]) getRGB.invoke(null, actual), expected.getRGB(0, 0, w, h, null, 0, w));
            Assert.assertEquals(actualParent.getRGB(0, 0), 0, "subimage write escaped bounds");
        }
    }
}
