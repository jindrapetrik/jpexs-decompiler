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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ShapePaintsTest {

    @Test
    public void splittingStyleGroupsPreservesEveryFillArea() throws Exception {
        SWF swf;
        try (InputStream in = Files.newInputStream(Paths.get("testdata/xfl/multilayer-static-fills.swf"))) {
            swf = new SWF(in, false);
        }
        ShapeTag shape = (ShapeTag) swf.getCharacter(15);
        List<SHAPEWITHSTYLE> layers = ShapePaints.splitLayers(shape.getShapes(), shape.getShapeNum());
        assertEquals(layers.size(), 4);
        int style = 1;
        for (SHAPEWITHSTYLE layer : layers) {
            for (int i = 1; i <= layer.fillStyles.fillStyles.length; i++) {
                Area expected = new Area(PathExporter.exportFillStyle(shape.getWindingRule(), shape.getShapeNum(), swf, shape.getShapes(), style++));
                Area actual = new Area(PathExporter.exportFillStyle(shape.getWindingRule(), shape.getShapeNum(), swf, layer, i));
                expected.exclusiveOr(actual);
                assertTrue(expected.isEmpty(), "Keep the geometry of every style bank");
            }
        }
        assertEquals(style, 27);
    }

    @DataProvider(name = "shapeVersion")
    public Object[][] shapeVersion() {
        return new Object[][]{{3}, {4}};
    }

    @Test(dataProvider = "shapeVersion")
    public void styleChangesRetainThePenAndInheritedFillAndStroke(int shapeNum) {
        SHAPEWITHSTYLE source = SHAPEWITHSTYLE.createEmpty(shapeNum);
        source.shapeRecords.clear();
        source.fillStyles = fills(new RGBA(255, 0, 0, 255));
        source.lineStyles = lines(shapeNum, new RGBA(0, 0, 0, 255));
        StyleChangeRecord start = new StyleChangeRecord();
        start.stateMoveTo = true;
        start.moveDeltaX = 20;
        start.moveDeltaY = 30;
        start.stateFillStyle1 = true;
        start.fillStyle1 = 1;
        start.stateLineStyle = true;
        start.lineStyle = 1;
        source.shapeRecords.add(start);
        square(source, 10);

        // No move or style selection: the next group must inherit the old
        // red fill, black stroke and current pen position, despite a new bank.
        StyleChangeRecord bank = new StyleChangeRecord();
        bank.stateNewStyles = true;
        bank.fillStyles = fills(new RGBA(0, 0, 255, 255));
        bank.lineStyles = lines(shapeNum, new RGBA(255, 255, 255, 255));
        source.shapeRecords.add(bank);
        square(source, 5);
        StyleChangeRecord blue = (StyleChangeRecord) start.clone();
        blue.moveDeltaX = 40;
        source.shapeRecords.add(blue);
        square(source, 5);
        source.shapeRecords.add(new EndShapeRecord());

        List<SHAPEWITHSTYLE> layers = ShapePaints.splitLayers(source, shapeNum);
        assertEquals(layers.size(), 2);
        SHAPEWITHSTYLE second = layers.get(1);
        assertEquals(second.fillStyles.fillStyles.length, 2);
        assertEquals(second.fillStyles.fillStyles[0].color, source.fillStyles.fillStyles[0].color);
        assertEquals(second.fillStyles.fillStyles[1].color, bank.fillStyles.fillStyles[0].color);
        SWF swf = new SWF();
        assertEquals(PathExporter.exportFillStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, swf, second, 1).getBounds2D(),
                new Rectangle2D.Double(20, 30, 5, 5));
        assertEquals(PathExporter.exportFillStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, swf, second, 2).getBounds2D(),
                new Rectangle2D.Double(40, 30, 5, 5));
        assertEquals(PathExporter.exportLineStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, swf, second, 1).getBounds2D(),
                new Rectangle2D.Double(20, 30, 5, 5));
        assertEquals(PathExporter.exportLineStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, swf, second, 2).getBounds2D(),
                new Rectangle2D.Double(40, 30, 5, 5));
        assertTrue(bank.stateNewStyles, "Do not alter the source records");
        assertEquals(blue.fillStyle1, 1);
        assertEquals(blue.lineStyle, 1);
    }

    private static FILLSTYLEARRAY fills(RGBA color) {
        FILLSTYLE fill = new FILLSTYLE();
        fill.color = color;
        FILLSTYLEARRAY result = new FILLSTYLEARRAY();
        result.fillStyles = new FILLSTYLE[]{fill};
        return result;
    }

    private static LINESTYLEARRAY lines(int shapeNum, RGBA color) {
        LINESTYLEARRAY result = new LINESTYLEARRAY();
        if (shapeNum <= 3) {
            LINESTYLE line = new LINESTYLE();
            line.width = 2;
            line.color = color;
            result.lineStyles = new LINESTYLE[]{line};
        } else {
            LINESTYLE2 line = new LINESTYLE2();
            line.width = 2;
            line.color = color;
            result.lineStyles2 = new LINESTYLE2[]{line};
        }
        return result;
    }

    private static void square(SHAPEWITHSTYLE shape, int size) {
        for (int[] delta : new int[][]{{size, 0}, {0, size}, {-size, 0}, {0, -size}}) {
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.generalLineFlag = true;
            edge.deltaX = delta[0];
            edge.deltaY = delta[1];
            shape.shapeRecords.add(edge);
        }
    }
}
