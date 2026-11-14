/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.SerializableImage;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BitmapPixelHintingTest {

    @DataProvider
    public Object[][] strokeWidths() {
        return new Object[][]{
            {20, 1.0, 1},
            {20, 0.25, 1},
            {0, 0.25, 1},
            {30, 1.0, 1},
            {38, 1.0, 2},
            {28, 1.5, 2},
            {60, 1.0, 3}
        };
    }

    @Test(dataProvider = "strokeWidths")
    public void alignsStrokeEdgesToOutputPixels(int width, double zoom, int expectedWidth) {
        for (int aaScale : new int[]{1, 4}) {
            for (boolean vertical : new boolean[]{false, true}) {
                BufferedImage image = render(width, zoom, aaScale, vertical, true, 0.25);
                int first = 8 - expectedWidth / 2;
                assertCoverage(image, vertical, first, expectedWidth);
            }
        }
    }

    @Test
    public void alignsIntegralWidthEvenWithoutHinting() {
        for (int aaScale : new int[]{1, 4}) {
            for (boolean vertical : new boolean[]{false, true}) {
                for (boolean hinting : new boolean[]{false, true}) {
                    assertCoverage(render(20, 1, aaScale, vertical, hinting, 0), vertical, 8, 1);
                    assertCoverage(render(20, 1, aaScale, vertical, hinting, 0.85), vertical, 8, 1);
                    assertCoverage(render(20, 1, aaScale, vertical, hinting, 0.90), vertical, 9, 1);
                    assertCoverage(render(40, 1, aaScale, vertical, hinting, 0.35), vertical, 7, 2);
                    assertCoverage(render(40, 1, aaScale, vertical, hinting, 0.40), vertical, 8, 2);
                }
                assertCoverage(render(0, 0.25, aaScale, vertical, false, 0), vertical, 8, 1);
            }
        }
    }

    @Test
    public void retainsSubpixelCoverageForFractionalWidthWithoutHinting() {
        for (int aaScale : new int[]{1, 4}) {
            BufferedImage image = render(30, 1, aaScale, false, false, 0.25);
            Assert.assertEquals(sampleAlpha(image, false, 7), aaScale == 1 ? 127 : 128);
            Assert.assertEquals(sampleAlpha(image, false, 8), 255);
            Assert.assertEquals(sampleAlpha(image, false, 9), 0);
        }
    }

    @Test
    public void preservesDifferentCapsOnPixelHintedStrokes() {
        for (int aaScale : new int[]{1, 4}) {
            // The 8 px wide stroke runs from (8, 8) to (18, 8).
            BufferedImage image = render(640, 0.25, aaScale, false, true, 0,
                    LINESTYLE2.SQUARE_CAP, LINESTYLE2.ROUND_CAP);
            Assert.assertEquals(image.getRGB(4, 4) >>> 24, 255);
            Assert.assertEquals(image.getRGB(20, 8) >>> 24, 255);
            Assert.assertEquals(image.getRGB(21, 4) >>> 24, 0);

            image = render(640, 0.25, aaScale, false, true, 0,
                    LINESTYLE2.NO_CAP, LINESTYLE2.SQUARE_CAP);
            Assert.assertEquals(image.getRGB(7, 8) >>> 24, 0);
            Assert.assertEquals(image.getRGB(21, 4) >>> 24, 255);
        }
    }

    private static void assertCoverage(BufferedImage image, boolean vertical, int first, int width) {
        for (int i = first - 1; i <= first + width; i++) {
            Assert.assertEquals(sampleAlpha(image, vertical, i), i >= first && i < first + width ? 255 : 0,
                    "Pixel " + i + ", vertical=" + vertical);
        }
    }

    private static int sampleAlpha(BufferedImage image, boolean vertical, int index) {
        return (vertical ? image.getRGB(index, 12) : image.getRGB(12, index)) >>> 24;
    }

    private static BufferedImage render(int width, double zoom, int aaScale, boolean vertical, boolean pixelHinting, double phase) {
        return render(width, zoom, aaScale, vertical, pixelHinting, phase, LINESTYLE2.NO_CAP, LINESTYLE2.NO_CAP);
    }

    private static BufferedImage render(int width, double zoom, int aaScale, boolean vertical, boolean pixelHinting, double phase, int startCap, int endCap) {
        LINESTYLE2 line = new LINESTYLE2();
        line.width = width;
        line.color = new RGBA(0, 0, 0, 255);
        line.pixelHintingFlag = pixelHinting;
        line.startCapStyle = startCap;
        line.endCapStyle = endCap;

        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = new FILLSTYLEARRAY();
        shape.fillStyles.fillStyles = new FILLSTYLE[0];
        shape.lineStyles = new LINESTYLEARRAY();
        shape.lineStyles.lineStyles2 = new LINESTYLE2[]{line};
        shape.shapeRecords = new ArrayList<>();

        StyleChangeRecord style = new StyleChangeRecord();
        style.stateMoveTo = true;
        style.stateLineStyle = true;
        style.lineStyle = 1;
        shape.shapeRecords.add(style);
        StraightEdgeRecord edge = new StraightEdgeRecord();
        edge.vertLineFlag = vertical;
        edge.deltaX = vertical ? 0 : 800;
        edge.deltaY = vertical ? 800 : 0;
        shape.shapeRecords.add(edge);
        shape.shapeRecords.add(new EndShapeRecord());

        double offset = (8 + phase) * SWF.unitDivisor;
        Matrix transform = Matrix.getTranslateInstance(offset, offset)
                .concatenate(Matrix.getScaleInstance(zoom));
        SerializableImage image = new SerializableImage(24, 24, BufferedImage.TYPE_INT_ARGB_PRE);
        BitmapExporter.export(ShapeTag.WIND_EVEN_ODD, 4, null, shape, null, image,
                zoom, transform, new Matrix(), null, true, true, aaScale);
        return image.getBufferedImage();
    }
}
