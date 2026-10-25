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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.shapes.ShapeTransformer;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.xfl.shapefixer.CurvedEdgeRecordAdvanced;
import com.jpexs.decompiler.flash.xfl.shapefixer.ShapeFixer;
import com.jpexs.decompiler.flash.xfl.shapefixer.ShapeRecordAdvanced;
import com.jpexs.decompiler.flash.xfl.shapefixer.StraightEdgeRecordAdvanced;
import com.jpexs.helpers.Helper;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class XflShapeFixerTest {

    @Test
    public void nearbyCrossingsDoNotMergeWithExistingCorner() throws Exception {
        boolean previous = Configuration.flaExportFixShapes.get();
        try {
            Configuration.flaExportFixShapes.set(true);
            SWF swf;
            // Only shape 66 from 285.swf, without the other characters or scripts.
            try (InputStream in = Files.newInputStream(Paths.get("testdata/xfl/close-intersections.swf"))) {
                swf = new SWF(in, false);
            }
            ShapeTag source = (ShapeTag) swf.getCharacter(66);
            SHAPEWITHSTYLE shape = Helper.deepCopy(source.getShapes());
            ShapeTransformer transformer = new ShapeTransformer();
            transformer.transformShapeRecords(Matrix.getScaleInstance(20), shape.shapeRecords, source.getShapeNum());
            List<ShapeRecordAdvanced> fixed = new ShapeFixer().fix(shape.shapeRecords, source.getShapeNum(),
                    shape.fillStyles, shape.lineStyles, true);
            Map<Point2D, Integer> degree = new HashMap<>();
            double x = 0;
            double y = 0;
            for (ShapeRecordAdvanced record : fixed) {
                double nextX = record.changeX(x);
                double nextY = record.changeY(y);
                if (record instanceof StraightEdgeRecordAdvanced || record instanceof CurvedEdgeRecordAdvanced) {
                    degree.merge(new Point2D.Double(x, y), 1, Integer::sum);
                    degree.merge(new Point2D.Double(nextX, nextY), 1, Integer::sum);
                }
                x = nextX;
                y = nextY;
            }
            assertEquals(degree.get(new Point2D.Double(-1660, 2580)), Integer.valueOf(2),
                    "The original corner must not absorb the nearby crossing; CS6 loses the main blue fill otherwise");
            int nearbyCrossings = 0;
            for (Map.Entry<Point2D, Integer> entry : degree.entrySet()) {
                double distance = entry.getKey().distance(-1660, 2580);
                if (distance > 0 && distance < 1) {
                    assertEquals(entry.getValue(), Integer.valueOf(4), "Both curves must use the same intersection coordinate");
                    nearbyCrossings++;
                }
            }
            assertEquals(nearbyCrossings, 3, "Keep all three crossings separate from their shared endpoint");
            assertEquals(source.getShapes().shapeRecords.size(), 115, "Do not modify the source SWF");
        } finally {
            Configuration.flaExportFixShapes.set(previous);
        }
    }
}
