/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ImagePanelCursorTest {

    @Test
    public void shapeToolHotSpotsStayOnTheirVisibleCrossAtDifferentNativeSizes() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel custom cursors require a graphics environment");
        }
        for (String name : new String[]{"paint", "ink", "eyedropper", "lasso"}) {
            BufferedImage source = ImageIO.read(ImagePanel.class.getResource("/com/jpexs/decompiler/flash/gui/graphics/cursors/" + name + ".png"));
            for (int size : new int[]{32, 48, 64, 96}) {
                BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = scaled.createGraphics();
                graphics.drawImage(source, 0, 0, size, size, null);
                graphics.dispose();
                // Locate the cross independently from the supplied hotspot:
                // its center has the longest black row and column at the left
                // bottom of the actual cursor bitmap.
                int leftWidth = size * 9 / source.getWidth();
                int bottomStart = size * 23 / source.getHeight();
                int[] rows = new int[size];
                int[] columns = new int[leftWidth];
                for (int y = bottomStart; y < size; y++) {
                    for (int x = 0; x < leftWidth; x++) {
                        if (scaled.getRGB(x, y) == 0xff000000) {
                            rows[y]++;
                            columns[x]++;
                        }
                    }
                }
                int crossX = 0;
                int crossY = bottomStart;
                for (int x = 1; x < columns.length; x++) {
                    if (columns[x] > columns[crossX]) {
                        crossX = x;
                    }
                }
                for (int y = bottomStart + 1; y < size; y++) {
                    if (rows[y] > rows[crossY]) {
                        crossY = y;
                    }
                }
                assertTrue(rows[crossY] > 0 && columns[crossX] > 0);
                Point actual = ImagePanel.scaleCursorHotSpot(new Point(4, 27), source.getWidth(), source.getHeight(), new Dimension(size, size));
                assertEquals(actual, new Point(crossX, crossY), name + " hotspot at native size " + size);
            }
        }
    }
}
