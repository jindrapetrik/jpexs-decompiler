/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License version 3 or later.
 */
package com.jpexs.decompiler.flash.types.filters;

import java.awt.CompositeContext;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BlendCompositeTest {

    @Test
    public void reusedContextMatchesFreshContextForChangingTileSizesAndRasterOffsets() {
        BlendComposite[] modes = {BlendComposite.Alpha, BlendComposite.Erase, BlendComposite.Invert,
            BlendComposite.Multiply, BlendComposite.Screen, BlendComposite.Darken, BlendComposite.Lighten,
            BlendComposite.Overlay, BlendComposite.HardLight, BlendComposite.Difference,
            BlendComposite.Add, BlendComposite.Subtract};
        Random random = new Random(41728);
        for (BlendComposite mode : modes) {
            BufferedImage source = new BufferedImage(101, 10, BufferedImage.TYPE_INT_ARGB_PRE);
            BufferedImage destination = new BufferedImage(101, 10, BufferedImage.TYPE_INT_ARGB_PRE);
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 101; x++) {
                    source.setRGB(x, y, random.nextInt());
                    destination.setRGB(x, y, random.nextInt());
                }
            }
            CompositeContext reused = mode.createContext(source.getColorModel(), destination.getColorModel(), null);
            for (int width : new int[]{1, 32, 7, 96, 2, 64}) {
                WritableRaster src = source.getRaster().createWritableChild(2, 3, width, 5, 0, 0, null);
                WritableRaster dst = destination.getRaster().createWritableChild(3, 2, width, 5, 0, 0, null);
                WritableRaster expected = dst.createCompatibleWritableRaster();
                WritableRaster actual = dst.createCompatibleWritableRaster();
                CompositeContext fresh = mode.createContext(source.getColorModel(), destination.getColorModel(), null);
                fresh.compose(src, dst, expected);
                reused.compose(src, dst, actual);
                fresh.dispose();
                Assert.assertEquals((int[]) actual.getDataElements(0, 0, width, 5, null),
                        (int[]) expected.getDataElements(0, 0, width, 5, null));
            }
            reused.dispose();
        }
    }
}
